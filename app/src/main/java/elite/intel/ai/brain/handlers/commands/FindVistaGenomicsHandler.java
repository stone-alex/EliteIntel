package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.dao.LocationDao;
import elite.intel.db.managers.LocationManager;
import elite.intel.search.spansh.station.vista.VistaGenomicsLocationDto;
import elite.intel.search.spansh.station.vista.VistaGenomicsSearch;
import elite.intel.search.spansh.station.vista.VistaSearchCriteria;
import elite.intel.db.managers.ReminderManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.util.json.GetNumberFromParam;

import java.util.List;
import java.util.Optional;

public class FindVistaGenomicsHandler extends CommandOperator implements CommandHandler {

    private final GameController controller;

    public FindVistaGenomicsHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.controller = controller;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Number range = GetNumberFromParam.extractRangeParameter(params, 250);
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Searching for Vista Genomics... Stand by..."));


        VistaSearchCriteria criteria = new VistaSearchCriteria();
        VistaSearchCriteria.Filters filters = new VistaSearchCriteria.Filters();
        VistaSearchCriteria.Service service = new VistaSearchCriteria.Service();
        service.setName(List.of("Vista Genomics"));
        filters.setServices(List.of(service));

        VistaSearchCriteria.Distance distance = new VistaSearchCriteria.Distance();
        distance.setMin(0);
        distance.setMax(range.intValue());
        filters.setDistance(distance);
        criteria.setFilters(filters);

        LocationDao.Coordinates galacticCoordinates = LocationManager.getInstance().getGalacticCoordinates();

        VistaSearchCriteria.ReferenceCoords coords = new VistaSearchCriteria.ReferenceCoords();
        coords.setX(galacticCoordinates.x());
        coords.setY(galacticCoordinates.y());
        coords.setZ(galacticCoordinates.z());
        criteria.setReferenceCoords(coords);

        List<VistaGenomicsLocationDto.Result> results = VistaGenomicsSearch.findVistaGenomics(criteria);
        if (results == null || results.isEmpty()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("No Vista Genomics found."));
            return;
        }

        Optional<VistaGenomicsLocationDto.Result> first = results.stream().findFirst();
        RoutePlotter routePlotter = new RoutePlotter(this.controller);
        VistaGenomicsLocationDto.Result result = first.get();

        String reminder = "Head to " + result.getSystemName() + " star system. When you get there looks for" + result.getStationName();
        ReminderManager.getInstance().setDestination(reminder);
        EventBusManager.publish(new MissionCriticalAnnouncementEvent(reminder));
        routePlotter.plotRoute(result.getSystemName());
    }
}
