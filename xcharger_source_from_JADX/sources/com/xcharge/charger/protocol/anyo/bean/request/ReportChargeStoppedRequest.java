package com.xcharge.charger.protocol.anyo.bean.request;

import android.support.p000v4.internal.view.SupportMenu;
import android.support.p000v4.view.MotionEventCompat;
import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.utils.EndianUtils;
import java.nio.charset.Charset;
import java.util.InputMismatchException;
import org.apache.commons.lang3.CharEncoding;

public class ReportChargeStoppedRequest extends AnyoMessage {
    public static final byte CARD_BALANCED_FLAG = 1;
    public static final byte CARD_UNBALANCED_FLAG = 0;
    public static final byte CHARGE_STOP_CAUSE_CAR_CMD = 5;
    public static final byte CHARGE_STOP_CAUSE_CAR_RESPONSE_TIMEOUT = 6;
    public static final byte CHARGE_STOP_CAUSE_DEVICE_ERROR = 4;
    public static final byte CHARGE_STOP_CAUSE_FULL = 3;
    public static final byte CHARGE_STOP_CAUSE_PLUGOUT = 1;
    public static final byte CHARGE_STOP_CAUSE_SERVER_CMD = 0;
    public static final byte CHARGE_STOP_CAUSE_SWIPE_CARD = 2;
    public static final byte USERID_TYPE_CARD_NO = 2;
    public static final byte USERID_TYPE_USER_ID = 1;
    private int billId = 0;
    private short bmsFaultCause = 0;
    private byte bmsStopCause = 0;
    private byte cardBalanceFlag = 0;
    private long chargeFee = 0;
    private byte chargeStopCause = 0;
    private long chargeTime = 0;
    private byte portNo = 0;
    private int power = 0;
    private long startTime = 0;
    private long stopTime = 0;
    private String userId = null;
    private int userIdLength = 0;
    private byte userIdType = 0;

    public int getBillId() {
        return this.billId;
    }

    public void setBillId(int billId2) {
        this.billId = billId2;
    }

    public int getPower() {
        return this.power;
    }

    public void setPower(int power2) {
        this.power = power2;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime2) {
        this.startTime = startTime2;
    }

    public long getStopTime() {
        return this.stopTime;
    }

    public void setStopTime(long stopTime2) {
        this.stopTime = stopTime2;
    }

    public long getChargeTime() {
        return this.chargeTime;
    }

    public void setChargeTime(long chargeTime2) {
        this.chargeTime = chargeTime2;
    }

    public long getChargeFee() {
        return this.chargeFee;
    }

    public void setChargeFee(long chargeFee2) {
        this.chargeFee = chargeFee2;
    }

    public byte getCardBalanceFlag() {
        return this.cardBalanceFlag;
    }

    public void setCardBalanceFlag(byte cardBalanceFlag2) {
        this.cardBalanceFlag = cardBalanceFlag2;
    }

    public byte getBmsStopCause() {
        return this.bmsStopCause;
    }

    public void setBmsStopCause(byte bmsStopCause2) {
        this.bmsStopCause = bmsStopCause2;
    }

    public short getBmsFaultCause() {
        return this.bmsFaultCause;
    }

    public void setBmsFaultCause(short bmsFaultCause2) {
        this.bmsFaultCause = bmsFaultCause2;
    }

    public byte getChargeStopCause() {
        return this.chargeStopCause;
    }

    public void setChargeStopCause(byte chargeStopCause2) {
        this.chargeStopCause = chargeStopCause2;
    }

    public byte getUserIdType() {
        return this.userIdType;
    }

    public void setUserIdType(byte userIdType2) {
        this.userIdType = userIdType2;
    }

    public int getUserIdLength() {
        return this.userIdLength;
    }

    public void setUserIdLength(int userIdLength2) {
        this.userIdLength = userIdLength2;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId2) {
        this.userId = userId2;
    }

    public byte getPortNo() {
        return this.portNo;
    }

    public void setPortNo(byte portNo2) {
        this.portNo = portNo2;
    }

    public byte[] bodyToBytes() throws Exception {
        int portPos;
        if ((this.userIdType != 1 || this.userIdLength == 4) && ((this.userIdType != 2 || this.userIdLength <= 20) && (this.userIdType != 2 || this.userId.length() == this.userIdLength))) {
            byte[] bytes = new byte[(this.userIdLength + 30)];
            System.arraycopy(EndianUtils.intToLittleBytes(this.billId), 0, bytes, 0, 4);
            System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.power & SupportMenu.USER_MASK)), 0, bytes, 4, 2);
            System.arraycopy(EndianUtils.intToLittleBytes((int) (this.startTime & XMSZHead.ID_BROADCAST)), 0, bytes, 6, 4);
            System.arraycopy(EndianUtils.intToLittleBytes((int) (this.stopTime & XMSZHead.ID_BROADCAST)), 0, bytes, 10, 4);
            System.arraycopy(EndianUtils.intToLittleBytes((int) (this.chargeTime & XMSZHead.ID_BROADCAST)), 0, bytes, 14, 4);
            System.arraycopy(EndianUtils.intToLittleBytes((int) (this.chargeFee & XMSZHead.ID_BROADCAST)), 0, bytes, 18, 4);
            bytes[22] = this.cardBalanceFlag;
            bytes[23] = this.bmsStopCause;
            System.arraycopy(EndianUtils.shortToLittleBytes(this.bmsFaultCause), 0, bytes, 24, 2);
            bytes[26] = this.chargeStopCause;
            bytes[27] = this.userIdType;
            bytes[28] = (byte) (this.userIdLength & MotionEventCompat.ACTION_MASK);
            Charset utf8 = Charset.forName(CharEncoding.UTF_8);
            if (this.userIdType == 1) {
                System.arraycopy(EndianUtils.intToLittleBytes(Integer.valueOf(this.userId).intValue()), 0, bytes, 29, 4);
                portPos = 29 + 4;
            } else {
                System.arraycopy(this.userId.getBytes(utf8), 0, bytes, 29, this.userIdLength);
                portPos = 29 + this.userIdLength;
            }
            bytes[portPos] = this.portNo;
            return bytes;
        }
        Log.e("ChargeStoppedRequest.bodyToBytes", "illegal user id info in message: " + toJson());
        throw new InputMismatchException();
    }

    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
