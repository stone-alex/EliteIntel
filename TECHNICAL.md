# EliteIntel - Technical Architecture

> This document is intended for developers and engineers interested in the architecture and
> design decisions behind EliteIntel. For user documentation, see the [Wiki](https://github.com/stone-alex/EliteIntel/wiki).

---

## Overview

EliteIntel is a Java 21 desktop application providing real-time AI-assisted voice interaction
for the game *Elite Dangerous*. It ingests live game telemetry, routes natural-language voice
commands through an LLM inference pipeline, and executes keyboard-mapped ship controls - all
while remaining fully compliant with Frontier Developments' Terms of Service.

The application runs on Linux and Windows, supports fully offline operation, and has real users
in a growing community.

---

## Core Architecture

### Event-Driven, Decoupled Design

The application is built around a central event registry using **reflection-based handler
discovery**. Components register interest in event types at startup; at runtime they communicate
exclusively via events with no direct inter-component dependencies.

This means the journal parser, AI inference pipeline, voice I/O layer, and ship control
dispatcher are entirely decoupled. Adding a new capability - a new command type, a new data
source, a new provider - requires no changes to existing components.

### Multithreaded Pipeline

Audio capture, STT transcription, LLM inference, TTS synthesis, and game control dispatch
run on independent threads. Cloud STT is optimized for response and performance, the audio is
streamed to provider from the moment a mic gate opens to the moment it closes the server
begins processing the audio before the user finishes the utterance.

In Professional mode (LLM temperature 0.1), round-trip latency for a simple ship command
such as `deploy_landing_gear` is approximately 750ms over a typical cloud connection.

---

## AI Provider Abstraction

### Pluggable Provider Pattern

All LLM providers implement a common interface. The active provider is selected at runtime
from configuration with no changes to the inference pipeline or command handling logic.

Supported providers:

| Provider         | Type            | Notes                                |
|------------------|-----------------|--------------------------------------|
| Ollama           | Local / offline | Runs on user hardware, zero API cost |
| Anthropic Claude | Cloud           | Sonnet / Haiku                       |
| xAI Grok         | Cloud           | Fast non-reasoning model             |
| OpenAI ChatGPT   | Cloud           | GPT-4 class                          |

### Dual-Model Local Inference

When running against Ollama, the application uses **two separate models** for different
cognitive loads:

- **Command model** (e.g. `tulu3:8b`) - intent classification and ship control dispatch.
  Smaller, faster, lower temperature. Optimised for latency on simple commands.
- **Query model** (e.g. `qwen2.5:14b`) - data analysis, exploration summaries, multi-step
  reasoning. Larger model tolerates higher latency in exchange for reasoning quality.

This split avoids over-provisioning the command path with an expensive model while ensuring
the analysis path has sufficient capacity.

### Temperature as a Design Parameter

LLM temperature is a first-class configuration value tied to the user-selectable personality
mode, not a hidden constant:

| Personality  | Temperature | Behaviour                                   |
|--------------|-------------|---------------------------------------------|
| Professional | 0.1         | Deterministic, low-latency, concise         |
| Friendly     | 0.5         | Conversational, moderate creativity         |
| Unhinged     | 0.8         | High creativity, tangential responses       |
| Rogue        | 1.0         | Maximum creativity, slowest, most expensive |

---

## Voice Pipeline

### STT (Speech-to-Text)

Two STT backends are supported behind a common interface:

- **Whisper** (local, offline) - bundled with the installer, no API key required
- **Google Cloud STT** (cloud) - higher accuracy, especially on domain-specific vocabulary

A **STT correction dictionary** handles domain-specific vocabulary that general-purpose
STT models misrecognise. *Elite Dangerous* has hundreds of commodity and system names that
are phonetically ambiguous. The dictionary supports user-extensible entries in the form
`"misrecognised_word"="intended_term"`, allowing the community to contribute corrections.

### TTS (Text-to-Speech)

- **Piper TTS** (local, offline) - runs as a local HTTP server on port 5000, integrated via
  REST. No API key, no cloud dependency, sub-100ms synthesis latency on modern hardware.
- **Google Cloud TTS** (cloud) - 14 voices across British, American, and Australian accents,
  selected at runtime via voice command.

---

## Game Data Pipeline

### Journal File Parsing

*Elite Dangerous* exposes game state via a structured JSON journal file that is appended to
in real time. EliteIntel tails this file and parses events as they are written, maintaining
a session state model that the LLM can query.

No memory reading, no overlay injection, no game client modification. All data access is
through the officially documented third-party API.

### Session State Model

The application maintains an in-memory session state across the following domains:

- Current star system, body, and coordinates
- FSS / DSS scan results for the current system
- Exobiology codex entries and sample locations (persisted across system exits and returns)
- Active missions (pirate massacre kill counts, target factions)
- Fleet carrier status (fuel, credits, jump destination)
- Cargo manifest
- Ship loadout

The LLM receives only the subset of session state relevant to the current query, not the
full session context, keeping token usage minimal.

### EDSM Integration

For data not available in the local journal (historical market prices, third-party system
data), the application queries the EDSM API. Results are cached in the session to avoid
redundant network calls.

---

## Security and Privacy

### API Key Storage

API keys for cloud providers are stored **encrypted in a local SQLite database**. Keys are
never logged, never transmitted except in request headers to the configured provider
endpoint, and never leave the user's machine in any other form.

### Streaming Mode / Privacy Mode

- **Streaming mode** - the application ignores all audio input unless prefixed with a
  configurable wake word (default: "Computer"). STT still transcribes continuously in this
  mode, which has token cost implications the user is informed of.
- **Privacy mode** - STT is disabled completely. No audio is captured or transmitted.
  Cannot be toggled by voice command; requires manual UI interaction to prevent accidental
  re-activation.

### No Game Memory Access

The application does not read game client memory, inject into the game process, or use
overlays. All data is sourced from the journal file API or EDSM. This is a hard architectural
constraint, not an implementation choice - it is what makes the application TOS-compliant.

---

## Build and Distribution

- **Language**: Java 21
- **Build**: Gradle 9 with Gradle Wrapper
- **Packaging**: Fat JAR (`app-shadow.tar`) via Shadow plugin
- **Linux installer**: Bash script, no `sudo` required, installs to `~/.var/app/elite.intel.app`
- **Windows installer**: NSIS-based, standard next-next-done
- **Build from source**: `sh gradlew clean build` - requires Java 21 and Gradle Wrapper 8.7+

Local STT (Whisper) is bundled with the installer. Local TTS (Piper) and local LLM (Ollama)
are optional components installed separately via documented procedures.

---

## Design Principles

**DRY and SRP throughout.** New provider implementations follow the existing pattern - the
abstraction layer was designed from the first provider with extension in mind, not retrofitted
after the fact.

**Offline-first.** Every cloud dependency has a local alternative. A user with sufficient
hardware can run the entire stack - STT, LLM, TTS - with no internet connection and no
ongoing API cost.

**TOS compliance as a hard constraint.** The architecture was designed around what the
official API exposes, not around what would be technically possible with memory access.
This is not a limitation - it is what makes the application distributable and trustworthy.

**Minimal UI surface.** Three API key fields, a handful of buttons, everything else via
voice. The interface does not compete with the game for screen real estate.

---

## Repository

[github.com/stone-alex/EliteIntel](https://github.com/stone-alex/EliteIntel)

Community: [Matrix #krondor:matrix.org](https://matrix.to/#/#krondor:matrix.org)
