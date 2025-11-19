package elite.intel.ai.search.edsm;

import com.google.gson.JsonSyntaxException;
import elite.intel.ai.ConfigManager;
import elite.intel.ai.search.edsm.dto.*;
import elite.intel.ai.search.edsm.dto.data.*;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class EdsmApiClient {
    private static final Logger log = LogManager.getLogger(EdsmApiClient.class);
    private static final String BASE_URL = "https://www.edsm.net";

    private static StringBuilder publicUrl(String endpoint) {
        return new StringBuilder(BASE_URL + endpoint + "?");
    }

    private static StringBuilder authenticatedUrl(String endpoint) {
        return new StringBuilder(BASE_URL + endpoint + "?");
    }

    public static MaterialsDto getMaterials() {
        String endpoint = "/api-commander-v1/get-materials";
        String edsmKey = ConfigManager.getInstance().getSystemKey(ConfigManager.EDSM_KEY);
        String inGamePlayerName = PlayerSession.getInstance().getInGameName();

        StringBuilder query = authenticatedUrl(endpoint);
        query.append("apiKey=").append(edsmKey);
        query.append("&commanderName=").append(URLEncoder.encode(inGamePlayerName, StandardCharsets.UTF_8));
        query.append("&type=").append(MaterialsType.MATERIAL.getType());

        String response = getResponse(query);
        if (response == null || response.isEmpty()) {
            return new MaterialsDto();
        }
        return GsonFactory.getGson().fromJson(response, MaterialsDto.class);
    }


    public static EncodedMaterialsDto getEncodedMaterials() {
        String endpoint = "/api-commander-v1/get-materials";
        String edsmKey = ConfigManager.getInstance().getSystemKey(ConfigManager.EDSM_KEY);
        String inGamePlayerName = PlayerSession.getInstance().getInGameName();

        StringBuilder query = authenticatedUrl(endpoint);
        query.append("apiKey=").append(edsmKey);
        query.append("&commanderName=").append(URLEncoder.encode(inGamePlayerName, StandardCharsets.UTF_8));
        query.append("&type=").append(MaterialsType.ENCODED.getType());

        String response = getResponse(query);
        if (response == null || response.isEmpty()) {
            return new EncodedMaterialsDto();
        }
        return GsonFactory.getGson().fromJson(response, EncodedMaterialsDto.class);
    }


    public static StarSystemDto searchStarSystem(String starSystemName, int showInformation) {
        if (starSystemName == null) return new StarSystemDto();
        String endpoint = "/api-v1/systems";  // Note: Fixed to match docs if needed; check your DTO
        StringBuilder query = publicUrl(endpoint);
        try {
            query.append("systemName=").append(URLEncoder.encode(starSystemName, StandardCharsets.UTF_8));
            query.append("&showInformation=").append(showInformation);
        } catch (Exception e) {
            log.error("Failed to encode query parameters", e);
            return new StarSystemDto();
        }
        String response = getResponse(query);
        long timestamp = System.currentTimeMillis();
        StarSystemData data;
        if (response.isEmpty()) {
            data = new StarSystemData();
        } else {
            try {
                data = GsonFactory.getGson().fromJson(response, StarSystemData.class);
            } catch (JsonSyntaxException e) {
                log.warn("Invalid JSON from EDSM: {}", response, e);
                return new StarSystemDto();
            }
        }
        StarSystemDto dto = new StarSystemDto();
        dto.data = data;
        dto.timestamp = timestamp;
        return dto;
    }

    public static StarsWithinRadiusDto searchStarSystems(String starSystemName, int radius) {
        if (starSystemName == null) return new StarsWithinRadiusDto();
        String endpoint = "/api-v1/sphere-systems";
        StringBuilder query = publicUrl(endpoint);
        try {
            query.append("systemName=").append(URLEncoder.encode(starSystemName, StandardCharsets.UTF_8));
            query.append("&minRadius=1");
            query.append("&radius=").append(radius);
        } catch (Exception e) {
            log.error("Failed to encode query parameters", e);
            return new StarsWithinRadiusDto();
        }
        String response = getResponse(query);
        if (response.isEmpty()) {
            return new StarsWithinRadiusDto();
        }
        try {
            return GsonFactory.getGson().fromJson(response, StarsWithinRadiusDto.class);
        } catch (JsonSyntaxException e) {
            log.warn("Invalid JSON from EDSM: {}", response, e);
            return new StarsWithinRadiusDto();
        }
    }

    public static SystemBodiesDto searchSystemBodies(String starSystemName) {
        if (starSystemName == null) return new SystemBodiesDto();
        String endpoint = "/api-system-v1/bodies";
        StringBuilder query = publicUrl(endpoint);
        try {
            query.append("systemName=").append(URLEncoder.encode(starSystemName, StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Failed to encode query parameters", e);
            return new SystemBodiesDto();
        }
        String response = getResponse(query);
        log.debug("EDSM bodies response for {}: {}", starSystemName, response);  // Swapped to debug
        long timestamp = System.currentTimeMillis();
        SystemBodiesData data;
        if (response.isEmpty()) {
            data = new SystemBodiesData();
        } else {
            try {
                data = GsonFactory.getGson().fromJson(response, SystemBodiesData.class);
            } catch (JsonSyntaxException e) {
                log.warn("Invalid JSON from EDSM bodies: {}", response, e);
                data = new SystemBodiesData();
            }
        }
        SystemBodiesDto dto = new SystemBodiesDto();
        dto.data = data;
        dto.timestamp = timestamp;
        return dto;
    }

    public static EstimatedScanValuesDto searchEstimatedScanValues(String starSystemName) {
        if (starSystemName == null) return new EstimatedScanValuesDto();
        String endpoint = "/api-system-v1/estimated-value";
        StringBuilder query = publicUrl(endpoint);
        try {
            query.append("systemName=").append(URLEncoder.encode(starSystemName, StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Failed to encode query parameters", e);
            return new EstimatedScanValuesDto();
        }
        String response = getResponse(query);
        long timestamp = System.currentTimeMillis();
        EstimatedScanValuesData data;
        if (response.isEmpty()) {
            data = new EstimatedScanValuesData();
        } else {
            try {
                data = GsonFactory.getGson().fromJson(response, EstimatedScanValuesData.class);
            } catch (JsonSyntaxException e) {
                log.warn("Invalid JSON from EDSM estimated-value: {}", response, e);
                data = new EstimatedScanValuesData();
            }
        }
        EstimatedScanValuesDto dto = new EstimatedScanValuesDto();
        dto.data = data;
        dto.timestamp = timestamp;
        return dto;
    }

    public static StationsDto searchStations(String starSystemName) {
        if (starSystemName == null) return new StationsDto();
        String endpoint = "/api-system-v1/stations";
        StringBuilder query = publicUrl(endpoint);
        try {
            query.append("systemName=").append(URLEncoder.encode(starSystemName, StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Failed to encode query parameters", e);
            return new StationsDto();
        }
        String response = getResponse(query);
        long timestamp = System.currentTimeMillis();
        StationsData data;
        if (response.isEmpty()) {
            data = new StationsData();
        } else {
            try {
                data = GsonFactory.getGson().fromJson(response, StationsData.class);
            } catch (JsonSyntaxException e) {
                log.warn("Invalid JSON from EDSM stations: {}", response, e);
                data = new StationsData();
            }
        }
        StationsDto dto = new StationsDto();
        dto.data = data;
        dto.timestamp = timestamp;
        return dto;
    }

    public static FactionDto searchFaction(String starSystemName, int showHistory) {
        if (starSystemName == null) return new FactionDto();
        String endpoint = "/api-system-v1/factions";
        StringBuilder query = publicUrl(endpoint);
        try {
            query.append("systemName=").append(URLEncoder.encode(starSystemName, StandardCharsets.UTF_8));
            if (showHistory == 1) {
                query.append("&showHistory=1");
            }
        } catch (Exception e) {
            log.error("Failed to encode query parameters", e);
            return new FactionDto();
        }
        String response = getResponse(query);
        long timestamp = System.currentTimeMillis();
        FactionStats data;
        if (response.isEmpty()) {
            data = new FactionStats();
        } else {
            try {
                data = GsonFactory.getGson().fromJson(response, FactionStats.class);
            } catch (JsonSyntaxException e) {
                log.warn("Invalid JSON from EDSM factions: {}", response, e);
                data = new FactionStats();
            }
        }
        FactionDto dto = new FactionDto();
        dto.data = data;
        dto.timestamp = timestamp;
        return dto;
    }

    public static TrafficDto searchTraffic(String starSystemName) {
        if (starSystemName == null) return new TrafficDto();
        String endpoint = "/api-system-v1/traffic";
        StringBuilder query = publicUrl(endpoint);
        try {
            query.append("systemName=").append(URLEncoder.encode(starSystemName, StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Failed to encode query parameters", e);
            return new TrafficDto();
        }
        String response = getResponse(query);
        long timestamp = System.currentTimeMillis();
        TrafficData data;
        if (response.isEmpty()) {
            data = new TrafficData();
        } else {
            try {
                data = GsonFactory.getGson().fromJson(response, TrafficData.class);
            } catch (JsonSyntaxException e) {
                log.warn("Invalid JSON from EDSM traffic: {}", response, e);
                data = new TrafficData();
            }
        }
        TrafficDto dto = new TrafficDto();
        dto.data = data;
        dto.timestamp = timestamp;
        return dto;
    }

    public static DeathsDto searchDeaths(String starSystemName) {
        if (starSystemName == null) return new DeathsDto();
        String endpoint = "/api-system-v1/deaths";
        StringBuilder query = publicUrl(endpoint);
        try {
            query.append("systemName=").append(URLEncoder.encode(starSystemName, StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Failed to encode query parameters", e);
            return new DeathsDto();
        }
        String response = getResponse(query);
        long timestamp = System.currentTimeMillis();
        DeathsData data;
        if (response.isEmpty()) {
            data = new DeathsData();
        } else {
            try {
                data = GsonFactory.getGson().fromJson(response, DeathsData.class);
            } catch (JsonSyntaxException e) {
                log.warn("Invalid JSON from EDSM deaths: {}", response, e);
                data = new DeathsData();
            }
        }
        DeathsDto dto = new DeathsDto();
        dto.data = data;
        dto.timestamp = timestamp;
        return dto;
    }

    public static MarketDto searchMarket(long marketId, String orSystemName, String andStationName) {
        if (marketId <= 0 && (orSystemName == null || andStationName == null)) return new MarketDto();
        String endpoint = "/api-system-v1/stations/market";
        String response = getServicesResponse(marketId, orSystemName, andStationName, endpoint);
        long timestamp = System.currentTimeMillis();
        MarketStats data;
        if (response.isEmpty()) {
            data = new MarketStats();
        } else {
            try {
                data = GsonFactory.getGson().fromJson(response, MarketStats.class);
            } catch (JsonSyntaxException e) {
                log.warn("Invalid JSON from EDSM market: {}", response, e);
                data = new MarketStats();
            }
        }
        MarketDto dto = new MarketDto();
        dto.data = data;
        dto.timestamp = timestamp;
        return dto;
    }

    public static ShipyardDto searchShipyard(long marketId, String orSystemName, String andStationName) {
        if (marketId <= 0 && (orSystemName == null || andStationName == null)) return new ShipyardDto();
        String endpoint = "/api-system-v1/stations/shipyard";
        String response = getServicesResponse(marketId, orSystemName, andStationName, endpoint);
        long timestamp = System.currentTimeMillis();
        ShipyardData data;
        if (response.isEmpty()) {
            data = new ShipyardData();
        } else {
            try {
                data = GsonFactory.getGson().fromJson(response, ShipyardData.class);
            } catch (JsonSyntaxException e) {
                log.warn("Invalid JSON from EDSM shipyard: {}", response, e);
                data = new ShipyardData();
            }
        }
        ShipyardDto dto = new ShipyardDto();
        dto.data = data;
        dto.timestamp = timestamp;
        return dto;
    }

    public static OutfittingDto searchOutfitting(long marketId, String orSystemName, String andStationName) {
        if (marketId <= 0 && (orSystemName == null || andStationName == null)) return new OutfittingDto();
        String endpoint = "/api-system-v1/stations/outfitting";
        String response = getServicesResponse(marketId, orSystemName, andStationName, endpoint);
        long timestamp = System.currentTimeMillis();
        OutfittingData data;
        if (response.isEmpty()) {
            data = new OutfittingData();
        } else {
            try {
                data = GsonFactory.getGson().fromJson(response, OutfittingData.class);
            } catch (JsonSyntaxException e) {
                log.warn("Invalid JSON from EDSM outfitting: {}", response, e);
                data = new OutfittingData();
            }
        }
        OutfittingDto dto = new OutfittingDto();
        dto.data = data;
        dto.timestamp = timestamp;
        return dto;
    }

    private static String getServicesResponse(long marketId, String orSystemName, String andStationName, String endpoint) {
        StringBuilder query = publicUrl(endpoint);
        try {
            if (marketId > 0) {
                query.append("marketId=").append(marketId);
            } else {
                query.append("systemName=").append(URLEncoder.encode(orSystemName, StandardCharsets.UTF_8));
                query.append("&stationName=").append(URLEncoder.encode(andStationName, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            log.error("Failed to encode query parameters", e);
            return "";
        }
        return getResponse(query);
    }

    private static String getResponse(StringBuilder query) {
        try {
            URI uri = URI.create(query.toString());
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                log.error("EDSM API error: {} - {}", responseCode, conn.getResponseMessage());
                return "";
            }

            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                String response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                log.debug("EDSM API response: {}", response);
                return response;
            }
        } catch (Exception e) {
            log.error("Failed to query EDSM API: {}", e.getMessage(), e);
            return "";
        }
    }
}