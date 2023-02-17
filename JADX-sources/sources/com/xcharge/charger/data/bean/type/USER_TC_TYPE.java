package com.xcharge.charger.data.bean.type;

import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;

/* loaded from: classes.dex */
public enum USER_TC_TYPE {
    auto(ChargeStopCondition.TYPE_AUTO),
    fee(ChargeStopCondition.TYPE_FEE),
    power(ChargeStopCondition.TYPE_POWER),
    time(ChargeStopCondition.TYPE_TIME),
    toc("toc");
    
    private String type;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static USER_TC_TYPE[] valuesCustom() {
        USER_TC_TYPE[] valuesCustom = values();
        int length = valuesCustom.length;
        USER_TC_TYPE[] user_tc_typeArr = new USER_TC_TYPE[length];
        System.arraycopy(valuesCustom, 0, user_tc_typeArr, 0, length);
        return user_tc_typeArr;
    }

    USER_TC_TYPE(String type) {
        this.type = null;
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
