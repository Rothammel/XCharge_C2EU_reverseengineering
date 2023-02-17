package org.apache.mina.transport.socket;

import org.apache.mina.core.session.AbstractIoSessionConfig;
import org.apache.mina.core.session.IoSessionConfig;

/* loaded from: classes.dex */
public abstract class AbstractDatagramSessionConfig extends AbstractIoSessionConfig implements DatagramSessionConfig {
    private boolean closeOnPortUnreachable = true;

    @Override // org.apache.mina.core.session.AbstractIoSessionConfig, org.apache.mina.core.session.IoSessionConfig
    public void setAll(IoSessionConfig config) {
        super.setAll(config);
        if (config instanceof DatagramSessionConfig) {
            if (config instanceof AbstractDatagramSessionConfig) {
                AbstractDatagramSessionConfig cfg = (AbstractDatagramSessionConfig) config;
                if (cfg.isBroadcastChanged()) {
                    setBroadcast(cfg.isBroadcast());
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
                if (cfg.isTrafficClassChanged() && getTrafficClass() != cfg.getTrafficClass()) {
                    setTrafficClass(cfg.getTrafficClass());
                    return;
                }
                return;
            }
            DatagramSessionConfig cfg2 = (DatagramSessionConfig) config;
            setBroadcast(cfg2.isBroadcast());
            setReceiveBufferSize(cfg2.getReceiveBufferSize());
            setReuseAddress(cfg2.isReuseAddress());
            setSendBufferSize(cfg2.getSendBufferSize());
            if (getTrafficClass() != cfg2.getTrafficClass()) {
                setTrafficClass(cfg2.getTrafficClass());
            }
        }
    }

    protected boolean isBroadcastChanged() {
        return true;
    }

    protected boolean isReceiveBufferSizeChanged() {
        return true;
    }

    protected boolean isReuseAddressChanged() {
        return true;
    }

    protected boolean isSendBufferSizeChanged() {
        return true;
    }

    protected boolean isTrafficClassChanged() {
        return true;
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public boolean isCloseOnPortUnreachable() {
        return this.closeOnPortUnreachable;
    }

    @Override // org.apache.mina.transport.socket.DatagramSessionConfig
    public void setCloseOnPortUnreachable(boolean closeOnPortUnreachable) {
        this.closeOnPortUnreachable = closeOnPortUnreachable;
    }
}