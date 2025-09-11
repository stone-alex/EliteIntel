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

        // Map query to corresponding handler
        Map<QueryActions, Supplier<String>> queryHandlers = new HashMap<>();
        queryHandlers.put(QueryActions.QUERY_MISSION_KILLS_REMAINING, () -> computeKillsRemaining(missions, bounties));
        queryHandlers.put(QueryActions.QUERY_MISSION_PROFIT, () -> computeMissionProfit(missions, bounties));

        // Execute handler or return default message
        return queryHandlers.getOrDefault(query, () -> "no data available").get();
    }

    private String computeKillsRemaining(Map<Long, MissionDto> missions, Set<BountyDto> bounties) {
        // Count unique kills by pilotName and victimFaction
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

        // Sort all missions by missionId for kill allocation
        List<MissionDto> sortedMissions = missions.values().stream()
                .filter(mission -> mission.getMissionTargetFaction() != null)
                .sorted(Comparator.comparingLong(MissionDto::getMissionId))
                .collect(Collectors.toList());

        // Allocate kills to missions
        Map<String, Integer> killsCompletedByFaction = new HashMap<>();
        Map<String, Integer> killsRequiredByFaction = new HashMap<>();
        Map<String, Long> remainingKillsByTarget = new HashMap<>(uniqueKillsByTarget);

        for (MissionDto mission : sortedMissions) {
            String faction = mission.getFaction();
            String targetFaction = mission.getMissionTargetFaction();
            long availableKills = remainingKillsByTarget.getOrDefault(targetFaction, 0L);
            int killsCompleted = (int) Math.min(mission.getKillCount(), availableKills);

            killsCompletedByFaction.merge(faction, killsCompleted, Integer::sum);
            killsRequiredByFaction.merge(faction, mission.getKillCount(), Integer::sum);
            remainingKillsByTarget.computeIfPresent(targetFaction, (k, v) -> v - killsCompleted);
        }

        // Build response string
        StringBuilder response = new StringBuilder();
        List<String> factionSummaries = new ArrayList<>();
        for (String faction : killsRequiredByFaction.keySet()) {
            int killsRequired = killsRequiredByFaction.getOrDefault(faction, 0);
            int killsCompleted = killsCompletedByFaction.getOrDefault(faction, 0);
            int killsRemaining = Math.max(0, killsRequired - killsCompleted);
            factionSummaries.add(String.format("%s - %d kills remaining", faction, killsRemaining));
        }

        // Sort summaries for consistent output
        factionSummaries.sort(String::compareTo);
        response.append(String.join(", ", factionSummaries));

        return response.length() > 0 ? response.toString() : "no missions available";
    }

    private String computeMissionProfit(Map<Long, MissionDto> missions, Set<BountyDto> bounties) {
        // Count unique kills by pilotName and victimFaction
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

        // Sort all missions by missionId for kill allocation
        List<MissionDto> sortedMissions = missions.values().stream()
                .filter(mission -> mission.getMissionTargetFaction() != null)
                .sorted(Comparator.comparingLong(MissionDto::getMissionId))
                .collect(Collectors.toList());

        // Allocate kills and track profits
        Map<String, Long> profitByFaction = new HashMap<>();
        Map<String, Long> remainingKillsByTarget = new HashMap<>(uniqueKillsByTarget);

        for (MissionDto mission : sortedMissions) {
            String faction = mission.getFaction();
            String targetFaction = mission.getMissionTargetFaction();
            long availableKills = remainingKillsByTarget.getOrDefault(targetFaction, 0L);
            int killsCompleted = (int) Math.min(mission.getKillCount(), availableKills);

            // Only include incomplete missions
            if (killsCompleted < mission.getKillCount()) {
                profitByFaction.merge(faction, mission.getReward(), Long::sum);
            }
            remainingKillsByTarget.computeIfPresent(targetFaction, (k, v) -> v - killsCompleted);
        }

        // Build response string
        StringBuilder response = new StringBuilder();
        List<String> factionSummaries = new ArrayList<>();
        long totalProfit = 0;

        for (String faction : profitByFaction.keySet()) {
            long factionProfit = profitByFaction.get(faction);
            totalProfit += factionProfit;
            factionSummaries.add(String.format("%s - %d credits", faction, factionProfit));
        }

        // Sort summaries for consistent output
        factionSummaries.sort(String::compareTo);
        response.append(String.join(", ", factionSummaries));
        if (factionSummaries.size() > 1) {
            response.append(String.format(", Total - %d credits", totalProfit));
        }

        return response.length() > 0 ? response.toString() : "no missions available";
    }
}