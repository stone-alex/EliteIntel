package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.FSSSignalDiscoveredEvent;
import elite.companion.session.SystemSession;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FSSSignalDiscoveredSubscriber { // Or fold into ShipAIModule

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pendingSummaryTask;
    private final SystemSession systemSession = SystemSession.getInstance();

    public FSSSignalDiscoveredSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onFSSSignalDiscovered(FSSSignalDiscoveredEvent event) {
        systemSession.addSignal(event);

        // Debounce: Cancel any pending task and schedule a new one
        if (pendingSummaryTask != null && !pendingSummaryTask.isDone()) {
            pendingSummaryTask.cancel(false);
        }

        pendingSummaryTask = scheduler.schedule(() -> {
            String summary = systemSession.buildSignalSummary();
            if (summary != null) {
                systemSession.setFssData(summary);
                // No need to call grok.processSystemCommand() - your loop handles it
            }
        }, 1, TimeUnit.SECONDS); // 1-second debounce; adjust as needed
    }

    // Shutdown scheduler on app close
    public void shutdown() {
        scheduler.shutdown();
    }
}
/*

        if("outpost".equals(event.getSignalType().toLowerCase())) {
            outpostCounter++;
            sensorData.append("Outposts discovered: ").append(outpostCounter).append(", ");
            //handle outpost signal discovered use signanalName. localizedName will be null
            //systemSession.setSensorData("Outpost signal discovered: " + event.getSignalName());
        }

        if(event.getSignalType() != null && event.getSignalType().toLowerCase().contains("station")) {
            stationCounter++;
            sensorData.append("Stations discovered: ").append(stationCounter).append(", ");
            //handle station signal discovered use signalName. localizedName will be null
            //systemSession.setSensorData("Station signal discovered: " + event.getSignalName());
        }

        if(event.getSignalType() != null && event.getSignalType().toLowerCase().contains("carrier")) {
            carrierCounter++;
            sensorData.append("Carriers discovered: ").append(carrierCounter).append(",");
            //handle carrier signal discovered use signalName. localizedName will be null
            //will have flag isStation set to true
            //systemSession.setSensorData("Carrier signal discovered: " + event.getSignalName());
        }

        if(event.getSignalNameLocalised() != null && event.getSignalNameLocalised().toLowerCase().contains("extraction")) {
            extractionCounter++;
            sensorData.append("Extraction sites discovered: ").append(extractionCounter).append(",");
            //handle extraction signal discovered use signalNameLocalised. signal name will contain game code.
            //isStation flag will be null.
            //systemSession.setSensorData("Extraction signal discovered: " + event.getSignalNameLocalised());
        }

        if(event.getSignalNameLocalised() != null && event.getSignalNameLocalised().toLowerCase().contains("installation")) {
            instalationCounter++;
            sensorData.append("Installations discovered: ").append(instalationCounter).append(",");
            //handle instalation signal discovered use signal name, localized name will be null.
            //isStation flag will be null.
            //systemSession.setSensorData("Instalation signal discovered: " + event.getSignalNameLocalised());
        }
    }
}
*/
