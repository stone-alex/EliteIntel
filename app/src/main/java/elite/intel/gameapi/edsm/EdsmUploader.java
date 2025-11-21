package elite.intel.gameapi.edsm;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.BaseEvent;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class EdsmUploader {
    private static final Logger log = LogManager.getLogger(EdsmUploader.class);

    private static final String URL = "https://www.edsm.net/api-journal-v1";
    private static final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Set<String> discard = ConcurrentHashMap.newKeySet();
    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final SystemSession systemSession = SystemSession.getInstance();
    private JsonArray batch = new JsonArray();
    private String commander;
    private String apiKey;
    private String version;

    public EdsmUploader() {
        loadDiscardList();
        scheduler.scheduleAtFixedRate(this::flush, 30, 30, TimeUnit.SECONDS);
        log.info("EDSM uploader active");
    }

    @Subscribe
    private void onJournalEvent(BaseEvent event) {
        String type = event.getEventType();
        if (discard.contains(type)) return;
        batch.add(event.toJsonObject()); // already the raw journal object
    }

    private void flush() {
        commander = playerSession.getInGameName();
        apiKey = systemSession.getEdsmApiKey();
        version = playerSession.getGameVersion();

        if (commander.isBlank() || apiKey.isBlank()) {
            String message = "EDSM credentials missing – upload disabled commander=["+commander+"] apiKey=["+apiKey+"]";
            log.warn(message);
            EventBusManager.publish(new AppLogEvent(message));
            return;
        }

        if (batch.isEmpty()) return;

        JsonObject payload = new JsonObject();
        payload.addProperty("commanderName", commander);
        payload.addProperty("apiKey", apiKey);
        payload.addProperty("fromSoftware", "EliteIntel");
        payload.addProperty("fromSoftwareVersion", version);
        payload.add("message", batch);

        HttpRequest req = HttpRequest.newBuilder(URI.create(URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GsonFactory.getGson().toJson(payload)))
                .timeout(Duration.ofSeconds(20))
                .build();

        try {
            HttpResponse<String> postResponse = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (postResponse.statusCode() == 200) {
                String message = "EDSM: " + batch.size() + " events uploaded";
                EventBusManager.publish(new AppLogEvent(message));
                log.info("EDSM: {} events uploaded", batch.size());
            } else if (postResponse.statusCode() != 200) {
                String message = "EDSM upload failed with status: " + postResponse.statusCode();
                EventBusManager.publish(new AppLogEvent(message));
                log.warn("POST to EDSM failed with status: {}", postResponse.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            log.error("EDSM upload exception", e);
        } finally {
            batch = new JsonArray(); //clear batch
        }
    }

    private void loadDiscardList() {
        // fire-and-forget, cache forever
        client.sendAsync(
                HttpRequest.newBuilder(URI.create("https://www.edsm.net/api-journal-v1/discard"))
                        .GET()
                        .timeout(Duration.ofSeconds(10))
                        .build(),

                HttpResponse.BodyHandlers.ofString()).thenAccept(r -> {
            if (r.statusCode() == 200) {
                JsonArray arr = GsonFactory.getGson().fromJson(r.body(), JsonArray.class);
                arr.forEach(e -> discard.add(e.getAsString()));
                log.info("EDSM discard list loaded ({} events ignored)", discard.size());
            }
        });
    }

    public void shutdown() {
        scheduler.shutdownNow();
        flush();
    }
}