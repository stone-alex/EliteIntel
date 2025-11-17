package elite.intel.ai.search.spansh.stellarobjects;

import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;

import java.io.IOException;
import java.util.Arrays;

public class StellarObjectSearch {

    private static StellarObjectSearch instance;

    private StellarObjectSearch() {
    }

    public static StellarObjectSearch getInstance() {
        if (instance == null) {
            instance = new StellarObjectSearch();
        }
        return instance;
    }


    public StellarObjectSearchResultDto findRings(String material, ReserveLevel level, PlayerSession.GalacticCoordinates coords, int range) {

        try {
            StellarObjectSearchClient searchClient = StellarObjectSearchClient.getInstance();
            StellarObjectSearchRequestDto criteria = new StellarObjectSearchRequestDto();

            StellarObjectSearchRequestDto.ReserveLevel reserveLevel = new StellarObjectSearchRequestDto.ReserveLevel();
            reserveLevel.setValue(Arrays.asList(level.getType()));

            StellarObjectSearchRequestDto.Filters filters = new StellarObjectSearchRequestDto.Filters();
            filters.setReserveLevel(reserveLevel);
            StellarObjectSearchRequestDto.Distance distance = new StellarObjectSearchRequestDto.Distance();
            distance.setMin(0);
            distance.setMax(range);
            filters.setDistance(distance);

            StellarObjectSearchRequestDto.RingSignal ringSignal = new StellarObjectSearchRequestDto.RingSignal();
            ringSignal.setCount(Arrays.asList(1, 17));
            ringSignal.setComparison("<=>");
            ringSignal.setName(material);

            filters.setRingSignals(Arrays.asList(ringSignal));


            criteria.setSize(10);
            criteria.setPage(1);
            criteria.setFilters(filters);

            StellarObjectSearchRequestDto.ReferenceCoords referenceCoords = new StellarObjectSearchRequestDto.ReferenceCoords();
            referenceCoords.setX(coords.x());
            referenceCoords.setY(coords.y());
            referenceCoords.setZ(coords.z());
            criteria.setReferenceCoords(referenceCoords);

            return GsonFactory.getGson().fromJson(searchClient.performSearch(criteria), StellarObjectSearchResultDto.class);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}