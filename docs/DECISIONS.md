# Decisions log

| Date | Decision | Why |
|---|---|---|
| 2026-11-24 | Name the agent **Shadow** | Owner's choice |
| 2026-11-24 | Build on **Hermes Agent** instead of a custom brain | Already has memory, self-learning loop, skills, cron, Telegram, MCP, local models |
| 2026-11-24 | Memory in an **Obsidian vault outside the repo** | Owner can read/edit/veto Shadow's mind; private data never reaches GitHub |
| 2026-11-24 | Android + Windows only, no Apple | iOS blocks third-party always-on assistants |
| 2026-11-24 | Local model default; ChatGPT sign-in for hard thinking | Low cost; Claude Pro can't legally power third-party agents |
| 2026-11-24 | Payments: prepare + owner fingerprint confirm; never cards/OTPs | One tricked payment is unrecoverable; banks treat shared OTPs as owner liability |
| 2026-11-24 | Single agent + background workers; add reader/doer split at phase 5 | Simpler, cheaper, debuggable; split needed once untrusted text arrives |
| 2026-11-24 | Self-improvement: learning free, installs gated by sandbox + approval | Malicious repos/models can target agents |
