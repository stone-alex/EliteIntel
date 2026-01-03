package test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import elite.intel.ai.hands.*;
import elite.intel.ai.hands.KeyBindingsParser.KeyBinding;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.lang.reflect.Field;

public class BindingsVerifier {
	private static final Logger log = LogManager.getLogger(BindingsVerifier.class);
    private static BindingsVerifier instance;

    private BindingsVerifier() {
    	// Private constructor for singleton
    }

    public static synchronized BindingsVerifier getInstance() {
    	if (instance == null) {
    		instance = new BindingsVerifier();
    	}
    	return instance;
    }

	public static void getBindings() {
		Path filePath = Path.of("/home/andreas/.steam/steam/steamapps/compatdata/359320/pfx/drive_c/users/steamuser/AppData/Local/Frontier Developments/Elite Dangerous/Options/Bindings/Custom.4.2.binds");
		File file = filePath.toFile();

		KeyBindingsParser parser = KeyBindingsParser.getInstance();

		Map<String, ?> bindings = parser.parseBindings(file);
		BindingsVerifier.print(bindings);
		/*
		try {
			Map<String, KeyBinding> bindings = parser.parseBindings(file);
			for (String i : bindings.keySet()) {
				System.out.println(i + ":" + bindings.get(i));
			}
		}
		catch (Exception e) {
			System.err.println("Failed to parse bindings: " + e.getMessage());
            e.printStackTrace();
		}
		*/
	}

	private static String format(Object kb) throws ReflectiveOperationException {
		Class <?> c = kb.getClass();

		Field fKey = c.getDeclearedField("key");
		Field fModifiers = c.getDeclearedField("modifiers");
		Field fHold = c.getDeclearedField("hold");

		fKey.setAccesible(true);
		fModifiers.setAccesible(true);
		fHold.setAccesible(true);

		String key = (String) fKey.get(kb);
		String modifiers = (String[]) fModifiers.get(kb);
		boolean hold = (boolean) fHold.get(kb);

		StringBuilder sb = new StringBuilder();
		if (mods != null) {
			for (String m : mods) sb.append(m).append("=");
		}
		sb.append(key);
		if(hold) sb.append(" (hold)");
		return sb.toString();
	}

	public static void print(Map<String, ?> map) {
		map.forEach((action, kb) -> {
			try {
				System.out.println(action + " = " + format(kb));
			}
			catch (Exception e) {
                System.err.println("Could not decode binding for " + action);
            }
		});
	}
}