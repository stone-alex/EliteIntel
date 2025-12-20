package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(RouteMonetisationDao.RouteMonetisationMapper.class)
public interface RouteMonetisationDao {

    @SqlUpdate("""
            INSERT OR REPLACE INTO trade_tuple (id, sourceCommodity, sourceStarSystem, sourceStationName, sourceStationType, 
                                                            sourceBuyPrice, sourceSupply, destinationCommodity, destinationStarSystem, \
                                                            destinationStationName, destinationStationType, destinationSellPrice, destinationDemand)
                        values (1, :sourceCommodity, :sourceStarSystem, :sourceStationName, :sourceStationType, 
                                            :sourceBuyPrice, :sourceSupply, :destinationCommodity, :destinationStarSystem, 
                                            :destinationStationName, :destinationStationType, :destinationSellPrice, :destinationDemand)
            """)
    void save(@BindBean MonetisationTransaction data);

    @SqlUpdate("DELETE FROM trade_tuple")
    void clear();

    @SqlQuery("SELECT * FROM trade_tuple where id = 1")
    MonetisationTransaction get();


    class RouteMonetisationMapper implements RowMapper<MonetisationTransaction> {

        @Override public MonetisationTransaction map(ResultSet rs, StatementContext ctx) throws SQLException {
            MonetisationTransaction entity = new MonetisationTransaction();
            entity.setSourceCommodity(rs.getString("sourceCommodity"));
            entity.setSourceStarSystem(rs.getString("sourceStarSystem"));
            entity.setSourceStationName(rs.getString("sourceStationName"));
            entity.setSourceStationType(rs.getString("sourceStationType"));
            entity.setSourceBuyPrice(rs.getInt("sourceBuyPrice"));
            entity.setSourceSupply(rs.getLong("sourceSupply"));
            entity.setDestinationCommodity(rs.getString("destinationCommodity"));
            entity.setDestinationStarSystem(rs.getString("destinationStarSystem"));
            entity.setDestinationStationName(rs.getString("destinationStationName"));
            entity.setDestinationStationType(rs.getString("destinationStationType"));
            entity.setDestinationSellPrice(rs.getInt("destinationSellPrice"));
            entity.setDestinationDemand(rs.getLong("destinationDemand"));
            return entity;
        }
    }



    class MonetisationTransaction {
        private String sourceCommodity;
        private String sourceStarSystem;
        private String sourceStationName;
        private String sourceStationType;
        private int sourceBuyPrice;
        private long sourceSupply;
        private String destinationCommodity;
        private String destinationStarSystem;
        private String destinationStationName;
        private String destinationStationType;
        private int destinationSellPrice;
        private long destinationDemand;

        public String getSourceCommodity() {
            return sourceCommodity;
        }

        public void setSourceCommodity(String sourceCommodity) {
            this.sourceCommodity = sourceCommodity;
        }

        public String getSourceStarSystem() {
            return sourceStarSystem;
        }

        public void setSourceStarSystem(String sourceStarSystem) {
            this.sourceStarSystem = sourceStarSystem;
        }

        public String getSourceStationName() {
            return sourceStationName;
        }

        public void setSourceStationName(String sourceStationName) {
            this.sourceStationName = sourceStationName;
        }

        public String getSourceStationType() {
            return sourceStationType;
        }

        public void setSourceStationType(String sourceStationType) {
            this.sourceStationType = sourceStationType;
        }

        public int getSourceBuyPrice() {
            return sourceBuyPrice;
        }

        public void setSourceBuyPrice(int sourceBuyPrice) {
            this.sourceBuyPrice = sourceBuyPrice;
        }

        public long getSourceSupply() {
            return sourceSupply;
        }

        public void setSourceSupply(long sourceSupply) {
            this.sourceSupply = sourceSupply;
        }

        public String getDestinationCommodity() {
            return destinationCommodity;
        }

        public void setDestinationCommodity(String destinationCommodity) {
            this.destinationCommodity = destinationCommodity;
        }

        public String getDestinationStarSystem() {
            return destinationStarSystem;
        }

        public void setDestinationStarSystem(String destinationStarSystem) {
            this.destinationStarSystem = destinationStarSystem;
        }

        public String getDestinationStationName() {
            return destinationStationName;
        }

        public void setDestinationStationName(String destinationStationName) {
            this.destinationStationName = destinationStationName;
        }

        public String getDestinationStationType() {
            return destinationStationType;
        }

        public void setDestinationStationType(String destinationStationType) {
            this.destinationStationType = destinationStationType;
        }

        public int getDestinationSellPrice() {
            return destinationSellPrice;
        }

        public void setDestinationSellPrice(int destinationSellPrice) {
            this.destinationSellPrice = destinationSellPrice;
        }

        public long getDestinationDemand() {
            return destinationDemand;
        }

        public void setDestinationDemand(long destinationDemand) {
            this.destinationDemand = destinationDemand;
        }
    }

}
