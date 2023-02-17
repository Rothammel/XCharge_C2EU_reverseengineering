package com.xcharge.charger.ui.c2.activity.charge.nfc;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import com.xcharge.charger.R;
import com.xcharge.charger.data.bean.device.NFC;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.db.ContentDB;
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
import com.xcharge.charger.ui.c2.activity.widget.LoadingDialog;
import java.util.HashMap;

/* loaded from: classes.dex */
public class NFCChargeWaittingStartActivity extends BaseActivity {
    private LoadingDialog loadingDialog;
    private TextView tv_bottom;
    private TextView tv_relieve;
    private int waittingStartChargeTime;
    private final int MSG_WAITTING_START_CHARGE = 1;
    private final int MSG_SHOW_DIALOG = 2;
    private final int MSG_DISMISS_DIALOG = 3;
    Handler mHandler = new Handler() { // from class: com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeWaittingStartActivity.1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    NFCChargeWaittingStartActivity.this.mHandler.removeMessages(1);
                    NFCChargeWaittingStartActivity nFCChargeWaittingStartActivity = NFCChargeWaittingStartActivity.this;
                    nFCChargeWaittingStartActivity.waittingStartChargeTime--;
                    if (NFCChargeWaittingStartActivity.this.waittingStartChargeTime >= 0) {
                        NFCChargeWaittingStartActivity.this.loadingDialog.changeLoadingText(NFCChargeWaittingStartActivity.this.getString(R.string.waitting_gun_connect, new Object[]{Integer.valueOf(NFCChargeWaittingStartActivity.this.waittingStartChargeTime)}));
                        sendEmptyMessageDelayed(1, 1000L);
                        return;
                    }
                    return;
                case 2:
                    if (NFCChargeWaittingStartActivity.this.loadingDialog == null) {
                        NFCChargeWaittingStartActivity.this.loadingDialog = LoadingDialog.createDialog(NFCChargeWaittingStartActivity.this, NFCChargeWaittingStartActivity.this.getString(R.string.waitting_gun_connect, new Object[]{Integer.valueOf(NFCChargeWaittingStartActivity.this.waittingStartChargeTime)}));
                    }
                    NFCChargeWaittingStartActivity.this.loadingDialog.changeLoadingText(NFCChargeWaittingStartActivity.this.getString(R.string.waitting_gun_connect, new Object[]{Integer.valueOf(NFCChargeWaittingStartActivity.this.waittingStartChargeTime)}));
                    if (!NFCChargeWaittingStartActivity.this.loadingDialog.isShowing()) {
                        NFCChargeWaittingStartActivity.this.loadingDialog.show();
                    }
                    sendEmptyMessageDelayed(1, 1000L);
                    return;
                case 3:
                    if (NFCChargeWaittingStartActivity.this.loadingDialog != null && NFCChargeWaittingStartActivity.this.loadingDialog.isShowing()) {
                        NFCChargeWaittingStartActivity.this.loadingDialog.dismiss();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus("1");
        if (portStatus != null) {
            String charge_id = portStatus.getCharge_id();
            if (!TextUtils.isEmpty(charge_id)) {
                Variate.getInstance().setChargeId(charge_id);
            }
        }
        setContentView(R.layout.activity_nfc_charge_waitting_start);
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        Utils.setPermitNFC(true, false, false, false);
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void initView() {
        this.tv_relieve = (TextView) findViewById(R.id.tv_relieve);
        this.tv_bottom = (TextView) findViewById(R.id.tv_bottom);
        NFC portNFCStatus = HardwareStatusCacheProvider.getInstance().getPortNFCStatus("1");
        if (portNFCStatus == null) {
            this.tv_relieve.setVisibility(8);
        } else if (NFC_CARD_TYPE.U2.equals(portNFCStatus.getLatestCardType())) {
            this.tv_relieve.setVisibility(0);
        } else {
            this.tv_relieve.setVisibility(8);
        }
        CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
        if (CHARGE_PLATFORM.xcharge.equals(platform) || CHARGE_PLATFORM.yzx.equals(platform)) {
            if (portNFCStatus == null) {
                this.tv_bottom.setVisibility(8);
            } else if (NFC_CARD_TYPE.U2.equals(portNFCStatus.getLatestCardType()) || NFC_CARD_TYPE.U3.equals(portNFCStatus.getLatestCardType())) {
                showUserBalance();
            } else {
                this.tv_bottom.setVisibility(8);
            }
        } else if (CHARGE_PLATFORM.anyo.equals(platform) || CHARGE_PLATFORM.xmsz.equals(platform) || CHARGE_PLATFORM.ecw.equals(platform)) {
            if (portNFCStatus == null) {
                this.tv_bottom.setVisibility(8);
            } else if (NFC_CARD_TYPE.anyo_svw.equals(portNFCStatus.getLatestCardType())) {
                this.tv_bottom.setVisibility(8);
            } else {
                showUserBalance();
            }
        }
    }

    private void showUserBalance() {
        ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(Variate.getInstance().getChargeId());
        if (chargeBill == null) {
            this.tv_bottom.setVisibility(8);
            return;
        }
        this.tv_bottom.setVisibility(0);
        this.tv_bottom.setText(getString(R.string.home_cur_balance, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(chargeBill.getUser_balance() / 100.0d)), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
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
            if (UIEventMessage.TYPE_UI_ELEMENT.equals(type) && UIEventMessage.SUBTYPE_UI_LOADING_DIALOG.equals(subType) && "mLoadingDialog".equals(name) && "update".equals(opr)) {
                this.waittingStartChargeTime = Integer.parseInt((String) data.get("waitStart"));
                if ("show".equals((String) data.get(ContentDB.AuthInfoTable.STATUS))) {
                    this.mHandler.sendEmptyMessage(2);
                } else if ("dismiss".equals((String) data.get(ContentDB.AuthInfoTable.STATUS))) {
                    this.mHandler.sendEmptyMessage(3);
                }
            }
        }
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        Log.d("NFCChargeWaittingStartActivity", "onResume");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        Log.d("NFCChargeWaittingStartActivity", "onPause");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        Log.d("NFCChargeWaittingStartActivity", "onStop");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        Log.d("NFCChargeWaittingStartActivity", "onDestroy");
        if (this.loadingDialog != null && this.loadingDialog.isShowing()) {
            this.loadingDialog.dismiss();
        }
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(3);
        this.mHandler.removeCallbacksAndMessages(null);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), "key", null, getClass().getName(), "up", null);
    }
}