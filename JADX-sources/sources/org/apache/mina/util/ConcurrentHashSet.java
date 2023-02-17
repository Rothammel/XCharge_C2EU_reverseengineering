package org.apache.mina.util;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/* loaded from: classes.dex */
public class ConcurrentHashSet<E> extends MapBackedSet<E> {
    private static final long serialVersionUID = 8518578988740277828L;

    public ConcurrentHashSet() {
        super(new ConcurrentHashMap());
    }

    public ConcurrentHashSet(Collection<E> c) {
        super(new ConcurrentHashMap(), c);
    }

    @Override // org.apache.mina.util.MapBackedSet, java.util.AbstractCollection, java.util.Collection, java.util.Set
    public boolean add(E o) {
        Boolean answer = (Boolean) ((ConcurrentMap) this.map).putIfAbsent(o, Boolean.TRUE);
        return answer == null;
    }
}
