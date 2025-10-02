package elite.intel.gameapi;

public class VocalisationRequestEvent {

    public VocalisationRequestEvent(String textToVoice) {

        this.textToVoice = textToVoice;
        this.useRandom = false;
    }

    public VocalisationRequestEvent(String textToVoice, boolean useRandom) {
        this.textToVoice = textToVoice;
        this.useRandom = useRandom;
    }

    private String textToVoice;
    private boolean useRandom;

    public String getText() {
        return textToVoice;
    }


    public boolean isUseRandom() {
        return useRandom;
    }
}
