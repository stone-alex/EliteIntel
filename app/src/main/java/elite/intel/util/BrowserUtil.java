package elite.intel.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for browser-related operations.
 */
public class BrowserUtil {
    private static final Logger LOGGER = Logger.getLogger(BrowserUtil.class.getName());

    /**
     * Opens the specified URL in the system's default browser.
     *
     * @param url the URL to open
     * @return true if the browser was opened successfully, false otherwise
     */
    public static boolean openUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            LOGGER.warning("Cannot open browser: URL is null or empty");
            return false;
        }

        // Check if Desktop is supported on this platform
        if (!Desktop.isDesktopSupported()) {
            LOGGER.warning("Desktop is not supported on this platform");
            return false;
        }

        try {
            URI uri = new URI(url);
            Desktop.getDesktop().browse(uri);
            LOGGER.info("Opened URL in default browser: " + url);
            return true;
        } catch (URISyntaxException e) {
            LOGGER.log(Level.WARNING, "Invalid URL syntax: " + url, e);
            return false;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to open browser for URL: " + url, e);
            return false;
        }
    }
}