package elite.companion.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.gamestate.events.GameEvents;
import elite.companion.session.PlayerSession;
import elite.companion.util.json.GsonFactory;

public class StatusChangeSubscriber {

    @Subscribe
    public void onStatusChange(GameEvents.StatusEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.put(PlayerSession.CURRENT_STATUS, GsonFactory.getGson().toJson(event));
        //write a Grok query to access status. this method is called often, but returns same data most of the time.
    }
}
