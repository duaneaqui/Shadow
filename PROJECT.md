# Shadow — Project Brief

> The single source of truth for what Shadow is. Every builder (human, Claude, Codex) reads this first.

## Vision
Shadow is a **self-hosted personal AI agent** — a Jarvis-style assistant that is always around,
controlled by voice across the owner's Android phone, Windows laptop and home devices.
It remembers everything about the owner (stored on the owner's own laptop), learns his
personality, slowly develops its own, and helps run his life and online business —
like a best friend plus a whole team.

It is **not** an operating system. It is an AI layer that sits on top of every device's OS.

## Architecture (hub and spokes)

```
                 ┌──────────────────────────────┐
                 │  HUB — Shadow's brain        │
                 │  Hermes Agent (Nous Research)│
                 │  on the Windows laptop       │
                 │  + small always-on server    │
                 └──────────────┬───────────────┘
       memory: Obsidian vault "Shadow-Mind" (Markdown, on laptop, NOT in this repo)
                                │
   ┌──────────────┬─────────────┼──────────────┬──────────────────┐
   │ Telegram     │ Android app │ Windows      │ Home Assistant   │
   │ (phase 1     │ (assistant, │ companion    │ (TV, smart home) │
   │  chat/voice) │  wake word) │ (Spotify,    │                  │
   │              │             │  Discord)    │                  │
   └──────────────┴─────────────┴──────────────┴──────────────────┘
```

### Models
| Role | Model | Why |
|---|---|---|
| Everyday + private brain (80–90% of work) | Local model via Ollama on RTX 2050 (4 GB VRAM) — 3–4B class | Free, private |
| Hard thinking | ChatGPT subscription via "Sign in with ChatGPT" | Already paid for; officially allowed in own tools |
| Public, non-personal tasks only | Gemini free tier | Free — but trains on prompts outside EU/UK, so never personal data |
| Ears / voice / wake word / memory search | Hugging Face: Whisper, Kokoro or Piper, openWakeWord, multilingual embeddings | Local, swappable |

Claude Pro is a **builder** (Claude Code), not Shadow's runtime brain — Anthropic does not allow
subscription logins to power third-party agents.

### Owner hardware
- Laptop: Windows, 32 GB RAM, RTX 2050 (4 GB VRAM)
- Phone: Android (brand/model TBD)
- TV: TBD
- Always-on server: Oracle Cloud Always Free (now 2 OCPU / 12 GB ARM) — optional, later

## Features (target)
1. **Voice control** — "Shadow, play X on Spotify", "lower it 20%", "join meeting room 1 in work Discord", "mute".
2. **Briefings** — news from owner-approved sources, lifestyle-relevant updates.
3. **Email watch** — read-only Gmail; e.g. "Sir, Spotify renews in 3 days. Pay it?"
4. **Business inbox** — Facebook/Instagram DMs → Shadow tells owner → owner answers by voice → Shadow sends.
5. **Content factory** — AI promo videos (cloud-generated) → notify → owner approves by voice → post.
6. **Market alerts** — stock/crypto prices and chart signals as *information*. Never trades, never says "buy".
7. **TV and home** — via Home Assistant: "open Netflix on the TV and play One Piece episode 1000".
8. **Self-improvement** — nightly reflection, evolving persona, weekly check-in with owner, scam-detection skill.
9. **Upgrade scouting** — weekly scan of Hugging Face/GitHub for better parts; test in sandbox; owner approves.

## Ground rules (non-negotiable)
- **Payments:** Shadow prepares everything and opens the real payment page; owner confirms on the
  bank/merchant screen with fingerprint. Shadow never stores or enters card numbers, never reads OTPs,
  never moves money. Recurring bills use each service's own auto-renew on a low-limit card.
- **Ask first** before posting publicly, messaging other people, or anything irreversible.
- **Reader/doer split:** untrusted text (emails, DMs, web pages, repo READMEs) is read by a tool-less
  "reader"; the tool-using agent only sees the reader's summary.
- **Hermes locked down:** terminal/command execution off; only the tools Shadow needs.
- **Secrets** (API keys, Wi-Fi/router passwords, tokens) live in a vault/.env that tools use and the
  model never sees. Never committed to git.
- **Self-improvement:** learning, memory, persona and skills — free. Installing code/models, new tools,
  new permissions, changing core rules — sandbox test + owner approval. Over time owner may pre-approve categories.
- **Hugging Face safety:** only `.safetensors` / `.gguf` from known orgs; never "trust remote code";
  no pickle `.bin` from unknown uploaders.
- **Private data never** goes to free AI tiers that train on prompts.
- **Custom programs** fill gaps platforms leave open (Discord, Android assistant, TV). They never
  get around security protections (bank OTPs, 2FA, platform anti-automation rules).

## Out of scope
iPhone/Apple, a real operating system, self-copying onto devices via USB, local video generation,
automatic trading or payments, Discord self-bots.

## Build order
| Phase | What | Status |
|---|---|---|
| 0 | Repo + project memory files | ✅ |
| 1 | Install tools, Hermes as "Shadow", Telegram, Obsidian memory, lockdown, persona | ⏳ |
| 2 | Briefings: Gmail read-only, news, subscription reminders, price alerts | |
| 3 | Windows companion: wake word, Spotify, Discord | |
| 4 | Android app: default assistant, "Hey Shadow", voice | |
| 5 | Business: DM inbox, content pipeline, reader/doer split | |
| 6 | Home Assistant + TV | |
| 7 | Fine-tune personality on free cloud GPUs; more devices | |

## Open questions
- Phone brand/model (affects background-app killing)
- TV brand (affects how much control is possible)
- Which local model to standardize on (decide after testing in phase 1)
