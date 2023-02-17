package org.apache.http.impl.cookie;

import java.util.Collection;
import org.apache.http.annotation.Immutable;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

@Immutable
public class RFC2965SpecFactoryHC4 implements CookieSpecFactory, CookieSpecProvider {
    private final String[] datepatterns;
    private final boolean oneHeader;

    public RFC2965SpecFactoryHC4(String[] datepatterns2, boolean oneHeader2) {
        this.datepatterns = datepatterns2;
        this.oneHeader = oneHeader2;
    }

    public RFC2965SpecFactoryHC4() {
        this((String[]) null, false);
    }

    public CookieSpec newInstance(HttpParams params) {
        if (params == null) {
            return new RFC2965SpecHC4();
        }
        String[] patterns = null;
        Collection<?> param = (Collection) params.getParameter("http.protocol.cookie-datepatterns");
        if (param != null) {
            patterns = (String[]) param.toArray(new String[param.size()]);
        }
        return new RFC2965SpecHC4(patterns, params.getBooleanParameter("http.protocol.single-cookie-header", false));
    }

    public CookieSpec create(HttpContext context) {
        return new RFC2965SpecHC4(this.datepatterns, this.oneHeader);
    }
}
