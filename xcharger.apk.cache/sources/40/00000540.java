package com.xcharge.charger.core.api;

import com.xcharge.charger.data.proxy.IDGeneratorContentProxy;
import java.util.Random;

/* loaded from: classes.dex */
public class LocalIdGenerator {
    public static String getTsChargeId() {
        long ts = System.currentTimeMillis();
        int rand = new Random().nextInt(9999);
        return String.valueOf(String.format("%014d", Long.valueOf(ts))) + String.format("%04d", Integer.valueOf(rand));
    }

    public static synchronized String getChargeId() {
        String valueOf;
        synchronized (LocalIdGenerator.class) {
            String[] rslt = IDGeneratorContentProxy.getInstance().getChargeIdSeq();
            if (rslt == null && !IDGeneratorContentProxy.getInstance().initChargeIdSeq()) {
                valueOf = getTsChargeId();
            } else {
                long seq = Long.parseLong(rslt[1]);
                long newSeq = seq + 1;
                if (!IDGeneratorContentProxy.getInstance().updateChargeIdSeq(rslt[0], newSeq)) {
                    valueOf = getTsChargeId();
                } else {
                    valueOf = String.valueOf(newSeq);
                }
            }
        }
        return valueOf;
    }

    public static long formatChargeId(String chargeId) {
        String trimFirstZero = chargeId.replaceFirst("^0+", "");
        return Long.parseLong(trimFirstZero);
    }
}