package com.xcharge.charger.protocol.xmsz.bean;

import android.support.p000v4.internal.view.SupportMenu;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.EndianUtils;

public class XMSZHead extends JsonBean<XMSZHead> {
    public static final long ID_BROADCAST = 4294967295L;
    public static final long ID_CLOUD = 0;
    public static final int MESSAGE_HEAD_LENGTH = 14;
    private long dest = 0;
    private byte functionCode = 0;
    private int packetHeader = 18247;
    private byte packetID = 0;
    private int packetLength = 0;
    private long source = 0;

    public int getPacketHeader() {
        return this.packetHeader;
    }

    public void setPacketHeader(int packetHeader2) {
        this.packetHeader = packetHeader2;
    }

    public int getPacketLength() {
        return this.packetLength;
    }

    public void setPacketLength(int packetLength2) {
        this.packetLength = packetLength2;
    }

    public byte getPacketID() {
        return this.packetID;
    }

    public void setPacketID(byte packetID2) {
        this.packetID = packetID2;
    }

    public long getSource() {
        return this.source;
    }

    public void setSource(long source2) {
        this.source = source2;
    }

    public long getDest() {
        return this.dest;
    }

    public void setDest(long dest2) {
        this.dest = dest2;
    }

    public byte getFunctionCode() {
        return this.functionCode;
    }

    public void setFunctionCode(byte functionCode2) {
        this.functionCode = functionCode2;
    }

    public byte[] toBytes() {
        byte[] bytes = new byte[14];
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.packetHeader & SupportMenu.USER_MASK)), 0, bytes, 0, 2);
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.packetLength & SupportMenu.USER_MASK)), 0, bytes, 2, 2);
        bytes[4] = this.packetID;
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.source & ID_BROADCAST)), 0, bytes, 5, 4);
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.dest & ID_BROADCAST)), 0, bytes, 9, 4);
        bytes[13] = this.functionCode;
        return bytes;
    }

    public XMSZHead fromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 14) {
            throw new IllegalArgumentException();
        }
        this.packetHeader = EndianUtils.littleBytesToShort(new byte[]{bytes[0], bytes[1]}) & 65535;
        this.packetLength = EndianUtils.littleBytesToShort(new byte[]{bytes[2], bytes[3]}) & 65535;
        this.packetID = bytes[4];
        this.source = ((long) EndianUtils.littleBytesToInt(new byte[]{bytes[5], bytes[6], bytes[7], bytes[8]})) & ID_BROADCAST;
        this.dest = ((long) EndianUtils.littleBytesToInt(new byte[]{bytes[9], bytes[10], bytes[11], bytes[12]})) & ID_BROADCAST;
        this.functionCode = bytes[13];
        return this;
    }
}
