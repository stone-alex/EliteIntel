package elite.companion.about;

import elite.companion.about.dto.AICapabilitiesDto;
import elite.companion.about.dto.AINameDto;
import elite.companion.session.SystemSession;

public class EliteCompanionFactory {

    private static final EliteCompanionFactory instance = new EliteCompanionFactory();

    private EliteCompanionFactory() {
        // Prevent instantiation.
    }

    public static EliteCompanionFactory getInstance() {
        return instance;
    }


    public AINameDto getInfo() {
        String name = SystemSession.getInstance().getAIVoice().getName();
        return new AINameDto(name, "Elite Dangerous AI Companion");
    }

    public AICapabilitiesDto getCapabilities() {
        return new AICapabilitiesDto();
    }

}
