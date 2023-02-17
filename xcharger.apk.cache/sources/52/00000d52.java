package org.apache.mina.core.session;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;

/* loaded from: classes.dex */
public class DefaultIoSessionDataStructureFactory implements IoSessionDataStructureFactory {
    @Override // org.apache.mina.core.session.IoSessionDataStructureFactory
    public IoSessionAttributeMap getAttributeMap(IoSession session) throws Exception {
        return new DefaultIoSessionAttributeMap();
    }

    @Override // org.apache.mina.core.session.IoSessionDataStructureFactory
    public WriteRequestQueue getWriteRequestQueue(IoSession session) throws Exception {
        return new DefaultWriteRequestQueue();
    }

    /* loaded from: classes.dex */
    private static class DefaultIoSessionAttributeMap implements IoSessionAttributeMap {
        private final ConcurrentHashMap<Object, Object> attributes = new ConcurrentHashMap<>(4);

        @Override // org.apache.mina.core.session.IoSessionAttributeMap
        public Object getAttribute(IoSession session, Object key, Object defaultValue) {
            if (key == null) {
                throw new IllegalArgumentException("key");
            }
            if (defaultValue == null) {
                return this.attributes.get(key);
            }
            Object object = this.attributes.putIfAbsent(key, defaultValue);
            return object != null ? object : defaultValue;
        }

        @Override // org.apache.mina.core.session.IoSessionAttributeMap
        public Object setAttribute(IoSession session, Object key, Object value) {
            if (key == null) {
                throw new IllegalArgumentException("key");
            }
            return value == null ? this.attributes.remove(key) : this.attributes.put(key, value);
        }

        @Override // org.apache.mina.core.session.IoSessionAttributeMap
        public Object setAttributeIfAbsent(IoSession session, Object key, Object value) {
            if (key == null) {
                throw new IllegalArgumentException("key");
            }
            if (value == null) {
                return null;
            }
            return this.attributes.putIfAbsent(key, value);
        }

        @Override // org.apache.mina.core.session.IoSessionAttributeMap
        public Object removeAttribute(IoSession session, Object key) {
            if (key == null) {
                throw new IllegalArgumentException("key");
            }
            return this.attributes.remove(key);
        }

        @Override // org.apache.mina.core.session.IoSessionAttributeMap
        public boolean removeAttribute(IoSession session, Object key, Object value) {
            if (key == null) {
                throw new IllegalArgumentException("key");
            }
            if (value == null) {
                return false;
            }
            try {
                return this.attributes.remove(key, value);
            } catch (NullPointerException e) {
                return false;
            }
        }

        @Override // org.apache.mina.core.session.IoSessionAttributeMap
        public boolean replaceAttribute(IoSession session, Object key, Object oldValue, Object newValue) {
            try {
                return this.attributes.replace(key, oldValue, newValue);
            } catch (NullPointerException e) {
                return false;
            }
        }

        @Override // org.apache.mina.core.session.IoSessionAttributeMap
        public boolean containsAttribute(IoSession session, Object key) {
            return this.attributes.containsKey(key);
        }

        @Override // org.apache.mina.core.session.IoSessionAttributeMap
        public Set<Object> getAttributeKeys(IoSession session) {
            HashSet hashSet;
            synchronized (this.attributes) {
                hashSet = new HashSet(this.attributes.keySet());
            }
            return hashSet;
        }

        @Override // org.apache.mina.core.session.IoSessionAttributeMap
        public void dispose(IoSession session) throws Exception {
        }
    }

    /* loaded from: classes.dex */
    private static class DefaultWriteRequestQueue implements WriteRequestQueue {
        private final Queue<WriteRequest> q = new ConcurrentLinkedQueue();

        @Override // org.apache.mina.core.write.WriteRequestQueue
        public void dispose(IoSession session) {
        }

        @Override // org.apache.mina.core.write.WriteRequestQueue
        public void clear(IoSession session) {
            this.q.clear();
        }

        @Override // org.apache.mina.core.write.WriteRequestQueue
        public synchronized boolean isEmpty(IoSession session) {
            return this.q.isEmpty();
        }

        @Override // org.apache.mina.core.write.WriteRequestQueue
        public synchronized void offer(IoSession session, WriteRequest writeRequest) {
            this.q.offer(writeRequest);
        }

        @Override // org.apache.mina.core.write.WriteRequestQueue
        public synchronized WriteRequest poll(IoSession session) {
            WriteRequest answer;
            answer = this.q.poll();
            if (answer == AbstractIoSession.CLOSE_REQUEST) {
                session.closeNow();
                dispose(session);
                answer = null;
            }
            return answer;
        }

        public String toString() {
            return this.q.toString();
        }

        @Override // org.apache.mina.core.write.WriteRequestQueue
        public int size() {
            return this.q.size();
        }
    }
}