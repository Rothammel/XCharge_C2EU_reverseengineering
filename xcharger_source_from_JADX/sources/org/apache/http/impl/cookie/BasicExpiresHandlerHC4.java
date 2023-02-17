package org.apache.http.impl.cookie;

import java.util.Date;
import org.apache.http.annotation.Immutable;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;
import org.apache.http.util.Args;

@Immutable
public class BasicExpiresHandlerHC4 extends AbstractCookieAttributeHandlerHC4 {
    private final String[] datepatterns;

    public BasicExpiresHandlerHC4(String[] datepatterns2) {
        Args.notNull(datepatterns2, "Array of date patterns");
        this.datepatterns = datepatterns2;
    }

    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        if (value == null) {
            throw new MalformedCookieException("Missing value for expires attribute");
        }
        Date expiry = DateUtils.parseDate(value, this.datepatterns);
        if (expiry == null) {
            throw new MalformedCookieException("Unable to parse expires attribute: " + value);
        }
        cookie.setExpiryDate(expiry);
    }
}
