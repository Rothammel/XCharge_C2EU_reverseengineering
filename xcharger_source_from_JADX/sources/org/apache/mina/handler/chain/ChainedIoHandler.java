package org.apache.mina.handler.chain;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.chain.IoHandlerCommand;

public class ChainedIoHandler extends IoHandlerAdapter {
    private final IoHandlerChain chain;

    public ChainedIoHandler() {
        this.chain = new IoHandlerChain();
    }

    public ChainedIoHandler(IoHandlerChain chain2) {
        if (chain2 == null) {
            throw new IllegalArgumentException("chain");
        }
        this.chain = chain2;
    }

    public IoHandlerChain getChain() {
        return this.chain;
    }

    public void messageReceived(IoSession session, Object message) throws Exception {
        this.chain.execute((IoHandlerCommand.NextCommand) null, session, message);
    }
}
