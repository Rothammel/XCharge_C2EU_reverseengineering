package com.xcharge.charger.data.bean.type;

import com.xcharge.charger.protocol.monitor.bean.request.InitRequest;

/* loaded from: classes.dex */
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

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static CHARGE_USER_TYPE[] valuesCustom() {
        CHARGE_USER_TYPE[] valuesCustom = values();
        int length = valuesCustom.length;
        CHARGE_USER_TYPE[] charge_user_typeArr = new CHARGE_USER_TYPE[length];
        System.arraycopy(valuesCustom, 0, charge_user_typeArr, 0, length);
        return charge_user_typeArr;
    }

    CHARGE_USER_TYPE(String userType) {
        this.userType = null;
        this.userType = userType;
    }

    public String getUserType() {
        return this.userType;
    }
}
