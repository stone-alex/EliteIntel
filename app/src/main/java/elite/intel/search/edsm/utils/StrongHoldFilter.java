package elite.intel.search.edsm.utils;

import elite.intel.gameapi.data.PowerPlayData;
import elite.intel.search.spansh.starsystems.StarSystemResult;

public class StrongHoldFilter {

    public static boolean skipEnemyStrongHold(StarSystemResult a, StarSystemResult b, String pledgedPower) {
        if(a == null || b == null) return false;
        StarSystemResult.SystemRecord aRecord = a.getRecord();
        StarSystemResult.SystemRecord bRecord = b.getRecord();

        boolean aPower = PowerPlayData.hasPower(
                aRecord.getControllingPower()
        ) && pledgedPower.equalsIgnoreCase(aRecord.getControllingPower());

        boolean bPower = PowerPlayData.hasPower(
                bRecord.getControllingPower()
        ) && pledgedPower.equalsIgnoreCase(bRecord.getControllingPower());

        boolean aStrongHold = aPower && "stronghold".equalsIgnoreCase(aRecord.getPowerState());
        boolean bStrongHold = bPower && "stronghold".equalsIgnoreCase(bRecord.getPowerState());
        if (aStrongHold || bStrongHold) return true;
        return false;
    }

}
