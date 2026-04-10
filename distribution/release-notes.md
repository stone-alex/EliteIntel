## Elite Intel v-0.0354-beta

### bug fixes / features:

- Fix for the biome (Bacteria is the most likely candidate)
- Fix for the Carrier arrival times
- Unit test support for conditional action map assembly
- Added station services (open). This only fires when you are in your ship at the station, port or fleet carrier.
- Fixed find broker / trader.
- Fixed "welcome back aboard" when pulling in the SRV
- Fixed loadout query. "Do we have a fuel scoop" will not invent actions anymore


## Elite Intel v-0.0352-beta

### bug fixes / features:

- strict reducer setting. If conversation mode is off, (default) the app should have less changes to perform action on radom chatter



## Elite Intel v-0.0352-beta

### bug fixes / features:

- setting ship voice, personality and cadence now only available via UI.  No longer available via a voice command.
- Added "disembark" command to when you are in the ship.
- Added "show/open/display station services" when you are in your ship at the station/port/carrier.
- Wrapped the subscribers that do IO in to virtual threads. (slight performance improvement).
- Fixed carrier scheduled to depart message. Now it will say carrier will depart in H hours M minutes.
- Consolidated action map, to reduce the options even farther based on isOnFoot, isInMainShip, and isInSRV.



## Elite Intel v-0.0350-beta

### bug fixes / features:
- basic on-foot commands (wheel)
- Show/Open: Commander/Role/Central panel should open the central panel on the top tab.
- Deploy SRV / Deploy vehicle / deploy buggy should launch vehicle
- Disembark (and only disembark) will put you on foot.
- deploy fighter, launch fighter, send out fighter

- order fighter defend ship, fighter defend, fighter defensive
- order fighter attack my target, fighter attack, fighter on target
- order fighter hold fire, fighter cease fire, fighter stand down",
- order fighter return to ship, fighter dock, recall fighter
- Fire at Will!, Attack! (that's open orders, but can't use word "open", or it will think it needs to open something on UI)


## Elite Intel v-0.0348-beta

### bug fixes / features:

- removed confidence param (no longer use, not provided by STT anymore).
- distance to bubble/earth adjustments
- Stricter "Ignore nonsensical input" rule when NOT in conversation mode
- Updated tests.
- Fixed ignore / and pass through keywords
- reduce the chatter for the error messaging.
- distance to X / distance to Earth prompt adjustments.
- Prohibition to use player stats command unless explicitly asked (used to fall back on that for some reason)


## Elite Intel v-0.0347-beta

### bug fixes / features:

- hot fix. saving new ship was broken

## Elite Intel v-0.0346-beta

### bug fixes / features:

- Fixed projected payment on organic scan. It was sending the wrong, and the average value now sends the correct value for Genus / Variant
- Added Conversation mode on/off toggle. **(UI ONLY!)** When on, the app will act as usual, when off it will ignore all input that does not match an existing command or query. This is a strict parser mode.
- Added "disembark" command. Not to be confused with "exit" which will exit Game UI panes / maps etc.
- Fixed an incorrect bio sample projected payment announcement.

## Elite Intel v-0.0342-beta

### bug fixes / features:

- Reminder will only trigger in the destination system
- Adding missing material capacity updates, fixed incorrect material capacity entries.
- Changed Parakeet to work with greedy algo instead of hotwords.
- Minor adjustments to organic scan announcements.
- Change to an exo-bio announcement, let the user know what genus remains to scan. Fixed sell organic data sensor event.
- Removing handlers that remove data. too dangerous to have them around. Can falsely remove user data.
- The sibilance is aliasing from the resampler. When downsampling 48kHz 🡢 16kHz, any energy above 8kHz (the new Nyquist) folds back into the 4-8kHz range - right where natural sibilants live. The sherpa-onnx resampler's anti-aliasing filter isn't aggressive enough to prevent this.
- Fix: apply a biquad lowpass filter at ~7.2kHz before the resampler on each frame. Need a stateful filter so the delay registers persist across frames (otherwise you get a transient click at every buffer boundary).
- The chain is now: capture 🡢 anti-alias LPF (7.2kHz cutoff) 🡢 linear-interp resample 🡢 VAD/collect 🡢 whole-utterance normalize 🡢 Parakeet. The filter state is held in the AntiAliasingFilter instance and carries across frames, so no clicks at buffer boundaries.
- Bio samples collection improvements.
- Orbital navigation fixes. Account for radius, polar correction. Adjusted glide angle calculation, illuminated sharp turns while in orbit, added pull up / level off prompts.

## Elite Intel v-0.0338-beta

### bug fixes / features:

- Wrote a test harness for LLM commands
- Improved app accuracy (part 1 LLM)
- Decoupled handlers from game controller.

## Elite Intel v-0.0335-beta

### bug fixes / features:

- Cleaned up Parakeet input, filtering random STT junk.
- Expended hotwords boost file
- Adjusted discovery commands / queries
- Improved community and mining hotspot search.
- Simplified toggle commands to one word (no longer require qualifier on/off)
- Added exit Detailed Surface Scan mode to "exit" command.
- Updated user manual.

## Elite Intel v-0.0334-beta

### bug fixes / features:

- Pirate missions will now calculate correctly based on bounty collected against a target faction. Old bounties no longer count towards the mission. (bug fix)
- Pirate mission accept would fail if no hunting were available. fixed.
- Added Windows key support for bindings.
- Added application menu support for key bindings.
- Found and removed long and unnecessary reminder in pirate missions.
- Say "Exit" or "Close" to exit any window or tab.
- Nightvision toggle no longer requires on/off clarification.
- Lights toggle no longer requires on/off clarification.
- Reminders can now be interrupted
- Added better support for subsystem targeting. Say "target fsd" or "target drive" or "target engines" or "target power distributor" etc. NOTE: Optional subsystems are not included.

## Elite Intel v-0.0333-beta

### bug fixes / features:

- Attempt to fix no TTS Issue on Windows. (App was looking to load a non-existent C++ library)

## Elite Intel v-0.0332-beta

### bug fixes / features:
- Prompt normalization algorithm and logic.
- Adjusted missing material caps
- Combined query for commodity and materials. Now you can ask: "Do we have <blah🡢" and it will query both cargo and inventory

## Elite Intel v-0.0331-beta

### bug fixes / features:

- Replacing whisper with NVIDIA Parakeet
- Dictionary file no longer in use and no longer needed.

## Elite Intel v-0.0328-beta

### bug fixes / features:

- Added native key bind support for left and right modifiers for Linux and Windows.
- Added panel navigation support to Fighter.
- Radio now has random vocalizations and 2-way radio effect static.
- Added suport for left/right modifier keys for Windows and Linux.

## Elite Intel v-0.0327-beta

### bug fixes / features:

- Fuzzy search for STT corrections. This works like a spell checker attempting to match bad input to commands known to the app.
- Hot fix for left / vs right key modifiers.

## Elite Intel v-0.0322-beta

### bug fixes / features:

- Fixed the updater problem where it would indicate that a new version is available before it can actually be downloaded.
- "Navigate from memory" command. This will open the galaxy map and navigate to the location you have copied from somewhere via Ctrl+C.
- Fixed combo key bindings.
- Fixed sub system targeting. Say "target fsd" or "target drive" or "target engines" etc. Fallback is powerplant.

## Elite Intel v-0.0320

### bug fixes / features:

- Added Audio wave / mic input visualization to help with calibration.
- Added trim to saveApiKey calls to ensure that the key is not saved with a trailing space.
- Simplified GeneralConversationHandler removed chat history to avoid LLM confusion.
- Added Radar contact on/off toggle.
- Voice detection (ignore mode) on-off toggle changed. Say "ignore me" or "stop ignoring me" to toggle. Say "listen" to bypass.
- Added fighter commands: "Deploy Fighter", "Fighter Escort", "Fighter return to base", "attack my target", "cease fire", "hold your fire" etc.
- "Use" checkboxes for Local vs Cloud services now work properly.
- Added more misinterpreted words to the correction dictionary.
- Fixed Speed +N / Speed -N commands.

## Elite Intel v-0.0316

### bug fixes / features:

- Fixed 'Change Personality' and 'Change Cadence' via voice commands.
- UI will now update properly when you change the ship cadence, personality or voice.
- Fixed hot LLM swap when changing from LM Studio to Gemini.

## Elite Intel v-0.0315

### bug fixes / features:

- Changed Gemini model to "gemini-3.1-flash-lite-preview" per request.


## Elite Intel v-0.0314

### bug fixes / features:

- Added LM Studio support.
- Fixing orbital navigation.


## Elite Intel v-0.0313 (hot fix)

### bug fixes / features:

- Navigation announcements will turn on automatically when you ask to navigate to surface coordinates or codex entry.
- Gemeni response format tuning.
- Added a Fleet management panel under the Player tab. You can now customize the personality, cadence and voice of your ships. NOTE: The voice names will be reset to default if you switch from Local to Cloud voices and back.


## Elite Intel v-0.0311-beta

### bug fixes / features:

- Added support for Google Gemeni (Generative Language API)
- Added disable all announcements callout.
- Added audio calibration verification on service startup.
- If the Noise Floor to RMS ratio is low, (below 300) you will get a warning.
- If audio is not calibrated at all, the app will issue a warning.
- Added isInSupercruise check for hyperspace jump preparation. This should fix unnecessary check for hardpoints etc
  before jumping.
- Performance improvements
- Introduced Virtual threads, so game events are processed in a more predictable manner.
- Fixed EDSM blocking calls, the app will no longer be blocked by EDSM failed calls.
- Introduced adjustable threads to Whisper processing. This allows for faster STT transcriptions on more powerful
  hardware
- Increased the default speed between tab stop on UI navigation.
- Location coordinates were not saved correctly in some corner cases
- Fleet Carrier jump location / ETA was broken.
- UI navigation improvements
- You can now tell the app to ignore you by saying "ignore me" and turn that back on by saying "listen to me" or "stop
  ignoring me"
- Some TTS corrections (quirks of Whisper when it transcribes 'exit' as 'thank you' for some reason, and others like it.
- Fix for qwen3.5 and other local LLMs where it would cancel the previous prompt while issuing a new one asynchronously.
  the prompts are now queued and are processed sequentially in the order they arrive.
- Removed chat history from chit-chat. LLM would just echo that back at the user. That was annoying.
- Default to all announcements OFF.
- Short acknowledgement call-outs. The app will no longer post-fix with "Commander" or "<your name🡢"
- Checking "Use" box on the Local LLM settings panel will not automatically re-start the app's LLM service.