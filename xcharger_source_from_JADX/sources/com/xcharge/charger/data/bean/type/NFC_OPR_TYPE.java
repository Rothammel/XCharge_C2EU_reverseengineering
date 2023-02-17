package com.xcharge.charger.data.bean.type;

public enum NFC_OPR_TYPE {
    set("set"),
    charge("charge"),
    pay("pay"),
    bind("bind");
    
    private String type;

    private NFC_OPR_TYPE(String type2) {
        this.type = null;
        this.type = type2;
    }

    public String getType() {
        return this.type;
    }
}
