package com.xcharge.charger.p006ui.p009c2.activity.charge.online;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.data.p004db.ContentDB;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.p006ui.api.UIEventMessageProxy;
import com.xcharge.charger.p006ui.api.bean.UICtrlMessage;
import com.xcharge.charger.p006ui.api.bean.UIEventMessage;
import com.xcharge.charger.p006ui.p009c2.activity.BaseActivity;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;
import com.xcharge.charger.p006ui.p009c2.activity.widget.LoadingDialog;
import java.util.HashMap;

/* renamed from: com.xcharge.charger.ui.c2.activity.charge.online.WaittingStartChargeActivity */
public class WaittingStartChargeActivity extends BaseActivity {
    private final int MSG_DISMISS_DIALOG = 3;
    private final int MSG_SHOW_DIALOG = 2;
    private final int MSG_TIME_REPEAT = 4;
    private final int MSG_WAITTING_START_CHARGE = 1;
    private int curTimerTime;
    private LinearLayout ll_timer;
    /* access modifiers changed from: private */
    public LoadingDialog loadingDialog;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    WaittingStartChargeActivity.this.mHandler.removeMessages(1);
                    WaittingStartChargeActivity waittingStartChargeActivity = WaittingStartChargeActivity.this;
                    waittingStartChargeActivity.waittingStartChargeTime = waittingStartChargeActivity.waittingStartChargeTime - 1;
                    if (WaittingStartChargeActivity.this.waittingStartChargeTime >= 0) {
                        WaittingStartChargeActivity.this.loadingDialog.changeLoadingText(WaittingStartChargeActivity.this.getString(C0221R.string.waitting_gun_connect, new Object[]{Integer.valueOf(WaittingStartChargeActivity.this.waittingStartChargeTime)}));
                        sendEmptyMessageDelayed(1, 1000);
                        return;
                    }
                    return;
                case 2:
                    if (WaittingStartChargeActivity.this.loadingDialog == null) {
                        WaittingStartChargeActivity.this.loadingDialog = LoadingDialog.createDialog(WaittingStartChargeActivity.this, WaittingStartChargeActivity.this.getString(C0221R.string.waitting_gun_connect, new Object[]{Integer.valueOf(WaittingStartChargeActivity.this.waittingStartChargeTime)}));
                    }
                    WaittingStartChargeActivity.this.loadingDialog.changeLoadingText(WaittingStartChargeActivity.this.getString(C0221R.string.waitting_gun_connect, new Object[]{Integer.valueOf(WaittingStartChargeActivity.this.waittingStartChargeTime)}));
                    if (!WaittingStartChargeActivity.this.loadingDialog.isShowing()) {
                        WaittingStartChargeActivity.this.loadingDialog.show();
                    }
                    sendEmptyMessageDelayed(1, 1000);
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
    private TextView tv_bottom;
    private TextView tv_hint;
    private TextView tv_second;
    private TextView tv_timer;
    /* access modifiers changed from: private */
    public int waittingStartChargeTime;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0221R.layout.activity_waitting_start_charge);
        UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), UIEventMessage.TYPE_UI_ACTIVITY, (String) null, getClass().getName(), "create", (HashMap) null);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        Utils.setPermitNFC(false, false, false, false);
    }

    /* access modifiers changed from: protected */
    public void initView() {
        this.ll_timer = (LinearLayout) findViewById(C0221R.C0223id.ll_timer);
        this.tv_timer = (TextView) findViewById(C0221R.C0223id.tv_timer);
        this.tv_second = (TextView) findViewById(C0221R.C0223id.tv_second);
        this.tv_hint = (TextView) findViewById(C0221R.C0223id.tv_hint);
        this.tv_bottom = (TextView) findViewById(C0221R.C0223id.tv_bottom);
        if (CountrySettingCacheProvider.getInstance().isSetRTL()) {
            this.ll_timer.setLayoutDirection(1);
        } else {
            this.ll_timer.setLayoutDirection(0);
        }
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

    /* access modifiers changed from: private */
    public void waittingConnectTimer() {
        this.mHandler.removeMessages(4);
        this.tv_timer.setText(String.format("%02d", new Object[]{Integer.valueOf(this.curTimerTime)}));
        this.tv_second.setText(C0221R.string.unit_second);
        this.curTimerTime--;
        if (this.curTimerTime >= 0) {
            this.mHandler.sendEmptyMessageDelayed(4, 1000);
        }
    }

    private void updateBottomView(String isGun) {
        if ("yes".equals(isGun)) {
            this.tv_hint.setText(C0221R.string.wait_charge_hint);
            this.tv_bottom.setText(C0221R.string.bottom_hint);
        } else if ("no".equals(isGun)) {
            this.tv_bottom.setText(C0221R.string.waitting_start_hint_text);
        }
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        Log.d("WaittingStartChargeActivity", "onResume");
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        Log.d("WaittingStartChargeActivity", "onPause");
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        Log.d("WaittingStartChargeActivity", "onStop");
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        Log.d("WaittingStartChargeActivity", "onDestroy");
        if (this.loadingDialog != null && this.loadingDialog.isShowing()) {
            this.loadingDialog.dismiss();
        }
        this.mHandler.removeMessages(4);
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(3);
        this.mHandler.removeMessages(1);
        this.mHandler.removeCallbacksAndMessages((Object) null);
    }

    public void onBackPressed() {
        UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), "key", (String) null, getClass().getName(), "up", (HashMap) null);
    }
}
