alter table game_session add column useLocalCommandLlm boolean default false;
alter table game_session add column useLocalQueryLlm boolean default false;
alter table game_session add column useLocalTTS boolean default false;