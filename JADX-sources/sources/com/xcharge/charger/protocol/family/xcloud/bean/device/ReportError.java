package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class ReportError extends JsonBean<ReportError> {
    private int port = 1;
    private long time = 0;
    private ArrayList<DeviceError> error = null;

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public ArrayList<DeviceError> getError() {
        return this.error;
    }

    public void setError(ArrayList<DeviceError> error) {
        this.error = error;
    }
}
