package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class NFCGroupSeed extends JsonBean<NFCGroupSeed> {
    private Long id = null;
    private String seedM1 = null;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSeedM1() {
        return this.seedM1;
    }

    public void setSeedM1(String seedM1) {
        this.seedM1 = seedM1;
    }
}