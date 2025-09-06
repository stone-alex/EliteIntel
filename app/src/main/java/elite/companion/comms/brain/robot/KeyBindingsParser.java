package elite.companion.comms.brain.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

/**
 * A parser that reads and processes key binding configurations from an XML file.
 * The class is designed to handle mappings between actions and their associated
 * keyboard inputs, including modifiers and an optional hold property.
 * <p>
 * This parser supports excluding certain predefined blacklisted actions and
 * specific patterns, such as actions starting with "Humanoid". It processes
 * both primary and secondary key bindings defined in the XML structure.
 */
public class KeyBindingsParser {
    private static final Logger log = LoggerFactory.getLogger(KeyBindingsParser.class);

    public static class KeyBinding {
        String key;
        String[] modifiers;
        boolean hold;

        public KeyBinding(String key, String[] modifiers, boolean hold) {
            this.key = key;
            this.modifiers = modifiers;
            this.hold = hold;
        }
    }

    private static final Set<String> BLACKLISTED_ACTIONS = new HashSet<>(Arrays.asList(
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

                // Skip blacklisted actions and any Humanoid* actions
                if (BLACKLISTED_ACTIONS.contains(actionName) || actionName.startsWith("Humanoid")) {
                    log.debug("Skipping blacklisted or Humanoid action: {}", actionName);
                    continue;
                }

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