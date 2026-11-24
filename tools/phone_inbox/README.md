# Shadow phone inbox

Receives notifications forwarded by the **Shadow Companion** Android app and lets Shadow read them.

```
Phone app ──HTTPS over Tailscale (tailnet only)──► tailscale serve ──► 127.0.0.1:8645 receiver.py
                                                                         │ SQLite (30 days)
                                     Telegram alert (no AI) ◄── important ┤
                                     Shadow (MCP: list_phone_notifications) ◄┘
```

- Listens on `127.0.0.1` only; `tailscale serve` makes it reachable from your own devices, with HTTPS.
- Phone must send `Authorization: Bearer <SHADOW_PHONE_SECRET>` (in `.env`, never committed).
- OTP-looking codes are replaced with `[code hidden]` (the phone app also filters them).
- Rules (no AI) flag meeting changes and due/deducted payments → plain Telegram alert, zero tokens.
- Shadow reads everything via the `list_phone_notifications` tool, wrapped as UNTRUSTED content.
  Notifications never trigger Shadow to act on their own.

Data: `%LOCALAPPDATA%\shadow-data\phone_inbox\events.db`

## Files
| File | Purpose |
|---|---|
| `receiver.py` | HTTP endpoint (`POST /events`, `GET /health`) |
| `rules.py` | OTP redaction + importance rules |
| `store.py` | SQLite storage |
| `mcp_server.py` | Shadow's read-only tool |
| `setup.ps1` | venv, secret, auto-start at login |
| `show-secret.ps1` | display the secret to type into the phone app |
