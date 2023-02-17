package org.java_websocket.framing;

import java.nio.ByteBuffer;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.util.ByteBufferUtils;

public abstract class FramedataImpl1 implements Framedata {
    private boolean fin = true;
    private Framedata.Opcode optcode;
    private boolean rsv1 = false;
    private boolean rsv2 = false;
    private boolean rsv3 = false;
    private boolean transferemasked = false;
    private ByteBuffer unmaskedpayload = ByteBufferUtils.getEmptyByteBuffer();

    public abstract void isValid() throws InvalidDataException;

    public FramedataImpl1(Framedata.Opcode op) {
        this.optcode = op;
    }

    public boolean isRSV1() {
        return this.rsv1;
    }

    public boolean isRSV2() {
        return this.rsv2;
    }

    public boolean isRSV3() {
        return this.rsv3;
    }

    public boolean isFin() {
        return this.fin;
    }

    public Framedata.Opcode getOpcode() {
        return this.optcode;
    }

    public boolean getTransfereMasked() {
        return this.transferemasked;
    }

    public ByteBuffer getPayloadData() {
        return this.unmaskedpayload;
    }

    public void append(Framedata nextframe) {
        ByteBuffer b = nextframe.getPayloadData();
        if (this.unmaskedpayload == null) {
            this.unmaskedpayload = ByteBuffer.allocate(b.remaining());
            b.mark();
            this.unmaskedpayload.put(b);
            b.reset();
        } else {
            b.mark();
            this.unmaskedpayload.position(this.unmaskedpayload.limit());
            this.unmaskedpayload.limit(this.unmaskedpayload.capacity());
            if (b.remaining() > this.unmaskedpayload.remaining()) {
                ByteBuffer tmp = ByteBuffer.allocate(b.remaining() + this.unmaskedpayload.capacity());
                this.unmaskedpayload.flip();
                tmp.put(this.unmaskedpayload);
                tmp.put(b);
                this.unmaskedpayload = tmp;
            } else {
                this.unmaskedpayload.put(b);
            }
            this.unmaskedpayload.rewind();
            b.reset();
        }
        this.fin = nextframe.isFin();
    }

    public String toString() {
        return "Framedata{ optcode:" + getOpcode() + ", fin:" + isFin() + ", rsv1:" + isRSV1() + ", rsv2:" + isRSV2() + ", rsv3:" + isRSV3() + ", payloadlength:[pos:" + this.unmaskedpayload.position() + ", len:" + this.unmaskedpayload.remaining() + "], payload:" + (this.unmaskedpayload.remaining() > 1000 ? "(too big to display)" : new String(this.unmaskedpayload.array())) + '}';
    }

    public void setPayload(ByteBuffer payload) {
        this.unmaskedpayload = payload;
    }

    public void setFin(boolean fin2) {
        this.fin = fin2;
    }

    public void setRSV1(boolean rsv12) {
        this.rsv1 = rsv12;
    }

    public void setRSV2(boolean rsv22) {
        this.rsv2 = rsv22;
    }

    public void setRSV3(boolean rsv32) {
        this.rsv3 = rsv32;
    }

    public void setTransferemasked(boolean transferemasked2) {
        this.transferemasked = transferemasked2;
    }

    public static FramedataImpl1 get(Framedata.Opcode opcode) {
        if (opcode == null) {
            throw new IllegalArgumentException("Supplied opcode cannot be null");
        }
        switch (opcode) {
            case PING:
                return new PingFrame();
            case PONG:
                return new PongFrame();
            case TEXT:
                return new TextFrame();
            case BINARY:
                return new BinaryFrame();
            case CLOSING:
                return new CloseFrame();
            case CONTINUOUS:
                return new ContinuousFrame();
            default:
                throw new IllegalArgumentException("Supplied opcode is invalid");
        }
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FramedataImpl1 that = (FramedataImpl1) o;
        if (this.fin != that.fin || this.transferemasked != that.transferemasked || this.rsv1 != that.rsv1 || this.rsv2 != that.rsv2 || this.rsv3 != that.rsv3 || this.optcode != that.optcode) {
            return false;
        }
        if (this.unmaskedpayload != null) {
            z = this.unmaskedpayload.equals(that.unmaskedpayload);
        } else if (that.unmaskedpayload != null) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int result;
        int i;
        int i2;
        int i3;
        int i4;
        int i5 = 1;
        if (this.fin) {
            result = 1;
        } else {
            result = 0;
        }
        int hashCode = ((result * 31) + this.optcode.hashCode()) * 31;
        if (this.unmaskedpayload != null) {
            i = this.unmaskedpayload.hashCode();
        } else {
            i = 0;
        }
        int i6 = (hashCode + i) * 31;
        if (this.transferemasked) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        int i7 = (i6 + i2) * 31;
        if (this.rsv1) {
            i3 = 1;
        } else {
            i3 = 0;
        }
        int i8 = (i7 + i3) * 31;
        if (this.rsv2) {
            i4 = 1;
        } else {
            i4 = 0;
        }
        int i9 = (i8 + i4) * 31;
        if (!this.rsv3) {
            i5 = 0;
        }
        return i9 + i5;
    }
}
