ALTER TABLE player_status
    ADD COLUMN pips TEXT DEFAULT NULL;
ALTER TABLE player_status
    ADD COLUMN legalState TEXT DEFAULT NULL;
ALTER TABLE player_status
    ADD COLUMN destination TEXT DEFAULT NULL;
ALTER TABLE player_status
    ADD COLUMN oxygen REAL DEFAULT NULL;
ALTER TABLE player_status
    ADD COLUMN health REAL DEFAULT NULL;
ALTER TABLE player_status
    ADD COLUMN temperature REAL DEFAULT NULL;
ALTER TABLE player_status
    ADD COLUMN selectedWeapon TEXT DEFAULT NULL;
ALTER TABLE player_status
    ADD COLUMN gravity REAL DEFAULT NULL;
