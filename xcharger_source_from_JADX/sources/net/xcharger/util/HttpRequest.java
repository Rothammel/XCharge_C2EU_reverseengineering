package net.xcharger.util;

import java.util.logging.Logger;
import net.xcharger.mqtt.plugn.MqttPlugin;

public class HttpRequest {
    private static final Class<?> cclass = MqttPlugin.class;
    private static final String className = cclass.getName();
    private static Logger logger = Logger.getLogger(className);

    /* JADX WARNING: Removed duplicated region for block: B:32:0x00d7 A[SYNTHETIC, Splitter:B:32:0x00d7] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String sendGet(java.lang.String r14, java.lang.String r15) {
        /*
            java.lang.String r8 = ""
            r3 = 0
            r9 = 0
            if (r15 == 0) goto L_0x0094
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00af }
            java.lang.String r11 = java.lang.String.valueOf(r14)     // Catch:{ Exception -> 0x00af }
            r10.<init>(r11)     // Catch:{ Exception -> 0x00af }
            java.lang.String r11 = "?"
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ Exception -> 0x00af }
            java.lang.StringBuilder r10 = r10.append(r15)     // Catch:{ Exception -> 0x00af }
            java.lang.String r9 = r10.toString()     // Catch:{ Exception -> 0x00af }
        L_0x001d:
            java.net.URL r7 = new java.net.URL     // Catch:{ Exception -> 0x00af }
            r7.<init>(r9)     // Catch:{ Exception -> 0x00af }
            java.net.URLConnection r0 = r7.openConnection()     // Catch:{ Exception -> 0x00af }
            r10 = 10000(0x2710, float:1.4013E-41)
            r0.setConnectTimeout(r10)     // Catch:{ Exception -> 0x00af }
            r10 = 10000(0x2710, float:1.4013E-41)
            r0.setReadTimeout(r10)     // Catch:{ Exception -> 0x00af }
            r10 = 0
            r0.setUseCaches(r10)     // Catch:{ Exception -> 0x00af }
            java.lang.String r10 = "accept"
            java.lang.String r11 = "*/*"
            r0.setRequestProperty(r10, r11)     // Catch:{ Exception -> 0x00af }
            java.lang.String r10 = "connection"
            java.lang.String r11 = "Close"
            r0.setRequestProperty(r10, r11)     // Catch:{ Exception -> 0x00af }
            java.lang.String r10 = "user-agent"
            java.lang.String r11 = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)"
            r0.setRequestProperty(r10, r11)     // Catch:{ Exception -> 0x00af }
            java.lang.String r10 = "Content-Type"
            java.lang.String r11 = "application/json;charset=utf-8"
            r0.setRequestProperty(r10, r11)     // Catch:{ Exception -> 0x00af }
            r0.connect()     // Catch:{ Exception -> 0x00af }
            java.util.Map r6 = r0.getHeaderFields()     // Catch:{ Exception -> 0x00af }
            java.util.Set r10 = r6.keySet()     // Catch:{ Exception -> 0x00af }
            java.util.Iterator r11 = r10.iterator()     // Catch:{ Exception -> 0x00af }
        L_0x005f:
            boolean r10 = r11.hasNext()     // Catch:{ Exception -> 0x00af }
            if (r10 != 0) goto L_0x0096
            java.io.BufferedReader r4 = new java.io.BufferedReader     // Catch:{ Exception -> 0x00af }
            java.io.InputStreamReader r10 = new java.io.InputStreamReader     // Catch:{ Exception -> 0x00af }
            java.io.InputStream r11 = r0.getInputStream()     // Catch:{ Exception -> 0x00af }
            r10.<init>(r11)     // Catch:{ Exception -> 0x00af }
            r4.<init>(r10)     // Catch:{ Exception -> 0x00af }
        L_0x0073:
            java.lang.String r5 = r4.readLine()     // Catch:{ Exception -> 0x00e9, all -> 0x00e6 }
            if (r5 != 0) goto L_0x009d
            java.io.PrintStream r10 = java.lang.System.out     // Catch:{ Exception -> 0x00e9, all -> 0x00e6 }
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00e9, all -> 0x00e6 }
            java.lang.String r12 = "发送GET return :"
            r11.<init>(r12)     // Catch:{ Exception -> 0x00e9, all -> 0x00e6 }
            java.lang.StringBuilder r11 = r11.append(r5)     // Catch:{ Exception -> 0x00e9, all -> 0x00e6 }
            java.lang.String r11 = r11.toString()     // Catch:{ Exception -> 0x00e9, all -> 0x00e6 }
            r10.println(r11)     // Catch:{ Exception -> 0x00e9, all -> 0x00e6 }
            if (r4 == 0) goto L_0x00e4
            r4.close()     // Catch:{ Exception -> 0x00e0 }
            r3 = r4
        L_0x0093:
            return r8
        L_0x0094:
            r9 = r14
            goto L_0x001d
        L_0x0096:
            java.lang.Object r10 = r11.next()     // Catch:{ Exception -> 0x00af }
            java.lang.String r10 = (java.lang.String) r10     // Catch:{ Exception -> 0x00af }
            goto L_0x005f
        L_0x009d:
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00e9, all -> 0x00e6 }
            java.lang.String r11 = java.lang.String.valueOf(r8)     // Catch:{ Exception -> 0x00e9, all -> 0x00e6 }
            r10.<init>(r11)     // Catch:{ Exception -> 0x00e9, all -> 0x00e6 }
            java.lang.StringBuilder r10 = r10.append(r5)     // Catch:{ Exception -> 0x00e9, all -> 0x00e6 }
            java.lang.String r8 = r10.toString()     // Catch:{ Exception -> 0x00e9, all -> 0x00e6 }
            goto L_0x0073
        L_0x00af:
            r1 = move-exception
        L_0x00b0:
            java.util.logging.Logger r10 = logger     // Catch:{ all -> 0x00d4 }
            java.util.logging.Level r11 = java.util.logging.Level.WARNING     // Catch:{ all -> 0x00d4 }
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ all -> 0x00d4 }
            java.lang.String r13 = "发送GET请求出现异常！"
            r12.<init>(r13)     // Catch:{ all -> 0x00d4 }
            java.lang.StringBuilder r12 = r12.append(r1)     // Catch:{ all -> 0x00d4 }
            java.lang.String r12 = r12.toString()     // Catch:{ all -> 0x00d4 }
            r10.log(r11, r12)     // Catch:{ all -> 0x00d4 }
            r1.printStackTrace()     // Catch:{ all -> 0x00d4 }
            if (r3 == 0) goto L_0x0093
            r3.close()     // Catch:{ Exception -> 0x00cf }
            goto L_0x0093
        L_0x00cf:
            r2 = move-exception
            r2.printStackTrace()
            goto L_0x0093
        L_0x00d4:
            r10 = move-exception
        L_0x00d5:
            if (r3 == 0) goto L_0x00da
            r3.close()     // Catch:{ Exception -> 0x00db }
        L_0x00da:
            throw r10
        L_0x00db:
            r2 = move-exception
            r2.printStackTrace()
            goto L_0x00da
        L_0x00e0:
            r2 = move-exception
            r2.printStackTrace()
        L_0x00e4:
            r3 = r4
            goto L_0x0093
        L_0x00e6:
            r10 = move-exception
            r3 = r4
            goto L_0x00d5
        L_0x00e9:
            r1 = move-exception
            r3 = r4
            goto L_0x00b0
        */
        throw new UnsupportedOperationException("Method not decompiled: net.xcharger.util.HttpRequest.sendGet(java.lang.String, java.lang.String):java.lang.String");
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x00f8 A[SYNTHETIC, Splitter:B:25:0x00f8] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0124 A[SYNTHETIC, Splitter:B:32:0x0124] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String sendPost(java.lang.String r15, java.lang.String r16, java.lang.String r17) throws java.lang.Exception {
        /*
            r7 = 0
            r4 = 0
            java.lang.String r10 = ""
            java.util.logging.Logger r11 = logger     // Catch:{ Exception -> 0x00d0 }
            java.util.logging.Level r12 = java.util.logging.Level.INFO     // Catch:{ Exception -> 0x00d0 }
            java.lang.StringBuilder r13 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00d0 }
            java.lang.String r14 = "sendPost url:"
            r13.<init>(r14)     // Catch:{ Exception -> 0x00d0 }
            java.lang.StringBuilder r13 = r13.append(r15)     // Catch:{ Exception -> 0x00d0 }
            java.lang.String r13 = r13.toString()     // Catch:{ Exception -> 0x00d0 }
            r11.log(r12, r13)     // Catch:{ Exception -> 0x00d0 }
            java.util.logging.Logger r11 = logger     // Catch:{ Exception -> 0x00d0 }
            java.util.logging.Level r12 = java.util.logging.Level.INFO     // Catch:{ Exception -> 0x00d0 }
            java.lang.StringBuilder r13 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00d0 }
            java.lang.String r14 = "sendPost param:"
            r13.<init>(r14)     // Catch:{ Exception -> 0x00d0 }
            r0 = r16
            java.lang.StringBuilder r13 = r13.append(r0)     // Catch:{ Exception -> 0x00d0 }
            java.lang.String r13 = r13.toString()     // Catch:{ Exception -> 0x00d0 }
            r11.log(r12, r13)     // Catch:{ Exception -> 0x00d0 }
            java.net.URL r9 = new java.net.URL     // Catch:{ Exception -> 0x00d0 }
            r9.<init>(r15)     // Catch:{ Exception -> 0x00d0 }
            java.net.URLConnection r1 = r9.openConnection()     // Catch:{ Exception -> 0x00d0 }
            r11 = 10000(0x2710, float:1.4013E-41)
            r1.setConnectTimeout(r11)     // Catch:{ Exception -> 0x00d0 }
            r11 = 10000(0x2710, float:1.4013E-41)
            r1.setReadTimeout(r11)     // Catch:{ Exception -> 0x00d0 }
            r11 = 0
            r1.setUseCaches(r11)     // Catch:{ Exception -> 0x00d0 }
            r11 = 1
            r1.setDoInput(r11)     // Catch:{ Exception -> 0x00d0 }
            r11 = 1
            r1.setDoOutput(r11)     // Catch:{ Exception -> 0x00d0 }
            boolean r11 = org.apache.http.util.TextUtils.isEmpty(r17)     // Catch:{ Exception -> 0x00d0 }
            if (r11 != 0) goto L_0x005e
            java.lang.String r11 = "Host"
            r0 = r17
            r1.setRequestProperty(r11, r0)     // Catch:{ Exception -> 0x00d0 }
        L_0x005e:
            java.lang.String r11 = "accept"
            java.lang.String r12 = "*/*"
            r1.setRequestProperty(r11, r12)     // Catch:{ Exception -> 0x00d0 }
            java.lang.String r11 = "connection"
            java.lang.String r12 = "Close"
            r1.setRequestProperty(r11, r12)     // Catch:{ Exception -> 0x00d0 }
            java.lang.String r11 = "user-agent"
            java.lang.String r12 = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)"
            r1.setRequestProperty(r11, r12)     // Catch:{ Exception -> 0x00d0 }
            java.lang.String r11 = "Content-Type"
            java.lang.String r12 = "application/json;charset=utf-8"
            r1.setRequestProperty(r11, r12)     // Catch:{ Exception -> 0x00d0 }
            java.io.PrintWriter r8 = new java.io.PrintWriter     // Catch:{ Exception -> 0x00d0 }
            java.io.OutputStream r11 = r1.getOutputStream()     // Catch:{ Exception -> 0x00d0 }
            r8.<init>(r11)     // Catch:{ Exception -> 0x00d0 }
            r0 = r16
            r8.print(r0)     // Catch:{ Exception -> 0x0179, all -> 0x0172 }
            r8.flush()     // Catch:{ Exception -> 0x0179, all -> 0x0172 }
            r8.close()     // Catch:{ Exception -> 0x0179, all -> 0x0172 }
            java.io.BufferedReader r5 = new java.io.BufferedReader     // Catch:{ Exception -> 0x0179, all -> 0x0172 }
            java.io.InputStreamReader r11 = new java.io.InputStreamReader     // Catch:{ Exception -> 0x0179, all -> 0x0172 }
            java.io.InputStream r12 = r1.getInputStream()     // Catch:{ Exception -> 0x0179, all -> 0x0172 }
            r11.<init>(r12)     // Catch:{ Exception -> 0x0179, all -> 0x0172 }
            r5.<init>(r11)     // Catch:{ Exception -> 0x0179, all -> 0x0172 }
        L_0x009c:
            java.lang.String r6 = r5.readLine()     // Catch:{ Exception -> 0x017d, all -> 0x0175 }
            if (r6 != 0) goto L_0x00be
            java.io.PrintStream r11 = java.lang.System.out     // Catch:{ Exception -> 0x017d, all -> 0x0175 }
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x017d, all -> 0x0175 }
            java.lang.String r13 = "发送POST return :"
            r12.<init>(r13)     // Catch:{ Exception -> 0x017d, all -> 0x0175 }
            java.lang.StringBuilder r12 = r12.append(r10)     // Catch:{ Exception -> 0x017d, all -> 0x0175 }
            java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x017d, all -> 0x0175 }
            r11.println(r12)     // Catch:{ Exception -> 0x017d, all -> 0x0175 }
            if (r5 == 0) goto L_0x0182
            r5.close()     // Catch:{ IOException -> 0x014d }
            r4 = r5
            r7 = r8
        L_0x00bd:
            return r10
        L_0x00be:
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x017d, all -> 0x0175 }
            java.lang.String r12 = java.lang.String.valueOf(r10)     // Catch:{ Exception -> 0x017d, all -> 0x0175 }
            r11.<init>(r12)     // Catch:{ Exception -> 0x017d, all -> 0x0175 }
            java.lang.StringBuilder r11 = r11.append(r6)     // Catch:{ Exception -> 0x017d, all -> 0x0175 }
            java.lang.String r10 = r11.toString()     // Catch:{ Exception -> 0x017d, all -> 0x0175 }
            goto L_0x009c
        L_0x00d0:
            r2 = move-exception
        L_0x00d1:
            java.lang.String r10 = ""
            java.util.logging.Logger r11 = logger     // Catch:{ all -> 0x0121 }
            java.util.logging.Level r12 = java.util.logging.Level.WARNING     // Catch:{ all -> 0x0121 }
            java.lang.StringBuilder r13 = new java.lang.StringBuilder     // Catch:{ all -> 0x0121 }
            java.lang.String r14 = "发送 POST 请求出现异常=！"
            r13.<init>(r14)     // Catch:{ all -> 0x0121 }
            java.lang.String r14 = r2.getMessage()     // Catch:{ all -> 0x0121 }
            java.lang.StringBuilder r13 = r13.append(r14)     // Catch:{ all -> 0x0121 }
            java.lang.String r13 = r13.toString()     // Catch:{ all -> 0x0121 }
            r11.log(r12, r13)     // Catch:{ all -> 0x0121 }
            net.xcharger.sdk.device.MessageProxyException r11 = new net.xcharger.sdk.device.MessageProxyException     // Catch:{ all -> 0x0121 }
            java.lang.String r12 = r2.toString()     // Catch:{ all -> 0x0121 }
            r11.<init>(r12)     // Catch:{ all -> 0x0121 }
            if (r4 == 0) goto L_0x00bd
            r4.close()     // Catch:{ IOException -> 0x00fc }
            goto L_0x00bd
        L_0x00fc:
            r3 = move-exception
            java.util.logging.Logger r11 = logger
            java.util.logging.Level r12 = java.util.logging.Level.WARNING
            java.lang.StringBuilder r13 = new java.lang.StringBuilder
            java.lang.String r14 = "发送 POST 请求出现异常！"
            r13.<init>(r14)
            java.lang.String r14 = r3.getMessage()
            java.lang.StringBuilder r13 = r13.append(r14)
            java.lang.String r13 = r13.toString()
            r11.log(r12, r13)
            net.xcharger.sdk.device.MessageProxyException r11 = new net.xcharger.sdk.device.MessageProxyException
            java.lang.String r12 = r3.toString()
            r11.<init>(r12)
            throw r3
        L_0x0121:
            r11 = move-exception
        L_0x0122:
            if (r4 == 0) goto L_0x0127
            r4.close()     // Catch:{ IOException -> 0x0128 }
        L_0x0127:
            throw r11
        L_0x0128:
            r3 = move-exception
            java.util.logging.Logger r11 = logger
            java.util.logging.Level r12 = java.util.logging.Level.WARNING
            java.lang.StringBuilder r13 = new java.lang.StringBuilder
            java.lang.String r14 = "发送 POST 请求出现异常！"
            r13.<init>(r14)
            java.lang.String r14 = r3.getMessage()
            java.lang.StringBuilder r13 = r13.append(r14)
            java.lang.String r13 = r13.toString()
            r11.log(r12, r13)
            net.xcharger.sdk.device.MessageProxyException r11 = new net.xcharger.sdk.device.MessageProxyException
            java.lang.String r12 = r3.toString()
            r11.<init>(r12)
            throw r3
        L_0x014d:
            r3 = move-exception
            java.util.logging.Logger r11 = logger
            java.util.logging.Level r12 = java.util.logging.Level.WARNING
            java.lang.StringBuilder r13 = new java.lang.StringBuilder
            java.lang.String r14 = "发送 POST 请求出现异常！"
            r13.<init>(r14)
            java.lang.String r14 = r3.getMessage()
            java.lang.StringBuilder r13 = r13.append(r14)
            java.lang.String r13 = r13.toString()
            r11.log(r12, r13)
            net.xcharger.sdk.device.MessageProxyException r11 = new net.xcharger.sdk.device.MessageProxyException
            java.lang.String r12 = r3.toString()
            r11.<init>(r12)
            throw r3
        L_0x0172:
            r11 = move-exception
            r7 = r8
            goto L_0x0122
        L_0x0175:
            r11 = move-exception
            r4 = r5
            r7 = r8
            goto L_0x0122
        L_0x0179:
            r2 = move-exception
            r7 = r8
            goto L_0x00d1
        L_0x017d:
            r2 = move-exception
            r4 = r5
            r7 = r8
            goto L_0x00d1
        L_0x0182:
            r4 = r5
            r7 = r8
            goto L_0x00bd
        */
        throw new UnsupportedOperationException("Method not decompiled: net.xcharger.util.HttpRequest.sendPost(java.lang.String, java.lang.String, java.lang.String):java.lang.String");
    }
}
