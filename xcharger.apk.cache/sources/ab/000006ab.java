package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class ReportSettingResult extends JsonBean<ReportSettingResult> {
    private Long sid = null;
    private ArrayList<String> fileUrls = null;
    private long time = 0;
    private DeviceError error = null;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public ArrayList<String> getFileUrls() {
        return this.fileUrls;
    }

    public void setFileUrls(ArrayList<String> fileUrls) {
        this.fileUrls = fileUrls;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public DeviceError getError() {
        return this.error;
    }

    public void setError(DeviceError error) {
        this.error = error;
    }
}