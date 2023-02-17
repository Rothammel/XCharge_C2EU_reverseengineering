package org.apache.mina.core.file;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FilenameFileRegion extends DefaultFileRegion {
    private final File file;

    public FilenameFileRegion(File file2, FileChannel channel) throws IOException {
        this(file2, channel, 0, file2.length());
    }

    public FilenameFileRegion(File file2, FileChannel channel, long position, long remainingBytes) {
        super(channel, position, remainingBytes);
        if (file2 == null) {
            throw new IllegalArgumentException("file can not be null");
        }
        this.file = file2;
    }

    public String getFilename() {
        return this.file.getAbsolutePath();
    }
}
