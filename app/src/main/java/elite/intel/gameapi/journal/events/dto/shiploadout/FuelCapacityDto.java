package elite.intel.gameapi.journal.events.dto.shiploadout;

import elite.intel.gameapi.gamestate.dtos.BaseJsonDto;
import elite.intel.util.json.ToJsonConvertible;

public class FuelCapacityDto extends BaseJsonDto implements ToJsonConvertible {

    private double mainTank;
    private double reserveTank;

    public double getMainTank() {
        return mainTank;
    }

    public void setMainTank(double mainTank) {
        this.mainTank = mainTank;
    }

    public double getReserveTank() {
        return reserveTank;
    }

    public void setReserveTank(double reserveTank) {
        this.reserveTank = reserveTank;
    }
}
