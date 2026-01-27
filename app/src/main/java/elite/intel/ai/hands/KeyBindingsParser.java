package elite.intel.ai.hands;

import elite.intel.db.managers.KeyBindingManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;


/**
 * A singleton class responsible for parsing key bindings from XML configuration files.
 * This class reads mappings between actions and keyboard inputs, including associated
 * keys, modifiers, and hold states.
 */
public class KeyBindingsParser {
    private static final Logger log = LogManager.getLogger(KeyBindingsParser.class);
    private static KeyBindingsParser instance;

    private KeyBindingsParser() {
        // Private constructor for singleton
    }

    public static synchronized KeyBindingsParser getInstance() {
        if (instance == null) {
            instance = new KeyBindingsParser();
        }
        return instance;
    }

    public class KeyBinding {
        String key;
        String[] modifiers;
        boolean hold;

        public KeyBinding(String key, String[] modifiers, boolean hold) {
            this.key = key;
            this.modifiers = modifiers;
            this.hold = hold;
        }
    }

    private final Set<String> BLACKLISTED_ACTIONS = new HashSet<>(Arrays.asList(
            "PrimaryFire", "SecondaryFire", "TriggerFieldNeutraliser",
            "BuggyPrimaryFireButton", "BuggySecondaryFireButton"
    ));

    public Map<String, KeyBinding> parseBindings(File file) throws Exception {
        Map<String, KeyBinding> bindings = new HashMap<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();

        NodeList nodes = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element) {
                Element element = (Element) nodes.item(i);
                String actionName = element.getTagName();

                NodeList primaryList = element.getElementsByTagName("Primary");
                NodeList secondaryList = element.getElementsByTagName("Secondary");

                if (primaryList.getLength() > 0) {
                    Element primary = (Element) primaryList.item(0);
                    if ("Keyboard".equals(primary.getAttribute("Device"))) {
                        String key = primary.getAttribute("Key");
                        boolean hold = "1".equals(primary.getAttribute("Hold"));
                        String[] modifiers = getModifiers(primary);
                        bindings.put(actionName, new KeyBinding(key, modifiers, hold));
                        log.debug("Parsed primary binding for {}: key={}, hold={}", actionName, key, hold);
                    }
                }

                if (secondaryList.getLength() > 0) {
                    Element secondary = (Element) secondaryList.item(0);
                    if ("Keyboard".equals(secondary.getAttribute("Device"))) {
                        String key = secondary.getAttribute("Key");
                        boolean hold = "1".equals(secondary.getAttribute("Hold"));
                        String[] modifiers = getModifiers(secondary);
                        bindings.put(actionName, new KeyBinding(key, modifiers, hold));
                        log.debug("Parsed secondary binding for {}: key={}, hold={}", actionName, key, hold);
                    }
                }
            }
        }
        log.info("Parsed {} bindings from file: {}", bindings.size(), file.getName());
        KeyBindingManager.getInstance().clear();
        return bindings;
    }

    private String[] getModifiers(Element binding) {
        NodeList modifierList = binding.getElementsByTagName("Modifier");
        String[] modifiers = new String[modifierList.getLength()];
        for (int i = 0; i < modifierList.getLength(); i++) {
            Element modifier = (Element) modifierList.item(i);
            modifiers[i] = modifier.getAttribute("Key");
        }
        return modifiers;
    }
}