create table if not exists player_status (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp    TEXT    not null default CURRENT_TIMESTAMP,
    event        text    not null default '',
    flags        integer not null default 0,
    flags2       integer not null default 0,
    fireGroup    integer not null default 0,
    guiFocus     integer not null default 0,
    cargo        double not null default 0,
    latituge     double not null default 0,
    longitude    double not null default 0,
    heading      integer not null default 0,
    altitude     double not null default 0,
    balance      BIGINT  not null default 0,
    planetRadius double  not null default 0
);
insert into player_status (id, timestamp, event,flags,flags2,fireGroup, guiFocus,cargo,latituge,longitude,heading,altitude,balance,planetRadius)
values (1, '1984-01-01 00:00:00', 'init', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);