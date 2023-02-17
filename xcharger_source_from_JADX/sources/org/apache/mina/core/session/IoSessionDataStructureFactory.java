package org.apache.mina.core.session;

import org.apache.mina.core.write.WriteRequestQueue;

public interface IoSessionDataStructureFactory {
    IoSessionAttributeMap getAttributeMap(IoSession ioSession) throws Exception;

    WriteRequestQueue getWriteRequestQueue(IoSession ioSession) throws Exception;
}
