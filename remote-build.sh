git tag -a "$(cat app/src/resources/version.txt)" -m "Target subsystem"
git push origin "$(cat app/src/resources/version.txt)"