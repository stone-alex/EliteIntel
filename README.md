# Elite Companion App

## Overview

Elite Companion is a Quality of Life (QoL) companion app for *Elite Dangerous*, built in pure Java. It enhances gameplay with voice-activated commands, real-time journal parsing, and AI-driven assistance, fully compliant with Frontier Developments' TOS. No AFK play or automation—user input is
required for all actions.

This app uses Google STT/TTS for voice interaction and xAI's Grok for intelligent responses, processing game journal files (`elite.companion.gameapi.JournalParser`) and publishing events via an event bus. Currently private, it may open-source under a Creative Commons license (e.g., CC BY-NC-SA 4.0)
if released publicly.

## Features

- **Voice Commands**: Hands-free control with natural language (no memorized phrases). Maps to game commands (e.g., "deploy landing gear") but can't fire weapons or steer.
- **AI Capabilities**: Distinguishes commands, queries (game or general, e.g., "summarize scanners" or "favorite Motorhead album"), and chats (ends with a question). Analyzes data (loadouts, scans) on request.
- **Mission Tracking**: Supports stacked missions (e.g., pirate massacre) and more.
- **Announcements**: Alerts for enemy scans, wanted/mission targets, system jumps (with fuel status).
- **Streaming Mode**: Ignores input unless addressed as the voice name (e.g., "Jennifer") or "computer".
- **Customization**: 14 voices, 3 profiles (Imperial, Federation, Alliance), 4 personalities (Professional, Friendly, Unhinged, Rogue). Adjustable on-the-fly. Addresses user by pilot name, rank, or honorific.
- **Session Persistence**: Recovers session data after crashes/restarts (`elite.companion.session`).
- **Route Analysis**: Provides route suggestions on user prompt.

## Limitations

- No autonomous decisions or AFK activity.
- Relies on game API data; no external modifications.
- Speaks only when prompted (except specific events).

## Installation & Setup

1. **Prerequisites**: Java JDK 17+, Google Cloud STT/TTS keys, xAI Grok API key.
2. **Build**: Clone and run with Gradle (private repo for now).
3. **Configure**: Set API keys in UI ("System" tab), adjust voice/profile settings.
4. **Run**: Launch and start voice listening.

## Usage Examples

- "Jennifer, deploy landing gear" → Executes command, confirms via TTS.
- "Computer, summarize scanners" → Analyzes journal, responds.
- "What’s my fuel?" → Reports current status.

## Contributing

(If open-sourced) Fork and submit PRs. Follow TOS, use modular event-driven design (`elite.companion.gameapi`), and test thoroughly. Contact maintainer for private contributions.

## License

Private for now. Future public release under Creative Commons (e.g., CC BY-NC-SA 4.0). See [creativecommons.org](https://creativecommons.org/licenses/by-nc-sa/4.0/).