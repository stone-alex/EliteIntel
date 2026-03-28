## Elite Intel v-0.0333-beta

### bug fixes / features:

- Attempt to fix no TTS Issue on Windows. (App was looking to load a non-existent C++ library)

## Elite Intel v-0.0332-beta

### bug fixes / features:
- Prompt normalization algorithm and logic.
- Adjusted missing material caps
- Combined query for commodity and materials. Now you can ask: "Do we have <blah>" and it will query both cargo and inventory

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

---

## Elite Intel v-0.0314

### bug fixes / features:

- Added LM Studio support.
- Fixing orbital navigation.

---

## Elite Intel v-0.0313 (hot fix)

### bug fixes / features:

- Navigation announcements will turn on automatically when you ask to navigate to surface coordinates or codex entry.
- Gemeni response format tuning.
- Added a Fleet management panel under the Player tab. You can now customize the personality, cadence and voice of your ships. NOTE: The voice names will be reset to default if you switch from Local to Cloud voices and back.

---

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
- Short acknowledgement call-outs. The app will no longer post-fix with "Commander" or "<your name>"
- Checking "Use" box on the Local LLM settings panel will not automatically re-start the app's LLM service.