package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class DAPCapability extends JsonBean<DAPCapability> {
    public static final String AC = "ac";
    public static final String DC = "dc";
    private double amp_capacity;
    private String current_type;
    private boolean gun_lock;
    private int kwatt_capacity;
    private boolean nfc = true;
    private boolean park_lock;
    private int phases;
    private int ports;
    private boolean radar;
    private String screen;

    public double getAmp_capacity() {
        return this.amp_capacity;
    }

    public void setAmp_capacity(double amp_capacity) {
        this.amp_capacity = amp_capacity;
    }

    public int getKwatt_capacity() {
        return this.kwatt_capacity;
    }

    public void setKwatt_capacity(int kwatt_capacity) {
        this.kwatt_capacity = kwatt_capacity;
    }

    public String getCurrent_type() {
        return this.current_type;
    }

    public void setCurrent_type(String current_type) {
        this.current_type = current_type;
    }

    public int getPhases() {
        return this.phases;
    }

    public void setPhases(int phases) {
        this.phases = phases;
    }

    public int getPorts() {
        return this.ports;
    }

    public void setPorts(int ports) {
        this.ports = ports;
    }

    public boolean isGun_lock() {
        return this.gun_lock;
    }

    public void setGun_lock(boolean gun_lock) {
        this.gun_lock = gun_lock;
    }

    public boolean isNfc() {
        return this.nfc;
    }

    public void setNfc(boolean nfc) {
        this.nfc = nfc;
    }

    public String getScreen() {
        return this.screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public boolean isRadar() {
        return this.radar;
    }

    public void setRadar(boolean radar) {
        this.radar = radar;
    }

    public boolean isPark_lock() {
        return this.park_lock;
    }

    public void setPark_lock(boolean park_lock) {
        this.park_lock = park_lock;
    }
}