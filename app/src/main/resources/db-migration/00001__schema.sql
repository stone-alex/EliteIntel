CREATE TABLE IF NOT EXISTS materials (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    materialName TEXT NOT NULL UNIQUE,
    materialType TEXT NOT NULL,
    amount INTEGER NOT NULL DEFAULT 0,
    maxCapacity INTEGER NOT NULL DEFAULT 100000
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_material_name ON materials(materialName);

CREATE TABLE IF NOT EXISTS location (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    inGameId     INTEGER NOT NULL,
    locationName TEXT    NOT NULL UNIQUE,
    primaryStar  TEXT    NOT NULL,
    json         TEXT    NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_location_name ON location(locationName);


CREATE TABLE IF NOT EXISTS player (
    id                              INTEGER PRIMARY KEY CHECK (id = 1),
    current_primary_star            TEXT        NOT NULL DEFAULT '',
    bounty_collected_this_session   INTEGER     NOT NULL DEFAULT 0,
    carrier_departure_time          TEXT        NOT NULL DEFAULT '',
    crew_wags_payout                INTEGER     NOT NULL DEFAULT 0,
    current_ship                    TEXT        NOT NULL DEFAULT '',
    current_ship_name               TEXT        NOT NULL DEFAULT '',
    current_location_id             INTEGER,
    current_wealth                  INTEGER     NOT NULL DEFAULT 0,
    is_discovery_announcement_on    BOOLEAN     NOT NULL DEFAULT 1,
    final_destination               TEXT        NOT NULL DEFAULT '',
    game_version                    TEXT        NOT NULL DEFAULT '',
    goods_sold_this_session         INTEGER     NOT NULL DEFAULT 0,
    highest_single_transaction      INTEGER     NOT NULL DEFAULT 0,
    in_game_name                    TEXT        NOT NULL DEFAULT '',
    insurance_claims                INTEGER     NOT NULL DEFAULT 0,
    is_mining_announcement_on       BOOLEAN     NOT NULL DEFAULT 1,
    is_navigation_announcement_on   BOOLEAN     NOT NULL DEFAULT 1,
    is_radio_transmission_on        BOOLEAN,
    is_route_announcement_on        BOOLEAN     NOT NULL DEFAULT 1,
    jumping_to_star_system          TEXT        NOT NULL DEFAULT '',
    last_known_carrier_location     TEXT        NOT NULL DEFAULT '',
    last_scan_id                    INTEGER     NOT NULL DEFAULT -1,
    market_profits                  INTEGER     NOT NULL DEFAULT 0,
    personal_credits_available      INTEGER     NOT NULL DEFAULT 0,
    player_highest_military_rank    TEXT        NOT NULL DEFAULT '',
    player_mission_statement        TEXT        NOT NULL DEFAULT '',
    player_name                     TEXT        NOT NULL DEFAULT '',
    player_title                    TEXT        NOT NULL DEFAULT '',
    ship_cargo_capacity             INTEGER     NOT NULL DEFAULT 0,
    ship_fuel_level                 REAL        NOT NULL DEFAULT 0.0,
    ships_owned                     INTEGER     NOT NULL DEFAULT 0,
    species_first_logged            INTEGER     NOT NULL DEFAULT 0,
    target_market_station_id        INTEGER,
    total_bounty_claimed            INTEGER     NOT NULL DEFAULT 0,
    total_bounty_profit             INTEGER     NOT NULL DEFAULT 0,
    total_distance_traveled         REAL        NOT NULL DEFAULT 0.0,
    total_hyperspace_distance       INTEGER     NOT NULL DEFAULT 0,
    total_profits_from_exploration  INTEGER     NOT NULL DEFAULT 0,
    total_systems_visited           INTEGER     NOT NULL DEFAULT 0,
    exobiology_profits              INTEGER     NOT NULL DEFAULT 0
);

-- ensure only ever one row
INSERT OR IGNORE INTO player (id) VALUES (1);