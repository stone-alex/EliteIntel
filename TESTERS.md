# Testers Instructions

*Normally you can download the app from the releases page. But if you want to test the latest changes or a specific branch
these are the steps you need to take.*


## 🐧Linux Instructions

### This is a one time setup:
Install git
See instructions for your distro: https://git-scm.com/install/linux
---

Once installed, run
```git --version```
And you should see something like
```git version 2.51.0```
Install Java 21 (LTS) on Linux
https://www.oracle.com/java/technologies/downloads/#java21

After installation run command:
```java --version```
You should see something like:
```
java 21.0.9 2025-10-21 LTS
Java(TM) SE Runtime Environment (build 21.0.9+7-LTS-338)
Java HotSpot(TM) 64-Bit Server VM (build 21.0.9+7-LTS-338, mixed mode, sharing)
```

----
### Building and running

Here is a shell file to automate the process:
```shell
#!/bin/bash
git clone https://github.com/stone-alex/EliteIntel.git
cd ~/EliteIntel
sh gradlew --no-daemon clean build
mkdir ~/ei
cp ~/EliteIntel/app/build/distributions/app-shadow.tar ~/ei/
cd ~/ei
tar -xvf app-shadow.tar
rm -Rf app-shadow/bin
cd app-shadow/lib/
cp -Rfv ~/EliteIntel/dictionary ~/ei/app-shadow/lib/
java -jar elite_intel.jar
```

This will download the sources, build the app and run it.

---
### Or do it manually. Here is a step-by-step guide:

Run this command while in your home directory (no sudo needed)
```shell 
git clone https://github.com/stone-alex/EliteIntel.git
```

or if you want to test a specific branch:

```shell
git clone -b <branch_name> https://github.com/stone-alex/EliteIntel.git
```

Change to that directory:
```shell
 cd ~/EliteIntel 
 ```


This will create EliteIntel directory and pull the sources from the github
Build the app by running build command:
```shell
 sh gradlew --no-daemon clean build 
```

This will build the package and place it in to
```shell
app/build/distributions/app-shadow.tar 
```

directory.

This directory and file will always be removed and re-built when you run the build command above. 
So perhaps you want to place stuff somewhere else to run it. Let’s create a directory

```shell
mkdir ~/ei
```

Copy the package there:

```shell
cp ~/EliteIntel/app/build/distributions/app-shadow.tar ~/ei/
```

Change to the directory from which you will run the app:

```shell
cd ~/ei
```

Extract the package:

```shell
tar -xvf app-shadow.tar
```

You should have something like this in your ~/ei directory now:
```shell
.
├── app-shadow
│   ├── bin
│        ├── app
│        └── app.bat
│   └── lib
│        └── elite_intel.jar
└── app-shadow.tar
```

You do not need the bin directory. It is an artifact of the build process.
Ignore it or remove it.

```shell
rm -Rf app-shadow/bin
```

Change to the app directory:

```shell
cd app-shadow/lib/
```

Copy the dictionary file here as well. It will help with STT mistakes:
```shell
 cp -Rfv ~/EliteIntel/dictionary ~/ei/app-shadow/lib/
```

run the app

```shell
java -jar elite_intel.jar
```

When app starts it creates this directory structure:
```shell
.
├── db
│   ├── database.db
│   ├── database.db-shm
│   └── database.db-wal
├── dictionary
│   └── stt-correction-dictionary.txt
├── elite_intel.jar
└── logs
    └── elite-intel.log
```

The database is stored in the db directory. 
When you update the app, (by re-building it from friesh source) 
replace the elite_intel.jar file with the new one.

To back up the database, copy the db directory to some other place.





------

## 🪟 Windows Instructions

This is a one-time setup:

### Install Git for Windows
Git is required to clone the repository. 
Download and install it from the official site: https://git-scm.com/download/win
Bulletproof Installation Steps:

Click the "64-bit Git for Windows Setup" link to download the installer 
(e.g., Git-2.51.0-64-bit.exe).
Run the installer as Administrator (right-click the .exe and select "Run as administrator") 
to avoid permission issues. During setup, accept the defaults for most options—they're 
programmer-friendly: Choose "Git from the command line and also from 3rd-party software" 
(adds Git to your PATH).
Select "Use MinTTY" for the terminal (better for Linux-like experience).
Enable "Git Credential Manager" for secure repo access.

If you encounter UAC prompts, approve them. Restart your Command Prompt after installation.

Verification:
Open Command Prompt (Windows Key + R) and run:
```cmd```

This opens a window terminal window. Type ```git --version``` and press Enter. 
You should see something like:
```git version 2.51.0.windows.1```

Tip: If git isn't recognized, restart your PC or manually add 
C:\Program Files\Git\cmd to your system PATH (search "Environment Variables" in Start).


### Install Java 21 (LTS) on Windows
Java 21 is required to run the app. Download the official JDK from: 
https://www.oracle.com/java/technologies/downloads/#java21
Bulletproof Installation Steps:

Under "Java SE Development Kit 21.x.x" (e.g., 21.0.9), select the x64 MSI Installer for Windows 
(file: jdk-21.0.9_windows-x64_bin.msi, ~163 MB).
Download and run the MSI as Administrator (right-click > "Run as administrator") to ensure 
a proper installation. Follow the installer prompts—accept the license and default path 
(usually C:\Program Files\Java\jdk-21.0.9). The installer automatically adds Java to your 
system PATH. If not, see manual steps below. Important: Open a new Command Prompt 
after installation (old ones won't see the PATH changes).

Verification:
In a new Command Prompt, run
```shell
java --version  
```
You should see something like:
```shell
java 21.0.9 2025-10-21 LTS  
Java(TM) SE Runtime Environment (build 21.0.9+7-LTS-338)  
Java HotSpot(TM) 64-Bit Server VM (build 21.0.9+7-LTS-338, mixed mode, sharing)  
```

If java isn't recognized:

- Manually add to PATH: Search "Environment Variables" in 
Start > System Properties > Advanced > Environment Variables > Edit "Path" 
under System Variables > Add C:\Program Files\Java\jdk-21.0.9\bin > OK. Restart Command Prompt.


- Check for multiple Javas: Run where java to see the active one. If wrong, prioritize Java 21 in PATH.
Pitfall: Antivirus might flag the installer temporarily disable or allow it.

Note on tar Command: Windows 10 (build 17063+) and Windows 11 have tar built-in. 
To check your build: Run winver in Command Prompt. If older, install 7-Zip 
and use its 7z x app-shadow.tar instead of tar.

---

## Building and Running
All commands below are for Command Prompt (cmd.exe)—not PowerShell, to match Linux simplicity. Run as a regular user (no admin needed). Start in your home directory: Run cd %USERPROFILE% in Command Prompt.


Clone the Repository
```shell
git clone https://github.com/stone-alex/EliteIntel.git  
```

Or for a specific branch:

```shell
git clone -b <branch_name> https://github.com/stone-alex/EliteIntel.git  
```

This creates an EliteIntel folder in %USERPROFILE% (e.g., C:\Users\YourName\EliteIntel).
Change to that directory:

```shell
cd EliteIntel  
```

Build the App
The Gradle wrapper (gradlew.bat) downloads itself on first run.
```shell
gradlew.bat --no-daemon clean build  
```

- This builds the app and creates app\build\distributions\app-shadow.tar.
- First build takes time (downloads dependencies)—be patient, no internet issues.
- If errors: Ensure Java 21 is active (java --version) and Git is in PATH.

The tar file is rebuilt each time, so copy it elsewhere for running. Create an install folder:

```shell
mkdir %USERPROFILE%\ei  
```
Copy package:

```shell
copy app\build\distributions\app-shadow.tar %USERPROFILE%\ei\  
```

Change to the run directory:

```shell
cd %USERPROFILE%\ei  
```

```shell
cd %USERPROFILE%\ei  
```

tar -xf app-shadow.tar  

- You should now have an app-shadow folder.
- If tar fails (older Windows): Install 7-Zip, then run 7z x app-shadow.tar.
- Or alternatively use winrar to extract the tar file.

Your %USERPROFILE%\ei directory should look like:
```shell
.  
├── app-shadow  
│   ├── bin  
│   │   ├── app.bat  
│   │   └── app  
│   └── lib  
│       └── elite_intel.jar  
└── app-shadow.tar  
```

The bin folder is a build artifact—remove it (Java doesn't need it):

```shell
rmdir /S /Q app-shadow\bin  
```

Change to the dictionary folder:

```shell
cd app-shadow\lib  
```
Copy the dictionary (helps with speech-to-text corrections):

```shell
xcopy /E /I /Y %USERPROFILE%\EliteIntel\dictionary %USERPROFILE%\ei\app-shadow\lib\dictionary  
```

Run the App:
```shell
java -jar elite_intel.jar  
```

- For better performance on big logs: java -XX:+UseZGC -Xmx4g -jar elite_intel.jar.
- The app creates: db (SQLite database), dictionary, logs.

When the app starts, it creates this structure in %USERPROFILE%\ei\app-shadow\lib\:

```shell
.  
├── db  
│   ├── database.db  
│   ├── database.db-shm  
│   └── database.db-wal  
├── dictionary  
│   └── stt-correction-dictionary.txt  
├── elite_intel.jar  
└── logs  
    └── elite-intel.log  
```

The database lives in db. For updates: Rebuild and replace elite_intel.jar (keep db for data).
Backup: Copy the db folder elsewhere.

### Troubleshooting Builds/Runs
- Permission errors: Run Command Prompt as Administrator once to init Gradle cache.
Path too long: Use short paths or enable long path support 
(regedit: HKLM\SYSTEM\CurrentControlSet\Control\FileSystem\LongPathsEnabled=1).

- Antivirus blocks: Add exclusions for %USERPROFILE%\EliteIntel and java.exe.

- Multiple terminals: Always open a new Command Prompt after PATH changes.

- Logs: Check elite-intel.log for issues.

Automated Batch File (Recommended for Testers)
Save this as build_eliteintel.bat in %USERPROFILE%, then double-click or run build_eliteintel.bat in Command Prompt. 
It handles clone/update, build, extract, and launch.

```shell

@echo off  
setlocal EnableDelayedExpansion  

:: Config: Change if needed  
set "REPO=%USERPROFILE%\EliteIntel"  
set "INSTALL=%USERPROFILE%\ei"  

echo =================================================  
echo  EliteIntel - Windows Bulletproof Builder (Java 21)  
echo =================================================  

:: Clone or update repo  
if exist "%REPO%" (  
    echo Updating existing repo...  
    cd /d "%REPO%" && git pull --quiet  
) else (  
    echo Cloning repo...  
    git clone --depth 1 https://github.com/stone-alex/EliteIntel.git "%REPO%"  
    cd /d "%REPO%"  
)  

:: Build (skips tests for speed)  
echo Building with Gradle...  
call gradlew.bat --no-daemon clean build -x test  
if errorlevel 1 (  
    echo Build failed :( Check Java version and internet.  
    pause  
    exit /b 1  
)  

:: Fresh install (removes old for clean test)  
if exist "%INSTALL%" rmdir /S /Q "%INSTALL%"  
mkdir "%INSTALL%"  

:: Extract tar  
echo Extracting...  
cd /d "%INSTALL%"  
tar -xf "%REPO%\app\build\distributions\app-shadow.tar"  
:: Fallback for old Windows: Uncomment if needed  
:: "%ProgramFiles%\7-Zip\7z.exe" x "%REPO%\app\build\distributions\app-shadow.tar" -o"%INSTALL%"  

:: Remove bin artifact  
rmdir /S /Q app-shadow\bin 2>nul  

:: Copy dictionary  
echo Copying dictionary...  
xcopy /E /I /Y "%REPO%\dictionary" "app-shadow\lib\dictionary"  

:: Launch  
echo.  
echo Starting EliteIntel—enjoy the intel grind!  
cd app-shadow\lib  
java -XX:+UseZGC -Xmx4g -jar elite_intel.jar  

pause  
```
