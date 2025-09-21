package elite.intel.ai.search.edsm.dto.data;

import com.google.gson.annotations.SerializedName;

import java.util.StringJoiner;

public class TrafficStats {
    @SerializedName("total")
    public int total;
    @SerializedName("week")
    public int week;
    @SerializedName("day")
    public int day;

    public int getTotal() {
        return total;
    }

    public int getWeek() {
        return week;
    }

    public int getDay() {
        return day;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TrafficStats.class.getSimpleName() + "[", "]")
                .add("total=" + total)
                .add("week=" + week)
                .add("day=" + day)
                .toString();
    }
}
