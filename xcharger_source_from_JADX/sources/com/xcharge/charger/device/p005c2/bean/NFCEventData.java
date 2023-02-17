package com.xcharge.charger.device.p005c2.bean;

import com.xcharge.common.bean.JsonBean;

/* renamed from: com.xcharge.charger.device.c2.bean.NFCEventData */
public class NFCEventData extends JsonBean<NFCEventData> {
    public static final String EVENT_TAG = "E@Tag";
    private String baud_rate = null;
    private String modulation_type = null;
    private String port = "1";
    private boolean present = false;
    private String szuuid = null;
    private int uuid = 0;

    public boolean isPresent() {
        return this.present;
    }

    public void setPresent(boolean present2) {
        this.present = present2;
    }

    public String getModulation_type() {
        return this.modulation_type;
    }

    public void setModulation_type(String modulation_type2) {
        this.modulation_type = modulation_type2;
    }

    public String getBaud_rate() {
        return this.baud_rate;
    }

    public void setBaud_rate(String baud_rate2) {
        this.baud_rate = baud_rate2;
    }

    public int getUuid() {
        return this.uuid;
    }

    public void setUuid(int uuid2) {
        this.uuid = uuid2;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port2) {
        this.port = port2;
    }

    public String getSzuuid() {
        return this.szuuid;
    }

    public void setSzuuid(String szuuid2) {
        this.szuuid = szuuid2;
    }
}
