package net.xcharger.util;

import android.support.p000v4.view.MotionEventCompat;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Random;
import org.apache.commons.lang3.CharEncoding;

public class CodecUtil {
    /* access modifiers changed from: private */
    public static final Charset CHARSET = Charset.forName(CharEncoding.UTF_8);

    public static class Base32 {
        private static final char[] ALPHABET = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7'};
        private static final byte[] DECODE_TABLE = new byte[128];

        static {
            for (int i = 0; i < DECODE_TABLE.length; i++) {
                DECODE_TABLE[i] = -1;
            }
            for (int i2 = 0; i2 < ALPHABET.length; i2++) {
                DECODE_TABLE[ALPHABET[i2]] = (byte) i2;
                if (i2 < 24) {
                    DECODE_TABLE[Character.toLowerCase(ALPHABET[i2])] = (byte) i2;
                }
            }
        }

        public static String encode(String str) {
            byte[] data = str.getBytes(CodecUtil.CHARSET);
            char[] chars = new char[((data.length % 5 != 0 ? 1 : 0) + ((data.length * 8) / 5))];
            int j = 0;
            int index = 0;
            for (int i = 0; i < chars.length; i++) {
                if (index > 3) {
                    int b = data[j] & (MotionEventCompat.ACTION_MASK >> index);
                    index = (index + 5) % 8;
                    int b2 = b << index;
                    if (j < data.length - 1) {
                        b2 |= (data[j + 1] & 255) >> (8 - index);
                    }
                    chars[i] = ALPHABET[b2];
                    j++;
                } else {
                    chars[i] = ALPHABET[(data[j] >> (8 - (index + 5))) & 31];
                    index = (index + 5) % 8;
                    if (index == 0) {
                        j++;
                    }
                }
            }
            return new String(chars);
        }

        public static String decode(String s) {
            char[] stringData = s.toCharArray();
            byte[] data = new byte[((stringData.length * 5) / 8)];
            int i = 0;
            int j = 0;
            int index = 0;
            while (i < stringData.length) {
                try {
                    byte val = DECODE_TABLE[stringData[i]];
                    if (val == 255) {
                        return null;
                    }
                    if (index <= 3) {
                        index = (index + 5) % 8;
                        if (index == 0) {
                            data[j] = (byte) (data[j] | val);
                            j++;
                        } else {
                            data[j] = (byte) (data[j] | (val << (8 - index)));
                        }
                    } else {
                        index = (index + 5) % 8;
                        int j2 = j + 1;
                        data[j] = (byte) (data[j] | (val >> index));
                        if (j2 < data.length) {
                            data[j2] = (byte) (data[j2] | (val << (8 - index)));
                        }
                        j = j2;
                    }
                    i++;
                } catch (ArrayIndexOutOfBoundsException e) {
                    return null;
                }
            }
            return new String(data, CodecUtil.CHARSET);
        }
    }

    public static class Md5 {
        public static String encode(String inStr) {
            try {
                byte[] md5Bytes = MessageDigest.getInstance("MD5").digest(inStr.getBytes(CodecUtil.CHARSET));
                StringBuffer hexValue = new StringBuffer();
                for (byte b : md5Bytes) {
                    int val = b & MotionEventCompat.ACTION_MASK;
                    if (val < 16) {
                        hexValue.append("0");
                    }
                    hexValue.append(Integer.toHexString(val));
                }
                return hexValue.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static String randomInt(int length) {
        return random("0123456789", length);
    }

    public static String randomString(int length) {
        return random("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", length);
    }

    public static String random(String base, int length) {
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            sb.append(base.charAt(random.nextInt(base.length())));
        }
        return sb.toString();
    }
}
