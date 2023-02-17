package org.apache.http;

import java.nio.charset.Charset;
import org.apache.commons.lang3.CharEncoding;

/* loaded from: classes.dex */
public final class Consts {
    public static final int CR = 13;
    public static final int HT = 9;
    public static final int LF = 10;
    public static final int SP = 32;
    public static final Charset UTF_8 = Charset.forName(CharEncoding.UTF_8);
    public static final Charset ASCII = Charset.forName(CharEncoding.US_ASCII);
    public static final Charset ISO_8859_1 = Charset.forName(CharEncoding.ISO_8859_1);

    private Consts() {
    }
}