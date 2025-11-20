package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface LocationDao {


    @SqlUpdate("""
            INSERT INTO location (inGameId, locationName, primaryStar, json) 
            VALUES (:inGameId, :locationName, :primaryStar, :json)
            ON CONFLICT(locationName) DO UPDATE SET
                json = excluded.json,
                inGameId = excluded.inGameId
            """)
    void upsert(
            @Bind("inGameId") long inGameId,
            @Bind("locationName") String locationName,
            @Bind("primaryStar") String primaryStar,
            @Bind("json") String json
    );


    @SqlQuery("SELECT * FROM location WHERE inGameId = :inGameId")
    @RegisterRowMapper(LocationDao.LocationMapper.class)
    LocationDao.Location findByInGameId(Long inGameId);

    @SqlQuery("SELECT * FROM location WHERE inGameId = :inGameId AND :primaryStar = primaryStar")
    @RegisterRowMapper(LocationDao.LocationMapper.class)
    LocationDao.Location findByInGameIdAndPrimaryStar(Long inGameId, String primaryStar);

    @SqlQuery("SELECT * FROM location WHERE primaryStar = :primaryStar")
    @RegisterRowMapper(LocationDao.LocationMapper.class)
    List<Location> findByPrimaryStar(@Bind("primaryStar") String primaryStar);


    class LocationMapper implements RowMapper<LocationDao.Location> {
        @Override
        public LocationDao.Location map(ResultSet rs, StatementContext ctx) throws SQLException {
            LocationDao.Location entity = new LocationDao.Location(
                    rs.getLong("id"),
                    rs.getLong("inGameId"),
                    rs.getString("locationName"),
                    rs.getString("primaryStar"),
                    rs.getString("json")
            );
            return entity;
        }
    }


    record Location(long id, long inGameId, String locationName, String primaryStar, String json) {
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
    }
}
