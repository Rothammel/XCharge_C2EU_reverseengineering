package com.xcharge.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.CharEncoding;

/* loaded from: classes.dex */
public class Strings {
    private static final Pattern XML_INVALID_CHARS = Pattern.compile("[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD]+");

    public static String readStream(Reader reader) throws IOException {
        StringBuilder result = new StringBuilder();
        BufferedReader in = new BufferedReader(reader);
        while (true) {
            String line = in.readLine();
            if (line != null) {
                result.append(line);
                result.append('\n');
            } else {
                in.close();
                return result.toString();
            }
        }
    }

    public static String readFile(File f) throws IOException {
        return readStream(new InputStreamReader(new FileInputStream(f), CharEncoding.UTF_8));
    }

    public static List<String> readFileLines(File f) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), CharEncoding.UTF_8));
        List<String> list = new ArrayList<>();
        while (true) {
            String line = in.readLine();
            if (line != null) {
                list.add(line);
            } else {
                in.close();
                return list;
            }
        }
    }

    public static String join(String delimiter, Object... objects) {
        return join(Arrays.asList(objects), delimiter);
    }

    public static String join(Iterable<?> objects, String delimiter) {
        Iterator<?> i = objects.iterator();
        if (!i.hasNext()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append(i.next());
        while (i.hasNext()) {
            result.append(delimiter).append(i.next());
        }
        return result.toString();
    }

    public static String[] objectsToStrings(Object[] objects) {
        String[] result = new String[objects.length];
        int length = objects.length;
        int i = 0;
        int i2 = 0;
        while (i < length) {
            Object o = objects[i];
            result[i2] = o.toString();
            i++;
            i2++;
        }
        return result;
    }

    public static String[] objectsToStrings(Collection<?> objects) {
        return objectsToStrings(objects.toArray());
    }

    public static String xmlSanitize(String text) {
        StringBuffer result = new StringBuffer();
        Matcher matcher = XML_INVALID_CHARS.matcher(text);
        while (matcher.find()) {
            matcher.appendReplacement(result, "");
            result.append(escapeCodePoint(matcher.group()));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String escapeCodePoint(CharSequence cs) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < cs.length(); i++) {
            result.append(String.format("U+%04X", Integer.valueOf(cs.charAt(i))));
        }
        return result.toString();
    }
}