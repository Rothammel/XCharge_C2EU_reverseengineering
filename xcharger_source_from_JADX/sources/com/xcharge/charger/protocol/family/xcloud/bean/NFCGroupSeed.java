package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

public class NFCGroupSeed extends JsonBean<NFCGroupSeed> {

    /* renamed from: id */
    private Long f82id = null;
    private String seedM1 = null;

    public Long getId() {
        return this.f82id;
    }

    public void setId(Long id) {
        this.f82id = id;
    }

    public String getSeedM1() {
        return this.seedM1;
    }

    public void setSeedM1(String seedM12) {
        this.seedM1 = seedM12;
    }
}
