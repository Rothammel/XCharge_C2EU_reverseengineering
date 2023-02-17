package com.xcharge.charger.data.bean.type;

import com.xcharge.charger.protocol.monitor.bean.request.InitRequest;

public enum CHARGE_USER_TYPE {
    nfc(InitRequest.UTYPE_NFC),
    wxmp(InitRequest.UTYPE_WXMP),
    wechat(InitRequest.UTYPE_WECHAT),
    qq(InitRequest.UTYPE_QQ),
    weibo(InitRequest.UTYPE_WEIBO),
    twitter(InitRequest.UTYPE_TWITTER),
    facebook(InitRequest.UTYPE_FACEBOOK),
    anyo("anyo"),
    xcharge("xcharge"),
    xmsz("xmsz"),
    ptne("ptne"),
    ecw("ecw"),
    yzx("yzx"),
    cddz("cddz"),
    ocpp("ocpp");
    
    private String userType;

    private CHARGE_USER_TYPE(String userType2) {
        this.userType = null;
        this.userType = userType2;
    }

    public String getUserType() {
        return this.userType;
    }
}
