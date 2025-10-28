package elite.intel.ai.brain;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.HttpURLConnection;

public interface Client {
    JsonObject createRequestBodyHeader(String model, float temp);
    JsonObject createErrorResponse(String message);
    HttpURLConnection getHttpURLConnection() throws IOException;
}
