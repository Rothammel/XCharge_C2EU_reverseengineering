package com.xcharge.charger.protocol.xmsz.bean.device;

import android.support.v4.internal.view.SupportMenu;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.utils.EndianUtils;

/* loaded from: classes.dex */
public class GetChargeInfoResponse extends XMSZMessage {
    public static final byte RC_FAIL = 0;
    public static final byte RC_OK = 1;
    private byte returnCode = 1;
    private long transactionId = 0;
    private byte battryPercent = 0;
    private int chargeTime = 0;
    private int remainChargeTime = SupportMenu.USER_MASK;
    private short temperature = 0;
    private int currentPower = 0;
    private int currentVoltage = 0;
    private int currentAvailable = 0;
    private int maxVoltage = 0;
    private short maxTemperature = 0;
    private int meterValue = 0;

    public byte getReturnCode() {
        return this.returnCode;
    }

    public void setReturnCode(byte returnCode) {
        this.returnCode = returnCode;
    }

    public long getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public byte getBattryPercent() {
        return this.battryPercent;
    }

    public void setBattryPercent(byte battryPercent) {
        this.battryPercent = battryPercent;
    }

    public int getChargeTime() {
        return this.chargeTime;
    }

    public void setChargeTime(int chargeTime) {
        this.chargeTime = chargeTime;
    }

    public int getRemainChargeTime() {
        return this.remainChargeTime;
    }

    public void setRemainChargeTime(int remainChargeTime) {
        this.remainChargeTime = remainChargeTime;
    }

    public short getTemperature() {
        return this.temperature;
    }

    public void setTemperature(short temperature) {
        this.temperature = temperature;
    }

    public int getCurrentPower() {
        return this.currentPower;
    }

    public void setCurrentPower(int currentPower) {
        this.currentPower = currentPower;
    }

    public int getCurrentVoltage() {
        return this.currentVoltage;
    }

    public void setCurrentVoltage(int currentVoltage) {
        this.currentVoltage = currentVoltage;
    }

    public int getCurrentAvailable() {
        return this.currentAvailable;
    }

    public void setCurrentAvailable(int currentAvailable) {
        this.currentAvailable = currentAvailable;
    }

    public int getMaxVoltage() {
        return this.maxVoltage;
    }

    public void setMaxVoltage(int maxVoltage) {
        this.maxVoltage = maxVoltage;
    }

    public short getMaxTemperature() {
        return this.maxTemperature;
    }

    public void setMaxTemperature(short maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public int getMeterValue() {
        return this.meterValue;
    }

    public void setMeterValue(int meterValue) {
        this.meterValue = meterValue;
    }

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = new byte[24];
        bytes[0] = this.returnCode;
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.transactionId & XMSZHead.ID_BROADCAST)), 0, bytes, 1, 4);
        bytes[5] = this.battryPercent;
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.chargeTime & SupportMenu.USER_MASK)), 0, bytes, 6, 2);
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.remainChargeTime & SupportMenu.USER_MASK)), 0, bytes, 8, 2);
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.temperature & 65535)), 0, bytes, 10, 2);
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.currentPower & SupportMenu.USER_MASK)), 0, bytes, 12, 2);
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.currentVoltage & SupportMenu.USER_MASK)), 0, bytes, 14, 2);
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.currentAvailable & SupportMenu.USER_MASK)), 0, bytes, 16, 2);
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.maxVoltage & SupportMenu.USER_MASK)), 0, bytes, 18, 2);
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.maxTemperature & 65535)), 0, bytes, 20, 2);
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.meterValue & SupportMenu.USER_MASK)), 0, bytes, 22, 2);
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
