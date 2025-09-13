package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.brain.AICadence;
import elite.intel.ai.brain.AIPersonality;
import elite.intel.ai.mouth.AiVoices;
import elite.intel.ai.mouth.VoiceToAllegiances;
import elite.intel.gameapi.data.PowerDetails;
import elite.intel.gameapi.data.PowerPlayData;
import elite.intel.gameapi.journal.events.PowerplayEvent;
import elite.intel.gameapi.journal.events.dto.RankAndProgressDto;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;

@SuppressWarnings("unused")
public class PowerPlaySubscriber {

    @Subscribe
    public void onPowerPlayEvent(PowerplayEvent event) {
        PlayerSession session = PlayerSession.getInstance();
        RankAndProgressDto rp = session.getRankAndProgressDto();
        PowerDetails powerDetails = PowerPlayData.getPowerDetails(event.getPower());

        SystemSession systemSession = SystemSession.getInstance();
        if (powerDetails != null) {
            String allegiance = powerDetails.allegiance();
            rp.setAllegiance(allegiance);
            VoiceToAllegiances voiceToAllegiances = VoiceToAllegiances.getInstance();
/*
            if ("Empire".equalsIgnoreCase(allegiance)) {
                systemSession.setAICadence(AICadence.IMPERIAL);
                voiceToAllegiances.getVoiceForCadence(AICadence.IMPERIAL, systemSession.getAIVoice());
            } else if ("Federation".equalsIgnoreCase(allegiance)) {
                systemSession.setAICadence(AICadence.FEDERATION);
                voiceToAllegiances.getVoiceForCadence(AICadence.FEDERATION, systemSession.getAIVoice());
            } else {
                systemSession.setAICadence(AICadence.ALLIANCE);
                voiceToAllegiances.getVoiceForCadence(AICadence.ALLIANCE, systemSession.getAIVoice());
            }
*/
        } else {
            System.out.println(
                    "Power [" + event.getPower() + "] is not included in programming. Please notify developer with exact power name as shown in this line"
            );
            //default to Empire, British cadence in honor of the country that made this game.
            systemSession.setAICadence(AICadence.IMPERIAL);
            systemSession.setAIPersonality(AIPersonality.FRIENDLY);
            systemSession.setAIVoice(AiVoices.JAMES);
        }

        rp.setPledgedToPower(event.getPower());
        rp.setPowerRank(event.getRank());
        rp.setMerrits(event.getMerits());
        rp.setTimePledged(event.getTimePledged());
        session.setRankAndProgressDto(rp);
    }
}
