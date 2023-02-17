package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum NFC_OPR_TYPE {
    set("set"),
    charge("charge"),
    pay("pay"),
    bind("bind");
    
    private String type;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static NFC_OPR_TYPE[] valuesCustom() {
        NFC_OPR_TYPE[] valuesCustom = values();
        int length = valuesCustom.length;
        NFC_OPR_TYPE[] nfc_opr_typeArr = new NFC_OPR_TYPE[length];
        System.arraycopy(valuesCustom, 0, nfc_opr_typeArr, 0, length);
        return nfc_opr_typeArr;
    }

    NFC_OPR_TYPE(String type) {
        this.type = null;
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
