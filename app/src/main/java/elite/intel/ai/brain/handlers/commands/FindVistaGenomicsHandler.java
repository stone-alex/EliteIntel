package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.hands.GameController;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.search.spansh.station.vista.VistaGenomicsLocationDto;
import elite.intel.search.spansh.station.vista.VistaGenomicsSearch;
import elite.intel.search.spansh.station.vista.VistaSearchCriteria;
import elite.intel.db.managers.DestinationReminderManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GetNumberFromParam;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FindVistaGenomicsHandler extends CommandOperator implements CommandHandler {

    private final GameController controller;

    public FindVistaGenomicsHandler(GameController controller) {
        super(controller.getMonitor(), controller.getExecutor());
        this.controller = controller;
    }

    @Override public void handle(String action, JsonObject params, String responseText) {
        Number range = GetNumberFromParam.getNumberFromParam(params, 250);
        EventBusManager.publish(new AiVoxResponseEvent("Searching for Vista Genomics... Stand by..."));


        VistaSearchCriteria criteria = new VistaSearchCriteria();
        VistaSearchCriteria.Filters filters = new VistaSearchCriteria.Filters();
        VistaSearchCriteria.Service service = new VistaSearchCriteria.Service();
        service.setName(Arrays.asList("Vista Genomics"));
        filters.setServices(Arrays.asList(service));

        VistaSearchCriteria.Distance distance = new VistaSearchCriteria.Distance();
        distance.setMin(0);
        distance.setMax(range.intValue());
        filters.setDistance(distance);
        criteria.setFilters(filters);

        PlayerSession playerSession = PlayerSession.getInstance();
        PlayerSession.GalacticCoordinates galacticCoordinates = playerSession.getGalacticCoordinates();

        VistaSearchCriteria.ReferenceCoords coords = new VistaSearchCriteria.ReferenceCoords();
        coords.setX(galacticCoordinates.x());
        coords.setY(galacticCoordinates.y());
        coords.setZ(galacticCoordinates.z());
        criteria.setReferenceCoords(coords);

        List<VistaGenomicsLocationDto.Result> results = VistaGenomicsSearch.findVistaGenomics(criteria);
        if (results == null || results.isEmpty()) {
            EventBusManager.publish(new AiVoxResponseEvent("No Vista Genomics found."));
            return;
        }

        Optional<VistaGenomicsLocationDto.Result> first = results.stream().findFirst();
        RoutePlotter routePlotter = new RoutePlotter(this.controller);
        VistaGenomicsLocationDto.Result result = first.get();
        DestinationReminderManager.getInstance().setDestination(result.toJson());
        EventBusManager.publish(new AiVoxResponseEvent("Head to " + result.getSystemName() + " star system. When you get there looks for" + result.getStationName()));
        routePlotter.plotRoute(result.getSystemName());
    }
}
