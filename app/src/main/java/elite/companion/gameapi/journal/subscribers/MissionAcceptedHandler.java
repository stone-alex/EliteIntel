package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.gameapi.journal.events.MissionAcceptedEvent;
import elite.companion.gameapi.journal.events.userfriendly.MissionAccepted;
import elite.companion.session.SystemSession;

public class MissionAcceptedHandler {

    public MissionAcceptedHandler(){
        EventBusManager.register(this);
    }

    @Subscribe
    public void onMissionAcceptedEvent(MissionAcceptedEvent event){
        MissionAccepted mission = new MissionAccepted(event);
        SystemSession systemSession = SystemSession.getInstance();
        systemSession.addMission(mission);
        systemSession.setSensorData("Mission Accepted: " + mission.toJson());
    }
}
