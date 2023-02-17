package com.xcharge.charger.ui.c2.activity.charge.online;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xcharge.charger.R;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.ui.c2.activity.data.Variate;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;

/* loaded from: classes.dex */
public class ChargeCostActivity extends BaseActivity {
    private LinearLayout ll_fee;
    private LinearLayout ll_power;
    private Handler mHandler = new Handler();
    private Runnable paidTimeoutTask = new Runnable() { // from class: com.xcharge.charger.ui.c2.activity.charge.online.ChargeCostActivity.1
        @Override // java.lang.Runnable
        public void run() {
            Utils.skipNfcQrcode(ChargeCostActivity.this);
            ChargeCostActivity.this.finish();
        }
    };
    private TextView tv_bottom;
    private TextView tv_fee;
    private TextView tv_fee_unit;
    private TextView tv_free;
    private TextView tv_power;
    private TextView tv_timer;
    private TextView tv_title;

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge_cost);
        this.mHandler.postDelayed(this.paidTimeoutTask, 10000L);
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        Utils.setPermitNFC(false, false, false, false);
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void initView() {
        this.tv_title = (TextView) findViewById(R.id.tv_title);
        this.ll_fee = (LinearLayout) findViewById(R.id.ll_fee);
        this.tv_fee = (TextView) findViewById(R.id.tv_fee);
        this.tv_fee_unit = (TextView) findViewById(R.id.tv_fee_unit);
        this.tv_free = (TextView) findViewById(R.id.tv_free);
        this.tv_timer = (TextView) findViewById(R.id.tv_timer);
        this.ll_power = (LinearLayout) findViewById(R.id.ll_power);
        this.tv_power = (TextView) findViewById(R.id.tv_power);
        this.tv_bottom = (TextView) findViewById(R.id.tv_bottom);
        if (CountrySettingCacheProvider.getInstance().isSetRTL()) {
            this.ll_fee.setLayoutDirection(1);
            this.ll_power.setLayoutDirection(1);
        } else {
            this.ll_fee.setLayoutDirection(0);
            this.ll_power.setLayoutDirection(0);
        }
        ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(Variate.getInstance().getChargeId());
        if (chargeBill != null) {
            if (CHARGE_PLATFORM.anyo.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
                this.tv_title.setText(getString(R.string.chargecomplete_text));
                this.tv_fee.setText("");
                this.tv_fee_unit.setText(getString(R.string.clearing_hint_text));
                this.tv_bottom.setText("");
            } else {
                this.tv_title.setText(getString(R.string.charge_cost_text));
                this.tv_fee.setText(CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(chargeBill.getTotal_fee() / 100.0d)));
                this.tv_fee_unit.setText(CountrySettingCacheProvider.getInstance().getMoneyDisp());
                switch (chargeBill.getPay_flag()) {
                    case -1:
                        this.tv_bottom.setText(R.string.clearing_hint_text);
                        break;
                    case 0:
                        this.tv_bottom.setText(R.string.clearing_hint_text);
                        break;
                    case 1:
                        this.tv_title.setText(getString(R.string.personal_cost_title));
                        this.tv_bottom.setText(R.string.paycomplete_welcome);
                        break;
                }
            }
            switch (chargeBill.getIs_free()) {
                case -1:
                    this.tv_free.setVisibility(8);
                    break;
                case 0:
                    this.tv_free.setVisibility(8);
                    break;
                case 1:
                    this.tv_free.setVisibility(0);
                    break;
            }
            this.tv_timer.setText(Utils.formatTime(chargeBill.getStop_time() - chargeBill.getStart_time()));
            this.tv_power.setText(CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(chargeBill.getTotal_power())));
        }
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        Log.d("ChargeCostActivity", "onResume");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        Log.d("ChargeCostActivity", "onPause");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        Log.d("ChargeCostActivity", "onStop");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ChargeCostActivity", "onDestroy");
        this.mHandler.removeCallbacksAndMessages(null);
        this.mHandler.removeCallbacks(this.paidTimeoutTask);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        Utils.skipNfcQrcode(this);
        finish();
    }
}