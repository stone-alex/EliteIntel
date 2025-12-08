package elite.intel.util;

public class OsDetector {

    public static String os = System.getProperty("os.name").toLowerCase();

    public static OS getOs() {
        return OS.LINUX.getOs().equals(os) ? OS.LINUX : OS.WINDOWS;
    }

    public enum OS {
        WINDOWS("windows"), LINUX("linux"), MAC("mac");

        private String os;

        OS(String os) {
            this.os = os;
        }

        public String getOs() {
            return os;
        }
    }
}
