"""Shadow phone-inbox receiver.

Listens on 127.0.0.1 only. Your phone reaches it through `tailscale serve` (HTTPS, tailnet-only).
POST /events   Authorization: Bearer <SHADOW_PHONE_SECRET>   body: {"events": [ ... ]}
GET  /health
Important events are forwarded to your Telegram as plain alerts (no AI, no tokens).
"""
import hmac
import json
import os
import re
import sys
from pathlib import Path

import httpx
import uvicorn
from starlette.applications import Starlette
from starlette.requests import Request
from starlette.responses import JSONResponse
from starlette.routing import Route

import rules
import store

MAX_EVENTS_PER_POST = 200
MAX_FIELD = 4000


def load_env():
    """Read our own .env next to this file, then fall back to Hermes's .env for the Telegram bot."""
    env = {}
    for path in (Path(__file__).with_name(".env"),
                 Path(os.environ.get("LOCALAPPDATA", "")) / "hermes" / ".env"):
        if path.exists():
            for line in path.read_text(encoding="utf-8-sig", errors="replace").splitlines():
                m = re.match(r"\s*([A-Z0-9_]+)\s*=\s*(.*)\s*$", line)
                if m and not line.lstrip().startswith("#"):
                    env.setdefault(m.group(1), m.group(2).strip().strip('"').strip("'"))
    for k, v in os.environ.items():
        if k.startswith(("SHADOW_", "TELEGRAM_")):
            env[k] = v
    return env


ENV = load_env()
SECRET = ENV.get("SHADOW_PHONE_SECRET", "")
BOT_TOKEN = ENV.get("TELEGRAM_BOT_TOKEN", "")
CHAT_ID = ENV.get("SHADOW_ALERT_CHAT_ID") or (ENV.get("TELEGRAM_ALLOWED_USERS", "").split(",")[0].strip())


def authorized(request: Request) -> bool:
    if not SECRET:
        return False
    header = request.headers.get("authorization", "")
    return hmac.compare_digest(header, f"Bearer {SECRET}")


def clean(value, limit=MAX_FIELD):
    return str(value or "")[:limit]


async def send_alert(ev: dict):
    if not (BOT_TOKEN and CHAT_ID):
        return
    icon = {"meeting": "📅", "money": "💸"}.get(ev["category"], "🔔")
    text = f"{icon} {ev['app']}: {ev['title']}\n{ev['text']}"[:1500]
    try:
        async with httpx.AsyncClient(timeout=10) as client:
            await client.post(f"https://api.telegram.org/bot{BOT_TOKEN}/sendMessage",
                              json={"chat_id": CHAT_ID, "text": text, "disable_web_page_preview": True})
    except httpx.HTTPError:
        pass  # alert is best-effort; the event is already stored for Shadow


async def events(request: Request):
    if not authorized(request):
        return JSONResponse({"error": "unauthorized"}, status_code=401)
    try:
        body = await request.json()
        items = body.get("events", [])
        assert isinstance(items, list)
    except Exception:
        return JSONResponse({"error": "bad json"}, status_code=400)

    stored = 0
    db = store.connect()
    try:
        for raw in items[:MAX_EVENTS_PER_POST]:
            if not isinstance(raw, dict) or not raw.get("id"):
                continue
            ev = {
                "id": clean(raw["id"], 128),
                "posted_at": float(raw["posted_at"]) / 1000 if raw.get("posted_at") else None,
                "app": clean(raw.get("app"), 80),
                "package": clean(raw.get("package"), 200),
                "title": rules.redact_otp(clean(raw.get("title"), 500)),
                "text": rules.redact_otp(clean(raw.get("text"))),
            }
            ev["category"], flagged = rules.classify(ev["app"], ev["title"], ev["text"])
            ev["flagged"] = int(flagged)
            if store.insert(db, ev):
                stored += 1
                if flagged:
                    await send_alert(ev)
        store.prune(db)
    finally:
        db.close()
    return JSONResponse({"ok": True, "stored": stored, "received": len(items)})


async def health(request: Request):
    return JSONResponse({"ok": True, "secret_set": bool(SECRET), "alerts": bool(BOT_TOKEN and CHAT_ID)})


app = Starlette(routes=[Route("/events", events, methods=["POST"]), Route("/health", health)])

if __name__ == "__main__":
    # pythonw (hidden window) has no console; send output to a log file instead of crashing.
    if sys.stdout is None or sys.stderr is None:
        _log = open(store.data_dir() / "receiver.log", "a", buffering=1, encoding="utf-8")
        sys.stdout = sys.stdout or _log
        sys.stderr = sys.stderr or _log
    if not SECRET:
        raise SystemExit("SHADOW_PHONE_SECRET is not set. Run setup.ps1 first.")
    port = int(ENV.get("SHADOW_PHONE_PORT", "8645"))
    uvicorn.run(app, host="127.0.0.1", port=port, log_level="warning")
