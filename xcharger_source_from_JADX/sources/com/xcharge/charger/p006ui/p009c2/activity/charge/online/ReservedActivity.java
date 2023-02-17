package com.xcharge.charger.p006ui.p009c2.activity.charge.online;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.p006ui.api.UIEventMessageProxy;
import com.xcharge.charger.p006ui.api.bean.UICtrlMessage;
import com.xcharge.charger.p006ui.api.bean.UIEventMessage;
import com.xcharge.charger.p006ui.p009c2.activity.BaseActivity;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;
import com.xcharge.common.utils.TimeUtils;
import java.util.HashMap;

/* renamed from: com.xcharge.charger.ui.c2.activity.charge.online.ReservedActivity */
public class ReservedActivity extends BaseActivity {
    private ImageView iv_status;
    private TextView tv_status;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0221R.layout.activity_base_status);
        UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), UIEventMessage.TYPE_UI_ACTIVITY, (String) null, getClass().getName(), "create", (HashMap) null);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        Utils.setPermitNFC(false, false, false, false);
    }

    /* access modifiers changed from: protected */
    public void initView() {
        this.iv_status = (ImageView) findViewById(C0221R.C0223id.iv_status_one);
        this.iv_status.setImageResource(C0221R.C0222drawable.ic_waitting);
        this.tv_status = (TextView) findViewById(C0221R.C0223id.tv_status_one);
    }

    /* access modifiers changed from: protected */
    public void onUICtrlReceived(UICtrlMessage msg) {
        super.onUICtrlReceived(msg);
        String activity = msg.getActivity();
        if (!TextUtils.isEmpty(activity) && getClass().getName().equals(activity)) {
            String type = msg.getType();
            String subType = msg.getSubType();
            String name = msg.getName();
            String opr = msg.getOpr();
            HashMap<String, Object> data = msg.getData();
            if (UIEventMessage.TYPE_UI_ELEMENT.equals(type) && "initReserved".equals(name) && "update".equals(opr)) {
                this.tv_status.setText(getString(C0221R.string.reserved_hint_text, new Object[]{TimeUtils.getHHmmFormat(Long.valueOf((String) data.get("reservedTime")).longValue())}));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        Log.d("ReservedActivity", "onResume");
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        Log.d("ReservedActivity", "onPause");
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        Log.d("ReservedActivity", "onStop");
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        Log.d("ReservedActivity", "onDestroy");
    }

    /* access modifiers changed from: protected */
    public void keepScreenOn() {
    }

    public void onBackPressed() {
    }
}
