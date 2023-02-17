package org.apache.mina.handler.multiton;

import org.apache.mina.core.session.IoSession;

@Deprecated
/* loaded from: classes.dex */
public interface SingleSessionIoHandlerFactory {
    SingleSessionIoHandler getHandler(IoSession ioSession) throws Exception;
}