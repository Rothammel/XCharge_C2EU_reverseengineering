package com.xcharge.charger.protocol.family.xcloud.type;

/* loaded from: classes.dex */
public enum EnumDeviceAction {
    restart("restart"),
    lockPlug("lockPlug"),
    unlockPlug("unlockPlug");
    
    private String action;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static EnumDeviceAction[] valuesCustom() {
        EnumDeviceAction[] valuesCustom = values();
        int length = valuesCustom.length;
        EnumDeviceAction[] enumDeviceActionArr = new EnumDeviceAction[length];
        System.arraycopy(valuesCustom, 0, enumDeviceActionArr, 0, length);
        return enumDeviceActionArr;
    }

    EnumDeviceAction(String action) {
        this.action = null;
        this.action = action;
    }

    public String getAction() {
        return this.action;
    }
}
