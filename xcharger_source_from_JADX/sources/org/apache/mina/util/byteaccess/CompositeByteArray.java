package org.apache.mina.util.byteaccess;

import android.support.p000v4.view.MotionEventCompat;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.util.byteaccess.ByteArray;
import org.apache.mina.util.byteaccess.ByteArrayList;

public final class CompositeByteArray extends AbstractByteArray {
    /* access modifiers changed from: private */
    public final ByteArrayList bas;
    /* access modifiers changed from: private */
    public final ByteArrayFactory byteArrayFactory;
    /* access modifiers changed from: private */
    public ByteOrder order;

    public interface CursorListener {
        void enteredFirstComponent(int i, ByteArray byteArray);

        void enteredLastComponent(int i, ByteArray byteArray);

        void enteredNextComponent(int i, ByteArray byteArray);

        void enteredPreviousComponent(int i, ByteArray byteArray);
    }

    public CompositeByteArray() {
        this((ByteArrayFactory) null);
    }

    public CompositeByteArray(ByteArrayFactory byteArrayFactory2) {
        this.bas = new ByteArrayList();
        this.byteArrayFactory = byteArrayFactory2;
    }

    public ByteArray getFirst() {
        if (this.bas.isEmpty()) {
            return null;
        }
        return this.bas.getFirst().getByteArray();
    }

    public void addFirst(ByteArray ba) {
        addHook(ba);
        this.bas.addFirst(ba);
    }

    public ByteArray removeFirst() {
        ByteArrayList.Node node = this.bas.removeFirst();
        if (node == null) {
            return null;
        }
        return node.getByteArray();
    }

    public ByteArray removeTo(int index) {
        if (index < first() || index > last()) {
            throw new IndexOutOfBoundsException();
        }
        CompositeByteArray prefix = new CompositeByteArray(this.byteArrayFactory);
        int remaining = index - first();
        while (remaining > 0) {
            ByteArray component = removeFirst();
            if (component.last() <= remaining) {
                prefix.addLast(component);
                remaining -= component.last();
            } else {
                IoBuffer bb = component.getSingleIoBuffer();
                int originalLimit = bb.limit();
                bb.position(0);
                bb.limit(remaining);
                IoBuffer bb1 = bb.slice();
                bb.position(remaining);
                bb.limit(originalLimit);
                IoBuffer bb2 = bb.slice();
                ByteArray ba1 = new BufferByteArray(bb1) {
                    public void free() {
                    }
                };
                prefix.addLast(ba1);
                remaining -= ba1.last();
                final ByteArray componentFinal = component;
                addFirst(new BufferByteArray(bb2) {
                    public void free() {
                        componentFinal.free();
                    }
                });
            }
        }
        return prefix;
    }

    public void addLast(ByteArray ba) {
        addHook(ba);
        this.bas.addLast(ba);
    }

    public ByteArray removeLast() {
        ByteArrayList.Node node = this.bas.removeLast();
        if (node == null) {
            return null;
        }
        return node.getByteArray();
    }

    public void free() {
        while (!this.bas.isEmpty()) {
            this.bas.getLast().getByteArray().free();
            this.bas.removeLast();
        }
    }

    /* access modifiers changed from: private */
    public void checkBounds(int index, int accessSize) {
        int lower = index;
        int upper = index + accessSize;
        if (lower < first()) {
            throw new IndexOutOfBoundsException("Index " + lower + " less than start " + first() + ".");
        } else if (upper > last()) {
            throw new IndexOutOfBoundsException("Index " + upper + " greater than length " + last() + ".");
        }
    }

    public Iterable<IoBuffer> getIoBuffers() {
        if (this.bas.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<IoBuffer> result = new ArrayList<>();
        ByteArrayList.Node node = this.bas.getFirst();
        for (IoBuffer bb : node.getByteArray().getIoBuffers()) {
            result.add(bb);
        }
        while (node.hasNextNode()) {
            node = node.getNextNode();
            for (IoBuffer bb2 : node.getByteArray().getIoBuffers()) {
                result.add(bb2);
            }
        }
        return result;
    }

    public IoBuffer getSingleIoBuffer() {
        if (this.byteArrayFactory == null) {
            throw new IllegalStateException("Can't get single buffer from CompositeByteArray unless it has a ByteArrayFactory.");
        } else if (this.bas.isEmpty()) {
            return this.byteArrayFactory.create(1).getSingleIoBuffer();
        } else {
            int actualLength = last() - first();
            ByteArray ba = this.bas.getFirst().getByteArray();
            if (ba.last() == actualLength) {
                return ba.getSingleIoBuffer();
            }
            ByteArray target = this.byteArrayFactory.create(actualLength);
            IoBuffer bb = target.getSingleIoBuffer();
            cursor().put(bb);
            while (!this.bas.isEmpty()) {
                ByteArray component = this.bas.getLast().getByteArray();
                this.bas.removeLast();
                component.free();
            }
            this.bas.addLast(target);
            return bb;
        }
    }

    public ByteArray.Cursor cursor() {
        return new CursorImpl(this);
    }

    public ByteArray.Cursor cursor(int index) {
        return new CursorImpl(this, index);
    }

    public ByteArray.Cursor cursor(CursorListener listener) {
        return new CursorImpl(this, listener);
    }

    public ByteArray.Cursor cursor(int index, CursorListener listener) {
        return new CursorImpl(index, listener);
    }

    public ByteArray slice(int index, int length) {
        return cursor(index).slice(length);
    }

    public byte get(int index) {
        return cursor(index).get();
    }

    public void put(int index, byte b) {
        cursor(index).put(b);
    }

    public void get(int index, IoBuffer bb) {
        cursor(index).get(bb);
    }

    public void put(int index, IoBuffer bb) {
        cursor(index).put(bb);
    }

    public int first() {
        return this.bas.firstByte();
    }

    public int last() {
        return this.bas.lastByte();
    }

    private void addHook(ByteArray ba) {
        if (ba.first() != 0) {
            throw new IllegalArgumentException("Cannot add byte array that doesn't start from 0: " + ba.first());
        } else if (this.order == null) {
            this.order = ba.order();
        } else if (!this.order.equals(ba.order())) {
            throw new IllegalArgumentException("Cannot add byte array with different byte order: " + ba.order());
        }
    }

    public ByteOrder order() {
        if (this.order != null) {
            return this.order;
        }
        throw new IllegalStateException("Byte order not yet set.");
    }

    public void order(ByteOrder order2) {
        if (order2 == null || !order2.equals(this.order)) {
            this.order = order2;
            if (!this.bas.isEmpty()) {
                for (ByteArrayList.Node node = this.bas.getFirst(); node.hasNextNode(); node = node.getNextNode()) {
                    node.getByteArray().order(order2);
                }
            }
        }
    }

    public short getShort(int index) {
        return cursor(index).getShort();
    }

    public void putShort(int index, short s) {
        cursor(index).putShort(s);
    }

    public int getInt(int index) {
        return cursor(index).getInt();
    }

    public void putInt(int index, int i) {
        cursor(index).putInt(i);
    }

    public long getLong(int index) {
        return cursor(index).getLong();
    }

    public void putLong(int index, long l) {
        cursor(index).putLong(l);
    }

    public float getFloat(int index) {
        return cursor(index).getFloat();
    }

    public void putFloat(int index, float f) {
        cursor(index).putFloat(f);
    }

    public double getDouble(int index) {
        return cursor(index).getDouble();
    }

    public void putDouble(int index, double d) {
        cursor(index).putDouble(d);
    }

    public char getChar(int index) {
        return cursor(index).getChar();
    }

    public void putChar(int index, char c) {
        cursor(index).putChar(c);
    }

    private class CursorImpl implements ByteArray.Cursor {
        private ByteArray.Cursor componentCursor;
        private int componentIndex;
        private ByteArrayList.Node componentNode;
        private int index;
        private final CursorListener listener;

        public CursorImpl(CompositeByteArray compositeByteArray) {
            this(0, (CursorListener) null);
        }

        public CursorImpl(CompositeByteArray compositeByteArray, int index2) {
            this(index2, (CursorListener) null);
        }

        public CursorImpl(CompositeByteArray compositeByteArray, CursorListener listener2) {
            this(0, listener2);
        }

        public CursorImpl(int index2, CursorListener listener2) {
            this.index = index2;
            this.listener = listener2;
        }

        public int getIndex() {
            return this.index;
        }

        public void setIndex(int index2) {
            CompositeByteArray.this.checkBounds(index2, 0);
            this.index = index2;
        }

        public void skip(int length) {
            setIndex(this.index + length);
        }

        public ByteArray slice(int length) {
            CompositeByteArray slice = new CompositeByteArray(CompositeByteArray.this.byteArrayFactory);
            int remaining = length;
            while (remaining > 0) {
                prepareForAccess(remaining);
                int componentSliceSize = Math.min(remaining, this.componentCursor.getRemaining());
                slice.addLast(this.componentCursor.slice(componentSliceSize));
                this.index += componentSliceSize;
                remaining -= componentSliceSize;
            }
            return slice;
        }

        public ByteOrder order() {
            return CompositeByteArray.this.order();
        }

        private void prepareForAccess(int accessSize) {
            if (this.componentNode != null && this.componentNode.isRemoved()) {
                this.componentNode = null;
                this.componentCursor = null;
            }
            CompositeByteArray.this.checkBounds(this.index, accessSize);
            ByteArrayList.Node oldComponentNode = this.componentNode;
            if (this.componentNode == null) {
                if (this.index <= ((CompositeByteArray.this.last() - CompositeByteArray.this.first()) / 2) + CompositeByteArray.this.first()) {
                    this.componentNode = CompositeByteArray.this.bas.getFirst();
                    this.componentIndex = CompositeByteArray.this.first();
                    if (this.listener != null) {
                        this.listener.enteredFirstComponent(this.componentIndex, this.componentNode.getByteArray());
                    }
                } else {
                    this.componentNode = CompositeByteArray.this.bas.getLast();
                    this.componentIndex = CompositeByteArray.this.last() - this.componentNode.getByteArray().last();
                    if (this.listener != null) {
                        this.listener.enteredLastComponent(this.componentIndex, this.componentNode.getByteArray());
                    }
                }
            }
            while (this.index < this.componentIndex) {
                this.componentNode = this.componentNode.getPreviousNode();
                this.componentIndex -= this.componentNode.getByteArray().last();
                if (this.listener != null) {
                    this.listener.enteredPreviousComponent(this.componentIndex, this.componentNode.getByteArray());
                }
            }
            while (this.index >= this.componentIndex + this.componentNode.getByteArray().length()) {
                this.componentIndex += this.componentNode.getByteArray().last();
                this.componentNode = this.componentNode.getNextNode();
                if (this.listener != null) {
                    this.listener.enteredNextComponent(this.componentIndex, this.componentNode.getByteArray());
                }
            }
            int internalComponentIndex = this.index - this.componentIndex;
            if (this.componentNode == oldComponentNode) {
                this.componentCursor.setIndex(internalComponentIndex);
            } else {
                this.componentCursor = this.componentNode.getByteArray().cursor(internalComponentIndex);
            }
        }

        public int getRemaining() {
            return (CompositeByteArray.this.last() - this.index) + 1;
        }

        public boolean hasRemaining() {
            return getRemaining() > 0;
        }

        public byte get() {
            prepareForAccess(1);
            byte b = this.componentCursor.get();
            this.index++;
            return b;
        }

        public void put(byte b) {
            prepareForAccess(1);
            this.componentCursor.put(b);
            this.index++;
        }

        public void get(IoBuffer bb) {
            while (bb.hasRemaining()) {
                int remainingBefore = bb.remaining();
                prepareForAccess(remainingBefore);
                this.componentCursor.get(bb);
                this.index += remainingBefore - bb.remaining();
            }
        }

        public void put(IoBuffer bb) {
            while (bb.hasRemaining()) {
                int remainingBefore = bb.remaining();
                prepareForAccess(remainingBefore);
                this.componentCursor.put(bb);
                this.index += remainingBefore - bb.remaining();
            }
        }

        public short getShort() {
            prepareForAccess(2);
            if (this.componentCursor.getRemaining() >= 4) {
                short s = this.componentCursor.getShort();
                this.index += 2;
                return s;
            }
            byte b0 = get();
            byte b1 = get();
            if (CompositeByteArray.this.order.equals(ByteOrder.BIG_ENDIAN)) {
                return (short) ((b0 << 8) | (b1 & 255));
            }
            return (short) ((b1 << 8) | (b0 & 255));
        }

        public void putShort(short s) {
            byte b0;
            byte b1;
            prepareForAccess(2);
            if (this.componentCursor.getRemaining() >= 4) {
                this.componentCursor.putShort(s);
                this.index += 2;
                return;
            }
            if (CompositeByteArray.this.order.equals(ByteOrder.BIG_ENDIAN)) {
                b0 = (byte) ((s >> 8) & MotionEventCompat.ACTION_MASK);
                b1 = (byte) ((s >> 0) & MotionEventCompat.ACTION_MASK);
            } else {
                b0 = (byte) ((s >> 0) & MotionEventCompat.ACTION_MASK);
                b1 = (byte) ((s >> 8) & MotionEventCompat.ACTION_MASK);
            }
            put(b0);
            put(b1);
        }

        public int getInt() {
            prepareForAccess(4);
            if (this.componentCursor.getRemaining() >= 4) {
                int i = this.componentCursor.getInt();
                this.index += 4;
                return i;
            }
            int b0 = get();
            byte b1 = get();
            byte b2 = get();
            int b3 = get();
            if (CompositeByteArray.this.order.equals(ByteOrder.BIG_ENDIAN)) {
                return (b0 << 24) | ((b1 & 255) << 16) | ((b2 & 255) << 8) | (b3 & MotionEventCompat.ACTION_MASK);
            }
            return (b3 << 24) | ((b2 & 255) << 16) | ((b1 & 255) << 8) | (b0 & MotionEventCompat.ACTION_MASK);
        }

        public void putInt(int i) {
            byte b0;
            byte b1;
            byte b2;
            byte b3;
            prepareForAccess(4);
            if (this.componentCursor.getRemaining() >= 4) {
                this.componentCursor.putInt(i);
                this.index += 4;
                return;
            }
            if (CompositeByteArray.this.order.equals(ByteOrder.BIG_ENDIAN)) {
                b0 = (byte) ((i >> 24) & MotionEventCompat.ACTION_MASK);
                b1 = (byte) ((i >> 16) & MotionEventCompat.ACTION_MASK);
                b2 = (byte) ((i >> 8) & MotionEventCompat.ACTION_MASK);
                b3 = (byte) ((i >> 0) & MotionEventCompat.ACTION_MASK);
            } else {
                b0 = (byte) ((i >> 0) & MotionEventCompat.ACTION_MASK);
                b1 = (byte) ((i >> 8) & MotionEventCompat.ACTION_MASK);
                b2 = (byte) ((i >> 16) & MotionEventCompat.ACTION_MASK);
                b3 = (byte) ((i >> 24) & MotionEventCompat.ACTION_MASK);
            }
            put(b0);
            put(b1);
            put(b2);
            put(b3);
        }

        public long getLong() {
            prepareForAccess(8);
            if (this.componentCursor.getRemaining() >= 4) {
                long l = this.componentCursor.getLong();
                this.index += 8;
                return l;
            }
            byte b0 = get();
            byte b1 = get();
            byte b2 = get();
            byte b3 = get();
            byte b4 = get();
            byte b5 = get();
            byte b6 = get();
            byte b7 = get();
            if (CompositeByteArray.this.order.equals(ByteOrder.BIG_ENDIAN)) {
                return ((((long) b0) & 255) << 56) | ((((long) b1) & 255) << 48) | ((((long) b2) & 255) << 40) | ((((long) b3) & 255) << 32) | ((((long) b4) & 255) << 24) | ((((long) b5) & 255) << 16) | ((((long) b6) & 255) << 8) | (((long) b7) & 255);
            }
            return ((((long) b7) & 255) << 56) | ((((long) b6) & 255) << 48) | ((((long) b5) & 255) << 40) | ((((long) b4) & 255) << 32) | ((((long) b3) & 255) << 24) | ((((long) b2) & 255) << 16) | ((((long) b1) & 255) << 8) | (((long) b0) & 255);
        }

        public void putLong(long l) {
            byte b0;
            byte b1;
            byte b2;
            byte b3;
            byte b4;
            byte b5;
            byte b6;
            byte b7;
            prepareForAccess(8);
            if (this.componentCursor.getRemaining() >= 4) {
                this.componentCursor.putLong(l);
                this.index += 8;
                return;
            }
            if (CompositeByteArray.this.order.equals(ByteOrder.BIG_ENDIAN)) {
                b0 = (byte) ((int) ((l >> 56) & 255));
                b1 = (byte) ((int) ((l >> 48) & 255));
                b2 = (byte) ((int) ((l >> 40) & 255));
                b3 = (byte) ((int) ((l >> 32) & 255));
                b4 = (byte) ((int) ((l >> 24) & 255));
                b5 = (byte) ((int) ((l >> 16) & 255));
                b6 = (byte) ((int) ((l >> 8) & 255));
                b7 = (byte) ((int) ((l >> 0) & 255));
            } else {
                b0 = (byte) ((int) ((l >> 0) & 255));
                b1 = (byte) ((int) ((l >> 8) & 255));
                b2 = (byte) ((int) ((l >> 16) & 255));
                b3 = (byte) ((int) ((l >> 24) & 255));
                b4 = (byte) ((int) ((l >> 32) & 255));
                b5 = (byte) ((int) ((l >> 40) & 255));
                b6 = (byte) ((int) ((l >> 48) & 255));
                b7 = (byte) ((int) ((l >> 56) & 255));
            }
            put(b0);
            put(b1);
            put(b2);
            put(b3);
            put(b4);
            put(b5);
            put(b6);
            put(b7);
        }

        public float getFloat() {
            prepareForAccess(4);
            if (this.componentCursor.getRemaining() < 4) {
                return Float.intBitsToFloat(getInt());
            }
            float f = this.componentCursor.getFloat();
            this.index += 4;
            return f;
        }

        public void putFloat(float f) {
            prepareForAccess(4);
            if (this.componentCursor.getRemaining() >= 4) {
                this.componentCursor.putFloat(f);
                this.index += 4;
                return;
            }
            putInt(Float.floatToIntBits(f));
        }

        public double getDouble() {
            prepareForAccess(8);
            if (this.componentCursor.getRemaining() < 4) {
                return Double.longBitsToDouble(getLong());
            }
            double d = this.componentCursor.getDouble();
            this.index += 8;
            return d;
        }

        public void putDouble(double d) {
            prepareForAccess(8);
            if (this.componentCursor.getRemaining() >= 4) {
                this.componentCursor.putDouble(d);
                this.index += 8;
                return;
            }
            putLong(Double.doubleToLongBits(d));
        }

        public char getChar() {
            prepareForAccess(2);
            if (this.componentCursor.getRemaining() >= 4) {
                char c = this.componentCursor.getChar();
                this.index += 2;
                return c;
            }
            byte b0 = get();
            byte b1 = get();
            if (CompositeByteArray.this.order.equals(ByteOrder.BIG_ENDIAN)) {
                return (char) ((b0 << 8) | (b1 & 255));
            }
            return (char) ((b1 << 8) | (b0 & 255));
        }

        public void putChar(char c) {
            byte b0;
            byte b1;
            prepareForAccess(2);
            if (this.componentCursor.getRemaining() >= 4) {
                this.componentCursor.putChar(c);
                this.index += 2;
                return;
            }
            if (CompositeByteArray.this.order.equals(ByteOrder.BIG_ENDIAN)) {
                b0 = (byte) ((c >> 8) & MotionEventCompat.ACTION_MASK);
                b1 = (byte) ((c >> 0) & MotionEventCompat.ACTION_MASK);
            } else {
                b0 = (byte) ((c >> 0) & MotionEventCompat.ACTION_MASK);
                b1 = (byte) ((c >> 8) & MotionEventCompat.ACTION_MASK);
            }
            put(b0);
            put(b1);
        }
    }
}
