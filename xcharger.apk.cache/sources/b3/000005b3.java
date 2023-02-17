package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum NFC_CARD_TYPE {
    M1("M1"),
    M2("M2"),
    U1("U1"),
    U2("U2"),
    U3("U3"),
    CT_DEMO("CT_DEMO"),
    anyo1("anyo1"),
    anyo_svw("anyo_svw"),
    ptne1("ptne1"),
    ecw1("ecw1"),
    cddz_m("cddz_m"),
    cddz_1("cddz_1"),
    cddz_2("cddz_2"),
    ocpp("ocpp");
    
    private String type;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static NFC_CARD_TYPE[] valuesCustom() {
        NFC_CARD_TYPE[] valuesCustom = values();
        int length = valuesCustom.length;
        NFC_CARD_TYPE[] nfc_card_typeArr = new NFC_CARD_TYPE[length];
        System.arraycopy(valuesCustom, 0, nfc_card_typeArr, 0, length);
        return nfc_card_typeArr;
    }

    NFC_CARD_TYPE(String type) {
        this.type = null;
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}