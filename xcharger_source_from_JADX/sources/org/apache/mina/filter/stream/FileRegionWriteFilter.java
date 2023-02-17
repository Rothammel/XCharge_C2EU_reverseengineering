package org.apache.mina.filter.stream;

import java.io.IOException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.file.FileRegion;

public class FileRegionWriteFilter extends AbstractStreamWriteFilter<FileRegion> {
    /* access modifiers changed from: protected */
    public Class<FileRegion> getMessageClass() {
        return FileRegion.class;
    }

    /* access modifiers changed from: protected */
    public IoBuffer getNextBuffer(FileRegion fileRegion) throws IOException {
        if (fileRegion.getRemainingBytes() <= 0) {
            return null;
        }
        IoBuffer buffer = IoBuffer.allocate((int) Math.min((long) getWriteBufferSize(), fileRegion.getRemainingBytes()));
        fileRegion.update((long) fileRegion.getFileChannel().read(buffer.buf(), fileRegion.getPosition()));
        buffer.flip();
        return buffer;
    }
}
