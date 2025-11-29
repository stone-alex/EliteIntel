create table if not exists ship (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    shipName        TEXT    NOT NULL,
    shipId          INTEGER NOT NULL UNIQUE,
    shipIdentifier  TEXT,
    cargoCapacity   INTEGER,
    tradeProfileId  INTEGER,
    FOREIGN KEY(tradeProfileId) REFERENCES trade_profile(id) ON DELETE SET NULL
);

create table if not exists trade_profile (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    shipId              INTEGER NOT NULL UNIQUE,
    padSize             TEXT    CHECK(padSize IN ('S','M','L')),
    allowPlanetary      INTEGER NOT NULL DEFAULT 1,
    allowProhibited     INTEGER NOT NULL DEFAULT 0,
    allowPermit         INTEGER NOT NULL DEFAULT 0,
    allowFleetCarrier   INTEGER NOT NULL DEFAULT 1,
    defaultBudget       REAL    NOT NULL DEFAULT 0,
    maxDistanceLy       INTEGER,
    maxHops             INTEGER NOT NULL DEFAULT 15,
    FOREIGN KEY(shipId) REFERENCES ship(shipId) ON DELETE CASCADE
);