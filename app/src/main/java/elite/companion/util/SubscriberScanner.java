package elite.companion.util;

import com.google.common.eventbus.Subscribe;  // Adjust if using custom EventBus
import elite.companion.EventBusManager;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class SubscriberScanner {

    private static final String SUBSCRIBER_PACKAGE = "elite.companion.events";
    private static final String SUBSCRIBER_SUFFIX = "Subscriber";

    public static void registerAllSubscribers() {
        List<Class<?>> subscriberClasses = findSubscriberClasses();
        if (subscriberClasses.isEmpty()) {
            System.err.println("No subscriber classes found in " + SUBSCRIBER_PACKAGE);
        }
        for (Class<?> clazz : subscriberClasses) {
            try {
                Object subscriber = clazz.getDeclaredConstructor().newInstance();
                if (hasSubscribeMethods(clazz)) {
                    EventBusManager.register(subscriber);
                    System.out.println("Registered subscriber: " + clazz.getSimpleName());
                } else {
                    System.out.println("Skipped " + clazz.getSimpleName() + ": No @Subscribe methods");
                }
            } catch (Exception e) {
                System.err.println("Failed to instantiate/register " + clazz.getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    private static List<Class<?>> findSubscriberClasses() {
        List<Class<?>> classes = new ArrayList<>();
        String packagePath = SUBSCRIBER_PACKAGE.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try {
            Enumeration<java.net.URL> resources = classLoader.getResources(packagePath);
            if (!resources.hasMoreElements()) {
                System.err.println("No resources found for package: " + packagePath);
            }
            while (resources.hasMoreElements()) {
                java.net.URL resource = resources.nextElement();
                System.out.println("Scanning resource: " + resource.toString());
                if (resource.getProtocol().equals("file")) {
                    // Development: Likely app/build/classes/java/main/elite/companion/events
                    try {
                        Path dir = Paths.get(resource.toURI());
                        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                            System.err.println("Resource path is not a valid directory: " + dir);
                            continue;
                        }
                        classes.addAll(Files.walk(dir, 1) // Depth 1 to avoid subpackages
                                .filter(Files::isRegularFile)
                                .filter(p -> p.toString().endsWith(".class"))
                                .map(p -> getClassFromPath(p, SUBSCRIBER_PACKAGE))
                                .filter(Objects::nonNull)
                                .filter(c -> c.getSimpleName().endsWith(SUBSCRIBER_SUFFIX))
                                .collect(Collectors.toList()));
                    } catch (URISyntaxException | IOException e) {
                        System.err.println("Error processing file resource " + resource + ": " + e.getMessage());
                    }
                } else if (resource.getProtocol().equals("jar")) {
                    // Production: Inside a JAR
                    String jarPath = resource.toString().substring(0, resource.toString().indexOf("!"));
                    try (FileSystem fs = FileSystems.newFileSystem(URI.create(jarPath), Collections.emptyMap())) {
                        Path jarDir = fs.getPath("/" + packagePath);
                        if (!Files.exists(jarDir)) {
                            System.err.println("JAR directory not found: " + jarDir);
                            continue;
                        }
                        classes.addAll(Files.walk(jarDir, 1)
                                .filter(Files::isRegularFile)
                                .filter(p -> p.toString().endsWith(".class"))
                                .map(p -> getClassFromPath(p, SUBSCRIBER_PACKAGE))
                                .filter(Objects::nonNull)
                                .filter(c -> c.getSimpleName().endsWith(SUBSCRIBER_SUFFIX))
                                .collect(Collectors.toList()));
                    } catch (IOException e) {
                        System.err.println("Error processing JAR resource " + resource + ": " + e.getMessage());
                    }
                } else {
                    System.err.println("Unsupported resource protocol: " + resource.getProtocol());
                }
            }
        } catch (IOException e) {
            System.err.println("Error accessing resources for " + packagePath + ": " + e.getMessage());
        }

        if (classes.isEmpty()) {
            System.err.println("No subscriber classes found. Ensure .class files exist in " + packagePath);
        } else {
            System.out.println("Found classes: " + classes.stream().map(Class::getSimpleName).collect(Collectors.toList()));
        }
        return classes;
    }

    private static Class<?> getClassFromPath(Path path, String packageName) {
        try {
            String className = path.getFileName().toString().replace(".class", "");
            if (!className.isEmpty()) {
                String fullClassName = packageName + "." + className;
                return Class.forName(fullClassName);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found for path " + path + ": " + e.getMessage());
        }
        return null;
    }

    private static boolean hasSubscribeMethods(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Subscribe.class)) {
                return true;
            }
        }
        return false;
    }
}