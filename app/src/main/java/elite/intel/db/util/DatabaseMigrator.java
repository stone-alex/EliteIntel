package elite.intel.db.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.Handle;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;


public class DatabaseMigrator {

    private static final Logger log = LogManager.getLogger(DatabaseMigrator.class);
    private static final Pattern MIGRATION_PATTERN = Pattern.compile("^(V\\d+(__.*)?\\.sql|\\d{4}-\\d{2}-\\d{2}(__.*)?\\.sql)$");

    public static void migrate(Handle handle) throws Exception {
        handle.execute("PRAGMA journal_mode = WAL;");

        // Create migration tracking table + user_version fallback
        handle.execute("""
                CREATE TABLE IF NOT EXISTS schema_migration (
                    version TEXT PRIMARY KEY,
                    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """);

        // Load all migration files from classpath
        String migrationsPath = "/db-migration";
        SortedSet<String> allFiles = new TreeSet<>();
        var url = DatabaseMigrator.class.getResource(migrationsPath);
        if (url == null) throw new IllegalStateException("Migration folder not found: " + migrationsPath);

        if ("file".equals(url.getProtocol())) {
            // IDE / running from src
            Files.walk(Paths.get(url.toURI()))
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(name -> MIGRATION_PATTERN.matcher(name).find())
                    .forEach(allFiles::add);
        } else {
            // JAR runtime — scan classpath (works with Gradle shadowJar too)
            try (var stream = DatabaseMigrator.class.getResourceAsStream(migrationsPath)) {
                if (stream != null) {
                    var reader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
                    reader.lines()
                            .filter(name -> MIGRATION_PATTERN.matcher(name).find())
                            .forEach(allFiles::add);
                }
            }
        }

        // Get already applied versions
        var applied = handle.createQuery("SELECT version FROM schema_migration")
                .mapTo(String.class)
                .set();

        for (String file : allFiles) {
            if (applied.contains(file)) continue;

            log.info("Applying migration: " + file); // or proper logger
            String sql = new String(DatabaseMigrator.class.getResourceAsStream(migrationsPath + "/" + file)
                    .readAllBytes())
                    .replace("\r\n", "\n");

            for (String statement : sql.split(";\\s*\n")) {
                if (!statement.trim().isEmpty()) {
                    handle.execute(statement.trim());
                }
            }

            handle.execute("INSERT INTO schema_migration (version) VALUES (?)", file);
        }

        log.info("Migrations complete. Total applied: " + allFiles.size());
    }
}