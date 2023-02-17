package com.xcharge.charger.data.bean.type;

public enum SERVICE_REGION {
    China("China"),
    Europe("Europe");
    
    private String region;

    private SERVICE_REGION(String region2) {
        this.region = null;
        this.region = region2;
    }

    public String getRegion() {
        return this.region;
    }
}
