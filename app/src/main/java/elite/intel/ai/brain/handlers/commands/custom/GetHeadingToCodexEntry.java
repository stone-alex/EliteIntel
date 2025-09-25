package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.CodexEntryEvent;
import elite.intel.session.PlayerSession;

import java.util.List;

import static elite.intel.util.NavigationUtils.getHeading;

public class GetHeadingToCodexEntry implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {

        PlayerSession playerSession = PlayerSession.getInstance();
        List<CodexEntryEvent> codexEntries = playerSession.getCodexEntries();
        if (codexEntries == null || codexEntries.isEmpty()) {
            EventBusManager.publish(new VoiceProcessEvent("No codex entries found."));
            return;
        }

        CodexEntryEvent entry = codexEntries.stream().findFirst().orElse(null /*not empty, so will never be null*/);

        double entryLatitude = entry.getLatitude();
        double entryLongitude = entry.getLongitude();
        double userLongitude = playerSession.getStatus().getLongitude();
        double userLatitude = playerSession.getStatus().getLatitude();
        double planetRadius = playerSession.getStatus().getPlanetRadius();

        EventBusManager.publish(new VoiceProcessEvent(getHeading(entryLatitude, entryLongitude, userLatitude, userLongitude, planetRadius)));
    }
}
