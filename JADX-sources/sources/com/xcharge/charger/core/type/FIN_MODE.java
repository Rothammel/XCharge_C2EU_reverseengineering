package com.xcharge.charger.core.type;

import com.xcharge.charger.protocol.monitor.bean.request.InitRequest;

/* loaded from: classes.dex */
public enum FIN_MODE {
    normal("normal"),
    timeout("timeout"),
    cancel("cancel"),
    port_forbiden("port_forbiden"),
    busy("busy"),
    no_feerate("no_feerate"),
    plugin_timeout("plugin_timeout"),
    car("car"),
    error("error"),
    reserve_error("error"),
    remote("remote"),
    nfc(InitRequest.UTYPE_NFC),
    refuse("refuse");
    
    private String mode;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static FIN_MODE[] valuesCustom() {
        FIN_MODE[] valuesCustom = values();
        int length = valuesCustom.length;
        FIN_MODE[] fin_modeArr = new FIN_MODE[length];
        System.arraycopy(valuesCustom, 0, fin_modeArr, 0, length);
        return fin_modeArr;
    }

    FIN_MODE(String mode) {
        this.mode = null;
        this.mode = mode;
    }

    public String getMode() {
        return this.mode;
    }
}
