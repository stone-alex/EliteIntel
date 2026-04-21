create table if not exists global_settings
(
    id                                   INTEGER PRIMARY KEY AUTOINCREMENT,
    autoSpeedUpForFtl                    boolean default false,
    autoLightsForFtl                     boolean default false,
    autoNightVisionOffForSrv             boolean default false,
    autoCargoScoopRetractForFtl          boolean default false,
    autoLandingGearUpForFtl              boolean default false,
    autoHardpointsRetractForFtl          boolean default false,
    autoGearUpOnTakeOff                  boolean default false,
    autoExitUiBeforeOpeningAnotherWindow boolean default false,
    autoLightsOffForSrvDeployment        boolean default false,
    autoFighterOutFighterDocking         boolean default false
);

insert into global_settings (id)
values (1);


create table if not exists ship_settings
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    shipId        INTEGER NOT NULL UNIQUE,
    honkTrigger   integer not null default 1,
    honkFireGroup TEXT             default 'A',
    honkOnJump    boolean          default false,
    FOREIGN KEY (shipId) REFERENCES ship (shipId) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS binding_conflicts
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    conflict_key TEXT NOT NULL UNIQUE,
    description  TEXT NOT NULL
);
