package elite.intel.ai.brain;

import elite.intel.session.SystemSession;

import java.util.HashMap;
import java.util.Map;

import static elite.intel.ai.brain.handlers.commands.Commands.*;
import static elite.intel.ai.brain.handlers.query.Queries.*;

public class AiCommandsAndQueries {

    private final SystemSession systemSession = SystemSession.getInstance();

    private final static String KEY_X = " {key:X}";
    private final static String KEY_X_WITH_DISTANCE = " {key:X, max_distance:Y}";
    private final static String KEY_STATE = " {state:true/false}";
    private final static String KEY_LAT_LON = " {lat:X, lon:Y}";

    private static final AiCommandsAndQueries INSTANCE = new AiCommandsAndQueries();

    private AiCommandsAndQueries() {
    }

    public static AiCommandsAndQueries getInstance() {
        return INSTANCE;
    }

    private Map<String, String> buildCommandMap() {
        Map<String, String> commandMap = new HashMap<>();

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

        commandMap.put("show comms panel, open comms", DISPLAY_COMMS_PANEL.getAction());
        commandMap.put("display contacts panel", DISPLAY_CONTACTS_PANEL.getAction());
        commandMap.put("display internal panel", DISPLAY_INTERNAL_PANEL.getAction());
        commandMap.put("show central panel", DISPLAY_CENTRAL_PANEL.getAction());
        commandMap.put("jump to hyperspace, let's get out of here, lets go, enter hyperspace, go to next waypoint, engage FSD jump", JUMP_TO_HYPERSPACE.getAction());
        commandMap.put("drop from supercruise, disengage FSD, exit supercruise", EXIT_SUPER_CRUISE.getAction());
        commandMap.put("navigate to carrier, return to base, go to fleet carrier", NAVIGATE_TO_CARRIER.getAction());
        //commandMap.put("navigate to home system", TAKE_ME_HOME.getAction());
        commandMap.put("set optimal speed", SET_OPTIMAL_SPEED.getAction());
        commandMap.put("scan the system, open FSS, perform full system scan, honk", OPEN_FSS_AND_SCAN.getAction());
        commandMap.put("navigate to landing zone, get heading to LZ", GET_HEADING_TO_LZ.getAction());
        commandMap.put("deploy SRV, deploy buggy, deploy surface vehicle", DEPLOY_SRV.getAction());
        commandMap.put("calculate carrier jump route, plot fleet carrier route", CALCULATE_FLEET_CARRIER_ROUTE.getAction());
        commandMap.put("enter carrier destination, set next carrier waypoint", ENTER_NEXT_FLEET_CARRIER_DESTINATION.getAction());
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
        commandMap.put("set fuel reserve" + KEY_X, SET_CARRIER_FUEL_RESERVE.getAction());
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
        commandMap.put("dismiss ship, go to orbit, go play", DISMISS_SHIP.getAction());
        commandMap.put("return to surface, pick me up, extraction requested", RETURN_TO_SURFACE.getAction());
        commandMap.put("order fighter defend ship", REQUEST_DEFENSIVE_BEHAVIOUR.getAction());
        commandMap.put("order fighter focus my target", REQUEST_FOCUS_TARGET.getAction());
        commandMap.put("order fighter hold fire", REQUEST_HOLD_FIRE.getAction());
        commandMap.put("order fighter return to mothership", REQUEST_REQUEST_DOCK.getAction());
        commandMap.put("enter supercruise, engage FSD supercruise", ENTER_SUPER_CRUISE.getAction());
        commandMap.put("retract hardpoints, store weapons, weapons cold", RETRACT_HARDPOINTS.getAction());
        commandMap.put("deploy hardpoints, weapons hot, combat ready", DEPLOY_HARDPOINTS.getAction());
        commandMap.put("landing gear down, deploy landing gear, gear down", DEPLOY_LANDING_GEAR.getAction());
        commandMap.put("landing gear up, retract landing gear, gear up", RETRACT_LANDING_GEAR.getAction());
        commandMap.put("request docking, ask for landing permission, contact tower, get parking spot", REQUEST_DOCKING.getAction());
        commandMap.put("select highest threat, target most dangerous", SELECT_HIGHEST_THREAT.getAction());
        commandMap.put("headlights on/off, toggle lights" + KEY_STATE, LIGHTS_ON_OFF.getAction());
        commandMap.put("drive assist on/off" + KEY_STATE, DRIVE_ASSIST.getAction());
        commandMap.put("recover SRV, recover buggy, requesting extraction, extract surface vehicle", RECOVER_SRV.getAction());

        /// any status
        commandMap.put("activate, punch it, engage", ACTIVATE.getAction());
        commandMap.put("system shutdown", SHUT_DOWN.getAction());
        commandMap.put("streaming mode on/off" + KEY_STATE, SET_STREAMING_MODE.getAction());
        commandMap.put("show carrier management panel", DISPLAY_CARRIER_MANAGEMENT.getAction());
        commandMap.put("change trade profile starting budget to" + KEY_X, CHANGE_TRADE_PROFILE_SET_STARTING_BUDGET.getAction());
        commandMap.put("change trade profile max stops to" + KEY_X, CHANGE_TRADE_PROFILE_SET_MAX_NUMBER_OF_STOPS.getAction());
        commandMap.put("change trade profile max distance from entry to" + KEY_X, CHANGE_TRADE_PROFILE_SET_MAX_DISTANCE_FROM_ENTRY.getAction());
        commandMap.put("change trade profile allow prohibited cargo on/off" + KEY_STATE, CHANGE_TRADE_PROFILE_SET_ALLOW_PROHIBITED_CARGO.getAction());
        commandMap.put("change trade profile allow planetary port on/off" + KEY_STATE, CHANGE_TRADE_PROFILE_SET_ALLOW_PLANETARY_PORT.getAction());
        commandMap.put("change trade profile allow permit systems on/off" + KEY_STATE, CHANGE_TRADE_PROFILE_SET_ALLOW_PERMIT_SYSTEMS.getAction());
        commandMap.put("change trade profile allow strongholds on/off" + KEY_STATE, CHANGE_TRADE_PROFILE_SET_ALLOW_STRONGHOLDS.getAction());
        commandMap.put("galaxy map, open galaxy map, show galaxy map", OPEN_GALAXY_MAP.getAction());
        commandMap.put("local map, open system map, show local map", OPEN_SYSTEM_MAP.getAction());
        commandMap.put("close map", CLOSE_ANY_MAP.getAction());
        commandMap.put("exit to HUD, close panel", EXIT_TO_HUD.getAction());
        commandMap.put("list voices", LIST_AVAILABLE_VOICES.getAction());
        commandMap.put("delete this codex entry", DELETE_CODEX_ENTRY.getAction());
        commandMap.put("clear all codex entries", CLEAR_CODEX_ENTRIES.getAction());
        commandMap.put("clear cache", CLEAR_CACHE.getAction());
        commandMap.put("clear reminders", CLEAR_REMINDERS.getAction());
        commandMap.put("set reminder" + KEY_X, SET_REMINDER.getAction());
        if (!systemSession.isRunningLocalLLM()) {
            commandMap.put("change personality to" + KEY_X, SET_PERSONALITY.getAction());
            commandMap.put("change profile to" + KEY_X, SET_PROFILE.getAction());
        }
        commandMap.put("verify LLM connection, connection check", CONNECTION_CHECK.getAction());
        return commandMap;
    }

    private Map<String, String> buildQueryMap() {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("help with <topic>, how do I <topic>, explain <topic>, can you help me with <topic>, how can you help me with <topic> → key=topic" + KEY_X, HELP.getAction());
        queryMap.put("check missing key bindings", KEY_BINDINGS_ANALYSIS.getAction());
        queryMap.put("which stellar objects need bio scan, organic scan status in system", BIO_SAMPLE_IN_STAR_SYSTEM.getAction());
        queryMap.put("organics on this planet, biology samples here, exobiology this location", EXOBIOLOGY_SAMPLES.getAction());
        queryMap.put("stellar objects analysis, landable planets and moons", QUERY_STELLAR_OBJETS.getAction());
        queryMap.put("signals detected, what signals are here", QUERY_STELLAR_SIGNALS.getAction());
        queryMap.put("geo signals, which planets have geological signals", QUERY_GEO_SIGNALS.getAction());
        queryMap.put("what stations or ports are in this system", QUERY_STATIONS.getAction());
        queryMap.put("fleet carriers in this system", QUERY_CARRIERS.getAction());
        queryMap.put("system security, traffic, factions, controlling powers", SYSTEM_SECURITY_ANALYSIS.getAction());
        queryMap.put("trade profile settings, show trade parameters", TRADE_PROFILE_ANALYSIS.getAction());
        queryMap.put("distance to stellar object" + KEY_X, DISTANCE_TO_BODY.getAction());
        queryMap.put("analyze last scan, what did we just scan", LAST_SCAN_ANALYSIS.getAction());
        queryMap.put("inventory for material X, how much X do we have" + KEY_X, MATERIALS_INVENTORY.getAction());
        queryMap.put("materials on this planet, what can we collect here", PLANET_MATERIALS.getAction());
        queryMap.put("exploration profits, exobiology earnings", EXPLORATION_PROFITS.getAction());
        queryMap.put("current location, where are we, system info", CURRENT_LOCATION.getAction());
        queryMap.put("ship fuel level, how much fuel do we have", SHIP_FUEL_STATUS.getAction());
        queryMap.put("analyze jump destination, info on selected system", FSD_TARGET_ANALYSIS.getAction());
        queryMap.put("ship plotted route, our navigation route", PLOTTED_ROUTE_ANALYSIS.getAction());
        queryMap.put("fleet carrier plotted route, carrier navigation plan", CARRIER_ROUTE_ANALYSIS.getAction());
        queryMap.put("trade schedule, trade route stops, our trading plan", TRADE_ROUTE_ANALYSIS.getAction());
        queryMap.put("outfitting available at station, ship modules for sale", LOCAL_OUTFITTING.getAction());
        queryMap.put("ships for sale at station, local shipyard", LOCAL_SHIPYARD.getAction());
        queryMap.put("cargo hold contents, what commodities do we have", CARGO_HOLD_CONTENTS.getAction());
        queryMap.put("carrier destination, where is the carrier going", CARRIER_DESTINATION.getAction());
        queryMap.put("carrier fuel supply, carrier tritium, carrier jump range", CARRIER_TRITIUM_SUPPLY.getAction());
        queryMap.put("carrier finances, how many days of operation funded", CARRIER_STATUS.getAction());
        queryMap.put("carrier ETA, when does the carrier arrive", CARRIER_ETA.getAction());
        queryMap.put("distance to fleet carrier, how far is the carrier", DISTANCE_TO_CARRIER.getAction());
        queryMap.put("pirate mission progress, massacre mission kills", PIRATE_MISSION_PROGRESS.getAction());
        queryMap.put("player stats, ranks, combat rank, exploration rank", PLAYER_PROFILE_ANALYSIS.getAction());
        queryMap.put("ship loadout, damage report, what modules do we have", SHIP_LOADOUT.getAction());
        queryMap.put("station services, what services are available here", STATION_DETAILS.getAction());
        queryMap.put("what can you do, your capabilities, app features", APP_CAPABILITIES.getAction());
        queryMap.put("your name, your designation, who are you", AI_DESIGNATION.getAction());
        queryMap.put("total bounties collected, bounty earnings", TOTAL_BOUNTIES.getAction());
        queryMap.put("distance to the bubble, how far from civilisation", DISTANCE_TO_BUBBLE.getAction());
        queryMap.put("distance to last bio sample, how far to next organic", DISTANCE_TO_LAST_BIO_SAMPLE.getAction());
        queryMap.put("what time is it, earth time, current time", TIME_IN_ZONE.getAction());
        queryMap.put("star system biome analysis" + KEY_X, PLANET_BIOME_ANALYSIS.getAction());
        queryMap.put("reminder, remind me, what was my reminder", REMINDER.getAction());
        queryMap.put("stations ports and settlements in system, local infrastructure", ANALYZE_LOCAL_STATIONS.getAction());
        queryMap.put("active missions, mission list, what missions do we have", ANALYZE_MISSIONS.getAction());
        queryMap.put("(fallback) general conversation, chitchat, anything not matched above", GENERAL_CONVERSATION.getAction());

        return queryMap;
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
