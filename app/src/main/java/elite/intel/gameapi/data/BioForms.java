package elite.intel.gameapi.data;

import elite.intel.util.StringUtls;
import java.util.HashMap;
import java.util.Map;

import static elite.intel.util.StringUtls.capitalizeWords;

/**
 * Static utility for looking up known exobiology forms based on public wiki data.
 * Provides genus/species details and biome descriptions.
 */
public class BioForms {
    public record BioDetails(long creditValue, int colonyRange, String atmosphere, Double minTemp, Double maxTemp, String volcanism) {}

    private static final Map<String, String> GENUS_TO_BIOME = new HashMap<>();
    private static final Map<String, Map<String, BioDetails>> GENUS_TO_SPECIES = new HashMap<>();

    static {
        // Aleoida
        GENUS_TO_BIOME.put("Aleoida", "Found on Rocky or High Metal Content worlds with maximum gravity of 0.27. Has a 150m Colony Range.");
        Map<String, BioDetails> aleoidaSpecies = new HashMap<>();
        aleoidaSpecies.put("Aleoida Arcus", new BioDetails(7252500, 150, "CO2", 175.0, 180.0, null));
        aleoidaSpecies.put("Aleoida Coronamus", new BioDetails(6284600, 150, "CO2", 180.0, 190.0, null));
        aleoidaSpecies.put("Aleoida Gravis", new BioDetails(12934900, 150, "CO2", 190.0, 195.0, null));
        aleoidaSpecies.put("Aleoida Laminiae", new BioDetails(3385200, 150, "Ammonia", null, null, null));
        aleoidaSpecies.put("Aleoida Spica", new BioDetails(3385200, 150, "Ammonia", null, null, null));
        GENUS_TO_SPECIES.put("Aleoida", aleoidaSpecies);

        // Amphora Plant
        GENUS_TO_BIOME.put("Amphora Plant", "Colony range of 100m. Requires Type A parent star and specific system features like Earth-Like Worlds or Gas Giants with life.");
        Map<String, BioDetails> amphoraSpecies = new HashMap<>();
        amphoraSpecies.put("Amphora Plant", new BioDetails(3626400, 100, "No Atmosphere Only", null, null, null));
        GENUS_TO_SPECIES.put("Amphora Plant", amphoraSpecies);

        // Anemone
        GENUS_TO_BIOME.put("Anemone", "Colony Range of 100m. Typically found on planets with no atmosphere, but have been found on planets with thin atmosphere.");
        Map<String, BioDetails> anemoneSpecies = new HashMap<>();
        anemoneSpecies.put("Anemone Blatteum Bioluminescent", new BioDetails(1499900, 100, null, null, null, null));
        anemoneSpecies.put("Anemone Croceum", new BioDetails(3399800, 100, null, null, null, null));
        anemoneSpecies.put("Anemone Luteolum", new BioDetails(1499900, 100, null, null, null, null));
        anemoneSpecies.put("Anemone Prasinum Bioluminescent", new BioDetails(1499900, 100, null, null, null, null));
        anemoneSpecies.put("Anemone Puniceum", new BioDetails(1499900, 100, null, null, null, null));
        anemoneSpecies.put("Anemone Roseum", new BioDetails(1499900, 100, null, null, null, null));
        anemoneSpecies.put("Anemone Roseum Bioluminescent", new BioDetails(1499900, 100, null, null, null, null));
        anemoneSpecies.put("Anemone Rubeum Bioluminescent", new BioDetails(1499900, 100, null, null, null, null));
        GENUS_TO_SPECIES.put("Anemone", anemoneSpecies);

        // Bacterium
        GENUS_TO_BIOME.put("Bacterium", "Has a colony range of 500m and can appear on any planet type.");
        Map<String, BioDetails> bacteriumSpecies = new HashMap<>();
        bacteriumSpecies.put("Bacterium Nebulus", new BioDetails(9116600, 500, "Helium", null, null, "None"));
        bacteriumSpecies.put("Bacterium Acies", new BioDetails(1000000, 500, "Neon or Neon-Rich", null, null, "None"));
        bacteriumSpecies.put("Bacterium Omentum", new BioDetails(4638900, 500, "Neon or Neon-Rich", null, null, "Nitrogen or Ammonia"));
        bacteriumSpecies.put("Bacterium Scopulum", new BioDetails(8633800, 500, "Neon or Neon-Rich", null, null, "Carbon or Methane"));
        bacteriumSpecies.put("Bacterium Verrata", new BioDetails(3897000, 500, "Neon or Neon-Rich", null, null, "Water"));
        bacteriumSpecies.put("Bacterium Bullaris", new BioDetails(1152500, 500, "Methane or Methane-Rich", null, null, "None"));
        bacteriumSpecies.put("Bacterium Vesicula", new BioDetails(1000000, 500, "Argon or Argon-Rich", null, null, "None"));
        bacteriumSpecies.put("Bacterium Informem", new BioDetails(8418000, 500, "Nitrogen", null, null, "None"));
        bacteriumSpecies.put("Bacterium Volu", new BioDetails(7774700, 500, "Oxygen", null, null, "None"));
        bacteriumSpecies.put("Bacterium Alcyoneum", new BioDetails(1658500, 500, "Ammonia", null, null, "None"));
        bacteriumSpecies.put("Bacterium Aurasus", new BioDetails(1000000, 500, "CO2", null, null, "None"));
        bacteriumSpecies.put("Bacterium Cerbrus", new BioDetails(1689800, 500, "Water, SO2", null, null, "None"));
        bacteriumSpecies.put("Bacterium Tela", new BioDetails(1949000, 500, "Any", null, null, "None, Helium, Iron, or Silicate"));
        GENUS_TO_SPECIES.put("Bacterium", bacteriumSpecies);

        // Bark Mound
        GENUS_TO_BIOME.put("Bark Mound", "Has a colony distance of 100m. Have a special property where they have growths that can be harvested for materials.");
        Map<String, BioDetails> barkMoundSpecies = new HashMap<>();
        barkMoundSpecies.put("Bark Mound", new BioDetails(1471900, 100, "None only", null, null, null));
        GENUS_TO_SPECIES.put("Bark Mound", barkMoundSpecies);

        // Brain Tree
        GENUS_TO_BIOME.put("Brain Tree", "Has a colony distance of 100m. Excepting the species of Roseum, will only be found in systems with a black hole.");
        Map<String, BioDetails> brainTreeSpecies = new HashMap<>();
        brainTreeSpecies.put("Brain Tree Aureum", new BioDetails(1234000, 100, null, null, null, null));
        brainTreeSpecies.put("Brain Tree Caput", new BioDetails(1234000, 100, null, null, null, null));
        brainTreeSpecies.put("Brain Tree Fuscum", new BioDetails(1234000, 100, null, null, null, null));
        brainTreeSpecies.put("Brain Tree Luteum", new BioDetails(1234000, 100, null, null, null, null));
        brainTreeSpecies.put("Brain Tree Prasinum", new BioDetails(1234000, 100, null, null, null, null));
        brainTreeSpecies.put("Brain Tree Roseum", new BioDetails(1234000, 100, null, null, null, null));
        GENUS_TO_SPECIES.put("Brain Tree", brainTreeSpecies);

        // Cactoida
        GENUS_TO_BIOME.put("Cactoida", "Found on Rocky worlds or High Metal Content worlds. Colony Range 150m.");
        Map<String, BioDetails> cactoidaSpecies = new HashMap<>();
        cactoidaSpecies.put("Cactoida Dulcis", new BioDetails(2812500, 150, "Ammonia", null, null, null));
        cactoidaSpecies.put("Cactoida Amplectens", new BioDetails(2812500, 150, "Ammonia", null, null, null));
        cactoidaSpecies.put("Cactoida Flagrans", new BioDetails(2812500, 150, "CO2", null, null, null));
        cactoidaSpecies.put("Cactoida Porphyrica", new BioDetails(2812500, 150, "CO2", null, null, null));
        cactoidaSpecies.put("Cactoida Silvestris", new BioDetails(2812500, 150, "CO2", null, null, null));
        cactoidaSpecies.put("Cactoida Spectabile", new BioDetails(2812500, 150, "CO2", null, null, null));
        GENUS_TO_SPECIES.put("Cactoida", cactoidaSpecies);

        // Clypeus
        GENUS_TO_BIOME.put("Clypeus", "Found on Rocky worlds or High Metal Content worlds. Colony Range 150m.");
        Map<String, BioDetails> clypeusSpecies = new HashMap<>();
        clypeusSpecies.put("Clypeus Aureum", new BioDetails(1125000, 150, "CO2", null, null, null));
        clypeusSpecies.put("Clypeus Fuscum", new BioDetails(1125000, 150, "CO2", null, null, null));
        clypeusSpecies.put("Clypeus Ianthinum", new BioDetails(1125000, 150, "CO2", null, null, null));
        clypeusSpecies.put("Clypeus Luteolum", new BioDetails(1125000, 150, "CO2", null, null, null));
        clypeusSpecies.put("Clypeus Prasinum", new BioDetails(1125000, 150, "CO2", null, null, null));
        clypeusSpecies.put("Clypeus Purpureum", new BioDetails(1125000, 150, "CO2", null, null, null));
        GENUS_TO_SPECIES.put("Clypeus", clypeusSpecies);

        // Concha
        GENUS_TO_BIOME.put("Concha", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds.");
        Map<String, BioDetails> conchaSpecies = new HashMap<>();
        conchaSpecies.put("Concha Aureum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        conchaSpecies.put("Concha Flavum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        conchaSpecies.put("Concha Fuscum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        conchaSpecies.put("Concha Purpureum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        conchaSpecies.put("Concha Rubrum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        conchaSpecies.put("Concha Viride", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        GENUS_TO_SPECIES.put("Concha", conchaSpecies);

        // Corallium
        GENUS_TO_BIOME.put("Corallium", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds.");
        Map<String, BioDetails> coralliumSpecies = new HashMap<>();
        coralliumSpecies.put("Corallium Aureum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        coralliumSpecies.put("Corallium Flavum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        coralliumSpecies.put("Corallium Fuscum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        coralliumSpecies.put("Corallium Purpureum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        coralliumSpecies.put("Corallium Rubrum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        coralliumSpecies.put("Corallium Viride", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        GENUS_TO_SPECIES.put("Corallium", coralliumSpecies);

        // Cryo Pod
        GENUS_TO_BIOME.put("Cryo Pod", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds with thin Ammonia atmospheres.");
        Map<String, BioDetails> cryoPodSpecies = new HashMap<>();
        cryoPodSpecies.put("Cryo Pod Alabastrum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        cryoPodSpecies.put("Cryo Pod Argenteum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        cryoPodSpecies.put("Cryo Pod Aureum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        cryoPodSpecies.put("Cryo Pod Glauces", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        cryoPodSpecies.put("Cryo Pod Hyacinthinum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        cryoPodSpecies.put("Cryo Pod Luteolum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        GENUS_TO_SPECIES.put("Cryo Pod", cryoPodSpecies);

        // Dendrodonta
        GENUS_TO_BIOME.put("Dendrodonta", "Found on Rocky worlds or High Metal Content worlds. Colony Range 150m.");
        Map<String, BioDetails> dendrodontaSpecies = new HashMap<>();
        dendrodontaSpecies.put("Dendrodonta Argenteum", new BioDetails(5625000, 150, "Ammonia", null, null, null));
        dendrodontaSpecies.put("Dendrodonta Aureum", new BioDetails(5625000, 150, "Ammonia", null, null, null));
        dendrodontaSpecies.put("Dendrodonta Fuscum", new BioDetails(5625000, 150, "Ammonia", null, null, null));
        dendrodontaSpecies.put("Dendrodonta Glauces", new BioDetails(5625000, 150, "Ammonia", null, null, null));
        dendrodontaSpecies.put("Dendrodonta Luteolum", new BioDetails(5625000, 150, "Ammonia", null, null, null));
        dendrodontaSpecies.put("Dendrodonta Purpureum", new BioDetails(5625000, 150, "Ammonia", null, null, null));
        GENUS_TO_SPECIES.put("Dendrodonta", dendrodontaSpecies);

        // Eudora
        GENUS_TO_BIOME.put("Eudora", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds.");
        Map<String, BioDetails> eudoraSpecies = new HashMap<>();
        eudoraSpecies.put("Eudora Aureum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        eudoraSpecies.put("Eudora Flavum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        eudoraSpecies.put("Eudora Fuscum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        eudoraSpecies.put("Eudora Purpureum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        eudoraSpecies.put("Eudora Rubrum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        eudoraSpecies.put("Eudora Viride", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        GENUS_TO_SPECIES.put("Eudora", eudoraSpecies);

        // Felis
        GENUS_TO_BIOME.put("Felis", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds with Ammonia atmospheres.");
        Map<String, BioDetails> felisSpecies = new HashMap<>();
        felisSpecies.put("Felis Argenteum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        felisSpecies.put("Felis Aureum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        felisSpecies.put("Felis Fuscum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        felisSpecies.put("Felis Glauces", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        felisSpecies.put("Felis Luteolum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        felisSpecies.put("Felis Purpureum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        GENUS_TO_SPECIES.put("Felis", felisSpecies);

        // Fonticulua
        GENUS_TO_BIOME.put("Fonticulua", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds.");
        Map<String, BioDetails> fonticuluaSpecies = new HashMap<>();
        fonticuluaSpecies.put("Fonticulua Aureum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        fonticuluaSpecies.put("Fonticulua Flavum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        fonticuluaSpecies.put("Fonticulua Fuscum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        fonticuluaSpecies.put("Fonticulua Purpureum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        fonticuluaSpecies.put("Fonticulua Rubrum", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        fonticuluaSpecies.put("Fonticulua Viride", new BioDetails(2250000, 100, "Ammonia", null, null, null));
        GENUS_TO_SPECIES.put("Fonticulua", fonticuluaSpecies);

        // Fungal Cluster
        GENUS_TO_BIOME.put("Fungal Cluster", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds.");
        Map<String, BioDetails> fungalClusterSpecies = new HashMap<>();
        fungalClusterSpecies.put("Fungal Cluster Alabastrum", new BioDetails(1125000, 100, "CO2", null, null, null));
        fungalClusterSpecies.put("Fungal Cluster Argenteum", new BioDetails(1125000, 100, "CO2", null, null, null));
        fungalClusterSpecies.put("Fungal Cluster Aureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        fungalClusterSpecies.put("Fungal Cluster Flavum", new BioDetails(1125000, 100, "CO2", null, null, null));
        fungalClusterSpecies.put("Fungal Cluster Fuscum", new BioDetails(1125000, 100, "CO2", null, null, null));
        fungalClusterSpecies.put("Fungal Cluster Glauces", new BioDetails(1125000, 100, "CO2", null, null, null));
        GENUS_TO_SPECIES.put("Fungal Cluster", fungalClusterSpecies);

        // Fungoida
        GENUS_TO_BIOME.put("Fungoida", "Found on Rocky or High Metal Content worlds with thin atmospheres. Colony Range 150m.");
        Map<String, BioDetails> fungoidaSpecies = new HashMap<>();
        fungoidaSpecies.put("Fungoida Setisis", new BioDetails(2812500, 150, "CO2", 150.0, 200.0, null));
        fungoidaSpecies.put("Fungoida Stabitis", new BioDetails(2812500, 150, "CO2", 150.0, 200.0, null));
        fungoidaSpecies.put("Fungoida Gelata", new BioDetails(2812500, 150, "Ammonia", null, null, null));
        fungoidaSpecies.put("Fungoida Bullarum", new BioDetails(2812500, 150, "Ammonia", null, null, null));
        GENUS_TO_SPECIES.put("Fungoida", fungoidaSpecies);

        // Frutexa
        GENUS_TO_BIOME.put("Frutexa", "Found on Rocky or High Metal Content worlds in mountainous or hilly areas. Colony Range 150m. Can be difficult to spot; use night vision or foot recon.");
        Map<String, BioDetails> frutexaSpecies = new HashMap<>();
        frutexaSpecies.put("Frutexa Acus", new BioDetails(7774700, 150, "CO2 or CO2-Rich", null, 195.0, null));
        frutexaSpecies.put("Frutexa Collum", new BioDetails(1639800, 150, "SO2", null, null, null));
        frutexaSpecies.put("Frutexa Fera", new BioDetails(1632500, 150, "CO2 or CO2-Rich", null, 195.0, null));
        frutexaSpecies.put("Frutexa Flabellum", new BioDetails(1808900, 150, "Ammonia", null, null, null));
        frutexaSpecies.put("Frutexa Flammasis", new BioDetails(10326000, 150, "Ammonia", null, null, null));
        frutexaSpecies.put("Frutexa Metallicum", new BioDetails(1632500, 150, "CO2 or Ammonia", null, 195.0, null));
        frutexaSpecies.put("Frutexa Sponsae", new BioDetails(5988000, 150, "Water or Water-Rich", null, null, null));
        GENUS_TO_SPECIES.put("Frutexa", frutexaSpecies);

        // Funnel Cap
        GENUS_TO_BIOME.put("Funnel Cap", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds.");
        Map<String, BioDetails> funnelCapSpecies = new HashMap<>();
        funnelCapSpecies.put("Funnel Cap Alabastrum", new BioDetails(1125000, 100, "CO2", null, null, null));
        funnelCapSpecies.put("Funnel Cap Argenteum", new BioDetails(1125000, 100, "CO2", null, null, null));
        funnelCapSpecies.put("Funnel Cap Aureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        funnelCapSpecies.put("Funnel Cap Flavum", new BioDetails(1125000, 100, "CO2", null, null, null));
        funnelCapSpecies.put("Funnel Cap Fuscum", new BioDetails(1125000, 100, "CO2", null, null, null));
        funnelCapSpecies.put("Funnel Cap Glauces", new BioDetails(1125000, 100, "CO2", null, null, null));
        GENUS_TO_SPECIES.put("Funnel Cap", funnelCapSpecies);

        // Gourd
        GENUS_TO_BIOME.put("Gourd", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds.");
        Map<String, BioDetails> gourdSpecies = new HashMap<>();
        gourdSpecies.put("Gourd Argenteum", new BioDetails(1125000, 100, "CO2", null, null, null));
        gourdSpecies.put("Gourd Aureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        gourdSpecies.put("Gourd Fuscum", new BioDetails(1125000, 100, "CO2", null, null, null));
        gourdSpecies.put("Gourd Glauces", new BioDetails(1125000, 100, "CO2", null, null, null));
        gourdSpecies.put("Gourd Luteolum", new BioDetails(1125000, 100, "CO2", null, null, null));
        gourdSpecies.put("Gourd Purpureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        GENUS_TO_SPECIES.put("Gourd", gourdSpecies);

        // Helix
        GENUS_TO_BIOME.put("Helix", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds.");
        Map<String, BioDetails> helixSpecies = new HashMap<>();
        helixSpecies.put("Helix Argenteum", new BioDetails(1125000, 100, "CO2", null, null, null));
        helixSpecies.put("Helix Aureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        helixSpecies.put("Helix Fuscum", new BioDetails(1125000, 100, "CO2", null, null, null));
        helixSpecies.put("Helix Glauces", new BioDetails(1125000, 100, "CO2", null, null, null));
        helixSpecies.put("Helix Luteolum", new BioDetails(1125000, 100, "CO2", null, null, null));
        helixSpecies.put("Helix Purpureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        GENUS_TO_SPECIES.put("Helix", helixSpecies);

        // Hydrae
        GENUS_TO_BIOME.put("Hydrae", "Found on Rocky worlds or High Metal Content worlds. Colony Range 150m.");
        Map<String, BioDetails> hydraeSpecies = new HashMap<>();
        hydraeSpecies.put("Hydrae Aureum", new BioDetails(5625000, 150, "CO2", null, null, null));
        hydraeSpecies.put("Hydrae Fuscum", new BioDetails(5625000, 150, "CO2", null, null, null));
        hydraeSpecies.put("Hydrae Ianthinum", new BioDetails(5625000, 150, "CO2", null, null, null));
        hydraeSpecies.put("Hydrae Luteolum", new BioDetails(5625000, 150, "CO2", null, null, null));
        hydraeSpecies.put("Hydrae Prasinum", new BioDetails(5625000, 150, "CO2", null, null, null));
        hydraeSpecies.put("Hydrae Purpureum", new BioDetails(5625000, 150, "CO2", null, null, null));
        GENUS_TO_SPECIES.put("Hydrae", hydraeSpecies);

        // Lepidoptera
        GENUS_TO_BIOME.put("Lepidoptera", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds with thin Ammonia atmospheres.");
        Map<String, BioDetails> lepidopteraSpecies = new HashMap<>();
        lepidopteraSpecies.put("Lepidoptera Aureum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        lepidopteraSpecies.put("Lepidoptera Flavum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        lepidopteraSpecies.put("Lepidoptera Fuscum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        lepidopteraSpecies.put("Lepidoptera Purpureum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        lepidopteraSpecies.put("Lepidoptera Rubrum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        lepidopteraSpecies.put("Lepidoptera Viride", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        GENUS_TO_SPECIES.put("Lepidoptera", lepidopteraSpecies);

        // Osseus
        GENUS_TO_BIOME.put("Osseus", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds.");
        Map<String, BioDetails> osseusSpecies = new HashMap<>();
        osseusSpecies.put("Osseus Alabastrum", new BioDetails(1125000, 100, "CO2", null, null, null));
        osseusSpecies.put("Osseus Argenteum", new BioDetails(1125000, 100, "CO2", null, null, null));
        osseusSpecies.put("Osseus Aureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        osseusSpecies.put("Osseus Flavum", new BioDetails(1125000, 100, "CO2", null, null, null));
        osseusSpecies.put("Osseus Fuscum", new BioDetails(1125000, 100, "CO2", null, null, null));
        osseusSpecies.put("Osseus Glauces", new BioDetails(1125000, 100, "CO2", null, null, null));
        GENUS_TO_SPECIES.put("Osseus", osseusSpecies);

        // Phagium
        GENUS_TO_BIOME.put("Phagium", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds.");
        Map<String, BioDetails> phagiumSpecies = new HashMap<>();
        phagiumSpecies.put("Phagium Alabastrum", new BioDetails(1125000, 100, "CO2", null, null, null));
        phagiumSpecies.put("Phagium Argenteum", new BioDetails(1125000, 100, "CO2", null, null, null));
        phagiumSpecies.put("Phagium Aureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        phagiumSpecies.put("Phagium Flavum", new BioDetails(1125000, 100, "CO2", null, null, null));
        phagiumSpecies.put("Phagium Fuscum", new BioDetails(1125000, 100, "CO2", null, null, null));
        phagiumSpecies.put("Phagium Glauces", new BioDetails(1125000, 100, "CO2", null, null, null));
        GENUS_TO_SPECIES.put("Phagium", phagiumSpecies);

        // Pod
        GENUS_TO_BIOME.put("Pod", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds.");
        Map<String, BioDetails> podSpecies = new HashMap<>();
        podSpecies.put("Pod Argenteum", new BioDetails(1125000, 100, "CO2", null, null, null));
        podSpecies.put("Pod Aureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        podSpecies.put("Pod Fuscum", new BioDetails(1125000, 100, "CO2", null, null, null));
        podSpecies.put("Pod Glauces", new BioDetails(1125000, 100, "CO2", null, null, null));
        podSpecies.put("Pod Luteolum", new BioDetails(1125000, 100, "CO2", null, null, null));
        podSpecies.put("Pod Purpureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        GENUS_TO_SPECIES.put("Pod", podSpecies);

        // Pruniformis
        GENUS_TO_BIOME.put("Pruniformis", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds with thin Ammonia atmospheres.");
        Map<String, BioDetails> pruniformisSpecies = new HashMap<>();
        pruniformisSpecies.put("Pruniformis Argenteum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        pruniformisSpecies.put("Pruniformis Aureum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        pruniformisSpecies.put("Pruniformis Fuscum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        pruniformisSpecies.put("Pruniformis Glauces", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        pruniformisSpecies.put("Pruniformis Luteolum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        pruniformisSpecies.put("Pruniformis Purpureum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        GENUS_TO_SPECIES.put("Pruniformis", pruniformisSpecies);

        // Recepta
        GENUS_TO_BIOME.put("Recepta", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds.");
        Map<String, BioDetails> receptaSpecies = new HashMap<>();
        receptaSpecies.put("Recepta Argenteum", new BioDetails(1125000, 100, "CO2", null, null, null));
        receptaSpecies.put("Recepta Aureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        receptaSpecies.put("Recepta Fuscum", new BioDetails(1125000, 100, "CO2", null, null, null));
        receptaSpecies.put("Recepta Glauces", new BioDetails(1125000, 100, "CO2", null, null, null));
        receptaSpecies.put("Recepta Luteolum", new BioDetails(1125000, 100, "CO2", null, null, null));
        receptaSpecies.put("Recepta Purpureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        GENUS_TO_SPECIES.put("Recepta", receptaSpecies);

        // Reticulum
        GENUS_TO_BIOME.put("Reticulum", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds.");
        Map<String, BioDetails> reticulumSpecies = new HashMap<>();
        reticulumSpecies.put("Reticulum Argenteum", new BioDetails(1125000, 100, "CO2", null, null, null));
        reticulumSpecies.put("Reticulum Aureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        reticulumSpecies.put("Reticulum Fuscum", new BioDetails(1125000, 100, "CO2", null, null, null));
        reticulumSpecies.put("Reticulum Glauces", new BioDetails(1125000, 100, "CO2", null, null, null));
        reticulumSpecies.put("Reticulum Luteolum", new BioDetails(1125000, 100, "CO2", null, null, null));
        reticulumSpecies.put("Reticulum Purpureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        GENUS_TO_SPECIES.put("Reticulum", reticulumSpecies);

        // Sarracenia
        GENUS_TO_BIOME.put("Sarracenia", "Found on Rocky worlds or High Metal Content worlds. Colony Range 150m.");
        Map<String, BioDetails> sarraceniaSpecies = new HashMap<>();
        sarraceniaSpecies.put("Sarracenia Aureum", new BioDetails(2812500, 150, "Ammonia", null, null, null));
        sarraceniaSpecies.put("Sarracenia Flavum", new BioDetails(2812500, 150, "Ammonia", null, null, null));
        sarraceniaSpecies.put("Sarracenia Fuscum", new BioDetails(2812500, 150, "Ammonia", null, null, null));
        sarraceniaSpecies.put("Sarracenia Purpureum", new BioDetails(2812500, 150, "Ammonia", null, null, null));
        sarraceniaSpecies.put("Sarracenia Rubrum", new BioDetails(2812500, 150, "Ammonia", null, null, null));
        sarraceniaSpecies.put("Sarracenia Viride", new BioDetails(2812500, 150, "Ammonia", null, null, null));
        GENUS_TO_SPECIES.put("Sarracenia", sarraceniaSpecies);

        // Scutia
        GENUS_TO_BIOME.put("Scutia", "Found on Rocky worlds or High Metal Content worlds. Colony Range 150m.");
        Map<String, BioDetails> scutiaSpecies = new HashMap<>();
        scutiaSpecies.put("Scutia Aureum", new BioDetails(5625000, 150, "CO2", null, null, null));
        scutiaSpecies.put("Scutia Fuscum", new BioDetails(5625000, 150, "CO2", null, null, null));
        scutiaSpecies.put("Scutia Ianthinum", new BioDetails(5625000, 150, "CO2", null, null, null));
        scutiaSpecies.put("Scutia Luteolum", new BioDetails(5625000, 150, "CO2", null, null, null));
        scutiaSpecies.put("Scutia Prasinum", new BioDetails(5625000, 150, "CO2", null, null, null));
        scutiaSpecies.put("Scutia Purpureum", new BioDetails(5625000, 150, "CO2", null, null, null));
        GENUS_TO_SPECIES.put("Scutia", scutiaSpecies);

        // Stratum
        GENUS_TO_BIOME.put("Stratum", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds.");
        Map<String, BioDetails> stratumSpecies = new HashMap<>();
        stratumSpecies.put("Stratum Alabastrum", new BioDetails(1125000, 100, "CO2", null, null, null));
        stratumSpecies.put("Stratum Argenteum", new BioDetails(1125000, 100, "CO2", null, null, null));
        stratumSpecies.put("Stratum Aureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        stratumSpecies.put("Stratum Flavum", new BioDetails(1125000, 100, "CO2", null, null, null));
        stratumSpecies.put("Stratum Fuscum", new BioDetails(1125000, 100, "CO2", null, null, null));
        stratumSpecies.put("Stratum Glauces", new BioDetails(1125000, 100, "CO2", null, null, null));
        GENUS_TO_SPECIES.put("Stratum", stratumSpecies);

        // Tangles
        GENUS_TO_BIOME.put("Tangles", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds with thin Ammonia atmospheres.");
        Map<String, BioDetails> tanglesSpecies = new HashMap<>();
        tanglesSpecies.put("Tangles Argenteum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        tanglesSpecies.put("Tangles Aureum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        tanglesSpecies.put("Tangles Fuscum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        tanglesSpecies.put("Tangles Glauces", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        tanglesSpecies.put("Tangles Luteolum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        tanglesSpecies.put("Tangles Purpureum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        GENUS_TO_SPECIES.put("Tangles", tanglesSpecies);

        // Thallus
        GENUS_TO_BIOME.put("Thallus", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds.");
        Map<String, BioDetails> thallusSpecies = new HashMap<>();
        thallusSpecies.put("Thallus Alabastrum", new BioDetails(1125000, 100, "CO2", null, null, null));
        thallusSpecies.put("Thallus Argenteum", new BioDetails(1125000, 100, "CO2", null, null, null));
        thallusSpecies.put("Thallus Aureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        thallusSpecies.put("Thallus Flavum", new BioDetails(1125000, 100, "CO2", null, null, null));
        thallusSpecies.put("Thallus Fuscum", new BioDetails(1125000, 100, "CO2", null, null, null));
        thallusSpecies.put("Thallus Glauces", new BioDetails(1125000, 100, "CO2", null, null, null));
        GENUS_TO_SPECIES.put("Thallus", thallusSpecies);

        // Tubus
        GENUS_TO_BIOME.put("Tubus", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds.");
        Map<String, BioDetails> tubusSpecies = new HashMap<>();
        tubusSpecies.put("Tubus Argenteum", new BioDetails(1125000, 100, "CO2", null, null, null));
        tubusSpecies.put("Tubus Aureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        tubusSpecies.put("Tubus Fuscum", new BioDetails(1125000, 100, "CO2", null, null, null));
        tubusSpecies.put("Tubus Glauces", new BioDetails(1125000, 100, "CO2", null, null, null));
        tubusSpecies.put("Tubus Luteolum", new BioDetails(1125000, 100, "CO2", null, null, null));
        tubusSpecies.put("Tubus Purpureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        GENUS_TO_SPECIES.put("Tubus", tubusSpecies);

        // Umbrella Tree
        GENUS_TO_BIOME.put("Umbrella Tree", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds.");
        Map<String, BioDetails> umbrellaTreeSpecies = new HashMap<>();
        umbrellaTreeSpecies.put("Umbrella Tree Argenteum", new BioDetails(1125000, 100, "CO2", null, null, null));
        umbrellaTreeSpecies.put("Umbrella Tree Aureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        umbrellaTreeSpecies.put("Umbrella Tree Fuscum", new BioDetails(1125000, 100, "CO2", null, null, null));
        umbrellaTreeSpecies.put("Umbrella Tree Glauces", new BioDetails(1125000, 100, "CO2", null, null, null));
        umbrellaTreeSpecies.put("Umbrella Tree Luteolum", new BioDetails(1125000, 100, "CO2", null, null, null));
        umbrellaTreeSpecies.put("Umbrella Tree Purpureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        GENUS_TO_SPECIES.put("Umbrella Tree", umbrellaTreeSpecies);

        // Uncus
        GENUS_TO_BIOME.put("Uncus", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds with thin Ammonia atmospheres.");
        Map<String, BioDetails> uncusSpecies = new HashMap<>();
        uncusSpecies.put("Uncus Argenteum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        uncusSpecies.put("Uncus Aureum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        uncusSpecies.put("Uncus Fuscum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        uncusSpecies.put("Uncus Glauces", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        uncusSpecies.put("Uncus Luteolum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        uncusSpecies.put("Uncus Purpureum", new BioDetails(1125000, 100, "Ammonia", null, null, null));
        GENUS_TO_SPECIES.put("Uncus", uncusSpecies);

        // Verticulum
        GENUS_TO_BIOME.put("Verticulum", "Colony Range of 100m. Found on Rocky worlds or High Metal Content worlds.");
        Map<String, BioDetails> verticulumSpecies = new HashMap<>();
        verticulumSpecies.put("Verticulum Argenteum", new BioDetails(1125000, 100, "CO2", null, null, null));
        verticulumSpecies.put("Verticulum Aureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        verticulumSpecies.put("Verticulum Fuscum", new BioDetails(1125000, 100, "CO2", null, null, null));
        verticulumSpecies.put("Verticulum Glauces", new BioDetails(1125000, 100, "CO2", null, null, null));
        verticulumSpecies.put("Verticulum Luteolum", new BioDetails(1125000, 100, "CO2", null, null, null));
        verticulumSpecies.put("Verticulum Purpureum", new BioDetails(1125000, 100, "CO2", null, null, null));
        GENUS_TO_SPECIES.put("Verticulum", verticulumSpecies);
    }

    /**
     * Get details for a specific species within a genus.
     * @param genus The genus name (case-sensitive).
     * @param species The species name (case-sensitive).
     * @return BioDetails or null if not found.
     */
    public static BioDetails getDetails(String genus, String species) {
        Map<String, BioDetails> speciesMap = GENUS_TO_SPECIES.get(capitalizeWords(genus));
        return speciesMap != null ? speciesMap.get(capitalizeWords(species)) : null;
    }

    /**
     * Get the biome description for a genus.
     * @param genus The genus name (case-sensitive).
     * @return Description string or null if not found.
     */
    public static String getBiomeDescription(String genus) {
        return GENUS_TO_BIOME.get(capitalizeWords(genus));
    }
}