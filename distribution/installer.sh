#!/usr/bin/env bash
##############################################################################################################################
#                                                                                                                            #
#    Mind you, this program is written to be as compatible with as many linux distributions as possible, therefore            #
#    I have not used any functions that can not be expected to be part of the kernel, or is by default shipped with many     #
#    distors, like "wget" is not a default installation contrary to curl.                                                    #
#                                                                                                                            #
#    The script is also written with single responsibility in mind, so that if you want to test or improve                    #
#    only one part of the program you only have to comment out the functions you don't want to safely test and update the     #
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

DEFAULT_INSTALL_LOCATION="$HOME/.var/app/elite.intel.app"
INSTALL_FOLDER="elite.intel.app"
STEAM_FOLDER=""

# Oracle JDK 21 LTS - bundled with the app, not dependent on system Java.
# Direct download; no login/paywall for this rolling "latest" URL.
JDK_URL="https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz"
JDK_DIR="jdk"

#
#	Functions
#

install_jdk() {
    local JDK_PATH="$DEFAULT_INSTALL_LOCATION/$JDK_DIR"

    # Skip download if a bundled JDK is already present from a previous install
    if [ -x "$JDK_PATH/bin/java" ]; then
        echo "Bundled JDK already present, skipping download."
        return
    fi

    echo "Downloading bundled JDK (Oracle JDK 21)..."
    mkdir -p "$JDK_PATH"

    if ! curl -L -o /tmp/ei_jdk.tar.gz "$JDK_URL"; then
        echo "ERROR: Failed to download JDK. Check your internet connection."
        exit 1
    fi

    tar -xzf /tmp/ei_jdk.tar.gz -C "$JDK_PATH" --strip-components=1
    rm -f /tmp/ei_jdk.tar.gz

    if [ ! -x "$JDK_PATH/bin/java" ]; then
        echo "ERROR: JDK extraction failed. Installation cannot continue."
        exit 1
    fi

    echo "Bundled JDK installed: $("$JDK_PATH/bin/java" -version 2>&1 | head -1)"
}

detect_steam() {
    echo "Locating Steam installation..."

    if command -v steam >/dev/null 2>&1; then
        echo "looking in home-folder ..."

        if [ -d "$HOME/.steam/steam" ]; then
             STEAM_FOLDER="$HOME/.steam/steam"
            return
        elif [ -d "$HOME/snap/steam/common/.local/share/Steam" ]; then
            STEAM_FOLDER="$HOME/snap/steam/common/.local/share/Steam"
            return
        elif [ -d "$HOME/.local/share/Steam" ]; then
             STEAM_FOLDER="$HOME/.local/share/Steam"
            return
        elif [ -d "$HOME/.var/app/com.valvesoftware.Steam/.local/share/Steam" ]; then
            STEAM_FOLDER="$HOME/.var/app/com.valvesoftware.Steam/.local/share/Steam"
            return
        fi
    fi

    if command -v flatpak >/dev/null 2>&1; then
        echo "Testing flatpak ..."

        if flatpak list --app | grep -q 'com.valvesoftware.Steam'; then
            STEAM_FOLDER="$HOME/.var/app/com.valvesoftware.Steam/.local/share/Steam"
            return
        fi
    fi

    if command -v snap >/dev/null 2>&1; then
        echo "Testing snap ..."

        if snap list | awk '{print $1}' | grep -qx 'steam'; then
            STEAM_FOLDER="$HOME/snap/steam/common/.local/share/Steam/"
            return
        fi
    fi

    echo "Steam not detected"
}

fetch_latest_zip_url() {
    local URL
    URL=$(curl -sf "https://api.github.com/repos/stone-alex/EliteIntel/releases" \
        | grep '"browser_download_url"' \
        | grep '\.zip' \
        | grep -v '\.exe' \
        | head -1 \
        | cut -d '"' -f 4)

    if [ -z "$URL" ]; then
        echo "ERROR: Could not resolve download URL. Check your internet connection or GitHub API rate limit." >&2
        exit 1
    fi

    echo "Resolved download URL: $URL" >&2
    echo "$URL"
}

set_install_folder() {
    cat << EOF

    Do you want to install EliteIntel with the default location?
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
        mkdir "$DEFAULT_INSTALL_LOCATION"
    fi

    echo "Downloading latest EliteIntel release from GitHub..."
    local DOWNLOAD_URL
    DOWNLOAD_URL=$(fetch_latest_zip_url)

    if ! curl -L -o /tmp/elite_intel.zip "$DOWNLOAD_URL"; then
        echo "ERROR: Download failed."
        exit 1
    fi

    unzip -o /tmp/elite_intel.zip -d "$DEFAULT_INSTALL_LOCATION"
    rm -f /tmp/elite_intel.zip

    echo "Downloaded and installed the latest version of EliteIntel!"
}

create_bindings() {
    echo "Creating symlinks to Elite Dangerous"

    local PREFIX="$STEAM_FOLDER/steamapps/compatdata/359320/pfx/drive_c/users/steamuser"

    BINDINGS_FOLDER=$(find "$PREFIX/AppData/Local" -type d -path "*/Frontier Developments/Elite Dangerous/Options/Bindings" 2>/dev/null | head -n1)
    JOURNAL_FOLDER=$(find "$PREFIX/Saved Games" -type d -path "*/Frontier Developments/Elite Dangerous" 2>/dev/null | head -n1)

    if [ -z "$BINDINGS_FOLDER" ] || [ -z "$JOURNAL_FOLDER" ]; then
        echo "Folders not found. Launch Elite Dangerous once via Steam to create them."
        return 1
    fi

    # Remove any nested symlinks that previous ln -sf runs may have created
    # inside the real game directories (e.g. Bindings/Bindings/Bindings/...)
    find "$BINDINGS_FOLDER" -maxdepth 6 -type l -name "$(basename "$BINDINGS_FOLDER")" -delete 2>/dev/null
    find "$JOURNAL_FOLDER"  -maxdepth 6 -type l -name "$(basename "$JOURNAL_FOLDER")"  -delete 2>/dev/null

    cd "$DEFAULT_INSTALL_LOCATION"

    rm -f ed-bindings
    ln -s "$BINDINGS_FOLDER" ed-bindings
    rm -f ed-journal
    ln -s "$JOURNAL_FOLDER" ed-journal

    echo "Symlinks created: ed-bindings -> $BINDINGS_FOLDER"
    echo "                  ed-journal -> $JOURNAL_FOLDER"
}

print_usage() {
    cat << EOF

    Thank you commander for trying out EliteIntel, an AI Copilot for Elite Dangerous.

    This script will help you to install EliteIntel on your linux flavour, it can also
    assist with updating your existing installation. Or removing it when you are done with
    Elite Dangerous.

    Fly dangerous, commander!

    --------------------------------------------------------------------------------

    Basic usage:        \$bash install.sh       | Installs EliteIntel into $DEFAULT_INSTALL_LOCATION

    Optional flags:     \$bash install.sh [-h] [-u] [-d]

                -u       Updates the EliteIntel by fetching the latest update from github
                -d       Deletes the EliteIntel installation and associated files from the computer
                -h       Displays this message.

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
            ELITEINTEL_FOLDER="$updatePath"
            ;;
    esac

    echo "Updating EliteIntel ..."
    local DOWNLOAD_URL
    DOWNLOAD_URL=$(fetch_latest_zip_url)

    if ! curl -L -o /tmp/elite_intel.zip "$DOWNLOAD_URL"; then
        echo "ERROR: Download failed."
        exit 1
    fi

    # Update all distributed assets - jar, dictionary, models and native libs.
    # The bundled JDK lives in $ELITEINTEL_FOLDER/jdk/ and is preserved across updates
    # unless explicitly replaced.
    unzip -o /tmp/elite_intel.zip -d "$ELITEINTEL_FOLDER"

    rm -f /tmp/elite_intel.zip

    echo "Done!"
    echo "Fly Dangerous, commander"
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
        echo "It seems that EliteIntel is already deleted!"
    fi

    # Remove the start menu shortcut and launcher script
    local STARTMENU_DIR="$HOME/.local/share/applications"
    rm -f "$STARTMENU_DIR/elite-intel.desktop"
    rm -f "$DEFAULT_INSTALL_LOCATION/elite-intel.sh"
    command -v update-desktop-database >/dev/null && update-desktop-database "$STARTMENU_DIR"

    # Prompt for database deletion
    cat << EOF

    Do you want to remove the database to, this is non reversible damage?
    This is not recommended as it also securely stores your API-KeYs for AI, STT, TTS and EDSM.

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

    Fly Dangerous!

EOF
        exit 0
        ;;
    esac
    exit 0
}

create_start_menu() {
    echo "Creating desktop shortcut..."

    local DESKTOP_DIR="$HOME/.local/share/applications"
    local DESKTOP_FILE="$DESKTOP_DIR/elite-intel.desktop"
    local ICON_PATH="$DEFAULT_INSTALL_LOCATION/elite-logo.png"
    local LAUNCHER="$DEFAULT_INSTALL_LOCATION/elite-intel.sh"
    local NATIVE_DIR="$DEFAULT_INSTALL_LOCATION/native/sherpa-onnx"
    local JAVA_BIN="$DEFAULT_INSTALL_LOCATION/$JDK_DIR/bin/java"

    mkdir -p "$DESKTOP_DIR"

    # Download icon only if missing
    [ -f "$ICON_PATH" ] || curl -L -o "$ICON_PATH" https://raw.githubusercontent.com/stone-alex/EliteIntel/master/app/src/main/resources/images/elite-logo.png

    # Launcher script - uses the bundled Oracle JDK so system Java version is irrelevant.
    # Also sets LD_LIBRARY_PATH so
    # sherpa-onnx JNI resolves libsherpa-onnx-jni.so by name (required
    # by JDK 21+, cannot be done post-launch). DISPLAY fallback handles
    # Wayland desktops where the .desktop launcher may not inherit $DISPLAY.
    cat > "$LAUNCHER" << 'LAUNCHEOF'
#!/usr/bin/env bash
INSTALL_DIR="INSTALL_DIR_PLACEHOLDER"
NATIVE_DIR="NATIVE_DIR_PLACEHOLDER"
JAVA_BIN="JAVA_BIN_PLACEHOLDER"

export LD_LIBRARY_PATH="$NATIVE_DIR:$LD_LIBRARY_PATH"
export DISPLAY="${DISPLAY:-:0}"

cd "$INSTALL_DIR"
exec "$JAVA_BIN" -Xmx6g -Djava.library.path="$NATIVE_DIR" -jar "$INSTALL_DIR/elite_intel.jar"
LAUNCHEOF

    # Substitute actual paths (avoids heredoc variable expansion issues)
    sed -i "s|INSTALL_DIR_PLACEHOLDER|$DEFAULT_INSTALL_LOCATION|g" "$LAUNCHER"
    sed -i "s|NATIVE_DIR_PLACEHOLDER|$NATIVE_DIR|g" "$LAUNCHER"
    sed -i "s|JAVA_BIN_PLACEHOLDER|$JAVA_BIN|g" "$LAUNCHER"
    chmod +x "$LAUNCHER"

    cat > "$DESKTOP_FILE" << EOF
[Desktop Entry]
Version=1.0
Name=Elite Intel
Comment=Elite Dangerous AI companion
Path=$DEFAULT_INSTALL_LOCATION
Exec=$LAUNCHER
Icon=$ICON_PATH
Terminal=false
Type=Application
Categories=Game;Utility;
StartupNotify=true
EOF

    chmod +x "$DESKTOP_FILE"
    command -v update-desktop-database >/dev/null && update-desktop-database "$DESKTOP_DIR"

    echo "Launcher script: $LAUNCHER"
}


##########################################################################################################################
#       Main Loop
#
while getopts ":hud" opt; do
    case $opt in
        h) print_usage ;;
        u) update_elite_intel ;;
        d) delete_elite_intel ;;
        \?) echo "Error: Invalid option -$OPTARG" >&2; print_usage ;;
        :)  echo "Error: Option -$OPTARG requires an argument." >&2; print_usage ;;
    esac
done

#
#		Install EliteIntel
#
set_install_folder
sleep 0.2

#
#       Download and install bundled JDK (Oracle JDK 21)
#       Must run after set_install_folder so DEFAULT_INSTALL_LOCATION is finalised
#
install_jdk
sleep 0.2

#       Determine Steam installation
detect_steam
sleep 0.2

#
#		Create Symlink to relevant folders
#
create_bindings
sleep 0.2

#
#       Create Start menu shortcut
#
create_start_menu
sleep 0.2

cat << EOF

    Thank you for downloading EliteIntel, your new side-kick.
    Bugs? Feature requests? Contact developer at https://matrix.to/#/#krondor:matrix.org

    Fly dangerous and explore the galaxy!

EOF
