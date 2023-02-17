package com.xcharge.charger.protocol.xmsz.bean.cloud;

import android.util.Log;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.utils.FormatUtils;

public class ResetRequest extends XMSZMessage {
    public byte[] bodyToBytes() throws Exception {
        return new byte[0];
    }

    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length <= 0) {
            return this;
        }
        Log.e("ResetRequest.bodyFromBytes", "body must be empty !!! body: " + FormatUtils.bytesToHexString(bytes));
        throw new IllegalArgumentException();
    }
}
