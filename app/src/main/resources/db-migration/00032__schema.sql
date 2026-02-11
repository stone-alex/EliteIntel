PRAGMA foreign_keys = OFF;

BEGIN TRANSACTION;
create table if not exists ship_new (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    shipName       TEXT    NOT NULL,
    shipId         INTEGER NOT NULL UNIQUE,
    shipIdentifier TEXT,
    cargoCapacity  INTEGER
);


INSERT INTO ship_new
SELECT id, shipName, shipId, shipIdentifier, cargoCapacity
FROM ship;

DROP TABLE ship;

ALTER TABLE ship_new
RENAME TO ship;
PRAGMA foreign_keys = ON;
PRAGMA
foreign_key_check1;

COMMIT;