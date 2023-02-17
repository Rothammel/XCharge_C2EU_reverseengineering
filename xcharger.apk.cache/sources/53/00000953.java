package it.sauronsoftware.ftp4j.extrecognizers;

/* loaded from: classes.dex */
public class DefaultTextualExtensionRecognizer extends ParametricTextualExtensionRecognizer {
    private static final Object lock = new Object();
    private static DefaultTextualExtensionRecognizer instance = null;

    public static DefaultTextualExtensionRecognizer getInstance() {
        synchronized (lock) {
            if (instance == null) {
                instance = new DefaultTextualExtensionRecognizer();
            }
        }
        return instance;
    }

    /* JADX WARN: Removed duplicated region for block: B:43:0x0039 A[EDGE_INSN: B:43:0x0039->B:16:0x0039 ?: BREAK  , SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:6:0x001e A[Catch: Exception -> 0x0031, all -> 0x004d, TryCatch #6 {Exception -> 0x0031, all -> 0x004d, blocks: (B:4:0x0018, B:6:0x001e, B:7:0x0023, B:9:0x0029), top: B:41:0x0018 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private DefaultTextualExtensionRecognizer() {
        /*
            r7 = this;
            r7.<init>()
            r1 = 0
            java.io.BufferedReader r2 = new java.io.BufferedReader     // Catch: java.lang.Throwable -> L40 java.lang.Exception -> L50
            java.io.InputStreamReader r4 = new java.io.InputStreamReader     // Catch: java.lang.Throwable -> L40 java.lang.Exception -> L50
            java.lang.Class r5 = r7.getClass()     // Catch: java.lang.Throwable -> L40 java.lang.Exception -> L50
            java.lang.String r6 = "textualexts"
            java.io.InputStream r5 = r5.getResourceAsStream(r6)     // Catch: java.lang.Throwable -> L40 java.lang.Exception -> L50
            r4.<init>(r5)     // Catch: java.lang.Throwable -> L40 java.lang.Exception -> L50
            r2.<init>(r4)     // Catch: java.lang.Throwable -> L40 java.lang.Exception -> L50
        L18:
            java.lang.String r0 = r2.readLine()     // Catch: java.lang.Exception -> L31 java.lang.Throwable -> L4d
            if (r0 == 0) goto L39
            java.util.StringTokenizer r3 = new java.util.StringTokenizer     // Catch: java.lang.Exception -> L31 java.lang.Throwable -> L4d
            r3.<init>(r0)     // Catch: java.lang.Exception -> L31 java.lang.Throwable -> L4d
        L23:
            boolean r4 = r3.hasMoreTokens()     // Catch: java.lang.Exception -> L31 java.lang.Throwable -> L4d
            if (r4 == 0) goto L18
            java.lang.String r4 = r3.nextToken()     // Catch: java.lang.Exception -> L31 java.lang.Throwable -> L4d
            r7.addExtension(r4)     // Catch: java.lang.Exception -> L31 java.lang.Throwable -> L4d
            goto L23
        L31:
            r4 = move-exception
            r1 = r2
        L33:
            if (r1 == 0) goto L38
            r1.close()     // Catch: java.lang.Throwable -> L47
        L38:
            return
        L39:
            if (r2 == 0) goto L3e
            r2.close()     // Catch: java.lang.Throwable -> L4b
        L3e:
            r1 = r2
            goto L38
        L40:
            r4 = move-exception
        L41:
            if (r1 == 0) goto L46
            r1.close()     // Catch: java.lang.Throwable -> L49
        L46:
            throw r4
        L47:
            r4 = move-exception
            goto L38
        L49:
            r5 = move-exception
            goto L46
        L4b:
            r4 = move-exception
            goto L3e
        L4d:
            r4 = move-exception
            r1 = r2
            goto L41
        L50:
            r4 = move-exception
            goto L33
        */
        throw new UnsupportedOperationException("Method not decompiled: it.sauronsoftware.ftp4j.extrecognizers.DefaultTextualExtensionRecognizer.<init>():void");
    }
}