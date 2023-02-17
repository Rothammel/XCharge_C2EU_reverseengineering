package com.xcharge.charger.core.type;

import com.xcharge.charger.protocol.monitor.bean.request.InitRequest;

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

    private FIN_MODE(String mode2) {
        this.mode = null;
        this.mode = mode2;
    }

    public String getMode() {
        return this.mode;
    }
}
