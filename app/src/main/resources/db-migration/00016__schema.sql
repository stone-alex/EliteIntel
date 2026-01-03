create table if not exists sub_system (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    subsystem TEXT not null unique
);
create index if not exists sub_system_subsystem_idx on sub_system(subsystem);

insert into sub_system (subsystem) values ('Beam Laser');
insert into sub_system (subsystem) values ('Cannon');
insert into sub_system (subsystem) values ('Cargo Hatch');
insert into sub_system (subsystem) values ('Collector');
insert into sub_system (subsystem) values ('Drive');
insert into sub_system (subsystem) values ('ECM');
insert into sub_system (subsystem) values ('FSD Interdictor');
insert into sub_system (subsystem) values ('Hatch Breaker');
insert into sub_system (subsystem) values ('Heatsink');
insert into sub_system (subsystem) values ('Life Support');
insert into sub_system (subsystem) values ('Manifest Scanner');
insert into sub_system (subsystem) values ('Missile Rack');
insert into sub_system (subsystem) values ('Point Defence Turret');
insert into sub_system (subsystem) values ('Power Plant');
insert into sub_system (subsystem) values ('Shield Booster');
insert into sub_system (subsystem) values ('Shield Cell Bank');
insert into sub_system (subsystem) values ('Shield Generator');


