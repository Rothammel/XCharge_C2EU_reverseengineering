package org.apache.mina.handler.chain;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

/* loaded from: classes.dex */
public class ChainedIoHandler extends IoHandlerAdapter {
    private final IoHandlerChain chain;

    public ChainedIoHandler() {
        this.chain = new IoHandlerChain();
    }

    public ChainedIoHandler(IoHandlerChain chain) {
        if (chain == null) {
            throw new IllegalArgumentException("chain");
        }
        this.chain = chain;
    }

    public IoHandlerChain getChain() {
        return this.chain;
    }

    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void messageReceived(IoSession session, Object message) throws Exception {
        this.chain.execute(null, session, message);
    }
}
