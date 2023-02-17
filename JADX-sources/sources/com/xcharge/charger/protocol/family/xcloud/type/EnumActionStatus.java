package com.xcharge.charger.protocol.family.xcloud.type;

import com.xcharge.charger.protocol.monitor.bean.YZXProperty;

/* loaded from: classes.dex */
public enum EnumActionStatus {
    received("received"),
    success(YZXProperty.UPGRADE_SUCCESS),
    failed(YZXProperty.UPGRADE_FAILED);
    
    private String status;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static EnumActionStatus[] valuesCustom() {
        EnumActionStatus[] valuesCustom = values();
        int length = valuesCustom.length;
        EnumActionStatus[] enumActionStatusArr = new EnumActionStatus[length];
        System.arraycopy(valuesCustom, 0, enumActionStatusArr, 0, length);
        return enumActionStatusArr;
    }

    EnumActionStatus(String status) {
        this.status = null;
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
