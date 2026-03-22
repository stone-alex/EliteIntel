BEGIN;
ALTER TABLE game_session
    ADD COLUMN localLlmProvider VARCHAR(255) NOT NULL DEFAULT 'OLLAMA';
ALTER TABLE game_session
    ADD COLUMN localLlmAddress VARCHAR(255) NOT NULL DEFAULT 'http://localhost:11434/api/chat';
UPDATE game_session
SET localLlmAddress = (SELECT localLlmAddress FROM player WHERE player.id = 1)
WHERE game_session.id = 1;
COMMIT;
