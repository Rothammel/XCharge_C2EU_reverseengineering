package org.apache.http.impl.client;

import org.apache.http.annotation.Immutable;
import org.apache.http.client.methods.HttpHeadHC4;
import org.apache.http.client.methods.HttpPostHC4;

@Immutable
/* loaded from: classes.dex */
public class LaxRedirectStrategy extends DefaultRedirectStrategy {
    private static final String[] REDIRECT_METHODS = {"GET", HttpPostHC4.METHOD_NAME, HttpHeadHC4.METHOD_NAME};

    @Override // org.apache.http.impl.client.DefaultRedirectStrategy
    protected boolean isRedirectable(String method) {
        String[] strArr;
        for (String m : REDIRECT_METHODS) {
            if (m.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }
}
