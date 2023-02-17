package com.xcharge.charger.p006ui.p009c2.activity.charge.online;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.p006ui.p009c2.activity.BaseActivity;
import com.xcharge.charger.p006ui.p009c2.activity.data.Variate;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;

/* renamed from: com.xcharge.charger.ui.c2.activity.charge.online.ChargeCostActivity */
public class ChargeCostActivity extends BaseActivity {
    private LinearLayout ll_fee;
    private LinearLayout ll_power;
    private Handler mHandler = new Handler();
    private Runnable paidTimeoutTask = new Runnable() {
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

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0221R.layout.activity_charge_cost);
        this.mHandler.postDelayed(this.paidTimeoutTask, 10000);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        Utils.setPermitNFC(false, false, false, false);
    }

    /* access modifiers changed from: protected */
    public void initView() {
        this.tv_title = (TextView) findViewById(C0221R.C0223id.tv_title);
        this.ll_fee = (LinearLayout) findViewById(C0221R.C0223id.ll_fee);
        this.tv_fee = (TextView) findViewById(C0221R.C0223id.tv_fee);
        this.tv_fee_unit = (TextView) findViewById(C0221R.C0223id.tv_fee_unit);
        this.tv_free = (TextView) findViewById(C0221R.C0223id.tv_free);
        this.tv_timer = (TextView) findViewById(C0221R.C0223id.tv_timer);
        this.ll_power = (LinearLayout) findViewById(C0221R.C0223id.ll_power);
        this.tv_power = (TextView) findViewById(C0221R.C0223id.tv_power);
        this.tv_bottom = (TextView) findViewById(C0221R.C0223id.tv_bottom);
        if (CountrySettingCacheProvider.getInstance().isSetRTL()) {
            this.ll_fee.setLayoutDirection(1);
            this.ll_power.setLayoutDirection(1);
        } else {
            this.ll_fee.setLayoutDirection(0);
            this.ll_power.setLayoutDirection(0);
        }
        ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(Variate.getInstance().getChargeId());
        if (chargeBill != null) {
            if (!CHARGE_PLATFORM.anyo.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
                this.tv_title.setText(getString(C0221R.string.charge_cost_text));
                this.tv_fee.setText(CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(((double) chargeBill.getTotal_fee()) / 100.0d)));
                this.tv_fee_unit.setText(CountrySettingCacheProvider.getInstance().getMoneyDisp());
                switch (chargeBill.getPay_flag()) {
                    case -1:
                        this.tv_bottom.setText(C0221R.string.clearing_hint_text);
                        break;
                    case 0:
                        this.tv_bottom.setText(C0221R.string.clearing_hint_text);
                        break;
                    case 1:
                        this.tv_title.setText(getString(C0221R.string.personal_cost_title));
                        this.tv_bottom.setText(C0221R.string.paycomplete_welcome);
                        break;
                }
            } else {
                this.tv_title.setText(getString(C0221R.string.chargecomplete_text));
                this.tv_fee.setText("");
                this.tv_fee_unit.setText(getString(C0221R.string.clearing_hint_text));
                this.tv_bottom.setText("");
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

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        Log.d("ChargeCostActivity", "onResume");
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        Log.d("ChargeCostActivity", "onPause");
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        Log.d("ChargeCostActivity", "onStop");
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        Log.d("ChargeCostActivity", "onDestroy");
        this.mHandler.removeCallbacksAndMessages((Object) null);
        this.mHandler.removeCallbacks(this.paidTimeoutTask);
    }

    public void onBackPressed() {
        Utils.skipNfcQrcode(this);
        finish();
    }
}
