package com.xcharge.charger.ui.c2.activity.charge.online;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import com.xcharge.charger.R;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.ui.api.UIEventMessageProxy;
import com.xcharge.charger.ui.api.bean.UICtrlMessage;
import com.xcharge.charger.ui.api.bean.UIEventMessage;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.ui.c2.activity.data.Variate;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;
import java.util.HashMap;

/* loaded from: classes.dex */
public class ChargeCompleteActivity extends BaseActivity {
    private TextView tv_bottom1;
    private TextView tv_bottom2;
    private TextView tv_hint;
    private TextView tv_title;
    private long curTimeout = 0;
    private long lockTimestamp = 0;
    private boolean isDelay = false;
    Handler mHandler = new Handler();
    Runnable runnable = new Runnable() { // from class: com.xcharge.charger.ui.c2.activity.charge.online.ChargeCompleteActivity.1
        @Override // java.lang.Runnable
        public void run() {
            try {
                ChargeCompleteActivity.this.mHandler.postDelayed(this, 1000L);
                ChargeCompleteActivity.this.curTimeout--;
                ChargeCompleteActivity.this.initView();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge_complete);
        UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, getClass().getName(), "create", null);
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        Utils.setPermitNFC(false, false, false, false);
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void initView() {
        int countDown;
        this.tv_title = (TextView) findViewById(R.id.tv_title);
        this.tv_hint = (TextView) findViewById(R.id.tv_hint);
        this.tv_bottom1 = (TextView) findViewById(R.id.tv_bottom1);
        this.tv_bottom2 = (TextView) findViewById(R.id.tv_bottom2);
        this.tv_title.setText(getString(R.string.chargecomplete_text));
        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus("1");
        if (portStatus != null) {
            this.tv_bottom1.setText(getString(R.string.chargecomplete_hint_text1, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(portStatus.getDelayPrice())), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
        }
        ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(Variate.getInstance().getChargeId());
        if (chargeBill != null) {
            if (this.isDelay) {
                this.tv_bottom1.setVisibility(0);
                if (this.curTimeout % 60 == 0) {
                    countDown = (int) (this.curTimeout / 60);
                } else {
                    countDown = ((int) (this.curTimeout / 60)) + 1;
                }
                this.tv_hint.setText(getString(R.string.chargecomplete_drawgun_not_delayfee_hint, new Object[]{this.sdf.format(Long.valueOf(chargeBill.getStop_time())), Integer.valueOf(countDown)}));
            } else {
                this.tv_bottom1.setVisibility(4);
                this.tv_hint.setText(getString(R.string.chargecomplete_drawgun_delayfee_hint, new Object[]{this.sdf.format(Long.valueOf(chargeBill.getStop_time()))}));
            }
        }
        this.tv_bottom2.setText(R.string.chargecomplete_hint_text2);
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
            if (UIEventMessage.TYPE_UI_ELEMENT.equals(type) && "initChargeComplete".equals(name) && "update".equals(opr)) {
                this.isDelay = ((Boolean) data.get("willDelayHandleNow")).booleanValue();
                if (this.isDelay) {
                    this.curTimeout = Long.valueOf((String) data.get("plugoutTime")).longValue();
                    this.mHandler.postDelayed(this.runnable, 1000L);
                }
            }
        }
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        Log.d("ChargeCompleteActivity", "onResume");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        Log.d("ChargeCompleteActivity", "onPause");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        Log.d("ChargeCompleteActivity", "onStop");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ChargeCompleteActivity", "onDestroy");
        this.mHandler.removeCallbacksAndMessages(null);
        this.mHandler.removeCallbacks(this.runnable);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        if (System.currentTimeMillis() - this.lockTimestamp > 2000) {
            DCAPProxy.getInstance().gunLockCtrl("1", 0, LOCK_STATUS.unlock);
            this.lockTimestamp = System.currentTimeMillis();
        }
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void keepScreenOn() {
        if (!PLATFORM_CUSTOMER.anyo_private.equals(SystemSettingCacheProvider.getInstance().getPlatformCustomer())) {
            getWindow().setFlags(128, 128);
        }
    }
}