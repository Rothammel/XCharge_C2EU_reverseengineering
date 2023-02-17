package org.apache.mina.core.filterchain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class DefaultIoFilterChain implements IoFilterChain {
    private final EntryImpl head;
    private final Map<String, IoFilterChain.Entry> name2entry = new ConcurrentHashMap();
    private final AbstractIoSession session;
    private final EntryImpl tail;
    public static final AttributeKey SESSION_CREATED_FUTURE = new AttributeKey(DefaultIoFilterChain.class, "connectFuture");
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultIoFilterChain.class);

    public DefaultIoFilterChain(AbstractIoSession session) {
        if (session == null) {
            throw new IllegalArgumentException("session");
        }
        this.session = session;
        this.head = new EntryImpl(null, null, "head", new HeadFilter());
        this.tail = new EntryImpl(this.head, null, "tail", new TailFilter());
        this.head.nextEntry = this.tail;
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public IoSession getSession() {
        return this.session;
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public IoFilterChain.Entry getEntry(String name) {
        IoFilterChain.Entry e = this.name2entry.get(name);
        if (e == null) {
            return null;
        }
        return e;
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public IoFilterChain.Entry getEntry(IoFilter filter) {
        for (EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
            if (e.getFilter() == filter) {
                return e;
            }
        }
        return null;
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public IoFilterChain.Entry getEntry(Class<? extends IoFilter> filterType) {
        for (EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
            if (filterType.isAssignableFrom(e.getFilter().getClass())) {
                return e;
            }
        }
        return null;
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public IoFilter get(String name) {
        IoFilterChain.Entry e = getEntry(name);
        if (e == null) {
            return null;
        }
        return e.getFilter();
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public IoFilter get(Class<? extends IoFilter> filterType) {
        IoFilterChain.Entry e = getEntry(filterType);
        if (e == null) {
            return null;
        }
        return e.getFilter();
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public IoFilter.NextFilter getNextFilter(String name) {
        IoFilterChain.Entry e = getEntry(name);
        if (e == null) {
            return null;
        }
        return e.getNextFilter();
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public IoFilter.NextFilter getNextFilter(IoFilter filter) {
        IoFilterChain.Entry e = getEntry(filter);
        if (e == null) {
            return null;
        }
        return e.getNextFilter();
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public IoFilter.NextFilter getNextFilter(Class<? extends IoFilter> filterType) {
        IoFilterChain.Entry e = getEntry(filterType);
        if (e == null) {
            return null;
        }
        return e.getNextFilter();
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public synchronized void addFirst(String name, IoFilter filter) {
        checkAddable(name);
        register(this.head, name, filter);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public synchronized void addLast(String name, IoFilter filter) {
        checkAddable(name);
        register(this.tail.prevEntry, name, filter);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public synchronized void addBefore(String baseName, String name, IoFilter filter) {
        EntryImpl baseEntry = checkOldName(baseName);
        checkAddable(name);
        register(baseEntry.prevEntry, name, filter);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public synchronized void addAfter(String baseName, String name, IoFilter filter) {
        EntryImpl baseEntry = checkOldName(baseName);
        checkAddable(name);
        register(baseEntry, name, filter);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public synchronized IoFilter remove(String name) {
        EntryImpl entry;
        entry = checkOldName(name);
        deregister(entry);
        return entry.getFilter();
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public synchronized void remove(IoFilter filter) {
        for (EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
            if (e.getFilter() == filter) {
                deregister(e);
            }
        }
        throw new IllegalArgumentException("Filter not found: " + filter.getClass().getName());
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public synchronized IoFilter remove(Class<? extends IoFilter> filterType) {
        IoFilter oldFilter;
        for (EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
            if (filterType.isAssignableFrom(e.getFilter().getClass())) {
                oldFilter = e.getFilter();
                deregister(e);
            }
        }
        throw new IllegalArgumentException("Filter not found: " + filterType.getName());
        return oldFilter;
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public synchronized IoFilter replace(String name, IoFilter newFilter) {
        IoFilter oldFilter;
        EntryImpl entry = checkOldName(name);
        oldFilter = entry.getFilter();
        try {
            newFilter.onPreAdd(this, name, entry.getNextFilter());
            entry.setFilter(newFilter);
            try {
                newFilter.onPostAdd(this, name, entry.getNextFilter());
            } catch (Exception e) {
                entry.setFilter(oldFilter);
                throw new IoFilterLifeCycleException("onPostAdd(): " + name + ':' + newFilter + " in " + getSession(), e);
            }
        } catch (Exception e2) {
            throw new IoFilterLifeCycleException("onPreAdd(): " + name + ':' + newFilter + " in " + getSession(), e2);
        }
        return oldFilter;
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public synchronized void replace(IoFilter oldFilter, IoFilter newFilter) {
        for (EntryImpl entry = this.head.nextEntry; entry != this.tail; entry = entry.nextEntry) {
            if (entry.getFilter() == oldFilter) {
                String oldFilterName = null;
                for (Map.Entry<String, IoFilterChain.Entry> mapping : this.name2entry.entrySet()) {
                    if (entry == mapping.getValue()) {
                        String oldFilterName2 = mapping.getKey();
                        oldFilterName = oldFilterName2;
                        break;
                    }
                }
                try {
                    newFilter.onPreAdd(this, oldFilterName, entry.getNextFilter());
                    entry.setFilter(newFilter);
                    try {
                        newFilter.onPostAdd(this, oldFilterName, entry.getNextFilter());
                    } catch (Exception e) {
                        entry.setFilter(oldFilter);
                        throw new IoFilterLifeCycleException("onPostAdd(): " + oldFilterName + ':' + newFilter + " in " + getSession(), e);
                    }
                } catch (Exception e2) {
                    throw new IoFilterLifeCycleException("onPreAdd(): " + oldFilterName + ':' + newFilter + " in " + getSession(), e2);
                }
            }
        }
        throw new IllegalArgumentException("Filter not found: " + oldFilter.getClass().getName());
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public synchronized IoFilter replace(Class<? extends IoFilter> oldFilterType, IoFilter newFilter) {
        IoFilter oldFilter;
        for (EntryImpl entry = this.head.nextEntry; entry != this.tail; entry = entry.nextEntry) {
            if (oldFilterType.isAssignableFrom(entry.getFilter().getClass())) {
                oldFilter = entry.getFilter();
                String oldFilterName = null;
                for (Map.Entry<String, IoFilterChain.Entry> mapping : this.name2entry.entrySet()) {
                    if (entry == mapping.getValue()) {
                        String oldFilterName2 = mapping.getKey();
                        oldFilterName = oldFilterName2;
                        break;
                    }
                }
                try {
                    newFilter.onPreAdd(this, oldFilterName, entry.getNextFilter());
                    entry.setFilter(newFilter);
                    try {
                        newFilter.onPostAdd(this, oldFilterName, entry.getNextFilter());
                    } catch (Exception e) {
                        entry.setFilter(oldFilter);
                        throw new IoFilterLifeCycleException("onPostAdd(): " + oldFilterName + ':' + newFilter + " in " + getSession(), e);
                    }
                } catch (Exception e2) {
                    throw new IoFilterLifeCycleException("onPreAdd(): " + oldFilterName + ':' + newFilter + " in " + getSession(), e2);
                }
            }
        }
        throw new IllegalArgumentException("Filter not found: " + oldFilterType.getName());
        return oldFilter;
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public synchronized void clear() throws Exception {
        List<IoFilterChain.Entry> l = new ArrayList<>(this.name2entry.values());
        for (IoFilterChain.Entry entry : l) {
            try {
                deregister((EntryImpl) entry);
            } catch (Exception e) {
                throw new IoFilterLifeCycleException("clear(): " + entry.getName() + " in " + getSession(), e);
            }
        }
    }

    private void register(EntryImpl prevEntry, String name, IoFilter filter) {
        EntryImpl newEntry = new EntryImpl(prevEntry, prevEntry.nextEntry, name, filter);
        try {
            filter.onPreAdd(this, name, newEntry.getNextFilter());
            prevEntry.nextEntry.prevEntry = newEntry;
            prevEntry.nextEntry = newEntry;
            this.name2entry.put(name, newEntry);
            try {
                filter.onPostAdd(this, name, newEntry.getNextFilter());
            } catch (Exception e) {
                deregister0(newEntry);
                throw new IoFilterLifeCycleException("onPostAdd(): " + name + ':' + filter + " in " + getSession(), e);
            }
        } catch (Exception e2) {
            throw new IoFilterLifeCycleException("onPreAdd(): " + name + ':' + filter + " in " + getSession(), e2);
        }
    }

    private void deregister(EntryImpl entry) {
        IoFilter filter = entry.getFilter();
        try {
            filter.onPreRemove(this, entry.getName(), entry.getNextFilter());
            deregister0(entry);
            try {
                filter.onPostRemove(this, entry.getName(), entry.getNextFilter());
            } catch (Exception e) {
                throw new IoFilterLifeCycleException("onPostRemove(): " + entry.getName() + ':' + filter + " in " + getSession(), e);
            }
        } catch (Exception e2) {
            throw new IoFilterLifeCycleException("onPreRemove(): " + entry.getName() + ':' + filter + " in " + getSession(), e2);
        }
    }

    private void deregister0(EntryImpl entry) {
        EntryImpl prevEntry = entry.prevEntry;
        EntryImpl nextEntry = entry.nextEntry;
        prevEntry.nextEntry = nextEntry;
        nextEntry.prevEntry = prevEntry;
        this.name2entry.remove(entry.name);
    }

    private EntryImpl checkOldName(String baseName) {
        EntryImpl e = (EntryImpl) this.name2entry.get(baseName);
        if (e == null) {
            throw new IllegalArgumentException("Filter not found:" + baseName);
        }
        return e;
    }

    private void checkAddable(String name) {
        if (this.name2entry.containsKey(name)) {
            throw new IllegalArgumentException("Other filter is using the same name '" + name + "'");
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public void fireSessionCreated() {
        callNextSessionCreated(this.head, this.session);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callNextSessionCreated(IoFilterChain.Entry entry, IoSession session) {
        try {
            IoFilter filter = entry.getFilter();
            IoFilter.NextFilter nextFilter = entry.getNextFilter();
            filter.sessionCreated(nextFilter, session);
        } catch (Error e) {
            fireExceptionCaught(e);
            throw e;
        } catch (Exception e2) {
            fireExceptionCaught(e2);
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public void fireSessionOpened() {
        callNextSessionOpened(this.head, this.session);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callNextSessionOpened(IoFilterChain.Entry entry, IoSession session) {
        try {
            IoFilter filter = entry.getFilter();
            IoFilter.NextFilter nextFilter = entry.getNextFilter();
            filter.sessionOpened(nextFilter, session);
        } catch (Error e) {
            fireExceptionCaught(e);
            throw e;
        } catch (Exception e2) {
            fireExceptionCaught(e2);
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public void fireSessionClosed() {
        try {
            this.session.getCloseFuture().setClosed();
        } catch (Error e) {
            fireExceptionCaught(e);
            throw e;
        } catch (Exception e2) {
            fireExceptionCaught(e2);
        }
        callNextSessionClosed(this.head, this.session);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callNextSessionClosed(IoFilterChain.Entry entry, IoSession session) {
        try {
            IoFilter filter = entry.getFilter();
            IoFilter.NextFilter nextFilter = entry.getNextFilter();
            filter.sessionClosed(nextFilter, session);
        } catch (Error e) {
            fireExceptionCaught(e);
        } catch (Exception e2) {
            fireExceptionCaught(e2);
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public void fireSessionIdle(IdleStatus status) {
        this.session.increaseIdleCount(status, System.currentTimeMillis());
        callNextSessionIdle(this.head, this.session, status);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callNextSessionIdle(IoFilterChain.Entry entry, IoSession session, IdleStatus status) {
        try {
            IoFilter filter = entry.getFilter();
            IoFilter.NextFilter nextFilter = entry.getNextFilter();
            filter.sessionIdle(nextFilter, session, status);
        } catch (Error e) {
            fireExceptionCaught(e);
            throw e;
        } catch (Exception e2) {
            fireExceptionCaught(e2);
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public void fireMessageReceived(Object message) {
        if (message instanceof IoBuffer) {
            this.session.increaseReadBytes(((IoBuffer) message).remaining(), System.currentTimeMillis());
        }
        callNextMessageReceived(this.head, this.session, message);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callNextMessageReceived(IoFilterChain.Entry entry, IoSession session, Object message) {
        try {
            IoFilter filter = entry.getFilter();
            IoFilter.NextFilter nextFilter = entry.getNextFilter();
            filter.messageReceived(nextFilter, session, message);
        } catch (Error e) {
            fireExceptionCaught(e);
            throw e;
        } catch (Exception e2) {
            fireExceptionCaught(e2);
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public void fireMessageSent(WriteRequest request) {
        try {
            request.getFuture().setWritten();
        } catch (Error e) {
            fireExceptionCaught(e);
            throw e;
        } catch (Exception e2) {
            fireExceptionCaught(e2);
        }
        if (!request.isEncoded()) {
            callNextMessageSent(this.head, this.session, request);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callNextMessageSent(IoFilterChain.Entry entry, IoSession session, WriteRequest writeRequest) {
        try {
            IoFilter filter = entry.getFilter();
            IoFilter.NextFilter nextFilter = entry.getNextFilter();
            filter.messageSent(nextFilter, session, writeRequest);
        } catch (Error e) {
            fireExceptionCaught(e);
            throw e;
        } catch (Exception e2) {
            fireExceptionCaught(e2);
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public void fireExceptionCaught(Throwable cause) {
        callNextExceptionCaught(this.head, this.session, cause);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callNextExceptionCaught(IoFilterChain.Entry entry, IoSession session, Throwable cause) {
        ConnectFuture future = (ConnectFuture) session.removeAttribute(SESSION_CREATED_FUTURE);
        if (future == null) {
            try {
                IoFilter filter = entry.getFilter();
                IoFilter.NextFilter nextFilter = entry.getNextFilter();
                filter.exceptionCaught(nextFilter, session, cause);
                return;
            } catch (Throwable e) {
                LOGGER.warn("Unexpected exception from exceptionCaught handler.", e);
                return;
            }
        }
        if (!session.isClosing()) {
            session.closeNow();
        }
        future.setException(cause);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public void fireInputClosed() {
        IoFilterChain.Entry head = this.head;
        callNextInputClosed(head, this.session);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callNextInputClosed(IoFilterChain.Entry entry, IoSession session) {
        try {
            IoFilter filter = entry.getFilter();
            IoFilter.NextFilter nextFilter = entry.getNextFilter();
            filter.inputClosed(nextFilter, session);
        } catch (Throwable e) {
            fireExceptionCaught(e);
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public void fireFilterWrite(WriteRequest writeRequest) {
        callPreviousFilterWrite(this.tail, this.session, writeRequest);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callPreviousFilterWrite(IoFilterChain.Entry entry, IoSession session, WriteRequest writeRequest) {
        try {
            IoFilter filter = entry.getFilter();
            IoFilter.NextFilter nextFilter = entry.getNextFilter();
            filter.filterWrite(nextFilter, session, writeRequest);
        } catch (Error e) {
            writeRequest.getFuture().setException(e);
            fireExceptionCaught(e);
            throw e;
        } catch (Exception e2) {
            writeRequest.getFuture().setException(e2);
            fireExceptionCaught(e2);
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public void fireFilterClose() {
        callPreviousFilterClose(this.tail, this.session);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callPreviousFilterClose(IoFilterChain.Entry entry, IoSession session) {
        try {
            IoFilter filter = entry.getFilter();
            IoFilter.NextFilter nextFilter = entry.getNextFilter();
            filter.filterClose(nextFilter, session);
        } catch (Error e) {
            fireExceptionCaught(e);
            throw e;
        } catch (Exception e2) {
            fireExceptionCaught(e2);
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public List<IoFilterChain.Entry> getAll() {
        List<IoFilterChain.Entry> list = new ArrayList<>();
        for (EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
            list.add(e);
        }
        return list;
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public List<IoFilterChain.Entry> getAllReversed() {
        List<IoFilterChain.Entry> list = new ArrayList<>();
        for (EntryImpl e = this.tail.prevEntry; e != this.head; e = e.prevEntry) {
            list.add(e);
        }
        return list;
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public boolean contains(String name) {
        return getEntry(name) != null;
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public boolean contains(IoFilter filter) {
        return getEntry(filter) != null;
    }

    @Override // org.apache.mina.core.filterchain.IoFilterChain
    public boolean contains(Class<? extends IoFilter> filterType) {
        return getEntry(filterType) != null;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{ ");
        boolean empty = true;
        for (EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
            if (!empty) {
                buf.append(", ");
            } else {
                empty = false;
            }
            buf.append('(');
            buf.append(e.getName());
            buf.append(':');
            buf.append(e.getFilter());
            buf.append(')');
        }
        if (empty) {
            buf.append("empty");
        }
        buf.append(" }");
        return buf.toString();
    }

    /* loaded from: classes.dex */
    private class HeadFilter extends IoFilterAdapter {
        private HeadFilter() {
        }

        @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
        public void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
            AbstractIoSession s = (AbstractIoSession) session;
            if (writeRequest.getMessage() instanceof IoBuffer) {
                IoBuffer buffer = (IoBuffer) writeRequest.getMessage();
                buffer.mark();
                int remaining = buffer.remaining();
                if (remaining > 0) {
                    s.increaseScheduledWriteBytes(remaining);
                }
            } else {
                s.increaseScheduledWriteMessages();
            }
            WriteRequestQueue writeRequestQueue = s.getWriteRequestQueue();
            if (!s.isWriteSuspended()) {
                if (writeRequestQueue.isEmpty(session)) {
                    s.getProcessor().write(s, writeRequest);
                    return;
                }
                s.getWriteRequestQueue().offer(s, writeRequest);
                s.getProcessor().flush(s);
                return;
            }
            s.getWriteRequestQueue().offer(s, writeRequest);
        }

        @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
        public void filterClose(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
            ((AbstractIoSession) session).getProcessor().remove(session);
        }
    }

    /* loaded from: classes.dex */
    private static class TailFilter extends IoFilterAdapter {
        private TailFilter() {
        }

        @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
        public void sessionCreated(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
            try {
                session.getHandler().sessionCreated(session);
            } finally {
                ConnectFuture future = (ConnectFuture) session.removeAttribute(DefaultIoFilterChain.SESSION_CREATED_FUTURE);
                if (future != null) {
                    future.setSession(session);
                }
            }
        }

        @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
        public void sessionOpened(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
            session.getHandler().sessionOpened(session);
        }

        @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
        public void sessionClosed(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
            AbstractIoSession s = (AbstractIoSession) session;
            try {
                s.getHandler().sessionClosed(session);
                try {
                    s.getWriteRequestQueue().dispose(session);
                    try {
                        s.getAttributeMap().dispose(session);
                        try {
                            session.getFilterChain().clear();
                        } finally {
                            if (s.getConfig().isUseReadOperation()) {
                                s.offerClosedReadFuture();
                            }
                        }
                    } catch (Throwable th) {
                        try {
                            session.getFilterChain().clear();
                            if (s.getConfig().isUseReadOperation()) {
                                s.offerClosedReadFuture();
                            }
                            throw th;
                        } finally {
                            if (s.getConfig().isUseReadOperation()) {
                                s.offerClosedReadFuture();
                            }
                        }
                    }
                } catch (Throwable th2) {
                    try {
                        s.getAttributeMap().dispose(session);
                        try {
                            session.getFilterChain().clear();
                            if (s.getConfig().isUseReadOperation()) {
                                s.offerClosedReadFuture();
                            }
                            throw th2;
                        } finally {
                            if (s.getConfig().isUseReadOperation()) {
                                s.offerClosedReadFuture();
                            }
                        }
                    } catch (Throwable th3) {
                        try {
                            session.getFilterChain().clear();
                            if (s.getConfig().isUseReadOperation()) {
                                s.offerClosedReadFuture();
                            }
                            throw th3;
                        } finally {
                            if (s.getConfig().isUseReadOperation()) {
                                s.offerClosedReadFuture();
                            }
                        }
                    }
                }
            } catch (Throwable th4) {
                try {
                    s.getWriteRequestQueue().dispose(session);
                    try {
                        s.getAttributeMap().dispose(session);
                        try {
                            session.getFilterChain().clear();
                            if (s.getConfig().isUseReadOperation()) {
                                s.offerClosedReadFuture();
                            }
                            throw th4;
                        } finally {
                            if (s.getConfig().isUseReadOperation()) {
                                s.offerClosedReadFuture();
                            }
                        }
                    } catch (Throwable th5) {
                        try {
                            session.getFilterChain().clear();
                            if (s.getConfig().isUseReadOperation()) {
                                s.offerClosedReadFuture();
                            }
                            throw th5;
                        } finally {
                            if (s.getConfig().isUseReadOperation()) {
                                s.offerClosedReadFuture();
                            }
                        }
                    }
                } catch (Throwable th6) {
                    try {
                        s.getAttributeMap().dispose(session);
                        try {
                            session.getFilterChain().clear();
                            if (s.getConfig().isUseReadOperation()) {
                                s.offerClosedReadFuture();
                            }
                            throw th6;
                        } finally {
                            if (s.getConfig().isUseReadOperation()) {
                                s.offerClosedReadFuture();
                            }
                        }
                    } catch (Throwable th7) {
                        try {
                            session.getFilterChain().clear();
                            if (s.getConfig().isUseReadOperation()) {
                                s.offerClosedReadFuture();
                            }
                            throw th7;
                        } finally {
                            if (s.getConfig().isUseReadOperation()) {
                                s.offerClosedReadFuture();
                            }
                        }
                    }
                }
            }
        }

        @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
        public void sessionIdle(IoFilter.NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
            session.getHandler().sessionIdle(session, status);
        }

        @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
        public void exceptionCaught(IoFilter.NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
            AbstractIoSession s = (AbstractIoSession) session;
            try {
                s.getHandler().exceptionCaught(s, cause);
            } finally {
                if (s.getConfig().isUseReadOperation()) {
                    s.offerFailedReadFuture(cause);
                }
            }
        }

        @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
        public void inputClosed(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
            session.getHandler().inputClosed(session);
        }

        @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
        public void messageReceived(IoFilter.NextFilter nextFilter, IoSession session, Object message) throws Exception {
            AbstractIoSession s = (AbstractIoSession) session;
            if (!(message instanceof IoBuffer)) {
                s.increaseReadMessages(System.currentTimeMillis());
            } else if (!((IoBuffer) message).hasRemaining()) {
                s.increaseReadMessages(System.currentTimeMillis());
            }
            if (session.getService() instanceof AbstractIoService) {
                ((AbstractIoService) session.getService()).getStatistics().updateThroughput(System.currentTimeMillis());
            }
            try {
                session.getHandler().messageReceived(s, message);
            } finally {
                if (s.getConfig().isUseReadOperation()) {
                    s.offerReadFuture(message);
                }
            }
        }

        @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
        public void messageSent(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
            ((AbstractIoSession) session).increaseWrittenMessages(writeRequest, System.currentTimeMillis());
            if (session.getService() instanceof AbstractIoService) {
                ((AbstractIoService) session.getService()).getStatistics().updateThroughput(System.currentTimeMillis());
            }
            session.getHandler().messageSent(session, writeRequest.getMessage());
        }

        @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
        public void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
            nextFilter.filterWrite(session, writeRequest);
        }

        @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
        public void filterClose(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
            nextFilter.filterClose(session);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class EntryImpl implements IoFilterChain.Entry {
        private IoFilter filter;
        private final String name;
        private EntryImpl nextEntry;
        private final IoFilter.NextFilter nextFilter;
        private EntryImpl prevEntry;

        private EntryImpl(EntryImpl prevEntry, EntryImpl nextEntry, String name, IoFilter filter) {
            if (filter == null) {
                throw new IllegalArgumentException("filter");
            }
            if (name == null) {
                throw new IllegalArgumentException("name");
            }
            this.prevEntry = prevEntry;
            this.nextEntry = nextEntry;
            this.name = name;
            this.filter = filter;
            this.nextFilter = new IoFilter.NextFilter() { // from class: org.apache.mina.core.filterchain.DefaultIoFilterChain.EntryImpl.1
                @Override // org.apache.mina.core.filterchain.IoFilter.NextFilter
                public void sessionCreated(IoSession session) {
                    IoFilterChain.Entry nextEntry2 = EntryImpl.this.nextEntry;
                    DefaultIoFilterChain.this.callNextSessionCreated(nextEntry2, session);
                }

                @Override // org.apache.mina.core.filterchain.IoFilter.NextFilter
                public void sessionOpened(IoSession session) {
                    IoFilterChain.Entry nextEntry2 = EntryImpl.this.nextEntry;
                    DefaultIoFilterChain.this.callNextSessionOpened(nextEntry2, session);
                }

                @Override // org.apache.mina.core.filterchain.IoFilter.NextFilter
                public void sessionClosed(IoSession session) {
                    IoFilterChain.Entry nextEntry2 = EntryImpl.this.nextEntry;
                    DefaultIoFilterChain.this.callNextSessionClosed(nextEntry2, session);
                }

                @Override // org.apache.mina.core.filterchain.IoFilter.NextFilter
                public void sessionIdle(IoSession session, IdleStatus status) {
                    IoFilterChain.Entry nextEntry2 = EntryImpl.this.nextEntry;
                    DefaultIoFilterChain.this.callNextSessionIdle(nextEntry2, session, status);
                }

                @Override // org.apache.mina.core.filterchain.IoFilter.NextFilter
                public void exceptionCaught(IoSession session, Throwable cause) {
                    IoFilterChain.Entry nextEntry2 = EntryImpl.this.nextEntry;
                    DefaultIoFilterChain.this.callNextExceptionCaught(nextEntry2, session, cause);
                }

                @Override // org.apache.mina.core.filterchain.IoFilter.NextFilter
                public void inputClosed(IoSession session) {
                    IoFilterChain.Entry nextEntry2 = EntryImpl.this.nextEntry;
                    DefaultIoFilterChain.this.callNextInputClosed(nextEntry2, session);
                }

                @Override // org.apache.mina.core.filterchain.IoFilter.NextFilter
                public void messageReceived(IoSession session, Object message) {
                    IoFilterChain.Entry nextEntry2 = EntryImpl.this.nextEntry;
                    DefaultIoFilterChain.this.callNextMessageReceived(nextEntry2, session, message);
                }

                @Override // org.apache.mina.core.filterchain.IoFilter.NextFilter
                public void messageSent(IoSession session, WriteRequest writeRequest) {
                    IoFilterChain.Entry nextEntry2 = EntryImpl.this.nextEntry;
                    DefaultIoFilterChain.this.callNextMessageSent(nextEntry2, session, writeRequest);
                }

                @Override // org.apache.mina.core.filterchain.IoFilter.NextFilter
                public void filterWrite(IoSession session, WriteRequest writeRequest) {
                    IoFilterChain.Entry nextEntry2 = EntryImpl.this.prevEntry;
                    DefaultIoFilterChain.this.callPreviousFilterWrite(nextEntry2, session, writeRequest);
                }

                @Override // org.apache.mina.core.filterchain.IoFilter.NextFilter
                public void filterClose(IoSession session) {
                    IoFilterChain.Entry nextEntry2 = EntryImpl.this.prevEntry;
                    DefaultIoFilterChain.this.callPreviousFilterClose(nextEntry2, session);
                }

                public String toString() {
                    return EntryImpl.this.nextEntry.name;
                }
            };
        }

        @Override // org.apache.mina.core.filterchain.IoFilterChain.Entry
        public String getName() {
            return this.name;
        }

        @Override // org.apache.mina.core.filterchain.IoFilterChain.Entry
        public IoFilter getFilter() {
            return this.filter;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void setFilter(IoFilter filter) {
            if (filter == null) {
                throw new IllegalArgumentException("filter");
            }
            this.filter = filter;
        }

        @Override // org.apache.mina.core.filterchain.IoFilterChain.Entry
        public IoFilter.NextFilter getNextFilter() {
            return this.nextFilter;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("('").append(getName()).append('\'');
            sb.append(", prev: '");
            if (this.prevEntry != null) {
                sb.append(this.prevEntry.name);
                sb.append(':');
                sb.append(this.prevEntry.getFilter().getClass().getSimpleName());
            } else {
                sb.append("null");
            }
            sb.append("', next: '");
            if (this.nextEntry != null) {
                sb.append(this.nextEntry.name);
                sb.append(':');
                sb.append(this.nextEntry.getFilter().getClass().getSimpleName());
            } else {
                sb.append("null");
            }
            sb.append("')");
            return sb.toString();
        }

        @Override // org.apache.mina.core.filterchain.IoFilterChain.Entry
        public void addAfter(String name, IoFilter filter) {
            DefaultIoFilterChain.this.addAfter(getName(), name, filter);
        }

        @Override // org.apache.mina.core.filterchain.IoFilterChain.Entry
        public void addBefore(String name, IoFilter filter) {
            DefaultIoFilterChain.this.addBefore(getName(), name, filter);
        }

        @Override // org.apache.mina.core.filterchain.IoFilterChain.Entry
        public void remove() {
            DefaultIoFilterChain.this.remove(getName());
        }

        @Override // org.apache.mina.core.filterchain.IoFilterChain.Entry
        public void replace(IoFilter newFilter) {
            DefaultIoFilterChain.this.replace(getName(), newFilter);
        }
    }
}
