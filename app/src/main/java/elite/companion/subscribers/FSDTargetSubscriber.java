package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.FSDTargetEvent;
import elite.companion.session.SystemSession;

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

        StringBuilder sb = new StringBuilder();
        sb.append("Targeting Star System: ");
        sb.append(targetName);
        sb.append(" ");
        sb.append("Class: ");
        sb.append(targetClass);
        sb.append(" ");
        sb.append("Star Scoopable: ");
        sb.append(isScoopable ? "Yes" : "No");
        sb.append(".");


        SystemSession.getInstance().setSensorData(sb.toString());
    }
}
