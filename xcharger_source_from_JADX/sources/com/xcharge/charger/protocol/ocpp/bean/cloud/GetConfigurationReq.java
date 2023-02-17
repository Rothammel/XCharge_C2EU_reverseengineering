package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

public class GetConfigurationReq extends JsonBean<GetConfigurationReq> {
    private ArrayList<String> key = new ArrayList<>();

    public ArrayList<String> getKey() {
        return this.key;
    }

    public void setKey(ArrayList<String> key2) {
        this.key = key2;
    }
}
