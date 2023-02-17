package com.xcharge.charger.protocol.ocpp.bean.device;

import com.xcharge.charger.protocol.ocpp.bean.types.KeyValue;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

public class GetConfigurationConf extends JsonBean<GetConfigurationConf> {
    private ArrayList<KeyValue> configurationKey = null;
    private ArrayList<String> unknownKey = null;

    public ArrayList<KeyValue> getConfigurationKey() {
        return this.configurationKey;
    }

    public void setConfigurationKey(ArrayList<KeyValue> configurationKey2) {
        this.configurationKey = configurationKey2;
    }

    public ArrayList<String> getUnknownKey() {
        return this.unknownKey;
    }

    public void setUnknownKey(ArrayList<String> unknownKey2) {
        this.unknownKey = unknownKey2;
    }
}
