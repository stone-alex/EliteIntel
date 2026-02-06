package elite.intel.ai.brain;

import elite.intel.session.Status;
import elite.intel.session.SystemSession;

import java.util.HashMap;
import java.util.Map;

import static elite.intel.ai.brain.handlers.commands.Commands.*;
import static elite.intel.ai.brain.handlers.query.Queries.*;

public class AiCommandsAndQueries {

    private final static String KEY_X = " param template: {\"key\": X} or empty X";
    private final static String KEY_X_WITH_DISTANCE = " param template: {\"key\": X, \"max_distance\": Y}  or empty X Y";
    private final static String KEY_STATE = " param template: {\"state\": true/false}";
    private final static String KEY_LAT_LON = " param template: {\"lat\": X, \"lon\": Y}y";


    private static final AiCommandsAndQueries INSTANCE = new AiCommandsAndQueries();
    private final Status status = Status.getInstance();
    private final SystemSession systemSession = SystemSession.getInstance();


    private AiCommandsAndQueries() {
    }

    public static AiCommandsAndQueries getInstance() {
        return INSTANCE;
    }

    private Map<String, String> buildCommandMap() {
        Map<String, String> commandMap = new HashMap<>();
        commandMap.put(" command Verify LLM Connection ", CONNECTION_CHECK.getAction());
        /// parametrized
        /// ship
        if (status.isInMainShip()) {
            commandMap.put(" command navigate to next mission" + KEY_X, NAVIGATE_TO_NEXT_MISSION.getAction());
            commandMap.put(" command add <material> to mining targets " + KEY_X, ADD_MINING_TARGET.getAction());
            commandMap.put(" command find raw material trader" + KEY_X, FIND_RAW_MATERIAL_TRADER.getAction());
            commandMap.put(" command find encoded material trader" + KEY_X, FIND_ENCODED_MATERIAL_TRADER.getAction());
            commandMap.put(" command find manufactured material trader " + KEY_X, FIND_MANUFACTURED_MATERIAL_TRADER.getAction());
            commandMap.put(" command find hunting grounds " + KEY_X, FIND_HUNTING_GROUNDS.getAction());  // Fixed typo
            commandMap.put(" command find human tech broker" + KEY_X, FIND_HUMAN_TECHNOLOGY_BROKER.getAction());
            commandMap.put(" command find guardian tech broker " + KEY_X, FIND_GUARDIAN_TECHNOLOGY_BROKER.getAction());
            commandMap.put(" command find brain trees " + KEY_X_WITH_DISTANCE, FIND_BRAIN_TREES.getAction());
            commandMap.put(" command find mine material X " + KEY_X_WITH_DISTANCE, FIND_MINING_SITE.getAction());
            commandMap.put(" command find where we can mine tritium " + KEY_X_WITH_DISTANCE, FIND_FLEET_CARRIER_FUEL_MINING_SITE.getAction());
            commandMap.put(" command find where we where can we buy X, within Y ly " + KEY_X_WITH_DISTANCE, FIND_COMMODITY.getAction());
            commandMap.put(" command set as home system ", SET_HOME_SYSTEM.getAction());
            commandMap.put(" command toggle radio on/off " + KEY_STATE, SET_RADIO_TRANSMISSION_MODE.getAction());
            commandMap.put(" command navigate to coordinates lat/lon " + KEY_LAT_LON, NAVIGATE_TO_TARGET.getAction());
            commandMap.put(" command discovery announcements on/off " + KEY_STATE, DISCOVERY_ON_OFF.getAction());
            commandMap.put(" command mining announcements on/off " + KEY_STATE, MINING_ON_OFF.getAction());
            commandMap.put(" command route announcements on/off " + KEY_STATE, ROUTE_ON_OFF.getAction());
            commandMap.put(" command set speed plus X " + KEY_X, INCREASE_SPEED_BY.getAction());  // Fixed to assumed correct action
            commandMap.put(" command set voice to X, change voice to X " + KEY_X, SET_AI_VOICE.getAction());
            commandMap.put(" command set speed minus X " + KEY_X, DECREASE_SPEED_BY.getAction());

            /// non parametrized
            commandMap.put(" command clear mining targets ", CLEAR_MINING_TARGETS.getAction());
            commandMap.put(" command recon target system ", RECON_TARGET_SYSTEM.getAction());
            commandMap.put(" command interrupt, silence, cancel", INTERRUPT_TTS.getAction());
            commandMap.put(" command plot route to pirate mission provider ", RECON_PROVIDER_SYSTEM.getAction());
            commandMap.put(" command plot route to pirate mission battle ground ", NAVIGATE_TO_PIRATE_MISSION_TARGET_SYSTEM.getAction());
            commandMap.put(" command monetize route", MONETIZE_ROUTE.getAction());
            commandMap.put(" command locate/find nearest vista genomics ", FIND_VISTA_GENOMICS.getAction());
            commandMap.put(" command locate/find nearest fleet carrier ", FIND_NEAREST_FLEET_CARRIER.getAction());
            commandMap.put(" command clear or cancel fleet carrier route ", CLEAR_FLEET_CARRIER_ROUTE.getAction());

            commandMap.put(" command show comms panel, show comms ", DISPLAY_COMMS_PANEL.getAction());
            commandMap.put(" command display contacts panel", DISPLAY_CONTACTS_PANEL.getAction());
            commandMap.put(" command display internal panel ", DISPLAY_INTERNAL_PANEL.getAction());
            commandMap.put(" command show loadout panel ", DISPLAY_LOADOUT_PANEL.getAction());
            commandMap.put(" command **lets go**, lets get out of here, take us to hyperspace, enter hyperspace, go to next way point ", JUMP_TO_HYPERSPACE.getAction());
            commandMap.put(" command drop, disengage FSD, drop from supercruise", EXIT_SUPER_CRUISE.getAction());
            commandMap.put(" command plot route to carrier, plot route to base, return to base ", PLOT_ROUTE_TO_CARRIER.getAction());
            commandMap.put(" command plot route to home system ", TAKE_ME_HOME.getAction());
            commandMap.put(" command set optimal speed ", SET_OPTIMAL_SPEED.getAction());  // Kept here
            commandMap.put(" command scan this system, perform scan, open FSS and scan, scan system, honk, scan ", OPEN_FSS_AND_SCAN.getAction());  // Fixed typo
            commandMap.put(" command navigate to LZ/landing zone ", GET_HEADING_TO_LZ.getAction());
            commandMap.put(" command deploy srv/surface reconnaissance vehicle ", DEPLOY_SRV.getAction());
            commandMap.put(" command calculate carrier route ", CALCULATE_FLEET_CARRIER_ROUTE.getAction());
            commandMap.put(" command enter carrier destination ", ENTER_NEXT_FLEET_CARRIER_DESTINATION.getAction());
            commandMap.put(" command calculate trade route, get us a trade route ", CALCULATE_TRADE_ROUTE.getAction());
            commandMap.put(" command **plot route** to trade stop/station/port ", PLOT_ROUTE_TO_NEXT_TRADE_STOP.getAction());
            commandMap.put(" command describe trade profile ", LIST_TRADE_ROUTE_PARAMETERS.getAction());
            commandMap.put(" command cancel/clear trade route ", CLEAR_TRADE_ROUTE.getAction());

            commandMap.put(" command deploy heat sink ", DEPLOY_HEAT_SINK.getAction());
            commandMap.put(" command stop, engine stop, full stop, taxi, set speed zero ", STOP.getAction());
            commandMap.put(" command quarter throttle, quarter speed, speed 25 ", SET_SPEED25.getAction());
            commandMap.put(" command half throttle, half speed, speed 50 ", SET_SPEED50.getAction());
            commandMap.put(" command throttle 75, three quarters throttle, set speed 75 ", SET_SPEED75.getAction());  // Removed duplicates
            commandMap.put(" command max speed, full speed ", SET_SPEED100.getAction());
            commandMap.put(" command set fuel reserve X " + KEY_X, SET_CARRIER_FUEL_RESERVE.getAction());
            commandMap.put(" command select/target next system in route ", TARGET_NEXT_ROUTE_SYSTEM.getAction());
//            commandMap.put(" command target wingman 1 ", TARGET_WINGMAN0.getAction());
//            commandMap.put(" command target wingman 2 ", TARGET_WINGMAN1.getAction());
//            commandMap.put(" command target wingman 3 ", TARGET_WINGMAN2.getAction());
            commandMap.put(" command wing nav lock, lock on wing ", WING_NAV_LOCK.getAction());
        }

        /// ship or SRV
        if (status.isInMainShip() || status.isInSrv()) {
            commandMap.put(" command night vision toggle on/off " + KEY_STATE, NIGHT_VISION_ON_OFF.getAction());
            commandMap.put(" command change mode to combat, hud to combat ", ACTIVATE_COMBAT_MODE.getAction());
            commandMap.put(" command change mode to analysis, hud to analysis ", ACTIVATE_ANALYSIS_MODE.getAction());
            commandMap.put(" command max shields, shields up, shields ", INCREASE_SHIELDS_POWER.getAction());
            commandMap.put(" command max engines, full power to engines ", INCREASE_ENGINES_POWER.getAction());
            commandMap.put(" command max weapons, full power to weapons ", INCREASE_WEAPONS_POWER.getAction());
            commandMap.put(" command equalize power, reset power ", RESET_POWER.getAction());
            commandMap.put(" command cancel navigation ", NAVIGATION_ON_OFF.getAction());
            commandMap.put(" command open cargo bay door, open cargo scoop ", OPEN_CARGO_SCOOP.getAction());
            commandMap.put(" command close cargo bay door, close cargo scoop ", CLOSE_CARGO_SCOOP.getAction());
            commandMap.put(" command navigate to next organic/sample/marker/codex entry ", NAVIGATE_TO_NEXT_BIO_SAMPLE.getAction());
        }

        /// ship normal space flight (not supercruise)
        if (!status.isInSupercruise()) {
            commandMap.put(" command target power plant, target subsystem, X ", TARGET_SUB_SYSTEM.getAction());
            commandMap.put(" command recall ship / dismiss ship ", RECALL_DISMISS_SHIP.getAction());
            commandMap.put(" command order fighter to defend ship ", REQUEST_DEFENSIVE_BEHAVIOUR.getAction());
            commandMap.put(" command order fighter focus my target ", REQUEST_FOCUS_TARGET.getAction());
            commandMap.put(" command order fighter to hold fire ", REQUEST_HOLD_FIRE.getAction());
            commandMap.put(" command order fighter return to mother ship ", REQUEST_REQUEST_DOCK.getAction());
            commandMap.put(" command supercruise, enter super cruise, engage FSD ", ENTER_SUPER_CRUISE.getAction());
            commandMap.put(" command retract hardpoints, stove weapons ", RETRACT_HARDPOINTS.getAction());
            commandMap.put(" command deploy hardpoints, weapons hot, deploy weapons ", DEPLOY_HARDPOINTS.getAction());
            commandMap.put(" command gear-down, deploy landing gear, landing gear DOWN ", DEPLOY_LANDING_GEAR.getAction());
            commandMap.put(" command gear-up, retract landing gear, landing gear UP ", RETRACT_LANDING_GEAR.getAction());
            commandMap.put(" command request docking, ask for landing permission, contact tower request landing, get a parking spot.", REQUEST_DOCKING.getAction());
            commandMap.put(" command select/target highest threat ", SELECT_HIGHEST_THREAT.getAction());
        }

        ///  SRV only
        if (status.isInSrv()) {
            commandMap.put(" command toggle headlights on/off " + KEY_STATE, LIGHTS_ON_OFF.getAction());
            commandMap.put(" command drive assist on/off " + KEY_STATE, DRIVE_ASSIST.getAction());
            commandMap.put(" command recover srv/surface reconnaissance vehicle, requesting extraction, extract srv ", RECOVER_SRV.getAction());
        }

        if (status.isOnFoot()) {
            /// none yet.
        }

        /// any status
        commandMap.put(" command activate, punch it, engage ", ACTIVATE.getAction());
        commandMap.put(" command system shut-down ", SHUT_DOWN.getAction());
        commandMap.put(" command set streaming mode on/off " + KEY_STATE, SET_STREAMING_MODE.getAction());
        commandMap.put(" command show/display carrier management panel ", DISPLAY_CARRIER_MANAGEMENT.getAction());
        commandMap.put(" command change trade profile, set starting budget to X " + KEY_X, CHANGE_TRADE_PROFILE_SET_STARTING_BUDGET.getAction());
        commandMap.put(" command change trade profile, set max number of stops to X " + KEY_X, CHANGE_TRADE_PROFILE_SET_MAX_NUMBER_OF_STOPS.getAction());
        commandMap.put(" command change trade profile, set max distance from entry to X " + KEY_X, CHANGE_TRADE_PROFILE_SET_MAX_DISTANCE_FROM_ENTRY.getAction());
        commandMap.put(" command change trade profile, allow prohibited cargo on/off " + KEY_STATE, CHANGE_TRADE_PROFILE_SET_ALLOW_PROHIBITED_CARGO.getAction());
        commandMap.put(" command change trade profile, allow planetary port on/off " + KEY_STATE, CHANGE_TRADE_PROFILE_SET_ALLOW_PLANETARY_PORT.getAction());
        commandMap.put(" command change trade profile, allow permit systems on/off " + KEY_STATE, CHANGE_TRADE_PROFILE_SET_ALLOW_PERMIT_SYSTEMS.getAction());
        commandMap.put(" command change trade profile, allow strongholds on/off " + KEY_STATE, CHANGE_TRADE_PROFILE_SET_ALLOW_STRONGHOLDS.getAction());
        commandMap.put(" command galaxy map, open/show galaxy map ", OPEN_GALAXY_MAP.getAction());
        commandMap.put(" command local map, open/show local map, show local map, open local map ", OPEN_SYSTEM_MAP.getAction());
        commandMap.put(" command close map ", CLOSE_ANY_MAP.getAction());
        commandMap.put(" command exit, exit to HUD ", EXIT_TO_HUD.getAction());
        commandMap.put(" command list voices ", LIST_AVAILABLE_VOICES.getAction());
        commandMap.put(" command clear codex entries ", CLEAR_CODEX_ENTRIES.getAction());
        commandMap.put(" command clear cache ", CLEAR_CACHE.getAction());
        commandMap.put(" command Clear Reminders", CLEAR_REMINDERS.getAction());
        commandMap.put(" command set reminder "+KEY_X, SET_REMINDER.getAction());


        if (!systemSession.isRunningLocalLLM()) {
            /// Cloud LLM only
            commandMap.put(" command change personality to X " + KEY_X, SET_PERSONALITY.getAction());
            commandMap.put(" command change profile to X " + KEY_X, SET_PROFILE.getAction());
        }


        return commandMap;
    }

    private Map<String, String> buildQueryMap() {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(" query check missing bindings", KEY_BINDINGS_ANALYSIS.getAction());
        queryMap.put(" query help with, how can I..., explain how to... ", HELP.getAction());
        queryMap.put(" query stellar objects that bio/organic scans required/completed ", BIO_SAMPLE_IN_STAR_SYSTEM.getAction());
        queryMap.put(" query orgranics/biology this planet/moon/location ", EXOBIOLOGY_SAMPLES.getAction());
        queryMap.put(" query stellar objects analysis, landable planets/moons", QUERY_STELLAR_OBJETS.getAction());
        queryMap.put(" query, signals ", QUERY_STELLAR_SIGNALS.getAction());
        queryMap.put(" query planets have geo signals ", QUERY_GEO_SIGNALS.getAction());
        queryMap.put(" query presence of stations or ports ", QUERY_STATIONS.getAction());
        queryMap.put(" query fleet carriers ", QUERY_CARRIERS.getAction());
        queryMap.put(" query system security/traffic/casualties/controlling factions/major powers", SYSTEM_SECURITY_ANALYSIS.getAction());
        queryMap.put(" query trade profile", TRADE_PROFILE_ANALYSIS.getAction());
        queryMap.put(" query distance to stellar object X " + KEY_X, DISTANCE_TO_BODY.getAction());
        queryMap.put(" query analyze last scan ", LAST_SCAN_ANALYSIS.getAction());
        queryMap.put(" query inventory for X " + KEY_X, MATERIALS_INVENTORY.getAction());
        queryMap.put(" query materials available on planet/moon ", PLANET_MATERIALS.getAction());
        queryMap.put(" query exploration profits ", EXPLORATION_PROFITS.getAction());
        queryMap.put(" query current location data ", CURRENT_LOCATION.getAction());
        queryMap.put(" query ship fuel level ", SHIP_FUEL_STATUS.getAction());
        queryMap.put(" query analyze selected destination ", FSD_TARGET_ANALYSIS.getAction());
        queryMap.put(" query ship plotted route", PLOTTED_ROUTE_ANALYSIS.getAction());
        queryMap.put(" query fleet carrier plotted route ", CARRIER_ROUTE_ANALYSIS.getAction());
        queryMap.put(" query trade schedule", TRADE_ROUTE_ANALYSIS.getAction());
        queryMap.put(" query outfitting/ship parts/modules for sale at station ", LOCAL_OUTFITTING.getAction());
        queryMap.put(" query ships available for sale at the station ", LOCAL_SHIPYARD.getAction());
        queryMap.put(" query cargo hold contents, commodities on board ", CARGO_HOLD_CONTENTS.getAction());
        queryMap.put(" query fleet carrier destination ", CARRIER_DESTINATION.getAction());
        queryMap.put(" query fleet carrier fuel supply/jump range/fuel reserve ", CARRIER_TRITIUM_SUPPLY.getAction());
        queryMap.put(" query carrier finances, funded days of operation ", CARRIER_STATUS.getAction());
        queryMap.put(" query carrier estimated time of arrival ", CARRIER_ETA.getAction());
        queryMap.put(" query distance from from the fleet carrier ", DISTANCE_TO_CARRIER.getAction());
        queryMap.put(" query pirate missions progress ", PIRATE_MISSION_PROGRESS.getAction());
        queryMap.put(" query player stats, ranks, progress etc ", PLAYER_PROFILE_ANALYSIS.getAction());
        queryMap.put(" query ship loadout ", SHIP_LOADOUT.getAction());
        queryMap.put(" query services available at this station ", STATION_DETAILS.getAction());
        queryMap.put(" query your capabilities ", APP_CAPABILITIES.getAction());
        queryMap.put(" query your designation ", AI_DESIGNATION.getAction());
        queryMap.put(" query amount of bounties collected", TOTAL_BOUNTIES.getAction());
        queryMap.put(" query distance to the bubble/earth", DISTANCE_TO_BUBBLE.getAction());
        queryMap.put(" query distance to organic, bio sample, previous bioscan ", DISTANCE_TO_LAST_BIO_SAMPLE.getAction());
        queryMap.put(" query what time is it ", TIME_IN_ZONE.getAction());
        queryMap.put(" query biome analysis for star system/planet/moon X " + KEY_X, PLANET_BIOME_ANALYSIS.getAction());
        queryMap.put(" query reminder, remind me, reminder data, where going, what are we doing, what are we buying, what are we selling. ", REMINDER.getAction());
        queryMap.put(" query active missions ", ANALYZE_MISSIONS.getAction());
        queryMap.put(" IF NOTHING MATCHES USE THIS ", GENERAL_CONVERSATION.getAction());

        return queryMap;
    }


    public String getCommandMap() {
        StringBuilder sb = new StringBuilder();
        sb.append("ALLOWED COMMANDS (use ONLY these exact action names):\n\n");
        buildCommandMap().forEach((concept, action) ->
                sb.append("  ").append(action).append(" ← ").append(concept).append("\n"));
        return sb.toString();
    }

    public String getQueries() {
        StringBuilder sb = new StringBuilder();
        sb.append("ALLOWED QUERIES (use ONLY these exact action names):\n\n");
        buildQueryMap().forEach((concept, action) ->
                sb.append("  ").append(action).append(" ← ").append(concept).append("\n"));
        return sb.toString();
    }
}