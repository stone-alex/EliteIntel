@echo off
cd /d "%~dp0"
:: Wait for the Java process to close and release the file lock
timeout /t 2 /nobreak > nul
for /f "delims=" %%u in ('curl -s https://api.github.com/repos/stone-alex/EliteIntel/releases/latest ^| findstr browser_download_url ^| findstr elite_intel.*\.zip') do (
    set "URL=%%u"
)
set "URL=%URL:~22,-1%"
curl -L -o update.zip "%URL%"

powershell -command "Expand-Archive -Force 'update.zip' '%1'"
del update.zip

echo Updated! Restarting EliteIntel...
:: Use start to launch the new process independently
start javaw -jar "%1\elite_intel.jar"
exit