package org.apache.mina.filter.errorgenerating;

import java.util.Random;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorGeneratingFilter extends IoFilterAdapter {
    private int changeByteProbability = 0;
    private int duplicatePduProbability = 0;
    private int insertByteProbability = 0;
    private final Logger logger = LoggerFactory.getLogger((Class<?>) ErrorGeneratingFilter.class);
    private boolean manipulateReads = false;
    private boolean manipulateWrites = false;
    private int maxInsertByte = 10;
    private int removeByteProbability = 0;
    private int removePduProbability = 0;
    private int resendPduLasterProbability = 0;
    private Random rng = new Random();

    public void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        if (this.manipulateWrites) {
            if (writeRequest.getMessage() instanceof IoBuffer) {
                manipulateIoBuffer(session, (IoBuffer) writeRequest.getMessage());
                IoBuffer buffer = insertBytesToNewIoBuffer(session, (IoBuffer) writeRequest.getMessage());
                if (buffer != null) {
                    writeRequest = new DefaultWriteRequest(buffer, writeRequest.getFuture(), writeRequest.getDestination());
                }
            } else {
                if (this.duplicatePduProbability > this.rng.nextInt()) {
                    nextFilter.filterWrite(session, writeRequest);
                }
                if (this.resendPduLasterProbability > this.rng.nextInt()) {
                }
                if (this.removePduProbability > this.rng.nextInt()) {
                    return;
                }
            }
        }
        nextFilter.filterWrite(session, writeRequest);
    }

    public void messageReceived(IoFilter.NextFilter nextFilter, IoSession session, Object message) throws Exception {
        if (this.manipulateReads && (message instanceof IoBuffer)) {
            manipulateIoBuffer(session, (IoBuffer) message);
            IoBuffer buffer = insertBytesToNewIoBuffer(session, (IoBuffer) message);
            if (buffer != null) {
                message = buffer;
            }
        }
        nextFilter.messageReceived(session, message);
    }

    private IoBuffer insertBytesToNewIoBuffer(IoSession session, IoBuffer buffer) {
        if (this.insertByteProbability <= this.rng.nextInt(1000)) {
            return null;
        }
        this.logger.info(buffer.getHexDump());
        int pos = this.rng.nextInt(buffer.remaining()) - 1;
        int count = this.rng.nextInt(this.maxInsertByte - 1) + 1;
        IoBuffer newBuff = IoBuffer.allocate(buffer.remaining() + count);
        for (int i = 0; i < pos; i++) {
            newBuff.put(buffer.get());
        }
        for (int i2 = 0; i2 < count; i2++) {
            newBuff.put((byte) this.rng.nextInt(256));
        }
        while (buffer.remaining() > 0) {
            newBuff.put(buffer.get());
        }
        newBuff.flip();
        this.logger.info("Inserted " + count + " bytes.");
        this.logger.info(newBuff.getHexDump());
        return newBuff;
    }

    private void manipulateIoBuffer(IoSession session, IoBuffer buffer) {
        if (buffer.remaining() > 0 && this.removeByteProbability > this.rng.nextInt(1000)) {
            this.logger.info(buffer.getHexDump());
            int pos = this.rng.nextInt(buffer.remaining());
            int count = this.rng.nextInt(buffer.remaining() - pos) + 1;
            if (count == buffer.remaining()) {
                count = buffer.remaining() - 1;
            }
            IoBuffer newBuff = IoBuffer.allocate(buffer.remaining() - count);
            for (int i = 0; i < pos; i++) {
                newBuff.put(buffer.get());
            }
            buffer.skip(count);
            while (newBuff.remaining() > 0) {
                newBuff.put(buffer.get());
            }
            newBuff.flip();
            buffer.rewind();
            buffer.put(newBuff);
            buffer.flip();
            this.logger.info("Removed " + count + " bytes at position " + pos + ".");
            this.logger.info(buffer.getHexDump());
        }
        if (buffer.remaining() > 0 && this.changeByteProbability > this.rng.nextInt(1000)) {
            this.logger.info(buffer.getHexDump());
            int count2 = this.rng.nextInt(buffer.remaining() - 1) + 1;
            byte[] values = new byte[count2];
            this.rng.nextBytes(values);
            for (byte put : values) {
                buffer.put(this.rng.nextInt(buffer.remaining()), put);
            }
            this.logger.info("Modified " + count2 + " bytes.");
            this.logger.info(buffer.getHexDump());
        }
    }

    public int getChangeByteProbability() {
        return this.changeByteProbability;
    }

    public void setChangeByteProbability(int changeByteProbability2) {
        this.changeByteProbability = changeByteProbability2;
    }

    public int getDuplicatePduProbability() {
        return this.duplicatePduProbability;
    }

    public void setDuplicatePduProbability(int duplicatePduProbability2) {
        this.duplicatePduProbability = duplicatePduProbability2;
    }

    public int getInsertByteProbability() {
        return this.insertByteProbability;
    }

    public void setInsertByteProbability(int insertByteProbability2) {
        this.insertByteProbability = insertByteProbability2;
    }

    public boolean isManipulateReads() {
        return this.manipulateReads;
    }

    public void setManipulateReads(boolean manipulateReads2) {
        this.manipulateReads = manipulateReads2;
    }

    public boolean isManipulateWrites() {
        return this.manipulateWrites;
    }

    public void setManipulateWrites(boolean manipulateWrites2) {
        this.manipulateWrites = manipulateWrites2;
    }

    public int getRemoveByteProbability() {
        return this.removeByteProbability;
    }

    public void setRemoveByteProbability(int removeByteProbability2) {
        this.removeByteProbability = removeByteProbability2;
    }

    public int getRemovePduProbability() {
        return this.removePduProbability;
    }

    public void setRemovePduProbability(int removePduProbability2) {
        this.removePduProbability = removePduProbability2;
    }

    public int getResendPduLasterProbability() {
        return this.resendPduLasterProbability;
    }

    public void setResendPduLasterProbability(int resendPduLasterProbability2) {
        this.resendPduLasterProbability = resendPduLasterProbability2;
    }

    public int getMaxInsertByte() {
        return this.maxInsertByte;
    }

    public void setMaxInsertByte(int maxInsertByte2) {
        this.maxInsertByte = maxInsertByte2;
    }
}
