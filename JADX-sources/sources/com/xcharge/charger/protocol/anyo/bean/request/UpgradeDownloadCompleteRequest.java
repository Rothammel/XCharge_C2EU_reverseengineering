package com.xcharge.charger.protocol.anyo.bean.request;

import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.common.utils.FormatUtils;

/* loaded from: classes.dex */
public class UpgradeDownloadCompleteRequest extends AnyoMessage {
    public static final byte DL_FAIL = 2;
    public static final byte DL_FAIL_RETRY = 1;
    public static final byte DL_SUCCESS = 0;
    private byte result = 0;

    public byte getResult() {
        return this.result;
    }

    public void setResult(byte result) {
        this.result = result;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = {this.result};
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 1) {
            Log.e("UpgradeDownloadCompleteRequest.bodyFromBytes", "body length must be 1 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        this.result = bytes[0];
        return this;
    }
}
