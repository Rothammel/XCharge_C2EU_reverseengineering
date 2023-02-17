package org.apache.http.client.entity;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.commons.lang3.CharEncoding;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.utils.URLEncodedUtilsHC4;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntityHC4;

@NotThreadSafe
public class UrlEncodedFormEntityHC4 extends StringEntityHC4 {
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public UrlEncodedFormEntityHC4(List<? extends NameValuePair> parameters, String charset) throws UnsupportedEncodingException {
        super(URLEncodedUtilsHC4.format(parameters, charset != null ? charset : Charset.forName(CharEncoding.ISO_8859_1).name()), ContentType.create(URLEncodedUtilsHC4.CONTENT_TYPE, charset));
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public UrlEncodedFormEntityHC4(Iterable<? extends NameValuePair> parameters, Charset charset) {
        super(URLEncodedUtilsHC4.format(parameters, charset != null ? charset : Charset.forName(CharEncoding.ISO_8859_1)), ContentType.create(URLEncodedUtilsHC4.CONTENT_TYPE, charset));
    }

    public UrlEncodedFormEntityHC4(List<? extends NameValuePair> parameters) throws UnsupportedEncodingException {
        this((Iterable<? extends NameValuePair>) parameters, (Charset) null);
    }

    public UrlEncodedFormEntityHC4(Iterable<? extends NameValuePair> parameters) {
        this(parameters, (Charset) null);
    }
}
