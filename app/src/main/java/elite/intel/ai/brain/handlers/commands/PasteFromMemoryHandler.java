package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.KeyProcessor;
import elite.intel.util.ClipboardUtils;

public class PasteFromMemoryHandler implements CommandHandler {


    @Override
    public void handle(String action, JsonObject params, String responseText) {
        KeyProcessor.getInstance().enterText(ClipboardUtils.getClipboardText());
    }
}
