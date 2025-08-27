package elite.companion.gameapi.journal.events;

import java.time.Duration;
import java.util.Objects;
import java.util.StringJoiner;

public class CommanderEvent extends BaseEvent {
    private String FID;
    private String Name;

    public CommanderEvent(String timestamp, String FID, String Name) {
        super(timestamp, 1, Duration.ofSeconds(30), CommanderEvent.class.getName());
        this.FID = FID;
        this.Name = Name;
    }

    public String getFID() {
        return FID;
    }

    public void setFID(String FID) {
        this.FID = FID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommanderEvent commander = (CommanderEvent) o;
        return Objects.equals(FID, commander.FID) && Objects.equals(Name, commander.Name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(FID, Name);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CommanderEvent.class.getSimpleName() + "[", "]")
                .add("FID='" + FID + "'")
                .add("Name='" + Name + "'")
                .toString();
    }

}
