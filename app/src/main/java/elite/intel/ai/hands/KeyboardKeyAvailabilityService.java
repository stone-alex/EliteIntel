package elite.intel.ai.hands;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Computes keyboard-key availability from the whole active {@code .binds} file.
 * <p>
 * This must not be limited to {@link Bindings.GameCommand}; a key assigned to
 * any Elite Dangerous action can still create an in-game conflict if reused.
 */
public class KeyboardKeyAvailabilityService {

    /**
     * Returns every occupied keyboard token found in Primary and Secondary slots
     * across all action nodes.
     */
    public Set<String> occupiedKeyboardKeys(Path bindsFile) throws Exception {
        Set<String> occupied = new LinkedHashSet<>();
        for (SlotAssignment assignment : keyboardSlotAssignments(bindsFile)) {
            occupied.add(assignment.key());
        }
        return occupied;
    }

    /**
     * Returns assignable keys that are not occupied anywhere in the file.
     */
    public List<String> availableKeys(Path bindsFile) throws Exception {
        Set<String> occupied = occupiedKeyboardKeys(bindsFile);
        return EliteKeyboardKeys.assignableKeys().stream()
                .filter(key -> !occupied.contains(key))
                .toList();
    }

    /**
     * Returns free keys plus the edited slot's current keyboard key, if any, so
     * a dialog can represent "leave unchanged" without permitting a new conflict.
     */
    public List<String> availableKeys(Path bindsFile, String bindingId, BindingSlotType slotType) throws Exception {
        Set<String> occupied = occupiedKeyboardKeys(bindsFile);
        List<String> available = new ArrayList<>(EliteKeyboardKeys.assignableKeys().stream()
                .filter(key -> !occupied.contains(key))
                .toList());

        currentKeyboardSlotKey(bindsFile, bindingId, slotType).ifPresent(currentKey -> {
            if (!available.contains(currentKey)) {
                available.add(currentKey);
            }
        });
        available.sort(String.CASE_INSENSITIVE_ORDER);
        return List.copyOf(available);
    }

    /**
     * Save-time conflict check that ignores only the slot currently being edited.
     */
    public boolean isKeyOccupiedByOtherSlot(
            Path bindsFile,
            String bindingId,
            BindingSlotType slotType,
            String key
    ) throws Exception {
        for (SlotAssignment assignment : keyboardSlotAssignments(bindsFile)) {
            if (!assignment.key().equals(key)) {
                continue;
            }
            if (assignment.bindingId().equals(bindingId) && assignment.slotType() == slotType) {
                continue;
            }
            return true;
        }
        return false;
    }

    /**
     * Reads the current keyboard key from the selected slot, if the slot is a
     * keyboard assignment.
     */
    public Optional<String> currentKeyboardSlotKey(
            Path bindsFile,
            String bindingId,
            BindingSlotType slotType
    ) throws Exception {
        Document doc = parse(bindsFile);
        Element action = directChildElement(doc.getDocumentElement(), bindingId).orElse(null);
        if (action == null) {
            return Optional.empty();
        }

        Element slot = directChildElement(action, slotType.xmlElementName()).orElse(null);
        if (slot == null) {
            return Optional.empty();
        }

        String device = slot.getAttribute("Device");
        String key = slot.getAttribute("Key");
        return isOccupiedKeyboardKey(device, key) ? Optional.of(key) : Optional.empty();
    }

    private List<SlotAssignment> keyboardSlotAssignments(Path bindsFile) throws Exception {
        Document doc = parse(bindsFile);
        List<SlotAssignment> assignments = new ArrayList<>();
        collectKeyboardSlotAssignments(doc, BindingSlotType.PRIMARY, assignments);
        collectKeyboardSlotAssignments(doc, BindingSlotType.SECONDARY, assignments);
        return assignments;
    }

    private void collectKeyboardSlotAssignments(
            Document doc,
            BindingSlotType slotType,
            List<SlotAssignment> assignments
    ) {
        NodeList slots = doc.getElementsByTagName(slotType.xmlElementName());
        for (int i = 0; i < slots.getLength(); i++) {
            Node slotNode = slots.item(i);
            if (!(slotNode instanceof Element slot)) {
                continue;
            }
            String device = slot.getAttribute("Device");
            String key = slot.getAttribute("Key");
            if (!isOccupiedKeyboardKey(device, key)) {
                continue;
            }

            Node parent = slot.getParentNode();
            String bindingId = parent instanceof Element action ? action.getTagName() : "";
            assignments.add(new SlotAssignment(bindingId, slotType, key));
        }
    }

    private boolean isOccupiedKeyboardKey(String device, String key) {
        // Only keyboard assignments block keyboard availability. HOTAS, joystick,
        // gamepad, and mouse bindings are displayed elsewhere but cannot consume
        // a keyboard key for this MVP.
        return "Keyboard".equals(device)
                && key != null
                && !key.isBlank()
                && !"{NoDevice}".equals(key)
                && !"Key_".equals(key);
    }

    private Optional<Element> directChildElement(Element parent, String tagName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element element && tagName.equals(element.getTagName())) {
                return Optional.of(element);
            }
        }
        return Optional.empty();
    }

    private Document parse(Path bindsFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(bindsFile.toFile());
        doc.getDocumentElement().normalize();
        return doc;
    }

    private record SlotAssignment(String bindingId, BindingSlotType slotType, String key) {
    }
}
