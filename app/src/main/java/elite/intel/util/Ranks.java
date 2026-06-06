package elite.intel.util;

import elite.intel.gameapi.journal.events.dto.RankAndProgressDto;
import elite.intel.session.PlayerSession;
import elite.intel.ui.i18n.MultiLingualTextProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;

/**
 * The Ranks class provides various static methods to manage and retrieve mappings
 * of different ranks, honorific titles, and their respective hierarchical levels.
 * These mappings include data for Imperial, Federation, Combat, Exobiology, Exploration,
 * and Trade ranks.
 */
public class Ranks {

    private static final Map<String, String> RANK_I18N_KEY_MAP = Map.ofEntries(
            // Imperial
            Map.entry("Outsider",             "ranks.imperial.outsider"),
            Map.entry("Serf",                 "ranks.imperial.serf"),
            Map.entry("Master",               "ranks.imperial.master"),
            Map.entry("Squire",               "ranks.imperial.squire"),
            Map.entry("Knight",               "ranks.imperial.knight"),
            Map.entry("Lord",                 "ranks.imperial.lord"),
            Map.entry("Baron",                "ranks.imperial.baron"),
            Map.entry("Viscount",             "ranks.imperial.viscount"),
            Map.entry("Count",                "ranks.imperial.count"),
            Map.entry("Earl",                 "ranks.imperial.earl"),
            Map.entry("Marquis",              "ranks.imperial.marquis"),
            Map.entry("Duke",                 "ranks.imperial.duke"),
            Map.entry("Prince",               "ranks.imperial.prince"),
            Map.entry("King",                 "ranks.imperial.king"),
            // Federation
            Map.entry("Recruit",              "ranks.federation.recruit"),
            Map.entry("Cadet",                "ranks.federation.cadet"),
            Map.entry("Midshipman",           "ranks.federation.midshipman"),
            Map.entry("Petty Officer",        "ranks.federation.pettyOfficer"),
            Map.entry("Chief Petty Officer",  "ranks.federation.chiefPettyOfficer"),
            Map.entry("Warrant Officer",      "ranks.federation.warrantOfficer"),
            Map.entry("Ensign",               "ranks.federation.ensign"),
            Map.entry("Lieutenant",           "ranks.federation.lieutenant"),
            Map.entry("Lieutenant Commander", "ranks.federation.lieutenantCommander"),
            Map.entry("Post Commander",       "ranks.federation.postCommander"),
            Map.entry("Post Captain",         "ranks.federation.postCaptain"),
            Map.entry("Rear Admiral",         "ranks.federation.rearAdmiral"),
            Map.entry("Vice Admiral",         "ranks.federation.viceAdmiral"),
            Map.entry("Admiral",              "ranks.federation.admiral")
    );

    /**
     * Returns the localized display name for the given English rank name sourced from the game journal.
     * Falls back to the original English name if no translation key is registered.
     * Returns {@code null} for {@code null}, blank, or {@code "none"} input so callers can filter it out.
     */
    public static String getLocalizedRankName(String englishRankName) {
        if (englishRankName == null || englishRankName.isBlank() || "none".equalsIgnoreCase(englishRankName)) {
            return null;
        }
        String key = RANK_I18N_KEY_MAP.get(englishRankName);
        return key != null ? getText(key) : englishRankName;
    }

    /**
     * Returns the honorific map. Military rank to Honorific mapping.
     * Values are resolved from the active UI language at call time.
     */
    public static HashMap<String, String> getHonorificMap() {
        HashMap<String, String> rankMap = new HashMap<>();

        //Imperial ranks
        rankMap.put("none",     getText("ranks.honorific.commander"));
        rankMap.put("Outsider", getText("ranks.honorific.outsider"));
        rankMap.put("Serf",     getText("ranks.honorific.serf"));
        rankMap.put("Master",   getText("ranks.honorific.master"));
        rankMap.put("Squire",   getText("ranks.honorific.squire"));
        rankMap.put("Knight",   getText("ranks.honorific.sir"));
        rankMap.put("Lord",     getText("ranks.honorific.myLord"));
        rankMap.put("Baron",    getText("ranks.honorific.myLord"));
        rankMap.put("Viscount", getText("ranks.honorific.myLord"));
        rankMap.put("Count",    getText("ranks.honorific.myLord"));
        rankMap.put("Earl",     getText("ranks.honorific.myLord"));
        rankMap.put("Marquis",  getText("ranks.honorific.myLord"));
        rankMap.put("Duke",     getText("ranks.honorific.yourGrace"));
        rankMap.put("Prince",   getText("ranks.honorific.yourHighness"));
        rankMap.put("King",     getText("ranks.honorific.yourMajesty"));

        //Federation ranks
        rankMap.put("Recruit",              getText("ranks.honorific.recruit"));
        rankMap.put("Cadet",                getText("ranks.honorific.cadet"));
        rankMap.put("Midshipman",           getText("ranks.honorific.midshipman"));
        rankMap.put("Petty Officer",        getText("ranks.honorific.po"));
        rankMap.put("Chief Petty Officer",  getText("ranks.honorific.chief"));
        rankMap.put("Warrant Officer",      getText("ranks.honorific.warrant"));
        rankMap.put("Ensign",               getText("ranks.honorific.ensign"));
        rankMap.put("Lieutenant",           getText("ranks.honorific.lieutenant"));
        rankMap.put("Lieutenant Commander", getText("ranks.honorific.commander"));
        rankMap.put("Post Commander",       getText("ranks.honorific.commander"));
        rankMap.put("Post Captain",         getText("ranks.honorific.captain"));
        rankMap.put("Rear Admiral",         getText("ranks.honorific.admiral"));
        rankMap.put("Vice Admiral",         getText("ranks.honorific.admiral"));
        rankMap.put("Admiral",              getText("ranks.honorific.admiral"));

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
        rankMap.put(9, "Elite 1");
        rankMap.put(10, "Elite 2");
        rankMap.put(11, "Elite 3");
        rankMap.put(12, "Elite 4");
        rankMap.put(13, "Elite 5");
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
        rankMap.put(9, "Elite 1");
        rankMap.put(10, "Elite 2");
        rankMap.put(11, "Elite 3");
        rankMap.put(12, "Elite 4");
        rankMap.put(13, "Elite 5");
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
        rankMap.put(9, "Elite 1");
        rankMap.put(10, "Elite 2");
        rankMap.put(11, "Elite 3");
        rankMap.put(12, "Elite 4");
        rankMap.put(13, "Elite 5");
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
        rankMap.put(9, "Elite 1");
        rankMap.put(10, "Elite 2");
        rankMap.put(11, "Elite 3");
        rankMap.put(12, "Elite 4");
        rankMap.put(13, "Elite 5");
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
            return chooseAtRandom(imperial, federation);
        }
    }

    private static @NonNull String chooseAtRandom(int imperial, int federation) {
        Random random = new Random();
        int choice = random.nextInt(2); // Returns 0 or 1
        if (choice == 0) {
            return getHonorificMap().get(getImperialRankMap().get(imperial));
        } else {
            return getHonorificMap().get(getFederationRankMap().get(federation));
        }
    }

    public static String getHighestRankAsString(Integer imperial, Integer federation) {
        if (imperial > federation) {
            return getImperialRankMap().get(imperial);
        } else if (federation > imperial) {
            return getFederationRankMap().get(federation);
        } else {
            return chooseAtRandom(imperial, federation);
        }
    }

    public static String getPlayerHonorific() {
        RankAndProgressDto rankDto = PlayerSession.getInstance().getRankAndProgressDto();
        return rankDto.getHonorific();
    }

    private static final Map<String, String> ALL_RANK_KEY_MAP;

    static {
        Map<String, String> m = new HashMap<>();
        // Honorifics (getHonorificMap values stored in RankAndProgressDto.honorific)
        m.put("Commander", "speech.commander");
        m.put("Outsider", "rank.honorific.outsider");
        m.put("Serf", "rank.honorific.serf");
        m.put("Master", "rank.honorific.master");
        m.put("Squire", "rank.honorific.squire");
        m.put("Sir", "rank.honorific.sir");
        m.put("My Lord", "rank.honorific.myLord");
        m.put("Your Grace", "rank.honorific.yourGrace");
        m.put("Your Highness", "rank.honorific.yourHighness");
        m.put("Your Majesty", "rank.honorific.yourMajesty");
        m.put("Recruit", "rank.honorific.recruit");
        m.put("Cadet", "rank.honorific.cadet");
        m.put("Midshipman", "rank.honorific.midshipman");
        m.put("PO", "rank.honorific.po");
        m.put("Chief", "rank.honorific.chief");
        m.put("Warrant", "rank.honorific.warrant");
        m.put("Ensign", "rank.honorific.ensign");
        m.put("Lieutenant", "rank.honorific.lieutenant");
        m.put("Captain", "rank.honorific.captain");
        m.put("Admiral", "rank.honorific.admiral");
        // Imperial rank names
        m.put("Knight", "rank.imperial.knight");
        m.put("Lord", "rank.imperial.lord");
        m.put("Baron", "rank.imperial.baron");
        m.put("Viscount", "rank.imperial.viscount");
        m.put("Count", "rank.imperial.count");
        m.put("Earl", "rank.imperial.earl");
        m.put("Marquis", "rank.imperial.marquis");
        m.put("Duke", "rank.imperial.duke");
        m.put("Prince", "rank.imperial.prince");
        m.put("King", "rank.imperial.king");
        // Federation rank names
        m.put("Petty Officer", "rank.federation.pettyOfficer");
        m.put("Chief Petty Officer", "rank.federation.chiefPettyOfficer");
        m.put("Warrant Officer", "rank.federation.warrantOfficer");
        m.put("Lieutenant Commander", "rank.federation.lieutenantCommander");
        m.put("Post Commander", "rank.federation.postCommander");
        m.put("Post Captain", "rank.federation.postCaptain");
        m.put("Rear Admiral", "rank.federation.rearAdmiral");
        m.put("Vice Admiral", "rank.federation.viceAdmiral");
        // Combat ranks
        m.put("Harmless", "rank.combat.harmless");
        m.put("Mostly Harmless", "rank.combat.mostlyHarmless");
        m.put("Novice", "rank.combat.novice");
        m.put("Competent", "rank.combat.competent");
        m.put("Expert", "rank.combat.expert");
        m.put("Dangerous", "rank.combat.dangerous");
        m.put("Deadly", "rank.combat.deadly");
        m.put("Elite", "rank.combat.elite");
        m.put("Elite 1", "rank.combat.elite1");
        m.put("Elite 2", "rank.combat.elite2");
        m.put("Elite 3", "rank.combat.elite3");
        m.put("Elite 4", "rank.combat.elite4");
        m.put("Elite 5", "rank.combat.elite5");
        // Trade ranks ("Entrepreneur" and "Elite*" reuse existing keys)
        m.put("Penniless", "rank.trade.penniless");
        m.put("Mostly Penniless", "rank.trade.mostlyPenniless");
        m.put("Peddler", "rank.trade.peddler");
        m.put("Dealer", "rank.trade.dealer");
        m.put("Merchant", "rank.trade.merchant");
        m.put("Broker", "rank.trade.broker");
        m.put("Entrepreneur", "rank.trade.entrepreneur");
        m.put("Tycoon", "rank.trade.tycoon");
        // Exploration ranks ("Elite*" reuse combat keys)
        m.put("Aimless", "rank.exploration.aimless");
        m.put("Mostly Aimless", "rank.exploration.mostlyAimless");
        m.put("Scout", "rank.exploration.scout");
        m.put("Surveyor", "rank.exploration.surveyor");
        m.put("Trailblazer", "rank.exploration.trailblazer");
        m.put("Pathfinder", "rank.exploration.pathfinder");
        m.put("Ranger", "rank.exploration.ranger");
        m.put("Pioneer", "rank.exploration.pioneer");
        // Exobiology ranks ("Knight" reuses imperial key, "Elite*" reuse combat keys)
        m.put("Directionless", "rank.exobiology.directionless");
        m.put("Mostly Directionless", "rank.exobiology.mostlyDirectionless");
        m.put("Compiler", "rank.exobiology.compiler");
        m.put("Cataloguer", "rank.exobiology.cataloguer");
        m.put("Taxonomist", "rank.exobiology.taxonomist");
        m.put("Ecologist", "rank.exobiology.ecologist");
        m.put("Geneticist", "rank.exobiology.geneticist");
        // Mercenary ranks
        m.put("Defenceless", "rank.mercenary.defenceless");
        m.put("Mostly Defenceless", "rank.mercenary.mostlyDefenceless");
        m.put("Rookie", "rank.mercenary.rookie");
        m.put("Soldier", "rank.mercenary.soldier");
        m.put("Gunslinger", "rank.mercenary.gunslinger");
        m.put("Warrior", "rank.mercenary.warrior");
        m.put("Gladiator", "rank.mercenary.gladiator");
        m.put("Deadeye", "rank.mercenary.deadeye");
        m.put("Elite I", "rank.mercenary.eliteI");
        m.put("Elite II", "rank.mercenary.eliteII");
        m.put("Elite III", "rank.mercenary.eliteIII");
        m.put("Elite VI", "rank.mercenary.eliteVI");
        m.put("Elite V", "rank.mercenary.eliteV");
        ALL_RANK_KEY_MAP = Collections.unmodifiableMap(m);
    }

    public static String localizeRankValue(String rank) {
        if (rank == null) return null;
        String key = ALL_RANK_KEY_MAP.get(rank);
        return key != null ? MultiLingualTextProvider.getText(key) : rank;
    }

    public static String getLocalizedPlayerHonorific() {
        String honorific = PlayerSession.getInstance().getRankAndProgressDto().getHonorific();
        return localizeRankValue(honorific);
    }

    public static String getLocalizedPlayerMilitaryRank() {
        String rank = PlayerSession.getInstance().getPlayerHighestMilitaryRank();
        return localizeRankValue(rank);
    }
}
