package elite.intel.ai.brain;

import elite.intel.session.Status;
import elite.intel.session.SystemSession;

import java.util.HashMap;
import java.util.Map;

import static elite.intel.ai.brain.handlers.commands.Commands.*;
import static elite.intel.ai.brain.handlers.query.Queries.*;

public class AiCommandsAndQueries {

    private final static String KEY_X = "{\"key\": X}";
    private final static String KEY_X_WITH_DISTANCE = "{\"key\": X, \"max_distance\": Y}";
    private final static String KEY_STATE = "{\"state\": true/false}";
    private final static String KEY_LAT_LON = "{\"lat\": X, \"lon\": Y}";


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
        /// parametrized
        /// ship
        if (status.isInMainShip()) {
            commandMap.put(" navigate to next mission, set destination for mission type <type>, plot route to mission type <type>. -IF mission type is provided return it in " + KEY_X, NAVIGATE_TO_NEXT_MISSION.getAction());
            commandMap.put(" set mining target <material>, add <material> to mining targets " + KEY_X, ADD_MINING_TARGET.getAction());
            commandMap.put(" raw material trader within X light years " + KEY_X, FIND_RAW_MATERIAL_TRADER.getAction());
            commandMap.put(" encoded material trader within X light years " + KEY_X, FIND_ENCODED_MATERIAL_TRADER.getAction());
            commandMap.put(" manufactured material trader within X light years  " + KEY_X, FIND_MANUFACTURED_MATERIAL_TRADER.getAction());
            commandMap.put(" hunting grounds, where we can hunt pirates within X light years " + KEY_X, FIND_HUNTING_GROUNDS.getAction());  // Fixed typo
            commandMap.put(" human tech broker, human tech broker within X light years " + KEY_X, FIND_HUMAN_TECHNOLOGY_BROKER.getAction());
            commandMap.put(" guardian tech broker, guardian broker within X light years " + KEY_X, FIND_GUARDIAN_TECHNOLOGY_BROKER.getAction());
            commandMap.put(" planet with brain trees that contains material X " + KEY_X_WITH_DISTANCE, FIND_BRAIN_TREES.getAction());
            commandMap.put(" star system where we can mine tritium, carrier fuel mining site " + KEY_X_WITH_DISTANCE, FIND_FLEET_CARRIER_FUEL_MINING_SITE.getAction());
            commandMap.put(" star system where we can mine material X, where we can mine silver " + KEY_X_WITH_DISTANCE, FIND_MINING_SITE.getAction());
            commandMap.put(" market where we can buy commodity X, where can we buy X, star system where we can buy tea within X ly " + KEY_X_WITH_DISTANCE, FIND_COMMODITY.getAction());
            commandMap.put(" set home system, this will be our new home ", SET_HOME_SYSTEM.getAction());
            commandMap.put(" turn on radio, turn off radio " + KEY_STATE, SET_RADIO_TRANSMISSION_MODE.getAction());
            commandMap.put(" navigate to coordinates lat/lon " + KEY_LAT_LON, NAVIGATE_TO_TARGET.getAction());
            commandMap.put(" discovery announcements on/off " + KEY_STATE, DISCOVERY_ON_OFF.getAction());
            commandMap.put(" mining announcements on/off " + KEY_STATE, MINING_ON_OFF.getAction());
            commandMap.put(" route announcements on/off " + KEY_STATE, ROUTE_ON_OFF.getAction());
            commandMap.put(" set speed plus X " + KEY_X, INCREASE_SPEED_BY.getAction());  // Fixed to assumed correct action
            commandMap.put(" set voice to X, change voice to X " + KEY_X, SET_AI_VOICE.getAction());
            commandMap.put(" set speed minus X " + KEY_X, DECREASE_SPEED_BY.getAction());

            /// non parametrized
            commandMap.put(" recon target system, plot route to recon mission location, plot route to hunting ground ", RECON_TARGET_SYSTEM.getAction());
            commandMap.put(" clear mining targets ", CLEAR_MINING_TARGETS.getAction());
            commandMap.put(" interrupt text to speech, silence, cancel, belay that, shut up ", INTERRUPT_TTS.getAction());
            commandMap.put(" plot route to pirate mission provider ", RECON_PROVIDER_SYSTEM.getAction());
            commandMap.put(" plot route to pirate mission battle ground ", NAVIGATE_TO_PIRATE_MISSION_TARGET_SYSTEM.getAction());
            commandMap.put(" monetize route, lets make some money on this run, trade along the route ", MONETIZE_ROUTE.getAction());
            commandMap.put(" locate/find nearest vista genomics ", FIND_VISTA_GENOMICS.getAction());
            commandMap.put(" locate/find nearest fleet carrier ", FIND_NEAREST_FLEET_CARRIER.getAction());
            commandMap.put(" clear or cancel fleet carrier route ", CLEAR_FLEET_CARRIER_ROUTE.getAction());

            commandMap.put(" display communications panel, show comms panel, show comms ", DISPLAY_COMMS_PANEL.getAction());
            commandMap.put(" display contacts panel, show contacts panel, show navigation panel, show left panel, show contacts, display left panel ", DISPLAY_CONTACTS_PANEL.getAction());
            commandMap.put(" display internal panel ", DISPLAY_INTERNAL_PANEL.getAction());
            //commandMap.put(" show radar ", DISPLAY_RADAR_PANEL.getAction());
            commandMap.put(" show loadout panel ", DISPLAY_LOADOUT_PANEL.getAction());
            commandMap.put(" **lets go**, lets get out of here, take us to hyperspace, enter hyperspace, go to next way point ", JUMP_TO_HYPERSPACE.getAction());
            commandMap.put(" disengage FSD, drop from supercruise, drop ", EXIT_SUPER_CRUISE.getAction());
            commandMap.put(" combat mode, hud to combat ", ACTIVATE_COMBAT_MODE.getAction());
            commandMap.put(" plot route to carrier, plot route to home base, return to base ", PLOT_ROUTE_TO_CARRIER.getAction());
            commandMap.put(" plot route to home system ", TAKE_ME_HOME.getAction());
            commandMap.put(" set optimal speed, optimize speed, approaching planet, optimal approach speed, approach speed. ", SET_OPTIMAL_SPEED.getAction());  // Kept here
            commandMap.put(" open FSS and scan, scan system, honk, scan ", OPEN_FSS_AND_SCAN.getAction());  // Fixed typo
            commandMap.put(" navigate to LZ/landing zone ", GET_HEADING_TO_LZ.getAction());
            commandMap.put(" deploy srv/surface reconnaissance vehicle ", DEPLOY_SRV.getAction());
            commandMap.put(" calculate carrier route ", CALCULATE_FLEET_CARRIER_ROUTE.getAction());
            commandMap.put(" enter carrier destination ", ENTER_NEXT_FLEET_CARRIER_DESTINATION.getAction());
            commandMap.put(" calculate trade route, get us a trade route ", CALCULATE_TRADE_ROUTE.getAction());
            commandMap.put(" **plot route** to next trade stop/station/port ", PLOT_ROUTE_TO_NEXT_TRADE_STOP.getAction());
            commandMap.put(" what is our trade route profile, describe trade route profile ", LIST_TRADE_ROUTE_PARAMETERS.getAction());
            commandMap.put(" cancel/clear trade route ", CLEAR_TRADE_ROUTE.getAction());
            commandMap.put(" activate, punch it, engage ", ACTIVATE.getAction());
            commandMap.put(" deploy heat sink ", DEPLOY_HEAT_SINK.getAction());
            commandMap.put(" stop, engine stop, full stop, taxi, set speed zero ", STOP.getAction());
            commandMap.put(" quarter throttle, quarter speed, speed 25 ", SET_SPEED25.getAction());
            commandMap.put(" half throttle, half speed, speed 50 ", SET_SPEED50.getAction());
            commandMap.put(" throttle 75, three quarters throttle, set speed 75 ", SET_SPEED75.getAction());  // Removed duplicates
            commandMap.put(" max speed, full speed ", SET_SPEED100.getAction());
            commandMap.put(" set fuel reserve X "+KEY_X, SET_CARRIER_FUEL_RESERVE.getAction());
            commandMap.put(" select/target next system in route ", TARGET_NEXT_ROUTE_SYSTEM.getAction());
            commandMap.put(" target wingman 1 ", TARGET_WINGMAN0.getAction());
            commandMap.put(" target wingman 2 ", TARGET_WINGMAN1.getAction());
            commandMap.put(" target wingman 3 ", TARGET_WINGMAN2.getAction());
            commandMap.put(" wing nav lock, lock on wing ", WING_NAV_LOCK.getAction());
        }

        /// ship or SRV
        if (status.isInMainShip() || status.isInSrv()) {
            commandMap.put(" night vision toggle on/off " + KEY_STATE, NIGHT_VISION_ON_OFF.getAction());
            commandMap.put(" analysis mode, hud to analysis ", ACTIVATE_ANALYSIS_MODE.getAction());
            commandMap.put(" power to shields, max shields, shields up, shields, shields maximum power ", INCREASE_SHIELDS_POWER.getAction());
            commandMap.put(" power to engines, max engines, full power to engines, engines maximum power ", INCREASE_ENGINES_POWER.getAction());
            commandMap.put(" power to weapons, max weapons, full power to weapons, maximum power to weapons ", INCREASE_WEAPONS_POWER.getAction());
            commandMap.put(" equalize power, reset power, power balance ", RESET_POWER.getAction());
            commandMap.put(" cancel navigation ", NAVIGATION_ON_OFF.getAction());
            commandMap.put(" open cargo bay, open cargo scoop, open cargo door, open cargo ", OPEN_CARGO_SCOOP.getAction());
            commandMap.put(" close cargo bay, close cargo scoop, close cargo door, close cargo ", CLOSE_CARGO_SCOOP.getAction());
            commandMap.put(" navigate to next organic/sample/marker/codex entry ", NAVIGATE_TO_NEXT_BIO_SAMPLE.getAction());
        }

        /// ship normal space flight (not supercruise)
        if (!status.isInSupercruise()) {
            commandMap.put(" target power plant, target engines, target subsystem, X ", TARGET_SUB_SYSTEM.getAction());
            commandMap.put(" recall ship / dismiss ship ", RECALL_DISMISS_SHIP.getAction());
            commandMap.put(" defend ship ", REQUEST_DEFENSIVE_BEHAVIOUR.getAction());
            commandMap.put(" focus my target ", REQUEST_FOCUS_TARGET.getAction());
            commandMap.put(" hold fire ", REQUEST_HOLD_FIRE.getAction());
            commandMap.put(" fighter to base, recall fighter ", REQUEST_REQUEST_DOCK.getAction());
            commandMap.put(" supercruise, enter super cruise, engage FSD ", ENTER_SUPER_CRUISE.getAction());
            commandMap.put(" retract hardpoints, stove weapons ", RETRACT_HARDPOINTS.getAction());
            commandMap.put(" deploy hardpoints, hot, deploy weapons ", DEPLOY_HARDPOINTS.getAction());
            commandMap.put(" deploy landing gear, landing gear down ", DEPLOY_LANDING_GEAR.getAction());
            commandMap.put(" retract landing gear, landing gear up ", RETRACT_LANDING_GEAR.getAction());
            commandMap.put(" request docking, ask for landing permission, docking authorization, contact tower request landing, get a parking spot.", REQUEST_DOCKING.getAction());
            commandMap.put(" select/target highest threat ", SELECT_HIGHEST_THREAT.getAction());
        }

        ///  SRV only
        if (status.isInSrv()) {
            commandMap.put(" headlights off, headlights on, turn off the lights, turn on the lights " + KEY_STATE, LIGHTS_ON_OFF.getAction());
            commandMap.put(" drive assist on/off " + KEY_STATE, DRIVE_ASSIST.getAction());
            commandMap.put(" recover srv/surface reconnaissance vehicle, requesting extraction, extract srv ", RECOVER_SRV.getAction());
        }

        if (status.isOnFoot()) {
            /// none yet.
        }

        /// any status
        commandMap.put(" system shut down ", SHUT_DOWN.getAction());
        commandMap.put(" set streaming mode on/off " + KEY_STATE, SET_STREAMING_MODE.getAction());
        commandMap.put(" show/display carrier management panel ", DISPLAY_CARRIER_MANAGEMENT.getAction());
        commandMap.put(" change trade profile, set starting budget to X " + KEY_X, CHANGE_TRADE_PROFILE_SET_STARTING_BUDGET.getAction());
        commandMap.put(" change trade profile, set max number of stops to X " + KEY_X, CHANGE_TRADE_PROFILE_SET_MAX_NUMBER_OF_STOPS.getAction());
        commandMap.put(" change trade profile, set max distance from entry to X " + KEY_X, CHANGE_TRADE_PROFILE_SET_MAX_DISTANCE_FROM_ENTRY.getAction());
        commandMap.put(" change trade profile, allow prohibited cargo on/off " + KEY_STATE, CHANGE_TRADE_PROFILE_SET_ALLOW_PROHIBITED_CARGO.getAction());
        commandMap.put(" change trade profile, allow planetary port on/off " + KEY_STATE, CHANGE_TRADE_PROFILE_SET_ALLOW_PLANETARY_PORT.getAction());
        commandMap.put(" change trade profile, allow permit systems on/off " + KEY_STATE, CHANGE_TRADE_PROFILE_SET_ALLOW_PERMIT_SYSTEMS.getAction());
        commandMap.put(" change trade profile, allow strongholds on/off " + KEY_STATE, CHANGE_TRADE_PROFILE_SET_ALLOW_STRONGHOLDS.getAction());
        commandMap.put(" galaxy map, open/show galaxy map ", OPEN_GALAXY_MAP.getAction());
        commandMap.put(" local map, open/show local map, show local map, open local map ", OPEN_SYSTEM_MAP.getAction());
        commandMap.put(" close map ", CLOSE_ANY_MAP.getAction());
        commandMap.put(" exit, exit to HUD ", EXIT_TO_HUD.getAction());
        commandMap.put(" list voices ", LIST_AVAILABLE_VOICES.getAction());
        commandMap.put(" clear codex entries ", CLEAR_CODEX_ENTRIES.getAction());
        commandMap.put(" clear cache ", CLEAR_CACHE.getAction());

        if (!systemSession.isRunningLocalLLM()) {
            /// Cloud LLM only
            commandMap.put(" change personality to X " + KEY_X, SET_PERSONALITY.getAction());
            commandMap.put(" change profile to X " + KEY_X, SET_PROFILE.getAction());
        }


        return commandMap;
    }

    private Map<String, String> buildQueryMap() {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(" are there any missing bindings, check key bindings  ", KEY_BINDINGS_ANALYSIS.getAction());
        queryMap.put(" help with, how can I..., explain how to... ", HELP.getAction());
        queryMap.put(" are there any organics in the star system, what planets have bio forms to scan, are there any bio signals in star system ", BIO_SAMPLE_IN_STAR_SYSTEM.getAction());
        queryMap.put(" lookup organics or exobiology for plant scans ", EXOBIOLOGY_SAMPLES.getAction());
        queryMap.put(" lookup star system data, stellar objects analysis, landable planets/moons, classes of stellar objects ", QUERY_STELLAR_OBJETS.getAction());
        queryMap.put(" query, system signals, bio signals, geological signals, hostspots, mining sites, battle grounds, resource sites ", QUERY_STELLAR_SIGNALS.getAction());
        queryMap.put(" what planets have geo signals ", QUERY_GEO_SIGNALS.getAction());
        queryMap.put(" lookup stations, ports and settlements ", QUERY_STATIONS.getAction());
        queryMap.put(" lookup fleet carriers present in the star system ", QUERY_CARRIERS.getAction());
        queryMap.put(" lookup system security, traffic, casualties, local controlling factions and major powers, who is in charge, local government  ", SYSTEM_SECURITY_ANALYSIS.getAction());
        queryMap.put(" what is our trade profile, summarize trade profile, analyze tade profile", TRADE_PROFILE_ANALYSIS.getAction());
        queryMap.put(" how far is planet <planet name>, how far is station <station name> ", DISTANCE_TO_BODY.getAction());
        queryMap.put(" analyze X we just scanned, tell me about this last scan,  ", LAST_SCAN_ANALYSIS.getAction());
        queryMap.put(" check inventory how much X do we have, do we have any Y in our inventory, do we have any Zirconium ", MATERIALS_INVENTORY.getAction());
        queryMap.put(" geology questions, percentage of material X, name X common materials on this planet, name X most rare materials on this planet ", PLANET_MATERIALS.getAction());
        queryMap.put(" what are potential profits from exploration data ", EXPLORATION_PROFITS.getAction());
        queryMap.put(" lookup current location, where are we, what is the temperature on this planet, how long does the day last here ", CURRENT_LOCATION.getAction());
        queryMap.put(" how much fuel does our ship has, (do not confuse with carrier) ", SHIP_FUEL_STATUS.getAction());
        queryMap.put(" what information do you have on this target, analyze selected target ", FSD_TARGET_ANALYSIS.getAction());
        queryMap.put(" tell me about our trade route, analyze trade route, check trade route ", TRADE_ROUTE_ANALYSIS.getAction());
        queryMap.put(" lookup outfitting available at the station ", LOCAL_OUTFITTING.getAction());
        queryMap.put(" lookup ships available for sale at the station ", LOCAL_SHIPYARD.getAction());
        //queryMap.put(" query stations in this star system, are there any shipyards around, are there any markets around ", LOCAL_STATIONS.getAction());
        queryMap.put(" distance to the ship destination, how many stops left, how far to final destination ", DISTANCE_TO_DESTINATION.getAction());
        queryMap.put(" lookup cargo hold contents, commodities on board. what do we have in cargo, what do we have on board, how much free spacec we have ", CARGO_HOLD_CONTENTS.getAction());
        queryMap.put(" lookup ship route, information about next jump/stop/waypoint, what class is the next star, are there any traffic or casualties on route ", PLOTTED_ROUTE_ANALYSIS.getAction());
        queryMap.put(" lookup carrier route (do not confuse with ship route), how long will it take for carrier to reach final destination ", CARRIER_ROUTE_ANALYSIS.getAction());
        queryMap.put(" lookup carrier destination (do not confuse with ship) ", CARRIER_DESTINATION.getAction());
        queryMap.put(" lookup carrier fuel supply, jump range, fuel reserve, what is our tritium supply, tritium reserve (do not confuse with ship) ", CARRIER_TRITIUM_SUPPLY.getAction());
        queryMap.put(" lookup carrier finances, funded days of operation V ", CARRIER_STATUS.getAction());
        queryMap.put(" lookup carrier estimated time of arrival (do not confuse with ship) ", CARRIER_ETA.getAction());
        queryMap.put(" how far are we from the carrier ", DISTANCE_TO_CARRIER.getAction());
        //queryMap.put(" what missions do we have, are there any outstanding missions, do we have any active missions ", OUTSTANDING_MISSIONS.getAction());
        queryMap.put(" how many kills left on pirate missions, pirate massacre mission progress (these are pirate massacre specific missions only) ", PIRATE_MISSION_PROGRESS.getAction());
        queryMap.put(" lookup player stats, ranks, rank progress etc ", PLAYER_PROFILE_ANALYSIS.getAction());
        queryMap.put(" ship combat readiness, damage report, jump range, loadout, ship capabilities. Do we have fuel scoop equipped, is ship battle worthy, what class of ship is it ", SHIP_LOADOUT.getAction());
        queryMap.put(" what services are available at this station ", STATION_DETAILS.getAction());
        queryMap.put(" what can you do, what are your capabilities ", APP_CAPABILITIES.getAction());
        queryMap.put(" what is your name ", AI_DESIGNATION.getAction());
        queryMap.put(" how much do we have in bounties, how many bounties we have collected (in credits) ", TOTAL_BOUNTIES.getAction());
        queryMap.put(" how far are we from the bubble, how long will it take to get to the bubble ", DISTANCE_TO_BUBBLE.getAction());
        queryMap.put(" how far are we from last organic, bio sample, previous bioscan ", DISTANCE_TO_LAST_BIO_SAMPLE.getAction());
        queryMap.put(" what time is it ", TIME_IN_ZONE.getAction());
        queryMap.put(" run biome analysis, analyze biome for planet/moon X (parameter example: \"key\":\"2a\")", PLANET_BIOME_ANALYSIS.getAction());
        queryMap.put(" remind me about X, destination reminder, remind what are we selling, buying, duing. ", REMINDER.getAction());
        queryMap.put(" what missions do we have, are there any outstanding missions, do we have any active missions", ANALYZE_MISSIONS.getAction());
        queryMap.put(" if nothing else matches use this ", GENERAL_CONVERSATION.getAction());
        return queryMap;
    }


    public String getCommandMap() {
        StringBuilder sb = new StringBuilder();
        buildCommandMap().forEach((s, s2) -> {
            sb.append("|").append(s).append(" -> ").append(s2).append(" \n");
        });
        return sb.toString();
    }

    public String getQueries() {
        StringBuilder sb = new StringBuilder();
        buildQueryMap().forEach((s, s2) -> {
            sb.append("|").append(s).append(" -> ").append(s2).append(" \n");
        });
        return sb.toString();
    }
}