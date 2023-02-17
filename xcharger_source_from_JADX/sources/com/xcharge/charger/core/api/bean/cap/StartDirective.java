package com.xcharge.charger.core.api.bean.cap;

import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.common.bean.JsonBean;

public class StartDirective extends JsonBean<StartDirective> {
    private USER_TC_TYPE user_tc_type = null;
    private String user_tc_value = null;

    public USER_TC_TYPE getUser_tc_type() {
        return this.user_tc_type;
    }

    public void setUser_tc_type(USER_TC_TYPE user_tc_type2) {
        this.user_tc_type = user_tc_type2;
    }

    public String getUser_tc_value() {
        return this.user_tc_value;
    }

    public void setUser_tc_value(String user_tc_value2) {
        this.user_tc_value = user_tc_value2;
    }
}
