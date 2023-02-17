package com.xcharge.charger.protocol.xmsz.bean;

import android.support.p000v4.internal.view.SupportMenu;
import android.util.Log;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.CRCUtils;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;
import java.nio.charset.Charset;

public abstract class XMSZMessage extends JsonBean<XMSZMessage> {
    public static final byte AuthorizeIDRequest = 18;
    public static final byte AuthorizeIDResponse = -110;
    public static final byte BootNotificationRequest = 17;
    public static final byte BootNotificationResponse = -111;
    public static final Charset CHARSET_GBK = Charset.forName(GBK_NAME);
    public static final byte ChangeAvailabilityRequest = 10;
    public static final byte ChangeAvailabilityResponse = -118;
    public static final String DEFAULT_STR = "";
    public static final byte FirmwareUpdateInformRequest = 23;
    public static final byte FirmwareUpdateInformResponse = -105;
    public static final String GBK_NAME = "GBK";
    public static final byte GetChargeInfoRequest = 12;
    public static final byte GetChargeInfoResponse = -116;
    public static final byte HeartBeatRequest = 21;
    public static final byte HeartBeatResponse = -107;
    public static final byte RemoteStartChargingRequest = 2;
    public static final byte RemoteStartChargingResponse = -126;
    public static final byte RemoteStopChargingRequest = 3;
    public static final byte RemoteStopChargingResponse = -125;
    public static final byte ResetRequest = 8;
    public static final byte ResetResponse = -120;
    public static final byte StartTransactionRequest = 19;
    public static final byte StartTransactionResponse = -109;
    public static final byte StatusNotificationRequest = 22;
    public static final byte StatusNotificationResponse = -106;
    public static final byte StopTransactionRequest = 20;
    public static final byte StopTransactionResponse = -108;
    public static final byte UpdateFirmwareRequest = 9;
    public static final byte UpdateFirmwareResponse = -119;
    private int crc16 = 0;
    private XMSZHead head = null;
    private boolean isCheckOk = false;
    private String port = null;
    private int retrySend = 0;

    public abstract XMSZMessage bodyFromBytes(byte[] bArr) throws Exception;

    public abstract byte[] bodyToBytes() throws Exception;

    public XMSZHead getHead() {
        return this.head;
    }

    public void setHead(XMSZHead head2) {
        this.head = head2;
    }

    public int getCrc16() {
        return this.crc16;
    }

    public void setCrc16(int crc162) {
        this.crc16 = crc162;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port2) {
        this.port = port2;
    }

    public int getRetrySend() {
        return this.retrySend;
    }

    public void setRetrySend(int retrySend2) {
        this.retrySend = retrySend2;
    }

    public boolean isCheckOk() {
        return this.isCheckOk;
    }

    public void setCheckOk(boolean isCheckOk2) {
        this.isCheckOk = isCheckOk2;
    }

    public byte[] toBytes() throws Exception {
        byte[] head2 = this.head.toBytes();
        byte[] body = bodyToBytes();
        byte[] bytes = new byte[(body.length + 14 + 2)];
        System.arraycopy(head2, 0, bytes, 0, head2.length);
        System.arraycopy(body, 0, bytes, head2.length, body.length);
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.crc16 & SupportMenu.USER_MASK)), 0, bytes, head2.length + body.length, 2);
        return bytes;
    }

    public int calcCheckSum() throws Exception {
        byte[] head2 = this.head.toBytes();
        byte[] body = bodyToBytes();
        byte[] bytes = new byte[(body.length + 14)];
        System.arraycopy(head2, 0, bytes, 0, head2.length);
        System.arraycopy(body, 0, bytes, head2.length, body.length);
        int checkSum = CRCUtils.calcCrc16(bytes);
        Log.d("XMSZMessage.calcCheckSum", "bytes: " + FormatUtils.bytesToHexString(bytes) + ", crc16: " + checkSum);
        return checkSum;
    }

    public boolean verifyCheckSum() {
        try {
            return calcCheckSum() == this.crc16;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
