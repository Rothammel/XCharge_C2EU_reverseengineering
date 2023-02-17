package org.apache.http.impl.cookie;

import java.util.Collection;
import org.apache.http.annotation.Immutable;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

@Immutable
public class NetscapeDraftSpecFactoryHC4 implements CookieSpecFactory, CookieSpecProvider {
    private final String[] datepatterns;

    public NetscapeDraftSpecFactoryHC4(String[] datepatterns2) {
        this.datepatterns = datepatterns2;
    }

    public NetscapeDraftSpecFactoryHC4() {
        this((String[]) null);
    }

    public CookieSpec newInstance(HttpParams params) {
        if (params == null) {
            return new NetscapeDraftSpecHC4();
        }
        String[] patterns = null;
        Collection<?> param = (Collection) params.getParameter("http.protocol.cookie-datepatterns");
        if (param != null) {
            patterns = (String[]) param.toArray(new String[param.size()]);
        }
        return new NetscapeDraftSpecHC4(patterns);
    }

    public CookieSpec create(HttpContext context) {
        return new NetscapeDraftSpecHC4(this.datepatterns);
    }
}
