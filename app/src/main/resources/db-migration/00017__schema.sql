create table if not exists trade_tuple (
    id                     INTEGER PRIMARY KEY AUTOINCREMENT,
    sourceCommodity        TEXT    not null,
    sourceStarSystem       TEXT    not null,
    sourceStationName      TEXT    not null,
    sourceStationType      TEXT    not null,
    sourceBuyPrice       INTEGER not null,
    sourceSupply           BIGINT  not null,
    destinationCommodity   TEXT    not null,
    destinationStarSystem  TEXT    not null,
    destinationStationName TEXT    not null,
    destinationStationType TEXT    not null,
    destinationSellPrice   INTEGER not null,
    destinationDemand      BIGINT  not null
);
