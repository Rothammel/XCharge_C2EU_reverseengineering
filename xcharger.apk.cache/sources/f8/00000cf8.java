package org.apache.mina.core.buffer;

import android.support.v4.internal.view.SupportMenu;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
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
import java.nio.charset.CoderResult;
import java.util.EnumSet;
import java.util.Set;
import org.apache.commons.lang3.CharEncoding;

/* loaded from: classes.dex */
public abstract class AbstractIoBuffer extends IoBuffer {
    private static final long BYTE_MASK = 255;
    private static final long INT_MASK = 4294967295L;
    private static final long SHORT_MASK = 65535;
    private boolean autoExpand;
    private boolean autoShrink;
    private final boolean derived;
    private int mark = -1;
    private int minimumCapacity;
    private boolean recapacityAllowed;

    protected abstract IoBuffer asReadOnlyBuffer0();

    protected abstract void buf(ByteBuffer byteBuffer);

    protected abstract IoBuffer duplicate0();

    protected abstract IoBuffer slice0();

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractIoBuffer(IoBufferAllocator allocator, int initialCapacity) {
        this.recapacityAllowed = true;
        setAllocator(allocator);
        this.recapacityAllowed = true;
        this.derived = false;
        this.minimumCapacity = initialCapacity;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public AbstractIoBuffer(AbstractIoBuffer parent) {
        this.recapacityAllowed = true;
        setAllocator(IoBuffer.getAllocator());
        this.recapacityAllowed = false;
        this.derived = true;
        this.minimumCapacity = parent.minimumCapacity;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final boolean isDirect() {
        return buf().isDirect();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final boolean isReadOnly() {
        return buf().isReadOnly();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final int minimumCapacity() {
        return this.minimumCapacity;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer minimumCapacity(int minimumCapacity) {
        if (minimumCapacity < 0) {
            throw new IllegalArgumentException("minimumCapacity: " + minimumCapacity);
        }
        this.minimumCapacity = minimumCapacity;
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final int capacity() {
        return buf().capacity();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer capacity(int newCapacity) {
        if (!this.recapacityAllowed) {
            throw new IllegalStateException("Derived buffers and their parent can't be expanded.");
        }
        if (newCapacity > capacity()) {
            int pos = position();
            int limit = limit();
            ByteOrder bo = order();
            ByteBuffer oldBuf = buf();
            ByteBuffer newBuf = getAllocator().allocateNioBuffer(newCapacity, isDirect());
            oldBuf.clear();
            newBuf.put(oldBuf);
            buf(newBuf);
            buf().limit(limit);
            if (this.mark >= 0) {
                buf().position(this.mark);
                buf().mark();
            }
            buf().position(pos);
            buf().order(bo);
        }
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final boolean isAutoExpand() {
        return this.autoExpand && this.recapacityAllowed;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final boolean isAutoShrink() {
        return this.autoShrink && this.recapacityAllowed;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final boolean isDerived() {
        return this.derived;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer setAutoExpand(boolean autoExpand) {
        if (!this.recapacityAllowed) {
            throw new IllegalStateException("Derived buffers and their parent can't be expanded.");
        }
        this.autoExpand = autoExpand;
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer setAutoShrink(boolean autoShrink) {
        if (!this.recapacityAllowed) {
            throw new IllegalStateException("Derived buffers and their parent can't be shrinked.");
        }
        this.autoShrink = autoShrink;
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer expand(int expectedRemaining) {
        return expand(position(), expectedRemaining, false);
    }

    private IoBuffer expand(int expectedRemaining, boolean autoExpand) {
        return expand(position(), expectedRemaining, autoExpand);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer expand(int pos, int expectedRemaining) {
        return expand(pos, expectedRemaining, false);
    }

    private IoBuffer expand(int pos, int expectedRemaining, boolean autoExpand) {
        int newCapacity;
        if (!this.recapacityAllowed) {
            throw new IllegalStateException("Derived buffers and their parent can't be expanded.");
        }
        int end = pos + expectedRemaining;
        if (autoExpand) {
            newCapacity = IoBuffer.normalizeCapacity(end);
        } else {
            newCapacity = end;
        }
        if (newCapacity > capacity()) {
            capacity(newCapacity);
        }
        if (end > limit()) {
            buf().limit(end);
        }
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer shrink() {
        if (!this.recapacityAllowed) {
            throw new IllegalStateException("Derived buffers and their parent can't be expanded.");
        }
        int position = position();
        int capacity = capacity();
        int limit = limit();
        if (capacity != limit) {
            int newCapacity = capacity;
            int minCapacity = Math.max(this.minimumCapacity, limit);
            while ((newCapacity >>> 1) >= minCapacity) {
                newCapacity >>>= 1;
                if (minCapacity == 0) {
                    break;
                }
            }
            int newCapacity2 = Math.max(minCapacity, newCapacity);
            if (newCapacity2 != capacity) {
                ByteOrder bo = order();
                ByteBuffer oldBuf = buf();
                ByteBuffer newBuf = getAllocator().allocateNioBuffer(newCapacity2, isDirect());
                oldBuf.position(0);
                oldBuf.limit(limit);
                newBuf.put(oldBuf);
                buf(newBuf);
                buf().position(position);
                buf().limit(limit);
                buf().order(bo);
                this.mark = -1;
            }
        }
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final int position() {
        return buf().position();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer position(int newPosition) {
        autoExpand(newPosition, 0);
        buf().position(newPosition);
        if (this.mark > newPosition) {
            this.mark = -1;
        }
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final int limit() {
        return buf().limit();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer limit(int newLimit) {
        autoExpand(newLimit, 0);
        buf().limit(newLimit);
        if (this.mark > newLimit) {
            this.mark = -1;
        }
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer mark() {
        ByteBuffer byteBuffer = buf();
        byteBuffer.mark();
        this.mark = byteBuffer.position();
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final int markValue() {
        return this.mark;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer reset() {
        buf().reset();
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer clear() {
        buf().clear();
        this.mark = -1;
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer sweep() {
        clear();
        return fillAndReset(remaining());
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer sweep(byte value) {
        clear();
        return fillAndReset(value, remaining());
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer flip() {
        buf().flip();
        this.mark = -1;
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer rewind() {
        buf().rewind();
        this.mark = -1;
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final int remaining() {
        ByteBuffer byteBuffer = buf();
        return byteBuffer.limit() - byteBuffer.position();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final boolean hasRemaining() {
        ByteBuffer byteBuffer = buf();
        return byteBuffer.limit() > byteBuffer.position();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final byte get() {
        return buf().get();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final short getUnsigned() {
        return (short) (get() & 255);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer put(byte b) {
        autoExpand(1);
        buf().put(b);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsigned(byte value) {
        autoExpand(1);
        buf().put((byte) (value & 255));
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsigned(int index, byte value) {
        autoExpand(index, 1);
        buf().put(index, (byte) (value & 255));
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsigned(short value) {
        autoExpand(1);
        buf().put((byte) (value & 255));
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsigned(int index, short value) {
        autoExpand(index, 1);
        buf().put(index, (byte) (value & 255));
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsigned(int value) {
        autoExpand(1);
        buf().put((byte) (value & MotionEventCompat.ACTION_MASK));
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsigned(int index, int value) {
        autoExpand(index, 1);
        buf().put(index, (byte) (value & MotionEventCompat.ACTION_MASK));
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsigned(long value) {
        autoExpand(1);
        buf().put((byte) (BYTE_MASK & value));
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putUnsigned(int index, long value) {
        autoExpand(index, 1);
        buf().put(index, (byte) (BYTE_MASK & value));
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final byte get(int index) {
        return buf().get(index);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final short getUnsigned(int index) {
        return (short) (get(index) & 255);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer put(int index, byte b) {
        autoExpand(index, 1);
        buf().put(index, b);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer get(byte[] dst, int offset, int length) {
        buf().get(dst, offset, length);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer put(ByteBuffer src) {
        autoExpand(src.remaining());
        buf().put(src);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer put(byte[] src, int offset, int length) {
        autoExpand(length);
        buf().put(src, offset, length);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer compact() {
        int remaining = remaining();
        int capacity = capacity();
        if (capacity != 0) {
            if (isAutoShrink() && remaining <= (capacity >>> 2) && capacity > this.minimumCapacity) {
                int newCapacity = capacity;
                int minCapacity = Math.max(this.minimumCapacity, remaining << 1);
                while ((newCapacity >>> 1) >= minCapacity) {
                    newCapacity >>>= 1;
                }
                int newCapacity2 = Math.max(minCapacity, newCapacity);
                if (newCapacity2 != capacity) {
                    ByteOrder bo = order();
                    if (remaining > newCapacity2) {
                        throw new IllegalStateException("The amount of the remaining bytes is greater than the new capacity.");
                    }
                    ByteBuffer oldBuf = buf();
                    ByteBuffer newBuf = getAllocator().allocateNioBuffer(newCapacity2, isDirect());
                    newBuf.put(oldBuf);
                    buf(newBuf);
                    buf().order(bo);
                }
            } else {
                buf().compact();
            }
            this.mark = -1;
        }
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final ByteOrder order() {
        return buf().order();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer order(ByteOrder bo) {
        buf().order(bo);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final char getChar() {
        return buf().getChar();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putChar(char value) {
        autoExpand(2);
        buf().putChar(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final char getChar(int index) {
        return buf().getChar(index);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putChar(int index, char value) {
        autoExpand(index, 2);
        buf().putChar(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final CharBuffer asCharBuffer() {
        return buf().asCharBuffer();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final short getShort() {
        return buf().getShort();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putShort(short value) {
        autoExpand(2);
        buf().putShort(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final short getShort(int index) {
        return buf().getShort(index);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putShort(int index, short value) {
        autoExpand(index, 2);
        buf().putShort(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final ShortBuffer asShortBuffer() {
        return buf().asShortBuffer();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final int getInt() {
        return buf().getInt();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putInt(int value) {
        autoExpand(4);
        buf().putInt(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putUnsignedInt(byte value) {
        autoExpand(4);
        buf().putInt(value & 255);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putUnsignedInt(int index, byte value) {
        autoExpand(index, 4);
        buf().putInt(index, value & 255);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putUnsignedInt(short value) {
        autoExpand(4);
        buf().putInt(65535 & value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putUnsignedInt(int index, short value) {
        autoExpand(index, 4);
        buf().putInt(index, 65535 & value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putUnsignedInt(int value) {
        autoExpand(4);
        buf().putInt(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putUnsignedInt(int index, int value) {
        autoExpand(index, 4);
        buf().putInt(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putUnsignedInt(long value) {
        autoExpand(4);
        buf().putInt((int) ((-1) & value));
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putUnsignedInt(int index, long value) {
        autoExpand(index, 4);
        buf().putInt(index, (int) (4294967295L & value));
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putUnsignedShort(byte value) {
        autoExpand(2);
        buf().putShort((short) (value & 255));
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putUnsignedShort(int index, byte value) {
        autoExpand(index, 2);
        buf().putShort(index, (short) (value & 255));
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putUnsignedShort(short value) {
        autoExpand(2);
        buf().putShort(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putUnsignedShort(int index, short value) {
        autoExpand(index, 2);
        buf().putShort(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putUnsignedShort(int value) {
        autoExpand(2);
        buf().putShort((short) value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putUnsignedShort(int index, int value) {
        autoExpand(index, 2);
        buf().putShort(index, (short) value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putUnsignedShort(long value) {
        autoExpand(2);
        buf().putShort((short) value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putUnsignedShort(int index, long value) {
        autoExpand(index, 2);
        buf().putShort(index, (short) value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final int getInt(int index) {
        return buf().getInt(index);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putInt(int index, int value) {
        autoExpand(index, 4);
        buf().putInt(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IntBuffer asIntBuffer() {
        return buf().asIntBuffer();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final long getLong() {
        return buf().getLong();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putLong(long value) {
        autoExpand(8);
        buf().putLong(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final long getLong(int index) {
        return buf().getLong(index);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putLong(int index, long value) {
        autoExpand(index, 8);
        buf().putLong(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final LongBuffer asLongBuffer() {
        return buf().asLongBuffer();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final float getFloat() {
        return buf().getFloat();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putFloat(float value) {
        autoExpand(4);
        buf().putFloat(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final float getFloat(int index) {
        return buf().getFloat(index);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putFloat(int index, float value) {
        autoExpand(index, 4);
        buf().putFloat(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final FloatBuffer asFloatBuffer() {
        return buf().asFloatBuffer();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final double getDouble() {
        return buf().getDouble();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putDouble(double value) {
        autoExpand(8);
        buf().putDouble(value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final double getDouble(int index) {
        return buf().getDouble(index);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer putDouble(int index, double value) {
        autoExpand(index, 8);
        buf().putDouble(index, value);
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final DoubleBuffer asDoubleBuffer() {
        return buf().asDoubleBuffer();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer asReadOnlyBuffer() {
        this.recapacityAllowed = false;
        return asReadOnlyBuffer0();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer duplicate() {
        this.recapacityAllowed = false;
        return duplicate0();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer slice() {
        this.recapacityAllowed = false;
        return slice0();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer getSlice(int index, int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length: " + length);
        }
        int pos = position();
        int limit = limit();
        if (index > limit) {
            throw new IllegalArgumentException("index: " + index);
        }
        int endIndex = index + length;
        if (endIndex > limit) {
            throw new IndexOutOfBoundsException("index + length (" + endIndex + ") is greater than limit (" + limit + ").");
        }
        clear();
        limit(endIndex);
        position(index);
        IoBuffer slice = slice();
        limit(limit);
        position(pos);
        return slice;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public final IoBuffer getSlice(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length: " + length);
        }
        int pos = position();
        int limit = limit();
        int nextPos = pos + length;
        if (limit < nextPos) {
            throw new IndexOutOfBoundsException("position + length (" + nextPos + ") is greater than limit (" + limit + ").");
        }
        limit(pos + length);
        IoBuffer slice = slice();
        position(nextPos);
        limit(limit);
        return slice;
    }

    public int hashCode() {
        int h = 1;
        int p = position();
        for (int i = limit() - 1; i >= p; i--) {
            h = (h * 31) + get(i);
        }
        return h;
    }

    public boolean equals(Object o) {
        if (o instanceof IoBuffer) {
            IoBuffer that = (IoBuffer) o;
            if (remaining() == that.remaining()) {
                int p = position();
                int i = limit() - 1;
                int j = that.limit() - 1;
                while (i >= p) {
                    byte v1 = get(i);
                    byte v2 = that.get(j);
                    if (v1 != v2) {
                        return false;
                    }
                    i--;
                    j--;
                }
                return true;
            }
            return false;
        }
        return false;
    }

    @Override // java.lang.Comparable
    public int compareTo(IoBuffer that) {
        int n = position() + Math.min(remaining(), that.remaining());
        int i = position();
        int j = that.position();
        while (i < n) {
            byte v1 = get(i);
            byte v2 = that.get(j);
            if (v1 == v2) {
                i++;
                j++;
            } else if (v1 < v2) {
                return -1;
            } else {
                return 1;
            }
        }
        return remaining() - that.remaining();
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (isDirect()) {
            buf.append("DirectBuffer");
        } else {
            buf.append("HeapBuffer");
        }
        buf.append("[pos=");
        buf.append(position());
        buf.append(" lim=");
        buf.append(limit());
        buf.append(" cap=");
        buf.append(capacity());
        buf.append(": ");
        buf.append(getHexDump(16));
        buf.append(']');
        return buf.toString();
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer get(byte[] dst) {
        return get(dst, 0, dst.length);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer put(IoBuffer src) {
        return put(src.buf());
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer put(byte[] src) {
        return put(src, 0, src.length);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int getUnsignedShort() {
        return getShort() & 65535;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int getUnsignedShort(int index) {
        return getShort(index) & 65535;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public long getUnsignedInt() {
        return getInt() & 4294967295L;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int getMediumInt() {
        byte b1 = get();
        byte b2 = get();
        byte b3 = get();
        return ByteOrder.BIG_ENDIAN.equals(order()) ? getMediumInt(b1, b2, b3) : getMediumInt(b3, b2, b1);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int getUnsignedMediumInt() {
        int b1 = getUnsigned();
        int b2 = getUnsigned();
        int b3 = getUnsigned();
        return ByteOrder.BIG_ENDIAN.equals(order()) ? (b1 << 16) | (b2 << 8) | b3 : (b3 << 16) | (b2 << 8) | b1;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int getMediumInt(int index) {
        byte b1 = get(index);
        byte b2 = get(index + 1);
        byte b3 = get(index + 2);
        return ByteOrder.BIG_ENDIAN.equals(order()) ? getMediumInt(b1, b2, b3) : getMediumInt(b3, b2, b1);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int getUnsignedMediumInt(int index) {
        int b1 = getUnsigned(index);
        int b2 = getUnsigned(index + 1);
        int b3 = getUnsigned(index + 2);
        return ByteOrder.BIG_ENDIAN.equals(order()) ? (b1 << 16) | (b2 << 8) | b3 : (b3 << 16) | (b2 << 8) | b1;
    }

    private int getMediumInt(byte b1, byte b2, byte b3) {
        int ret = ((b1 << 16) & 16711680) | ((b2 << 8) & 65280) | (b3 & 255);
        if ((b1 & AnyoMessage.CMD_RESET_CHARGE) == 128) {
            return ret | ViewCompat.MEASURED_STATE_MASK;
        }
        return ret;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putMediumInt(int value) {
        byte b1 = (byte) (value >> 16);
        byte b2 = (byte) (value >> 8);
        byte b3 = (byte) value;
        if (ByteOrder.BIG_ENDIAN.equals(order())) {
            put(b1).put(b2).put(b3);
        } else {
            put(b3).put(b2).put(b1);
        }
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putMediumInt(int index, int value) {
        byte b1 = (byte) (value >> 16);
        byte b2 = (byte) (value >> 8);
        byte b3 = (byte) value;
        if (ByteOrder.BIG_ENDIAN.equals(order())) {
            put(index, b1).put(index + 1, b2).put(index + 2, b3);
        } else {
            put(index, b3).put(index + 1, b2).put(index + 2, b1);
        }
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public long getUnsignedInt(int index) {
        return getInt(index) & 4294967295L;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public InputStream asInputStream() {
        return new InputStream() { // from class: org.apache.mina.core.buffer.AbstractIoBuffer.1
            @Override // java.io.InputStream
            public int available() {
                return AbstractIoBuffer.this.remaining();
            }

            @Override // java.io.InputStream
            public synchronized void mark(int readlimit) {
                AbstractIoBuffer.this.mark();
            }

            @Override // java.io.InputStream
            public boolean markSupported() {
                return true;
            }

            @Override // java.io.InputStream
            public int read() {
                if (AbstractIoBuffer.this.hasRemaining()) {
                    return AbstractIoBuffer.this.get() & 255;
                }
                return -1;
            }

            @Override // java.io.InputStream
            public int read(byte[] b, int off, int len) {
                int remaining = AbstractIoBuffer.this.remaining();
                if (remaining > 0) {
                    int readBytes = Math.min(remaining, len);
                    AbstractIoBuffer.this.get(b, off, readBytes);
                    return readBytes;
                }
                return -1;
            }

            @Override // java.io.InputStream
            public synchronized void reset() {
                AbstractIoBuffer.this.reset();
            }

            @Override // java.io.InputStream
            public long skip(long n) {
                int bytes;
                if (n > 2147483647L) {
                    bytes = AbstractIoBuffer.this.remaining();
                } else {
                    bytes = Math.min(AbstractIoBuffer.this.remaining(), (int) n);
                }
                AbstractIoBuffer.this.skip(bytes);
                return bytes;
            }
        };
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public OutputStream asOutputStream() {
        return new OutputStream() { // from class: org.apache.mina.core.buffer.AbstractIoBuffer.2
            @Override // java.io.OutputStream
            public void write(byte[] b, int off, int len) {
                AbstractIoBuffer.this.put(b, off, len);
            }

            @Override // java.io.OutputStream
            public void write(int b) {
                AbstractIoBuffer.this.put((byte) b);
            }
        };
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public String getHexDump() {
        return getHexDump(Integer.MAX_VALUE);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public String getHexDump(int lengthLimit) {
        return IoBufferHexDumper.getHexdump(this, lengthLimit);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public String getString(CharsetDecoder decoder) throws CharacterCodingException {
        int newPos;
        CoderResult cr;
        if (!hasRemaining()) {
            return "";
        }
        boolean utf16 = decoder.charset().name().startsWith(CharEncoding.UTF_16);
        int oldPos = position();
        int oldLimit = limit();
        int end = -1;
        if (!utf16) {
            end = indexOf((byte) 0);
            if (end < 0) {
                end = oldLimit;
                newPos = oldLimit;
            } else {
                newPos = end + 1;
            }
        } else {
            int i = oldPos;
            while (true) {
                boolean wasZero = get(i) == 0;
                i++;
                if (i >= oldLimit) {
                    break;
                } else if (get(i) != 0) {
                    i++;
                    if (i >= oldLimit) {
                        break;
                    }
                } else if (wasZero) {
                    end = i - 1;
                    break;
                }
            }
            if (end < 0) {
                end = oldPos + ((oldLimit - oldPos) & (-2));
                newPos = end;
            } else if (end + 2 <= oldLimit) {
                newPos = end + 2;
            } else {
                newPos = end;
            }
        }
        if (oldPos == end) {
            position(newPos);
            return "";
        }
        limit(end);
        decoder.reset();
        int expectedLength = ((int) (remaining() * decoder.averageCharsPerByte())) + 1;
        CharBuffer out = CharBuffer.allocate(expectedLength);
        while (true) {
            if (hasRemaining()) {
                cr = decoder.decode(buf(), out, true);
            } else {
                cr = decoder.flush(out);
            }
            if (!cr.isUnderflow()) {
                if (cr.isOverflow()) {
                    CharBuffer o = CharBuffer.allocate(out.capacity() + expectedLength);
                    out.flip();
                    o.put(out);
                    out = o;
                } else if (cr.isError()) {
                    limit(oldLimit);
                    position(oldPos);
                    cr.throwException();
                }
            } else {
                limit(oldLimit);
                position(newPos);
                return out.flip().toString();
            }
        }
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public String getString(int fieldSize, CharsetDecoder decoder) throws CharacterCodingException {
        CoderResult cr;
        checkFieldSize(fieldSize);
        if (fieldSize == 0 || !hasRemaining()) {
            return "";
        }
        boolean utf16 = decoder.charset().name().startsWith(CharEncoding.UTF_16);
        if (utf16 && (fieldSize & 1) != 0) {
            throw new IllegalArgumentException("fieldSize is not even.");
        }
        int oldPos = position();
        int oldLimit = limit();
        int end = oldPos + fieldSize;
        if (oldLimit < end) {
            throw new BufferUnderflowException();
        }
        if (!utf16) {
            int i = oldPos;
            while (i < end && get(i) != 0) {
                i++;
            }
            if (i == end) {
                limit(end);
            } else {
                limit(i);
            }
        } else {
            int i2 = oldPos;
            while (i2 < end && (get(i2) != 0 || get(i2 + 1) != 0)) {
                i2 += 2;
            }
            if (i2 == end) {
                limit(end);
            } else {
                limit(i2);
            }
        }
        if (!hasRemaining()) {
            limit(oldLimit);
            position(end);
            return "";
        }
        decoder.reset();
        int expectedLength = ((int) (remaining() * decoder.averageCharsPerByte())) + 1;
        CharBuffer out = CharBuffer.allocate(expectedLength);
        while (true) {
            if (hasRemaining()) {
                cr = decoder.decode(buf(), out, true);
            } else {
                cr = decoder.flush(out);
            }
            if (!cr.isUnderflow()) {
                if (cr.isOverflow()) {
                    CharBuffer o = CharBuffer.allocate(out.capacity() + expectedLength);
                    out.flip();
                    o.put(out);
                    out = o;
                } else if (cr.isError()) {
                    limit(oldLimit);
                    position(oldPos);
                    cr.throwException();
                }
            } else {
                limit(oldLimit);
                position(end);
                return out.flip().toString();
            }
        }
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putString(CharSequence val, CharsetEncoder encoder) throws CharacterCodingException {
        CoderResult cr;
        if (val.length() != 0) {
            CharBuffer in = CharBuffer.wrap(val);
            encoder.reset();
            int expandedState = 0;
            while (true) {
                if (in.hasRemaining()) {
                    cr = encoder.encode(in, buf(), true);
                } else {
                    cr = encoder.flush(buf());
                }
                if (!cr.isUnderflow()) {
                    if (cr.isOverflow()) {
                        if (isAutoExpand()) {
                            switch (expandedState) {
                                case 0:
                                    autoExpand((int) Math.ceil(in.remaining() * encoder.averageBytesPerChar()));
                                    expandedState++;
                                    continue;
                                case 1:
                                    autoExpand((int) Math.ceil(in.remaining() * encoder.maxBytesPerChar()));
                                    expandedState++;
                                    continue;
                                default:
                                    throw new RuntimeException("Expanded by " + ((int) Math.ceil(in.remaining() * encoder.maxBytesPerChar())) + " but that wasn't enough for '" + ((Object) val) + "'");
                            }
                        }
                    } else {
                        expandedState = 0;
                    }
                    cr.throwException();
                }
            }
        }
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putString(CharSequence val, int fieldSize, CharsetEncoder encoder) throws CharacterCodingException {
        CoderResult cr;
        checkFieldSize(fieldSize);
        if (fieldSize != 0) {
            autoExpand(fieldSize);
            boolean utf16 = encoder.charset().name().startsWith(CharEncoding.UTF_16);
            if (utf16 && (fieldSize & 1) != 0) {
                throw new IllegalArgumentException("fieldSize is not even.");
            }
            int oldLimit = limit();
            int end = position() + fieldSize;
            if (oldLimit < end) {
                throw new BufferOverflowException();
            }
            if (val.length() == 0) {
                if (!utf16) {
                    put((byte) 0);
                } else {
                    put((byte) 0);
                    put((byte) 0);
                }
                position(end);
            } else {
                CharBuffer in = CharBuffer.wrap(val);
                limit(end);
                encoder.reset();
                while (true) {
                    if (in.hasRemaining()) {
                        cr = encoder.encode(in, buf(), true);
                    } else {
                        cr = encoder.flush(buf());
                    }
                    if (cr.isUnderflow() || cr.isOverflow()) {
                        break;
                    }
                    cr.throwException();
                }
                limit(oldLimit);
                if (position() < end) {
                    if (!utf16) {
                        put((byte) 0);
                    } else {
                        put((byte) 0);
                        put((byte) 0);
                    }
                }
                position(end);
            }
        }
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public String getPrefixedString(CharsetDecoder decoder) throws CharacterCodingException {
        return getPrefixedString(2, decoder);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public String getPrefixedString(int prefixLength, CharsetDecoder decoder) throws CharacterCodingException {
        CoderResult cr;
        if (!prefixedDataAvailable(prefixLength)) {
            throw new BufferUnderflowException();
        }
        int fieldSize = 0;
        switch (prefixLength) {
            case 1:
                fieldSize = getUnsigned();
                break;
            case 2:
                fieldSize = getUnsignedShort();
                break;
            case 4:
                fieldSize = getInt();
                break;
        }
        if (fieldSize == 0) {
            return "";
        }
        boolean utf16 = decoder.charset().name().startsWith(CharEncoding.UTF_16);
        if (utf16 && (fieldSize & 1) != 0) {
            throw new BufferDataException("fieldSize is not even for a UTF-16 string.");
        }
        int oldLimit = limit();
        int end = position() + fieldSize;
        if (oldLimit < end) {
            throw new BufferUnderflowException();
        }
        limit(end);
        decoder.reset();
        int expectedLength = ((int) (remaining() * decoder.averageCharsPerByte())) + 1;
        CharBuffer out = CharBuffer.allocate(expectedLength);
        while (true) {
            if (hasRemaining()) {
                cr = decoder.decode(buf(), out, true);
            } else {
                cr = decoder.flush(out);
            }
            if (!cr.isUnderflow()) {
                if (cr.isOverflow()) {
                    CharBuffer o = CharBuffer.allocate(out.capacity() + expectedLength);
                    out.flip();
                    o.put(out);
                    out = o;
                } else {
                    cr.throwException();
                }
            } else {
                limit(oldLimit);
                position(end);
                return out.flip().toString();
            }
        }
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putPrefixedString(CharSequence in, CharsetEncoder encoder) throws CharacterCodingException {
        return putPrefixedString(in, 2, 0, encoder);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putPrefixedString(CharSequence in, int prefixLength, CharsetEncoder encoder) throws CharacterCodingException {
        return putPrefixedString(in, prefixLength, 0, encoder);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putPrefixedString(CharSequence in, int prefixLength, int padding, CharsetEncoder encoder) throws CharacterCodingException {
        return putPrefixedString(in, prefixLength, padding, (byte) 0, encoder);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putPrefixedString(CharSequence val, int prefixLength, int padding, byte padValue, CharsetEncoder encoder) throws CharacterCodingException {
        int maxLength;
        int padMask;
        CoderResult cr;
        switch (prefixLength) {
            case 1:
                maxLength = MotionEventCompat.ACTION_MASK;
                break;
            case 2:
                maxLength = SupportMenu.USER_MASK;
                break;
            case 3:
            default:
                throw new IllegalArgumentException("prefixLength: " + prefixLength);
            case 4:
                maxLength = Integer.MAX_VALUE;
                break;
        }
        if (val.length() > maxLength) {
            throw new IllegalArgumentException("The specified string is too long.");
        }
        if (val.length() == 0) {
            switch (prefixLength) {
                case 1:
                    put((byte) 0);
                    break;
                case 2:
                    putShort((short) 0);
                    break;
                case 4:
                    putInt(0);
                    break;
            }
        } else {
            switch (padding) {
                case 0:
                case 1:
                    padMask = 0;
                    break;
                case 2:
                    padMask = 1;
                    break;
                case 3:
                default:
                    throw new IllegalArgumentException("padding: " + padding);
                case 4:
                    padMask = 3;
                    break;
            }
            CharBuffer in = CharBuffer.wrap(val);
            skip(prefixLength);
            int oldPos = position();
            encoder.reset();
            int expandedState = 0;
            while (true) {
                if (in.hasRemaining()) {
                    cr = encoder.encode(in, buf(), true);
                } else {
                    cr = encoder.flush(buf());
                }
                if (position() - oldPos > maxLength) {
                    throw new IllegalArgumentException("The specified string is too long.");
                }
                if (!cr.isUnderflow()) {
                    if (cr.isOverflow()) {
                        if (isAutoExpand()) {
                            switch (expandedState) {
                                case 0:
                                    autoExpand((int) Math.ceil(in.remaining() * encoder.averageBytesPerChar()));
                                    expandedState++;
                                    continue;
                                case 1:
                                    autoExpand((int) Math.ceil(in.remaining() * encoder.maxBytesPerChar()));
                                    expandedState++;
                                    continue;
                                default:
                                    throw new RuntimeException("Expanded by " + ((int) Math.ceil(in.remaining() * encoder.maxBytesPerChar())) + " but that wasn't enough for '" + ((Object) val) + "'");
                            }
                        }
                    } else {
                        expandedState = 0;
                    }
                    cr.throwException();
                } else {
                    fill(padValue, padding - ((position() - oldPos) & padMask));
                    int length = position() - oldPos;
                    switch (prefixLength) {
                        case 1:
                            put(oldPos - 1, (byte) length);
                            break;
                        case 2:
                            putShort(oldPos - 2, (short) length);
                            break;
                        case 4:
                            putInt(oldPos - 4, length);
                            break;
                    }
                }
            }
        }
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public Object getObject() throws ClassNotFoundException {
        return getObject(Thread.currentThread().getContextClassLoader());
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public Object getObject(final ClassLoader classLoader) throws ClassNotFoundException {
        if (!prefixedDataAvailable(4)) {
            throw new BufferUnderflowException();
        }
        int length = getInt();
        if (length <= 4) {
            throw new BufferDataException("Object length should be greater than 4: " + length);
        }
        int oldLimit = limit();
        limit(position() + length);
        ObjectInputStream in = null;
        try {
            try {
                ObjectInputStream in2 = new ObjectInputStream(asInputStream()) { // from class: org.apache.mina.core.buffer.AbstractIoBuffer.3
                    @Override // java.io.ObjectInputStream
                    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
                        int type = read();
                        if (type < 0) {
                            throw new EOFException();
                        }
                        switch (type) {
                            case 0:
                                return super.readClassDescriptor();
                            case 1:
                                String className = readUTF();
                                Class<?> clazz = Class.forName(className, true, classLoader);
                                return ObjectStreamClass.lookup(clazz);
                            default:
                                throw new StreamCorruptedException("Unexpected class descriptor type: " + type);
                        }
                    }

                    @Override // java.io.ObjectInputStream
                    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                        Class<?> clazz = desc.forClass();
                        if (clazz == null) {
                            String name = desc.getName();
                            try {
                                return Class.forName(name, false, classLoader);
                            } catch (ClassNotFoundException e) {
                                return super.resolveClass(desc);
                            }
                        }
                        return clazz;
                    }
                };
                try {
                    Object readObject = in2.readObject();
                    if (in2 != null) {
                        try {
                            in2.close();
                        } catch (IOException e) {
                        }
                    }
                    limit(oldLimit);
                    return readObject;
                } catch (IOException e2) {
                    e = e2;
                    throw new BufferDataException(e);
                } catch (Throwable th) {
                    th = th;
                    in = in2;
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e3) {
                        }
                    }
                    limit(oldLimit);
                    throw th;
                }
            } catch (IOException e4) {
                e = e4;
            }
        } catch (Throwable th2) {
            th = th2;
        }
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putObject(Object o) {
        int oldPos = position();
        skip(4);
        ObjectOutputStream out = null;
        try {
            try {
                ObjectOutputStream out2 = new ObjectOutputStream(asOutputStream()) { // from class: org.apache.mina.core.buffer.AbstractIoBuffer.4
                    @Override // java.io.ObjectOutputStream
                    protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
                        Class<?> clazz = desc.forClass();
                        if (clazz.isArray() || clazz.isPrimitive() || !Serializable.class.isAssignableFrom(clazz)) {
                            write(0);
                            super.writeClassDescriptor(desc);
                            return;
                        }
                        write(1);
                        writeUTF(desc.getName());
                    }
                };
                try {
                    out2.writeObject(o);
                    out2.flush();
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (IOException e) {
                        }
                    }
                    int newPos = position();
                    position(oldPos);
                    putInt((newPos - oldPos) - 4);
                    position(newPos);
                    return this;
                } catch (IOException e2) {
                    e = e2;
                    throw new BufferDataException(e);
                } catch (Throwable th) {
                    th = th;
                    out = out2;
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e3) {
                        }
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e = e4;
            }
        } catch (Throwable th2) {
            th = th2;
        }
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public boolean prefixedDataAvailable(int prefixLength) {
        return prefixedDataAvailable(prefixLength, Integer.MAX_VALUE);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public boolean prefixedDataAvailable(int prefixLength, int maxDataLength) {
        int dataLength;
        if (remaining() < prefixLength) {
            return false;
        }
        switch (prefixLength) {
            case 1:
                dataLength = getUnsigned(position());
                break;
            case 2:
                dataLength = getUnsignedShort(position());
                break;
            case 3:
            default:
                throw new IllegalArgumentException("prefixLength: " + prefixLength);
            case 4:
                dataLength = getInt(position());
                break;
        }
        if (dataLength < 0 || dataLength > maxDataLength) {
            throw new BufferDataException("dataLength: " + dataLength);
        }
        return remaining() - prefixLength >= dataLength;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public int indexOf(byte b) {
        if (hasArray()) {
            int arrayOffset = arrayOffset();
            int beginPos = arrayOffset + position();
            int limit = arrayOffset + limit();
            byte[] array = array();
            for (int i = beginPos; i < limit; i++) {
                if (array[i] == b) {
                    return i - arrayOffset;
                }
            }
        } else {
            int beginPos2 = position();
            int limit2 = limit();
            for (int i2 = beginPos2; i2 < limit2; i2++) {
                if (get(i2) == b) {
                    return i2;
                }
            }
        }
        return -1;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer skip(int size) {
        autoExpand(size);
        return position(position() + size);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer fill(byte value, int size) {
        autoExpand(size);
        int q = size >>> 3;
        int r = size & 7;
        if (q > 0) {
            int intValue = (value & 255) | ((value << 8) & 65280) | ((value << 16) & 16711680) | (value << AnyoMessage.CMD_REPORT_CHARGE_STOPPED);
            long longValue = (intValue & 4294967295L) | (intValue << 32);
            for (int i = q; i > 0; i--) {
                putLong(longValue);
            }
        }
        int q2 = r >>> 2;
        int r2 = r & 3;
        if (q2 > 0) {
            putInt((value & 255) | ((value << 8) & 65280) | ((value << 16) & 16711680) | (value << AnyoMessage.CMD_REPORT_CHARGE_STOPPED));
        }
        int q3 = r2 >> 1;
        int r3 = r2 & 1;
        if (q3 > 0) {
            short shortValue = (short) ((value & 255) | (value << 8));
            putShort(shortValue);
        }
        if (r3 > 0) {
            put(value);
        }
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer fillAndReset(byte value, int size) {
        autoExpand(size);
        int pos = position();
        try {
            fill(value, size);
            return this;
        } finally {
            position(pos);
        }
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer fill(int size) {
        autoExpand(size);
        int q = size >>> 3;
        int r = size & 7;
        for (int i = q; i > 0; i--) {
            putLong(0L);
        }
        int q2 = r >>> 2;
        int r2 = r & 3;
        if (q2 > 0) {
            putInt(0);
        }
        int q3 = r2 >> 1;
        int r3 = r2 & 1;
        if (q3 > 0) {
            putShort((short) 0);
        }
        if (r3 > 0) {
            put((byte) 0);
        }
        return this;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer fillAndReset(int size) {
        autoExpand(size);
        int pos = position();
        try {
            fill(size);
            return this;
        } finally {
            position(pos);
        }
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> E getEnum(Class<E> enumClass) {
        return (E) toEnum(enumClass, getUnsigned());
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> E getEnum(int index, Class<E> enumClass) {
        return (E) toEnum(enumClass, getUnsigned(index));
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> E getEnumShort(Class<E> enumClass) {
        return (E) toEnum(enumClass, getUnsignedShort());
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> E getEnumShort(int index, Class<E> enumClass) {
        return (E) toEnum(enumClass, getUnsignedShort(index));
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> E getEnumInt(Class<E> enumClass) {
        return (E) toEnum(enumClass, getInt());
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> E getEnumInt(int index, Class<E> enumClass) {
        return (E) toEnum(enumClass, getInt(index));
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putEnum(Enum<?> e) {
        if (e.ordinal() > BYTE_MASK) {
            throw new IllegalArgumentException(enumConversionErrorMessage(e, "byte"));
        }
        return put((byte) e.ordinal());
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putEnum(int index, Enum<?> e) {
        if (e.ordinal() > BYTE_MASK) {
            throw new IllegalArgumentException(enumConversionErrorMessage(e, "byte"));
        }
        return put(index, (byte) e.ordinal());
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putEnumShort(Enum<?> e) {
        if (e.ordinal() > SHORT_MASK) {
            throw new IllegalArgumentException(enumConversionErrorMessage(e, "short"));
        }
        return putShort((short) e.ordinal());
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putEnumShort(int index, Enum<?> e) {
        if (e.ordinal() > SHORT_MASK) {
            throw new IllegalArgumentException(enumConversionErrorMessage(e, "short"));
        }
        return putShort(index, (short) e.ordinal());
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putEnumInt(Enum<?> e) {
        return putInt(e.ordinal());
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public IoBuffer putEnumInt(int index, Enum<?> e) {
        return putInt(index, e.ordinal());
    }

    private <E> E toEnum(Class<E> enumClass, int i) {
        E[] enumConstants = enumClass.getEnumConstants();
        if (i > enumConstants.length) {
            throw new IndexOutOfBoundsException(String.format("%d is too large of an ordinal to convert to the enum %s", Integer.valueOf(i), enumClass.getName()));
        }
        return enumConstants[i];
    }

    private String enumConversionErrorMessage(Enum<?> e, String type) {
        return String.format("%s.%s has an ordinal value too large for a %s", e.getClass().getName(), e.name(), type);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> EnumSet<E> getEnumSet(Class<E> enumClass) {
        return toEnumSet(enumClass, get() & BYTE_MASK);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> EnumSet<E> getEnumSet(int index, Class<E> enumClass) {
        return toEnumSet(enumClass, get(index) & BYTE_MASK);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> EnumSet<E> getEnumSetShort(Class<E> enumClass) {
        return toEnumSet(enumClass, getShort() & SHORT_MASK);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> EnumSet<E> getEnumSetShort(int index, Class<E> enumClass) {
        return toEnumSet(enumClass, getShort(index) & SHORT_MASK);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> EnumSet<E> getEnumSetInt(Class<E> enumClass) {
        return toEnumSet(enumClass, getInt() & 4294967295L);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> EnumSet<E> getEnumSetInt(int index, Class<E> enumClass) {
        return toEnumSet(enumClass, getInt(index) & 4294967295L);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> EnumSet<E> getEnumSetLong(Class<E> enumClass) {
        return toEnumSet(enumClass, getLong());
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> EnumSet<E> getEnumSetLong(int index, Class<E> enumClass) {
        return toEnumSet(enumClass, getLong(index));
    }

    private <E extends Enum<E>> EnumSet<E> toEnumSet(Class<E> clazz, long vector) {
        E[] enumConstants;
        EnumSet<E> set = EnumSet.noneOf(clazz);
        long mask = 1;
        for (E e : clazz.getEnumConstants()) {
            if ((mask & vector) == mask) {
                set.add(e);
            }
            mask <<= 1;
        }
        return set;
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> IoBuffer putEnumSet(Set<E> set) {
        long vector = toLong(set);
        if (((-256) & vector) != 0) {
            throw new IllegalArgumentException("The enum set is too large to fit in a byte: " + set);
        }
        return put((byte) vector);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> IoBuffer putEnumSet(int index, Set<E> set) {
        long vector = toLong(set);
        if (((-256) & vector) != 0) {
            throw new IllegalArgumentException("The enum set is too large to fit in a byte: " + set);
        }
        return put(index, (byte) vector);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> IoBuffer putEnumSetShort(Set<E> set) {
        long vector = toLong(set);
        if (((-65536) & vector) != 0) {
            throw new IllegalArgumentException("The enum set is too large to fit in a short: " + set);
        }
        return putShort((short) vector);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> IoBuffer putEnumSetShort(int index, Set<E> set) {
        long vector = toLong(set);
        if (((-65536) & vector) != 0) {
            throw new IllegalArgumentException("The enum set is too large to fit in a short: " + set);
        }
        return putShort(index, (short) vector);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> IoBuffer putEnumSetInt(Set<E> set) {
        long vector = toLong(set);
        if (((-4294967296L) & vector) != 0) {
            throw new IllegalArgumentException("The enum set is too large to fit in an int: " + set);
        }
        return putInt((int) vector);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> IoBuffer putEnumSetInt(int index, Set<E> set) {
        long vector = toLong(set);
        if (((-4294967296L) & vector) != 0) {
            throw new IllegalArgumentException("The enum set is too large to fit in an int: " + set);
        }
        return putInt(index, (int) vector);
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> IoBuffer putEnumSetLong(Set<E> set) {
        return putLong(toLong(set));
    }

    @Override // org.apache.mina.core.buffer.IoBuffer
    public <E extends Enum<E>> IoBuffer putEnumSetLong(int index, Set<E> set) {
        return putLong(index, toLong(set));
    }

    private <E extends Enum<E>> long toLong(Set<E> set) {
        long vector = 0;
        for (E e : set) {
            if (e.ordinal() >= 64) {
                throw new IllegalArgumentException("The enum set is too large to fit in a bit vector: " + set);
            }
            vector |= 1 << e.ordinal();
        }
        return vector;
    }

    private IoBuffer autoExpand(int expectedRemaining) {
        if (isAutoExpand()) {
            expand(expectedRemaining, true);
        }
        return this;
    }

    private IoBuffer autoExpand(int pos, int expectedRemaining) {
        if (isAutoExpand()) {
            expand(pos, expectedRemaining, true);
        }
        return this;
    }

    private static void checkFieldSize(int fieldSize) {
        if (fieldSize < 0) {
            throw new IllegalArgumentException("fieldSize cannot be negative: " + fieldSize);
        }
    }
}