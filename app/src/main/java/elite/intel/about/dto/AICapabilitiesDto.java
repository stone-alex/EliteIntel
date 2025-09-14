package elite.intel.about.dto;

import com.google.gson.annotations.SerializedName;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;


/**
 * Represents a Data Transfer Object (DTO) for defining the AI capabilities and limitations
 * of an Elite Dangerous AI companion application. This class includes detailed descriptions
 * of what features the application provides and what actions it does not perform.
 * <p>
 * The primary purpose of this class is to encapsulate the capabilities and limitations
 * as structured information, with the ability to convert this structure into a JSON format.
 */
public class AICapabilitiesDto implements ToJsonConvertible {


    @SerializedName("capabilities")
    private String capabilities;

    @SerializedName("limitations")
    private String limitations;


    public AICapabilitiesDto() {
        capabilities = "## What the App Does\n" +
                "\n" +
                "- **Voice-Activated Commands for VR Users:** Enables hands-free control in VR setups. Users can issue voice triggers (e.g., \"deploy landing gear\" or \"analyze cargo hold contents\") to execute in-game actions via keyboard events. This reduces the need to remove VR headsets for common tasks.\n" +
                "  \n" +
                "- **Real-Time Journal Parsing:** Reads and processes *Elite Dangerous* journal files (JSON format) using a `JournalReader` to post events to an internal event bus. Modules subscribe to these events for handling, allowing the app to track session data and respond dynamically.\n" +
                "\n" +
                "- **Intelligent AI Integration:** Uses Grok-3-fast to interpret voice commands, provide helpful responses (e.g., route suggestions, mining targets), and route actions. Grok is aware of all game commands, including user-friendly mappings (e.g., `deploy_landing_gear` to `LandingGearToggle`).\n" +
                "\n" +
                "- **Dynamic Key Mapping:** Automatically generates mappings from the game's `.binds` file using `GenerateGameCommandMapping`. This ensures compatibility with game updates—simply rerun the generator if bindings change. Supports custom hold times for keys (e.g., pressing and holding for route plotting).\n" +
                "\n" +
                "- **Event Bus and Session Tracking:** Manages in-memory storage of session data via an event bus. Modules like `SpeechRecognizer`, `GrokInteractionHandler`, `GrokResponseRouter`, `VoiceCommandHandler`, and `KeyBindingExecutor` handle STT/TTS, AI responses, and keyboard interactions.\n" +
                "\n" +
                "- **Custom Commands:** Beyond standard bindings, supports app-specific actions like `set_mining_target` or `plot_route`, executed as sequences of key presses with configurable delays.\n";


        limitations = "## What the App Does NOT Do\n" +
                "\n" +
                "- **No Automation or AFK Play:** The app requires explicit voice triggers from the user for every action. It cannot play the game autonomously, farm resources, or perform any unattended operations. This ensures full TOS compliance—no macros, bots, or exploits.\n" +
                "\n" +
                "- **No Direct Game Modification:** It does not alter game files, inject code, or interact with the game executable beyond simulating keyboard events (which are TOS-allowed for accessibility tools).\n" +
                "\n" +
                "- **No Data Collection or Sharing:** All processing is local. Journal data is parsed in-memory for the current session only; no external storage or transmission occurs without user consent.\n" +
                "\n" +
                "- **No Advanced AI Overreach:** Grok-3-fast is used solely for command interpretation and response generation. It does not make decisions that violate TOS or user intent.\n" +
                "\n" +
                "- **Limited Scope:** Focused on QoL features like voice commands and info relay. It does not include features like overlay HUDs, external APIs (beyond configured endpoints), or multiplayer coordination.\n";
    }


    public String getCapabilities() {
        return capabilities;
    }

    public String getLimitations() {
        return limitations;
    }

    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

}
