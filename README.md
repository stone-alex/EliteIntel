# Elite Intel - Public Beta

## Installation & Setup

### ⚙️ [INSTALL](https://github.com/stone-alex/EliteIntel/wiki/Installation-and-Configuration) | 🎓 [**WIKI**](https://github.com/stone-alex/eliteintel/wiki) | 💻[TECH STUFF](https://github.com/stone-alex/EliteIntel/blob/master/TECHNICAL.md)

--- 

## Linux and Windows. Can be ran 100% off-line.

---

Latest [release notes](https://github.com/stone-alex/EliteIntel/blob/master/distribution/release-notes.md)

**✅ The off-line TTS. (Linux and Windows)**
Powered by Kokoro. built-in / included.

**✅ The off-line STT. (Linux and Windows)**
Powered by NVIDIA Parakeet. built-in / included.

**✅ Local off-line private LLMs are now supported**
[Installing local LLM](https://github.com/stone-alex/EliteIntel/wiki/installing-local-llms)

### Minimum requirements for **running the game and the LLM on the same GPU**
- NVIDIA RTX 3090 **24GB VRAM** or equivalent.

- RTX 5090 with only **12GB VRAM will not do**. Not enough VRAM.
    - Game takes 9GB
    - LLM takes 6.5GB

- If you do not have the hardware to run your local LLM you can use cloud LLM from Claude, xAI, Google, or Open AI but
  that comes with a small fee. (about $6 a month)
  [**Real life Cloud LLM cost breakdown**](https://github.com/stone-alex/EliteIntel/wiki/LLM-Real-Live-Cost)
  

---

### 🧠 LLMs (Larger Language Models)

- **LMStudio (Local off-line Model matrixportalx/tulu-3.1-8b-supernova)**
- **Ollama (Local off-line Model matrixportalx/tulu-3.1-8b-supernova)**
(Why tulu-3.1-8b Supernova specifically)[https://github.com/stone-alex/EliteIntel/wiki/Why-Tulu3.1-supernova]

- Claude (Cloud. Models: Sonnet/Haiku)
- Grok (Cloud model xAI grok-4-1-fast-non-reasoning)
- Open AI (Cloud Chat GPT gpt-5.2)
- Google Gemini (Cloud Generative Language API gemini-2.5-flash)
- Deepseek deepseek-v4-flash

### 🎤 STT (Speech To Text)

- **NVIDIA Parakeet (Local - off-line)**

### 🔊 TTS (Text To Speech)

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
