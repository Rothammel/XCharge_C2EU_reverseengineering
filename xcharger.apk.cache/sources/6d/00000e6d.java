package org.apache.mina.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/* loaded from: classes.dex */
public class CopyOnWriteMap<K, V> implements Map<K, V>, Cloneable {
    private volatile Map<K, V> internalMap;

    public CopyOnWriteMap() {
        this.internalMap = new HashMap();
    }

    public CopyOnWriteMap(int initialCapacity) {
        this.internalMap = new HashMap(initialCapacity);
    }

    public CopyOnWriteMap(Map<K, V> data) {
        this.internalMap = new HashMap(data);
    }

    @Override // java.util.Map
    public V put(K key, V value) {
        V val;
        synchronized (this) {
            Map<K, V> newMap = new HashMap<>(this.internalMap);
            val = newMap.put(key, value);
            this.internalMap = newMap;
        }
        return val;
    }

    @Override // java.util.Map
    public V remove(Object key) {
        V val;
        synchronized (this) {
            Map<K, V> newMap = new HashMap<>(this.internalMap);
            val = newMap.remove(key);
            this.internalMap = newMap;
        }
        return val;
    }

    @Override // java.util.Map
    public void putAll(Map<? extends K, ? extends V> newData) {
        synchronized (this) {
            Map<K, V> newMap = new HashMap<>(this.internalMap);
            newMap.putAll(newData);
            this.internalMap = newMap;
        }
    }

    @Override // java.util.Map
    public void clear() {
        synchronized (this) {
            this.internalMap = new HashMap();
        }
    }

    @Override // java.util.Map
    public int size() {
        return this.internalMap.size();
    }

    @Override // java.util.Map
    public boolean isEmpty() {
        return this.internalMap.isEmpty();
    }

    @Override // java.util.Map
    public boolean containsKey(Object key) {
        return this.internalMap.containsKey(key);
    }

    @Override // java.util.Map
    public boolean containsValue(Object value) {
        return this.internalMap.containsValue(value);
    }

    @Override // java.util.Map
    public V get(Object key) {
        return this.internalMap.get(key);
    }

    @Override // java.util.Map
    public Set<K> keySet() {
        return this.internalMap.keySet();
    }

    @Override // java.util.Map
    public Collection<V> values() {
        return this.internalMap.values();
    }

    @Override // java.util.Map
    public Set<Map.Entry<K, V>> entrySet() {
        return this.internalMap.entrySet();
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}