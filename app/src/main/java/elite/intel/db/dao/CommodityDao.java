package elite.intel.db.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

public interface CommodityDao {

    @SqlQuery("SELECT commodity FROM commodities ORDER BY commodity")
    List<String> getAllNamesLowerCase();

    @SqlQuery("SELECT commodity FROM commodities WHERE LOWER(commodity) = LOWER(:name) LIMIT 1")
    String getOriginalCase(@Bind("name") String name);

    // optional – one-time init if table empty
    @SqlQuery("SELECT COUNT(*) FROM commodities")
    int count();

    @SqlBatch("INSERT OR IGNORE INTO commodities (commodity) VALUES (:name)")
    void insertAll(@Bind("name") List<String> names);
}