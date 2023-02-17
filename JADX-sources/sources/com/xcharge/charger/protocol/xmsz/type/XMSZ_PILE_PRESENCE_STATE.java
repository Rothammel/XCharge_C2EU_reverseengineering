package com.xcharge.charger.protocol.xmsz.type;

import com.xcharge.charger.protocol.monitor.bean.YZXDCAPMessage;

/* loaded from: classes.dex */
public enum XMSZ_PILE_PRESENCE_STATE {
    offline("offline"),
    online(YZXDCAPMessage.OP_ONLINE);
    
    private String status;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static XMSZ_PILE_PRESENCE_STATE[] valuesCustom() {
        XMSZ_PILE_PRESENCE_STATE[] valuesCustom = values();
        int length = valuesCustom.length;
        XMSZ_PILE_PRESENCE_STATE[] xmsz_pile_presence_stateArr = new XMSZ_PILE_PRESENCE_STATE[length];
        System.arraycopy(valuesCustom, 0, xmsz_pile_presence_stateArr, 0, length);
        return xmsz_pile_presence_stateArr;
    }

    XMSZ_PILE_PRESENCE_STATE(String status) {
        this.status = null;
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
