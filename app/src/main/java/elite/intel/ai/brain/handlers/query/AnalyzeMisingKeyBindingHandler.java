package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.db.dao.KeyBindingDao.KeyBinding;
import elite.intel.db.managers.KeyBindingManager;

import java.util.List;

public class AnalyzeMisingKeyBindingHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        KeyBindingManager bindingManager = KeyBindingManager.getInstance();
        List<KeyBinding> missingBindings = bindingManager.getMissingBindings();
        StringBuilder sb = new StringBuilder();

        if (!missingBindings.isEmpty()) {
            sb.append("Missing Bindings: ");
            for (KeyBinding key : missingBindings) {
                sb.append(key.getKeyBinding());
                sb.append(",");
            }
            return process(sb.toString());
        } else {
            return process("No missing key bindings found.");
        }
    }
}
