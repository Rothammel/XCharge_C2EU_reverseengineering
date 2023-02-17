package com.xcharge.charger.ui.adpter.c2.update;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.ui.adapter.c2.R;
import com.xcharge.charger.ui.adapter.type.HOME_UI_STAGE;
import com.xcharge.charger.ui.adapter.type.UI_STATUS;
import com.xcharge.charger.ui.adpter.c2.UIService;
import com.xcharge.charger.ui.api.UICtrlMessageProxy;
import com.xcharge.charger.ui.api.bean.UICtrlMessage;
import com.xcharge.charger.ui.api.bean.UIEventMessage;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeInitActivity;
import com.xcharge.charger.ui.c2.activity.charge.online.QrcodeActivity;
import com.xcharge.charger.ui.c2.activity.data.Variate;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;
import java.util.HashMap;

/* loaded from: classes.dex */
public class UpdateHomeView {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$ui$adapter$type$HOME_UI_STAGE;

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$ui$adapter$type$HOME_UI_STAGE() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$ui$adapter$type$HOME_UI_STAGE;
        if (iArr == null) {
            iArr = new int[HOME_UI_STAGE.valuesCustom().length];
            try {
                iArr[HOME_UI_STAGE.boot_error.ordinal()] = 2;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[HOME_UI_STAGE.booting.ordinal()] = 1;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[HOME_UI_STAGE.normal.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            $SWITCH_TABLE$com$xcharge$charger$ui$adapter$type$HOME_UI_STAGE = iArr;
        }
        return iArr;
    }

    public void homeUIMode(Bundle data, Context context) {
        Log.d("UpdateHomeView.homeUIMode", "UI_STATUS:" + UIService.status);
        HOME_UI_STAGE stage = HOME_UI_STAGE.valueOf(data.getString("stage"));
        Log.d("UpdateHomeView.homeUIMode", "HOME_UI_STAGE:" + stage);
        switch ($SWITCH_TABLE$com$xcharge$charger$ui$adapter$type$HOME_UI_STAGE()[stage.ordinal()]) {
            case 1:
                Utils.skipNfcQrcode(context);
                UIService.dataForWait = data;
                UIService.status = UI_STATUS.wait_home;
                return;
            case 2:
                if (UIService.status.equals(UI_STATUS.wait_home)) {
                    UIService.dataForWait = data;
                    return;
                } else if (UIService.status.equals(UI_STATUS.home_boot)) {
                    dismissDialog();
                    ErrorCode error = HardwareStatusCacheProvider.getInstance().getDeviceFaultStatus();
                    if (error.getCode() == 30001) {
                        BaseActivity.showSmallDialog(context.getString(R.string.nfc_no_sn));
                        UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "testCharge", UICtrlMessage.OPR_SKIP, null);
                    } else if (error.getCode() == 30002) {
                        BaseActivity.showSmallDialog(context.getString(R.string.no_port));
                        UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "testCharge", UICtrlMessage.OPR_SKIP, null);
                    }
                    UIService.status = UI_STATUS.home_present;
                    return;
                } else {
                    return;
                }
            case 3:
                if (UIService.status.equals(UI_STATUS.home_boot)) {
                    dismissDialog();
                    Variate.getInstance().setInit(true);
                }
                UIService.status = UI_STATUS.home_present;
                return;
            default:
                return;
        }
    }

    public void homeUIWidgetCreate(UIEventMessage event, Context context) {
        if ((event.getActivity().equals(QrcodeActivity.class.getName()) || event.getActivity().equals(NFCChargeInitActivity.class.getName())) && event.getStatus().equals("create") && UI_STATUS.wait_home.equals(UIService.status)) {
            if (UIService.dataForWait == null) {
                UIService.status = UI_STATUS.home_create;
                return;
            }
            HOME_UI_STAGE stage = HOME_UI_STAGE.valueOf(UIService.dataForWait.getString("stage"));
            if (HOME_UI_STAGE.booting.equals(stage)) {
                showDialog();
                UIService.status = UI_STATUS.home_boot;
            } else if (HOME_UI_STAGE.boot_error.equals(stage)) {
                dismissDialog();
                ErrorCode error = HardwareStatusCacheProvider.getInstance().getDeviceFaultStatus();
                if (error.getCode() == 30001) {
                    BaseActivity.showSmallDialog(context.getString(R.string.nfc_no_sn));
                    UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "testCharge", UICtrlMessage.OPR_SKIP, null);
                } else if (error.getCode() == 30002) {
                    BaseActivity.showSmallDialog(context.getString(R.string.no_port));
                    UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "testCharge", UICtrlMessage.OPR_SKIP, null);
                }
                UIService.status = UI_STATUS.home_present;
            }
        }
    }

    private void dismissDialog() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("nfcDialogStatus", "dismiss");
        if (Variate.getInstance().isOnline()) {
            UICtrlMessageProxy.getInstance().sendCtrl(QrcodeActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, UIEventMessage.SUBTYPE_UI_LOADING_DIALOG, "initLoadingDialog", "update", data);
        } else {
            UICtrlMessageProxy.getInstance().sendCtrl(NFCChargeInitActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, UIEventMessage.SUBTYPE_UI_LOADING_DIALOG, "initLoadingDialog", "update", data);
        }
    }

    private void showDialog() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("nfcDialogStatus", "show");
        if (Variate.getInstance().isOnline()) {
            UICtrlMessageProxy.getInstance().sendCtrl(QrcodeActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, UIEventMessage.SUBTYPE_UI_LOADING_DIALOG, "initLoadingDialog", "update", data);
        } else {
            UICtrlMessageProxy.getInstance().sendCtrl(NFCChargeInitActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, UIEventMessage.SUBTYPE_UI_LOADING_DIALOG, "initLoadingDialog", "update", data);
        }
    }
}