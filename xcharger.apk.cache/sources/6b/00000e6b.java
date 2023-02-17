package org.apache.mina.util;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Queue;
import org.apache.mina.proxy.handlers.http.ntlm.NTLMConstants;

/* loaded from: classes.dex */
public class CircularQueue<E> extends AbstractList<E> implements Queue<E>, Serializable {
    private static final int DEFAULT_CAPACITY = 4;
    private static final long serialVersionUID = 3993421269224511264L;
    private int first;
    private boolean full;
    private final int initialCapacity;
    private volatile Object[] items;
    private int last;
    private int mask;
    private int shrinkThreshold;

    public CircularQueue() {
        this(4);
    }

    public CircularQueue(int initialCapacity) {
        this.first = 0;
        this.last = 0;
        int actualCapacity = normalizeCapacity(initialCapacity);
        this.items = new Object[actualCapacity];
        this.mask = actualCapacity - 1;
        this.initialCapacity = actualCapacity;
        this.shrinkThreshold = 0;
    }

    private static int normalizeCapacity(int initialCapacity) {
        int actualCapacity = 1;
        while (actualCapacity < initialCapacity) {
            actualCapacity <<= 1;
            if (actualCapacity < 0) {
                return NTLMConstants.FLAG_NEGOTIATE_KEY_EXCHANGE;
            }
        }
        return actualCapacity;
    }

    public int capacity() {
        return this.items.length;
    }

    @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.Collection, java.util.List
    public void clear() {
        if (!isEmpty()) {
            Arrays.fill(this.items, (Object) null);
            this.first = 0;
            this.last = 0;
            this.full = false;
            shrinkIfNeeded();
        }
    }

    @Override // java.util.Queue
    public E poll() {
        if (isEmpty()) {
            return null;
        }
        E e = (E) this.items[this.first];
        this.items[this.first] = null;
        decreaseSize();
        if (this.first == this.last) {
            this.last = 0;
            this.first = 0;
        }
        shrinkIfNeeded();
        return e;
    }

    @Override // java.util.Queue
    public boolean offer(E item) {
        if (item == null) {
            throw new IllegalArgumentException("item");
        }
        expandIfNeeded();
        this.items[this.last] = item;
        increaseSize();
        return true;
    }

    @Override // java.util.Queue
    public E peek() {
        if (isEmpty()) {
            return null;
        }
        return (E) this.items[this.first];
    }

    @Override // java.util.AbstractList, java.util.List
    public E get(int idx) {
        checkIndex(idx);
        return (E) this.items[getRealIndex(idx)];
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
    public boolean isEmpty() {
        return this.first == this.last && !this.full;
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
    public int size() {
        if (this.full) {
            return capacity();
        }
        if (this.last >= this.first) {
            return this.last - this.first;
        }
        return (this.last - this.first) + capacity();
    }

    @Override // java.util.AbstractCollection
    public String toString() {
        return "first=" + this.first + ", last=" + this.last + ", size=" + size() + ", mask = " + this.mask;
    }

    private void checkIndex(int idx) {
        if (idx < 0 || idx >= size()) {
            throw new IndexOutOfBoundsException(String.valueOf(idx));
        }
    }

    private int getRealIndex(int idx) {
        return (this.first + idx) & this.mask;
    }

    private void increaseSize() {
        this.last = (this.last + 1) & this.mask;
        this.full = this.first == this.last;
    }

    private void decreaseSize() {
        this.first = (this.first + 1) & this.mask;
        this.full = false;
    }

    private void expandIfNeeded() {
        if (this.full) {
            int oldLen = this.items.length;
            int newLen = oldLen << 1;
            Object[] tmp = new Object[newLen];
            if (this.first < this.last) {
                System.arraycopy(this.items, this.first, tmp, 0, this.last - this.first);
            } else {
                System.arraycopy(this.items, this.first, tmp, 0, oldLen - this.first);
                System.arraycopy(this.items, 0, tmp, oldLen - this.first, this.last);
            }
            this.first = 0;
            this.last = oldLen;
            this.items = tmp;
            this.mask = tmp.length - 1;
            if ((newLen >>> 3) > this.initialCapacity) {
                this.shrinkThreshold = newLen >>> 3;
            }
        }
    }

    private void shrinkIfNeeded() {
        int size = size();
        if (size <= this.shrinkThreshold) {
            int oldLen = this.items.length;
            int newLen = normalizeCapacity(size);
            if (size == newLen) {
                newLen <<= 1;
            }
            if (newLen < oldLen) {
                if (newLen < this.initialCapacity) {
                    if (oldLen != this.initialCapacity) {
                        newLen = this.initialCapacity;
                    } else {
                        return;
                    }
                }
                Object[] tmp = new Object[newLen];
                if (size > 0) {
                    if (this.first < this.last) {
                        System.arraycopy(this.items, this.first, tmp, 0, this.last - this.first);
                    } else {
                        System.arraycopy(this.items, this.first, tmp, 0, oldLen - this.first);
                        System.arraycopy(this.items, 0, tmp, oldLen - this.first, this.last);
                    }
                }
                this.first = 0;
                this.last = size;
                this.items = tmp;
                this.mask = tmp.length - 1;
                this.shrinkThreshold = 0;
            }
        }
    }

    @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.Collection, java.util.List, java.util.Queue
    public boolean add(E o) {
        return offer(o);
    }

    @Override // java.util.AbstractList, java.util.List
    public E set(int idx, E o) {
        checkIndex(idx);
        int realIdx = getRealIndex(idx);
        E e = (E) this.items[realIdx];
        this.items[realIdx] = o;
        return e;
    }

    @Override // java.util.AbstractList, java.util.List
    public void add(int idx, E o) {
        if (idx == size()) {
            offer(o);
            return;
        }
        checkIndex(idx);
        expandIfNeeded();
        int realIdx = getRealIndex(idx);
        if (this.first < this.last) {
            System.arraycopy(this.items, realIdx, this.items, realIdx + 1, this.last - realIdx);
        } else if (realIdx >= this.first) {
            System.arraycopy(this.items, 0, this.items, 1, this.last);
            this.items[0] = this.items[this.items.length - 1];
            System.arraycopy(this.items, realIdx, this.items, realIdx + 1, (this.items.length - realIdx) - 1);
        } else {
            System.arraycopy(this.items, realIdx, this.items, realIdx + 1, this.last - realIdx);
        }
        this.items[realIdx] = o;
        increaseSize();
    }

    @Override // java.util.AbstractList, java.util.List
    public E remove(int idx) {
        if (idx == 0) {
            return poll();
        }
        checkIndex(idx);
        int realIdx = getRealIndex(idx);
        E e = (E) this.items[realIdx];
        if (this.first < this.last) {
            System.arraycopy(this.items, this.first, this.items, this.first + 1, realIdx - this.first);
        } else if (realIdx >= this.first) {
            System.arraycopy(this.items, this.first, this.items, this.first + 1, realIdx - this.first);
        } else {
            System.arraycopy(this.items, 0, this.items, 1, realIdx);
            this.items[0] = this.items[this.items.length - 1];
            System.arraycopy(this.items, this.first, this.items, this.first + 1, (this.items.length - this.first) - 1);
        }
        this.items[this.first] = null;
        decreaseSize();
        shrinkIfNeeded();
        return e;
    }

    @Override // java.util.Queue
    public E remove() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return poll();
    }

    @Override // java.util.Queue
    public E element() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return peek();
    }
}