package org.apache.commons.lang3;

import android.support.p000v4.media.TransportMediator;
import java.util.Random;

public class RandomStringUtils {
    private static final Random RANDOM = new Random();

    public static String random(int count) {
        return random(count, false, false);
    }

    public static String randomAscii(int count) {
        return random(count, 32, TransportMediator.KEYCODE_MEDIA_PAUSE, false, false);
    }

    public static String randomAlphabetic(int count) {
        return random(count, true, false);
    }

    public static String randomAlphanumeric(int count) {
        return random(count, true, true);
    }

    public static String randomNumeric(int count) {
        return random(count, false, true);
    }

    public static String random(int count, boolean letters, boolean numbers) {
        return random(count, 0, 0, letters, numbers);
    }

    public static String random(int count, int start, int end, boolean letters, boolean numbers) {
        return random(count, start, end, letters, numbers, (char[]) null, RANDOM);
    }

    public static String random(int count, int start, int end, boolean letters, boolean numbers, char... chars) {
        return random(count, start, end, letters, numbers, chars, RANDOM);
    }

    public static String random(int count, int start, int end, boolean letters, boolean numbers, char[] chars, Random random) {
        char ch;
        if (count == 0) {
            return "";
        }
        if (count < 0) {
            throw new IllegalArgumentException("Requested random string length " + count + " is less than 0.");
        } else if (chars == null || chars.length != 0) {
            if (start == 0 && end == 0) {
                if (chars != null) {
                    end = chars.length;
                } else if (letters || numbers) {
                    end = 123;
                    start = 32;
                } else {
                    end = Integer.MAX_VALUE;
                }
            } else if (end <= start) {
                throw new IllegalArgumentException("Parameter end (" + end + ") must be greater than start (" + start + ")");
            }
            char[] buffer = new char[count];
            int gap = end - start;
            while (true) {
                int count2 = count;
                count = count2 - 1;
                if (count2 == 0) {
                    return new String(buffer);
                }
                if (chars == null) {
                    ch = (char) (random.nextInt(gap) + start);
                } else {
                    ch = chars[random.nextInt(gap) + start];
                }
                if ((!letters || !Character.isLetter(ch)) && ((!numbers || !Character.isDigit(ch)) && (letters || numbers))) {
                    count++;
                } else if (ch < 56320 || ch > 57343) {
                    if (ch < 55296 || ch > 56191) {
                        if (ch < 56192 || ch > 56319) {
                            buffer[count] = ch;
                        } else {
                            count++;
                        }
                    } else if (count == 0) {
                        count++;
                    } else {
                        buffer[count] = (char) (random.nextInt(128) + 56320);
                        count--;
                        buffer[count] = ch;
                    }
                } else if (count == 0) {
                    count++;
                } else {
                    buffer[count] = ch;
                    count--;
                    buffer[count] = (char) (random.nextInt(128) + 55296);
                }
            }
        } else {
            throw new IllegalArgumentException("The chars array must not be empty");
        }
    }

    public static String random(int count, String chars) {
        if (chars != null) {
            return random(count, chars.toCharArray());
        }
        return random(count, 0, 0, false, false, (char[]) null, RANDOM);
    }

    public static String random(int count, char... chars) {
        if (chars == null) {
            return random(count, 0, 0, false, false, (char[]) null, RANDOM);
        }
        return random(count, 0, chars.length, false, false, chars, RANDOM);
    }
}
