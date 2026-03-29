# db - Persistent Data Storage

This package provides all SQLite persistence for EliteIntel. It follows a strict two-layer pattern: **DAO
** (data access only, no logic) and **Manager
** (business logic, caching, event publishing). Consumers always call Managers, never DAOs directly.

---

## Package Structure

```
db/
├── util/
│   ├── Database.java              # Singleton: HikariCP pool, Jdbi setup, DAO registration
│   └── DatabaseMigrator.java      # Versioned schema migration runner
│
├── dao/                           # ~35 DAO interfaces (@SqlObject)
│   Examples:
│   PlayerDao, ShipDao, CargoDao, TradeRouteDao, LocationDao,
│   MissionDao, StationMarketDao, FleetCarrierDao, FleetCarrierRouteDao,
│   BrainTreesDao, BioSampleDao, CodexEntryDao, MaterialsDao,
│   BountyDao, KeyBindingDao, ChatHistoryDao, ReminderDao, …
│
└── managers/                      # ~30 Manager singletons
    Examples:
    PlayerManager, ShipManager, CargoHoldManager, TradeRouteManager,
    LocationManager, MissionManager, FleetCarrierManager,
    FleetCarrierRouteManager, BrainTreeManager, BioSamplesManager,
    CodexEntryManager, MaterialManager, BountyManager, ReminderManager,
    KeyBindingManager, MonetizeRouteManager, …
```

---

## Database Configuration

```
JDBC URL:    jdbc:sqlite:{appDataPath}/database.db
Pool:
  maxPoolSize      = 10
  minIdle          = 2
  connectionTimeout = 30 s
  idleTimeout      = 10 min
  maxLifetime      = 30 min
SQLite pragmas (applied at connection open):
  journal_mode     = WAL          # concurrent reads during writes
  synchronous      = NORMAL       # safe on Linux; faster than FULL
  foreign_keys     = ON
  case_sensitive_like = OFF
```

`Database` is a singleton. All DAOs are registered with Jdbi once at startup in
`Database.init()`. Consumers obtain DAO instances via `Database.getInstance().getJdbi().onDemand(FooDao.class)`.

---

## Schema Migrations

`DatabaseMigrator` applies versioned SQL scripts from:

```
app/src/main/resources/db-migration/V{n}__description.sql
```

Migrations are numbered sequentially (currently 36 files). They run automatically at startup before any DAO is used. *
*Never modify an existing migration file** - add a new numbered file instead.

---

## DAO Layer

DAOs are plain Jdbi `@SqlObject` interfaces. They know nothing about business logic, session state, or events.

```java
public interface CargoDao {
    @SqlQuery("SELECT * FROM cargo WHERE cmdr_id = :cmdrid")
    List<CargoItem> getCargo(@Bind("cmdrid") String cmdrId);

    @SqlUpdate("INSERT OR REPLACE INTO cargo …")
    void upsertCargo(@BindBean CargoItem item);
}
```

- One DAO per domain entity (or closely related group of entities)
- Methods map 1:1 with SQL statements - no logic
- Return raw entities or primitives; no event publishing

---

## Manager Layer

Managers are singletons that add business logic on top of a DAO:

- Cache frequently-read data in memory to avoid repeated DB hits
- Coordinate multi-table operations transactionally
- Publish EventBus events when state changes (e.g. cargo updated)
- Expose a clean API to the rest of the application

```java
public class CargoHoldManager {
    private static final CargoHoldManager INSTANCE = new CargoHoldManager();
    private final CargoDao dao = Database.getInstance().getJdbi().onDemand(CargoDao.class);
    private List<CargoItem> cache;

    public List<CargoItem> getCargo() {
        if (cache == null) cache = dao.getCargo(session.getCmdrId());
        return cache;
    }

    public void updateCargo(List<CargoItem> items) {
        dao.upsertCargo(items);
        cache = items;
        EventBusManager.publish(new CargoChangedEvent(items));
    }
}
```

---

## Data Flow: Write Path

Game events → DB → session state update:

```
GameAPI journal event (e.g. MarketBuyEvent)
  └─► Journal subscriber @Subscribe handler
        └─► Manager.update*(data)
              ├─ DAO.upsert*(entity)     → SQLite write
              ├─ Updates in-memory cache
              └─ EventBusManager.publish(StateChangedEvent)
```

## Data Flow: Read Path

AI handler needs current game state:

```
brain/ QueryHandler.handle()
  └─► Manager.get*()
        ├─ Cache hit  → return cached value (no DB hit)
        └─ Cache miss → DAO.get*() → SQLite read → cache → return
```

---

## Manager Inventory

| Manager                    | Domain                                         |
|----------------------------|------------------------------------------------|
| `PlayerManager`            | CMDR profile, ranks, credits                   |
| `ShipManager`              | Active ship loadout, stored ships              |
| `CargoHoldManager`         | Cargo hold contents                            |
| `TradeRouteManager`        | Cached Spansh trade routes + filter logic      |
| `LocationManager`          | Notable/bookmarked locations                   |
| `MissionManager`           | Active and completed missions                  |
| `FleetCarrierManager`      | Fleet carrier status and inventory             |
| `FleetCarrierRouteManager` | Carrier jump schedule history                  |
| `BrainTreeManager`         | Guardian site locations                        |
| `BioSamplesManager`        | Alien biology sample collection                |
| `CodexEntryManager`        | Codex exploration log                          |
| `MaterialManager`          | Engineering raw/encoded/manufactured materials |
| `BountyManager`            | Active bounties and vouchers                   |
| `ReminderManager`          | In-game reminders and notifications            |
| `KeyBindingManager`        | Persisted key binding overrides                |
| `MonetizeRouteManager`     | Route profitability calculations               |

---

## API Key Storage

API keys (Google TTS, OpenAI, etc.) are stored encrypted in SQLite via `Cypher`. Accessed through
`ConfigManager.getInstance().getSystemKey(KeyName.*)`. **Never log or transmit API keys.
** Never query the key columns in plain-text SQL outside of `ConfigManager`.

---

## EventBus

Managers publish domain change events; they do not consume events directly. Event consumption happens in
`gameapi/` subscribers which then call manager update methods.

| Published by       | Event                 | Consumers                 |
|--------------------|-----------------------|---------------------------|
| `CargoHoldManager` | `CargoChangedEvent`   | UI, brain context builder |
| `FuelStateManager` | `FuelLowEvent`        | mouth (TTS warning)       |
| `MissionManager`   | `MissionExpiredEvent` | mouth, UI                 |
| Various managers   | `SensorDataEvent`     | brain (LLM context)       |
