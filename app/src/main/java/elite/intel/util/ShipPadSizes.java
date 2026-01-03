package elite.intel.util;

import java.util.Map;

public class ShipPadSizes {

    public static final Map<String, String> PAD_SIZES = Map.ofEntries(
            // Small pads
            Map.entry("sidewinder", "S"),
            Map.entry("eagle", "S"),
            Map.entry("hauler", "S"),
            Map.entry("adder", "S"),
            Map.entry("imperial eagle", "S"),
            Map.entry("viper mkiii", "S"),
            Map.entry("diamondback scout", "S"),
            Map.entry("dolphin", "S"),
            Map.entry("diamondback explorer", "S"),
            Map.entry("imperial courier", "S"),
            Map.entry("vulture", "S"),
            Map.entry("viper", "S"),  // Alias for Mk IV
            Map.entry("cobramkiii", "S"),  // Cobra Mk III
            Map.entry("cobramkiv", "S"),  // Cobra Mk IV
            Map.entry("cobramkv", "S"),   // Cobra Mk V

            // Medium pads
            Map.entry("type-6 transporter", "M"),
            Map.entry("keelback", "M"),
            Map.entry("asp scout", "M"),
            Map.entry("asp explorer", "M"),
            Map.entry("federal dropship", "M"),
            Map.entry("mandalay", "M"),
            Map.entry("alliance chieftain", "M"),
            Map.entry("federal assault ship", "M"),
            Map.entry("alliance crusader", "M"),
            Map.entry("alliance challenger", "M"),
            Map.entry("federal gunship", "M"),
            Map.entry("krait phantom", "M"),
            Map.entry("type-8 transporter", "M"),
            Map.entry("krait mkii", "M"),
            Map.entry("fer-de-lance", "M"),
            Map.entry("mamba", "M"),
            Map.entry("python", "M"),
            Map.entry("python mkii", "M"),
            Map.entry("type-11 prospector", "M"),
            Map.entry("corsair", "M"),

            // Large pads
            Map.entry("type-7 transporter", "L"),
            Map.entry("imperial clipper", "L"),
            Map.entry("orca", "L"),
            Map.entry("type-9 heavy", "L"),
            Map.entry("beluga liner", "L"),
            Map.entry("type-10 defender", "L"),
            Map.entry("anaconda", "L"),
            Map.entry("federal corvette", "L"),
            Map.entry("imperial cutter", "L"),
            Map.entry("panther clipper mkii", "L")
    );

    public static String getPadSize(String shipName){
        return PAD_SIZES.getOrDefault(shipName, "L");
    }
}