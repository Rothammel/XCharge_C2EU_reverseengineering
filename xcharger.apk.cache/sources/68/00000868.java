package com.xcharge.charger.ui.c2.activity.charge.nfc;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import com.xcharge.charger.R;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.ui.c2.activity.data.Variate;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;

/* loaded from: classes.dex */
public class NFCChargeBalanceActivity extends BaseActivity {
    private Handler mHandler = new Handler();
    private Runnable paidTimeoutTask = new Runnable() { // from class: com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeBalanceActivity.1
        @Override // java.lang.Runnable
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

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_charge_balance);
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
        this.tv_timer = (TextView) findViewById(R.id.tv_timer);
        this.tv_power = (TextView) findViewById(R.id.tv_power);
        this.tv_fee = (TextView) findViewById(R.id.tv_fee);
        this.tv_balance = (TextView) findViewById(R.id.tv_balance);
        this.tv_bottom = (TextView) findViewById(R.id.tv_bottom);
        ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(Variate.getInstance().getChargeId());
        if (chargeBill != null) {
            this.tv_timer.setText(getString(R.string.personal_cost_time, new Object[]{Utils.formatTime(chargeBill.getStop_time() - chargeBill.getStart_time())}));
            this.tv_power.setText(getString(R.string.personal_cost_pwr, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(chargeBill.getTotal_power()))}));
            if (!CHARGE_PLATFORM.anyo.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
                this.tv_fee.setText(getString(R.string.personal_cost_fee, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(chargeBill.getTotal_fee() / 100.0d)), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
            }
            if ((CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.U1).equals(chargeBill.getUser_type()) || (CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.CT_DEMO).equals(chargeBill.getUser_type())) {
                this.tv_title.setText(R.string.personal_charger_title);
            } else if ((CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.U2).equals(chargeBill.getUser_type())) {
                int totalFee = chargeBill.getTotal_fee();
                if (totalFee == 0) {
                    long user_balance = chargeBill.getUser_balance();
                    this.tv_balance.setText(getString(R.string.personal_cost_balance, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(user_balance / 100.0d)), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
                    this.tv_title.setText(R.string.personal_cost_title);
                } else {
                    int payFlag = chargeBill.getPay_flag();
                    if (payFlag == 1) {
                        long user_balance2 = chargeBill.getUser_balance();
                        this.tv_balance.setText(getString(R.string.personal_cost_balance, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(user_balance2 / 100.0d)), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
                        this.tv_title.setText(R.string.personal_cost_title);
                    } else {
                        this.tv_balance.setText(getString(R.string.personal_cost_unbalance));
                        this.tv_title.setText(R.string.personal_cost_unbalance_title);
                    }
                }
            } else if ((CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.U3).equals(chargeBill.getUser_type())) {
                int payFlag2 = chargeBill.getPay_flag();
                if (payFlag2 == 1) {
                    this.tv_title.setText(R.string.personal_cost_title);
                } else {
                    this.tv_title.setText(R.string.personal_cost_unbalance_title);
                }
            } else if ((CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.anyo1).equals(chargeBill.getUser_type()) || (CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.anyo_svw).equals(chargeBill.getUser_type()) || (CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.ecw1).equals(chargeBill.getUser_type())) {
                this.tv_title.setText(R.string.personal_cost_title);
            }
        }
        this.tv_bottom.setText(R.string.paycomplete_welcome);
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        Log.d("NFCChargeBalanceActivity", "onResume");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        Log.d("NFCChargeBalanceActivity", "onPause");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        Log.d("NFCChargeBalanceActivity", "onStop");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        Log.d("NFCChargeBalanceActivity", "onDestroy");
        this.mHandler.removeCallbacksAndMessages(null);
        this.mHandler.removeCallbacks(this.paidTimeoutTask);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        Utils.skipNfcQrcode(this);
        finish();
    }
}