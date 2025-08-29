package elite.companion.util;

import com.google.common.eventbus.EventBus;

public class EventBusManager {
    private static final EventBus bus = new EventBus();

    public static void publish(Object event) {
        bus.post(event);
    }

    public static void register(Object subscriber) {
        bus.register(subscriber);
    }
}
