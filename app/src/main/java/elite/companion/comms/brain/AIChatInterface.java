package elite.companion.comms.brain;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface AIChatInterface {
    JsonObject sendToAi(JsonArray messages);
}
