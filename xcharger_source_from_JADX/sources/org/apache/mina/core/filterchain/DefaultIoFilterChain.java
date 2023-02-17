package org.apache.mina.core.filterchain;

import java.util.ArrayList;
import java.util.Iterator;
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

public class DefaultIoFilterChain implements IoFilterChain {
    private static final Logger LOGGER = LoggerFactory.getLogger((Class<?>) DefaultIoFilterChain.class);
    public static final AttributeKey SESSION_CREATED_FUTURE = new AttributeKey(DefaultIoFilterChain.class, "connectFuture");
    private final EntryImpl head;
    private final Map<String, IoFilterChain.Entry> name2entry = new ConcurrentHashMap();
    private final AbstractIoSession session;
    private final EntryImpl tail;

    public DefaultIoFilterChain(AbstractIoSession session2) {
        if (session2 == null) {
            throw new IllegalArgumentException("session");
        }
        this.session = session2;
        this.head = new EntryImpl((EntryImpl) null, (EntryImpl) null, "head", new HeadFilter());
        this.tail = new EntryImpl(this.head, (EntryImpl) null, "tail", new TailFilter());
        EntryImpl unused = this.head.nextEntry = this.tail;
    }

    public IoSession getSession() {
        return this.session;
    }

    public IoFilterChain.Entry getEntry(String name) {
        IoFilterChain.Entry e = this.name2entry.get(name);
        if (e == null) {
            return null;
        }
        return e;
    }

    public IoFilterChain.Entry getEntry(IoFilter filter) {
        for (EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
            if (e.getFilter() == filter) {
                return e;
            }
        }
        return null;
    }

    public IoFilterChain.Entry getEntry(Class<? extends IoFilter> filterType) {
        for (EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
            if (filterType.isAssignableFrom(e.getFilter().getClass())) {
                return e;
            }
        }
        return null;
    }

    public IoFilter get(String name) {
        IoFilterChain.Entry e = getEntry(name);
        if (e == null) {
            return null;
        }
        return e.getFilter();
    }

    public IoFilter get(Class<? extends IoFilter> filterType) {
        IoFilterChain.Entry e = getEntry(filterType);
        if (e == null) {
            return null;
        }
        return e.getFilter();
    }

    public IoFilter.NextFilter getNextFilter(String name) {
        IoFilterChain.Entry e = getEntry(name);
        if (e == null) {
            return null;
        }
        return e.getNextFilter();
    }

    public IoFilter.NextFilter getNextFilter(IoFilter filter) {
        IoFilterChain.Entry e = getEntry(filter);
        if (e == null) {
            return null;
        }
        return e.getNextFilter();
    }

    public IoFilter.NextFilter getNextFilter(Class<? extends IoFilter> filterType) {
        IoFilterChain.Entry e = getEntry(filterType);
        if (e == null) {
            return null;
        }
        return e.getNextFilter();
    }

    public synchronized void addFirst(String name, IoFilter filter) {
        checkAddable(name);
        register(this.head, name, filter);
    }

    public synchronized void addLast(String name, IoFilter filter) {
        checkAddable(name);
        register(this.tail.prevEntry, name, filter);
    }

    public synchronized void addBefore(String baseName, String name, IoFilter filter) {
        EntryImpl baseEntry = checkOldName(baseName);
        checkAddable(name);
        register(baseEntry.prevEntry, name, filter);
    }

    public synchronized void addAfter(String baseName, String name, IoFilter filter) {
        EntryImpl baseEntry = checkOldName(baseName);
        checkAddable(name);
        register(baseEntry, name, filter);
    }

    public synchronized IoFilter remove(String name) {
        EntryImpl entry;
        entry = checkOldName(name);
        deregister(entry);
        return entry.getFilter();
    }

    public synchronized void remove(IoFilter filter) {
        EntryImpl e = this.head.nextEntry;
        while (e != this.tail) {
            if (e.getFilter() == filter) {
                deregister(e);
            } else {
                e = e.nextEntry;
            }
        }
        throw new IllegalArgumentException("Filter not found: " + filter.getClass().getName());
    }

    public synchronized IoFilter remove(Class<? extends IoFilter> filterType) {
        IoFilter oldFilter;
        EntryImpl e = this.head.nextEntry;
        while (e != this.tail) {
            if (filterType.isAssignableFrom(e.getFilter().getClass())) {
                oldFilter = e.getFilter();
                deregister(e);
            } else {
                e = e.nextEntry;
            }
        }
        throw new IllegalArgumentException("Filter not found: " + filterType.getName());
        return oldFilter;
    }

    public synchronized IoFilter replace(String name, IoFilter newFilter) {
        IoFilter oldFilter;
        EntryImpl entry = checkOldName(name);
        oldFilter = entry.getFilter();
        try {
            newFilter.onPreAdd(this, name, entry.getNextFilter());
            entry.setFilter(newFilter);
            newFilter.onPostAdd(this, name, entry.getNextFilter());
        } catch (Exception e) {
            entry.setFilter(oldFilter);
            throw new IoFilterLifeCycleException("onPostAdd(): " + name + ':' + newFilter + " in " + getSession(), e);
        } catch (Exception e2) {
            throw new IoFilterLifeCycleException("onPreAdd(): " + name + ':' + newFilter + " in " + getSession(), e2);
        }
        return oldFilter;
    }

    public synchronized void replace(IoFilter oldFilter, IoFilter newFilter) {
        EntryImpl entry = this.head.nextEntry;
        while (entry != this.tail) {
            if (entry.getFilter() == oldFilter) {
                String oldFilterName = null;
                Iterator<Map.Entry<String, IoFilterChain.Entry>> it = this.name2entry.entrySet().iterator();
                while (true) {
                    if (it.hasNext()) {
                        Map.Entry<String, IoFilterChain.Entry> mapping = it.next();
                        if (entry == mapping.getValue()) {
                            oldFilterName = mapping.getKey();
                            break;
                        }
                    }
                }
                try {
                    newFilter.onPreAdd(this, oldFilterName, entry.getNextFilter());
                    entry.setFilter(newFilter);
                    newFilter.onPostAdd(this, oldFilterName, entry.getNextFilter());
                } catch (Exception e) {
                    entry.setFilter(oldFilter);
                    throw new IoFilterLifeCycleException("onPostAdd(): " + oldFilterName + ':' + newFilter + " in " + getSession(), e);
                } catch (Exception e2) {
                    throw new IoFilterLifeCycleException("onPreAdd(): " + oldFilterName + ':' + newFilter + " in " + getSession(), e2);
                }
            } else {
                entry = entry.nextEntry;
            }
        }
        throw new IllegalArgumentException("Filter not found: " + oldFilter.getClass().getName());
    }

    public synchronized IoFilter replace(Class<? extends IoFilter> oldFilterType, IoFilter newFilter) {
        IoFilter oldFilter;
        EntryImpl entry = this.head.nextEntry;
        while (entry != this.tail) {
            if (oldFilterType.isAssignableFrom(entry.getFilter().getClass())) {
                oldFilter = entry.getFilter();
                String oldFilterName = null;
                Iterator<Map.Entry<String, IoFilterChain.Entry>> it = this.name2entry.entrySet().iterator();
                while (true) {
                    if (it.hasNext()) {
                        Map.Entry<String, IoFilterChain.Entry> mapping = it.next();
                        if (entry == mapping.getValue()) {
                            oldFilterName = mapping.getKey();
                            break;
                        }
                    }
                }
                try {
                    newFilter.onPreAdd(this, oldFilterName, entry.getNextFilter());
                    entry.setFilter(newFilter);
                    newFilter.onPostAdd(this, oldFilterName, entry.getNextFilter());
                } catch (Exception e) {
                    entry.setFilter(oldFilter);
                    throw new IoFilterLifeCycleException("onPostAdd(): " + oldFilterName + ':' + newFilter + " in " + getSession(), e);
                } catch (Exception e2) {
                    throw new IoFilterLifeCycleException("onPreAdd(): " + oldFilterName + ':' + newFilter + " in " + getSession(), e2);
                }
            } else {
                entry = entry.nextEntry;
            }
        }
        throw new IllegalArgumentException("Filter not found: " + oldFilterType.getName());
        return oldFilter;
    }

    public synchronized void clear() throws Exception {
        for (IoFilterChain.Entry entry : new ArrayList<>(this.name2entry.values())) {
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
            EntryImpl unused = prevEntry.nextEntry.prevEntry = newEntry;
            EntryImpl unused2 = prevEntry.nextEntry = newEntry;
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
        EntryImpl unused = prevEntry.nextEntry = nextEntry;
        EntryImpl unused2 = nextEntry.prevEntry = prevEntry;
        this.name2entry.remove(entry.name);
    }

    private EntryImpl checkOldName(String baseName) {
        EntryImpl e = (EntryImpl) this.name2entry.get(baseName);
        if (e != null) {
            return e;
        }
        throw new IllegalArgumentException("Filter not found:" + baseName);
    }

    private void checkAddable(String name) {
        if (this.name2entry.containsKey(name)) {
            throw new IllegalArgumentException("Other filter is using the same name '" + name + "'");
        }
    }

    public void fireSessionCreated() {
        callNextSessionCreated(this.head, this.session);
    }

    /* access modifiers changed from: private */
    public void callNextSessionCreated(IoFilterChain.Entry entry, IoSession session2) {
        try {
            entry.getFilter().sessionCreated(entry.getNextFilter(), session2);
        } catch (Exception e) {
            fireExceptionCaught(e);
        } catch (Error e2) {
            fireExceptionCaught(e2);
            throw e2;
        }
    }

    public void fireSessionOpened() {
        callNextSessionOpened(this.head, this.session);
    }

    /* access modifiers changed from: private */
    public void callNextSessionOpened(IoFilterChain.Entry entry, IoSession session2) {
        try {
            entry.getFilter().sessionOpened(entry.getNextFilter(), session2);
        } catch (Exception e) {
            fireExceptionCaught(e);
        } catch (Error e2) {
            fireExceptionCaught(e2);
            throw e2;
        }
    }

    public void fireSessionClosed() {
        try {
            this.session.getCloseFuture().setClosed();
        } catch (Exception e) {
            fireExceptionCaught(e);
        } catch (Error e2) {
            fireExceptionCaught(e2);
            throw e2;
        }
        callNextSessionClosed(this.head, this.session);
    }

    /* access modifiers changed from: private */
    public void callNextSessionClosed(IoFilterChain.Entry entry, IoSession session2) {
        try {
            entry.getFilter().sessionClosed(entry.getNextFilter(), session2);
        } catch (Exception e) {
            fireExceptionCaught(e);
        } catch (Error e2) {
            fireExceptionCaught(e2);
        }
    }

    public void fireSessionIdle(IdleStatus status) {
        this.session.increaseIdleCount(status, System.currentTimeMillis());
        callNextSessionIdle(this.head, this.session, status);
    }

    /* access modifiers changed from: private */
    public void callNextSessionIdle(IoFilterChain.Entry entry, IoSession session2, IdleStatus status) {
        try {
            entry.getFilter().sessionIdle(entry.getNextFilter(), session2, status);
        } catch (Exception e) {
            fireExceptionCaught(e);
        } catch (Error e2) {
            fireExceptionCaught(e2);
            throw e2;
        }
    }

    public void fireMessageReceived(Object message) {
        if (message instanceof IoBuffer) {
            this.session.increaseReadBytes((long) ((IoBuffer) message).remaining(), System.currentTimeMillis());
        }
        callNextMessageReceived(this.head, this.session, message);
    }

    /* access modifiers changed from: private */
    public void callNextMessageReceived(IoFilterChain.Entry entry, IoSession session2, Object message) {
        try {
            entry.getFilter().messageReceived(entry.getNextFilter(), session2, message);
        } catch (Exception e) {
            fireExceptionCaught(e);
        } catch (Error e2) {
            fireExceptionCaught(e2);
            throw e2;
        }
    }

    public void fireMessageSent(WriteRequest request) {
        try {
            request.getFuture().setWritten();
        } catch (Exception e) {
            fireExceptionCaught(e);
        } catch (Error e2) {
            fireExceptionCaught(e2);
            throw e2;
        }
        if (!request.isEncoded()) {
            callNextMessageSent(this.head, this.session, request);
        }
    }

    /* access modifiers changed from: private */
    public void callNextMessageSent(IoFilterChain.Entry entry, IoSession session2, WriteRequest writeRequest) {
        try {
            entry.getFilter().messageSent(entry.getNextFilter(), session2, writeRequest);
        } catch (Exception e) {
            fireExceptionCaught(e);
        } catch (Error e2) {
            fireExceptionCaught(e2);
            throw e2;
        }
    }

    public void fireExceptionCaught(Throwable cause) {
        callNextExceptionCaught(this.head, this.session, cause);
    }

    /* access modifiers changed from: private */
    public void callNextExceptionCaught(IoFilterChain.Entry entry, IoSession session2, Throwable cause) {
        ConnectFuture future = (ConnectFuture) session2.removeAttribute(SESSION_CREATED_FUTURE);
        if (future == null) {
            try {
                entry.getFilter().exceptionCaught(entry.getNextFilter(), session2, cause);
            } catch (Throwable e) {
                LOGGER.warn("Unexpected exception from exceptionCaught handler.", e);
            }
        } else {
            if (!session2.isClosing()) {
                session2.closeNow();
            }
            future.setException(cause);
        }
    }

    public void fireInputClosed() {
        callNextInputClosed(this.head, this.session);
    }

    /* access modifiers changed from: private */
    public void callNextInputClosed(IoFilterChain.Entry entry, IoSession session2) {
        try {
            entry.getFilter().inputClosed(entry.getNextFilter(), session2);
        } catch (Throwable e) {
            fireExceptionCaught(e);
        }
    }

    public void fireFilterWrite(WriteRequest writeRequest) {
        callPreviousFilterWrite(this.tail, this.session, writeRequest);
    }

    /* access modifiers changed from: private */
    public void callPreviousFilterWrite(IoFilterChain.Entry entry, IoSession session2, WriteRequest writeRequest) {
        try {
            entry.getFilter().filterWrite(entry.getNextFilter(), session2, writeRequest);
        } catch (Exception e) {
            writeRequest.getFuture().setException(e);
            fireExceptionCaught(e);
        } catch (Error e2) {
            writeRequest.getFuture().setException(e2);
            fireExceptionCaught(e2);
            throw e2;
        }
    }

    public void fireFilterClose() {
        callPreviousFilterClose(this.tail, this.session);
    }

    /* access modifiers changed from: private */
    public void callPreviousFilterClose(IoFilterChain.Entry entry, IoSession session2) {
        try {
            entry.getFilter().filterClose(entry.getNextFilter(), session2);
        } catch (Exception e) {
            fireExceptionCaught(e);
        } catch (Error e2) {
            fireExceptionCaught(e2);
            throw e2;
        }
    }

    public List<IoFilterChain.Entry> getAll() {
        List<IoFilterChain.Entry> list = new ArrayList<>();
        for (EntryImpl e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
            list.add(e);
        }
        return list;
    }

    public List<IoFilterChain.Entry> getAllReversed() {
        List<IoFilterChain.Entry> list = new ArrayList<>();
        for (EntryImpl e = this.tail.prevEntry; e != this.head; e = e.prevEntry) {
            list.add(e);
        }
        return list;
    }

    public boolean contains(String name) {
        return getEntry(name) != null;
    }

    public boolean contains(IoFilter filter) {
        return getEntry(filter) != null;
    }

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

    private class HeadFilter extends IoFilterAdapter {
        private HeadFilter() {
        }

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
            if (s.isWriteSuspended()) {
                s.getWriteRequestQueue().offer(s, writeRequest);
            } else if (writeRequestQueue.isEmpty(session)) {
                s.getProcessor().write(s, writeRequest);
            } else {
                s.getWriteRequestQueue().offer(s, writeRequest);
                s.getProcessor().flush(s);
            }
        }

        public void filterClose(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
            ((AbstractIoSession) session).getProcessor().remove(session);
        }
    }

    private static class TailFilter extends IoFilterAdapter {
        private TailFilter() {
        }

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

        public void sessionOpened(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
            session.getHandler().sessionOpened(session);
        }

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
                        if (s.getConfig().isUseReadOperation()) {
                            s.offerClosedReadFuture();
                        }
                        throw th;
                    }
                } catch (Throwable th2) {
                    if (s.getConfig().isUseReadOperation()) {
                        s.offerClosedReadFuture();
                    }
                    throw th2;
                }
            } catch (Throwable th3) {
                if (s.getConfig().isUseReadOperation()) {
                    s.offerClosedReadFuture();
                }
                throw th3;
            }
        }

        public void sessionIdle(IoFilter.NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
            session.getHandler().sessionIdle(session, status);
        }

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

        public void inputClosed(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
            session.getHandler().inputClosed(session);
        }

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

        public void messageSent(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
            ((AbstractIoSession) session).increaseWrittenMessages(writeRequest, System.currentTimeMillis());
            if (session.getService() instanceof AbstractIoService) {
                ((AbstractIoService) session.getService()).getStatistics().updateThroughput(System.currentTimeMillis());
            }
            session.getHandler().messageSent(session, writeRequest.getMessage());
        }

        public void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
            nextFilter.filterWrite(session, writeRequest);
        }

        public void filterClose(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
            nextFilter.filterClose(session);
        }
    }

    private final class EntryImpl implements IoFilterChain.Entry {
        private IoFilter filter;
        /* access modifiers changed from: private */
        public final String name;
        /* access modifiers changed from: private */
        public EntryImpl nextEntry;
        private final IoFilter.NextFilter nextFilter;
        /* access modifiers changed from: private */
        public EntryImpl prevEntry;

        private EntryImpl(EntryImpl prevEntry2, EntryImpl nextEntry2, String name2, IoFilter filter2) {
            if (filter2 == null) {
                throw new IllegalArgumentException("filter");
            } else if (name2 == null) {
                throw new IllegalArgumentException("name");
            } else {
                this.prevEntry = prevEntry2;
                this.nextEntry = nextEntry2;
                this.name = name2;
                this.filter = filter2;
                this.nextFilter = new IoFilter.NextFilter(DefaultIoFilterChain.this) {
                    public void sessionCreated(IoSession session) {
                        DefaultIoFilterChain.this.callNextSessionCreated(EntryImpl.this.nextEntry, session);
                    }

                    public void sessionOpened(IoSession session) {
                        DefaultIoFilterChain.this.callNextSessionOpened(EntryImpl.this.nextEntry, session);
                    }

                    public void sessionClosed(IoSession session) {
                        DefaultIoFilterChain.this.callNextSessionClosed(EntryImpl.this.nextEntry, session);
                    }

                    public void sessionIdle(IoSession session, IdleStatus status) {
                        DefaultIoFilterChain.this.callNextSessionIdle(EntryImpl.this.nextEntry, session, status);
                    }

                    public void exceptionCaught(IoSession session, Throwable cause) {
                        DefaultIoFilterChain.this.callNextExceptionCaught(EntryImpl.this.nextEntry, session, cause);
                    }

                    public void inputClosed(IoSession session) {
                        DefaultIoFilterChain.this.callNextInputClosed(EntryImpl.this.nextEntry, session);
                    }

                    public void messageReceived(IoSession session, Object message) {
                        DefaultIoFilterChain.this.callNextMessageReceived(EntryImpl.this.nextEntry, session, message);
                    }

                    public void messageSent(IoSession session, WriteRequest writeRequest) {
                        DefaultIoFilterChain.this.callNextMessageSent(EntryImpl.this.nextEntry, session, writeRequest);
                    }

                    public void filterWrite(IoSession session, WriteRequest writeRequest) {
                        DefaultIoFilterChain.this.callPreviousFilterWrite(EntryImpl.this.prevEntry, session, writeRequest);
                    }

                    public void filterClose(IoSession session) {
                        DefaultIoFilterChain.this.callPreviousFilterClose(EntryImpl.this.prevEntry, session);
                    }

                    public String toString() {
                        return EntryImpl.this.nextEntry.name;
                    }
                };
            }
        }

        public String getName() {
            return this.name;
        }

        public IoFilter getFilter() {
            return this.filter;
        }

        /* access modifiers changed from: private */
        public void setFilter(IoFilter filter2) {
            if (filter2 == null) {
                throw new IllegalArgumentException("filter");
            }
            this.filter = filter2;
        }

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

        public void addAfter(String name2, IoFilter filter2) {
            DefaultIoFilterChain.this.addAfter(getName(), name2, filter2);
        }

        public void addBefore(String name2, IoFilter filter2) {
            DefaultIoFilterChain.this.addBefore(getName(), name2, filter2);
        }

        public void remove() {
            DefaultIoFilterChain.this.remove(getName());
        }

        public void replace(IoFilter newFilter) {
            DefaultIoFilterChain.this.replace(getName(), newFilter);
        }
    }
}
