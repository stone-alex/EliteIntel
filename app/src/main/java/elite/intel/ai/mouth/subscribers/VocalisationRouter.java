package elite.intel.ai.mouth.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.*;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;

public class VocalisationRouter {

    private final PlayerSession playerSession = PlayerSession.getInstance();

    /// --- always pass through
    @Subscribe
    public void onAiVoxResponseEvent(AiVoxResponseEvent event) {
        EventBusManager.publish(new VocalisationRequestEvent(event.getText(), AiVoxResponseEvent.class));
    }

    @Subscribe
    public void onMissionCriticalAnnouncementEvent(MissionCriticalAnnouncementEvent event) {
        EventBusManager.publish(new VocalisationRequestEvent(event.getText(), MissionCriticalAnnouncementEvent.class));
    }

    /// --- on/off based on user settings
    @Subscribe
    public void onNavigationVocalisationRequest(NavigationVocalisationEvent event) {
        if (playerSession.isNavigationAnnouncementOn()) {
            EventBusManager.publish(new VocalisationRequestEvent(event.getText(), NavigationVocalisationEvent.class));
        }
    }

    @Subscribe
    public void onDiscoveryAnnouncementEvent(DiscoveryAnnouncementEvent event) {
        if (playerSession.isDiscoveryAnnouncementOn()) {
            EventBusManager.publish(new VocalisationRequestEvent(event.getText(), DiscoveryAnnouncementEvent.class));
        }
    }

    @Subscribe
    public void onMiningAnnouncementEvent(MiningAnnouncementEvent event) {
        if (playerSession.isMiningAnnouncementOn()) {
            EventBusManager.publish(new VocalisationRequestEvent(event.getText(), MiningAnnouncementEvent.class));
        }
    }

    @Subscribe
    public void onRouteAnnouncementEvent(RouteAnnouncementEvent event) {
        if (playerSession.isRouteAnnouncementOn()) {
            EventBusManager.publish(new VocalisationRequestEvent(event.getText(), RouteAnnouncementEvent.class));
        }
    }

    @Subscribe
    public void onRadioTransmissionEvent(RadioTransmissionEvent event) {
        if (playerSession.isRadioTransmissionOn()) {
            EventBusManager.publish(new VocalisationRequestEvent(event.getText(), RadioTransmissionEvent.class));
        }
    }
}
