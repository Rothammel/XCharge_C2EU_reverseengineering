package org.apache.mina.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.NoSuchElementException;
import java.util.Set;

public class AvailablePortFinder {
    public static final int MAX_PORT_NUMBER = 49151;
    public static final int MIN_PORT_NUMBER = 1;

    private AvailablePortFinder() {
    }

    public static Set<Integer> getAvailablePorts() {
        return getAvailablePorts(1, MAX_PORT_NUMBER);
    }

    public static int getNextAvailable() {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            try {
                int port = serverSocket.getLocalPort();
                serverSocket.close();
                return port;
            } catch (IOException e) {
                ioe = e;
                ServerSocket serverSocket2 = serverSocket;
                throw new NoSuchElementException(ioe.getMessage());
            }
        } catch (IOException e2) {
            ioe = e2;
            throw new NoSuchElementException(ioe.getMessage());
        }
    }

    public static int getNextAvailable(int fromPort) {
        if (fromPort < 1 || fromPort > 49151) {
            throw new IllegalArgumentException("Invalid start port: " + fromPort);
        }
        for (int i = fromPort; i <= 49151; i++) {
            if (available(i)) {
                return i;
            }
        }
        throw new NoSuchElementException("Could not find an available port above " + fromPort);
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x004a A[SYNTHETIC, Splitter:B:25:0x004a] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0052  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0057 A[SYNTHETIC, Splitter:B:32:0x0057] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean available(int r7) {
        /*
            r4 = 1
            if (r7 < r4) goto L_0x0008
            r5 = 49151(0xbfff, float:6.8875E-41)
            if (r7 <= r5) goto L_0x0021
        L_0x0008:
            java.lang.IllegalArgumentException r4 = new java.lang.IllegalArgumentException
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "Invalid start port: "
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.StringBuilder r5 = r5.append(r7)
            java.lang.String r5 = r5.toString()
            r4.<init>(r5)
            throw r4
        L_0x0021:
            r2 = 0
            r0 = 0
            java.net.ServerSocket r3 = new java.net.ServerSocket     // Catch:{ IOException -> 0x0042, all -> 0x004f }
            r3.<init>(r7)     // Catch:{ IOException -> 0x0042, all -> 0x004f }
            r5 = 1
            r3.setReuseAddress(r5)     // Catch:{ IOException -> 0x0068, all -> 0x0061 }
            java.net.DatagramSocket r1 = new java.net.DatagramSocket     // Catch:{ IOException -> 0x0068, all -> 0x0061 }
            r1.<init>(r7)     // Catch:{ IOException -> 0x0068, all -> 0x0061 }
            r5 = 1
            r1.setReuseAddress(r5)     // Catch:{ IOException -> 0x006b, all -> 0x0064 }
            if (r1 == 0) goto L_0x003a
            r1.close()
        L_0x003a:
            if (r3 == 0) goto L_0x003f
            r3.close()     // Catch:{ IOException -> 0x005b }
        L_0x003f:
            r0 = r1
            r2 = r3
        L_0x0041:
            return r4
        L_0x0042:
            r4 = move-exception
        L_0x0043:
            if (r0 == 0) goto L_0x0048
            r0.close()
        L_0x0048:
            if (r2 == 0) goto L_0x004d
            r2.close()     // Catch:{ IOException -> 0x005d }
        L_0x004d:
            r4 = 0
            goto L_0x0041
        L_0x004f:
            r4 = move-exception
        L_0x0050:
            if (r0 == 0) goto L_0x0055
            r0.close()
        L_0x0055:
            if (r2 == 0) goto L_0x005a
            r2.close()     // Catch:{ IOException -> 0x005f }
        L_0x005a:
            throw r4
        L_0x005b:
            r5 = move-exception
            goto L_0x003f
        L_0x005d:
            r4 = move-exception
            goto L_0x004d
        L_0x005f:
            r5 = move-exception
            goto L_0x005a
        L_0x0061:
            r4 = move-exception
            r2 = r3
            goto L_0x0050
        L_0x0064:
            r4 = move-exception
            r0 = r1
            r2 = r3
            goto L_0x0050
        L_0x0068:
            r4 = move-exception
            r2 = r3
            goto L_0x0043
        L_0x006b:
            r4 = move-exception
            r0 = r1
            r2 = r3
            goto L_0x0043
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.util.AvailablePortFinder.available(int):boolean");
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0051 A[SYNTHETIC, Splitter:B:23:0x0051] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x005a A[SYNTHETIC, Splitter:B:28:0x005a] */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0048 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.util.Set<java.lang.Integer> getAvailablePorts(int r7, int r8) {
        /*
            r4 = 1
            if (r7 < r4) goto L_0x000a
            r4 = 49151(0xbfff, float:6.8875E-41)
            if (r8 > r4) goto L_0x000a
            if (r7 <= r8) goto L_0x002d
        L_0x000a:
            java.lang.IllegalArgumentException r4 = new java.lang.IllegalArgumentException
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "Invalid port range: "
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.StringBuilder r5 = r5.append(r7)
            java.lang.String r6 = " ~ "
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.StringBuilder r5 = r5.append(r8)
            java.lang.String r5 = r5.toString()
            r4.<init>(r5)
            throw r4
        L_0x002d:
            java.util.TreeSet r1 = new java.util.TreeSet
            r1.<init>()
            r0 = r7
        L_0x0033:
            if (r0 > r8) goto L_0x0060
            r2 = 0
            java.net.ServerSocket r3 = new java.net.ServerSocket     // Catch:{ IOException -> 0x004e, all -> 0x0057 }
            r3.<init>(r0)     // Catch:{ IOException -> 0x004e, all -> 0x0057 }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r0)     // Catch:{ IOException -> 0x0064, all -> 0x0061 }
            r1.add(r4)     // Catch:{ IOException -> 0x0064, all -> 0x0061 }
            if (r3 == 0) goto L_0x0067
            r3.close()     // Catch:{ IOException -> 0x004b }
            r2 = r3
        L_0x0048:
            int r0 = r0 + 1
            goto L_0x0033
        L_0x004b:
            r4 = move-exception
            r2 = r3
            goto L_0x0048
        L_0x004e:
            r4 = move-exception
        L_0x004f:
            if (r2 == 0) goto L_0x0048
            r2.close()     // Catch:{ IOException -> 0x0055 }
            goto L_0x0048
        L_0x0055:
            r4 = move-exception
            goto L_0x0048
        L_0x0057:
            r4 = move-exception
        L_0x0058:
            if (r2 == 0) goto L_0x005d
            r2.close()     // Catch:{ IOException -> 0x005e }
        L_0x005d:
            throw r4
        L_0x005e:
            r5 = move-exception
            goto L_0x005d
        L_0x0060:
            return r1
        L_0x0061:
            r4 = move-exception
            r2 = r3
            goto L_0x0058
        L_0x0064:
            r4 = move-exception
            r2 = r3
            goto L_0x004f
        L_0x0067:
            r2 = r3
            goto L_0x0048
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.util.AvailablePortFinder.getAvailablePorts(int, int):java.util.Set");
    }
}
