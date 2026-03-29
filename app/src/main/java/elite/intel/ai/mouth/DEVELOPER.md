# ai/mouth - Text-to-Speech & Audio Output

This package converts text to speech and plays it back through the system audio output. It supports multiple TTS providers (cloud and local), applies optional audio post-processing (de-clicker, radio filter), and handles prioritisation and interruption of queued utterances.

---

## Package Structure

```
ai/mouth/
├── MouthInterface.java                   # TTS lifecycle + VocalisationRequestEvent subscriber
├── VoiceProvider.java                    # Generic interface for voice selection logic
├── GoogleVoices.java                     # Enum of available Google Cloud TTS voices
├── KokoroVoices.java                     # Enum of available Kokoro local TTS voices
├── VoiceToAllegiances.java               # Maps voices to in-game factions/allegiances
├── AudioDeClicker.java                   # Removes click artefacts at audio boundaries
├── RadioFilter.java                      # Applies radio-transmission audio effect
│
├── google/
│   ├── GoogleVoiceProvider.java          # Voice selection logic for Google TTS
│   └── GoogleTTSImpl.java                # Google Cloud Text-to-Speech HTTP client
│
├── kokoro/
│   └── KokoroTTS.java                    # Local Kokoro TTS via sherpa-onnx
│
└── subscribers/
    ├── VocalisationRouter.java           # Converts domain events → VocalisationRequestEvent
    └── events/
        ├── BaseVoxEvent.java             # Base class: getText(), useRandomVoice()
        ├── VocalisationRequestEvent.java # Canonical TTS request (text, voice, priority, …)
        ├── VocalisationSuccessfulEvent.java
        ├── TTSInterruptEvent.java
        ├── AiVoxResponseEvent.java       # AI-generated spoken response
        ├── NavigationVocalisationEvent.java
        ├── MiningAnnouncementEvent.java
        ├── DiscoveryAnnouncementEvent.java
        ├── RouteAnnouncementEvent.java
        ├── RadioTransmissionEvent.java
        ├── RadarContactAnnouncementEvent.java
        ├── MissionCriticalAnnouncementEvent.java
        ├── AiVoxDemoEvent.java
        └── YtVoxEvent.java
```

---

## Core Interfaces & Classes

| Class                      | Responsibility                                                                                                                                                                               |
|----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `MouthInterface`           | Lifecycle (`start()` / `stop()`). Subscribes to `VocalisationRequestEvent`; owns the internal playback queue and thread.                                                                     |
| `VoiceProvider<T>`         | `getUserSelectedVoice()`, `getRandomVoice()`, `getSpeechRate(name)`, `getVoiceParams(name)`. Implemented per TTS provider.                                                                   |
| `VocalisationRouter`       | Subscribes to all domain voice events; applies per-event-type logic (check session flags, choose voice, set `isRadio`, set interruptibility) then emits a single `VocalisationRequestEvent`. |
| `VocalisationRequestEvent` | Canonical TTS request. Fields: `text`, `voiceName` (nullable = use default), `originType` (event class), `interruptible` (boolean), `isRadio` (boolean).                                     |
| `AudioDeClicker`           | Windowed fade-in / fade-out applied to PCM frames to eliminate boundary clicks.                                                                                                              |
| `RadioFilter`              | Band-pass + distortion + noise to simulate radio transmission audio. Applied when `VocalisationRequestEvent.isRadio == true`.                                                                |

---

## TTS Providers

| Provider         | Class           | Mode  | Notes                                           |
|------------------|-----------------|-------|-------------------------------------------------|
| Google Cloud TTS | `GoogleTTSImpl` | Cloud | High quality, multiple voices, requires API key |
| Kokoro           | `KokoroTTS`     | Local | Fast, no cloud dependency, sherpa-onnx backend  |

Provider is selected via
`SystemSession.useLocalTTS()`. Both run on their own internal queuing thread; the active provider subscribes to
`VocalisationRequestEvent`.

**Adding a new provider:** Implement `MouthInterface` and `VoiceProvider<T>`. Enqueue incoming
`VocalisationRequestEvent`s on an internal queue; process sequentially on a dedicated thread. Respect
`interruptible` - a non-interruptible utterance must finish before the next one starts.

---

## Event Routing (VocalisationRouter)

`VocalisationRouter` is the single point where domain-specific voice events are translated into the provider-agnostic
`VocalisationRequestEvent`:

| Source Event                       | Session Gate                    | Voice Choice              | Interruptible | Radio    |
|------------------------------------|---------------------------------|---------------------------|---------------|----------|
| `AiVoxResponseEvent`               | none                            | user-selected voice       | true          | false    |
| `MissionCriticalAnnouncementEvent` | none                            | user-selected voice       | **false**     | false    |
| `DiscoveryAnnouncementEvent`       | `isDiscoveryAnnouncementOn()`   | random                    | true          | false    |
| `MiningAnnouncementEvent`          | `isMiningAnnouncementOn()`      | fixed                     | true          | false    |
| `RadioTransmissionEvent`           | `isRadioTransmissionOn()`       | random (excl. ship voice) | true          | **true** |
| `NavigationVocalisationEvent`      | `isNavigationAnnouncementsOn()` | user-selected voice       | true          | false    |
| `RadarContactAnnouncementEvent`    | `isRadarAnnouncementsOn()`      | random                    | true          | false    |

Session gates silently drop the event if the feature is disabled in settings. No error is raised.

---

## Audio Post-Processing Pipeline

```
VocalisationRequestEvent
  └─► TTS provider synthesises PCM
        └─► AudioDeClicker.process(pcm)          # remove boundary clicks
              └─► [if isRadio] RadioFilter.apply(pcm)   # radio effect
                    └─► AudioOutputStream → speaker
                          └─► VocalisationSuccessfulEvent
```

---

## EventBus

| Direction | Event                              | Notes                                               |
|-----------|------------------------------------|-----------------------------------------------------|
| Consumed  | `VocalisationRequestEvent`         | Canonical TTS request; processed by active provider |
| Consumed  | `AiVoxResponseEvent`               | Routed by VocalisationRouter                        |
| Consumed  | `NavigationVocalisationEvent`      | Routed by VocalisationRouter                        |
| Consumed  | `MiningAnnouncementEvent`          | Routed by VocalisationRouter                        |
| Consumed  | `DiscoveryAnnouncementEvent`       | Routed by VocalisationRouter                        |
| Consumed  | `RouteAnnouncementEvent`           | Routed by VocalisationRouter                        |
| Consumed  | `RadioTransmissionEvent`           | Routed by VocalisationRouter → radio filter applied |
| Consumed  | `RadarContactAnnouncementEvent`    | Routed by VocalisationRouter                        |
| Consumed  | `MissionCriticalAnnouncementEvent` | Routed by VocalisationRouter; non-interruptible     |
| Consumed  | `AiVoxDemoEvent`                   | Demo voice playback from settings UI                |
| Consumed  | `IsSpeakingEvent`                  | Pauses/resumes playback while mic is active         |
| Published | `VocalisationSuccessfulEvent`      | Confirms playback completed                         |
| Published | `TTSInterruptEvent`                | Signals current playback should stop                |

---

## Voice & Allegiance Mapping

`VoiceToAllegiances` maps TTS voice names to Elite Dangerous factions (Federation, Empire, Alliance, Independent). The game uses this to assign consistent voices to NPC radio transmissions - e.g. a Federation station always uses a Federation voice.

---

## External Dependencies

- **Google Cloud Text-to-Speech API** - requires `google.tts.api.key` in config
- **sherpa-onnx** - native library for Kokoro local TTS
- **Java Sound API** (`javax.sound.sampled`) - PCM playback
