package com.xcharge.charger.protocol.anyo.bean.request;

import android.support.v4.internal.view.SupportMenu;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.utils.EndianUtils;

/* loaded from: classes.dex */
public class ReportChargeRequest extends AnyoMessage {
    private int power = 0;
    private int volt = 0;
    private int amp = 0;
    private long chargeTime = 0;
    private byte portNo = 0;

    public int getPower() {
        return this.power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getVolt() {
        return this.volt;
    }

    public void setVolt(int volt) {
        this.volt = volt;
    }

    public int getAmp() {
        return this.amp;
    }

    public void setAmp(int amp) {
        this.amp = amp;
    }

    public long getChargeTime() {
        return this.chargeTime;
    }

    public void setChargeTime(long chargeTime) {
        this.chargeTime = chargeTime;
    }

    public byte getPortNo() {
        return this.portNo;
    }

    public void setPortNo(byte portNo) {
        this.portNo = portNo;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = new byte[11];
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.power & SupportMenu.USER_MASK)), 0, bytes, 0, 2);
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.volt & SupportMenu.USER_MASK)), 0, bytes, 2, 2);
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.amp & SupportMenu.USER_MASK)), 0, bytes, 4, 2);
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.chargeTime & XMSZHead.ID_BROADCAST)), 0, bytes, 6, 4);
        bytes[10] = this.portNo;
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
