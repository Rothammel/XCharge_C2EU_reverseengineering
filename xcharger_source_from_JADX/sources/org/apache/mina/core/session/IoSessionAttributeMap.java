package org.apache.mina.core.session;

import java.util.Set;

public interface IoSessionAttributeMap {
    boolean containsAttribute(IoSession ioSession, Object obj);

    void dispose(IoSession ioSession) throws Exception;

    Object getAttribute(IoSession ioSession, Object obj, Object obj2);

    Set<Object> getAttributeKeys(IoSession ioSession);

    Object removeAttribute(IoSession ioSession, Object obj);

    boolean removeAttribute(IoSession ioSession, Object obj, Object obj2);

    boolean replaceAttribute(IoSession ioSession, Object obj, Object obj2, Object obj3);

    Object setAttribute(IoSession ioSession, Object obj, Object obj2);

    Object setAttributeIfAbsent(IoSession ioSession, Object obj, Object obj2);
}
