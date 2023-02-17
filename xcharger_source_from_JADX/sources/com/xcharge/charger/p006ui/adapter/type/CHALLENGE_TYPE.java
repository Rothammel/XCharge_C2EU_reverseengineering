package com.xcharge.charger.p006ui.adapter.type;

/* renamed from: com.xcharge.charger.ui.adapter.type.CHALLENGE_TYPE */
public enum CHALLENGE_TYPE {
    verification("verification");
    
    private String type;

    private CHALLENGE_TYPE(String type2) {
        this.type = null;
        this.type = type2;
    }

    public String getType() {
        return this.type;
    }
}
