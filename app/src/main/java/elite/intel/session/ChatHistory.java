package elite.intel.session;

import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

public class ChatHistory implements ToYamlConvertable, ToJsonConvertible {

    private String commanderLog;
    private String coPilotBrief;

    public ChatHistory(String userMessage, String assistantMessage) {
        this.commanderLog = userMessage;
        this.coPilotBrief = assistantMessage;
    }

    public ChatHistory() {
    }

    public String getCommanderLog() {
        return commanderLog;
    }

    public void setCommanderLog(String commanderLog) {
        this.commanderLog = commanderLog;
    }

    public String getCoPilotBrief() {
        return coPilotBrief;
    }

    public void setCoPilotBrief(String coPilotBrief) {
        this.coPilotBrief = coPilotBrief;
    }

    @Override public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override public String toYaml() {
        return YamlFactory.toYaml(this);
    }
}
