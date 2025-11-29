package elite.intel.search.spansh.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

    public JsonObject performSearch(ToJsonConvertible criteria) throws IOException, InterruptedException {

        String searchRefId = submitSearch(criteria);
        if (searchRefId == null) return null;

        int attempt = 0;
        final int maxAttempts = 60;           // ~5–6 min max wait
        final long baseDelayMs = 2_000L;       // start at 2 s

        while (attempt < maxAttempts) {
            attempt++;

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(RESULTS_URL + searchRefId))
                    .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:144.0) Gecko/20100101 Firefox/144.0")
                    .GET()
                    .build();

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


    private String submitSearch(ToJsonConvertible criteria)
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


}
