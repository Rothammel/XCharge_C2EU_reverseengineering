package com.xcharge.charger.protocol.family.xcloud.type;

/* loaded from: classes.dex */
public enum EnumPaymentMethod {
    wechatPage("wechatPage"),
    userBalance("userBalance"),
    groupBalance("groupBalance"),
    record("record"),
    outside("outside");
    
    private String method;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static EnumPaymentMethod[] valuesCustom() {
        EnumPaymentMethod[] valuesCustom = values();
        int length = valuesCustom.length;
        EnumPaymentMethod[] enumPaymentMethodArr = new EnumPaymentMethod[length];
        System.arraycopy(valuesCustom, 0, enumPaymentMethodArr, 0, length);
        return enumPaymentMethodArr;
    }

    EnumPaymentMethod(String method) {
        this.method = null;
        this.method = method;
    }

    public String getMethod() {
        return this.method;
    }
}
