package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.help.HelpData;
import elite.intel.util.BrowserUtil;

import java.util.List;

import static elite.intel.ai.brain.handlers.commands.custom.CustomCommands.HELP;

public class HelpHandler implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {

        JsonElement jsonElement = params.get(HELP.getParamKey());
        String topic = jsonElement == null ? null : jsonElement.getAsString();

        HelpData helpData = HelpData.getInstance();
        String result = topic == null ? null : helpData.getHelp(topic);

        if (result == null) {
            List<String> topics = helpData.getTopics();
            topics.sort(String::compareTo);
            String response = "Available topics: " + String.join(", ", topics)+". To access topic say Help me with, followed by topic";
            EventBusManager.publish(new AiVoxResponseEvent(response));
        } else {
            EventBusManager.publish(new AiVoxResponseEvent("Moment..."));
            if (result.toLowerCase().startsWith("http")) {
                BrowserUtil.openUrl(result);
            } else {
                EventBusManager.publish(new AiVoxResponseEvent(result));
            }
        }
    }
}