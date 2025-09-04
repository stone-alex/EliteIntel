package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.MaterialsEvent;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;

@SuppressWarnings("unused")
public class MaterialsEventSubscriber {

    @Subscribe
    public void onMaterialsEvent(MaterialsEvent event) {
        SystemSession.getInstance().put(PlayerSession.MATERIALS, event.toJson());
    }
}
