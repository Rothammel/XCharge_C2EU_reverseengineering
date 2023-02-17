package com.xcharge.charger.device.p005c2.bean;

import com.xcharge.charger.data.bean.type.WORK_MODE;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

/* renamed from: com.xcharge.charger.device.c2.bean.ManageCardData */
public class ManageCardData extends JsonBean<ManageCardData> {
    public static final String RADAR_DISABLE = "disable";
    public static final String RADAR_ENABLE = "enable";
    private boolean clean = false;
    private Integer cpRange = null;
    private int elecLockMode = -1;
    private String group = null;
    private String key = null;
    private String keyseed = null;
    private int outpower = -1;
    private String radermode = null;
    private ArrayList<ArrayList<Integer>> timedPrice = null;
    private Integer voltageRange = null;
    private String welcome = null;
    private String workmode = WORK_MODE.Public.getMode();

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group2) {
        this.group = group2;
    }

    public String getKeyseed() {
        return this.keyseed;
    }

    public void setKeyseed(String keyseed2) {
        this.keyseed = keyseed2;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key2) {
        this.key = key2;
    }

    public boolean isClean() {
        return this.clean;
    }

    public void setClean(boolean clean2) {
        this.clean = clean2;
    }

    public ArrayList<ArrayList<Integer>> getTimedPrice() {
        return this.timedPrice;
    }

    public void setTimedPrice(ArrayList<ArrayList<Integer>> timedPrice2) {
        this.timedPrice = timedPrice2;
    }

    public String getWelcome() {
        return this.welcome;
    }

    public void setWelcome(String welcome2) {
        this.welcome = welcome2;
    }

    public String getWorkmode() {
        return this.workmode;
    }

    public void setWorkmode(String workmode2) {
        this.workmode = workmode2;
    }

    public int getOutpower() {
        return this.outpower;
    }

    public void setOutpower(int outpower2) {
        this.outpower = outpower2;
    }

    public int getElecLockMode() {
        return this.elecLockMode;
    }

    public void setElecLockMode(int elecLockMode2) {
        this.elecLockMode = elecLockMode2;
    }

    public String getRadermode() {
        return this.radermode;
    }

    public void setRadermode(String radermode2) {
        this.radermode = radermode2;
    }

    public Integer getCpRange() {
        return this.cpRange;
    }

    public void setCpRange(Integer cpRange2) {
        this.cpRange = cpRange2;
    }

    public Integer getVoltageRange() {
        return this.voltageRange;
    }

    public void setVoltageRange(Integer voltageRange2) {
        this.voltageRange = voltageRange2;
    }
}
