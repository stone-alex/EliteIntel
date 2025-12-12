package elite.intel.eddn;

import elite.intel.session.PlayerSession;
import elite.intel.util.Cypher;

import java.io.ByteArrayInputStream;
import java.util.UUID;
import java.util.zip.InflaterInputStream;


public class ZMQUtil {
    protected static byte[] decompress(byte[] input) {
        try (InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(input))) {
            return iis.readAllBytes();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    /// Generate anonymous ID
    public static String generateUploaderID() {
        PlayerSession playerSession = PlayerSession.getInstance();
        String playerName = playerSession.getPlayerName();
        String inGameName = playerSession.getInGameName();
        return UUID.nameUUIDFromBytes((Cypher.encrypt(playerName + inGameName)).getBytes()).toString();
    }
}

