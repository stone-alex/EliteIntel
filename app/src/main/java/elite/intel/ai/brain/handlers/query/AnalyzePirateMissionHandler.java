package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.BountyDto;
import elite.intel.gameapi.journal.events.dto.MissionDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.*;
import java.util.stream.Collectors;

public class AnalyzePirateMissionHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing mission data... Stand by..."));
        PlayerSession session = PlayerSession.getInstance();
        Map<Long, MissionDto> missions = session.getMissions();
        Set<BountyDto> bounties = session.getBounties();
        String remainingKills = computeKillsRemaining(missions, bounties);
        String missionProfit = computeMissionProfit(missions, bounties);
        String instructions = """
                Do not sum anything do not calculate! 
                Just use data pre-calculated for you to answer the question. 
                If asked about total kills remaining only return the number of kills remaining to complete all assignments. 
                Else provide complete summary.
                """;
        return process(new AiDataStruct(instructions, new DataDto(remainingKills, missionProfit)), originalUserInput);
    }

    private String computeKillsRemaining(Map<Long, MissionDto> missions, Set<BountyDto> bounties) {
        // Count unique kills by victimFaction
        Map<String, Long> uniqueKillsByTarget = bounties.stream()
                .filter(bounty -> bounty.getVictimFaction() != null && bounty.getPilotName() != null)
                .collect(Collectors.groupingBy(
                        bounty -> bounty.getVictimFaction() + "|" + bounty.getPilotName(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getKey().split("\\|")[0],
                        Collectors.summingLong(Map.Entry::getValue)
                ));

        // Sort all missions by missionId
        List<MissionDto> sortedMissions = missions.values().stream()
                .filter(mission -> mission.getMissionTargetFaction() != null)
                .sorted(Comparator.comparingLong(MissionDto::getMissionId))
                .collect(Collectors.toList());

        if (sortedMissions.isEmpty()) {
            return "no missions available";
        }

        // Group missions by faction
        Map<String, List<MissionDto>> missionsByFaction = sortedMissions.stream()
                .collect(Collectors.groupingBy(MissionDto::getFaction));

        String targetFaction = sortedMissions.get(0).getMissionTargetFaction();
        long totalKills = uniqueKillsByTarget.getOrDefault(targetFaction, 0L);

        // Prepare pending missions per faction (Deque of required kill counts)
        Map<String, Deque<Integer>> pendingMissionsByFaction = new HashMap<>();
        for (String faction : missionsByFaction.keySet()) {
            List<Integer> killCounts = missionsByFaction.get(faction).stream()
                    .sorted(Comparator.comparingLong(MissionDto::getMissionId))
                    .mapToInt(MissionDto::getKillCount)
                    .boxed()
                    .collect(Collectors.toList());
            pendingMissionsByFaction.put(faction, new LinkedList<>(killCounts));
        }

        // Simulate kill allocation to find current state
        Map<String, Integer> activeMissions = new HashMap<>();
        Map<String, Integer> completedMissionsByFaction = new HashMap<>();
        for (String faction : pendingMissionsByFaction.keySet()) {
            Deque<Integer> pending = pendingMissionsByFaction.get(faction);
            if (!pending.isEmpty()) {
                activeMissions.put(faction, pending.pollFirst());
            }
            completedMissionsByFaction.put(faction, 0);
        }

        long remainingKillsToApply = totalKills;
        while (remainingKillsToApply > 0 && !activeMissions.isEmpty()) {
            long minRemaining = activeMissions.values().stream().min(Integer::compareTo).orElse(Integer.MAX_VALUE);
            minRemaining = Math.min(minRemaining, remainingKillsToApply);
            List<String> factionsToProcess = new ArrayList<>(activeMissions.keySet());
            for (String faction : factionsToProcess) {
                int newRemaining = activeMissions.get(faction) - (int) minRemaining;
                activeMissions.put(faction, newRemaining);
                if (newRemaining <= 0) {
                    completedMissionsByFaction.merge(faction, 1, Integer::sum);
                    activeMissions.remove(faction);
                    Deque<Integer> pending = pendingMissionsByFaction.get(faction);
                    if (!pending.isEmpty()) {
                        activeMissions.put(faction, pending.pollFirst());
                    }
                }
            }
            remainingKillsToApply -= minRemaining;
        }

        // Kills remaining per faction (current active or 0)
        Map<String, Integer> killsRemainingByFaction = new HashMap<>();
        for (String faction : missionsByFaction.keySet()) {
            killsRemainingByFaction.put(faction, activeMissions.getOrDefault(faction, 0));
        }

        // Compute total kills remaining to complete all missions
        // Make copies for simulation
        Map<String, Integer> simActive = new HashMap<>(activeMissions);
        Map<String, Deque<Integer>> simPending = new HashMap<>();
        for (String faction : pendingMissionsByFaction.keySet()) {
            simPending.put(faction, new LinkedList<>(pendingMissionsByFaction.get(faction)));
        }

        long totalKillsRemaining = 0;
        while (!simActive.isEmpty()) {
            long minRemaining = simActive.values().stream().min(Integer::compareTo).orElse(Integer.MAX_VALUE);
            totalKillsRemaining += minRemaining;
            List<String> factionsToProcess = new ArrayList<>(simActive.keySet());
            for (String faction : factionsToProcess) {
                int newRemaining = simActive.get(faction) - (int) minRemaining;
                simActive.put(faction, newRemaining);
                if (newRemaining <= 0) {
                    simActive.remove(faction);
                    Deque<Integer> pending = simPending.get(faction);
                    if (!pending.isEmpty()) {
                        simActive.put(faction, pending.pollFirst());
                    }
                }
            }
        }

        // Build response string
        List<String> factionSummaries = new ArrayList<>();
        List<String> sortedFactions = new ArrayList<>(missionsByFaction.keySet());
        sortedFactions.sort(String::compareTo);
        for (String faction : sortedFactions) {
            int completed = completedMissionsByFaction.getOrDefault(faction, 0);
            int killsRemaining = killsRemainingByFaction.getOrDefault(faction, 0);
            StringBuilder summary = new StringBuilder();
            summary.append(faction).append(" ").append(killsRemaining).append(" Kills remaining");
            if (completed > 0) {
                summary.append(", ").append(completed == 1 ? "one" : completed).append(" mission");
                if (completed > 1) {
                    summary.append("s");
                }
                summary.append(" completed");
            }
            factionSummaries.add(summary.toString());
        }

        String summary = String.join(". ", factionSummaries);
        StringBuilder sb = new StringBuilder();
        if (!factionSummaries.isEmpty()) {
            sb.append(totalKillsRemaining).append(" kills remain to complete the assignment. Summary: ");
        }
        sb.append(summary);

        return sb.toString();
    }

    private String computeMissionProfit(Map<Long, MissionDto> missionsMap, Set<BountyDto> bounties) {

        Collection<MissionDto> missions = missionsMap.values();
        long missionReward = 0;
        for (MissionDto mission : missions) {
            missionReward += mission.getReward();
        }

        long bountyReward = 0;
        for (BountyDto bounty : bounties) {
            bountyReward += bounty.getRewards().stream().mapToLong(r -> r.getReward()).sum();
        }
        return "Total mission profit:" + (missionReward + bountyReward);
    }

    record DataDto(String totalMissionKillsLeft, String totalMissionProfit) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}