package org.apache.http.impl.conn;

import com.xcharge.charger.data.bean.setting.ConsoleSetting;
import org.apache.http.HttpHost;
import org.apache.http.annotation.Immutable;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.UnsupportedSchemeException;
import org.apache.http.util.Args;
import org.java_websocket.WebSocket;

@Immutable
public class DefaultSchemePortResolver implements SchemePortResolver {
    public static final DefaultSchemePortResolver INSTANCE = new DefaultSchemePortResolver();

    public int resolve(HttpHost host) throws UnsupportedSchemeException {
        Args.notNull(host, "HTTP host");
        int port = host.getPort();
        if (port > 0) {
            return port;
        }
        String name = host.getSchemeName();
        if (name.equalsIgnoreCase(ConsoleSetting.SCHEMA_HTTP)) {
            return 80;
        }
        if (name.equalsIgnoreCase("https")) {
            return WebSocket.DEFAULT_WSS_PORT;
        }
        throw new UnsupportedSchemeException(String.valueOf(name) + " protocol is not supported");
    }
}
