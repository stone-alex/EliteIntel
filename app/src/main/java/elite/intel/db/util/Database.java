package elite.intel.db.util;

import elite.intel.db.dao.*;
import elite.intel.search.spansh.station.DestinationDto;
import elite.intel.util.AppPaths;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.nio.file.Path;

public class Database {

    private static final Jdbi JDBI;

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
        // Use withExtension to obtain a thread-safe handle for this specific operation
        try {
            return JDBI.withExtension(daoClass, block::apply);
        } catch (Exception e) {
            throw new RuntimeException("DAO operation failed: " + daoClass.getSimpleName(), e);
        }
    }

    // shortcut if you just need a handle
    public static Handle init() {
        return JDBI.open();
    }

    static {
        Path dbPath = AppPaths.getDatabasePath();
        dbPath.getParent().toFile().mkdirs();

        String url = "jdbc:sqlite:" + dbPath
                + "?journal_mode=WAL"      // safe concurrent reads/writes
                + "&busy_timeout=5000"     // don’t deadlock if two threads hit it
                + "&synchronous=NORMAL"    // fast + still safe on Linux
                + "&foreign_keys=ON";      // Ensure Foreign Keys are enforced on every connection

        JDBI = Jdbi.create(url).installPlugin(new SqlObjectPlugin());

        // Open once so the file gets created if missing and persistent pragmas are set
        JDBI.withHandle(h -> {
            h.execute("PRAGMA foreign_keys = ON;");           // always good
            h.execute("PRAGMA case_sensitive_like = OFF;");   // makes LIKE ignore case
            h.execute("PRAGMA journal_mode = WAL;");          // safe + fast
            h.execute("PRAGMA synchronous = NORMAL;");        // fast + still safe on Linux
            h.execute("PRAGMA busy_timeout = 5000;");         // avoid lock errors

            h.attach(BioSampleDao.class);
            h.attach(BountyDao.class);
            h.attach(ChatHistoryDao.class);
            h.attach(DestinationReminderDao.class);
            h.attach(FleetCarrierDao.class);
            h.attach(FleetCarrierRouteDao.class);
            h.attach(FsdTargetDao.class);
            h.attach(GameSessionDao.class);
            h.attach(GenusPaymentAnnouncementDao.class);
            h.attach(LocationDao.class);
            h.attach(MaterialsDao.class);
            h.attach(MiningTargetDao.class);
            h.attach(PlayerDao.class);
            h.attach(RankAndProgressDao.class);
            h.attach(ReputationDao.class);
            h.attach(ShipLoadoutDao.class);
            h.attach(ShipScansDao.class);
            h.attach(StationMarketDao.class);
            h.attach(StatusDao.class);
            h.attach(TargetLocationDao.class);

            return null;
        });


        // Run migrations (see below)
        migrateIfNeeded();
    }
}
