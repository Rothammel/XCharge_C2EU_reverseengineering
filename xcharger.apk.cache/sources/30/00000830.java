package com.xcharge.charger.ui.adpter.c2.update;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.type.CHARGE_REFUSE_CAUSE;
import com.xcharge.charger.core.type.FIN_MODE;
import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.charger.data.bean.type.ADVERT_POLICY;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.device.c2.bean.PortRuntimeData;
import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.charger.ui.adapter.api.UIServiceProxy;
import com.xcharge.charger.ui.adapter.c2.R;
import com.xcharge.charger.ui.adapter.type.CHARGE_UI_STAGE;
import com.xcharge.charger.ui.adapter.type.UI_STATUS;
import com.xcharge.charger.ui.adpter.c2.UIService;
import com.xcharge.charger.ui.api.UICtrlMessageProxy;
import com.xcharge.charger.ui.api.bean.UICtrlMessage;
import com.xcharge.charger.ui.api.bean.UIEventMessage;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeFinActivity;
import com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeWaittingStartActivity;
import com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargingActivity;
import com.xcharge.charger.ui.c2.activity.charge.online.ChargeCompleteActivity;
import com.xcharge.charger.ui.c2.activity.charge.online.ChargingActivity;
import com.xcharge.charger.ui.c2.activity.charge.online.QrcodeActivity;
import com.xcharge.charger.ui.c2.activity.charge.online.ReservedActivity;
import com.xcharge.charger.ui.c2.activity.charge.online.WaittingStartChargeActivity;
import com.xcharge.charger.ui.c2.activity.data.Variate;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.http.util.TextUtils;

/* loaded from: classes.dex */
public class UpdateChargeView {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$ui$adapter$type$CHARGE_UI_STAGE;

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$ui$adapter$type$CHARGE_UI_STAGE() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$ui$adapter$type$CHARGE_UI_STAGE;
        if (iArr == null) {
            iArr = new int[CHARGE_UI_STAGE.valuesCustom().length];
            try {
                iArr[CHARGE_UI_STAGE.auth.ordinal()] = 2;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[CHARGE_UI_STAGE.billed.ordinal()] = 14;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[CHARGE_UI_STAGE.charging.ordinal()] = 8;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[CHARGE_UI_STAGE.delay.ordinal()] = 13;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[CHARGE_UI_STAGE.delay_wait.ordinal()] = 12;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[CHARGE_UI_STAGE.error_stop.ordinal()] = 16;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[CHARGE_UI_STAGE.inited.ordinal()] = 5;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[CHARGE_UI_STAGE.paid.ordinal()] = 11;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[CHARGE_UI_STAGE.plugin.ordinal()] = 6;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[CHARGE_UI_STAGE.prestop.ordinal()] = 9;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[CHARGE_UI_STAGE.ready.ordinal()] = 1;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[CHARGE_UI_STAGE.refuse.ordinal()] = 15;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[CHARGE_UI_STAGE.reserve.ordinal()] = 7;
            } catch (NoSuchFieldError e13) {
            }
            try {
                iArr[CHARGE_UI_STAGE.scan_advert.ordinal()] = 3;
            } catch (NoSuchFieldError e14) {
            }
            try {
                iArr[CHARGE_UI_STAGE.stopped.ordinal()] = 10;
            } catch (NoSuchFieldError e15) {
            }
            try {
                iArr[CHARGE_UI_STAGE.user_reserved.ordinal()] = 4;
            } catch (NoSuchFieldError e16) {
            }
            $SWITCH_TABLE$com$xcharge$charger$ui$adapter$type$CHARGE_UI_STAGE = iArr;
        }
        return iArr;
    }

    public void nfcChargeUIMode(Bundle data, Context context) {
        Log.d("UpdateChargeView.nfcChargeUIMode", "UI_STATUS:" + UIService.status);
        if (!UI_STATUS.wait_home.equals(UIService.status) && !UI_STATUS.home_present.equals(UIService.status) && !UI_STATUS.home_boot.equals(UIService.status)) {
            Utils.skipNfcQrcode(context);
            UIService.dataForWait = data;
            UIService.status = UI_STATUS.home_present;
        } else if (UI_STATUS.home_present.equals(UIService.status)) {
            UIService.dataForWait = data;
            CHARGE_UI_STAGE stage = CHARGE_UI_STAGE.valueOf(data.getString("stage"));
            Log.e("UpdateChargeView.nfcChargeUIMode", "CHARGE_UI_STAGE:" + stage);
            Variate.getInstance().setNFC(data.getBoolean("isNFC"));
            if (data.getBoolean("isNFC")) {
                NfcChargeUiStage(stage, context);
            } else {
                OnlineChargeUiStage(stage, context);
            }
        }
    }

    private void NfcChargeUiStage(CHARGE_UI_STAGE stage, Context context) {
        HashMap<String, Object> data = new HashMap<>();
        switch ($SWITCH_TABLE$com$xcharge$charger$ui$adapter$type$CHARGE_UI_STAGE()[stage.ordinal()]) {
            case 1:
                UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "nfcReady", UICtrlMessage.OPR_SKIP, null);
                return;
            case 2:
            case 4:
            case 6:
            case 15:
            default:
                return;
            case 3:
                UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "nfcScanAdvert", UICtrlMessage.OPR_SKIP, null);
                return;
            case 5:
                UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "nfcInited", UICtrlMessage.OPR_SKIP, null);
                return;
            case 7:
                if (!UIService.dataForWait.containsKey("waitStart")) {
                    data.put("waitStart", "60");
                } else {
                    data.put("waitStart", new StringBuilder(String.valueOf(UIService.dataForWait.getInt("waitStart"))).toString());
                }
                if (UIService.dataForWait.getBoolean("isClean")) {
                    data.put(ContentDB.AuthInfoTable.STATUS, "dismiss");
                } else {
                    data.put(ContentDB.AuthInfoTable.STATUS, "show");
                }
                UICtrlMessageProxy.getInstance().sendCtrl(NFCChargeWaittingStartActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, UIEventMessage.SUBTYPE_UI_LOADING_DIALOG, "mLoadingDialog", "update", data);
                return;
            case 8:
                if (Utils.getCurrentClassName(context).equals(NFCChargingActivity.class.getName())) {
                    data.put(ContentDB.AuthInfoTable.STATUS, "dismiss");
                    UICtrlMessageProxy.getInstance().sendCtrl(NFCChargingActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, UIEventMessage.SUBTYPE_UI_LOADING_DIALOG, "mShowHintDialog", "update", data);
                    return;
                }
                UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "nfcCharging", UICtrlMessage.OPR_SKIP, null);
                return;
            case 9:
                if (!UIService.dataForWait.containsKey("waitStop")) {
                    data.put("waitStop", "30");
                } else {
                    data.put("waitStop", new StringBuilder(String.valueOf(UIService.dataForWait.getInt("waitStop"))).toString());
                }
                if (UIService.dataForWait.getBoolean("isClean")) {
                    data.put(ContentDB.AuthInfoTable.STATUS, "dismiss");
                } else {
                    data.put(ContentDB.AuthInfoTable.STATUS, "show");
                }
                UICtrlMessageProxy.getInstance().sendCtrl(NFCChargingActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, UIEventMessage.SUBTYPE_UI_LOADING_DIALOG, "mShowHintDialog", "update", data);
                return;
            case 10:
                UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "nfcStopped", UICtrlMessage.OPR_SKIP, null);
                return;
            case PortRuntimeData.STATUS_EX_11 /* 11 */:
                BaseActivity.showSmallDialog(context.getString(R.string.nfc_consume_succ));
                UICtrlMessageProxy.getInstance().sendCtrl(NFCChargeFinActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, UIEventMessage.SUBTYPE_UI_TEXT_VIEW, "mTvChargingTimer", "update", null);
                return;
            case PortRuntimeData.STATUS_EX_12 /* 12 */:
                data.put("willDelayHandleNow", true);
                if (!UIService.dataForWait.containsKey("waitPlugout")) {
                    data.put("plugoutTime", "0");
                } else {
                    data.put("plugoutTime", new StringBuilder(String.valueOf(UIService.dataForWait.getInt("waitPlugout"))).toString());
                }
                UICtrlMessageProxy.getInstance().sendCtrl(NFCChargeFinActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, null, "initNFCChargeFin", "update", data);
                return;
            case 13:
                UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "nfcDelay", UICtrlMessage.OPR_SKIP, null);
                return;
            case 14:
                ArrayList<ContentItem> pullArrayList = RemoteSettingCacheProvider.getInstance().getAdvertContent(ADVERT_POLICY.pullAdvsite);
                if (pullArrayList == null || !Utils.listIsEmpty(pullArrayList) || !Utils.fileIsEmpty(Utils.platformAdPath(ADVERT_POLICY.pullAdvsite.getPolicy())) || !Utils.fileNameIsEqual(pullArrayList)) {
                    UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "nfcBilled", UICtrlMessage.OPR_SKIP, null);
                    return;
                }
                Log.i("pullAdvsite", RemoteSettingCacheProvider.getInstance().getRemoteSetting().getAdvertSetting().toJson());
                UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "nfcPullAdvsite", UICtrlMessage.OPR_SKIP, null);
                return;
            case 16:
                data.put("error", new StringBuilder(String.valueOf(UIService.dataForWait.getInt("error"))).toString());
                data.put("cnt", new StringBuilder(String.valueOf(UIService.dataForWait.getInt("cnt"))).toString());
                UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, null, "errorStop", "update", data);
                return;
        }
    }

    private void OnlineChargeUiStage(CHARGE_UI_STAGE stage, Context context) {
        HashMap<String, Object> data = new HashMap<>();
        switch ($SWITCH_TABLE$com$xcharge$charger$ui$adapter$type$CHARGE_UI_STAGE()[stage.ordinal()]) {
            case 1:
                UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "onlineReady", UICtrlMessage.OPR_SKIP, null);
                return;
            case 2:
            case PortRuntimeData.STATUS_EX_11 /* 11 */:
            default:
                return;
            case 3:
                UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "onlineScanAdvert", UICtrlMessage.OPR_SKIP, null);
                return;
            case 4:
                UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "onlineUserReserved", UICtrlMessage.OPR_SKIP, null);
                return;
            case 5:
                Variate.getInstance().setPlugin(false);
                UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "onlineInited", UICtrlMessage.OPR_SKIP, null);
                return;
            case 6:
                if (!Utils.getCurrentClassName(context).equals(WaittingStartChargeActivity.class.getName())) {
                    Variate.getInstance().setPlugin(true);
                    UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "onlinePlugin", UICtrlMessage.OPR_SKIP, null);
                    return;
                }
                data.put("pluginTime", new StringBuilder(String.valueOf(UIService.dataForWait.getInt("waitStart"))).toString());
                data.put("isGun", "yes");
                UICtrlMessageProxy.getInstance().sendCtrl(WaittingStartChargeActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, null, "initWaittingStartCharge", "update", data);
                return;
            case 7:
                if (!UIService.dataForWait.containsKey("waitStart")) {
                    data.put("waitStart", "60");
                } else {
                    data.put("waitStart", new StringBuilder(String.valueOf(UIService.dataForWait.getInt("waitStart"))).toString());
                }
                if (UIService.dataForWait.getBoolean("isClean")) {
                    data.put(ContentDB.AuthInfoTable.STATUS, "dismiss");
                } else {
                    data.put(ContentDB.AuthInfoTable.STATUS, "show");
                }
                UICtrlMessageProxy.getInstance().sendCtrl(WaittingStartChargeActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, UIEventMessage.SUBTYPE_UI_LOADING_DIALOG, "mLoadingDialog", "update", data);
                return;
            case 8:
                if (Utils.getCurrentClassName(context).equals(ChargingActivity.class.getName())) {
                    data.put(ContentDB.AuthInfoTable.STATUS, "dismiss");
                    UICtrlMessageProxy.getInstance().sendCtrl(ChargingActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, UIEventMessage.SUBTYPE_UI_LOADING_DIALOG, "mShowHintDialog", "update", data);
                    return;
                }
                UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "onlineCharging", UICtrlMessage.OPR_SKIP, null);
                return;
            case 9:
                if (!UIService.dataForWait.containsKey("waitStop")) {
                    data.put("waitStop", "30");
                } else {
                    data.put("waitStop", new StringBuilder(String.valueOf(UIService.dataForWait.getInt("waitStop"))).toString());
                }
                if (UIService.dataForWait.getBoolean("isClean")) {
                    data.put(ContentDB.AuthInfoTable.STATUS, "dismiss");
                } else {
                    data.put(ContentDB.AuthInfoTable.STATUS, "show");
                }
                UICtrlMessageProxy.getInstance().sendCtrl(ChargingActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, UIEventMessage.SUBTYPE_UI_LOADING_DIALOG, "mShowHintDialog", "update", data);
                return;
            case 10:
                UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "onlineStopped", UICtrlMessage.OPR_SKIP, null);
                return;
            case PortRuntimeData.STATUS_EX_12 /* 12 */:
                data.put("willDelayHandleNow", true);
                if (!UIService.dataForWait.containsKey("waitPlugout")) {
                    data.put("plugoutTime", "0");
                } else {
                    data.put("plugoutTime", new StringBuilder(String.valueOf(UIService.dataForWait.getInt("waitPlugout"))).toString());
                }
                UICtrlMessageProxy.getInstance().sendCtrl(ChargeCompleteActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, null, "initChargeComplete", "update", data);
                return;
            case 13:
                UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "onlineDelay", UICtrlMessage.OPR_SKIP, null);
                return;
            case 14:
                ArrayList<ContentItem> pullArrayList = RemoteSettingCacheProvider.getInstance().getAdvertContent(ADVERT_POLICY.pullAdvsite);
                if (pullArrayList == null || !Utils.listIsEmpty(pullArrayList) || !Utils.fileIsEmpty(Utils.platformAdPath(ADVERT_POLICY.pullAdvsite.getPolicy())) || !Utils.fileNameIsEqual(pullArrayList)) {
                    UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "onlineBilled", UICtrlMessage.OPR_SKIP, null);
                    return;
                }
                Log.i("pullAdvsite", RemoteSettingCacheProvider.getInstance().getRemoteSetting().getAdvertSetting().toJson());
                UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "onlinePullAdvsite", UICtrlMessage.OPR_SKIP, null);
                return;
            case 15:
                showErrorCause(UIService.dataForWait.getString("cause"), context);
                return;
            case 16:
                data.put("error", new StringBuilder(String.valueOf(UIService.dataForWait.getInt("error"))).toString());
                data.put("cnt", new StringBuilder(String.valueOf(UIService.dataForWait.getInt("cnt"))).toString());
                UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, null, "errorStop", "update", data);
                return;
        }
    }

    private void showErrorCause(String cause, Context context) {
        Log.d("UpdateChargeView.showErrorCause", "cause:" + cause);
        Bundle causeData = UIService.dataForWait;
        if (CHARGE_REFUSE_CAUSE.BAD_QRCODE.getCause().equals(cause)) {
            BaseActivity.showSmallDialog(context.getString(R.string.error_invalid_qrcode));
        } else if (CHARGE_REFUSE_CAUSE.USERGROUP_FORBIDDEN.getCause().equals(cause)) {
            BaseActivity.showSmallDialog(context.getString(R.string.error_usergroup_forbidden));
        } else if (CHARGE_REFUSE_CAUSE.BILL_UNPAID.getCause().equals(cause)) {
            String fee = null;
            if (causeData != null) {
                causeData.getString("bill_id");
                fee = causeData.getString(ChargeStopCondition.TYPE_FEE);
            }
            if (!TextUtils.isEmpty(fee)) {
                BaseActivity.showSmallDialog(context.getString(R.string.error_bill_unpaid_with_fee, CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(Integer.parseInt(fee) / 100.0d)), CountrySettingCacheProvider.getInstance().getMoneyDisp()));
            } else {
                BaseActivity.showSmallDialog(context.getString(R.string.error_bill_unpaid));
            }
        } else if (CHARGE_REFUSE_CAUSE.CHARGE_UNFINISHED.getCause().equals(cause)) {
            BaseActivity.showSmallDialog(context.getString(R.string.error_charge_unfinish));
        } else if (CHARGE_REFUSE_CAUSE.NOT_RESERVED.getCause().equals(cause)) {
            BaseActivity.showSmallDialog(context.getString(R.string.error_not_reserved));
        } else if (CHARGE_REFUSE_CAUSE.NOT_QUEUED.getCause().equals(cause)) {
            BaseActivity.showSmallDialog(context.getString(R.string.error_not_queued));
        } else if (CHARGE_REFUSE_CAUSE.RESERVE_UNDUE.getCause().equals(cause)) {
            BaseActivity.showSmallDialog(context.getString(R.string.error_reserved_undue));
        } else if (CHARGE_REFUSE_CAUSE.QUEUE_UNDUE.getCause().equals(cause)) {
            String queueOrder = null;
            if (causeData != null) {
                queueOrder = causeData.getString("queue_order");
            }
            int waitNum = -1;
            if (!TextUtils.isEmpty(queueOrder)) {
                waitNum = Integer.parseInt(queueOrder) - 1;
            }
            if (waitNum < 0) {
                BaseActivity.showSmallDialog(context.getString(R.string.error_queued_undue));
            } else {
                BaseActivity.showSmallDialog(context.getString(R.string.error_queued_undue, Integer.valueOf(waitNum)));
            }
        } else if (CHARGE_REFUSE_CAUSE.RESERVE_TIMEOUT.getCause().equals(cause)) {
            BaseActivity.showSmallDialog(context.getString(R.string.error_reserve_timeout));
        } else if (CHARGE_REFUSE_CAUSE.QUEUE_TIMEOUT.getCause().equals(cause)) {
            BaseActivity.showSmallDialog(context.getString(R.string.error_queued_timeout));
        } else if (CHARGE_REFUSE_CAUSE.BUSY.getCause().equals(cause)) {
            BaseActivity.showSmallDialog(context.getString(R.string.error_charge_busy));
        } else if (CHARGE_REFUSE_CAUSE.AUTH_TIMEOUT.getCause().equals(cause)) {
            BaseActivity.showSmallDialog(context.getString(R.string.error_auth_timeout));
        } else if (CHARGE_REFUSE_CAUSE.AUTH_REFUSE.getCause().equals(cause)) {
            BaseActivity.showSmallDialog(context.getString(R.string.error_auth_refuse));
        } else if (CHARGE_REFUSE_CAUSE.PORT_FORBIDEN.getCause().equals(cause)) {
            BaseActivity.showSmallDialog(context.getString(R.string.error_port_forbiden));
        } else if (!CHARGE_REFUSE_CAUSE.NO_FEERATE.getCause().equals(cause)) {
            if (CHARGE_REFUSE_CAUSE.EXCEPT.getCause().equals(cause)) {
                BaseActivity.showSmallDialog(context.getString(R.string.error_pile_except));
            } else if (CHARGE_REFUSE_CAUSE.BALANCE_INSUFFICIENT.getCause().equals(cause)) {
                BaseActivity.showSmallDialog(context.getString(R.string.error_no_balance));
            } else if (CHARGE_REFUSE_CAUSE.BAD_IDCARD.getCause().equals(cause)) {
                BaseActivity.showSmallDialog(context.getString(R.string.error_bad_card));
            } else if (CHARGE_REFUSE_CAUSE.NOT_PLUGGED.getCause().equals(cause)) {
                BaseActivity.showSmallDialog(context.getString(R.string.error_not_plugged));
            } else {
                BaseActivity.showSmallDialog(context.getString(R.string.error_unknown));
            }
        }
    }

    public void chargeUIWidgetCreate(UIEventMessage event) {
        if (event.getActivity().equals(QrcodeActivity.class.getName())) {
            if (event.getStatus().equals("resume")) {
                UIServiceProxy.getInstance().sendUIServiceEvent(UIServiceProxy.UI_SERIVCE_EVENT_UPDATE_QRCODE);
            }
        } else if (event.getActivity().equals(ReservedActivity.class.getName())) {
            if (event.getStatus().equals("create")) {
                sendReservedTime();
            }
        } else if (event.getActivity().equals(WaittingStartChargeActivity.class.getName())) {
            if (event.getStatus().equals("create")) {
                sendWaitPlugInTime();
            }
        } else if ((event.getActivity().equals(ChargeCompleteActivity.class.getName()) || event.getActivity().equals(NFCChargeFinActivity.class.getName())) && event.getStatus().equals("create")) {
            sendWaitPlugOutTime();
        }
    }

    public void chargeUIWidgetKey(UIEventMessage event, Context context) {
        String chargeId = ChargeStatusCacheProvider.getInstance().getPortStatus("1").getCharge_id();
        if (event.getActivity().equals(NFCChargeWaittingStartActivity.class.getName())) {
            if (event.getStatus().equals("up") && chargeId != null) {
                DCAPProxy.getInstance().finRequest(FIN_MODE.cancel, ChargeContentProxy.getInstance().getChargeBill(chargeId).getUser_type(), ChargeContentProxy.getInstance().getChargeBill(chargeId).getUser_code(), chargeId);
            }
        } else if (event.getActivity().equals(QrcodeActivity.class.getName())) {
            if (event.getStatus().equals("up")) {
                UIServiceProxy.getInstance().sendUIServiceEvent(UIServiceProxy.UI_SERIVCE_EVENT_UPDATE_QRCODE);
            }
        } else if (event.getActivity().equals(WaittingStartChargeActivity.class.getName()) && event.getStatus().equals("up") && chargeId != null) {
            DCAPProxy.getInstance().finRequest(FIN_MODE.cancel, ChargeContentProxy.getInstance().getChargeBill(chargeId).getUser_type(), ChargeContentProxy.getInstance().getChargeBill(chargeId).getUser_code(), chargeId);
        }
    }

    private void sendReservedTime() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("reservedTime", new StringBuilder(String.valueOf(UIService.dataForWait.getLong(ChargeStopCondition.TYPE_TIME))).toString());
        UICtrlMessageProxy.getInstance().sendCtrl(ReservedActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, null, "initReserved", "update", data);
    }

    public void sendWaitPlugInTime() {
        HashMap<String, Object> data = new HashMap<>();
        if (Variate.getInstance().isPlugin()) {
            if (!UIService.dataForWait.containsKey("waitStart")) {
                data.put("pluginTime", "15");
            } else {
                data.put("pluginTime", new StringBuilder(String.valueOf(UIService.dataForWait.getInt("waitStart"))).toString());
            }
            data.put("isGun", "yes");
        } else {
            if (!UIService.dataForWait.containsKey("waitPlugin")) {
                data.put("pluginTime", "90");
            } else {
                data.put("pluginTime", new StringBuilder(String.valueOf(UIService.dataForWait.getInt("waitPlugin"))).toString());
            }
            data.put("isGun", "no");
        }
        UICtrlMessageProxy.getInstance().sendCtrl(WaittingStartChargeActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, null, "initWaittingStartCharge", "update", data);
    }

    private void sendWaitPlugOutTime() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("willDelayHandleNow", Boolean.valueOf(UIService.dataForWait.getBoolean("willDelayHandleNow")));
        if (!UIService.dataForWait.containsKey("waitPlugout")) {
            data.put("plugoutTime", "0");
        } else {
            data.put("plugoutTime", new StringBuilder(String.valueOf(UIService.dataForWait.getInt("waitPlugout"))).toString());
        }
        if (Variate.getInstance().isNFC) {
            UICtrlMessageProxy.getInstance().sendCtrl(NFCChargeFinActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, null, "initNFCChargeFin", "update", data);
        } else {
            UICtrlMessageProxy.getInstance().sendCtrl(ChargeCompleteActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, null, "initChargeComplete", "update", data);
        }
    }
}