package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.events.VoiceCommandDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Robot;
import java.awt.event.KeyEvent;

public class CargoHatchControlSubscriber {
    private static final Logger log = LoggerFactory.getLogger(CargoHatchControlSubscriber.class);
    private final Robot robot;

    public CargoHatchControlSubscriber() {
        EventBusManager.register(this);
        try {
            robot = new Robot(); // For keyboard events
        } catch (Exception e) {
            log.error("Failed to initialize Robot: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Subscribe
    public void onVoiceCommand(VoiceCommandDTO dto) {
        if ("open_cargo_hatch".equals(dto.getInterpretedAction())) {
            // Simulate keyboard combo (e.g., 'O' key, adjust based on game bindings)
            robot.keyPress(KeyEvent.VK_O);
            robot.keyRelease(KeyEvent.VK_O);
            VoiceGenerator.getInstance().speak("Opening cargo hatch!");
            log.debug("Triggered cargo hatch open");
        }
    }
}