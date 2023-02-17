package com.xcharge.charger.ui.c2.activity.fault;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import com.xcharge.charger.R;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.ui.c2.activity.charge.online.WaittingStartChargeActivity;
import com.xcharge.charger.ui.c2.activity.data.Variate;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;
import com.xcharge.common.utils.HandlerTimer;

/* loaded from: classes.dex */
public class ExceptionFaultDialog extends BaseDialog {
    public static final int MSG_DISMISS_DIALOG = 1;
    public int cnt;
    public int error;
    private FaultDialogContentObserver faultDialogContentObserver;
    private HandlerTimer handlerTimer;
    protected Handler mRadarHandler;
    public Port port;
    public String visibility;

    public ExceptionFaultDialog(Context context) {
        super(context, R.style.Dialog_Fullscreen);
        this.port = null;
        this.handlerTimer = null;
        this.mRadarHandler = new Handler() { // from class: com.xcharge.charger.ui.c2.activity.fault.ExceptionFaultDialog.1
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        ExceptionFaultDialog.this.dismissFaultDialog();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    @Override // com.xcharge.charger.ui.c2.activity.fault.BaseDialog
    public void initView() {
        super.initView();
        if (HardwareStatusCacheProvider.getInstance().getPort("1") != null) {
            this.port = HardwareStatusCacheProvider.getInstance().getPort("1");
        }
        this.iv_status_one.setImageResource(R.drawable.ic_repair_status);
        this.tv_status_two.setVisibility(0);
        switch (this.error) {
            case ErrorCode.EC_DEVICE_NOT_INIT /* 30010 */:
                this.tv_status_one.setText(R.string.device_not_initialized);
                break;
            case ErrorCode.EC_DEVICE_NO_GROUND /* 30011 */:
                this.tv_status_one.setText(R.string.device_ground_invalid);
                break;
            case ErrorCode.EC_DEVICE_LOST_PHASE /* 30012 */:
                this.tv_status_one.setText(R.string.line_phase_lost);
                this.tv_status_two.setText(this.context.getString(R.string.voltage_exception_details, CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, this.port.getVolts().get(0)), CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, this.port.getVolts().get(1)), CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, this.port.getVolts().get(2))));
                break;
            case ErrorCode.EC_DEVICE_EMERGENCY_STOP /* 30013 */:
                this.tv_status_one.setText(R.string.repair_scram_status_text);
                break;
            case ErrorCode.EC_DEVICE_VOLT_ERROR /* 30014 */:
                this.tv_status_one.setText(R.string.line_voltage_exception);
                this.tv_status_two.setText(this.context.getString(R.string.voltage_exception_details, CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, this.port.getVolts().get(0)), CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, this.port.getVolts().get(1)), CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, this.port.getVolts().get(2))));
                break;
            case ErrorCode.EC_DEVICE_AMP_ERROR /* 30015 */:
                this.tv_status_one.setText(R.string.line_current_exception);
                this.tv_status_two.setText(this.context.getString(R.string.current_exception_details, CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, this.port.getAmps().get(0)), CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, this.port.getAmps().get(1)), CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, this.port.getAmps().get(2))));
                break;
            case ErrorCode.EC_DEVICE_TEMP_ERROR /* 30016 */:
                this.tv_status_one.setText(R.string.device_temperature_exception);
                this.tv_status_two.setText(this.context.getString(R.string.temperature_exception_details, CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, Double.valueOf(this.port.getChipTemp().doubleValue() / 10.0d))));
                break;
            case ErrorCode.EC_DEVICE_POWER_LEAK /* 30017 */:
                this.tv_status_one.setText(R.string.device_current_leakage);
                this.tv_status_two.setText(this.context.getString(R.string.current_leakage_details, CountrySettingCacheProvider.getInstance().format(BaseActivity.THREEDP, this.port.getLeakAmp())));
                break;
            case ErrorCode.EC_DEVICE_COMM_ERROR /* 30018 */:
                this.tv_status_one.setText(R.string.communication_exception_with_auto);
                this.tv_status_two.setText(this.context.getString(R.string.communication_details, CountrySettingCacheProvider.getInstance().format(BaseActivity.THREEDP, Double.valueOf(this.port.getCpVoltage().intValue() / 1000.0d))));
                break;
        }
        if (this.visibility.equals("VISIBLE")) {
            if (Variate.getInstance().isNFC) {
                if (this.cnt == 1) {
                    this.tv_status_two.setText(this.context.getString(R.string.charge_nfc_error_termination));
                } else {
                    this.tv_status_two.setText(this.context.getString(R.string.charge_nfc_error_termination_exceed, Integer.valueOf(this.cnt)));
                }
            } else if (Utils.getCurrentClassName(this.context).equals(WaittingStartChargeActivity.class.getName())) {
                if (this.cnt == 1) {
                    this.tv_status_two.setText(this.context.getString(R.string.charge_onlie_error_termination_key));
                } else {
                    this.tv_status_two.setText(this.context.getString(R.string.charge_onlie_error_termination_key_exceed, Integer.valueOf(this.cnt)));
                }
            } else if (this.cnt == 1) {
                this.tv_status_two.setText(this.context.getString(R.string.charge_onlie_error_termination));
            } else {
                this.tv_status_two.setText(this.context.getString(R.string.charge_onlie_error_termination_exceed, Integer.valueOf(this.cnt)));
            }
        }
    }

    /* loaded from: classes.dex */
    public class FaultDialogContentObserver extends ContentObserver {
        public FaultDialogContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            ExceptionFaultDialog.this.dismissFaultDialog();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dismissFaultDialog() {
        ErrorCode error = HardwareStatusCacheProvider.getInstance().getPort("1").getDeviceError();
        if (error.getCode() == 200) {
            dismiss();
        }
    }

    @Override // com.xcharge.charger.ui.c2.activity.fault.BaseDialog, android.app.Dialog
    public void show() {
        super.show();
        this.faultDialogContentObserver = new FaultDialogContentObserver(new Handler());
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/1"), false, this.faultDialogContentObserver);
        this.handlerTimer = new HandlerTimer(this.mRadarHandler);
        this.handlerTimer.init(this.context);
        this.handlerTimer.startTimer(1000L, 1, null);
    }

    @Override // com.xcharge.charger.ui.c2.activity.fault.BaseDialog, android.app.Dialog, android.content.DialogInterface
    public void dismiss() {
        super.dismiss();
        if (this.faultDialogContentObserver != null) {
            this.context.getContentResolver().unregisterContentObserver(this.faultDialogContentObserver);
        }
        if (this.handlerTimer != null) {
            this.handlerTimer.stopTimer(1);
            this.handlerTimer.destroy();
        }
    }

    @Override // com.xcharge.charger.ui.c2.activity.fault.BaseDialog, android.app.Dialog
    public void onBackPressed() {
        super.onBackPressed();
        dismissFaultDialog();
    }
}
