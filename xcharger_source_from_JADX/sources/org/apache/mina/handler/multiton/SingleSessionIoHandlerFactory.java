package org.apache.mina.handler.multiton;

import org.apache.mina.core.session.IoSession;

@Deprecated
public interface SingleSessionIoHandlerFactory {
    SingleSessionIoHandler getHandler(IoSession ioSession) throws Exception;
}
