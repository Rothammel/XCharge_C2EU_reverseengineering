package com.xcharge.charger.protocol.family.xcloud.type;

public enum EnumCurrencyType {
    cny("cny"),
    usd("usd");
    
    private String type;

    private EnumCurrencyType(String type2) {
        this.type = null;
        this.type = type2;
    }

    public String getType() {
        return this.type;
    }
}
