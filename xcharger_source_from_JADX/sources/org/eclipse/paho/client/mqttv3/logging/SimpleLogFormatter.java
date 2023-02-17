package org.eclipse.paho.client.mqttv3.logging;

import java.util.logging.Formatter;

public class SimpleLogFormatter extends Formatter {

    /* renamed from: LS */
    private static final String f203LS = System.getProperty("line.separator");

    /* JADX WARNING: Removed duplicated region for block: B:20:0x00f1 A[SYNTHETIC, Splitter:B:20:0x00f1] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String format(java.util.logging.LogRecord r23) {
        /*
            r22 = this;
            java.lang.StringBuffer r9 = new java.lang.StringBuffer
            r9.<init>()
            java.util.logging.Level r14 = r23.getLevel()
            java.lang.String r14 = r14.getName()
            java.lang.StringBuffer r14 = r9.append(r14)
            java.lang.String r15 = "\t"
            r14.append(r15)
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            java.lang.String r15 = "{0, date, yy-MM-dd} {0, time, kk:mm:ss.SSSS} "
            r16 = 1
            r0 = r16
            java.lang.Object[] r0 = new java.lang.Object[r0]
            r16 = r0
            r17 = 0
            java.util.Date r18 = new java.util.Date
            long r20 = r23.getMillis()
            r0 = r18
            r1 = r20
            r0.<init>(r1)
            r16[r17] = r18
            java.lang.String r15 = java.text.MessageFormat.format(r15, r16)
            java.lang.String r15 = java.lang.String.valueOf(r15)
            r14.<init>(r15)
            java.lang.String r15 = "\t"
            java.lang.StringBuilder r14 = r14.append(r15)
            java.lang.String r14 = r14.toString()
            r9.append(r14)
            java.lang.String r6 = r23.getSourceClassName()
            java.lang.String r4 = ""
            if (r6 == 0) goto L_0x0065
            int r5 = r6.length()
            r14 = 20
            if (r5 <= r14) goto L_0x00d1
            java.lang.String r14 = r23.getSourceClassName()
            int r15 = r5 + -19
            java.lang.String r4 = r14.substring(r15)
        L_0x0065:
            java.lang.StringBuffer r14 = r9.append(r4)
            java.lang.String r15 = "\t"
            java.lang.StringBuffer r14 = r14.append(r15)
            java.lang.String r15 = " "
            r14.append(r15)
            java.lang.String r14 = r23.getSourceMethodName()
            r15 = 23
            r16 = 32
            java.lang.String r14 = left(r14, r15, r16)
            java.lang.StringBuffer r14 = r9.append(r14)
            java.lang.String r15 = "\t"
            r14.append(r15)
            int r14 = r23.getThreadID()
            java.lang.StringBuffer r14 = r9.append(r14)
            java.lang.String r15 = "\t"
            r14.append(r15)
            java.lang.String r14 = r22.formatMessage(r23)
            java.lang.StringBuffer r14 = r9.append(r14)
            java.lang.String r15 = f203LS
            r14.append(r15)
            java.lang.Throwable r14 = r23.getThrown()
            if (r14 == 0) goto L_0x00cc
            java.lang.String r14 = "Throwable occurred: "
            r9.append(r14)
            java.lang.Throwable r13 = r23.getThrown()
            r7 = 0
            java.io.StringWriter r12 = new java.io.StringWriter     // Catch:{ all -> 0x00ee }
            r12.<init>()     // Catch:{ all -> 0x00ee }
            java.io.PrintWriter r8 = new java.io.PrintWriter     // Catch:{ all -> 0x00ee }
            r8.<init>(r12)     // Catch:{ all -> 0x00ee }
            r13.printStackTrace(r8)     // Catch:{ all -> 0x00f9 }
            java.lang.String r14 = r12.toString()     // Catch:{ all -> 0x00f9 }
            r9.append(r14)     // Catch:{ all -> 0x00f9 }
            if (r8 == 0) goto L_0x00cc
            r8.close()     // Catch:{ Exception -> 0x00f7 }
        L_0x00cc:
            java.lang.String r14 = r9.toString()
            return r14
        L_0x00d1:
            r14 = 1
            char[] r11 = new char[r14]
            r14 = 0
            r15 = 32
            r11[r14] = r15
            java.lang.StringBuffer r14 = new java.lang.StringBuffer
            r14.<init>()
            java.lang.StringBuffer r10 = r14.append(r6)
            r14 = 0
            r15 = 1
            java.lang.StringBuffer r14 = r10.append(r11, r14, r15)
            java.lang.String r4 = r14.toString()
            goto L_0x0065
        L_0x00ee:
            r14 = move-exception
        L_0x00ef:
            if (r7 == 0) goto L_0x00f4
            r7.close()     // Catch:{ Exception -> 0x00f5 }
        L_0x00f4:
            throw r14
        L_0x00f5:
            r15 = move-exception
            goto L_0x00f4
        L_0x00f7:
            r14 = move-exception
            goto L_0x00cc
        L_0x00f9:
            r14 = move-exception
            r7 = r8
            goto L_0x00ef
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.paho.client.mqttv3.logging.SimpleLogFormatter.format(java.util.logging.LogRecord):java.lang.String");
    }

    public static String left(String s, int width, char fillChar) {
        if (s.length() >= width) {
            return s;
        }
        StringBuffer sb = new StringBuffer(width);
        sb.append(s);
        int i = width - s.length();
        while (true) {
            i--;
            if (i < 0) {
                return sb.toString();
            }
            sb.append(fillChar);
        }
    }
}
