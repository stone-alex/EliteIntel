package elite.companion.comms.brain;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface AiQueryInterface {
    JsonObject sendToAi(JsonArray messages);
}
