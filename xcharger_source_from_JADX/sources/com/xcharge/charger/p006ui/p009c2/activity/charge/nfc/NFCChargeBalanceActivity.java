package com.xcharge.charger.p006ui.p009c2.activity.charge.nfc;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.p006ui.p009c2.activity.BaseActivity;
import com.xcharge.charger.p006ui.p009c2.activity.data.Variate;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;

/* renamed from: com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeBalanceActivity */
public class NFCChargeBalanceActivity extends BaseActivity {
    private Handler mHandler = new Handler();
    private Runnable paidTimeoutTask = new Runnable() {
        public void run() {
            Utils.skipNfcQrcode(NFCChargeBalanceActivity.this);
            NFCChargeBalanceActivity.this.finish();
        }
    };
    private TextView tv_balance;
    private TextView tv_bottom;
    private TextView tv_fee;
    private TextView tv_power;
    private TextView tv_timer;
    private TextView tv_title;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0221R.layout.activity_nfc_charge_balance);
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
        this.tv_timer = (TextView) findViewById(C0221R.C0223id.tv_timer);
        this.tv_power = (TextView) findViewById(C0221R.C0223id.tv_power);
        this.tv_fee = (TextView) findViewById(C0221R.C0223id.tv_fee);
        this.tv_balance = (TextView) findViewById(C0221R.C0223id.tv_balance);
        this.tv_bottom = (TextView) findViewById(C0221R.C0223id.tv_bottom);
        ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(Variate.getInstance().getChargeId());
        if (chargeBill != null) {
            this.tv_timer.setText(getString(C0221R.string.personal_cost_time, new Object[]{Utils.formatTime(chargeBill.getStop_time() - chargeBill.getStart_time())}));
            this.tv_power.setText(getString(C0221R.string.personal_cost_pwr, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(chargeBill.getTotal_power()))}));
            if (!CHARGE_PLATFORM.anyo.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
                this.tv_fee.setText(getString(C0221R.string.personal_cost_fee, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(((double) chargeBill.getTotal_fee()) / 100.0d)), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
            }
            if ((CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.U1).equals(chargeBill.getUser_type()) || (CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.CT_DEMO).equals(chargeBill.getUser_type())) {
                this.tv_title.setText(C0221R.string.personal_charger_title);
            } else if ((CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.U2).equals(chargeBill.getUser_type())) {
                if (chargeBill.getTotal_fee() == 0) {
                    long user_balance = chargeBill.getUser_balance();
                    this.tv_balance.setText(getString(C0221R.string.personal_cost_balance, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(((double) user_balance) / 100.0d)), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
                    this.tv_title.setText(C0221R.string.personal_cost_title);
                } else if (chargeBill.getPay_flag() == 1) {
                    long user_balance2 = chargeBill.getUser_balance();
                    this.tv_balance.setText(getString(C0221R.string.personal_cost_balance, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(((double) user_balance2) / 100.0d)), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
                    this.tv_title.setText(C0221R.string.personal_cost_title);
                } else {
                    this.tv_balance.setText(getString(C0221R.string.personal_cost_unbalance));
                    this.tv_title.setText(C0221R.string.personal_cost_unbalance_title);
                }
            } else if ((CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.U3).equals(chargeBill.getUser_type())) {
                if (chargeBill.getPay_flag() == 1) {
                    this.tv_title.setText(C0221R.string.personal_cost_title);
                } else {
                    this.tv_title.setText(C0221R.string.personal_cost_unbalance_title);
                }
            } else if ((CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.anyo1).equals(chargeBill.getUser_type()) || (CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.anyo_svw).equals(chargeBill.getUser_type()) || (CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.ecw1).equals(chargeBill.getUser_type())) {
                this.tv_title.setText(C0221R.string.personal_cost_title);
            }
        }
        this.tv_bottom.setText(C0221R.string.paycomplete_welcome);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        Log.d("NFCChargeBalanceActivity", "onResume");
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        Log.d("NFCChargeBalanceActivity", "onPause");
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        Log.d("NFCChargeBalanceActivity", "onStop");
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        Log.d("NFCChargeBalanceActivity", "onDestroy");
        this.mHandler.removeCallbacksAndMessages((Object) null);
        this.mHandler.removeCallbacks(this.paidTimeoutTask);
    }

    public void onBackPressed() {
        Utils.skipNfcQrcode(this);
        finish();
    }
}
