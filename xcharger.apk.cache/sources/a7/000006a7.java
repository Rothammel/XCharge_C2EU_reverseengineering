package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.LocalChargeBill;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class ReportLocalChargeBill extends JsonBean<ReportLocalChargeBill> {
    private Long sid = null;
    private ArrayList<LocalChargeBill> chargeBills = new ArrayList<>();
    private long time = 0;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public ArrayList<LocalChargeBill> getChargeBills() {
        return this.chargeBills;
    }

    public void setChargeBills(ArrayList<LocalChargeBill> chargeBills) {
        this.chargeBills = chargeBills;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}