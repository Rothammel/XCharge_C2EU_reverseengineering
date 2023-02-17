package org.apache.mina.core.buffer;

import android.support.p000v4.internal.view.SupportMenu;
import android.support.p000v4.view.MotionEventCompat;
import android.support.p000v4.view.ViewCompat;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import java.io.InputStream;
import java.io.OutputStream;
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

public abstract class AbstractIoBuffer extends IoBuffer {
    private static final long BYTE_MASK = 255;
    private static final long INT_MASK = 4294967295L;
    private static final long SHORT_MASK = 65535;
    private boolean autoExpand;
    private boolean autoShrink;
    private final boolean derived;
    private int mark = -1;
    private int minimumCapacity;
    private boolean recapacityAllowed = true;

    /* access modifiers changed from: protected */
    public abstract IoBuffer asReadOnlyBuffer0();

    /* access modifiers changed from: protected */
    public abstract void buf(ByteBuffer byteBuffer);

    /* access modifiers changed from: protected */
    public abstract IoBuffer duplicate0();

    /* access modifiers changed from: protected */
    public abstract IoBuffer slice0();

    protected AbstractIoBuffer(IoBufferAllocator allocator, int initialCapacity) {
        setAllocator(allocator);
        this.recapacityAllowed = true;
        this.derived = false;
        this.minimumCapacity = initialCapacity;
    }

    protected AbstractIoBuffer(AbstractIoBuffer parent) {
        setAllocator(IoBuffer.getAllocator());
        this.recapacityAllowed = false;
        this.derived = true;
        this.minimumCapacity = parent.minimumCapacity;
    }

    public final boolean isDirect() {
        return buf().isDirect();
    }

    public final boolean isReadOnly() {
        return buf().isReadOnly();
    }

    public final int minimumCapacity() {
        return this.minimumCapacity;
    }

    public final IoBuffer minimumCapacity(int minimumCapacity2) {
        if (minimumCapacity2 < 0) {
            throw new IllegalArgumentException("minimumCapacity: " + minimumCapacity2);
        }
        this.minimumCapacity = minimumCapacity2;
        return this;
    }

    public final int capacity() {
        return buf().capacity();
    }

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

    public final boolean isAutoExpand() {
        return this.autoExpand && this.recapacityAllowed;
    }

    public final boolean isAutoShrink() {
        return this.autoShrink && this.recapacityAllowed;
    }

    public final boolean isDerived() {
        return this.derived;
    }

    public final IoBuffer setAutoExpand(boolean autoExpand2) {
        if (!this.recapacityAllowed) {
            throw new IllegalStateException("Derived buffers and their parent can't be expanded.");
        }
        this.autoExpand = autoExpand2;
        return this;
    }

    public final IoBuffer setAutoShrink(boolean autoShrink2) {
        if (!this.recapacityAllowed) {
            throw new IllegalStateException("Derived buffers and their parent can't be shrinked.");
        }
        this.autoShrink = autoShrink2;
        return this;
    }

    public final IoBuffer expand(int expectedRemaining) {
        return expand(position(), expectedRemaining, false);
    }

    private IoBuffer expand(int expectedRemaining, boolean autoExpand2) {
        return expand(position(), expectedRemaining, autoExpand2);
    }

    public final IoBuffer expand(int pos, int expectedRemaining) {
        return expand(pos, expectedRemaining, false);
    }

    private IoBuffer expand(int pos, int expectedRemaining, boolean autoExpand2) {
        int newCapacity;
        if (!this.recapacityAllowed) {
            throw new IllegalStateException("Derived buffers and their parent can't be expanded.");
        }
        int end = pos + expectedRemaining;
        if (autoExpand2) {
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

    public final int position() {
        return buf().position();
    }

    public final IoBuffer position(int newPosition) {
        autoExpand(newPosition, 0);
        buf().position(newPosition);
        if (this.mark > newPosition) {
            this.mark = -1;
        }
        return this;
    }

    public final int limit() {
        return buf().limit();
    }

    public final IoBuffer limit(int newLimit) {
        autoExpand(newLimit, 0);
        buf().limit(newLimit);
        if (this.mark > newLimit) {
            this.mark = -1;
        }
        return this;
    }

    public final IoBuffer mark() {
        ByteBuffer byteBuffer = buf();
        byteBuffer.mark();
        this.mark = byteBuffer.position();
        return this;
    }

    public final int markValue() {
        return this.mark;
    }

    public final IoBuffer reset() {
        buf().reset();
        return this;
    }

    public final IoBuffer clear() {
        buf().clear();
        this.mark = -1;
        return this;
    }

    public final IoBuffer sweep() {
        clear();
        return fillAndReset(remaining());
    }

    public final IoBuffer sweep(byte value) {
        clear();
        return fillAndReset(value, remaining());
    }

    public final IoBuffer flip() {
        buf().flip();
        this.mark = -1;
        return this;
    }

    public final IoBuffer rewind() {
        buf().rewind();
        this.mark = -1;
        return this;
    }

    public final int remaining() {
        ByteBuffer byteBuffer = buf();
        return byteBuffer.limit() - byteBuffer.position();
    }

    public final boolean hasRemaining() {
        ByteBuffer byteBuffer = buf();
        return byteBuffer.limit() > byteBuffer.position();
    }

    public final byte get() {
        return buf().get();
    }

    public final short getUnsigned() {
        return (short) (get() & 255);
    }

    public final IoBuffer put(byte b) {
        autoExpand(1);
        buf().put(b);
        return this;
    }

    public IoBuffer putUnsigned(byte value) {
        autoExpand(1);
        buf().put((byte) (value & 255));
        return this;
    }

    public IoBuffer putUnsigned(int index, byte value) {
        autoExpand(index, 1);
        buf().put(index, (byte) (value & 255));
        return this;
    }

    public IoBuffer putUnsigned(short value) {
        autoExpand(1);
        buf().put((byte) (value & 255));
        return this;
    }

    public IoBuffer putUnsigned(int index, short value) {
        autoExpand(index, 1);
        buf().put(index, (byte) (value & 255));
        return this;
    }

    public IoBuffer putUnsigned(int value) {
        autoExpand(1);
        buf().put((byte) (value & MotionEventCompat.ACTION_MASK));
        return this;
    }

    public IoBuffer putUnsigned(int index, int value) {
        autoExpand(index, 1);
        buf().put(index, (byte) (value & MotionEventCompat.ACTION_MASK));
        return this;
    }

    public IoBuffer putUnsigned(long value) {
        autoExpand(1);
        buf().put((byte) ((int) (BYTE_MASK & value)));
        return this;
    }

    public IoBuffer putUnsigned(int index, long value) {
        autoExpand(index, 1);
        buf().put(index, (byte) ((int) (BYTE_MASK & value)));
        return this;
    }

    public final byte get(int index) {
        return buf().get(index);
    }

    public final short getUnsigned(int index) {
        return (short) (get(index) & 255);
    }

    public final IoBuffer put(int index, byte b) {
        autoExpand(index, 1);
        buf().put(index, b);
        return this;
    }

    public final IoBuffer get(byte[] dst, int offset, int length) {
        buf().get(dst, offset, length);
        return this;
    }

    public final IoBuffer put(ByteBuffer src) {
        autoExpand(src.remaining());
        buf().put(src);
        return this;
    }

    public final IoBuffer put(byte[] src, int offset, int length) {
        autoExpand(length);
        buf().put(src, offset, length);
        return this;
    }

    public final IoBuffer compact() {
        int remaining = remaining();
        int capacity = capacity();
        if (capacity != 0) {
            if (!isAutoShrink() || remaining > (capacity >>> 2) || capacity <= this.minimumCapacity) {
                buf().compact();
            } else {
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
            }
            this.mark = -1;
        }
        return this;
    }

    public final ByteOrder order() {
        return buf().order();
    }

    public final IoBuffer order(ByteOrder bo) {
        buf().order(bo);
        return this;
    }

    public final char getChar() {
        return buf().getChar();
    }

    public final IoBuffer putChar(char value) {
        autoExpand(2);
        buf().putChar(value);
        return this;
    }

    public final char getChar(int index) {
        return buf().getChar(index);
    }

    public final IoBuffer putChar(int index, char value) {
        autoExpand(index, 2);
        buf().putChar(index, value);
        return this;
    }

    public final CharBuffer asCharBuffer() {
        return buf().asCharBuffer();
    }

    public final short getShort() {
        return buf().getShort();
    }

    public final IoBuffer putShort(short value) {
        autoExpand(2);
        buf().putShort(value);
        return this;
    }

    public final short getShort(int index) {
        return buf().getShort(index);
    }

    public final IoBuffer putShort(int index, short value) {
        autoExpand(index, 2);
        buf().putShort(index, value);
        return this;
    }

    public final ShortBuffer asShortBuffer() {
        return buf().asShortBuffer();
    }

    public final int getInt() {
        return buf().getInt();
    }

    public final IoBuffer putInt(int value) {
        autoExpand(4);
        buf().putInt(value);
        return this;
    }

    public final IoBuffer putUnsignedInt(byte value) {
        autoExpand(4);
        buf().putInt(value & 255);
        return this;
    }

    public final IoBuffer putUnsignedInt(int index, byte value) {
        autoExpand(index, 4);
        buf().putInt(index, value & 255);
        return this;
    }

    public final IoBuffer putUnsignedInt(short value) {
        autoExpand(4);
        buf().putInt(65535 & value);
        return this;
    }

    public final IoBuffer putUnsignedInt(int index, short value) {
        autoExpand(index, 4);
        buf().putInt(index, 65535 & value);
        return this;
    }

    public final IoBuffer putUnsignedInt(int value) {
        autoExpand(4);
        buf().putInt(value);
        return this;
    }

    public final IoBuffer putUnsignedInt(int index, int value) {
        autoExpand(index, 4);
        buf().putInt(index, value);
        return this;
    }

    public final IoBuffer putUnsignedInt(long value) {
        autoExpand(4);
        buf().putInt((int) (-1 & value));
        return this;
    }

    public final IoBuffer putUnsignedInt(int index, long value) {
        autoExpand(index, 4);
        buf().putInt(index, (int) (4294967295L & value));
        return this;
    }

    public final IoBuffer putUnsignedShort(byte value) {
        autoExpand(2);
        buf().putShort((short) (value & 255));
        return this;
    }

    public final IoBuffer putUnsignedShort(int index, byte value) {
        autoExpand(index, 2);
        buf().putShort(index, (short) (value & 255));
        return this;
    }

    public final IoBuffer putUnsignedShort(short value) {
        autoExpand(2);
        buf().putShort(value);
        return this;
    }

    public final IoBuffer putUnsignedShort(int index, short value) {
        autoExpand(index, 2);
        buf().putShort(index, value);
        return this;
    }

    public final IoBuffer putUnsignedShort(int value) {
        autoExpand(2);
        buf().putShort((short) value);
        return this;
    }

    public final IoBuffer putUnsignedShort(int index, int value) {
        autoExpand(index, 2);
        buf().putShort(index, (short) value);
        return this;
    }

    public final IoBuffer putUnsignedShort(long value) {
        autoExpand(2);
        buf().putShort((short) ((int) value));
        return this;
    }

    public final IoBuffer putUnsignedShort(int index, long value) {
        autoExpand(index, 2);
        buf().putShort(index, (short) ((int) value));
        return this;
    }

    public final int getInt(int index) {
        return buf().getInt(index);
    }

    public final IoBuffer putInt(int index, int value) {
        autoExpand(index, 4);
        buf().putInt(index, value);
        return this;
    }

    public final IntBuffer asIntBuffer() {
        return buf().asIntBuffer();
    }

    public final long getLong() {
        return buf().getLong();
    }

    public final IoBuffer putLong(long value) {
        autoExpand(8);
        buf().putLong(value);
        return this;
    }

    public final long getLong(int index) {
        return buf().getLong(index);
    }

    public final IoBuffer putLong(int index, long value) {
        autoExpand(index, 8);
        buf().putLong(index, value);
        return this;
    }

    public final LongBuffer asLongBuffer() {
        return buf().asLongBuffer();
    }

    public final float getFloat() {
        return buf().getFloat();
    }

    public final IoBuffer putFloat(float value) {
        autoExpand(4);
        buf().putFloat(value);
        return this;
    }

    public final float getFloat(int index) {
        return buf().getFloat(index);
    }

    public final IoBuffer putFloat(int index, float value) {
        autoExpand(index, 4);
        buf().putFloat(index, value);
        return this;
    }

    public final FloatBuffer asFloatBuffer() {
        return buf().asFloatBuffer();
    }

    public final double getDouble() {
        return buf().getDouble();
    }

    public final IoBuffer putDouble(double value) {
        autoExpand(8);
        buf().putDouble(value);
        return this;
    }

    public final double getDouble(int index) {
        return buf().getDouble(index);
    }

    public final IoBuffer putDouble(int index, double value) {
        autoExpand(index, 8);
        buf().putDouble(index, value);
        return this;
    }

    public final DoubleBuffer asDoubleBuffer() {
        return buf().asDoubleBuffer();
    }

    public final IoBuffer asReadOnlyBuffer() {
        this.recapacityAllowed = false;
        return asReadOnlyBuffer0();
    }

    public final IoBuffer duplicate() {
        this.recapacityAllowed = false;
        return duplicate0();
    }

    public final IoBuffer slice() {
        this.recapacityAllowed = false;
        return slice0();
    }

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
            throw new IndexOutOfBoundsException("index + length (" + endIndex + ") is greater " + "than limit (" + limit + ").");
        }
        clear();
        limit(endIndex);
        position(index);
        IoBuffer slice = slice();
        limit(limit);
        position(pos);
        return slice;
    }

    public final IoBuffer getSlice(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length: " + length);
        }
        int pos = position();
        int limit = limit();
        int nextPos = pos + length;
        if (limit < nextPos) {
            throw new IndexOutOfBoundsException("position + length (" + nextPos + ") is greater " + "than limit (" + limit + ").");
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
        if (!(o instanceof IoBuffer)) {
            return false;
        }
        IoBuffer that = (IoBuffer) o;
        if (remaining() != that.remaining()) {
            return false;
        }
        int p = position();
        int i = limit() - 1;
        int j = that.limit() - 1;
        while (i >= p) {
            if (get(i) != that.get(j)) {
                return false;
            }
            i--;
            j--;
        }
        return true;
    }

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

    public IoBuffer get(byte[] dst) {
        return get(dst, 0, dst.length);
    }

    public IoBuffer put(IoBuffer src) {
        return put(src.buf());
    }

    public IoBuffer put(byte[] src) {
        return put(src, 0, src.length);
    }

    public int getUnsignedShort() {
        return getShort() & 65535;
    }

    public int getUnsignedShort(int index) {
        return getShort(index) & 65535;
    }

    public long getUnsignedInt() {
        return ((long) getInt()) & 4294967295L;
    }

    public int getMediumInt() {
        byte b1 = get();
        byte b2 = get();
        byte b3 = get();
        if (ByteOrder.BIG_ENDIAN.equals(order())) {
            return getMediumInt(b1, b2, b3);
        }
        return getMediumInt(b3, b2, b1);
    }

    public int getUnsignedMediumInt() {
        int b1 = getUnsigned();
        int b2 = getUnsigned();
        int b3 = getUnsigned();
        if (ByteOrder.BIG_ENDIAN.equals(order())) {
            return (b1 << 16) | (b2 << 8) | b3;
        }
        return (b3 << 16) | (b2 << 8) | b1;
    }

    public int getMediumInt(int index) {
        byte b1 = get(index);
        byte b2 = get(index + 1);
        byte b3 = get(index + 2);
        if (ByteOrder.BIG_ENDIAN.equals(order())) {
            return getMediumInt(b1, b2, b3);
        }
        return getMediumInt(b3, b2, b1);
    }

    public int getUnsignedMediumInt(int index) {
        int b1 = getUnsigned(index);
        int b2 = getUnsigned(index + 1);
        int b3 = getUnsigned(index + 2);
        if (ByteOrder.BIG_ENDIAN.equals(order())) {
            return (b1 << 16) | (b2 << 8) | b3;
        }
        return (b3 << 16) | (b2 << 8) | b1;
    }

    private int getMediumInt(byte b1, byte b2, byte b3) {
        int ret = ((b1 << 16) & 16711680) | ((b2 << 8) & 65280) | (b3 & MotionEventCompat.ACTION_MASK);
        if ((b1 & AnyoMessage.CMD_RESET_CHARGE) == 128) {
            return ret | ViewCompat.MEASURED_STATE_MASK;
        }
        return ret;
    }

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

    public long getUnsignedInt(int index) {
        return ((long) getInt(index)) & 4294967295L;
    }

    public InputStream asInputStream() {
        return new InputStream() {
            public int available() {
                return AbstractIoBuffer.this.remaining();
            }

            public synchronized void mark(int readlimit) {
                AbstractIoBuffer.this.mark();
            }

            public boolean markSupported() {
                return true;
            }

            public int read() {
                if (AbstractIoBuffer.this.hasRemaining()) {
                    return AbstractIoBuffer.this.get() & 255;
                }
                return -1;
            }

            public int read(byte[] b, int off, int len) {
                int remaining = AbstractIoBuffer.this.remaining();
                if (remaining <= 0) {
                    return -1;
                }
                int readBytes = Math.min(remaining, len);
                AbstractIoBuffer.this.get(b, off, readBytes);
                return readBytes;
            }

            public synchronized void reset() {
                AbstractIoBuffer.this.reset();
            }

            public long skip(long n) {
                int bytes;
                if (n > 2147483647L) {
                    bytes = AbstractIoBuffer.this.remaining();
                } else {
                    bytes = Math.min(AbstractIoBuffer.this.remaining(), (int) n);
                }
                AbstractIoBuffer.this.skip(bytes);
                return (long) bytes;
            }
        };
    }

    public OutputStream asOutputStream() {
        return new OutputStream() {
            public void write(byte[] b, int off, int len) {
                AbstractIoBuffer.this.put(b, off, len);
            }

            public void write(int b) {
                AbstractIoBuffer.this.put((byte) b);
            }
        };
    }

    public String getHexDump() {
        return getHexDump(Integer.MAX_VALUE);
    }

    public String getHexDump(int lengthLimit) {
        return IoBufferHexDumper.getHexdump(this, lengthLimit);
    }

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
                end = oldPos + ((oldLimit - oldPos) & -2);
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
        int expectedLength = ((int) (((float) remaining()) * decoder.averageCharsPerByte())) + 1;
        CharBuffer out = CharBuffer.allocate(expectedLength);
        while (true) {
            if (hasRemaining()) {
                cr = decoder.decode(buf(), out, true);
            } else {
                cr = decoder.flush(out);
            }
            if (cr.isUnderflow()) {
                limit(oldLimit);
                position(newPos);
                return out.flip().toString();
            } else if (cr.isOverflow()) {
                CharBuffer o = CharBuffer.allocate(out.capacity() + expectedLength);
                out.flip();
                o.put(out);
                out = o;
            } else if (cr.isError()) {
                limit(oldLimit);
                position(oldPos);
                cr.throwException();
            }
        }
    }

    public String getString(int fieldSize, CharsetDecoder decoder) throws CharacterCodingException {
        CoderResult cr;
        checkFieldSize(fieldSize);
        if (fieldSize == 0 || !hasRemaining()) {
            return "";
        }
        boolean utf16 = decoder.charset().name().startsWith(CharEncoding.UTF_16);
        if (!utf16 || (fieldSize & 1) == 0) {
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
            int expectedLength = ((int) (((float) remaining()) * decoder.averageCharsPerByte())) + 1;
            CharBuffer out = CharBuffer.allocate(expectedLength);
            while (true) {
                if (hasRemaining()) {
                    cr = decoder.decode(buf(), out, true);
                } else {
                    cr = decoder.flush(out);
                }
                if (cr.isUnderflow()) {
                    limit(oldLimit);
                    position(end);
                    return out.flip().toString();
                } else if (cr.isOverflow()) {
                    CharBuffer o = CharBuffer.allocate(out.capacity() + expectedLength);
                    out.flip();
                    o.put(out);
                    out = o;
                } else if (cr.isError()) {
                    limit(oldLimit);
                    position(oldPos);
                    cr.throwException();
                }
            }
        } else {
            throw new IllegalArgumentException("fieldSize is not even.");
        }
    }

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
                    if (!cr.isOverflow()) {
                        expandedState = 0;
                    } else if (isAutoExpand()) {
                        switch (expandedState) {
                            case 0:
                                autoExpand((int) Math.ceil((double) (((float) in.remaining()) * encoder.averageBytesPerChar())));
                                expandedState++;
                                break;
                            case 1:
                                autoExpand((int) Math.ceil((double) (((float) in.remaining()) * encoder.maxBytesPerChar())));
                                expandedState++;
                                break;
                            default:
                                throw new RuntimeException("Expanded by " + ((int) Math.ceil((double) (((float) in.remaining()) * encoder.maxBytesPerChar()))) + " but that wasn't enough for '" + val + "'");
                        }
                    }
                    cr.throwException();
                }
            }
        }
        return this;
    }

    public IoBuffer putString(CharSequence val, int fieldSize, CharsetEncoder encoder) throws CharacterCodingException {
        CoderResult cr;
        checkFieldSize(fieldSize);
        if (fieldSize != 0) {
            autoExpand(fieldSize);
            boolean utf16 = encoder.charset().name().startsWith(CharEncoding.UTF_16);
            if (!utf16 || (fieldSize & 1) == 0) {
                int oldLimit = limit();
                int end = position() + fieldSize;
                if (oldLimit < end) {
                    throw new BufferOverflowException();
                } else if (val.length() == 0) {
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
                            limit(oldLimit);
                        } else {
                            cr.throwException();
                        }
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
            } else {
                throw new IllegalArgumentException("fieldSize is not even.");
            }
        }
        return this;
    }

    public String getPrefixedString(CharsetDecoder decoder) throws CharacterCodingException {
        return getPrefixedString(2, decoder);
    }

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
        if (!decoder.charset().name().startsWith(CharEncoding.UTF_16) || (fieldSize & 1) == 0) {
            int oldLimit = limit();
            int end = position() + fieldSize;
            if (oldLimit < end) {
                throw new BufferUnderflowException();
            }
            limit(end);
            decoder.reset();
            int expectedLength = ((int) (((float) remaining()) * decoder.averageCharsPerByte())) + 1;
            CharBuffer out = CharBuffer.allocate(expectedLength);
            while (true) {
                if (hasRemaining()) {
                    cr = decoder.decode(buf(), out, true);
                } else {
                    cr = decoder.flush(out);
                }
                if (cr.isUnderflow()) {
                    limit(oldLimit);
                    position(end);
                    return out.flip().toString();
                } else if (cr.isOverflow()) {
                    CharBuffer o = CharBuffer.allocate(out.capacity() + expectedLength);
                    out.flip();
                    o.put(out);
                    out = o;
                } else {
                    cr.throwException();
                }
            }
        } else {
            throw new BufferDataException("fieldSize is not even for a UTF-16 string.");
        }
    }

    public IoBuffer putPrefixedString(CharSequence in, CharsetEncoder encoder) throws CharacterCodingException {
        return putPrefixedString(in, 2, 0, encoder);
    }

    public IoBuffer putPrefixedString(CharSequence in, int prefixLength, CharsetEncoder encoder) throws CharacterCodingException {
        return putPrefixedString(in, prefixLength, 0, encoder);
    }

    public IoBuffer putPrefixedString(CharSequence in, int prefixLength, int padding, CharsetEncoder encoder) throws CharacterCodingException {
        return putPrefixedString(in, prefixLength, padding, (byte) 0, encoder);
    }

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
            case 4:
                maxLength = Integer.MAX_VALUE;
                break;
            default:
                throw new IllegalArgumentException("prefixLength: " + prefixLength);
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
                    putShort(0);
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
                case 4:
                    padMask = 3;
                    break;
                default:
                    throw new IllegalArgumentException("padding: " + padding);
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
                } else if (cr.isUnderflow()) {
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
                } else {
                    if (!cr.isOverflow()) {
                        expandedState = 0;
                    } else if (isAutoExpand()) {
                        switch (expandedState) {
                            case 0:
                                autoExpand((int) Math.ceil((double) (((float) in.remaining()) * encoder.averageBytesPerChar())));
                                expandedState++;
                                break;
                            case 1:
                                autoExpand((int) Math.ceil((double) (((float) in.remaining()) * encoder.maxBytesPerChar())));
                                expandedState++;
                                break;
                            default:
                                throw new RuntimeException("Expanded by " + ((int) Math.ceil((double) (((float) in.remaining()) * encoder.maxBytesPerChar()))) + " but that wasn't enough for '" + val + "'");
                        }
                    }
                    cr.throwException();
                }
            }
        }
        return this;
    }

    public Object getObject() throws ClassNotFoundException {
        return getObject(Thread.currentThread().getContextClassLoader());
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0059 A[SYNTHETIC, Splitter:B:24:0x0059] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.Object getObject(final java.lang.ClassLoader r9) throws java.lang.ClassNotFoundException {
        /*
            r8 = this;
            r6 = 4
            boolean r5 = r8.prefixedDataAvailable(r6)
            if (r5 != 0) goto L_0x000d
            java.nio.BufferUnderflowException r5 = new java.nio.BufferUnderflowException
            r5.<init>()
            throw r5
        L_0x000d:
            int r3 = r8.getInt()
            if (r3 > r6) goto L_0x002c
            org.apache.mina.core.buffer.BufferDataException r5 = new org.apache.mina.core.buffer.BufferDataException
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "Object length should be greater than 4: "
            java.lang.StringBuilder r6 = r6.append(r7)
            java.lang.StringBuilder r6 = r6.append(r3)
            java.lang.String r6 = r6.toString()
            r5.<init>((java.lang.String) r6)
            throw r5
        L_0x002c:
            int r4 = r8.limit()
            int r5 = r8.position()
            int r5 = r5 + r3
            r8.limit(r5)
            r1 = 0
            org.apache.mina.core.buffer.AbstractIoBuffer$3 r2 = new org.apache.mina.core.buffer.AbstractIoBuffer$3     // Catch:{ IOException -> 0x004f }
            java.io.InputStream r5 = r8.asInputStream()     // Catch:{ IOException -> 0x004f }
            r2.<init>(r5, r9)     // Catch:{ IOException -> 0x004f }
            java.lang.Object r5 = r2.readObject()     // Catch:{ IOException -> 0x0067, all -> 0x0064 }
            if (r2 == 0) goto L_0x004b
            r2.close()     // Catch:{ IOException -> 0x0060 }
        L_0x004b:
            r8.limit(r4)
            return r5
        L_0x004f:
            r0 = move-exception
        L_0x0050:
            org.apache.mina.core.buffer.BufferDataException r5 = new org.apache.mina.core.buffer.BufferDataException     // Catch:{ all -> 0x0056 }
            r5.<init>((java.lang.Throwable) r0)     // Catch:{ all -> 0x0056 }
            throw r5     // Catch:{ all -> 0x0056 }
        L_0x0056:
            r5 = move-exception
        L_0x0057:
            if (r1 == 0) goto L_0x005c
            r1.close()     // Catch:{ IOException -> 0x0062 }
        L_0x005c:
            r8.limit(r4)
            throw r5
        L_0x0060:
            r6 = move-exception
            goto L_0x004b
        L_0x0062:
            r6 = move-exception
            goto L_0x005c
        L_0x0064:
            r5 = move-exception
            r1 = r2
            goto L_0x0057
        L_0x0067:
            r0 = move-exception
            r1 = r2
            goto L_0x0050
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.core.buffer.AbstractIoBuffer.getObject(java.lang.ClassLoader):java.lang.Object");
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0039 A[SYNTHETIC, Splitter:B:16:0x0039] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public org.apache.mina.core.buffer.IoBuffer putObject(java.lang.Object r8) {
        /*
            r7 = this;
            int r2 = r7.position()
            r5 = 4
            r7.skip(r5)
            r3 = 0
            org.apache.mina.core.buffer.AbstractIoBuffer$4 r4 = new org.apache.mina.core.buffer.AbstractIoBuffer$4     // Catch:{ IOException -> 0x002f }
            java.io.OutputStream r5 = r7.asOutputStream()     // Catch:{ IOException -> 0x002f }
            r4.<init>(r5)     // Catch:{ IOException -> 0x002f }
            r4.writeObject(r8)     // Catch:{ IOException -> 0x0044, all -> 0x0041 }
            r4.flush()     // Catch:{ IOException -> 0x0044, all -> 0x0041 }
            if (r4 == 0) goto L_0x001d
            r4.close()     // Catch:{ IOException -> 0x003d }
        L_0x001d:
            int r1 = r7.position()
            r7.position(r2)
            int r5 = r1 - r2
            int r5 = r5 + -4
            r7.putInt(r5)
            r7.position(r1)
            return r7
        L_0x002f:
            r0 = move-exception
        L_0x0030:
            org.apache.mina.core.buffer.BufferDataException r5 = new org.apache.mina.core.buffer.BufferDataException     // Catch:{ all -> 0x0036 }
            r5.<init>((java.lang.Throwable) r0)     // Catch:{ all -> 0x0036 }
            throw r5     // Catch:{ all -> 0x0036 }
        L_0x0036:
            r5 = move-exception
        L_0x0037:
            if (r3 == 0) goto L_0x003c
            r3.close()     // Catch:{ IOException -> 0x003f }
        L_0x003c:
            throw r5
        L_0x003d:
            r5 = move-exception
            goto L_0x001d
        L_0x003f:
            r6 = move-exception
            goto L_0x003c
        L_0x0041:
            r5 = move-exception
            r3 = r4
            goto L_0x0037
        L_0x0044:
            r0 = move-exception
            r3 = r4
            goto L_0x0030
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.core.buffer.AbstractIoBuffer.putObject(java.lang.Object):org.apache.mina.core.buffer.IoBuffer");
    }

    public boolean prefixedDataAvailable(int prefixLength) {
        return prefixedDataAvailable(prefixLength, Integer.MAX_VALUE);
    }

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
            case 4:
                dataLength = getInt(position());
                break;
            default:
                throw new IllegalArgumentException("prefixLength: " + prefixLength);
        }
        if (dataLength < 0 || dataLength > maxDataLength) {
            throw new BufferDataException("dataLength: " + dataLength);
        } else if (remaining() - prefixLength >= dataLength) {
            return true;
        } else {
            return false;
        }
    }

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

    public IoBuffer skip(int size) {
        autoExpand(size);
        return position(position() + size);
    }

    public IoBuffer fill(byte value, int size) {
        autoExpand(size);
        int q = size >>> 3;
        int r = size & 7;
        if (q > 0) {
            int intValue = (value & MotionEventCompat.ACTION_MASK) | ((value << 8) & 65280) | ((value << 16) & 16711680) | (value << AnyoMessage.CMD_REPORT_CHARGE_STOPPED);
            long longValue = (((long) intValue) & 4294967295L) | (((long) intValue) << 32);
            for (int i = q; i > 0; i--) {
                putLong(longValue);
            }
        }
        int q2 = r >>> 2;
        int r2 = r & 3;
        if (q2 > 0) {
            putInt((value & MotionEventCompat.ACTION_MASK) | ((value << 8) & 65280) | ((value << 16) & 16711680) | (value << AnyoMessage.CMD_REPORT_CHARGE_STOPPED));
        }
        int q3 = r2 >> 1;
        int r3 = r2 & 1;
        if (q3 > 0) {
            putShort((short) ((value & 255) | (value << 8)));
        }
        if (r3 > 0) {
            put(value);
        }
        return this;
    }

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

    public IoBuffer fill(int size) {
        autoExpand(size);
        int r = size & 7;
        for (int i = size >>> 3; i > 0; i--) {
            putLong(0);
        }
        int q = r >>> 2;
        int r2 = r & 3;
        if (q > 0) {
            putInt(0);
        }
        int q2 = r2 >> 1;
        int r3 = r2 & 1;
        if (q2 > 0) {
            putShort(0);
        }
        if (r3 > 0) {
            put((byte) 0);
        }
        return this;
    }

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

    public <E extends Enum<E>> E getEnum(Class<E> enumClass) {
        return (Enum) toEnum(enumClass, getUnsigned());
    }

    public <E extends Enum<E>> E getEnum(int index, Class<E> enumClass) {
        return (Enum) toEnum(enumClass, getUnsigned(index));
    }

    public <E extends Enum<E>> E getEnumShort(Class<E> enumClass) {
        return (Enum) toEnum(enumClass, getUnsignedShort());
    }

    public <E extends Enum<E>> E getEnumShort(int index, Class<E> enumClass) {
        return (Enum) toEnum(enumClass, getUnsignedShort(index));
    }

    public <E extends Enum<E>> E getEnumInt(Class<E> enumClass) {
        return (Enum) toEnum(enumClass, getInt());
    }

    public <E extends Enum<E>> E getEnumInt(int index, Class<E> enumClass) {
        return (Enum) toEnum(enumClass, getInt(index));
    }

    public IoBuffer putEnum(Enum<?> e) {
        if (((long) e.ordinal()) <= BYTE_MASK) {
            return put((byte) e.ordinal());
        }
        throw new IllegalArgumentException(enumConversionErrorMessage(e, "byte"));
    }

    public IoBuffer putEnum(int index, Enum<?> e) {
        if (((long) e.ordinal()) <= BYTE_MASK) {
            return put(index, (byte) e.ordinal());
        }
        throw new IllegalArgumentException(enumConversionErrorMessage(e, "byte"));
    }

    public IoBuffer putEnumShort(Enum<?> e) {
        if (((long) e.ordinal()) <= SHORT_MASK) {
            return putShort((short) e.ordinal());
        }
        throw new IllegalArgumentException(enumConversionErrorMessage(e, "short"));
    }

    public IoBuffer putEnumShort(int index, Enum<?> e) {
        if (((long) e.ordinal()) <= SHORT_MASK) {
            return putShort(index, (short) e.ordinal());
        }
        throw new IllegalArgumentException(enumConversionErrorMessage(e, "short"));
    }

    public IoBuffer putEnumInt(Enum<?> e) {
        return putInt(e.ordinal());
    }

    public IoBuffer putEnumInt(int index, Enum<?> e) {
        return putInt(index, e.ordinal());
    }

    private <E> E toEnum(Class<E> enumClass, int i) {
        E[] enumConstants = enumClass.getEnumConstants();
        if (i <= enumConstants.length) {
            return enumConstants[i];
        }
        throw new IndexOutOfBoundsException(String.format("%d is too large of an ordinal to convert to the enum %s", new Object[]{Integer.valueOf(i), enumClass.getName()}));
    }

    private String enumConversionErrorMessage(Enum<?> e, String type) {
        return String.format("%s.%s has an ordinal value too large for a %s", new Object[]{e.getClass().getName(), e.name(), type});
    }

    public <E extends Enum<E>> EnumSet<E> getEnumSet(Class<E> enumClass) {
        return toEnumSet(enumClass, ((long) get()) & BYTE_MASK);
    }

    public <E extends Enum<E>> EnumSet<E> getEnumSet(int index, Class<E> enumClass) {
        return toEnumSet(enumClass, ((long) get(index)) & BYTE_MASK);
    }

    public <E extends Enum<E>> EnumSet<E> getEnumSetShort(Class<E> enumClass) {
        return toEnumSet(enumClass, ((long) getShort()) & SHORT_MASK);
    }

    public <E extends Enum<E>> EnumSet<E> getEnumSetShort(int index, Class<E> enumClass) {
        return toEnumSet(enumClass, ((long) getShort(index)) & SHORT_MASK);
    }

    public <E extends Enum<E>> EnumSet<E> getEnumSetInt(Class<E> enumClass) {
        return toEnumSet(enumClass, ((long) getInt()) & 4294967295L);
    }

    public <E extends Enum<E>> EnumSet<E> getEnumSetInt(int index, Class<E> enumClass) {
        return toEnumSet(enumClass, ((long) getInt(index)) & 4294967295L);
    }

    public <E extends Enum<E>> EnumSet<E> getEnumSetLong(Class<E> enumClass) {
        return toEnumSet(enumClass, getLong());
    }

    public <E extends Enum<E>> EnumSet<E> getEnumSetLong(int index, Class<E> enumClass) {
        return toEnumSet(enumClass, getLong(index));
    }

    private <E extends Enum<E>> EnumSet<E> toEnumSet(Class<E> clazz, long vector) {
        EnumSet<E> set = EnumSet.noneOf(clazz);
        long mask = 1;
        for (E e : (Enum[]) clazz.getEnumConstants()) {
            if ((mask & vector) == mask) {
                set.add(e);
            }
            mask <<= 1;
        }
        return set;
    }

    public <E extends Enum<E>> IoBuffer putEnumSet(Set<E> set) {
        long vector = toLong(set);
        if ((-256 & vector) == 0) {
            return put((byte) ((int) vector));
        }
        throw new IllegalArgumentException("The enum set is too large to fit in a byte: " + set);
    }

    public <E extends Enum<E>> IoBuffer putEnumSet(int index, Set<E> set) {
        long vector = toLong(set);
        if ((-256 & vector) == 0) {
            return put(index, (byte) ((int) vector));
        }
        throw new IllegalArgumentException("The enum set is too large to fit in a byte: " + set);
    }

    public <E extends Enum<E>> IoBuffer putEnumSetShort(Set<E> set) {
        long vector = toLong(set);
        if ((-65536 & vector) == 0) {
            return putShort((short) ((int) vector));
        }
        throw new IllegalArgumentException("The enum set is too large to fit in a short: " + set);
    }

    public <E extends Enum<E>> IoBuffer putEnumSetShort(int index, Set<E> set) {
        long vector = toLong(set);
        if ((-65536 & vector) == 0) {
            return putShort(index, (short) ((int) vector));
        }
        throw new IllegalArgumentException("The enum set is too large to fit in a short: " + set);
    }

    public <E extends Enum<E>> IoBuffer putEnumSetInt(Set<E> set) {
        long vector = toLong(set);
        if ((-4294967296L & vector) == 0) {
            return putInt((int) vector);
        }
        throw new IllegalArgumentException("The enum set is too large to fit in an int: " + set);
    }

    public <E extends Enum<E>> IoBuffer putEnumSetInt(int index, Set<E> set) {
        long vector = toLong(set);
        if ((-4294967296L & vector) == 0) {
            return putInt(index, (int) vector);
        }
        throw new IllegalArgumentException("The enum set is too large to fit in an int: " + set);
    }

    public <E extends Enum<E>> IoBuffer putEnumSetLong(Set<E> set) {
        return putLong(toLong(set));
    }

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
