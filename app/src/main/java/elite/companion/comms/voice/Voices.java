package elite.companion.comms.voice;

public enum Voices {
    ANNA("Anna", 1.2, false),
    CHARLES("Charles", 1.2, true),
    JAMES("James", 1.1, true),
    JENNIFER("Jennifer", 1.2, false),
    JOSEPH("Joseph", 1.2, true),
    KAREN("Karen", 1.2, false),
    JAKE("Lana", 1.2, false),
    MARY("Mary", 1.2, false),
    MICHAEL("Michael", 1.2, true),
    RACHEL("Rachel", 1.2, false),
    STEVE("Steve", 1.2, true),
    BETTY("Betty", 1.2, false),
    EMMA("Emma", 1.2, false),
    OLIVIA("Olivia", 1.2, false),
    ;

    // Default voice settings
    private String name = "James";
    private double speechRate = 1.2;
    private boolean isMale = true;


    Voices(String name, double speechRate, boolean gender) {
        this.name = name;
        this.speechRate = speechRate;
        this.isMale = gender;
    }

    public double getSpeechRate() {
        return speechRate;
    }

    public String getName() {
        return name;
    }

    public boolean isMale() {
        return isMale;
    }
}
