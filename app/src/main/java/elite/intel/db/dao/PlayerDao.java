package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(PlayerDao.PlayerMapper.class)
public interface PlayerDao {

    // always row 1
    @SqlQuery("SELECT * FROM player WHERE id = 1")
    Player get();

    @SqlUpdate("""
                    INSERT OR REPLACE INTO player
                      (id, current_primary_star,
                       bounty_collected_this_session, carrier_departure_time, crew_wags_payout,
                       current_ship, current_ship_name, current_location_id, current_wealth,
                       is_discovery_announcement_on, final_destination, game_version,
                       goods_sold_this_session, highest_single_transaction, in_game_name,
                       insurance_claims, is_mining_announcement_on, is_navigation_announcement_on,
                       is_radio_transmission_on, is_route_announcement_on, jumping_to_star_system,
                       last_known_carrier_location, last_scan_id, market_profits,
                       personal_credits_available, player_highest_military_rank,
                       player_mission_statement, player_name, player_title,
                       ship_cargo_capacity, ship_fuel_level, ships_owned, species_first_logged,
                       target_market_station_id, total_bounty_claimed, total_bounty_profit,
                       total_distance_traveled, total_hyperspace_distance,
                       total_profits_from_exploration, total_systems_visited, exobiology_profits, alternative_name,
                       journal_dir, bindings_dir, logging_enabled)
                    VALUES (1, :currentPrimaryStar,
                       :bountyCollectedThisSession, :carrierDepartureTime, :crewWagsPayout,
                       :currentShip, :currentShipName, :currentLocationId, :currentWealth,
                       :discoveryAnnouncementOn, :finalDestination, :gameVersion,
                       :goodsSoldThisSession, :highestSingleTransaction, :inGameName,
                       :insuranceClaims, :miningAnnouncementOn, :navigationAnnouncementOn,
                       :radioTransmissionOn, :routeAnnouncementOn, :jumpingToStarSystem,
                       :lastKnownCarrierLocation, :lastScanId, :marketProfits,
                       :personalCreditsAvailable, :playerHighestMilitaryRank,
                       :playerMissionStatement, :playerName, :playerTitle,
                       :shipCargoCapacity, :shipFuelLevel, :shipsOwned, :speciesFirstLogged,
                       :targetMarketStationId, :totalBountyClaimed, :totalBountyProfit,
                       :totalDistanceTraveled, :totalHyperspaceDistance,
                       :totalProfitsFromExploration, :totalSystemsVisited, :exobiologyProfits, :alternativeName,
                       :journalDirectory, :bindingsDirectory, :loggingEnabled
                    )
            """)
    void save(@BindBean Player player);


    class Player {
        private long bountyCollectedThisSession = 0;
        private String carrierDepartureTime = "";
        private long crewWagsPayout = 0;
        private String currentShip = "";
        private String currentShipName = "";
        private Long currentLocationId = -1L;
        private long currentWealth = 0;
        private boolean isDiscoveryAnnouncementOn = true;
        private String finalDestination = "";
        private String gameVersion = "";
        private int goodsSoldThisSession = 0;
        private long highestSingleTransaction = 0;
        private String inGameName = "";
        private int insuranceClaims = 0;
        private boolean isMiningAnnouncementOn = true;
        private boolean isNavigationAnnouncementOn = true;
        private Boolean isRadioTransmissionOn = null;
        private boolean isRouteAnnouncementOn = true;
        private String jumpingToStarSystem = "";
        private String lastKnownCarrierLocation = "";
        private long lastScanId = -1;
        private long marketProfits = 0;
        private long personalCreditsAvailable = 0;
        private String playerHighestMilitaryRank = "";
        private String playerMissionStatement = "";
        private String playerName = "";
        private String playerTitle = "";
        private int shipCargoCapacity = 0;
        private double shipFuelLevel = 0.0;
        private int shipsOwned = 0;
        private int speciesFirstLogged = 0;
        private Long targetMarketStationId = null;
        private long totalBountyClaimed = 0;
        private long totalBountyProfit = 0;
        private double totalDistanceTraveled = 0.0;
        private long totalHyperspaceDistance = 0;
        private long totalProfitsFromExploration = 0;
        private int totalSystemsVisited = 0;
        private long exobiologyProfits = 0;
        private String currentPrimaryStar = "";

        private String alternativeName;
        private String journalDirectory;
        private String bindingsDirectory;
        private Boolean loggingEnabled;

        public Player() {
        } // required for JDBI

        public String getAlternativeName() {
            return alternativeName;
        }

        public void setAlternativeName(String alternativeName) {
            this.alternativeName = alternativeName;
        }

        public String getJournalDirectory() {
            return journalDirectory;
        }

        public void setJournalDirectory(String journalDirectory) {
            this.journalDirectory = journalDirectory;
        }

        public String getBindingsDirectory() {
            return bindingsDirectory;
        }

        public void setBindingsDirectory(String bindingsDirectory) {
            this.bindingsDirectory = bindingsDirectory;
        }

        public Boolean getLoggingEnabled() {
            return loggingEnabled;
        }

        public void setLoggingEnabled(Boolean loggingEnabled) {
            this.loggingEnabled = loggingEnabled;
        }

        public String getCurrentPrimaryStar() {
            return currentPrimaryStar;
        }
        public void setCurrentPrimaryStar(String currentPrimaryStar) {
            this.currentPrimaryStar = currentPrimaryStar;
        }
        public long getBountyCollectedThisSession() {
            return bountyCollectedThisSession;
        }

        public void setBountyCollectedThisSession(long bountyCollectedThisSession) {
            this.bountyCollectedThisSession = bountyCollectedThisSession;
        }

        public String getCarrierDepartureTime() {
            return carrierDepartureTime;
        }

        public void setCarrierDepartureTime(String carrierDepartureTime) {
            this.carrierDepartureTime = carrierDepartureTime;
        }

        public long getCrewWagsPayout() {
            return crewWagsPayout;
        }

        public void setCrewWagsPayout(long crewWagsPayout) {
            this.crewWagsPayout = crewWagsPayout;
        }

        public String getCurrentShip() {
            return currentShip;
        }

        public void setCurrentShip(String currentShip) {
            this.currentShip = currentShip;
        }

        public String getCurrentShipName() {
            return currentShipName;
        }

        public void setCurrentShipName(String currentShipName) {
            this.currentShipName = currentShipName;
        }

        public Long getCurrentLocationId() {
            return currentLocationId;
        }

        public void setCurrentLocationId(Long currentLocationId) {
            this.currentLocationId = currentLocationId;
        }

        public long getCurrentWealth() {
            return currentWealth;
        }

        public void setCurrentWealth(long currentWealth) {
            this.currentWealth = currentWealth;
        }

        public boolean isDiscoveryAnnouncementOn() {
            return isDiscoveryAnnouncementOn;
        }

        public void setDiscoveryAnnouncementOn(boolean discoveryAnnouncementOn) {
            isDiscoveryAnnouncementOn = discoveryAnnouncementOn;
        }

        public String getFinalDestination() {
            return finalDestination;
        }

        public void setFinalDestination(String finalDestination) {
            this.finalDestination = finalDestination;
        }

        public String getGameVersion() {
            return gameVersion;
        }

        public void setGameVersion(String gameVersion) {
            this.gameVersion = gameVersion;
        }

        public int getGoodsSoldThisSession() {
            return goodsSoldThisSession;
        }

        public void setGoodsSoldThisSession(int goodsSoldThisSession) {
            this.goodsSoldThisSession = goodsSoldThisSession;
        }

        public long getHighestSingleTransaction() {
            return highestSingleTransaction;
        }

        public void setHighestSingleTransaction(long highestSingleTransaction) {
            this.highestSingleTransaction = highestSingleTransaction;
        }

        public String getInGameName() {
            return inGameName;
        }

        public void setInGameName(String inGameName) {
            this.inGameName = inGameName;
        }

        public int getInsuranceClaims() {
            return insuranceClaims;
        }

        public void setInsuranceClaims(int insuranceClaims) {
            this.insuranceClaims = insuranceClaims;
        }

        public boolean isMiningAnnouncementOn() {
            return isMiningAnnouncementOn;
        }

        public void setMiningAnnouncementOn(boolean miningAnnouncementOn) {
            isMiningAnnouncementOn = miningAnnouncementOn;
        }

        public boolean isNavigationAnnouncementOn() {
            return isNavigationAnnouncementOn;
        }

        public void setNavigationAnnouncementOn(boolean navigationAnnouncementOn) {
            isNavigationAnnouncementOn = navigationAnnouncementOn;
        }

        public Boolean getRadioTransmissionOn() {
            return isRadioTransmissionOn;
        }

        public void setRadioTransmissionOn(Boolean radioTransmissionOn) {
            isRadioTransmissionOn = radioTransmissionOn;
        }

        public boolean isRouteAnnouncementOn() {
            return isRouteAnnouncementOn;
        }

        public void setRouteAnnouncementOn(boolean routeAnnouncementOn) {
            isRouteAnnouncementOn = routeAnnouncementOn;
        }

        public String getJumpingToStarSystem() {
            return jumpingToStarSystem;
        }

        public void setJumpingToStarSystem(String jumpingToStarSystem) {
            this.jumpingToStarSystem = jumpingToStarSystem;
        }

        public String getLastKnownCarrierLocation() {
            return lastKnownCarrierLocation;
        }

        public void setLastKnownCarrierLocation(String lastKnownCarrierLocation) {
            this.lastKnownCarrierLocation = lastKnownCarrierLocation;
        }

        public long getLastScanId() {
            return lastScanId;
        }

        public void setLastScanId(long lastScanId) {
            this.lastScanId = lastScanId;
        }

        public long getMarketProfits() {
            return marketProfits;
        }

        public void setMarketProfits(long marketProfits) {
            this.marketProfits = marketProfits;
        }

        public long getPersonalCreditsAvailable() {
            return personalCreditsAvailable;
        }

        public void setPersonalCreditsAvailable(long personalCreditsAvailable) {
            this.personalCreditsAvailable = personalCreditsAvailable;
        }

        public String getPlayerHighestMilitaryRank() {
            return playerHighestMilitaryRank;
        }

        public void setPlayerHighestMilitaryRank(String playerHighestMilitaryRank) {
            this.playerHighestMilitaryRank = playerHighestMilitaryRank;
        }

        public String getPlayerMissionStatement() {
            return playerMissionStatement;
        }

        public void setPlayerMissionStatement(String playerMissionStatement) {
            this.playerMissionStatement = playerMissionStatement;
        }

        public String getPlayerName() {
            return playerName;
        }

        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }

        public String getPlayerTitle() {
            return playerTitle;
        }

        public void setPlayerTitle(String playerTitle) {
            this.playerTitle = playerTitle;
        }

        public int getShipCargoCapacity() {
            return shipCargoCapacity;
        }

        public void setShipCargoCapacity(int shipCargoCapacity) {
            this.shipCargoCapacity = shipCargoCapacity;
        }

        public double getShipFuelLevel() {
            return shipFuelLevel;
        }

        public void setShipFuelLevel(double shipFuelLevel) {
            this.shipFuelLevel = shipFuelLevel;
        }

        public int getShipsOwned() {
            return shipsOwned;
        }

        public void setShipsOwned(int shipsOwned) {
            this.shipsOwned = shipsOwned;
        }

        public int getSpeciesFirstLogged() {
            return speciesFirstLogged;
        }

        public void setSpeciesFirstLogged(int speciesFirstLogged) {
            this.speciesFirstLogged = speciesFirstLogged;
        }

        public Long getTargetMarketStationId() {
            return targetMarketStationId;
        }

        public void setTargetMarketStationId(Long targetMarketStationId) {
            this.targetMarketStationId = targetMarketStationId;
        }

        public long getTotalBountyClaimed() {
            return totalBountyClaimed;
        }

        public void setTotalBountyClaimed(long totalBountyClaimed) {
            this.totalBountyClaimed = totalBountyClaimed;
        }

        public long getTotalBountyProfit() {
            return totalBountyProfit;
        }

        public void setTotalBountyProfit(long totalBountyProfit) {
            this.totalBountyProfit = totalBountyProfit;
        }

        public double getTotalDistanceTraveled() {
            return totalDistanceTraveled;
        }

        public void setTotalDistanceTraveled(double totalDistanceTraveled) {
            this.totalDistanceTraveled = totalDistanceTraveled;
        }

        public long getTotalHyperspaceDistance() {
            return totalHyperspaceDistance;
        }

        public void setTotalHyperspaceDistance(long totalHyperspaceDistance) {
            this.totalHyperspaceDistance = totalHyperspaceDistance;
        }

        public long getTotalProfitsFromExploration() {
            return totalProfitsFromExploration;
        }

        public void setTotalProfitsFromExploration(long totalProfitsFromExploration) {
            this.totalProfitsFromExploration = totalProfitsFromExploration;
        }

        public int getTotalSystemsVisited() {
            return totalSystemsVisited;
        }

        public void setTotalSystemsVisited(int totalSystemsVisited) {
            this.totalSystemsVisited = totalSystemsVisited;
        }

        public long getExobiologyProfits() {
            return exobiologyProfits;
        }

        public void setExobiologyProfits(long exobiologyProfits) {
            this.exobiologyProfits = exobiologyProfits;
        }
    }

    class PlayerMapper implements RowMapper<Player> {
        @Override
        public Player map(ResultSet rs, StatementContext ctx) throws SQLException {
            Player p = new Player();
            p.setBountyCollectedThisSession(rs.getLong("bounty_collected_this_session"));
            p.setCarrierDepartureTime(rs.getString("carrier_departure_time"));
            p.setCrewWagsPayout(rs.getLong("crew_wags_payout"));
            p.setCurrentShip(rs.getString("current_ship"));
            p.setCurrentShipName(rs.getString("current_ship_name"));
            p.setCurrentLocationId(rs.getObject("current_location_id") != null ? rs.getLong("current_location_id") : null);
            p.setCurrentWealth(rs.getLong("current_wealth"));
            p.setDiscoveryAnnouncementOn(rs.getBoolean("is_discovery_announcement_on"));
            p.setFinalDestination(rs.getString("final_destination"));
            p.setGameVersion(rs.getString("game_version"));
            p.setGoodsSoldThisSession(rs.getInt("goods_sold_this_session"));
            p.setHighestSingleTransaction(rs.getLong("highest_single_transaction"));
            p.setInGameName(rs.getString("in_game_name"));
            p.setInsuranceClaims(rs.getInt("insurance_claims"));
            p.setMiningAnnouncementOn(rs.getBoolean("is_mining_announcement_on"));
            p.setNavigationAnnouncementOn(rs.getBoolean("is_navigation_announcement_on"));
            p.setRadioTransmissionOn(rs.getObject("is_radio_transmission_on") != null && rs.getBoolean("is_radio_transmission_on"));
            p.setRouteAnnouncementOn(rs.getBoolean("is_route_announcement_on"));
            p.setJumpingToStarSystem(rs.getString("jumping_to_star_system"));
            p.setLastKnownCarrierLocation(rs.getString("last_known_carrier_location"));
            p.setLastScanId(rs.getLong("last_scan_id"));
            p.setMarketProfits(rs.getLong("market_profits"));
            p.setPersonalCreditsAvailable(rs.getLong("personal_credits_available"));
            p.setPlayerHighestMilitaryRank(rs.getString("player_highest_military_rank"));
            p.setPlayerMissionStatement(rs.getString("player_mission_statement"));
            p.setPlayerName(rs.getString("player_name"));
            p.setPlayerTitle(rs.getString("player_title"));
            p.setShipCargoCapacity(rs.getInt("ship_cargo_capacity"));
            p.setShipFuelLevel(rs.getDouble("ship_fuel_level"));
            p.setShipsOwned(rs.getInt("ships_owned"));
            p.setSpeciesFirstLogged(rs.getInt("species_first_logged"));
            p.setTargetMarketStationId(rs.getObject("target_market_station_id") != null ? rs.getLong("target_market_station_id") : null);
            p.setTotalBountyClaimed(rs.getLong("total_bounty_claimed"));
            p.setTotalBountyProfit(rs.getLong("total_bounty_profit"));
            p.setTotalDistanceTraveled(rs.getDouble("total_distance_traveled"));
            p.setTotalHyperspaceDistance(rs.getLong("total_hyperspace_distance"));
            p.setTotalProfitsFromExploration(rs.getLong("total_profits_from_exploration"));
            p.setTotalSystemsVisited(rs.getInt("total_systems_visited"));
            p.setExobiologyProfits(rs.getLong("exobiology_profits"));
            p.setCurrentPrimaryStar(rs.getString("current_primary_star"));
            p.setAlternativeName(rs.getString("alternative_name"));
            p.setJournalDirectory(rs.getString("journal_dir"));
            p.setBindingsDirectory(rs.getString("bindings_dir"));
            p.setLoggingEnabled(rs.getBoolean("logging_enabled"));
            return p;
        }
    }
}