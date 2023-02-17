package org.apache.mina.core.filterchain;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

public interface IoFilter {

    public interface NextFilter {
        void exceptionCaught(IoSession ioSession, Throwable th);

        void filterClose(IoSession ioSession);

        void filterWrite(IoSession ioSession, WriteRequest writeRequest);

        void inputClosed(IoSession ioSession);

        void messageReceived(IoSession ioSession, Object obj);

        void messageSent(IoSession ioSession, WriteRequest writeRequest);

        void sessionClosed(IoSession ioSession);

        void sessionCreated(IoSession ioSession);

        void sessionIdle(IoSession ioSession, IdleStatus idleStatus);

        void sessionOpened(IoSession ioSession);
    }

    void destroy() throws Exception;

    void exceptionCaught(NextFilter nextFilter, IoSession ioSession, Throwable th) throws Exception;

    void filterClose(NextFilter nextFilter, IoSession ioSession) throws Exception;

    void filterWrite(NextFilter nextFilter, IoSession ioSession, WriteRequest writeRequest) throws Exception;

    void init() throws Exception;

    void inputClosed(NextFilter nextFilter, IoSession ioSession) throws Exception;

    void messageReceived(NextFilter nextFilter, IoSession ioSession, Object obj) throws Exception;

    void messageSent(NextFilter nextFilter, IoSession ioSession, WriteRequest writeRequest) throws Exception;

    void onPostAdd(IoFilterChain ioFilterChain, String str, NextFilter nextFilter) throws Exception;

    void onPostRemove(IoFilterChain ioFilterChain, String str, NextFilter nextFilter) throws Exception;

    void onPreAdd(IoFilterChain ioFilterChain, String str, NextFilter nextFilter) throws Exception;

    void onPreRemove(IoFilterChain ioFilterChain, String str, NextFilter nextFilter) throws Exception;

    void sessionClosed(NextFilter nextFilter, IoSession ioSession) throws Exception;

    void sessionCreated(NextFilter nextFilter, IoSession ioSession) throws Exception;

    void sessionIdle(NextFilter nextFilter, IoSession ioSession, IdleStatus idleStatus) throws Exception;

    void sessionOpened(NextFilter nextFilter, IoSession ioSession) throws Exception;
}
