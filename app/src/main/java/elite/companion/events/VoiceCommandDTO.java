package elite.companion.events;


public class VoiceCommandDTO {
    private String timestamp;
    private String transcribedText;
    private String interpretedAction;
    private String target;

    public VoiceCommandDTO(String timestamp, String transcribedText) {
        this.timestamp = timestamp;
        this.transcribedText = transcribedText;
    }


    public void setInterpretedAction(String interpretedAction) {
        this.interpretedAction = interpretedAction;
    }

    public void setTarget(String param) {
        this.target = param;
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

    public String getTarget() {
        return this.target;
    }
}
