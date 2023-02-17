package org.apache.mina.core.buffer;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.EnumSet;
import java.util.Set;

/* loaded from: classes.dex */
public abstract class IoBuffer implements Comparable<IoBuffer> {
    private static IoBufferAllocator allocator = new SimpleBufferAllocator();
    private static boolean useDirectBuffer = false;

    public abstract byte[] array();

    public abstract int arrayOffset();

    public abstract CharBuffer asCharBuffer();

    public abstract DoubleBuffer asDoubleBuffer();

    public abstract FloatBuffer asFloatBuffer();

    public abstract InputStream asInputStream();

    public abstract IntBuffer asIntBuffer();

    public abstract LongBuffer asLongBuffer();

    public abstract OutputStream asOutputStream();

    public abstract IoBuffer asReadOnlyBuffer();

    public abstract ShortBuffer asShortBuffer();

    public abstract ByteBuffer buf();

    public abstract int capacity();

    public abstract IoBuffer capacity(int i);

    public abstract IoBuffer clear();

    public abstract IoBuffer compact();

    public abstract IoBuffer duplicate();

    public abstract IoBuffer expand(int i);

    public abstract IoBuffer expand(int i, int i2);

    public abstract IoBuffer fill(byte b, int i);

    public abstract IoBuffer fill(int i);

    public abstract IoBuffer fillAndReset(byte b, int i);

    public abstract IoBuffer fillAndReset(int i);

    public abstract IoBuffer flip();

    public abstract void free();

    public abstract byte get();

    public abstract byte get(int i);

    public abstract IoBuffer get(byte[] bArr);

    public abstract IoBuffer get(byte[] bArr, int i, int i2);

    public abstract char getChar();

    public abstract char getChar(int i);

    public abstract double getDouble();

    public abstract double getDouble(int i);

    public abstract <E extends Enum<E>> E getEnum(int i, Class<E> cls);

    public abstract <E extends Enum<E>> E getEnum(Class<E> cls);

    public abstract <E extends Enum<E>> E getEnumInt(int i, Class<E> cls);

    public abstract <E extends Enum<E>> E getEnumInt(Class<E> cls);

    public abstract <E extends Enum<E>> EnumSet<E> getEnumSet(int i, Class<E> cls);

    public abstract <E extends Enum<E>> EnumSet<E> getEnumSet(Class<E> cls);

    public abstract <E extends Enum<E>> EnumSet<E> getEnumSetInt(int i, Class<E> cls);

    public abstract <E extends Enum<E>> EnumSet<E> getEnumSetInt(Class<E> cls);

    public abstract <E extends Enum<E>> EnumSet<E> getEnumSetLong(int i, Class<E> cls);

    public abstract <E extends Enum<E>> EnumSet<E> getEnumSetLong(Class<E> cls);

    public abstract <E extends Enum<E>> EnumSet<E> getEnumSetShort(int i, Class<E> cls);

    public abstract <E extends Enum<E>> EnumSet<E> getEnumSetShort(Class<E> cls);

    public abstract <E extends Enum<E>> E getEnumShort(int i, Class<E> cls);

    public abstract <E extends Enum<E>> E getEnumShort(Class<E> cls);

    public abstract float getFloat();

    public abstract float getFloat(int i);

    public abstract String getHexDump();

    public abstract String getHexDump(int i);

    public abstract int getInt();

    public abstract int getInt(int i);

    public abstract long getLong();

    public abstract long getLong(int i);

    public abstract int getMediumInt();

    public abstract int getMediumInt(int i);

    public abstract Object getObject() throws ClassNotFoundException;

    public abstract Object getObject(ClassLoader classLoader) throws ClassNotFoundException;

    public abstract String getPrefixedString(int i, CharsetDecoder charsetDecoder) throws CharacterCodingException;

    public abstract String getPrefixedString(CharsetDecoder charsetDecoder) throws CharacterCodingException;

    public abstract short getShort();

    public abstract short getShort(int i);

    public abstract IoBuffer getSlice(int i);

    public abstract IoBuffer getSlice(int i, int i2);

    public abstract String getString(int i, CharsetDecoder charsetDecoder) throws CharacterCodingException;

    public abstract String getString(CharsetDecoder charsetDecoder) throws CharacterCodingException;

    public abstract short getUnsigned();

    public abstract short getUnsigned(int i);

    public abstract long getUnsignedInt();

    public abstract long getUnsignedInt(int i);

    public abstract int getUnsignedMediumInt();

    public abstract int getUnsignedMediumInt(int i);

    public abstract int getUnsignedShort();

    public abstract int getUnsignedShort(int i);

    public abstract boolean hasArray();

    public abstract boolean hasRemaining();

    public abstract int indexOf(byte b);

    public abstract boolean isAutoExpand();

    public abstract boolean isAutoShrink();

    public abstract boolean isDerived();

    public abstract boolean isDirect();

    public abstract boolean isReadOnly();

    public abstract int limit();

    public abstract IoBuffer limit(int i);

    public abstract IoBuffer mark();

    public abstract int markValue();

    public abstract int minimumCapacity();

    public abstract IoBuffer minimumCapacity(int i);

    public abstract ByteOrder order();

    public abstract IoBuffer order(ByteOrder byteOrder);

    public abstract int position();

    public abstract IoBuffer position(int i);

    public abstract boolean prefixedDataAvailable(int i);

    public abstract boolean prefixedDataAvailable(int i, int i2);

    public abstract IoBuffer put(byte b);

    public abstract IoBuffer put(int i, byte b);

    public abstract IoBuffer put(ByteBuffer byteBuffer);

    public abstract IoBuffer put(IoBuffer ioBuffer);

    public abstract IoBuffer put(byte[] bArr);

    public abstract IoBuffer put(byte[] bArr, int i, int i2);

    public abstract IoBuffer putChar(char c);

    public abstract IoBuffer putChar(int i, char c);

    public abstract IoBuffer putDouble(double d);

    public abstract IoBuffer putDouble(int i, double d);

    public abstract IoBuffer putEnum(int i, Enum<?> r2);

    public abstract IoBuffer putEnum(Enum<?> r1);

    public abstract IoBuffer putEnumInt(int i, Enum<?> r2);

    public abstract IoBuffer putEnumInt(Enum<?> r1);

    public abstract <E extends Enum<E>> IoBuffer putEnumSet(int i, Set<E> set);

    public abstract <E extends Enum<E>> IoBuffer putEnumSet(Set<E> set);

    public abstract <E extends Enum<E>> IoBuffer putEnumSetInt(int i, Set<E> set);

    public abstract <E extends Enum<E>> IoBuffer putEnumSetInt(Set<E> set);

    public abstract <E extends Enum<E>> IoBuffer putEnumSetLong(int i, Set<E> set);

    public abstract <E extends Enum<E>> IoBuffer putEnumSetLong(Set<E> set);

    public abstract <E extends Enum<E>> IoBuffer putEnumSetShort(int i, Set<E> set);

    public abstract <E extends Enum<E>> IoBuffer putEnumSetShort(Set<E> set);

    public abstract IoBuffer putEnumShort(int i, Enum<?> r2);

    public abstract IoBuffer putEnumShort(Enum<?> r1);

    public abstract IoBuffer putFloat(float f);

    public abstract IoBuffer putFloat(int i, float f);

    public abstract IoBuffer putInt(int i);

    public abstract IoBuffer putInt(int i, int i2);

    public abstract IoBuffer putLong(int i, long j);

    public abstract IoBuffer putLong(long j);

    public abstract IoBuffer putMediumInt(int i);

    public abstract IoBuffer putMediumInt(int i, int i2);

    public abstract IoBuffer putObject(Object obj);

    public abstract IoBuffer putPrefixedString(CharSequence charSequence, int i, int i2, byte b, CharsetEncoder charsetEncoder) throws CharacterCodingException;

    public abstract IoBuffer putPrefixedString(CharSequence charSequence, int i, int i2, CharsetEncoder charsetEncoder) throws CharacterCodingException;

    public abstract IoBuffer putPrefixedString(CharSequence charSequence, int i, CharsetEncoder charsetEncoder) throws CharacterCodingException;

    public abstract IoBuffer putPrefixedString(CharSequence charSequence, CharsetEncoder charsetEncoder) throws CharacterCodingException;

    public abstract IoBuffer putShort(int i, short s);

    public abstract IoBuffer putShort(short s);

    public abstract IoBuffer putString(CharSequence charSequence, int i, CharsetEncoder charsetEncoder) throws CharacterCodingException;

    public abstract IoBuffer putString(CharSequence charSequence, CharsetEncoder charsetEncoder) throws CharacterCodingException;

    public abstract IoBuffer putUnsigned(byte b);

    public abstract IoBuffer putUnsigned(int i);

    public abstract IoBuffer putUnsigned(int i, byte b);

    public abstract IoBuffer putUnsigned(int i, int i2);

    public abstract IoBuffer putUnsigned(int i, long j);

    public abstract IoBuffer putUnsigned(int i, short s);

    public abstract IoBuffer putUnsigned(long j);

    public abstract IoBuffer putUnsigned(short s);

    public abstract IoBuffer putUnsignedInt(byte b);

    public abstract IoBuffer putUnsignedInt(int i);

    public abstract IoBuffer putUnsignedInt(int i, byte b);

    public abstract IoBuffer putUnsignedInt(int i, int i2);

    public abstract IoBuffer putUnsignedInt(int i, long j);

    public abstract IoBuffer putUnsignedInt(int i, short s);

    public abstract IoBuffer putUnsignedInt(long j);

    public abstract IoBuffer putUnsignedInt(short s);

    public abstract IoBuffer putUnsignedShort(byte b);

    public abstract IoBuffer putUnsignedShort(int i);

    public abstract IoBuffer putUnsignedShort(int i, byte b);

    public abstract IoBuffer putUnsignedShort(int i, int i2);

    public abstract IoBuffer putUnsignedShort(int i, long j);

    public abstract IoBuffer putUnsignedShort(int i, short s);

    public abstract IoBuffer putUnsignedShort(long j);

    public abstract IoBuffer putUnsignedShort(short s);

    public abstract int remaining();

    public abstract IoBuffer reset();

    public abstract IoBuffer rewind();

    public abstract IoBuffer setAutoExpand(boolean z);

    public abstract IoBuffer setAutoShrink(boolean z);

    public abstract IoBuffer shrink();

    public abstract IoBuffer skip(int i);

    public abstract IoBuffer slice();

    public abstract IoBuffer sweep();

    public abstract IoBuffer sweep(byte b);

    public static IoBufferAllocator getAllocator() {
        return allocator;
    }

    public static void setAllocator(IoBufferAllocator newAllocator) {
        if (newAllocator == null) {
            throw new IllegalArgumentException("allocator");
        }
        IoBufferAllocator oldAllocator = allocator;
        allocator = newAllocator;
        if (oldAllocator != null) {
            oldAllocator.dispose();
        }
    }

    public static boolean isUseDirectBuffer() {
        return useDirectBuffer;
    }

    public static void setUseDirectBuffer(boolean useDirectBuffer2) {
        useDirectBuffer = useDirectBuffer2;
    }

    public static IoBuffer allocate(int capacity) {
        return allocate(capacity, useDirectBuffer);
    }

    public static IoBuffer allocate(int capacity, boolean useDirectBuffer2) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity: " + capacity);
        }
        return allocator.allocate(capacity, useDirectBuffer2);
    }

    public static IoBuffer wrap(ByteBuffer nioBuffer) {
        return allocator.wrap(nioBuffer);
    }

    public static IoBuffer wrap(byte[] byteArray) {
        return wrap(ByteBuffer.wrap(byteArray));
    }

    public static IoBuffer wrap(byte[] byteArray, int offset, int length) {
        return wrap(ByteBuffer.wrap(byteArray, offset, length));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static int normalizeCapacity(int requestedCapacity) {
        if (requestedCapacity < 0) {
            return Integer.MAX_VALUE;
        }
        int newCapacity = Integer.highestOneBit(requestedCapacity);
        int newCapacity2 = newCapacity << (newCapacity < requestedCapacity ? 1 : 0);
        if (newCapacity2 < 0) {
            newCapacity2 = Integer.MAX_VALUE;
        }
        return newCapacity2;
    }
}