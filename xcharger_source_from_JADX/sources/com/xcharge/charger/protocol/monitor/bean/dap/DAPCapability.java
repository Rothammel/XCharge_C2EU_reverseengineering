package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;

public class DAPCapability extends JsonBean<DAPCapability> {

    /* renamed from: AC */
    public static final String f96AC = "ac";

    /* renamed from: DC */
    public static final String f97DC = "dc";
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

    public void setAmp_capacity(double amp_capacity2) {
        this.amp_capacity = amp_capacity2;
    }

    public int getKwatt_capacity() {
        return this.kwatt_capacity;
    }

    public void setKwatt_capacity(int kwatt_capacity2) {
        this.kwatt_capacity = kwatt_capacity2;
    }

    public String getCurrent_type() {
        return this.current_type;
    }

    public void setCurrent_type(String current_type2) {
        this.current_type = current_type2;
    }

    public int getPhases() {
        return this.phases;
    }

    public void setPhases(int phases2) {
        this.phases = phases2;
    }

    public int getPorts() {
        return this.ports;
    }

    public void setPorts(int ports2) {
        this.ports = ports2;
    }

    public boolean isGun_lock() {
        return this.gun_lock;
    }

    public void setGun_lock(boolean gun_lock2) {
        this.gun_lock = gun_lock2;
    }

    public boolean isNfc() {
        return this.nfc;
    }

    public void setNfc(boolean nfc2) {
        this.nfc = nfc2;
    }

    public String getScreen() {
        return this.screen;
    }

    public void setScreen(String screen2) {
        this.screen = screen2;
    }

    public boolean isRadar() {
        return this.radar;
    }

    public void setRadar(boolean radar2) {
        this.radar = radar2;
    }

    public boolean isPark_lock() {
        return this.park_lock;
    }

    public void setPark_lock(boolean park_lock2) {
        this.park_lock = park_lock2;
    }
}
