package com.xcharge.charger.data.bean;

import com.xcharge.common.bean.JsonBean;

public class XKeyseed extends JsonBean<XKeyseed> {
    private String group = null;

    /* renamed from: id */
    private String f48id = null;
    private String seed = null;
    private String type = null;

    public XKeyseed() {
    }

    public XKeyseed(String group2, String seed2) {
        this.group = group2;
        this.seed = seed2;
    }

    public String getId() {
        return this.f48id;
    }

    public void setId(String id) {
        this.f48id = id;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group2) {
        this.group = group2;
    }

    public String getSeed() {
        return this.seed;
    }

    public void setSeed(String seed2) {
        this.seed = seed2;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
    }
}
