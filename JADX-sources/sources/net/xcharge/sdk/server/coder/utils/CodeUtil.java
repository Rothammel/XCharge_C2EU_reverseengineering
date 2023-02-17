package net.xcharge.sdk.server.coder.utils;

import android.support.v4.view.MotionEventCompat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.xcharge.sdk.server.coder.model.rule.subitem.ValueMap;

/* loaded from: classes.dex */
public class CodeUtil {
    public static int hexString2int(String b) {
        return Integer.decode("0x" + b).intValue();
    }

    public static char[] int2BinaryChars(int num, int length) {
        String str = Integer.toBinaryString(num);
        if (str.length() > length) {
            return str.substring(str.length() - length, str.length()).toCharArray();
        }
        if (str.length() < length) {
            String newStr = "";
            for (int i = 0; i < length - str.length(); i++) {
                newStr = newStr + "0";
            }
            return (newStr + str).toCharArray();
        }
        return str.toCharArray();
    }

    public static String int2BinaryString(int num, int length) {
        char[] chars = int2BinaryChars(num, length);
        return new String(chars);
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
        int number = (int) Math.floor(inputString.length() / length);
        for (int index = 0; index < number; index++) {
            String childStr = inputString.substring(index * length, (index + 1) * length);
            divList.add(childStr);
        }
        if (remainder > 0) {
            String cStr = inputString.substring(number * length, inputString.length());
            divList.add(cStr);
        }
        return divList;
    }

    public static String int2BinaryString(int num) {
        String str = Integer.toBinaryString(num);
        return str;
    }

    public static String byte2hexString(byte b) {
        return int2HexStr(b, 2);
    }

    public static byte[] int2Bytes(int value) {
        byte[] src = {(byte) ((value >> 24) & MotionEventCompat.ACTION_MASK), (byte) ((value >> 16) & MotionEventCompat.ACTION_MASK), (byte) ((value >> 8) & MotionEventCompat.ACTION_MASK), (byte) (value & MotionEventCompat.ACTION_MASK)};
        return src;
    }

    public static byte[] short2Bytes(short value) {
        byte[] src = {(byte) ((value >> 8) & MotionEventCompat.ACTION_MASK), (byte) (value & 255)};
        return src;
    }

    public static int bytes2Int(byte... bytes) {
        String s = bytes2binaryString(bytes);
        return binaryString2Integer(s).intValue();
    }

    public static String int2HexStr(int num, int length) {
        return String.format("%0" + length + "X", Integer.valueOf(num)).toUpperCase();
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
        HashMap<String, Integer> map = valueMap.get(mapName);
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue().intValue() == value) {
                return entry.getKey();
            }
        }
        return String.valueOf(value);
    }

    public static Integer getValueFromValueMap(ValueMap valueMap, String mapName, String key) {
        HashMap<String, Integer> map = valueMap.get(mapName);
        Integer value = map.get(key);
        if (value == null) {
            value = 1;
        }
        return Integer.valueOf(value.intValue());
    }
}
