package org.apache.http.impl.cookie;

import java.util.Collection;
import org.apache.http.annotation.Immutable;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

@Immutable
/* loaded from: classes.dex */
public class RFC2965SpecFactoryHC4 implements CookieSpecFactory, CookieSpecProvider {
    private final String[] datepatterns;
    private final boolean oneHeader;

    public RFC2965SpecFactoryHC4(String[] datepatterns, boolean oneHeader) {
        this.datepatterns = datepatterns;
        this.oneHeader = oneHeader;
    }

    public RFC2965SpecFactoryHC4() {
        this(null, false);
    }

    public CookieSpec newInstance(HttpParams params) {
        if (params != null) {
            String[] patterns = null;
            Collection<?> param = (Collection) params.getParameter("http.protocol.cookie-datepatterns");
            if (param != null) {
                String[] patterns2 = new String[param.size()];
                patterns = (String[]) param.toArray(patterns2);
            }
            boolean singleHeader = params.getBooleanParameter("http.protocol.single-cookie-header", false);
            return new RFC2965SpecHC4(patterns, singleHeader);
        }
        return new RFC2965SpecHC4();
    }

    @Override // org.apache.http.cookie.CookieSpecProvider
    public CookieSpec create(HttpContext context) {
        return new RFC2965SpecHC4(this.datepatterns, this.oneHeader);
    }
}
