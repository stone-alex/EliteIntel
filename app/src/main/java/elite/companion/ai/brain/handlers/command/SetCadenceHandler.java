package elite.companion.ai.brain.handlers.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.companion.ai.brain.AICadence;
import elite.companion.ai.mouth.GoogleVoices;
import elite.companion.ai.mouth.VoiceToAllegiances;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.session.SystemSession;

import static elite.companion.util.json.JsonParameterExtractor.extractParameter;

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
    @Override public void handle(JsonObject params, String responseText) {
        SystemSession systemSession = SystemSession.getInstance();
        try {
            JsonElement jsonElement = extractParameter(CommandActionsCustom.SET_PROFILE.getPlaceholder(), params);
            AICadence aiCadence = AICadence.valueOf(jsonElement.getAsString().toUpperCase());

            GoogleVoices currentVoice = systemSession.getAIVoice();
            VoiceToAllegiances.getInstance().getVoiceForCadence(aiCadence, currentVoice);

            systemSession.setAICadence(aiCadence);
        } catch (IllegalArgumentException e) {
            EventBusManager.publish(new SensorDataEvent("No such cadence. try Imperial, Federation or Alliance"));
        }

    }
}
