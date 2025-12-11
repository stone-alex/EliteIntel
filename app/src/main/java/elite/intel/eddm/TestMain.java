package elite.intel.eddm;

public class TestMain {

    public static void main(String[] args) {
        EdDnClient client = EdDnClient.getInstance();
        client.startListening(jsonNode -> {
            System.out.println(jsonNode);
        });
    }
}
