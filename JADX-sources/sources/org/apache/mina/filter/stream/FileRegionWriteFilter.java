package org.apache.mina.filter.stream;

import java.io.IOException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.file.FileRegion;

/* loaded from: classes.dex */
public class FileRegionWriteFilter extends AbstractStreamWriteFilter<FileRegion> {
    @Override // org.apache.mina.filter.stream.AbstractStreamWriteFilter
    protected Class<FileRegion> getMessageClass() {
        return FileRegion.class;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.mina.filter.stream.AbstractStreamWriteFilter
    public IoBuffer getNextBuffer(FileRegion fileRegion) throws IOException {
        if (fileRegion.getRemainingBytes() <= 0) {
            return null;
        }
        int bufferSize = (int) Math.min(getWriteBufferSize(), fileRegion.getRemainingBytes());
        IoBuffer buffer = IoBuffer.allocate(bufferSize);
        int bytesRead = fileRegion.getFileChannel().read(buffer.buf(), fileRegion.getPosition());
        fileRegion.update(bytesRead);
        buffer.flip();
        return buffer;
    }
}
