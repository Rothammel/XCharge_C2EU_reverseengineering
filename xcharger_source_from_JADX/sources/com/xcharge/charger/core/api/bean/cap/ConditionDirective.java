package com.xcharge.charger.core.api.bean.cap;

import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

public class ConditionDirective extends JsonBean<ConditionDirective> {
    public static final String CONDITION_USER_RESERVE = "user_reserve";
    public static final String CONDITION_USER_STOP = "user_stop";
    private HashMap<String, Object> attach = null;
    private Long reserveTime = null;
    private USER_TC_TYPE userTcType = null;
    private String userTcValue = null;

    public USER_TC_TYPE getUserTcType() {
        return this.userTcType;
    }

    public void setUserTcType(USER_TC_TYPE userTcType2) {
        this.userTcType = userTcType2;
    }

    public String getUserTcValue() {
        return this.userTcValue;
    }

    public void setUserTcValue(String userTcValue2) {
        this.userTcValue = userTcValue2;
    }

    public Long getReserveTime() {
        return this.reserveTime;
    }

    public void setReserveTime(Long reserveTime2) {
        this.reserveTime = reserveTime2;
    }

    public HashMap<String, Object> getAttach() {
        return this.attach;
    }

    public void setAttach(HashMap<String, Object> attach2) {
        this.attach = attach2;
    }
}
