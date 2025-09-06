package elite.companion.util.json;


import com.google.gson.JsonObject;

import java.util.List;

/**
 * A singleton class that acts as a utility for converting primitive data types or collections
 * of primitive data into JSON objects. The class is designed to handle operations involving
 * instances of {@code PrimitiveData} and directly converts their string representations or lists
 * into JSON, leveraging Gson for serialization.
 * <p>
 * This class prevents external instantiation and provides a single globally accessible instance.
 */
public class PrimitiveDataWrapper {


    public static final PrimitiveDataWrapper INSTANCE = new PrimitiveDataWrapper();

    private PrimitiveDataWrapper() {
        // private constructor to prevent instantiation
    }

    public static PrimitiveDataWrapper getInstance() {
        return INSTANCE;
    }


    public JsonObject convertToJson(String data) {
        PrimitiveData primitiveData = new PrimitiveData();
        primitiveData.setValue(data);
        return GsonFactory.getGson().fromJson(primitiveData.toJson(), JsonObject.class);
    }

    public JsonObject convertToJson(List<PrimitiveData> values) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (PrimitiveData data : values) {
            sb.append(data.toJson()).append(", ");
        }
        sb.append("]");
        String result = sb.toString().replace(", ]", "]");
        return convertToJson(result);
    }
}

