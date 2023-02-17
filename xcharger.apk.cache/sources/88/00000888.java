package com.xcharge.charger.ui.c2.activity.charge.online;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.xcharge.charger.R;
import com.xcharge.charger.ui.api.UIEventMessageProxy;
import com.xcharge.charger.ui.api.bean.UICtrlMessage;
import com.xcharge.charger.ui.api.bean.UIEventMessage;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;
import com.xcharge.common.utils.TimeUtils;
import java.util.HashMap;

/* loaded from: classes.dex */
public class ReservedActivity extends BaseActivity {
    private ImageView iv_status;
    private TextView tv_status;

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_status);
        UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, getClass().getName(), "create", null);
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        Utils.setPermitNFC(false, false, false, false);
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void initView() {
        this.iv_status = (ImageView) findViewById(R.id.iv_status_one);
        this.iv_status.setImageResource(R.drawable.ic_waitting);
        this.tv_status = (TextView) findViewById(R.id.tv_status_one);
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void onUICtrlReceived(UICtrlMessage msg) {
        super.onUICtrlReceived(msg);
        String activity = msg.getActivity();
        if (!TextUtils.isEmpty(activity) && getClass().getName().equals(activity)) {
            String type = msg.getType();
            msg.getSubType();
            String name = msg.getName();
            String opr = msg.getOpr();
            HashMap<String, Object> data = msg.getData();
            if (UIEventMessage.TYPE_UI_ELEMENT.equals(type) && "initReserved".equals(name) && "update".equals(opr)) {
                this.tv_status.setText(getString(R.string.reserved_hint_text, new Object[]{TimeUtils.getHHmmFormat(Long.valueOf((String) data.get("reservedTime")).longValue())}));
            }
        }
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        Log.d("ReservedActivity", "onResume");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        Log.d("ReservedActivity", "onPause");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        Log.d("ReservedActivity", "onStop");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ReservedActivity", "onDestroy");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void keepScreenOn() {
    }

    @Override // android.app.Activity
    public void onBackPressed() {
    }
}