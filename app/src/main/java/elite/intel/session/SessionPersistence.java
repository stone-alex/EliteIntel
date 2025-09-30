package elite.intel.session;

import com.google.gson.*;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The SessionPersistence class provides methods to manage the persistence of session data.
 * It handles saving, loading, and deleting session files and manages the serialization
 * and deserialization of field data.
 */
class SessionPersistence {
    private static final Logger log = LogManager.getLogger(SessionPersistence.class);
    public static  String SESSION_DIR;
    protected String APP_DIR;
    protected String sessionFile = "system_session.json";
    private final Map<String, FieldHandler<?>> fields = new HashMap<>();
    private final BlockingQueue<SaveOperation> saveQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<ReadOperation> readQueue = new LinkedBlockingQueue<>();
    private final Thread workerThread;
    private volatile boolean isShutdown = false;


    protected SessionPersistence(String directory) {
        SESSION_DIR = directory;
        workerThread = new Thread(this::processQueue);
        workerThread.setDaemon(true);
        workerThread.start();
    }



    public void ensureFileAndDirectoryExist(String sessionFile) {
        String appDir = SESSION_DIR;
        try {
            URI jarUri = SessionPersistence.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            File jarFile = new File(jarUri);
            if (jarFile.getPath().endsWith(".jar")) {
                String parentDir = jarFile.getParent();
                if (parentDir != null) {
                    appDir = parentDir + File.separator + SESSION_DIR;
                    log.debug("Running from JAR, set APP_DIR to: {}", appDir);
                } else {
                    log.warn("JAR parent directory is null, using empty APP_DIR: {}", appDir);
                }
            } else {
                log.debug("Not running from JAR, using empty APP_DIR for classpath resources");
            }
        } catch (Exception e) {
            log.warn("Could not determine JAR location, using empty APP_DIR: {}. Error: {}", appDir, e.getMessage());
        }
        APP_DIR = appDir;
        this.sessionFile = sessionFile;
    }


    public static class FieldHandler<T> {
        private final Supplier<T> getter;
        private final Consumer<T> setter;

        private final Type type;
        public FieldHandler(Supplier<T> getter, Consumer<T> setter, Type type) {
            this.getter = getter;
            this.setter = setter;
            this.type = type;
        }

    }

    protected <T> void registerField(String name, Supplier<T> getter, Consumer<T> setter, Type type) {
        fields.put(name, new FieldHandler<>(getter, setter, type));
    }


    private static class SaveOperation {
        private final Map<String, Object> stateMap;

        public SaveOperation(Map<String, Object> stateMap) {
            this.stateMap = new HashMap<>(stateMap);
        }
    }

    private static class ReadOperation {
        private final Consumer<JsonObject> jsonConsumer;

        public ReadOperation(Consumer<JsonObject> jsonConsumer) {
            this.jsonConsumer = jsonConsumer;
        }
    }


    protected void saveSession(Map<String, Object> stateMap) {
        try {
            saveQueue.put(new SaveOperation(stateMap));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while queueing save operation");
        }
    }

    protected void save() {
        try {
            saveQueue.put(new SaveOperation(new HashMap<>()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while queueing save operation");
        }
    }


    private void processQueue() {
        while (!isShutdown) {
            try {
                ReadOperation readOp = readQueue.poll();
                if (readOp != null) {
                    processReadOperation(readOp);
                }

                while (!saveQueue.isEmpty()) {
                    SaveOperation saveOp = saveQueue.take();
                    processSaveOperation(saveOp);
                }

                Thread.sleep(100); // Prevent busy waiting
            } catch (InterruptedException e) {
                if (isShutdown) {
                    break;
                }
                Thread.currentThread().interrupt();
                log.error("Worker thread interrupted", e);
            }
        }
    }

    private void processSaveOperation(SaveOperation operation) {
        File file = ensureSessionDirectory();
        if (file == null) {
            return;
        }

        JsonObject existingJson = loadJsonForMerge();
        JsonObject json = new JsonObject();
        JsonObject stateJson = existingJson.has("state") ? existingJson.getAsJsonObject("state") : null;

        if(stateJson != null) {
            for (Map.Entry<String, Object> entry : operation.stateMap.entrySet()) {
                stateJson.add(entry.getKey(), GsonFactory.getGson().toJsonTree(entry.getValue()));
            }
            json.add("state", stateJson);
        }

        for (Map.Entry<String, FieldHandler<?>> entry : fields.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue().getter.get();
            json.add(name, value != null ? GsonFactory.getGson().toJsonTree(value) : existingJson.has(name) ? existingJson.get(name) : JsonNull.INSTANCE);
        }

        try (Writer writer = new FileWriter(file)) {
            GsonFactory.getGson().toJson(json, writer);
            log.debug("Saved session to: {}", file.getPath());
        } catch (IOException e) {
            log.error("Failed to save session to {}: {}", file.getPath(), e.getMessage(), e);
        }
    }

    public void shutdown() {
        isShutdown = true;
        workerThread.interrupt();
    }

    protected void loadSession(Consumer<JsonObject> jsonConsumer) {
        try {
            readQueue.put(new ReadOperation(jsonConsumer));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while queueing read operation");
        }
    }

    private void processReadOperation(ReadOperation operation) {
        File file = ensureSessionDirectory();
        if (file == null) {
            return;
        }

        if (!file.exists()) {
            try {
                JsonObject emptyJson = new JsonObject();
                emptyJson.add("state", new JsonObject());
                Files.write(file.toPath(), GsonFactory.getGson().toJson(emptyJson).getBytes());
                log.info("Created empty session file: {}", file.getPath());
                operation.jsonConsumer.accept(emptyJson);
            } catch (IOException e) {
                log.error("Failed to create empty session file {}: {}", file.getPath(), e.getMessage(), e);
            }
            return;
        }

        try (Reader reader = new FileReader(file)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            if (jsonElement == null || !jsonElement.isJsonObject()) return;
            JsonObject json = jsonElement.getAsJsonObject();
            operation.jsonConsumer.accept(json);
            log.debug("Loaded session from: {}", file.getPath());
        } catch (IOException | JsonSyntaxException e) {
            // retry
        }
    }

    private JsonObject loadJsonForMerge() {
        File file = ensureSessionDirectory();
        if (file == null || !file.exists()) {
            return new JsonObject();
        }

        try (Reader reader = new FileReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            log.error("Failed to load session for merge from {}: {}", file.getPath(), e.getMessage(), e);
            return new JsonObject();
        }
    }

    protected void loadFields(JsonObject json, Map<String, Object> stateMap) {
        if (json.has("state")) {
            JsonObject stateJson = json.getAsJsonObject("state");
            for (Map.Entry<String, JsonElement> entry : stateJson.entrySet()) {
                stateMap.put(entry.getKey(), GsonFactory.getGson().fromJson(entry.getValue(), Object.class));
            }
        }

        for (Map.Entry<String, FieldHandler<?>> entry : fields.entrySet()) {
            String name = entry.getKey();
            FieldHandler<?> handler = entry.getValue();
            if (json.has(name) && !json.get(name).isJsonNull()) {
                try {
                    Object value = GsonFactory.getGson().fromJson(json.get(name), handler.type);
                    ((Consumer<Object>) handler.setter).accept(value);
                } catch (JsonSyntaxException e) {
                    log.error("Failed to deserialize field {}: {}", name, e.getMessage(), e);
                }
            }
        }
    }

    private File ensureSessionDirectory() {
        File file = new File(APP_DIR, sessionFile);
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                log.error("Failed to create session directory: {}", parentDir.getPath());
                return null;
            }
        }
        log.info("Session file: {}", file.getAbsolutePath());
        return file;
    }
}