package com.xcharge.charger.protocol.anyo.bean;

import android.support.v4.internal.view.SupportMenu;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.EndianUtils;
import java.util.InputMismatchException;

/* loaded from: classes.dex */
public class AnyoHead extends JsonBean<AnyoHead> {
    public static final int MESSAGE_HEAD_LENGTH = 8;
    private byte startCode = 0;
    private byte cmdCode = 0;
    private byte statusCode = 0;
    private byte seq = 0;
    private int bodyLength = 0;
    private byte flags = 0;
    private byte checkSum = 0;

    public byte getStartCode() {
        return this.startCode;
    }

    public void setStartCode(byte startCode) {
        this.startCode = startCode;
    }

    public byte getCmdCode() {
        return this.cmdCode;
    }

    public void setCmdCode(byte cmdCode) {
        this.cmdCode = cmdCode;
    }

    public byte getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(byte statusCode) {
        this.statusCode = statusCode;
    }

    public byte getSeq() {
        return this.seq;
    }

    public void setSeq(byte seq) {
        this.seq = seq;
    }

    public int getBodyLength() {
        return this.bodyLength;
    }

    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }

    public byte getFlags() {
        return this.flags;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }

    public byte getCheckSum() {
        return this.checkSum;
    }

    public void setCheckSum(byte checkSum) {
        this.checkSum = checkSum;
    }

    public byte[] toBytes() {
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.bodyLength & SupportMenu.USER_MASK)), 0, bytes, 4, 2);
        byte[] bytes = {this.startCode, this.cmdCode, this.statusCode, this.seq, 0, 0, this.flags, this.checkSum};
        return bytes;
    }

    public AnyoHead fromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 8) {
            throw new IllegalArgumentException();
        }
        this.startCode = bytes[0];
        this.cmdCode = bytes[1];
        this.statusCode = bytes[2];
        this.seq = bytes[3];
        this.bodyLength = EndianUtils.littleBytesToShort(new byte[]{bytes[4], bytes[5]}) & 65535;
        if (this.bodyLength > 1024) {
            throw new InputMismatchException();
        }
        this.flags = bytes[6];
        this.checkSum = bytes[7];
        return this;
    }
}
