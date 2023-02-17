package p010it.sauronsoftware.ftp4j.extrecognizers;

/* renamed from: it.sauronsoftware.ftp4j.extrecognizers.DefaultTextualExtensionRecognizer */
public class DefaultTextualExtensionRecognizer extends ParametricTextualExtensionRecognizer {
    private static DefaultTextualExtensionRecognizer instance = null;
    private static final Object lock = new Object();

    public static DefaultTextualExtensionRecognizer getInstance() {
        synchronized (lock) {
            if (instance == null) {
                instance = new DefaultTextualExtensionRecognizer();
            }
        }
        return instance;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0035 A[SYNTHETIC, Splitter:B:13:0x0035] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0043 A[SYNTHETIC, Splitter:B:21:0x0043] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0039 A[EDGE_INSN: B:30:0x0039->B:15:0x0039 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:32:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x001e A[Catch:{ Exception -> 0x0031, all -> 0x004d }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private DefaultTextualExtensionRecognizer() {
        /*
            r7 = this;
            r7.<init>()
            r1 = 0
            java.io.BufferedReader r2 = new java.io.BufferedReader     // Catch:{ Exception -> 0x0050, all -> 0x0040 }
            java.io.InputStreamReader r4 = new java.io.InputStreamReader     // Catch:{ Exception -> 0x0050, all -> 0x0040 }
            java.lang.Class r5 = r7.getClass()     // Catch:{ Exception -> 0x0050, all -> 0x0040 }
            java.lang.String r6 = "textualexts"
            java.io.InputStream r5 = r5.getResourceAsStream(r6)     // Catch:{ Exception -> 0x0050, all -> 0x0040 }
            r4.<init>(r5)     // Catch:{ Exception -> 0x0050, all -> 0x0040 }
            r2.<init>(r4)     // Catch:{ Exception -> 0x0050, all -> 0x0040 }
        L_0x0018:
            java.lang.String r0 = r2.readLine()     // Catch:{ Exception -> 0x0031, all -> 0x004d }
            if (r0 == 0) goto L_0x0039
            java.util.StringTokenizer r3 = new java.util.StringTokenizer     // Catch:{ Exception -> 0x0031, all -> 0x004d }
            r3.<init>(r0)     // Catch:{ Exception -> 0x0031, all -> 0x004d }
        L_0x0023:
            boolean r4 = r3.hasMoreTokens()     // Catch:{ Exception -> 0x0031, all -> 0x004d }
            if (r4 == 0) goto L_0x0018
            java.lang.String r4 = r3.nextToken()     // Catch:{ Exception -> 0x0031, all -> 0x004d }
            r7.addExtension(r4)     // Catch:{ Exception -> 0x0031, all -> 0x004d }
            goto L_0x0023
        L_0x0031:
            r4 = move-exception
            r1 = r2
        L_0x0033:
            if (r1 == 0) goto L_0x0038
            r1.close()     // Catch:{ Throwable -> 0x0047 }
        L_0x0038:
            return
        L_0x0039:
            if (r2 == 0) goto L_0x003e
            r2.close()     // Catch:{ Throwable -> 0x004b }
        L_0x003e:
            r1 = r2
            goto L_0x0038
        L_0x0040:
            r4 = move-exception
        L_0x0041:
            if (r1 == 0) goto L_0x0046
            r1.close()     // Catch:{ Throwable -> 0x0049 }
        L_0x0046:
            throw r4
        L_0x0047:
            r4 = move-exception
            goto L_0x0038
        L_0x0049:
            r5 = move-exception
            goto L_0x0046
        L_0x004b:
            r4 = move-exception
            goto L_0x003e
        L_0x004d:
            r4 = move-exception
            r1 = r2
            goto L_0x0041
        L_0x0050:
            r4 = move-exception
            goto L_0x0033
        */
        throw new UnsupportedOperationException("Method not decompiled: p010it.sauronsoftware.ftp4j.extrecognizers.DefaultTextualExtensionRecognizer.<init>():void");
    }
}
