package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(ShipDao.ShipRowMapper.class)
public interface ShipDao {


    @SqlQuery("SELECT * FROM ship where shipId= :shipId")
    Ship findShip(int shipId);

    @SqlUpdate(""" 
            INSERT INTO ship (shipName, shipId, shipIdentifier, cargoCapacity, tradeProfileId) 
                        VALUES (:shipName, :shipId, :shipIdentifier, :cargoCapacity, :tradeProfileId)
                        on conflict DO UPDATE set
                        shipName = excluded.shipName,
                        shipIdentifier = excluded.shipIdentifier,
                        cargoCapacity = excluded.cargoCapacity,
                        tradeProfileId= excluded.tradeProfileId
            """)
    void save(@BindBean ShipDao.Ship ship);


    class ShipRowMapper implements RowMapper<Ship> {
        @Override public Ship map(ResultSet rs, StatementContext ctx) throws SQLException {
            Ship ship = new Ship();
            ship.setShipName(rs.getString("shipName"));
            ship.setShipId(rs.getInt("shipId"));
            ship.setShipIdentifier(rs.getString("shipIdentifier"));
            ship.setCargoCapacity(rs.getInt("cargoCapacity"));
            ship.setTradeProfileId(rs.getInt("tradeProfileId"));
            return ship;
        }
    }



    public enum PadSize {
        SMALL("S"), MEDIUM("M"), LARGE("L");

        private String size;
        PadSize(String size) {
            this.size = size;
        }

        public String getSize() {
            return size;
        }
    }

    class Ship {
        private String shipName;
        private Integer shipId;
        private String shipIdentifier;
        private Integer cargoCapacity;
        private Integer tradeProfileId;

        public String getShipName() {
            return shipName;
        }

        public void setShipName(String shipName) {
            this.shipName = shipName;
        }

        public Integer getShipId() {
            return shipId;
        }

        public void setShipId(Integer shipId) {
            this.shipId = shipId;
        }

        public String getShipIdentifier() {
            return shipIdentifier;
        }

        public void setShipIdentifier(String shipIdentifier) {
            this.shipIdentifier = shipIdentifier;
        }

        public int getCargoCapacity() {
            return cargoCapacity;
        }

        public void setCargoCapacity(int cargoCapacity) {
            this.cargoCapacity = cargoCapacity;
        }

        public Integer getTradeProfileId() {
            return tradeProfileId;
        }

        public void setTradeProfileId(Integer tradeProfileId) {
            this.tradeProfileId = tradeProfileId;
        }
    }
}
