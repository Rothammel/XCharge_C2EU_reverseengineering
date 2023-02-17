package com.xcharge.charger.protocol.anyo.bean.request;

import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.common.utils.FormatUtils;

public class QuerySysInfoRequest extends AnyoMessage {
    public byte[] bodyToBytes() throws Exception {
        return new byte[0];
    }

    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length <= 0) {
            return this;
        }
        Log.e("QuerySysInfoRequest.bodyFromBytes", "body must be empty !!! body: " + FormatUtils.bytesToHexString(bytes));
        throw new IllegalArgumentException();
    }
}
