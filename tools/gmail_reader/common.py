"""Shared helpers for Shadow's read-only Gmail reader."""
import os
from pathlib import Path

# The ONLY permission this tool ever asks Google for. Google enforces it:
# with this scope, sending, deleting, labelling or changing mail is impossible.
SCOPES = ["https://www.googleapis.com/auth/gmail.readonly"]


def secrets_dir() -> Path:
    """Where credentials live: outside the repo, never committed."""
    default = Path(os.environ.get("LOCALAPPDATA", Path.home())) / "shadow-secrets" / "gmail"
    path = Path(os.environ.get("SHADOW_GMAIL_DIR", default))
    path.mkdir(parents=True, exist_ok=True)
    return path


def credentials_path() -> Path:
    return secrets_dir() / "credentials.json"


def token_path() -> Path:
    return secrets_dir() / "token.json"


def load_credentials():
    """Load the saved token, refresh it if needed, and refuse anything broader than read-only."""
    from google.auth.transport.requests import Request
    from google.oauth2.credentials import Credentials

    if not token_path().exists():
        raise RuntimeError("Gmail is not connected yet. Run auth.py once to sign in.")
    creds = Credentials.from_authorized_user_file(str(token_path()), SCOPES)
    granted = set(creds.scopes or [])
    if granted and granted != set(SCOPES):
        raise RuntimeError(f"Token has unexpected permissions {sorted(granted)}; refusing. "
                           "Delete token.json and run auth.py again.")
    if not creds.valid:
        if creds.expired and creds.refresh_token:
            creds.refresh(Request())
            token_path().write_text(creds.to_json(), encoding="utf-8")
        else:
            raise RuntimeError("Gmail sign-in expired. Run auth.py again.")
    return creds


def gmail_service():
    from googleapiclient.discovery import build
    return build("gmail", "v1", credentials=load_credentials(), cache_discovery=False)
