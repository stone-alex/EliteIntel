package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;


public interface StatusDao {


    @SqlUpdate("""
            INSERT OR REPLACE INTO player_status (id, timestamp, event, flags, flags2, fireGroup, guiFocus, cargo, latituge, longitude, heading, altitude, balance, planetRadius) 
            VALUES (1, :timestamp, :event, :flags, :flags2, :fireGroup, :guiFocus, :cargo, :latituge, :longitude, :heading, :altitude, :balance, :planetRadius)
    """)
    void save(@BindBean StatusDao.Status status);


    @SqlQuery("SELECT * FROM player_status WHERE id = 1")
    @RegisterRowMapper(StatusDao.StatusMapper.class)
    StatusDao.Status getStatus();


    class StatusMapper implements RowMapper<StatusDao.Status> {

        @Override public Status map(ResultSet rs, StatementContext ctx) throws SQLException {
            Status status = new Status();
            status.setTimestamp(rs.getString("timestamp"));
            status.setEvent(rs.getString("event"));
            status.setFlags(rs.getInt("flags"));
            status.setFlags2(rs.getInt("flags2"));
            status.setFireGroup(rs.getInt("fireGroup"));
            status.setGuiFocus(rs.getInt("guiFocus"));
            status.setCargo(rs.getDouble("cargo"));
            status.setLatituge(rs.getDouble("latituge"));
            status.setLongitude(rs.getDouble("longitude"));
            status.setHeading(rs.getInt("heading"));
            status.setAltitude(rs.getDouble("altitude"));
            status.setBalance(rs.getLong("balance"));
            status.setPlanetRadius(rs.getDouble("planetRadius"));

            return status;
        }
    }


    class Status {
        public Status() {
        }

        private String timestamp ="";
        private String event = "";
        private int flags =0;
        private int flags2=0;
        private int fireGroup=0;
        private int guiFocus=0;
        private double cargo=0.0;
        private double latituge=0.0;
        private double longitude=0.0;
        private int heading=0;
        private double altitude=0.0;
        private long balance=0;
        private double planetRadius=0.0;


        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public int getFlags() {
            return flags;
        }

        public void setFlags(int flags) {
            this.flags = flags;
        }

        public int getFlags2() {
            return flags2;
        }

        public void setFlags2(int flags2) {
            this.flags2 = flags2;
        }

        public int getFireGroup() {
            return fireGroup;
        }

        public void setFireGroup(int fireGroup) {
            this.fireGroup = fireGroup;
        }

        public int getGuiFocus() {
            return guiFocus;
        }

        public void setGuiFocus(int guiFocus) {
            this.guiFocus = guiFocus;
        }

        public double getCargo() {
            return cargo;
        }

        public void setCargo(double cargo) {
            this.cargo = cargo;
        }

        public double getLatituge() {
            return latituge;
        }

        public void setLatituge(double latituge) {
            this.latituge = latituge;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public int getHeading() {
            return heading;
        }

        public void setHeading(int heading) {
            this.heading = heading;
        }

        public double getAltitude() {
            return altitude;
        }

        public void setAltitude(double altitude) {
            this.altitude = altitude;
        }

        public long getBalance() {
            return balance;
        }

        public void setBalance(long balance) {
            this.balance = balance;
        }

        public double getPlanetRadius() {
            return planetRadius;
        }

        public void setPlanetRadius(double planetRadius) {
            this.planetRadius = planetRadius;
        }
    }

}
