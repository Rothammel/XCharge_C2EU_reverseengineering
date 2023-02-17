package net.xcharge.sdk.server.coder.utils;

import android.support.p000v4.view.MotionEventCompat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.xcharge.sdk.server.coder.model.rule.subitem.ValueMap;

public class CodeUtil {
    public static int hexString2int(String b) {
        return Integer.decode("0x" + b).intValue();
    }

    public static char[] int2BinaryChars(int num, int length) {
        String str = Integer.toBinaryString(num);
        if (str.length() > length) {
            return str.substring(str.length() - length, str.length()).toCharArray();
        }
        if (str.length() >= length) {
            return str.toCharArray();
        }
        String newStr = "";
        for (int i = 0; i < length - str.length(); i++) {
            newStr = newStr + "0";
        }
        return (newStr + str).toCharArray();
    }

    public static String int2BinaryString(int num, int length) {
        return new String(int2BinaryChars(num, length));
    }

    public static Integer binaryString2Integer(String binaryString) {
        return Integer.valueOf(Integer.parseInt(binaryString, 2));
    }

    public static byte binaryString2byte(String binaryString) {
        return (byte) Integer.parseInt(binaryString, 2);
    }

    public static List<String> splitByLength(String inputString, int length) {
        List<String> divList = new ArrayList<>();
        int remainder = inputString.length() % length;
        int number = (int) Math.floor((double) (inputString.length() / length));
        for (int index = 0; index < number; index++) {
            divList.add(inputString.substring(index * length, (index + 1) * length));
        }
        if (remainder > 0) {
            divList.add(inputString.substring(number * length, inputString.length()));
        }
        return divList;
    }

    public static String int2BinaryString(int num) {
        return Integer.toBinaryString(num);
    }

    public static String byte2hexString(byte b) {
        return int2HexStr(b, 2);
    }

    public static byte[] int2Bytes(int value) {
        return new byte[]{(byte) ((value >> 24) & MotionEventCompat.ACTION_MASK), (byte) ((value >> 16) & MotionEventCompat.ACTION_MASK), (byte) ((value >> 8) & MotionEventCompat.ACTION_MASK), (byte) (value & MotionEventCompat.ACTION_MASK)};
    }

    public static byte[] short2Bytes(short value) {
        return new byte[]{(byte) ((value >> 8) & MotionEventCompat.ACTION_MASK), (byte) (value & 255)};
    }

    public static int bytes2Int(byte... bytes) {
        return binaryString2Integer(bytes2binaryString(bytes)).intValue();
    }

    public static String int2HexStr(int num, int length) {
        return String.format("%0" + length + "X", new Object[]{Integer.valueOf(num)}).toUpperCase();
    }

    public static String bytes2pString(byte... bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hexString = int2HexStr(b, 2);
            StringBuilder append = sb.append(int2BinaryString(b, 8)).append("(");
            if (hexString.length() > 2) {
                hexString = hexString.substring(hexString.length() - 2);
            }
            append.append(hexString).append(")");
        }
        return sb.toString();
    }

    public static String bytes2binaryString(byte... bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(int2BinaryString(b, 8));
        }
        return sb.toString();
    }

    public static String getKeyFromValueMap(ValueMap valueMap, String mapName, int value) {
        for (Map.Entry<String, Integer> entry : ((HashMap) valueMap.get(mapName)).entrySet()) {
            if (entry.getValue().intValue() == value) {
                return entry.getKey();
            }
        }
        return String.valueOf(value);
    }

    public static Integer getValueFromValueMap(ValueMap valueMap, String mapName, String key) {
        Integer value = ((HashMap) valueMap.get(mapName)).get(key);
        if (value == null) {
            value = 1;
        }
        return Integer.valueOf(value.intValue());
    }
}
