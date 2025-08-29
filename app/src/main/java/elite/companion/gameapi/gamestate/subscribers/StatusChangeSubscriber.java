package elite.companion.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import elite.companion.util.EventBusManager;
import elite.companion.gameapi.gamestate.events.GameEvents;
import elite.companion.session.SystemSession;
import elite.companion.util.GsonFactory;

public class StatusChangeSubscriber {

    public StatusChangeSubscriber() {
        EventBusManager.register(this);
    }


    @Subscribe
    public void onStatusChange(GameEvents.StatusEvent event) {
        SystemSession systemSession = SystemSession.getInstance();
        systemSession.put(SystemSession.CURRENT_STATUS, GsonFactory.getGson().toJson(event));
        //write a Grok query to access status. this method is called often, but returns same data most of the time.
    }
}
