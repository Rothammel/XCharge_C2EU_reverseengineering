package com.xcharge.charger.device.p005c2.bean;

import com.xcharge.common.bean.JsonBean;

/* renamed from: com.xcharge.charger.device.c2.bean.CDDZCardKeySeeds */
public class CDDZCardKeySeeds extends JsonBean<CDDZCardKeySeeds> {
    private String seedA = null;
    private String seedB = null;

    public String getSeedA() {
        return this.seedA;
    }

    public void setSeedA(String seedA2) {
        this.seedA = seedA2;
    }

    public String getSeedB() {
        return this.seedB;
    }

    public void setSeedB(String seedB2) {
        this.seedB = seedB2;
    }
}
