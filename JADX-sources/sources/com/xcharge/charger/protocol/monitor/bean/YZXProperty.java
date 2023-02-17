package com.xcharge.charger.protocol.monitor.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class YZXProperty extends JsonBean<YZXProperty> {
    public static final String CAPABILITY_AC = "ac";
    public static final String CAPABILITY_DC = "dc";
    public static final String CHARGE_STATUS_CHARGING = "charging";
    public static final String CHARGE_STATUS_IDLE = "idle";
    public static final String CHARGE_STATUS_RESERVED = "reserved";
    public static final String CHARGE_STATUS_STARTING = "starting";
    public static final String CHARGE_STATUS_STOPPED = "stopped";
    public static final String GUN_CONNECT_CONTAINER = "container";
    public static final String GUN_CONNECT_NONE = "none";
    public static final String GUN_CONNECT_VEHICLE = "vehicle";
    public static final String GUN_LOCK_DISABLE = "disable";
    public static final String GUN_LOCK_LOCKED = "locked";
    public static final String GUN_LOCK_UNLOCKED = "unlocked";
    public static final String RADAR_DISABLE = "disable";
    public static final String RADAR_ENABLE = "enable";
    public static final String UPGRADE_DOWNLOADING = "downloading";
    public static final String UPGRADE_FAILED = "failed";
    public static final String UPGRADE_SUCCESS = "success";
    public static final String UPGRADE_UPDATING = "updating";
    public static final String UPGRADE_VALIDATING = "validating";
    private String id = null;
    private String type = null;
    private Object value = null;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
