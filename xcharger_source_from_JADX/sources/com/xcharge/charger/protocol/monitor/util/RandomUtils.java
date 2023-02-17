package com.xcharge.charger.protocol.monitor.util;

import java.util.Random;

public class RandomUtils {
    public static String getCharAndNumr(int length) {
        String val = "";
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            if ("char".equalsIgnoreCase(charOrNum)) {
                val = String.valueOf(val) + ((char) (random.nextInt(26) + (random.nextInt(2) % 2 == 0 ? 65 : 97)));
            } else if ("num".equalsIgnoreCase(charOrNum)) {
                val = String.valueOf(val) + String.valueOf(random.nextInt(10));
            }
        }
        return val;
    }

    public static String getFixLenLetter(int strLength) {
        String str = "";
        for (int i = 0; i < strLength; i++) {
            str = String.valueOf(str) + ((char) ((int) ((Math.random() * 26.0d) + 65.0d)));
        }
        return str;
    }

    public static String getFixLenStr(int strLength) {
        String ret = String.valueOf((1.0d + new Random().nextDouble()) * Math.pow(10.0d, (double) strLength)).substring(1, strLength + 1);
        if (ret.startsWith("0")) {
            return getFixLenStr(strLength);
        }
        return ret;
    }
}
