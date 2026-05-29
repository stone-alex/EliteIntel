# Elite Intel

## Installation & Setup

### ⚙️ [INSTALL](https://github.com/stone-alex/EliteIntel/wiki/Installation-and-Configuration) | 🎓 [**WIKI**](https://github.com/stone-alex/eliteintel/wiki) | 💻[TECH STUFF](https://github.com/stone-alex/EliteIntel/blob/master/TECHNICAL.md)

--- 

## Linux and Windows.
### Can be ran 100% off-line or with various cloud services

---

## Windows 🪟
1. Download the [👉**installer**👈](https://github.com/stone-alex/EliteIntel/releases).
2. Run the installer and follow the on-screen prompts.
   - **Parakeet STT** (local speech recognition) and **Kokoro TTS** (local text-to-speech) are both included. No additional steps or services are required.
---

## Linux 🐧
### Installation (no sudo required)
Do not use zip file from the artifacts. That is for updates.

1. Download the installer script:
```shell
curl -L -o installer.sh https://raw.githubusercontent.com/stone-alex/EliteIntel/refs/heads/master/distribution/installer.sh
```

2. Make the script executable and run it:
```shell
chmod +x installer.sh
./installer.sh
```
The app installs to `~/.var/app/elite.intel.app`.
Both **Parakeet STT** and **Kokoro TTS** are bundled with the app. No additional installation is needed. Enable them in the app via the **Settings tab ☑ Use** checkboxes.

Setup complete. See [**Configure the app**](UI-and-Configuration-Options) for next steps.

- If you do not have the hardware to run your local LLM you have choices.
## 👉Free cloud (if you don't have hardware for local LLM)👈
[**Mistral**](https://console.mistral.ai/home) (some limitations may apply)

---

## Set up an LLM. Two options are available:
   - **Local LLM** (free, offline): See the [**Local LLM guide**](installing-local-llms). Requires capable GPU hardware.
   - **Cloud LLM** (easier to set up): See the [**Configure the app**](UI-and-Configuration-Options) guide for API key setup.

Latest [release notes](https://github.com/stone-alex/EliteIntel/blob/master/distribution/release-notes.md)

**✅ The off-line TTS. (Linux and Windows)**
Powered by Kokoro. built-in / included.

**✅ The off-line STT. (Linux and Windows)**
Powered by NVIDIA Parakeet. built-in / included.

**✅ Local off-line private LLMs are now supported**
[Installing local LLM](https://github.com/stone-alex/EliteIntel/wiki/installing-local-llms)

- If you do not have the hardware to run your local LLM you have choices.
## Free cloud (if you don't have hardware for local LLM)
[**Mistral**](https://console.mistral.ai/home). (some limitations may apply)

Paid cloud services: Claude, xAI, Gemini, Deepseek, or Open AI. The cost will very depending on which one you choose.

---

## Minimum requirements for **running the game and the LLM on the same GPU**
- NVIDIA RTX 3090 **24GB VRAM** or equivalent.

- RTX 5090 with only **12GB VRAM will not do**. Not enough VRAM.
    - Game takes 8GB (or more)
    - LLM takes 6.5GB





---

## 🧠 LLMs (Larger Language Models)

### Local

- **LMStudio (Local off-line Model matrixportalx/tulu-3.1-8b-supernova)**
- **Ollama (Local off-line Model matrixportalx/tulu-3.1-8b-supernova)**

[Why tulu-3.1-8b Supernova specifically](https://github.com/stone-alex/EliteIntel/wiki/Why-Tulu3.1-supernova)

### Cloud Paid
- Claude | claude-haiku-4-5
- Grok | grok-4-1-fast-non-reasoning
- Open AI | gpt-5.2
- Google Gemini | gemini-3.1-flash-lite
- Deepseek | deepseek-v4-flash

### Cloud Free
- Mistral | mistral-small-2506

**Mistral is a cloud AI that allows you to run this app for free, but there is a limit on tokens per hour.
[Create KEY in Mistral Console](https://console.mistral.ai/home) only email required, no credit card.**

---

## 🎤 STT (Speech To Text)

- **NVIDIA Parakeet (Local - off-line)**

## 🔊 TTS (Text To Speech)

- **Kokoro (Local - off-line)**
- Google (Cloud)

## Overview

Elite Intel is your AI-powered, sidekick for *Elite Dangerous*.
It's a Quality of Life (QoL) app that enhances gameplay with voice-activated commands 
**uttered in natural language**,
real-time journal parsing, and smart AI assistance-all while staying fully compliant
with Frontier Developments' Terms of Service. No AFK play, and no unfair advantages;
everything requires your input.

The app uses Speech-to-Text (STT) and Text-to-Speech (TTS) for **natural voice interactions**,
and LLM for handling commands, queries, and chats. It processes game data from journal 
and auxiliary files, pulls in info from EDSM when needed, and keeps things responsive.

---

## Contributing

Fork it and submit pull requests! Keep things modular and event-driven, and test your changes. For dev
details, check out `DEVELOPERS.md` in the root. It covers the architecture (decoupled, multithreaded, event registry via
reflection) and principles like DRY and SRP. All PRs go through review.

## License

The project is open-source under a [Creative Commons license CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en),
so feel free to check out the code and contribute. (see [DEVELOPERS.md](DEVELOPERS.md) for details)

## Contact Developer / Join the Community
If you run into issues, hit us up on GitHub issues or Feedback and bug reports are super welcome!
To get in touch: 👉[**Matrix**](https://matrix.to/#/#krondor:matrix.org)👈
