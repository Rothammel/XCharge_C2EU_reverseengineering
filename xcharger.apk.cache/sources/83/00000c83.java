package org.apache.http.impl.cookie;

import com.xcharge.charger.protocol.monitor.bean.YZXDCAPOption;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.FormattedHeader;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.cookie.BrowserCompatSpecFactoryHC4;
import org.apache.http.message.BasicHeaderElement;
import org.apache.http.message.BasicHeaderValueFormatterHC4;
import org.apache.http.message.BufferedHeader;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;

@NotThreadSafe
/* loaded from: classes.dex */
public class BrowserCompatSpecHC4 extends CookieSpecBaseHC4 {
    private static /* synthetic */ int[] $SWITCH_TABLE$org$apache$http$impl$cookie$BrowserCompatSpecFactoryHC4$SecurityLevel;
    private static final String[] DEFAULT_DATE_PATTERNS = {DateUtils.PATTERN_RFC1123, DateUtils.PATTERN_RFC1036, DateUtils.PATTERN_ASCTIME, "EEE, dd-MMM-yyyy HH:mm:ss z", "EEE, dd-MMM-yyyy HH-mm-ss z", "EEE, dd MMM yy HH:mm:ss z", "EEE dd-MMM-yyyy HH:mm:ss z", "EEE dd MMM yyyy HH:mm:ss z", "EEE dd-MMM-yyyy HH-mm-ss z", "EEE dd-MMM-yy HH:mm:ss z", "EEE dd MMM yy HH:mm:ss z", "EEE,dd-MMM-yy HH:mm:ss z", "EEE,dd-MMM-yyyy HH:mm:ss z", "EEE, dd-MM-yyyy HH:mm:ss z"};
    private final String[] datepatterns;

    static /* synthetic */ int[] $SWITCH_TABLE$org$apache$http$impl$cookie$BrowserCompatSpecFactoryHC4$SecurityLevel() {
        int[] iArr = $SWITCH_TABLE$org$apache$http$impl$cookie$BrowserCompatSpecFactoryHC4$SecurityLevel;
        if (iArr == null) {
            iArr = new int[BrowserCompatSpecFactoryHC4.SecurityLevel.valuesCustom().length];
            try {
                iArr[BrowserCompatSpecFactoryHC4.SecurityLevel.SECURITYLEVEL_DEFAULT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[BrowserCompatSpecFactoryHC4.SecurityLevel.SECURITYLEVEL_IE_MEDIUM.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            $SWITCH_TABLE$org$apache$http$impl$cookie$BrowserCompatSpecFactoryHC4$SecurityLevel = iArr;
        }
        return iArr;
    }

    public BrowserCompatSpecHC4(String[] datepatterns, BrowserCompatSpecFactoryHC4.SecurityLevel securityLevel) {
        if (datepatterns != null) {
            this.datepatterns = (String[]) datepatterns.clone();
        } else {
            this.datepatterns = DEFAULT_DATE_PATTERNS;
        }
        switch ($SWITCH_TABLE$org$apache$http$impl$cookie$BrowserCompatSpecFactoryHC4$SecurityLevel()[securityLevel.ordinal()]) {
            case 1:
                registerAttribHandler("path", new BasicPathHandlerHC4());
                break;
            case 2:
                registerAttribHandler("path", new BasicPathHandlerHC4() { // from class: org.apache.http.impl.cookie.BrowserCompatSpecHC4.1
                    @Override // org.apache.http.impl.cookie.BasicPathHandlerHC4
                    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
                    }
                });
                break;
            default:
                throw new RuntimeException("Unknown security level");
        }
        registerAttribHandler("domain", new BasicDomainHandlerHC4());
        registerAttribHandler("max-age", new BasicMaxAgeHandlerHC4());
        registerAttribHandler("secure", new BasicSecureHandlerHC4());
        registerAttribHandler("comment", new BasicCommentHandlerHC4());
        registerAttribHandler("expires", new BasicExpiresHandlerHC4(this.datepatterns));
        registerAttribHandler(YZXDCAPOption.VERSION, new BrowserCompatVersionAttributeHandler());
    }

    public BrowserCompatSpecHC4(String[] datepatterns) {
        this(datepatterns, BrowserCompatSpecFactoryHC4.SecurityLevel.SECURITYLEVEL_DEFAULT);
    }

    public BrowserCompatSpecHC4() {
        this(null, BrowserCompatSpecFactoryHC4.SecurityLevel.SECURITYLEVEL_DEFAULT);
    }

    public List<Cookie> parse(Header header, CookieOrigin origin) throws MalformedCookieException {
        CharArrayBuffer buffer;
        ParserCursor cursor;
        Args.notNull(header, "Header");
        Args.notNull(origin, "Cookie origin");
        String headername = header.getName();
        if (!headername.equalsIgnoreCase("Set-Cookie")) {
            throw new MalformedCookieException("Unrecognized cookie header '" + header.toString() + "'");
        }
        HeaderElement[] helems = header.getElements();
        boolean versioned = false;
        boolean netscape = false;
        for (HeaderElement helem : helems) {
            if (helem.getParameterByName(YZXDCAPOption.VERSION) != null) {
                versioned = true;
            }
            if (helem.getParameterByName("expires") != null) {
                netscape = true;
            }
        }
        if (netscape || !versioned) {
            NetscapeDraftHeaderParserHC4 parser = NetscapeDraftHeaderParserHC4.DEFAULT;
            if (header instanceof FormattedHeader) {
                buffer = ((FormattedHeader) header).getBuffer();
                cursor = new ParserCursor(((FormattedHeader) header).getValuePos(), buffer.length());
            } else {
                String s = header.getValue();
                if (s == null) {
                    throw new MalformedCookieException("Header value is null");
                }
                buffer = new CharArrayBuffer(s.length());
                buffer.append(s);
                cursor = new ParserCursor(0, buffer.length());
            }
            helems = new HeaderElement[]{parser.parseHeader(buffer, cursor)};
        }
        return parse(helems, origin);
    }

    private static boolean isQuoteEnclosed(String s) {
        return s != null && s.startsWith("\"") && s.endsWith("\"");
    }

    public List<Header> formatCookies(List<Cookie> cookies) {
        Args.notEmpty(cookies, "List of cookies");
        CharArrayBuffer buffer = new CharArrayBuffer(cookies.size() * 20);
        buffer.append("Cookie");
        buffer.append(": ");
        for (int i = 0; i < cookies.size(); i++) {
            Cookie cookie = cookies.get(i);
            if (i > 0) {
                buffer.append("; ");
            }
            String cookieName = cookie.getName();
            String cookieValue = cookie.getValue();
            if (cookie.getVersion() > 0 && !isQuoteEnclosed(cookieValue)) {
                BasicHeaderValueFormatterHC4.INSTANCE.formatHeaderElement(buffer, (HeaderElement) new BasicHeaderElement(cookieName, cookieValue), false);
            } else {
                buffer.append(cookieName);
                buffer.append("=");
                if (cookieValue != null) {
                    buffer.append(cookieValue);
                }
            }
        }
        List<Header> headers = new ArrayList<>(1);
        headers.add(new BufferedHeader(buffer));
        return headers;
    }

    public int getVersion() {
        return 0;
    }

    public Header getVersionHeader() {
        return null;
    }

    public String toString() {
        return CookieSpecs.BROWSER_COMPATIBILITY;
    }
}