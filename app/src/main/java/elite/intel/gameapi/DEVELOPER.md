# gameapi - Game State & Event Integration

This package is the bridge between Elite Dangerous and the rest of EliteIntel. It reads the game's journal log files and real-time status file, parses them into typed Java events, and publishes those events on the Guava EventBus so that all other packages can react without coupling to each other.

---

## Package Structure

```
gameapi/
├── EventBusManager.java                    # Singleton Guava EventBus wrapper
│
├── journal/
│   ├── events/
│   │   ├── BaseEvent.java                  # Base class for all journal events
│   │   ├── dto/                            # Shared DTOs used across multiple events
│   │   │   ├── VoiceCommandDTO.java
│   │   │   ├── RankAndProgressDto.java
│   │   │   ├── TargetLocation.java
│   │   │   └── shiploadout/               # Ship loadout DTOs
│   │   └── (100+ event classes)           # One class per journal entry type
│   │       Examples:
│   │       LoadGameEvent, LocationEvent, DockedEvent, UndockedEvent,
│   │       StartJumpEvent, FSDJumpEvent, SupercruiseEntryEvent,
│   │       SAAScanCompleteEvent, FSSSignalDiscoveredEvent,
│   │       ProspectedAsteroidEvent, MiningRefinedEvent,
│   │       MarketBuyEvent, MarketSellEvent, CargoTransferEvent,
│   │       MissionAcceptedEvent, MissionCompletedEvent, MissionFailedEvent,
│   │       ScannedEvent, CommitCrimeEvent, BeingInterdictedEvent,
│   │       CommanderEvent, NpcCrewPaidWageEvent, ReputationEvent, …
│   │
│   ├── subscribers/
│   │   └── (20+ subscriber classes)       # @Subscribe handlers for journal events
│   │       Examples:
│   │       MiningEventSubscriber, NavRouteSetSubscriber,
│   │       CommanderEventSubscriber, CargoTransferSubscriber,
│   │       LaunchSRVSubscriber, StatisticsSubscriber, …
│   │
│   └── BioSampleDistanceCalculator.java
│
├── gamestate/
│   ├── status_events/                     # Events fired from status.json polling
│   │   ├── BeingInterdictedEvent.java
│   │   ├── InGlideEvent.java
│   │   └── PlayerMovedEvent.java
│   │
│   └── subscribers/                       # Real-time game-state monitors
│       Examples:
│       CargoChangedEventSubscriber, FuelStateSubscriber,
│       InterdictionHandler, ShipyardSubscriber, MarketSubscriber, …
│
└── data/
    ├── BioForms.java                      # Alien biology reference data
    ├── PowerDetails.java                  # Powerplay faction details
    └── PowerPlayData.java
```

---

## EventBusManager

`EventBusManager` is the single Guava `EventBus` instance for the entire application.

```java
// Publishing
EventBusManager.publish(new DockedEvent(jsonData));

// Subscribing (registration at startup via SubscriberRegistration)
@Subscribe
public void onDocked(DockedEvent event) { …}
```

- **Registration**: `SubscriberRegistration.registerSubscribers()` scans packages at startup via reflection and calls
  `EventBusManager.register(this)` on all subscriber singletons.
- **Singleton subscribers** call `EventBusManager.register(this)` in their own constructor.
- All
  `@Subscribe` methods are called on the publishing thread unless the subscriber explicitly dispatches to another thread.

---

## Journal Event System

### Source

Elite Dangerous appends JSON lines to a daily journal file:

```
~/.local/share/Frontier Developments/Elite Dangerous/journal/Journal.YYYY-MM-DD.NN.log
```

A `JournalWatcher` tail-reads this file and dispatches each JSON line to the appropriate event class based on the
`"event"` field.

### BaseEvent

All journal events extend `BaseEvent`, which provides:

- `timestamp` - ISO-8601 string from the journal
- `ttl` - optional time-to-live duration (events older than TTL are ignored)
- `eventType` - the raw journal event string

### Adding a Journal Event

1. Create a class in `gameapi/journal/events/` that extends `BaseEvent`.
2. Add it to `EventRegistry` (maps the journal `"event"` string to the class).
3. Create a subscriber in `gameapi/journal/subscribers/` with a `@Subscribe`-annotated handler.
4. The subscriber is picked up automatically by `SubscriberRegistration`.

---

## Game State (Real-time)

### Source

Elite writes a `status.json` file in the same directory, updated several times per second. A
`StatusWatcher` polls this file and publishes game-state events when values change.

### Key Real-time Events

| Event                                    | Trigger                             |
|------------------------------------------|-------------------------------------|
| `PlayerMovedEvent`                       | Ship coordinates or heading changed |
| `BeingInterdictedEvent`                  | Interdiction flag set in status     |
| `InGlideEvent`                           | Glide approach flag set             |
| `FuelStateSubscriber` (internal)         | Fuel level crosses a threshold      |
| `CargoChangedEventSubscriber` (internal) | Hold contents differ from last poll |

---

## Key Events Reference

### Navigation

| Event                                            | Description                            |
|--------------------------------------------------|----------------------------------------|
| `LoadGameEvent`                                  | Game session started / CMDR loaded     |
| `LocationEvent`                                  | Ship position on login or carrier jump |
| `StartJumpEvent`                                 | FSD jump initiated                     |
| `FSDJumpEvent`                                   | Hyperspace jump completed              |
| `SupercruiseEntryEvent` / `SupercruiseExitEvent` | SC transitions                         |
| `NavRouteEvent`                                  | Navigation route plotted               |
| `DockedEvent` / `UndockedEvent`                  | Station dock state                     |

### Exploration

| Event                      | Description                     |
|----------------------------|---------------------------------|
| `FSSSignalDiscoveredEvent` | FSS signal found                |
| `SAAScanCompleteEvent`     | Detailed surface scan completed |
| `ProspectedAsteroidEvent`  | Mining prospector result        |

### Trade & Cargo

| Event                                | Description                 |
|--------------------------------------|-----------------------------|
| `MarketBuyEvent` / `MarketSellEvent` | Commodity transactions      |
| `CargoTransferEvent`                 | Cargo moved to/from carrier |
| `MiningRefinedEvent`                 | Ore refined into cargo      |

### Missions

| Event                   | Description               |
|-------------------------|---------------------------|
| `MissionAcceptedEvent`  | Mission taken             |
| `MissionCompletedEvent` | Mission handed in         |
| `MissionFailedEvent`    | Mission expired or failed |

### Combat & Social

| Event                   | Description                     |
|-------------------------|---------------------------------|
| `ScannedEvent`          | Player scanned an NPC or player |
| `BeingInterdictedEvent` | Player being interdicted        |
| `CommitCrimeEvent`      | Crime committed                 |
| `ReputationEvent`       | Faction reputation changed      |

---

## EventBus - Published vs Consumed

This package is the **source** of game state for the whole application:

| Direction | Event                                        | Consumer                                                    |
|-----------|----------------------------------------------|-------------------------------------------------------------|
| Published | All journal events                           | `db/` managers, `brain/` context builders, `session/` state |
| Published | `PlayerMovedEvent`                           | Navigation handlers, radar                                  |
| Published | `IsSpeakingEvent` *(re-published from ears)* | `mouth/` TTS gate                                           |
| Consumed  | `UserInputEvent`                             | None in this package - published by `ears/`                 |
| Consumed  | `LoadSessionEvent`                           | Internal: triggers session initialisation                   |

---

## Game API

All game data originates from the journal file and
`status.json` - the officially documented Elite Dangerous third-party API. **No game memory reading, process injection,
or overlay techniques are used anywhere in this package.** This is a hard architectural constraint.
