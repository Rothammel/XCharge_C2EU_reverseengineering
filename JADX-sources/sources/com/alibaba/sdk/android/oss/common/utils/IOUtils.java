package com.alibaba.sdk.android.oss.common.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/* loaded from: classes.dex */
public class IOUtils {
    private static final int BUFFER_SIZE = 4096;

    public static String readStreamAsString(InputStream in, String charset) throws IOException {
        if (in == null) {
            return "";
        }
        Reader reader = null;
        Writer writer = new StringWriter();
        char[] buffer = new char[4096];
        try {
            Reader reader2 = new BufferedReader(new InputStreamReader(in, charset));
            while (true) {
                try {
                    int n = reader2.read(buffer);
                    if (n <= 0) {
                        break;
                    }
                    writer.write(buffer, 0, n);
                } catch (Throwable th) {
                    th = th;
                    reader = reader2;
                    safeClose(in);
                    if (reader != null) {
                        reader.close();
                    }
                    if (writer != null) {
                        writer.close();
                    }
                    throw th;
                }
            }
            String obj = writer.toString();
            safeClose(in);
            if (reader2 != null) {
                reader2.close();
            }
            if (writer != null) {
                writer.close();
                return obj;
            }
            return obj;
        } catch (Throwable th2) {
            th = th2;
        }
    }

    public static byte[] readStreamAsBytesArray(InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        while (true) {
            int len = in.read(buffer);
            if (len > -1) {
                output.write(buffer, 0, len);
            } else {
                output.flush();
                safeClose(output);
                return output.toByteArray();
            }
        }
    }

    public static byte[] readStreamAsBytesArray(InputStream in, int readLength) throws IOException {
        int len;
        if (in == null) {
            return new byte[0];
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        long readed = 0;
        while (readed < readLength && (len = in.read(buffer, 0, Math.min(2048, (int) (readLength - readed)))) > -1) {
            output.write(buffer, 0, len);
            readed += len;
        }
        output.flush();
        safeClose(output);
        return output.toByteArray();
    }

    public static void safeClose(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }
    }

    public static void safeClose(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
            }
        }
    }
}
