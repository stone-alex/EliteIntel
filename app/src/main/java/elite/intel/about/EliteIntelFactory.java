package elite.intel.about;

import elite.intel.about.dto.AICapabilitiesDto;
import elite.intel.about.dto.AINameDto;
import elite.intel.session.SystemSession;

public class EliteIntelFactory {

    private static final EliteIntelFactory instance = new EliteIntelFactory();

    private EliteIntelFactory() {
        // Prevent instantiation.
    }

    public static EliteIntelFactory getInstance() {
        return instance;
    }


    public AINameDto getInfo() {
        String name = SystemSession.getInstance().getAIVoice().getName();
        return new AINameDto(name, "Elite Dangerous AI");
    }

    public AICapabilitiesDto getCapabilities() {
        return new AICapabilitiesDto();
    }

}
