package elite.intel.util;

import elite.intel.gameapi.journal.events.dto.RankAndProgressDto;
import elite.intel.session.PlayerSession;

import java.util.HashMap;

/**
 * The Ranks class provides various static methods to manage and retrieve mappings
 * of different ranks, honorific titles, and their respective hierarchical levels.
 * These mappings include data for Imperial, Federation, Combat, Exobiology, Exploration,
 * and Trade ranks.
 */
public class Ranks {

    /**
     * Returns the honorific map. Military rank to Honorific mapping.
     *
     */
    public static HashMap<String, String> getHonorificMap() {
        HashMap<String, String> rankMap = new HashMap<>();

        //Imperial ranks
        rankMap.put("none", "Commander");
        rankMap.put("Outsider", "Outsider");
        rankMap.put("Serf", "Serf");
        rankMap.put("Master", "Master");
        rankMap.put("Squire", "Squire");
        rankMap.put("Knight", "Sir");
        rankMap.put("Lord", "My Lord");
        rankMap.put("Baron", "My Lord");
        rankMap.put("Viscount", "My Lord");
        rankMap.put("Count", "My Lord");
        rankMap.put("Earl", "My Lord");
        rankMap.put("Marquis", "My Lord");
        rankMap.put("Duke", "Your Grace");
        rankMap.put("Prince", "Your Highness");
        rankMap.put("King", "Your Majesty");

        //Federation ranks
        rankMap.put("Recruit", "Recruit");
        rankMap.put("Cadet", "Cadet");
        rankMap.put("Midshipman", "Midshipman");
        rankMap.put("Petty Officer", "PO");
        rankMap.put("Chief Petty Officer", "Chief");
        rankMap.put("Warrant Officer", "Warrant");
        rankMap.put("Ensign", "Ensign");
        rankMap.put("Lieutenant", "Lieutenant");
        rankMap.put("Lieutenant Commander", "Commander");
        rankMap.put("Post Commander", "Commander");
        rankMap.put("Post Captain", "Captain");
        rankMap.put("Rear Admiral", "Admiral");
        rankMap.put("Vice Admiral", "Admiral");
        rankMap.put("Admiral", "Admiral");

        return rankMap;
    }


    /**
     * Returns the imperial military rank map int to rank name mapping.
     *
     */
    public static HashMap<Integer, String> getImperialRankMap() {
        HashMap<Integer, String> rankMap = new HashMap<>();
        rankMap.put(0, "none");
        rankMap.put(1, "Outsider");
        rankMap.put(2, "Serf");
        rankMap.put(3, "Master");
        rankMap.put(4, "Squire");
        rankMap.put(5, "Knight");
        rankMap.put(6, "Lord");
        rankMap.put(7, "Baron");
        rankMap.put(8, "Viscount");
        rankMap.put(9, "Count");
        rankMap.put(10, "Earl");
        rankMap.put(11, "Marquis");
        rankMap.put(12, "Duke");
        rankMap.put(13, "Prince");
        rankMap.put(14, "King");
        return rankMap;
    }


    /**
     * Returns the federation honorific map rank name to honorific mapping.
     *
     */
    public static HashMap<String, String> getFederationHonorificMap() {
        HashMap<String, String> rankMap = new HashMap<>();
        rankMap.put("Recruit", "");
        rankMap.put("Cadet", "Cadet");
        rankMap.put("Midshipman", "Midshipman");
        rankMap.put("Petty Officer", "PO");
        rankMap.put("Chief Petty Officer", "Chief");
        rankMap.put("Warrant Officer", "Warrant");
        rankMap.put("Ensign", "Ensign");
        rankMap.put("Lieutenant", "Lieutenant");
        rankMap.put("Lieutenant Commander", "Commander");
        rankMap.put("Post Commander", "Commander");
        rankMap.put("Post Captain", "Captain");
        rankMap.put("Rear Admiral", "Admiral");
        rankMap.put("Vice Admiral", "Admiral");
        rankMap.put("Admiral", "Admiral");
        return rankMap;
    }


    /**
     * Returns the federation military rank map int to rank name mapping.
     *
     */
    public static HashMap<Integer, String> getFederationRankMap() {
        HashMap<Integer, String> rankMap = new HashMap<>();
        rankMap.put(0, "none");
        rankMap.put(1, "Recruit");
        rankMap.put(2, "Cadet");
        rankMap.put(3, "Midshipman");
        rankMap.put(4, "Petty Officer");
        rankMap.put(5, "Chief Petty Officer");
        rankMap.put(6, "Warrant Officer");
        rankMap.put(7, "Ensign");
        rankMap.put(8, "Lieutenant");
        rankMap.put(9, "Lieutenant Commander");
        rankMap.put(10, "Post Commander");
        rankMap.put(11, "Post Captain");
        rankMap.put(12, "Rear Admiral");
        rankMap.put(13, "Vice Admiral");
        rankMap.put(14, "Admiral");
        return rankMap;
    }


    /**
     * Returns the combat rank map int to rank name mapping.
     *
     */
    public static HashMap<Integer, String> getCombatRankMap() {
        HashMap<Integer, String> rankMap = new HashMap<>();
        rankMap.put(0, "Harmless");
        rankMap.put(1, "Mostly Harmless");
        rankMap.put(2, "Novice");
        rankMap.put(3, "Competent");
        rankMap.put(4, "Expert");
        rankMap.put(5, "Master");
        rankMap.put(6, "Dangerous");
        rankMap.put(7, "Deadly");
        rankMap.put(8, "Elite");
        rankMap.put(9, "Elite I");
        rankMap.put(10, "Elite II");
        rankMap.put(11, "Elite III");
        rankMap.put(12, "Elite IV");
        rankMap.put(13, "Elite V");
        return rankMap;
    }


    /**
     * Returns the exobiology rank map int to rank name mapping.
     *
     */
    public static HashMap<Integer, String> getExobiologyRankMap() {
        HashMap<Integer, String> rankMap = new HashMap<>();
        rankMap.put(0, "Directionless");
        rankMap.put(1, "Mostly Directionless");
        rankMap.put(2, "Compiler");
        rankMap.put(3, "Cataloguer");
        rankMap.put(4, "Taxonomist");
        rankMap.put(5, "Knight");
        rankMap.put(6, "Ecologist");
        rankMap.put(7, "Geneticist");
        rankMap.put(8, "Elite");
        rankMap.put(9, "Elite I");
        rankMap.put(10, "Elite II");
        rankMap.put(11, "Elite III");
        rankMap.put(12, "Elite IV");
        rankMap.put(13, "Elite V");
        return rankMap;
    }


    /**
     * Returns the exploration rank map int to rank name mapping.
     *
     */
    public static HashMap<Integer, String> getExplorationRankMap() {
        HashMap<Integer, String> rankMap = new HashMap<>();
        rankMap.put(0, "Aimless");
        rankMap.put(1, "Mostly Aimless");
        rankMap.put(2, "Scout");
        rankMap.put(3, "Surveyor");
        rankMap.put(4, "Trailblazer");
        rankMap.put(5, "Pathfinder");
        rankMap.put(6, "Ranger");
        rankMap.put(7, "Pioneer");
        rankMap.put(8, "Elite");
        rankMap.put(9, "Elite I");
        rankMap.put(10, "Elite II");
        rankMap.put(11, "Elite III");
        rankMap.put(12, "Elite IV");
        rankMap.put(13, "Elite V");
        return rankMap;
    }


    /**
     * Returns the trade rank map int to rank name mapping.
     */
    public static HashMap<Integer, String> getTradeRankMap() {
        HashMap<Integer, String> rankMap = new HashMap<>();
        rankMap.put(0, "Penniless");
        rankMap.put(1, "Mostly Penniless");
        rankMap.put(2, "Peddler");
        rankMap.put(3, "Dealer");
        rankMap.put(4, "Merchant");
        rankMap.put(5, "Broker");
        rankMap.put(6, "Entrepreneur");
        rankMap.put(7, "Tycoon");
        rankMap.put(8, "Elite");
        rankMap.put(9, "Elite I");
        rankMap.put(10, "Elite II");
        rankMap.put(11, "Elite III");
        rankMap.put(12, "Elite VI");
        rankMap.put(13, "Elite V");
        return rankMap;
    }


    public static HashMap<Integer, String> getMercenaryRankMap() {
        HashMap<Integer, String> rankMap = new HashMap<>();
        rankMap.put(0, "Defenceless");
        rankMap.put(1, "Mostly Defenceless");
        rankMap.put(2, "Rookie");
        rankMap.put(3, "Soldier");
        rankMap.put(4, "Gunslinger");
        rankMap.put(5, "Warrior");
        rankMap.put(6, "Entrepreneur");
        rankMap.put(7, "Gladiator");
        rankMap.put(8, "Deadeye");
        rankMap.put(9, "Elite I");
        rankMap.put(10, "Elite II");
        rankMap.put(11, "Elite III");
        rankMap.put(12, "Elite VI");
        rankMap.put(13, "Elite V");
        return rankMap;
    }


/**
 *
 * Defenceless	0	Dominator Suit Body Suit Livery (Bronze)
 * Mostly Defenceless	10,000,000	Dominator Suit Torso, Arms, and Legs Livery (Bronze)
 * Rookie	30,000,000	Dominator Suit Helmet Livery (Bronze)
 * Soldier	60,000,000	Dominator Suit Body Suit Livery (Silver)
 * Gunslinger	125,000,000	Dominator Suit Torso, Arms, and Legs Livery (Silver)
 * Warrior	350,000,000	Dominator Suit Helmet Livery (Silver)
 * Gladiator	520,000,000	Dominator Suit Body Suit Livery (Gold)
 * Deadeye	888,000,000	Dominator Suit Torso, Arms, and Legs Livery (Gold)
 * Elite
 * */


    /**
     * return honorific for imperial or federation depending on which rank is higher.
     */
    public static String getHonorific(int imperial, int federation) {
        if (imperial > federation) {
            return getHonorificMap().get(getImperialRankMap().get(imperial));

        } else if (federation > imperial) {
            return getHonorificMap().get(getFederationRankMap().get(federation));
        } else {
            return "Sir/Mam";
        }
    }

    public static String getHighestRankAsString(Integer imperial, Integer federation) {
        if (imperial > federation) {
            return getImperialRankMap().get(imperial);
        } else if (federation > imperial) {
            return getFederationRankMap().get(federation);
        } else {
            return "Sir/Mam"; //Default value.
        }
    }

    public static String getPlayerHonorific() {
        RankAndProgressDto rankDto = PlayerSession.getInstance().getRankAndProgressDto();
        return rankDto.getHonorific();
    }
}
