#!/usr/bin/env bash
sleep 2
cd "$(dirname "$0")"
curl -L -O $(curl -s https://api.github.com/repos/stone-alex/EliteIntel/releases/latest | grep -o '"browser_download_url": "[^"]*elite_intel[^"]*\.zip"' | cut -d'"' -f4)
ZIP=$(ls elite_intel*.zip | head -1)
unzip -o "$ZIP" elite_intel.jar dictionary/ -d "$1"
rm "$ZIP"
echo "Updated! Restarting EliteIntel..."
# Using nohup or just exec to replace the shell process
exec java -jar "$1/elite_intel.jar" &
exit