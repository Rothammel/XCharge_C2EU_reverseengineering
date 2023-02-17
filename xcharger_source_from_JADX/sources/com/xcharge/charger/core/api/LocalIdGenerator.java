package com.xcharge.charger.core.api;

import com.xcharge.charger.data.proxy.IDGeneratorContentProxy;
import java.util.Random;

public class LocalIdGenerator {
    public static String getTsChargeId() {
        long ts = System.currentTimeMillis();
        int rand = new Random().nextInt(9999);
        return String.valueOf(String.format("%014d", new Object[]{Long.valueOf(ts)})) + String.format("%04d", new Object[]{Integer.valueOf(rand)});
    }

    public static synchronized String getChargeId() {
        String valueOf;
        synchronized (LocalIdGenerator.class) {
            String[] rslt = IDGeneratorContentProxy.getInstance().getChargeIdSeq();
            if (rslt != null || IDGeneratorContentProxy.getInstance().initChargeIdSeq()) {
                long newSeq = Long.parseLong(rslt[1]) + 1;
                if (!IDGeneratorContentProxy.getInstance().updateChargeIdSeq(rslt[0], newSeq)) {
                    valueOf = getTsChargeId();
                } else {
                    valueOf = String.valueOf(newSeq);
                }
            } else {
                valueOf = getTsChargeId();
            }
        }
        return valueOf;
    }

    public static long formatChargeId(String chargeId) {
        return Long.parseLong(chargeId.replaceFirst("^0+", ""));
    }
}
