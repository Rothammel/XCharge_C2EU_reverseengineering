package org.apache.mina.filter.codec.serialization;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.StreamCorruptedException;
import org.apache.mina.core.buffer.IoBuffer;

public class ObjectSerializationInputStream extends InputStream implements ObjectInput {
    private final ClassLoader classLoader;

    /* renamed from: in */
    private final DataInputStream f188in;
    private int maxObjectSize;

    public ObjectSerializationInputStream(InputStream in) {
        this(in, (ClassLoader) null);
    }

    public ObjectSerializationInputStream(InputStream in, ClassLoader classLoader2) {
        this.maxObjectSize = 1048576;
        if (in == null) {
            throw new IllegalArgumentException("in");
        }
        classLoader2 = classLoader2 == null ? Thread.currentThread().getContextClassLoader() : classLoader2;
        if (in instanceof DataInputStream) {
            this.f188in = (DataInputStream) in;
        } else {
            this.f188in = new DataInputStream(in);
        }
        this.classLoader = classLoader2;
    }

    public int getMaxObjectSize() {
        return this.maxObjectSize;
    }

    public void setMaxObjectSize(int maxObjectSize2) {
        if (maxObjectSize2 <= 0) {
            throw new IllegalArgumentException("maxObjectSize: " + maxObjectSize2);
        }
        this.maxObjectSize = maxObjectSize2;
    }

    public int read() throws IOException {
        return this.f188in.read();
    }

    public Object readObject() throws ClassNotFoundException, IOException {
        int objectSize = this.f188in.readInt();
        if (objectSize <= 0) {
            throw new StreamCorruptedException("Invalid objectSize: " + objectSize);
        } else if (objectSize > this.maxObjectSize) {
            throw new StreamCorruptedException("ObjectSize too big: " + objectSize + " (expected: <= " + this.maxObjectSize + ')');
        } else {
            IoBuffer buf = IoBuffer.allocate(objectSize + 4, false);
            buf.putInt(objectSize);
            this.f188in.readFully(buf.array(), 4, objectSize);
            buf.position(0);
            buf.limit(objectSize + 4);
            return buf.getObject(this.classLoader);
        }
    }

    public boolean readBoolean() throws IOException {
        return this.f188in.readBoolean();
    }

    public byte readByte() throws IOException {
        return this.f188in.readByte();
    }

    public char readChar() throws IOException {
        return this.f188in.readChar();
    }

    public double readDouble() throws IOException {
        return this.f188in.readDouble();
    }

    public float readFloat() throws IOException {
        return this.f188in.readFloat();
    }

    public void readFully(byte[] b) throws IOException {
        this.f188in.readFully(b);
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        this.f188in.readFully(b, off, len);
    }

    public int readInt() throws IOException {
        return this.f188in.readInt();
    }

    @Deprecated
    public String readLine() throws IOException {
        return this.f188in.readLine();
    }

    public long readLong() throws IOException {
        return this.f188in.readLong();
    }

    public short readShort() throws IOException {
        return this.f188in.readShort();
    }

    public String readUTF() throws IOException {
        return this.f188in.readUTF();
    }

    public int readUnsignedByte() throws IOException {
        return this.f188in.readUnsignedByte();
    }

    public int readUnsignedShort() throws IOException {
        return this.f188in.readUnsignedShort();
    }

    public int skipBytes(int n) throws IOException {
        return this.f188in.skipBytes(n);
    }
}
