package elite.intel.db.util;

import elite.intel.db.dao.*;
import elite.intel.util.AppPaths;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

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
        Path dbPath;
        try {
            dbPath = AppPaths.getDatabasePath();
        } catch (IOException e) {
            throw new RuntimeException("Unable to create database directory " + e.getMessage(), e);
        }


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

            // Automatically attach all DAO classes from elite.intel.db.dao package
            try {
                Set<Class<?>> daoClasses = findDaoClasses("elite.intel.db.dao");
                for (Class<?> daoClass : daoClasses) {
                    h.attach(daoClass);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to attach DAO classes", e);
            }

            return null;
        });


        // Run migrations (see below)
        migrateIfNeeded();
    }

    private static Set<Class<?>> findDaoClasses(String packageName) throws Exception {
        Set<Class<?>> classes = new HashSet<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Enumeration<URL> resources = classLoader.getResources(path);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();

            if ("file".equals(protocol)) {
                Path root = Paths.get(resource.toURI());
                try (var walk = Files.walk(root)) {
                    walk.filter(p -> p.toString().endsWith(".class"))
                            .forEach(p -> {
                                try {
                                    String className = packageName + "." +
                                            root.relativize(p).toString()
                                                    .replace(FileSystems.getDefault().getSeparator(), ".")
                                                    .replace(".class", "");
                                    Class<?> clazz = Class.forName(className);
                                    if (clazz.isInterface() && clazz.getSimpleName().endsWith("Dao")) {
                                        classes.add(clazz);
                                    }
                                } catch (ClassNotFoundException e) {
                                    // Skip classes that can't be loaded
                                }
                            });
                }
            } else if ("jar".equals(protocol)) {
                String urlStr = resource.toString();
                int sep = urlStr.indexOf("!/");
                String jarPart = urlStr.substring(0, sep);
                URI jarUri = URI.create(jarPart);

                try (FileSystem fs = FileSystems.newFileSystem(jarUri, Collections.emptyMap())) {
                    Path root = fs.getPath("/" + path);
                    try (var walk = Files.walk(root)) {
                        walk.filter(p -> p.toString().endsWith(".class"))
                                .forEach(p -> {
                                    try {
                                        String className = packageName + "." +
                                                root.relativize(p).toString()
                                                        .replace("/", ".")
                                                        .replace(".class", "");
                                        Class<?> clazz = Class.forName(className);
                                        if (clazz.isInterface() && clazz.getSimpleName().endsWith("Dao")) {
                                            classes.add(clazz);
                                        }
                                    } catch (ClassNotFoundException e) {
                                        // Skip classes that can't be loaded
                                    }
                                });
                    }
                }
            }
        }
        return classes;
    }
}
