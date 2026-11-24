"""SQLite storage for phone notification events (shared by receiver and MCP server)."""
import os
import sqlite3
import time
from pathlib import Path

RETENTION_DAYS = 30

SCHEMA = """
CREATE TABLE IF NOT EXISTS events (
    id TEXT PRIMARY KEY,          -- sent by the phone; makes uploads idempotent
    received_at REAL NOT NULL,
    posted_at REAL,
    app TEXT,                      -- human app name, e.g. "Messenger"
    package TEXT,
    title TEXT,
    text TEXT,
    category TEXT,                 -- meeting | money | other
    flagged INTEGER DEFAULT 0      -- 1 = important, was alerted
);
CREATE INDEX IF NOT EXISTS idx_events_time ON events(received_at);
"""


def data_dir() -> Path:
    default = Path(os.environ.get("LOCALAPPDATA", Path.home())) / "shadow-data" / "phone_inbox"
    path = Path(os.environ.get("SHADOW_PHONE_DIR", default))
    path.mkdir(parents=True, exist_ok=True)
    return path


def connect() -> sqlite3.Connection:
    db = sqlite3.connect(data_dir() / "events.db", timeout=10)
    db.row_factory = sqlite3.Row
    db.executescript(SCHEMA)
    return db


def insert(db, ev: dict) -> bool:
    """Insert one event; returns False if it was already stored."""
    cur = db.execute(
        "INSERT OR IGNORE INTO events (id, received_at, posted_at, app, package, title, text, category, flagged) "
        "VALUES (?,?,?,?,?,?,?,?,?)",
        (ev["id"], time.time(), ev.get("posted_at"), ev.get("app"), ev.get("package"),
         ev.get("title"), ev.get("text"), ev.get("category", "other"), int(ev.get("flagged", 0))))
    db.commit()
    return cur.rowcount == 1


def prune(db):
    db.execute("DELETE FROM events WHERE received_at < ?", (time.time() - RETENTION_DAYS * 86400,))
    db.commit()


def query(db, since_hours=24.0, app=None, flagged_only=False, limit=50):
    sql = "SELECT * FROM events WHERE received_at >= ?"
    args = [time.time() - float(since_hours) * 3600]
    if app:
        sql += " AND lower(app) = lower(?)"
        args.append(app)
    if flagged_only:
        sql += " AND flagged = 1"
    sql += " ORDER BY COALESCE(posted_at, received_at) DESC LIMIT ?"
    args.append(int(limit))
    return [dict(r) for r in db.execute(sql, args).fetchall()]
