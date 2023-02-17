package net.xcharge.sdk.server.coder.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesUtil {
    private static final Logger logger = LoggerFactory.getLogger((Class<?>) PropertiesUtil.class);

    /* JADX WARNING: Removed duplicated region for block: B:33:0x00b5 A[SYNTHETIC, Splitter:B:33:0x00b5] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String loadFileInJar(java.lang.Class r9, java.lang.String r10) {
        /*
            r2 = 0
            java.lang.Object r6 = r9.newInstance()     // Catch:{ Exception -> 0x00c5 }
            java.lang.Class r6 = r6.getClass()     // Catch:{ Exception -> 0x00c5 }
            java.security.ProtectionDomain r6 = r6.getProtectionDomain()     // Catch:{ Exception -> 0x00c5 }
            java.security.CodeSource r6 = r6.getCodeSource()     // Catch:{ Exception -> 0x00c5 }
            java.net.URL r6 = r6.getLocation()     // Catch:{ Exception -> 0x00c5 }
            java.lang.String r5 = r6.toString()     // Catch:{ Exception -> 0x00c5 }
            java.lang.String r6 = "jar:"
            boolean r6 = r5.startsWith(r6)     // Catch:{ Exception -> 0x00c5 }
            if (r6 != 0) goto L_0x0034
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00c5 }
            r6.<init>()     // Catch:{ Exception -> 0x00c5 }
            java.lang.String r7 = "jar:"
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ Exception -> 0x00c5 }
            java.lang.StringBuilder r6 = r6.append(r5)     // Catch:{ Exception -> 0x00c5 }
            java.lang.String r5 = r6.toString()     // Catch:{ Exception -> 0x00c5 }
        L_0x0034:
            java.lang.String r6 = "!"
            boolean r6 = r5.contains(r6)     // Catch:{ Exception -> 0x00c5 }
            if (r6 == 0) goto L_0x0047
            r6 = 0
            java.lang.String r7 = "!"
            int r7 = r5.indexOf(r7)     // Catch:{ Exception -> 0x00c5 }
            java.lang.String r5 = r5.substring(r6, r7)     // Catch:{ Exception -> 0x00c5 }
        L_0x0047:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00c5 }
            r6.<init>()     // Catch:{ Exception -> 0x00c5 }
            java.lang.StringBuilder r6 = r6.append(r5)     // Catch:{ Exception -> 0x00c5 }
            java.lang.String r7 = "!"
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ Exception -> 0x00c5 }
            java.lang.StringBuilder r6 = r6.append(r10)     // Catch:{ Exception -> 0x00c5 }
            java.lang.String r5 = r6.toString()     // Catch:{ Exception -> 0x00c5 }
            java.io.PrintStream r6 = java.lang.System.out     // Catch:{ Exception -> 0x00c5 }
            r6.println(r5)     // Catch:{ Exception -> 0x00c5 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00c5 }
            r4.<init>()     // Catch:{ Exception -> 0x00c5 }
            java.io.BufferedReader r3 = new java.io.BufferedReader     // Catch:{ Exception -> 0x00c5 }
            java.io.InputStreamReader r6 = new java.io.InputStreamReader     // Catch:{ Exception -> 0x00c5 }
            java.net.URL r7 = new java.net.URL     // Catch:{ Exception -> 0x00c5 }
            r7.<init>(r5)     // Catch:{ Exception -> 0x00c5 }
            java.io.InputStream r7 = r7.openStream()     // Catch:{ Exception -> 0x00c5 }
            r6.<init>(r7)     // Catch:{ Exception -> 0x00c5 }
            r3.<init>(r6)     // Catch:{ Exception -> 0x00c5 }
        L_0x007b:
            java.lang.String r1 = r3.readLine()     // Catch:{ Exception -> 0x0085, all -> 0x00c2 }
            if (r1 == 0) goto L_0x0095
            r4.append(r1)     // Catch:{ Exception -> 0x0085, all -> 0x00c2 }
            goto L_0x007b
        L_0x0085:
            r0 = move-exception
            r2 = r3
        L_0x0087:
            org.slf4j.Logger r6 = logger     // Catch:{ all -> 0x00b2 }
            java.lang.String r7 = ""
            r6.error((java.lang.String) r7, (java.lang.Throwable) r0)     // Catch:{ all -> 0x00b2 }
            if (r2 == 0) goto L_0x0093
            r2.close()     // Catch:{ IOException -> 0x00a9 }
        L_0x0093:
            r6 = 0
        L_0x0094:
            return r6
        L_0x0095:
            java.lang.String r6 = r4.toString()     // Catch:{ Exception -> 0x0085, all -> 0x00c2 }
            if (r3 == 0) goto L_0x009e
            r3.close()     // Catch:{ IOException -> 0x00a0 }
        L_0x009e:
            r2 = r3
            goto L_0x0094
        L_0x00a0:
            r0 = move-exception
            org.slf4j.Logger r7 = logger
            java.lang.String r8 = ""
            r7.info((java.lang.String) r8, (java.lang.Throwable) r0)
            goto L_0x009e
        L_0x00a9:
            r0 = move-exception
            org.slf4j.Logger r6 = logger
            java.lang.String r7 = ""
            r6.info((java.lang.String) r7, (java.lang.Throwable) r0)
            goto L_0x0093
        L_0x00b2:
            r6 = move-exception
        L_0x00b3:
            if (r2 == 0) goto L_0x00b8
            r2.close()     // Catch:{ IOException -> 0x00b9 }
        L_0x00b8:
            throw r6
        L_0x00b9:
            r0 = move-exception
            org.slf4j.Logger r7 = logger
            java.lang.String r8 = ""
            r7.info((java.lang.String) r8, (java.lang.Throwable) r0)
            goto L_0x00b8
        L_0x00c2:
            r6 = move-exception
            r2 = r3
            goto L_0x00b3
        L_0x00c5:
            r0 = move-exception
            goto L_0x0087
        */
        throw new UnsupportedOperationException("Method not decompiled: net.xcharge.sdk.server.coder.utils.PropertiesUtil.loadFileInJar(java.lang.Class, java.lang.String):java.lang.String");
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x004e A[SYNTHETIC, Splitter:B:27:0x004e] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String loadFile(java.lang.Class r8, java.lang.String r9) {
        /*
            r2 = 0
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x005e }
            r4.<init>()     // Catch:{ Exception -> 0x005e }
            java.io.BufferedReader r3 = new java.io.BufferedReader     // Catch:{ Exception -> 0x005e }
            java.io.InputStreamReader r5 = new java.io.InputStreamReader     // Catch:{ Exception -> 0x005e }
            java.io.InputStream r6 = r8.getResourceAsStream(r9)     // Catch:{ Exception -> 0x005e }
            r5.<init>(r6)     // Catch:{ Exception -> 0x005e }
            r3.<init>(r5)     // Catch:{ Exception -> 0x005e }
        L_0x0014:
            java.lang.String r1 = r3.readLine()     // Catch:{ Exception -> 0x001e, all -> 0x005b }
            if (r1 == 0) goto L_0x002e
            r4.append(r1)     // Catch:{ Exception -> 0x001e, all -> 0x005b }
            goto L_0x0014
        L_0x001e:
            r0 = move-exception
            r2 = r3
        L_0x0020:
            org.slf4j.Logger r5 = logger     // Catch:{ all -> 0x004b }
            java.lang.String r6 = ""
            r5.error((java.lang.String) r6, (java.lang.Throwable) r0)     // Catch:{ all -> 0x004b }
            if (r2 == 0) goto L_0x002c
            r2.close()     // Catch:{ IOException -> 0x0042 }
        L_0x002c:
            r5 = 0
        L_0x002d:
            return r5
        L_0x002e:
            java.lang.String r5 = r4.toString()     // Catch:{ Exception -> 0x001e, all -> 0x005b }
            if (r3 == 0) goto L_0x0037
            r3.close()     // Catch:{ IOException -> 0x0039 }
        L_0x0037:
            r2 = r3
            goto L_0x002d
        L_0x0039:
            r0 = move-exception
            org.slf4j.Logger r6 = logger
            java.lang.String r7 = ""
            r6.error((java.lang.String) r7, (java.lang.Throwable) r0)
            goto L_0x0037
        L_0x0042:
            r0 = move-exception
            org.slf4j.Logger r5 = logger
            java.lang.String r6 = ""
            r5.error((java.lang.String) r6, (java.lang.Throwable) r0)
            goto L_0x002c
        L_0x004b:
            r5 = move-exception
        L_0x004c:
            if (r2 == 0) goto L_0x0051
            r2.close()     // Catch:{ IOException -> 0x0052 }
        L_0x0051:
            throw r5
        L_0x0052:
            r0 = move-exception
            org.slf4j.Logger r6 = logger
            java.lang.String r7 = ""
            r6.error((java.lang.String) r7, (java.lang.Throwable) r0)
            goto L_0x0051
        L_0x005b:
            r5 = move-exception
            r2 = r3
            goto L_0x004c
        L_0x005e:
            r0 = move-exception
            goto L_0x0020
        */
        throw new UnsupportedOperationException("Method not decompiled: net.xcharge.sdk.server.coder.utils.PropertiesUtil.loadFile(java.lang.Class, java.lang.String):java.lang.String");
    }
}
