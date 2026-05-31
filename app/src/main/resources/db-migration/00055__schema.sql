CREATE TABLE IF NOT EXISTS squadron_carrier_route
(
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
CREATE INDEX IF NOT EXISTS squadron_carrier_route_key_index ON squadron_carrier_route (leg);
