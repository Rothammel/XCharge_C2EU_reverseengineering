package com.xcharge.charger.protocol.family.xcloud.type;

public enum EnumPaymentMethod {
    wechatPage("wechatPage"),
    userBalance("userBalance"),
    groupBalance("groupBalance"),
    record("record"),
    outside("outside");
    
    private String method;

    private EnumPaymentMethod(String method2) {
        this.method = null;
        this.method = method2;
    }

    public String getMethod() {
        return this.method;
    }
}
