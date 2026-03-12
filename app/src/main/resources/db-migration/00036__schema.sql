update game_session
set useLocalStt = true
where id = 1;
update game_session
set speechSpeed = 0.30
where id = 1;
update player
set localLlmAddress = 'http://localhost:11434/api/chat'
where id = 1;