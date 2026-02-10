package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.brain.handlers.commands.CommandOperator;
import elite.intel.ai.hands.BindingsMonitor;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.hands.KeyBindingExecutor;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.SupercruiseDestinationDropEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;
import elite.intel.util.StringUtls;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_ACTIVATE_ANALYSIS_MODE;
import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.BINDING_ACTIVATE_COMBAT_MODE;

public class SuperCruiseDropSubscriber extends CommandOperator {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();
    private final Status status = Status.getInstance();

    public SuperCruiseDropSubscriber(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
    }

    @Subscribe
    public void onSuperCruiseDrop(SupercruiseDestinationDropEvent event) {

        boolean analysisMode = status.isAnalysisMode();

        if (event.getThreat() > 0) {
            EventBusManager.publish(new SensorDataEvent(" Dropped from supercruise. Threat level: " + event.getThreat() + ". ", "Notify user about supercruise exit and threat level"));
            if (event.getThreat() > 2 && analysisMode) {
                operateKeyboard(BINDING_ACTIVATE_COMBAT_MODE.getGameBinding(), 0);
            } else if (event.getThreat() < 2 && !analysisMode) {
                operateKeyboard(BINDING_ACTIVATE_ANALYSIS_MODE.getGameBinding(), 0);
            }
        }


        LocationDto location = locationManager.findByMarketId(event.getMarketID());
        if (location.getBodyId() < 1 && location.getSystemAddress() < 1) {
            location.setLocationType(LocationDto.LocationType.STATION);
            location.setSystemAddress(playerSession.getLocationData().getSystemAddress());
            locationManager.save(location);
        }

        String carrierName = playerSession.getCarrierData().getCarrierName();
        if (event.getType().toUpperCase().startsWith(carrierName.toUpperCase())) {
            EventBusManager.publish(new SensorDataEvent("Welcome Home to " + StringUtls.capitalizeWords(carrierName) + "! ", "We are back at base"));
        }
    }
}
