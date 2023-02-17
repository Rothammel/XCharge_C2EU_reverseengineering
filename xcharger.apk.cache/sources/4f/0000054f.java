package com.xcharge.charger.core.api.bean.cap;

import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

/* loaded from: classes.dex */
public class ConditionDirective extends JsonBean<ConditionDirective> {
    public static final String CONDITION_USER_RESERVE = "user_reserve";
    public static final String CONDITION_USER_STOP = "user_stop";
    private USER_TC_TYPE userTcType = null;
    private String userTcValue = null;
    private Long reserveTime = null;
    private HashMap<String, Object> attach = null;

    public USER_TC_TYPE getUserTcType() {
        return this.userTcType;
    }

    public void setUserTcType(USER_TC_TYPE userTcType) {
        this.userTcType = userTcType;
    }

    public String getUserTcValue() {
        return this.userTcValue;
    }

    public void setUserTcValue(String userTcValue) {
        this.userTcValue = userTcValue;
    }

    public Long getReserveTime() {
        return this.reserveTime;
    }

    public void setReserveTime(Long reserveTime) {
        this.reserveTime = reserveTime;
    }

    public HashMap<String, Object> getAttach() {
        return this.attach;
    }

    public void setAttach(HashMap<String, Object> attach) {
        this.attach = attach;
    }
}