package elite.companion.util;

import elite.companion.ui.event.AppLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleApiKeyProvider {
    private static final Logger log = LoggerFactory.getLogger(GoogleApiKeyProvider.class);
    private static final GoogleApiKeyProvider INSTANCE = new GoogleApiKeyProvider();
    private String apiKey;

    private GoogleApiKeyProvider() {
        // Load API key on initialization to avoid repeated I/O
        apiKey = ConfigManager.getInstance().getSystemKey(ConfigManager.GOOGLE_API_KEY);
        if (apiKey == null || apiKey.trim().isEmpty()) {
            String errorMessage = "Google API key not found in system.conf";
            log.error(errorMessage);
            EventBusManager.publish(new AppLogEvent(errorMessage));
        }
        log.debug("Google API key loaded successfully");
    }

    public static GoogleApiKeyProvider getInstance() {
        return INSTANCE;
    }

    public String getGoogleApiKey() {
        return apiKey;
    }
}