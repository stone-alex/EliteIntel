package elite.intel.gameapi.data;

import java.util.HashMap;
import java.util.Map;

import static elite.intel.util.StringUtls.capitalizeWords;

/**
 * Static utility for looking up known exobiology forms based on public wiki data.
 * Provides genus/species details and biome descriptions.
 */
public class BioForms {
    public record BioDetails(long creditValue, long firstDiscoveryBonus, Integer colonyRange, String atmosphere, Double minTemp, Double maxTemp, String volcanism) {
    }

    private static final Map<String, String> GENUS_TO_BIOME = new HashMap<>();
    private static final Map<String, Map<String, BioDetails>> GENUS_TO_SPECIES = new HashMap<>();

    static {
        // Aleoida
        GENUS_TO_BIOME.put("Aleoida", "Planet:Rocky,High Metal Content|Atmosphere:CO2,CO2-Rich,Ammonia|Gravity:<=0.27|Temperature:175-195K|Volcanism:None|System:Any");
        Map<String, BioDetails> aleoidaSpecies = new HashMap<>();
        aleoidaSpecies.put("Arcus", new BioDetails(7252500, 29010000, 150, "CO2-Rich or CO2", 175.0, 180.0, null));
        aleoidaSpecies.put("Coronamus", new BioDetails(6284600, 25138400, 150, "CO2-Rich or CO2", 180.0, 190.0, null));
        aleoidaSpecies.put("Gravis", new BioDetails(12934900, 51739600, 150, "CO2-Rich or CO2", 190.0, 195.0, null));
        aleoidaSpecies.put("Laminiae", new BioDetails(3385200, 13540800, 150, "Ammonia", null, null, null));
        aleoidaSpecies.put("Spica", new BioDetails(3385200, 13540800, 150, "Ammonia", null, null, null));
        GENUS_TO_SPECIES.put("Aleoida", aleoidaSpecies);

        // Amphora Plant
        GENUS_TO_BIOME.put("Amphora Plant", "Planet:Any|Atmosphere:None|Gravity:Any|Temperature:Any|Volcanism:None|System:Star Type A,Earth-Like World,Ammonia World,Gas Giant with Life,>12000Ls from star");
        Map<String, BioDetails> amphoraSpecies = new HashMap<>();
        amphoraSpecies.put("Amphora Plant", new BioDetails(3626400, 14505600, 100, "No Atmosphere Only", null, null, null));
        GENUS_TO_SPECIES.put("Amphora Plant", amphoraSpecies);

        // Anemone
        GENUS_TO_BIOME.put("Anemone", "Planet:Any|Atmosphere:None,Thin|Gravity:Any|Temperature:Any|Volcanism:None|System:Star Type varies by species");
        Map<String, BioDetails> anemoneSpecies = new HashMap<>();
        anemoneSpecies.put("Blatteum Bioluminescent", new BioDetails(1499900, 5999600, 100, null, null, null, null));
        anemoneSpecies.put("Croceum", new BioDetails(3399800, 13599200, 100, null, null, null, null));
        anemoneSpecies.put("Luteolum", new BioDetails(1499900, 5999600, 100, null, null, null, null));
        anemoneSpecies.put("Prasinum Bioluminescent", new BioDetails(1499900, 5999600, 100, null, null, null, null));
        anemoneSpecies.put("Puniceum", new BioDetails(1499900, 5999600, 100, null, null, null, null));
        anemoneSpecies.put("Roseum", new BioDetails(1499900, 5999600, 100, null, null, null, null));
        anemoneSpecies.put("Roseum Bioluminescent", new BioDetails(1499900, 5999600, 100, null, null, null, null));
        anemoneSpecies.put("Rubeum Bioluminescent", new BioDetails(1499900, 5999600, 100, null, null, null, null));
        GENUS_TO_SPECIES.put("Anemone", anemoneSpecies);

        // Bacterium
        GENUS_TO_BIOME.put("Bacterium", "Planet:Any|Atmosphere:Any|Gravity:Any|Temperature:Any|Volcanism:Any|System:Any");
        Map<String, BioDetails> bacteriumSpecies = new HashMap<>();
        bacteriumSpecies.put("Nebulus", new BioDetails(9116600, 36466400, 500, "Helium", null, null, "None"));
        bacteriumSpecies.put("Acies", new BioDetails(1000000, 4000000, 500, "Neon or Neon-Rich", null, null, "None"));
        bacteriumSpecies.put("Omentum", new BioDetails(4638900, 18555600, 500, "Neon or Neon-Rich", null, null, "Nitrogen or Ammonia"));
        bacteriumSpecies.put("Scopulum", new BioDetails(8633800, 34535200, 500, "Neon or Neon-Rich", null, null, "Carbon or Methane"));
        bacteriumSpecies.put("Verrata", new BioDetails(3897000, 15588000, 500, "Neon or Neon-Rich", null, null, "Water"));
        bacteriumSpecies.put("Bullaris", new BioDetails(1152500, 4610000, 500, "Methane or Methane-Rich", null, null, "None"));
        bacteriumSpecies.put("Vesicula", new BioDetails(1000000, 4000000, 500, "Argon or Argon-Rich", null, null, "None"));
        bacteriumSpecies.put("Informem", new BioDetails(8418000, 33672000, 500, "Nitrogen", null, null, "None"));
        bacteriumSpecies.put("Volu", new BioDetails(7774700, 31098800, 500, "Oxygen", null, null, "None"));
        bacteriumSpecies.put("Alcyoneum", new BioDetails(1658500, 6634000, 500, "Ammonia", null, null, "None"));
        bacteriumSpecies.put("Aurasus", new BioDetails(1000000, 4000000, 500, "CO2-Rich or CO2", null, null, "None"));
        bacteriumSpecies.put("Cerbrus", new BioDetails(1689800, 6759200, 500, "Water, SO2", null, null, "None"));
        bacteriumSpecies.put("Tela", new BioDetails(1949000, 7796000, 500, "Any", null, null, "None, Helium, Iron, or Silicate"));
        GENUS_TO_SPECIES.put("Bacterium", bacteriumSpecies);

        // Bark Mound
        GENUS_TO_BIOME.put("Bark Mound", "Planet:Any|Atmosphere:None|Gravity:Any|Temperature:Any|Volcanism:None|System:Within 150Ly of nebula center");
        Map<String, BioDetails> barkMoundSpecies = new HashMap<>();
        barkMoundSpecies.put("Bark Mound", new BioDetails(1471900, 5887600, 100, "None only", null, null, null));
        GENUS_TO_SPECIES.put("Bark Mound", barkMoundSpecies);

        // Brain Tree
        GENUS_TO_BIOME.put("Brain Tree", "Planet:Any|Atmosphere:Any|Gravity:Any|Temperature:100-500K|Volcanism:None|System:Earth-Like World,Gas Giant with Water-based Life,Roseum Any");
        Map<String, BioDetails> brainTreeSpecies = new HashMap<>();
        brainTreeSpecies.put("Aureum", new BioDetails(3565100, 14260400, 100, null, 300.0, 500.0, null));
        brainTreeSpecies.put("Gypseeum", new BioDetails(3565100, 14260400, 100, null, 200.0, 300.0, null));
        brainTreeSpecies.put("Lindigoticum", new BioDetails(3565100, 14260400, 100, null, 300.0, 500.0, null));
        brainTreeSpecies.put("Lividum", new BioDetails(1593700, 6374800, 100, null, 300.0, 500.0, null));
        brainTreeSpecies.put("Ostrinum", new BioDetails(3565100, 14260400, 100, null, null, null, null));
        brainTreeSpecies.put("Puniceum", new BioDetails(3565100, 14260400, 100, null, null, null, null));
        brainTreeSpecies.put("Roseum", new BioDetails(1593700, 6374800, 100, null, 200.0, 500.0, null));
        brainTreeSpecies.put("Viride", new BioDetails(1593700, 6374800, 100, null, 100.0, 270.0, null));
        GENUS_TO_SPECIES.put("Brain Tree", brainTreeSpecies);

        // Cactoida
        GENUS_TO_BIOME.put("Cactoida", "Planet:Rocky,High Metal Content|Atmosphere:CO2,CO2-Rich,Ammonia,Water|Gravity:Any|Temperature:180-195K|Volcanism:None|System:Any");
        Map<String, BioDetails> cactoidaSpecies = new HashMap<>();
        cactoidaSpecies.put("Cortexum", new BioDetails(3667600, 14670400, 300, "CO2-Rich or CO2", null, null, null));
        cactoidaSpecies.put("Lapis", new BioDetails(2483600, 9934400, 300, "Ammonia", null, null, null));
        cactoidaSpecies.put("Peperatis", new BioDetails(2483600, 9934400, 300, "Ammonia", null, null, null));
        cactoidaSpecies.put("Pullulanta", new BioDetails(3667600, 14670400, 300, "CO2-Rich or CO2", 180.0, 195.0, null));
        cactoidaSpecies.put("Vermis", new BioDetails(16202800, 64811200, 300, "Water", null, null, null));
        GENUS_TO_SPECIES.put("Cactoida", cactoidaSpecies);

        // Clypeus
        GENUS_TO_BIOME.put("Clypeus", "Planet:Rocky,High Metal Content|Atmosphere:Water,CO2|Gravity:<=0.27|Temperature:>=190K|Volcanism:None|System:Any");
        Map<String, BioDetails> clypeusSpecies = new HashMap<>();
        clypeusSpecies.put("Lacrimam", new BioDetails(8418000, 33672000, 150, "Water or CO2", 190.0, null, null));
        clypeusSpecies.put("Margaritus", new BioDetails(11873200, 47492800, 150, "Water or CO2", 190.0, null, null));
        clypeusSpecies.put("Speculumi", new BioDetails(16202800, 64811200, 150, "Water or CO2", 190.0, null, null));
        GENUS_TO_SPECIES.put("Clypeus", clypeusSpecies);

        // Concha
        GENUS_TO_BIOME.put("Concha", "Planet:Rocky,High Metal Content|Atmosphere:Ammonia,Nitrogen,CO2,CO2-Rich,Water,Water-Rich|Gravity:Any|Temperature:180-195K|Volcanism:None|System:Any");
        Map<String, BioDetails> conchaSpecies = new HashMap<>();
        conchaSpecies.put("Aureolas", new BioDetails(7774700, 31098800, 150, "Ammonia", null, null, null));
        conchaSpecies.put("Biconcavis", new BioDetails(16777215, 67108860, 150, "Nitrogen", null, null, null));
        conchaSpecies.put("Labiata", new BioDetails(2352400, 9409600, 150, "CO2-Rich or CO2", null, 190.0, null));
        conchaSpecies.put("Renibus", new BioDetails(4572400, 18289600, 150, "Water, Water-Rich, CO2-Rich, or CO2", 180.0, 195.0, null));
        GENUS_TO_SPECIES.put("Concha", conchaSpecies);

        // Crystalline Shard
        GENUS_TO_BIOME.put("Crystalline Shard", "Planet:Any|Atmosphere:None|Gravity:Any|Temperature:Any|Volcanism:None|System:Star Type A,F,G,K,M,S,Earth-Like World,Ammonia World,Gas Giant with Life,>12000Ls from star");
        Map<String, BioDetails> crystallineShardSpecies = new HashMap<>();
        crystallineShardSpecies.put("Crystalline Shard", new BioDetails(3626400, 14505600, 100, "None only", null, null, null));
        GENUS_TO_SPECIES.put("Crystalline Shard", crystallineShardSpecies);

        // Electricae
        GENUS_TO_BIOME.put("Electricae", "Planet:Icy|Atmosphere:Helium,Neon,Argon|Gravity:<=0.27|Temperature:Any|Volcanism:None|System:Any");
        Map<String, BioDetails> electricaeSpecies = new HashMap<>();
        electricaeSpecies.put("Pluma", new BioDetails(6284600, 25138400, 1000, "Helium, Neon, or Argon", null, null, null));
        electricaeSpecies.put("Radialem", new BioDetails(6284600, 25138400, 1000, "Helium, Neon, or Argon", null, null, null));
        GENUS_TO_SPECIES.put("Electricae", electricaeSpecies);

        // Fonticulua
        GENUS_TO_BIOME.put("Fonticulua", "Planet:Icy|Atmosphere:Argon,Argon-Rich,Methane,Oxygen,Nitrogen,Neon,Neon-Rich|Gravity:Any|Temperature:Any|Volcanism:None|System:Any");
        Map<String, BioDetails> fonticuluaSpecies = new HashMap<>();
        fonticuluaSpecies.put("Campestris", new BioDetails(1000000, 4000000, 500, "Argon", null, null, null));
        fonticuluaSpecies.put("Digitos", new BioDetails(1804100, 7216400, 500, "Methane", null, null, null));
        fonticuluaSpecies.put("Fluctus", new BioDetails(16777215, 67108860, 500, "Oxygen", null, null, null));
        fonticuluaSpecies.put("Lapida", new BioDetails(3111000, 12444000, 500, "Nitrogen", null, null, null));
        fonticuluaSpecies.put("Segmentatus", new BioDetails(19010800, 76043200, 500, "Neon, Neon-Rich", null, null, null));
        fonticuluaSpecies.put("Upupam", new BioDetails(5727600, 22910400, 500, "Argon Rich", null, null, null));
        GENUS_TO_SPECIES.put("Fonticulua", fonticuluaSpecies);

        // Frutexa
        GENUS_TO_BIOME.put("Frutexa", "Planet:Rocky,High Metal Content|Atmosphere:CO2,CO2-Rich,SO2,Ammonia,Water,Water-Rich|Gravity:Any|Temperature:<=195K|Volcanism:None|System:Any");
        Map<String, BioDetails> frutexaSpecies = new HashMap<>();
        frutexaSpecies.put("Acus", new BioDetails(7774700, 31098800, 150, "CO2-Rich or CO2", null, 195.0, null));
        frutexaSpecies.put("Collum", new BioDetails(1639800, 6559200, 150, "SO2", null, null, null));
        frutexaSpecies.put("Fera", new BioDetails(1632500, 6530000, 150, "CO2-Rich or CO2", null, 195.0, null));
        frutexaSpecies.put("Flabellum", new BioDetails(1808900, 7235600, 150, "Ammonia", null, null, null));
        frutexaSpecies.put("Flammasis", new BioDetails(10326000, 41304000, 150, "Ammonia", null, null, null));
        frutexaSpecies.put("Metallicum", new BioDetails(1632500, 6530000, 150, "Ammonia, CO2-Rich or CO2", null, 195.0, null));
        frutexaSpecies.put("Sponsae", new BioDetails(5988000, 23952000, 150, "Water, Water-Rich", null, null, null));
        GENUS_TO_SPECIES.put("Frutexa", frutexaSpecies);

        // Fumerola
        GENUS_TO_BIOME.put("Fumerola", "Planet:Any|Atmosphere:Any|Gravity:Any|Temperature:Any|Volcanism:Water,Methane,CO2,Nitrogen,Ammonia,Silicate,Iron,Rocky|System:Any");
        Map<String, BioDetails> fumerolaSpecies = new HashMap<>();
        fumerolaSpecies.put("Aquatis", new BioDetails(6284600, 25138400, 100, null, null, null, "Water"));
        fumerolaSpecies.put("Carbosis", new BioDetails(6284600, 25138400, 100, null, null, null, "Methane, CO2"));
        fumerolaSpecies.put("Extremus", new BioDetails(16202800, 64811200, 100, null, null, null, "Silicate, Iron or Rocky"));
        fumerolaSpecies.put("Nitris", new BioDetails(7500900, 30003600, 100, null, null, null, "Nitrogen or Ammonia"));
        GENUS_TO_SPECIES.put("Fumerola", fumerolaSpecies);

        // Fungoida
        GENUS_TO_BIOME.put("Fungoida", "Planet:Rocky,High Metal Content|Atmosphere:Argon,Argon-Rich,CO2,CO2-Rich,Water,Ammonia,Methane,Methane-Rich|Gravity:Any|Temperature:180-195K|Volcanism:None|System:Any");
        Map<String, BioDetails> fungoidaSpecies = new HashMap<>();
        fungoidaSpecies.put("Bullarum", new BioDetails(3703200, 14812800, 300, "Argon, Argon-Rich", null, null, null));
        fungoidaSpecies.put("Gelata", new BioDetails(3330300, 13321200, 300, "CO2-Rich or CO2, Water", 180.0, 195.0, null));
        fungoidaSpecies.put("Setisis", new BioDetails(1670100, 6680400, 300, "Ammonia, Methane, Methane-Rich", null, null, null));
        fungoidaSpecies.put("Stabitis", new BioDetails(2680300, 10721200, 300, "CO2-Rich or CO2, Water", 180.0, 195.0, null));
        GENUS_TO_SPECIES.put("Fungoida", fungoidaSpecies);

        // Osseus
        GENUS_TO_BIOME.put("Osseus", "Planet:Rocky,High Metal Content|Atmosphere:CO2,CO2-Rich,Water,Water-Rich,Ammonia,Methane,Methane-Rich,Argon,Argon-Rich,Nitrogen|Gravity:Any|Temperature:180-195K|Volcanism:None|System:Any");
        Map<String, BioDetails> osseusSpecies = new HashMap<>();
        osseusSpecies.put("Cornibus", new BioDetails(1483000, 5932000, 800, "CO2-Rich or CO2", 180.0, 195.0, null));
        osseusSpecies.put("Discus", new BioDetails(12934900, 51739600, 800, "Water, Water-Rich", null, null, null));
        osseusSpecies.put("Fractus", new BioDetails(4027800, 16111200, 800, "CO2-Rich or CO2", 180.0, 190.0, null));
        osseusSpecies.put("Pellebantus", new BioDetails(9739000, 38956000, 800, "CO2-Rich or CO2", 190.0, 195.0, null));
        osseusSpecies.put("Pumice", new BioDetails(3156300, 12625200, 800, "Methane, Methane-Rich, Argon, Argon-Rich, Nitrogen", null, null, null));
        osseusSpecies.put("Spiralis", new BioDetails(2404700, 9618800, 800, "Ammonia", null, null, null));
        GENUS_TO_SPECIES.put("Osseus", osseusSpecies);

        // Recepta
        GENUS_TO_BIOME.put("Recepta", "Planet:Any|Atmosphere:SO2|Gravity:<=0.27|Temperature:Any|Volcanism:None|System:Any");
        Map<String, BioDetails> receptaSpecies = new HashMap<>();
        receptaSpecies.put("Conditivus", new BioDetails(14313700, 57254800, 150, "SO2", null, null, null));
        receptaSpecies.put("Deltahedronix", new BioDetails(16202800, 64811200, 150, "SO2", null, null, null));
        receptaSpecies.put("Umbrux", new BioDetails(12934900, 51739600, 150, "SO2", null, null, null));
        GENUS_TO_SPECIES.put("Recepta", receptaSpecies);

        // Sinuous Tuber
        GENUS_TO_BIOME.put("Sinuous Tuber", "Planet:Any|Atmosphere:None|Gravity:Any|Temperature:Any|Volcanism:Any|System:Galactic Core Preferred");
        Map<String, BioDetails> sinuousTuberSpecies = new HashMap<>();
        sinuousTuberSpecies.put("Albidum", new BioDetails(3425600, 13702400, 100, "None", null, null, "Any"));
        sinuousTuberSpecies.put("Blatteum", new BioDetails(1514500, 6058000, 100, "None", null, null, "Any"));
        sinuousTuberSpecies.put("Caeruleum", new BioDetails(1514500, 6058000, 100, "None", null, null, "Any"));
        sinuousTuberSpecies.put("Lindigoticum", new BioDetails(1514500, 6058000, 100, "None", null, null, "Any"));
        sinuousTuberSpecies.put("Prasinum", new BioDetails(1514500, 6058000, 100, "None", null, null, "Any"));
        sinuousTuberSpecies.put("Roseus", new BioDetails(1514500, 6058000, 100, "None", null, null, "Silicate Magma"));
        sinuousTuberSpecies.put("Violaceum", new BioDetails(1514500, 6058000, 100, "None", null, null, "Any"));
        sinuousTuberSpecies.put("Viride", new BioDetails(1514500, 6058000, 100, "None", null, null, "Any"));
        GENUS_TO_SPECIES.put("Sinuous Tuber", sinuousTuberSpecies);

        // Stratum
        GENUS_TO_BIOME.put("Stratum", "Planet:Rocky,High Metal Content|Atmosphere:SO2,CO2,CO2-Rich,Ammonia,Water,Water-Rich|Gravity:Any|Temperature:>=165K|Volcanism:None|System:Any");
        Map<String, BioDetails> stratumSpecies = new HashMap<>();
        stratumSpecies.put("Araneamus", new BioDetails(2448900, 9795600, 500, "SO2", 165.0, null, null));
        stratumSpecies.put("Cucumisis", new BioDetails(16777215, 67108860, 500, "SO2, CO2-Rich or CO2", 190.0, null, null));
        stratumSpecies.put("Excutitus", new BioDetails(2448900, 9795600, 500, "SO2, CO2-Rich or CO2", 165.0, 190.0, null));
        stratumSpecies.put("Frigus", new BioDetails(2637500, 10550000, 500, "SO2, CO2-Rich or CO2", 190.0, null, null));
        stratumSpecies.put("Laminamus", new BioDetails(2788300, 11153200, 500, "Ammonia", 165.0, null, null));
        stratumSpecies.put("Limaxus", new BioDetails(1362000, 5448000, 500, "SO2, CO2-Rich or CO2", 165.0, 190.0, null));
        stratumSpecies.put("Paleas", new BioDetails(1362000, 5448000, 500, "Ammonia, Water, Water-Rich", 165.0, null, null));
        stratumSpecies.put("Tectonicas", new BioDetails(19010800, 76043200, 500, "Any", 165.0, null, null));
        GENUS_TO_SPECIES.put("Stratum", stratumSpecies);

        // Tubus
        GENUS_TO_BIOME.put("Tubus", "Planet:Rocky,High Metal Content|Atmosphere:CO2,CO2-Rich,Ammonia|Gravity:Any|Temperature:160-190K|Volcanism:None|System:Any");
        Map<String, BioDetails> tubusSpecies = new HashMap<>();
        tubusSpecies.put("Cavas", new BioDetails(11873200, 47492800, 800, "CO2, CO2-Rich", 160.0, 190.0, null));
        tubusSpecies.put("Compagibus", new BioDetails(7774700, 31098800, 800, "CO2, CO2-Rich", 160.0, 190.0, null));
        tubusSpecies.put("Conifer", new BioDetails(2415500, 9662000, 800, "CO2, CO2-Rich", 160.0, 190.0, null));
        tubusSpecies.put("Rosarium", new BioDetails(2637500, 10550000, 800, "Ammonia", 160.0, null, null));
        tubusSpecies.put("Sororibus", new BioDetails(5727600, 22910400, 800, "Ammonia, CO2, CO2-Rich", 160.0, 190.0, null));
        GENUS_TO_SPECIES.put("Tubus", tubusSpecies);

        // Roseum Sinuous Tubers (likely a misnamed Sinuous Tuber entry)
        GENUS_TO_BIOME.put("Roseum Sinuous Tubers", "Planet:Any|Atmosphere:None|Gravity:Any|Temperature:Any|Volcanism:Any|System:Galactic Core Preferred");
        Map<String, BioDetails> blatteumSpecies = new HashMap<>();
        blatteumSpecies.put("Blatteum Sinuous Tubers", new BioDetails(1514500, 6058000, 800, "None", null, null, "Any"));
        GENUS_TO_SPECIES.put("Roseum Sinuous Tubers", blatteumSpecies);

        // Tussock
        GENUS_TO_BIOME.put("Tussock", "Planet:Rocky|Atmosphere:CO2,CO2-Rich,Methane,Methane-Rich,Argon,Argon-Rich,Ammonia,Water,Water-Rich,SO2|Gravity:Any|Temperature:145-195K|Volcanism:None|System:Any");
        Map<String, BioDetails> tussockSpecies = new HashMap<>();
        tussockSpecies.put("Albata", new BioDetails(3252500, 13010000, 200, "CO2, CO2-Rich", 175.0, 180.0, null));
        tussockSpecies.put("Capillum", new BioDetails(7025800, 28103200, 200, "Methane, Methane-Rich, Argon, Argon-Rich", null, null, null));
        tussockSpecies.put("Caputus", new BioDetails(3472400, 13889600, 200, "CO2, CO2-Rich", 180.0, 190.0, null));
        tussockSpecies.put("Catena", new BioDetails(1766600, 7066400, 200, "Ammonia", null, null, null));
        tussockSpecies.put("Cultro", new BioDetails(1766600, 7066400, 200, "Ammonia", null, null, null));
        tussockSpecies.put("Divisa", new BioDetails(1766600, 7066400, 200, "Ammonia", null, null, null));
        tussockSpecies.put("Ignis", new BioDetails(1849000, 7396000, 200, "CO2, CO2-Rich", 160.0, 170.0, null));
        tussockSpecies.put("Pennata", new BioDetails(5853800, 23415200, 200, "CO2, CO2-Rich", 145.0, 155.0, null));
        tussockSpecies.put("Pennatis", new BioDetails(1000000, 4000000, 200, "CO2, CO2-Rich", null, 195.0, null));
        tussockSpecies.put("Propagito", new BioDetails(1000000, 4000000, 200, "CO2, CO2-Rich", null, 195.0, null));
        tussockSpecies.put("Serrati", new BioDetails(4447100, 17788400, 200, "CO2, CO2-Rich", 170.0, 175.0, null));
        tussockSpecies.put("Stigmasis", new BioDetails(19010800, 76043200, 200, "Sulphur dioxide", null, null, null));
        tussockSpecies.put("Triticum", new BioDetails(7774700, 31098800, 200, "CO2, CO2-Rich", 190.0, 195.0, null));
        tussockSpecies.put("Ventusa", new BioDetails(3277700, 13110800, 200, "CO2, CO2-Rich", 155.0, 160.0, null));
        tussockSpecies.put("Virgam", new BioDetails(14313700, 57254800, 200, "Water, Water-Rich", null, null, null));
        GENUS_TO_SPECIES.put("Tussock", tussockSpecies);
    }

    /**
     * Get details for a specific species within a genus.
     * @param genus The genus name (case-sensitive).
     * @param species The species name (e.g., "Concha Renibus - Blue"), which may include the genus prefix and color suffix.
     * @return BioDetails or null if not found.
     */
    public static BioDetails getDetails(String genus, String species) {
        String capitalizedGenus = capitalizeWords(genus);
        Map<String, BioDetails> speciesMap = GENUS_TO_SPECIES.get(capitalizedGenus);
        if (speciesMap == null) {
            return null;
        }
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

    public static ProjectedPayment getAverageProjectedPayment(String genus) {
        String capitalizedGenus = capitalizeWords(genus);
        Map<String, BioDetails> speciesMap = GENUS_TO_SPECIES.get(capitalizedGenus);
        if (speciesMap == null || speciesMap.isEmpty()) {
            return null;
        }
        long creditValue = 0;
        long firstDiscovery = 0;
        for (BioDetails details : speciesMap.values()) {
            creditValue += details.creditValue();
            firstDiscovery += details.firstDiscoveryBonus();
        }
        return new ProjectedPayment(creditValue / speciesMap.size(), firstDiscovery / speciesMap.size());
    }

    public static ProjectedPayment getProjectedPayment(String genus, String species) {
        String capitalizedGenus = capitalizeWords(genus);
        Map<String, BioDetails> speciesMap = GENUS_TO_SPECIES.get(capitalizedGenus);
        if (speciesMap == null || speciesMap.isEmpty()) {
            return null;
        }

        BioDetails bioDetails = speciesMap.get(capitalizeWords(species));
        return bioDetails == null ? null : new ProjectedPayment(bioDetails.creditValue(), bioDetails.firstDiscoveryBonus());
    }


    public record ProjectedPayment(Long payment, Long firstDiscoveryBonus) {
    }

    public static Map<String, String> getGenusToBiome() {
        return GENUS_TO_BIOME;
    }

    public static int getDistance(String genus){
        String capitalizedGenus = capitalizeWords(genus);
        Map<String, BioDetails> speciesMap = GENUS_TO_SPECIES.get(capitalizedGenus);
        if (speciesMap == null || speciesMap.isEmpty()) {
            return 0;
        }

        // Get the first species entry - all species in a genus share the same colony range
        return speciesMap.values().iterator().next().colonyRange();
    }
}