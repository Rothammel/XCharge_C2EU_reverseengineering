package net.xcharger.util;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.logging.Logger;

public class Identities {
    static final String className = Identities.class.getName();
    private static Logger logger = Logger.getLogger(className);
    private static SecureRandom random = new SecureRandom();

    private Identities() {
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static long uuid2() {
        return IdGentral.get().nextId();
    }

    public static long randomLong() {
        return random.nextLong();
    }

    public static String getKey(String index, String type) {
        String lsret = "";
        String[] sw = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        String asmax = index;
        if (asmax == null) {
            asmax = type;
        }
        boolean stepOut = false;
        for (int j = asmax.length() - 1; j >= 0 && !stepOut; j--) {
            String lstmp = asmax.substring(j, j + 1);
            int i = 0;
            while (true) {
                if (i >= sw.length) {
                    break;
                } else if (!lstmp.equals(sw[i])) {
                    i++;
                } else if (i == sw.length - 1) {
                    lsret = String.valueOf("0") + lsret;
                } else {
                    lsret = String.valueOf(sw[i + 1]) + lsret;
                    stepOut = true;
                }
            }
        }
        return String.valueOf(asmax.substring(0, asmax.length() - lsret.length())) + lsret;
    }

    public static void main(String[] args) {
        System.out.println(uuid());
        for (int i = 0; i < 10; i++) {
            System.out.println(uuid2());
            System.out.println(uuid2());
            System.out.println(uuid2());
            System.out.println(uuid2());
            System.out.println(uuid2());
        }
        System.out.println(getKey("XZA000000001", "B0682546525988978688"));
    }
}
