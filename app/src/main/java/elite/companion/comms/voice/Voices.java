package elite.companion.comms.voice;

public enum Voices {
    ANNA("Anna", 1.2),
    CHARLES("Charles", 1.2),
    JAMES("James", 1.1),
    JENNIFER("Jennifer", 1.2),
    JOSEPH("Joseph", 1.2),
    KAREN("Karen", 1.2),
    LANA("Lana", 1.2),
    MARY("Mary", 1.2),
    MICHAEL("Michael", 1.2),
    RACHEL("Rachel", 1.2),
    STEVE("Steve", 1.2),
    ;

    private String name;
    private double speechRate = 1.2;


    Voices(String name, double speechRate) {
        this.name = name;
        this.speechRate = speechRate;
    }

    public double getSpeechRate() {
        return speechRate;
    }

    public String getName() {
        return name;
    }
}
