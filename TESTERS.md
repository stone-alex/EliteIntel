## Elite Intel

### Bug fixes

- Traced down the problem when app responds with "Unable to Reach Ollama" and similar messages where llm would fail to
  process response.
  The problem was with concurrent prompt processing. Local llm prompts are now put in processing queue and are send in
  sequence.
  The same problem cause some of the background events to miss-fire which lead to missing recrods about the game play
  and environment.

- Found and fixed problem no star coordinates problem - (major bug - hot fix posted earlier)

- Changed the listen / ignore feature so user can interract with the ship computer in a more natural way. "ignore me"
  will cause the appp to ignore user input unless words "listen" or "computer" are used. The ignore mode can be turned
  off via voice command "ship stop ignoring me", or "listen up, do not ignore me" etc.

