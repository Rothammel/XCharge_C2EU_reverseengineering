package org.apache.mina.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

/* loaded from: classes.dex */
public class SynchronizedQueue<E> implements Queue<E>, Serializable {
    private static final long serialVersionUID = -1439242290701194806L;
    private final Queue<E> q;

    public SynchronizedQueue(Queue<E> q) {
        this.q = q;
    }

    @Override // java.util.Queue, java.util.Collection
    public synchronized boolean add(E e) {
        return this.q.add(e);
    }

    @Override // java.util.Queue
    public synchronized E element() {
        return this.q.element();
    }

    @Override // java.util.Queue
    public synchronized boolean offer(E e) {
        return this.q.offer(e);
    }

    @Override // java.util.Queue
    public synchronized E peek() {
        return this.q.peek();
    }

    @Override // java.util.Queue
    public synchronized E poll() {
        return this.q.poll();
    }

    @Override // java.util.Queue
    public synchronized E remove() {
        return this.q.remove();
    }

    @Override // java.util.Collection
    public synchronized boolean addAll(Collection<? extends E> c) {
        return this.q.addAll(c);
    }

    @Override // java.util.Collection
    public synchronized void clear() {
        this.q.clear();
    }

    @Override // java.util.Collection
    public synchronized boolean contains(Object o) {
        return this.q.contains(o);
    }

    @Override // java.util.Collection
    public synchronized boolean containsAll(Collection<?> c) {
        return this.q.containsAll(c);
    }

    @Override // java.util.Collection
    public synchronized boolean isEmpty() {
        return this.q.isEmpty();
    }

    @Override // java.util.Collection, java.lang.Iterable
    public synchronized Iterator<E> iterator() {
        return this.q.iterator();
    }

    @Override // java.util.Collection
    public synchronized boolean remove(Object o) {
        return this.q.remove(o);
    }

    @Override // java.util.Collection
    public synchronized boolean removeAll(Collection<?> c) {
        return this.q.removeAll(c);
    }

    @Override // java.util.Collection
    public synchronized boolean retainAll(Collection<?> c) {
        return this.q.retainAll(c);
    }

    @Override // java.util.Collection
    public synchronized int size() {
        return this.q.size();
    }

    @Override // java.util.Collection
    public synchronized Object[] toArray() {
        return this.q.toArray();
    }

    @Override // java.util.Collection
    public synchronized <T> T[] toArray(T[] a) {
        return (T[]) this.q.toArray(a);
    }

    @Override // java.util.Collection
    public synchronized boolean equals(Object obj) {
        return this.q.equals(obj);
    }

    @Override // java.util.Collection
    public synchronized int hashCode() {
        return this.q.hashCode();
    }

    public synchronized String toString() {
        return this.q.toString();
    }
}