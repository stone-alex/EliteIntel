alter table game_session
    add column useLocalCommandLlm boolean default true;
alter table game_session
    add column useLocalQueryLlm boolean default true;
alter table game_session
    add column useLocalTTS boolean default true;