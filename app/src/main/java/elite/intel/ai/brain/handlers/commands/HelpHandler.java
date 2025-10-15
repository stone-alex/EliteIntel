package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.help.HelpData;
import elite.intel.util.BrowserUtil;

import java.util.List;

public class HelpHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {

        String topic = params.get("key").getAsString();
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