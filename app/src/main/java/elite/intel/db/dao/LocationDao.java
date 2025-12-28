package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RegisterRowMapper(LocationDao.LocationMapper.class)
public interface LocationDao {

    @SqlUpdate("""
            INSERT INTO location (inGameId, locationName, primaryStar, homeSystem, systemAddress, json) 
            VALUES (:inGameId, :locationName, :primaryStar, :homeSystem, :systemAddress, :json)
            ON CONFLICT(locationName) DO UPDATE SET
                json = excluded.json,
                inGameId = excluded.inGameId,
                systemAddress = excluded.systemAddress,
                homeSystem = excluded.homeSystem
            """)
    void upsert(
            @Bind("inGameId") long inGameId,
            @Bind("locationName") String locationName,
            @Bind("primaryStar") String primaryStar,
            @Bind("homeSystem") Boolean homeSystem,
            @Bind("systemAddress") Long systemAddress,
            @Bind("json") String json
    );


    @SqlQuery("SELECT * FROM location WHERE inGameId = :inGameId")
    LocationDao.Location findByInGameId(Long inGameId);

    @SqlQuery("SELECT * FROM location WHERE inGameId = :inGameId AND :primaryStar = primaryStar")
    LocationDao.Location findByInGameIdAndPrimaryStar(Long inGameId, String primaryStar);

    @SqlQuery("SELECT * FROM location WHERE primaryStar = :primaryStar")
    List<Location> findByPrimaryStar(@Bind("primaryStar") String primaryStar);

    @SqlUpdate("""   
            UPDATE location
            SET homeSystem = 1
            WHERE primaryStar = :primaryStar and systemAddress = :systemAddress
            """)
    void setCurrentStarSystemAsHome(@Bind("primaryStar") String primaryStar, @Bind("systemAddress") Long systemAddress);

    @SqlUpdate("""
        UPDATE location set homeSystem = 0;
    """)
    void clearHomeSystem();

    @SqlQuery("SELECT * FROM location WHERE homeSystem = 1")
    Location findHomeSystem();

    @SqlQuery("select json from location where primaryStar = (select current_primary_star from player) and inGameId = (select current_location_id from player)")
    Location primaryStarAtCurrentLocation();

    @SqlQuery("select * from location where primaryStar = :starSystem and json like '%\"PRIMARY_STAR\"%'")
    Location findPrimaryStar(String starSystem);

    @SqlQuery("""
            SELECT location.primaryStar,
                json ->> '$.X' AS x,
                json ->> '$.Y' AS y,
                json ->> '$.Z' AS z
            from location location where primaryStar = (select current_primary_star from player) and json ->> '$.X' != 0 and json ->> '$.Y' !=0 and json ->> '$.Z' !=0 LIMIT 1;
            """)
    @RegisterConstructorMapper(Coordinates.class)
    Coordinates currentCoordinates();

    @SqlQuery("select * from location where systemAddress = :systemAddress and json->> '$.planetName' = :planetName")
    Location findBySystemAddress(long systemAddress, String planetName);

    @SqlQuery("select * from location where systemAddress = :systemAddress and json->> '$.bodyId' = :bodyId")
    Location findBySystemAddress(long systemAddress, Long bodyId);

    @SqlQuery("select * from location where systemAddress = :systemAddress")
    Location findBySystemAddress(long systemAddress);

    @SqlQuery("select * from location where json ->> '$.marketID' = :marketID")
    Location findByMarketId(long marketID);


    class LocationMapper implements RowMapper<LocationDao.Location> {
        @Override
        public LocationDao.Location map(ResultSet rs, StatementContext ctx) throws SQLException {
            LocationDao.Location entity = new LocationDao.Location(
                    rs.getLong("id"),
                    rs.getLong("inGameId"),
                    rs.getString("locationName"),
                    rs.getString("primaryStar"),
                    rs.getBoolean("homeSystem"),
                    rs.getLong("systemAddress"),
                    rs.getString("json")
            );
            return entity;
        }
    }

    record Coordinates(String primaryStar,  double x, double y, double z) {

    }

    record Location(long id, long inGameId, String locationName, String primaryStar, Boolean homeSystem, Long systemAddress, String json) {
        public long getId() {
            return id;
        }

        public long getInGameId() {
            return inGameId;
        }

        public String getLocationName() {
            return locationName;
        }

        public String getPrimaryStar() {
            return primaryStar;
        }

        public Long getSystemAddress() {
            return systemAddress;
        }

        public String getJson() {
            return json;
        }

        public Boolean homeSystem() {
            return homeSystem;
        }

    }
}
