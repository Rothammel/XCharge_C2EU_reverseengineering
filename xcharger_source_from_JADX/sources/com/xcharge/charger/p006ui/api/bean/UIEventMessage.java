package com.xcharge.charger.p006ui.api.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

/* renamed from: com.xcharge.charger.ui.api.bean.UIEventMessage */
public class UIEventMessage extends JsonBean<UIEventMessage> {
    public static final String ACTION_UI_EVENT = "com.xcharge.charger.ui.api.ACTION_UI_EVENT";
    public static final String KEY_HOME = "home";
    public static final String STATUS_ACTIVITY_CREATE = "create";
    public static final String STATUS_ACTIVITY_DESTROY = "destroy";
    public static final String STATUS_ACTIVITY_NONE = "none";
    public static final String STATUS_ACTIVITY_PAUSE = "pause";
    public static final String STATUS_ACTIVITY_RESTART = "restart";
    public static final String STATUS_ACTIVITY_RESUME = "resume";
    public static final String STATUS_ACTIVITY_START = "start";
    public static final String STATUS_ACTIVITY_STOP = "stop";
    public static final String STATUS_ELEMENT_CREATE = "create";
    public static final String STATUS_ELEMENT_DESTROY = "destroy";
    public static final String STATUS_ELEMENT_HIDE = "hide";
    public static final String STATUS_ELEMENT_NONE = "none";
    public static final String STATUS_ELEMENT_VIEW = "view";
    public static final String STATUS_KEY_DOWN = "down";
    public static final String STATUS_KEY_UP = "up";
    public static final String SUBTYPE_UI_AUTO_SCROLL_VIEW_PAGER = "auto_scroll_view_pager";
    public static final String SUBTYPE_UI_BUBBLE_VIEW = "bubble_view";
    public static final String SUBTYPE_UI_IMAGE_VIEW = "image_view";
    public static final String SUBTYPE_UI_LOADING_DIALOG = "loading_dialog";
    public static final String SUBTYPE_UI_SHOW_INFO_DIALOG = "show_info_dialog";
    public static final String SUBTYPE_UI_TEXT_VIEW = "text_view";
    public static final String SUBTYPE_UI_VIEW_GROUP = "view_group";
    public static final String TYPE_UI_ACTIVITY = "activity";
    public static final String TYPE_UI_ELEMENT = "element";
    public static final String TYPE_UI_KEY = "key";
    private String activity = null;
    private HashMap<String, Object> data = null;
    private String name = null;
    private String status = null;
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

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status2) {
        this.status = status2;
    }

    public HashMap<String, Object> getData() {
        return this.data;
    }

    public void setData(HashMap<String, Object> data2) {
        this.data = data2;
    }
}
