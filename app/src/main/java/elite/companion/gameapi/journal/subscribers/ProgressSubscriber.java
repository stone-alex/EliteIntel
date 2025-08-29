package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.ProgressEvent;
import elite.companion.gameapi.journal.events.dto.RankDto;
import elite.companion.session.SystemSession;

@SuppressWarnings("unused")
public class ProgressSubscriber {

    @Subscribe
    public void onProgressEvent(ProgressEvent event) {
        // NOTE: this looks similar to Rank event, the fields are named the same, but the data is the progress to next rank in percents.
        // NOTE: have to handle this differently
    }

}
