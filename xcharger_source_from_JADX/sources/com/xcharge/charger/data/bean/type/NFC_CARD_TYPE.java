package com.xcharge.charger.data.bean.type;

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

    private NFC_CARD_TYPE(String type2) {
        this.type = null;
        this.type = type2;
    }

    public String getType() {
        return this.type;
    }
}
