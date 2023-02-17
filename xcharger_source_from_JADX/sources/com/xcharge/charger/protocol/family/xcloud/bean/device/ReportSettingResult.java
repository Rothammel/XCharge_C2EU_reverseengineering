package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

public class ReportSettingResult extends JsonBean<ReportSettingResult> {
    private DeviceError error = null;
    private ArrayList<String> fileUrls = null;
    private Long sid = null;
    private long time = 0;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid2) {
        this.sid = sid2;
    }

    public ArrayList<String> getFileUrls() {
        return this.fileUrls;
    }

    public void setFileUrls(ArrayList<String> fileUrls2) {
        this.fileUrls = fileUrls2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }

    public DeviceError getError() {
        return this.error;
    }

    public void setError(DeviceError error2) {
        this.error = error2;
    }
}
