package elite.intel.ai.brain;

import java.util.Map;

import static elite.intel.ai.brain.handlers.commands.Commands.*;
import static elite.intel.ai.brain.handlers.query.Queries.*;

public class AiCommandsAndQueries {

    private static final AiCommandsAndQueries INSTANCE = new AiCommandsAndQueries();
    private final Map<String, String> commandMap = Map.<String, String>ofEntries(

            Map.entry(" navigate to next mission, set destination for 'MissionType' (MissionType can be Delivery,Assassinate,Salvage,Rescue,Passenger,Tourist,Courier,Piracy), plot route to mission location, let's get after this 'MissioTarget' (MissionTarget is either a persons name, Known Pirate or Known Terrorist), (optional keyword <key>) ", NAVIGATE_TO_NEXT_MISSION.getAction()),
            Map.entry(" headlights off, headlights on, turn off the lights, turn on the lights ", LIGHTS_ON_OFF.getAction()),
            Map.entry(" set mining target <material> ", ADD_MINING_TARGET.getAction()),
            Map.entry(" clear mining targets ", CLEAR_MINING_TARGETS.getAction()),
            Map.entry(" interrupt text to speech, silence, cancel, belay that, shut up ", INTERRUPT_TTS.getAction()),
            Map.entry(" find raw material trader ", FIND_RAW_MATERIAL_TRADER.getAction()),
            Map.entry(" find encoded material trader ", FIND_ENCODED_MATERIAL_TRADER.getAction()),
            Map.entry(" find manufactured material trader ", FIND_MANUFACTURED_MATERIAL_TRADER.getAction()),
            Map.entry(" find hunting grounds, find where we can hunt pirates ", FIND_HUNTING_GROUNDS.getAction()),  // Fixed typo
            Map.entry(" recon target system, plot route to recon mission location ", RECON_TARGET_SYSTEM.getAction()),
            Map.entry(" plot route to pirate mission provider ", RECON_PROVIDER_SYSTEM.getAction()),
            Map.entry(" plot route to pirate mission battle ground ", NAVIGATE_TO_PIRATE_MISSION_TARGET_SYSTEM.getAction()),
            Map.entry(" monetize route, lets make some money on this run, find trade along the route ", MONETIZE_ROUTE.getAction()),
            Map.entry(" find human tech broker ", FIND_HUMAN_TECHNOLOGY_BROKER.getAction()),
            Map.entry(" find guardian tech broker ", FIND_GUARDIAN_TECHNOLOGY_BROKER.getAction()),
            Map.entry(" find vista genomics ", FIND_VISTA_GENOMICS.getAction()),
            Map.entry(" find planet with brain trees that contains material X ", FIND_BRAIN_TREES.getAction()),
            Map.entry(" find star system where we can mine tritium, find carrier fuel mining site ", FIND_FLEET_CARRIER_FUEL_MINING_SITE.getAction()),
            Map.entry(" find star system where we can mine material X, find where we can mine silver ", FIND_MINING_SITE.getAction()),
            Map.entry(" find nearest fleet carrier ", FIND_NEAREST_FLEET_CARRIER.getAction()),
            Map.entry(" clear or cancel fleet carrier route ", CLEAR_FLEET_CARRIER_ROUTE.getAction()),
            Map.entry(" find market where we can buy commodity X, where can we buy X, find star system where we can buy tea within 100 ly ", FIND_COMMODITY.getAction()),
            Map.entry(" set voice to X, change voice to X ", SET_AI_VOICE.getAction()),
            Map.entry(" set home system, this will be our new home ", SET_HOME_SYSTEM.getAction()),
            Map.entry(" change personality to X ", SET_PERSONALITY.getAction()),
            Map.entry(" change profile to X ", SET_PROFILE.getAction()),
            Map.entry(" turn on radio, turn off radio ", SET_RADIO_TRANSMISSION_MODE.getAction()),
            Map.entry(" set streaming mode on/off ", SET_STREAMING_MODE.getAction()),
            Map.entry(" navigate to coordinates lat/lon ", NAVIGATE_TO_TARGET.getAction()),
            Map.entry(" cancel navigation ", NAVIGATION_ON_OFF.getAction()),
            Map.entry(" discovery announcements on/off ", DISCOVERY_ON_OFF.getAction()),
            Map.entry(" mining announcements on/off ", MINING_ON_OFF.getAction()),
            Map.entry(" route announcements on/off ", ROUTE_ON_OFF.getAction()),
            Map.entry(" set speed plus X ", INCREASE_SPEED_BY.getAction()),  // Fixed to assumed correct action
            Map.entry(" set speed minus X ", DECREASE_SPEED_BY.getAction()),
            Map.entry(" power to engines, engines full power, engines ", INCREASE_ENGINES_POWER.getAction()),
            Map.entry(" navigate to next organic/sample/marker/codex entry ", NAVIGATE_TO_NEXT_BIO_SAMPLE.getAction()),
            Map.entry(" power to shields, max shields, shields up, shields, shields maximum power ", INCREASE_SHIELDS_POWER.getAction()),
            Map.entry(" power to engines, max engines, full power to engines, engines maximum power ", INCREASE_ENGINES_POWER.getAction()),
            Map.entry(" power to weapons, max weapons, full power to weapons, maximum power to weapons ", INCREASE_WEAPONS_POWER.getAction()),
            Map.entry(" equalize power, reset power, power balance ", RESET_POWER.getAction()),  // Fixed typo
            Map.entry(" open/show galaxy map ", OPEN_GALAXY_MAP.getAction()),
            Map.entry(" open/show local map ", OPEN_SYSTEM_MAP.getAction()),
            Map.entry(" close map ", CLOSE_ANY_MAP.getAction()),
            Map.entry(" exit, exit to HUD ", EXIT_TO_HUD.getAction()),
            Map.entry(" display communications panel, show comms panel, show comms ", DISPLAY_COMMS_PANEL.getAction()),
            Map.entry(" display contacts panel, show contacts panel, show navigation panel, show left panel, show contacts, display left panel ", DISPLAY_CONTACTS_PANEL.getAction()),
            Map.entry(" display internal panel ", DISPLAY_INTERNAL_PANEL.getAction()),
            Map.entry(" show radar ", DISPLAY_RADAR_PANEL.getAction()),
            Map.entry(" show loadout panel ", DISPLAY_LOADOUT_PANEL.getAction()),
            Map.entry(" show/display carrier management panel ", DISPLAY_CARRIER_MANAGEMENT.getAction()),
            Map.entry(" open cargo bay, open cargo scoop, open cargo door, open cargo ", OPEN_CARGO_SCOOP.getAction()),
            Map.entry(" close cargo bay, close cargo scoop, close cargo door, close cargo ", CLOSE_CARGO_SCOOP.getAction()),
            Map.entry(" retract hardpoints, stove weapons ", RETRACT_HARDPOINTS.getAction()),
            Map.entry(" deploy hardpoints, weapons hot, deploy weapons ", DEPLOY_HARDPOINTS.getAction()),
            Map.entry(" deploy landing gear, landing gear down ", DEPLOY_LANDING_GEAR.getAction()),
            Map.entry(" retract landing gear, landing gear up ", RETRACT_LANDING_GEAR.getAction()),
            Map.entry(" **lets go**, lets get out of here, take us to hyperspace, enter hyperspace, go to next way point ", JUMP_TO_HYPERSPACE.getAction()),
            Map.entry(" supercruise, enter super cruise, engage FSD ", ENTER_SUPER_CRUISE.getAction()),
            Map.entry(" disengage FSD, drop from supercruise, drop ", EXIT_SUPER_CRUISE.getAction()),
            Map.entry(" analysis mode, hud to analysis ", ACTIVATE_ANALYSIS_MODE.getAction()),
            Map.entry(" combat mode, hud to combat ", ACTIVATE_COMBAT_MODE.getAction()),
            Map.entry(" plot route to carrier ", PLOT_ROUTE_TO_CARRIER.getAction()),
            Map.entry(" plot route to home system ", TAKE_ME_HOME.getAction()),
            Map.entry(" set optimal speed, optimize speed, approaching planet ", SET_OPTIMAL_SPEED.getAction()),  // Kept here
            Map.entry(" open FSS and scan, scan system, honk, scan ", OPEN_FSS_AND_SCAN.getAction()),  // Fixed typo
            Map.entry(" navigate to LZ/landing zone ", GET_HEADING_TO_LZ.getAction()),
            Map.entry(" deploy srv/surface reconnaissance vehicle ", DEPLOY_SRV.getAction()),
            Map.entry(" recover srv/surface reconnaissance vehicle, requesting extraction ", RECOVER_SRV.getAction()),
            Map.entry(" calculate carrier route ", CALCULATE_FLEET_CARRIER_ROUTE.getAction()),
            Map.entry(" enter carrier destination ", ENTER_NEXT_FLEET_CARRIER_DESTINATION.getAction()),
            Map.entry(" system shut down ", SHUT_DOWN.getAction()),
            Map.entry(" calculate trade route, get us a trade route ", CALCULATE_TRADE_ROUTE.getAction()),
            Map.entry(" **plot route** to next trade stop/station/port ", PLOT_ROUTE_TO_NEXT_TRADE_STOP.getAction()),
            Map.entry(" change trade profile, set starting budget to X ", CHANGE_TRADE_PROFILE_SET_STARTING_BUDGET.getAction()),
            Map.entry(" change trade profile, set max number of stops to X ", CHANGE_TRADE_PROFILE_SET_MAX_NUMBER_OF_STOPS.getAction()),
            Map.entry(" change trade profile, set max distance from entry to X ", CHANGE_TRADE_PROFILE_SET_MAX_DISTANCE_FROM_ENTRY.getAction()),
            Map.entry(" change trade profile, allow prohibited cargo on/off ", CHANGE_TRADE_PROFILE_SET_ALLOW_PROHIBITED_CARGO.getAction()),
            Map.entry(" change trade profile, allow planetary port on/off ", CHANGE_TRADE_PROFILE_SET_ALLOW_PLANETARY_PORT.getAction()),
            Map.entry(" change trade profile, allow permit systems on/off ", CHANGE_TRADE_PROFILE_SET_ALLOW_PERMIT_SYSTEMS.getAction()),
            Map.entry(" change trade profile, allow strongholds on/off ", CHANGE_TRADE_PROFILE_SET_ALLOW_STRONGHOLDS.getAction()),
            Map.entry(" what is our trade route profile, describe trade route profile ", LIST_TRADE_ROUTE_PARAMETERS.getAction()),
            Map.entry(" cancel/clear trade route ", CLEAR_TRADE_ROUTE.getAction()),
            Map.entry(" activate, punch it, engage ", ACTIVATE.getAction()),
            Map.entry(" night vision toggle on/off", NIGHT_VISION_ON_OFF.getAction()),
            Map.entry(" request docking, ask for landing permission, docking authorization", REQUEST_DOCKING.getAction()),
            Map.entry(" deploy heat sink ", DEPLOY_HEAT_SINK.getAction()),
            Map.entry(" drive assist on/off ", DRIVE_ASSIST.getAction()),
            Map.entry(" target power plant, target engines, target subsystem, X ", TARGET_SUB_SYSTEM.getAction()),
            Map.entry(" recall ship / dismiss ship ", RECALL_DISMISS_SHIP.getAction()),
            Map.entry(" defend ship ", REQUEST_DEFENSIVE_BEHAVIOUR.getAction()),
            Map.entry(" focus my target ", REQUEST_FOCUS_TARGET.getAction()),
            Map.entry(" hold fire ", REQUEST_HOLD_FIRE.getAction()),
            Map.entry(" fighter to base, return to base ", REQUEST_REQUEST_DOCK.getAction()),
            Map.entry(" stop, engine stop, full stop, taxi, set speed zero ", STOP.getAction()),
            Map.entry(" quarter throttle, quarter speed, speed 25 ", SET_SPEED25.getAction()),
            Map.entry(" half throttle, half speed, speed 50 ", SET_SPEED50.getAction()),
            Map.entry(" throttle 75, three quarters throttle, set speed 75 ", SET_SPEED75.getAction()),  // Removed duplicates
            Map.entry(" max speed, full speed ", SET_SPEED100.getAction()),
            Map.entry(" set fuel reserve X ", SET_CARRIER_FUEL_RESERVE.getAction()),
            Map.entry(" select/target highest threat ", SELECT_HIGHEST_THREAT.getAction()),
            Map.entry(" select/target next system in route ", TARGET_NEXT_ROUTE_SYSTEM.getAction()),
            Map.entry(" target wingman 1 ", TARGET_WINGMAN0.getAction()),
            Map.entry(" target wingman 2 ", TARGET_WINGMAN1.getAction()),
            Map.entry(" target wingman 3 ", TARGET_WINGMAN2.getAction()),
            Map.entry(" wing nav lock, lock on wing ", WING_NAV_LOCK.getAction()),
            Map.entry(" list voices ", LIST_AVAILABLE_VOICES.getAction()),
            Map.entry(" clear codex entries ", CLEAR_CODEX_ENTRIES.getAction()),
            Map.entry(" clear cache ", CLEAR_CACHE.getAction())
    );
    private final Map<String, String> queryMap = Map.ofEntries(
            Map.entry(" are there any missing bindings, check key bindings  ", KEY_BINDINGS_ANALYSIS.getAction()),
            Map.entry(" help with, how can I..., explain how to... ", HELP.getAction()),
            Map.entry(" are there any organics in the star system, what planets have bio forms to scan, are there any bio signals in star system ", BIO_SAMPLE_IN_STAR_SYSTEM.getAction()),
            Map.entry(" queries about organics or exobiology for plant scans ", EXOBIOLOGY_SAMPLES.getAction()),
            Map.entry(" star system analysis, stellar objects analysis, are there landable planets/moons, what class is the stellar object ", QUERY_STELLAR_OBJETS.getAction()),
            Map.entry(" query, system signals, bio signals, geological signals, hostspots, mining sites, battle grounds, resource sites ", QUERY_STELLAR_SIGNALS.getAction()),
            Map.entry(" what planets have geo signals ", QUERY_GEO_SIGNALS.getAction()),
            Map.entry(" queries about stations, ports and settlements ", QUERY_STATIONS.getAction()),
            Map.entry(" queries about fleet carriers present in the star system ", QUERY_CARRIERS.getAction()),
            Map.entry(" queries about system security, traffic, casualties, local controlling factions and major powers  ", SYSTEM_SECURITY_ANALYSIS.getAction()),
            Map.entry(" what is our trade profile, summarize trade profile, analyze tade profile", TRADE_PROFILE_ANALYSIS.getAction()),
            Map.entry(" how far is planet <planet name>, how far is station <station name> ", DISTANCE_TO_BODY.getAction()),
            Map.entry(" analyze X we just scanned, tell me about this last scan,  ", LAST_SCAN_ANALYSIS.getAction()),
            Map.entry(" check inventory how much X do we have, do we have any Y in our inventory ", MATERIALS_INVENTORY.getAction()),
            Map.entry(" geology questions, percentage of material X, name X common materials on this planet, name X most rare materials on this planet ", PLANET_MATERIALS.getAction()),
            Map.entry(" what are potential profits from exploration data ", EXPLORATION_PROFITS.getAction()),
            Map.entry(" queries about current location, where are we, what is the temperature on this planet, how long does the day last here ", CURRENT_LOCATION.getAction()),
            Map.entry(" how much fuel does our ship has, (do not confuse with carrier) ", SHIP_FUEL_STATUS.getAction()),
            Map.entry(" what information do you have on this target, analyze selected target ", FSD_TARGET_ANALYSIS.getAction()),
            Map.entry(" tell me about our trade route, analyze trade route ", TRADE_ROUTE_ANALYSIS.getAction()),
            Map.entry(" queries about outfitting available at the station ", LOCAL_OUTFITTING.getAction()),
            Map.entry(" queries about ships available for sale at the station ", LOCAL_SHIPYARD.getAction()),
            //Map.entry(" query stations in this star system, are there any shipyards around, are there any markets around ", LOCAL_STATIONS.getAction()),
            Map.entry(" distance to the ship destination, how many stops left, how far to final destination ", DISTANCE_TO_DESTINATION.getAction()),
            Map.entry(" queries about cargo hold contents, commodities on board. what do we have in cargo, do we have any booz on board, what do we have on board, how much free spacec we have ", CARGO_HOLD_CONTENTS.getAction()),
            Map.entry(" queries about ship route, can we get fuel at next star/stop/waypoint, what class is the next star, are there any traffic or casualties on route ", PLOTTED_ROUTE_ANALYSIS.getAction()),
            Map.entry(" queries about carrier route (do not confuse with ship route), how long will it take for carrier to reach final destination ", CARRIER_ROUTE_ANALYSIS.getAction()),
            Map.entry(" queries about carrier destination (do not confuse with ship) ", CARRIER_DESTINATION.getAction()),
            Map.entry(" queries about carrier fuel supply, jump range, fuel reserve, what is our tritium supply, tritium reserve (do not confuse with ship) ", CARRIER_TRITIUM_SUPPLY.getAction()),
            Map.entry(" queries about carrier finances, funded days of operation V ", CARRIER_STATUS.getAction()),
            Map.entry(" queries about carrier estimated time of arrival (do not confuse with ship) ", CARRIER_ETA.getAction()),
            Map.entry(" how far are we from the carrier ", DISTANCE_TO_CARRIER.getAction()),
            //Map.entry(" what missions do we have, are there any outstanding missions, do we have any active missions ", OUTSTANDING_MISSIONS.getAction()),
            Map.entry(" how many kills left on pirate missions, pirate massacre mission progress (these are pirate massacre specific missions only) ", PIRATE_MISSION_PROGRESS.getAction()),
            Map.entry(" queries about player stats, ranks, rank progress etc ", PLAYER_PROFILE_ANALYSIS.getAction()),
            Map.entry(" ship combat readiness, damage report, jump range, loadout, ship capabilities. Do we have fuel scoop equipped, is ship battle worthy, what class of ship is it ", SHIP_LOADOUT.getAction()),
            Map.entry(" what services are available at this station ", STATION_DETAILS.getAction()),
            Map.entry(" what can you do, what are your capabilities ", APP_CAPABILITIES.getAction()),
            Map.entry(" what is your name ", AI_DESIGNATION.getAction()),
            Map.entry(" how much do we have in bounties, how many bounties we have collected (in credits) ", TOTAL_BOUNTIES.getAction()),
            Map.entry(" how far are we from the bubble, how long will it take to get to the bubble ", DISTANCE_TO_BUBBLE.getAction()),
            Map.entry(" how far are we from last organic, bio sample, previous bioscan ", DISTANCE_TO_LAST_BIO_SAMPLE.getAction()),
            Map.entry(" what time is it ", TIME_IN_ZONE.getAction()),
            Map.entry(" run biome analysis, analyze biome for planet/moon X (parameter example: \"key\":\"2a\")", PLANET_BIOME_ANALYSIS.getAction()),
            Map.entry(" remind me about X ", REMINDER.getAction()),
            Map.entry(" what missions do we have, are there any outstanding missions, do we have any active missions, what station are we looking for, questions about missions that are not pirateMassacre related ", ANALYZE_MISSIONS.getAction()),
            Map.entry(" if nothing else matches use this ", GENERAL_CONVERSATION.getAction())
    );

    private AiCommandsAndQueries() {
    }

    public static AiCommandsAndQueries getInstance() {
        return INSTANCE;
    }

    public String getCommandMap() {
        StringBuilder sb = new StringBuilder();
        this.commandMap.forEach((s, s2) -> {
            sb.append("|").append(s).append(" -> ").append(s2).append(" \n");
        });
        return sb.toString();
    }

    public String getQueries() {
        StringBuilder sb = new StringBuilder();
        this.queryMap.forEach((s, s2) -> {
            sb.append("|").append(s).append(" -> ").append(s2).append(" \n");
        });
        return sb.toString();
    }
}