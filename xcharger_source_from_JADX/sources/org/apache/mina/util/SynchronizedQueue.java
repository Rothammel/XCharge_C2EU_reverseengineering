package org.apache.mina.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

public class SynchronizedQueue<E> implements Queue<E>, Serializable {
    private static final long serialVersionUID = -1439242290701194806L;

    /* renamed from: q */
    private final Queue<E> f197q;

    public SynchronizedQueue(Queue<E> q) {
        this.f197q = q;
    }

    public synchronized boolean add(E e) {
        return this.f197q.add(e);
    }

    public synchronized E element() {
        return this.f197q.element();
    }

    public synchronized boolean offer(E e) {
        return this.f197q.offer(e);
    }

    public synchronized E peek() {
        return this.f197q.peek();
    }

    public synchronized E poll() {
        return this.f197q.poll();
    }

    public synchronized E remove() {
        return this.f197q.remove();
    }

    public synchronized boolean addAll(Collection<? extends E> c) {
        return this.f197q.addAll(c);
    }

    public synchronized void clear() {
        this.f197q.clear();
    }

    public synchronized boolean contains(Object o) {
        return this.f197q.contains(o);
    }

    public synchronized boolean containsAll(Collection<?> c) {
        return this.f197q.containsAll(c);
    }

    public synchronized boolean isEmpty() {
        return this.f197q.isEmpty();
    }

    public synchronized Iterator<E> iterator() {
        return this.f197q.iterator();
    }

    public synchronized boolean remove(Object o) {
        return this.f197q.remove(o);
    }

    public synchronized boolean removeAll(Collection<?> c) {
        return this.f197q.removeAll(c);
    }

    public synchronized boolean retainAll(Collection<?> c) {
        return this.f197q.retainAll(c);
    }

    public synchronized int size() {
        return this.f197q.size();
    }

    public synchronized Object[] toArray() {
        return this.f197q.toArray();
    }

    public synchronized <T> T[] toArray(T[] a) {
        return this.f197q.toArray(a);
    }

    public synchronized boolean equals(Object obj) {
        return this.f197q.equals(obj);
    }

    public synchronized int hashCode() {
        return this.f197q.hashCode();
    }

    public synchronized String toString() {
        return this.f197q.toString();
    }
}
