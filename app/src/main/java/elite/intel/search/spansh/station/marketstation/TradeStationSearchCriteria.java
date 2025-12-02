package elite.intel.search.spansh.station.marketstation;

import com.google.gson.annotations.SerializedName;
import elite.intel.gameapi.gamestate.dtos.BaseJsonDto;
import elite.intel.search.spansh.stellarobjects.StellarObjectSearchRequestDto;
import elite.intel.util.json.ToJsonConvertible;

import java.util.Collections;
import java.util.List;

public class TradeStationSearchCriteria extends BaseJsonDto implements ToJsonConvertible {

    @SerializedName("filters")
    private Filters filters = new Filters();

    @SerializedName("reference_coords")
    private ReferenceCoords referenceCoords;

    @SerializedName("sort")
    private List<Object> sort = Collections.emptyList();

    @SerializedName("size")
    private int size = 10;

    @SerializedName("page")
    private int page = 0;

    // === Setters for builder-style usage ===

    public void setReferenceCoords(ReferenceCoords referenceCoords) {
        this.referenceCoords = referenceCoords;
    }

    public void setFilters(Filters filters) {
        this.filters = filters;
    }

    public void setSort(List<Object> sort) {
        this.sort = sort != null ? sort : Collections.emptyList();
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setPage(int page) {
        this.page = page;
    }

    // === Nested classes ===


    public static class Distance {
        @SerializedName("min")
        private int min;
        @SerializedName("max")
        private int max;

        public void setMin(int min) {
            this.min = min;
        }

        public void setMax(int max) {
            this.max = max;
        }
    }

    public static class Filters {

        @SerializedName("distance_to_arrival")
        private RangeFilter distanceToArrival;

        @SerializedName("distance")
        private Distance distance;


        @SerializedName("small_pads")
        private RangeFilter smallPads;

        @SerializedName("medium_pads")
        private RangeFilter mediumPads;

        @SerializedName("large_pads")
        private RangeFilter largePads;

        // Setters
        public void setDistanceToArrival(RangeFilter distanceToArrival) {
            this.distanceToArrival = distanceToArrival;
        }

        public void setSmallPads(RangeFilter smallPads) {
            this.smallPads = smallPads;
        }

        public void setMediumPads(RangeFilter mediumPads) {
            this.mediumPads = mediumPads;
        }

        public void setLargePads(RangeFilter largePads) {
            this.largePads = largePads;
        }

        public void setDistanceToStarSystem(Distance distanceToStarSystem) {
            this.distance = distanceToStarSystem;
        }
    }

    public static class ReferenceCoords {

        @SerializedName("x")
        private int x;

        @SerializedName("y")
        private int y;

        @SerializedName("z")
        private int z;

        public ReferenceCoords() {}

        public ReferenceCoords(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        // Setters
        public void setX(int x) { this.x = x; }
        public void setY(int y) { this.y = y; }
        public void setZ(int z) { this.z = z; }
    }

    public static class RangeFilter {

        @SerializedName("comparison")
        private String comparison = "<=>";  // Spansh uses "<=>" for range

        @SerializedName("value")
        private int[] value = new int[2];

        public RangeFilter() {}

        public RangeFilter(int min, int max) {
            this.value[0] = min;
            this.value[1] = max;
        }

        public void setValue(int[] value) {
            this.value = value != null ? value : new int[]{0, 0};
        }

        public void setComparison(String comparison) {
            this.comparison = comparison;
        }
    }
}