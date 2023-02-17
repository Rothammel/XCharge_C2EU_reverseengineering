package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.LocalChargeBill;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

public class ReportLocalChargeBill extends JsonBean<ReportLocalChargeBill> {
    private ArrayList<LocalChargeBill> chargeBills = new ArrayList<>();
    private Long sid = null;
    private long time = 0;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid2) {
        this.sid = sid2;
    }

    public ArrayList<LocalChargeBill> getChargeBills() {
        return this.chargeBills;
    }

    public void setChargeBills(ArrayList<LocalChargeBill> chargeBills2) {
        this.chargeBills = chargeBills2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }
}
