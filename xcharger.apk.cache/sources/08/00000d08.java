package org.apache.mina.core.file;

import java.io.IOException;
import java.nio.channels.FileChannel;

/* loaded from: classes.dex */
public class DefaultFileRegion implements FileRegion {
    private final FileChannel channel;
    private final long originalPosition;
    private long position;
    private long remainingBytes;

    public DefaultFileRegion(FileChannel channel) throws IOException {
        this(channel, 0L, channel.size());
    }

    public DefaultFileRegion(FileChannel channel, long position, long remainingBytes) {
        if (channel == null) {
            throw new IllegalArgumentException("channel can not be null");
        }
        if (position < 0) {
            throw new IllegalArgumentException("position may not be less than 0");
        }
        if (remainingBytes < 0) {
            throw new IllegalArgumentException("remainingBytes may not be less than 0");
        }
        this.channel = channel;
        this.originalPosition = position;
        this.position = position;
        this.remainingBytes = remainingBytes;
    }

    @Override // org.apache.mina.core.file.FileRegion
    public long getWrittenBytes() {
        return this.position - this.originalPosition;
    }

    @Override // org.apache.mina.core.file.FileRegion
    public long getRemainingBytes() {
        return this.remainingBytes;
    }

    @Override // org.apache.mina.core.file.FileRegion
    public FileChannel getFileChannel() {
        return this.channel;
    }

    @Override // org.apache.mina.core.file.FileRegion
    public long getPosition() {
        return this.position;
    }

    @Override // org.apache.mina.core.file.FileRegion
    public void update(long value) {
        this.position += value;
        this.remainingBytes -= value;
    }

    @Override // org.apache.mina.core.file.FileRegion
    public String getFilename() {
        return null;
    }
}