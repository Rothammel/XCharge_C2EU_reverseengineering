package org.apache.mina.util.byteaccess;

import android.support.v4.view.MotionEventCompat;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.util.byteaccess.ByteArray;
import org.apache.mina.util.byteaccess.ByteArrayList;

/* loaded from: classes.dex */
public final class CompositeByteArray extends AbstractByteArray {
    private final ByteArrayList bas;
    private final ByteArrayFactory byteArrayFactory;
    private ByteOrder order;

    /* loaded from: classes.dex */
    public interface CursorListener {
        void enteredFirstComponent(int i, ByteArray byteArray);

        void enteredLastComponent(int i, ByteArray byteArray);

        void enteredNextComponent(int i, ByteArray byteArray);

        void enteredPreviousComponent(int i, ByteArray byteArray);
    }

    public CompositeByteArray() {
        this(null);
    }

    public CompositeByteArray(ByteArrayFactory byteArrayFactory) {
        this.bas = new ByteArrayList();
        this.byteArrayFactory = byteArrayFactory;
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
            final ByteArray component = removeFirst();
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
                ByteArray ba1 = new BufferByteArray(bb1) { // from class: org.apache.mina.util.byteaccess.CompositeByteArray.1
                    @Override // org.apache.mina.util.byteaccess.BufferByteArray, org.apache.mina.util.byteaccess.ByteArray
                    public void free() {
                    }
                };
                prefix.addLast(ba1);
                remaining -= ba1.last();
                ByteArray ba2 = new BufferByteArray(bb2) { // from class: org.apache.mina.util.byteaccess.CompositeByteArray.2
                    @Override // org.apache.mina.util.byteaccess.BufferByteArray, org.apache.mina.util.byteaccess.ByteArray
                    public void free() {
                        component.free();
                    }
                };
                addFirst(ba2);
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

    @Override // org.apache.mina.util.byteaccess.ByteArray
    public void free() {
        while (!this.bas.isEmpty()) {
            ByteArrayList.Node node = this.bas.getLast();
            node.getByteArray().free();
            this.bas.removeLast();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkBounds(int index, int accessSize) {
        int upper = index + accessSize;
        if (index < first()) {
            throw new IndexOutOfBoundsException("Index " + index + " less than start " + first() + ".");
        }
        if (upper > last()) {
            throw new IndexOutOfBoundsException("Index " + upper + " greater than length " + last() + ".");
        }
    }

    @Override // org.apache.mina.util.byteaccess.ByteArray
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

    @Override // org.apache.mina.util.byteaccess.ByteArray
    public IoBuffer getSingleIoBuffer() {
        if (this.byteArrayFactory == null) {
            throw new IllegalStateException("Can't get single buffer from CompositeByteArray unless it has a ByteArrayFactory.");
        }
        if (this.bas.isEmpty()) {
            return this.byteArrayFactory.create(1).getSingleIoBuffer();
        }
        int actualLength = last() - first();
        ByteArrayList.Node node = this.bas.getFirst();
        ByteArray ba = node.getByteArray();
        if (ba.last() == actualLength) {
            return ba.getSingleIoBuffer();
        }
        ByteArray target = this.byteArrayFactory.create(actualLength);
        IoBuffer bb = target.getSingleIoBuffer();
        ByteArray.Cursor cursor = cursor();
        cursor.put(bb);
        while (!this.bas.isEmpty()) {
            ByteArrayList.Node node2 = this.bas.getLast();
            ByteArray component = node2.getByteArray();
            this.bas.removeLast();
            component.free();
        }
        this.bas.addLast(target);
        return bb;
    }

    @Override // org.apache.mina.util.byteaccess.ByteArray
    public ByteArray.Cursor cursor() {
        return new CursorImpl(this);
    }

    @Override // org.apache.mina.util.byteaccess.ByteArray
    public ByteArray.Cursor cursor(int index) {
        return new CursorImpl(this, index);
    }

    public ByteArray.Cursor cursor(CursorListener listener) {
        return new CursorImpl(this, listener);
    }

    public ByteArray.Cursor cursor(int index, CursorListener listener) {
        return new CursorImpl(index, listener);
    }

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteReader
    public ByteArray slice(int index, int length) {
        return cursor(index).slice(length);
    }

    @Override // org.apache.mina.util.byteaccess.ByteArray, org.apache.mina.util.byteaccess.IoAbsoluteReader
    public byte get(int index) {
        return cursor(index).get();
    }

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteWriter
    public void put(int index, byte b) {
        cursor(index).put(b);
    }

    @Override // org.apache.mina.util.byteaccess.ByteArray, org.apache.mina.util.byteaccess.IoAbsoluteReader
    public void get(int index, IoBuffer bb) {
        cursor(index).get(bb);
    }

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteWriter
    public void put(int index, IoBuffer bb) {
        cursor(index).put(bb);
    }

    @Override // org.apache.mina.util.byteaccess.ByteArray, org.apache.mina.util.byteaccess.IoAbsoluteReader, org.apache.mina.util.byteaccess.IoAbsoluteWriter
    public int first() {
        return this.bas.firstByte();
    }

    @Override // org.apache.mina.util.byteaccess.ByteArray, org.apache.mina.util.byteaccess.IoAbsoluteReader, org.apache.mina.util.byteaccess.IoAbsoluteWriter
    public int last() {
        return this.bas.lastByte();
    }

    private void addHook(ByteArray ba) {
        if (ba.first() != 0) {
            throw new IllegalArgumentException("Cannot add byte array that doesn't start from 0: " + ba.first());
        }
        if (this.order == null) {
            this.order = ba.order();
        } else if (!this.order.equals(ba.order())) {
            throw new IllegalArgumentException("Cannot add byte array with different byte order: " + ba.order());
        }
    }

    @Override // org.apache.mina.util.byteaccess.ByteArray, org.apache.mina.util.byteaccess.IoAbsoluteReader, org.apache.mina.util.byteaccess.IoAbsoluteWriter
    public ByteOrder order() {
        if (this.order == null) {
            throw new IllegalStateException("Byte order not yet set.");
        }
        return this.order;
    }

    @Override // org.apache.mina.util.byteaccess.ByteArray
    public void order(ByteOrder order) {
        if (order == null || !order.equals(this.order)) {
            this.order = order;
            if (!this.bas.isEmpty()) {
                for (ByteArrayList.Node node = this.bas.getFirst(); node.hasNextNode(); node = node.getNextNode()) {
                    node.getByteArray().order(order);
                }
            }
        }
    }

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteReader
    public short getShort(int index) {
        return cursor(index).getShort();
    }

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteWriter
    public void putShort(int index, short s) {
        cursor(index).putShort(s);
    }

    @Override // org.apache.mina.util.byteaccess.ByteArray, org.apache.mina.util.byteaccess.IoAbsoluteReader
    public int getInt(int index) {
        return cursor(index).getInt();
    }

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteWriter
    public void putInt(int index, int i) {
        cursor(index).putInt(i);
    }

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteReader
    public long getLong(int index) {
        return cursor(index).getLong();
    }

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteWriter
    public void putLong(int index, long l) {
        cursor(index).putLong(l);
    }

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteReader
    public float getFloat(int index) {
        return cursor(index).getFloat();
    }

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteWriter
    public void putFloat(int index, float f) {
        cursor(index).putFloat(f);
    }

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteReader
    public double getDouble(int index) {
        return cursor(index).getDouble();
    }

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteWriter
    public void putDouble(int index, double d) {
        cursor(index).putDouble(d);
    }

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteReader
    public char getChar(int index) {
        return cursor(index).getChar();
    }

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteWriter
    public void putChar(int index, char c) {
        cursor(index).putChar(c);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class CursorImpl implements ByteArray.Cursor {
        private ByteArray.Cursor componentCursor;
        private int componentIndex;
        private ByteArrayList.Node componentNode;
        private int index;
        private final CursorListener listener;

        public CursorImpl(CompositeByteArray compositeByteArray) {
            this(0, null);
        }

        public CursorImpl(CompositeByteArray compositeByteArray, int index) {
            this(index, null);
        }

        public CursorImpl(CompositeByteArray compositeByteArray, CursorListener listener) {
            this(0, listener);
        }

        public CursorImpl(int index, CursorListener listener) {
            this.index = index;
            this.listener = listener;
        }

        @Override // org.apache.mina.util.byteaccess.ByteArray.Cursor
        public int getIndex() {
            return this.index;
        }

        @Override // org.apache.mina.util.byteaccess.ByteArray.Cursor
        public void setIndex(int index) {
            CompositeByteArray.this.checkBounds(index, 0);
            this.index = index;
        }

        @Override // org.apache.mina.util.byteaccess.IoRelativeReader, org.apache.mina.util.byteaccess.IoRelativeWriter
        public void skip(int length) {
            setIndex(this.index + length);
        }

        @Override // org.apache.mina.util.byteaccess.IoRelativeReader
        public ByteArray slice(int length) {
            CompositeByteArray slice = new CompositeByteArray(CompositeByteArray.this.byteArrayFactory);
            int remaining = length;
            while (remaining > 0) {
                prepareForAccess(remaining);
                int componentSliceSize = Math.min(remaining, this.componentCursor.getRemaining());
                ByteArray componentSlice = this.componentCursor.slice(componentSliceSize);
                slice.addLast(componentSlice);
                this.index += componentSliceSize;
                remaining -= componentSliceSize;
            }
            return slice;
        }

        @Override // org.apache.mina.util.byteaccess.IoRelativeReader, org.apache.mina.util.byteaccess.IoRelativeWriter
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
                int basMidpoint = ((CompositeByteArray.this.last() - CompositeByteArray.this.first()) / 2) + CompositeByteArray.this.first();
                if (this.index <= basMidpoint) {
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

        @Override // org.apache.mina.util.byteaccess.ByteArray.Cursor, org.apache.mina.util.byteaccess.IoRelativeReader, org.apache.mina.util.byteaccess.IoRelativeWriter
        public int getRemaining() {
            return (CompositeByteArray.this.last() - this.index) + 1;
        }

        @Override // org.apache.mina.util.byteaccess.ByteArray.Cursor, org.apache.mina.util.byteaccess.IoRelativeReader, org.apache.mina.util.byteaccess.IoRelativeWriter
        public boolean hasRemaining() {
            return getRemaining() > 0;
        }

        @Override // org.apache.mina.util.byteaccess.ByteArray.Cursor, org.apache.mina.util.byteaccess.IoRelativeReader
        public byte get() {
            prepareForAccess(1);
            byte b = this.componentCursor.get();
            this.index++;
            return b;
        }

        @Override // org.apache.mina.util.byteaccess.IoRelativeWriter
        public void put(byte b) {
            prepareForAccess(1);
            this.componentCursor.put(b);
            this.index++;
        }

        @Override // org.apache.mina.util.byteaccess.ByteArray.Cursor, org.apache.mina.util.byteaccess.IoRelativeReader
        public void get(IoBuffer bb) {
            while (bb.hasRemaining()) {
                int remainingBefore = bb.remaining();
                prepareForAccess(remainingBefore);
                this.componentCursor.get(bb);
                int remainingAfter = bb.remaining();
                int chunkSize = remainingBefore - remainingAfter;
                this.index += chunkSize;
            }
        }

        @Override // org.apache.mina.util.byteaccess.IoRelativeWriter
        public void put(IoBuffer bb) {
            while (bb.hasRemaining()) {
                int remainingBefore = bb.remaining();
                prepareForAccess(remainingBefore);
                this.componentCursor.put(bb);
                int remainingAfter = bb.remaining();
                int chunkSize = remainingBefore - remainingAfter;
                this.index += chunkSize;
            }
        }

        @Override // org.apache.mina.util.byteaccess.IoRelativeReader
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
                short s2 = (short) ((b0 << 8) | (b1 & 255));
                return s2;
            }
            short s3 = (short) ((b1 << 8) | (b0 & 255));
            return s3;
        }

        @Override // org.apache.mina.util.byteaccess.IoRelativeWriter
        public void putShort(short s) {
            byte b0;
            byte b1;
            prepareForAccess(2);
            if (this.componentCursor.getRemaining() < 4) {
                if (CompositeByteArray.this.order.equals(ByteOrder.BIG_ENDIAN)) {
                    b0 = (byte) ((s >> 8) & MotionEventCompat.ACTION_MASK);
                    b1 = (byte) ((s >> 0) & MotionEventCompat.ACTION_MASK);
                } else {
                    b0 = (byte) ((s >> 0) & MotionEventCompat.ACTION_MASK);
                    b1 = (byte) ((s >> 8) & MotionEventCompat.ACTION_MASK);
                }
                put(b0);
                put(b1);
                return;
            }
            this.componentCursor.putShort(s);
            this.index += 2;
        }

        @Override // org.apache.mina.util.byteaccess.ByteArray.Cursor, org.apache.mina.util.byteaccess.IoRelativeReader
        public int getInt() {
            prepareForAccess(4);
            if (this.componentCursor.getRemaining() >= 4) {
                int i = this.componentCursor.getInt();
                this.index += 4;
                return i;
            }
            byte b0 = get();
            byte b1 = get();
            byte b2 = get();
            byte b3 = get();
            if (CompositeByteArray.this.order.equals(ByteOrder.BIG_ENDIAN)) {
                int i2 = (b0 << AnyoMessage.CMD_REPORT_CHARGE_STOPPED) | ((b1 & 255) << 16) | ((b2 & 255) << 8) | (b3 & 255);
                return i2;
            }
            int i3 = (b3 << AnyoMessage.CMD_REPORT_CHARGE_STOPPED) | ((b2 & 255) << 16) | ((b1 & 255) << 8) | (b0 & 255);
            return i3;
        }

        @Override // org.apache.mina.util.byteaccess.IoRelativeWriter
        public void putInt(int i) {
            byte b0;
            byte b1;
            byte b2;
            byte b3;
            prepareForAccess(4);
            if (this.componentCursor.getRemaining() < 4) {
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
                return;
            }
            this.componentCursor.putInt(i);
            this.index += 4;
        }

        @Override // org.apache.mina.util.byteaccess.IoRelativeReader
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
                long l2 = ((b0 & 255) << 56) | ((b1 & 255) << 48) | ((b2 & 255) << 40) | ((b3 & 255) << 32) | ((b4 & 255) << 24) | ((b5 & 255) << 16) | ((b6 & 255) << 8) | (b7 & 255);
                return l2;
            }
            long l3 = ((b7 & 255) << 56) | ((b6 & 255) << 48) | ((b5 & 255) << 40) | ((b4 & 255) << 32) | ((b3 & 255) << 24) | ((b2 & 255) << 16) | ((b1 & 255) << 8) | (b0 & 255);
            return l3;
        }

        @Override // org.apache.mina.util.byteaccess.IoRelativeWriter
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
            if (this.componentCursor.getRemaining() < 4) {
                if (CompositeByteArray.this.order.equals(ByteOrder.BIG_ENDIAN)) {
                    b0 = (byte) ((l >> 56) & 255);
                    b1 = (byte) ((l >> 48) & 255);
                    b2 = (byte) ((l >> 40) & 255);
                    b3 = (byte) ((l >> 32) & 255);
                    b4 = (byte) ((l >> 24) & 255);
                    b5 = (byte) ((l >> 16) & 255);
                    b6 = (byte) ((l >> 8) & 255);
                    b7 = (byte) ((l >> 0) & 255);
                } else {
                    b0 = (byte) ((l >> 0) & 255);
                    b1 = (byte) ((l >> 8) & 255);
                    b2 = (byte) ((l >> 16) & 255);
                    b3 = (byte) ((l >> 24) & 255);
                    b4 = (byte) ((l >> 32) & 255);
                    b5 = (byte) ((l >> 40) & 255);
                    b6 = (byte) ((l >> 48) & 255);
                    b7 = (byte) ((l >> 56) & 255);
                }
                put(b0);
                put(b1);
                put(b2);
                put(b3);
                put(b4);
                put(b5);
                put(b6);
                put(b7);
                return;
            }
            this.componentCursor.putLong(l);
            this.index += 8;
        }

        @Override // org.apache.mina.util.byteaccess.IoRelativeReader
        public float getFloat() {
            prepareForAccess(4);
            if (this.componentCursor.getRemaining() >= 4) {
                float f = this.componentCursor.getFloat();
                this.index += 4;
                return f;
            }
            int i = getInt();
            float f2 = Float.intBitsToFloat(i);
            return f2;
        }

        @Override // org.apache.mina.util.byteaccess.IoRelativeWriter
        public void putFloat(float f) {
            prepareForAccess(4);
            if (this.componentCursor.getRemaining() >= 4) {
                this.componentCursor.putFloat(f);
                this.index += 4;
                return;
            }
            int i = Float.floatToIntBits(f);
            putInt(i);
        }

        @Override // org.apache.mina.util.byteaccess.IoRelativeReader
        public double getDouble() {
            prepareForAccess(8);
            if (this.componentCursor.getRemaining() >= 4) {
                double d = this.componentCursor.getDouble();
                this.index += 8;
                return d;
            }
            long l = getLong();
            double d2 = Double.longBitsToDouble(l);
            return d2;
        }

        @Override // org.apache.mina.util.byteaccess.IoRelativeWriter
        public void putDouble(double d) {
            prepareForAccess(8);
            if (this.componentCursor.getRemaining() >= 4) {
                this.componentCursor.putDouble(d);
                this.index += 8;
                return;
            }
            long l = Double.doubleToLongBits(d);
            putLong(l);
        }

        @Override // org.apache.mina.util.byteaccess.IoRelativeReader
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
                char c2 = (char) ((b0 << 8) | (b1 & 255));
                return c2;
            }
            char c3 = (char) ((b1 << 8) | (b0 & 255));
            return c3;
        }

        @Override // org.apache.mina.util.byteaccess.IoRelativeWriter
        public void putChar(char c) {
            byte b0;
            byte b1;
            prepareForAccess(2);
            if (this.componentCursor.getRemaining() < 4) {
                if (CompositeByteArray.this.order.equals(ByteOrder.BIG_ENDIAN)) {
                    b0 = (byte) ((c >> '\b') & MotionEventCompat.ACTION_MASK);
                    b1 = (byte) ((c >> 0) & MotionEventCompat.ACTION_MASK);
                } else {
                    b0 = (byte) ((c >> 0) & MotionEventCompat.ACTION_MASK);
                    b1 = (byte) ((c >> '\b') & MotionEventCompat.ACTION_MASK);
                }
                put(b0);
                put(b1);
                return;
            }
            this.componentCursor.putChar(c);
            this.index += 2;
        }
    }
}
