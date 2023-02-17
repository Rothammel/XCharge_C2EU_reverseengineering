package com.xcharge.charger.data.bean.type;

public enum PLATFORM_CUSTOMER {
    jsmny("jsmny"),
    anyo_private("anyo_private"),
    anyo_svw("anyo_svw"),
    ct_demo("ct_demo"),
    be_ENERGISED("be_ENERGISED"),
    emobility_partner("emobility_partner");
    
    private String customer;

    private PLATFORM_CUSTOMER(String customer2) {
        this.customer = null;
        this.customer = customer2;
    }

    public String getCustomer() {
        return this.customer;
    }
}
