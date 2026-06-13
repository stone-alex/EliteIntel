package elite.intel.db.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

public interface MaterialNameDao {

    @SqlQuery("SELECT name FROM material_names ORDER BY name")
    List<String> getAllNamesLowerCase();

    @SqlQuery("SELECT name FROM material_names WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    String getOriginalCase(@Bind("name") String name);

    // optional – one-time init if table empty
    @SqlQuery("SELECT COUNT(*) FROM material_names")
    int count();

    @SqlBatch("INSERT OR IGNORE INTO material_names (name) VALUES (:name)")
    void insertAll(@Bind("name") List<String> names);

}
