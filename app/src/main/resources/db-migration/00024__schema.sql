alter table missions add column missionType text not null default 'PIRATES';
update missions set missionType = 'PIRATES';