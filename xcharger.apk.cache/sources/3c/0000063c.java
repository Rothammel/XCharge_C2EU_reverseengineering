package com.xcharge.charger.protocol.anyo.bean.response;

import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.common.utils.FormatUtils;

/* loaded from: classes.dex */
public class ReportChargeResponse extends AnyoMessage {
    private Byte portNo = null;

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public byte[] bodyToBytes() throws Exception {
        if (this.portNo == null) {
            return new byte[0];
        }
        byte[] bytes = {this.portNo.byteValue()};
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 0 && bytes.length != 1) {
            Log.e("ReportChargeResponse.bodyFromBytes", "body length must be 0 or 1 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        if (bytes.length == 1) {
            this.portNo = Byte.valueOf(bytes[0]);
        }
        return this;
    }
}