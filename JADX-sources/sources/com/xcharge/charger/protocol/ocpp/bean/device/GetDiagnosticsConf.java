package com.xcharge.charger.protocol.ocpp.bean.device;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class GetDiagnosticsConf extends JsonBean<GetDiagnosticsConf> {
    private String fileName;

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
