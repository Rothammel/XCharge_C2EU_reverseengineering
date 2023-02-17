package org.apache.http.impl.cookie;

import java.util.Collection;
import org.apache.http.annotation.Immutable;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

@Immutable
public class BrowserCompatSpecFactoryHC4 implements CookieSpecFactory, CookieSpecProvider {
    private final String[] datepatterns;
    private final SecurityLevel securityLevel;

    public enum SecurityLevel {
        SECURITYLEVEL_DEFAULT,
        SECURITYLEVEL_IE_MEDIUM
    }

    public BrowserCompatSpecFactoryHC4(String[] datepatterns2, SecurityLevel securityLevel2) {
        this.datepatterns = datepatterns2;
        this.securityLevel = securityLevel2;
    }

    public BrowserCompatSpecFactoryHC4(String[] datepatterns2) {
        this((String[]) null, SecurityLevel.SECURITYLEVEL_DEFAULT);
    }

    public BrowserCompatSpecFactoryHC4() {
        this((String[]) null, SecurityLevel.SECURITYLEVEL_DEFAULT);
    }

    public CookieSpec newInstance(HttpParams params) {
        if (params == null) {
            return new BrowserCompatSpecHC4((String[]) null, this.securityLevel);
        }
        String[] patterns = null;
        Collection<?> param = (Collection) params.getParameter("http.protocol.cookie-datepatterns");
        if (param != null) {
            patterns = (String[]) param.toArray(new String[param.size()]);
        }
        return new BrowserCompatSpecHC4(patterns, this.securityLevel);
    }

    public CookieSpec create(HttpContext context) {
        return new BrowserCompatSpecHC4(this.datepatterns);
    }
}
