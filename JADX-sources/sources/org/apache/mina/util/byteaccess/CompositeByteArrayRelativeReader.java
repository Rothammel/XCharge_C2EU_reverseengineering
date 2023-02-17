package org.apache.mina.util.byteaccess;

import java.nio.ByteOrder;
import org.apache.mina.core.buffer.IoBuffer;

/* loaded from: classes.dex */
public class CompositeByteArrayRelativeReader extends CompositeByteArrayRelativeBase implements IoRelativeReader {
    private final boolean autoFree;

    @Override // org.apache.mina.util.byteaccess.CompositeByteArrayRelativeBase, org.apache.mina.util.byteaccess.IoRelativeReader, org.apache.mina.util.byteaccess.IoRelativeWriter
    public /* bridge */ /* synthetic */ ByteOrder order() {
        return super.order();
    }

    public CompositeByteArrayRelativeReader(CompositeByteArray cba, boolean autoFree) {
        super(cba);
        this.autoFree = autoFree;
    }

    @Override // org.apache.mina.util.byteaccess.CompositeByteArrayRelativeBase
    protected void cursorPassedFirstComponent() {
        if (this.autoFree) {
            this.cba.removeFirst().free();
        }
    }

    @Override // org.apache.mina.util.byteaccess.IoRelativeReader, org.apache.mina.util.byteaccess.IoRelativeWriter
    public void skip(int length) {
        this.cursor.skip(length);
    }

    @Override // org.apache.mina.util.byteaccess.IoRelativeReader
    public ByteArray slice(int length) {
        return this.cursor.slice(length);
    }

    @Override // org.apache.mina.util.byteaccess.IoRelativeReader
    public byte get() {
        return this.cursor.get();
    }

    @Override // org.apache.mina.util.byteaccess.IoRelativeReader
    public void get(IoBuffer bb) {
        this.cursor.get(bb);
    }

    @Override // org.apache.mina.util.byteaccess.IoRelativeReader
    public short getShort() {
        return this.cursor.getShort();
    }

    @Override // org.apache.mina.util.byteaccess.IoRelativeReader
    public int getInt() {
        return this.cursor.getInt();
    }

    @Override // org.apache.mina.util.byteaccess.IoRelativeReader
    public long getLong() {
        return this.cursor.getLong();
    }

    @Override // org.apache.mina.util.byteaccess.IoRelativeReader
    public float getFloat() {
        return this.cursor.getFloat();
    }

    @Override // org.apache.mina.util.byteaccess.IoRelativeReader
    public double getDouble() {
        return this.cursor.getDouble();
    }

    @Override // org.apache.mina.util.byteaccess.IoRelativeReader
    public char getChar() {
        return this.cursor.getChar();
    }
}
