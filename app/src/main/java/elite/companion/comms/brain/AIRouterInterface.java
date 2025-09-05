package elite.companion.comms.brain;

import com.google.gson.JsonObject;

import javax.annotation.Nullable;

public interface AIRouterInterface {
    void start() throws Exception;
    void stop();
    void processAiResponse(JsonObject jsonResponse, @Nullable String userInput);
}
