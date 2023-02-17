package com.xcharge.charger.protocol.family.xcloud.type;

public enum EnumDeviceAction {
    restart("restart"),
    lockPlug("lockPlug"),
    unlockPlug("unlockPlug");
    
    private String action;

    private EnumDeviceAction(String action2) {
        this.action = null;
        this.action = action2;
    }

    public String getAction() {
        return this.action;
    }
}
