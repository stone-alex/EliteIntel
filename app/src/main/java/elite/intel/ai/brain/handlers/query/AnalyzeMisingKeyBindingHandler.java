package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.db.dao.KeyBindingDao.KeyBinding;
import elite.intel.db.managers.KeyBindingManager;

import java.util.List;

public class AnalyzeMisingKeyBindingHandler extends BaseQueryAnalyzer implements QueryHandler {

	@Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
		KeyBindingManager bindingManager = KeyBindingManager.getInstance();
		List<KeyBinding> bindings = bindingManager.getBindings();
		StringBuilder sb = new StringBuilder();

		sb.append("bindings: [ ");
		for (KeyBinding key : bindings) {
			sb.append(key.getKeyBinding());
			sb.append(",");
		}
		sb.append(" ]");
		return process(sb.toString());
	}
}
