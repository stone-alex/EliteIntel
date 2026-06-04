package elite.intel.ai.hands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;


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
        public String key;
        String[] modifiers;
        public boolean hold;

        public KeyBinding(String key, String[] modifiers, boolean hold) {
            this.key = key;
            this.modifiers = modifiers;
            this.hold = hold;
        }
    }

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

                KeyBinding keyBinding = null;

                if (primaryList.getLength() > 0) {
                    Element primary = (Element) primaryList.item(0);
                    if ("Keyboard".equals(primary.getAttribute("Device"))) {
                        String key = primary.getAttribute("Key");
                        boolean hold = "1".equals(primary.getAttribute("Hold"));
                        keyBinding = new KeyBinding(key, getModifiers(primary), hold);
                        log.debug("Parsed primary binding for {}: key={}, hold={}", actionName, key, hold);
                    }
                }

                if (keyBinding == null && secondaryList.getLength() > 0) {
                    Element secondary = (Element) secondaryList.item(0);
                    if ("Keyboard".equals(secondary.getAttribute("Device"))) {
                        String key = secondary.getAttribute("Key");
                        boolean hold = "1".equals(secondary.getAttribute("Hold"));
                        keyBinding = new KeyBinding(key, getModifiers(secondary), hold);
                        log.debug("Parsed secondary binding for {}: key={}, hold={}", actionName, key, hold);
                    }
                }

                if (keyBinding == null && primaryList.getLength() > 0 && secondaryList.getLength() > 0) {
                    log.warn("No keyboard binding for action '{}' (primary={}, secondary={})",
                            actionName,
                            ((Element) primaryList.item(0)).getAttribute("Device"),
                            ((Element) secondaryList.item(0)).getAttribute("Device"));
                }

                if (keyBinding != null) {
                    bindings.put(actionName, keyBinding);
                }
            }
        }
        log.info("Parsed {} bindings from file: {}", bindings.size(), file.getName());
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