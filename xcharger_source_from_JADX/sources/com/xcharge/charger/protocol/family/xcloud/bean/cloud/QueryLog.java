package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.common.bean.JsonBean;

public class QueryLog extends JsonBean<QueryLog> {
    private long endTime = 0;
    private Long sid = null;
    private long startTime = 0;
    private String[] subject = null;
    private long time = 0;
    private String uploadUrl;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid2) {
        this.sid = sid2;
    }

    public String getUploadUrl() {
        return this.uploadUrl;
    }

    public void setUploadUrl(String uploadUrl2) {
        this.uploadUrl = uploadUrl2;
    }

    public String[] getSubject() {
        return this.subject;
    }

    public void setSubject(String[] subject2) {
        this.subject = subject2;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime2) {
        this.startTime = startTime2;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public void setEndTime(long endTime2) {
        this.endTime = endTime2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }
}
