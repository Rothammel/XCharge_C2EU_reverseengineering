package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.charger.core.type.FIN_MODE;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class FinCause extends JsonBean<FinCause> {
    private FIN_MODE mode = null;
    private ErrorCode error = null;

    public FIN_MODE getMode() {
        return this.mode;
    }

    public void setMode(FIN_MODE mode) {
        this.mode = mode;
    }

    public ErrorCode getError() {
        return this.error;
    }

    public void setError(ErrorCode error) {
        this.error = error;
    }
}
