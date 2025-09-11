package elite.companion.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.companion.gameapi.journal.events.dto.BountyDto;
import elite.companion.gameapi.journal.events.dto.MissionDto;
import elite.companion.session.PlayerSession;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PirateMissionAnalyzer extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) {
        QueryActions query = findQuery(action);
        String data = buildDataForAiResponse(query);
        return analyzeData(data, originalUserInput);
    }

    private String buildDataForAiResponse(QueryActions query) {
        PlayerSession session = PlayerSession.getInstance();
        Map<Long, MissionDto> missions = session.getMissions();
        Set<BountyDto> bounties = session.getBounties();

        Map<QueryActions, Supplier<String>> queryHandlers = new HashMap<>();
        queryHandlers.put(QueryActions.QUERY_PIRATE_MISSION_KILLS_REMAINING, () -> computeKillsRemaining(missions, bounties));
        queryHandlers.put(QueryActions.QUERY_PIRATE_MISSION_PROFIT, () -> computeMissionProfit(missions, bounties));

        return queryHandlers.getOrDefault(query, () -> "no data available").get();
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

        String response = String.join(". ", factionSummaries);
        if (!factionSummaries.isEmpty()) {
            response += ". " + totalKillsRemaining + " kills remain to complete the assignment";
        }

        return response.length() > 0 ? response.toString() : "no missions available";
    }

    private String computeMissionProfit(Map<Long, MissionDto> missions, Set<BountyDto> bounties) {
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

        // Prepare pending kills and rewards per faction
        Map<String, Deque<Integer>> pendingKillsByFaction = new HashMap<>();
        Map<String, Deque<Long>> pendingRewardsByFaction = new HashMap<>();
        for (String faction : missionsByFaction.keySet()) {
            Deque<Integer> kills = new LinkedList<>();
            Deque<Long> rewards = new LinkedList<>();
            for (MissionDto m : missionsByFaction.get(faction).stream().sorted(Comparator.comparingLong(MissionDto::getMissionId)).collect(Collectors.toList())) {
                kills.add(m.getKillCount());
                rewards.add(m.getReward());
            }
            pendingKillsByFaction.put(faction, kills);
            pendingRewardsByFaction.put(faction, rewards);
        }

        // Simulate kill allocation
        Map<String, Integer> activeRemaining = new HashMap<>();
        Map<String, Long> activeReward = new HashMap<>();
        for (String faction : pendingKillsByFaction.keySet()) {
            Deque<Integer> kills = pendingKillsByFaction.get(faction);
            Deque<Long> rewards = pendingRewardsByFaction.get(faction);
            if (!kills.isEmpty()) {
                activeRemaining.put(faction, kills.pollFirst());
                activeReward.put(faction, rewards.pollFirst());
            }
        }

        long remainingKillsToApply = totalKills;
        while (remainingKillsToApply > 0 && !activeRemaining.isEmpty()) {
            long minRemaining = activeRemaining.values().stream().min(Integer::compareTo).orElse(Integer.MAX_VALUE);
            minRemaining = Math.min(minRemaining, remainingKillsToApply);
            List<String> factionsToProcess = new ArrayList<>(activeRemaining.keySet());
            for (String faction : factionsToProcess) {
                int newRemaining = activeRemaining.get(faction) - (int) minRemaining;
                activeRemaining.put(faction, newRemaining);
                if (newRemaining <= 0) {
                    activeRemaining.remove(faction);
                    activeReward.remove(faction);
                    Deque<Integer> pendingKills = pendingKillsByFaction.get(faction);
                    Deque<Long> pendingRewards = pendingRewardsByFaction.get(faction);
                    if (!pendingKills.isEmpty()) {
                        activeRemaining.put(faction, pendingKills.pollFirst());
                        activeReward.put(faction, pendingRewards.pollFirst());
                    }
                }
            }
            remainingKillsToApply -= minRemaining;
        }

        // Calculate profit for incomplete missions
        Map<String, Long> profitByFaction = new HashMap<>();
        for (String faction : missionsByFaction.keySet()) {
            long factionProfit = 0;
            // Active mission if remaining >0
            if (activeRemaining.getOrDefault(faction, 0) > 0) {
                factionProfit += activeReward.get(faction);
            }
            // All pending missions
            Deque<Long> pendingRewards = pendingRewardsByFaction.get(faction);
            for (Long r : pendingRewards) {
                factionProfit += r;
            }
            if (factionProfit > 0) {
                profitByFaction.put(faction, factionProfit);
            }
        }

        // Build response string
        List<String> factionSummaries = new ArrayList<>();
        long totalProfit = 0;

        for (String faction : profitByFaction.keySet()) {
            long factionProfit = profitByFaction.get(faction);
            totalProfit += factionProfit;
            factionSummaries.add(String.format("%s - %d credits", faction, factionProfit));
        }

        factionSummaries.sort(String::compareTo);
        StringBuilder response = new StringBuilder();
        response.append(String.join(", ", factionSummaries));
        if (factionSummaries.size() > 1) {
            response.append(String.format(", Total - %d credits", totalProfit));
        }

        return response.length() > 0 ? response.toString() : "no missions available";
    }
}