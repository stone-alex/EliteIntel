package elite.companion.events;

import com.google.gson.annotations.SerializedName;

import java.time.Duration;

public class ProspectedAsteroidEvent extends BaseEventEvent {

    public static class Material {
        public String Name;
        @SerializedName("Name_Localised")
        public String NameLocalised;
        public double Proportion;
    }

    @SerializedName("Materials")
    public Material[] Materials;
    public String Content;
    @SerializedName("Content_Localised")
    public String ContentLocalised;
    public double Remaining;

    public ProspectedAsteroidEvent(String timestamp, Material[] materials, String content, String contentLocalised, double remaining) {
        super(timestamp, 2, Duration.ofMinutes(10), ProspectedAsteroidEvent.class.getName()); // Medium priority, short TTL
        this.Materials = materials;
        this.Content = content;
        this.ContentLocalised = contentLocalised;
        this.Remaining = remaining;
    }

    @Override
    public String toString() {
        return timestamp + ": Asteroid with content: " + ContentLocalised;
    }
}
