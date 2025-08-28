package elite.companion.comms.voice;

public enum Voices {
    JAMES("James"),
    MICHAEL("Michael"),
    JENNIFER("Jennifer"),
    MARY("Mary"),
    ANNA("Anna"),
    CHARLES("Charles"),
    STEVE("Steve"),
    JOSEPH("Joseph"),
    LANA("Lana"),
    KAREN("Karen"),
    RACHEL("Rachel"),
    ;

    private String name;


    Voices(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
