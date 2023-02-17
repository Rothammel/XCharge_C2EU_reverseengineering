package org.apache.mina.filter.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;

public abstract class CumulativeProtocolDecoder extends ProtocolDecoderAdapter {
    private final AttributeKey BUFFER = new AttributeKey(getClass(), "buffer");
    private boolean transportMetadataFragmentation = true;

    /* access modifiers changed from: protected */
    public abstract boolean doDecode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception;

    protected CumulativeProtocolDecoder() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x000e A[LOOP:0: B:4:0x000e->B:7:0x0018, LOOP_START] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void decode(org.apache.mina.core.session.IoSession r9, org.apache.mina.core.buffer.IoBuffer r10, org.apache.mina.filter.codec.ProtocolDecoderOutput r11) throws java.lang.Exception {
        /*
            r8 = this;
            boolean r6 = r8.transportMetadataFragmentation
            if (r6 == 0) goto L_0x001b
            org.apache.mina.core.service.TransportMetadata r6 = r9.getTransportMetadata()
            boolean r6 = r6.hasFragmentation()
            if (r6 != 0) goto L_0x001b
        L_0x000e:
            boolean r6 = r10.hasRemaining()
            if (r6 == 0) goto L_0x001a
            boolean r6 = r8.doDecode(r9, r10, r11)
            if (r6 != 0) goto L_0x000e
        L_0x001a:
            return
        L_0x001b:
            r5 = 1
            org.apache.mina.core.session.AttributeKey r6 = r8.BUFFER
            java.lang.Object r1 = r9.getAttribute(r6)
            org.apache.mina.core.buffer.IoBuffer r1 = (org.apache.mina.core.buffer.IoBuffer) r1
            if (r1 == 0) goto L_0x007a
            r0 = 0
            boolean r6 = r1.isAutoExpand()
            if (r6 == 0) goto L_0x0031
            r1.put((org.apache.mina.core.buffer.IoBuffer) r10)     // Catch:{ IllegalStateException -> 0x00a0, IndexOutOfBoundsException -> 0x00a2 }
            r0 = 1
        L_0x0031:
            if (r0 == 0) goto L_0x004e
            r1.flip()
        L_0x0036:
            int r4 = r1.position()
            boolean r2 = r8.doDecode(r9, r1, r11)
            if (r2 == 0) goto L_0x0083
            int r6 = r1.position()
            if (r6 != r4) goto L_0x007d
            java.lang.IllegalStateException r6 = new java.lang.IllegalStateException
            java.lang.String r7 = "doDecode() can't return true when buffer is not consumed."
            r6.<init>(r7)
            throw r6
        L_0x004e:
            r1.flip()
            int r6 = r1.remaining()
            int r7 = r10.remaining()
            int r6 = r6 + r7
            org.apache.mina.core.buffer.IoBuffer r6 = org.apache.mina.core.buffer.IoBuffer.allocate(r6)
            r7 = 1
            org.apache.mina.core.buffer.IoBuffer r3 = r6.setAutoExpand(r7)
            java.nio.ByteOrder r6 = r1.order()
            r3.order(r6)
            r3.put((org.apache.mina.core.buffer.IoBuffer) r1)
            r3.put((org.apache.mina.core.buffer.IoBuffer) r10)
            r3.flip()
            r1 = r3
            org.apache.mina.core.session.AttributeKey r6 = r8.BUFFER
            r9.setAttribute(r6, r1)
            goto L_0x0036
        L_0x007a:
            r1 = r10
            r5 = 0
            goto L_0x0036
        L_0x007d:
            boolean r6 = r1.hasRemaining()
            if (r6 != 0) goto L_0x0036
        L_0x0083:
            boolean r6 = r1.hasRemaining()
            if (r6 == 0) goto L_0x0099
            if (r5 == 0) goto L_0x0095
            boolean r6 = r1.isAutoExpand()
            if (r6 == 0) goto L_0x0095
            r1.compact()
            goto L_0x001a
        L_0x0095:
            r8.storeRemainingInSession(r1, r9)
            goto L_0x001a
        L_0x0099:
            if (r5 == 0) goto L_0x001a
            r8.removeSessionBuffer(r9)
            goto L_0x001a
        L_0x00a0:
            r6 = move-exception
            goto L_0x0031
        L_0x00a2:
            r6 = move-exception
            goto L_0x0031
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.filter.codec.CumulativeProtocolDecoder.decode(org.apache.mina.core.session.IoSession, org.apache.mina.core.buffer.IoBuffer, org.apache.mina.filter.codec.ProtocolDecoderOutput):void");
    }

    public void dispose(IoSession session) throws Exception {
        removeSessionBuffer(session);
    }

    private void removeSessionBuffer(IoSession session) {
        session.removeAttribute(this.BUFFER);
    }

    private void storeRemainingInSession(IoBuffer buf, IoSession session) {
        IoBuffer remainingBuf = IoBuffer.allocate(buf.capacity()).setAutoExpand(true);
        remainingBuf.order(buf.order());
        remainingBuf.put(buf);
        session.setAttribute(this.BUFFER, remainingBuf);
    }

    public void setTransportMetadataFragmentation(boolean transportMetadataFragmentation2) {
        this.transportMetadataFragmentation = transportMetadataFragmentation2;
    }
}
