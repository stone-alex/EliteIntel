package elite.intel.ai.brain.actions.macro;

import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.StringUtls;

/**
 * Announces macro load results via TTS on application startup.
 * Call {@link #announce()} after {@link MacroRegistry#load()} has completed and
 * TTS subscribers are active.
 */
public class MacroLoadAnnouncement {

    private static volatile MacroLoadAnnouncement instance;

    private MacroLoadAnnouncement() {
    }

    public static synchronized MacroLoadAnnouncement getInstance() {
        if (instance == null) instance = new MacroLoadAnnouncement();
        return instance;
    }

    /**
     * Speaks a localized summary of the macro load result.
     * Reports the number of successfully loaded macros and, separately,
     * the number of macros skipped due to validation failures.
     */
    public void announce() {
        int loaded = MacroRegistry.getInstance().getMacros().size();
        int skipped = MacroRegistry.getInstance().getSkippedOnLastLoad();

        if (loaded > 0 && skipped > 0) {
            EventBusManager.publish(new AiVoxResponseEvent(
                    StringUtls.localizedSpeech("speech.macrosLoadedWithSkipped", loaded, skipped)
            ));
        } else if (loaded > 0) {
            EventBusManager.publish(new AiVoxResponseEvent(
                    StringUtls.localizedSpeech("speech.macrosLoaded", loaded)
            ));
        } else if (skipped > 0) {
            EventBusManager.publish(new AiVoxResponseEvent(
                    StringUtls.localizedSpeech("speech.macrosSkipped", skipped)
            ));
        }

        if (skipped > 0) {
            MacroRegistry.getInstance().getSkippedLabelsOnLastLoad()
                    .forEach(label -> EventBusManager.publish(new AppLogEvent("Invalid macro: " + label)));
        }
    }
}
