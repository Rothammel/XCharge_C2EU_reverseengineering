package org.apache.http;

import java.nio.charset.Charset;
import org.apache.commons.lang3.CharEncoding;

public final class Consts {
    public static final Charset ASCII = Charset.forName(CharEncoding.US_ASCII);

    /* renamed from: CR */
    public static final int f158CR = 13;

    /* renamed from: HT */
    public static final int f159HT = 9;
    public static final Charset ISO_8859_1 = Charset.forName(CharEncoding.ISO_8859_1);

    /* renamed from: LF */
    public static final int f160LF = 10;

    /* renamed from: SP */
    public static final int f161SP = 32;
    public static final Charset UTF_8 = Charset.forName(CharEncoding.UTF_8);

    private Consts() {
    }
}
