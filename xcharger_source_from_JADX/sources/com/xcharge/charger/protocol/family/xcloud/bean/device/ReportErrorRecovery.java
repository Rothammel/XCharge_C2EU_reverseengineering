package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

public class ReportErrorRecovery extends JsonBean<ReportErrorRecovery> {
    private ArrayList<DeviceError> error = null;
    private int port = 1;
    private long time = 0;

    public int getPort() {
        return this.port;
    }

    public void setPort(int port2) {
        this.port = port2;
    }

    public ArrayList<DeviceError> getError() {
        return this.error;
    }

    public void setError(ArrayList<DeviceError> error2) {
        this.error = error2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }
}
