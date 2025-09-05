package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.MaterialsEvent;
import elite.companion.session.PlayerSession;

@SuppressWarnings("unused")
public class MaterialsEventSubscriber {

    @Subscribe
    public void onMaterialsEvent(MaterialsEvent event) {
        PlayerSession.getInstance().put(PlayerSession.MATERIALS, event.toJson());
    }
}
