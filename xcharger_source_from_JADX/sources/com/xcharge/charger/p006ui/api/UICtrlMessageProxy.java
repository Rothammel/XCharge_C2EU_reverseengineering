package com.xcharge.charger.p006ui.api;

import android.content.Context;
import android.content.Intent;
import android.support.p000v4.content.LocalBroadcastManager;
import android.util.Log;
import com.xcharge.charger.p006ui.api.bean.UICtrlMessage;
import java.util.HashMap;

/* renamed from: com.xcharge.charger.ui.api.UICtrlMessageProxy */
public class UICtrlMessageProxy {
    private static UICtrlMessageProxy insatnce = null;
    private Context context = null;

    public static UICtrlMessageProxy getInstance() {
        if (insatnce == null) {
            insatnce = new UICtrlMessageProxy();
        }
        return insatnce;
    }

    public void init(Context context2) {
        this.context = context2;
    }

    public void destroy() {
    }

    public boolean sendCtrl(UICtrlMessage ctrl) {
        try {
            Intent intent = new Intent(UICtrlMessage.ACTION_UI_CTRL);
            intent.putExtra("body", ctrl.toJson());
            LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
            return true;
        } catch (Exception e) {
            Log.e("UICtrlMessageProxy.sendCtrl", Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean sendCtrl(String activity, String type, String subtype, String name, String opr, HashMap data) {
        UICtrlMessage ctrl = new UICtrlMessage();
        ctrl.setActivity(activity);
        ctrl.setType(type);
        ctrl.setSubType(subtype);
        ctrl.setName(name);
        ctrl.setOpr(opr);
        ctrl.setData(data);
        return sendCtrl(ctrl);
    }
}
