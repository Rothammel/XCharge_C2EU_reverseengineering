package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class DAPAdvsite extends JsonBean<DAPAdvsite> {
    private ArrayList<Advsite> content = null;

    public ArrayList<Advsite> getContent() {
        return this.content;
    }

    public void setContent(ArrayList<Advsite> content) {
        this.content = content;
    }
}
