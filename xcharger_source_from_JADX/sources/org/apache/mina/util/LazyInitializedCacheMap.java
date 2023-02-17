package org.apache.mina.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LazyInitializedCacheMap<K, V> implements Map<K, V> {
    private ConcurrentMap<K, LazyInitializer<V>> cache;

    public class NoopInitializer extends LazyInitializer<V> {
        private V value;

        public NoopInitializer(V value2) {
            this.value = value2;
        }

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

    public V get(Object key) {
        LazyInitializer<V> c = (LazyInitializer) this.cache.get(key);
        if (c != null) {
            return c.get();
        }
        return null;
    }

    public V remove(Object key) {
        LazyInitializer<V> c = (LazyInitializer) this.cache.remove(key);
        if (c != null) {
            return c.get();
        }
        return null;
    }

    public V putIfAbsent(K key, LazyInitializer<V> value) {
        LazyInitializer<V> v = (LazyInitializer) this.cache.get(key);
        if (v == null && (v = (LazyInitializer) this.cache.putIfAbsent(key, value)) == null) {
            return value.get();
        }
        return v.get();
    }

    public V put(K key, V value) {
        LazyInitializer<V> c = (LazyInitializer) this.cache.put(key, new NoopInitializer(value));
        if (c != null) {
            return c.get();
        }
        return null;
    }

    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    public Set<Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            this.cache.put(e.getKey(), new NoopInitializer(e.getValue()));
        }
    }

    public Collection<LazyInitializer<V>> getValues() {
        return this.cache.values();
    }

    public void clear() {
        this.cache.clear();
    }

    public boolean containsKey(Object key) {
        return this.cache.containsKey(key);
    }

    public boolean isEmpty() {
        return this.cache.isEmpty();
    }

    public Set<K> keySet() {
        return this.cache.keySet();
    }

    public int size() {
        return this.cache.size();
    }
}
