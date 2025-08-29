package elite.companion.util;

import com.google.common.eventbus.Subscribe;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import java.lang.reflect.Method;
import java.util.Set;

public class SubscriberRegistration {
    public static void registerSubscribers() {
        Reflections reflections = new Reflections(
                "elite.companion.gameapi.journal.subscribers",
                new MethodAnnotationsScanner()
        );
        // Find methods annotated with @Subscribe
        Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(Subscribe.class);

        // Collect unique classes containing these methods
        Set<Class<?>> subscriberClasses = new java.util.HashSet<>();
        for (Method method : annotatedMethods) {
            subscriberClasses.add(method.getDeclaringClass());
        }

        // Instantiate and register each subscriber class
        for (Class<?> subscriberClass : subscriberClasses) {
            try {
                Object subscriberInstance = subscriberClass.getDeclaredConstructor().newInstance();
                EventBusManager.register(subscriberInstance);
            } catch (Exception e) {
                System.err.println("Failed to instantiate subscriber: " + subscriberClass.getName());
                e.printStackTrace();
            }
        }
    }
}