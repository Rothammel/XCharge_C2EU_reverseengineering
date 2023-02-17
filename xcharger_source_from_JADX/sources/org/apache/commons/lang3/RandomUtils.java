package org.apache.commons.lang3;

import java.util.Random;

public class RandomUtils {
    private static final Random RANDOM = new Random();

    public static byte[] nextBytes(int count) {
        boolean z;
        if (count >= 0) {
            z = true;
        } else {
            z = false;
        }
        Validate.isTrue(z, "Count cannot be negative.", new Object[0]);
        byte[] result = new byte[count];
        RANDOM.nextBytes(result);
        return result;
    }

    public static int nextInt(int startInclusive, int endExclusive) {
        boolean z;
        boolean z2 = true;
        if (endExclusive >= startInclusive) {
            z = true;
        } else {
            z = false;
        }
        Validate.isTrue(z, "Start value must be smaller or equal to end value.", new Object[0]);
        if (startInclusive < 0) {
            z2 = false;
        }
        Validate.isTrue(z2, "Both range values must be non-negative.", new Object[0]);
        return startInclusive == endExclusive ? startInclusive : startInclusive + RANDOM.nextInt(endExclusive - startInclusive);
    }

    public static long nextLong(long startInclusive, long endExclusive) {
        boolean z;
        boolean z2 = true;
        if (endExclusive >= startInclusive) {
            z = true;
        } else {
            z = false;
        }
        Validate.isTrue(z, "Start value must be smaller or equal to end value.", new Object[0]);
        if (startInclusive < 0) {
            z2 = false;
        }
        Validate.isTrue(z2, "Both range values must be non-negative.", new Object[0]);
        return startInclusive == endExclusive ? startInclusive : (long) nextDouble((double) startInclusive, (double) endExclusive);
    }

    public static double nextDouble(double startInclusive, double endInclusive) {
        boolean z;
        boolean z2 = true;
        if (endInclusive >= startInclusive) {
            z = true;
        } else {
            z = false;
        }
        Validate.isTrue(z, "Start value must be smaller or equal to end value.", new Object[0]);
        if (startInclusive < 0.0d) {
            z2 = false;
        }
        Validate.isTrue(z2, "Both range values must be non-negative.", new Object[0]);
        return startInclusive == endInclusive ? startInclusive : startInclusive + ((endInclusive - startInclusive) * RANDOM.nextDouble());
    }

    public static float nextFloat(float startInclusive, float endInclusive) {
        boolean z;
        boolean z2 = true;
        if (endInclusive >= startInclusive) {
            z = true;
        } else {
            z = false;
        }
        Validate.isTrue(z, "Start value must be smaller or equal to end value.", new Object[0]);
        if (startInclusive < 0.0f) {
            z2 = false;
        }
        Validate.isTrue(z2, "Both range values must be non-negative.", new Object[0]);
        return startInclusive == endInclusive ? startInclusive : startInclusive + ((endInclusive - startInclusive) * RANDOM.nextFloat());
    }
}
