"""One-time Gmail sign-in for Shadow (read-only).

    python auth.py          sign in (opens your browser)
    python auth.py --check  confirm it works and show your 3 newest email subjects
"""
import sys

from common import SCOPES, credentials_path, gmail_service, token_path


def sign_in():
    from google_auth_oauthlib.flow import InstalledAppFlow

    if not credentials_path().exists():
        sys.exit(f"Missing {credentials_path()}\n"
                 "Download the OAuth client file from Google Cloud and save it there as credentials.json.")
    flow = InstalledAppFlow.from_client_secrets_file(str(credentials_path()), SCOPES)
    creds = flow.run_local_server(port=0, open_browser=True)
    token_path().write_text(creds.to_json(), encoding="utf-8")
    print(f"Signed in. Token saved to {token_path()}")


def check():
    svc = gmail_service()
    profile = svc.users().getProfile(userId="me").execute()
    print(f"Connected (read-only) as {profile['emailAddress']}")
    msgs = svc.users().messages().list(userId="me", maxResults=3).execute().get("messages", [])
    for m in msgs:
        meta = svc.users().messages().get(userId="me", id=m["id"], format="metadata",
                                          metadataHeaders=["Subject"]).execute()
        subject = next((h["value"] for h in meta["payload"].get("headers", [])
                        if h["name"].lower() == "subject"), "(no subject)")
        print(f"  - {subject}")


if __name__ == "__main__":
    if "--check" in sys.argv:
        check()
    else:
        sign_in()
        check()
