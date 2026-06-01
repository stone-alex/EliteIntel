package elite.intel.gameapi;

import elite.intel.db.dao.ShipSettingsDao;

import java.util.HashMap;
import java.util.Map;

public class FireGroups {
    public static final Map<String, Integer> fireGroups = new HashMap<>();

    static {
        fireGroups.put("A", 0);
        fireGroups.put("B", 1);
        fireGroups.put("C", 2);
        fireGroups.put("D", 3);
        fireGroups.put("E", 4);
        fireGroups.put("F", 5);
        fireGroups.put("G", 6);
        fireGroups.put("H", 7);
        fireGroups.put("I", 8);
        fireGroups.put("J", 9);
        fireGroups.put("K", 10);
        fireGroups.put("L", 11);
        fireGroups.put("M", 12);
        fireGroups.put("N", 13);
        fireGroups.put("O", 14);
        fireGroups.put("P", 15);
        fireGroups.put("Q", 16);
        fireGroups.put("R", 17);
        fireGroups.put("S", 18);
        fireGroups.put("T", 19);
        fireGroups.put("U", 20);
        fireGroups.put("V", 21);
        fireGroups.put("W", 22);
        fireGroups.put("X", 23);
        fireGroups.put("Y", 24);
        fireGroups.put("Z", 25);
    }

    static final Map<String, String> natoAlphabet = new HashMap<>();

    static {
        natoAlphabet.put("alpha", "A");
        natoAlphabet.put("bravo", "B");
        natoAlphabet.put("charlie", "C");
        natoAlphabet.put("delta", "D");
        natoAlphabet.put("echo", "E");
        natoAlphabet.put("foxtrot", "F");
        natoAlphabet.put("golf", "G");
        natoAlphabet.put("hotel", "H");
        natoAlphabet.put("india", "I");
        natoAlphabet.put("juliett", "J");
        natoAlphabet.put("kilo", "K");
        natoAlphabet.put("lima", "L");
        natoAlphabet.put("mike", "M");
        natoAlphabet.put("november", "N");
        natoAlphabet.put("oscar", "O");
        natoAlphabet.put("papa", "P");
        natoAlphabet.put("quebec", "Q");
        natoAlphabet.put("romeo", "R");
        natoAlphabet.put("sierra", "S");
        natoAlphabet.put("tango", "T");
        natoAlphabet.put("uniform", "U");
        natoAlphabet.put("victor", "V");
        natoAlphabet.put("whiskey", "W");
        natoAlphabet.put("x-ray", "X");
        natoAlphabet.put("yankee", "Y");
        natoAlphabet.put("zulu", "Z");
    }

    public static int fireGroupInSettings(ShipSettingsDao.ShipSettings settings) {
        String fireGroup = settings.getHonkFireGroup();
        if (fireGroup == null) return 0;
        Integer result = fireGroups.get(fireGroup);
        return result == null ? 0 : result;
    }

    private static String getNato(String key) {
        if (key == null) return null;
        return natoAlphabet.get(key.trim());
    }

    public static int fireGroupByNato(String nato) {
        if (nato == null) return -1;
        nato = getNato(nato);
        if (nato == null) return 0;
        Integer result = fireGroups.get(nato);
        return result == null ? 0 : result;
    }
}
