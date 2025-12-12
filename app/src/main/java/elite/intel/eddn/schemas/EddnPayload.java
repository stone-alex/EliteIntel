package elite.intel.eddn.schemas;

import com.google.gson.annotations.SerializedName;

public class EddnPayload<T> {
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
}
