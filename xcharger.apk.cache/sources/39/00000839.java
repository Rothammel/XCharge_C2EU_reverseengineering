package com.xcharge.charger.ui.api.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

/* loaded from: classes.dex */
public class UICtrlMessage extends JsonBean<UICtrlMessage> {
    public static final String ACTION_UI_CTRL = "com.xcharge.charger.ui.api.ACTION_UI_CTRL";
    public static final String OPR_CREATE = "create";
    public static final String OPR_DESTROY = "destroy";
    public static final String OPR_ERROR = "error";
    public static final String OPR_HIDE = "hide";
    public static final String OPR_KEY_DOWN = "down";
    public static final String OPR_KEY_UP = "up";
    public static final String OPR_NONE = "none";
    public static final String OPR_PAUSE = "pause";
    public static final String OPR_RESTART = "restart";
    public static final String OPR_RESUME = "resume";
    public static final String OPR_SATRT = "start";
    public static final String OPR_SKIP = "skip";
    public static final String OPR_STOP = "stop";
    public static final String OPR_TRANSFER = "transfer";
    public static final String OPR_UPDATE = "update";
    public static final String OPR_VIEW = "view";
    private String type = null;
    private String subType = null;
    private String activity = null;
    private String name = null;
    private String opr = null;
    private HashMap<String, Object> data = null;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubType() {
        return this.subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getActivity() {
        return this.activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOpr() {
        return this.opr;
    }

    public void setOpr(String opr) {
        this.opr = opr;
    }

    public HashMap<String, Object> getData() {
        return this.data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }
}