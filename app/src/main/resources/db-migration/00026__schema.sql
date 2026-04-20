alter table player
    add column localLlmAddress text default 'http://localhost:1234/v1/chat/completions';
alter table game_session
    add column localLlmCommandModel text default 'matrixportalx/tulu-3.1-8b-supernova';
alter table game_session
    add column localLlmQueryModel text default 'matrixportalx/tulu-3.1-8b-supernova';