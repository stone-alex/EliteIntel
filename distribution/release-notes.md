## v-0.0379-beta

### bug fixes / features:
- Removed experimental Web Socket integration point.
- Added warning message for anyone who dare to check the "Delegate Controller to VM" check box. (experimental feature)


## v-0.0377-beta

### bug fixes / features:

- Added VM integration. The LLM inferred actions are pushed to http://127.0.0.1:8080/ExecuteMacro=EliteDangerous/ACTION_NAME


## v-0.0376-beta

### bug fixes / features:
- Added websocket on port 7497. It exposes the llm actions as they return.


## v-0.0372-beta

### bug fixes / features:
- Parametrized market searches. You can now specified **nearest** vs **best price**. The market also must have the commodity in stock 2x your max cargo hold capacity. If you specify **within X light years** the query will be limited to markets within that range, else it is 3x your ship jump range. NOTE: The data is from Spansh, and is dependant on other commanders submitting market data. It can be wrong or outdated. Max data age is 1 hour.



## v-0.0372-beta

### bug fixes / features:
**Station** - 5 fixes:
  - TradersAndBrokersSearch: swapped size/page (was size=1, page=5; now size=5, page=0) - closest stations were being skipped entirely
  - TradersAndBrokersSearch: missing space in TTS string "looks for" + station name
  - TraderAndBrokerSearchDto.getResults(): null guard before sort
  - SearchForMaterialBrokerOrTrader: null check on dto before calling getResults()
  - VistaGenomicsSearch: null checks on dto/getResults(), null-safe services stream, fixed wrong error log message

**Stellarobjects** - 2 fixes:
  - StellarObjectSearch: page = 1 → page = 0, removed unused IOException import

**Traderoute** - 4 fixes:
  - TradeRouteFilter: null guard on originalResults before for-each
  - TradeRouteSearchCriteria: allowStrongHold was silently dropped from the query string, now included as allow_restricted_access; ONE_HOUR made final
  - EnemyStarSystemFilter (used by both traderoute and missions/pirates): getPowerState() NPE when power state is null - flipped to "Stronghold".equalsIgnoreCase(...) style

**IntraRequest** - MissionProvider.faction and both MissionProvider.distance / BattleGround.distance were missing @SerializedName("fac") / @SerializedName("dist"). The API was receiving "faction" and "distance" instead of the abbreviated keys it uses everywhere else, so those filters were silently ignored - meaning the HAZ-RES distance cap and minimum faction count were never actually enforced.
  - IntraResponse.Head - added getMissionProvider() / setMissionProvider() for the shop count, which was write-only.
  - PirateMassacreMissionSearch - added response.getOk() != 1 check before processing the body.

  - Wrong arg station.getName() → starSystemName; added null guard on market.getData() before .getCommodities()
  - Removed debug main() method
  - allowedStations.remove("outpost") (no-op, inverted logic) → allowedStations.add("outpost")
  - Removed dead authenticatedUrl() duplicate; removed dev comment from searchStarSystem
  - EdsmApiClient.java + DeathsDto.java
  - Added missing dto.timestamp = timestamp in searchDeaths; added timestamp field to DeathsDto
  - Added a short pause after modifier is pressed and before the main key is triggered (hope this fixes some issues on Windows - maybe...)


## v-0.0371-beta

### bug fixes / features:
- added deepseek



- refactored clients to not use deprecated http client api.
## v-0.0370-beta

### bug fixes / features:
- refactored clients to not use deprecated http client api.


## v-0.0370-beta

### bug fixes / features:
- The common thread across all failing combinations is Right Alt on Windows. When VK_RMENU is sent via SendInput with wScan=0, on many European keyboard layouts (AltGr keyboards) Windows automatically injects a synthetic VK_LCONTROL, turning the combo into Ctrl+RightAlt+Apps+6, which doesn't match the binding
- 1. DirectInput uses hardware scan codes, not VK codes, to identify keys. With wScan = 0, DirectInput can't reliably match the modifier key events to the binding, so combos like RightAlt + Apps + 6 silently fail. Meanwhile, Java Robot's keybd_event calls auto-compute the scan code from the VK (via MapVirtualKey), which is why simple keys sent through Robot work fine.
- 2. AltGr keyboard layouts (German, French, etc.): sending VK_RMENU via VK code causes Windows to inject a synthetic VK_LCONTROL, turning RightAlt + Apps + 6 into Ctrl + RightAlt + Apps + 6 from the game's perspective resulting in no binding match.
- Fix: Switch to KEYEVENTF_SCANCODE with hardcoded PS/2 Set 1 hardware scan codes (wVk = 0, wScan = scan code). Extended keys (Right Ctrl, Right Alt, Win, Apps) get KEYEVENTF_EXTENDEDKEY | KEYEVENTF_SCANCODE to produce the correct E0-prefixed scan sequence. This bypasses both issues and is consistent with how physical key presses arrive at DirectInput.


## v-0.0369-beta

### bug fixes / features:
- The auto-hunk... Next to each ship you will see a button with three dots. Click that, select the check box if you want that ship to auto-hunk. Select which fire group the hunker is on, and which trigger it is on. When you come out of hyperspace the ship will switch to analysis mode, change to firegroup that you have selected and use the trigger you specified to hunk the system.
- There is now a Ship Options button that applies to all ships. This allow you to set the global settings for all ships to automate some of the functions, such as pre-FTL check for gear, scoop, weapons, fighter out, auto-accelerate etc. Toggle what you want.




## v-0.0368-beta

### bug fixes / features:
- Added check for backing out of UI before switching to another UI section via voice. Instead of backing out every time the app will attempt to back out only if it necessary. (Performance improvement)
- Changed default voice in Kokoro from EMMA to GEORGE for case when the app is just installed. (EMMA a bit cringe - bad impression on first launch)
- Removed auto-throttle on FTL per request. (Might add later as a UI toggle option)



## v-0.0367-beta

### bug fixes / features:
- Fixing on-boarding. Default to LMStudio tulu3.1:8b-supernova etc



## v-0.0366-beta

### bug fixes / features:
- ParakeetSTTImpl.buildRecognizer(). On Windows, before any sherpa-onnx JNI class is touched, we call System.load() with the absolute path to the bundled onnxruntime.dll. Windows DLL loading is idempotent. Once a DLL is in the process, any subsequent load request for the same name reuses the already-loaded module, so sherpa-onnx-jni.dll will get the correct version.

## v-0.0365-beta

### bug fixes / features:
- Changed carrier estimated operation cost calculation
- Added logging for Google TTS for debugging.






## v-0.0364-beta

### bug fixes / features:
- Keybinding monitor fix. A) will not update the bindings without restart. B) it will not read the StartPreset, not just the bindings file that was updated last.


## v-0.0363-beta

### bug fixes / features:
- Changed installer.sh script to download a known Java 21 JRE and run the app with it. Turns out some setups might have a headless JDK which can't run GUI apps.




## v-0.0362-beta

### bug fixes / features:
- Refactoring LLM provider package for DRY - no functional changes
- Warn user to properly setup bindings and journal directory paths. Game is required to run this app.
- Load beep volume from settings on startup.
- Theme adjustments



## v-0.0360-beta

### bug fixes / features:
- Fix for EDSM client. headers and API call limit/timeout
- Increased wait time for galaxy map to open before we attempt to enter the route.
- Fix for home system setting. Setting home system from a station/carrier would return incorrect systemAddress (0) and therefore no route could be plotted.


### bug fixes / features:
- changes to normalizer for "email" commands
- removed lights and night vision from pre-FTL check.


## v-0.0357-beta

### bug fixes / features:
- Trade route fix. Allow to **optionally** filter out enemy Stronghold. NOTE this may introduce a gap in the route.


## v-0.0356-beta

### bug fixes / features:
- Pirate Massacre mission finder fix



## v-0.0355-beta

### bug fixes / features:
- Added SttCorrector. It automatically corrects input adjusting it against the curated domain list of tokens "blending" to "landing", "sip" to "ship".
- trade route fix
- use corrected STT in display

## v-0.0354-beta

### bug fixes / features:

- Fix for the biome (Bacteria is the most likely candidate)
- Fix for the Carrier arrival times
- Unit test support for conditional action map assembly
- Added station services (open). This only fires when you are in your ship at the station, port or fleet carrier.
- Fixed find broker / trader.
- Fixed "welcome back aboard" when pulling in the SRV
- Fixed loadout query. "Do we have a fuel scoop" will not invent actions anymore


## v-0.0352-beta

### bug fixes / features:

- strict reducer setting. If conversation mode is off, (default) the app should have less changes to perform action on radom chatter



## v-0.0352-beta

### bug fixes / features:

- setting ship voice, personality and cadence now only available via UI.  No longer available via a voice command.
- Added "disembark" command to when you are in the ship.
- Added "show/open/display station services" when you are in your ship at the station/port/carrier.
- Wrapped the subscribers that do IO in to virtual threads. (slight performance improvement).
- Fixed carrier scheduled to depart message. Now it will say carrier will depart in H hours M minutes.
- Consolidated action map, to reduce the options even farther based on isOnFoot, isInMainShip, and isInSRV.



## v-0.0350-beta

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


## v-0.0348-beta

### bug fixes / features:

- removed confidence param (no longer use, not provided by STT anymore).
- distance to bubble/earth adjustments
- Stricter "Ignore nonsensical input" rule when NOT in conversation mode
- Updated tests.
- Fixed ignore / and pass through keywords
- reduce the chatter for the error messaging.
- distance to X / distance to Earth prompt adjustments.
- Prohibition to use player stats command unless explicitly asked (used to fall back on that for some reason)


## v-0.0347-beta

### bug fixes / features:

- hot fix. saving new ship was broken

## v-0.0346-beta

### bug fixes / features:

- Fixed projected payment on organic scan. It was sending the wrong, and the average value now sends the correct value for Genus / Variant
- Added Conversation mode on/off toggle. **(UI ONLY!)** When on, the app will act as usual, when off it will ignore all input that does not match an existing command or query. This is a strict parser mode.
- Added "disembark" command. Not to be confused with "exit" which will exit Game UI panes / maps etc.
- Fixed an incorrect bio sample projected payment announcement.

## v-0.0342-beta

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

## v-0.0338-beta

### bug fixes / features:

- Wrote a test harness for LLM commands
- Improved app accuracy (part 1 LLM)
- Decoupled handlers from game controller.

## v-0.0335-beta

### bug fixes / features:

- Cleaned up Parakeet input, filtering random STT junk.
- Expended hotwords boost file
- Adjusted discovery commands / queries
- Improved community and mining hotspot search.
- Simplified toggle commands to one word (no longer require qualifier on/off)
- Added exit Detailed Surface Scan mode to "exit" command.
- Updated user manual.

## v-0.0334-beta

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

## v-0.0333-beta

### bug fixes / features:

- Attempt to fix no TTS Issue on Windows. (App was looking to load a non-existent C++ library)

## v-0.0332-beta

### bug fixes / features:
- Prompt normalization algorithm and logic.
- Adjusted missing material caps
- Combined query for commodity and materials. Now you can ask: "Do we have <blah🡢" and it will query both cargo and inventory

## v-0.0331-beta

### bug fixes / features:

- Replacing whisper with NVIDIA Parakeet
- Dictionary file no longer in use and no longer needed.

## v-0.0328-beta

### bug fixes / features:

- Added native key bind support for left and right modifiers for Linux and Windows.
- Added panel navigation support to Fighter.
- Radio now has random vocalizations and 2-way radio effect static.
- Added suport for left/right modifier keys for Windows and Linux.

## v-0.0327-beta

### bug fixes / features:

- Fuzzy search for STT corrections. This works like a spell checker attempting to match bad input to commands known to the app.
- Hot fix for left / vs right key modifiers.

## v-0.0322-beta

### bug fixes / features:

- Fixed the updater problem where it would indicate that a new version is available before it can actually be downloaded.
- "Navigate from memory" command. This will open the galaxy map and navigate to the location you have copied from somewhere via Ctrl+C.
- Fixed combo key bindings.
- Fixed sub system targeting. Say "target fsd" or "target drive" or "target engines" etc. Fallback is powerplant.

## v-0.0320

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

## v-0.0316

### bug fixes / features:

- Fixed 'Change Personality' and 'Change Cadence' via voice commands.
- UI will now update properly when you change the ship cadence, personality or voice.
- Fixed hot LLM swap when changing from LM Studio to Gemini.

## v-0.0315

### bug fixes / features:

- Changed Gemini model to "gemini-3.1-flash-lite-preview" per request.


## v-0.0314

### bug fixes / features:

- Added LM Studio support.
- Fixing orbital navigation.


## v-0.0313 (hot fix)

### bug fixes / features:

- Navigation announcements will turn on automatically when you ask to navigate to surface coordinates or codex entry.
- Gemini response format tuning.
- Added a Fleet management panel under the Player tab. You can now customize the personality, cadence and voice of your ships. NOTE: The voice names will be reset to default if you switch from Local to Cloud voices and back.


## v-0.0311-beta

### bug fixes / features:

- Added support for Google Gemini (Generative Language API)
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