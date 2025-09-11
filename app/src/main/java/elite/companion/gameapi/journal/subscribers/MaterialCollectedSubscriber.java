package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.MaterialCollectedEvent;

public class MaterialCollectedSubscriber {

    @Subscribe
    public void onMaterialCollected(MaterialCollectedEvent event){
        //implement material collected event
    }
}
