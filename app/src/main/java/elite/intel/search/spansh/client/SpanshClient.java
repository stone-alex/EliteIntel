package elite.intel.search.spansh.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.util.AudioPlayer;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SpanshClient {

    private final Logger log = LogManager.getLogger(SpanshClient.class);
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = GsonFactory.getGson();

    private String BASE_URL;
    private String RESULTS_URL;

    public SpanshClient(String BASE_URL, String RESULTS_URL) {
        this.BASE_URL = BASE_URL;
        this.RESULTS_URL = RESULTS_URL;
    }


    private String postSearch(ToJsonConvertible criteria)
            throws IOException, InterruptedException {

        HttpRequest post = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:144.0) Gecko/20100101 Firefox/144.0")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(criteria.toJson()))
                .build();

        HttpResponse<String> resp = httpClient.send(post, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            log.warn("POST failed: {}", resp.statusCode());
            return null;
        }

        JsonObject json = gson.fromJson(resp.body(), JsonObject.class);
        return json.has("search_reference") ? json.get("search_reference").getAsString() : null;
    }

    /**
     * Corner case, string query post, used by trade route plotter
     * */
    private String getSearch(String query) throws IOException, InterruptedException {

        HttpRequest post = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:144.0) Gecko/20100101 Firefox/144.0")
                .header("Accept", "*/*")
                .header("Accept-Language", "en-US,en;q=0.5")
                .header("Accept-Encoding", "gzip, deflate, br, zstd")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Origin", "https://spansh.co.uk")
                .header("Referer", "https://spansh.co.uk/trade")
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "same-origin")
                .POST(HttpRequest.BodyPublishers.ofString(query))
                .build();

        HttpResponse<String> resp = httpClient.send(post, HttpResponse.BodyHandlers.ofString());
        if(resp.statusCode() == 400){
            log.warn("POST failed: {}", resp.body());
            EventBusManager.publish(new SensorDataEvent("Unable to complete Shapnsh request: "+resp.body()));
        }

        if (resp.statusCode() != 202) {
            log.warn("POST failed: {}", resp.statusCode());
            return null;
        }

        JsonObject json = gson.fromJson(resp.body(), JsonObject.class);
        return json.has("job") ? json.get("job").getAsString() : null;
    }

    public JsonObject performSearch(StringQuery query) {
        try {
            log.info("performing search: {}", BASE_URL+query.getQuery());
            String searchRefId = getSearch(query.getQuery());
            return waitForResults(searchRefId);
        } catch (IOException | InterruptedException e) {
            log.error("Error performing search", e);
        }
        return new JsonObject();
    }

    public JsonObject performSearch(ToJsonConvertible criteria){
        try {
            String searchRefId = postSearch(criteria);
            return waitForResults(searchRefId);
        } catch (IOException | InterruptedException e) {
            log.error("Error performing search", e);
        }
        return new JsonObject();
    }



    public JsonObject waitForResults(String searchRefId) throws IOException, InterruptedException {
        if (searchRefId == null) return null;

        int attempt = 0;
        final int maxAttempts = 60;           // ~5–6 min max wait
        final long baseDelayMs = 4_000L;       // start at 4 s

        while (attempt < maxAttempts) {
            attempt++;

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(RESULTS_URL + searchRefId))
                    .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:144.0) Gecko/20100101 Firefox/144.0")
                    .GET()
                    .build();

            log.info("polling search {} (attempt {}) url: {}", searchRefId, attempt, req.uri());
            AudioPlayer.getInstance().playBeep(AudioPlayer.BEEP_2); // audio indicator of background search

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 202 || resp.statusCode() == 204) {
                // still processing
                long delay = baseDelayMs * (1L << (attempt - 1)); // 2s → 4s → 8s …
                delay = delay + (long) (Math.random() * 1_000);     // +0–1s jitter
                log.debug("Search {} in progress ({}), retry in {} ms", searchRefId, resp.statusCode(), delay);
                Thread.sleep(delay);
                continue;
            }

            if (resp.statusCode() == 200) {
                String body = resp.body().trim();
                if (body.isEmpty() || body.equals("null")) {
                    // known Spansh quirk – empty 200
                    Thread.sleep(2_000);
                    continue;
                }
                log.info("Search {} completed after {} attempts", searchRefId, attempt);
                return gson.fromJson(body, JsonObject.class);
            }

            log.warn("Unexpected status {} for search {}", resp.statusCode(), searchRefId);
            break;
        }

        log.error("Search {} timed out after {} attempts", searchRefId, maxAttempts);
        return null;
    }
}
