package org.apache.commons.lang3.math;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class NumberUtils {
    public static final Byte BYTE_MINUS_ONE = (byte) -1;
    public static final Byte BYTE_ONE = (byte) 1;
    public static final Byte BYTE_ZERO = (byte) 0;
    public static final Double DOUBLE_MINUS_ONE = Double.valueOf(-1.0d);
    public static final Double DOUBLE_ONE = Double.valueOf(1.0d);
    public static final Double DOUBLE_ZERO = Double.valueOf(0.0d);
    public static final Float FLOAT_MINUS_ONE = Float.valueOf(-1.0f);
    public static final Float FLOAT_ONE = Float.valueOf(1.0f);
    public static final Float FLOAT_ZERO = Float.valueOf(0.0f);
    public static final Integer INTEGER_MINUS_ONE = -1;
    public static final Integer INTEGER_ONE = 1;
    public static final Integer INTEGER_ZERO = 0;
    public static final Long LONG_MINUS_ONE = -1L;
    public static final Long LONG_ONE = 1L;
    public static final Long LONG_ZERO = 0L;
    public static final Short SHORT_MINUS_ONE = -1;
    public static final Short SHORT_ONE = 1;
    public static final Short SHORT_ZERO = 0;

    public static int toInt(String str) {
        return toInt(str, 0);
    }

    public static int toInt(String str, int defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static long toLong(String str) {
        return toLong(str, 0);
    }

    public static long toLong(String str, long defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static float toFloat(String str) {
        return toFloat(str, 0.0f);
    }

    public static float toFloat(String str, float defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static double toDouble(String str) {
        return toDouble(str, 0.0d);
    }

    public static double toDouble(String str, double defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static byte toByte(String str) {
        return toByte(str, (byte) 0);
    }

    public static byte toByte(String str, byte defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Byte.parseByte(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static short toShort(String str) {
        return toShort(str, 0);
    }

    public static short toShort(String str, short defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Short.parseShort(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Number createNumber(String str) throws NumberFormatException {
        String mant;
        String dec;
        String exp;
        String exp2;
        if (str == null) {
            return null;
        }
        if (StringUtils.isBlank(str)) {
            throw new NumberFormatException("A blank string is not a valid number");
        }
        int pfxLen = 0;
        String[] arr$ = {"0x", "0X", "-0x", "-0X", MqttTopic.MULTI_LEVEL_WILDCARD, "-#"};
        int len$ = arr$.length;
        int i$ = 0;
        while (true) {
            if (i$ >= len$) {
                break;
            }
            String pfx = arr$[i$];
            if (str.startsWith(pfx)) {
                pfxLen = 0 + pfx.length();
                break;
            }
            i$++;
        }
        if (pfxLen > 0) {
            char firstSigDigit = 0;
            int i = pfxLen;
            while (i < str.length() && (firstSigDigit = str.charAt(i)) == '0') {
                pfxLen++;
                i++;
            }
            int hexDigits = str.length() - pfxLen;
            if (hexDigits > 16 || (hexDigits == 16 && firstSigDigit > '7')) {
                return createBigInteger(str);
            }
            if (hexDigits > 8 || (hexDigits == 8 && firstSigDigit > '7')) {
                return createLong(str);
            }
            return createInteger(str);
        }
        char lastChar = str.charAt(str.length() - 1);
        int decPos = str.indexOf(46);
        int expPos = str.indexOf(101) + str.indexOf(69) + 1;
        int numDecimals = 0;
        if (decPos > -1) {
            if (expPos <= -1) {
                dec = str.substring(decPos + 1);
            } else if (expPos < decPos || expPos > str.length()) {
                throw new NumberFormatException(str + " is not a valid number.");
            } else {
                dec = str.substring(decPos + 1, expPos);
            }
            mant = getMantissa(str, decPos);
            numDecimals = dec.length();
        } else {
            if (expPos <= -1) {
                mant = getMantissa(str);
            } else if (expPos > str.length()) {
                throw new NumberFormatException(str + " is not a valid number.");
            } else {
                mant = getMantissa(str, expPos);
            }
            dec = null;
        }
        if (Character.isDigit(lastChar) || lastChar == '.') {
            if (expPos <= -1 || expPos >= str.length() - 1) {
                exp = null;
            } else {
                exp = str.substring(expPos + 1, str.length());
            }
            if (dec == null && exp == null) {
                try {
                    return createInteger(str);
                } catch (NumberFormatException e) {
                    try {
                        return createLong(str);
                    } catch (NumberFormatException e2) {
                        return createBigInteger(str);
                    }
                }
            } else {
                boolean allZeros = isAllZeros(mant) && isAllZeros(exp);
                if (numDecimals <= 7) {
                    try {
                        Float f = createFloat(str);
                        if (!f.isInfinite() && (f.floatValue() != 0.0f || allZeros)) {
                            return f;
                        }
                    } catch (NumberFormatException e3) {
                    }
                }
                if (numDecimals <= 16) {
                    try {
                        Double d = createDouble(str);
                        if (!d.isInfinite() && (d.doubleValue() != 0.0d || allZeros)) {
                            return d;
                        }
                    } catch (NumberFormatException e4) {
                    }
                }
                return createBigDecimal(str);
            }
        } else {
            if (expPos <= -1 || expPos >= str.length() - 1) {
                exp2 = null;
            } else {
                exp2 = str.substring(expPos + 1, str.length() - 1);
            }
            String numeric = str.substring(0, str.length() - 1);
            boolean allZeros2 = isAllZeros(mant) && isAllZeros(exp2);
            switch (lastChar) {
                case 'D':
                case 'd':
                    break;
                case 'F':
                case 'f':
                    try {
                        Float f2 = createFloat(numeric);
                        if (!f2.isInfinite() && (f2.floatValue() != 0.0f || allZeros2)) {
                            return f2;
                        }
                    } catch (NumberFormatException e5) {
                        break;
                    }
                case 'L':
                case 'l':
                    if (dec == null && exp2 == null && ((numeric.charAt(0) == '-' && isDigits(numeric.substring(1))) || isDigits(numeric))) {
                        try {
                            return createLong(numeric);
                        } catch (NumberFormatException e6) {
                            return createBigInteger(numeric);
                        }
                    } else {
                        throw new NumberFormatException(str + " is not a valid number.");
                    }
            }
            try {
                Double d2 = createDouble(numeric);
                if (!d2.isInfinite() && (((double) d2.floatValue()) != 0.0d || allZeros2)) {
                    return d2;
                }
            } catch (NumberFormatException e7) {
            }
            try {
                return createBigDecimal(numeric);
            } catch (NumberFormatException e8) {
            }
        }
        throw new NumberFormatException(str + " is not a valid number.");
    }

    private static String getMantissa(String str) {
        return getMantissa(str, str.length());
    }

    private static String getMantissa(String str, int stopPos) {
        boolean hasSign;
        char firstChar = str.charAt(0);
        if (firstChar == '-' || firstChar == '+') {
            hasSign = true;
        } else {
            hasSign = false;
        }
        return hasSign ? str.substring(1, stopPos) : str.substring(0, stopPos);
    }

    private static boolean isAllZeros(String str) {
        if (str == null) {
            return true;
        }
        for (int i = str.length() - 1; i >= 0; i--) {
            if (str.charAt(i) != '0') {
                return false;
            }
        }
        if (str.length() <= 0) {
            return false;
        }
        return true;
    }

    public static Float createFloat(String str) {
        if (str == null) {
            return null;
        }
        return Float.valueOf(str);
    }

    public static Double createDouble(String str) {
        if (str == null) {
            return null;
        }
        return Double.valueOf(str);
    }

    public static Integer createInteger(String str) {
        if (str == null) {
            return null;
        }
        return Integer.decode(str);
    }

    public static Long createLong(String str) {
        if (str == null) {
            return null;
        }
        return Long.decode(str);
    }

    public static BigInteger createBigInteger(String str) {
        if (str == null) {
            return null;
        }
        int pos = 0;
        int radix = 10;
        boolean negate = false;
        if (str.startsWith("-")) {
            negate = true;
            pos = 1;
        }
        if (str.startsWith("0x", pos) || str.startsWith("0X", pos)) {
            radix = 16;
            pos += 2;
        } else if (str.startsWith(MqttTopic.MULTI_LEVEL_WILDCARD, pos)) {
            radix = 16;
            pos++;
        } else if (str.startsWith("0", pos) && str.length() > pos + 1) {
            radix = 8;
            pos++;
        }
        BigInteger value = new BigInteger(str.substring(pos), radix);
        if (negate) {
            return value.negate();
        }
        return value;
    }

    public static BigDecimal createBigDecimal(String str) {
        if (str == null) {
            return null;
        }
        if (StringUtils.isBlank(str)) {
            throw new NumberFormatException("A blank string is not a valid number");
        } else if (!str.trim().startsWith("--")) {
            return new BigDecimal(str);
        } else {
            throw new NumberFormatException(str + " is not a valid number.");
        }
    }

    public static long min(long... array) {
        validateArray(array);
        long min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    public static int min(int... array) {
        validateArray(array);
        int min = array[0];
        for (int j = 1; j < array.length; j++) {
            if (array[j] < min) {
                min = array[j];
            }
        }
        return min;
    }

    public static short min(short... array) {
        validateArray(array);
        short min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    public static byte min(byte... array) {
        validateArray(array);
        byte min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    public static double min(double... array) {
        validateArray(array);
        double min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (Double.isNaN(array[i])) {
                return Double.NaN;
            }
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    public static float min(float... array) {
        validateArray(array);
        float min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (Float.isNaN(array[i])) {
                return Float.NaN;
            }
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    public static long max(long... array) {
        validateArray(array);
        long max = array[0];
        for (int j = 1; j < array.length; j++) {
            if (array[j] > max) {
                max = array[j];
            }
        }
        return max;
    }

    public static int max(int... array) {
        validateArray(array);
        int max = array[0];
        for (int j = 1; j < array.length; j++) {
            if (array[j] > max) {
                max = array[j];
            }
        }
        return max;
    }

    public static short max(short... array) {
        validateArray(array);
        short max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    public static byte max(byte... array) {
        validateArray(array);
        byte max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    public static double max(double... array) {
        validateArray(array);
        double max = array[0];
        for (int j = 1; j < array.length; j++) {
            if (Double.isNaN(array[j])) {
                return Double.NaN;
            }
            if (array[j] > max) {
                max = array[j];
            }
        }
        return max;
    }

    public static float max(float... array) {
        validateArray(array);
        float max = array[0];
        for (int j = 1; j < array.length; j++) {
            if (Float.isNaN(array[j])) {
                return Float.NaN;
            }
            if (array[j] > max) {
                max = array[j];
            }
        }
        return max;
    }

    private static void validateArray(Object array) {
        boolean z;
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        if (Array.getLength(array) != 0) {
            z = true;
        } else {
            z = false;
        }
        Validate.isTrue(z, "Array cannot be empty.", new Object[0]);
    }

    public static long min(long a, long b, long c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            return c;
        }
        return a;
    }

    public static int min(int a, int b, int c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            return c;
        }
        return a;
    }

    public static short min(short a, short b, short c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            return c;
        }
        return a;
    }

    public static byte min(byte a, byte b, byte c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            return c;
        }
        return a;
    }

    public static double min(double a, double b, double c) {
        return Math.min(Math.min(a, b), c);
    }

    public static float min(float a, float b, float c) {
        return Math.min(Math.min(a, b), c);
    }

    public static long max(long a, long b, long c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            return c;
        }
        return a;
    }

    public static int max(int a, int b, int c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            return c;
        }
        return a;
    }

    public static short max(short a, short b, short c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            return c;
        }
        return a;
    }

    public static byte max(byte a, byte b, byte c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            return c;
        }
        return a;
    }

    public static double max(double a, double b, double c) {
        return Math.max(Math.max(a, b), c);
    }

    public static float max(float a, float b, float c) {
        return Math.max(Math.max(a, b), c);
    }

    public static boolean isDigits(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumber(String str) {
        int start;
        boolean z = true;
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        char[] chars = str.toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        if (chars[0] == '-') {
            start = 1;
        } else {
            start = 0;
        }
        if (sz > start + 1 && chars[start] == '0') {
            if (chars[start + 1] == 'x' || chars[start + 1] == 'X') {
                int i = start + 2;
                if (i == sz) {
                    return false;
                }
                while (i < chars.length) {
                    if ((chars[i] < '0' || chars[i] > '9') && ((chars[i] < 'a' || chars[i] > 'f') && (chars[i] < 'A' || chars[i] > 'F'))) {
                        return false;
                    }
                    i++;
                }
                return true;
            } else if (Character.isDigit(chars[start + 1])) {
                for (int i2 = start + 1; i2 < chars.length; i2++) {
                    if (chars[i2] < '0' || chars[i2] > '7') {
                        return false;
                    }
                }
                return true;
            }
        }
        int sz2 = sz - 1;
        int i3 = start;
        while (true) {
            if (i3 < sz2 || (i3 < sz2 + 1 && allowSigns && !foundDigit)) {
                if (chars[i3] >= '0' && chars[i3] <= '9') {
                    foundDigit = true;
                    allowSigns = false;
                } else if (chars[i3] == '.') {
                    if (hasDecPoint || hasExp) {
                        return false;
                    }
                    hasDecPoint = true;
                } else if (chars[i3] == 'e' || chars[i3] == 'E') {
                    if (hasExp || !foundDigit) {
                        return false;
                    }
                    hasExp = true;
                    allowSigns = true;
                } else if ((chars[i3] != '+' && chars[i3] != '-') || !allowSigns) {
                    return false;
                } else {
                    allowSigns = false;
                    foundDigit = false;
                }
                i3++;
            }
        }
        if (i3 >= chars.length) {
            if (allowSigns || !foundDigit) {
                z = false;
            }
            return z;
        } else if (chars[i3] >= '0' && chars[i3] <= '9') {
            return true;
        } else {
            if (chars[i3] == 'e' || chars[i3] == 'E') {
                return false;
            }
            if (chars[i3] == '.') {
                if (hasDecPoint || hasExp) {
                    return false;
                }
                return foundDigit;
            } else if (!allowSigns && (chars[i3] == 'd' || chars[i3] == 'D' || chars[i3] == 'f' || chars[i3] == 'F')) {
                return foundDigit;
            } else {
                if (chars[i3] != 'l' && chars[i3] != 'L') {
                    return false;
                }
                if (!foundDigit || hasExp || hasDecPoint) {
                    z = false;
                }
                return z;
            }
        }
    }

    public static boolean isParsable(String str) {
        if (StringUtils.endsWith(str, ".")) {
            return false;
        }
        if (StringUtils.startsWith(str, "-")) {
            return isDigits(StringUtils.replaceOnce(str.substring(1), ".", ""));
        }
        return isDigits(StringUtils.replaceOnce(str, ".", ""));
    }

    public static int compare(int x, int y) {
        if (x == y) {
            return 0;
        }
        if (x < y) {
            return -1;
        }
        return 1;
    }

    public static int compare(long x, long y) {
        if (x == y) {
            return 0;
        }
        if (x < y) {
            return -1;
        }
        return 1;
    }

    public static int compare(short x, short y) {
        if (x == y) {
            return 0;
        }
        if (x < y) {
            return -1;
        }
        return 1;
    }

    public static int compare(byte x, byte y) {
        return x - y;
    }
}
