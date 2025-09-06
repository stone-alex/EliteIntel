## Installation.

Use the zip file provided and unpack it into any directory on your computer.
You will end up with the following structure:

```
│   elite_companion.jar
│   player.conf <-- blank default
│   system.conf <-- blank default
│
└───session
player_session.json <-- blank default
system_session.json <-- blank default
```

### Install Oracle Java 17 (or later)

https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
After intalling Java, run a test command in a terminal: java -version

### Create a custom shortcut:

Right click in to the directory where you unpacked the zip file
Select "New" / "Create Shortcut"
In the field named "Type the location of the item" enter
"C:\FULL PATH TO THE\javaw.exe" -jar "C:\PATH TO THE\elite-companion.jar"

Example: ```"C:\Program Files\Java\jdk-17\bin\javaw.exe" -jar "C:\elite_companion\elite-companion.jar"```

Save the shortcut, add it to the start menu, and run it.

The app expects the journal files to be located in default location:
```C:\Users\YOUR_USERNAME\Saved Games\Frontier Developments\Elite Dangerous```
(will be configurable in the future)

See HELP tab for more info on how to get the keys for STT/TTS and xAI (currently available cloud implementation).

### Bug reports

Report issues here: https://github.com/stone-alex/elite-companion
or in Discord: 