package org.apache.mina.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/* loaded from: classes.dex */
public class LazyInitializedCacheMap<K, V> implements Map<K, V> {
    private ConcurrentMap<K, LazyInitializer<V>> cache;

    /* loaded from: classes.dex */
    public class NoopInitializer extends LazyInitializer<V> {
        private V value;

        public NoopInitializer(V value) {
            this.value = value;
        }

        @Override // org.apache.mina.util.LazyInitializer
        public V init() {
            return this.value;
        }
    }

    public LazyInitializedCacheMap() {
        this.cache = new ConcurrentHashMap();
    }

    public LazyInitializedCacheMap(ConcurrentHashMap<K, LazyInitializer<V>> map) {
        this.cache = map;
    }

    @Override // java.util.Map
    public V get(Object key) {
        LazyInitializer<V> c = this.cache.get(key);
        if (c != null) {
            return c.get();
        }
        return null;
    }

    @Override // java.util.Map
    public V remove(Object key) {
        LazyInitializer<V> c = this.cache.remove(key);
        if (c != null) {
            return c.get();
        }
        return null;
    }

    public V putIfAbsent(K key, LazyInitializer<V> value) {
        LazyInitializer<V> v = this.cache.get(key);
        return (v == null && (v = this.cache.putIfAbsent(key, value)) == null) ? value.get() : v.get();
    }

    @Override // java.util.Map
    public V put(K key, V value) {
        LazyInitializer<V> c = this.cache.put(key, new NoopInitializer(value));
        if (c != null) {
            return c.get();
        }
        return null;
    }

    @Override // java.util.Map
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.Map
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.Map
    public Set<Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.Map
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            this.cache.put(e.getKey(), new NoopInitializer(e.getValue()));
        }
    }

    public Collection<LazyInitializer<V>> getValues() {
        return this.cache.values();
    }

    @Override // java.util.Map
    public void clear() {
        this.cache.clear();
    }

    @Override // java.util.Map
    public boolean containsKey(Object key) {
        return this.cache.containsKey(key);
    }

    @Override // java.util.Map
    public boolean isEmpty() {
        return this.cache.isEmpty();
    }

    @Override // java.util.Map
    public Set<K> keySet() {
        return this.cache.keySet();
    }

    @Override // java.util.Map
    public int size() {
        return this.cache.size();
    }
}
