package elite.intel.db.util;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Database {

    private static final Jdbi JDBI;
    private static final Handle HANDLE;

    private static void migrateIfNeeded() {
        JDBI.useHandle(handle -> {
            try {
                DatabaseMigrator.migrate(handle);
            } catch (Exception e) {
                throw new RuntimeException("Migration failed — your DB might be b0rked", e);
            }
        });
    }

    public static <T, R> R withDao(Class<T> daoClass, java.util.function.Function<T, R> block) {
        T dao = HANDLE.attach(daoClass);
        try {
            return block.apply(dao);
        } catch (Exception e) {
            throw new RuntimeException("DAO operation failed: " + daoClass.getSimpleName(), e);
        }
    }

    // shortcut if you just need a handle
    public static Handle handle() {
        return HANDLE;
    }

    static {
        Path dbPath = Paths.get("./db/database.db");
        dbPath.getParent().toFile().mkdirs();

        String url = "jdbc:sqlite:" + dbPath
                + "?journal_mode=WAL"      // safe concurrent reads/writes
                + "&busy_timeout=5000"     // don’t deadlock if two threads hit it
                + "&synchronous=NORMAL";   // fast + still safe on Linux

        JDBI = Jdbi.create(url).installPlugin(new SqlObjectPlugin());

        // Open once so the file gets created if missing
        JDBI.withHandle(h -> {
            h.execute("PRAGMA foreign_keys = ON;");           // always good
            h.execute("PRAGMA case_sensitive_like = OFF;");   // makes LIKE ignore case
            h.execute("PRAGMA journal_mode = WAL;");          // safe + fast
            h.execute("PRAGMA synchronous = NORMAL;");        // fast + still safe on Linux
            h.execute("PRAGMA busy_timeout = 5000;");         // avoid lock errors
            return null;
        });


        // Run migrations (see below)
        migrateIfNeeded();

        HANDLE = JDBI.open(); // keep one long-lived handle for the whole app lifetime
    }
}
