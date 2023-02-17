package org.apache.mina.filter.codec.textline;

import com.alibaba.sdk.android.oss.common.RequestParameters;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.proxy.handlers.http.HttpProxyConstants;

public class LineDelimiter {
    public static final LineDelimiter AUTO = new LineDelimiter("");
    public static final LineDelimiter CRLF = new LineDelimiter(HttpProxyConstants.CRLF);
    public static final LineDelimiter DEFAULT;
    public static final LineDelimiter MAC = new LineDelimiter(StringUtils.f145CR);
    public static final LineDelimiter NUL = new LineDelimiter("\u0000");
    public static final LineDelimiter UNIX = new LineDelimiter(StringUtils.f146LF);
    public static final LineDelimiter WINDOWS = CRLF;
    private final String value;

    static {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        new PrintWriter(bout, true).println();
        DEFAULT = new LineDelimiter(new String(bout.toByteArray()));
    }

    public LineDelimiter(String value2) {
        if (value2 == null) {
            throw new IllegalArgumentException(RequestParameters.DELIMITER);
        }
        this.value = value2;
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
        return this.value.equals(((LineDelimiter) o).value);
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
