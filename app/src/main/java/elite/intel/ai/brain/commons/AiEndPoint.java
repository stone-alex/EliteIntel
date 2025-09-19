package elite.intel.ai.brain.commons;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public abstract class AiEndPoint {

    //private static final ThreadLocal<JsonArray> messageHistory = new ThreadLocal<>();

    protected JsonArray sanitizeJsonArray(JsonArray messages) {
        if (messages == null) {
            return new JsonArray();
        }
        JsonArray sanitized = new JsonArray();
        for (int i = 0; i < messages.size(); i++) {
            JsonObject original = messages.get(i).getAsJsonObject();
            JsonObject sanitizedObj = new JsonObject();
            sanitizedObj.addProperty("role", original.get("role").getAsString());
            sanitizedObj.addProperty("content", escapeJson(original.get("content").getAsString()));
            sanitized.add(sanitizedObj);
        }
        return sanitized;
    }

    protected String escapeJson(String input) {
        if (input == null || input.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c < 32 || c == 127 || c == 0xFEFF) {
                sb.append(' '); // Replace control characters
            } else if (c == '"') {
                sb.append("\\\"");
            } else if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '\t') {
                sb.append("\\t");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    protected String toDebugString(String input) {
        if (input == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c < 32 || c == 127 || c == 0xFEFF) {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

/*    protected ThreadLocal<JsonArray> getMessageHistory() {
        return messageHistory;
    }

    protected void clearMessageHistory() {
        messageHistory.remove();
    }*/
}
