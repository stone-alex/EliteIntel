package elite.intel.ai.brain;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Normalizes raw STT user input by substituting synonym verbs/phrases with
 * canonical forms that match the action keys in AiActionsMap.
 * <p>
 * Phrases are matched before single words (order matters).
 * Context-sensitive verbs (show, open, set, check) are intentionally excluded -
 * the LLM handles those correctly and normalization would cause false substitutions.
 */
public class InputNormalizer {

    private static final InputNormalizer INSTANCE = new InputNormalizer();

    // Ordered: phrases first, then single words.
    // Key = what user might say (lowercase), Value = canonical form used in action keys.
    private static final LinkedHashMap<String, String> SYNONYM_MAP = new LinkedHashMap<>();

    static {
        // HUD mode switches - must appear before single-word "activate" rules

        // HUD mode switches - must appear before single-word "activate" rules
        SYNONYM_MAP.put("activate combat mode", "switch to combat mode");
        SYNONYM_MAP.put("activate analysis mode", "switch to analysis mode");
        SYNONYM_MAP.put("hud combat mode", "switch to combat mode");
        SYNONYM_MAP.put("hud analysis mode", "switch to analysis mode");
        SYNONYM_MAP.put("combat mode", "switch to combat mode");
        SYNONYM_MAP.put("analysis mode", "switch to analysis mode");
        SYNONYM_MAP.put("switch to combat", "switch to combat mode");
        SYNONYM_MAP.put("enter combat mode", "switch to combat mode");
        SYNONYM_MAP.put("combat hud", "switch to combat mode");
        SYNONYM_MAP.put("switch to analysis", "switch to analysis mode");
        SYNONYM_MAP.put("analysis hud", "switch to analysis mode");
        SYNONYM_MAP.put("explorer mode", "switch to analysis mode");

        // find / locate / search
        SYNONYM_MAP.put("search for", "find");
        SYNONYM_MAP.put("search", "find");
        SYNONYM_MAP.put("locate", "find");
        SYNONYM_MAP.put("look for", "find");
        SYNONYM_MAP.put("where is", "find");
        SYNONYM_MAP.put("where can i", "find");
        SYNONYM_MAP.put("where can we", "find");
        SYNONYM_MAP.put("find me", "find");
        SYNONYM_MAP.put("location of", "find");

        // navigate / go / head / travel
        SYNONYM_MAP.put("head over to", "navigate to");
        SYNONYM_MAP.put("head to", "navigate to");
        SYNONYM_MAP.put("go to", "navigate to");
        SYNONYM_MAP.put("take us to", "navigate to");
        SYNONYM_MAP.put("take me to", "navigate to");
        SYNONYM_MAP.put("get me to", "navigate to");
        SYNONYM_MAP.put("get us to", "navigate to");
        SYNONYM_MAP.put("travel to", "navigate to");
        SYNONYM_MAP.put("route to", "navigate to");
        SYNONYM_MAP.put("set course to", "navigate to");
        SYNONYM_MAP.put("set course for", "navigate to");
        SYNONYM_MAP.put("guide me to", "navigate to");
        SYNONYM_MAP.put("guide us to", "navigate to");
        SYNONYM_MAP.put("plot course to", "navigate to");
        SYNONYM_MAP.put("plot a course", "navigate");
        SYNONYM_MAP.put("fly to", "navigate to");
        SYNONYM_MAP.put("set destination", "navigate to");
        SYNONYM_MAP.put("take us to", "navigate to");
        SYNONYM_MAP.put("get me to", "navigate to");
        SYNONYM_MAP.put("plot route to", "navigate to");

        // jump / hyperspace
        SYNONYM_MAP.put("get out of here", "jump to hyperspace");
        SYNONYM_MAP.put("let's get moving", "jump to hyperspace");
        SYNONYM_MAP.put("proceed to", "jump to hyperspace");
        SYNONYM_MAP.put("next way point", "jump to hyperspace");
        SYNONYM_MAP.put("punch it", "jump to hyperspace");
        // new additions
        SYNONYM_MAP.put("engage fsd", "jump to hyperspace");
        SYNONYM_MAP.put("engage hyperspace", "jump to hyperspace");
        SYNONYM_MAP.put("jump", "jump to hyperspace");
        SYNONYM_MAP.put("hyperspace jump", "jump to hyperspace");
        SYNONYM_MAP.put("make the jump", "jump to hyperspace");
        SYNONYM_MAP.put("let's jump", "jump to hyperspace");
        SYNONYM_MAP.put("frame shift drive", "jump to hyperspace");
        SYNONYM_MAP.put("engage drive", "jump to hyperspace");
        SYNONYM_MAP.put("get us out", "jump to hyperspace");
        SYNONYM_MAP.put("let's bounce", "jump to hyperspace");

        // retract / store / holster
        SYNONYM_MAP.put("weapons cold", "retract hardpoints");
        SYNONYM_MAP.put("store weapons", "retract hardpoints");
        SYNONYM_MAP.put("holster weapons", "retract hardpoints");
        SYNONYM_MAP.put("weapons away", "retract hardpoints");
        // new additions
        SYNONYM_MAP.put("retract hardpoints", "retract hardpoints");
        SYNONYM_MAP.put("retract weapons", "retract hardpoints");
        SYNONYM_MAP.put("stow hardpoints", "retract hardpoints");
        SYNONYM_MAP.put("put weapons away", "retract hardpoints");
        SYNONYM_MAP.put("hardpoints in", "retract hardpoints");

        // deploy hardpoints
        SYNONYM_MAP.put("weapons hot", "deploy hardpoints");
        SYNONYM_MAP.put("weapons free", "deploy hardpoints");
        SYNONYM_MAP.put("weapons out", "deploy hardpoints");
        SYNONYM_MAP.put("weapons ready", "deploy hardpoints");
        SYNONYM_MAP.put("arm weapons", "deploy hardpoints");
        SYNONYM_MAP.put("deploy hardpoints", "deploy hardpoints");
        SYNONYM_MAP.put("deploy weapons", "deploy hardpoints");
        SYNONYM_MAP.put("hardpoints out", "deploy hardpoints");
        SYNONYM_MAP.put("arm up", "deploy hardpoints");
        SYNONYM_MAP.put("ready weapons", "deploy hardpoints");

        // deploy / extend / release
        SYNONYM_MAP.put("extend", "deploy");
        SYNONYM_MAP.put("release", "deploy");
        SYNONYM_MAP.put("lower", "deploy");
        SYNONYM_MAP.put("put out", "deploy");
        SYNONYM_MAP.put("launch", "deploy");


        // Exploration
        SYNONYM_MAP.put("launch srv", "deploy srv");
        SYNONYM_MAP.put("deploy car", "deploy srv");
        SYNONYM_MAP.put("deploy vehicle", "deploy srv");

        SYNONYM_MAP.put("board ship", "recover SRV");
        SYNONYM_MAP.put("extract car", "recover SRV");
        SYNONYM_MAP.put("extract vehicle", "recover SRV");
        SYNONYM_MAP.put("recover car", "recover SRV");
        SYNONYM_MAP.put("recover vehicle", "recover SRV");
        SYNONYM_MAP.put("recover srv", "recover SRV");
        SYNONYM_MAP.put("extract srv", "recover SRV");

        SYNONYM_MAP.put("discovery profit", "exploration profits");
        SYNONYM_MAP.put("profits from discovery", "exploration profits");
        SYNONYM_MAP.put("profit from discovery", "exploration profits");
        SYNONYM_MAP.put("exobiology profit", "exploration profits");
        SYNONYM_MAP.put("profit from exobiology", "exploration profits");
        SYNONYM_MAP.put("profits from exobiology", "exploration profits");


        // retract / raise
        SYNONYM_MAP.put("raise", "retract");
        SYNONYM_MAP.put("stow", "retract");
        SYNONYM_MAP.put("retract", "retract");
        SYNONYM_MAP.put("pull in", "retract");
        SYNONYM_MAP.put("hide", "retract");

        // calculate / compute / plot
        SYNONYM_MAP.put("compute", "calculate");
        SYNONYM_MAP.put("work out", "calculate");
        SYNONYM_MAP.put("figure out", "calculate");

        // cancel / abort / stop (navigation/route context)
        SYNONYM_MAP.put("abort", "cancel");
        SYNONYM_MAP.put("clear route", "cancel navigation");
        SYNONYM_MAP.put("stop navigation", "cancel navigation");

        // target
        SYNONYM_MAP.put("aim at", "target");
        SYNONYM_MAP.put("focus on", "target");
        SYNONYM_MAP.put("lock on", "target");
        SYNONYM_MAP.put("lock onto", "target");
        SYNONYM_MAP.put("acquire target", "target");
        SYNONYM_MAP.put("select target", "target");

        // scan
        SYNONYM_MAP.put("honk", "scan the system");
        SYNONYM_MAP.put("full scan", "scan the system");
        SYNONYM_MAP.put("fss scan", "scan the system");
        SYNONYM_MAP.put("open fss and scan", "scan the system");
        SYNONYM_MAP.put("system scan", "scan the system");
        // new additions
        SYNONYM_MAP.put("scan system", "scan the system");
        SYNONYM_MAP.put("run a scan", "scan the system");
        SYNONYM_MAP.put("perform system scan", "scan the system");
        SYNONYM_MAP.put("discovery scan", "scan the system");

        // dismiss / send away
        SYNONYM_MAP.put("go play", "dismiss ship");
        SYNONYM_MAP.put("dismiss ship", "dismiss ship");

        // equalize / balance / reset
        SYNONYM_MAP.put("balance power", "equalize power");
        SYNONYM_MAP.put("reset power", "equalize power");
        SYNONYM_MAP.put("equalize", "equalize power");
        SYNONYM_MAP.put("equalize power", "equalize power");
        SYNONYM_MAP.put("balance", "equalize power");
        SYNONYM_MAP.put("reset systems", "equalize power");
        SYNONYM_MAP.put("even out power", "equalize power");

        // activate
        SYNONYM_MAP.put("engage", "activate");
        SYNONYM_MAP.put("select", "activate");
        SYNONYM_MAP.put("activate", "activate");
        SYNONYM_MAP.put("turn on", "activate");
        SYNONYM_MAP.put("enable", "activate");

        // stop listening / ignore user
        SYNONYM_MAP.put("ignore me", "stop listening");
        SYNONYM_MAP.put("sleep", "stop listening");
        SYNONYM_MAP.put("stop listening", "stop listening");

        // start listening
        SYNONYM_MAP.put("listen up", "start listening");
        SYNONYM_MAP.put("listen to me", "start listening");
        SYNONYM_MAP.put("pay attention", "start listening");
        SYNONYM_MAP.put("start listening", "start listening");
        SYNONYM_MAP.put("wake up", "start listening");

        // phonetics
        SYNONYM_MAP.put("of", "off");
        SYNONYM_MAP.put("manax", "max");
        SYNONYM_MAP.put("hard points", "hardpoints");
    }

    private InputNormalizer() {
    }

    public static InputNormalizer getInstance() {
        return INSTANCE;
    }

    /**
     * Returns a normalized version of the input with synonyms replaced by
     * their canonical forms. The original input is returned unchanged if no
     * synonyms match. Matching is case-insensitive; output case follows the
     * canonical form for the replaced segment and preserves original case elsewhere.
     */
    public String normalize(String input) {
        if (input == null || input.isBlank()) return input;
        String lower = input.toLowerCase();
        for (Map.Entry<String, String> entry : SYNONYM_MAP.entrySet()) {
            String synonym = entry.getKey();
            String canonical = entry.getValue();
            int idx = lower.indexOf(synonym);
            if (idx >= 0) {
                input = input.substring(0, idx) + canonical + input.substring(idx + synonym.length());
                lower = input.toLowerCase();
            }
        }
        // Strip STT noise words that carry no action signal
        input = input.replaceAll("(?i)\\bwhere\\b", "").replaceAll("\\s{2,}", " ").trim();
        return input;
    }
}
