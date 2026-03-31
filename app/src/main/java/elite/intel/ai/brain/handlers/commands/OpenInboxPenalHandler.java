package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.Status;
import elite.intel.session.StatusFlags;
import elite.intel.session.ui.CommsPanel;
import elite.intel.session.ui.UINavigator;

public class OpenInboxPenalHandler implements CommandHandler {


    private final UINavigator navigator = new UINavigator();
    private final Status status = Status.getInstance();


    @Override
    public void handle(String action, JsonObject params, String responseText) {
        if (status.isInMainShip() || status.isInFighter() || status.isInSrv()) {
            navigator.openAndNavigate(StatusFlags.GuiFocus.COMMS_PANEL, CommsPanel.INBOX);
        } else {
            EventBusManager.publish(new AiVoxResponseEvent("Sorry, I can't do that right now."));
        }
    }
}
