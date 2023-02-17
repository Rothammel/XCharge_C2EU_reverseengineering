package org.apache.http.impl.conn;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.annotation.Immutable;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.protocol.HttpContext;

@Immutable
/* loaded from: classes.dex */
public class SystemDefaultRoutePlanner extends DefaultRoutePlanner {
    private static /* synthetic */ int[] $SWITCH_TABLE$java$net$Proxy$Type;
    private final ProxySelector proxySelector;

    static /* synthetic */ int[] $SWITCH_TABLE$java$net$Proxy$Type() {
        int[] iArr = $SWITCH_TABLE$java$net$Proxy$Type;
        if (iArr == null) {
            iArr = new int[Proxy.Type.values().length];
            try {
                iArr[Proxy.Type.DIRECT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Proxy.Type.HTTP.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Proxy.Type.SOCKS.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            $SWITCH_TABLE$java$net$Proxy$Type = iArr;
        }
        return iArr;
    }

    public SystemDefaultRoutePlanner(SchemePortResolver schemePortResolver, ProxySelector proxySelector) {
        super(schemePortResolver);
        this.proxySelector = proxySelector == null ? ProxySelector.getDefault() : proxySelector;
    }

    public SystemDefaultRoutePlanner(ProxySelector proxySelector) {
        this(null, proxySelector);
    }

    @Override // org.apache.http.impl.conn.DefaultRoutePlanner
    protected HttpHost determineProxy(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
        try {
            URI targetURI = new URI(target.toURI());
            List<Proxy> proxies = this.proxySelector.select(targetURI);
            Proxy p = chooseProxy(proxies);
            if (p.type() != Proxy.Type.HTTP) {
                return null;
            }
            if (!(p.address() instanceof InetSocketAddress)) {
                throw new HttpException("Unable to handle non-Inet proxy address: " + p.address());
            }
            InetSocketAddress isa = (InetSocketAddress) p.address();
            HttpHost result = new HttpHost(getHost(isa), isa.getPort());
            return result;
        } catch (URISyntaxException ex) {
            throw new HttpException("Cannot convert host to URI: " + target, ex);
        }
    }

    private String getHost(InetSocketAddress isa) {
        return isa.isUnresolved() ? isa.getHostName() : isa.getAddress().getHostAddress();
    }

    private Proxy chooseProxy(List<Proxy> proxies) {
        Proxy result = null;
        for (int i = 0; result == null && i < proxies.size(); i++) {
            Proxy p = proxies.get(i);
            switch ($SWITCH_TABLE$java$net$Proxy$Type()[p.type().ordinal()]) {
                case 1:
                case 2:
                    result = p;
                    break;
            }
        }
        if (result == null) {
            Proxy result2 = Proxy.NO_PROXY;
            return result2;
        }
        return result;
    }
}
