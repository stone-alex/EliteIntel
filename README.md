# Elite Companion - Public Beta

### NOTE: This is an early public beta release—expect bugs and ongoing improvements! ###

**It's not a stable version yet. I'll be posting frequent updates on the GitHub releases page and announcing them in our
Discord server: [discord.gg/3qAqBENsBm](https://discord.gg/3qAqBENsBm). Your feedback is crucial to making this better.
**

## Overview

Elite Companion is your AI-powered, TOS-compliant sidekick for *Elite Dangerous*.
It's a Quality of Life (QoL) app that enhances gameplay with voice-activated commands **uttered in natural language**,
real-time journal parsing, and smart AI assistance—all while staying fully compliant
with Frontier Developments' Terms of Service. No AFK play, no botting, and no
automation that gives unfair advantages; everything requires your input.

The app uses Google Speech-to-Text (STT) and Text-to-Speech (TTS) for **natural voice interactions**,
and xAI's Grok for handling commands, queries, and chats. It processes game data from journal and auxiliary
files, pulls in info from EDSM when needed, and keeps things responsive.

The project is open-source under
a [Creative Commons license CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en),
so feel free to check out the code and contribute. (see [DEVELOPERS.md](DEVELOPERS.md) for details)

## Features

- **Voice Commands and Natural Language**: Hands-free control without memorizing a huge list of phrases—just talk like
  you would to a human copilot. It can handle key-bound game actions (like "deploy landing gear" or "jump to
  hyperspace"), but some are blocked by design—no firing weapons, steering, or anything autonomous.


- **AI-Powered Analysis**: On-demand breakdowns of game data, like security assessments for your next jump, biological
  signals on nearby planets, mission progress, ship loadouts, market info, local signals, and route suggestions. It
  combines journal scans, auxiliary files, and EDSM data and general knowledge for accurate insights.


- **Mission Tracking and Announcements**: Keeps tabs on stacked missions (e.g., pirate massacres) and alerts you to
  critical stuff like enemy scans, wanted ships, mission targets, or system jumps with fuel status. Unprompted
  announcements are limited to route data and mission-critical info.


- **Streaming Mode**: Ignores background noise unless you address it by name (e.g., "Jennifer" or "computer")—perfect
  for streamers or noisy setups.


- **Privacy Mode**: Temporarily stops and disables Speech to text recognition. The app will still announce critical
  events, but will not be able to hear you.


- **Automatic Audio Calibration**: Adjusts audio settings for best performance matching your system's microphone.


- **Customization**: Choose from 14 voices, 3 profiles (Imperial with British cadence, Federation with American, or
  Alliance mixing both), and 4 personalities:
    - **Professional**: Military-style, no-nonsense replies with profile-specific cadence; skips idle chit-chat.
    - **Friendly**: Casual and concise, like chatting with a buddy, using slang that fits the profile.
    - **Unhinged**: Super brief, playful, and cheeky with humor and light sarcasm; slang matches the cadence.
    - **Rogue**: Bold and witty, extremely concise with heavy jargon and occasional cheeky language (nothing too wild,
      but watch out for that edge).
      Switch them on the fly. and it'll address you by your pilot name, rank, or honorific.
    - **Variable User Addressing**: The AI will address you by your name, rank, or honorific.
        - **Name**: Address by your pilot name or overridden name in user setting if you in-game name is
          unpronounceable.
        - **Rank**: Address by your rank (e.g., "Viscount" or "Ensign"). depending on which of your navy ranks is
          higher.
        - **Honorific**: Address by your honorific (e.g., "My Lord", "Your Grace", "Sarg" or "Lieutenant"). derived from
          highest navy rank.
            - **NOTE** If your navy ranks are too low, it will address you as "Commander".


- **Event-Driven Responses**: Events like planetary approach trigger automatic info (e.g., "Entering orbital cruise,
  gravity 1.2G, temperature 254 Kelvin—gravity warning") if data's available from journals or EDSM. Similarly hyperspace
  jumps will trigger announcement of the target system suitability for fuel. You can ask the AI for specific information
  on the current or target system, such as allegiances, security or anything else. The AI will reply with summary if
  legal data is available (Journal Files, EDSM query). But it will not read in-game hidden memory.

The app sticks to legal data sources only—no memory reading or hidden game hacks. When it tells you that no data
available even if you
see it on in-game screens, it means this data indeed not available in either journals or EDSM.

The app maintains persisted session and recovers from crashes or restarts without losing your session progress.
You can delete your session data by removing files from the session folder.

The Speech to Text (STT) sometimes makes mistakes such as "southwest" instead of "switch voice to" etc. There is a
dictionary
directory that contains a dictionary file for corrections. If you encounter new mistakes, you can add a correction to
the dictionary.

The correction dictionary format is:

```"incorrect word or phrase"="correct word or phrase".```

## Limitations

- No autonomous decisions, AFK activity, or macros that could violate TOS. It won't operate weapons, steer your ship, or
  handle harvesting/mining.
- Relies solely on game API data (journals, auxiliary files) and EDSM—no external mods or unfair edges.
- Speaks only when prompted, except for specific mission-critical events.
- A few commands do multiple keystrokes for convenience (e.g., "Display HUD" to exit nested menus quickly), but only to
  avoid VR frustration—not for advantages.
- Current setup needs API keys for Google STT/TTS and xAI Grok—it's not plug-and-play without them.
- Google and Grok API are not free. You must make an account and get your own keys. See Help tab in the app for
  instructions.
- English only - No support for other languages.

### Current Implementation Limitations (Beta-Specific)

As this is an early beta, here are some known areas we're still refining:

1. **Commands and keybinds are assumed to be the same for ship and SRV settings. For Left, Right and Central panel as
   well as thrust and pips settings.** This keeps things simple for user interaction and saves on AI tokens (reducing
   costs per request) by not needing to infer or specify vehicle type. Sending separate SRV commands (which mirror ship
   commands for things like left/right/center panels and pips) would double the command list per request or require
   unreliable detection of whether you're in a ship or SRV—since we don't read game memory directly.


2. All on-foot keybinds are currently disabled, though data analysis features remain available.


3. Not all mission types have been fully tested—expect potential quirks with less common ones.


4. While the app has automatic audio detection and calibration, it hasn't been tested on a wide variety of systems. If
   you run into audio issues, please report them on GitHub with logs and a description of your audio setup.

## Usage Examples

These aren't rigid, pre-set commands—the AI understands natural language, so you can phrase things however feels right.
Responses adapt to your chosen personality and profile, and it handles variations like excitement, slang, or casual
chat. Here are some examples to give you an idea:

- **Security Check**: "Give me a security rundown on where we're headed next" or "Is the next system sketchy?" or "
  Assess risks for our jump destination."  
  AI: "Security assessment for our next destination indicates moderate risk. System data shows elevated hostile activity
  with wanted vessels around. Recent combat losses and pirate traffic suggest combat readiness on arrival."


- **Planet Scan**: "Scan nearby planets for bio signals" or "Any life signs on local worlds?" or "Tell me about
  biological stuff in this system."  
  AI: "Two bodies in the system show biological signals. One icy world likely has bacteria; the other is a metal-rich,
  terraformable planet with five signals detected."


- **Jump Command**: "Transition to Hyperspace" or "enter supercruise" or "exit supercruise" or "Engage the FSD" or "Time
  to jump—hit it!" or "Get us outta here, quick!"  
  AI: Engages FSD without extra talk (in Professional mode) or with a quick quip in others.
- NOTE: it is recommended to set supercruise and hyperspace keybinds, else AI will rely on toggle FSD keybind which may
  lead to hyperspace jump instead of supercruise.


- **Speed command**: "Set low speed" or "Go Slow" will trigger 25% throttle. "Midium speed" etc. will trigger 50%
  throttle. "Optimal Speed" will set throttle to 75% (very useful on approach to a station in supercruise). "Full speed
  ahead" or variants on the theme will set throttle to 100%.


- **Landing Gear**: "Drop the gear," "Prepare for landing," or "Hey, let's touch down here."  
  AI: Drops the gear and might comment depending on personality, like "Gear down, Commander—smooth landing ahead."


- **Planetary Approach (Unprompted)**: On event trigger...  
  AI: "Entering orbital cruise, gravity 1.2G, temperature 254 Kelvin—gravity warning." (If no data: "No data available;
  monitor HUD for gravity info.")


- **Ship Targeted Scan**: AI Will not announce every ship you scan. However it will announce wanted ships as "Legal
  Target" and bounty amount. If you have a current mission against the faction that ship belongs to it will announce it
  as "Mission Target"
- NOTE: For safety, always check the in-game left panel for target info before shooting. AI will have network latency,
  and you may hear announcement for the ship you have previously scanned.


- **Basic Queries**: "Break down my scanner data" or "What's the scanners saying?" → Analyzes journal data and
  responds.  
  Even off-topic stuff: "What's your favorite Motorhead album?" → Handles general chat or trivia. (mine like Ace of
  Spades)

## Installation & Setup

1. **Requirements**: Minimum Java 17 or newer (tested up to 21). Grab Oracle Java
   from [oracle.com/java/technologies/downloads/](https://www.oracle.com/java/technologies/downloads/). Verify with
   `java --version` in your terminal—it should show something like Java 17+ or better.


2. **API Keys**: Get STT/TTS keys from [Google Cloud Console](https://console.cloud.google.com/). For AI, snag a Grok
   key from [xAI Console](https://console.x.ai/).


3. **Download**: Grab the ZIP from releases (on the right side of this GitHub page).


4. **Extract and Run**: Unzip to any folder. Double-click `run.bat` to start, or use `debug.bat` from a terminal for
   troubleshooting.- **NOTE**: If updating existing installation, unzip in temp folder and grab only the JAR file.


5. **Configure**: In the app's UI ("System" tab), plug in your API keys and tweak voice/profile settings.


6. **Go Time**: Launch, start voice listening (will run mic calibration initially), and you're set.

If you run into issues, hit us up on GitHub issues or Discord: [discord.gg/3qAqBENsBm](https://discord.gg/3qAqBENsBm).
Feedback and bug reports are super welcome!

## Contributing

Fork it and submit pull requests! Stick to TOS, keep things modular and event-driven, and test your changes. For dev
details, check out `DEVELOPERS.md` in the root. It covers the architecture (decoupled, multithreaded, event registry via
reflection) and principles like DRY and SRP. All PRs go through review.

## License

Released under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0).
See [creativecommons.org/licenses/by-nc-sa/4.0/](https://creativecommons.org/licenses/by-nc-sa/4.0/) for details. Share
and adapt, but give credit, don't sell it, and keep the same license.