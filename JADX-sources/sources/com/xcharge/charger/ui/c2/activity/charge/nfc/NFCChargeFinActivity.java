package com.xcharge.charger.ui.c2.activity.charge.nfc;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.xcharge.charger.R;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.data.bean.device.NFC;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
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
import org.apache.commons.lang3.StringUtils;

/* loaded from: classes.dex */
public class NFCChargeFinActivity extends BaseActivity {
    private StringBuffer buffer;
    private LinearLayout include_one_line;
    private RelativeLayout include_two_line;
    private TextView tv_bottom;
    private TextView tv_bottom1;
    private TextView tv_bottom2;
    private TextView tv_hint;
    private long lockTimestamp = 0;
    private long curTimeout = 0;
    private boolean isDelay = false;
    Handler mHandler = new Handler();
    Runnable runnable = new Runnable() { // from class: com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeFinActivity.1
        @Override // java.lang.Runnable
        public void run() {
            try {
                NFCChargeFinActivity.this.mHandler.postDelayed(this, 1000L);
                NFCChargeFinActivity.this.curTimeout--;
                NFCChargeFinActivity.this.updateDelayTime(Variate.getInstance().getChargeId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_charge_fin);
        UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, getClass().getName(), "create", null);
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        Utils.setPermitNFC(true, false, false, false);
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void initView() {
        NFC portNFCStatus;
        String chargeId = Variate.getInstance().getChargeId();
        this.tv_hint = (TextView) findViewById(R.id.tv_hint);
        this.include_one_line = (LinearLayout) findViewById(R.id.include_one_line);
        this.include_two_line = (RelativeLayout) findViewById(R.id.include_two_line);
        this.tv_bottom = (TextView) findViewById(R.id.tv_bottom);
        this.tv_bottom1 = (TextView) findViewById(R.id.tv_bottom1);
        this.tv_bottom2 = (TextView) findViewById(R.id.tv_bottom2);
        ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(chargeId);
        if (chargeBill != null) {
            long stopTime = chargeBill.getStop_time();
            if ((CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.U1).equals(chargeBill.getUser_type()) || (CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.CT_DEMO).equals(chargeBill.getUser_type()) || (CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.anyo1).equals(chargeBill.getUser_type()) || (CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.anyo_svw).equals(chargeBill.getUser_type()) || (CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.ecw1).equals(chargeBill.getUser_type()) || (CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.ocpp).equals(chargeBill.getUser_type())) {
                this.include_one_line.setVisibility(0);
                this.include_two_line.setVisibility(8);
                this.tv_hint.setText(getString(R.string.chargecomplete_drawgun_delayfee_hint, new Object[]{this.sdf.format(Long.valueOf(stopTime))}));
            } else if ((CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.U2).equals(chargeBill.getUser_type())) {
                this.include_one_line.setVisibility(0);
                this.include_two_line.setVisibility(8);
                int totalFee = chargeBill.getTotal_fee();
                if (totalFee == 0) {
                    if (40014 == HardwareStatusCacheProvider.getInstance().getPortNFCStatus("1").getLatestError().getCode()) {
                        this.tv_hint.setText(getString(R.string.chargecomplete_drawgun_relieve_pairing_hint, new Object[]{this.sdf.format(Long.valueOf(stopTime))}));
                    } else {
                        this.tv_hint.setText(getString(R.string.chargecomplete_drawgun_delayfee_hint, new Object[]{this.sdf.format(Long.valueOf(stopTime))}));
                    }
                } else if (chargeBill.getPay_flag() == 1) {
                    this.tv_hint.setText(getString(R.string.chargecomplete_drawgun_already_pay_hint, new Object[]{this.sdf.format(Long.valueOf(stopTime)), CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(totalFee / 100.0d)), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
                } else {
                    this.tv_hint.setText(getString(R.string.chargecomplete_drawgun_not_pay_hint, new Object[]{this.sdf.format(Long.valueOf(stopTime)), CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(totalFee / 100.0d)), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
                }
            } else if ((CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.U3).equals(chargeBill.getUser_type())) {
                this.include_one_line.setVisibility(8);
                this.include_two_line.setVisibility(0);
                this.tv_bottom2.setText(R.string.chargecomplete_hint_text2);
                updateDelayTime(chargeId);
            }
            if (!(CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.U3).equals(chargeBill.getUser_type())) {
                this.tv_bottom.setTextSize(18.0f);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
                layoutParams.setMargins(0, 0, 0, 30);
                this.tv_bottom.setLayoutParams(layoutParams);
                this.buffer = new StringBuffer();
                this.buffer.append(getString(R.string.personal_cost_time, new Object[]{Utils.formatTime(chargeBill.getStop_time() - chargeBill.getStart_time())})).append("  ");
                this.buffer.append(getString(R.string.personal_cost_pwr, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(chargeBill.getTotal_power()))})).append(StringUtils.LF);
                CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
                if (!CHARGE_PLATFORM.anyo.equals(platform)) {
                    this.buffer.append(getString(R.string.personal_cost_fee, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(chargeBill.getTotal_fee() / 100.0d)), CountrySettingCacheProvider.getInstance().getMoneyDisp()})).append("  ");
                }
                if (CHARGE_PLATFORM.xcharge.equals(platform) && (portNFCStatus = HardwareStatusCacheProvider.getInstance().getPortNFCStatus("1")) != null && NFC_CARD_TYPE.U2.equals(portNFCStatus.getLatestCardType())) {
                    this.buffer.append(getString(R.string.home_cur_balance, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(chargeBill.getUser_balance() / 100.0d)), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
                }
                this.tv_bottom.setText(this.buffer);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateDelayTime(String chargeId) {
        ChargeBill chargeBill;
        int countDown;
        if (!TextUtils.isEmpty(chargeId) && (chargeBill = ChargeContentProxy.getInstance().getChargeBill(chargeId)) != null) {
            if (this.isDelay) {
                PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus("1");
                if (portStatus != null) {
                    this.tv_bottom1.setText(getString(R.string.chargecomplete_hint_text1, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(portStatus.getDelayPrice())), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
                }
                if (this.curTimeout % 60 == 0) {
                    countDown = (int) (this.curTimeout / 60);
                } else {
                    countDown = ((int) (this.curTimeout / 60)) + 1;
                }
                this.tv_hint.setText(getString(R.string.chargecomplete_drawgun_not_delayfee_hint, new Object[]{this.sdf.format(Long.valueOf(chargeBill.getStop_time())), Integer.valueOf(countDown)}));
                return;
            }
            this.tv_hint.setText(getString(R.string.chargecomplete_drawgun_delayfee_hint, new Object[]{this.sdf.format(Long.valueOf(chargeBill.getStop_time()))}));
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
                if (UIEventMessage.SUBTYPE_UI_TEXT_VIEW.equals(subType) && "mTvChargingTimer".equals(name) && "update".equals(opr)) {
                    this.mHandler.removeCallbacks(this.runnable);
                    initView();
                }
                if ("initNFCChargeFin".equals(name) && "update".equals(opr)) {
                    this.isDelay = ((Boolean) data.get("willDelayHandleNow")).booleanValue();
                    if (this.isDelay) {
                        this.curTimeout = Long.valueOf((String) data.get("plugoutTime")).longValue();
                        this.mHandler.postDelayed(this.runnable, 1000L);
                    }
                }
            }
        }
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        Log.d("NFCChargeFinActivity", "onResume");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        Log.d("NFCChargeFinActivity", "onPause");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        Log.d("NFCChargeFinActivity", "onStop");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        Log.d("NFCChargeFinActivity", "onDestroy");
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
