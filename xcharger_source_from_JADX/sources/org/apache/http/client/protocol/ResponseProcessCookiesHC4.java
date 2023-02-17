package org.apache.http.client.protocol;

import android.util.Log;
import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.annotation.Immutable;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

@Immutable
public class ResponseProcessCookiesHC4 implements HttpResponseInterceptor {
    private static final String TAG = "HttpClient";

    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        Args.notNull(response, "HTTP request");
        Args.notNull(context, "HTTP context");
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        CookieSpec cookieSpec = clientContext.getCookieSpec();
        if (cookieSpec != null) {
            CookieStore cookieStore = clientContext.getCookieStore();
            if (cookieStore != null) {
                CookieOrigin cookieOrigin = clientContext.getCookieOrigin();
                if (cookieOrigin != null) {
                    processCookies(response.headerIterator("Set-Cookie"), cookieSpec, cookieOrigin, cookieStore);
                    if (cookieSpec.getVersion() > 0) {
                        processCookies(response.headerIterator("Set-Cookie2"), cookieSpec, cookieOrigin, cookieStore);
                    }
                } else if (Log.isLoggable(TAG, 3)) {
                    Log.d(TAG, "Cookie origin not specified in HTTP context");
                }
            } else if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Cookie store not specified in HTTP context");
            }
        } else if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "Cookie spec not specified in HTTP context");
        }
    }

    private void processCookies(HeaderIterator iterator, CookieSpec cookieSpec, CookieOrigin cookieOrigin, CookieStore cookieStore) {
        while (iterator.hasNext()) {
            Header header = iterator.nextHeader();
            try {
                for (Cookie cookie : cookieSpec.parse(header, cookieOrigin)) {
                    try {
                        cookieSpec.validate(cookie, cookieOrigin);
                        cookieStore.addCookie(cookie);
                        if (Log.isLoggable(TAG, 3)) {
                            Log.d(TAG, "Cookie accepted [" + formatCooke(cookie) + "]");
                        }
                    } catch (MalformedCookieException ex) {
                        if (Log.isLoggable(TAG, 5)) {
                            Log.w(TAG, "Cookie rejected [" + formatCooke(cookie) + "] " + ex.getMessage());
                        }
                    }
                }
            } catch (MalformedCookieException ex2) {
                if (Log.isLoggable(TAG, 5)) {
                    Log.w(TAG, "Invalid cookie header: \"" + header + "\". " + ex2.getMessage());
                }
            }
        }
    }

    private static String formatCooke(Cookie cookie) {
        StringBuilder buf = new StringBuilder();
        buf.append(cookie.getName());
        buf.append("=\"");
        String v = cookie.getValue();
        if (v.length() > 100) {
            v = String.valueOf(v.substring(0, 100)) + "...";
        }
        buf.append(v);
        buf.append("\"");
        buf.append(", version:");
        buf.append(Integer.toString(cookie.getVersion()));
        buf.append(", domain:");
        buf.append(cookie.getDomain());
        buf.append(", path:");
        buf.append(cookie.getPath());
        buf.append(", expiry:");
        buf.append(cookie.getExpiryDate());
        return buf.toString();
    }
}
