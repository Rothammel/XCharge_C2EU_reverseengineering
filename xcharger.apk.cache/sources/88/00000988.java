package net.xcharger.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.xcharger.mqtt.plugn.MqttPlugin;
import net.xcharger.sdk.device.MessageProxyException;
import org.apache.http.util.TextUtils;

/* loaded from: classes.dex */
public class HttpRequest {
    private static final Class<?> cclass = MqttPlugin.class;
    private static final String className = cclass.getName();
    private static Logger logger = Logger.getLogger(className);

    /* JADX WARN: Removed duplicated region for block: B:43:0x00cb A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:48:0x00d7 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static java.lang.String sendGet(java.lang.String r14, java.lang.String r15) {
        /*
            java.lang.String r8 = ""
            r3 = 0
            r9 = 0
            if (r15 == 0) goto L94
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            java.lang.String r11 = java.lang.String.valueOf(r14)     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            r10.<init>(r11)     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            java.lang.String r11 = "?"
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            java.lang.StringBuilder r10 = r10.append(r15)     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            java.lang.String r9 = r10.toString()     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
        L1d:
            java.net.URL r7 = new java.net.URL     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            r7.<init>(r9)     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            java.net.URLConnection r0 = r7.openConnection()     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            r10 = 10000(0x2710, float:1.4013E-41)
            r0.setConnectTimeout(r10)     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            r10 = 10000(0x2710, float:1.4013E-41)
            r0.setReadTimeout(r10)     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            r10 = 0
            r0.setUseCaches(r10)     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            java.lang.String r10 = "accept"
        */
        //  java.lang.String r11 = "*/*"
        /*
            r0.setRequestProperty(r10, r11)     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            java.lang.String r10 = "connection"
            java.lang.String r11 = "Close"
            r0.setRequestProperty(r10, r11)     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            java.lang.String r10 = "user-agent"
            java.lang.String r11 = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)"
            r0.setRequestProperty(r10, r11)     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            java.lang.String r10 = "Content-Type"
            java.lang.String r11 = "application/json;charset=utf-8"
            r0.setRequestProperty(r10, r11)     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            r0.connect()     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            java.util.Map r6 = r0.getHeaderFields()     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            java.util.Set r10 = r6.keySet()     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            java.util.Iterator r11 = r10.iterator()     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
        L5f:
            boolean r10 = r11.hasNext()     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            if (r10 != 0) goto L96
            java.io.BufferedReader r4 = new java.io.BufferedReader     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            java.io.InputStreamReader r10 = new java.io.InputStreamReader     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            java.io.InputStream r11 = r0.getInputStream()     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            r10.<init>(r11)     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            r4.<init>(r10)     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
        L73:
            java.lang.String r5 = r4.readLine()     // Catch: java.lang.Throwable -> Le6 java.lang.Exception -> Le9
            if (r5 != 0) goto L9d
            java.io.PrintStream r10 = java.lang.System.out     // Catch: java.lang.Throwable -> Le6 java.lang.Exception -> Le9
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> Le6 java.lang.Exception -> Le9
            java.lang.String r12 = "发送GET return :"
            r11.<init>(r12)     // Catch: java.lang.Throwable -> Le6 java.lang.Exception -> Le9
            java.lang.StringBuilder r11 = r11.append(r5)     // Catch: java.lang.Throwable -> Le6 java.lang.Exception -> Le9
            java.lang.String r11 = r11.toString()     // Catch: java.lang.Throwable -> Le6 java.lang.Exception -> Le9
            r10.println(r11)     // Catch: java.lang.Throwable -> Le6 java.lang.Exception -> Le9
            if (r4 == 0) goto Le4
            r4.close()     // Catch: java.lang.Exception -> Le0
            r3 = r4
        L93:
            return r8
        L94:
            r9 = r14
            goto L1d
        L96:
            java.lang.Object r10 = r11.next()     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            java.lang.String r10 = (java.lang.String) r10     // Catch: java.lang.Exception -> Laf java.lang.Throwable -> Ld4
            goto L5f
        L9d:
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> Le6 java.lang.Exception -> Le9
            java.lang.String r11 = java.lang.String.valueOf(r8)     // Catch: java.lang.Throwable -> Le6 java.lang.Exception -> Le9
            r10.<init>(r11)     // Catch: java.lang.Throwable -> Le6 java.lang.Exception -> Le9
            java.lang.StringBuilder r10 = r10.append(r5)     // Catch: java.lang.Throwable -> Le6 java.lang.Exception -> Le9
            java.lang.String r8 = r10.toString()     // Catch: java.lang.Throwable -> Le6 java.lang.Exception -> Le9
            goto L73
        Laf:
            r1 = move-exception
        Lb0:
            java.util.logging.Logger r10 = net.xcharger.util.HttpRequest.logger     // Catch: java.lang.Throwable -> Ld4
            java.util.logging.Level r11 = java.util.logging.Level.WARNING     // Catch: java.lang.Throwable -> Ld4
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> Ld4
            java.lang.String r13 = "发送GET请求出现异常！"
            r12.<init>(r13)     // Catch: java.lang.Throwable -> Ld4
            java.lang.StringBuilder r12 = r12.append(r1)     // Catch: java.lang.Throwable -> Ld4
            java.lang.String r12 = r12.toString()     // Catch: java.lang.Throwable -> Ld4
            r10.log(r11, r12)     // Catch: java.lang.Throwable -> Ld4
            r1.printStackTrace()     // Catch: java.lang.Throwable -> Ld4
            if (r3 == 0) goto L93
            r3.close()     // Catch: java.lang.Exception -> Lcf
            goto L93
        Lcf:
            r2 = move-exception
            r2.printStackTrace()
            goto L93
        Ld4:
            r10 = move-exception
        Ld5:
            if (r3 == 0) goto Lda
            r3.close()     // Catch: java.lang.Exception -> Ldb
        Lda:
            throw r10
        Ldb:
            r2 = move-exception
            r2.printStackTrace()
            goto Lda
        Le0:
            r2 = move-exception
            r2.printStackTrace()
        Le4:
            r3 = r4
            goto L93
        Le6:
            r10 = move-exception
            r3 = r4
            goto Ld5
        Le9:
            r1 = move-exception
            r3 = r4
            goto Lb0
        */
        throw new UnsupportedOperationException("Method not decompiled: net.xcharger.util.HttpRequest.sendGet(java.lang.String, java.lang.String):java.lang.String");
    }

    public static String sendPost(String url, String param, String host) throws Exception {
        BufferedReader in = null;
        String result = "";
        try {
            try {
                logger.log(Level.INFO, "sendPost url:" + url);
                logger.log(Level.INFO, "sendPost param:" + param);
                URL realUrl = new URL(url);
                URLConnection conn = realUrl.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                if (!TextUtils.isEmpty(host)) {
                    conn.setRequestProperty("Host", host);
                }
                conn.setRequestProperty("accept", "*/*");
                conn.setRequestProperty("connection", "Close");
                conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
                conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                try {
                    out.print(param);
                    out.flush();
                    out.close();
                    BufferedReader in2 = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while (true) {
                        try {
                            String line = in2.readLine();
                            if (line == null) {
                                break;
                            }
                            result = String.valueOf(result) + line;
                        } catch (Exception e) {
                            e = e;
                            in = in2;
                            result = "";
                            logger.log(Level.WARNING, "发送 POST 请求出现异常=！" + e.getMessage());
                            new MessageProxyException(e.toString());
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException ex) {
                                    logger.log(Level.WARNING, "发送 POST 请求出现异常！" + ex.getMessage());
                                    new MessageProxyException(ex.toString());
                                    throw ex;
                                }
                            }
                            return result;
                        } catch (Throwable th) {
                            th = th;
                            in = in2;
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException ex2) {
                                    logger.log(Level.WARNING, "发送 POST 请求出现异常！" + ex2.getMessage());
                                    new MessageProxyException(ex2.toString());
                                    throw ex2;
                                }
                            }
                            throw th;
                        }
                    }
                    System.out.println("发送POST return :" + result);
                    if (in2 != null) {
                        try {
                            in2.close();
                        } catch (IOException ex3) {
                            logger.log(Level.WARNING, "发送 POST 请求出现异常！" + ex3.getMessage());
                            new MessageProxyException(ex3.toString());
                            throw ex3;
                        }
                    }
                } catch (Exception e2) {
                    e = e2;
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Throwable th3) {
                th = th3;
            }
        } catch (Exception e3) {
            e = e3;
        }
        return result;
    }
}