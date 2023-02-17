package org.apache.mina.core.session;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;

public class DefaultIoSessionDataStructureFactory implements IoSessionDataStructureFactory {
    public IoSessionAttributeMap getAttributeMap(IoSession session) throws Exception {
        return new DefaultIoSessionAttributeMap();
    }

    public WriteRequestQueue getWriteRequestQueue(IoSession session) throws Exception {
        return new DefaultWriteRequestQueue();
    }

    private static class DefaultIoSessionAttributeMap implements IoSessionAttributeMap {
        private final ConcurrentHashMap<Object, Object> attributes = new ConcurrentHashMap<>(4);

        public Object getAttribute(IoSession session, Object key, Object defaultValue) {
            if (key == null) {
                throw new IllegalArgumentException("key");
            } else if (defaultValue == null) {
                return this.attributes.get(key);
            } else {
                Object object = this.attributes.putIfAbsent(key, defaultValue);
                if (object != null) {
                    return object;
                }
                return defaultValue;
            }
        }

        public Object setAttribute(IoSession session, Object key, Object value) {
            if (key == null) {
                throw new IllegalArgumentException("key");
            } else if (value == null) {
                return this.attributes.remove(key);
            } else {
                return this.attributes.put(key, value);
            }
        }

        public Object setAttributeIfAbsent(IoSession session, Object key, Object value) {
            if (key == null) {
                throw new IllegalArgumentException("key");
            } else if (value == null) {
                return null;
            } else {
                return this.attributes.putIfAbsent(key, value);
            }
        }

        public Object removeAttribute(IoSession session, Object key) {
            if (key != null) {
                return this.attributes.remove(key);
            }
            throw new IllegalArgumentException("key");
        }

        public boolean removeAttribute(IoSession session, Object key, Object value) {
            if (key == null) {
                throw new IllegalArgumentException("key");
            } else if (value == null) {
                return false;
            } else {
                try {
                    return this.attributes.remove(key, value);
                } catch (NullPointerException e) {
                    return false;
                }
            }
        }

        public boolean replaceAttribute(IoSession session, Object key, Object oldValue, Object newValue) {
            try {
                return this.attributes.replace(key, oldValue, newValue);
            } catch (NullPointerException e) {
                return false;
            }
        }

        public boolean containsAttribute(IoSession session, Object key) {
            return this.attributes.containsKey(key);
        }

        public Set<Object> getAttributeKeys(IoSession session) {
            HashSet hashSet;
            synchronized (this.attributes) {
                hashSet = new HashSet(this.attributes.keySet());
            }
            return hashSet;
        }

        public void dispose(IoSession session) throws Exception {
        }
    }

    private static class DefaultWriteRequestQueue implements WriteRequestQueue {

        /* renamed from: q */
        private final Queue<WriteRequest> f185q = new ConcurrentLinkedQueue();

        public void dispose(IoSession session) {
        }

        public void clear(IoSession session) {
            this.f185q.clear();
        }

        public synchronized boolean isEmpty(IoSession session) {
            return this.f185q.isEmpty();
        }

        public synchronized void offer(IoSession session, WriteRequest writeRequest) {
            this.f185q.offer(writeRequest);
        }

        public synchronized WriteRequest poll(IoSession session) {
            WriteRequest answer;
            answer = this.f185q.poll();
            if (answer == AbstractIoSession.CLOSE_REQUEST) {
                session.closeNow();
                dispose(session);
                answer = null;
            }
            return answer;
        }

        public String toString() {
            return this.f185q.toString();
        }

        public int size() {
            return this.f185q.size();
        }
    }
}
