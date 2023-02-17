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
public class BrowserCompatSpecFactoryHC4 implements CookieSpecFactory, CookieSpecProvider {
    private final String[] datepatterns;
    private final SecurityLevel securityLevel;

    /* loaded from: classes.dex */
    public enum SecurityLevel {
        SECURITYLEVEL_DEFAULT,
        SECURITYLEVEL_IE_MEDIUM;

        /* renamed from: values  reason: to resolve conflict with enum method */
        public static SecurityLevel[] valuesCustom() {
            SecurityLevel[] valuesCustom = values();
            int length = valuesCustom.length;
            SecurityLevel[] securityLevelArr = new SecurityLevel[length];
            System.arraycopy(valuesCustom, 0, securityLevelArr, 0, length);
            return securityLevelArr;
        }
    }

    public BrowserCompatSpecFactoryHC4(String[] datepatterns, SecurityLevel securityLevel) {
        this.datepatterns = datepatterns;
        this.securityLevel = securityLevel;
    }

    public BrowserCompatSpecFactoryHC4(String[] datepatterns) {
        this(null, SecurityLevel.SECURITYLEVEL_DEFAULT);
    }

    public BrowserCompatSpecFactoryHC4() {
        this(null, SecurityLevel.SECURITYLEVEL_DEFAULT);
    }

    public CookieSpec newInstance(HttpParams params) {
        if (params != null) {
            String[] patterns = null;
            Collection<?> param = (Collection) params.getParameter("http.protocol.cookie-datepatterns");
            if (param != null) {
                String[] patterns2 = new String[param.size()];
                patterns = (String[]) param.toArray(patterns2);
            }
            return new BrowserCompatSpecHC4(patterns, this.securityLevel);
        }
        return new BrowserCompatSpecHC4(null, this.securityLevel);
    }

    @Override // org.apache.http.cookie.CookieSpecProvider
    public CookieSpec create(HttpContext context) {
        return new BrowserCompatSpecHC4(this.datepatterns);
    }
}