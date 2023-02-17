package org.apache.http.impl.cookie;

import java.util.Locale;
import org.apache.http.annotation.Immutable;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieRestrictionViolationException;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;
import org.apache.http.util.Args;

@Immutable
public class RFC2109DomainHandlerHC4 implements CookieAttributeHandler {
    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        if (value == null) {
            throw new MalformedCookieException("Missing value for domain attribute");
        } else if (value.trim().length() == 0) {
            throw new MalformedCookieException("Blank value for domain attribute");
        } else {
            cookie.setDomain(value);
        }
    }

    /* JADX WARNING: type inference failed for: r4v6, types: [java.lang.Throwable, org.apache.http.cookie.CookieRestrictionViolationException] */
    /* JADX WARNING: type inference failed for: r4v13, types: [java.lang.Throwable, org.apache.http.cookie.CookieRestrictionViolationException] */
    /* JADX WARNING: type inference failed for: r4v14, types: [java.lang.Throwable, org.apache.http.cookie.CookieRestrictionViolationException] */
    /* JADX WARNING: type inference failed for: r4v15, types: [java.lang.Throwable, org.apache.http.cookie.CookieRestrictionViolationException] */
    /* JADX WARNING: type inference failed for: r4v16, types: [java.lang.Throwable, org.apache.http.cookie.CookieRestrictionViolationException] */
    /* JADX WARNING: type inference failed for: r4v17, types: [java.lang.Throwable, org.apache.http.cookie.CookieRestrictionViolationException] */
    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        String host = origin.getHost();
        String domain = cookie.getDomain();
        if (domain == null) {
            throw new CookieRestrictionViolationException("Cookie domain may not be null");
        } else if (domain.equals(host)) {
        } else {
            if (domain.indexOf(46) == -1) {
                throw new CookieRestrictionViolationException("Domain attribute \"" + domain + "\" does not match the host \"" + host + "\"");
            } else if (!domain.startsWith(".")) {
                throw new CookieRestrictionViolationException("Domain attribute \"" + domain + "\" violates RFC 2109: domain must start with a dot");
            } else {
                int dotIndex = domain.indexOf(46, 1);
                if (dotIndex < 0 || dotIndex == domain.length() - 1) {
                    throw new CookieRestrictionViolationException("Domain attribute \"" + domain + "\" violates RFC 2109: domain must contain an embedded dot");
                }
                String host2 = host.toLowerCase(Locale.ENGLISH);
                if (!host2.endsWith(domain)) {
                    throw new CookieRestrictionViolationException("Illegal domain attribute \"" + domain + "\". Domain of origin: \"" + host2 + "\"");
                } else if (host2.substring(0, host2.length() - domain.length()).indexOf(46) != -1) {
                    throw new CookieRestrictionViolationException("Domain attribute \"" + domain + "\" violates RFC 2109: host minus domain may not contain any dots");
                }
            }
        }
    }

    public boolean match(Cookie cookie, CookieOrigin origin) {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        String host = origin.getHost();
        String domain = cookie.getDomain();
        if (domain == null) {
            return false;
        }
        if (host.equals(domain) || (domain.startsWith(".") && host.endsWith(domain))) {
            return true;
        }
        return false;
    }
}
