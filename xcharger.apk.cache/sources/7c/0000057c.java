package com.xcharge.charger.data.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class XKeyseed extends JsonBean<XKeyseed> {
    private String group;
    private String id;
    private String seed;
    private String type;

    public XKeyseed() {
        this.id = null;
        this.group = null;
        this.seed = null;
        this.type = null;
    }

    public XKeyseed(String group, String seed) {
        this.id = null;
        this.group = null;
        this.seed = null;
        this.type = null;
        this.group = group;
        this.seed = seed;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getSeed() {
        return this.seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
}