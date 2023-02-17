package org.apache.mina.transport.socket;

import org.apache.mina.core.session.AbstractIoSessionConfig;
import org.apache.mina.core.session.IoSessionConfig;

public abstract class AbstractDatagramSessionConfig extends AbstractIoSessionConfig implements DatagramSessionConfig {
    private boolean closeOnPortUnreachable = true;

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

    /* access modifiers changed from: protected */
    public boolean isBroadcastChanged() {
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
    public boolean isTrafficClassChanged() {
        return true;
    }

    public boolean isCloseOnPortUnreachable() {
        return this.closeOnPortUnreachable;
    }

    public void setCloseOnPortUnreachable(boolean closeOnPortUnreachable2) {
        this.closeOnPortUnreachable = closeOnPortUnreachable2;
    }
}
