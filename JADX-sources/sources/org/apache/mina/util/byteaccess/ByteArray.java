package org.apache.mina.util.byteaccess;

import java.nio.ByteOrder;
import org.apache.mina.core.buffer.IoBuffer;

/* loaded from: classes.dex */
public interface ByteArray extends IoAbsoluteReader, IoAbsoluteWriter {

    /* loaded from: classes.dex */
    public interface Cursor extends IoRelativeReader, IoRelativeWriter {
        @Override // org.apache.mina.util.byteaccess.IoRelativeReader
        byte get();

        @Override // org.apache.mina.util.byteaccess.IoRelativeReader
        void get(IoBuffer ioBuffer);

        int getIndex();

        @Override // org.apache.mina.util.byteaccess.IoRelativeReader
        int getInt();

        @Override // org.apache.mina.util.byteaccess.IoRelativeReader, org.apache.mina.util.byteaccess.IoRelativeWriter
        int getRemaining();

        @Override // org.apache.mina.util.byteaccess.IoRelativeReader, org.apache.mina.util.byteaccess.IoRelativeWriter
        boolean hasRemaining();

        void setIndex(int i);
    }

    Cursor cursor();

    Cursor cursor(int i);

    boolean equals(Object obj);

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteReader, org.apache.mina.util.byteaccess.IoAbsoluteWriter
    int first();

    void free();

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteReader
    byte get(int i);

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteReader
    void get(int i, IoBuffer ioBuffer);

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteReader
    int getInt(int i);

    Iterable<IoBuffer> getIoBuffers();

    IoBuffer getSingleIoBuffer();

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteReader, org.apache.mina.util.byteaccess.IoAbsoluteWriter
    int last();

    @Override // org.apache.mina.util.byteaccess.IoAbsoluteReader, org.apache.mina.util.byteaccess.IoAbsoluteWriter
    ByteOrder order();

    void order(ByteOrder byteOrder);
}
