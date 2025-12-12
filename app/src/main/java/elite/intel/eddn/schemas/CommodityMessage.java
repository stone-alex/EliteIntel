package elite.intel.eddn.schemas;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CommodityMessage {
    @SerializedName("timestamp")
    private String timestamp;
    @SerializedName("systemName")
    private String systemName;
    @SerializedName("stationName")
    private String stationName;
    @SerializedName("marketId")
    private long marketId;
    @SerializedName("horizons")
    private boolean horizons = true;
    @SerializedName("odyssey")
    private boolean odyssey = true;
    @SerializedName("commodities")
    private List<CommodityItem> commodities;
    @SerializedName("economies")
    private List<Economy> economies; // Add if available in DTO
    @SerializedName("prohibited")
    private List<String> prohibited; // Add if available


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public long getMarketId() {
        return marketId;
    }

    public void setMarketId(long marketId) {
        this.marketId = marketId;
    }

    public boolean isHorizons() {
        return horizons;
    }

    public void setHorizons(boolean horizons) {
        this.horizons = horizons;
    }

    public boolean isOdyssey() {
        return odyssey;
    }

    public void setOdyssey(boolean odyssey) {
        this.odyssey = odyssey;
    }

    public List<CommodityItem> getCommodities() {
        return commodities;
    }

    public void setCommodities(List<CommodityItem> commodities) {
        this.commodities = commodities;
    }

    public List<Economy> getEconomies() {
        return economies;
    }

    public void setEconomies(List<Economy> economies) {
        this.economies = economies;
    }

    public List<String> getProhibited() {
        return prohibited;
    }

    public void setProhibited(List<String> prohibited) {
        this.prohibited = prohibited;
    }

    // Getters/setters
    public static class CommodityItem {
        @SerializedName("name")
        private String name;
        @SerializedName("buyPrice")
        private int buyPrice;
        @SerializedName("sellPrice")
        private int sellPrice;
        @SerializedName("meanPrice")
        private int meanPrice;
        @SerializedName("stock")
        private int stock;
        @SerializedName("stockBracket")
        private int stockBracket;
        @SerializedName("demand")
        private int demand;
        @SerializedName("demandBracket")
        private int demandBracket;
        // Add statusFlags if needed

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getBuyPrice() {
            return buyPrice;
        }

        public void setBuyPrice(int buyPrice) {
            this.buyPrice = buyPrice;
        }

        public int getSellPrice() {
            return sellPrice;
        }

        public void setSellPrice(int sellPrice) {
            this.sellPrice = sellPrice;
        }

        public int getMeanPrice() {
            return meanPrice;
        }

        public void setMeanPrice(int meanPrice) {
            this.meanPrice = meanPrice;
        }

        public int getStock() {
            return stock;
        }

        public void setStock(int stock) {
            this.stock = stock;
        }

        public int getStockBracket() {
            return stockBracket;
        }

        public void setStockBracket(int stockBracket) {
            this.stockBracket = stockBracket;
        }

        public int getDemand() {
            return demand;
        }

        public void setDemand(int demand) {
            this.demand = demand;
        }

        public int getDemandBracket() {
            return demandBracket;
        }

        public void setDemandBracket(int demandBracket) {
            this.demandBracket = demandBracket;
        }
    }

    public static class Economy {
        @SerializedName("name")
        private String name;
        @SerializedName("proportion")
        private double proportion;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getProportion() {
            return proportion;
        }

        public void setProportion(double proportion) {
            this.proportion = proportion;
        }

        // Getters/setters
    }
}
