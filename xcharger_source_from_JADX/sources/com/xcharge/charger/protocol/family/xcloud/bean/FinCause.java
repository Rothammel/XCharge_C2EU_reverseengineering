package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.charger.core.type.FIN_MODE;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.common.bean.JsonBean;

public class FinCause extends JsonBean<FinCause> {
    private ErrorCode error = null;
    private FIN_MODE mode = null;

    public FIN_MODE getMode() {
        return this.mode;
    }

    public void setMode(FIN_MODE mode2) {
        this.mode = mode2;
    }

    public ErrorCode getError() {
        return this.error;
    }

    public void setError(ErrorCode error2) {
        this.error = error2;
    }
}
