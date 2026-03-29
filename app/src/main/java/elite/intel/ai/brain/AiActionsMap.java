package elite.intel.ai.brain;

import elite.intel.session.Status;
import elite.intel.session.SystemSession;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static elite.intel.ai.brain.commons.AiEndPoint.CONNECTION_CHECK_COMMAND;
import static elite.intel.ai.brain.handlers.commands.Commands.*;
import static elite.intel.ai.brain.handlers.query.Queries.*;

public class AiActionsMap {

    private static final AiActionsMap INSTANCE = new AiActionsMap();
    private final SystemSession systemSession = SystemSession.getInstance();
    private final Status status = Status.getInstance();

    // Words too common to be useful for filtering
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "to", "of", "in", "on", "at", "by", "for",
            "with", "and", "or", "is", "are", "am", "be", "do", "does",
            "what", "where", "how", "which", "any", "our", "my", "me",
            "we", "us", "i", "you", "it", "this", "that", "get", "have",
            "has", "can", "could", "would", "should", "not", "no", "up",
            "here", "there", "some", "much", "many"
    );

    private AiActionsMap() {
    }

    public static AiActionsMap getInstance() {
        return INSTANCE;
    }

    private Map<String, String> buildActionsMap() {
        Map<String, String> map = new LinkedHashMap<>();

        // always available
        map.put("start listening, listen, listen up, pay attention, I'm talking to you", START_LISTENING.getAction());
        map.put("ignore me, do not monitor", IGNORE_ME.getAction());
        map.put("interrupt", INTERRUPT_TTS.getAction());
        map.put("switch to combat mode", ACTIVATE_COMBAT_MODE.getAction());
        map.put("switch to analysis mode", ACTIVATE_ANALYSIS_MODE.getAction());

        // navigation
        map.put("navigate to coordinates {lat:X, lon:Y}", NAVIGATE_TO_TARGET.getAction());
        map.put("navigate to active mission target, go to mission target {key:X}", NAVIGATE_TO_NEXT_MISSION.getAction());
        map.put("navigate to fleet carrier, go to carrier, head to carrier, return to carrier, take us to carrier", NAVIGATE_TO_CARRIER.getAction());
        map.put("navigate to landing zone, bearing to landing zone, heading to landing zone, find landing zone", GET_HEADING_TO_LZ.getAction());
        map.put("navigate to next trade stop, go to next trade stop", NAVIGATE_TO_NEXT_TRADE_STOP.getAction());
        map.put("navigate from memory, paste from memory", NAVIGATE_TO_ADDRESS_FROM_MEMORY.getAction());
        map.put("cancel navigation, abort navigation, clear route, stop navigation", NAVIGATION_OFF.getAction());
        map.put("select next system in route", TARGET_NEXT_ROUTE_SYSTEM.getAction());
        map.put("jump to hyperspace, engage jump, engage FSD, engage drive, hyperspace jump, enter hyperspace, lets go, next way point", JUMP_TO_HYPERSPACE.getAction());
        map.put("drop from supercruise, exit supercruise, disengage supercruise, drop out, leave supercruise, drop in", DROP_FROM_SUPER_CRUISE.getAction());
        map.put("enter supercruise, engage supercruise, go supercruise, supercruise", ENTER_SUPER_CRUISE.getAction());
        map.put("set home system", SET_HOME_SYSTEM.getAction());

        // speed / throttle
        map.put("stop engines, stop here, full stop, all stop, halt, kill engines, cut throttle, zero throttle, stop ship", SET_SPEED_ZERO.getAction());
        map.put("taxi to landing, taxi, auto land, autopilot landing", TAXI.getAction());
        map.put("quarter throttle, 25 percent, slow speed, one quarter", SET_SPEED25.getAction());
        map.put("half throttle, 50 percent, half speed", SET_SPEED50.getAction());
        map.put("three quarters throttle, 75 percent, three quarter speed", SET_SPEED75.getAction());
        map.put("full throttle, 100 percent, full speed, maximum speed, max throttle", SET_SPEED100.getAction());
        map.put("increase speed by {key:X}", INCREASE_SPEED_BY.getAction());
        map.put("decrease speed by {key:X}", DECREASE_SPEED_BY.getAction());
        map.put("set optimal speed, optimal approach speed, approach speed", SET_OPTIMAL_SPEED.getAction());

        // flight / ship systems
        map.put("deploy landing gear, gear down, lower landing gear, extend landing gear", DEPLOY_LANDING_GEAR.getAction());
        map.put("retract landing gear, gear up, raise landing gear, stow landing gear", RETRACT_LANDING_GEAR.getAction());
        map.put("request docking, dock at station, request landing, docking request, ask for docking, request parking, parking spot, request pad", REQUEST_DOCKING.getAction());
        map.put("open / close cargo scoop, deploy / retract cargo scoop, open / close cargo bay", TOGGLE_CARGO_SCOOP.getAction());
        map.put("night vision, nightvision, turn on night vision, turn off night vision ", NIGHT_VISION_ON_OFF.getAction());
        map.put("headlights, lights, turn off lights, turn on lights, ship lights, lights on, lights off ", LIGHTS_ON_OFF.getAction());
        map.put("drive assist, driving assist, SRV assist {state:true/false}", DRIVE_ASSIST.getAction());
        map.put("dismiss ship, send ship away, park ship, ship to orbit", DISMISS_SHIP.getAction());
        map.put("return to surface, recall ship, pick me up", RETURN_TO_SURFACE.getAction());

        // market / traders / brokers
        map.put("find raw material trader, raw trader, where to trade raw materials {key:X}", FIND_RAW_MATERIAL_TRADER.getAction());
        map.put("find encoded material trader, encoded trader, data trader {key:X}", FIND_ENCODED_MATERIAL_TRADER.getAction());
        map.put("find manufactured material trader, manufactured trader {key:X}", FIND_MANUFACTURED_MATERIAL_TRADER.getAction());
        map.put("find human tech broker, human technology broker {key:X}", FIND_HUMAN_TECHNOLOGY_BROKER.getAction());
        map.put("find guardian tech broker, guardian technology broker {key:X}", FIND_GUARDIAN_TECHNOLOGY_BROKER.getAction());
        map.put("find commodity, buy commodity, where to buy, find market {key:X, max_distance:Y}", FIND_COMMODITY.getAction());
        map.put("find nearest fleet carrier, nearest carrier", FIND_NEAREST_FLEET_CARRIER.getAction());

        // fleet carrier
        map.put("set carrier fuel reserve, carrier tritium reserve {key:X}", SET_CARRIER_FUEL_RESERVE.getAction());
        map.put("calculate fleet carrier route, plan carrier route, carrier jump route", CALCULATE_FLEET_CARRIER_ROUTE.getAction());
        map.put("enter carrier destination, set carrier destination, carrier destination", ENTER_FLEET_CARRIER_DESTINATION.getAction());
        map.put("clear fleet carrier route, cancel carrier route, abort carrier jump", CLEAR_FLEET_CARRIER_ROUTE.getAction());

        // trade
        map.put("calculate trade route", CALCULATE_TRADE_ROUTE.getAction());
        map.put("list trade route parameters", LIST_TRADE_ROUTE_PARAMETERS.getAction());
        map.put("cancel trade route", CLEAR_TRADE_ROUTE.getAction());
        map.put("monetize route", MONETIZE_ROUTE.getAction());
        map.put("change trade profile starting budget {key:X}", CHANGE_TRADE_PROFILE_SET_STARTING_BUDGET.getAction());
        map.put("change trade profile max stops {key:X}", CHANGE_TRADE_PROFILE_SET_MAX_NUMBER_OF_STOPS.getAction());
        map.put("change trade profile max distance {key:X}", CHANGE_TRADE_PROFILE_SET_MAX_DISTANCE_FROM_ENTRY.getAction());
        map.put("change trade profile allow prohibited cargo {state:true/false}", CHANGE_TRADE_PROFILE_SET_ALLOW_PROHIBITED_CARGO.getAction());
        map.put("change trade profile allow planetary port {state:true/false}", CHANGE_TRADE_PROFILE_SET_ALLOW_PLANETARY_PORT.getAction());
        map.put("change trade profile allow permit systems {state:true/false}", CHANGE_TRADE_PROFILE_SET_ALLOW_PERMIT_SYSTEMS.getAction());
        map.put("change trade profile allow strongholds {state:true/false}", CHANGE_TRADE_PROFILE_SET_ALLOW_STRONGHOLDS.getAction());

        // announcements / app settings
        map.put("toggle radio, radio traffic, radio transmissions {state:true/false}", SET_RADIO_TRANSMISSION_MODE.getAction());
        map.put("radar contact announcement {state:true/false}", SET_RADAR_CONTACT_ANNOUNCEMENT.getAction());
        map.put("discovery announcements {state:true/false}", DISCOVERY_ON_OFF.getAction());
        map.put("route announcements {state:true/false}", ROUTE_ON_OFF.getAction());
        map.put("disable all announcements", DISABLE_ALL_ANNOUNCEMENTS.getAction());
        map.put("set voice {key:X}", SET_AI_VOICE.getAction());
        map.put("list available voices", LIST_AVAILABLE_VOICES.getAction());
        map.put("clear cache", CLEAR_CACHE.getAction());
        map.put("clear reminders", CLEAR_REMINDERS.getAction());
        map.put("set reminder {key:X}", SET_REMINDER.getAction());

        if (!systemSession.useLocalQueryLlm()) {
            map.put("set personality {key:X}", SET_PERSONALITY.getAction());
            map.put("set cadence {key:X}", SET_CADENCE.getAction());
        }

        // UI panels
        map.put("activate", ACTIVATE.getAction());
        map.put("show, open or display transactions panel", SHOW_TRANSACTIONS.getAction());
        map.put("show, open or display contacts panel", SHOW_CONTACTS.getAction());
        map.put("show, open or display navigation panel", SHOW_NAVIGATION.getAction());
        map.put("show, open or display chat panel", SHOW_CHAT_PANEL.getAction());
        map.put("show, open or display inbox panel", SHOW_INBOX_PANEL.getAction());
        map.put("show, open or display social panel", SHOW_SOCIAL_PANEL.getAction());
        map.put("show, open or display history panel", SHOW_HISTORY_PANEL.getAction());
        map.put("show, open or display squadron panel", SHOW_SQUADRON.getAction());
        map.put("show, open or display commander panel", SHOW_COMMANDER_PANEL.getAction());
        map.put("show, open or display fighter panel", SHOW_FIGHTER_PANEL.getAction());
        map.put("show, open or display crew panel", SHOW_CREW.getAction());
        map.put("show, open or display home panel", SHOW_INTERNAL_PANEL.getAction());
        map.put("show, open or display modules panel", SHOW_MODULES_PANEL.getAction());
        map.put("show, open or display fire groups", SHOW_FIRE_GROUPS.getAction());
        map.put("show, open or display inventory panel", SHOW_INVENTORY_PANEL.getAction());
        map.put("show, open or display storage panel", SHOW_STORAGE_PANEL.getAction());
        map.put("show, open or display status panel", SHOW_STATUS_PANEL.getAction());
        map.put("show, open or display carrier management panel", DISPLAY_CARRIER_MANAGEMENT.getAction());
        map.put("show, open or display galaxy map", OPEN_GALAXY_MAP.getAction());
        map.put("show, open or display system map", OPEN_SYSTEM_MAP.getAction());
        map.put("exit close panel", EXIT_CLOSE.getAction());
        map.put("power to shields, max shields, boost shields", INCREASE_SHIELDS_POWER.getAction());
        map.put("power to engines, max engines, boost engines", INCREASE_ENGINES_POWER.getAction());
        map.put("power to weapons, max weapons, boost weapons", INCREASE_WEAPONS_POWER.getAction());

        // pirate massacre missions
        map.put("find hunting grounds {key:X}", FIND_HUNTING_GROUNDS.getAction());
        map.put("recon hunting ground", RECON_TARGET_SYSTEM.getAction());
        map.put("navigate to mission provider system", RECON_PROVIDER_SYSTEM.getAction());
        map.put("navigate to pirate mission provider", NAVIGATE_TO_PIRATE_MISSION_PROVIDER.getAction());
        map.put("ignore hunting ground", IGNORE_HUNTING_GROUND.getAction());
        map.put("confirm hunting ground", CONFIRM_HUNTING_GROUND.getAction());
        map.put("deploy hardpoints, weapons hot, combat ready, weapons free, weapons out, arm weapons, weapons ready", DEPLOY_HARDPOINTS.getAction());

        // vehicle deployment
        map.put("deploy SRV, launch SRV, send out SRV, drop SRV", DEPLOY_SRV.getAction());
        map.put("recover SRV, board ship, return SRV, retrieve SRV, SRV dock", RECOVER_SRV.getAction());
        map.put("deploy heat sink, launch heat sink, dump heat", DEPLOY_HEAT_SINK.getAction());
        map.put("equalize power, balance power, reset power, distribute power equally", RESET_POWER.getAction());
        map.put("retract hardpoints, weapons cold, weapons away, stand down, holster weapons, weapons down, safe weapons", RETRACT_HARDPOINTS.getAction());

        // science / mining / biology
        map.put("add mining target {key:X}", ADD_MINING_TARGET.getAction());
        map.put("remove mining target {key:X}", REMOVE_MINING_TARGET.getAction());
        map.put("clear mining targets", CLEAR_MINING_TARGETS.getAction());
        map.put("mining announcements {state:true/false}", MINING_ON_OFF.getAction());
        map.put("find brain trees {key:X, max_distance:Y}", FIND_BRAIN_TREES.getAction());
        map.put("find mining site, find mining location, find mining hotspot, find where to mine, find asteroid field {key:X, max_distance:Y}", FIND_MINING_SITE.getAction());
        map.put("find tritium mining site, find tritium field {key:X, max_distance:Y}", FIND_FLEET_CARRIER_FUEL_MINING_SITE.getAction());
        map.put("navigate to next bio sample, go to next sample, navigate to next organic, codex entry", NAVIGATE_TO_NEXT_BIO_SAMPLE.getAction());
        map.put("scan the system, open fss, full scan, honk, system scan, discovery scan, FSS, full spectrum scan, scan system", OPEN_FSS_AND_SCAN.getAction());
        map.put("find nearest vista genomics, find genomics, vista genomics", FIND_VISTA_GENOMICS.getAction());
        map.put("delete codex entry", DELETE_CODEX_ENTRY.getAction());
        map.put("clear codex entries", CLEAR_CODEX_ENTRIES.getAction());

        // combat
        map.put("target fsd {key:fsd}, target engines {key:drive}, target Power Distributor {key:power distributor} target power plant {key:powerplant}, target powerplant {key:powerplant}, target life support {key:life support}, ", TARGET_SUB_SYSTEM.getAction());
        map.put("target wingman 1, wingman alpha", TARGET_WINGMAN0.getAction());
        map.put("target wingman 2, wingman bravo", TARGET_WINGMAN1.getAction());
        map.put("target wingman 3, wingman charlie", TARGET_WINGMAN2.getAction());
        map.put("wing nav lock, lock wingman nav, follow wingman", WING_NAV_LOCK.getAction());
        map.put("priority target, target highest threat, target most dangerous, select hostile, next enemy, select enemy", SELECT_HIGHEST_THREAT.getAction());

        // fighter
        if (status.isInMainShip()) {
            map.put("deploy fighter, launch fighter, send out fighter", DEPLOY_FIGHTER.getAction());
            map.put("order fighter defend ship, fighter defend, fighter defensive", FIGHTER_REQUEST_DEFENSIVE_BEHAVIOUR.getAction());
            map.put("order fighter attack my target, fighter attack, sic fighter on target", FIGHTER_REQUEST_FOCUS_TARGET.getAction());
            map.put("order fighter hold fire, fighter cease fire, fighter stand down", FIGHTER_REQUEST_HOLD_FIRE.getAction());
            map.put("order fighter return to ship, fighter dock, recall fighter", FIGHTER_REQUEST_REQUEST_DOCK.getAction());
        }

        // queries
        if (!systemSession.useLocalQueryLlm()) {
            //map.put("help with topic {key:X}", HELP.getAction());
        }
        map.put("check key bindings, missing key bindings, unbound keys, keyboard bindings, keybind check, missing bindings", KEY_BINDINGS_ANALYSIS.getAction());
        map.put("bio scan progress, biology scan progress, organics scanned, how many samples, bio signals in system, biosignals, biological signals, organics in system, which planets have bio signals", BIO_SAMPLE_IN_STAR_SYSTEM.getAction());
        map.put("exobiology samples, biology samples, organics at location, what organisms, what's left to scan, remaining organisms, samples left, organisms remaining, exobiology progress, scan remaining", EXOBIOLOGY_SAMPLES.getAction());
        map.put("stellar objects, planets in system, is planet or moon landable, bodies in system, what planets, how many planets, system bodies, stellar bodies, what's in this system", QUERY_STELLAR_OBJETS.getAction());
        map.put("signals in system, FSS signals, mining hot spots, resource extraction sites, what signals, conflict zones, emissions, unidentified signals, system signals, detected signals, anomalous signals", QUERY_STELLAR_SIGNALS.getAction());
        map.put("geo signals, geological signals, volcanic signals, geological activity, volcanic activity, geology in system", QUERY_GEO_SIGNALS.getAction());
        map.put("stations in system, what stations, nearby stations, star ports, space stations, docking available", QUERY_STATIONS.getAction());
        map.put("fleet carriers in system, carriers in system, how many carriers, fleet carriers here, any carriers nearby", QUERY_CARRIERS.getAction());
        map.put("system security, faction control, who controls, power struggle, security level, who owns this system, dominant faction, controlling power", SYSTEM_SECURITY_ANALYSIS.getAction());
        map.put("trade profile, trade settings, trading parameters, trade configuration, trading criteria", TRADE_PROFILE_ANALYSIS.getAction());
        map.put("distance to stellar object, how far to body, distance to planet {key:X}, range to planet, how far to moon, how far to station, range to body", DISTANCE_TO_BODY.getAction());
        map.put("last scan, what did we scan, last scanned object, most recent scan, latest scan, recent body scan, what did i last scan", LAST_SCAN_ANALYSIS.getAction());
        map.put("material inventory {key:X}, how many items {key:X}, how many guardian item {key:X}, how much material {key:X}, do we have material {key:X}, how much {key:X} do we have, do we have any {key:X}, engineering material {key:X}, raw material {key:X}, manufactured material {key:X}, encoded material {key:X}, material stock {key:X}", MATERIALS_INVENTORY.getAction());
        map.put("planet materials, materials here, what materials on this planet, surface materials, what materials are here, material deposits, minerals on planet", PLANET_MATERIALS.getAction());
        map.put("exploration profits, discovery profits, how much exploration worth, exploration value, scan value, mapping profit, exobiology value, scan earnings, what are scans worth", EXPLORATION_PROFITS.getAction());
        map.put("current location, where are we, our position, what system are we in, where am i, our coordinates, current system, what planet are we at, our current position", CURRENT_LOCATION.getAction());
        map.put("fuel status, fuel level, how much fuel, fuel remaining, are we low on fuel, fuel check, do we have enough fuel, tank level, fuel gauge", SHIP_FUEL_STATUS.getAction());
        map.put("FSD target, analyze destination, what star are we targeting, analyze jump target, info on next jump", FSD_TARGET_ANALYSIS.getAction());
        map.put("plotted route, fuel at next stop, is fuel available at next way point, route analysis, current route, navigation route, jumps remaining, jumps left, how many jumps, route progress, next star scoopable, fuel stop", PLOTTED_ROUTE_ANALYSIS.getAction());
        map.put("carrier route, carrier navigation, carrier jump route, carrier trip, carrier journey, carrier travel plan", CARRIER_ROUTE_ANALYSIS.getAction());
        map.put("trade route, trading route, current trade plan, what are we trading, our trading plan, trading schedule, trade legs", TRADE_ROUTE_ANALYSIS.getAction());
        map.put("outfitting, ship upgrades, modules available, what modules at station, available modules, available equipment, buy modules, ship parts, station equipment", LOCAL_OUTFITTING.getAction());
        map.put("shipyard, ships for sale, what ships at station, buy a ship, available ships, ships to buy, new ship", LOCAL_SHIPYARD.getAction());
        map.put("cargo hold, what are we carrying, cargo contents, commodities on board, cargo manifest, what are we hauling, hold contents, commodity inventory, trading commodities", CARGO_HOLD_CONTENTS.getAction());
        map.put("carrier destination, where is carrier going, carrier next jump, where is carrier headed, carrier heading, carrier endpoint, carrier final destination", CARRIER_DESTINATION.getAction());
        map.put("carrier tritium, carrier fuel, how much tritium, tritium supply, tritium level, carrier fuel level, tritium reserve, carrier tritium status", CARRIER_TRITIUM_SUPPLY.getAction());
        map.put("carrier status, carrier finances, carrier balance, carrier overview, carrier health, how is carrier doing, carrier condition", CARRIER_STATUS.getAction());
        map.put("carrier ETA, when does carrier arrive, how long until carrier, carrier arrival time, carrier arrival, when does carrier jump, carrier jump time", CARRIER_ETA.getAction());
        map.put("distance to carrier, how far is carrier, where is our carrier, range to carrier, carrier proximity, how far away is carrier", DISTANCE_TO_CARRIER.getAction());
        map.put("pirate mission, kill count, how many kills, massacre mission progress, kills remaining, massacre progress, pirates remaining, pirate kills, bounty hunt progress", PIRATE_MISSION_PROGRESS.getAction());
        map.put("player profile, our ranks, combat rank, trade rank, exploration rank, commander stats, pilot rank, our ranking, commander profile, what rank are we", PLAYER_PROFILE_ANALYSIS.getAction());
        map.put("ship loadout, ship modules, combat readiness report, ship equipment, ship specs, what am I flying, what are we equipped with, do you have, is it equipped, shield generator, hull reinforcement, sensors, thrusters, frameshift, fuel scoop, installed", SHIP_LOADOUT.getAction());
        map.put("station details, station services, what services here, what does station offer, station info, station facilities, what's at this station, services available", STATION_DETAILS.getAction());
        map.put("app capabilities, what can you do, your features, list capabilities, what commands do you know, your abilities, what can you help with", APP_CAPABILITIES.getAction());
        map.put("AI designation, what is your name, who are you, what are you called, ai name, system designation", AI_DESIGNATION.getAction());
        map.put("bounties, total bounties, bounty collected, how much in bounties, bounty earnings, credits from bounties, bounty credits", TOTAL_BOUNTIES.getAction());
        map.put("distance to bubble, how far from bubble, how far from inhabited space, distance from inhabited space, how far from civilization, range from human space", DISTANCE_TO_BUBBLE.getAction());
        map.put("distance to last bio sample, how far to sample, how far to last organism, range to bio sample, how far to previous organism, navigate to bio sample", DISTANCE_TO_LAST_BIO_SAMPLE.getAction());
        map.put("current time, what time is it, time on earth, galactic time, utc time, what's the time, real time", TIME_IN_ZONE.getAction());
        map.put("planet biome, biome analysis, what biome {key:X}, planetary biome, atmosphere analysis, what life is here, biome type", PLANET_BIOME_ANALYSIS.getAction());
        map.put("reminder, what was the reminder, destination reminder, any reminders, recall reminder, what did we set as reminder", REMINDER.getAction());
        map.put("local stations, stations and settlements, outposts in system, space stations here, star ports, all stations in system, settlements in system", ANALYZE_LOCAL_STATIONS.getAction());
        map.put("active missions, current missions, mission log, what missions, mission status, what are our missions, ongoing missions, mission board", ANALYZE_MISSIONS.getAction());
        map.put("general conversation", GENERAL_CONVERSATION.getAction());

        // machine-only
        map.put(CONNECTION_CHECK_COMMAND, CONNECTION_CHECK.getAction());

        return map;
    }

    /**
     * Returns the full action list formatted for the LLM prompt, optionally
     * filtered to only those actions whose key contains a word from the
     * normalized user input.
     * <p>
     * Falls back to the full map if fewer than MIN_MATCH_THRESHOLD actions match,
     * to avoid over-filtering on ambiguous or low-signal input.
     */
    public String getActions(String normalizedInput) {
        Map<String, String> full = buildActionsMap();
        Map<String, String> filtered = filterByInput(normalizedInput, full);
        Map<String, String> source = !filtered.isEmpty() ? filtered : full;
        return formatActions(source);
    }

    /**
     * Returns the full unfiltered action list (used by capability queries).
     */
    public String getActions() {
        return formatActions(buildActionsMap());
    }

    private Map<String, String> filterByInput(String normalizedInput, Map<String, String> full) {
        if (normalizedInput == null || normalizedInput.isBlank()) return full;

        Set<String> inputWords = Arrays.stream(normalizedInput.toLowerCase().split("\\W+"))
                .filter(w -> w.length() > 2)
                .filter(w -> !STOP_WORDS.contains(w))
                .collect(Collectors.toSet());

        if (inputWords.isEmpty()) return full;

        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : full.entrySet()) {
            String keyLower = entry.getKey().toLowerCase();
            for (String word : inputWords) {
                if (keyLower.contains(word)) {
                    result.put(entry.getKey(), entry.getValue());
                    break;
                }
            }
        }
        return result;
    }

    private String formatActions(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("ACTIONS (use ONLY these exact action names):\n\n");
        map.forEach((key, action) ->
                sb.append("  ").append(action).append(" ← ").append(key).append("\n"));
        return sb.toString();
    }
}
