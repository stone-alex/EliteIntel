create table if not exists deferred_notifications (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    key          text    not null,
    timeToNotify big int not null,
    notification text    not null
);