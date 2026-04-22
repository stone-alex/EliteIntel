create table if not exists global_settings
(
    id                                   INTEGER PRIMARY KEY AUTOINCREMENT,
    autoSpeedUpForFtl                    boolean default false,
    autoLightsForFtl                     boolean default false,
    autoNightVisionOff boolean default false,
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


INSERT INTO binding_conflicts (conflict_key, description)
VALUES ('QuitCamera|UI_Back', 'UI Back and Quit Camera share a key and may interfere');
INSERT INTO binding_conflicts (conflict_key, description)
VALUES ('ExitSettlementPlacementCamera|UI_Back',
        'UI Back and Exit Settlement Placement Camera share a key and may interfere');
INSERT INTO binding_conflicts (conflict_key, description)
VALUES ('DownThrustButton|UI_Down', 'UI Down and Down Thrust Button share a key and may interfere');
INSERT INTO binding_conflicts (conflict_key, description)
VALUES ('FStopDec|UI_Left', 'UI Left and FStop Dec share a key and may interfere');
INSERT INTO binding_conflicts (conflict_key, description)
VALUES ('CyclePreviousSubsystem|UI_Left', 'UI Left and Cycle Previous Subsystem share a key and may interfere');
INSERT INTO binding_conflicts (conflict_key, description)
VALUES ('CycleNextSubsystem|UI_Right', 'UI Right and Cycle Next Subsystem share a key and may interfere');
INSERT INTO binding_conflicts (conflict_key, description)
VALUES ('FStopInc|UI_Right', 'UI Right and FStop Inc share a key and may interfere');
INSERT INTO binding_conflicts (conflict_key, description)
VALUES ('UI_Up|UpThrustButton', 'UI Up and Up Thrust Button share a key and may interfere');
INSERT INTO binding_conflicts (conflict_key, description)
VALUES ('ChangeConstructionOption|Hyperspace',
        'Hyperspace and Change Construction Option share a key and may interfere');
INSERT INTO binding_conflicts (conflict_key, description)
VALUES ('ToggleDriveAssist|ToggleFlightAssist',
        'Toggle Drive Assist and Toggle Flight Assist share a key and may interfere');
INSERT INTO binding_conflicts (conflict_key, description)
VALUES ('ToggleAdvanceMode|ToggleDriveAssist',
        'Toggle Drive Assist and Toggle Advance Mode share a key and may interfere');
INSERT INTO binding_conflicts (conflict_key, description)
VALUES ('SelectTargetsTarget|ToggleFreeCam', 'Select Targets Target and Toggle Free Cam share a key and may interfere');
