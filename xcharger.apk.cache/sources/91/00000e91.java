package org.apache.mina.util.byteaccess;

import java.nio.ByteOrder;
import org.apache.mina.core.buffer.IoBuffer;

/* loaded from: classes.dex */
public class CompositeByteArrayRelativeWriter extends CompositeByteArrayRelativeBase implements IoRelativeWriter {
    private final boolean autoFlush;
    private final Expander expander;
    private final Flusher flusher;

    /* loaded from: classes.dex */
    public interface Expander {
        void expand(CompositeByteArray compositeByteArray, int i);
    }

    /* loaded from: classes.dex */
    public interface Flusher {
        void flush(ByteArray byteArray);
    }

    @Override // org.apache.mina.util.byteaccess.CompositeByteArrayRelativeBase, org.apache.mina.util.byteaccess.IoRelativeReader, org.apache.mina.util.byteaccess.IoRelativeWriter
    public /* bridge */ /* synthetic */ ByteOrder order() {
        return super.order();
    }

    /* loaded from: classes.dex */
    public static class NopExpander implements Expander {
        @Override // org.apache.mina.util.byteaccess.CompositeByteArrayRelativeWriter.Expander
        public void expand(CompositeByteArray cba, int minSize) {
        }
    }

    /* loaded from: classes.dex */
    public static class ChunkedExpander implements Expander {
        private final ByteArrayFactory baf;
        private final int newComponentSize;

        public ChunkedExpander(ByteArrayFactory baf, int newComponentSize) {
            this.baf = baf;
            this.newComponentSize = newComponentSize;
        }

        @Override // org.apache.mina.util.byteaccess.CompositeByteArrayRelativeWriter.Expander
        public void expand(CompositeByteArray cba, int minSize) {
            int remaining = minSize;
            while (remaining > 0) {
                ByteArray component = this.baf.create(this.newComponentSize);
                cba.addLast(component);
                remaining -= this.newComponentSize;
            }
        }
    }

    public CompositeByteArrayRelativeWriter(CompositeByteArray cba, Expander expander, Flusher flusher, boolean autoFlush) {
        super(cba);
        this.expander = expander;
        this.flusher = flusher;
        this.autoFlush = autoFlush;
    }

    private void prepareForAccess(int size) {
        int underflow = (this.cursor.getIndex() + size) - last();
        if (underflow > 0) {
            this.expander.expand(this.cba, underflow);
        }
    }

    public void flush() {
        flushTo(this.cursor.getIndex());
    }

    public void flushTo(int index) {
        ByteArray removed = this.cba.removeTo(index);
        this.flusher.flush(removed);
    }

    @Override // org.apache.mina.util.byteaccess.IoRelativeWriter
    public void skip(int length) {
        this.cursor.skip(length);
    }

    @Override // org.apache.mina.util.byteaccess.CompositeByteArrayRelativeBase
    protected void cursorPassedFirstComponent() {
        if (this.autoFlush) {
            flushTo(this.cba.first() + this.cba.getFirst().length());
        }
    }

    @Override // org.apache.mina.util.byteaccess.IoRelativeWriter
    public void put(byte b) {
        prepareForAccess(1);
        this.cursor.put(b);
    }

    @Override // org.apache.mina.util.byteaccess.IoRelativeWriter
    public void put(IoBuffer bb) {
        prepareForAccess(bb.remaining());
        this.cursor.put(bb);
    }

    @Override // org.apache.mina.util.byteaccess.IoRelativeWriter
    public void putShort(short s) {
        prepareForAccess(2);
        this.cursor.putShort(s);
    }

    @Override // org.apache.mina.util.byteaccess.IoRelativeWriter
    public void putInt(int i) {
        prepareForAccess(4);
        this.cursor.putInt(i);
    }

    @Override // org.apache.mina.util.byteaccess.IoRelativeWriter
    public void putLong(long l) {
        prepareForAccess(8);
        this.cursor.putLong(l);
    }

    @Override // org.apache.mina.util.byteaccess.IoRelativeWriter
    public void putFloat(float f) {
        prepareForAccess(4);
        this.cursor.putFloat(f);
    }

    @Override // org.apache.mina.util.byteaccess.IoRelativeWriter
    public void putDouble(double d) {
        prepareForAccess(8);
        this.cursor.putDouble(d);
    }

    @Override // org.apache.mina.util.byteaccess.IoRelativeWriter
    public void putChar(char c) {
        prepareForAccess(2);
        this.cursor.putChar(c);
    }
}