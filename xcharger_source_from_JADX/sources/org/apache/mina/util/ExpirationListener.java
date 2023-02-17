package org.apache.mina.util;

public interface ExpirationListener<E> {
    void expired(E e);
}
