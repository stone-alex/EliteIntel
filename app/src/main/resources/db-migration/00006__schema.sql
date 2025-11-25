CREATE TABLE IF NOT EXISTS codex_entries (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    subCategory   TEXT    NOT NULL,
    starSystem    TEXT    NOT NULL,
    bodyId        INTEGER NOT NULL,
    latitude      REAL    NOT NULL,
    longitude     REAL    NOT NULL,
    entryName     TEXT    NOT NULL,
    voucherAmount BIGINT NOT NULL DEFAULT 0
);