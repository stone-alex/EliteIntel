CREATE TABLE IF NOT EXISTS ship_scans (
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    key  TEXT NOT NULL UNIQUE,
    scan TEXT
);
CREATE INDEX IF NOT EXISTS scan_key_index ON ship_scans(key);


CREATE TABLE IF NOT EXISTS missions (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    key     BIGINT NOT NULL UNIQUE,
    mission TEXT
);
CREATE INDEX IF NOT EXISTS mission_key_index ON missions(key);


CREATE TABLE IF NOT EXISTS bounties (
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    key    TEXT NOT NULL UNIQUE,
    bounty TEXT
);
CREATE INDEX IF NOT EXISTS bounty_key_index ON bounties(key);


CREATE TABLE IF NOT EXISTS mining_targets (
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    target TEXT NOT NULL UNIQUE
);


CREATE TABLE IF NOT EXISTS station_markets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    json TEXT,
    stationName TEXT,
    marketId BIGINT NOT NULL UNIQUE
);
CREATE INDEX IF NOT EXISTS station_market_index ON station_markets(stationName);


CREATE TABLE IF NOT EXISTS ranks_and_progress (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    json TEXT
);

CREATE TABLE IF NOT EXISTS fleet_carrier (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    json TEXT
);


CREATE TABLE IF NOT EXISTS bio_samples (
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    key    TEXT NOT NULL UNIQUE,
    json TEXT
);
CREATE INDEX IF NOT EXISTS bio_samples_key_index ON bio_samples(key);


CREATE TABLE IF NOT EXISTS ship_loadout (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    json TEXT
);
CREATE INDEX IF NOT EXISTS ship_loadout_index ON ship_loadout(id);


CREATE TABLE IF NOT EXISTS genus_payment_announcement (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    genus TEXT NOT NULL UNIQUE ,
    isOn BOOLEAN
);
CREATE INDEX IF NOT EXISTS genus_payment_announcement_index ON genus_payment_announcement(genus);


CREATE TABLE IF NOT EXISTS cargo (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    json TEXT
);


CREATE TABLE IF NOT EXISTS reputation(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    json TEXT
);


CREATE TABLE IF NOT EXISTS target_location (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    json TEXT
);

CREATE TABLE IF NOT EXISTS fsd_target (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    json TEXT
);

