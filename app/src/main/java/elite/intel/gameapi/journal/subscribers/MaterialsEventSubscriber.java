package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.MaterialsEvent;
import elite.intel.session.PlayerSession;

@SuppressWarnings("unused")
public class MaterialsEventSubscriber {

    @Subscribe
    public void onMaterialsEvent(MaterialsEvent event) {
        //this is a single material collection event.
        //we need a list of all collected materials to do something useful with it.
        PlayerSession.getInstance().put(PlayerSession.MATERIAL, event.toJson());
    }
}
