package elite.intel.db.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.Handle;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DatabaseMigrator {

    private static final Logger log = LogManager.getLogger(DatabaseMigrator.class);
    private static final Pattern MIGRATION_PATTERN = Pattern.compile("^(\\d{1,6})(__.*)?\\.sql$");
    private static final String MIGRATIONS_PATH = "/db-migration";

    public static void migrate(Handle handle) throws Exception {
        handle.execute("PRAGMA journal_mode = WAL;");

        handle.execute("""
                CREATE TABLE IF NOT EXISTS schema_migration (
                    version TEXT PRIMARY KEY,
                    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """);

        Set<String> allFiles = findMigrationFiles();

        var applied = handle.createQuery("SELECT version FROM schema_migration")
                .mapTo(String.class)
                .set();

        for (String file : allFiles) {
            if (applied.contains(file)) continue;

            log.info("Applying migration: {}", file);

            String sql;
            try (var in = DatabaseMigrator.class.getResourceAsStream(MIGRATIONS_PATH + "/" + file)) {
                if (in == null) throw new IllegalStateException("Migration not found: " + file);
                sql = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8)
                        .replace("\r\n", "\n");
            }

            for (String stmt : sql.split(";\\s*\n")) {
                if (!stmt.trim().isEmpty()) {
                    handle.execute(stmt.trim());
                }
            }

            handle.execute("INSERT INTO schema_migration (version) VALUES (?)", file);
        }

        log.info("Migrations complete. Applied: {}", allFiles.size());
    }


    private static Set<String> findMigrationFiles() throws IOException, URISyntaxException {
        Set<String> files = new TreeSet<>();
        ClassLoader cl = DatabaseMigrator.class.getClassLoader();
        String path = "db-migration";

        Enumeration<URL> urls = cl.getResources(path);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String protocol = url.getProtocol();

            if ("jar".equals(protocol)) {
                String urlStr = url.toString();
                int sep = urlStr.indexOf("!/");
                String jarPart = urlStr.substring(0, sep);
                URI jarUri = URI.create(jarPart);

                try (FileSystem fs = FileSystems.newFileSystem(jarUri, Collections.emptyMap())) {
                    Path root = fs.getPath("/" + path);
                    try (var walk = Files.walk(root)) {
                        walk.filter(Files::isRegularFile)
                                .map(p -> p.getFileName().toString())
                                .filter(n -> MIGRATION_PATTERN.matcher(n).matches())
                                .forEach(files::add);
                    }
                }
            } else if ("file".equals(protocol)) {
                Path dir = Paths.get(url.toURI());
                try (var walk = Files.walk(dir)) {
                    walk.filter(Files::isRegularFile)
                            .map(p -> p.getFileName().toString())
                            .filter(n -> MIGRATION_PATTERN.matcher(n).matches())
                            .forEach(files::add);
                }
            }
        }

        if (files.isEmpty()) {
            throw new IllegalStateException("No migration files found in classpath: db-migration");
        }
        return files;
    }
}