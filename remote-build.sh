git tag -a "$(cat app/src/main/resources/version.txt)" -m "Target subsystem"
git push origin "$(cat app/src/main/resources/version.txt)"