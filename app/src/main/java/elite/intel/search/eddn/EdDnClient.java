package elite.intel.search.eddn;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import elite.intel.search.eddn.schemas.EddnPayload;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class EdDnClient {

    private static final Logger log = LogManager.getLogger(EdDnClient.class);

    private static final String SUB_ENDPOINT = "tcp://eddn.edcd.io:9500";
    private static final String UPLOAD_ENDPOINT = "https://eddn.edcd.io:4430/upload/";
    private static final Object lock = new Object();
    private static volatile EdDnClient instance;
    private final ZContext context;
    private final ZMQ.Socket subscriber;
    private final ExecutorService executor;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private volatile boolean running = false;


    private EdDnClient() {
        context = new ZContext();
        subscriber = context.createSocket(SocketType.SUB);
        subscriber.connect(SUB_ENDPOINT);
        subscriber.subscribe("".getBytes(ZMQ.CHARSET));
        executor = Executors.newSingleThreadExecutor();
    }

    public static EdDnClient getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new EdDnClient();
                }
            }
        }
        return instance;
    }

    public void startListening(Consumer<JsonNode> handler) {
        if (running) return;
        running = true;
        executor.submit(() -> {
            while (running) {
                byte[] compressed = subscriber.recv(0);
                if (compressed == null || compressed.length == 0) continue;

                byte[] decompressed = ZMQUtil.decompress(compressed);
                if (decompressed.length == 0) continue;

                try {
                    JsonNode msg = mapper.readTree(decompressed);
                    handler.accept(msg);
                } catch (Exception e) {
                    // silently skip malformed JSON
                }
            }
        });
    }


    public void start() {
        if (running) return;
        startListening(jsonNode -> {
            //if (jsonNode.toString().contains("commodity/3")) {
            System.out.println(jsonNode);
            //}
        });
    }

    public void stop() {
        if (!running) return;
        running = false;
        executor.shutdownNow();
        subscriber.close();
        context.destroySocket(subscriber);
        context.destroy();
    }

    public <T> boolean upload(EddnPayload<T> payload) {
        try {
            String json = GsonFactory.getGson().toJson(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(UPLOAD_ENDPOINT))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            log.error("Failed to upload payload to EDDN", e);
            return false;
        }
    }
}
