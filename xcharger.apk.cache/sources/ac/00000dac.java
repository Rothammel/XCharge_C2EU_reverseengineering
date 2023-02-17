package org.apache.mina.filter.codec.serialization;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.StreamCorruptedException;
import org.apache.mina.core.buffer.IoBuffer;

/* loaded from: classes.dex */
public class ObjectSerializationInputStream extends InputStream implements ObjectInput {
    private final ClassLoader classLoader;
    private final DataInputStream in;
    private int maxObjectSize;

    public ObjectSerializationInputStream(InputStream in) {
        this(in, null);
    }

    public ObjectSerializationInputStream(InputStream in, ClassLoader classLoader) {
        this.maxObjectSize = 1048576;
        if (in == null) {
            throw new IllegalArgumentException("in");
        }
        classLoader = classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;
        if (in instanceof DataInputStream) {
            this.in = (DataInputStream) in;
        } else {
            this.in = new DataInputStream(in);
        }
        this.classLoader = classLoader;
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

    @Override // java.io.InputStream, java.io.ObjectInput
    public int read() throws IOException {
        return this.in.read();
    }

    @Override // java.io.ObjectInput
    public Object readObject() throws ClassNotFoundException, IOException {
        int objectSize = this.in.readInt();
        if (objectSize <= 0) {
            throw new StreamCorruptedException("Invalid objectSize: " + objectSize);
        }
        if (objectSize > this.maxObjectSize) {
            throw new StreamCorruptedException("ObjectSize too big: " + objectSize + " (expected: <= " + this.maxObjectSize + ')');
        }
        IoBuffer buf = IoBuffer.allocate(objectSize + 4, false);
        buf.putInt(objectSize);
        this.in.readFully(buf.array(), 4, objectSize);
        buf.position(0);
        buf.limit(objectSize + 4);
        return buf.getObject(this.classLoader);
    }

    @Override // java.io.DataInput
    public boolean readBoolean() throws IOException {
        return this.in.readBoolean();
    }

    @Override // java.io.DataInput
    public byte readByte() throws IOException {
        return this.in.readByte();
    }

    @Override // java.io.DataInput
    public char readChar() throws IOException {
        return this.in.readChar();
    }

    @Override // java.io.DataInput
    public double readDouble() throws IOException {
        return this.in.readDouble();
    }

    @Override // java.io.DataInput
    public float readFloat() throws IOException {
        return this.in.readFloat();
    }

    @Override // java.io.DataInput
    public void readFully(byte[] b) throws IOException {
        this.in.readFully(b);
    }

    @Override // java.io.DataInput
    public void readFully(byte[] b, int off, int len) throws IOException {
        this.in.readFully(b, off, len);
    }

    @Override // java.io.DataInput
    public int readInt() throws IOException {
        return this.in.readInt();
    }

    @Override // java.io.DataInput
    @Deprecated
    public String readLine() throws IOException {
        return this.in.readLine();
    }

    @Override // java.io.DataInput
    public long readLong() throws IOException {
        return this.in.readLong();
    }

    @Override // java.io.DataInput
    public short readShort() throws IOException {
        return this.in.readShort();
    }

    @Override // java.io.DataInput
    public String readUTF() throws IOException {
        return this.in.readUTF();
    }

    @Override // java.io.DataInput
    public int readUnsignedByte() throws IOException {
        return this.in.readUnsignedByte();
    }

    @Override // java.io.DataInput
    public int readUnsignedShort() throws IOException {
        return this.in.readUnsignedShort();
    }

    @Override // java.io.DataInput
    public int skipBytes(int n) throws IOException {
        return this.in.skipBytes(n);
    }
}