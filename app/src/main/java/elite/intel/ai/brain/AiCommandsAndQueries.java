package elite.intel.ai.brain;

import elite.intel.session.Status;
import elite.intel.session.SystemSession;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static elite.intel.ai.brain.commons.AiEndPoint.CONNECTION_CHECK_COMMAND;
import static elite.intel.ai.brain.handlers.commands.Commands.*;
import static elite.intel.ai.brain.handlers.query.Queries.*;

public class AiCommandsAndQueries {

    private final SystemSession systemSession = SystemSession.getInstance();

    private final static String KEY_X = " {key:X}";
    private final static String KEY_X_WITH_DISTANCE = " {key:X, max_distance:Y}";
    private final static String KEY_STATE = " {state:true/false}";
    private final static String KEY_LAT_LON = " {lat:X, lon:Y}";

    private static final AiCommandsAndQueries INSTANCE = new AiCommandsAndQueries();
    private final Status status = Status.getInstance();

    private AiCommandsAndQueries() {
    }

    public static AiCommandsAndQueries getInstance() {
        return INSTANCE;
    }

    private Map<String, String> buildCommandMap() {
        Map<String, String> commandMap = new LinkedHashMap<>();

        commandMap.put("start listening, listen to me, wake word ON, stop ignoring me", START_LISTENING.getAction());

        commandMap.put("stop listening, ignore me, wake word OFF", STOP_LISTENING.getAction());

        /// ship
        commandMap.put("add <material> to mining targets" + KEY_X, ADD_MINING_TARGET.getAction());
        commandMap.put("find raw material trader" + KEY_X, FIND_RAW_MATERIAL_TRADER.getAction());
        commandMap.put("find encoded material trader" + KEY_X, FIND_ENCODED_MATERIAL_TRADER.getAction());
        commandMap.put("find manufactured material trader" + KEY_X, FIND_MANUFACTURED_MATERIAL_TRADER.getAction());
        commandMap.put("find human tech broker" + KEY_X, FIND_HUMAN_TECHNOLOGY_BROKER.getAction());
        commandMap.put("find guardian tech broker" + KEY_X, FIND_GUARDIAN_TECHNOLOGY_BROKER.getAction());
        commandMap.put("find brain trees" + KEY_X_WITH_DISTANCE, FIND_BRAIN_TREES.getAction());
        commandMap.put("find mining site for material X" + KEY_X_WITH_DISTANCE, FIND_MINING_SITE.getAction());
        commandMap.put("find where to mine tritium, find tritium mining site" + KEY_X_WITH_DISTANCE, FIND_FLEET_CARRIER_FUEL_MINING_SITE.getAction());
        commandMap.put("find where to buy X, where can we buy X, within Y ly" + KEY_X_WITH_DISTANCE, FIND_COMMODITY.getAction());
        commandMap.put("set as home system", SET_HOME_SYSTEM.getAction());
        commandMap.put("toggle radio on/off" + KEY_STATE, SET_RADIO_TRANSMISSION_MODE.getAction());
        commandMap.put("navigate to coordinates lat/lon" + KEY_LAT_LON, NAVIGATE_TO_TARGET.getAction());
        commandMap.put("discovery announcements on/off" + KEY_STATE, DISCOVERY_ON_OFF.getAction());
        commandMap.put("mining and material announcements on/off" + KEY_STATE, MINING_ON_OFF.getAction());
        commandMap.put("route announcements on/off" + KEY_STATE, ROUTE_ON_OFF.getAction());
        commandMap.put("increase speed by X" + KEY_X, INCREASE_SPEED_BY.getAction());
        commandMap.put("set voice to X, change voice to X" + KEY_X, SET_AI_VOICE.getAction());
        commandMap.put("decrease speed by X" + KEY_X, DECREASE_SPEED_BY.getAction());

        /// Missions
        commandMap.put("plot reconnaissance route to hunting ground, recon hunting ground", RECON_TARGET_SYSTEM.getAction());
        commandMap.put("navigate to system with matching mission provider, go to mission provider system", RECON_PROVIDER_SYSTEM.getAction());
        commandMap.put("navigate to confirmed pirate massacre mission provider, go to pirate mission giver", NAVIGATE_TO_PIRATE_MISSION_PROVIDER.getAction());

        commandMap.put("find hunting grounds" + KEY_X, FIND_HUNTING_GROUNDS.getAction());
        commandMap.put("navigate to active mission target" + KEY_X, NAVIGATE_TO_NEXT_MISSION.getAction());
        commandMap.put("ignore hunting ground", IGNORE_HUNTING_GROUND.getAction());
        commandMap.put("confirm hunting ground", CONFIRM_HUNTING_GROUND.getAction());
        commandMap.put("interrupt, silence, cancel", INTERRUPT_TTS.getAction());

        commandMap.put("clear mining targets", CLEAR_MINING_TARGETS.getAction());
        commandMap.put("monetize route", MONETIZE_ROUTE.getAction());
        commandMap.put("locate nearest vista genomics", FIND_VISTA_GENOMICS.getAction());
        commandMap.put("locate nearest fleet carrier", FIND_NEAREST_FLEET_CARRIER.getAction());
        commandMap.put("clear or cancel fleet carrier route", CLEAR_FLEET_CARRIER_ROUTE.getAction());
        commandMap.put("jump to hyperspace, jump to the next way point, let's get out of here, lets go, enter hyperspace, go to next waypoint, engage FSD jump", JUMP_TO_HYPERSPACE.getAction());
        commandMap.put("drop from supercruise, disengage FSD, drop", DROP_FROM_SUPER_CRUISE.getAction());
        commandMap.put("navigate to carrier, return to base, go to fleet carrier", NAVIGATE_TO_CARRIER.getAction());
        //commandMap.put("navigate to home system", TAKE_ME_HOME.getAction());
        commandMap.put("set optimal speed", SET_OPTIMAL_SPEED.getAction());
        commandMap.put("scan the system, open FSS, perform full system scan, honk", OPEN_FSS_AND_SCAN.getAction());
        commandMap.put("navigate to landing zone, get heading to LZ", GET_HEADING_TO_LZ.getAction());
        commandMap.put("disable all announcements, be silent, be quiet", DISABLE_ALL_ANNOUNCEMENTS.getAction());
        commandMap.put("calculate carrier route, plot fleet carrier route", CALCULATE_FLEET_CARRIER_ROUTE.getAction());
        commandMap.put("enter carrier destination", ENTER_FLEET_CARRIER_DESTINATION.getAction());
        commandMap.put("calculate trade route, get us a trade route, find trade route", CALCULATE_TRADE_ROUTE.getAction());
        commandMap.put("navigate to next trade stop, go to trade station", NAVIGATE_TO_NEXT_TRADE_STOP.getAction());
        commandMap.put("describe trade profile, list trade parameters", LIST_TRADE_ROUTE_PARAMETERS.getAction());
        commandMap.put("cancel trade route, clear trade route", CLEAR_TRADE_ROUTE.getAction());

        commandMap.put("deploy heat sink", DEPLOY_HEAT_SINK.getAction());
        commandMap.put("stop, engine stop, full stop, speed zero", STOP.getAction());
        commandMap.put("taxi, auto pilot, auto docking, take us in", TAXI.getAction());
        commandMap.put("quarter throttle, quarter speed, speed 25", SET_SPEED25.getAction());
        commandMap.put("half throttle, half speed, speed 50", SET_SPEED50.getAction());
        commandMap.put("throttle 75, three quarters throttle, speed 75", SET_SPEED75.getAction());
        commandMap.put("max speed, full speed, full throttle", SET_SPEED100.getAction());
        commandMap.put("set fuel reserve, set fuel reserves, set carrier fuel reserve, set carrier fuel reserves" + KEY_X, SET_CARRIER_FUEL_RESERVE.getAction());
        commandMap.put("select next system in route, target next route system", TARGET_NEXT_ROUTE_SYSTEM.getAction());
        commandMap.put("target wingman 1", TARGET_WINGMAN0.getAction());
        commandMap.put("target wingman 2", TARGET_WINGMAN1.getAction());
        commandMap.put("target wingman 3", TARGET_WINGMAN2.getAction());
        commandMap.put("wing nav lock, lock on wing", WING_NAV_LOCK.getAction());

        /// ship or SRV
        commandMap.put("night vision on/off" + KEY_STATE, NIGHT_VISION_ON_OFF.getAction());
        commandMap.put("switch to combat mode, HUD combat mode", ACTIVATE_COMBAT_MODE.getAction());
        commandMap.put("switch to analysis mode, HUD analysis mode", ACTIVATE_ANALYSIS_MODE.getAction());
        commandMap.put("max shields, shields up, boost shields", INCREASE_SHIELDS_POWER.getAction());
        commandMap.put("max engines, full power to engines, boost engines", INCREASE_ENGINES_POWER.getAction());
        commandMap.put("max weapons, full power to weapons, boost weapons", INCREASE_WEAPONS_POWER.getAction());
        commandMap.put("equalize power, reset power distribution", RESET_POWER.getAction());
        commandMap.put("cancel navigation", NAVIGATION_ON_OFF.getAction());
        commandMap.put("open cargo scoop, open cargo bay" + KEY_STATE, OPEN_CARGO_SCOOP.getAction());
        commandMap.put("close cargo scoop, close cargo bay" + KEY_STATE, CLOSE_CARGO_SCOOP.getAction());
        commandMap.put("navigate to next bio sample, go to next organic, next codex marker", NAVIGATE_TO_NEXT_BIO_SAMPLE.getAction());

        /// ship normal space flight
        commandMap.put("target subsystem, target power plant" + KEY_X, TARGET_SUB_SYSTEM.getAction());
        commandMap.put("dismiss ship, go to orbit", DISMISS_SHIP.getAction());
        commandMap.put("return to surface, pick me up, extraction requested", RETURN_TO_SURFACE.getAction());
        commandMap.put("order fighter defend ship", REQUEST_DEFENSIVE_BEHAVIOUR.getAction());
        commandMap.put("order fighter focus my target", REQUEST_FOCUS_TARGET.getAction());
        commandMap.put("order fighter hold fire", REQUEST_HOLD_FIRE.getAction());
        commandMap.put("order fighter return to mothership", REQUEST_REQUEST_DOCK.getAction());
        commandMap.put("supercruise, light speed, engage FSD supercruise", ENTER_SUPER_CRUISE.getAction());
        commandMap.put("retract hardpoints, store weapons, weapons cold", RETRACT_HARDPOINTS.getAction());
        commandMap.put("deploy hardpoints, weapons hot, combat ready", DEPLOY_HARDPOINTS.getAction());
        commandMap.put("landing gear down, deploy landing gear, gear down", DEPLOY_LANDING_GEAR.getAction());
        commandMap.put("landing gear up, retract landing gear, gear up", RETRACT_LANDING_GEAR.getAction());
        commandMap.put("request docking, ask for landing permission, contact tower, get parking spot", REQUEST_DOCKING.getAction());
        commandMap.put("select highest threat, target most dangerous", SELECT_HIGHEST_THREAT.getAction());
        commandMap.put("headlights on/off, toggle lights" + KEY_STATE, LIGHTS_ON_OFF.getAction());
        commandMap.put("drive assist on/off" + KEY_STATE, DRIVE_ASSIST.getAction());


        if (status.isInMainShip()) {
            commandMap.put("deploy SRV, deploy buggy, deploy car, deploy surface vehicle", DEPLOY_SRV.getAction());
        }
        if (status.isInSrv()) {
            commandMap.put("recover SRV, recover buggy, recover car,get car back aboard ship, requesting extraction, extract surface vehicle", RECOVER_SRV.getAction());
        }

        /// UI panels
        commandMap.put("activate, punch it, engage", ACTIVATE.getAction());
        commandMap.put("show transactions, show transactions panel, display transactions, display transactions panel", SHOW_TRANSACTIONS.getAction());
        commandMap.put("show contacts, show contacts panel, display contacts, display contacts panel", SHOW_CONTACTS.getAction());
        commandMap.put("show navigation, show navigation panel, display navigation, display navigation panel", SHOW_NAVIGATION.getAction());
        commandMap.put("show comms, show chat, show chat panel, display comms, display chat", SHOW_CHAT_PANEL.getAction());
        commandMap.put("show inbox, show messages, display inbox, display messages", SHOW_INBOX_PANEL.getAction());
        commandMap.put("show social, show friends, display social, display friends", SHOW_SOCIAL_PANEL.getAction());
        commandMap.put("show history, show history panel, display history, display history panel", SHOW_HISTORY_PANEL.getAction());
        commandMap.put("show squadron, show squadron panel, display squadron, display squadron panel", SHOW_SQUADRON.getAction());
        commandMap.put("show central panel, show role panel, display central panel, display role panel, role panel", SHOW_COMMANDER_PANEL.getAction());
        commandMap.put("show fighter panel, display fighter panel", SHOW_FIGHTER_PANEL.getAction());
        commandMap.put("show crew, show crew panel, display crew, display crew panel", SHOW_CREW.getAction());
        commandMap.put("show internal panel, show home panel, display internal panel, display home panel, commander panel, home panel", SHOW_INTERNAL_PANEL.getAction());
        commandMap.put("show modules, show modules panel, display modules, display modules panel", SHOW_MODULES_PANEL.getAction());
        commandMap.put("show fire groups, display fire groups", SHOW_FIRE_GROUPS.getAction());
        commandMap.put("show inventory, show inventory panel, display inventory, display inventory panel, open inventory", SHOW_INVENTORY_PANEL.getAction());
        commandMap.put("show storage, show storage panel, display storage, display storage panel", SHOW_STORAGE_PANEL.getAction());
        commandMap.put("show status, show status panel, display status, display status panel", SHOW_STATUS_PANEL.getAction());
        commandMap.put("show ship panel, display ship panel", SHOW_SHIP_PANEL.getAction());

        commandMap.put("close, close panel, exit, esc, close map, close galaxy map, close system map, close local map, close star map", EXIT_CLOSE.getAction());

        commandMap.put("show carrier management, show carrier management panel, display carrier management, display carrier management panel", DISPLAY_CARRIER_MANAGEMENT.getAction());
        commandMap.put("change trade profile starting budget to" + KEY_X, CHANGE_TRADE_PROFILE_SET_STARTING_BUDGET.getAction());
        commandMap.put("change trade profile max stops to" + KEY_X, CHANGE_TRADE_PROFILE_SET_MAX_NUMBER_OF_STOPS.getAction());
        commandMap.put("change trade profile max distance from entry to" + KEY_X, CHANGE_TRADE_PROFILE_SET_MAX_DISTANCE_FROM_ENTRY.getAction());
        commandMap.put("change trade profile allow prohibited cargo on/off" + KEY_STATE, CHANGE_TRADE_PROFILE_SET_ALLOW_PROHIBITED_CARGO.getAction());
        commandMap.put("change trade profile allow planetary port on/off" + KEY_STATE, CHANGE_TRADE_PROFILE_SET_ALLOW_PLANETARY_PORT.getAction());
        commandMap.put("change trade profile allow permit systems on/off" + KEY_STATE, CHANGE_TRADE_PROFILE_SET_ALLOW_PERMIT_SYSTEMS.getAction());
        commandMap.put("change trade profile allow strongholds on/off" + KEY_STATE, CHANGE_TRADE_PROFILE_SET_ALLOW_STRONGHOLDS.getAction());
        commandMap.put("galaxy map, open galaxy map, show galaxy map", OPEN_GALAXY_MAP.getAction());
        commandMap.put("local map, open system map, show local map", OPEN_SYSTEM_MAP.getAction());
        commandMap.put("list voices, list available voices", LIST_AVAILABLE_VOICES.getAction());
        commandMap.put("delete this codex entry", DELETE_CODEX_ENTRY.getAction());
        commandMap.put("clear all codex entries", CLEAR_CODEX_ENTRIES.getAction());
        commandMap.put("clear cache", CLEAR_CACHE.getAction());
        commandMap.put("clear reminders", CLEAR_REMINDERS.getAction());
        commandMap.put("set reminder" + KEY_X, SET_REMINDER.getAction());
        if (!systemSession.useLocalQueryLlm()) {
            commandMap.put("change personality to" + KEY_X, SET_PERSONALITY.getAction());
            commandMap.put("change profile to" + KEY_X, SET_PROFILE.getAction());
        }

        /// very special case. machie-only command
        commandMap.put(CONNECTION_CHECK_COMMAND, CONNECTION_CHECK.getAction());
        return commandMap;
    }

    private Map<String, String> buildQueryMap() {
        Map<String, String> queryMap = new LinkedHashMap<>();
        if (!systemSession.useLocalQueryLlm()) {
            queryMap.put("help with <topic>, how do I <topic>, explain <topic>, can you help me with <topic>, how can you help me with <topic> → key=topic" + KEY_X, HELP.getAction());
        }

        queryMap.put("key binding check, what keys are not set, unbound key bindings, are any key bindings missing", KEY_BINDINGS_ANALYSIS.getAction());
        queryMap.put("which stellar objects need bio scan, organic scan status in system, which planets need scanning, any unscanned bio signals, bio scan progress", BIO_SAMPLE_IN_STAR_SYSTEM.getAction());
        queryMap.put("organics on this planet, biology samples here, exobiology this location, what organisms are here, any life on this planet, bio samples at current location", EXOBIOLOGY_SAMPLES.getAction());
        queryMap.put("stellar objects analysis, landable planets and moons, what is in this system, how many planets are here, any landable bodies, scan data for this system, what bodies are in this system", QUERY_STELLAR_OBJETS.getAction());
        queryMap.put("analyze signals, detected signals, any conflict zones, conflict zones in system, combat zones here, resource extraction sites, mining hot spots, resource sites, any outposts nearby, points of interest in system, what signals are detected, any resource sites here, nav beacon, any combat zones, signal breakdown", QUERY_STELLAR_SIGNALS.getAction());
        queryMap.put("geo signals, which planets have geological signals, any geology here, geological activity, volcanic signals", QUERY_GEO_SIGNALS.getAction());
        queryMap.put("what stations or ports are in this system, nearby stations, docking options, any coriolis here, where can we dock", QUERY_STATIONS.getAction());
        queryMap.put("fleet carriers in this system, any carriers here, player carriers nearby", QUERY_CARRIERS.getAction());
        queryMap.put("system security, traffic, factions, controlling powers, who controls this system, system politics, what faction runs this system, power play status, system allegiance", SYSTEM_SECURITY_ANALYSIS.getAction());
        queryMap.put("trade profile settings, our trade configuration, what are our trade settings, trade parameters", TRADE_PROFILE_ANALYSIS.getAction());
        queryMap.put("distance to stellar object" + KEY_X, DISTANCE_TO_BODY.getAction());
        queryMap.put("analyze last scan, what did we just scan, last scanned body, results of last scan, what was that we scanned", LAST_SCAN_ANALYSIS.getAction());
        queryMap.put("inventory for material X, how much X do we have, how many X do we have, do we have any X" + KEY_X, MATERIALS_INVENTORY.getAction());
        queryMap.put("materials on this planet, what can we collect here, surface materials here, what materials are available, what can we gather here", PLANET_MATERIALS.getAction());
        queryMap.put("exploration profits, exobiology earnings, how much have we earned, exploration income, total scan value, what have we made from exploration", EXPLORATION_PROFITS.getAction());
        queryMap.put("current location, where are we, system info, what system is this, our position, what star system are we in, navigation status", CURRENT_LOCATION.getAction());
        queryMap.put("ship fuel level, how much fuel do we have, fuel status, are we low on fuel, fuel remaining, fuel reserves", SHIP_FUEL_STATUS.getAction());
        queryMap.put("analyze selected destination, info on selected system, what is our target system, tell me about the destination", FSD_TARGET_ANALYSIS.getAction());
        queryMap.put("ship plotted route, our navigation route, how long is our route, route progress, where are we headed, how many jumps remaining", PLOTTED_ROUTE_ANALYSIS.getAction());
        queryMap.put("fleet carrier plotted route, carrier navigation plan, where is the carrier route going, carrier jump plan", CARRIER_ROUTE_ANALYSIS.getAction());
        queryMap.put("trade schedule, trade route stops, our trading plan, trade run details, trade itinerary", TRADE_ROUTE_ANALYSIS.getAction());
        queryMap.put("outfitting available at station, ship modules for sale, what can we buy here, module shop, any good outfitting here, what modules are available", LOCAL_OUTFITTING.getAction());
        queryMap.put("ships for sale at station, local shipyard, what ships are available, any ships to buy here, shipyard stock", LOCAL_SHIPYARD.getAction());
        queryMap.put("cargo hold contents, what commodities do we have, what are we carrying, how full is the hold, cargo manifest, what is in the hold", CARGO_HOLD_CONTENTS.getAction());
        queryMap.put("carrier destination, where is the carrier going, carrier next stop, carrier heading", CARRIER_DESTINATION.getAction());
        queryMap.put("carrier fuel supply, carrier tritium, carrier jump range, how much tritium does the carrier have, carrier fuel status", CARRIER_TRITIUM_SUPPLY.getAction());
        queryMap.put("carrier finances, how many days of operation funded, carrier upkeep, carrier running costs, carrier budget, how long can the carrier operate", CARRIER_STATUS.getAction());
        queryMap.put("carrier ETA, when does the carrier arrive, how long until carrier arrives, carrier arrival time", CARRIER_ETA.getAction());
        queryMap.put("distance to fleet carrier, distance to home base, how far is the carrier, where is my carrier, carrier location, how far to the carrier", DISTANCE_TO_CARRIER.getAction());
        queryMap.put("pirate mission info, massacre mission kills, how many kills do we have, bounty mission status, kill count, mission kill tally", PIRATE_MISSION_PROGRESS.getAction());
        queryMap.put("user statistics, user ranks and progress", PLAYER_PROFILE_ANALYSIS.getAction());
        queryMap.put("ship loadout, your equipment, your jump range, classification, damage report, module integrity check, what modules do we have, ship status, ship build, what is equipped, module check, are we armed, do we have a fuel scoop", SHIP_LOADOUT.getAction());
        queryMap.put("station services, what services are available here, what does this station offer, station facilities, can we repair here, can we rearm here", STATION_DETAILS.getAction());
        queryMap.put("what can you do, your capabilities, app features, what functions do you have, what commands do you know", APP_CAPABILITIES.getAction());
        queryMap.put("your name, your designation, who are you, what are you called, identify yourself", AI_DESIGNATION.getAction());
        queryMap.put("total bounties collected, bounty earnings, how much in bounties, our bounty total, bounty income", TOTAL_BOUNTIES.getAction());
        queryMap.put("distance to the bubble, how far from civilisation, how far from inhabited space, how far are we from the bubble", DISTANCE_TO_BUBBLE.getAction());
        queryMap.put("distance to last bio sample, how far to next organic, how far back to that organism, distance to previous sample", DISTANCE_TO_LAST_BIO_SAMPLE.getAction());
        queryMap.put("what time is it, earth time, current time, real world time, what is the time", TIME_IN_ZONE.getAction());
        queryMap.put("star system biome analysis, what organics are possible here, any biology in this star system" + KEY_X, PLANET_BIOME_ANALYSIS.getAction());
        queryMap.put("what was my reminder, any reminders, my notes, reminder status", REMINDER.getAction());
        queryMap.put("stations ports and settlements in system, local infrastructure, any settlements here, what ports are here, planetary bases nearby, any outposts in system", ANALYZE_LOCAL_STATIONS.getAction());
        queryMap.put("active missions, mission list, what missions do we have, current objectives, mission status, what are we doing", ANALYZE_MISSIONS.getAction());
        if (!systemSession.useLocalQueryLlm()) {
            queryMap.put("(fallback) general conversation, chitchat, anything not matched above", GENERAL_CONVERSATION.getAction());
        } else {
            queryMap.put("(fallback)", COMMAND_NOT_FOUND.getAction());
        }

        return queryMap;
    }

    private static final Set<String> NOISE_PREFIXES = Set.of(
            "and", "uh", "um", "please", "okay", "ok", "hey", "so", "now", "just", "right", "well", "go ahead and"
    );

    /**
     * Attempts to match input directly against command trigger phrases without LLM involvement.
     * Only matches parameter-free triggers (no {key:X} etc.) - parameterised commands still go to the LLM.
     * Handles leading STT noise ("and display modules"), plural/singular variance ("display module"),
     * and trailing filler ("show modules panel please").
     * Returns the action name if matched, null otherwise.
     */
    public String matchCommand(String input) {
        String normalized = normalize(input);
        String denoised = stripLeadingNoise(normalized);
        String depluraled = deplural(denoised);

        for (Map.Entry<String, String> entry : buildCommandMap().entrySet()) {
            for (String trigger : entry.getKey().split(",")) {
                String clean = normalize(trigger.replaceAll("\\{[^}]*}", "").replaceAll("<[^>]*>", ""));
                if (clean.isEmpty()) continue;
                if (normalized.endsWith(clean) || denoised.endsWith(clean) || depluraled.endsWith(clean)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private static String stripLeadingNoise(String s) {
        String result = s;
        boolean stripped = true;
        while (stripped) {
            stripped = false;
            for (String noise : NOISE_PREFIXES) {
                if (result.startsWith(noise + " ")) {
                    result = result.substring(noise.length()).trim();
                    stripped = true;
                }
            }
        }
        return result;
    }

    /**
     * Strips a trailing 's' from the last word to normalise simple plural/singular variance.
     */
    private static String deplural(String s) {
        if (s.endsWith("s") && s.length() > 2) return s.substring(0, s.length() - 1);
        return s;
    }

    private static String normalize(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9 ]", "").trim().replaceAll("\\s+", " ");
    }

    /**
     * Returns all unique words extracted from every command and query hint phrase.
     * Used by {@link elite.intel.util.SttTermCorrector} to fuzzy-correct STT output
     * against known colloquial terms. New entries added here are automatically included.
     */
    public Set<String> getVocabulary() {
        Set<String> words = new HashSet<>();
        collectWords(words, buildCommandMap().keySet());
        collectWords(words, buildQueryMap().keySet());
        return words;
    }

    private static void collectWords(Set<String> target, Set<String> phrases) {
        for (String phrase : phrases) {
            // Strip template markers {key:X}, {state:true/false}, <material>, etc.
            String cleaned = phrase.replaceAll("\\{[^}]*\\}", " ").replaceAll("<[^>]*>", " ");
            for (String word : cleaned.toLowerCase().split("[^a-z']+")) {
                if (word.length() >= 3) {
                    target.add(word);
                }
            }
        }
    }

    public String getCommandMap() {
        StringBuilder sb = new StringBuilder();
        sb.append("ALLOWED COMMANDS (use ONLY these exact action names): \n\n");
        buildCommandMap().forEach((concept, action) ->
                sb.append("  ").append(concept).append("  →  ").append(action).append(" \n"));
        return sb.toString();
    }

    public String getQueries() {
        StringBuilder sb = new StringBuilder();
        sb.append("ALLOWED QUERIES (use ONLY these exact action names): \n\n");
        buildQueryMap().forEach((concept, action) ->
                sb.append("  ").append(concept).append("  →  ").append(action).append(" \n"));
        return sb.toString();
    }
}