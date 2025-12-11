package elite.intel.eddm;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class EdDnClient {
    private static final String SUB_ENDPOINT = "tcp://eddn.edcd.io:9500";
    private static final String UPLOAD_ENDPOINT = "https://eddn.edcd.io:4430/upload/";
    private static final String SOFTWARE_NAME = "EliteIntel";
    private static final String SOFTWARE_VERSION = "v2025.12.10.beta-0158";
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

    private static Map<String, Object> commodityMap(GameEvents.MarketEvent.MarketItem i) {
        return Map.ofEntries(
                Map.entry("id", i.getId()),
                Map.entry("Name", i.getName()),
                Map.entry("BuyPrice", i.getBuyPrice()),
                Map.entry("SellPrice", i.getSellPrice()),
                Map.entry("MeanPrice", i.getMeanPrice()),
                Map.entry("StockBracket", i.getStockBracket()),
                Map.entry("DemandBracket", i.getDemandBracket()),
                Map.entry("Stock", i.getStock()),
                Map.entry("Demand", i.getDemand()),
                Map.entry("Consumer", i.isConsumer()),
                Map.entry("Producer", i.isProducer()),
                Map.entry("Rare", i.isRare())
        );
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

    public void stop() {
        running = false;
        executor.shutdownNow();
        subscriber.close();
        context.destroySocket(subscriber);
        context.destroy();
    }

    public boolean uploadMarket(GameEvents.MarketEvent marketEvent) {

        List<Map<String, Object>> commodities = marketEvent.getItems().stream().map(EdDnClient::commodityMap).toList();
        Map<String, Object> message = Map.of(
                "$schemaRef", "https://eddn.edcd.io/schemas/commodity/3.0",
                "timestamp", marketEvent.getTimestamp(),
                "event", marketEvent.getEvent(),
                "MarketID", marketEvent.getMarketID(),
                "StationName", marketEvent.getStationName(),
                "StarSystem", marketEvent.getStarSystem(),
                "horizons", true,
                "odyssey", true,
                "commodities", commodities
        );

        return sendCommodity(message);
    }

    public boolean sendCommodity(Map<String, Object> commodityData) {
        try {
            String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
            Map<String, Object> envelope = Map.of(
                    "softwareName", SOFTWARE_NAME,
                    "softwareVersion", SOFTWARE_VERSION,
                    "uploadTime", timestamp,
                    "message", commodityData
            );
            String json = mapper.writeValueAsString(envelope);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(UPLOAD_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
