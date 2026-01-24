package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.AICadence;
import elite.intel.ai.mouth.AiVoices;
import elite.intel.ai.mouth.VoiceToAllegiances;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.session.SystemSession;

/**
 * Handles the command to set the AI cadence in the system session.
 * <p>
 * The SetCadenceHandler class implements the CommandHandler interface and is
 * responsible for managing the execution of the "set_profile" command, which
 * updates the AI cadence in the application's system session based on the input
 * parameters.
 * <p>
 * Behavior:
 * - Extracts the cadence parameter based on the placeholder defined in
 * CommandActionsCustom.SET_PROFILE.
 * - Parses the extracted parameter to determine the desired AI cadence.
 * - Updates the system session's AI cadence to match the requested value.
 * - Triggers an event through EventBusManager if an invalid cadence is provided.
 * <p>
 * Usage:
 * This class is part of the command-handling system and is invoked through the
 * CommandHandler interface when the corresponding command is received.
 * <p>
 * Error Handling:
 * If an invalid cadence is specified, an appropriate error message is published
 * as a SensorDataEvent.
 */
public class SetCadenceHandler implements CommandHandler {
    @Override public void handle(String action, JsonObject params, String responseText) {
        SystemSession systemSession = SystemSession.getInstance();

        try {
            String profileName = params.get("key").getAsString();
            AICadence aiCadence = AICadence.valueOf(profileName.toUpperCase());

            AiVoices currentVoice = systemSession.getAIVoice();
            VoiceToAllegiances.getInstance().getVoiceForCadence(aiCadence, currentVoice);

            systemSession.setAICadence(aiCadence);
        } catch (IllegalArgumentException e) {
            EventBusManager.publish(new SensorDataEvent("No such cadence. try Imperial, Federation or Alliance", "Notify User"));
        }

    }
}
