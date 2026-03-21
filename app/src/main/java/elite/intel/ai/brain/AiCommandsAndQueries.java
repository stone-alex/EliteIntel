package elite.intel.ai.brain;

import com.google.gson.JsonObject;
import elite.intel.session.Status;
import elite.intel.session.SystemSession;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        commandMap.put("find mining site for X, find mining site for material X, find system to mine X, find star system to mine X, where can we mine X, where to mine X, find place to mine X, find somewhere to mine X, mine X within Y ly, find X mining site, find X mining location" + KEY_X_WITH_DISTANCE, FIND_MINING_SITE.getAction());
        //commandMap.put("find where to mine tritium, find tritium mining site" + KEY_X_WITH_DISTANCE, FIND_FLEET_CARRIER_FUEL_MINING_SITE.getAction());
        commandMap.put("find where to buy X, where can we buy X, within Y ly" + KEY_X_WITH_DISTANCE, FIND_COMMODITY.getAction());
        commandMap.put("set as home system", SET_HOME_SYSTEM.getAction());
        commandMap.put("toggle radio on/off" + KEY_STATE, SET_RADIO_TRANSMISSION_MODE.getAction());
        commandMap.put("navigate to coordinates lat/lon" + KEY_LAT_LON, NAVIGATE_TO_TARGET.getAction());
        commandMap.put("discovery announcements on/off" + KEY_STATE, DISCOVERY_ON_OFF.getAction());
        commandMap.put("mining and material announcements on/off" + KEY_STATE, MINING_ON_OFF.getAction());
        commandMap.put("route announcements on/off" + KEY_STATE, ROUTE_ON_OFF.getAction());
        commandMap.put("increase speed by X, speed plus X, set speed plus X, add X to speed" + KEY_X, INCREASE_SPEED_BY.getAction());
        commandMap.put("set voice to X, change voice to X" + KEY_X, SET_AI_VOICE.getAction());
        commandMap.put("decrease speed by X, speed minus X, set speed minus X, reduce speed by X" + KEY_X, DECREASE_SPEED_BY.getAction());

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
        commandMap.put("drop from supercruise, disengage FSD, drop, drop here, drop now, exit supercruise", DROP_FROM_SUPER_CRUISE.getAction());
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
        commandMap.put("switch to combat mode, HUD combat mode, combat mode, change to combat mode, activate combat mode, enable combat mode, go to combat mode", ACTIVATE_COMBAT_MODE.getAction());
        commandMap.put("switch to analysis mode, HUD analysis mode, analysis mode, change to analysis mode, activate analysis mode, enable analysis mode, go to analysis mode", ACTIVATE_ANALYSIS_MODE.getAction());
        commandMap.put("max shields, shields up, boost shields, transfer power to shields, power to shields, all power to shields, shields maximum, shields full, reinforce shields, divert power to shields, full shields, power shields", INCREASE_SHIELDS_POWER.getAction());
        commandMap.put("max engines, full power to engines, boost engines, transfer power to engines, power to engines, all power to engines, engines maximum, engines full, max thrusters, full thrusters, divert power to engines, divert power to thrusters, power to thrusters, full engines, power engines", INCREASE_ENGINES_POWER.getAction());
        commandMap.put("max weapons, full power to weapons, boost weapons, transfer power to weapons, power to weapons, all power to weapons, weapons maximum, weapons full, divert power to weapons, charge weapons, full weapons, power weapons", INCREASE_WEAPONS_POWER.getAction());
        commandMap.put("max systems, power to systems, all power to systems, full power to systems, transfer power to systems, systems maximum, systems full, boost systems, divert power to systems, reinforce systems, full systems, power systems", INCREASE_SYSTEMS_POWER.getAction());
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
        commandMap.put("supercruise, lightspeed, light speed, engage FSD supercruise, engage frame shift drive", ENTER_SUPER_CRUISE.getAction());
        commandMap.put("retract hardpoints, store weapons, weapons cold", RETRACT_HARDPOINTS.getAction());
        commandMap.put("deploy hardpoints, weapons hot, combat ready", DEPLOY_HARDPOINTS.getAction());
        commandMap.put("landing gear down, deploy landing gear, gear down", DEPLOY_LANDING_GEAR.getAction());
        commandMap.put("landing gear up, retract landing gear, gear up", RETRACT_LANDING_GEAR.getAction());
        commandMap.put("request docking, ask for landing permission, contact tower, contact the tower, get parking spot, get a parking spot, get me a parking spot, request a landing spot", REQUEST_DOCKING.getAction());
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

        return commandMap;
    }

    private Map<String, String> buildQueryMap() {
        Map<String, String> queryMap = new LinkedHashMap<>();
        if (!systemSession.useLocalQueryLlm()) {
            queryMap.put("help with <topic>, how do I <topic>, explain <topic>, can you help me with <topic>, how can you help me with <topic> → key=topic" + KEY_X, HELP.getAction());
        }

        // --- EXOBIOLOGY (three distinct scopes) ---
        // Scope 1: which planets in this system have bio signals / scan progress
        queryMap.put("which planets have bio signals, bio scan progress in this system, any organisms in this system, which planets need bio scanning, unscanned bio signals, organic scan status, what planets contain life forms", BIO_SAMPLE_IN_STAR_SYSTEM.getAction());
        // Scope 2: organisms at our CURRENT PLANET (where we are standing right now)
        queryMap.put("what organisms are here on this planet, exobiology at current location, what life forms are here, biology samples here, bio samples at our location, what life is on this planet", EXOBIOLOGY_SAMPLES.getAction());
        // Scope 3: predicted organisms based on planet/system conditions (before landing)
        queryMap.put("what organics are possible here, biome analysis, what organisms might exist, predict exobiology, possible bio genus, what biology could be here, what genus might be here, star system biome" + KEY_X, PLANET_BIOME_ANALYSIS.getAction());

        // --- CURRENT SYSTEM — science data ---
        // Bodies: what physically exists (planets, moons, rings, landable surfaces) — SCIENCE data
        queryMap.put("what planets and moons are in this system, any landable bodies, what bodies are in this system, how many planets here, any rings, any gas giants, any water worlds, landable surfaces in system, stellar objects analysis, scan data for bodies in system", QUERY_STELLAR_OBJETS.getAction());
        // Signals: activity signals (combat zones, hotspots, nav beacon) — ACTIVITY data, NOT mining site search
        queryMap.put("what signals are detected here, any combat zones in this system, conflict zones here, resource extraction sites in this system, mining hotspots in this system, any emission signals, nav beacon status, any fleet signals, signal sources in system, signal breakdown", QUERY_STELLAR_SIGNALS.getAction());
        // Geo: geological signals on planet surfaces
        queryMap.put("geological signals in this system, which planets have geo signals, any geological activity here, volcanic signals, any geology here", QUERY_GEO_SIGNALS.getAction());
        // Last scan results
        queryMap.put("what did we just scan, last scan results, analyze last scan, what was the last body we scanned, results of last scan", LAST_SCAN_ANALYSIS.getAction());

        // --- CURRENT SYSTEM — context ---
        queryMap.put("who controls this system, system factions, system security level, which faction runs this system, power play status, system allegiance, system politics, system government", SYSTEM_SECURITY_ANALYSIS.getAction());
        queryMap.put("where are we, what system are we in, current system, our position, navigation status, current location, what star system is this", CURRENT_LOCATION.getAction());

        // --- CURRENT SYSTEM — stations (consolidated: orbital + planetary + settlements) ---
        queryMap.put("what stations are in this system, nearby stations, docking options, any coriolis here, where can we dock, any shipyards here, any outfitting nearby, local stations, ports in this system, any orbital stations, settlements in this system, any planetary bases, outposts in system, local infrastructure, stations and ports here", ANALYZE_LOCAL_STATIONS.getAction());
        queryMap.put("fleet carriers in this system, any fleet carriers here, player carriers in system, any carriers here", QUERY_CARRIERS.getAction());

        // --- CURRENT STATION — what THIS docked station offers ---
        queryMap.put("what modules are for sale here, outfitting at this station, what can we buy for the ship, module shop, any good modules here, ship modules available, what can we fit here", LOCAL_OUTFITTING.getAction());
        queryMap.put("any ships for sale here, local shipyard, what ships can we buy, ships available at this station, ships to buy here", LOCAL_SHIPYARD.getAction());
        queryMap.put("what services does this station have, station facilities, can we repair here, can we rearm here, can we refuel here, station services, what does this station offer", STATION_DETAILS.getAction());

        // --- SHIP ---
        queryMap.put("what modules do we have, ship loadout, our equipment, ship build, what weapons do we have, ship classification, our jump range, module check, damage report, module integrity, what is fitted, are we armed, do we have a fuel scoop, ship status, hardpoints", SHIP_LOADOUT.getAction());
        queryMap.put("how much fuel do we have, ship fuel level, fuel remaining, fuel status, are we low on fuel, fuel reserves, how full is the fuel tank", SHIP_FUEL_STATUS.getAction());
        // Cargo = trade commodities in hold (NOT engineering materials)
        queryMap.put("what commodities are we carrying, cargo hold contents, what are we hauling, cargo manifest, what is in the hold, how full is the hold, cargo contents, what trade goods do we have", CARGO_HOLD_CONTENTS.getAction());
        // Engineering materials = raw/encoded/manufactured (NOT cargo, NOT planet surface)
        queryMap.put("what engineering materials do we have, material inventory, how much X material do we have, do we have any X, raw materials inventory, encoded materials, manufactured materials" + KEY_X, MATERIALS_INVENTORY.getAction());
        // Planet surface materials = what can be physically collected right now
        queryMap.put("what materials can we collect here, surface materials on this planet, what can we gather here, materials at our location, what is on this planet surface", PLANET_MATERIALS.getAction());

        // --- NAVIGATION ---
        queryMap.put("how many jumps remaining, our plotted route, navigation route, route progress, where are we headed, how long is our route", PLOTTED_ROUTE_ANALYSIS.getAction());
        // Destination = system currently selected/targeted in nav panel (before jumping)
        queryMap.put("analyze our destination, info on selected system, tell me about where we are jumping, destination system analysis, is there fuel at destination, what is the target system, selected jump destination", FSD_TARGET_ANALYSIS.getAction());
        queryMap.put("how far to X, distance to planet X, distance to moon X, distance to stellar object X" + KEY_X, DISTANCE_TO_BODY.getAction());
        queryMap.put("how far from the bubble, distance to inhabited space, how far from civilization, how far are we from the core systems", DISTANCE_TO_BUBBLE.getAction());
        queryMap.put("how far to last bio sample, distance to previous organic, distance to last organism, how far back to that bio site", DISTANCE_TO_LAST_BIO_SAMPLE.getAction());
        queryMap.put("how far is the carrier, distance to our carrier, carrier location, how far to home base", DISTANCE_TO_CARRIER.getAction());

        // --- FLEET CARRIER ---
        queryMap.put("how much tritium does the carrier have, carrier fuel supply, carrier tritium levels, carrier jump range, carrier fuel status", CARRIER_TRITIUM_SUPPLY.getAction());
        queryMap.put("carrier finances, carrier upkeep, how much does the carrier cost to run, carrier budget, how long can the carrier operate, days of operation funded, carrier running costs", CARRIER_STATUS.getAction());
        queryMap.put("where is the carrier route going, carrier jump plan, carrier navigation route, carrier plotted route", CARRIER_ROUTE_ANALYSIS.getAction());
        queryMap.put("where is the carrier going, carrier next stop, carrier destination, carrier heading", CARRIER_DESTINATION.getAction());
        queryMap.put("when does the carrier arrive, carrier ETA, how long until carrier gets here, carrier arrival time", CARRIER_ETA.getAction());

        // --- TRADE ---
        queryMap.put("our trade settings, trade profile, trade configuration, trade parameters, what are our trade preferences", TRADE_PROFILE_ANALYSIS.getAction());
        queryMap.put("our trade route, trade schedule, trade route stops, trade run details, our trading plan, where are we trading, trade itinerary", TRADE_ROUTE_ANALYSIS.getAction());

        // --- EARNINGS (science only — not trade) ---
        queryMap.put("how much have we earned from exploration, exploration earnings, science profits, exobiology income, total scan value, exploration income, what have we made from scanning", EXPLORATION_PROFITS.getAction());
        queryMap.put("how much in bounties, total bounty earnings, bounty income, our bounty total, bounties collected", TOTAL_BOUNTIES.getAction());

        // --- MISSIONS / PROGRESS ---
        queryMap.put("how many kills do we have, massacre mission progress, kill count, bounty mission kills, combat mission status, pirate mission kills", PIRATE_MISSION_PROGRESS.getAction());
        queryMap.put("our ranks, player stats, commander profile, what rank are we, progress overview, user statistics", PLAYER_PROFILE_ANALYSIS.getAction());
        queryMap.put("what missions do we have, active missions, mission list, current objectives, mission status, what are we doing", ANALYZE_MISSIONS.getAction());

        // --- UTILITY ---
        queryMap.put("any reminders, my notes, what was my reminder, reminder status", REMINDER.getAction());
        queryMap.put("what time is it, earth time, current time, real world time", TIME_IN_ZONE.getAction());
        queryMap.put("what is your name, who are you, your designation, identify yourself, what are you called", AI_DESIGNATION.getAction());
        queryMap.put("what can you do, your capabilities, what commands do you know, what functions do you have", APP_CAPABILITIES.getAction());
        queryMap.put("what keys are not set, unbound key bindings, missing key bindings, key binding check", KEY_BINDINGS_ANALYSIS.getAction());
        if (!systemSession.useLocalQueryLlm()) {
            queryMap.put("(fallback) general conversation, chitchat, anything not matched above", GENERAL_CONVERSATION.getAction());
        } else {
            queryMap.put("(fallback)", COMMAND_NOT_FOUND.getAction());
        }

        return queryMap;
    }

    // Matches "set speed plus/minus N", "speed plus/minus N", "increase/decrease speed by N"
    private static final Pattern SPEED_UP_PATTERN = Pattern.compile("(?:set speed (?:plus|\\+)|speed (?:plus|\\+)|increase speed by)\\s+(\\d+)");
    private static final Pattern SPEED_DOWN_PATTERN = Pattern.compile("(?:set speed (?:minus|\\-)|speed (?:minus|\\-)|decrease speed by|reduce speed by)\\s+(\\d+)");

    /**
     * Handles parameterized commands that can be resolved without the LLM.
     * Returns a fully-formed JSON response (action + params), or null if no match.
     */
    public JsonObject matchCommandJson(String input) {
        String lower = input.toLowerCase();
        Matcher m = SPEED_UP_PATTERN.matcher(lower);
        if (m.find()) return speedJson(m.group(1), false);
        m = SPEED_DOWN_PATTERN.matcher(lower);
        if (m.find()) return speedJson(m.group(1), true);
        return null;
    }

    private JsonObject speedJson(String digits, boolean negate) {
        int val = Integer.parseInt(digits);
        if (negate) val = -val;
        JsonObject params = new JsonObject();
        params.addProperty("key", String.valueOf(val));
        JsonObject j = new JsonObject();
        j.addProperty("action", INCREASE_SPEED_BY.getAction());
        j.add("params", params);
        return j;
    }

    private static final Set<String> NOISE_PREFIXES = Set.of(
            "and", "uh", "um", "please", "okay", "ok", "hey", "so", "now", "just", "right", "well", "go ahead and"
    );

    // Filler words stripped from anywhere in the input before matching
    private static final java.util.regex.Pattern FILLER_WORDS =
            java.util.regex.Pattern.compile("\\b(the|that|this|these|those|a|an|my|our|please)\\b");

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
        String deflled = stripFillers(denoised);
        String depluraled = deplural(deflled);

        // Pass 1: endsWith — precise, handles STT leading noise
        for (Map.Entry<String, String> entry : buildCommandMap().entrySet()) {
            for (String trigger : entry.getKey().split(",")) {
                String clean = normalize(trigger.replaceAll("\\{[^}]*}", "").replaceAll("<[^>]*>", ""));
                if (clean.isEmpty()) continue;
                if (normalized.endsWith(clean) || denoised.endsWith(clean) || deflled.endsWith(clean) || depluraled.endsWith(clean)) {
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

    private static String stripFillers(String s) {
        return FILLER_WORDS.matcher(s).replaceAll(" ").trim().replaceAll("\\s+", " ");
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
                sb.append("  ").append(action).append(" ← ").append(concept).append(" \n"));
        return sb.toString();
    }

    public String getQueries() {
        StringBuilder sb = new StringBuilder();
        sb.append("ALLOWED QUERIES (use ONLY these exact action names): \n\n");
        buildQueryMap().forEach((concept, action) ->
                sb.append("  ").append(action).append(" ← ").append(concept).append(" \n"));
        return sb.toString();
    }
}