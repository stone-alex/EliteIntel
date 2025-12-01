create table if not exists ship (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    shipName       TEXT    NOT NULL,
    shipId         INTEGER NOT NULL UNIQUE,
    shipIdentifier TEXT,
    cargoCapacity  INTEGER,
    tradeProfileId INTEGER,
    FOREIGN KEY (tradeProfileId) REFERENCES trade_profile(id) ON DELETE SET NULL
);

create table if not exists trade_profile (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    shipId            INTEGER NOT NULL UNIQUE,
    padSize           TEXT CHECK (padSize IN ('S', 'M', 'L')),
    allowPlanetary    INTEGER NOT NULL DEFAULT 0,
    allowProhibited   INTEGER NOT NULL DEFAULT 0,
    allowPermit       INTEGER NOT NULL DEFAULT 0,
    allowFleetCarrier INTEGER NOT NULL DEFAULT 0,
    startingBudget    integer NOT NULL DEFAULT 0,
    maxDistanceLs     INTEGER,
    maxJumps          INTEGER NOT NULL DEFAULT 15,
    FOREIGN KEY (shipId) REFERENCES ship(shipId) ON DELETE CASCADE
);
create index if not exists trade_profile_ship_id_index on trade_profile(shipId);

create table if not exists trade_route (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    legNumber         INTEGER not null unique,
    starSystem              text    not null,
    commodityInfoJson text    not null,
    stationInfoJson   text    not null
);