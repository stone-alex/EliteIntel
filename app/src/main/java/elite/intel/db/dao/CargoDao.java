package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(CargoDao.CargoMapper.class)
public interface CargoDao {


    @SqlUpdate("INSERT OR REPLACE INTO cargo (id, json) VALUES (1, :json)")
    void save(@BindBean CargoDao.Cargo data);

    @SqlQuery("SELECT json FROM cargo WHERE id = 1")
    Cargo get();


    class CargoMapper implements RowMapper<Cargo> {

        @Override public Cargo map(ResultSet rs, StatementContext ctx) throws SQLException {
            Cargo cargo = new Cargo();
            cargo.json = rs.getString("json");
            return cargo;
        }
    }


    class Cargo {
        public Cargo() {
        }

        private String json;

        public String getJson() {
            return json;
        }

        public void setJson(String json) {
            this.json = json;
        }
    }
}
