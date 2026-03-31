package elite.intel.test;

import elite.intel.ai.ApiFactory;
import elite.intel.ai.brain.AiCommandInterface;
import elite.intel.db.util.Database;
import elite.intel.session.SystemSession;
import elite.intel.util.Cypher;

/**
 * Starts only the BRAIN service (LLM endpoint + ResponseRouter + HandsSubscriber).
 * No TTS, no STT, no journals, no UI.
 * <p>
 * Prerequisites:
 * - Ollama (or whatever LLM backend is configured) must be running
 * - The app's SQLite DB must exist (normal app usage populates it)
 */
public class HeadlessBootstrap {

    private static AiCommandInterface brain;
    private static boolean initialized = false;

    public static synchronized void start() throws InterruptedException {
        if (initialized) return;

        Cypher.initializeKey();
        Database.init();

        // "false" = do not ignore input (i.e. listen mode on)
        SystemSession.getInstance().stopStartListening(false);

        brain = ApiFactory.getInstance().getCommandEndpoint();
        brain.start();

        // Give the LLM endpoint a moment to connect and stabilise
        Thread.sleep(2500);
        initialized = true;
    }

    public static synchronized void stop() {
        if (brain != null) {
            brain.stop();
            brain = null;
        }
        initialized = false;
    }
}
