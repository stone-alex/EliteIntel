alter table player add column localLlmAddress text default 'http://localhost:11434';
alter table game_session add column localLlmCommandModel text default 'qwen2.5:14b';
alter table game_session add column localLlmQueryModel text default 'qwen2.5:14b';