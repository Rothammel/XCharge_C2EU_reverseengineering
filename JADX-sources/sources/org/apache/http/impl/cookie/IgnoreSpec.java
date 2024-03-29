package org.apache.http.impl.cookie;

import java.util.Collections;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;

@NotThreadSafe
/* loaded from: classes.dex */
public class IgnoreSpec extends CookieSpecBaseHC4 {
    public int getVersion() {
        return 0;
    }

    public List<Cookie> parse(Header header, CookieOrigin origin) throws MalformedCookieException {
        return Collections.emptyList();
    }

    public List<Header> formatCookies(List<Cookie> cookies) {
        return Collections.emptyList();
    }

    public Header getVersionHeader() {
        return null;
    }
}
