package org.apache.http.impl.cookie;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.http.client.utils.Punycode;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;
import org.slf4j.Marker;

/* loaded from: classes.dex */
public class PublicSuffixFilterHC4 implements CookieAttributeHandler {
    private Set<String> exceptions;
    private Set<String> suffixes;
    private final CookieAttributeHandler wrapped;

    public PublicSuffixFilterHC4(CookieAttributeHandler wrapped) {
        this.wrapped = wrapped;
    }

    public void setPublicSuffixes(Collection<String> suffixes) {
        this.suffixes = new HashSet(suffixes);
    }

    public void setExceptions(Collection<String> exceptions) {
        this.exceptions = new HashSet(exceptions);
    }

    public boolean match(Cookie cookie, CookieOrigin origin) {
        if (isForPublicSuffix(cookie)) {
            return false;
        }
        return this.wrapped.match(cookie, origin);
    }

    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        this.wrapped.parse(cookie, value);
    }

    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        this.wrapped.validate(cookie, origin);
    }

    private boolean isForPublicSuffix(Cookie cookie) {
        String domain = cookie.getDomain();
        if (domain.startsWith(".")) {
            domain = domain.substring(1);
        }
        String domain2 = Punycode.toUnicode(domain);
        if ((this.exceptions == null || !this.exceptions.contains(domain2)) && this.suffixes != null) {
            while (!this.suffixes.contains(domain2)) {
                if (domain2.startsWith("*.")) {
                    domain2 = domain2.substring(2);
                }
                int nextdot = domain2.indexOf(46);
                if (nextdot == -1) {
                    return false;
                }
                domain2 = Marker.ANY_MARKER + domain2.substring(nextdot);
                if (domain2.length() <= 0) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}