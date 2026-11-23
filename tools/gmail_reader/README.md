# Shadow Gmail reader (read-only)

A tiny MCP server that lets Shadow **search and read** Gmail — nothing else.

- Asks Google for exactly one permission: `gmail.readonly`. Google itself blocks
  sending, deleting, labelling or changing mail.
- Refuses to run if its token ever has broader permissions.
- Wraps every result as **UNTRUSTED EMAIL CONTENT** so Shadow treats email text as
  information, never as instructions.
- Credentials live in `%LOCALAPPDATA%\shadow-secrets\gmail\` — outside this repo.

## Tools
| Tool | What it does |
|---|---|
| `search_emails(query, max_results)` | Gmail search syntax (`from:openai.com newer_than:30d`, `subject:invoice`). Max 25. |
| `read_email(message_id)` | Full text of one email (8,000 chars max). |

## Setup
1. `powershell -ExecutionPolicy Bypass -File setup.ps1`
2. Google Cloud: create project → enable Gmail API → OAuth consent screen (External, add yourself as test user)
   → Credentials → OAuth client ID → **Desktop app** → download JSON → save as
   `%LOCALAPPDATA%\shadow-secrets\gmail\credentials.json`.
3. `.venv\Scripts\python.exe auth.py` — sign in, approve **read-only** access.
4. Add `hermes-config-snippet.yaml` to Hermes `config.yaml`, then `hermes mcp test gmail_reader`.

## Revoke anytime
Google Account → Security → Third-party connections → remove the app, and delete `token.json`.
