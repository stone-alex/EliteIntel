package elite.companion.session;

/**
 * Used to track player stats for the given play session. It is reset on every new play session.
 * */
public class SessionTracker {

    private static PlayerStats playerStats;

    public static PlayerStats getPlayerStats() {
        if (playerStats == null) {return new PlayerStats();}
        return playerStats;
    }

    public static void setPlayerStats(PlayerStats playerStats) {
        SessionTracker.playerStats = playerStats;
    }
}
