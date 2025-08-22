package elite.companion.session;

/**
 * Temporary storage for player stats. This data is kept in memory and is reset on every new play session.
 * */
public class PlayerStats {

    private String playerName;
    private long creditBalance;
    private String carrierBalance;
    private String carrierReserve;
    private String currentShip;
    private String currentStarSystem;
    private String currentShipName;


    public String getPlayerName() {
        if( playerName == null) return "";
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public long getCreditBalance() {
        return creditBalance;
    }

    public void setCreditBalance(long creditBalance) {
        this.creditBalance = creditBalance;
    }

    public String getCarrierBalance() {
        return carrierBalance;
    }

    public void setCarrierBalance(String carrierBalance) {
        this.carrierBalance = carrierBalance;
    }

    public String getCarrierReserve() {
        return carrierReserve;
    }

    public void setCarrierReserve(String carrierReserve) {
        this.carrierReserve = carrierReserve;
    }

    public String getCurrentShip() {
        return currentShip;
    }

    public void setCurrentShip(String currentShip) {
        this.currentShip = currentShip;
    }

    public String getCurrentStarSystem() {
        return currentStarSystem;
    }

    public void setCurrentStarSystem(String currentStarSystem) {
        this.currentStarSystem = currentStarSystem;
    }

    public String getCurrentShipName() {
        return currentShipName;
    }

    public void setCurrentShipName(String currentShipName) {
        this.currentShipName = currentShipName;
    }
}
