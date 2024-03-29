package com.xcharge.charger.ui.c2.activity.charge.nfc;

import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xcharge.charger.R;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.CHARGE_MODE;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_STATUS;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.bean.type.PHASE;
import com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.ui.api.bean.UICtrlMessage;
import com.xcharge.charger.ui.api.bean.UIEventMessage;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.ui.c2.activity.data.Variate;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;
import com.xcharge.charger.ui.c2.activity.widget.BubbleView;
import com.xcharge.charger.ui.c2.activity.widget.LoadingDialog;
import com.xcharge.common.utils.HandlerTimer;
import java.util.ArrayList;
import java.util.HashMap;

/* loaded from: classes.dex */
public class NFCChargingActivity extends BaseActivity {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_MODE;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE;
    private ChargeStatusContentObserver chargeStatusContentObserver;
    private ImageView iv_phase;
    private LinearLayout ll_current;
    private LinearLayout ll_fee;
    private LinearLayout ll_power;
    private BubbleView popView;
    private LoadingDialog showHintDialog;
    private HandlerTimer timerHandler;
    private TextView tv_current;
    private TextView tv_fee;
    private TextView tv_fee_unit;
    private TextView tv_kwatt;
    private TextView tv_power;
    private TextView tv_state;
    private TextView tv_timer;
    private final int MSG_ADD_POP = 1;
    private final int MSG_TIMER_REPEAT = 2;
    private final int MSG_CHARGE_PAUSE = 3;
    private final int MSG_CHARGE_PAUSE_REPEAT = 4;
    private long chargingTime = 0;
    private long curPauseTime = 0;
    private int waitStop = 0;
    Handler mHandler = new Handler() { // from class: com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargingActivity.1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    removeMessages(1);
                    NFCChargingActivity.this.updateBubbleView();
                    NFCChargingActivity.this.mHandler.sendEmptyMessageDelayed(1, 300L);
                    return;
                case 2:
                default:
                    return;
                case 3:
                    NFCChargingActivity.this.chargeStateChange();
                    if (NFCChargingActivity.this.showHintDialog == null) {
                        NFCChargingActivity.this.showHintDialog = LoadingDialog.createDialog(NFCChargingActivity.this, NFCChargingActivity.this.getString(R.string.charge_pause_charge_hint, new Object[]{Integer.valueOf(NFCChargingActivity.this.waitStop)}));
                    } else {
                        NFCChargingActivity.this.showHintDialog.changeLoadingText(NFCChargingActivity.this.getString(R.string.charge_pause_charge_hint, new Object[]{Long.valueOf(NFCChargingActivity.this.waitStop - NFCChargingActivity.this.curPauseTime)}));
                    }
                    if (!NFCChargingActivity.this.showHintDialog.isShowing()) {
                        NFCChargingActivity.this.showHintDialog.show();
                    }
                    sendEmptyMessageDelayed(4, 1000L);
                    return;
                case 4:
                    removeMessages(4);
                    NFCChargingActivity.this.curPauseTime++;
                    if (NFCChargingActivity.this.curPauseTime < NFCChargingActivity.this.waitStop) {
                        if (NFCChargingActivity.this.showHintDialog != null) {
                            NFCChargingActivity.this.showHintDialog.changeLoadingText(NFCChargingActivity.this.getString(R.string.charge_pause_charge_hint, new Object[]{Long.valueOf(NFCChargingActivity.this.waitStop - NFCChargingActivity.this.curPauseTime)}));
                        }
                        sendEmptyMessageDelayed(4, 1000L);
                        return;
                    }
                    NFCChargingActivity.this.handChargeServiceStopUI();
                    return;
            }
        }
    };
    protected Handler mTimerHandler = new Handler() { // from class: com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargingActivity.2
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    long nowTime = (System.currentTimeMillis() - NFCChargingActivity.this.chargingTime) / 1000;
                    NFCChargingActivity.this.tv_timer.setText(Utils.fromatTotalTime(nowTime));
                    NFCChargingActivity.this.timerHandler.startTimer(1000L, 2, null);
                    return;
                default:
                    return;
            }
        }
    };

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_MODE() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_MODE;
        if (iArr == null) {
            iArr = new int[CHARGE_MODE.valuesCustom().length];
            try {
                iArr[CHARGE_MODE.full.ordinal()] = 5;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[CHARGE_MODE.normal_charge.ordinal()] = 3;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[CHARGE_MODE.paused.ordinal()] = 6;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[CHARGE_MODE.pre_charge.ordinal()] = 2;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[CHARGE_MODE.trickle_charge.ordinal()] = 4;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[CHARGE_MODE.unknow.ordinal()] = 1;
            } catch (NoSuchFieldError e6) {
            }
            $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_MODE = iArr;
        }
        return iArr;
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE;
        if (iArr == null) {
            iArr = new int[PHASE.valuesCustom().length];
            try {
                iArr[PHASE.DC_PHASE.ordinal()] = 4;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[PHASE.SINGLE_PHASE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[PHASE.THREE_PHASE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[PHASE.UNKOWN_PHASE.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE = iArr;
        }
        return iArr;
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        this.timerHandler = new HandlerTimer(this.mTimerHandler);
        this.timerHandler.init(context);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_charging);
        this.chargeStatusContentObserver = new ChargeStatusContentObserver(this.mHandler);
        getContentResolver().registerContentObserver(ChargeStatusCacheProvider.getInstance().getUriFor("ports/"), true, this.chargeStatusContentObserver);
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        Utils.setPermitNFC(true, false, false, false);
        this.timerHandler.startTimer(1000L, 2, null);
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void initView() {
        this.popView = (BubbleView) findViewById(R.id.popView);
        this.tv_state = (TextView) findViewById(R.id.tv_state);
        this.tv_timer = (TextView) findViewById(R.id.tv_timer);
        this.ll_fee = (LinearLayout) findViewById(R.id.ll_fee);
        this.tv_fee = (TextView) findViewById(R.id.tv_fee);
        this.tv_fee_unit = (TextView) findViewById(R.id.tv_fee_unit);
        this.ll_power = (LinearLayout) findViewById(R.id.ll_power);
        this.ll_current = (LinearLayout) findViewById(R.id.ll_current);
        this.tv_current = (TextView) findViewById(R.id.tv_current);
        this.iv_phase = (ImageView) findViewById(R.id.iv_phase);
        this.tv_power = (TextView) findViewById(R.id.tv_power);
        this.tv_kwatt = (TextView) findViewById(R.id.tv_kwatt);
        if (CountrySettingCacheProvider.getInstance().isSetRTL()) {
            this.ll_fee.setLayoutDirection(1);
            this.ll_power.setLayoutDirection(1);
            this.ll_current.setLayoutDirection(1);
        } else {
            this.ll_fee.setLayoutDirection(0);
            this.ll_power.setLayoutDirection(0);
            this.ll_current.setLayoutDirection(0);
        }
        switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE()[HardwareStatusCacheProvider.getInstance().getHardwareStatus().getPhase().ordinal()]) {
            case 1:
                this.iv_phase.setImageResource(R.drawable.ic_charger_type_unkown);
                break;
            case 2:
                this.iv_phase.setImageResource(R.drawable.ic_charger_type_single);
                break;
            case 3:
                this.iv_phase.setImageResource(R.drawable.ic_charger_type_three);
                break;
            case 4:
                this.iv_phase.setImageResource(R.drawable.ic_charger_type_dc);
                break;
        }
        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus("1");
        if (portStatus != null) {
            long chargeStartTime = portStatus.getChargeStartTime();
            if (chargeStartTime == 0) {
                this.chargingTime = System.currentTimeMillis();
            } else {
                this.chargingTime = chargeStartTime;
            }
            CHARGE_MODE chargeMode = portStatus.getChargeMode();
            CHARGE_STATUS chargeStatus = portStatus.getChargeStatus();
            if (chargeMode != null) {
                if (this.nowChargeMode == null) {
                    if (chargeMode.getMode() == CHARGE_MODE.normal_charge.getMode()) {
                        acquireScreenLock();
                    } else if (PLATFORM_CUSTOMER.anyo_private.equals(SystemSettingCacheProvider.getInstance().getPlatformCustomer())) {
                        acquireScreenLock();
                    } else {
                        acquireScreenLockAndKeep();
                    }
                    this.nowChargeMode = chargeMode;
                } else if (this.nowChargeMode.getMode() != chargeMode.getMode()) {
                    if (this.nowChargeMode.getMode() == CHARGE_MODE.normal_charge.getMode()) {
                        if (PLATFORM_CUSTOMER.anyo_private.equals(SystemSettingCacheProvider.getInstance().getPlatformCustomer())) {
                            acquireScreenLock();
                        } else {
                            acquireScreenLockAndKeep();
                        }
                    } else if (chargeMode.getMode() == CHARGE_MODE.normal_charge.getMode()) {
                        acquireScreenLock();
                    }
                    this.nowChargeMode = chargeMode;
                }
                if (CHARGE_STATUS.CHARGING.equals(chargeStatus)) {
                    switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_MODE()[chargeMode.ordinal()]) {
                        case 1:
                            this.mHandler.removeMessages(1);
                            this.tv_state.setText(R.string.charge_state_unknow);
                            this.tv_state.setTextColor(-256);
                            break;
                        case 2:
                            this.mHandler.sendEmptyMessage(1);
                            this.tv_state.setText(R.string.charge_state_prepare);
                            this.tv_state.setTextColor(-256);
                            break;
                        case 3:
                            this.mHandler.sendEmptyMessage(1);
                            this.tv_state.setText(R.string.charge_state_start);
                            this.tv_state.setTextColor(-1);
                            break;
                        case 4:
                            this.mHandler.sendEmptyMessage(1);
                            this.tv_state.setText(R.string.charge_state_trickle);
                            this.tv_state.setTextColor(-256);
                            break;
                        case 6:
                            this.mHandler.removeMessages(1);
                            this.tv_state.setText(R.string.charge_state_stop);
                            this.tv_state.setTextColor(-256);
                            break;
                    }
                } else {
                    this.mHandler.removeMessages(1);
                    this.tv_state.setText(R.string.charge_state_stop);
                    this.tv_state.setTextColor(-256);
                }
            }
            Double power = portStatus.getPower();
            if (power == null) {
                this.tv_power.setText(CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(0.0d)));
            } else {
                this.tv_power.setText(CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, power));
            }
            this.tv_fee_unit.setText(CountrySettingCacheProvider.getInstance().getMoneyDisp());
            ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(Variate.getInstance().getChargeId());
            if (chargeBill == null) {
                this.tv_fee.setText(CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(0.0d)));
            } else if (CHARGE_PLATFORM.anyo.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
                if ((CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.anyo_svw).equals(chargeBill.getUser_type())) {
                    this.tv_fee.setText(CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(0.0d)));
                } else {
                    this.tv_fee.setText("");
                    this.tv_fee_unit.setText(getString(R.string.charge_platform_billing));
                }
            } else if ((CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.U1).equals(chargeBill.getUser_type()) || (CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.CT_DEMO).equals(chargeBill.getUser_type())) {
                this.tv_fee.setText(CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(0.0d)));
            } else {
                this.tv_fee.setText(CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(portStatus.getTotalFee())));
            }
            ArrayList<Double> amps = portStatus.getAmps();
            if (amps == null || amps.get(0) == null) {
                this.tv_current.setText(getString(R.string.cur_current_text, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(0.0d))}));
            } else {
                this.tv_current.setText(getString(R.string.cur_current_text, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, amps.get(0))}));
            }
            Double kwatt = portStatus.getKwatt();
            if (kwatt == null) {
                this.tv_kwatt.setText(getString(R.string.cur_pwr_text, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.THREEDP, Double.valueOf(0.0d))}));
            } else {
                this.tv_kwatt.setText(getString(R.string.cur_pwr_text, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.THREEDP, kwatt)}));
            }
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
            if (UIEventMessage.TYPE_UI_ELEMENT.equals(type) && UIEventMessage.SUBTYPE_UI_LOADING_DIALOG.equals(subType) && "mShowHintDialog".equals(name) && "update".equals(opr)) {
                if ("show".equals((String) data.get(ContentDB.AuthInfoTable.STATUS))) {
                    this.waitStop = Integer.parseInt((String) data.get("waitStop"));
                    this.mHandler.sendEmptyMessage(3);
                    Utils.releaseScreenLock(context);
                } else if ("dismiss".equals((String) data.get(ContentDB.AuthInfoTable.STATUS))) {
                    handChargeStart();
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void chargeStateChange() {
        this.tv_state.setText(R.string.charge_state_stop);
        this.tv_state.setTextColor(-256);
        this.mHandler.removeMessages(1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handChargeServiceStopUI() {
        this.mHandler.removeMessages(4);
        this.curPauseTime = 0L;
        if (this.showHintDialog != null && this.showHintDialog.isShowing()) {
            this.showHintDialog.dismiss();
        }
    }

    private void handChargeStart() {
        this.curPauseTime = 0L;
        this.mHandler.removeMessages(4);
        this.mHandler.sendEmptyMessage(1);
        if (this.showHintDialog != null && this.showHintDialog.isShowing()) {
            this.showHintDialog.dismiss();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateBubbleView() {
        this.popView.start(this.popView.addOval());
        this.popView.start(this.popView.addOval());
        this.popView.start(this.popView.addOval());
        this.popView.start(this.popView.addOval());
        this.popView.start(this.popView.addOval());
        this.popView.start(this.popView.addOval());
        this.popView.start(this.popView.addOval());
        this.popView.start(this.popView.addOval());
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        Log.d("NFCChargingActivity", "onResume");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        Log.d("NFCChargingActivity", "onPause");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        Log.d("NFCChargingActivity", "onStop");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        Log.d("NFCChargingActivity", "onDestroy");
        this.timerHandler.stopTimer(2);
        this.timerHandler.destroy();
        this.mHandler.removeMessages(1);
        this.mHandler.removeCallbacksAndMessages(null);
        this.popView.destroy();
        handChargeServiceStopUI();
        getContentResolver().unregisterContentObserver(this.chargeStatusContentObserver);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void keepScreenOn() {
    }

    /* loaded from: classes.dex */
    public class ChargeStatusContentObserver extends ContentObserver {
        public ChargeStatusContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            NFCChargingActivity.this.initView();
        }
    }
}