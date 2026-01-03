alter table game_session add column encryptedLLMKey text;
alter table game_session add column encryptedSTTKey text;
alter table game_session add column encryptedTTSKey text;
alter table game_session add column encryptedEDSSMKey text;