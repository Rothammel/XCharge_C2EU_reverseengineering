package org.apache.http.impl.cookie;

import com.xcharge.charger.protocol.monitor.bean.YZXDCAPOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookiePathComparator;
import org.apache.http.cookie.CookieRestrictionViolationException;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.message.BufferedHeader;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;

@NotThreadSafe
public class RFC2109SpecHC4 extends CookieSpecBaseHC4 {
    private static final String[] DATE_PATTERNS = {DateUtils.PATTERN_RFC1123, DateUtils.PATTERN_RFC1036, DateUtils.PATTERN_ASCTIME};
    private static final CookiePathComparator PATH_COMPARATOR = new CookiePathComparator();
    private final String[] datepatterns;
    private final boolean oneHeader;

    public RFC2109SpecHC4(String[] datepatterns2, boolean oneHeader2) {
        if (datepatterns2 != null) {
            this.datepatterns = (String[]) datepatterns2.clone();
        } else {
            this.datepatterns = DATE_PATTERNS;
        }
        this.oneHeader = oneHeader2;
        registerAttribHandler(YZXDCAPOption.VERSION, new RFC2109VersionHandlerHC4());
        registerAttribHandler("path", new BasicPathHandlerHC4());
        registerAttribHandler("domain", new RFC2109DomainHandlerHC4());
        registerAttribHandler("max-age", new BasicMaxAgeHandlerHC4());
        registerAttribHandler("secure", new BasicSecureHandlerHC4());
        registerAttribHandler("comment", new BasicCommentHandlerHC4());
        registerAttribHandler("expires", new BasicExpiresHandlerHC4(this.datepatterns));
    }

    public RFC2109SpecHC4() {
        this((String[]) null, false);
    }

    public List<Cookie> parse(Header header, CookieOrigin origin) throws MalformedCookieException {
        Args.notNull(header, "Header");
        Args.notNull(origin, "Cookie origin");
        if (header.getName().equalsIgnoreCase("Set-Cookie")) {
            return parse(header.getElements(), origin);
        }
        throw new MalformedCookieException("Unrecognized cookie header '" + header.toString() + "'");
    }

    /* JADX WARNING: type inference failed for: r1v5, types: [java.lang.Throwable, org.apache.http.cookie.CookieRestrictionViolationException] */
    /* JADX WARNING: type inference failed for: r1v6, types: [java.lang.Throwable, org.apache.http.cookie.CookieRestrictionViolationException] */
    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        String name = cookie.getName();
        if (name.indexOf(32) != -1) {
            throw new CookieRestrictionViolationException("Cookie name may not contain blanks");
        } else if (name.startsWith("$")) {
            throw new CookieRestrictionViolationException("Cookie name may not start with $");
        } else {
            super.validate(cookie, origin);
        }
    }

    public List<Header> formatCookies(List<Cookie> cookies) {
        List<Cookie> cookieList;
        Args.notEmpty(cookies, "List of cookies");
        if (cookies.size() > 1) {
            cookieList = new ArrayList<>(cookies);
            Collections.sort(cookieList, PATH_COMPARATOR);
        } else {
            cookieList = cookies;
        }
        if (this.oneHeader) {
            return doFormatOneHeader(cookieList);
        }
        return doFormatManyHeaders(cookieList);
    }

    private List<Header> doFormatOneHeader(List<Cookie> cookies) {
        int version = Integer.MAX_VALUE;
        for (Cookie cookie : cookies) {
            if (cookie.getVersion() < version) {
                version = cookie.getVersion();
            }
        }
        CharArrayBuffer buffer = new CharArrayBuffer(cookies.size() * 40);
        buffer.append("Cookie");
        buffer.append(": ");
        buffer.append("$Version=");
        buffer.append(Integer.toString(version));
        for (Cookie cooky : cookies) {
            buffer.append("; ");
            formatCookieAsVer(buffer, cooky, version);
        }
        List<Header> headers = new ArrayList<>(1);
        headers.add(new BufferedHeader(buffer));
        return headers;
    }

    private List<Header> doFormatManyHeaders(List<Cookie> cookies) {
        List<Header> headers = new ArrayList<>(cookies.size());
        for (Cookie cookie : cookies) {
            int version = cookie.getVersion();
            CharArrayBuffer buffer = new CharArrayBuffer(40);
            buffer.append("Cookie: ");
            buffer.append("$Version=");
            buffer.append(Integer.toString(version));
            buffer.append("; ");
            formatCookieAsVer(buffer, cookie, version);
            headers.add(new BufferedHeader(buffer));
        }
        return headers;
    }

    /* access modifiers changed from: protected */
    public void formatParamAsVer(CharArrayBuffer buffer, String name, String value, int version) {
        buffer.append(name);
        buffer.append("=");
        if (value == null) {
            return;
        }
        if (version > 0) {
            buffer.append(TokenParser.DQUOTE);
            buffer.append(value);
            buffer.append(TokenParser.DQUOTE);
            return;
        }
        buffer.append(value);
    }

    /* access modifiers changed from: protected */
    public void formatCookieAsVer(CharArrayBuffer buffer, Cookie cookie, int version) {
        formatParamAsVer(buffer, cookie.getName(), cookie.getValue(), version);
        if (cookie.getPath() != null && (cookie instanceof ClientCookie) && ((ClientCookie) cookie).containsAttribute("path")) {
            buffer.append("; ");
            formatParamAsVer(buffer, "$Path", cookie.getPath(), version);
        }
        if (cookie.getDomain() != null && (cookie instanceof ClientCookie) && ((ClientCookie) cookie).containsAttribute("domain")) {
            buffer.append("; ");
            formatParamAsVer(buffer, "$Domain", cookie.getDomain(), version);
        }
    }

    public int getVersion() {
        return 1;
    }

    public Header getVersionHeader() {
        return null;
    }

    public String toString() {
        return "rfc2109";
    }
}
