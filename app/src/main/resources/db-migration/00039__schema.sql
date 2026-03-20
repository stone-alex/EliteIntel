alter table game_session
    add column sttThreads integer not null default 4;
alter table ship
    add column voice text not null default 'EMMA';
update ship
set voice = 'EMMA'
where id > 0;