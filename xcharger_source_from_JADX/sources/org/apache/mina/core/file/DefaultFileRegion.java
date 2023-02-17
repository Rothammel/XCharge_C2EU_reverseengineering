package org.apache.mina.core.file;

import java.io.IOException;
import java.nio.channels.FileChannel;

public class DefaultFileRegion implements FileRegion {
    private final FileChannel channel;
    private final long originalPosition;
    private long position;
    private long remainingBytes;

    public DefaultFileRegion(FileChannel channel2) throws IOException {
        this(channel2, 0, channel2.size());
    }

    public DefaultFileRegion(FileChannel channel2, long position2, long remainingBytes2) {
        if (channel2 == null) {
            throw new IllegalArgumentException("channel can not be null");
        } else if (position2 < 0) {
            throw new IllegalArgumentException("position may not be less than 0");
        } else if (remainingBytes2 < 0) {
            throw new IllegalArgumentException("remainingBytes may not be less than 0");
        } else {
            this.channel = channel2;
            this.originalPosition = position2;
            this.position = position2;
            this.remainingBytes = remainingBytes2;
        }
    }

    public long getWrittenBytes() {
        return this.position - this.originalPosition;
    }

    public long getRemainingBytes() {
        return this.remainingBytes;
    }

    public FileChannel getFileChannel() {
        return this.channel;
    }

    public long getPosition() {
        return this.position;
    }

    public void update(long value) {
        this.position += value;
        this.remainingBytes -= value;
    }

    public String getFilename() {
        return null;
    }
}
