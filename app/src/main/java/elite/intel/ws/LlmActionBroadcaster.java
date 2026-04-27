package elite.intel.ws;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class LlmActionBroadcaster {

    private static final Logger log = LogManager.getLogger(LlmActionBroadcaster.class);
    private static final int PORT = 7497;
    private static final LlmActionBroadcaster INSTANCE = new LlmActionBroadcaster();

    private final Gson gson = new Gson();
    private volatile WebSocketServer server;

    private LlmActionBroadcaster() {
    }

    public static LlmActionBroadcaster getInstance() {
        return INSTANCE;
    }

    public void start() {
        // Construct and run the server entirely off the main thread to avoid a
        // JDK 17-21 ClassCircularityError in WeakPairMap triggered when the
        // WebSocket library initialises its module/proxy machinery during JVM startup.
        Thread t = new Thread(() -> {
            try {
                WebSocketServer srv = new WebSocketServer(new InetSocketAddress(PORT)) {
                    @Override
                    public void onOpen(WebSocket conn, ClientHandshake handshake) {
                        log.info("WS client connected: {}", conn.getRemoteSocketAddress());
                    }

                    @Override
                    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                        log.info("WS client disconnected: {}", conn.getRemoteSocketAddress());
                    }

                    @Override
                    public void onMessage(WebSocket conn, String message) {
                    }

                    @Override
                    public void onError(WebSocket conn, Exception ex) {
                        log.warn("WS error on {}: {}", conn != null ? conn.getRemoteSocketAddress() : "server", ex.getMessage());
                    }

                    @Override
                    public void onStart() {
                        log.info("LLM action WebSocket server started on ws://localhost:{}", PORT);
                    }
                };
                srv.setReuseAddr(true);
                server = srv;
                srv.run();
            } catch (Throwable e) {
                log.warn("LLM action WebSocket server failed — integration disabled: {}", e.getMessage());
                server = null;
            }
        }, "ws-broadcaster");
        t.setDaemon(true);
        t.start();
    }

    public void broadcast(JsonObject json) {
        WebSocketServer srv = server;
        if (srv == null) return;
        try {
            srv.broadcast(gson.toJson(json));
        } catch (Exception e) {
            log.debug("WS broadcast failed: {}", e.getMessage());
        }
    }
}