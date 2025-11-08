package elite.intel.yt;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import elite.intel.ai.ConfigManager;
import elite.intel.ai.mouth.subscribers.events.YtVoxEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.util.StringUtls;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class YouTubeChatVocalizer implements StreamChatVocalizer {

    private ConfigManager conf;

    private static final Logger log = LogManager.getLogger(YouTubeChatVocalizer.class);
    private static final String YT_VID_API_URL = "https://www.googleapis.com/youtube/v3/videos?part=liveStreamingDetails&id=%s&key=%s";
    private static final String YT_CHAT_API_URL = "https://www.googleapis.com/youtube/v3/liveChat/messages?liveChatId=%s&part=snippet,authorDetails&maxResults=1&key=%s";
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(2);
    private String chatId;
    private static YouTubeChatVocalizer instance;
    private HttpClient client;
    private ExecutorService executor;
    private Gson gson;
    private String nextPageToken;
    private final AtomicBoolean streamRunning = new AtomicBoolean(false);

    private YouTubeChatVocalizer() {
        this.client = HttpClient.newBuilder().build();
        this.executor = Executors.newFixedThreadPool(1); // Single thread for polling
        this.gson = GsonFactory.getGson(); // Use provided GsonFactory
        this.streamRunning.set(false);
        this.conf = ConfigManager.getInstance();
    }

    public static synchronized YouTubeChatVocalizer getInstance() {
        if (instance == null) {
            instance = new YouTubeChatVocalizer();
        }
        return instance;
    }

    public void start() {
        if (streamRunning.get()) return;

        streamRunning.set(true);
        executor.submit(this::pollChat);
    }

    public void stop() {
        streamRunning.set(false);
        this.chatId = null;
        executor.shutdown();
    }

    private void pollChat() {
        while (streamRunning.get()) {
            try {
                Thread.sleep(POLL_INTERVAL.toMillis());
            } catch (InterruptedException e) {
                this.streamRunning.set(false);
                break;
            }

            if (conf.getSystemKey(ConfigManager.YT_URL) == null) {
                this.streamRunning.set(false);
                return;
            }

            fetchChatIdIfNotSet();

            if (this.chatId == null) {
                continue;
            }

            try {
                String url = String.format(YT_CHAT_API_URL, this.chatId, conf.getSystemKey(ConfigManager.YT_API_KEY));

                if (nextPageToken != null) {
                    url += "&pageToken=" + nextPageToken;
                }

                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().timeout(Duration.ofSeconds(10)).build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    if ("404".equals(response.statusCode())) {
                        streamRunning.set(false);
                        log.warn("YouTube stream not found");
                    } else {
                        log.error("YT HTTP ERROR CODE: {}", response.statusCode());
                    }
                    continue;
                }

                JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                log.debug("YT Response: {}", GsonFactory.getGson().toJson(json));

                nextPageToken = json.get("nextPageToken") != null ? json.get("nextPageToken").getAsString() : null;

                JsonArray items = json.getAsJsonArray("items");
                for (int i = 0; i < items.size(); i++) {
                    processVocalisation(items.get(i).getAsJsonObject());
                }
            } catch (Exception e) {
                log.error("Error fetching messages: {}", e, e.getMessage());
            }
        }
    }

    private void processVocalisation(JsonObject item) {
        JsonObject snippet = item.getAsJsonObject("snippet");
        String message = snippet.get("displayMessage").getAsString();
        String author = item.getAsJsonObject("authorDetails").get("displayName").getAsString();

        Instant publishedAt = gson.fromJson(snippet.get("publishedAt"), Instant.class);
        if (Duration.between(publishedAt, Instant.now()).abs().getSeconds() < 5) {
            EventBusManager.publish(new YtVoxEvent(author + " say: " + sanitize(message)));
        }
    }

    public static String sanitize(String input) {
        if (input == null) return null;
        return input.replaceAll(":[a-zA-Z0-9_+\\-]+:", "").replace("_", " ");
    }

    private void fetchChatIdIfNotSet() {
        if (this.chatId != null) return;
        try {

            String ytUrl = conf.getSystemKey(ConfigManager.YT_URL);
            String ytKey = conf.getSystemKey(ConfigManager.YT_API_KEY);
            if (ytKey == null || ytKey.isEmpty()) {
                log.warn("YT key is null or empty");
                return;
            }
            if (ytUrl == null || ytUrl.isEmpty()) {
                log.info("YT url is null or empty");
                return;
            }


            String videoId = StringUtls.extractVideoId(ytUrl);
            String url = String.format(YT_VID_API_URL, videoId, ytKey);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().timeout(Duration.ofSeconds(10)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("Error getting liveChatId: {}", response.statusCode());
            }

            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
            JsonArray items = json.getAsJsonArray("items");
            if (items != null && !items.isEmpty()) {
                JsonObject item = items.get(0).getAsJsonObject();
                JsonObject details = item.getAsJsonObject("liveStreamingDetails");
                if (details != null && details.has("activeLiveChatId")) {
                    this.chatId = details.get("activeLiveChatId").getAsString();
                }
            }

        } catch (Exception e) {
            log.error("Unable to fetch chat id {}", e, e.getMessage());
        }
    }
}