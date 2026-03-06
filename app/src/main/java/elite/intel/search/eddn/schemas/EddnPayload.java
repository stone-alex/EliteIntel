package elite.intel.search.eddn.schemas;

import com.google.gson.annotations.SerializedName;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class EddnPayload<T> implements ToJsonConvertible {
    @SerializedName("$schemaRef")
    private final String schemaRef;
    @SerializedName("header")
    private final EddnHeader header;
    @SerializedName("message")
    private final T message;


    public EddnPayload(String schemaRef, EddnHeader header, T message) {
        this.schemaRef = schemaRef;
        this.header = header;
        this.message = message;
    }

    @Override public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}
