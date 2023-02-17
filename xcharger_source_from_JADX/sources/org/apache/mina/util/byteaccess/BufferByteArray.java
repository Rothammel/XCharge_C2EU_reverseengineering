package org.apache.mina.util.byteaccess;

import java.nio.ByteOrder;
import java.util.Collections;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.util.byteaccess.ByteArray;

public abstract class BufferByteArray extends AbstractByteArray {

    /* renamed from: bb */
    protected IoBuffer f198bb;

    public abstract void free();

    public BufferByteArray(IoBuffer bb) {
        this.f198bb = bb;
    }

    public Iterable<IoBuffer> getIoBuffers() {
        return Collections.singletonList(this.f198bb);
    }

    public IoBuffer getSingleIoBuffer() {
        return this.f198bb;
    }

    public ByteArray slice(int index, int length) {
        int oldLimit = this.f198bb.limit();
        this.f198bb.position(index);
        this.f198bb.limit(index + length);
        IoBuffer slice = this.f198bb.slice();
        this.f198bb.limit(oldLimit);
        return new BufferByteArray(slice) {
            public void free() {
            }
        };
    }

    public ByteArray.Cursor cursor() {
        return new CursorImpl();
    }

    public ByteArray.Cursor cursor(int index) {
        return new CursorImpl(index);
    }

    public int first() {
        return 0;
    }

    public int last() {
        return this.f198bb.limit();
    }

    public ByteOrder order() {
        return this.f198bb.order();
    }

    public void order(ByteOrder order) {
        this.f198bb.order(order);
    }

    public byte get(int index) {
        return this.f198bb.get(index);
    }

    public void put(int index, byte b) {
        this.f198bb.put(index, b);
    }

    public void get(int index, IoBuffer other) {
        this.f198bb.position(index);
        other.put(this.f198bb);
    }

    public void put(int index, IoBuffer other) {
        this.f198bb.position(index);
        this.f198bb.put(other);
    }

    public short getShort(int index) {
        return this.f198bb.getShort(index);
    }

    public void putShort(int index, short s) {
        this.f198bb.putShort(index, s);
    }

    public int getInt(int index) {
        return this.f198bb.getInt(index);
    }

    public void putInt(int index, int i) {
        this.f198bb.putInt(index, i);
    }

    public long getLong(int index) {
        return this.f198bb.getLong(index);
    }

    public void putLong(int index, long l) {
        this.f198bb.putLong(index, l);
    }

    public float getFloat(int index) {
        return this.f198bb.getFloat(index);
    }

    public void putFloat(int index, float f) {
        this.f198bb.putFloat(index, f);
    }

    public double getDouble(int index) {
        return this.f198bb.getDouble(index);
    }

    public void putDouble(int index, double d) {
        this.f198bb.putDouble(index, d);
    }

    public char getChar(int index) {
        return this.f198bb.getChar(index);
    }

    public void putChar(int index, char c) {
        this.f198bb.putChar(index, c);
    }

    private class CursorImpl implements ByteArray.Cursor {
        private int index;

        public CursorImpl() {
        }

        public CursorImpl(int index2) {
            setIndex(index2);
        }

        public int getRemaining() {
            return BufferByteArray.this.last() - this.index;
        }

        public boolean hasRemaining() {
            return getRemaining() > 0;
        }

        public int getIndex() {
            return this.index;
        }

        public void setIndex(int index2) {
            if (index2 < 0 || index2 > BufferByteArray.this.last()) {
                throw new IndexOutOfBoundsException();
            }
            this.index = index2;
        }

        public void skip(int length) {
            setIndex(this.index + length);
        }

        public ByteArray slice(int length) {
            ByteArray slice = BufferByteArray.this.slice(this.index, length);
            this.index += length;
            return slice;
        }

        public ByteOrder order() {
            return BufferByteArray.this.order();
        }

        public byte get() {
            byte b = BufferByteArray.this.get(this.index);
            this.index++;
            return b;
        }

        public void put(byte b) {
            BufferByteArray.this.put(this.index, b);
            this.index++;
        }

        public void get(IoBuffer bb) {
            int size = Math.min(getRemaining(), bb.remaining());
            BufferByteArray.this.get(this.index, bb);
            this.index += size;
        }

        public void put(IoBuffer bb) {
            int size = bb.remaining();
            BufferByteArray.this.put(this.index, bb);
            this.index += size;
        }

        public short getShort() {
            short s = BufferByteArray.this.getShort(this.index);
            this.index += 2;
            return s;
        }

        public void putShort(short s) {
            BufferByteArray.this.putShort(this.index, s);
            this.index += 2;
        }

        public int getInt() {
            int i = BufferByteArray.this.getInt(this.index);
            this.index += 4;
            return i;
        }

        public void putInt(int i) {
            BufferByteArray.this.putInt(this.index, i);
            this.index += 4;
        }

        public long getLong() {
            long l = BufferByteArray.this.getLong(this.index);
            this.index += 8;
            return l;
        }

        public void putLong(long l) {
            BufferByteArray.this.putLong(this.index, l);
            this.index += 8;
        }

        public float getFloat() {
            float f = BufferByteArray.this.getFloat(this.index);
            this.index += 4;
            return f;
        }

        public void putFloat(float f) {
            BufferByteArray.this.putFloat(this.index, f);
            this.index += 4;
        }

        public double getDouble() {
            double d = BufferByteArray.this.getDouble(this.index);
            this.index += 8;
            return d;
        }

        public void putDouble(double d) {
            BufferByteArray.this.putDouble(this.index, d);
            this.index += 8;
        }

        public char getChar() {
            char c = BufferByteArray.this.getChar(this.index);
            this.index += 2;
            return c;
        }

        public void putChar(char c) {
            BufferByteArray.this.putChar(this.index, c);
            this.index += 2;
        }
    }
}
