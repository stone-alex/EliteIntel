# Linux Installer script

## Overview
This is a helper script that mainly installs EliteIntel on any linux distro, and creates the nessecary files and links to make it work. 
The script also generates a startmenu shortcut.

There are few propmts from the script, which mostly are for your convenience incase you want to install the app in a particular folder


## Get the installer script

You can download this script with put pulling the whole EliteIntel github, to simplify the install process using this command:

`$ curl -L -o elite-intel-installer.sh https://raw.githubusercontent.com/stone-alex/EliteIntel/linux/install.sh`

Make the script executable with:

`$ chmod +x elite-intel-installer.sh`



# Extra features
## Update
You can run the script with the update flag `-u`.
This downloads the latest release of EliteIntel from github.

Example: `$ bash elite-intel-installer.sh -u`

**_(it will only update the `.jar` and `Dictionary` folder.)_**


## Delete EliteIntel
You can **Delete** the app using this script, with the flag `-d`.
This will primarily remove the installation folder and prompt you for confirmation to delete **ALL** the files, which is not recommended due to the secure storage of API-KEYS, and other profile data.

Example: `$ bash elite-intel-installer.sh -d`


## Install Piper-TTS
You can use the script to get assisted installation of Piper-TTS with the flag `-T`.
It is presumed that you have `Python3` installed beforehand, most Linux distros comes preinstalld with python, but nonetheless. 

### Get more piper models
This mode can also be used to download new voice models. The script will ask you what your initial intention is.

Example: `$ bash elite-intel-installer.sh -T`

`$ Do you want to do a fresh install, or download new models? (choose 'y' for install) [y/n]`

Select `n` to download new models, otherwise you wil begin an installation process for Piper-TTS.


# Issues
Report any issues at [Discord](https://discord.gg/3qAqBENsBm) or [GitHub](https://github.com/stone-alex/EliteIntel/issues)