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
            INSERT INTO location (inGameId, locationName, primaryStar, homeSystem, json) 
            VALUES (:inGameId, :locationName, :primaryStar, :homeSystem, :json)
            ON CONFLICT(locationName) DO UPDATE SET
                json = excluded.json,
                inGameId = excluded.inGameId,
                homeSystem = excluded.homeSystem
            """)
    void upsert(
            @Bind("inGameId") long inGameId,
            @Bind("locationName") String locationName,
            @Bind("primaryStar") String primaryStar,
            @Bind("homeSystem") Boolean homeSystem,
            @Bind("json") String json
    );


    @SqlQuery("SELECT * FROM location WHERE inGameId = :inGameId")
    LocationDao.Location findByInGameId(Long inGameId);

    @SqlQuery("SELECT * FROM location WHERE inGameId = :inGameId AND :primaryStar = primaryStar")
    LocationDao.Location findByInGameIdAndPrimaryStar(Long inGameId, String primaryStar);

    @SqlQuery("SELECT * FROM location WHERE primaryStar = :primaryStar")
    List<Location> findByPrimaryStar(@Bind("primaryStar") String primaryStar);

    @SqlUpdate("UPDATE location SET homeSystem = :home WHERE locationName = :name")
    void setHomeSystem(@Bind("name") String locationName, @Bind("home") boolean home);

    @SqlQuery("SELECT * FROM location WHERE homeSystem = 1")
    Location findHomeSystem();

    @SqlQuery("select json from location where primaryStar = (select current_primary_star from player) and inGameId = (select current_location_id from player)")
    Location primaryStarAtCurrentLocation();

    @SqlQuery("select * from location where primaryStar = :starSystem and json like '%\"PRIMARY_STAR\"%'")
    Location findPrimaryStar(String starSystem);


    @SqlQuery("""
            SELECT
                json ->> '$.X' AS x,
                json ->> '$.Y' AS y,
                json ->> '$.Z' AS z
            from location location where primaryStar = (select current_primary_star from player) and json ->> '$.X' != 0 and json ->> '$.Y' !=0 and json ->> '$.Z' !=0 LIMIT 1;
            """)
    @RegisterConstructorMapper(Coordinates.class)
    Coordinates currentCoordinates();


    class LocationMapper implements RowMapper<LocationDao.Location> {
        @Override
        public LocationDao.Location map(ResultSet rs, StatementContext ctx) throws SQLException {
            LocationDao.Location entity = new LocationDao.Location(
                    rs.getLong("id"),
                    rs.getLong("inGameId"),
                    rs.getString("locationName"),
                    rs.getString("primaryStar"),
                    rs.getBoolean("homeSystem"),
                    rs.getString("json")
            );
            return entity;
        }
    }

    record Coordinates(int x, int y, int z) {

    }

    record Location(long id, long inGameId, String locationName, String primaryStar, Boolean homeSystem, String json) {
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

        public String getJson() {
            return json;
        }

        public Boolean homeSystem() {
            return homeSystem;
        }
    }
}
