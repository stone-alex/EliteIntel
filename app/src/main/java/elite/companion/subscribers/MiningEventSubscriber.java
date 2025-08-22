package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.comms.VoiceNotifier;
import elite.companion.events.MiningRefinedEvent;
import elite.companion.events.ProspectedAsteroidEvent;
import elite.companion.events.VoiceCommandDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MiningEventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(MiningEventSubscriber.class);
    private final Map<String, ProspectedAsteroidEvent> activeProspectors = new HashMap<>();
    private String miningTarget = null;
    private final VoiceNotifier voice;

    public MiningEventSubscriber() {
        EventBusManager.register(this);
        voice = new VoiceNotifier();

    }

    @Subscribe
    public void onProspectedAsteroid(ProspectedAsteroidEvent dto) {
        if (!dto.isProcessed && !dto.isExpired()) {
            String closestDroneId = activeProspectors.keySet().stream()
                    .filter(id -> Instant.parse(id).isBefore(Instant.parse(dto.timestamp)))
                    .max(Comparator.comparing(Instant::parse))
                    .orElse(null);

            // ... existing code ...

            double matchedProportion = -1.0;
            if (closestDroneId != null && dto.Materials != null) {
                for (var m : dto.Materials) {
                    if (m != null && m.Name != null && m.Name.equalsIgnoreCase(miningTarget)) {
                        matchedProportion = m.Proportion; // Expecting value in 0.0..1.0
                        break;
                    }
                }
                if (matchedProportion >= 0) {
                    activeProspectors.put(closestDroneId, dto);
                    voice.speak(String.format(
                            "Prospector %s reports %.0f%% %s",
                            closestDroneId.substring(11, 19),
                            matchedProportion * 100.0,
                            miningTarget
                    ));
                }
            }
            dto.isProcessed = true;
        }
    }


    @Subscribe
    public void onMiningRefined(MiningRefinedEvent dto) {
        if (miningTarget != null && miningTarget.equalsIgnoreCase(dto.getMineralType())) {
            voice.speak("Refined " + dto.getMineralType() + "!");
        }
    }

    @Subscribe
    public void onVoiceCommand(VoiceCommandDTO dto) {
        if ("set_mining_target".equals(dto.getInterpretedAction())) {
            miningTarget = dto.getTarget();
            voice.speak("Mining target set to " + miningTarget + "!");
        }
    }
}
