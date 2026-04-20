update game_session
set useLocalStt = true
where id = 1;
update game_session
set speechSpeed = 0.30
where id = 1;
update player
set localLlmAddress = 'http://localhost:1234/v1/chat/completions'
where id = 1;