CREATE TABLE IF NOT EXISTS fleet_carrier_route (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    leg           INTEGER NOT NULL UNIQUE,
    systemName    TEXT    NOT NULL UNIQUE,
    distance      DOUBLE  NOT NULL,
    fuelUsed      INTEGER NOT NULL,
    remainingFuel INTEGER NOT NULL,
    hasIcyRing    BOOLEAN NOT NULL,
    isPristine    BOOLEAN NOT NULL,
    x             DOUBLE  NOT NULL,
    y             DOUBLE  NOT NULL,
    z             DOUBLE  NOT NULL
);
CREATE INDEX IF NOT EXISTS carrier_route_key_index ON fleet_carrier_route(leg);

alter table location
add column homeSystem boolean default false;

create table if not exists destination_reminder (
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    json text not null
);

create table if not exists ship_route (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    leg            INTEGER NOT NULL unique,
    x              double  not null,
    y              double  not null,
    z              double  not null,
    remainingJumps integer not null,
    starClass      text    not null,
    systemName     text    not null,
    scoopable      boolean not null
);
create index if not exists ship_route_key_index on ship_route(leg);

create table if not exists game_session (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    aiPersonality    text,
    aiApiKey         text,
    aiCadence        text,
    ttsApiKey        text,
    sttApiKey        text,
    loggingEnabled   boolean,
    aiVoice          text,
    privacyModeOn    text,
    rmsThresholdHigh double default 460,
    rmsThresholdLow  double default 100
);
-- Default session for initial installation
insert or replace into game_session (id) values (1);

CREATE TABLE IF NOT EXISTS chat_history (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    json      TEXT NOT NULL,
    timestamp TEXT NOT NULL DEFAULT (datetime('now'))
);