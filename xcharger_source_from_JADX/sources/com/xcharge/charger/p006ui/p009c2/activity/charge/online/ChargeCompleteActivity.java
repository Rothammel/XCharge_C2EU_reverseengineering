package com.xcharge.charger.p006ui.p009c2.activity.charge.online;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.p006ui.api.UIEventMessageProxy;
import com.xcharge.charger.p006ui.api.bean.UICtrlMessage;
import com.xcharge.charger.p006ui.api.bean.UIEventMessage;
import com.xcharge.charger.p006ui.p009c2.activity.BaseActivity;
import com.xcharge.charger.p006ui.p009c2.activity.data.Variate;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;
import java.util.HashMap;

/* renamed from: com.xcharge.charger.ui.c2.activity.charge.online.ChargeCompleteActivity */
public class ChargeCompleteActivity extends BaseActivity {
    /* access modifiers changed from: private */
    public long curTimeout = 0;
    private boolean isDelay = false;
    private long lockTimestamp = 0;
    Handler mHandler = new Handler();
    Runnable runnable = new Runnable() {
        public void run() {
            try {
                ChargeCompleteActivity.this.mHandler.postDelayed(this, 1000);
                ChargeCompleteActivity chargeCompleteActivity = ChargeCompleteActivity.this;
                chargeCompleteActivity.curTimeout = chargeCompleteActivity.curTimeout - 1;
                ChargeCompleteActivity.this.initView();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private TextView tv_bottom1;
    private TextView tv_bottom2;
    private TextView tv_hint;
    private TextView tv_title;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0221R.layout.activity_charge_complete);
        UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), UIEventMessage.TYPE_UI_ACTIVITY, (String) null, getClass().getName(), "create", (HashMap) null);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        Utils.setPermitNFC(false, false, false, false);
    }

    /* access modifiers changed from: protected */
    public void initView() {
        int countDown;
        this.tv_title = (TextView) findViewById(C0221R.C0223id.tv_title);
        this.tv_hint = (TextView) findViewById(C0221R.C0223id.tv_hint);
        this.tv_bottom1 = (TextView) findViewById(C0221R.C0223id.tv_bottom1);
        this.tv_bottom2 = (TextView) findViewById(C0221R.C0223id.tv_bottom2);
        this.tv_title.setText(getString(C0221R.string.chargecomplete_text));
        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus("1");
        if (portStatus != null) {
            this.tv_bottom1.setText(getString(C0221R.string.chargecomplete_hint_text1, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(portStatus.getDelayPrice())), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
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
                this.tv_hint.setText(getString(C0221R.string.chargecomplete_drawgun_not_delayfee_hint, new Object[]{this.sdf.format(Long.valueOf(chargeBill.getStop_time())), Integer.valueOf(countDown)}));
            } else {
                this.tv_bottom1.setVisibility(4);
                this.tv_hint.setText(getString(C0221R.string.chargecomplete_drawgun_delayfee_hint, new Object[]{this.sdf.format(Long.valueOf(chargeBill.getStop_time()))}));
            }
        }
        this.tv_bottom2.setText(C0221R.string.chargecomplete_hint_text2);
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
            if (UIEventMessage.TYPE_UI_ELEMENT.equals(type) && "initChargeComplete".equals(name) && "update".equals(opr)) {
                this.isDelay = ((Boolean) data.get("willDelayHandleNow")).booleanValue();
                if (this.isDelay) {
                    this.curTimeout = Long.valueOf((String) data.get("plugoutTime")).longValue();
                    this.mHandler.postDelayed(this.runnable, 1000);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        Log.d("ChargeCompleteActivity", "onResume");
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        Log.d("ChargeCompleteActivity", "onPause");
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        Log.d("ChargeCompleteActivity", "onStop");
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        Log.d("ChargeCompleteActivity", "onDestroy");
        this.mHandler.removeCallbacksAndMessages((Object) null);
        this.mHandler.removeCallbacks(this.runnable);
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() - this.lockTimestamp > 2000) {
            DCAPProxy.getInstance().gunLockCtrl("1", 0, LOCK_STATUS.unlock);
            this.lockTimestamp = System.currentTimeMillis();
        }
    }

    /* access modifiers changed from: protected */
    public void keepScreenOn() {
        if (!PLATFORM_CUSTOMER.anyo_private.equals(SystemSettingCacheProvider.getInstance().getPlatformCustomer())) {
            getWindow().setFlags(128, 128);
        }
    }
}
