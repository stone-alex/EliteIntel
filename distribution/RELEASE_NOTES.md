## Elite Intel v0.0308-beta

## ⚠ Windows Users:

- There is a new version 307. The installer (not the updater) contains the possible updater fix. Please update to 307
  manually by running installer, and let me know if the update works for the version after that.

## Linux Users:

- This change does not affect updater on linux. (As far as I know)

### bug fixes:

- Performance improvements
- Introduced Virtual threads, so game events are processed in a more predictable manner.
- Fixed EDSM blocking calls, the app will no longer be blocked by EDSM failed calls.
- Introduced adjustable threads to Whisper processing. This allows for faster STT transcriptions on more powerful
  hardware

- Location coordinates were not save correctly in some corner cases
- Fleet Carrier jump location / ETA was broken.
- UI navigation improvements
- You can now tell the app to ignore you by saying "ignore me" and turn that back on by saying "listen to me" (or
  something to that effect, as long as words 'ignore' or 'listen' present it will trigger. you can still issue commands
  in ignore mode by mixing in word 'ship' in to your command. Hopefully that flows more naturally.
- Some TTS corrections (quirks of Whisper when it transcribes 'exit' as 'thank you' for some reason, and others like it.
- Fix for qwen3.5 and other local LLMs where it would cancel previous prompt while issuing a new one assyncronously. the
  prompts are now queued and are processed sequentially in the order they arrive.
- Some other minor fixes.