package com.xcharge.charger.protocol.xmsz.bean.cloud;

import android.util.Log;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.utils.FormatUtils;

public class StatusNotificationResponse extends XMSZMessage {
    public static final byte RC_BUSY = 3;
    public static final byte RC_CRC_ERROR = 2;
    public static final byte RC_FAIL = 0;
    public static final byte RC_OK = 1;
    private byte returnCode = 1;

    public byte getReturnCode() {
        return this.returnCode;
    }

    public void setReturnCode(byte returnCode2) {
        this.returnCode = returnCode2;
    }

    public byte[] bodyToBytes() throws Exception {
        return new byte[]{this.returnCode};
    }

    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 1) {
            Log.e("StatusNotificationResponse.bodyFromBytes", "body length must be 1 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        this.returnCode = bytes[0];
        return this;
    }
}
