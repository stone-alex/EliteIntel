package elite.companion.modules;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import elite.companion.comms.VoiceNotifier;
import elite.companion.model.MiningRefinedDTO;
import elite.companion.model.VoiceCommandDTO;

import java.io.IOException;

public class MiningModule {

    private String miningTarget = null;
    private final VoiceNotifier voice;

    public MiningModule(EventBus bus) {
        bus.register(this);
        try {
            voice = new VoiceNotifier();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize VoiceNotifier", e);
        }
    }

    @Subscribe
    public void onMiningRefined(MiningRefinedDTO dto) {
        if (miningTarget != null && miningTarget.equalsIgnoreCase(dto.getMineralType())) {
            voice.speak("Refined " + dto.getMineralType() + "!");
        }
    }

    @Subscribe
    public void onVoiceCommand(VoiceCommandDTO dto) {
        if ("set_mining_target".equals(dto.getInterpretedAction())) {
            miningTarget = dto.getParams("target");
            voice.speak("Mining target set to " + miningTarget + "!");
        }
    }
}
