package elite.intel.ai.hands;

/** Thrown when applying a working copy to the game bindings directory fails. */
public class BindingsApplyException extends Exception {

    public BindingsApplyException(String message) {
        super(message);
    }

    public BindingsApplyException(String message, Throwable cause) {
        super(message, cause);
    }
}
