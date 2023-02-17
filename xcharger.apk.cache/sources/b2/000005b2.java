package com.xcharge.charger.data.bean.type;

import com.xcharge.charger.core.api.bean.cap.SetDirective;

/* loaded from: classes.dex */
public enum LOCK_STATUS {
    disable("disable"),
    lock(SetDirective.OPR_LOCK),
    unlock(SetDirective.OPR_UNLOCK),
    fault("fault");
    
    private String status;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static LOCK_STATUS[] valuesCustom() {
        LOCK_STATUS[] valuesCustom = values();
        int length = valuesCustom.length;
        LOCK_STATUS[] lock_statusArr = new LOCK_STATUS[length];
        System.arraycopy(valuesCustom, 0, lock_statusArr, 0, length);
        return lock_statusArr;
    }

    LOCK_STATUS(String status) {
        this.status = null;
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}