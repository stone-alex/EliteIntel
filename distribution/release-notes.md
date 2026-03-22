## Elite Intel v0.0310-beta

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