package elite.intel.eddm;

import java.io.ByteArrayInputStream;
import java.util.zip.InflaterInputStream;


public class ZMQUtil {
    protected static byte[] decompress(byte[] input) {
        try (InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(input))) {
            return iis.readAllBytes();
        } catch (Exception e) {
            return new byte[0];
        }
    }
}

