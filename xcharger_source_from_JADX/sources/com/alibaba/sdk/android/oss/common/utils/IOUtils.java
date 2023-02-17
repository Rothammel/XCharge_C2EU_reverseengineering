package com.alibaba.sdk.android.oss.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {
    private static final int BUFFER_SIZE = 4096;

    /* JADX WARNING: Removed duplicated region for block: B:13:0x002b  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0030  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String readStreamAsString(java.io.InputStream r7, java.lang.String r8) throws java.io.IOException {
        /*
            if (r7 != 0) goto L_0x0005
            java.lang.String r4 = ""
        L_0x0004:
            return r4
        L_0x0005:
            r2 = 0
            java.io.StringWriter r5 = new java.io.StringWriter
            r5.<init>()
            r6 = 4096(0x1000, float:5.74E-42)
            char[] r0 = new char[r6]
            java.io.BufferedReader r3 = new java.io.BufferedReader     // Catch:{ all -> 0x0046 }
            java.io.InputStreamReader r6 = new java.io.InputStreamReader     // Catch:{ all -> 0x0046 }
            r6.<init>(r7, r8)     // Catch:{ all -> 0x0046 }
            r3.<init>(r6)     // Catch:{ all -> 0x0046 }
        L_0x0019:
            int r1 = r3.read(r0)     // Catch:{ all -> 0x0024 }
            if (r1 <= 0) goto L_0x0034
            r6 = 0
            r5.write(r0, r6, r1)     // Catch:{ all -> 0x0024 }
            goto L_0x0019
        L_0x0024:
            r6 = move-exception
            r2 = r3
        L_0x0026:
            safeClose((java.io.InputStream) r7)
            if (r2 == 0) goto L_0x002e
            r2.close()
        L_0x002e:
            if (r5 == 0) goto L_0x0033
            r5.close()
        L_0x0033:
            throw r6
        L_0x0034:
            java.lang.String r4 = r5.toString()     // Catch:{ all -> 0x0024 }
            safeClose((java.io.InputStream) r7)
            if (r3 == 0) goto L_0x0040
            r3.close()
        L_0x0040:
            if (r5 == 0) goto L_0x0004
            r5.close()
            goto L_0x0004
        L_0x0046:
            r6 = move-exception
            goto L_0x0026
        */
        throw new UnsupportedOperationException("Method not decompiled: com.alibaba.sdk.android.oss.common.utils.IOUtils.readStreamAsString(java.io.InputStream, java.lang.String):java.lang.String");
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
                safeClose((OutputStream) output);
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
        while (readed < ((long) readLength) && (len = in.read(buffer, 0, Math.min(2048, (int) (((long) readLength) - readed)))) > -1) {
            output.write(buffer, 0, len);
            readed += (long) len;
        }
        output.flush();
        safeClose((OutputStream) output);
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
