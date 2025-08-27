package elite.companion.gameapi.events;

import com.google.gson.Gson;

public class BaseJsonDto {

    public String toJson() {
        return new Gson().toJson(this);
    }
}
