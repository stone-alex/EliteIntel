package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
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
        } else {
            System.out.println(
                    "Power [" + event.getPower() + "] is not included in programming. Please notify developer with exact power name as shown in this line"
            );
        }

        rp.setPledgedToPower(event.getPower());
        rp.setPowerRank(event.getRank());
        rp.setMerrits(event.getMerits());
        rp.setTimePledged(event.getTimePledged());
        session.setRankAndProgressDto(rp);
    }
}
