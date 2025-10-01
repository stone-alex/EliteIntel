package elite.intel.session;

import com.google.gson.*;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The SessionPersistence class provides methods to manage the persistence of session data.
 * It handles saving, loading, and deleting session files and manages the serialization
 * and deserialization of field data with synchronized read/write access.
 */
class SessionPersistence {
    private static final Logger log = LogManager.getLogger(SessionPersistence.class);
    public static String SESSION_DIR;
    protected String APP_DIR;
    protected String sessionFile = "x.json";
    private final Map<String, FieldHandler<?>> fields = new HashMap<>();
    private final BlockingQueue<SaveOperation> saveQueue = new LinkedBlockingQueue<>();
    private final Thread workerThread;
    private volatile boolean isShutdown = false;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

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
        // Placeholder for save operation
    }

    public void save() {
        try {
            saveQueue.put(new SaveOperation());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while queueing save operation");
        }
    }

    private void processQueue() {
        while (!isShutdown) {
            try {
                while (!saveQueue.isEmpty()) {
                    saveQueue.take();
                    processSaveOperation();
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

    private void processSaveOperation() {
        lock.writeLock().lock();
        try {
            File file = ensureSessionDirectory();
            if (file == null) {
                return;
            }

            JsonObject existingJson = loadJsonForMerge();
            JsonObject json = new JsonObject();

            for (Map.Entry<String, FieldHandler<?>> entry : fields.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue().getter.get();
                JsonElement element;
                if (value != null) {
                    element = GsonFactory.getGson().toJsonTree(value);
                } else if (existingJson.has(name)) {
                    element = existingJson.get(name);
                } else {
                    element = JsonNull.INSTANCE;
                }
                json.add(name, element);
            }

            try (Writer writer = new FileWriter(file)) {
                GsonFactory.getGson().toJson(json, writer);
                log.debug("Saved session to: {}", file.getPath());
            } catch (IOException e) {
                log.error("Failed to save session to {}: {}", file.getPath(), e.getMessage(), e);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void shutdown() {
        isShutdown = true;
        workerThread.interrupt();
    }

    protected void loadSession(Consumer<JsonObject> jsonConsumer) {
        lock.readLock().lock();
        try {
            File file = ensureSessionDirectory();
            if (file == null) {
                return;
            }

            if (!file.exists()) {
                try {
                    JsonObject emptyJson = new JsonObject();
                    Files.write(file.toPath(), GsonFactory.getGson().toJson(emptyJson).getBytes());
                    log.info("Created empty session file: {}", file.getPath());
                    jsonConsumer.accept(emptyJson);
                } catch (IOException e) {
                    log.error("Failed to create empty session file {}: {}", file.getPath(), e.getMessage(), e);
                }
                return;
            }

            try (Reader reader = new FileReader(file)) {
                JsonElement jsonElement = JsonParser.parseReader(reader);
                if (jsonElement == null || !jsonElement.isJsonObject()) return;
                JsonObject json = jsonElement.getAsJsonObject();
                jsonConsumer.accept(json);
                log.debug("Loaded session from: {}", file.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private JsonObject loadJsonForMerge() {
        lock.readLock().lock();
        try {
            File file = ensureSessionDirectory();
            if (file == null || !file.exists()) {
                return new JsonObject();
            }

            try (Reader reader = new FileReader(file)) {
                JsonElement jsonElement = JsonParser.parseReader(reader);
                if (jsonElement == null || jsonElement.isJsonNull() || !jsonElement.isJsonObject()) {
                    log.warn("Invalid or empty JSON in file {}, returning empty JsonObject", file.getPath());
                    return new JsonObject();
                }
                return jsonElement.getAsJsonObject();
            } catch (Exception e) {
                log.error("Failed to load session for merge from {}: {}", file.getPath(), e.getMessage(), e);
                return new JsonObject();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    protected void loadFields(JsonObject json) {
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
        if (sessionFile == null || sessionFile.isEmpty() || sessionFile.equalsIgnoreCase("null")) {
            return null;
        }
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