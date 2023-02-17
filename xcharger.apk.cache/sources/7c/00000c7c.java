package org.apache.http.impl.cookie;

import java.util.Date;
import org.apache.http.annotation.Immutable;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;
import org.apache.http.util.Args;

@Immutable
/* loaded from: classes.dex */
public class BasicMaxAgeHandlerHC4 extends AbstractCookieAttributeHandlerHC4 {
    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        if (value == null) {
            throw new MalformedCookieException("Missing value for max-age attribute");
        }
        try {
            int age = Integer.parseInt(value);
            if (age < 0) {
                throw new MalformedCookieException("Negative max-age attribute: " + value);
            }
            cookie.setExpiryDate(new Date(System.currentTimeMillis() + (age * 1000)));
        } catch (NumberFormatException e) {
            throw new MalformedCookieException("Invalid max-age attribute: " + value);
        }
    }
}