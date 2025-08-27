package elite.companion.gameapi.gamestate.events;

import com.google.gson.Gson;

public class BaseJsonDto {

    public String toJson() {
        return new Gson().toJson(this);
    }
}
