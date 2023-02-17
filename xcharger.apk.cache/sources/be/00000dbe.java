package org.apache.mina.filter.codec.textline;

import com.alibaba.sdk.android.oss.common.RequestParameters;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.proxy.handlers.http.HttpProxyConstants;

/* loaded from: classes.dex */
public class LineDelimiter {
    public static final LineDelimiter AUTO;
    public static final LineDelimiter CRLF;
    public static final LineDelimiter DEFAULT;
    public static final LineDelimiter MAC;
    public static final LineDelimiter NUL;
    public static final LineDelimiter UNIX;
    public static final LineDelimiter WINDOWS;
    private final String value;

    static {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter((OutputStream) bout, true);
        out.println();
        DEFAULT = new LineDelimiter(new String(bout.toByteArray()));
        AUTO = new LineDelimiter("");
        CRLF = new LineDelimiter(HttpProxyConstants.CRLF);
        UNIX = new LineDelimiter(StringUtils.LF);
        WINDOWS = CRLF;
        MAC = new LineDelimiter(StringUtils.CR);
        NUL = new LineDelimiter("\u0000");
    }

    public LineDelimiter(String value) {
        if (value == null) {
            throw new IllegalArgumentException(RequestParameters.DELIMITER);
        }
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public int hashCode() {
        return this.value.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LineDelimiter)) {
            return false;
        }
        LineDelimiter that = (LineDelimiter) o;
        return this.value.equals(that.value);
    }

    public String toString() {
        if (this.value.length() == 0) {
            return "delimiter: auto";
        }
        StringBuilder buf = new StringBuilder();
        buf.append("delimiter:");
        for (int i = 0; i < this.value.length(); i++) {
            buf.append(" 0x");
            buf.append(Integer.toHexString(this.value.charAt(i)));
        }
        return buf.toString();
    }
}