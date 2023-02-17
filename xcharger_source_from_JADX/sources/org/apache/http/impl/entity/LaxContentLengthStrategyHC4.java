package org.apache.http.impl.entity;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpMessage;
import org.apache.http.ParseException;
import org.apache.http.ProtocolException;
import org.apache.http.annotation.Immutable;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.util.Args;

@Immutable
public class LaxContentLengthStrategyHC4 implements ContentLengthStrategy {
    public static final LaxContentLengthStrategyHC4 INSTANCE = new LaxContentLengthStrategyHC4();
    private final int implicitLen;

    public LaxContentLengthStrategyHC4(int implicitLen2) {
        this.implicitLen = implicitLen2;
    }

    public LaxContentLengthStrategyHC4() {
        this(-1);
    }

    public long determineLength(HttpMessage message) throws HttpException {
        Args.notNull(message, "HTTP message");
        Header transferEncodingHeader = message.getFirstHeader(HttpHeaders.TRANSFER_ENCODING);
        if (transferEncodingHeader != null) {
            try {
                HeaderElement[] encodings = transferEncodingHeader.getElements();
                int len = encodings.length;
                if ("identity".equalsIgnoreCase(transferEncodingHeader.getValue())) {
                    return -1;
                }
                return (len <= 0 || !"chunked".equalsIgnoreCase(encodings[len + -1].getName())) ? -1 : -2;
            } catch (ParseException px) {
                throw new ProtocolException("Invalid Transfer-Encoding header value: " + transferEncodingHeader, px);
            }
        } else if (message.getFirstHeader("Content-Length") == null) {
            return (long) this.implicitLen;
        } else {
            long contentlen = -1;
            Header[] headers = message.getHeaders("Content-Length");
            int i = headers.length - 1;
            while (i >= 0) {
                try {
                    contentlen = Long.parseLong(headers[i].getValue());
                    break;
                } catch (NumberFormatException e) {
                    i--;
                }
            }
            if (contentlen < 0) {
                return -1;
            }
            return contentlen;
        }
    }
}
