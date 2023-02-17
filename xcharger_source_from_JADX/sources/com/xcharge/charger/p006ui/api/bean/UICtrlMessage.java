package com.xcharge.charger.p006ui.api.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

/* renamed from: com.xcharge.charger.ui.api.bean.UICtrlMessage */
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
    private String activity = null;
    private HashMap<String, Object> data = null;
    private String name = null;
    private String opr = null;
    private String subType = null;
    private String type = null;

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
    }

    public String getSubType() {
        return this.subType;
    }

    public void setSubType(String subType2) {
        this.subType = subType2;
    }

    public String getActivity() {
        return this.activity;
    }

    public void setActivity(String activity2) {
        this.activity = activity2;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
    }

    public String getOpr() {
        return this.opr;
    }

    public void setOpr(String opr2) {
        this.opr = opr2;
    }

    public HashMap<String, Object> getData() {
        return this.data;
    }

    public void setData(HashMap<String, Object> data2) {
        this.data = data2;
    }
}
