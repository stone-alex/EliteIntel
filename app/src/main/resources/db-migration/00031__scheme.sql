drop table pirate_factions;

create table if not exists pirate_hunting_grounds (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    starSystem    TEXT             not null unique,
    x             double precision not null,
    y             double precision not null,
    z             double precision not null,
    hasResSite    boolean          not null default false,
    ignored       boolean          not null default false,
    targetFaction TEXT
);
