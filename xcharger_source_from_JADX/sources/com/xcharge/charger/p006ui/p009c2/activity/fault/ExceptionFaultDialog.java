package com.xcharge.charger.p006ui.p009c2.activity.fault;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.p006ui.p009c2.activity.BaseActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.online.WaittingStartChargeActivity;
import com.xcharge.charger.p006ui.p009c2.activity.data.Variate;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;
import com.xcharge.common.utils.HandlerTimer;

/* renamed from: com.xcharge.charger.ui.c2.activity.fault.ExceptionFaultDialog */
public class ExceptionFaultDialog extends BaseDialog {
    public static final int MSG_DISMISS_DIALOG = 1;
    public int cnt;
    public int error;
    private FaultDialogContentObserver faultDialogContentObserver;
    private HandlerTimer handlerTimer = null;
    protected Handler mRadarHandler = new Handler() {
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
    public Port port = null;
    public String visibility;

    public ExceptionFaultDialog(Context context) {
        super(context, C0221R.style.Dialog_Fullscreen);
    }

    public void initView() {
        super.initView();
        if (HardwareStatusCacheProvider.getInstance().getPort("1") != null) {
            this.port = HardwareStatusCacheProvider.getInstance().getPort("1");
        }
        this.iv_status_one.setImageResource(C0221R.C0222drawable.ic_repair_status);
        this.tv_status_two.setVisibility(0);
        switch (this.error) {
            case ErrorCode.EC_DEVICE_NOT_INIT:
                this.tv_status_one.setText(C0221R.string.device_not_initialized);
                break;
            case ErrorCode.EC_DEVICE_NO_GROUND:
                this.tv_status_one.setText(C0221R.string.device_ground_invalid);
                break;
            case ErrorCode.EC_DEVICE_LOST_PHASE:
                this.tv_status_one.setText(C0221R.string.line_phase_lost);
                this.tv_status_two.setText(this.context.getString(C0221R.string.voltage_exception_details, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, this.port.getVolts().get(0)), CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, this.port.getVolts().get(1)), CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, this.port.getVolts().get(2))}));
                break;
            case ErrorCode.EC_DEVICE_EMERGENCY_STOP:
                this.tv_status_one.setText(C0221R.string.repair_scram_status_text);
                break;
            case ErrorCode.EC_DEVICE_VOLT_ERROR:
                this.tv_status_one.setText(C0221R.string.line_voltage_exception);
                this.tv_status_two.setText(this.context.getString(C0221R.string.voltage_exception_details, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, this.port.getVolts().get(0)), CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, this.port.getVolts().get(1)), CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, this.port.getVolts().get(2))}));
                break;
            case ErrorCode.EC_DEVICE_AMP_ERROR:
                this.tv_status_one.setText(C0221R.string.line_current_exception);
                this.tv_status_two.setText(this.context.getString(C0221R.string.current_exception_details, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, this.port.getAmps().get(0)), CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, this.port.getAmps().get(1)), CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, this.port.getAmps().get(2))}));
                break;
            case ErrorCode.EC_DEVICE_TEMP_ERROR:
                this.tv_status_one.setText(C0221R.string.device_temperature_exception);
                this.tv_status_two.setText(this.context.getString(C0221R.string.temperature_exception_details, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.ONEDP, Double.valueOf(this.port.getChipTemp().doubleValue() / 10.0d))}));
                break;
            case ErrorCode.EC_DEVICE_POWER_LEAK:
                this.tv_status_one.setText(C0221R.string.device_current_leakage);
                this.tv_status_two.setText(this.context.getString(C0221R.string.current_leakage_details, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.THREEDP, this.port.getLeakAmp())}));
                break;
            case ErrorCode.EC_DEVICE_COMM_ERROR:
                this.tv_status_one.setText(C0221R.string.communication_exception_with_auto);
                this.tv_status_two.setText(this.context.getString(C0221R.string.communication_details, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.THREEDP, Double.valueOf(((double) this.port.getCpVoltage().intValue()) / 1000.0d))}));
                break;
        }
        if (!this.visibility.equals("VISIBLE")) {
            return;
        }
        if (Variate.getInstance().isNFC) {
            if (this.cnt == 1) {
                this.tv_status_two.setText(this.context.getString(C0221R.string.charge_nfc_error_termination));
                return;
            }
            this.tv_status_two.setText(this.context.getString(C0221R.string.charge_nfc_error_termination_exceed, new Object[]{Integer.valueOf(this.cnt)}));
        } else if (Utils.getCurrentClassName(this.context).equals(WaittingStartChargeActivity.class.getName())) {
            if (this.cnt == 1) {
                this.tv_status_two.setText(this.context.getString(C0221R.string.charge_onlie_error_termination_key));
                return;
            }
            this.tv_status_two.setText(this.context.getString(C0221R.string.charge_onlie_error_termination_key_exceed, new Object[]{Integer.valueOf(this.cnt)}));
        } else if (this.cnt == 1) {
            this.tv_status_two.setText(this.context.getString(C0221R.string.charge_onlie_error_termination));
        } else {
            this.tv_status_two.setText(this.context.getString(C0221R.string.charge_onlie_error_termination_exceed, new Object[]{Integer.valueOf(this.cnt)}));
        }
    }

    /* renamed from: com.xcharge.charger.ui.c2.activity.fault.ExceptionFaultDialog$FaultDialogContentObserver */
    public class FaultDialogContentObserver extends ContentObserver {
        public FaultDialogContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            ExceptionFaultDialog.this.dismissFaultDialog();
        }
    }

    /* access modifiers changed from: private */
    public void dismissFaultDialog() {
        if (HardwareStatusCacheProvider.getInstance().getPort("1").getDeviceError().getCode() == 200) {
            dismiss();
        }
    }

    public void show() {
        super.show();
        this.faultDialogContentObserver = new FaultDialogContentObserver(new Handler());
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/1"), false, this.faultDialogContentObserver);
        this.handlerTimer = new HandlerTimer(this.mRadarHandler);
        this.handlerTimer.init(this.context);
        this.handlerTimer.startTimer(1000, 1, (Object) null);
    }

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

    public void onBackPressed() {
        super.onBackPressed();
        dismissFaultDialog();
    }
}
