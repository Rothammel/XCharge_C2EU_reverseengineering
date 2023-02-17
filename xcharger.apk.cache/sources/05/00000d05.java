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
public class IoBufferWrapper extends IoBuffer {
    private final IoBuffer buf;

    /* JADX INFO: Access modifiers changed from: protected */
    public IoBufferWrapper(IoBuffer buf) {
        if (buf == null) {
            throw new IllegalArgumentException("buf");
        }
        this.buf = buf;
    }

    public IoBuffer getParentBuffer() {
        return this.buf;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public boolean isDirect() {
        return this.buf.isDirect();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public ByteBuffer buf() {
        return this.buf.buf();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int capacity() {
        return this.buf.capacity();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int position() {
        return this.buf.position();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer position(int newPosition) {
        this.buf.position(newPosition);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int limit() {
        return this.buf.limit();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer limit(int newLimit) {
        this.buf.limit(newLimit);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer mark() {
        this.buf.mark();
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer reset() {
        this.buf.reset();
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer clear() {
        this.buf.clear();
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer sweep() {
        this.buf.sweep();
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer sweep(byte value) {
        this.buf.sweep(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer flip() {
        this.buf.flip();
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer rewind() {
        this.buf.rewind();
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int remaining() {
        return this.buf.remaining();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public boolean hasRemaining() {
        return this.buf.hasRemaining();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public byte get() {
        return this.buf.get();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public short getUnsigned() {
        return this.buf.getUnsigned();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer put(byte b) {
        this.buf.put(b);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public byte get(int index) {
        return this.buf.get(index);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public short getUnsigned(int index) {
        return this.buf.getUnsigned(index);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer put(int index, byte b) {
        this.buf.put(index, b);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer get(byte[] dst, int offset, int length) {
        this.buf.get(dst, offset, length);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer getSlice(int index, int length) {
        return this.buf.getSlice(index, length);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer getSlice(int length) {
        return this.buf.getSlice(length);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer get(byte[] dst) {
        this.buf.get(dst);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer put(IoBuffer src) {
        this.buf.put(src);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer put(ByteBuffer src) {
        this.buf.put(src);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer put(byte[] src, int offset, int length) {
        this.buf.put(src, offset, length);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer put(byte[] src) {
        this.buf.put(src);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer compact() {
        this.buf.compact();
        return this;
    }

    public String toString() {
        return this.buf.toString();
    }

    public int hashCode() {
        return this.buf.hashCode();
    }

    public boolean equals(Object ob) {
        return this.buf.equals(ob);
    }

    @Override // java.lang.Comparable
    public int compareTo(IoBuffer that) {
        return this.buf.compareTo(that);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public ByteOrder order() {
        return this.buf.order();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer order(ByteOrder bo) {
        this.buf.order(bo);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public char getChar() {
        return this.buf.getChar();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putChar(char value) {
        this.buf.putChar(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public char getChar(int index) {
        return this.buf.getChar(index);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putChar(int index, char value) {
        this.buf.putChar(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public CharBuffer asCharBuffer() {
        return this.buf.asCharBuffer();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public short getShort() {
        return this.buf.getShort();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int getUnsignedShort() {
        return this.buf.getUnsignedShort();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putShort(short value) {
        this.buf.putShort(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public short getShort(int index) {
        return this.buf.getShort(index);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int getUnsignedShort(int index) {
        return this.buf.getUnsignedShort(index);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putShort(int index, short value) {
        this.buf.putShort(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public ShortBuffer asShortBuffer() {
        return this.buf.asShortBuffer();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int getInt() {
        return this.buf.getInt();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public long getUnsignedInt() {
        return this.buf.getUnsignedInt();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putInt(int value) {
        this.buf.putInt(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsignedInt(byte value) {
        this.buf.putUnsignedInt(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsignedInt(int index, byte value) {
        this.buf.putUnsignedInt(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsignedInt(short value) {
        this.buf.putUnsignedInt(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsignedInt(int index, short value) {
        this.buf.putUnsignedInt(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsignedInt(int value) {
        this.buf.putUnsignedInt(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsignedInt(int index, int value) {
        this.buf.putUnsignedInt(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsignedInt(long value) {
        this.buf.putUnsignedInt(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsignedInt(int index, long value) {
        this.buf.putUnsignedInt(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsignedShort(byte value) {
        this.buf.putUnsignedShort(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsignedShort(int index, byte value) {
        this.buf.putUnsignedShort(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsignedShort(short value) {
        this.buf.putUnsignedShort(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsignedShort(int index, short value) {
        this.buf.putUnsignedShort(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsignedShort(int value) {
        this.buf.putUnsignedShort(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsignedShort(int index, int value) {
        this.buf.putUnsignedShort(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsignedShort(long value) {
        this.buf.putUnsignedShort(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsignedShort(int index, long value) {
        this.buf.putUnsignedShort(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int getInt(int index) {
        return this.buf.getInt(index);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public long getUnsignedInt(int index) {
        return this.buf.getUnsignedInt(index);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putInt(int index, int value) {
        this.buf.putInt(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IntBuffer asIntBuffer() {
        return this.buf.asIntBuffer();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public long getLong() {
        return this.buf.getLong();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putLong(long value) {
        this.buf.putLong(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public long getLong(int index) {
        return this.buf.getLong(index);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putLong(int index, long value) {
        this.buf.putLong(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public LongBuffer asLongBuffer() {
        return this.buf.asLongBuffer();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public float getFloat() {
        return this.buf.getFloat();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putFloat(float value) {
        this.buf.putFloat(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public float getFloat(int index) {
        return this.buf.getFloat(index);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putFloat(int index, float value) {
        this.buf.putFloat(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public FloatBuffer asFloatBuffer() {
        return this.buf.asFloatBuffer();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public double getDouble() {
        return this.buf.getDouble();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putDouble(double value) {
        this.buf.putDouble(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public double getDouble(int index) {
        return this.buf.getDouble(index);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putDouble(int index, double value) {
        this.buf.putDouble(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public DoubleBuffer asDoubleBuffer() {
        return this.buf.asDoubleBuffer();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public String getHexDump() {
        return this.buf.getHexDump();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public String getString(int fieldSize, CharsetDecoder decoder) throws CharacterCodingException {
        return this.buf.getString(fieldSize, decoder);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public String getString(CharsetDecoder decoder) throws CharacterCodingException {
        return this.buf.getString(decoder);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public String getPrefixedString(CharsetDecoder decoder) throws CharacterCodingException {
        return this.buf.getPrefixedString(decoder);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public String getPrefixedString(int prefixLength, CharsetDecoder decoder) throws CharacterCodingException {
        return this.buf.getPrefixedString(prefixLength, decoder);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putString(CharSequence in, int fieldSize, CharsetEncoder encoder) throws CharacterCodingException {
        this.buf.putString(in, fieldSize, encoder);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putString(CharSequence in, CharsetEncoder encoder) throws CharacterCodingException {
        this.buf.putString(in, encoder);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putPrefixedString(CharSequence in, CharsetEncoder encoder) throws CharacterCodingException {
        this.buf.putPrefixedString(in, encoder);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putPrefixedString(CharSequence in, int prefixLength, CharsetEncoder encoder) throws CharacterCodingException {
        this.buf.putPrefixedString(in, prefixLength, encoder);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putPrefixedString(CharSequence in, int prefixLength, int padding, CharsetEncoder encoder) throws CharacterCodingException {
        this.buf.putPrefixedString(in, prefixLength, padding, encoder);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putPrefixedString(CharSequence in, int prefixLength, int padding, byte padValue, CharsetEncoder encoder) throws CharacterCodingException {
        this.buf.putPrefixedString(in, prefixLength, padding, padValue, encoder);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer skip(int size) {
        this.buf.skip(size);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer fill(byte value, int size) {
        this.buf.fill(value, size);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer fillAndReset(byte value, int size) {
        this.buf.fillAndReset(value, size);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer fill(int size) {
        this.buf.fill(size);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer fillAndReset(int size) {
        this.buf.fillAndReset(size);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public boolean isAutoExpand() {
        return this.buf.isAutoExpand();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer setAutoExpand(boolean autoExpand) {
        this.buf.setAutoExpand(autoExpand);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer expand(int pos, int expectedRemaining) {
        this.buf.expand(pos, expectedRemaining);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer expand(int expectedRemaining) {
        this.buf.expand(expectedRemaining);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public Object getObject() throws ClassNotFoundException {
        return this.buf.getObject();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public Object getObject(ClassLoader classLoader) throws ClassNotFoundException {
        return this.buf.getObject(classLoader);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putObject(Object o) {
        this.buf.putObject(o);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public InputStream asInputStream() {
        return this.buf.asInputStream();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public OutputStream asOutputStream() {
        return this.buf.asOutputStream();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer duplicate() {
        return this.buf.duplicate();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer slice() {
        return this.buf.slice();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer asReadOnlyBuffer() {
        return this.buf.asReadOnlyBuffer();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public byte[] array() {
        return this.buf.array();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int arrayOffset() {
        return this.buf.arrayOffset();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int minimumCapacity() {
        return this.buf.minimumCapacity();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer minimumCapacity(int minimumCapacity) {
        this.buf.minimumCapacity(minimumCapacity);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer capacity(int newCapacity) {
        this.buf.capacity(newCapacity);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public boolean isReadOnly() {
        return this.buf.isReadOnly();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int markValue() {
        return this.buf.markValue();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public boolean hasArray() {
        return this.buf.hasArray();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public void free() {
        this.buf.free();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public boolean isDerived() {
        return this.buf.isDerived();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public boolean isAutoShrink() {
        return this.buf.isAutoShrink();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer setAutoShrink(boolean autoShrink) {
        this.buf.setAutoShrink(autoShrink);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer shrink() {
        this.buf.shrink();
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int getMediumInt() {
        return this.buf.getMediumInt();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int getUnsignedMediumInt() {
        return this.buf.getUnsignedMediumInt();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int getMediumInt(int index) {
        return this.buf.getMediumInt(index);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int getUnsignedMediumInt(int index) {
        return this.buf.getUnsignedMediumInt(index);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putMediumInt(int value) {
        this.buf.putMediumInt(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putMediumInt(int index, int value) {
        this.buf.putMediumInt(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public String getHexDump(int lengthLimit) {
        return this.buf.getHexDump(lengthLimit);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public boolean prefixedDataAvailable(int prefixLength) {
        return this.buf.prefixedDataAvailable(prefixLength);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public boolean prefixedDataAvailable(int prefixLength, int maxDataLength) {
        return this.buf.prefixedDataAvailable(prefixLength, maxDataLength);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int indexOf(byte b) {
        return this.buf.indexOf(b);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> E getEnum(Class<E> enumClass) {
        return (E) this.buf.getEnum(enumClass);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> E getEnum(int index, Class<E> enumClass) {
        return (E) this.buf.getEnum(index, enumClass);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> E getEnumShort(Class<E> enumClass) {
        return (E) this.buf.getEnumShort(enumClass);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> E getEnumShort(int index, Class<E> enumClass) {
        return (E) this.buf.getEnumShort(index, enumClass);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> E getEnumInt(Class<E> enumClass) {
        return (E) this.buf.getEnumInt(enumClass);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> E getEnumInt(int index, Class<E> enumClass) {
        return (E) this.buf.getEnumInt(index, enumClass);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putEnum(Enum<?> e) {
        this.buf.putEnum(e);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putEnum(int index, Enum<?> e) {
        this.buf.putEnum(index, e);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putEnumShort(Enum<?> e) {
        this.buf.putEnumShort(e);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putEnumShort(int index, Enum<?> e) {
        this.buf.putEnumShort(index, e);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putEnumInt(Enum<?> e) {
        this.buf.putEnumInt(e);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putEnumInt(int index, Enum<?> e) {
        this.buf.putEnumInt(index, e);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> EnumSet<E> getEnumSet(Class<E> enumClass) {
        return this.buf.getEnumSet(enumClass);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> EnumSet<E> getEnumSet(int index, Class<E> enumClass) {
        return this.buf.getEnumSet(index, enumClass);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> EnumSet<E> getEnumSetShort(Class<E> enumClass) {
        return this.buf.getEnumSetShort(enumClass);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> EnumSet<E> getEnumSetShort(int index, Class<E> enumClass) {
        return this.buf.getEnumSetShort(index, enumClass);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> EnumSet<E> getEnumSetInt(Class<E> enumClass) {
        return this.buf.getEnumSetInt(enumClass);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> EnumSet<E> getEnumSetInt(int index, Class<E> enumClass) {
        return this.buf.getEnumSetInt(index, enumClass);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> EnumSet<E> getEnumSetLong(Class<E> enumClass) {
        return this.buf.getEnumSetLong(enumClass);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> EnumSet<E> getEnumSetLong(int index, Class<E> enumClass) {
        return this.buf.getEnumSetLong(index, enumClass);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> IoBuffer putEnumSet(Set<E> set) {
        this.buf.putEnumSet(set);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> IoBuffer putEnumSet(int index, Set<E> set) {
        this.buf.putEnumSet(index, set);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> IoBuffer putEnumSetShort(Set<E> set) {
        this.buf.putEnumSetShort(set);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> IoBuffer putEnumSetShort(int index, Set<E> set) {
        this.buf.putEnumSetShort(index, set);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> IoBuffer putEnumSetInt(Set<E> set) {
        this.buf.putEnumSetInt(set);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> IoBuffer putEnumSetInt(int index, Set<E> set) {
        this.buf.putEnumSetInt(index, set);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> IoBuffer putEnumSetLong(Set<E> set) {
        this.buf.putEnumSetLong(set);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> IoBuffer putEnumSetLong(int index, Set<E> set) {
        this.buf.putEnumSetLong(index, set);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsigned(byte value) {
        this.buf.putUnsigned(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsigned(int index, byte value) {
        this.buf.putUnsigned(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsigned(short value) {
        this.buf.putUnsigned(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsigned(int index, short value) {
        this.buf.putUnsigned(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsigned(int value) {
        this.buf.putUnsigned(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsigned(int index, int value) {
        this.buf.putUnsigned(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsigned(long value) {
        this.buf.putUnsigned(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsigned(int index, long value) {
        this.buf.putUnsigned(index, value);
        return this;
    }
}