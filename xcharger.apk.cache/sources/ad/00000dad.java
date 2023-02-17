package org.apache.mina.filter.codec.serialization;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;
import org.apache.mina.core.buffer.IoBuffer;

/* loaded from: classes.dex */
public class ObjectSerializationOutputStream extends OutputStream implements ObjectOutput {
    private int maxObjectSize = Integer.MAX_VALUE;
    private final DataOutputStream out;

    public ObjectSerializationOutputStream(OutputStream out) {
        if (out == null) {
            throw new IllegalArgumentException("out");
        }
        if (out instanceof DataOutputStream) {
            this.out = (DataOutputStream) out;
        } else {
            this.out = new DataOutputStream(out);
        }
    }

    public int getMaxObjectSize() {
        return this.maxObjectSize;
    }

    public void setMaxObjectSize(int maxObjectSize) {
        if (maxObjectSize <= 0) {
            throw new IllegalArgumentException("maxObjectSize: " + maxObjectSize);
        }
        this.maxObjectSize = maxObjectSize;
    }

    @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable, java.io.ObjectOutput
    public void close() throws IOException {
        this.out.close();
    }

    @Override // java.io.OutputStream, java.io.Flushable, java.io.ObjectOutput
    public void flush() throws IOException {
        this.out.flush();
    }

    @Override // java.io.OutputStream, java.io.ObjectOutput, java.io.DataOutput
    public void write(int b) throws IOException {
        this.out.write(b);
    }

    @Override // java.io.OutputStream, java.io.ObjectOutput, java.io.DataOutput
    public void write(byte[] b) throws IOException {
        this.out.write(b);
    }

    @Override // java.io.OutputStream, java.io.ObjectOutput, java.io.DataOutput
    public void write(byte[] b, int off, int len) throws IOException {
        this.out.write(b, off, len);
    }

    @Override // java.io.ObjectOutput
    public void writeObject(Object obj) throws IOException {
        IoBuffer buf = IoBuffer.allocate(64, false);
        buf.setAutoExpand(true);
        buf.putObject(obj);
        int objectSize = buf.position() - 4;
        if (objectSize > this.maxObjectSize) {
            throw new IllegalArgumentException("The encoded object is too big: " + objectSize + " (> " + this.maxObjectSize + ')');
        }
        this.out.write(buf.array(), 0, buf.position());
    }

    @Override // java.io.DataOutput
    public void writeBoolean(boolean v) throws IOException {
        this.out.writeBoolean(v);
    }

    @Override // java.io.DataOutput
    public void writeByte(int v) throws IOException {
        this.out.writeByte(v);
    }

    @Override // java.io.DataOutput
    public void writeBytes(String s) throws IOException {
        this.out.writeBytes(s);
    }

    @Override // java.io.DataOutput
    public void writeChar(int v) throws IOException {
        this.out.writeChar(v);
    }

    @Override // java.io.DataOutput
    public void writeChars(String s) throws IOException {
        this.out.writeChars(s);
    }

    @Override // java.io.DataOutput
    public void writeDouble(double v) throws IOException {
        this.out.writeDouble(v);
    }

    @Override // java.io.DataOutput
    public void writeFloat(float v) throws IOException {
        this.out.writeFloat(v);
    }

    @Override // java.io.DataOutput
    public void writeInt(int v) throws IOException {
        this.out.writeInt(v);
    }

    @Override // java.io.DataOutput
    public void writeLong(long v) throws IOException {
        this.out.writeLong(v);
    }

    @Override // java.io.DataOutput
    public void writeShort(int v) throws IOException {
        this.out.writeShort(v);
    }

    @Override // java.io.DataOutput
    public void writeUTF(String str) throws IOException {
        this.out.writeUTF(str);
    }
}