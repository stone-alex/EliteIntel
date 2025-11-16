package elite.intel.ai.search.spansh.stellarobjects;

import com.google.gson.annotations.SerializedName;
import elite.intel.gameapi.gamestate.dtos.BaseJsonDto;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;

public class StellarObjectSearchResultDto extends BaseJsonDto implements ToJsonConvertible {

    @SerializedName("count")
    private int count;

    @SerializedName("from")
    private int from;

    @SerializedName("reference")
    private Reference reference;

    @SerializedName("results")
    private List<Result> results;

    @SerializedName("search")
    private Search search;

    @SerializedName("search_reference")
    private String search_reference;

    @SerializedName("size")
    private int size;


    public int getCount() {
        return count;
    }

    public int getFrom() {
        return from;
    }

    public Reference getReference() {
        return reference;
    }

    public List<Result> getResults() {
        return results;
    }

    public Search getSearch() {
        return search;
    }

    public String getSearch_reference() {
        return search_reference;
    }

    public int getSize() {
        return size;
    }

    public static class Reference {

        @SerializedName("id64")
        private long id64;

        @SerializedName("name")
        private String name;

        @SerializedName("x")
        private double x;

        @SerializedName("y")
        private double y;

        @SerializedName("z")
        private double z;

        public long getId64() {
            return id64;
        }

        public String getName() {
            return name;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }
    }

    public static class Result implements ToJsonConvertible {

        @SerializedName("arg_of_periapsis")
        private double arg_of_periapsis;

        @SerializedName("ascending_node")
        private Double ascending_node;

        @SerializedName("atmosphere")
        private String atmosphere;

        @SerializedName("atmosphere_composition")
        private List<Composition> atmosphere_composition;

        @SerializedName("axis_tilt")
        private double axis_tilt;

        @SerializedName("body_id")
        private int body_id;

        @SerializedName("distance")
        private double distance;

        @SerializedName("distance_to_arrival")
        private double distance_to_arrival;

        @SerializedName("earth_masses")
        private double earth_masses;

        @SerializedName("estimated_mapping_value")
        private int estimated_mapping_value;

        @SerializedName("estimated_scan_value")
        private int estimated_scan_value;

        @SerializedName("gravity")
        private double gravity;

        @SerializedName("id")
        private String id;

        @SerializedName("id64")
        private long id64;

        @SerializedName("is_landable")
        private boolean is_landable;

        @SerializedName("is_main_star")
        private Boolean is_main_star;

        @SerializedName("is_rotational_period_tidally_locked")
        private boolean is_rotational_period_tidally_locked;

        @SerializedName("mean_anomaly")
        private Double mean_anomaly;

        @SerializedName("name")
        private String name;

        @SerializedName("orbital_eccentricity")
        private double orbital_eccentricity;

        @SerializedName("orbital_inclination")
        private double orbital_inclination;

        @SerializedName("orbital_period")
        private double orbital_period;

        @SerializedName("orbital_synchronicity")
        private Double orbital_synchronicity;

        @SerializedName("parents")
        private List<Parent> parents;

        @SerializedName("radius")
        private double radius;

        @SerializedName("reserve_level")
        private String reserve_level;

        @SerializedName("rings")
        private List<Ring> rings;

        @SerializedName("rotational_period")
        private double rotational_period;

        @SerializedName("semi_major_axis")
        private double semi_major_axis;

        @SerializedName("solid_composition")
        private List<Composition> solid_composition;

        @SerializedName("subtype")
        private String subtype;

        @SerializedName("surface_pressure")
        private double surface_pressure;

        @SerializedName("surface_temperature")
        private double surface_temperature;

        @SerializedName("system_id64")
        private long system_id64;

        @SerializedName("system_name")
        private String system_name;

        @SerializedName("system_region")
        private String system_region;

        @SerializedName("system_x")
        private double system_x;

        @SerializedName("system_y")
        private double system_y;

        @SerializedName("system_z")
        private double system_z;

        @SerializedName("terraforming_state")
        private String terraforming_state;

        @SerializedName("type")
        private String type;

        @SerializedName("updated_at")
        private String updated_at;

        @SerializedName("volcanism_type")
        private String volcanism_type;

        public double getArg_of_periapsis() {
            return arg_of_periapsis;
        }

        public Double getAscending_node() {
            return ascending_node;
        }

        public String getAtmosphere() {
            return atmosphere;
        }

        public List<Composition> getAtmosphere_composition() {
            return atmosphere_composition;
        }

        public double getAxis_tilt() {
            return axis_tilt;
        }

        public int getBody_id() {
            return body_id;
        }

        public double getDistance() {
            return distance;
        }

        public double getDistance_to_arrival() {
            return distance_to_arrival;
        }

        public double getEarth_masses() {
            return earth_masses;
        }

        public int getEstimated_mapping_value() {
            return estimated_mapping_value;
        }

        public int getEstimated_scan_value() {
            return estimated_scan_value;
        }

        public double getGravity() {
            return gravity;
        }

        public String getId() {
            return id;
        }

        public long getId64() {
            return id64;
        }

        public boolean isIs_landable() {
            return is_landable;
        }

        public Boolean getIs_main_star() {
            return is_main_star;
        }

        public boolean isIs_rotational_period_tidally_locked() {
            return is_rotational_period_tidally_locked;
        }

        public Double getMean_anomaly() {
            return mean_anomaly;
        }

        public String getName() {
            return name;
        }

        public double getOrbital_eccentricity() {
            return orbital_eccentricity;
        }

        public double getOrbital_inclination() {
            return orbital_inclination;
        }

        public double getOrbital_period() {
            return orbital_period;
        }

        public Double getOrbital_synchronicity() {
            return orbital_synchronicity;
        }

        public List<Parent> getParents() {
            return parents;
        }

        public double getRadius() {
            return radius;
        }

        public String getReserve_level() {
            return reserve_level;
        }

        public List<Ring> getRings() {
            return rings;
        }

        public double getRotational_period() {
            return rotational_period;
        }

        public double getSemi_major_axis() {
            return semi_major_axis;
        }

        public List<Composition> getSolid_composition() {
            return solid_composition;
        }

        public String getSubtype() {
            return subtype;
        }

        public double getSurface_pressure() {
            return surface_pressure;
        }

        public double getSurface_temperature() {
            return surface_temperature;
        }

        public long getSystem_id64() {
            return system_id64;
        }

        public String getSystem_name() {
            return system_name;
        }

        public String getSystem_region() {
            return system_region;
        }

        public double getSystem_x() {
            return system_x;
        }

        public double getSystem_y() {
            return system_y;
        }

        public double getSystem_z() {
            return system_z;
        }

        public String getTerraforming_state() {
            return terraforming_state;
        }

        public String getType() {
            return type;
        }

        public String getUpdated_at() {
            return updated_at;
        }

        public String getVolcanism_type() {
            return volcanism_type;
        }

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

        @Override public String toString() {
            return toJson();
        }
    }

    public static class Composition {

        @SerializedName("name")
        private String name;

        @SerializedName("share")
        private double share;

        public String getName() {
            return name;
        }

        public double getShare() {
            return share;
        }
    }

    public static class Parent {

        @SerializedName("id64")
        private long id64;

        @SerializedName("subtype")
        private String subtype;

        @SerializedName("type")
        private String type;

        public long getId64() {
            return id64;
        }

        public String getSubtype() {
            return subtype;
        }

        public String getType() {
            return type;
        }
    }

    public static class Ring {

        @SerializedName("inner_radius")
        private double inner_radius;

        @SerializedName("mass")
        private double mass;

        @SerializedName("name")
        private String name;

        @SerializedName("outer_radius")
        private double outer_radius;

        @SerializedName("signal_count")
        private Integer signal_count;

        @SerializedName("signals")
        private List<Signal> signals;

        @SerializedName("signals_updated_at")
        private String signals_updated_at;

        @SerializedName("type")
        private String type;

        public double getInner_radius() {
            return inner_radius;
        }

        public double getMass() {
            return mass;
        }

        public String getName() {
            return name;
        }

        public double getOuter_radius() {
            return outer_radius;
        }

        public Integer getSignal_count() {
            return signal_count;
        }

        public List<Signal> getSignals() {
            return signals;
        }

        public String getSignals_updated_at() {
            return signals_updated_at;
        }

        public String getType() {
            return type;
        }
    }

    public static class Signal {

        @SerializedName("count")
        private int count;

        @SerializedName("name")
        private String name;

        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
        }
    }

    public static class Search {

        @SerializedName("filters")
        private Filters filters;

        @SerializedName("page")
        private int page;

        @SerializedName("size")
        private int size;

        @SerializedName("sort")
        private List<Object> sort;

        public Filters getFilters() {
            return filters;
        }

        public int getPage() {
            return page;
        }

        public int getSize() {
            return size;
        }

        public List<Object> getSort() {
            return sort;
        }
    }

    public static class Filters {

        @SerializedName("reserve_level")
        private ReserveLevel reserve_level;

        @SerializedName("ring_signals")
        private List<RingSignal> ring_signals;

        public ReserveLevel getReserve_level() {
            return reserve_level;
        }

        public List<RingSignal> getRing_signals() {
            return ring_signals;
        }
    }

    public static class ReserveLevel {

        @SerializedName("value")
        private List<String> value;

        public List<String> getValue() {
            return value;
        }
    }

    public static class RingSignal {

        @SerializedName("comparison")
        private String comparison;

        @SerializedName("count")
        private List<Integer> count;

        @SerializedName("name")
        private String name;

        public String getComparison() {
            return comparison;
        }

        public List<Integer> getCount() {
            return count;
        }

        public String getName() {
            return name;
        }
    }
}