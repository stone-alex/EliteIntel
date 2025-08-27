package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;
import java.time.Duration;

public class ProspectedAsteroidEvent extends BaseEvent {

    @SerializedName("Materials")
    public Material[] materials;

    @SerializedName("Content")
    public String content;

    @SerializedName("Content_Localised")
    public String contentLocalised;

    @SerializedName("Remaining")
    public double remaining;

    public ProspectedAsteroidEvent(String timestamp, Material[] materials, String content, String contentLocalised, double remaining) {
        super(timestamp, 2, Duration.ofMinutes(10), ProspectedAsteroidEvent.class.getName());
        this.materials = materials;
        this.content = content;
        this.contentLocalised = contentLocalised;
        this.remaining = remaining;
    }

    public static class Material {
        @SerializedName("Name")
        public String name;

        @SerializedName("Name_Localised")
        public String nameLocalised;

        @SerializedName("Proportion")
        public double proportion;

        public String getName() {
            return name;
        }

        public String getNameLocalised() {
            return nameLocalised;
        }

        public double getProportion() {
            return proportion;
        }
    }


    public Material[] getMaterials() {
        return materials;
    }

    public String getContent() {
        return content;
    }

    public String getContentLocalised() {
        return contentLocalised;
    }

    public double getRemaining() {
        return remaining;
    }

    public String getMaterialSummary() {
        StringBuilder materialSummary = new StringBuilder();
        materialSummary.append("Prospector identified: ");
        if (materials != null) {
            for (Material m : materials) {
                materialSummary.append(String.format("%.2f%%", m.proportion)).append(" percent ").append(m.name).append(", ");
            }
        }
        return materialSummary.toString();
    }

    @Override
    public String toString() {

        return String.format("%s: Prospected asteroid (%s) with materials: %s", timestamp, contentLocalised, getMaterialSummary());
    }
}