BEGIN;
ALTER TABLE game_session
    ADD COLUMN localLlmProvider VARCHAR(255) NOT NULL DEFAULT 'LMSTUDIO';
ALTER TABLE game_session
    ADD COLUMN localLlmAddress VARCHAR(255) NOT NULL DEFAULT 'http://localhost:1234/v1/chat/completions';
UPDATE game_session
SET localLlmAddress = (SELECT localLlmAddress FROM player WHERE player.id = 1)
WHERE game_session.id = 1;
COMMIT;
