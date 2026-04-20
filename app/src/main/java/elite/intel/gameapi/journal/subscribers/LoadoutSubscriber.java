package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.GoogleVoices;
import elite.intel.ai.mouth.kokoro.KokoroVoices;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.dao.ShipDao;
import elite.intel.db.managers.ShipManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.LoadoutEvent;
import elite.intel.gameapi.journal.events.dto.shiploadout.LoadoutConverter;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;
import elite.intel.session.SystemSession;
import elite.intel.util.Ranks;

import java.util.Objects;


public class LoadoutSubscriber {

    private final ShipManager shipManager = ShipManager.getInstance();
    private final SystemSession systemSession = SystemSession.getInstance();
    private final PlayerSession playerSession = PlayerSession.getInstance();

    @Subscribe
    public void onLoadoutEvent(LoadoutEvent event) {
        Thread.ofVirtual().start(() -> {

            ShipDao.Ship currentShip = shipManager.getShip();
            Status.getInstance().setOkToAnnounceLoadout(
                    currentShip != null && !Objects.equals(currentShip.getShipId(), event.getShipId())
            );


            playerSession.setCurrentShipName(event.getShipName());
            playerSession.setCurrentShip(event.getShip());
            playerSession.setShipLoadout(LoadoutConverter.toShipLoadOutDto(event));


            ShipDao.Ship ship = shipManager.getShipById(event.getShipId());
            if (ship == null) {
                String shipDefaultVoice = KokoroVoices.GEORGE.name();
                if (!systemSession.useLocalTTS()) {
                    shipDefaultVoice = GoogleVoices.STEVE.name();
                }
                shipManager.save(event.getShipId(), event.getShipName(), event.getCargoCapacity(), event.getShip(), shipDefaultVoice);
            } else {
                ship.setCargoCapacity(event.getCargoCapacity());
                ship.setShipIdentifier(event.getShip());
                ship.setShipName(event.getShipName());
                shipManager.saveShip(ship);
            }

            if (Status.getInstance().isOkToAnnounceLoadout()) {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("Hello " + playerSession.getPlayerName() + ", I am " + event.getShipName() + ", at your service " + Ranks.getPlayerHonorific()));
            }
        });
    }
}
