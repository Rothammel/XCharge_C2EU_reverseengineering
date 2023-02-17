package com.xcharge.charger.device.c2.bean;

import com.xcharge.charger.data.bean.type.WORK_MODE;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class ManageCardData extends JsonBean<ManageCardData> {
    public static final String RADAR_DISABLE = "disable";
    public static final String RADAR_ENABLE = "enable";
    private String group = null;
    private String keyseed = null;
    private String key = null;
    private boolean clean = false;
    private ArrayList<ArrayList<Integer>> timedPrice = null;
    private String welcome = null;
    private String workmode = WORK_MODE.Public.getMode();
    private int outpower = -1;
    private int elecLockMode = -1;
    private String radermode = null;
    private Integer cpRange = null;
    private Integer voltageRange = null;

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getKeyseed() {
        return this.keyseed;
    }

    public void setKeyseed(String keyseed) {
        this.keyseed = keyseed;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isClean() {
        return this.clean;
    }

    public void setClean(boolean clean) {
        this.clean = clean;
    }

    public ArrayList<ArrayList<Integer>> getTimedPrice() {
        return this.timedPrice;
    }

    public void setTimedPrice(ArrayList<ArrayList<Integer>> timedPrice) {
        this.timedPrice = timedPrice;
    }

    public String getWelcome() {
        return this.welcome;
    }

    public void setWelcome(String welcome) {
        this.welcome = welcome;
    }

    public String getWorkmode() {
        return this.workmode;
    }

    public void setWorkmode(String workmode) {
        this.workmode = workmode;
    }

    public int getOutpower() {
        return this.outpower;
    }

    public void setOutpower(int outpower) {
        this.outpower = outpower;
    }

    public int getElecLockMode() {
        return this.elecLockMode;
    }

    public void setElecLockMode(int elecLockMode) {
        this.elecLockMode = elecLockMode;
    }

    public String getRadermode() {
        return this.radermode;
    }

    public void setRadermode(String radermode) {
        this.radermode = radermode;
    }

    public Integer getCpRange() {
        return this.cpRange;
    }

    public void setCpRange(Integer cpRange) {
        this.cpRange = cpRange;
    }

    public Integer getVoltageRange() {
        return this.voltageRange;
    }

    public void setVoltageRange(Integer voltageRange) {
        this.voltageRange = voltageRange;
    }
}