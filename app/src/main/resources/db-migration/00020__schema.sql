alter table player add column homeSystemId bigint default 0;
alter table location drop column homeSystem;