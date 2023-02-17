package com.xcharge.charger.protocol.xmsz.type;

import com.xcharge.charger.protocol.monitor.bean.YZXDCAPMessage;

public enum XMSZ_PILE_PRESENCE_STATE {
    offline("offline"),
    online(YZXDCAPMessage.OP_ONLINE);
    
    private String status;

    private XMSZ_PILE_PRESENCE_STATE(String status2) {
        this.status = null;
        this.status = status2;
    }

    public String getStatus() {
        return this.status;
    }
}
