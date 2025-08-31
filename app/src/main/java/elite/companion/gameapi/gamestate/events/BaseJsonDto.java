package elite.companion.gameapi.gamestate.events;

import elite.companion.util.GsonFactory;

public class BaseJsonDto {

    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
}
