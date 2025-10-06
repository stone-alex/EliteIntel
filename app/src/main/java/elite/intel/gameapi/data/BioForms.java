package elite.intel.gameapi.data;

import java.util.HashMap;
import java.util.Map;

import static elite.intel.util.StringUtls.capitalizeWords;

/**
 * Static utility for looking up known exobiology forms based on public wiki data.
 * Provides genus/species details and biome descriptions.
 */
public class BioForms {
    public record BioDetails(long creditValue, Integer colonyRange, String atmosphere, Double minTemp, Double maxTemp, String volcanism) {}

    private static final Map<String, String> GENUS_TO_BIOME = new HashMap<>();
    private static final Map<String, Map<String, BioDetails>> GENUS_TO_SPECIES = new HashMap<>();

    static {
        // Aleoida
        GENUS_TO_BIOME.put("Aleoida", "Found on Rocky or High Metal Content worlds with maximum gravity of 0.27. Has a 150m Colony Range.");
        Map<String, BioDetails> aleoidaSpecies = new HashMap<>();
        aleoidaSpecies.put("Arcus", new BioDetails(7252500, 150, "CO2-Rich or CO2", 175.0, 180.0, null));
        aleoidaSpecies.put("Coronamus", new BioDetails(6284600, 150, "CO2-Rich or CO2", 180.0, 190.0, null));
        aleoidaSpecies.put("Gravis", new BioDetails(12934900, 150, "CO2-Rich or CO2", 190.0, 195.0, null));
        aleoidaSpecies.put("Laminiae", new BioDetails(3385200, 150, "Ammonia", null, null, null));
        aleoidaSpecies.put("Spica", new BioDetails(3385200, 150, "Ammonia", null, null, null));
        GENUS_TO_SPECIES.put("Aleoida", aleoidaSpecies);

        // Amphora Plant
        GENUS_TO_BIOME.put("Amphora Plant", "Colony range of 100m. Requires Type A parent star and specific system features like Earth-Like Worlds, Ammonia Worlds, or Gas Giants with life. No Atmosphere Only. Only found greater than 12,000 Ls from the parent star.");
        Map<String, BioDetails> amphoraSpecies = new HashMap<>();
        amphoraSpecies.put("Amphora Plant", new BioDetails(3626400, 100, "No Atmosphere Only", null, null, null));
        GENUS_TO_SPECIES.put("Amphora Plant", amphoraSpecies);

        // Anemone
        GENUS_TO_BIOME.put("Anemone", "Colony Range of 100m. Typically found on planets with no atmosphere, but have been found on planets with thin atmosphere. Specific star class and luminosity requirements vary by species.");
        Map<String, BioDetails> anemoneSpecies = new HashMap<>();
        anemoneSpecies.put("Blatteum Bioluminescent", new BioDetails(1499900, 100, null, null, null, null));
        anemoneSpecies.put("Croceum", new BioDetails(3399800, 100, null, null, null, null));
        anemoneSpecies.put("Luteolum", new BioDetails(1499900, 100, null, null, null, null));
        anemoneSpecies.put("Prasinum Bioluminescent", new BioDetails(1499900, 100, null, null, null, null));
        anemoneSpecies.put("Puniceum", new BioDetails(1499900, 100, null, null, null, null));
        anemoneSpecies.put("Roseum", new BioDetails(1499900, 100, null, null, null, null));
        anemoneSpecies.put("Roseum Bioluminescent", new BioDetails(1499900, 100, null, null, null, null));
        anemoneSpecies.put("Rubeum Bioluminescent", new BioDetails(1499900, 100, null, null, null, null));
        GENUS_TO_SPECIES.put("Anemone", anemoneSpecies);

        // Bacterium
        GENUS_TO_BIOME.put("Bacterium", "Has a colony range of 500m and can appear on any planet type.");
        Map<String, BioDetails> bacteriumSpecies = new HashMap<>();
        bacteriumSpecies.put("Nebulus", new BioDetails(9116600, 500, "Helium", null, null, "None"));
        bacteriumSpecies.put("Acies", new BioDetails(1000000, 500, "Neon or Neon-Rich", null, null, "None"));
        bacteriumSpecies.put("Omentum", new BioDetails(4638900, 500, "Neon or Neon-Rich", null, null, "Nitrogen or Ammonia"));
        bacteriumSpecies.put("Scopulum", new BioDetails(8633800, 500, "Neon or Neon-Rich", null, null, "Carbon or Methane"));
        bacteriumSpecies.put("Verrata", new BioDetails(3897000, 500, "Neon or Neon-Rich", null, null, "Water"));
        bacteriumSpecies.put("Bullaris", new BioDetails(1152500, 500, "Methane or Methane-Rich", null, null, "None"));
        bacteriumSpecies.put("Vesicula", new BioDetails(1000000, 500, "Argon or Argon-Rich", null, null, "None"));
        bacteriumSpecies.put("Informem", new BioDetails(8418000, 500, "Nitrogen", null, null, "None"));
        bacteriumSpecies.put("Volu", new BioDetails(7774700, 500, "Oxygen", null, null, "None"));
        bacteriumSpecies.put("Alcyoneum", new BioDetails(1658500, 500, "Ammonia", null, null, "None"));
        bacteriumSpecies.put("Aurasus", new BioDetails(1000000, 500, "CO2-Rich or CO2", null, null, "None"));
        bacteriumSpecies.put("Cerbrus", new BioDetails(1689800, 500, "Water, SO2", null, null, "None"));
        bacteriumSpecies.put("Tela", new BioDetails(1949000, 500, "Any", null, null, "None, Helium, Iron, or Silicate"));
        GENUS_TO_SPECIES.put("Bacterium", bacteriumSpecies);

        // Bark Mound
        GENUS_TO_BIOME.put("Bark Mound", "Has a colony distance of 100m. Have a special property where they have growths that can be harvested for materials. Found within 150 Ly of the center of a nebula. No atmosphere only.");
        Map<String, BioDetails> barkMoundSpecies = new HashMap<>();
        barkMoundSpecies.put("Bark Mound", new BioDetails(1471900, 100, "None only", null, null, null));
        GENUS_TO_SPECIES.put("Bark Mound", barkMoundSpecies);

        // Brain Tree
        GENUS_TO_BIOME.put("Brain Tree", "Has a colony distance of 100m. Excepting the species of Roseum, will only be found in systems with an Earth-Like world or a Gas Giant with Water-based Life. Temperature ranges vary by species.");
        Map<String, BioDetails> brainTreeSpecies = new HashMap<>();
        brainTreeSpecies.put("Aureum", new BioDetails(3565100, 100, null, 300.0, 500.0, null));
        brainTreeSpecies.put("Gypseeum", new BioDetails(3565100, 100, null, 200.0, 300.0, null));
        brainTreeSpecies.put("Lindigoticum", new BioDetails(3565100, 100, null, 300.0, 500.0, null));
        brainTreeSpecies.put("Lividum", new BioDetails(1593700, 100, null, 300.0, 500.0, null));
        brainTreeSpecies.put("Ostrinum", new BioDetails(3565100, 100, null, null, null, null));
        brainTreeSpecies.put("Puniceum", new BioDetails(3565100, 100, null, null, null, null));
        brainTreeSpecies.put("Roseum", new BioDetails(1593700, 100, null, 200.0, 500.0, null));
        brainTreeSpecies.put("Viride", new BioDetails(1593700, 100, null, 100.0, 270.0, null));
        GENUS_TO_SPECIES.put("Brain Tree", brainTreeSpecies);

        // Cactoida
        GENUS_TO_BIOME.put("Cactoida", "Prefer hilly terrain on High Metal Content and Rocky worlds. Colony Range 300m.");
        Map<String, BioDetails> cactoidaSpecies = new HashMap<>();
        cactoidaSpecies.put("Cortexum", new BioDetails(3667600, 300, "CO2-Rich or CO2", null, null, null));
        cactoidaSpecies.put("Lapis", new BioDetails(2483600, 300, "Ammonia", null, null, null));
        cactoidaSpecies.put("Peperatis", new BioDetails(2483600, 300, "Ammonia", null, null, null));
        cactoidaSpecies.put("Pullulanta", new BioDetails(3667600, 300, "CO2-Rich or CO2", 180.0, 195.0, null));
        cactoidaSpecies.put("Vermis", new BioDetails(16202800, 300, "Water", null, null, null));
        GENUS_TO_SPECIES.put("Cactoida", cactoidaSpecies);

        // Clypeus
        GENUS_TO_BIOME.put("Clypeus", "Found only on Rocky or High Metal Content worlds with a mean temperature superior to 190K, water or carbon dioxide atmosphere, and a maximum gravity of 0.27. Colony Range 150m.");
        Map<String, BioDetails> clypeusSpecies = new HashMap<>();
        clypeusSpecies.put("Lacrimam", new BioDetails(8418000, 150, "Water or CO2", 190.0, null, null));
        clypeusSpecies.put("Margaritus", new BioDetails(11873200, 150, "Water or CO2", 190.0, null, null));
        clypeusSpecies.put("Speculumi", new BioDetails(16202800, 150, "Water or CO2", 190.0, null, null));
        GENUS_TO_SPECIES.put("Clypeus", clypeusSpecies);

        // Concha
        GENUS_TO_BIOME.put("Concha", "Found on rocky or hilly terrain. Colony Range 150m.");
        Map<String, BioDetails> conchaSpecies = new HashMap<>();
        conchaSpecies.put("Aureolas", new BioDetails(7774700, 150, "Ammonia", null, null, null));
        conchaSpecies.put("Biconcavis", new BioDetails(16777215, 150, "Nitrogen", null, null, null));
        conchaSpecies.put("Labiata", new BioDetails(2352400, 150, "CO2-Rich or CO2", null, 190.0, null));
        conchaSpecies.put("Renibus", new BioDetails(4572400, 150, "Water, Water-Rich, CO2-Rich, or CO2", 180.0, 195.0, null));
        GENUS_TO_SPECIES.put("Concha", conchaSpecies);

        // Crystalline Shard
        GENUS_TO_BIOME.put("Crystalline Shard", "Colony Range of 100m. A unique biological that produces high-grade materials matching the planet's composition, which can be mined with an SRV. Requires system with Earth-Like World, Ammonia World, or Gas Giant with life. No atmosphere only. Parent Star A, F, G, K, M, or S. Only found greater than 12,000 Ls from the parent star.");
        Map<String, BioDetails> crystallineShardSpecies = new HashMap<>();
        crystallineShardSpecies.put("Crystalline Shard", new BioDetails(3626400, 100, "None only", null, null, null));
        GENUS_TO_SPECIES.put("Crystalline Shard", crystallineShardSpecies);

        // Electricae
        GENUS_TO_BIOME.put("Electricae", "Colony Range of 1000m. Located on plains. Found on Icy planets with a Helium, Neon, or Argon atmosphere and a maximum gravity of 0.27.");
        Map<String, BioDetails> electricaeSpecies = new HashMap<>();
        electricaeSpecies.put("Pluma", new BioDetails(6284600, 1000, "Helium, Neon, or Argon", null, null, null));
        electricaeSpecies.put("Radialem", new BioDetails(6284600, 1000, "Helium, Neon, or Argon", null, null, null));
        GENUS_TO_SPECIES.put("Electricae", electricaeSpecies);

        // Fonticulua
        GENUS_TO_BIOME.put("Fonticulua", "Colony Range of 500m. Frequently found on flat ground near craters and rocky terrain.");
        Map<String, BioDetails> fonticuluaSpecies = new HashMap<>();
        fonticuluaSpecies.put("Campestris", new BioDetails(1000000, 500, "Argon", null, null, null));
        fonticuluaSpecies.put("Digitos", new BioDetails(1804100, 500, "Methane", null, null, null));
        fonticuluaSpecies.put("Fluctus", new BioDetails(16777215, 500, "Oxygen", null, null, null));
        fonticuluaSpecies.put("Lapida", new BioDetails(3111000, 500, "Nitrogen", null, null, null));
        fonticuluaSpecies.put("Segmentatus", new BioDetails(19010800, 500, "Neon, Neon-Rich", null, null, null));
        fonticuluaSpecies.put("Upupam", new BioDetails(5727600, 500, "Argon Rich", null, null, null));
        GENUS_TO_SPECIES.put("Fonticulua", fonticuluaSpecies);

        // Frutexa
        GENUS_TO_BIOME.put("Frutexa", "Found in mountains on flat or hilly areas, often appear near other species; recommended to use night vision and foot recon. Colony Range 150m.");
        Map<String, BioDetails> frutexaSpecies = new HashMap<>();
        frutexaSpecies.put("Acus", new BioDetails(7774700, 150, "CO2-Rich or CO2", null, 195.0, null));
        frutexaSpecies.put("Collum", new BioDetails(1639800, 150, "SO2", null, null, null));
        frutexaSpecies.put("Fera", new BioDetails(1632500, 150, "CO2-Rich or CO2", null, 195.0, null));
        frutexaSpecies.put("Flabellum", new BioDetails(1808900, 150, "Ammonia", null, null, null));
        frutexaSpecies.put("Flammasis", new BioDetails(10326000, 150, "Ammonia", null, null, null));
        frutexaSpecies.put("Metallicum", new BioDetails(1632500, 150, "Ammonia, CO2-Rich or CO2", null, 195.0, null));
        frutexaSpecies.put("Sponsae", new BioDetails(5988000, 150, "Water, Water-Rich", null, null, null));
        GENUS_TO_SPECIES.put("Frutexa", frutexaSpecies);

        // Fumerola
        GENUS_TO_BIOME.put("Fumerola", "Colony Range of 100m. Develop only on fumeroles, requiring planets with volcanism and geological features.");
        Map<String, BioDetails> fumerolaSpecies = new HashMap<>();
        fumerolaSpecies.put("Aquatis", new BioDetails(6284600, 100, null, null, null, "Water"));
        fumerolaSpecies.put("Carbosis", new BioDetails(6284600, 100, null, null, null, "Methane, CO2"));
        fumerolaSpecies.put("Extremus", new BioDetails(16202800, 100, null, null, null, "Silicate, Iron or Rocky"));
        fumerolaSpecies.put("Nitris", new BioDetails(7500900, 100, null, null, null, "Nitrogen or Ammonia"));
        GENUS_TO_SPECIES.put("Fumerola", fumerolaSpecies);

        // Fungoida
        GENUS_TO_BIOME.put("Fungoida", "Found in mountainous areas on the flatter portions of terrain, often near Frutexa. Colony Range 300m.");
        Map<String, BioDetails> fungoidaSpecies = new HashMap<>();
        fungoidaSpecies.put("Bullarum", new BioDetails(3703200, 300, "Argon, Argon-Rich", null, null, null));
        fungoidaSpecies.put("Gelata", new BioDetails(3330300, 300, "CO2-Rich or CO2, Water", 180.0, 195.0, null));
        fungoidaSpecies.put("Setisis", new BioDetails(1670100, 300, "Ammonia, Methane, Methane-Rich", null, null, null));
        fungoidaSpecies.put("Stabitis", new BioDetails(2680300, 300, "CO2-Rich or CO2, Water", 180.0, 195.0, null));
        GENUS_TO_SPECIES.put("Fungoida", fungoidaSpecies);

        // Osseus
        GENUS_TO_BIOME.put("Osseus", "Found only on rocky terrain (growing out of rocks, not where there are many rocks present). Colony Range 800m.");
        Map<String, BioDetails> osseusSpecies = new HashMap<>();
        osseusSpecies.put("Cornibus", new BioDetails(1483000, 800, "CO2-Rich or CO2", 180.0, 195.0, null));
        osseusSpecies.put("Discus", new BioDetails(12934900, 800, "Water, Water-Rich", null, null, null));
        osseusSpecies.put("Fractus", new BioDetails(4027800, 800, "CO2-Rich or CO2", 180.0, 190.0, null));
        osseusSpecies.put("Pellebantus", new BioDetails(9739000, 800, "CO2-Rich or CO2", 190.0, 195.0, null));
        osseusSpecies.put("Pumice", new BioDetails(3156300, 800, "Methane, Methane-rich, Argon, Argon-rich, Nitrogen", null, null, null));
        osseusSpecies.put("Spiralis", new BioDetails(2404700, 800, "Ammonia", null, null, null));
        GENUS_TO_SPECIES.put("Osseus", osseusSpecies);

        // Recepta
        GENUS_TO_BIOME.put("Recepta", "Colony Range of 150m. Occurs only on planets with an atmosphere of Sulphur Dioxide and gravity less than 0.27.");
        Map<String, BioDetails> receptaSpecies = new HashMap<>();
        receptaSpecies.put("Conditivus", new BioDetails(14313700, 150, "SO2", null, null, null));
        receptaSpecies.put("Deltahedronix", new BioDetails(16202800, 150, "SO2", null, null, null));
        receptaSpecies.put("Umbrux", new BioDetails(12934900, 150, "SO2", null, null, null));
        GENUS_TO_SPECIES.put("Recepta", receptaSpecies);

        // Sinuous Tuber
        GENUS_TO_BIOME.put("Sinuous Tuber", "Colony Range of 100m. Occurs only on planets with volcanism but no atmosphere. Appears more common in the galactic core.");
        Map<String, BioDetails> sinuousTuberSpecies = new HashMap<>();
        sinuousTuberSpecies.put("Albidum", new BioDetails(3425600, 100, "None", null, null, "Any"));
        sinuousTuberSpecies.put("Blatteum", new BioDetails(1514500, 100, "None", null, null, "Any"));
        sinuousTuberSpecies.put("Caeruleum", new BioDetails(1514500, 100, "None", null, null, "Any"));
        sinuousTuberSpecies.put("Lindigoticum", new BioDetails(1514500, 100, "None", null, null, "Any"));
        sinuousTuberSpecies.put("Prasinum", new BioDetails(1514500, 100, "None", null, null, "Any"));
        sinuousTuberSpecies.put("Roseus", new BioDetails(1514500, 100, "None", null, null, "Silicate Magma"));
        sinuousTuberSpecies.put("Violaceum", new BioDetails(1514500, 100, "None", null, null, "Any"));
        sinuousTuberSpecies.put("Viride", new BioDetails(1514500, 100, "None", null, null, "Any"));
        GENUS_TO_SPECIES.put("Sinuous Tuber", sinuousTuberSpecies);

        // Stratum
        GENUS_TO_BIOME.put("Stratum", "Colony Range of 500m.");
        Map<String, BioDetails> stratumSpecies = new HashMap<>();
        stratumSpecies.put("Araneamus", new BioDetails(2448900, 500, "SO2", 165.0, null, null));
        stratumSpecies.put("Cucumisis", new BioDetails(16777215, 500, "SO2, CO2-Rich or CO2", 190.0, null, null));
        stratumSpecies.put("Excutitus", new BioDetails(2448900, 500, "SO2, CO2-Rich or CO2", 165.0, 190.0, null));
        stratumSpecies.put("Frigus", new BioDetails(2637500, 500, "SO2, CO2-Rich or CO2", 190.0, null, null));
        stratumSpecies.put("Laminamus", new BioDetails(2788300, 500, "Ammonia", 165.0, null, null));
        stratumSpecies.put("Limaxus", new BioDetails(1362000, 500, "SO2, CO2-Rich or CO2", 165.0, 190.0, null));
        stratumSpecies.put("Paleas", new BioDetails(1362000, 500, "Ammonia, Water, Water-rich", 165.0, null, null));
        stratumSpecies.put("Tectonicas", new BioDetails(19010800, 500, "Any", 165.0, null, null));
        GENUS_TO_SPECIES.put("Stratum", stratumSpecies);

        // Tubus
        GENUS_TO_BIOME.put("Tubus", "Colony Range of 800m. Found on plains (i.e., flat terrain).");
        Map<String, BioDetails> tubusSpecies = new HashMap<>();
        tubusSpecies.put("Cavas", new BioDetails(11873200, 800, "CO2, CO2-Rich", 160.0, 190.0, null));
        tubusSpecies.put("Compagibus", new BioDetails(7774700, 800, "CO2, CO2-Rich", 160.0, 190.0, null));
        tubusSpecies.put("Conifer", new BioDetails(2415500, 800, "CO2, CO2-Rich", 160.0, 190.0, null));
        tubusSpecies.put("Rosarium", new BioDetails(2637500, 800, "Ammonia", 160.0, null, null));
        tubusSpecies.put("Sororibus", new BioDetails(5727600, 800, "Ammonia, CO2, CO2-Rich", 160.0, 190.0, null));
        GENUS_TO_SPECIES.put("Tubus", tubusSpecies);

        GENUS_TO_BIOME.put("Roseum Sinuous Tubers", "Colony Range of 800m. Found on plains (i.e., flat terrain).");
        Map<String, BioDetails> blatteumSpecies = new HashMap<>();
        blatteumSpecies.put("Blatteum Sinuous Tubers", new BioDetails(1514500, 800, "None", null, null, "Any"));
        GENUS_TO_SPECIES.put("Roseum Sinuous Tubers", blatteumSpecies);


        // Tussock
        GENUS_TO_BIOME.put("Tussock", "Colony Range of 200m. Can be found on Rocky planets on both plains and on mountains.");
        Map<String, BioDetails> tussockSpecies = new HashMap<>();
        tussockSpecies.put("Albata", new BioDetails(3252500, 200, "CO2, CO2-Rich", 175.0, 180.0, null));
        tussockSpecies.put("Capillum", new BioDetails(7025800, 200, "Methane, Methane-rich, Argon, Argon-rich", null, null, null));
        tussockSpecies.put("Caputus", new BioDetails(3472400, 200, "CO2, CO2-Rich", 180.0, 190.0, null));
        tussockSpecies.put("Catena", new BioDetails(1766600, 200, "Ammonia", null, null, null));
        tussockSpecies.put("Cultro", new BioDetails(1766600, 200, "Ammonia", null, null, null));
        tussockSpecies.put("Divisa", new BioDetails(1766600, 200, "Ammonia", null, null, null));
        tussockSpecies.put("Ignis", new BioDetails(1849000, 200, "CO2, CO2-Rich", 160.0, 170.0, null));
        tussockSpecies.put("Pennata", new BioDetails(5853800, 200, "CO2, CO2-Rich", 145.0, 155.0, null));
        tussockSpecies.put("Pennatis", new BioDetails(1000000, 200, "CO2, CO2-Rich", null, 195.0, null));
        tussockSpecies.put("Propagito", new BioDetails(1000000, 200, "CO2, CO2-Rich", null, 195.0, null));
        tussockSpecies.put("Serrati", new BioDetails(4447100, 200, "CO2, CO2-Rich", 170.0, 175.0, null));
        tussockSpecies.put("Stigmasis", new BioDetails(19010800, 200, "Sulphur dioxide", null, null, null));
        tussockSpecies.put("Triticum", new BioDetails(7774700, 200, "CO2, CO2-Rich", 190.0, 195.0, null));
        tussockSpecies.put("Ventusa", new BioDetails(3277700, 200, "CO2, CO2-Rich", 155.0, 160.0, null));
        tussockSpecies.put("Virgam", new BioDetails(14313700, 200, "Water, Water-rich", null, null, null));
        GENUS_TO_SPECIES.put("Tussock", tussockSpecies);
    }

    /**
     * Get details for a specific species within a genus.
     * @param genus The genus name (case-sensitive).
     * @param variant The variant name (e.g., "Concha Renibus - Blue"), which may include the genus prefix and color suffix.
     * @return BioDetails or null if not found.
     */
    public static BioDetails getDetails(String genus, String variant) {
        String capitalizedGenus = capitalizeWords(genus);
        Map<String, BioDetails> speciesMap = GENUS_TO_SPECIES.get(capitalizedGenus);
        if (speciesMap == null) {
            return null;
        }

        // Parse variant to extract the core species name (strip genus prefix if present, ignore color after " - ")
        String fullVariant = variant.trim();
        if (fullVariant.toLowerCase().startsWith(genus.toLowerCase() + " ")) {
            fullVariant = fullVariant.substring(genus.length() + 1);
        }
        String species = fullVariant.split(" - ")[0].trim();

        return speciesMap.get(capitalizeWords(species));
    }

    /**
     * Get the biome description for a genus.
     * @param genus The genus name (case-sensitive).
     * @return Description string or null if not found.
     */
    public static String getBiomeDescription(String genus) {
        return GENUS_TO_BIOME.get(capitalizeWords(genus));
    }

    public static long getAverageProjectedPayment(String genus){
        String capitalizedGenus = capitalizeWords(genus);
        Map<String, BioDetails> speciesMap = GENUS_TO_SPECIES.get(capitalizedGenus);
        if (speciesMap == null || speciesMap.isEmpty()) {
            return 0;
        }
        long creditValue = 0;
        for (BioDetails details : speciesMap.values()) {
            creditValue += details.creditValue();
        }
        return creditValue / speciesMap.size();
    }

    public static int getDistance(String genus){
        String capitalizedGenus = capitalizeWords(genus);
        Map<String, BioDetails> speciesMap = GENUS_TO_SPECIES.get(capitalizedGenus);
        if (speciesMap == null || speciesMap.isEmpty()) {
            return -1;
        }

        // Get the first species entry - all species in a genus share the same colony range
        return speciesMap.values().iterator().next().colonyRange();
    }
}