package com.xcharge.charger.protocol.family.xcloud.handler;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.xcharge.charger.core.type.CHARGE_REFUSE_CAUSE;
import com.xcharge.charger.core.type.FIN_MODE;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.CHARGE_STATUS;
import com.xcharge.charger.data.bean.type.CHARGE_STOP_CAUSE;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.bean.type.PHASE;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.data.proxy.PortChargeStatusObserver;
import com.xcharge.charger.data.proxy.PortStatusObserver;
import com.xcharge.charger.device.p005c2.bean.PortRuntimeData;
import com.xcharge.charger.p006ui.adapter.api.UIServiceProxy;
import com.xcharge.charger.p006ui.adapter.type.CHARGE_UI_STAGE;
import com.xcharge.charger.p006ui.adapter.type.UI_MODE;
import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.ConfirmChargeEnded;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.ConfirmChargeStarted;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestAutoStop;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestEndCharge;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestStartCharge;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestStopCharge;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.SendChargeQRCode;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportAutoStopResult;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportChargeCancelled;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportChargeEnded;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportChargePaused;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportChargeResumed;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportChargeStarted;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportChargeStatus;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportChargeStopped;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportDelayCountStarted;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportDelayFeeStarted;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportError;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportErrorRecovery;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportLocalChargeEnded;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportLocalChargeStarted;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportPlugStatus;
import com.xcharge.charger.protocol.family.xcloud.bean.device.RequestChargeQRCode;
import com.xcharge.charger.protocol.family.xcloud.bean.device.RequestChargeWithIDCard;
import com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway;
import com.xcharge.charger.protocol.family.xcloud.session.XCloudChargeSession;
import com.xcharge.charger.protocol.family.xcloud.session.XCloudRequestSession;
import com.xcharge.charger.protocol.family.xcloud.util.BillUtils;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.FormatUtils;
import com.xcharge.common.utils.HandlerTimer;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.MD5Utils;
import com.xcharge.common.utils.TimeUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class XCloudPortHandler {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$core$type$FIN_MODE = null;

    /* renamed from: $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE */
    private static /* synthetic */ int[] f85x26790b25 = null;
    private static final long INTERVAL_REPORT_HISTORY_BILL = 30000;
    private static final long INTERVAL_U3_WAIT_RESTART = 10000;
    public static final int MSG_BILL_PAID_EVENT = 73749;
    public static final int MSG_CLOUD_REQUEST = 73735;
    public static final int MSG_CLOUD_RESPONSE = 73736;
    public static final int MSG_FIN_CONFIRM = 73747;
    public static final int MSG_QRCODE_EXPIRE_TIMER = 73744;
    public static final int MSG_REPORT_CHARGE_TIMER = 73746;
    public static final int MSG_REPORT_HISTORY_BILL_TIMER = 73745;
    public static final int MSG_REQUEST_QRCODE = 73737;
    public static final int MSG_REQUEST_TIMEOUT = 73733;
    public static final int MSG_REQUSET_RESEND = 73734;
    public static final int MSG_SEND_FAIL = 73732;
    public static final int MSG_SEND_OK = 73731;
    public static final int MSG_SERVICE_ACTIVE = 73729;
    public static final int MSG_SERVICE_DEACTIVE = 73730;
    public static final int MSG_U3_WAIT_RESTART_TIMER = 73748;
    public static final int TIMEOUT_REPORT_CHARGE = 60000;
    private BillPayObserver billPayObserver = null;
    private XCloudChargeSession chargeSession = null;
    private Context context = null;
    private MsgHandler handler = null;
    /* access modifiers changed from: private */
    public HandlerTimer handlerTimer = null;
    /* access modifiers changed from: private */
    public boolean isActive = false;
    /* access modifiers changed from: private */
    public String port = null;
    private PortChargeStatusObserver portChargeStatusObserver = null;
    private PortStatusObserver portRecoverableFaultObserver = null;
    private PortStatusObserver portStatusObserver = null;

    /* renamed from: sn */
    private String f86sn = null;
    /* access modifiers changed from: private */
    public CHARGE_STATUS status = CHARGE_STATUS.IDLE;
    private HandlerThread thread = null;

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$core$type$FIN_MODE() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$core$type$FIN_MODE;
        if (iArr == null) {
            iArr = new int[FIN_MODE.values().length];
            try {
                iArr[FIN_MODE.busy.ordinal()] = 5;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[FIN_MODE.cancel.ordinal()] = 3;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[FIN_MODE.car.ordinal()] = 8;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[FIN_MODE.error.ordinal()] = 9;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[FIN_MODE.nfc.ordinal()] = 12;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[FIN_MODE.no_feerate.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[FIN_MODE.normal.ordinal()] = 1;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[FIN_MODE.plugin_timeout.ordinal()] = 7;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[FIN_MODE.port_forbiden.ordinal()] = 4;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[FIN_MODE.refuse.ordinal()] = 13;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[FIN_MODE.remote.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[FIN_MODE.reserve_error.ordinal()] = 10;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[FIN_MODE.timeout.ordinal()] = 2;
            } catch (NoSuchFieldError e13) {
            }
            $SWITCH_TABLE$com$xcharge$charger$core$type$FIN_MODE = iArr;
        }
        return iArr;
    }

    /* renamed from: $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE */
    static /* synthetic */ int[] m22x26790b25() {
        int[] iArr = f85x26790b25;
        if (iArr == null) {
            iArr = new int[CHARGE_STOP_CAUSE.values().length];
            try {
                iArr[CHARGE_STOP_CAUSE.car.ordinal()] = 7;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.fault.ordinal()] = 12;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.full.ordinal()] = 8;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.local_user.ordinal()] = 3;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.no_balance.ordinal()] = 9;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.plugout.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.reboot.ordinal()] = 11;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.remote_user.ordinal()] = 4;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.system_user.ordinal()] = 5;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.undefined.ordinal()] = 1;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.user.ordinal()] = 2;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.user_set.ordinal()] = 10;
            } catch (NoSuchFieldError e12) {
            }
            f85x26790b25 = iArr;
        }
        return iArr;
    }

    private static class BillPayObserver extends ContentObserver {
        private Handler handler = null;

        public BillPayObserver(Handler handler2) {
            super(handler2);
            this.handler = handler2;
        }

        public void onChange(boolean selfChange, Uri uri) {
            Log.d("XCloudPortHandler.BillPayObserver.onChange", "selfChange: " + selfChange + ", uri: " + uri.toString());
            super.onChange(selfChange, uri);
            this.handler.obtainMessage(XCloudPortHandler.MSG_BILL_PAID_EVENT, uri).sendToTarget();
        }
    }

    private class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r17) {
            /*
                r16 = this;
                r7 = 0
                r2 = 0
                r0 = r17
                int r11 = r0.what     // Catch:{ Exception -> 0x00ab }
                switch(r11) {
                    case 73729: goto L_0x000d;
                    case 73730: goto L_0x00e6;
                    case 73731: goto L_0x0113;
                    case 73732: goto L_0x0124;
                    case 73733: goto L_0x0135;
                    case 73734: goto L_0x0184;
                    case 73735: goto L_0x015e;
                    case 73736: goto L_0x016d;
                    case 73737: goto L_0x0193;
                    case 73744: goto L_0x01b8;
                    case 73745: goto L_0x0235;
                    case 73746: goto L_0x022c;
                    case 73747: goto L_0x01eb;
                    case 73748: goto L_0x024f;
                    case 73749: goto L_0x0399;
                    case 131073: goto L_0x02e8;
                    case 139265: goto L_0x0321;
                    default: goto L_0x0009;
                }
            L_0x0009:
                super.handleMessage(r17)
                return
            L_0x000d:
                java.lang.String r11 = "XCloudPortHandler.handleMessage"
                java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = "service actived !!! port: "
                r12.<init>(r13)     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r13 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = r13.port     // Catch:{ Exception -> 0x00ab }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x00ab }
                android.util.Log.i(r11, r12)     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r12 = 1
                r11.isActive = r12     // Catch:{ Exception -> 0x00ab }
                com.xcharge.charger.data.provider.HardwareStatusCacheProvider r11 = com.xcharge.charger.data.provider.HardwareStatusCacheProvider.getInstance()     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r12 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                java.lang.String r12 = r12.port     // Catch:{ Exception -> 0x00ab }
                boolean r11 = r11.hasDeviceErrors(r12)     // Catch:{ Exception -> 0x00ab }
                if (r11 == 0) goto L_0x00dc
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r11.reportError()     // Catch:{ Exception -> 0x00ab }
            L_0x004a:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r11.reportPlugStatus()     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                com.xcharge.charger.protocol.family.xcloud.session.XCloudChargeSession r2 = r11.getChargeSession()     // Catch:{ Exception -> 0x00ab }
                java.lang.String r11 = r2.getCharge_id()     // Catch:{ Exception -> 0x00ab }
                boolean r11 = android.text.TextUtils.isEmpty(r11)     // Catch:{ Exception -> 0x00ab }
                if (r11 != 0) goto L_0x0098
                java.lang.String r11 = r2.getUser_type()     // Catch:{ Exception -> 0x00ab }
                boolean r11 = android.text.TextUtils.isEmpty(r11)     // Catch:{ Exception -> 0x00ab }
                if (r11 != 0) goto L_0x0098
                com.xcharge.charger.data.bean.type.CHARGE_STATUS r11 = com.xcharge.charger.data.bean.type.CHARGE_STATUS.CHARGING     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r12 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                com.xcharge.charger.data.bean.type.CHARGE_STATUS r12 = r12.status     // Catch:{ Exception -> 0x00ab }
                boolean r11 = r11.equals(r12)     // Catch:{ Exception -> 0x00ab }
                if (r11 == 0) goto L_0x0098
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r11.reportChargeStatus()     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                com.xcharge.common.utils.HandlerTimer r11 = r11.handlerTimer     // Catch:{ Exception -> 0x00ab }
                int r12 = r2.getIntervalChargeReport()     // Catch:{ Exception -> 0x00ab }
                long r12 = (long) r12     // Catch:{ Exception -> 0x00ab }
                r14 = 73746(0x12012, float:1.0334E-40)
                r15 = 0
                r11.startTimer(r12, r14, r15)     // Catch:{ Exception -> 0x00ab }
            L_0x0098:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                com.xcharge.common.utils.HandlerTimer r11 = r11.handlerTimer     // Catch:{ Exception -> 0x00ab }
                r12 = 30000(0x7530, double:1.4822E-319)
                r14 = 73745(0x12011, float:1.03339E-40)
                r15 = 0
                r11.startTimer(r12, r14, r15)     // Catch:{ Exception -> 0x00ab }
                goto L_0x0009
            L_0x00ab:
                r4 = move-exception
                java.lang.String r11 = "XCloudPortHandler.handleMessage"
                java.lang.StringBuilder r12 = new java.lang.StringBuilder
                java.lang.String r13 = "except: "
                r12.<init>(r13)
                java.lang.String r13 = android.util.Log.getStackTraceString(r4)
                java.lang.StringBuilder r12 = r12.append(r13)
                java.lang.String r12 = r12.toString()
                android.util.Log.e(r11, r12)
                java.lang.StringBuilder r11 = new java.lang.StringBuilder
                java.lang.String r12 = "XCloudPortHandler handleMessage exception: "
                r11.<init>(r12)
                java.lang.String r12 = android.util.Log.getStackTraceString(r4)
                java.lang.StringBuilder r11 = r11.append(r12)
                java.lang.String r11 = r11.toString()
                com.xcharge.common.utils.LogUtils.syslog(r11)
                goto L_0x0009
            L_0x00dc:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r12 = 0
                r11.reportErrorRecovery(r12)     // Catch:{ Exception -> 0x00ab }
                goto L_0x004a
            L_0x00e6:
                java.lang.String r11 = "XCloudPortHandler.handleMessage"
                java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = "service deactived !!! port: "
                r12.<init>(r13)     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r13 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = r13.port     // Catch:{ Exception -> 0x00ab }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x00ab }
                android.util.Log.i(r11, r12)     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r12 = 0
                r11.isActive = r12     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r11.clearPortActiveStatus()     // Catch:{ Exception -> 0x00ab }
                goto L_0x0009
            L_0x0113:
                r0 = r17
                java.lang.Object r11 = r0.obj     // Catch:{ Exception -> 0x00ab }
                r0 = r11
                com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage r0 = (com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage) r0     // Catch:{ Exception -> 0x00ab }
                r7 = r0
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r11.handleMsgSended(r7)     // Catch:{ Exception -> 0x00ab }
                goto L_0x0009
            L_0x0124:
                r0 = r17
                java.lang.Object r11 = r0.obj     // Catch:{ Exception -> 0x00ab }
                r0 = r11
                com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage r0 = (com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage) r0     // Catch:{ Exception -> 0x00ab }
                r7 = r0
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r11.handleSendFail(r7)     // Catch:{ Exception -> 0x00ab }
                goto L_0x0009
            L_0x0135:
                r0 = r17
                java.lang.Object r11 = r0.obj     // Catch:{ Exception -> 0x00ab }
                r0 = r11
                com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage r0 = (com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage) r0     // Catch:{ Exception -> 0x00ab }
                r7 = r0
                java.lang.String r11 = "XCloudPortHandler.handleMessage"
                java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = "timeout for wait xcloud response for request: "
                r12.<init>(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = r7.toJson()     // Catch:{ Exception -> 0x00ab }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x00ab }
                android.util.Log.w(r11, r12)     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r11.handleSendFail(r7)     // Catch:{ Exception -> 0x00ab }
                goto L_0x0009
            L_0x015e:
                r0 = r17
                java.lang.Object r3 = r0.obj     // Catch:{ Exception -> 0x00ab }
                com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage r3 = (com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage) r3     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r11.handleCloudRequest(r3)     // Catch:{ Exception -> 0x00ab }
                goto L_0x0009
            L_0x016d:
                r0 = r17
                java.lang.Object r10 = r0.obj     // Catch:{ Exception -> 0x00ab }
                com.xcharge.charger.protocol.family.xcloud.session.XCloudRequestSession r10 = (com.xcharge.charger.protocol.family.xcloud.session.XCloudRequestSession) r10     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage r12 = r10.getRequest()     // Catch:{ Exception -> 0x00ab }
                com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage r13 = r10.getResponse()     // Catch:{ Exception -> 0x00ab }
                r11.handleCloudResponse(r12, r13)     // Catch:{ Exception -> 0x00ab }
                goto L_0x0009
            L_0x0184:
                r0 = r17
                java.lang.Object r6 = r0.obj     // Catch:{ Exception -> 0x00ab }
                com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage r6 = (com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage) r6     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r11.resendRequest(r6)     // Catch:{ Exception -> 0x00ab }
                goto L_0x0009
            L_0x0193:
                java.lang.String r11 = "XCloudPortHandler.handleMessage"
                java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = "request qrcode, port: "
                r12.<init>(r13)     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r13 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = r13.port     // Catch:{ Exception -> 0x00ab }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x00ab }
                android.util.Log.i(r11, r12)     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r11.requestQrcode()     // Catch:{ Exception -> 0x00ab }
                goto L_0x0009
            L_0x01b8:
                java.lang.String r11 = "XCloudPortHandler.handleMessage"
                java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = "qrcode expired, try to update it !!! port: "
                r12.<init>(r13)     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r13 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = r13.port     // Catch:{ Exception -> 0x00ab }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x00ab }
                android.util.Log.w(r11, r12)     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                com.xcharge.common.utils.HandlerTimer r11 = r11.handlerTimer     // Catch:{ Exception -> 0x00ab }
                r12 = 73744(0x12010, float:1.03337E-40)
                r11.stopTimer(r12)     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r11.requestQrcode()     // Catch:{ Exception -> 0x00ab }
                goto L_0x0009
            L_0x01eb:
                r0 = r17
                java.lang.Object r5 = r0.obj     // Catch:{ Exception -> 0x00ab }
                com.xcharge.charger.protocol.family.xcloud.bean.FinCause r5 = (com.xcharge.charger.protocol.family.xcloud.bean.FinCause) r5     // Catch:{ Exception -> 0x00ab }
                java.lang.String r11 = "XCloudPortHandler.handleMessage"
                java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = "handle fin confirm !!! port: "
                r12.<init>(r13)     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r13 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = r13.port     // Catch:{ Exception -> 0x00ab }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = ", fin cause: "
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = r5.toJson()     // Catch:{ Exception -> 0x00ab }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x00ab }
                android.util.Log.i(r11, r12)     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                com.xcharge.charger.core.type.FIN_MODE r12 = r5.getMode()     // Catch:{ Exception -> 0x00ab }
                com.xcharge.charger.data.bean.ErrorCode r13 = r5.getError()     // Catch:{ Exception -> 0x00ab }
                r11.handleFinConfirm(r12, r13)     // Catch:{ Exception -> 0x00ab }
                goto L_0x0009
            L_0x022c:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r11.handleChargeStatusPeriodicReport()     // Catch:{ Exception -> 0x00ab }
                goto L_0x0009
            L_0x0235:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x03a8 }
                r11.reportHistoryBill()     // Catch:{ Exception -> 0x03a8 }
            L_0x023c:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                com.xcharge.common.utils.HandlerTimer r11 = r11.handlerTimer     // Catch:{ Exception -> 0x00ab }
                r12 = 30000(0x7530, double:1.4822E-319)
                r14 = 73745(0x12011, float:1.03339E-40)
                r15 = 0
                r11.startTimer(r12, r14, r15)     // Catch:{ Exception -> 0x00ab }
                goto L_0x0009
            L_0x024f:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                com.xcharge.charger.protocol.family.xcloud.session.XCloudChargeSession r2 = r11.getChargeSession()     // Catch:{ Exception -> 0x00ab }
                r0 = r17
                java.lang.Object r8 = r0.obj     // Catch:{ Exception -> 0x00ab }
                com.xcharge.charger.protocol.family.xcloud.session.XCloudChargeSession r8 = (com.xcharge.charger.protocol.family.xcloud.session.XCloudChargeSession) r8     // Catch:{ Exception -> 0x00ab }
                java.lang.String r11 = "XCloudPortHandler.handleMessage"
                java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = "wait for u3 restart charge timeout !!! port: "
                r12.<init>(r13)     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r13 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = r13.port     // Catch:{ Exception -> 0x00ab }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = ", now session: "
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = r2.toJson()     // Catch:{ Exception -> 0x00ab }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = ", old session: "
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = r8.toJson()     // Catch:{ Exception -> 0x00ab }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x00ab }
                android.util.Log.w(r11, r12)     // Catch:{ Exception -> 0x00ab }
                com.xcharge.charger.data.bean.type.CHARGE_STATUS r11 = com.xcharge.charger.data.bean.type.CHARGE_STATUS.IDLE     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r12 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                com.xcharge.charger.data.bean.type.CHARGE_STATUS r12 = r12.status     // Catch:{ Exception -> 0x00ab }
                boolean r11 = r11.equals(r12)     // Catch:{ Exception -> 0x00ab }
                if (r11 != 0) goto L_0x02b5
                com.xcharge.charger.data.bean.type.CHARGE_STATUS r11 = com.xcharge.charger.data.bean.type.CHARGE_STATUS.CHARGE_START_WAITTING     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r12 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                com.xcharge.charger.data.bean.type.CHARGE_STATUS r12 = r12.status     // Catch:{ Exception -> 0x00ab }
                boolean r11 = r11.equals(r12)     // Catch:{ Exception -> 0x00ab }
                if (r11 == 0) goto L_0x0009
            L_0x02b5:
                java.lang.String r11 = r2.getUser_type()     // Catch:{ Exception -> 0x00ab }
                java.lang.String r12 = r8.getUser_type()     // Catch:{ Exception -> 0x00ab }
                boolean r11 = r11.equals(r12)     // Catch:{ Exception -> 0x00ab }
                if (r11 == 0) goto L_0x0009
                java.lang.String r11 = r2.getUser_code()     // Catch:{ Exception -> 0x00ab }
                java.lang.String r12 = r8.getUser_code()     // Catch:{ Exception -> 0x00ab }
                boolean r11 = r11.equals(r12)     // Catch:{ Exception -> 0x00ab }
                if (r11 == 0) goto L_0x0009
                java.lang.String r11 = r2.getCharge_id()     // Catch:{ Exception -> 0x00ab }
                java.lang.String r12 = r8.getCharge_id()     // Catch:{ Exception -> 0x00ab }
                boolean r11 = r11.equals(r12)     // Catch:{ Exception -> 0x00ab }
                if (r11 == 0) goto L_0x0009
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r11.clearChargeSession()     // Catch:{ Exception -> 0x00ab }
                goto L_0x0009
            L_0x02e8:
                r0 = r17
                java.lang.Object r9 = r0.obj     // Catch:{ Exception -> 0x00ab }
                android.net.Uri r9 = (android.net.Uri) r9     // Catch:{ Exception -> 0x00ab }
                java.lang.String r11 = "XCloudPortHandler.handleMessage"
                java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = "port charge status changed, port: "
                r12.<init>(r13)     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r13 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = r13.port     // Catch:{ Exception -> 0x00ab }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = ", uri: "
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = r9.toString()     // Catch:{ Exception -> 0x00ab }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x00ab }
                android.util.Log.d(r11, r12)     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r11.handlePortChargeStatusChanged(r9)     // Catch:{ Exception -> 0x00ab }
                goto L_0x0009
            L_0x0321:
                r0 = r17
                java.lang.Object r9 = r0.obj     // Catch:{ Exception -> 0x00ab }
                android.net.Uri r9 = (android.net.Uri) r9     // Catch:{ Exception -> 0x00ab }
                java.lang.String r11 = r9.getPath()     // Catch:{ Exception -> 0x00ab }
                java.lang.String r12 = "ports/fault/"
                boolean r11 = r11.contains(r12)     // Catch:{ Exception -> 0x00ab }
                if (r11 == 0) goto L_0x0366
                java.lang.String r11 = "XCloudPortHandler.handleMessage"
                java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = "port recoverable faults changed, port: "
                r12.<init>(r13)     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r13 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = r13.port     // Catch:{ Exception -> 0x00ab }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = ", uri: "
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = r9.toString()     // Catch:{ Exception -> 0x00ab }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x00ab }
                android.util.Log.d(r11, r12)     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r11.handlePortRecoverableFaultChanged(r9)     // Catch:{ Exception -> 0x00ab }
                goto L_0x0009
            L_0x0366:
                java.lang.String r11 = "XCloudPortHandler.handleMessage"
                java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = "port status changed, port: "
                r12.<init>(r13)     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r13 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = r13.port     // Catch:{ Exception -> 0x00ab }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = ", uri: "
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r13 = r9.toString()     // Catch:{ Exception -> 0x00ab }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x00ab }
                java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x00ab }
                android.util.Log.d(r11, r12)     // Catch:{ Exception -> 0x00ab }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r11.handlePortStatusChanged(r9)     // Catch:{ Exception -> 0x00ab }
                goto L_0x0009
            L_0x0399:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler r12 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.this     // Catch:{ Exception -> 0x00ab }
                r0 = r17
                java.lang.Object r11 = r0.obj     // Catch:{ Exception -> 0x00ab }
                android.net.Uri r11 = (android.net.Uri) r11     // Catch:{ Exception -> 0x00ab }
                r12.handleBillPaidEvent(r11)     // Catch:{ Exception -> 0x00ab }
                goto L_0x0009
            L_0x03a8:
                r11 = move-exception
                goto L_0x023c
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.family.xcloud.handler.XCloudPortHandler.MsgHandler.handleMessage(android.os.Message):void");
        }
    }

    public void init(Context context2, String port2) {
        this.context = context2;
        this.f86sn = HardwareStatusCacheProvider.getInstance().getSn();
        this.port = port2;
        this.thread = new HandlerThread("XCloudPortHandler#" + this.port, 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context2);
        this.portRecoverableFaultObserver = new PortStatusObserver(this.context, this.port, this.handler);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/fault/" + this.port), true, this.portRecoverableFaultObserver);
        this.portStatusObserver = new PortStatusObserver(this.context, this.port, this.handler);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/" + this.port), true, this.portStatusObserver);
        this.portChargeStatusObserver = new PortChargeStatusObserver(this.context, this.port, this.handler);
        this.context.getContentResolver().registerContentObserver(ChargeStatusCacheProvider.getInstance().getUriFor("ports/" + this.port), true, this.portChargeStatusObserver);
        this.billPayObserver = new BillPayObserver(this.handler);
        this.context.getContentResolver().registerContentObserver(ChargeContentProxy.getInstance().getUriFor("pay"), true, this.billPayObserver);
    }

    public void destroy() {
        this.context.getContentResolver().unregisterContentObserver(this.portChargeStatusObserver);
        this.context.getContentResolver().unregisterContentObserver(this.portStatusObserver);
        this.context.getContentResolver().unregisterContentObserver(this.portRecoverableFaultObserver);
        this.context.getContentResolver().unregisterContentObserver(this.billPayObserver);
        this.handlerTimer.destroy();
        this.handler.removeMessages(73729);
        this.handler.removeMessages(73730);
        this.handler.removeMessages(73731);
        this.handler.removeMessages(73732);
        this.handler.removeMessages(73733);
        this.handler.removeMessages(73734);
        this.handler.removeMessages(73735);
        this.handler.removeMessages(73736);
        this.handler.removeMessages(73737);
        this.handler.removeMessages(73744);
        this.handler.removeMessages(73745);
        this.handler.removeMessages(73746);
        this.handler.removeMessages(73747);
        this.handler.removeMessages(MSG_U3_WAIT_RESTART_TIMER);
        this.handler.removeMessages(PortChargeStatusObserver.MSG_PORT_CHARGE_STATUS_CHANGE);
        this.handler.removeMessages(PortStatusObserver.MSG_PORT_STATUS_CHANGE);
        this.handler.removeMessages(MSG_BILL_PAID_EVENT);
        this.thread.quit();
    }

    /* access modifiers changed from: private */
    public void clearPortActiveStatus() {
        this.handlerTimer.stopTimer(73746);
        this.handlerTimer.stopTimer(73745);
        this.handlerTimer.stopTimer(MSG_U3_WAIT_RESTART_TIMER);
        this.handler.removeMessages(73731);
        this.handler.removeMessages(73732);
        this.handler.removeMessages(73733);
        this.handler.removeMessages(73735);
        this.handler.removeMessages(73736);
        this.handler.removeMessages(73737);
        this.handler.removeMessages(73747);
    }

    public Message obtainMessage(int what) {
        return this.handler.obtainMessage(what);
    }

    public Message obtainMessage(int what, Object obj) {
        return this.handler.obtainMessage(what, obj);
    }

    public boolean sendMessage(Message msg) {
        return this.handler.sendMessage(msg);
    }

    public XCloudChargeSession getChargeSession() {
        if (this.chargeSession == null) {
            this.chargeSession = new XCloudChargeSession();
        }
        return this.chargeSession;
    }

    /* access modifiers changed from: private */
    public void clearChargeSession() {
        this.chargeSession = null;
    }

    public String getPort(String chargeId) {
        if (chargeId.equals(getChargeSession().getCharge_id())) {
            return this.port;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void resendRequest(XCloudMessage request) {
        String chargeId;
        ChargeBill chargeBill;
        if (XCloudMessage.ReportChargeStarted.equals(request.getMessageName()) || XCloudMessage.ReportLocalChargeStarted.equals(request.getMessageName())) {
            if (CHARGE_STATUS.CHARGING.equals(this.status)) {
                if (XCloudMessage.ReportChargeStarted.equals(request.getMessageName())) {
                    chargeId = String.valueOf(((ReportChargeStarted) request.getBody()).getBillId());
                } else {
                    chargeId = request.getLocalChargeId();
                }
                if (!getChargeSession().getCharge_id().equals(chargeId) || (chargeBill = ChargeContentProxy.getInstance().getChargeBill(getChargeSession().getCharge_id())) == null || chargeBill.getStart_report_flag() == 1) {
                    return;
                }
            } else {
                return;
            }
        }
        if (XCloudMessage.ReportChargeStopped.equals(request.getMessageName())) {
            if (CHARGE_STATUS.CHARGE_STOP_WAITTING.equals(this.status)) {
                if (!TextUtils.isEmpty(request.getLocalChargeId())) {
                    if (!request.getLocalChargeId().equals(getChargeSession().getCharge_id())) {
                        return;
                    }
                } else if (((ReportChargeStopped) request.getBody()).getBillId() != Long.parseLong(getChargeSession().getCharge_id())) {
                    return;
                }
                ChargeBill chargeBill2 = ChargeContentProxy.getInstance().getChargeBill(getChargeSession().getCharge_id());
                if (chargeBill2 == null || chargeBill2.getStop_report_flag() == 1) {
                    return;
                }
            } else {
                return;
            }
        }
        if (XCloudMessage.RequestChargeQRCode.equals(request.getMessageName())) {
            if (this.isActive) {
                RequestChargeQRCode requestChargeQRCode = (RequestChargeQRCode) request.getBody();
                requestChargeQRCode.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
                request.setData(requestChargeQRCode.toJson());
            } else {
                return;
            }
        }
        request.setResendCnt(request.getResendCnt() + 1);
        if (!sendMessage(request)) {
            this.handler.sendMessage(this.handler.obtainMessage(73732, request));
        }
    }

    /* access modifiers changed from: private */
    public void requestQrcode() {
        RequestChargeQRCode requestChargeQRCode = new RequestChargeQRCode();
        long sessionId = XCloudProtocolAgent.getInstance().genSid();
        requestChargeQRCode.setPort(Integer.parseInt(this.port));
        requestChargeQRCode.setSid(Long.valueOf(sessionId));
        requestChargeQRCode.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        XCloudMessage msg = new XCloudMessage();
        msg.setMessageName(XCloudMessage.RequestChargeQRCode);
        msg.setSrcId(this.f86sn);
        msg.setBody(requestChargeQRCode);
        msg.setSessionId(String.valueOf(sessionId));
        msg.setPort(this.port);
        msg.setData(requestChargeQRCode.toJson());
        if (!sendMessage(msg)) {
            this.handler.sendMessage(this.handler.obtainMessage(73732, msg));
        } else {
            ChargeStatusCacheProvider.getInstance().updatePortQrcodeContent(this.port, (String) null);
        }
    }

    public void reportPlugStatus() {
        ReportPlugStatus reportPlugStatus = new ReportPlugStatus();
        reportPlugStatus.setSid(Long.valueOf(XCloudProtocolAgent.getInstance().genSid()));
        reportPlugStatus.setPort(Integer.valueOf(Integer.parseInt(this.port)));
        reportPlugStatus.setConnected(HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port));
        reportPlugStatus.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        XCloudMessage report = new XCloudMessage();
        report.setMessageName(XCloudMessage.ReportPlugStatus);
        report.setSrcId(this.f86sn);
        report.setBody(reportPlugStatus);
        report.setData(reportPlugStatus.toJson());
        report.setPort(this.port);
        sendMessage(report);
    }

    /* access modifiers changed from: private */
    public void handlePortStatusChanged(Uri uri) {
        if (uri.getPath().contains("ports/" + this.port + "/plugin")) {
            reportPlugStatus();
        }
    }

    /* access modifiers changed from: private */
    public void handlePortRecoverableFaultChanged(Uri uri) {
        List<String> segments = uri.getPathSegments();
        int size = segments.size();
        String last = segments.get(size - 1);
        if (TextUtils.isDigitsOnly(last)) {
            String status2 = segments.get(size - 2);
            if ("new".equals(status2)) {
                reportError();
            } else if ("remove".equals(status2)) {
                int errorCode = Integer.parseInt(last);
                if (errorCode == 200) {
                    reportErrorRecovery((ErrorCode) null);
                } else {
                    reportErrorRecovery(new ErrorCode(errorCode));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handlePortChargeStatusChanged(Uri uri) {
        double doubleValue;
        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port);
        Log.d("XCloudPortHandler.handlePortChargeStatusChanged", "now port status: " + portStatus.toJson());
        CHARGE_STATUS chargeStatus = portStatus.getChargeStatus();
        XCloudChargeSession chargeSession2 = getChargeSession();
        String chargeId = chargeSession2.getCharge_id();
        if (!TextUtils.isEmpty(chargeId) && !TextUtils.isEmpty(chargeSession2.getUser_type()) && chargeId.equals(portStatus.getCharge_id()) && (CHARGE_STATUS.CHARGING.equals(chargeStatus) || CHARGE_STATUS.CHARGE_STOP_WAITTING.equals(chargeStatus))) {
            chargeSession2.setChargeStartTime(portStatus.getChargeStartTime());
            chargeSession2.setChargeStopTime(portStatus.getChargeStopTime());
            if (portStatus.getPower() == null) {
                doubleValue = 0.0d;
            } else {
                doubleValue = portStatus.getPower().doubleValue();
            }
            chargeSession2.setPower(doubleValue);
            chargeSession2.setChargeStopCause(portStatus.getChargeStopCause());
        }
        if (!this.status.equals(chargeStatus)) {
            if (chargeStatus.equals(CHARGE_STATUS.CHARGE_START_WAITTING)) {
                Log.i("XCloudPortHandler.handlePortChargeStatusChanged", "enter wait charge status !!!");
                XCloudChargeSession chargeSession3 = getChargeSession();
                String chargeId2 = portStatus.getCharge_id();
                ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(chargeId2);
                if (chargeBill != null) {
                    chargeSession3.setCharge_id(chargeId2);
                    chargeSession3.setUser_type(chargeBill.getUser_type());
                    chargeSession3.setUser_code(chargeBill.getUser_code());
                    chargeSession3.setInit_type(chargeBill.getInit_type());
                    chargeSession3.setUser_tc_type(chargeBill.getUser_tc_type());
                    chargeSession3.setUser_tc_value(chargeBill.getUser_tc_value());
                    chargeSession3.setUser_balance((int) (chargeBill.getUser_balance() & XMSZHead.ID_BROADCAST));
                    chargeSession3.setIs_free(chargeBill.getIs_free());
                    chargeSession3.setBinded_user(chargeBill.getBinded_user());
                    chargeSession3.setCharge_platform(chargeBill.getCharge_platform());
                } else {
                    Log.w("XCloudPortHandler.handlePortChargeStatusChanged", "failed to query info for charge: " + chargeId2);
                }
            } else if (chargeStatus.equals(CHARGE_STATUS.CHARGING)) {
                Log.i("XCloudPortHandler.handlePortChargeStatusChanged", "enter charging status !!!");
                XCloudChargeSession chargeSession4 = getChargeSession();
                if (TextUtils.isEmpty(chargeSession4.getCharge_id()) || TextUtils.isEmpty(chargeSession4.getUser_type())) {
                    String chargeId3 = portStatus.getCharge_id();
                    ChargeBill chargeBill2 = ChargeContentProxy.getInstance().getChargeBill(chargeId3);
                    if (chargeBill2 != null) {
                        chargeSession4.setCharge_id(chargeId3);
                        chargeSession4.setUser_type(chargeBill2.getUser_type());
                        chargeSession4.setUser_code(chargeBill2.getUser_code());
                        chargeSession4.setInit_type(chargeBill2.getInit_type());
                        chargeSession4.setUser_tc_type(chargeBill2.getUser_tc_type());
                        chargeSession4.setUser_tc_value(chargeBill2.getUser_tc_value());
                        chargeSession4.setUser_balance((int) (chargeBill2.getUser_balance() & XMSZHead.ID_BROADCAST));
                        chargeSession4.setIs_free(chargeBill2.getIs_free());
                        chargeSession4.setBinded_user(chargeBill2.getBinded_user());
                        chargeSession4.setCharge_platform(chargeBill2.getCharge_platform());
                    } else {
                        Log.w("XCloudPortHandler.handlePortChargeStatusChanged", "failed to query info for charge: " + chargeId3);
                    }
                }
                NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeSession4.getUser_type());
                if (NFC_CARD_TYPE.U1.equals(nfcCardType) || NFC_CARD_TYPE.U2.equals(nfcCardType) || NFC_CARD_TYPE.CT_DEMO.equals(nfcCardType)) {
                    reportLocalChargeStarted(chargeSession4.getCharge_id(), chargeSession4.getUser_code());
                } else {
                    reportChargeStarted(chargeSession4.getCharge_id());
                }
                chargeSession4.setIntervalChargeReport(5000);
                this.handlerTimer.startTimer((long) chargeSession4.getIntervalChargeReport(), 73746, (Object) null);
            } else if (chargeStatus.equals(CHARGE_STATUS.CHARGE_STOP_WAITTING)) {
                Log.i("XCloudPortHandler.handlePortChargeStatusChanged", "enter charge stopped status !!!");
                this.handlerTimer.stopTimer(73746);
                XCloudChargeSession chargeSession5 = getChargeSession();
                Long sid = null;
                String error = DeviceError.OTHER;
                String errMsg = null;
                CHARGE_STOP_CAUSE cause = portStatus.getChargeStopCause();
                if (cause != null) {
                    switch (m22x26790b25()[cause.ordinal()]) {
                        case 2:
                        case 4:
                            sid = chargeSession5.getRequestStopChargeSid();
                            error = DeviceError.USER_REMOTE;
                            break;
                        case 3:
                            errMsg = "刷卡";
                            error = DeviceError.USER_LOCAL;
                            break;
                        case 5:
                            sid = chargeSession5.getRequestStopChargeSid();
                            error = DeviceError.ADMIN_REMOTE;
                            break;
                        case 6:
                            errMsg = "拔枪";
                            error = DeviceError.USER_LOCAL;
                            break;
                        case 9:
                            error = DeviceError.NO_FUND;
                            break;
                        case PortRuntimeData.STATUS_EX_11:
                            errMsg = "重启";
                            error = DeviceError.USER_LOCAL;
                            break;
                        case PortRuntimeData.STATUS_EX_12:
                            error = "ERROR";
                            break;
                    }
                }
                if (sid == null) {
                    sid = Long.valueOf(XCloudProtocolAgent.getInstance().genSid());
                }
                DeviceError stopCause = new DeviceError(error, errMsg, (Object) null);
                if (CHARGE_STOP_CAUSE.user_set.equals(cause)) {
                    stopCause = new DeviceError(DeviceError.AUTO_STOP, (String) null, chargeSession5.getChargeStopCondition());
                } else if (CHARGE_STOP_CAUSE.full.equals(cause)) {
                    JsonObject deviceErrorData = new JsonObject();
                    deviceErrorData.addProperty("code", "A000");
                    stopCause = new DeviceError("ERROR", (String) null, deviceErrorData);
                } else if (CHARGE_STOP_CAUSE.car.equals(cause)) {
                    JsonObject deviceErrorData2 = new JsonObject();
                    deviceErrorData2.addProperty("code", "501A");
                    stopCause = new DeviceError("ERROR", (String) null, deviceErrorData2);
                }
                reportChargeStopped(chargeSession5.getCharge_id(), sid, stopCause);
            } else if (chargeStatus.equals(CHARGE_STATUS.IDLE)) {
                Log.i("XCloudPortHandler.handlePortChargeStatusChanged", "enter idle status !!!");
                this.handlerTimer.stopTimer(73746);
                XCloudChargeSession chargeSession6 = getChargeSession();
                if (this.status.equals(CHARGE_STATUS.CHARGE_START_WAITTING)) {
                    NFC_CARD_TYPE nfcCardType2 = getNFCTypeFromUserType(chargeSession6.getUser_type());
                    if (NFC_CARD_TYPE.U1.equals(nfcCardType2) || NFC_CARD_TYPE.U2.equals(nfcCardType2) || NFC_CARD_TYPE.CT_DEMO.equals(nfcCardType2)) {
                        clearChargeSession();
                    }
                } else {
                    String billId = chargeSession6.getCharge_id();
                    ChargeBill chargeBill3 = ChargeContentProxy.getInstance().getChargeBill(billId);
                    if (chargeBill3 != null) {
                        NFC_CARD_TYPE nfcCardType3 = getNFCTypeFromUserType(chargeBill3.getUser_type());
                        if (!NFC_CARD_TYPE.U1.equals(nfcCardType3) && !NFC_CARD_TYPE.U2.equals(nfcCardType3) && !NFC_CARD_TYPE.CT_DEMO.equals(nfcCardType3)) {
                            reportChargeEnded(chargeBill3);
                        } else if (TextUtils.isEmpty(chargeBill3.getCloud_charge_id())) {
                            Log.w("XCloudPortHandler.handlePortChargeStatusChanged", "offline card: " + chargeBill3.getUser_code() + ", and no available cloud charge id, report without charge id !!!");
                            reportLocalChargeEnded(chargeBill3);
                        } else {
                            reportChargeEnded(chargeBill3);
                        }
                    } else {
                        Log.w("XCloudPortHandler.handlePortChargeStatusChanged", "failed to query info for bill: " + billId);
                    }
                    clearChargeSession();
                }
            }
            this.status = chargeStatus;
        }
    }

    /* access modifiers changed from: private */
    public void handleBillPaidEvent(Uri uri) {
        String billId = uri.getLastPathSegment();
        ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(billId);
        if (chargeBill != null && this.port.equals(chargeBill.getPort())) {
            if (NFC_CARD_TYPE.U2.equals(getNFCTypeFromUserType(chargeBill.getUser_type())) && chargeBill.getReport_flag() == 1) {
                ChargeContentProxy.getInstance().clearReportedFlag(billId);
                String logstr = "card: " + chargeBill.getUser_code() + " paid, and reset reported flag for local bill: " + billId + (TextUtils.isEmpty(chargeBill.getCloud_charge_id()) ? "" : ", cloud bill: " + chargeBill.getCloud_charge_id());
                Log.i("XCloudPortHandler.handleBillPaidEvent", logstr);
                LogUtils.cloudlog(logstr);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleMsgSended(XCloudMessage msg) {
        if (XCloudMessage.ReportChargeStopped.equals(msg.getMessageName())) {
            ChargeContentProxy.getInstance().setChargeStopReportedFlag(String.valueOf(((ReportChargeStopped) msg.getBody()).getBillId()), 1);
        }
    }

    /* access modifiers changed from: private */
    public void handleSendFail(XCloudMessage msg) {
        String name = msg.getMessageName();
        if (XCloudMessage.RequestChargeWithIDCard.equals(name)) {
            Log.w("XCloudPortHandler.handleSendFail", "failed to send RequestChargeWithIDCard: " + msg.toJson());
            XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(77829, msg));
        } else if (XCloudMessage.ReportChargeStarted.equals(name)) {
            ReportChargeStarted reportChargeStarted = (ReportChargeStarted) msg.getBody();
            if (this.status.equals(CHARGE_STATUS.CHARGING) && reportChargeStarted.getBillId() == Long.parseLong(getChargeSession().getCharge_id())) {
                this.handler.sendMessageDelayed(this.handler.obtainMessage(73734, msg), 10000);
            }
        } else if (XCloudMessage.ReportChargeStopped.equals(name)) {
            ReportChargeStopped reportChargeStopped = (ReportChargeStopped) msg.getBody();
            if (!this.status.equals(CHARGE_STATUS.CHARGE_STOP_WAITTING)) {
                return;
            }
            if (!TextUtils.isEmpty(msg.getLocalChargeId())) {
                if (msg.getLocalChargeId().equals(getChargeSession().getCharge_id())) {
                    this.handler.sendMessageDelayed(this.handler.obtainMessage(73734, msg), 5000);
                }
            } else if (reportChargeStopped.getBillId() == Long.parseLong(getChargeSession().getCharge_id())) {
                this.handler.sendMessageDelayed(this.handler.obtainMessage(73734, msg), 5000);
            }
        } else if (XCloudMessage.ReportChargeEnded.equals(name)) {
        } else {
            if (XCloudMessage.RequestChargeQRCode.equals(name)) {
                this.handler.sendMessageDelayed(this.handler.obtainMessage(73734, msg), 10000);
            } else if (XCloudMessage.ReportLocalChargeStarted.equals(name)) {
                ReportLocalChargeStarted reportLocalChargeStarted = (ReportLocalChargeStarted) msg.getBody();
                if (this.status.equals(CHARGE_STATUS.CHARGING) && reportLocalChargeStarted.getCardSourceId().equals(getChargeSession().getUser_code())) {
                    this.handler.sendMessageDelayed(this.handler.obtainMessage(73734, msg), 10000);
                }
            } else {
                XCloudMessage.ReportLocalChargeEnded.equals(name);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleCloudRequest(XCloudMessage request) {
        String name = request.getMessageName();
        if (XCloudMessage.RequestStartCharge.equals(name)) {
            Log.i("XCloudPortHandler.handleCloudRequest", "receive XCloud RequestStartCharge: " + request.toJson());
            XCloudChargeSession chargeSession2 = getChargeSession();
            RequestStartCharge requestStartCharge = (RequestStartCharge) request.getBody();
            if (requestStartCharge.getSid() == null) {
                LogUtils.cloudlog("no sid in RequestStartCharge: " + request.getData());
                if (TextUtils.isEmpty(chargeSession2.getUser_type())) {
                    refuseCloudChargeRequest(String.valueOf(requestStartCharge.getBillId()), requestStartCharge.getSid(), new DeviceError(DeviceError.OTHER, (String) null, (Object) null));
                } else {
                    reportChargeCancelled(request, new DeviceError(DeviceError.OTHER, (String) null, (Object) null));
                }
            } else if (!ChargeStatusCacheProvider.getInstance().isCloudTimeSynch()) {
                LogUtils.cloudlog("not time synchnized from cloud, refuse start charge request from cloud");
                if (TextUtils.isEmpty(chargeSession2.getUser_type())) {
                    refuseCloudChargeRequest(String.valueOf(requestStartCharge.getBillId()), requestStartCharge.getSid(), new DeviceError(DeviceError.OTHER, (String) null, (Object) null));
                } else {
                    reportChargeCancelled(request, new DeviceError(DeviceError.OTHER, (String) null, (Object) null));
                }
            } else if (chargeSession2.getCharge_id() != null) {
                NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeSession2.getUser_type());
                if (!String.valueOf(requestStartCharge.getBillId()).equals(chargeSession2.getCharge_id()) || !NFC_CARD_TYPE.U3.equals(nfcCardType)) {
                    if (TextUtils.isEmpty(chargeSession2.getUser_type())) {
                        refuseCloudChargeRequest(String.valueOf(requestStartCharge.getBillId()), requestStartCharge.getSid(), new DeviceError(DeviceError.BUSY, (String) null, (Object) null));
                    } else {
                        reportChargeCancelled(request, new DeviceError(DeviceError.BUSY, (String) null, (Object) null));
                    }
                    Bundle data = new Bundle();
                    data.putString("stage", CHARGE_UI_STAGE.refuse.getStage());
                    data.putString("cause", CHARGE_REFUSE_CAUSE.BUSY.getCause());
                    UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
                    return;
                }
                chargeSession2.setRequestStartChargeSid(requestStartCharge.getSid());
                chargeSession2.setRequestStartCharge(request);
                XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(77827, request));
            } else if (!requestStartCharge.isForcePlugging() || HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port)) {
                chargeSession2.setCharge_id(String.valueOf(requestStartCharge.getBillId()));
                chargeSession2.setRequestStartChargeSid(requestStartCharge.getSid());
                chargeSession2.setRequestStartCharge(request);
                XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(77827, request));
            } else {
                LogUtils.cloudlog("refuse charge for not pluged, and need plugin gun first in RequestStartCharge: " + request.getData());
                reportChargeCancelled(request, new DeviceError(DeviceError.NOT_PLUGGED, (String) null, (Object) null));
                Bundle data2 = new Bundle();
                data2.putString("stage", CHARGE_UI_STAGE.refuse.getStage());
                data2.putString("cause", CHARGE_REFUSE_CAUSE.NOT_PLUGGED.getCause());
                UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data2);
            }
        } else if (XCloudMessage.RequestRefuseCharge.equals(name)) {
            Log.i("XCloudPortHandler.handleCloudRequest", "receive XCloud RequestRefuseCharge: " + request.toJson());
            XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(77827, request));
        } else if (XCloudMessage.RequestStopCharge.equals(name)) {
            Log.i("XCloudPortHandler.handleCloudRequest", "receive XCloud RequestStopCharge: " + request.toJson());
            getChargeSession().setRequestStopChargeSid(((RequestStopCharge) request.getBody()).getSid());
            XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(77827, request));
        } else if (XCloudMessage.RequestAutoStop.equals(name)) {
            Log.i("XCloudPortHandler.handleCloudRequest", "receive XCloud RequestAutoStop: " + request.toJson());
            getChargeSession().setChargeStopCondition(((RequestAutoStop) request.getBody()).getAutoStopAt());
            XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(77827, request));
        } else if (XCloudMessage.CancelAutoStop.equals(name)) {
            Log.i("XCloudPortHandler.handleCloudRequest", "receive XCloud CancelAutoStop: " + request.toJson());
            XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(77827, request));
        } else if (XCloudMessage.RequestUpdateStartTime.equals(name)) {
            Log.i("XCloudPortHandler.handleCloudRequest", "receive XCloud RequestUpdateStartTime: " + request.toJson());
            XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(77827, request));
        } else if (XCloudMessage.RequestEndCharge.equals(name)) {
            Log.i("XCloudPortHandler.handleCloudRequest", "receive XCloud RequestEndCharge: " + request.toJson());
            getChargeSession().setRequestStopChargeSid(((RequestEndCharge) request.getBody()).getSid());
            XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(77827, request));
        }
    }

    /* access modifiers changed from: private */
    public void handleCloudResponse(XCloudMessage request, XCloudMessage response) {
        String name = response.getMessageName();
        if (XCloudMessage.ConfirmChargeStarted.equals(name)) {
            Log.i("XCloudPortHandler.handleCloudResponse", "receive XCloud ConfirmChargeStarted: " + response.toJson() + ", request: " + request.toJson());
            handleConfirmChargeStarted(request, response);
        } else if (XCloudMessage.ConfirmChargeEnded.equals(name)) {
            Log.i("XCloudPortHandler.handleCloudResponse", "receive XCloud ConfirmChargeEnded: " + response.toJson() + ", request: " + request.toJson());
            handleConfirmChargeEnded(request, response);
        } else if (XCloudMessage.SendChargeQRCode.equals(name)) {
            Log.i("XCloudPortHandler.handleCloudResponse", "receive XCloud SendChargeQRCode: " + response.toJson() + ", request: " + request.toJson());
            handleSendChargeQRCode(request, response);
        } else if (XCloudMessage.RequestStartCharge.equals(name)) {
            Log.i("XCloudPortHandler.handleCloudResponse", "receive XCloud RequestStartCharge response: " + response.toJson() + ", request: " + request.toJson());
            RequestChargeWithIDCard requestChargeWithIDCard = (RequestChargeWithIDCard) request.getBody();
            RequestStartCharge requestStartCharge = (RequestStartCharge) response.getBody();
            if (requestStartCharge.getSid() == null) {
                LogUtils.cloudlog("no sid in U3 RequestStartCharge: " + response.getData());
                reportChargeCancelled(response, new DeviceError(DeviceError.OTHER, (String) null, (Object) null));
                XCloudRequestSession xcloudRequestSession = new XCloudRequestSession();
                xcloudRequestSession.setRequest(request);
                xcloudRequestSession.setResponse(response);
                XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(XCloudDCAPGateway.MSG_U3_AUTH_REFUSED, xcloudRequestSession));
            } else if (!requestStartCharge.isForcePlugging() || HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port)) {
                XCloudChargeSession chargeSession2 = getChargeSession();
                chargeSession2.setCharge_id(String.valueOf(requestStartCharge.getBillId()));
                chargeSession2.setUser_type(String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.U3.getType());
                chargeSession2.setUser_code(requestChargeWithIDCard.getSourceId());
                chargeSession2.setRequestStartChargeSid(requestStartCharge.getSid());
                chargeSession2.setRequestStartCharge(response);
                XCloudRequestSession xcloudRequestSession2 = new XCloudRequestSession();
                xcloudRequestSession2.setRequest(request);
                xcloudRequestSession2.setResponse(response);
                XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(77828, xcloudRequestSession2));
            } else {
                LogUtils.cloudlog("refuse charge for not pluged, and need plugin gun first in U3 RequestStartCharge: " + response.getData());
                reportChargeCancelled(response, new DeviceError(DeviceError.NOT_PLUGGED, (String) null, (Object) null));
                XCloudRequestSession xcloudRequestSession3 = new XCloudRequestSession();
                xcloudRequestSession3.setRequest(request);
                xcloudRequestSession3.setResponse(response);
                xcloudRequestSession3.setError(new ErrorCode(ErrorCode.EC_CHARGE_NEED_PLUGIN));
                XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(XCloudDCAPGateway.MSG_U3_AUTH_REFUSED, xcloudRequestSession3));
            }
        } else if (XCloudMessage.RequestRefuseCharge.equals(name)) {
            Log.i("XCloudPortHandler.handleCloudResponse", "receive XCloud RequestRefuseCharge response: " + response.toJson() + ", request: " + request.toJson());
            XCloudRequestSession xcloudRequestSession4 = new XCloudRequestSession();
            xcloudRequestSession4.setRequest(request);
            xcloudRequestSession4.setResponse(response);
            XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(77828, xcloudRequestSession4));
        }
    }

    private void handleSendChargeQRCode(XCloudMessage request, XCloudMessage response) {
        SendChargeQRCode sendChargeQRCode = (SendChargeQRCode) response.getBody();
        if (sendChargeQRCode.getError() != null) {
            Log.w("XCloudPortHandler.handleSendChargeQRCode", "failed to get Qrcode, request: " + request.toJson() + ", response: " + response.toJson());
            return;
        }
        String qrcode = sendChargeQRCode.getQrCode();
        String port2 = request.getPort();
        Log.i("XCloudPortHandler.handleSendChargeQRCode", "succeed to get Qrcode: " + qrcode + ", port: " + port2);
        ChargeStatusCacheProvider.getInstance().updatePortQrcodeContent(port2, qrcode);
        long expireIn = new BigDecimal(((double) (sendChargeQRCode.getExpireInterval() * 1000)) * 0.8d).setScale(0, 4).longValue();
        this.handlerTimer.stopTimer(73744);
        this.handlerTimer.startTimer(expireIn, 73744, port2);
    }

    private void handleConfirmChargeStarted(XCloudMessage request, XCloudMessage response) {
        String requestMsgName = request.getMessageName();
        ConfirmChargeStarted confirmChargeStarted = (ConfirmChargeStarted) response.getBody();
        if (XCloudMessage.ReportChargeStarted.equals(requestMsgName)) {
            long billId = confirmChargeStarted.getBillId();
            if (billId != Long.parseLong(getChargeSession().getCharge_id())) {
                Log.w("XCloudPortHandler.handleConfirmChargeStarted", "not now charge !!! request: " + request.toJson() + ", response: " + response.toJson());
            } else {
                ChargeContentProxy.getInstance().setChargeStartReportedFlag(String.valueOf(billId), 1);
            }
        } else if (XCloudMessage.ReportLocalChargeStarted.equals(requestMsgName)) {
            String localChargeId = request.getLocalChargeId();
            String cloudChargeId = String.valueOf(confirmChargeStarted.getBillId());
            if (localChargeId.equals(getChargeSession().getCharge_id())) {
                getChargeSession().setCloud_charge_id(cloudChargeId);
                ChargeContentProxy.getInstance().setChargeStartReportedFlag(localChargeId, 1);
            } else {
                Log.w("XCloudPortHandler.handleConfirmChargeStarted", "not now charge !!! request: " + request.toJson() + ", response: " + response.toJson());
            }
            ChargeContentProxy.getInstance().setCloudChargeId(localChargeId, cloudChargeId);
        }
    }

    private void handleConfirmChargeEnded(XCloudMessage request, XCloudMessage response) {
        Long billId;
        ConfirmChargeEnded confirmChargeEnded = (ConfirmChargeEnded) response.getBody();
        String localChargeId = request.getLocalChargeId();
        if (TextUtils.isEmpty(localChargeId)) {
            long billId2 = confirmChargeEnded.getBillId().longValue();
            ChargeContentProxy.getInstance().setReportedFlag(String.valueOf(billId2));
            if (confirmChargeEnded.isPaid()) {
                ChargeContentProxy.getInstance().setPaidFlag(String.valueOf(billId2), -1);
                return;
            }
            return;
        }
        if (XCloudMessage.ReportLocalChargeEnded.equals(request.getMessageName()) && (billId = confirmChargeEnded.getBillId()) != null) {
            ChargeContentProxy.getInstance().setCloudChargeId(localChargeId, String.valueOf(billId));
        }
        ChargeContentProxy.getInstance().setReportedFlag(localChargeId);
    }

    private boolean sendMessage(XCloudMessage msg) {
        return XCloudProtocolAgent.getInstance().sendMessage(msg);
    }

    /* access modifiers changed from: private */
    public void reportError() {
        ReportError reportError = new ReportError();
        reportError.setPort(Integer.parseInt(this.port));
        reportError.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        HashMap<String, ErrorCode> errors = HardwareStatusCacheProvider.getInstance().getAllDeviceErrors(this.port);
        if (errors != null) {
            ArrayList<DeviceError> deviceErrorArray = new ArrayList<>();
            for (Map.Entry<String, ErrorCode> entry : errors.entrySet()) {
                ErrorCode error = entry.getValue();
                HashMap<String, Object> errData = error.getData();
                Port portStatus = null;
                if (errData != null) {
                    String errRaw = (String) errData.get("raw");
                    String portStatusJson = (String) errData.get("portStatus");
                    if (!TextUtils.isEmpty(portStatusJson)) {
                        portStatus = (Port) new Port().fromJson(portStatusJson);
                    }
                }
                if (error.getCode() >= 30010 && error.getCode() <= 30018) {
                    int code = (error.getCode() - 30000) + 5000;
                    JsonObject deviceErrorData = null;
                    if (portStatus != null) {
                        PHASE phase = HardwareStatusCacheProvider.getInstance().getPhase();
                        if (error.getCode() == 30015) {
                            if (PHASE.SINGLE_PHASE.getPhase() == phase.getPhase()) {
                                deviceErrorData = new JsonObject();
                                deviceErrorData.addProperty("current", (Number) portStatus.getAmps().get(0));
                            } else {
                                if (PHASE.THREE_PHASE.getPhase() == phase.getPhase()) {
                                    deviceErrorData = new JsonObject();
                                    deviceErrorData.add("current", new JsonParser().parse(JsonBean.getGsonBuilder().create().toJson((Object) portStatus.getAmps(), new TypeToken<ArrayList>() {
                                    }.getType())).getAsJsonArray());
                                }
                            }
                        } else if (error.getCode() == 30014) {
                            if (PHASE.SINGLE_PHASE.getPhase() == phase.getPhase()) {
                                deviceErrorData = new JsonObject();
                                deviceErrorData.addProperty("voltage", (Number) portStatus.getVolts().get(0));
                            } else {
                                if (PHASE.THREE_PHASE.getPhase() == phase.getPhase()) {
                                    deviceErrorData = new JsonObject();
                                    deviceErrorData.add("voltage", new JsonParser().parse(JsonBean.getGsonBuilder().create().toJson((Object) portStatus.getVolts(), new TypeToken<ArrayList>() {
                                    }.getType())).getAsJsonArray());
                                }
                            }
                        } else if (error.getCode() == 30017) {
                            deviceErrorData = new JsonObject();
                            deviceErrorData.addProperty("current", (Number) portStatus.getLeakAmp());
                        } else if (error.getCode() == 30016) {
                            deviceErrorData = new JsonObject();
                            deviceErrorData.addProperty("temperature", (Number) Double.valueOf(portStatus.getChipTemp().doubleValue() / 10.0d));
                        } else if (error.getCode() == 30018) {
                            deviceErrorData = new JsonObject();
                            deviceErrorData.addProperty("voltage", (Number) portStatus.getCpVoltage());
                        }
                    }
                    deviceErrorArray.add(new DeviceError(String.valueOf(code), (String) null, deviceErrorData));
                }
            }
            if (deviceErrorArray.size() > 0) {
                reportError.setError(deviceErrorArray);
                XCloudMessage report = new XCloudMessage();
                report.setMessageName(XCloudMessage.ReportError);
                report.setSrcId(this.f86sn);
                report.setBody(reportError);
                report.setData(reportError.toJson());
                report.setPort(this.port);
                sendMessage(report);
            }
        }
    }

    /* access modifiers changed from: private */
    public void reportErrorRecovery(ErrorCode error) {
        ReportErrorRecovery reportErrorRecovery = new ReportErrorRecovery();
        reportErrorRecovery.setPort(Integer.parseInt(this.port));
        reportErrorRecovery.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        ArrayList<DeviceError> deviceErrorArray = null;
        if (error != null && error.getCode() >= 30010 && error.getCode() <= 30018) {
            deviceErrorArray = new ArrayList<>();
            deviceErrorArray.add(new DeviceError(String.valueOf((error.getCode() - 30000) + 5000), (String) null, (Object) null));
        }
        if (HardwareStatusCacheProvider.getInstance().hasDeviceErrors(this.port)) {
            reportErrorRecovery.setError(deviceErrorArray);
        }
        XCloudMessage report = new XCloudMessage();
        report.setMessageName(XCloudMessage.ReportErrorRecovery);
        report.setSrcId(this.f86sn);
        report.setBody(reportErrorRecovery);
        report.setData(reportErrorRecovery.toJson());
        report.setPort(this.port);
        sendMessage(report);
    }

    public void reportChargeStarted(String billId) {
        ReportChargeStarted reportChargeStarted = new ReportChargeStarted();
        Long sessionId = getChargeSession().getRequestStartChargeSid();
        if (sessionId == null) {
            LogUtils.cloudlog("report charge started, but not find sid in session: " + getChargeSession().toJson());
            sessionId = Long.valueOf(XCloudProtocolAgent.getInstance().genSid());
        }
        reportChargeStarted.setSid(sessionId);
        reportChargeStarted.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        reportChargeStarted.setBillId(Long.parseLong(billId));
        ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(billId);
        if (chargeBill != null && chargeBill.getStart_time() > 0) {
            reportChargeStarted.setTime(TimeUtils.getXCloudFormat(chargeBill.getStart_time(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        }
        XCloudMessage report = new XCloudMessage();
        report.setMessageName(XCloudMessage.ReportChargeStarted);
        report.setSrcId(this.f86sn);
        report.setBody(reportChargeStarted);
        report.setData(reportChargeStarted.toJson());
        report.setSessionId(String.valueOf(sessionId));
        report.setPort(this.port);
        if (!sendMessage(report)) {
            this.handler.sendMessage(this.handler.obtainMessage(73732, report));
        }
    }

    public void reportLocalChargeStarted(String localChargeId, String cardNo) {
        ReportLocalChargeStarted reportLocalChargeStarted = new ReportLocalChargeStarted();
        long sessionId = XCloudProtocolAgent.getInstance().genSid();
        reportLocalChargeStarted.setSid(Long.valueOf(sessionId));
        reportLocalChargeStarted.setPort(Integer.parseInt(this.port));
        reportLocalChargeStarted.setCardSourceId(cardNo);
        reportLocalChargeStarted.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(localChargeId);
        if (chargeBill != null && chargeBill.getStart_time() > 0) {
            reportLocalChargeStarted.setTime(TimeUtils.getXCloudFormat(chargeBill.getStart_time(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        }
        XCloudMessage report = new XCloudMessage();
        report.setMessageName(XCloudMessage.ReportLocalChargeStarted);
        report.setSrcId(this.f86sn);
        report.setBody(reportLocalChargeStarted);
        report.setData(reportLocalChargeStarted.toJson());
        report.setSessionId(String.valueOf(sessionId));
        report.setPort(this.port);
        report.setLocalChargeId(localChargeId);
        if (!sendMessage(report)) {
            this.handler.sendMessage(this.handler.obtainMessage(73732, report));
        }
    }

    /* access modifiers changed from: private */
    public void handleFinConfirm(FIN_MODE finMode, ErrorCode error) {
        DeviceError deviceError;
        XCloudChargeSession chargeSession2 = getChargeSession();
        String chargeId = chargeSession2.getCharge_id();
        Long sid = chargeSession2.getRequestStartChargeSid();
        if (!TextUtils.isEmpty(chargeId) && sid != null && !CHARGE_STATUS.CHARGING.equals(this.status) && !CHARGE_STATUS.CHARGE_STOP_WAITTING.equals(this.status)) {
            String code = DeviceError.OTHER;
            switch ($SWITCH_TABLE$com$xcharge$charger$core$type$FIN_MODE()[finMode.ordinal()]) {
                case 3:
                    code = DeviceError.USER_CANCEL;
                    break;
                case 4:
                    code = DeviceError.DISABLED;
                    break;
                case 5:
                    code = DeviceError.BUSY;
                    break;
                case 6:
                    code = DeviceError.NO_FEEPOLICY;
                    break;
                case 7:
                    code = DeviceError.PLUG_TIMEOUT;
                    break;
                case 8:
                case 9:
                    code = "ERROR";
                    break;
                case PortRuntimeData.STATUS_EX_11:
                    return;
            }
            if (error != null) {
                if (error.getCode() < 30010 || error.getCode() > 30018) {
                    deviceError = new DeviceError(code, (String) null, (Object) null);
                } else {
                    deviceError = new DeviceError(code, (String) null, String.valueOf((error.getCode() + 5000) - 30000));
                }
            } else if (FIN_MODE.car.equals(finMode)) {
                deviceError = new DeviceError(code, (String) null, "5019");
            } else {
                deviceError = new DeviceError(code, (String) null, (Object) null);
            }
            reportChargeCancelled(chargeId, Long.valueOf(sid.longValue()), deviceError);
        }
    }

    public void refuseCloudChargeRequest(String billId, Long sid, DeviceError error) {
        ReportChargeCancelled reportChargeCancelled = new ReportChargeCancelled();
        reportChargeCancelled.setSid(sid);
        reportChargeCancelled.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        reportChargeCancelled.setBillId(Long.parseLong(billId));
        if (error != null) {
            reportChargeCancelled.setCause(error);
        }
        XCloudMessage report = new XCloudMessage();
        report.setMessageName(XCloudMessage.ReportChargeCancelled);
        report.setSrcId(this.f86sn);
        report.setBody(reportChargeCancelled);
        report.setData(reportChargeCancelled.toJson());
        if (sid != null) {
            report.setSessionId(String.valueOf(sid));
        }
        report.setPort(this.port);
        sendMessage(report);
    }

    public void reportChargeCancelled(String billId, Long sid, DeviceError error) {
        ReportChargeCancelled reportChargeCancelled = new ReportChargeCancelled();
        reportChargeCancelled.setSid(sid);
        reportChargeCancelled.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        XCloudChargeSession chargeSession2 = getChargeSession();
        NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeSession2.getUser_type());
        if (NFC_CARD_TYPE.U1.equals(nfcCardType) || NFC_CARD_TYPE.U2.equals(nfcCardType) || NFC_CARD_TYPE.CT_DEMO.equals(nfcCardType)) {
            Log.w("XCloudPortHandler.reportChargeCancelled", "offline card: " + chargeSession2.getUser_code() + ", not report charge canceled !!!");
            return;
        }
        reportChargeCancelled.setBillId(Long.parseLong(billId));
        if (error != null) {
            reportChargeCancelled.setCause(error);
        }
        XCloudMessage report = new XCloudMessage();
        report.setMessageName(XCloudMessage.ReportChargeCancelled);
        report.setSrcId(this.f86sn);
        report.setBody(reportChargeCancelled);
        report.setData(reportChargeCancelled.toJson());
        if (sid != null) {
            report.setSessionId(String.valueOf(sid));
        }
        report.setPort(this.port);
        sendMessage(report);
        if (CHARGE_STATUS.IDLE.equals(this.status)) {
            if (error == null || !DeviceError.NO_FEEPOLICY.equals(error.getCode()) || !NFC_CARD_TYPE.U3.equals(nfcCardType)) {
                clearChargeSession();
                return;
            }
            Log.w("XCloudPortHandler.reportChargeCancelled", "no fee rate for U3, start timer to wait for charge restart !!! port: " + this.port);
            XCloudChargeSession snapSession = new XCloudChargeSession();
            snapSession.setUser_type(chargeSession2.getUser_type());
            snapSession.setUser_code(chargeSession2.getUser_code());
            snapSession.setCharge_id(chargeSession2.getCharge_id());
            this.handlerTimer.startTimer(10000, MSG_U3_WAIT_RESTART_TIMER, snapSession);
        } else if (!this.status.equals(CHARGE_STATUS.CHARGE_START_WAITTING)) {
        } else {
            if (error == null || !DeviceError.NO_FEEPOLICY.equals(error.getCode()) || !NFC_CARD_TYPE.U3.equals(nfcCardType)) {
                clearChargeSession();
                return;
            }
            Log.w("XCloudPortHandler.reportChargeCancelled", "no fee rate for U3, start timer to wait for charge restart !!! port: " + this.port);
            XCloudChargeSession snapSession2 = new XCloudChargeSession();
            snapSession2.setUser_type(chargeSession2.getUser_type());
            snapSession2.setUser_code(chargeSession2.getUser_code());
            snapSession2.setCharge_id(chargeSession2.getCharge_id());
            this.handlerTimer.startTimer(10000, MSG_U3_WAIT_RESTART_TIMER, snapSession2);
        }
    }

    public void reportChargeCancelled(XCloudMessage request, DeviceError error) {
        if (request != null) {
            RequestStartCharge requestStartCharge = (RequestStartCharge) request.getBody();
            reportChargeCancelled(String.valueOf(requestStartCharge.getBillId()), requestStartCharge.getSid(), error);
        } else if (CHARGE_STATUS.IDLE.equals(this.status) || CHARGE_STATUS.CHARGE_START_WAITTING.equals(this.status)) {
            XCloudChargeSession chargeSession2 = getChargeSession();
            String chargeId = chargeSession2.getCharge_id();
            Long sid = chargeSession2.getRequestStartChargeSid();
            if (!TextUtils.isEmpty(chargeId) && sid != null) {
                reportChargeCancelled(chargeId, sid, error);
            }
        }
    }

    public void reportAutoStopResult(XCloudMessage request, DeviceError error) {
        RequestAutoStop requestAutoStop = (RequestAutoStop) request.getBody();
        ReportAutoStopResult rptAutoStopResult = new ReportAutoStopResult();
        rptAutoStopResult.setSid(requestAutoStop.getSid());
        rptAutoStopResult.setBillId(requestAutoStop.getBillId());
        rptAutoStopResult.setError(error);
        rptAutoStopResult.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        XCloudMessage report = new XCloudMessage();
        report.setMessageName(XCloudMessage.ReportAutoStopResult);
        report.setSrcId(this.f86sn);
        report.setBody(rptAutoStopResult);
        report.setData(rptAutoStopResult.toJson());
        report.setPort(this.port);
        sendMessage(report);
    }

    public void reportChargeStopped(String billId, Long sid, DeviceError error) {
        ReportChargeStopped reportChargeStopped = new ReportChargeStopped();
        reportChargeStopped.setSid(sid);
        reportChargeStopped.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        XCloudMessage report = new XCloudMessage();
        report.setMessageName(XCloudMessage.ReportChargeStopped);
        report.setSrcId(this.f86sn);
        XCloudChargeSession chargeSession2 = getChargeSession();
        NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeSession2.getUser_type());
        if (NFC_CARD_TYPE.U1.equals(nfcCardType) || NFC_CARD_TYPE.U2.equals(nfcCardType) || NFC_CARD_TYPE.CT_DEMO.equals(nfcCardType)) {
            String cloudChargeId = chargeSession2.getCloud_charge_id();
            if (TextUtils.isEmpty(cloudChargeId)) {
                Log.w("XCloudPortHandler.reportChargeStopped", "offline card: " + chargeSession2.getUser_code() + ", but no available cloud charge id, not report charge stopped !!!");
                return;
            } else {
                reportChargeStopped.setBillId(Long.parseLong(cloudChargeId));
                report.setLocalChargeId(billId);
            }
        } else {
            reportChargeStopped.setBillId(Long.parseLong(billId));
        }
        ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(billId);
        if (chargeBill != null) {
            if (chargeBill.getStop_time() > 0) {
                reportChargeStopped.setTime(TimeUtils.getXCloudFormat(chargeBill.getStop_time(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
            }
            Object[] data = new Object[16];
            data[0] = Double.valueOf(chargeBill.getTotal_power());
            data[1] = 0;
            data[2] = 0;
            data[3] = Integer.valueOf(chargeBill.getPower_fee());
            data[4] = Integer.valueOf(chargeBill.getService_fee());
            data[5] = 0;
            data[6] = 0;
            data[7] = 0;
            Double volt = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port).getVolts().get(0);
            data[8] = Double.valueOf(volt == null ? 0.0d : volt.doubleValue());
            data[9] = 0;
            data[10] = 0;
            data[11] = 0;
            Double cp = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port).getCp();
            data[12] = Double.valueOf(cp == null ? 0.0d : cp.doubleValue());
            Double temprature = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port).getTemprature();
            data[13] = Double.valueOf(new BigDecimal((temprature == null ? 0.0d : temprature.doubleValue()) / 10.0d).setScale(1, 4).doubleValue());
            data[14] = 0;
            data[15] = 0;
            reportChargeStopped.setData(data);
        } else {
            Log.w("XCloudPortHandler.reportChargeStopped", "failed to query for bill: " + billId);
        }
        if (error != null) {
            reportChargeStopped.setCause(error);
        }
        report.setBody(reportChargeStopped);
        report.setData(reportChargeStopped.toJson());
        report.setSessionId(String.valueOf(sid));
        report.setPort(this.port);
        if (!sendMessage(report)) {
            this.handler.sendMessage(this.handler.obtainMessage(73732, report));
        }
    }

    public void reportChargeStopped(XCloudMessage request, DeviceError error) {
        RequestStopCharge requestStopCharge = (RequestStopCharge) request.getBody();
        reportChargeStopped(String.valueOf(requestStopCharge.getBillId()), requestStopCharge.getSid(), error);
    }

    public void reportChargePaused(String chargeId, DeviceError error, long time) {
        ReportChargePaused rptChargePaused = new ReportChargePaused();
        XCloudChargeSession chargeSession2 = getChargeSession();
        NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeSession2.getUser_type());
        if (NFC_CARD_TYPE.U1.equals(nfcCardType) || NFC_CARD_TYPE.U2.equals(nfcCardType) || NFC_CARD_TYPE.CT_DEMO.equals(nfcCardType)) {
            String cloudChargeId = chargeSession2.getCloud_charge_id();
            if (TextUtils.isEmpty(cloudChargeId)) {
                Log.w("XCloudPortHandler.reportChargePaused", "offline card: " + chargeSession2.getUser_code() + ", but no available cloud charge id, not report charge pause !!!");
                return;
            }
            rptChargePaused.setBillId(Long.parseLong(cloudChargeId));
        } else {
            rptChargePaused.setBillId(Long.parseLong(chargeId));
        }
        rptChargePaused.setCause(error);
        rptChargePaused.setTime(time);
        XCloudMessage report = new XCloudMessage();
        report.setMessageName(XCloudMessage.ReportChargePaused);
        report.setSrcId(this.f86sn);
        report.setBody(rptChargePaused);
        report.setData(rptChargePaused.toJson());
        report.setPort(this.port);
        sendMessage(report);
    }

    public void reportChargeResumed(String chargeId, DeviceError error, long time) {
        ReportChargeResumed rptChargeResumed = new ReportChargeResumed();
        XCloudChargeSession chargeSession2 = getChargeSession();
        NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeSession2.getUser_type());
        if (NFC_CARD_TYPE.U1.equals(nfcCardType) || NFC_CARD_TYPE.U2.equals(nfcCardType) || NFC_CARD_TYPE.CT_DEMO.equals(nfcCardType)) {
            String cloudChargeId = chargeSession2.getCloud_charge_id();
            if (TextUtils.isEmpty(cloudChargeId)) {
                Log.w("XCloudPortHandler.reportChargeResumed", "offline card: " + chargeSession2.getUser_code() + ", but no available cloud charge id, not report charge resume !!!");
                return;
            }
            rptChargeResumed.setBillId(Long.parseLong(cloudChargeId));
        } else {
            rptChargeResumed.setBillId(Long.parseLong(chargeId));
        }
        rptChargeResumed.setCause(error);
        rptChargeResumed.setTime(time);
        XCloudMessage report = new XCloudMessage();
        report.setMessageName(XCloudMessage.ReportChargeResumed);
        report.setSrcId(this.f86sn);
        report.setBody(rptChargeResumed);
        report.setData(rptChargeResumed.toJson());
        report.setPort(this.port);
        sendMessage(report);
    }

    public void reportDelayCountStarted(String chargeId) {
        ReportDelayCountStarted reportDelayCountStarted = new ReportDelayCountStarted();
        reportDelayCountStarted.setBillId(Long.parseLong(chargeId));
        reportDelayCountStarted.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        XCloudMessage report = new XCloudMessage();
        report.setMessageName(XCloudMessage.ReportDelayCountStarted);
        report.setSrcId(this.f86sn);
        report.setBody(reportDelayCountStarted);
        report.setData(reportDelayCountStarted.toJson());
        report.setPort(this.port);
        sendMessage(report);
    }

    public void reportDelayFeeStarted(String chargeId, long delayStart) {
        ReportDelayFeeStarted requestDelayFeeStarted = new ReportDelayFeeStarted();
        requestDelayFeeStarted.setBillId(Long.parseLong(chargeId));
        requestDelayFeeStarted.setTime(TimeUtils.getXCloudFormat(delayStart, RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        XCloudMessage report = new XCloudMessage();
        report.setMessageName(XCloudMessage.ReportDelayFeeStarted);
        report.setSrcId(this.f86sn);
        report.setBody(requestDelayFeeStarted);
        report.setData(requestDelayFeeStarted.toJson());
        report.setPort(this.port);
        sendMessage(report);
    }

    public XCloudMessage requestChargeWithIDCard(String cardNo, String timestamp, String nonce, String signature) {
        RequestChargeWithIDCard requestChargeWithIDCard = new RequestChargeWithIDCard();
        requestChargeWithIDCard.setSid(Long.valueOf(XCloudProtocolAgent.getInstance().genSid()));
        requestChargeWithIDCard.setPort(Integer.parseInt(this.port));
        requestChargeWithIDCard.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        requestChargeWithIDCard.setSourceId(cardNo);
        String timestamp2 = String.valueOf(System.currentTimeMillis());
        String nonce2 = FormatUtils.getRandomString(8);
        String sign = MD5Utils.MD5(String.valueOf(cardNo) + timestamp2 + nonce2 + signature);
        requestChargeWithIDCard.setTimestamp(timestamp2);
        requestChargeWithIDCard.setNonce(nonce2);
        requestChargeWithIDCard.setSignature(sign.toLowerCase());
        XCloudMessage request = new XCloudMessage();
        request.setMessageName(XCloudMessage.RequestChargeWithIDCard);
        request.setSrcId(this.f86sn);
        request.setBody(requestChargeWithIDCard);
        request.setData(requestChargeWithIDCard.toJson());
        request.setPort(this.port);
        request.setSessionId(String.valueOf(requestChargeWithIDCard.getSid()));
        if (sendMessage(request)) {
            return request;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void reportChargeStatus() {
        String chargeId = getChargeSession().getCharge_id();
        ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(chargeId);
        if (chargeBill != null) {
            ReportChargeStatus reportChargeStatus = new ReportChargeStatus();
            NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeBill.getUser_type());
            if (NFC_CARD_TYPE.U1.equals(nfcCardType) || NFC_CARD_TYPE.U2.equals(nfcCardType) || NFC_CARD_TYPE.CT_DEMO.equals(nfcCardType)) {
                String cloudChargeId = chargeBill.getCloud_charge_id();
                if (TextUtils.isEmpty(cloudChargeId)) {
                    Log.w("XCloudPortHandler.reportChargeStatus", "offline card: " + chargeBill.getUser_code() + ", but no available cloud charge id, not report charging info !!!");
                    return;
                }
                reportChargeStatus.setBillId(Long.parseLong(cloudChargeId));
            } else {
                reportChargeStatus.setBillId(Long.parseLong(chargeId));
            }
            Object[] data = new Object[16];
            data[0] = Double.valueOf(chargeBill.getTotal_power());
            Double kwatt = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port).getKwatt();
            Double amp = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port).getAmps().get(0);
            data[1] = Integer.valueOf(new BigDecimal((kwatt == null ? 0.0d : kwatt.doubleValue()) * 1000.0d).setScale(0, 4).intValue());
            data[2] = Integer.valueOf(new BigDecimal((amp == null ? 0.0d : amp.doubleValue()) * 1000.0d).setScale(0, 4).intValue());
            data[3] = Integer.valueOf(chargeBill.getPower_fee());
            data[4] = Integer.valueOf(chargeBill.getService_fee());
            data[5] = 0;
            data[6] = 0;
            data[7] = 0;
            Double volt = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port).getVolts().get(0);
            data[8] = Double.valueOf(volt == null ? 0.0d : volt.doubleValue());
            data[9] = 0;
            data[10] = 0;
            data[11] = 0;
            Double cp = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port).getCp();
            data[12] = Double.valueOf(cp == null ? 0.0d : cp.doubleValue());
            Double temprature = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port).getTemprature();
            data[13] = Double.valueOf(new BigDecimal((temprature == null ? 0.0d : temprature.doubleValue()) / 10.0d).setScale(1, 4).doubleValue());
            data[14] = 0;
            data[15] = 0;
            reportChargeStatus.setData(data);
            reportChargeStatus.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
            XCloudMessage report = new XCloudMessage();
            report.setMessageName(XCloudMessage.ReportChargeStatus);
            report.setSrcId(this.f86sn);
            report.setBody(reportChargeStatus);
            report.setData(reportChargeStatus.toJson());
            report.setPort(this.port);
            sendMessage(report);
            return;
        }
        Log.w("XCloudPortHandler.reportChargeRequest", "failed to query info for charge: " + chargeId);
    }

    /* access modifiers changed from: private */
    public void handleChargeStatusPeriodicReport() {
        try {
            XCloudChargeSession chargeSession2 = getChargeSession();
            Log.i("XCloudPortHandler.handleChargeStatusPeriodicReport", "report charge request periodically, port: " + this.port + ", interval: " + chargeSession2.getIntervalChargeReport() + ", status: " + this.status);
            reportChargeStatus();
            chargeSession2.incChargeReportCnt(1);
            if (chargeSession2.getChargeReportCnt() == 12) {
                int intervalChargeReport = RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalChargeReport() * 1000;
                if (intervalChargeReport <= 0) {
                    intervalChargeReport = 60000;
                }
                chargeSession2.setIntervalChargeReport(intervalChargeReport);
            }
        } catch (Exception e) {
            Log.e("XCloudPortHandler.handleChargeStatusPeriodicReport", Log.getStackTraceString(e));
        }
        this.handlerTimer.startTimer((long) this.chargeSession.getIntervalChargeReport(), 73746, (Object) null);
    }

    /* access modifiers changed from: private */
    public void reportHistoryBill() {
        ArrayList<ChargeBill> chargeBills = ChargeContentProxy.getInstance().getUnReportedBills(new String[]{CHARGE_USER_TYPE.xcharge.toString(), String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.U1, String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.U2, String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.U3, String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.CT_DEMO}, this.port);
        if (chargeBills != null && chargeBills.size() != 0) {
            Log.i("XCloudPortHandler.reportHistoryBill", "report history bill periodically, port: " + this.port);
            Iterator<ChargeBill> it = chargeBills.iterator();
            while (it.hasNext()) {
                ChargeBill chargeBill = it.next();
                if (chargeBill.getStart_time() > 0) {
                    NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeBill.getUser_type());
                    if (!NFC_CARD_TYPE.U1.equals(nfcCardType) && !NFC_CARD_TYPE.U2.equals(nfcCardType) && !NFC_CARD_TYPE.CT_DEMO.equals(nfcCardType)) {
                        reportChargeEnded(chargeBill);
                    } else if (TextUtils.isEmpty(chargeBill.getCloud_charge_id())) {
                        Log.w("XCloudPortHandler.reportHistoryBill", "offline card: " + chargeBill.getUser_code() + ", and no available cloud charge id, report without charge id !!!");
                        reportLocalChargeEnded(chargeBill);
                    } else {
                        reportChargeEnded(chargeBill);
                    }
                }
            }
        }
    }

    private void reportLocalChargeEnded(ChargeBill chargeBill) {
        Log.d("XCloudPortHandler.reportLocalChargeEnded", "local charge bill: " + chargeBill.toJson());
        String billId = chargeBill.getCharge_id();
        ReportLocalChargeEnded reportLocalChargeEnded = BillUtils.createReportLocalChargeEnded(chargeBill);
        reportLocalChargeEnded.setSid(Long.valueOf(XCloudProtocolAgent.getInstance().genSid()));
        reportLocalChargeEnded.setPort(Integer.parseInt(this.port));
        XCloudMessage report = new XCloudMessage();
        report.setLocalChargeId(billId);
        report.setMessageName(XCloudMessage.ReportLocalChargeEnded);
        report.setSrcId(this.f86sn);
        report.setBody(reportLocalChargeEnded);
        report.setData(reportLocalChargeEnded.toJson());
        report.setSessionId(String.valueOf(reportLocalChargeEnded.getSid()));
        report.setPort(this.port);
        if (!sendMessage(report)) {
            this.handler.sendMessage(this.handler.obtainMessage(73732, report));
        }
    }

    private void reportChargeEnded(ChargeBill chargeBill) {
        String billId = chargeBill.getCharge_id();
        if (chargeBill.getStart_time() > 0) {
            Log.d("XCloudPortHandler.reportChargeEnded", "charge bill: " + chargeBill.toJson());
            long sessionId = XCloudProtocolAgent.getInstance().genSid();
            ReportChargeEnded reportChargeEnded = BillUtils.createReportChargeEnded(chargeBill);
            XCloudMessage report = new XCloudMessage();
            NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeBill.getUser_type());
            if (NFC_CARD_TYPE.U1.equals(nfcCardType) || NFC_CARD_TYPE.U2.equals(nfcCardType) || NFC_CARD_TYPE.CT_DEMO.equals(nfcCardType)) {
                String cloudChargeId = chargeBill.getCloud_charge_id();
                if (TextUtils.isEmpty(cloudChargeId)) {
                    Log.w("XCloudPortHandler.reportChargeEnded", "offline card: " + chargeBill.getUser_code() + ", but no available cloud charge id, not report charge ended !!!");
                    return;
                } else {
                    reportChargeEnded.setBillId(Long.parseLong(cloudChargeId));
                    report.setLocalChargeId(billId);
                }
            } else {
                reportChargeEnded.setBillId(Long.parseLong(billId));
            }
            reportChargeEnded.setSid(Long.valueOf(sessionId));
            report.setMessageName(XCloudMessage.ReportChargeEnded);
            report.setSrcId(this.f86sn);
            report.setBody(reportChargeEnded);
            report.setData(reportChargeEnded.toJson());
            report.setSessionId(String.valueOf(sessionId));
            report.setPort(this.port);
            if (!sendMessage(report)) {
                this.handler.sendMessage(this.handler.obtainMessage(73732, report));
            }
        }
    }

    private NFC_CARD_TYPE getNFCTypeFromUserType(String userType) {
        if (TextUtils.isEmpty(userType)) {
            return null;
        }
        String[] userTypeSplit = userType.split("\\.");
        if (userTypeSplit.length != 2 || !CHARGE_USER_TYPE.nfc.getUserType().equals(userTypeSplit[0])) {
            return null;
        }
        return NFC_CARD_TYPE.valueOf(userTypeSplit[1]);
    }
}
