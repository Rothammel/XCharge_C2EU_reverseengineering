package com.xcharge.charger.device.c2.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class NFCEventData extends JsonBean<NFCEventData> {
    public static final String EVENT_TAG = "E@Tag";
    private boolean present = false;
    private String modulation_type = null;
    private String baud_rate = null;
    private int uuid = 0;
    private String szuuid = null;
    private String port = "1";

    public boolean isPresent() {
        return this.present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public String getModulation_type() {
        return this.modulation_type;
    }

    public void setModulation_type(String modulation_type) {
        this.modulation_type = modulation_type;
    }

    public String getBaud_rate() {
        return this.baud_rate;
    }

    public void setBaud_rate(String baud_rate) {
        this.baud_rate = baud_rate;
    }

    public int getUuid() {
        return this.uuid;
    }

    public void setUuid(int uuid) {
        this.uuid = uuid;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getSzuuid() {
        return this.szuuid;
    }

    public void setSzuuid(String szuuid) {
        this.szuuid = szuuid;
    }
}
