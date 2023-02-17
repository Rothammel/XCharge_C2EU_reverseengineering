package org.apache.http.impl;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.Immutable;
import org.apache.http.protocol.HttpContext;

@Immutable
public class NoConnectionReuseStrategyHC4 implements ConnectionReuseStrategy {
    public static final NoConnectionReuseStrategyHC4 INSTANCE = new NoConnectionReuseStrategyHC4();

    public boolean keepAlive(HttpResponse response, HttpContext context) {
        return false;
    }
}
