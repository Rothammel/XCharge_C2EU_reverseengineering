package org.apache.mina.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/* loaded from: classes.dex */
public class ExpiringMap<K, V> implements Map<K, V> {
    public static final int DEFAULT_EXPIRATION_INTERVAL = 1;
    public static final int DEFAULT_TIME_TO_LIVE = 60;
    private static volatile int expirerCount = 1;
    private final ConcurrentHashMap<K, ExpiringMap<K, V>.ExpiringObject> delegate;
    private final CopyOnWriteArrayList<ExpirationListener<V>> expirationListeners;
    private final ExpiringMap<K, V>.Expirer expirer;

    static /* synthetic */ int access$008() {
        int i = expirerCount;
        expirerCount = i + 1;
        return i;
    }

    public ExpiringMap() {
        this(60, 1);
    }

    public ExpiringMap(int timeToLive) {
        this(timeToLive, 1);
    }

    public ExpiringMap(int timeToLive, int expirationInterval) {
        this(new ConcurrentHashMap(), new CopyOnWriteArrayList(), timeToLive, expirationInterval);
    }

    private ExpiringMap(ConcurrentHashMap<K, ExpiringMap<K, V>.ExpiringObject> delegate, CopyOnWriteArrayList<ExpirationListener<V>> expirationListeners, int timeToLive, int expirationInterval) {
        this.delegate = delegate;
        this.expirationListeners = expirationListeners;
        this.expirer = new Expirer();
        this.expirer.setTimeToLive(timeToLive);
        this.expirer.setExpirationInterval(expirationInterval);
    }

    @Override // java.util.Map
    public V put(K key, V value) {
        ExpiringMap<K, V>.ExpiringObject answer = this.delegate.put(key, new ExpiringObject(key, value, System.currentTimeMillis()));
        if (answer == null) {
            return null;
        }
        return answer.getValue();
    }

    @Override // java.util.Map
    public V get(Object key) {
        ExpiringMap<K, V>.ExpiringObject object = this.delegate.get(key);
        if (object != null) {
            object.setLastAccessTime(System.currentTimeMillis());
            return object.getValue();
        }
        return null;
    }

    @Override // java.util.Map
    public V remove(Object key) {
        ExpiringMap<K, V>.ExpiringObject answer = this.delegate.remove(key);
        if (answer == null) {
            return null;
        }
        return answer.getValue();
    }

    @Override // java.util.Map
    public boolean containsKey(Object key) {
        return this.delegate.containsKey(key);
    }

    @Override // java.util.Map
    public boolean containsValue(Object value) {
        return this.delegate.containsValue(value);
    }

    @Override // java.util.Map
    public int size() {
        return this.delegate.size();
    }

    @Override // java.util.Map
    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    @Override // java.util.Map
    public void clear() {
        this.delegate.clear();
    }

    @Override // java.util.Map
    public int hashCode() {
        return this.delegate.hashCode();
    }

    @Override // java.util.Map
    public Set<K> keySet() {
        return this.delegate.keySet();
    }

    @Override // java.util.Map
    public boolean equals(Object obj) {
        return this.delegate.equals(obj);
    }

    @Override // java.util.Map
    public void putAll(Map<? extends K, ? extends V> inMap) {
        for (Map.Entry<? extends K, ? extends V> e : inMap.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override // java.util.Map
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.Map
    public Set<Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    public void addExpirationListener(ExpirationListener<V> listener) {
        this.expirationListeners.add(listener);
    }

    public void removeExpirationListener(ExpirationListener<V> listener) {
        this.expirationListeners.remove(listener);
    }

    public ExpiringMap<K, V>.Expirer getExpirer() {
        return this.expirer;
    }

    public int getExpirationInterval() {
        return this.expirer.getExpirationInterval();
    }

    public int getTimeToLive() {
        return this.expirer.getTimeToLive();
    }

    public void setExpirationInterval(int expirationInterval) {
        this.expirer.setExpirationInterval(expirationInterval);
    }

    public void setTimeToLive(int timeToLive) {
        this.expirer.setTimeToLive(timeToLive);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ExpiringObject {
        private K key;
        private long lastAccessTime;
        private final ReadWriteLock lastAccessTimeLock = new ReentrantReadWriteLock();
        private V value;

        ExpiringObject(K key, V value, long lastAccessTime) {
            if (value == null) {
                throw new IllegalArgumentException("An expiring object cannot be null.");
            }
            this.key = key;
            this.value = value;
            this.lastAccessTime = lastAccessTime;
        }

        public long getLastAccessTime() {
            this.lastAccessTimeLock.readLock().lock();
            try {
                return this.lastAccessTime;
            } finally {
                this.lastAccessTimeLock.readLock().unlock();
            }
        }

        public void setLastAccessTime(long lastAccessTime) {
            this.lastAccessTimeLock.writeLock().lock();
            try {
                this.lastAccessTime = lastAccessTime;
            } finally {
                this.lastAccessTimeLock.writeLock().unlock();
            }
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.value;
        }

        public boolean equals(Object obj) {
            return this.value.equals(obj);
        }

        public int hashCode() {
            return this.value.hashCode();
        }
    }

    /* loaded from: classes.dex */
    public class Expirer implements Runnable {
        private long expirationIntervalMillis;
        private long timeToLiveMillis;
        private final ReadWriteLock stateLock = new ReentrantReadWriteLock();
        private boolean running = false;
        private final Thread expirerThread = new Thread(this, "ExpiringMapExpirer-" + ExpiringMap.access$008());

        public Expirer() {
            this.expirerThread.setDaemon(true);
        }

        @Override // java.lang.Runnable
        public void run() {
            while (this.running) {
                processExpires();
                try {
                    Thread.sleep(this.expirationIntervalMillis);
                } catch (InterruptedException e) {
                }
            }
        }

        private void processExpires() {
            long timeNow = System.currentTimeMillis();
            for (V o : ExpiringMap.this.delegate.values()) {
                if (this.timeToLiveMillis > 0) {
                    long timeIdle = timeNow - o.getLastAccessTime();
                    if (timeIdle >= this.timeToLiveMillis) {
                        ExpiringMap.this.delegate.remove(o.getKey());
                        Iterator it2 = ExpiringMap.this.expirationListeners.iterator();
                        while (it2.hasNext()) {
                            ExpirationListener<V> listener = (ExpirationListener) it2.next();
                            listener.expired(o.getValue());
                        }
                    }
                }
            }
        }

        public void startExpiring() {
            this.stateLock.writeLock().lock();
            try {
                if (!this.running) {
                    this.running = true;
                    this.expirerThread.start();
                }
            } finally {
                this.stateLock.writeLock().unlock();
            }
        }

        public void startExpiringIfNotStarted() {
            this.stateLock.readLock().lock();
            try {
                if (!this.running) {
                    this.stateLock.readLock().unlock();
                    this.stateLock.writeLock().lock();
                    try {
                        if (!this.running) {
                            this.running = true;
                            this.expirerThread.start();
                        }
                    } finally {
                        this.stateLock.writeLock().unlock();
                    }
                }
            } finally {
                this.stateLock.readLock().unlock();
            }
        }

        public void stopExpiring() {
            this.stateLock.writeLock().lock();
            try {
                if (this.running) {
                    this.running = false;
                    this.expirerThread.interrupt();
                }
            } finally {
                this.stateLock.writeLock().unlock();
            }
        }

        public boolean isRunning() {
            this.stateLock.readLock().lock();
            try {
                return this.running;
            } finally {
                this.stateLock.readLock().unlock();
            }
        }

        public int getTimeToLive() {
            this.stateLock.readLock().lock();
            try {
                return ((int) this.timeToLiveMillis) / 1000;
            } finally {
                this.stateLock.readLock().unlock();
            }
        }

        public void setTimeToLive(long timeToLive) {
            this.stateLock.writeLock().lock();
            try {
                this.timeToLiveMillis = 1000 * timeToLive;
            } finally {
                this.stateLock.writeLock().unlock();
            }
        }

        public int getExpirationInterval() {
            this.stateLock.readLock().lock();
            try {
                return ((int) this.expirationIntervalMillis) / 1000;
            } finally {
                this.stateLock.readLock().unlock();
            }
        }

        public void setExpirationInterval(long expirationInterval) {
            this.stateLock.writeLock().lock();
            try {
                this.expirationIntervalMillis = 1000 * expirationInterval;
            } finally {
                this.stateLock.writeLock().unlock();
            }
        }
    }
}
