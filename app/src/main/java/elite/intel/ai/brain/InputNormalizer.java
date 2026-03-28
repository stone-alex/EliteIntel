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
        SYNONYM_MAP.put("brain tree", "find brain trees");
        SYNONYM_MAP.put("brain trees", "find brain trees");

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

        // FTL
        SYNONYM_MAP.put("light speed", "enter supercruise");
        SYNONYM_MAP.put("lightspeed", "enter supercruise");
        SYNONYM_MAP.put("supercruise", "enter supercruise");
        SYNONYM_MAP.put("super cruise", "enter supercruise");

        SYNONYM_MAP.put("drop", "drop from supercruise");
        SYNONYM_MAP.put("drop here", "drop from supercruise");
        SYNONYM_MAP.put("exit supercruise", "drop from supercruise");


        //ship queries
        SYNONYM_MAP.put("check loadout", "ship loadout");
        SYNONYM_MAP.put("access loadout", "ship loadout");
        SYNONYM_MAP.put("loadout", "ship loadout");

        // retract / store / holster
        SYNONYM_MAP.put("weapons cold", "retract hardpoints");
        SYNONYM_MAP.put("store weapons", "retract hardpoints");
        SYNONYM_MAP.put("holster weapons", "retract hardpoints");
        SYNONYM_MAP.put("weapons away", "retract hardpoints");
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

        SYNONYM_MAP.put("exit scanner", "exit close panel");
        SYNONYM_MAP.put("close scanner", "exit close panel");
        SYNONYM_MAP.put("close the scanner", "exit close panel");


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

        // ── Route / plotted route queries ─────────────────────────────────────
        SYNONYM_MAP.put("how many jumps remaining", "plotted route");
        SYNONYM_MAP.put("how many jumps left", "plotted route");
        SYNONYM_MAP.put("jumps to destination", "plotted route");
        SYNONYM_MAP.put("jumps remaining", "plotted route");
        SYNONYM_MAP.put("jumps left", "plotted route");
        SYNONYM_MAP.put("route progress", "route analysis");
        SYNONYM_MAP.put("is next star scoopable", "fuel at next stop");
        SYNONYM_MAP.put("can we scoop next star", "fuel at next stop");
        SYNONYM_MAP.put("scoopable at next stop", "fuel at next stop");
        SYNONYM_MAP.put("fuel at next star", "fuel at next stop");
        SYNONYM_MAP.put("next jump fuel", "fuel at next stop");
        SYNONYM_MAP.put("fuel stop on route", "fuel at next stop");

        // ── Ship fuel status ───────────────────────────────────────────────────
        SYNONYM_MAP.put("do we have enough fuel", "fuel status");
        SYNONYM_MAP.put("fuel check", "fuel status");
        SYNONYM_MAP.put("fuel gauge", "fuel status");
        SYNONYM_MAP.put("running low on fuel", "fuel status");
        SYNONYM_MAP.put("tank level", "fuel status");
        SYNONYM_MAP.put("fuel reading", "fuel status");

        // ── FSD jump target ────────────────────────────────────────────────────
        SYNONYM_MAP.put("next jump destination", "jump destination");
        SYNONYM_MAP.put("what star are we targeting", "FSD target");
        SYNONYM_MAP.put("analyze jump target", "analyze destination");
        SYNONYM_MAP.put("info on next jump", "FSD target");

        // ── Ship loadout ───────────────────────────────────────────────────────
        SYNONYM_MAP.put("ship configuration", "ship loadout");
        SYNONYM_MAP.put("what modules do we have", "ship loadout");
        SYNONYM_MAP.put("module list", "ship loadout");
        SYNONYM_MAP.put("our build", "ship loadout");
        SYNONYM_MAP.put("ready for a hunt", "ship loadout combat readiness summary");

        // ── Current location ───────────────────────────────────────────────────
        SYNONYM_MAP.put("where am i", "current location");
        SYNONYM_MAP.put("our coordinates", "current location");
        SYNONYM_MAP.put("what planet are we at", "current location");
        SYNONYM_MAP.put("current system", "current location");
        SYNONYM_MAP.put("our current position", "current location");

        // ── Stellar objects ────────────────────────────────────────────────────
        SYNONYM_MAP.put("how many planets", "planets in system");
        SYNONYM_MAP.put("system bodies", "bodies in system");
        SYNONYM_MAP.put("what bodies are here", "bodies in system");

        // ── Bio signals (system-wide) ──────────────────────────────────────────
        SYNONYM_MAP.put("bio signals in system", "bio scan progress");
        SYNONYM_MAP.put("biological signals in system", "bio scan progress");
        SYNONYM_MAP.put("biosignals in system", "bio scan progress");
        SYNONYM_MAP.put("organics in system", "bio scan progress");
        SYNONYM_MAP.put("how many bio signals", "bio scan progress");

        // ── Exobiology samples (planet surface) ───────────────────────────────
        SYNONYM_MAP.put("what organisms remain", "exobiology samples");
        SYNONYM_MAP.put("remaining organisms", "exobiology samples");
        SYNONYM_MAP.put("what's left to scan", "exobiology samples");
        SYNONYM_MAP.put("samples left", "exobiology samples");
        SYNONYM_MAP.put("exobiology remaining", "exobiology samples");
        SYNONYM_MAP.put("how many samples left", "exobiology samples");
        SYNONYM_MAP.put("scan remaining", "exobiology samples");

        // ── Exploration profits ────────────────────────────────────────────────
        SYNONYM_MAP.put("mapping profit", "exploration profits");
        SYNONYM_MAP.put("scan earnings", "exploration profits");
        SYNONYM_MAP.put("worth scanning", "exploration profits");
        SYNONYM_MAP.put("exobiology value", "exploration profits");
        SYNONYM_MAP.put("what are scans worth", "exploration profits");

        // ── Engineering materials (not cargo commodities) ─────────────────────
        SYNONYM_MAP.put("engineering materials", "material inventory");
        SYNONYM_MAP.put("raw materials inventory", "material inventory");
        SYNONYM_MAP.put("manufactured materials", "material inventory");
        SYNONYM_MAP.put("encoded materials", "material inventory");
        SYNONYM_MAP.put("crafting materials", "material inventory");

        // ── Ship Controls  ─────────────────────
        SYNONYM_MAP.put("radio traffic", "toggle radio");
        SYNONYM_MAP.put("radio traffic on", "toggle radio on");
        SYNONYM_MAP.put("radio traffic off", "toggle radio off");
        SYNONYM_MAP.put("radio transmission", "toggle radio");
        SYNONYM_MAP.put("radio transmission on", "toggle radio on");
        SYNONYM_MAP.put("radio transmission off", "toggle radio off");
        SYNONYM_MAP.put("shut up", "interrupt");

        // ── Cargo / commodities (not engineering materials) ───────────────────
        SYNONYM_MAP.put("commodity inventory", "cargo contents");
        SYNONYM_MAP.put("what commodities do we have", "cargo contents");
        SYNONYM_MAP.put("cargo manifest", "cargo hold");
        SYNONYM_MAP.put("what are we hauling", "cargo hold");
        SYNONYM_MAP.put("what's in the hold", "cargo hold");
        SYNONYM_MAP.put("hold contents", "cargo hold");
        SYNONYM_MAP.put("deploy cargo scoop", "open cargo scoop");
        SYNONYM_MAP.put("open cargo bay", "open cargo scoop");
        SYNONYM_MAP.put("retract cargo scoop", "close cargo scoop");
        SYNONYM_MAP.put("close cargo bay", "close cargo scoop");
        SYNONYM_MAP.put("display", "open");


        // ── Carrier status / fuel / ETA / destination ─────────────────────────
        SYNONYM_MAP.put("when does carrier arrive", "carrier ETA");
        SYNONYM_MAP.put("carrier arrival time", "carrier ETA");
        SYNONYM_MAP.put("carrier arrival", "carrier ETA");
        SYNONYM_MAP.put("when does carrier jump", "carrier ETA");
        SYNONYM_MAP.put("carrier jump time", "carrier ETA");
        SYNONYM_MAP.put("carrier fuel level", "carrier tritium");
        SYNONYM_MAP.put("tritium level", "carrier tritium");
        SYNONYM_MAP.put("tritium reserve", "carrier tritium");
        SYNONYM_MAP.put("where is carrier headed", "carrier destination");
        SYNONYM_MAP.put("carrier final destination", "carrier destination");
        SYNONYM_MAP.put("carrier heading", "carrier destination");

        // ── Station / outfitting / shipyard ───────────────────────────────────
        SYNONYM_MAP.put("station info", "station details");
        SYNONYM_MAP.put("station facilities", "station details");
        SYNONYM_MAP.put("available modules", "modules available");
        SYNONYM_MAP.put("buy modules", "outfitting");
        SYNONYM_MAP.put("ship parts", "outfitting");
        SYNONYM_MAP.put("station equipment", "outfitting");
        SYNONYM_MAP.put("available ships", "ships for sale");
        SYNONYM_MAP.put("buy a ship", "shipyard");
        SYNONYM_MAP.put("new ship options", "shipyard");

        // ── Trade ──────────────────────────────────────────────────────────────
        SYNONYM_MAP.put("what are we trading", "trade route");
        SYNONYM_MAP.put("trading schedule", "trade route");
        SYNONYM_MAP.put("our trading plan", "trade route");
        SYNONYM_MAP.put("trade configuration", "trade profile");
        SYNONYM_MAP.put("trading criteria", "trade profile");
        SYNONYM_MAP.put("our trade settings", "trade profile");

        // ── System security / politics ─────────────────────────────────────────
        SYNONYM_MAP.put("who owns this system", "faction control");
        SYNONYM_MAP.put("who controls this system", "faction control");
        SYNONYM_MAP.put("controlling power", "faction control");
        SYNONYM_MAP.put("dominant faction", "faction control");
        SYNONYM_MAP.put("security level", "system security");

        // ── Player profile / ranks ─────────────────────────────────────────────
        SYNONYM_MAP.put("commander stats", "player profile");
        SYNONYM_MAP.put("pilot rank", "player profile");
        SYNONYM_MAP.put("what rank are we", "player profile");
        SYNONYM_MAP.put("our ranking", "player profile");
        SYNONYM_MAP.put("commander profile", "player profile");

        // ── Pirate / massacre missions ─────────────────────────────────────────
        SYNONYM_MAP.put("kills remaining", "kill count");
        SYNONYM_MAP.put("how many pirates left", "kill count");
        SYNONYM_MAP.put("massacre progress", "massacre mission progress");
        SYNONYM_MAP.put("pirates remaining", "massacre mission progress");
        SYNONYM_MAP.put("pirate kills remaining", "massacre mission progress");

        // ── Distance queries ───────────────────────────────────────────────────
        SYNONYM_MAP.put("range to carrier", "distance to carrier");
        SYNONYM_MAP.put("how far is the carrier", "distance to carrier");
        SYNONYM_MAP.put("range to bio sample", "distance to last bio sample");
        SYNONYM_MAP.put("how far to previous organism", "distance to last bio sample");
        SYNONYM_MAP.put("distance from inhabited space", "distance to bubble");
        SYNONYM_MAP.put("how far from civilization", "distance to bubble");
        SYNONYM_MAP.put("range from human space", "distance to bubble");
        SYNONYM_MAP.put("range to planet", "distance to stellar object");
        SYNONYM_MAP.put("how far to moon", "distance to stellar object");
        SYNONYM_MAP.put("how far to station", "distance to stellar object");

        // ── Missions ───────────────────────────────────────────────────────────
        SYNONYM_MAP.put("mission status", "active missions");
        SYNONYM_MAP.put("what are our missions", "current missions");
        SYNONYM_MAP.put("ongoing missions", "active missions");

        // ── Bounties ───────────────────────────────────────────────────────────
        SYNONYM_MAP.put("bounty earnings", "total bounties");
        SYNONYM_MAP.put("credits from bounties", "total bounties");

        // ── Geological signals ─────────────────────────────────────────────────
        SYNONYM_MAP.put("geological activity", "geological signals");
        SYNONYM_MAP.put("volcanic activity", "volcanic signals");
        SYNONYM_MAP.put("geology in system", "geo signals");

        // ── Planet surface queries ─────────────────────────────────────────────
        SYNONYM_MAP.put("surface materials", "planet materials");
        SYNONYM_MAP.put("materials on the surface", "planet materials");
        SYNONYM_MAP.put("what materials are here", "planet materials");
        SYNONYM_MAP.put("planetary biome", "planet biome");
        SYNONYM_MAP.put("atmosphere analysis", "biome analysis");
        SYNONYM_MAP.put("what life is here", "planet biome");

        // ── Last scan ──────────────────────────────────────────────────────────
        SYNONYM_MAP.put("most recent scan", "last scan");
        SYNONYM_MAP.put("latest scan", "last scan");
        SYNONYM_MAP.put("what did i last scan", "last scanned object");

        // ── Time ───────────────────────────────────────────────────────────────
        SYNONYM_MAP.put("galactic time", "current time");
        SYNONYM_MAP.put("utc time", "current time");
        SYNONYM_MAP.put("earth time", "time on earth");

        // ── App capabilities ───────────────────────────────────────────────────
        SYNONYM_MAP.put("what commands do you know", "app capabilities");
        SYNONYM_MAP.put("what can you help with", "app capabilities");
        SYNONYM_MAP.put("your abilities", "app capabilities");

        // phonetics
        SYNONYM_MAP.put("of", "off");
        SYNONYM_MAP.put("manax", "max");
        SYNONYM_MAP.put("hard points", "hardpoints");
        SYNONYM_MAP.put("scott", "scan");
        SYNONYM_MAP.put("scale", "scan");
        SYNONYM_MAP.put("mining spots", "mining hot spots");
        SYNONYM_MAP.put("net vision", "night vision");
        SYNONYM_MAP.put("her style", "hostile");
        SYNONYM_MAP.put("hair style", "hostile");
        SYNONYM_MAP.put("did", "deploy");
        SYNONYM_MAP.put("did ploy", "deploy");
        SYNONYM_MAP.put("do they play", "deploy");
        SYNONYM_MAP.put("perimeter", "enter");
        SYNONYM_MAP.put("exit this window", "exit");
        SYNONYM_MAP.put("spectrum scan", "scan system");
        SYNONYM_MAP.put("full spectrum scan", "FSS");
        SYNONYM_MAP.put("full-spectrum scan", "FSS");
        SYNONYM_MAP.put("when wake up", "wake up");



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
