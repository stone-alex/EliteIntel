# Elite Companion App

## Overview

Elite Companion is a Quality of Life (QoL) companion application designed for players of *Elite Dangerous*. Built entirely in pure Java, it enhances gameplay by providing voice-activated interactions, real-time information from the game's journal files, and seamless integration with in-game controls—all while remaining fully compliant with Frontier Developments' Terms of Service (TOS). The app does **not** automate gameplay or allow AFK (Away From Keyboard) play; all interactions require active user input via voice triggers, ensuring it serves as a helpful assistant rather than a bot.

This app leverages Google Speech-to-Text (STT) for voice command recognition, Google Text-to-Speech (TTS) for audio responses, and Grok-3-fast (from xAI) as an AI backend for intelligent processing and decision-making. It processes the game's journal files (JSON event streams) to track play sessions, parse events, and provide contextual information or execute mapped commands.

**Important Note:** This project is currently in private development and not open-sourced. If made public in the future, the source code will be available on GitHub under a Creative Commons license (likely CC BY-NC-SA 4.0 or similar), distributed free of charge. Contributions or access will be at the discretion of the project maintainer.

## What the App Does

- **Voice-Activated Commands for VR Users:** Enables hands-free control in VR setups. Users can issue voice triggers (e.g., "deploy landing gear" or "plot route to Sol") to execute in-game actions via keyboard events. This reduces the need to remove VR headsets for common tasks.
  
- **Real-Time Journal Parsing:** Reads and processes *Elite Dangerous* journal files (JSON format) using a `JournalReader` to post events to an internal event bus. Modules subscribe to these events for handling, allowing the app to track session data and respond dynamically.

- **Intelligent AI Integration:** Uses Grok-3-fast to interpret voice commands, provide helpful responses (e.g., route suggestions, mining targets), and route actions. Grok is aware of all game commands, including user-friendly mappings (e.g., `deploy_landing_gear` to `LandingGearToggle`).

- **Dynamic Key Mapping:** Automatically generates mappings from the game's `.binds` file using `GenerateGameCommandMapping`. This ensures compatibility with game updates—simply rerun the generator if bindings change. Supports custom hold times for keys (e.g., pressing and holding for route plotting).

- **Event Bus and Session Tracking:** Manages in-memory storage of session data via an event bus. Modules like `SpeechRecognizer`, `GrokInteractionHandler`, `GrokResponseRouter`, `VoiceCommandHandler`, and `KeyBindingExecutor` handle STT/TTS, AI responses, and keyboard interactions.

- **Custom Commands:** Beyond standard bindings, supports app-specific actions like `set_mining_target` or `plot_route`, executed as sequences of key presses with configurable delays.

The app enhances immersion and accessibility, particularly for VR players, by bridging voice input with game controls and providing spoken feedback.

## What the App Does NOT Do

- **No Automation or AFK Play:** The app requires explicit voice triggers from the user for every action. It cannot play the game autonomously, farm resources, or perform any unattended operations. This ensures full TOS compliance—no macros, bots, or exploits.

- **No Direct Game Modification:** It does not alter game files, inject code, or interact with the game executable beyond simulating keyboard events (which are TOS-allowed for accessibility tools).

- **No Data Collection or Sharing:** All processing is local. Journal data is parsed in-memory for the current session only; no external storage or transmission occurs without user consent.

- **No Advanced AI Overreach:** Grok-3-fast is used solely for command interpretation and response generation. It does not make decisions that violate TOS or user intent.

- **Limited Scope:** Focused on QoL features like voice commands and info relay. It does not include features like overlay HUDs, external APIs (beyond configured endpoints), or multiplayer coordination.

## Installation and Setup (For Private Use)

1. **Prerequisites:**
   - Java JDK 11+ (pure Java implementation).
   - Google Cloud credentials for STT and TTS (configure API keys in the app's config).
   - Grok-3-fast endpoint setup (via xAI API; refer to x.ai/api for details).
   - *Elite Dangerous* installed with journal logging enabled (default behavior).

2. **Clone and Build:**
   - (Private for now) Clone from the private GitHub repo.
   - Build with Gradle.

3. **Configuration:**
   - Set paths to *Elite Dangerous* journal directory and `.binds` file in `config.properties`.
   - Generate key mappings: Run `GenerateGameCommandMapping` to create `GameCommandMapping`.
   - Update `GrokRequestHints.COMMANDS` if custom commands are added.

4. **Run the App:**
   - Launch via `java -jar elite-companion.jar`.
   - Start voice listening; issue commands prefixed with a trigger phrase (configurable, e.g., "Computer, [command]").

## Usage Examples

- **Voice Command:** Say "Computer, deploy landing gear." → App recognizes via STT, queries Grok if needed, maps to `LandingGearToggle`, executes key press, and responds via TTS: "Landing gear deployed."
  
- **Complex Action:** "Computer, plot route to Sol." → Grok processes, app holds `RoutePlot` key for 1000ms, simulates tab navigation, and confirms via TTS.

- **Info Query:** "Computer, what's my current fuel level?" → Parses journal events, responds via TTS.

Always monitor the console for logs during development/testing.

## Developer Guidelines

If this project is opened to the public, contributors should adhere to the following:

- **TOS Compliance First:** All features must align with *Elite Dangerous* TOS. No automation, no exploits. When in doubt, reference Frontier's guidelines.

- **Code Standards:**
  - Pure Java only—no external dependencies beyond configured libraries (e.g., Google APIs, xAI endpoints).
  - Use modular design: Leverage the event bus for loose coupling. New modules should subscribe to specific events.
  - Dynamic and Robust: Prefer generated mappings over hardcoding to handle game updates.
  - Testing: Unit tests for parsers, mappers, and handlers. Simulate journal events and voice inputs.

- **Contributions:**
  - Fork and PR on GitHub (if public).
  - Focus on QoL enhancements: Voice features, AI smarts, accessibility.
  - Avoid: Anything that could enable AFK play or violate licenses.

- **AI Integration Best Practices:**
  - Keep Grok prompts clear and contextual (e.g., include command lists).
  - Handle JSON responses carefully: Parse for `action`, `action_press_and_hold_delay`, etc.
  - Ensure fallback behaviors for AI downtime.

- **Licensing and Attribution:** If open-sourced, all code falls under Creative Commons (details in LICENSE file). Attribute any third-party inspirations.

- **Security:** No internet-required features beyond APIs. Sanitize inputs to prevent injection.

For questions or suggestions during private development, contact the maintainer directly.

## License

Currently private. If released publicly, this project will be licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0). See [creativecommons.org](https://creativecommons.org/licenses/by-nc-sa/4.0/) for details. The app is free to use and distribute under these terms.
