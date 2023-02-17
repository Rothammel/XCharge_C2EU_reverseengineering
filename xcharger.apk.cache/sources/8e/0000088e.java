package com.xcharge.charger.ui.c2.activity.data;

/* loaded from: classes.dex */
public class Variate {
    private static Variate instance = null;
    private String ChargeId;
    public String activity;
    public boolean isInit = false;
    public boolean isNFC = true;
    public boolean isHandleStatus = false;
    public boolean isRadar = false;
    public boolean isOnline = false;
    public boolean isPlugin = false;

    public static synchronized Variate getInstance() {
        Variate variate;
        synchronized (Variate.class) {
            if (instance == null) {
                instance = new Variate();
            }
            variate = instance;
        }
        return variate;
    }

    public boolean isInit() {
        return this.isInit;
    }

    public void setInit(boolean isInit) {
        this.isInit = isInit;
    }

    public String getChargeId() {
        return this.ChargeId;
    }

    public void setChargeId(String chargeId) {
        this.ChargeId = chargeId;
    }

    public boolean isNFC() {
        return this.isNFC;
    }

    public void setNFC(boolean isNFC) {
        this.isNFC = isNFC;
    }

    public boolean isHandleStatus() {
        return this.isHandleStatus;
    }

    public void setHandleStatus(boolean isHandleStatus) {
        this.isHandleStatus = isHandleStatus;
    }

    public boolean isRadar() {
        return this.isRadar;
    }

    public void setRadar(boolean isRadar) {
        this.isRadar = isRadar;
    }

    public String getActivity() {
        return this.activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public boolean isOnline() {
        return this.isOnline;
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public boolean isPlugin() {
        return this.isPlugin;
    }

    public void setPlugin(boolean isPlugin) {
        this.isPlugin = isPlugin;
    }
}