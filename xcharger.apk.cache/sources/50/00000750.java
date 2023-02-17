package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.charger.protocol.ocpp.bean.types.IdTagInfo;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class AuthorizeConf extends JsonBean<AuthorizeConf> {
    private IdTagInfo idTagInfo;

    public IdTagInfo getIdTagInfo() {
        return this.idTagInfo;
    }

    public void setIdTagInfo(IdTagInfo idTagInfo) {
        this.idTagInfo = idTagInfo;
    }
}