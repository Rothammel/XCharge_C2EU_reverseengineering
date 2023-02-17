package org.apache.mina.handler.chain;

import org.apache.mina.core.session.IoSession;

/* loaded from: classes.dex */
public interface IoHandlerCommand {

    /* loaded from: classes.dex */
    public interface NextCommand {
        void execute(IoSession ioSession, Object obj) throws Exception;
    }

    void execute(NextCommand nextCommand, IoSession ioSession, Object obj) throws Exception;
}
