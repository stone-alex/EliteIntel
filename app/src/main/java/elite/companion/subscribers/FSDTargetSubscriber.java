package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.FSDTargetEvent;
import elite.companion.gameapi.events.NavRouteDto;
import elite.companion.session.SystemSession;

import java.util.Map;

public class FSDTargetSubscriber {

    public static final String FSD_TARGET_CLASS = "KGBFOAM";

    public FSDTargetSubscriber() {
        EventBusManager.register(this);
    }


    @Subscribe
    public void onFSDTargetEvent(FSDTargetEvent event) {
        String targetName = event.getName();
        String targetClass = event.getStarClass();
        boolean isScoopable = FSD_TARGET_CLASS.contains(targetClass.toUpperCase());

        Map<String, NavRouteDto> route = SystemSession.getInstance().getRoute();


        StringBuilder sb = new StringBuilder();
        sb.append("Destination Star System: ");
        sb.append(targetName);
        sb.append(" ");
        sb.append("Class: ");
        sb.append(targetClass);
        sb.append(" ");
        sb.append("Star Scoopable: ");
        sb.append(isScoopable ? "Yes" : "No");
        if (route != null && route.size() > 0) {
            sb.append(", ");
            sb.append("Jumps remaining:" + (route.size() - 1));
        }
        sb.append(".");


        SystemSession.getInstance().setSensorData(sb.toString());
    }
}
