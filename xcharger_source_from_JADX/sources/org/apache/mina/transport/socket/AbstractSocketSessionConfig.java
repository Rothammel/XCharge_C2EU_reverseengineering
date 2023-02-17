package org.apache.mina.transport.socket;

import org.apache.mina.core.session.AbstractIoSessionConfig;
import org.apache.mina.core.session.IoSessionConfig;

public abstract class AbstractSocketSessionConfig extends AbstractIoSessionConfig implements SocketSessionConfig {
    public void setAll(IoSessionConfig config) {
        super.setAll(config);
        if (config instanceof SocketSessionConfig) {
            if (config instanceof AbstractSocketSessionConfig) {
                AbstractSocketSessionConfig cfg = (AbstractSocketSessionConfig) config;
                if (cfg.isKeepAliveChanged()) {
                    setKeepAlive(cfg.isKeepAlive());
                }
                if (cfg.isOobInlineChanged()) {
                    setOobInline(cfg.isOobInline());
                }
                if (cfg.isReceiveBufferSizeChanged()) {
                    setReceiveBufferSize(cfg.getReceiveBufferSize());
                }
                if (cfg.isReuseAddressChanged()) {
                    setReuseAddress(cfg.isReuseAddress());
                }
                if (cfg.isSendBufferSizeChanged()) {
                    setSendBufferSize(cfg.getSendBufferSize());
                }
                if (cfg.isSoLingerChanged()) {
                    setSoLinger(cfg.getSoLinger());
                }
                if (cfg.isTcpNoDelayChanged()) {
                    setTcpNoDelay(cfg.isTcpNoDelay());
                }
                if (cfg.isTrafficClassChanged() && getTrafficClass() != cfg.getTrafficClass()) {
                    setTrafficClass(cfg.getTrafficClass());
                    return;
                }
                return;
            }
            SocketSessionConfig cfg2 = (SocketSessionConfig) config;
            setKeepAlive(cfg2.isKeepAlive());
            setOobInline(cfg2.isOobInline());
            setReceiveBufferSize(cfg2.getReceiveBufferSize());
            setReuseAddress(cfg2.isReuseAddress());
            setSendBufferSize(cfg2.getSendBufferSize());
            setSoLinger(cfg2.getSoLinger());
            setTcpNoDelay(cfg2.isTcpNoDelay());
            if (getTrafficClass() != cfg2.getTrafficClass()) {
                setTrafficClass(cfg2.getTrafficClass());
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isKeepAliveChanged() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isOobInlineChanged() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isReceiveBufferSizeChanged() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isReuseAddressChanged() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isSendBufferSizeChanged() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isSoLingerChanged() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isTcpNoDelayChanged() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isTrafficClassChanged() {
        return true;
    }
}
