package com.xcharge.charger.protocol.anyo.bean.response;

import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.common.utils.FormatUtils;

public class ReportChargeResponse extends AnyoMessage {
    private Byte portNo = null;

    public byte[] bodyToBytes() throws Exception {
        if (this.portNo == null) {
            return new byte[0];
        }
        return new byte[]{this.portNo.byteValue()};
    }

    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length == 0 || bytes.length == 1) {
            if (bytes.length == 1) {
                this.portNo = Byte.valueOf(bytes[0]);
            }
            return this;
        }
        Log.e("ReportChargeResponse.bodyFromBytes", "body length must be 0 or 1 !!! body: " + FormatUtils.bytesToHexString(bytes));
        throw new IllegalArgumentException();
    }
}
