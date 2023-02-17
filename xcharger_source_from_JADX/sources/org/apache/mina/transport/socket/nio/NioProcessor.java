package org.apache.mina.transport.socket.nio;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.file.FileRegion;
import org.apache.mina.core.polling.AbstractPollingIoProcessor;
import org.apache.mina.core.session.SessionState;

public final class NioProcessor extends AbstractPollingIoProcessor<NioSession> {
    private Selector selector;
    private SelectorProvider selectorProvider = null;

    public NioProcessor(Executor executor) {
        super(executor);
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeIoException("Failed to open a selector.", e);
        }
    }

    public NioProcessor(Executor executor, SelectorProvider selectorProvider2) {
        super(executor);
        if (selectorProvider2 == null) {
            try {
                this.selector = Selector.open();
            } catch (IOException e) {
                throw new RuntimeIoException("Failed to open a selector.", e);
            }
        } else {
            this.selector = selectorProvider2.openSelector();
        }
    }

    /* access modifiers changed from: protected */
    public void doDispose() throws Exception {
        this.selector.close();
    }

    /* access modifiers changed from: protected */
    public int select(long timeout) throws Exception {
        return this.selector.select(timeout);
    }

    /* access modifiers changed from: protected */
    public int select() throws Exception {
        return this.selector.select();
    }

    /* access modifiers changed from: protected */
    public boolean isSelectorEmpty() {
        return this.selector.keys().isEmpty();
    }

    /* access modifiers changed from: protected */
    public void wakeup() {
        this.wakeupCalled.getAndSet(true);
        this.selector.wakeup();
    }

    /* access modifiers changed from: protected */
    public Iterator<NioSession> allSessions() {
        return new IoSessionIterator(this.selector.keys());
    }

    /* access modifiers changed from: protected */
    public Iterator<NioSession> selectedSessions() {
        return new IoSessionIterator(this.selector.selectedKeys());
    }

    /* access modifiers changed from: protected */
    public void init(NioSession session) throws Exception {
        SelectableChannel ch = (SelectableChannel) session.getChannel();
        ch.configureBlocking(false);
        session.setSelectionKey(ch.register(this.selector, 1, session));
    }

    /* access modifiers changed from: protected */
    public void destroy(NioSession session) throws Exception {
        ByteChannel ch = session.getChannel();
        SelectionKey key = session.getSelectionKey();
        if (key != null) {
            key.cancel();
        }
        if (ch.isOpen()) {
            ch.close();
        }
    }

    /* access modifiers changed from: protected */
    public void registerNewSelector() throws IOException {
        Selector newSelector;
        synchronized (this.selector) {
            Set<SelectionKey> keys = this.selector.keys();
            if (this.selectorProvider == null) {
                newSelector = Selector.open();
            } else {
                newSelector = this.selectorProvider.openSelector();
            }
            for (SelectionKey key : keys) {
                SelectableChannel ch = key.channel();
                NioSession session = (NioSession) key.attachment();
                session.setSelectionKey(ch.register(newSelector, key.interestOps(), session));
            }
            this.selector.close();
            this.selector = newSelector;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isBrokenConnection() throws IOException {
        boolean brokenSession = false;
        synchronized (this.selector) {
            for (SelectionKey key : this.selector.keys()) {
                SelectableChannel channel = key.channel();
                if (((channel instanceof DatagramChannel) && !((DatagramChannel) channel).isConnected()) || ((channel instanceof SocketChannel) && !((SocketChannel) channel).isConnected())) {
                    key.cancel();
                    brokenSession = true;
                }
            }
        }
        return brokenSession;
    }

    /* access modifiers changed from: protected */
    public SessionState getState(NioSession session) {
        SelectionKey key = session.getSelectionKey();
        if (key == null) {
            return SessionState.OPENING;
        }
        if (key.isValid()) {
            return SessionState.OPENED;
        }
        return SessionState.CLOSING;
    }

    /* access modifiers changed from: protected */
    public boolean isReadable(NioSession session) {
        SelectionKey key = session.getSelectionKey();
        return key != null && key.isValid() && key.isReadable();
    }

    /* access modifiers changed from: protected */
    public boolean isWritable(NioSession session) {
        SelectionKey key = session.getSelectionKey();
        return key != null && key.isValid() && key.isWritable();
    }

    /* access modifiers changed from: protected */
    public boolean isInterestedInRead(NioSession session) {
        SelectionKey key = session.getSelectionKey();
        return (key == null || !key.isValid() || (key.interestOps() & 1) == 0) ? false : true;
    }

    /* access modifiers changed from: protected */
    public boolean isInterestedInWrite(NioSession session) {
        SelectionKey key = session.getSelectionKey();
        return (key == null || !key.isValid() || (key.interestOps() & 4) == 0) ? false : true;
    }

    /* access modifiers changed from: protected */
    public void setInterestedInRead(NioSession session, boolean isInterested) throws Exception {
        int newInterestOps;
        SelectionKey key = session.getSelectionKey();
        if (key != null && key.isValid()) {
            int oldInterestOps = key.interestOps();
            int newInterestOps2 = oldInterestOps;
            if (isInterested) {
                newInterestOps = newInterestOps2 | 1;
            } else {
                newInterestOps = newInterestOps2 & -2;
            }
            if (oldInterestOps != newInterestOps) {
                key.interestOps(newInterestOps);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setInterestedInWrite(NioSession session, boolean isInterested) throws Exception {
        int newInterestOps;
        SelectionKey key = session.getSelectionKey();
        if (key != null && key.isValid()) {
            int newInterestOps2 = key.interestOps();
            if (isInterested) {
                newInterestOps = newInterestOps2 | 4;
            } else {
                newInterestOps = newInterestOps2 & -5;
            }
            key.interestOps(newInterestOps);
        }
    }

    /* access modifiers changed from: protected */
    public int read(NioSession session, IoBuffer buf) throws Exception {
        return session.getChannel().read(buf.buf());
    }

    /* access modifiers changed from: protected */
    public int write(NioSession session, IoBuffer buf, int length) throws IOException {
        if (buf.remaining() <= length) {
            return session.getChannel().write(buf.buf());
        }
        int oldLimit = buf.limit();
        buf.limit(buf.position() + length);
        try {
            return session.getChannel().write(buf.buf());
        } finally {
            buf.limit(oldLimit);
        }
    }

    /* access modifiers changed from: protected */
    public int transferFile(NioSession session, FileRegion region, int length) throws Exception {
        try {
            return (int) region.getFileChannel().transferTo(region.getPosition(), (long) length, session.getChannel());
        } catch (IOException e) {
            String message = e.getMessage();
            if (message != null && message.contains("temporarily unavailable")) {
                return 0;
            }
            throw e;
        }
    }

    protected static class IoSessionIterator<NioSession> implements Iterator<NioSession> {
        private final Iterator<SelectionKey> iterator;

        private IoSessionIterator(Set<SelectionKey> keys) {
            this.iterator = keys.iterator();
        }

        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        public NioSession next() {
            return this.iterator.next().attachment();
        }

        public void remove() {
            this.iterator.remove();
        }
    }
}
