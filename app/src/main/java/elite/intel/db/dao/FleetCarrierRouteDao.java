package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RegisterRowMapper(FleetCarrierRouteDao.FleetCarrierRouteMapper.class)
public interface FleetCarrierRouteDao {

    @SqlUpdate("""
    INSERT INTO fleet_carrier_route 
            (leg, distance, systemName, fuelUsed, remainingFuel, hasIcyRing, isPristine, x, y, z) 
            VALUES (:leg, :distance, :systemName, :fuelUsed, :remainingFuel, :hasIcyRing, :pristine, :x, :y, :z)
    """)
    void save(@BindBean FleetCarrierRouteLeg leg);

    @SqlQuery("SELECT * FROM fleet_carrier_route")
    List<FleetCarrierRouteDao.FleetCarrierRouteLeg> getAll();

    @SqlUpdate("DELETE FROM fleet_carrier_route where leg = :leg")
    void delete(int leg);

    @SqlUpdate("DELETE FROM fleet_carrier_route")
    void clear();


    class FleetCarrierRouteMapper implements RowMapper<FleetCarrierRouteLeg> {

        @Override public FleetCarrierRouteLeg map(ResultSet rs, StatementContext ctx) throws SQLException {
            FleetCarrierRouteLeg route = new FleetCarrierRouteLeg();
            route.setLeg(rs.getInt("leg"));
            route.setDistance(rs.getDouble("distance"));
            route.setSystemName(rs.getString("systemName"));
            route.setFuelUsed(rs.getInt("fuelUsed"));
            route.setRemainingFuel(rs.getInt("remainingFuel"));
            route.setHasIcyRing(rs.getBoolean("hasIcyRing"));
            route.setPristine(rs.getBoolean("isPristine"));
            route.setX(rs.getDouble("x"));
            route.setY(rs.getDouble("y"));
            route.setZ(rs.getDouble("z"));
            return route;
        }
    }


    class FleetCarrierRouteLeg {
        private Integer leg;
        private String systemName;
        private Double distance;
        private Integer fuelUsed;
        private Integer remainingFuel;
        private Boolean hasIcyRing;
        private Boolean pristine;
        private Double x, y, z;

        public FleetCarrierRouteLeg() {
        }

        public Integer getLeg() {
            return leg;
        }

        public void setLeg(Integer leg) {
            this.leg = leg;
        }

        public String getSystemName() {
            return systemName;
        }

        public void setSystemName(String systemName) {
            this.systemName = systemName;
        }

        public Double getDistance() {
            return distance;
        }

        public void setDistance(Double distance) {
            this.distance = distance;
        }

        public Integer getFuelUsed() {
            return fuelUsed;
        }

        public void setFuelUsed(Integer fuelUsed) {
            this.fuelUsed = fuelUsed;
        }

        public Integer getRemainingFuel() {
            return remainingFuel;
        }

        public void setRemainingFuel(Integer remainingFuel) {
            this.remainingFuel = remainingFuel;
        }

        public Boolean getHasIcyRing() {
            return hasIcyRing;
        }

        public void setHasIcyRing(Boolean hasIcyRing) {
            this.hasIcyRing = hasIcyRing;
        }

        public Boolean getPristine() {
            return pristine;
        }

        public void setPristine(Boolean pristine) {
            this.pristine = pristine;
        }

        public Double getX() {
            return x;
        }

        public void setX(Double x) {
            this.x = x;
        }

        public Double getY() {
            return y;
        }

        public void setY(Double y) {
            this.y = y;
        }

        public Double getZ() {
            return z;
        }

        public void setZ(Double z) {
            this.z = z;
        }
    }
}
