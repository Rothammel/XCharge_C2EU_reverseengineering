package com.xcharge.charger.ui.adapter.type;

/* loaded from: classes.dex */
public enum CHALLENGE_TYPE {
    verification("verification");
    
    private String type;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static CHALLENGE_TYPE[] valuesCustom() {
        CHALLENGE_TYPE[] valuesCustom = values();
        int length = valuesCustom.length;
        CHALLENGE_TYPE[] challenge_typeArr = new CHALLENGE_TYPE[length];
        System.arraycopy(valuesCustom, 0, challenge_typeArr, 0, length);
        return challenge_typeArr;
    }

    CHALLENGE_TYPE(String type) {
        this.type = null;
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}