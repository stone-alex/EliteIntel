package elite.companion.session;

import com.google.gson.*;
import elite.companion.util.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SessionPersistence {
    private static final Logger LOG = LoggerFactory.getLogger(SessionPersistence.class);
    private final String sessionFile;
    private final Map<String, FieldHandler<?>> fields = new HashMap<>();

    public static class FieldHandler<T> {
        private final Supplier<T> getter;
        private final Consumer<T> setter;
        private final Class<T> type;

        public FieldHandler(Supplier<T> getter, Consumer<T> setter, Class<T> type) {
            this.getter = getter;
            this.setter = setter;
            this.type = type;
        }
    }

    public SessionPersistence(String sessionFile) {
        this.sessionFile = sessionFile;
    }

    public <T> void registerField(String name, Supplier<T> getter, Consumer<T> setter, Class<T> type) {
        fields.put(name, new FieldHandler<>(getter, setter, type));
    }

    public void saveSession(Map<String, Object> stateMap) {
        File file = ensureSessionDirectory();
        if (file == null) {
            return;
        }

        JsonObject existingJson = loadJsonForMerge();
        JsonObject json = new JsonObject();
        JsonObject stateJson = existingJson.has("state") ? existingJson.getAsJsonObject("state") : new JsonObject();

        for (Map.Entry<String, Object> entry : stateMap.entrySet()) {
            stateJson.add(entry.getKey(), GsonFactory.getGson().toJsonTree(entry.getValue()));
        }
        json.add("state", stateJson);

        for (Map.Entry<String, FieldHandler<?>> entry : fields.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue().getter.get();
            json.add(name, value != null ? GsonFactory.getGson().toJsonTree(value) : existingJson.has(name) ? existingJson.get(name) : JsonNull.INSTANCE);
        }

        try (Writer writer = new FileWriter(file)) {
            GsonFactory.getGson().toJson(json, writer);
            LOG.debug("Saved session to: {}", file.getPath());
        } catch (IOException e) {
            LOG.error("Failed to save session to {}: {}", file.getPath(), e.getMessage(), e);
        }
    }

    public void loadSession(Consumer<JsonObject> jsonConsumer) {
        File file = ensureSessionDirectory();
        if (file == null) {
            return;
        }

        if (!file.exists()) {
            try {
                JsonObject emptyJson = new JsonObject();
                emptyJson.add("state", new JsonObject());
                Files.write(file.toPath(), GsonFactory.getGson().toJson(emptyJson).getBytes());
                LOG.info("Created empty session file: {}", file.getPath());
                jsonConsumer.accept(emptyJson);
            } catch (IOException e) {
                LOG.error("Failed to create empty session file {}: {}", file.getPath(), e.getMessage(), e);
            }
            return;
        }

        try (Reader reader = new FileReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            jsonConsumer.accept(json);
            LOG.debug("Loaded session from: {}", file.getPath());
        } catch (IOException | JsonSyntaxException e) {
            LOG.error("Failed to load session from {}: {}", file.getPath(), e.getMessage(), e);
        }
    }

    private JsonObject loadJsonForMerge() {
        File file = ensureSessionDirectory();
        if (file == null || !file.exists()) {
            return new JsonObject();
        }

        try (Reader reader = new FileReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            LOG.error("Failed to load session for merge from {}: {}", file.getPath(), e.getMessage(), e);
            return new JsonObject();
        }
    }

    public void loadFields(JsonObject json, Map<String, Object> stateMap) {
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
                    LOG.error("Failed to deserialize field {}: {}", name, e.getMessage(), e);
                }
            }
        }
    }

    public void deleteSessionFile() {
        String root = System.getProperty("user.dir");
        File file = new File(root, sessionFile);
        if (file.exists()) {
            try {
                Files.delete(Paths.get(file.getPath()));
                LOG.debug("Deleted session file: {}", file.getPath());
            } catch (IOException e) {
                LOG.error("Failed to delete session file {}: {}", file.getPath(), e.getMessage(), e);
            }
        }
    }

    private File ensureSessionDirectory() {
        String root = System.getProperty("user.dir");
        File file = new File(root, sessionFile);
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                LOG.error("Failed to create session directory: {}", parentDir.getPath());
                return null;
            }
        }
        return file;
    }
}