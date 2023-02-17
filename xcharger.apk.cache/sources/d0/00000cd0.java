package org.apache.http.params;

import java.nio.charset.Charset;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.SocketConfig;

@Deprecated
/* loaded from: classes.dex */
public final class HttpParamConfig {
    private HttpParamConfig() {
    }

    public static SocketConfig getSocketConfig(HttpParams params) {
        return SocketConfig.custom().setSoTimeout(params.getIntParameter("http.socket.timeout", 0)).setSoLinger(params.getIntParameter("http.socket.linger", -1)).setTcpNoDelay(params.getBooleanParameter("http.tcp.nodelay", true)).build();
    }

    public static MessageConstraints getMessageConstraints(HttpParams params) {
        return MessageConstraints.custom().setMaxHeaderCount(params.getIntParameter("http.connection.max-header-count", -1)).setMaxLineLength(params.getIntParameter("http.connection.max-line-length", -1)).build();
    }

    public static ConnectionConfig getConnectionConfig(HttpParams params) {
        MessageConstraints messageConstraints = getMessageConstraints(params);
        String csname = (String) params.getParameter("http.protocol.element-charset");
        return ConnectionConfig.custom().setCharset(csname != null ? Charset.forName(csname) : null).setMessageConstraints(messageConstraints).build();
    }
}