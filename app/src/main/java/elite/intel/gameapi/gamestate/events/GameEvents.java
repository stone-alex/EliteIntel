package elite.intel.gameapi.gamestate.events;

import com.google.gson.annotations.SerializedName;
import elite.intel.util.json.GsonFactory;

import java.util.List;

public class GameEvents {

    public static class CargoEvent {
        @SerializedName("timestamp")
        private String timestamp;
        @SerializedName("event")
        private String event;
        @SerializedName("Vessel")
        private String vessel;
        @SerializedName("Count")
        private int count;
        @SerializedName("Inventory")
        private List<Object> inventory;

        // Getters
        public String getTimestamp() {
            return timestamp;
        }

        public String getEvent() {
            return event;
        }

        public String getVessel() {
            return vessel;
        }

        public int getCount() {
            return count;
        }

        public List<Object> getInventory() {
            return inventory;
        }

        public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

    public static class ModulesInfoEvent {
        public static class Module {
            @SerializedName("Slot")
            private String slot;
            @SerializedName("Item")
            private String item;
            @SerializedName("Power")
            private double power;
            @SerializedName("Priority")
            private int priority;

            // Getters
            public String getSlot() {
                return slot;
            }

            public String getItem() {
                return item;
            }

            public double getPower() {
                return power;
            }

            public int getPriority() {
                return priority;
            }

            public String toJson() {
                return GsonFactory.getGson().toJson(this);
            }
        }

        @SerializedName("timestamp")
        private String timestamp;
        @SerializedName("event")
        private String event;
        @SerializedName("Modules")
        private List<Module> modules;

        // Getters
        public String getTimestamp() {
            return timestamp;
        }

        public String getEvent() {
            return event;
        }

        public List<Module> getModules() {
            return modules;
        }
    }

    public static class StatusEvent {
        public static class Fuel {
            @SerializedName("FuelMain")
            private double fuelMain;
            @SerializedName("FuelReservoir")
            private double fuelReservoir;

            // Getters
            public double getFuelMain() {
                return fuelMain;
            }

            public double getFuelReservoir() {
                return fuelReservoir;
            }

            public String toJson() {
                return GsonFactory.getGson().toJson(this);
            }
        }

        public static class Destination {
            @SerializedName("System")
            private long system;
            @SerializedName("Body")
            private int body;
            @SerializedName("Name")
            private String name;

            // Getters
            public long getSystem() {
                return system;
            }

            public int getBody() {
                return body;
            }

            public String getName() {
                return name;
            }

            public String toJson() {
                return GsonFactory.getGson().toJson(this);
            }
        }

        @SerializedName("timestamp")
        private String timestamp;
        @SerializedName("event")
        private String event;
        @SerializedName("Flags")
        private int flags;
        @SerializedName("Flags2")
        private int flags2;
        @SerializedName("Pips")
        private int[] pips;
        @SerializedName("FireGroup")
        private int fireGroup;
        @SerializedName("GuiFocus")
        private int guiFocus;
        @SerializedName("Fuel")
        private Fuel fuel;
        @SerializedName("Cargo")
        private double cargo;
        @SerializedName("LegalState")
        private String legalState;
        @SerializedName("Latitude")
        private double latitude;
        @SerializedName("Longitude")
        private double longitude;
        @SerializedName("Heading")
        private int heading;
        @SerializedName("Altitude")
        private double altitude;
        @SerializedName("Balance")
        private long balance;
        @SerializedName("Destination")
        private Destination destination;
        @SerializedName("PlanetRadius")
        private double planetRadius;


        // Getters
        public String getTimestamp() {
            return timestamp;
        }

        public String getEvent() {
            return event;
        }

        public int getFlags() {
            return flags;
        }

        public int getFlags2() {
            return flags2;
        }

        public int[] getPips() {
            return pips;
        }

        public int getFireGroup() {
            return fireGroup;
        }

        public int getGuiFocus() {
            return guiFocus;
        }

        public Fuel getFuel() {
            return fuel;
        }

        public double getCargo() {
            return cargo;
        }

        public String getLegalState() {
            return legalState;
        }

        public long getBalance() {
            return balance;
        }

        public Destination getDestination() {
            return destination;
        }

        public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public int getHeading() {
            return heading;
        }

        public double getAltitude() {
            return altitude;
        }

        public double getPlanetRadius() {
            return planetRadius;
        }
    }

    public static class BackpackEvent {
        @SerializedName("timestamp")
        private String timestamp;
        @SerializedName("event")
        private String event;
        @SerializedName("Items")
        private List<Object> items;
        @SerializedName("Components")
        private List<Object> components;
        @SerializedName("Consumables")
        private List<Object> consumables;
        @SerializedName("Data")
        private List<Object> data;

        // Getters
        public String getTimestamp() {
            return timestamp;
        }

        public String getEvent() {
            return event;
        }

        public List<Object> getItems() {
            return items;
        }

        public List<Object> getComponents() {
            return components;
        }

        public List<Object> getConsumables() {
            return consumables;
        }

        public List<Object> getData() {
            return data;
        }

        public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }


    }

    public static class NavRouteEvent {
        public static class RouteEntry {
            @SerializedName("StarSystem")
            private String starSystem;
            @SerializedName("SystemAddress")
            private long systemAddress;
            @SerializedName("StarPos")
            private float[] starPos;
            @SerializedName("StarClass")
            private String starClass;

            // Getters
            public String getStarSystem() {
                return starSystem;
            }

            public long getSystemAddress() {
                return systemAddress;
            }

            public float[] getStarPos() {
                return starPos;
            }

            public String getStarClass() {
                return starClass;
            }

            public boolean isFuelStart() {
                return "KGBFOAM".contains(starClass.toUpperCase());
            }

            public String toJson() {
                return GsonFactory.getGson().toJson(this);
            }
        }

        @SerializedName("timestamp")
        private String timestamp;
        @SerializedName("event")
        private String event;
        @SerializedName("Route")
        private List<RouteEntry> route;

        // Getters
        public String getTimestamp() {
            return timestamp;
        }

        public String getEvent() {
            return event;
        }

        public List<RouteEntry> getRoute() {
            return route;
        }

        public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

    public static class FCMaterialsEvent {
        @SerializedName("timestamp")
        private String timestamp;
        @SerializedName("event")
        private String event;
        @SerializedName("MarketID")
        private long marketID;
        @SerializedName("CarrierName")
        private String carrierName;
        @SerializedName("CarrierID")
        private String carrierID;
        @SerializedName("Items")
        private List<Object> items;

        // Getters
        public String getTimestamp() {
            return timestamp;
        }

        public String getEvent() {
            return event;
        }

        public long getMarketID() {
            return marketID;
        }

        public String getCarrierName() {
            return carrierName;
        }

        public String getCarrierID() {
            return carrierID;
        }

        public List<Object> getItems() {
            return items;
        }

        public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

    public static class OutfittingEvent {
        @SerializedName("timestamp")
        private String timestamp;
        @SerializedName("event")
        private String event;
        @SerializedName("MarketID")
        private long marketID;
        @SerializedName("StationName")
        private String stationName;
        @SerializedName("StarSystem")
        private String starSystem;
        @SerializedName("Horizons")
        private boolean horizons;
        @SerializedName("Items")
        private List<Object> items;

        // Getters
        public String getTimestamp() {
            return timestamp;
        }

        public String getEvent() {
            return event;
        }

        public long getMarketID() {
            return marketID;
        }

        public String getStationName() {
            return stationName;
        }

        public String getStarSystem() {
            return starSystem;
        }

        public boolean isHorizons() {
            return horizons;
        }

        public List<Object> getItems() {
            return items;
        }

        public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

    public static class ShipyardEvent {
        public static class ShipPrice {
            @SerializedName("id")
            private int id;
            @SerializedName("ShipType")
            private String shipType;
            @SerializedName("ShipType_Localised")
            private String shipTypeLocalised;
            @SerializedName("ShipPrice")
            private long shipPrice;

            // Getters
            public int getId() {
                return id;
            }

            public String getShipType() {
                return shipType;
            }

            public String getShipTypeLocalised() {
                return shipTypeLocalised;
            }

            public long getShipPrice() {
                return shipPrice;
            }

            public String toJson() {
                return GsonFactory.getGson().toJson(this);
            }
        }

        @SerializedName("timestamp")
        private String timestamp;
        @SerializedName("event")
        private String event;
        @SerializedName("MarketID")
        private long marketID;
        @SerializedName("StationName")
        private String stationName;
        @SerializedName("StarSystem")
        private String starSystem;
        @SerializedName("Horizons")
        private boolean horizons;
        @SerializedName("AllowCobraMkIV")
        private boolean allowCobraMkIV;
        @SerializedName("PriceList")
        private List<ShipPrice> priceList;

        // Getters
        public String getTimestamp() {
            return timestamp;
        }

        public String getEvent() {
            return event;
        }

        public long getMarketID() {
            return marketID;
        }

        public String getStationName() {
            return stationName;
        }

        public String getStarSystem() {
            return starSystem;
        }

        public boolean isHorizons() {
            return horizons;
        }

        public boolean isAllowCobraMkIV() {
            return allowCobraMkIV;
        }

        public List<ShipPrice> getPriceList() {
            return priceList;
        }

        public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

    public static class ShipLockerEvent {
        public static class Component {
            @SerializedName("Name")
            private String name;
            @SerializedName("Name_Localised")
            private String nameLocalised;
            @SerializedName("OwnerID")
            private int ownerID;
            @SerializedName("Count")
            private int count;

            // Getters
            public String getName() {
                return name;
            }

            public String getNameLocalised() {
                return nameLocalised;
            }

            public int getOwnerID() {
                return ownerID;
            }

            public int getCount() {
                return count;
            }

            public String toJson() {
                return GsonFactory.getGson().toJson(this);
            }
        }

        public static class Consumable {
            @SerializedName("Name")
            private String name;
            @SerializedName("Name_Localised")
            private String nameLocalised;
            @SerializedName("OwnerID")
            private int ownerID;
            @SerializedName("Count")
            private int count;

            // Getters
            public String getName() {
                return name;
            }

            public String getNameLocalised() {
                return nameLocalised;
            }

            public int getOwnerID() {
                return ownerID;
            }

            public int getCount() {
                return count;
            }

            public String toJson() {
                return GsonFactory.getGson().toJson(this);
            }
        }

        @SerializedName("timestamp")
        private String timestamp;
        @SerializedName("event")
        private String event;
        @SerializedName("Items")
        private List<Object> items;
        @SerializedName("Components")
        private List<Component> components;
        @SerializedName("Consumables")
        private List<Consumable> consumables;
        @SerializedName("Data")
        private List<Object> data;

        // Getters
        public String getTimestamp() {
            return timestamp;
        }

        public String getEvent() {
            return event;
        }

        public List<Object> getItems() {
            return items;
        }

        public List<Component> getComponents() {
            return components;
        }

        public List<Consumable> getConsumables() {
            return consumables;
        }

        public List<Object> getData() {
            return data;
        }

        public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

    public static class MarketEvent {
        public static class MarketItem {
            @SerializedName("id")
            private long id;
            @SerializedName("Name")
            private String name;
            @SerializedName("Name_Localised")
            private String nameLocalised;
            @SerializedName("Category")
            private String category;
            @SerializedName("Category_Localised")
            private String categoryLocalised;
            @SerializedName("BuyPrice")
            private int buyPrice;
            @SerializedName("SellPrice")
            int sellPrice;
            @SerializedName("MeanPrice")
            private int meanPrice;
            @SerializedName("StockBracket")
            private int stockBracket;
            @SerializedName("DemandBracket")
            private int demandBracket;
            @SerializedName("Stock")
            private int stock;
            @SerializedName("Demand")
            private int demand;
            @SerializedName("Consumer")
            private boolean consumer;
            @SerializedName("Producer")
            private boolean producer;
            @SerializedName("Rare")
            private boolean rare;

            // Getters
            public long getId() {
                return id;
            }

            public String getName() {
                return name;
            }

            public String getNameLocalised() {
                return nameLocalised;
            }

            public String getCategory() {
                return category;
            }

            public String getCategoryLocalised() {
                return categoryLocalised;
            }

            public int getBuyPrice() {
                return buyPrice;
            }

            public int getSellPrice() {
                return sellPrice;
            }

            public int getMeanPrice() {
                return meanPrice;
            }

            public int getStockBracket() {
                return stockBracket;
            }

            public int getDemandBracket() {
                return demandBracket;
            }

            public int getStock() {
                return stock;
            }

            public int getDemand() {
                return demand;
            }

            public boolean isConsumer() {
                return consumer;
            }

            public boolean isProducer() {
                return producer;
            }

            public boolean isRare() {
                return rare;
            }

            public String toJson() {
                return GsonFactory.getGson().toJson(this);
            }
        }

        @SerializedName("timestamp")
        private String timestamp;
        @SerializedName("event")
        private String event;
        @SerializedName("MarketID")
        private long marketID;
        @SerializedName("StationName")
        private String stationName;
        @SerializedName("StationType")
        private String stationType;
        @SerializedName("StarSystem")
        private String starSystem;
        @SerializedName("Items")
        private List<MarketItem> items;

        // Getters
        public String getTimestamp() {
            return timestamp;
        }

        public String getEvent() {
            return event;
        }

        public long getMarketID() {
            return marketID;
        }

        public String getStationName() {
            return stationName;
        }

        public String getStationType() {
            return stationType;
        }

        public String getStarSystem() {
            return starSystem;
        }

        public List<MarketItem> getItems() {
            return items;
        }

        public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}