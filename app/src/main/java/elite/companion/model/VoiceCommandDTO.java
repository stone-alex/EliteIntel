package elite.companion.model;


import java.util.Map;

public class VoiceCommandDTO {
    private String timestamp;
    private String transcribedText;
    private String interpretedAction;
    private Map<String, String> params = new java.util.HashMap<>();

    public VoiceCommandDTO(String timestamp, String transcribedText) {
        this.timestamp = timestamp;
        this.transcribedText = transcribedText;
    }


    public void setInterpretedAction(String interpretedAction) {
        this.interpretedAction = interpretedAction;
    }

    public void setParams(String param) {
        this.params.put("target", param);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getTranscribedText() {
        return transcribedText;
    }

    public String getInterpretedAction() {
        return interpretedAction;
    }

    public String getParams(String key) {
        return params.get(key);
    }
}
