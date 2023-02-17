package org.apache.mina.filter.codec;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.mina.core.buffer.IoBuffer;

public abstract class AbstractProtocolEncoderOutput implements ProtocolEncoderOutput {
    private boolean buffersOnly = true;
    private final Queue<Object> messageQueue = new ConcurrentLinkedQueue();

    public Queue<Object> getMessageQueue() {
        return this.messageQueue;
    }

    public void write(Object encodedMessage) {
        if (encodedMessage instanceof IoBuffer) {
            IoBuffer buf = (IoBuffer) encodedMessage;
            if (buf.hasRemaining()) {
                this.messageQueue.offer(buf);
                return;
            }
            throw new IllegalArgumentException("buf is empty. Forgot to call flip()?");
        }
        this.messageQueue.offer(encodedMessage);
        this.buffersOnly = false;
    }

    public void mergeAll() {
        if (!this.buffersOnly) {
            throw new IllegalStateException("the encoded message list contains a non-buffer.");
        } else if (this.messageQueue.size() >= 2) {
            int sum = 0;
            Iterator it = this.messageQueue.iterator();
            while (it.hasNext()) {
                sum += ((IoBuffer) it.next()).remaining();
            }
            IoBuffer newBuf = IoBuffer.allocate(sum);
            while (true) {
                IoBuffer buf = (IoBuffer) this.messageQueue.poll();
                if (buf == null) {
                    newBuf.flip();
                    this.messageQueue.add(newBuf);
                    return;
                }
                newBuf.put(buf);
            }
        }
    }
}
