#!/usr/bin/env bash
##############################################################################################################################
#                                                                                                                            #
#    Mind you, this program is written to be as compatible with as many linux distrobutions as posible, therefore            #
#    I have not used any functions that can not be expected to be part of the kernel or is default shipped with many         #
#    distors, like "wget" is not a default installation contrary to curl.                                                    #
#                                                                                                                            #
#    The script is also writen with single responsibility in mind, so that if you want to test or improve                    #
#    only one part of the program you only have to coment out the functions you don't want to safely test and update the     #
#    program.                                                                                                                #
#                                                                                                                            #
#    !!! IMPORTANT !!!                                                                                                       #
#    Before testing create a backup of your EliteIntel installation and database folder.                                     #
#    Database folder path: ~/.local/share/elite-intel                                                                        #
#                                                                                                                            #
#                                                                                                                            #
#    Author: github.com/hrHVN                                                                                                #
#    Date: 30.12.25                                                                                                          #
#                                                                                                                            #
##############################################################################################################################

DEFAULT_INSTALL_LOCATION="$HOME/.var/app/elite-intel"
INSTALL_FOLDER="elite-intel"
STEAM_FOLDER=$STEAM_DIR

#	
#	Functions
#
check_java_install() {

    echo "Setting up Java..."
    if command -v java >/dev/null; then
    	local java=$(java -version 2>&1 | awk -F'"' '/version/ {print $2}' | cut -d '.' -f 1)
    	if (( $java  >= 21 )); then 
    		echo "Java up-to-date!"
    		return
    	fi
    fi

    if command -v apt >/dev/null 2>&1;			then sudo apt update 		&& sudo apt install -y openjdk-21-jre
    elif command -v dnf >/dev/null 2>&1;		then sudo dnf update 		&& sudo dnf install -y openjdk-21-jre
    elif command -v yum >/dev/null 2>&1;		then sudo yum update 		&& sudo yum install -y openjdk-21-jre
    elif command -v pacman >/dev/null 2>&1;		then sudo pacman update 	&& sudo pacman install -y openjdk-21-jre
    elif command -v zypper >/dev/null 2>&1;		then sudo zypper update 	&& sudo zypper install -y openjdk-21-jre
    elif command -v apk >/dev/null 2>&1;		then sudo apk update 		&& sudo apk install -y openjdk-21-jre
    elif command -v emerge >/dev/null 2>&1;		then sudo emerge update 	&& sudo emerge install -y openjdk-21-jre
    elif command -v slackpkg >/dev/null 2>&1;	then sudo slackppkg update 	&& sudo slackppkg install -y openjdk-21-jre
    fi

    echo "Java installed!"
}

detect_steam() {
	if [ -n "$STEAM_FOLDER" ]; then
		echo "steam folder is here $STEAM_FOLDER"
		return
	fi

	echo "Locating Steam installation..."

    if command -v flatpak >/dev/null 2>&1; then
        if flatpak list --app | grep -q '^com.valvesoftware.Steam$'; then
            STEAM_FOLDER=$(find / -type d -name com.valvesoftware.Steam)
            return
        fi
    fi

    if command -v snap >/dev/null 2>&1; then
        if snap list | awk '{print $1}' | grep -qx 'steam'; then
            STEAM_FOLDER="$HOME/snap/steam/common/.local/share/Steam"
            return
        fi
    fi

    if command -v steam >/dev/null 2>&1; then
        if [ -d "$HOME/.steam/steam" ]; then
             STEAM_FOLDER="$HOME/.steam/steam"
            return
        elif [ -d "$HOME/.local/share/Steam" ]; then
             STEAM_FOLDER="$HOME/.local/share/Steam"
            return
        fi
    fi

    echo "Steam not detected"
}

set_install_folder() {
    cat << EOF 

    Do you want to install EliteIntel with the defaultlocation?
    \$$DEFAULT_INSTALL_LOCATION

EOF

    read -rp "Continue with default: [y / n]: " OP
        case "$OP" in
            "y") ;;
            "n")
                read -rp "write the full path to the location:" newLocation
                DEFAULT_INSTALL_LOCATION="$newLocation/$INSTALL_FOLDER"
                ;;
            *)             
                log "Invalid choice: $OP"
                exit 0
            ;;
        esac
    
    #    Setup install directory
    if [ ! -d "$DEFAULT_INSTALL_LOCATION" ]; then
        mkdir $DEFAULT_INSTALL_LOCATION
    fi
    
    #    Move the files
    if [ -e "elite_intel.jar" ]; then
        mv -r ./dictionary ./logs debug.bat run.bat elite_intel.jar "$DEFAULT_INSTALL_LOCATION"
    
    elif [ -e "elite_intel*.zip" ]; then
        local ELITE_ZIP="elite_intel*.zip"
        unzip "$ELITE_ZIP" -d "$DEFAULT_INSTALL_LOCATION"
        echo "Extracted the app files to $DEFAULT_INSTALL_LOCATION"
    
    else
        echo "Couldn't find the elite_intel.jar!"
    
        curl -L -O $(curl -s https://api.github.com/repos/stone-alex/EliteIntel/releases/latest | grep "browser_download_url" | cut -d '"' -f 4)
        local ELITE_ZIP="elite_intel*.zip"
        unzip "$ELITE_ZIP" -d "$DEFAULT_INSTALL_LOCATION"
    
        echo "Re-downloaded the latest version of EliteIntel!"
    fi
}

create_bindings()   {
    echo "Creating symlinks to EliteDangerous"

    cd "$DEFAULT_INSTALL_LOCATION"

    BINDINGS_FOLDER=$(find "$STEAM_DIR/steamapps/compatdata/359320/pfx/drive_c/users/steamuser/" -type d -name Bindings)
    JOURNAL_FOLDER=$(dirname "$(find "$STEAM_DIR/steamapps/compatdata/359320/pfx/drive_c/users/steamuser/" -type f -name *.json | head -n 1)")

# Bindings
    ln -s "$BINDINGS_FOLDER" ed-bindings
# Journal
    ln -s "$JOURNAL_FOLDER" ed-journal
}

print_usage() {
    cat << EOF

    Thank you comander for trying out EliteIntell, an AI Copilot for Elite Dangerous.
    
    This script will help you to install EliteIntell on your linux flavour, it can also
    assist with updating your existing installation. Or removing it when you are done with 
    Elite Dangerous.

    Fly safe, comander!

    --------------------------------------------------------------------------------

    Basic usage:        \$bash install.sh       | Installs EliteIntel into $DEFAULT_INSTALL_LOCATION

    Optional flags:     \$bash install.sh [-h] [-u] [-d]

                -u       Updates the EliteIntel by fetching the latest update from github
                -d       Deletes the EliteIntel instalation and associated files from the computer
                -h       Displays this message.
                -T       Install Piper-TTS server or download new Piper-voice-models

EOF
    exit 0
}

update_elite_intel() {
    local ELITEINTEL_FOLDER=$DEFAULT_INSTALL_LOCATION

    echo "Is this the correct installation folder: $ELITEINTEL_FOLDER"
    read -rp "Do you want to continue with the current path? [y/n] " updatePath
    case $updatePath in
        "y") continue;;
        "n")
            read -rp "Provide the full path: " updatePath;
            ELITEINTEL_FOLDER=$updatePath
            ;;
    esac

    echo "Updating EliteIntel ..."
    curl -L -O $(curl -s https://api.github.com/repos/stone-alex/EliteIntel/releases/latest | grep "browser_download_url" | cut -d '"' -f 4)
    local ELITE_ZIP="elite_intel*.zip"
    unzip -o "$ELITE_ZIP" elite_intel.jar dictionary/stt-correction-dictionary.txt -d "$ELITEINTEL_FOLDER"
    
    rm $ELITE_ZIP

    echo "Done!"
    echo "Fly safe, comander"
    exit 0
}

delete_elite_intel() {
    local ELITEINTEL_FOLDER=$DEFAULT_INSTALL_LOCATION
    local EI_DB=$(find / -type d -name elite-intel 2>/dev/null)

    #   Is the app not in the default location?
    echo "Is this the correct installation folder: $ELITEINTEL_FOLDER"
    read -rp "Do you want to continue with the current path? [y/n] " updatePath

    case $updatePath in
        "y") continue;;
        "n")
            read -rp "Provide the full path: " updatePath;
            ELITEINTEL_FOLDER=$updatePath
            ;;
    esac

    #   validate path    
    if [ -d "$ELITEINTEL_FOLDER" ]; then
        rm -r "$ELITEINTEL_FOLDER"
    else
        echo "It seems that EliteIntel is allready deleted!"
    fi
    # Prombt for database deletion
    cat << EOF

    Do you want to remove the database to, this is non reversible damage?
    This is not recomended as it also securly stores your API-KeYs for AI, STT, TTS and EDSM. 

EOF
    read -rp "default n: [y/n]" DELETEEVERYTHING

    case $DELETEEVERYTHING in
        y)  
            rm -rf "$EI_DB"
            cat << EOF 
            
            It is all gone now!

EOF
            exit 0
            ;;
        n)  cat <<  EOF

    EliteIntel has successfully been removed from your system.

    Fly safe!

EOF
        exit 0
        ;;
    esac
    exit 0
}

create_start_menu() {
    echo ""
    echo "Creating startmenu shortcut..."
    echo "Downloading EliteIntel icon..."
    
    #   Installs the launcher in a User only enviorment
    #   It is possible to make it system wide by putinh it in: "/usr/share/applications"
    local STARTMENU_DIR="$HOME/.local/share/applications"

    curl -L -o "$DEFAULT_INSTALL_LOCATION/elite-logo.png" https://raw.githubusercontent.com/stone-alex/EliteIntel/master/app/src/main/resources/images/elite-logo.png

    bash -c "cat > $STARTMENU_DIR/elite-intel.desktop" << EOF
[Desktop Entry]
Name=Elite Intel
Comment=Elite Intel AI companion
Exec=java -jar "$DEFAULT_INSTALL_LOCATION/elite_intel.jar"
Icon="$DEFAULT_INSTALL_LOCATION/elite-logo.png"
Terminal=false
Type=Application
Categories=Game
StartupNotify=true
EOF

    chmod +x $STARTMENU_DIR/elite-intel.desktop
    update-desktop-database $HOME/.local/share/applications

}

download_piper_tts_models() {
    local en_GB=(
        "alan"
        "alba"
        "aru"
        "cori"
        "jenny_dioco"
        "northern_english_male"
        "semaine"
        "vctk"
    )

    local en_US=(
        "amy"
        "artic"
        "bryce"
        "hfc_female"
        "hfc_male"
        "joe"
        "john"
        "kristin"
        "kusal"
        "l2artic"
        "lessac"
        "libritts_r"
        "ljspeech"
        "norman"
        "reza_imbrahim"
        "ryan"
        "sam"
    )

    local NATIONALITY=en_GB
    local MODEL=amy
    echo "Let's download some voices!"

    read -rp "Choose eiteher GB or US voice list: " _LOCALE

    case $_LOCALE in
        gb) 
            for idx in "${!en_GB[@]}"; do
                printf "  %2d) %s\n " "$((idx + 1))" "${en_GB[$idx]}"
            done
            NATIONALITY=en_GB

            read -rp "select a model number [0-9]: " _MODEL
            MODEL="${en_GB[$((_MODEL-1))]}"
            ;;
        GB) 
            for idx in "${!en_GB[@]}"; do
                printf "  %2d) %s\n " "$((idx + 1))" "${en_GB[$idx]}"
            done
            NATIONALITY=en_GB
            
            read -rp "select a model number [0-9]: " _MODEL
            MODEL="${en_GB[$((_MODEL-1))]}"
            ;;

        US) 
            for idx in "${!en_US[@]}"; do
                printf "  %2d) %s\n " "$((idx + 1))" "${en_US[$idx]}"
            done
            NATIONALITY=en_US

            read -rp "select a model number [0-9]: " _MODEL
            MODEL="${en_US[$((_MODEL-1))]}";;
        us) 
            for idx in "${!en_US[@]}"; do
                printf "  %2d) %s\n " "$((idx + 1))" "${en_US[$idx]}"
            done
            NATIONALITY=en_US

            read -rp "select a model number [0-9]: " _MODEL
            MODEL="${en_US[$((_MODEL-1))]}"
            ;;
    esac

    echo "$NATIONALITY - $MODEL"   

    ## download voice models
    curl -L -o "$NATIONALITY-$MODEL-medium.onnx" "https://huggingface.co/rhasspy/piper-voices/resolve/main/en/$NATIONALITY/$MODEL/medium/$NATIONALITY-$MODEL-medium.onnx"
    curl -L -o "$NATIONALITY-$MODEL-medium.onnx.json" "https://huggingface.co/rhasspy/piper-voices/resolve/main/en/$NATIONALITY/$MODEL/medium/$NATIONALITY-$MODEL-medium.onnx.json"

    echo "Downloaded $NATIONALITY-$MODEL-medium.onnx"
}

install_local_TTS() {
    if ! command -v python3 >/dev/null 2>&1; then
        echo "ERROR: Python is not installed on this system! Please resolve this error before trying again."
        exit 0
    fi
    
    cat << EOF

    Installing Piper Text-To-Speach python server.

    You can also do this manualy as described in the wiki: 
    https://github.com/stone-alex/EliteIntel/wiki/OffLineVoiceProcessor

EOF
    
    local PIPER_FOLDER="$HOME/.var/app/python.piper-tts"

    sudo mkdir -p "$PIPER_FOLDER"
    cd $PIPER_FOLDER
    
    #
    #   Install piper-tts
    #
    python3 -m venv .
    source bin/activate

    pip install --upgrade pip
    pip install piper-tts[http]
    
    #
    #   Get Piper models
    #
    download_piper_tts_models

    local MODEL_LIST=( *.onnx )

    for idx in "${!MODEL_LIST[@]}"; do
        printf "%2d) %s\n " "$((idx + 1))" "${MODEL_LIST[$idx]}"
    done

    read -rp "Select a model to use [0-9]: " _USEME
    local USE_MODEL="${MODEL_LIST[$((_USEME-1))]}"

    #
    #   Test the Piper installation
    #

    python3 -m piper.http_server -m $USE_MODEL &
    local PYTHON_PID=$!
    
    sleep 2
    curl -X POST -H 'Content-Type: application/json' -d '{ "text": "This is a test." }' -o test.wav localhost:5000 

    if command -v aplay >/dev/null 2>&1; then
        aplay test.wav
    elif command -v paplay >/dev/null 2>&1; then
        paplay test.wav
    else
        echo "Audio player not found – test.wav saved in ${PWD}"
    fi 

    #   Kill the python
    kill "$PYTHON_PID" 2>/dev/null

    #
    #   Create a startmenue shortcut
    #
    local STARTMENU_DIR="$HOME/.local/share/applications"

    bash -c "cat > $STARTMENU_DIR/piper-tts.desktop" << EOF
[Desktop Entry]
Name=Piper-TTS
Comment=Local offline Text‑to‑Speech server (piper‑tts)
Exec=$PIPER_FOLDER/.venv/bin/python -m piper.http_server -m $USE_MODEL %F
Icon=$PIPER_FOLDER/icons/piper-tts.svg
Terminal=false
Type=Application
Categories=Game
StartupNotify=true
EOF

    chmod +x $STARTMENU_DIR/piper-tts.desktop
    update-desktop-database $HOME/.local/share/applications

    cat << EOF 
   
    Piper-TTS Installed!

    If you want to change the voice that piper uses, locate the 'piper-tts.desktop' file
    and edit the voice part of the 'Exec= ... -m $USE_MODEL ..'

    Enjoy local TTS!

EOF
    exit 0
}

#
#       Main Loop
#
while getopts ":hudT" opt; do
    case $opt in
        h) print_usage;;
        u) update_elite_intel;;
        d) delete_elite_intel;;
        T) 
            read -rp "Do you want to do a fresh install, or download new models? (choose 'y' for install) [y/n]" _PIPER
            case $_PIPER in
                y) install_local_TTS;;
                n) download_piper_tts_models;;
            esac
        ;;
        \?) echo "Error: Invalid option -$OPTARG" >&2; print_usage ;;
        :)  echo "Error: Option -$OPTARG requires an argument." >&2; print_usage ;;
    esac
done

#
#		Update repositories and install/update Java runtime
#
   check_java_install
sleep 0.2
#       Determine Steam installation
   detect_steam
sleep 0.2
#
#		Install EliteIntel
#
   set_install_folder
sleep 0.2
#
#		Create Symlink to relevant folders
#
   create_bindings
sleep 0.2
#
#       Create Startmenu shortcut
#
    create_start_menu
sleep 0.2

cat << EOF

    Thank you for downloading EliteIntel, your new copilot (not microsoft affiliated)!

    Fly dangerous and explore the galaxy!

EOF