package com.xcharge.charger.protocol.anyo.bean.request;

import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;

/* loaded from: classes.dex */
public class StartChargeRequest extends AnyoMessage {
    public static final byte CHARGE_POLICY_FEE = 2;
    public static final byte CHARGE_POLICY_POWER = 1;
    public static final byte CHARGE_POLICY_TIME = 3;
    public static final byte CHARGE_POLICY_UNLIMITED = 0;
    private int userId = 0;
    private byte chargePolicyKey = 0;
    private long chargePolicyValue = 0;
    private byte chargeMode = 0;
    private int pwm = 0;
    private byte portNo = 0;

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public byte getChargePolicyKey() {
        return this.chargePolicyKey;
    }

    public void setChargePolicyKey(byte chargePolicyKey) {
        this.chargePolicyKey = chargePolicyKey;
    }

    public long getChargePolicyValue() {
        return this.chargePolicyValue;
    }

    public void setChargePolicyValue(long chargePolicyValue) {
        this.chargePolicyValue = chargePolicyValue;
    }

    public byte getChargeMode() {
        return this.chargeMode;
    }

    public void setChargeMode(byte chargeMode) {
        this.chargeMode = chargeMode;
    }

    public int getPwm() {
        return this.pwm;
    }

    public void setPwm(int pwm) {
        this.pwm = pwm;
    }

    public byte getPortNo() {
        return this.portNo;
    }

    public void setPortNo(byte portNo) {
        this.portNo = portNo;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = new byte[12];
        System.arraycopy(EndianUtils.intToLittleBytes(this.userId), 0, bytes, 0, 4);
        bytes[4] = this.chargePolicyKey;
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.chargePolicyValue & XMSZHead.ID_BROADCAST)), 0, bytes, 5, 4);
        bytes[9] = this.chargeMode;
        bytes[10] = (byte) (this.pwm & MotionEventCompat.ACTION_MASK);
        bytes[11] = this.portNo;
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 12) {
            Log.e("StartChargeRequest.bodyFromBytes", "body length must be 12 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        this.userId = EndianUtils.littleBytesToInt(new byte[]{bytes[0], bytes[1], bytes[2], bytes[3]});
        this.chargePolicyKey = bytes[4];
        this.chargePolicyValue = EndianUtils.littleBytesToInt(new byte[]{bytes[5], bytes[6], bytes[7], bytes[8]}) & XMSZHead.ID_BROADCAST;
        this.chargeMode = bytes[9];
        this.pwm = bytes[10] & 255;
        this.portNo = bytes[11];
        return this;
    }
}