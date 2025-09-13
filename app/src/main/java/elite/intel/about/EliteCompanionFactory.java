package elite.intel.about;

import elite.intel.about.dto.AICapabilitiesDto;
import elite.intel.about.dto.AINameDto;
import elite.intel.session.SystemSession;

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
