package com.xcharge.charger.ui.c2.activity.charge.online;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xcharge.charger.R;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.ui.api.UIEventMessageProxy;
import com.xcharge.charger.ui.api.bean.UICtrlMessage;
import com.xcharge.charger.ui.api.bean.UIEventMessage;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;
import com.xcharge.charger.ui.c2.activity.widget.LoadingDialog;
import java.util.HashMap;

/* loaded from: classes.dex */
public class WaittingStartChargeActivity extends BaseActivity {
    private int curTimerTime;
    private LinearLayout ll_timer;
    private LoadingDialog loadingDialog;
    private TextView tv_bottom;
    private TextView tv_hint;
    private TextView tv_second;
    private TextView tv_timer;
    private int waittingStartChargeTime;
    private final int MSG_WAITTING_START_CHARGE = 1;
    private final int MSG_SHOW_DIALOG = 2;
    private final int MSG_DISMISS_DIALOG = 3;
    private final int MSG_TIME_REPEAT = 4;
    Handler mHandler = new Handler() { // from class: com.xcharge.charger.ui.c2.activity.charge.online.WaittingStartChargeActivity.1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    WaittingStartChargeActivity.this.mHandler.removeMessages(1);
                    WaittingStartChargeActivity waittingStartChargeActivity = WaittingStartChargeActivity.this;
                    waittingStartChargeActivity.waittingStartChargeTime--;
                    if (WaittingStartChargeActivity.this.waittingStartChargeTime >= 0) {
                        WaittingStartChargeActivity.this.loadingDialog.changeLoadingText(WaittingStartChargeActivity.this.getString(R.string.waitting_gun_connect, new Object[]{Integer.valueOf(WaittingStartChargeActivity.this.waittingStartChargeTime)}));
                        sendEmptyMessageDelayed(1, 1000L);
                        return;
                    }
                    return;
                case 2:
                    if (WaittingStartChargeActivity.this.loadingDialog == null) {
                        WaittingStartChargeActivity.this.loadingDialog = LoadingDialog.createDialog(WaittingStartChargeActivity.this, WaittingStartChargeActivity.this.getString(R.string.waitting_gun_connect, new Object[]{Integer.valueOf(WaittingStartChargeActivity.this.waittingStartChargeTime)}));
                    }
                    WaittingStartChargeActivity.this.loadingDialog.changeLoadingText(WaittingStartChargeActivity.this.getString(R.string.waitting_gun_connect, new Object[]{Integer.valueOf(WaittingStartChargeActivity.this.waittingStartChargeTime)}));
                    if (!WaittingStartChargeActivity.this.loadingDialog.isShowing()) {
                        WaittingStartChargeActivity.this.loadingDialog.show();
                    }
                    sendEmptyMessageDelayed(1, 1000L);
                    return;
                case 3:
                    if (WaittingStartChargeActivity.this.loadingDialog != null && WaittingStartChargeActivity.this.loadingDialog.isShowing()) {
                        WaittingStartChargeActivity.this.loadingDialog.dismiss();
                        return;
                    }
                    return;
                case 4:
                    WaittingStartChargeActivity.this.waittingConnectTimer();
                    return;
                default:
                    return;
            }
        }
    };

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waitting_start_charge);
        UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, getClass().getName(), "create", null);
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        Utils.setPermitNFC(false, false, false, false);
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void initView() {
        this.ll_timer = (LinearLayout) findViewById(R.id.ll_timer);
        this.tv_timer = (TextView) findViewById(R.id.tv_timer);
        this.tv_second = (TextView) findViewById(R.id.tv_second);
        this.tv_hint = (TextView) findViewById(R.id.tv_hint);
        this.tv_bottom = (TextView) findViewById(R.id.tv_bottom);
        if (CountrySettingCacheProvider.getInstance().isSetRTL()) {
            this.ll_timer.setLayoutDirection(1);
        } else {
            this.ll_timer.setLayoutDirection(0);
        }
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void onUICtrlReceived(UICtrlMessage msg) {
        super.onUICtrlReceived(msg);
        String activity = msg.getActivity();
        if (!TextUtils.isEmpty(activity) && getClass().getName().equals(activity)) {
            String type = msg.getType();
            String subType = msg.getSubType();
            String name = msg.getName();
            String opr = msg.getOpr();
            HashMap<String, Object> data = msg.getData();
            if (UIEventMessage.TYPE_UI_ELEMENT.equals(type)) {
                if (UIEventMessage.SUBTYPE_UI_LOADING_DIALOG.equals(subType) && "mLoadingDialog".equals(name) && "update".equals(opr)) {
                    this.waittingStartChargeTime = Integer.parseInt((String) data.get("waitStart"));
                    if ("show".equals((String) data.get(ContentDB.AuthInfoTable.STATUS))) {
                        this.mHandler.sendEmptyMessage(2);
                    } else if ("dismiss".equals((String) data.get(ContentDB.AuthInfoTable.STATUS))) {
                        this.mHandler.sendEmptyMessage(3);
                    }
                }
                if ("initWaittingStartCharge".equals(name) && "update".equals(opr)) {
                    this.curTimerTime = Integer.parseInt((String) data.get("pluginTime"));
                    this.mHandler.sendEmptyMessage(4);
                    updateBottomView((String) data.get("isGun"));
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void waittingConnectTimer() {
        this.mHandler.removeMessages(4);
        this.tv_timer.setText(String.format("%02d", Integer.valueOf(this.curTimerTime)));
        this.tv_second.setText(R.string.unit_second);
        this.curTimerTime--;
        if (this.curTimerTime >= 0) {
            this.mHandler.sendEmptyMessageDelayed(4, 1000L);
        }
    }

    private void updateBottomView(String isGun) {
        if ("yes".equals(isGun)) {
            this.tv_hint.setText(R.string.wait_charge_hint);
            this.tv_bottom.setText(R.string.bottom_hint);
        } else if ("no".equals(isGun)) {
            this.tv_bottom.setText(R.string.waitting_start_hint_text);
        }
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        Log.d("WaittingStartChargeActivity", "onResume");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        Log.d("WaittingStartChargeActivity", "onPause");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        Log.d("WaittingStartChargeActivity", "onStop");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        Log.d("WaittingStartChargeActivity", "onDestroy");
        if (this.loadingDialog != null && this.loadingDialog.isShowing()) {
            this.loadingDialog.dismiss();
        }
        this.mHandler.removeMessages(4);
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(3);
        this.mHandler.removeMessages(1);
        this.mHandler.removeCallbacksAndMessages(null);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), "key", null, getClass().getName(), "up", null);
    }
}
