package elite.intel.search.eddn.mappers;

import elite.intel.db.dao.LocationDao;
import elite.intel.gameapi.journal.events.FSDJumpEvent;
import elite.intel.search.eddn.schemas.FsdJumpMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FsdJumpMapper {

    public static FsdJumpMessage map(FSDJumpEvent event, String primaryStarName, LocationDao.Coordinates coordinates) {
        FsdJumpMessage msg = new FsdJumpMessage();

        msg.setTimestamp(event.getTimestamp());
        msg.setStarSystem(event.getStarSystem());
        msg.setSystemAddress(event.getSystemAddress());
        msg.setStarPos(Arrays.asList(coordinates.x(), coordinates.y(), coordinates.z()));
        msg.setBody(event.getBody());
        msg.setBodyID(event.getBodyId());
        msg.setBodyType(event.getBodyType());
        msg.setSystemAllegiance(event.getSystemAllegiance());
        msg.setSystemEconomy(event.getSystemEconomy());  // Keep code like "$economy_Agri;"
        msg.setSystemSecondEconomy(event.getSystemSecondEconomy());
        msg.setSystemGovernment(event.getSystemGovernment());
        msg.setSystemSecurity(event.getSystemSecurity());
        msg.setPopulation(event.getPopulation());

        if (event.getSystemFaction() != null) {
            FsdJumpMessage.SystemFaction sf = new FsdJumpMessage.SystemFaction();
            sf.setName(event.getSystemFaction().getName());
            sf.setFactionState(event.getSystemFaction().getFactionState());
            msg.setSystemFaction(sf);
        }

        if (event.getFactions() != null) {
            List<FsdJumpMessage.Faction> factions = new ArrayList<>();
            for (FSDJumpEvent.Faction eFaction : event.getFactions()) {
                FsdJumpMessage.Faction f = new FsdJumpMessage.Faction();
                f.setName(eFaction.getName());
                f.setAllegiance(eFaction.getAllegiance());
                f.setGovernment(eFaction.getGovernment());
                f.setInfluence(eFaction.getInfluence());
                f.setHappiness(eFaction.getHappiness());  // Keep code like "$Faction_HappinessBand2;"
                f.setFactionState(eFaction.getFactionState());

                // Copy activeStates
                if (eFaction.getActiveStates() != null) {
                    List<FsdJumpMessage.ActiveState> asList = new ArrayList<>();
                    for (FSDJumpEvent.ActiveState eas : eFaction.getActiveStates()) {
                        FsdJumpMessage.ActiveState as = new FsdJumpMessage.ActiveState();
                        as.setState(eas.getState());
                        asList.add(as);
                    }
                    f.setActiveStates(asList);
                }

                // Copy pendingStates and recoveringStates similarly
                // Assuming event has getPendingStates() returning List<StateTrend>, etc.

                factions.add(f);
            }
            msg.setFactions(factions);
        }

/*
        if (event.getConflicts() != null) {
            List<FsdJumpMessage.Conflict> conflicts = new ArrayList<>();
            for (FSDJumpEvent.Conflict eConflict : event.getConflicts()) {
                FsdJumpMessage.Conflict c = new FsdJumpMessage.Conflict();
                c.warType = eConflict.getWarType();
                c.status = eConflict.getStatus();

                FsdJumpMessage.FactionSide fs1 = new FsdJumpMessage.FactionSide();
                fs1.name = eConflict.getFaction1().getName();
                fs1.stake = eConflict.getFaction1().getStake();
                fs1.wonDays = eConflict.getFaction1().getWonDays();
                c.faction1 = fs1;

                // Similarly for faction2

                conflicts.add(c);
            }
            msg.setConflicts(conflicts);
        }
*/

        msg.setPowers(event.getPowers());
        msg.setPowerplayState(event.getPowerplayState());

        return msg;
    }

    private static double[] toDoubleArray(float[] floats) {
        if (floats == null) return null;
        double[] doubles = new double[floats.length];
        for (int i = 0; i < floats.length; i++) {
            doubles[i] = floats[i];
        }
        return doubles;
    }
}