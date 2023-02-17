package com.xcharge.charger.p006ui.p009c2.activity.data;

/* renamed from: com.xcharge.charger.ui.c2.activity.data.Variate */
public class Variate {
    private static Variate instance = null;
    private String ChargeId;
    public String activity;
    public boolean isHandleStatus = false;
    public boolean isInit = false;
    public boolean isNFC = true;
    public boolean isOnline = false;
    public boolean isPlugin = false;
    public boolean isRadar = false;

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

    public void setInit(boolean isInit2) {
        this.isInit = isInit2;
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

    public void setNFC(boolean isNFC2) {
        this.isNFC = isNFC2;
    }

    public boolean isHandleStatus() {
        return this.isHandleStatus;
    }

    public void setHandleStatus(boolean isHandleStatus2) {
        this.isHandleStatus = isHandleStatus2;
    }

    public boolean isRadar() {
        return this.isRadar;
    }

    public void setRadar(boolean isRadar2) {
        this.isRadar = isRadar2;
    }

    public String getActivity() {
        return this.activity;
    }

    public void setActivity(String activity2) {
        this.activity = activity2;
    }

    public boolean isOnline() {
        return this.isOnline;
    }

    public void setOnline(boolean isOnline2) {
        this.isOnline = isOnline2;
    }

    public boolean isPlugin() {
        return this.isPlugin;
    }

    public void setPlugin(boolean isPlugin2) {
        this.isPlugin = isPlugin2;
    }
}
