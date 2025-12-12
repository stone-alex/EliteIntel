package elite.intel.eddn;

public class TestMain {

    public static void main(String[] args) {
        EdDnClient client = EdDnClient.getInstance();
        client.start();
    }
}
