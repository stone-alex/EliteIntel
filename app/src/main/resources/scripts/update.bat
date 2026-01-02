@echo off
setlocal enabledelayedexpansion
:: Move to the script's temporary location
cd /d "%~dp0"
:: Wait for the main app to close fully
timeout /t 3 /nobreak > nul

:: Set the target directory. Use %1 if provided, otherwise default to the parent folder
set "TARGET=%~1"
if "!TARGET!"=="" (
    :: Fallback: assume we are in 'scripts' folder and want to update the app root
    cd ..\..
    set "TARGET=!cd!"
    cd /d "%~dp0"
)

set "URL="
:: Extract the download URL from GitHub API
for /f "tokens=*" %%A in ('curl -s https://api.github.com/repos/stone-alex/EliteIntel/releases/latest ^| findstr "browser_download_url" ^| findstr "elite_intel" ^| findstr ".zip"') do (
    set "line=%%A"
    set "line=!line:*browser_download_url": "=!"
    set "line=!line:~0,-1!"
    set "URL=!line!"
)

if not defined URL (
    echo No update URL found!
    pause
    exit /b 1
)

echo Downloading update...
curl -L -o "update.zip" "%URL%"

if not exist "update.zip" (
    echo Failed to download update.zip
    pause
    exit /b 1
)

echo Extracting update to: !TARGET!
powershell -command "& { Expand-Archive -LiteralPath 'update.zip' -DestinationPath '!TARGET!' -Force }"

if %ERRORLEVEL% neq 0 (
    echo Extraction failed!
    pause
    exit /b 1
)

del "update.zip"

echo Updated! Restarting EliteIntel...
start "" javaw -jar "!TARGET!\elite_intel.jar"
exit