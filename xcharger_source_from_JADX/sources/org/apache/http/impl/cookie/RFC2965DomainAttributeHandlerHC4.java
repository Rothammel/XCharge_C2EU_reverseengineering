package org.apache.http.impl.cookie;

import java.util.Locale;
import org.apache.commons.lang3.ClassUtils;
import org.apache.http.annotation.Immutable;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieRestrictionViolationException;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;
import org.apache.http.util.Args;

@Immutable
public class RFC2965DomainAttributeHandlerHC4 implements CookieAttributeHandler {
    public void parse(SetCookie cookie, String domain) throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        if (domain == null) {
            throw new MalformedCookieException("Missing value for domain attribute");
        } else if (domain.trim().length() == 0) {
            throw new MalformedCookieException("Blank value for domain attribute");
        } else {
            String s = domain.toLowerCase(Locale.ENGLISH);
            if (!domain.startsWith(".")) {
                s = String.valueOf(ClassUtils.PACKAGE_SEPARATOR_CHAR) + s;
            }
            cookie.setDomain(s);
        }
    }

    public boolean domainMatch(String host, String domain) {
        return host.equals(domain) || (domain.startsWith(".") && host.endsWith(domain));
    }

    /* JADX WARNING: type inference failed for: r4v8, types: [java.lang.Throwable, org.apache.http.cookie.CookieRestrictionViolationException] */
    /* JADX WARNING: type inference failed for: r4v18, types: [java.lang.Throwable, org.apache.http.cookie.CookieRestrictionViolationException] */
    /* JADX WARNING: type inference failed for: r4v19, types: [java.lang.Throwable, org.apache.http.cookie.CookieRestrictionViolationException] */
    /* JADX WARNING: type inference failed for: r4v22, types: [java.lang.Throwable, org.apache.http.cookie.CookieRestrictionViolationException] */
    /* JADX WARNING: type inference failed for: r4v25, types: [java.lang.Throwable, org.apache.http.cookie.CookieRestrictionViolationException] */
    /* JADX WARNING: type inference failed for: r4v26, types: [java.lang.Throwable, org.apache.http.cookie.CookieRestrictionViolationException] */
    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        String host = origin.getHost().toLowerCase(Locale.ENGLISH);
        if (cookie.getDomain() == null) {
            throw new CookieRestrictionViolationException("Invalid cookie state: domain not specified");
        }
        String cookieDomain = cookie.getDomain().toLowerCase(Locale.ENGLISH);
        if (!(cookie instanceof ClientCookie) || !((ClientCookie) cookie).containsAttribute("domain")) {
            if (!cookie.getDomain().equals(host)) {
                throw new CookieRestrictionViolationException("Illegal domain attribute: \"" + cookie.getDomain() + "\"." + "Domain of origin: \"" + host + "\"");
            }
        } else if (!cookieDomain.startsWith(".")) {
            throw new CookieRestrictionViolationException("Domain attribute \"" + cookie.getDomain() + "\" violates RFC 2109: domain must start with a dot");
        } else {
            int dotIndex = cookieDomain.indexOf(46, 1);
            if ((dotIndex < 0 || dotIndex == cookieDomain.length() - 1) && !cookieDomain.equals(".local")) {
                throw new CookieRestrictionViolationException("Domain attribute \"" + cookie.getDomain() + "\" violates RFC 2965: the value contains no embedded dots " + "and the value is not .local");
            } else if (!domainMatch(host, cookieDomain)) {
                throw new CookieRestrictionViolationException("Domain attribute \"" + cookie.getDomain() + "\" violates RFC 2965: effective host name does not " + "domain-match domain attribute.");
            } else if (host.substring(0, host.length() - cookieDomain.length()).indexOf(46) != -1) {
                throw new CookieRestrictionViolationException("Domain attribute \"" + cookie.getDomain() + "\" violates RFC 2965: " + "effective host minus domain may not contain any dots");
            }
        }
    }

    public boolean match(Cookie cookie, CookieOrigin origin) {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        String host = origin.getHost().toLowerCase(Locale.ENGLISH);
        String cookieDomain = cookie.getDomain();
        if (domainMatch(host, cookieDomain) && host.substring(0, host.length() - cookieDomain.length()).indexOf(46) == -1) {
            return true;
        }
        return false;
    }
}
