package com.xcharge.charger.device.p005c2.nfc.charge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.p000v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.LocalIdGenerator;
import com.xcharge.charger.core.api.Sequence;
import com.xcharge.charger.core.api.bean.AckDirective;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.NackDirective;
import com.xcharge.charger.core.api.bean.cap.AuthDirective;
import com.xcharge.charger.core.api.bean.cap.CAPDirectiveOption;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.core.api.bean.cap.EventDirective;
import com.xcharge.charger.core.api.bean.cap.FinDirective;
import com.xcharge.charger.core.api.bean.cap.InitAckDirective;
import com.xcharge.charger.core.api.bean.cap.InitDirective;
import com.xcharge.charger.core.api.bean.cap.QueryDirective;
import com.xcharge.charger.core.api.bean.cap.StopDirective;
import com.xcharge.charger.core.type.FIN_MODE;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_STATUS;
import com.xcharge.charger.data.bean.type.CHARGE_USER_STATUS;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.charger.data.p004db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.data.proxy.PortStatusObserver;
import com.xcharge.charger.device.p005c2.bean.AuthSign;
import com.xcharge.charger.device.p005c2.bean.NFCEventData;
import com.xcharge.charger.device.p005c2.bean.XSign;
import com.xcharge.charger.device.p005c2.nfc.C2NFCAgent;
import com.xcharge.charger.device.p005c2.nfc.NFCUtils;
import com.xcharge.charger.device.p005c2.nfc.charge.type.NFC_CHARGE_STATUS;
import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.HandlerTimer;
import java.util.HashMap;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* renamed from: com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler */
public class C2NFCChargeHandler {

    /* renamed from: $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_USER_STATUS */
    private static /* synthetic */ int[] f72xff8e88ad = null;

    /* renamed from: $SWITCH_TABLE$com$xcharge$charger$device$c2$nfc$charge$type$NFC_CHARGE_STATUS */
    private static /* synthetic */ int[] f73x4f4c7f31 = null;
    public static final int MSG_ANYO1_CHARGE = 28678;
    public static final int MSG_ANYO_SVW_CHARGE = 28680;
    public static final int MSG_CDDZ1_CHARGE = 28679;
    public static final int MSG_CT_DEMO_CHARGE = 28681;
    public static final int MSG_DCAP_CONFIRM = 28673;
    public static final int MSG_DCAP_INDICATE = 28674;
    public static final int MSG_OCPP_CHARGE = 28688;
    public static final int MSG_TIMEOUT_AUTH_SENDED = 28704;
    public static final int MSG_TIMEOUT_DCAP_REQUEST = 28709;
    public static final int MSG_TIMEOUT_FIN_SENDED = 28708;
    public static final int MSG_TIMEOUT_INIT_SENDED = 28705;
    public static final int MSG_TIMEOUT_STOP_SENDED = 28707;
    public static final int MSG_U1_CHARGE = 28675;
    public static final int MSG_U2_CHARGE = 28676;
    public static final int MSG_U3_CHARGE = 28677;
    public static final long TIMEOUT_AUTH_SENDED = 10000;
    public static final long TIMEOUT_DEFAULT_DCAP_REQUEST = 30000;
    public static final long TIMEOUT_FIN_SENDED = 10000;
    public static final long TIMEOUT_INIT_SENDED = 10000;
    public static final long TIMEOUT_STOP_SENDED = 10000;
    private NFCChargeSession chargeSession = null;
    private Context context = null;
    private DCAPMessageReceiver dcapMessageReceiver = null;
    /* access modifiers changed from: private */
    public MsgHandler handler = null;
    /* access modifiers changed from: private */
    public HandlerTimer handlerTimer = null;
    /* access modifiers changed from: private */
    public String port = null;
    private PortStatusObserver portStatusObserver = null;
    /* access modifiers changed from: private */
    public HashMap<String, DCAPMessage> sendDCAPReqestState = null;
    /* access modifiers changed from: private */
    public NFC_CHARGE_STATUS status = null;
    private HandlerThread thread = null;

    /* renamed from: $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_USER_STATUS */
    static /* synthetic */ int[] m19xff8e88ad() {
        int[] iArr = f72xff8e88ad;
        if (iArr == null) {
            iArr = new int[CHARGE_USER_STATUS.values().length];
            try {
                iArr[CHARGE_USER_STATUS.illegal.ordinal()] = 5;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[CHARGE_USER_STATUS.need_pay.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[CHARGE_USER_STATUS.need_queue.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[CHARGE_USER_STATUS.need_rsrv.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[CHARGE_USER_STATUS.normal.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            f72xff8e88ad = iArr;
        }
        return iArr;
    }

    /* renamed from: $SWITCH_TABLE$com$xcharge$charger$device$c2$nfc$charge$type$NFC_CHARGE_STATUS */
    static /* synthetic */ int[] m20x4f4c7f31() {
        int[] iArr = f73x4f4c7f31;
        if (iArr == null) {
            iArr = new int[NFC_CHARGE_STATUS.values().length];
            try {
                iArr[NFC_CHARGE_STATUS.auth_sended.ordinal()] = 2;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[NFC_CHARGE_STATUS.charging.ordinal()] = 5;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[NFC_CHARGE_STATUS.fin_sended.ordinal()] = 8;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[NFC_CHARGE_STATUS.idle.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[NFC_CHARGE_STATUS.init_sended.ordinal()] = 3;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[NFC_CHARGE_STATUS.inited.ordinal()] = 4;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[NFC_CHARGE_STATUS.stop_sended.ordinal()] = 6;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[NFC_CHARGE_STATUS.stopped.ordinal()] = 7;
            } catch (NoSuchFieldError e8) {
            }
            f73x4f4c7f31 = iArr;
        }
        return iArr;
    }

    /* renamed from: com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler$DCAPMessageReceiver */
    private class DCAPMessageReceiver extends BroadcastReceiver {
        private DCAPMessageReceiver() {
        }

        /* synthetic */ DCAPMessageReceiver(C2NFCChargeHandler c2NFCChargeHandler, DCAPMessageReceiver dCAPMessageReceiver) {
            this();
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DCAPMessage.ACTION_DCAP_CONFIRM)) {
                C2NFCChargeHandler.this.sendMessage(C2NFCChargeHandler.this.handler.obtainMessage(C2NFCChargeHandler.MSG_DCAP_CONFIRM, intent.getStringExtra("body")));
            } else if (action.equals(DCAPMessage.ACTION_DCAP_INDICATE)) {
                C2NFCChargeHandler.this.sendMessage(C2NFCChargeHandler.this.handler.obtainMessage(C2NFCChargeHandler.MSG_DCAP_INDICATE, intent.getStringExtra("body")));
            }
        }
    }

    /* renamed from: com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler$MsgHandler */
    private class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r21) {
            /*
                r20 = this;
                r12 = 0
                r0 = r21
                int r2 = r0.what     // Catch:{ Exception -> 0x0034 }
                switch(r2) {
                    case 28673: goto L_0x000c;
                    case 28674: goto L_0x0064;
                    case 28675: goto L_0x008d;
                    case 28676: goto L_0x00be;
                    case 28677: goto L_0x010c;
                    case 28678: goto L_0x017f;
                    case 28679: goto L_0x01e1;
                    case 28680: goto L_0x01b0;
                    case 28681: goto L_0x014e;
                    case 28688: goto L_0x0212;
                    case 28704: goto L_0x0243;
                    case 28705: goto L_0x02cd;
                    case 28707: goto L_0x0351;
                    case 28708: goto L_0x03b7;
                    case 28709: goto L_0x0414;
                    case 139265: goto L_0x0528;
                    default: goto L_0x0008;
                }
            L_0x0008:
                super.handleMessage(r21)
                return
            L_0x000c:
                java.lang.String r3 = "C2NFCChargeHandler.handleMessage"
                java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = "receive DCAP Confirm: "
                r4.<init>(r2)     // Catch:{ Exception -> 0x0034 }
                r0 = r21
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = (java.lang.String) r2     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r2 = r4.append(r2)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = r2.toString()     // Catch:{ Exception -> 0x0034 }
                android.util.Log.i(r3, r2)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r3 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                r0 = r21
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = (java.lang.String) r2     // Catch:{ Exception -> 0x0034 }
                r3.handleConfirm(r2)     // Catch:{ Exception -> 0x0034 }
                goto L_0x0008
            L_0x0034:
                r13 = move-exception
                java.lang.String r2 = "C2NFCChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder
                java.lang.String r4 = "except: "
                r3.<init>(r4)
                java.lang.String r4 = android.util.Log.getStackTraceString(r13)
                java.lang.StringBuilder r3 = r3.append(r4)
                java.lang.String r3 = r3.toString()
                android.util.Log.e(r2, r3)
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                java.lang.String r3 = "C2NFCChargeHandler handleMessage exception: "
                r2.<init>(r3)
                java.lang.String r3 = android.util.Log.getStackTraceString(r13)
                java.lang.StringBuilder r2 = r2.append(r3)
                java.lang.String r2 = r2.toString()
                com.xcharge.common.utils.LogUtils.syslog(r2)
                goto L_0x0008
            L_0x0064:
                java.lang.String r3 = "C2NFCChargeHandler.handleMessage"
                java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = "receive DCAP Indicate: "
                r4.<init>(r2)     // Catch:{ Exception -> 0x0034 }
                r0 = r21
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = (java.lang.String) r2     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r2 = r4.append(r2)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = r2.toString()     // Catch:{ Exception -> 0x0034 }
                android.util.Log.i(r3, r2)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r3 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                r0 = r21
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = (java.lang.String) r2     // Catch:{ Exception -> 0x0034 }
                r3.handleIndicate(r2)     // Catch:{ Exception -> 0x0034 }
                goto L_0x0008
            L_0x008d:
                android.os.Bundle r12 = r21.getData()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = "C2NFCChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = "enter U1 charge: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r12.toString()     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0034 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = "uuid"
                java.lang.String r3 = r12.getString(r3)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = "cardno"
                java.lang.String r4 = r12.getString(r4)     // Catch:{ Exception -> 0x0034 }
                r2.handleU1ChargeMsg(r3, r4)     // Catch:{ Exception -> 0x0034 }
                goto L_0x0008
            L_0x00be:
                android.os.Bundle r12 = r21.getData()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = "C2NFCChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = "enter U2 charge: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r12.toString()     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0034 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.bean.XSign r2 = new com.xcharge.charger.device.c2.bean.XSign     // Catch:{ Exception -> 0x0034 }
                r2.<init>()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = "xsign"
                java.lang.String r3 = r12.getString(r3)     // Catch:{ Exception -> 0x0034 }
                java.lang.Object r7 = r2.fromJson(r3)     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.bean.XSign r7 = (com.xcharge.charger.device.p005c2.bean.XSign) r7     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = "uuid"
                java.lang.String r3 = r12.getString(r3)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = "cardno"
                java.lang.String r4 = r12.getString(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r5 = "balance"
                int r5 = r12.getInt(r5)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r6 = "key"
                byte[] r6 = r12.getByteArray(r6)     // Catch:{ Exception -> 0x0034 }
                r2.handleU2ChargeMsg(r3, r4, r5, r6, r7)     // Catch:{ Exception -> 0x0034 }
                goto L_0x0008
            L_0x010c:
                android.os.Bundle r12 = r21.getData()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = "C2NFCChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = "enter U3 charge: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r12.toString()     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0034 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r3 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = "uuid"
                java.lang.String r4 = r12.getString(r2)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = "cardno"
                java.lang.String r5 = r12.getString(r2)     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.bean.AuthSign r2 = new com.xcharge.charger.device.c2.bean.AuthSign     // Catch:{ Exception -> 0x0034 }
                r2.<init>()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r6 = "sign"
                java.lang.String r6 = r12.getString(r6)     // Catch:{ Exception -> 0x0034 }
                java.lang.Object r2 = r2.fromJson(r6)     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.bean.AuthSign r2 = (com.xcharge.charger.device.p005c2.bean.AuthSign) r2     // Catch:{ Exception -> 0x0034 }
                r3.handleU3ChargeMsg(r4, r5, r2)     // Catch:{ Exception -> 0x0034 }
                goto L_0x0008
            L_0x014e:
                android.os.Bundle r12 = r21.getData()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = "C2NFCChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = "enter ct demo charge: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r12.toString()     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0034 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = "uuid"
                java.lang.String r3 = r12.getString(r3)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = "cardno"
                java.lang.String r4 = r12.getString(r4)     // Catch:{ Exception -> 0x0034 }
                r2.handleCTDemoChargeMsg(r3, r4)     // Catch:{ Exception -> 0x0034 }
                goto L_0x0008
            L_0x017f:
                android.os.Bundle r12 = r21.getData()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = "C2NFCChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = "enter anyo1 charge: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r12.toString()     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0034 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = "uuid"
                java.lang.String r3 = r12.getString(r3)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = "cardno"
                java.lang.String r4 = r12.getString(r4)     // Catch:{ Exception -> 0x0034 }
                r2.handleAnyo1ChargeMsg(r3, r4)     // Catch:{ Exception -> 0x0034 }
                goto L_0x0008
            L_0x01b0:
                android.os.Bundle r12 = r21.getData()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = "C2NFCChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = "enter anyo svw charge: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r12.toString()     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0034 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = "uuid"
                java.lang.String r3 = r12.getString(r3)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = "cardno"
                java.lang.String r4 = r12.getString(r4)     // Catch:{ Exception -> 0x0034 }
                r2.handleAnyoSVWChargeMsg(r3, r4)     // Catch:{ Exception -> 0x0034 }
                goto L_0x0008
            L_0x01e1:
                android.os.Bundle r12 = r21.getData()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = "C2NFCChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = "enter cddz jianquan card charge: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r12.toString()     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0034 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = "uuid"
                java.lang.String r3 = r12.getString(r3)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = "cardno"
                java.lang.String r4 = r12.getString(r4)     // Catch:{ Exception -> 0x0034 }
                r2.handleCDDZJianQuanChargeMsg(r3, r4)     // Catch:{ Exception -> 0x0034 }
                goto L_0x0008
            L_0x0212:
                android.os.Bundle r12 = r21.getData()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = "C2NFCChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = "enter ocpp card charge: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r12.toString()     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0034 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = "uuid"
                java.lang.String r3 = r12.getString(r3)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = "cardno"
                java.lang.String r4 = r12.getString(r4)     // Catch:{ Exception -> 0x0034 }
                r2.handleOCPPChargeMsg(r3, r4)     // Catch:{ Exception -> 0x0034 }
                goto L_0x0008
            L_0x0243:
                java.lang.String r2 = "C2NFCChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0034 }
                r3.<init>()     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.charge.type.NFC_CHARGE_STATUS r4 = com.xcharge.charger.device.p005c2.nfc.charge.type.NFC_CHARGE_STATUS.auth_sended     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = " state timeout !!! port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r4 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r4 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.charge.NFCChargeSession r4 = r4.getChargeSession()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0034 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0034 }
                r0 = r21
                int r3 = r0.what     // Catch:{ Exception -> 0x0034 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = r2.port     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r2 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.getInstance(r2)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r3 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = r3.port     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r3 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.getInstance(r3)     // Catch:{ Exception -> 0x0034 }
                r4 = 24581(0x6005, float:3.4445E-41)
                com.xcharge.charger.data.bean.ErrorCode r5 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x0034 }
                r6 = 40015(0x9c4f, float:5.6073E-41)
                r5.<init>(r6)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r5 = r5.toJson()     // Catch:{ Exception -> 0x0034 }
                android.os.Message r3 = r3.obtainMessage(r4, r5)     // Catch:{ Exception -> 0x0034 }
                r2.sendMessage(r3)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                r2.clearChargeSession()     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.charge.type.NFC_CHARGE_STATUS r3 = com.xcharge.charger.device.p005c2.nfc.charge.type.NFC_CHARGE_STATUS.idle     // Catch:{ Exception -> 0x0034 }
                r2.status = r3     // Catch:{ Exception -> 0x0034 }
                goto L_0x0008
            L_0x02cd:
                java.lang.String r2 = "C2NFCChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0034 }
                r3.<init>()     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.charge.type.NFC_CHARGE_STATUS r4 = com.xcharge.charger.device.p005c2.nfc.charge.type.NFC_CHARGE_STATUS.init_sended     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = " state timeout !!! port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r4 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r4 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.charge.NFCChargeSession r4 = r4.getChargeSession()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0034 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0034 }
                r0 = r21
                int r3 = r0.what     // Catch:{ Exception -> 0x0034 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = r2.port     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r2 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.getInstance(r2)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r3 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = r3.port     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r3 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.getInstance(r3)     // Catch:{ Exception -> 0x0034 }
                r4 = 24581(0x6005, float:3.4445E-41)
                com.xcharge.charger.data.bean.ErrorCode r5 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x0034 }
                r6 = 40029(0x9c5d, float:5.6093E-41)
                r5.<init>(r6)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r5 = r5.toJson()     // Catch:{ Exception -> 0x0034 }
                android.os.Message r3 = r3.obtainMessage(r4, r5)     // Catch:{ Exception -> 0x0034 }
                r2.sendMessage(r3)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.core.type.FIN_MODE r3 = com.xcharge.charger.core.type.FIN_MODE.timeout     // Catch:{ Exception -> 0x0034 }
                r4 = 0
                r2.finRequest(r3, r4)     // Catch:{ Exception -> 0x0034 }
                goto L_0x0008
            L_0x0351:
                java.lang.String r2 = "C2NFCChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0034 }
                r3.<init>()     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.charge.type.NFC_CHARGE_STATUS r4 = com.xcharge.charger.device.p005c2.nfc.charge.type.NFC_CHARGE_STATUS.stop_sended     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = " state timeout !!! port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r4 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r4 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.charge.NFCChargeSession r4 = r4.getChargeSession()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0034 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0034 }
                r0 = r21
                int r3 = r0.what     // Catch:{ Exception -> 0x0034 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.charge.type.NFC_CHARGE_STATUS r2 = com.xcharge.charger.device.p005c2.nfc.charge.type.NFC_CHARGE_STATUS.stop_sended     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r3 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.charge.type.NFC_CHARGE_STATUS r3 = r3.status     // Catch:{ Exception -> 0x0034 }
                boolean r2 = r2.equals(r3)     // Catch:{ Exception -> 0x0034 }
                if (r2 == 0) goto L_0x0008
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.charge.type.NFC_CHARGE_STATUS r3 = com.xcharge.charger.device.p005c2.nfc.charge.type.NFC_CHARGE_STATUS.charging     // Catch:{ Exception -> 0x0034 }
                r2.status = r3     // Catch:{ Exception -> 0x0034 }
                goto L_0x0008
            L_0x03b7:
                java.lang.String r2 = "C2NFCChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0034 }
                r3.<init>()     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.charge.type.NFC_CHARGE_STATUS r4 = com.xcharge.charger.device.p005c2.nfc.charge.type.NFC_CHARGE_STATUS.fin_sended     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = " state timeout !!! port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r4 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r4 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.charge.NFCChargeSession r4 = r4.getChargeSession()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0034 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0034 }
                r0 = r21
                int r3 = r0.what     // Catch:{ Exception -> 0x0034 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                r2.clearChargeSession()     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.charge.type.NFC_CHARGE_STATUS r3 = com.xcharge.charger.device.p005c2.nfc.charge.type.NFC_CHARGE_STATUS.idle     // Catch:{ Exception -> 0x0034 }
                r2.status = r3     // Catch:{ Exception -> 0x0034 }
                goto L_0x0008
            L_0x0414:
                r0 = r21
                java.lang.Object r0 = r0.obj     // Catch:{ Exception -> 0x0034 }
                r18 = r0
                com.xcharge.charger.core.api.bean.DCAPMessage r18 = (com.xcharge.charger.core.api.bean.DCAPMessage) r18     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.util.HashMap r2 = r2.sendDCAPReqestState     // Catch:{ Exception -> 0x0034 }
                long r4 = r18.getSeq()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = java.lang.String.valueOf(r4)     // Catch:{ Exception -> 0x0034 }
                boolean r2 = r2.containsKey(r3)     // Catch:{ Exception -> 0x0034 }
                if (r2 == 0) goto L_0x0008
                java.lang.String r2 = "C2NFCChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = "timeout to send DCAP request: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r18.toJson()     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = " !!! port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r4 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r4 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.charge.NFCChargeSession r4 = r4.getChargeSession()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0034 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.util.HashMap r2 = r2.sendDCAPReqestState     // Catch:{ Exception -> 0x0034 }
                long r4 = r18.getSeq()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = java.lang.String.valueOf(r4)     // Catch:{ Exception -> 0x0034 }
                r2.remove(r3)     // Catch:{ Exception -> 0x0034 }
                java.lang.Object r9 = r18.getData()     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.core.api.bean.cap.CAPMessage r9 = (com.xcharge.charger.core.api.bean.cap.CAPMessage) r9     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = "query"
                java.lang.String r3 = r9.getOp()     // Catch:{ Exception -> 0x0034 }
                boolean r2 = r2.equals(r3)     // Catch:{ Exception -> 0x0034 }
                if (r2 == 0) goto L_0x0008
                java.lang.Object r17 = r9.getData()     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.core.api.bean.cap.QueryDirective r17 = (com.xcharge.charger.core.api.bean.cap.QueryDirective) r17     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.core.api.bean.cap.CAPDirectiveOption r15 = r9.getOpt()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = "card.status"
                java.lang.String r3 = r15.getQuery_id()     // Catch:{ Exception -> 0x0034 }
                boolean r2 = r2.equals(r3)     // Catch:{ Exception -> 0x0034 }
                if (r2 == 0) goto L_0x0008
                java.util.HashMap r16 = r17.getParams()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = "action"
                r0 = r16
                java.lang.Object r8 = r0.get(r2)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r8 = (java.lang.String) r8     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = "cardType"
                r0 = r16
                java.lang.Object r11 = r0.get(r2)     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.data.bean.type.NFC_CARD_TYPE r11 = (com.xcharge.charger.data.bean.type.NFC_CARD_TYPE) r11     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = "cardNo"
                r0 = r16
                java.lang.Object r10 = r0.get(r2)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r10 = (java.lang.String) r10     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.charge.NFCChargeSession r14 = r2.getChargeSession()     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = "end_charging"
                boolean r2 = r2.equals(r8)     // Catch:{ Exception -> 0x0034 }
                if (r2 == 0) goto L_0x0008
                com.xcharge.charger.device.c2.nfc.charge.type.NFC_CHARGE_STATUS r2 = com.xcharge.charger.device.p005c2.nfc.charge.type.NFC_CHARGE_STATUS.charging     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r3 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.charge.type.NFC_CHARGE_STATUS r3 = r3.status     // Catch:{ Exception -> 0x0034 }
                boolean r2 = r2.equals(r3)     // Catch:{ Exception -> 0x0034 }
                if (r2 == 0) goto L_0x0008
                com.xcharge.charger.data.bean.type.NFC_CARD_TYPE r2 = r14.getCardType()     // Catch:{ Exception -> 0x0034 }
                boolean r2 = r11.equals(r2)     // Catch:{ Exception -> 0x0034 }
                if (r2 == 0) goto L_0x0008
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = r2.port     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r2 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.getInstance(r2)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r3 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = r3.port     // Catch:{ Exception -> 0x0034 }
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r3 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.getInstance(r3)     // Catch:{ Exception -> 0x0034 }
                r4 = 24581(0x6005, float:3.4445E-41)
                com.xcharge.charger.data.bean.ErrorCode r5 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x0034 }
                r6 = 40009(0x9c49, float:5.6065E-41)
                r5.<init>(r6)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r5 = r5.toJson()     // Catch:{ Exception -> 0x0034 }
                android.os.Message r3 = r3.obtainMessage(r4, r5)     // Catch:{ Exception -> 0x0034 }
                r2.sendMessage(r3)     // Catch:{ Exception -> 0x0034 }
                goto L_0x0008
            L_0x0528:
                r0 = r21
                java.lang.Object r0 = r0.obj     // Catch:{ Exception -> 0x0034 }
                r19 = r0
                android.net.Uri r19 = (android.net.Uri) r19     // Catch:{ Exception -> 0x0034 }
                java.lang.String r2 = "C2NFCChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = "port status changed, port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r4 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = ", uri: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r4 = r19.toString()     // Catch:{ Exception -> 0x0034 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0034 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0034 }
                android.util.Log.d(r2, r3)     // Catch:{ Exception -> 0x0034 }
                r0 = r20
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r2 = com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.this     // Catch:{ Exception -> 0x0034 }
                r0 = r19
                r2.handlePortStatusChanged(r0)     // Catch:{ Exception -> 0x0034 }
                goto L_0x0008
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.MsgHandler.handleMessage(android.os.Message):void");
        }
    }

    /* access modifiers changed from: private */
    public void handlePortStatusChanged(Uri uri) {
        if (uri.getPath().contains("ports/" + this.port + "/plugin")) {
            boolean isConnected = HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port);
            if (SystemSettingCacheProvider.getInstance().isPlug2Charge() && isConnected) {
                NFCEventData nfcEventData = new NFCEventData();
                nfcEventData.setPort(this.port);
                nfcEventData.setPresent(true);
                nfcEventData.setUuid(0);
                C2NFCAgent.getInstance(this.port).handleEvent(nfcEventData);
            }
        }
    }

    public void init(Context context2, String port2) {
        this.context = context2;
        this.port = port2;
        this.status = NFC_CHARGE_STATUS.idle;
        this.sendDCAPReqestState = new HashMap<>();
        this.thread = new HandlerThread("C2NFCChargeHandler#" + this.port, 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(this.context);
        this.dcapMessageReceiver = new DCAPMessageReceiver(this, (DCAPMessageReceiver) null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DCAPMessage.ACTION_DCAP_CONFIRM);
        filter.addAction(DCAPMessage.ACTION_DCAP_INDICATE);
        LocalBroadcastManager.getInstance(context2).registerReceiver(this.dcapMessageReceiver, filter);
        this.portStatusObserver = new PortStatusObserver(this.context, this.port, this.handler);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/" + this.port), true, this.portStatusObserver);
    }

    public void destroy() {
        this.context.getContentResolver().unregisterContentObserver(this.portStatusObserver);
        LocalBroadcastManager.getInstance(this.context).unregisterReceiver(this.dcapMessageReceiver);
        this.handlerTimer.destroy();
        this.handler.removeMessages(MSG_DCAP_CONFIRM);
        this.handler.removeMessages(MSG_DCAP_INDICATE);
        this.handler.removeMessages(MSG_U1_CHARGE);
        this.handler.removeMessages(MSG_U2_CHARGE);
        this.handler.removeMessages(MSG_U3_CHARGE);
        this.handler.removeMessages(MSG_ANYO1_CHARGE);
        this.handler.removeMessages(MSG_CDDZ1_CHARGE);
        this.handler.removeMessages(MSG_ANYO_SVW_CHARGE);
        this.handler.removeMessages(MSG_CT_DEMO_CHARGE);
        this.handler.removeMessages(MSG_OCPP_CHARGE);
        this.handler.removeMessages(MSG_TIMEOUT_AUTH_SENDED);
        this.handler.removeMessages(MSG_TIMEOUT_INIT_SENDED);
        this.handler.removeMessages(MSG_TIMEOUT_STOP_SENDED);
        this.handler.removeMessages(MSG_TIMEOUT_FIN_SENDED);
        this.handler.removeMessages(MSG_TIMEOUT_DCAP_REQUEST);
        this.thread.quit();
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

    /* access modifiers changed from: private */
    public NFCChargeSession getChargeSession() {
        if (this.chargeSession == null) {
            this.chargeSession = new NFCChargeSession();
        }
        return this.chargeSession;
    }

    /* access modifiers changed from: private */
    public void clearChargeSession() {
        this.chargeSession = null;
    }

    public boolean isCharging() {
        if (!this.status.equals(NFC_CHARGE_STATUS.idle)) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0038 A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isChargeUser(java.lang.String r5, java.lang.String r6, com.xcharge.charger.data.bean.type.NFC_CARD_TYPE r7) {
        /*
            r4 = this;
            r1 = 1
            com.xcharge.charger.device.c2.nfc.charge.NFCChargeSession r0 = r4.getChargeSession()
            com.xcharge.charger.data.bean.type.NFC_CARD_TYPE r2 = com.xcharge.charger.data.bean.type.NFC_CARD_TYPE.U1
            boolean r2 = r2.equals(r7)
            if (r2 == 0) goto L_0x001a
            com.xcharge.charger.data.bean.type.NFC_CARD_TYPE r2 = com.xcharge.charger.data.bean.type.NFC_CARD_TYPE.U1
            com.xcharge.charger.data.bean.type.NFC_CARD_TYPE r3 = r0.getCardType()
            boolean r2 = r2.equals(r3)
            if (r2 == 0) goto L_0x0038
        L_0x0019:
            return r1
        L_0x001a:
            com.xcharge.charger.data.bean.type.NFC_CARD_TYPE r2 = com.xcharge.charger.data.bean.type.NFC_CARD_TYPE.ocpp
            boolean r2 = r2.equals(r7)
            if (r2 == 0) goto L_0x003a
            com.xcharge.charger.device.c2.nfc.charge.type.NFC_CHARGE_STATUS r2 = com.xcharge.charger.device.p005c2.nfc.charge.type.NFC_CHARGE_STATUS.charging
            com.xcharge.charger.device.c2.nfc.charge.type.NFC_CHARGE_STATUS r3 = r4.status
            boolean r2 = r2.equals(r3)
            if (r2 == 0) goto L_0x003a
            com.xcharge.charger.data.bean.type.NFC_CARD_TYPE r2 = com.xcharge.charger.data.bean.type.NFC_CARD_TYPE.ocpp
            com.xcharge.charger.data.bean.type.NFC_CARD_TYPE r3 = r0.getCardType()
            boolean r2 = r2.equals(r3)
            if (r2 != 0) goto L_0x0019
        L_0x0038:
            r1 = 0
            goto L_0x0019
        L_0x003a:
            java.lang.String r2 = r0.getCardUUID()
            boolean r2 = r5.equals(r2)
            if (r2 == 0) goto L_0x0038
            java.lang.String r2 = r0.getCardNo()
            boolean r2 = r6.equals(r2)
            if (r2 == 0) goto L_0x0038
            goto L_0x0019
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler.isChargeUser(java.lang.String, java.lang.String, com.xcharge.charger.data.bean.type.NFC_CARD_TYPE):boolean");
    }

    /* access modifiers changed from: private */
    public void handleConfirm(String json) {
        try {
            DCAPMessage confirm = (DCAPMessage) new DCAPMessage().fromJson(json);
            if (!checkDCAPMessageRoute(confirm)) {
                Log.w("C2NFCChargeHandler.handleConfirm", "route is not to me, ignore it !!!");
                return;
            }
            CAPMessage cap = (CAPMessage) new CAPMessage().fromJson(JsonBean.ObjectToJson(confirm.getData()));
            confirm.setData(cap);
            CAPDirectiveOption opt = cap.getOpt();
            String peerOp = opt.getOp();
            String op = cap.getOp();
            if (!"query".equals(peerOp)) {
                if (!this.sendDCAPReqestState.containsKey(String.valueOf(opt.getSeq()))) {
                    NFCChargeSession session = getChargeSession();
                    if ("fin".equals(peerOp)) {
                        session.setIs3rdPartFin(true);
                        Log.w("C2NFCChargeHandler.handleConfirm", "server or other module fin charge session, we support it !!!");
                    } else if ("stop".equals(peerOp)) {
                        session.setIs3rdPartStop(true);
                        Log.w("C2NFCChargeHandler.handleConfirm", "server or other module stop charge, we support it !!!");
                    } else {
                        Log.w("C2NFCChargeHandler.handleConfirm", "not expected seq, ignore it !!!");
                        return;
                    }
                } else {
                    this.sendDCAPReqestState.remove(String.valueOf(opt.getSeq()));
                }
                if ("ack".equals(op)) {
                    AckDirective ack = (AckDirective) new AckDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                    cap.setData(ack);
                    if ("auth".equals(peerOp)) {
                        parseAckAuth(ack);
                        handleAuthConfirm(true, (ErrorCode) null);
                    } else if ("fin".equals(peerOp)) {
                        handleFinConfirm();
                    } else if ("stop".equals(peerOp)) {
                        handleStopConfirm(true);
                    }
                } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
                    NackDirective nack = (NackDirective) new NackDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                    cap.setData(nack);
                    if ("auth".equals(peerOp)) {
                        handleAuthConfirm(false, parseNackAuth(nack));
                    } else if ("stop".equals(peerOp)) {
                        handleStopConfirm(false);
                    }
                }
            } else if (this.sendDCAPReqestState.containsKey(String.valueOf(opt.getSeq()))) {
                DCAPMessage queryRequest = this.sendDCAPReqestState.remove(String.valueOf(opt.getSeq()));
                if ("ack".equals(op)) {
                    AckDirective ack2 = (AckDirective) new AckDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                    cap.setData(ack2);
                    handleQueryConfirm(true, opt.getQuery_id(), ack2, queryRequest);
                } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
                    NackDirective nack2 = (NackDirective) new NackDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                    cap.setData(nack2);
                    handleQueryConfirm(false, opt.getQuery_id(), nack2, queryRequest);
                }
            }
        } catch (Exception e) {
            Log.e("C2NFCChargeHandler.handleConfirm", "confirm: " + json + ", exception: " + Log.getStackTraceString(e));
        }
    }

    private void parseAckAuth(AckDirective ack) {
        HashMap<String, Object> attach;
        NFCChargeSession chargeSession2 = getChargeSession();
        NFC_CARD_TYPE cardType = chargeSession2.getCardType();
        if (NFC_CARD_TYPE.anyo1.equals(cardType)) {
            HashMap<String, Object> attach2 = ack.getAttach();
            if (attach2 != null) {
                chargeSession2.setCharge_id((String) attach2.get("bill_id"));
                chargeSession2.setBinded_user((String) attach2.get("user_id"));
                chargeSession2.setUser_balance(Long.valueOf((String) attach2.get(ContentDB.NFCConsumeFailCacheTable.BALANCE)).longValue());
            }
        } else if (NFC_CARD_TYPE.U3.equals(cardType)) {
            HashMap<String, Object> attach3 = ack.getAttach();
            if (attach3 != null) {
                chargeSession2.setCharge_id((String) attach3.get("bill_id"));
                chargeSession2.setFee_rate((String) attach3.get(ContentDB.ChargeTable.FEE_RATE_ID));
                chargeSession2.setUser_balance((long) Integer.valueOf((String) attach3.get(ContentDB.NFCConsumeFailCacheTable.BALANCE)).intValue());
                Object userTcType = attach3.get(ContentDB.ChargeTable.USER_TC_TYPE);
                if (userTcType != null) {
                    chargeSession2.setUser_tc_type(USER_TC_TYPE.valueOf((String) userTcType));
                }
                Object userTcValue = attach3.get(ContentDB.ChargeTable.USER_TC_VALUE);
                if (userTcValue != null) {
                    chargeSession2.setUser_tc_value((String) userTcValue);
                }
            }
        } else if (NFC_CARD_TYPE.cddz_1.equals(cardType) && (attach = ack.getAttach()) != null) {
            chargeSession2.setCharge_id((String) attach.get("bill_id"));
            chargeSession2.setUser_balance(Long.valueOf((String) attach.get(ContentDB.NFCConsumeFailCacheTable.BALANCE)).longValue());
        }
    }

    private ErrorCode parseNackAuth(NackDirective nack) {
        NFCChargeSession chargeSession2 = getChargeSession();
        NFC_CARD_TYPE cardType = chargeSession2.getCardType();
        ErrorCode error = new ErrorCode(ErrorCode.EC_NFC_CARD_AUTH_FAIL);
        if (NFC_CARD_TYPE.U2.equals(cardType)) {
            HashMap<String, Object> attach = nack.getAttach();
            if (attach != null) {
                switch (m19xff8e88ad()[CHARGE_USER_STATUS.valueOf((String) attach.get("user_status")).ordinal()]) {
                    case 2:
                        String bill_id = (String) attach.get("bill_id");
                        int fee = Integer.parseInt((String) attach.get(ChargeStopCondition.TYPE_FEE));
                        int cardBalance = (int) (chargeSession2.getUser_balance() & XMSZHead.ID_BROADCAST);
                        if (fee > cardBalance) {
                            error.setCode(ErrorCode.EC_NFC_CARD_UNPAID_BALANCE_NOT_ENOUGH);
                        } else if (NFCUtils.consumeU2(cardBalance, fee, chargeSession2.getKey(), chargeSession2.getCardUUID(), chargeSession2.getCardNo(), 0)) {
                            Log.i("C2NFCChargeHandler.parseNackAuth", "succeeded to consume unpaid bill: " + bill_id + ", card: " + chargeSession2.getCardNo() + ", bill fee: " + fee + ", balance: " + cardBalance);
                            error.setCode(ErrorCode.EC_NFC_CARD_UNPAID_CONSUME_OK);
                            ChargeContentProxy.getInstance().setUserBalance(bill_id, (long) (cardBalance - fee));
                            ChargeContentProxy.getInstance().setPaidFlag(bill_id, 1);
                            chargeSession2.setUser_balance((long) (cardBalance - fee));
                        } else {
                            error.setCode(ErrorCode.EC_NFC_CARD_UNPAID_CONSUME_FAIL);
                        }
                        HashMap<String, Object> errorData = new HashMap<>();
                        errorData.put("billId", bill_id);
                        errorData.put(ContentDB.NFCConsumeFailCacheTable.BALANCE, String.valueOf(cardBalance));
                        errorData.put(ChargeStopCondition.TYPE_FEE, String.valueOf(fee));
                        error.setData(errorData);
                        break;
                }
            }
        } else if (NFC_CARD_TYPE.U3.equals(cardType)) {
            error.setCode(ErrorCode.EC_UI_OUT_OF_DISTURB);
        } else {
            NFC_CARD_TYPE.anyo1.equals(cardType);
        }
        return error;
    }

    private void handleAuthConfirm(boolean isAck, ErrorCode error) {
        if (NFC_CHARGE_STATUS.auth_sended.equals(this.status)) {
            stopTimer(this.status);
            if (isAck) {
                initRequest();
                return;
            }
            C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, error.toJson()));
            clearChargeSession();
            this.status = NFC_CHARGE_STATUS.idle;
            return;
        }
        Log.w("C2NFCChargeHandler.handleAuthConfirm", "ignore auth confirm, port: " + this.port + ", status: " + this.status.getStatus());
    }

    private void handleStopConfirm(boolean isAck) {
        NFCChargeSession chargeSession2 = getChargeSession();
        if (NFC_CHARGE_STATUS.stop_sended.equals(this.status)) {
            if (!chargeSession2.isIs3rdPartStop() || isAck) {
                stopTimer(this.status);
                if (isAck) {
                    this.status = NFC_CHARGE_STATUS.stopped;
                } else {
                    this.status = NFC_CHARGE_STATUS.charging;
                }
            } else {
                Log.w("C2NFCChargeHandler.handleStopConfirm", "nack confirm for 3rd part stop, ignore it !!!");
            }
        } else if (!NFC_CHARGE_STATUS.charging.equals(this.status) || !chargeSession2.isIs3rdPartStop() || !isAck) {
            Log.w("C2NFCChargeHandler.handleStopConfirm", "ignore stop confirm, port: " + this.port + ", status: " + this.status.getStatus());
        } else {
            Log.w("C2NFCChargeHandler.handleStopConfirm", "3rd part succeed to stop charge session: " + chargeSession2.toJson());
            this.status = NFC_CHARGE_STATUS.stopped;
        }
    }

    private void handleFinConfirm() {
        NFCChargeSession chargeSession2 = getChargeSession();
        if (NFC_CHARGE_STATUS.fin_sended.equals(this.status) || ((NFC_CHARGE_STATUS.inited.equals(this.status) || NFC_CHARGE_STATUS.charging.equals(this.status)) && chargeSession2.isIs3rdPartFin())) {
            if (chargeSession2.isIs3rdPartFin()) {
                Log.w("C2NFCChargeHandler.handleFinConfirm", "3rd part succeed to fin charge session: " + chargeSession2.toJson());
            }
            stopTimer(this.status);
            clearChargeSession();
            this.status = NFC_CHARGE_STATUS.idle;
            return;
        }
        Log.w("C2NFCChargeHandler.handleFinConfirm", "ignore fin confirm, port: " + this.port + ", status: " + this.status.getStatus());
    }

    private void handleQueryConfirm(boolean isAck, String queryId, Object confirmDirective, DCAPMessage queryRequest) {
        QueryDirective query = (QueryDirective) ((CAPMessage) queryRequest.getData()).getData();
        NFCChargeSession nfcChargeSession = getChargeSession();
        if (isAck) {
            HashMap<String, Object> data = ((AckDirective) confirmDirective).getAttach();
            if (QueryDirective.QUERY_ID_CARD_STATUS.equals(queryId)) {
                HashMap<String, Object> params = query.getParams();
                String action = (String) params.get("action");
                NFC_CARD_TYPE cardType = (NFC_CARD_TYPE) params.get("cardType");
                String cardNo = (String) params.get("cardNo");
                if (data != null) {
                    if ("end_charging".equals(action)) {
                        String status2 = (String) data.get(ContentDB.AuthInfoTable.STATUS);
                        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port);
                        if (!NFC_CHARGE_STATUS.charging.equals(this.status) || !cardType.equals(nfcChargeSession.getCardType())) {
                            if (NFC_CHARGE_STATUS.idle.equals(this.status) && CHARGE_STATUS.CHARGING.equals(portStatus.getChargeStatus())) {
                                if ("Accepted".equals(status2)) {
                                    C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
                                    fin3rdCharge(portStatus.getCharge_id(), "user:" + CHARGE_USER_TYPE.nfc.getUserType() + "." + NFC_CARD_TYPE.ocpp.getType() + MqttTopic.TOPIC_LEVEL_SEPARATOR + cardNo, FIN_MODE.nfc, (ErrorCode) null);
                                    return;
                                }
                                C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_SCAN_REFUSE).toJson()));
                            }
                        } else if ("Accepted".equals(status2)) {
                            C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
                            stopTimer(this.status);
                            finRequest(FIN_MODE.normal, (ErrorCode) null);
                        } else {
                            C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_SCAN_REFUSE).toJson()));
                        }
                    }
                } else if ("end_charging".equals(action) && NFC_CHARGE_STATUS.charging.equals(this.status) && cardType.equals(nfcChargeSession.getCardType())) {
                    C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_SCAN_REFUSE).toJson()));
                }
            }
        } else {
            NackDirective nackDirective = (NackDirective) confirmDirective;
            if (QueryDirective.QUERY_ID_CARD_STATUS.equals(queryId)) {
                HashMap<String, Object> params2 = query.getParams();
                NFC_CARD_TYPE cardType2 = (NFC_CARD_TYPE) params2.get("cardType");
                String str = (String) params2.get("cardNo");
                if ("end_charging".equals((String) params2.get("action")) && NFC_CHARGE_STATUS.charging.equals(this.status) && cardType2.equals(nfcChargeSession.getCardType())) {
                    C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_SCAN_REFUSE).toJson()));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleIndicate(String json) {
        try {
            DCAPMessage indicate = (DCAPMessage) new DCAPMessage().fromJson(json);
            if (!checkDCAPMessageRoute(indicate)) {
                Log.w("C2NFCChargeHandler.handleIndicate", "route is not to me, ignore it !!!");
                return;
            }
            CAPMessage cap = (CAPMessage) new CAPMessage().fromJson(JsonBean.ObjectToJson(indicate.getData()));
            indicate.setData(cap);
            String op = cap.getOp();
            if (CAPMessage.DIRECTIVE_INIT_ACK.equals(op)) {
                handleInitAckIndicate(indicate);
            } else if ("fin".equals(op)) {
                handleFinIndicate(indicate);
            } else if ("event".equals(op)) {
                cap.setData((EventDirective) new EventDirective().fromJson(JsonBean.ObjectToJson(cap.getData())));
                handleEventIndicate(indicate);
            }
        } catch (Exception e) {
            Log.e("C2NFCChargeHandler.handleIndicate", "indicate: " + json + ", exception: " + Log.getStackTraceString(e));
        }
    }

    private void handleInitAckIndicate(DCAPMessage indicate) {
        try {
            if (NFC_CHARGE_STATUS.init_sended.equals(this.status)) {
                CAPMessage cap = (CAPMessage) indicate.getData();
                CAPDirectiveOption opt = cap.getOpt();
                InitAckDirective initAck = (InitAckDirective) new InitAckDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(initAck);
                stopTimer(this.status);
                NFCChargeSession chargeSession2 = getChargeSession();
                chargeSession2.setCharge_id(opt.getCharge_id());
                chargeSession2.setFee_rate(initAck.getFee_rate());
                chargeSession2.setTimeout_plugin(initAck.getTimeout_plugin());
                chargeSession2.setTimeout_start(initAck.getTimeout_start());
                chargeSession2.setTimeout_plugout(initAck.getTimeout_plugout());
                if (!NFC_CARD_TYPE.U2.equals(chargeSession2.getCardType()) || reserveU2((int) (chargeSession2.getUser_balance() & XMSZHead.ID_BROADCAST), chargeSession2.getKey())) {
                    ackResponse(DCAPProxy.getInstance().createCAPResponseByIndcate(indicate));
                    this.status = NFC_CHARGE_STATUS.inited;
                    C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
                    return;
                }
                Log.w("C2NFCChargeHandler.handleInitAckIndicate", "failed to reserve card: " + chargeSession2.getCardNo() + ", port: " + this.port + ", status: " + this.status.getStatus());
                C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_CARD_RESERVE_FAIL).toJson()));
                finRequest(FIN_MODE.error, new ErrorCode(ErrorCode.EC_NFC_CARD_RESERVE_FAIL));
                return;
            }
            Log.w("C2NFCChargeHandler.handleInitAckIndicate", "ignore init ack indicate, port: " + this.port + ", status: " + this.status.getStatus());
        } catch (Exception e) {
            Log.e("C2NFCChargeHandler.handleInitAckIndicate", "indicate: " + indicate.toJson() + ", exception: " + Log.getStackTraceString(e));
        }
    }

    private void handleFinIndicate(DCAPMessage indicate) {
        try {
            CAPMessage cap = (CAPMessage) indicate.getData();
            String chargeId = cap.getOpt().getCharge_id();
            NFCChargeSession chargeSession2 = getChargeSession();
            if (!chargeId.equals(chargeSession2.getCharge_id())) {
                Log.w("C2NFCChargeHandler.handleFinIndicate", "ignore fin indicate, port: " + this.port + ", peer charge id: " + chargeId + ", local charge id: " + chargeSession2.getCharge_id());
            } else if (NFC_CHARGE_STATUS.init_sended.equals(this.status) || NFC_CHARGE_STATUS.inited.equals(this.status) || NFC_CHARGE_STATUS.charging.equals(this.status) || NFC_CHARGE_STATUS.stop_sended.equals(this.status) || NFC_CHARGE_STATUS.stopped.equals(this.status) || NFC_CHARGE_STATUS.fin_sended.equals(this.status)) {
                FinDirective fin = (FinDirective) new FinDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(fin);
                stopTimer(this.status);
                if (NFC_CHARGE_STATUS.init_sended.equals(this.status)) {
                    if (!FIN_MODE.no_feerate.equals(fin.getFin_mode()) || !CHARGE_PLATFORM.xcharge.equals(chargeSession2.getCharge_platform()) || !NFC_CARD_TYPE.U3.equals(chargeSession2.getCardType())) {
                        C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_UI_OUT_OF_DISTURB).toJson()));
                    } else {
                        Log.w("C2NFCChargeHandler.handleFinIndicate", "no fee rate for U3, keep this status: " + this.status.getStatus() + ", port: " + this.port);
                        this.handlerTimer.startTimer(10000, MSG_TIMEOUT_INIT_SENDED, (Object) null);
                        return;
                    }
                }
                ackResponse(DCAPProxy.getInstance().createCAPResponseByIndcate(indicate));
                clearChargeSession();
                this.status = NFC_CHARGE_STATUS.idle;
            } else {
                Log.w("C2NFCChargeHandler.handleFinIndicate", "ignore fin indicate, port: " + this.port + ", status: " + this.status.getStatus());
            }
        } catch (Exception e) {
            Log.e("C2NFCChargeHandler.handleFinIndicate", "indicate: " + indicate.toJson() + ", exception: " + Log.getStackTraceString(e));
        }
    }

    private void handleEventIndicate(DCAPMessage event) {
        try {
            CAPDirectiveOption opt = ((CAPMessage) event.getData()).getOpt();
            String eventId = opt.getEvent_id();
            String chargeId = opt.getCharge_id();
            NFCChargeSession chargeSession2 = getChargeSession();
            if (!chargeId.equals(chargeSession2.getCharge_id())) {
                Log.w("C2NFCChargeHandler.handleEventIndicate", "ignore event indicate, port: " + this.port + ", peer charge id: " + chargeId + ", local charge id: " + chargeSession2.getCharge_id());
            } else if (EventDirective.EVENT_CHARGE_START.equals(eventId)) {
                if (NFC_CHARGE_STATUS.inited.equals(this.status)) {
                    stopTimer(this.status);
                    this.status = NFC_CHARGE_STATUS.charging;
                    return;
                }
                Log.w("C2NFCChargeHandler.handleEventIndicate", "ignore charge_start event indicate, port: " + this.port + ", status: " + this.status.getStatus());
            } else if (!EventDirective.EVENT_CHARGE_STOP.equals(eventId)) {
            } else {
                if (NFC_CHARGE_STATUS.charging.equals(this.status) || NFC_CHARGE_STATUS.stop_sended.equals(this.status)) {
                    stopTimer(this.status);
                    this.status = NFC_CHARGE_STATUS.stopped;
                    return;
                }
                Log.w("C2NFCChargeHandler.handleEventIndicate", "ignore charge_stop event indicate, port: " + this.port + ", status: " + this.status.getStatus());
            }
        } catch (Exception e) {
            Log.e("C2NFCChargeHandler.handleEventIndicate", "event: " + event.toJson() + ", exception: " + Log.getStackTraceString(e));
        }
    }

    private boolean checkDCAPMessageRoute(DCAPMessage msg) {
        String from = msg.getFrom();
        String to = msg.getTo();
        if (!from.startsWith("device:") || !to.startsWith("user:nfc.")) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void handleU1ChargeMsg(String cardUUID, String cardNo) {
        if (NFC_CHARGE_STATUS.idle.equals(this.status)) {
            Log.i("C2NFCChargeHandler.handleU1ChargeMsg", "charge begin ...");
            NFCChargeSession nfcChargeSession = getChargeSession();
            String sn = HardwareStatusCacheProvider.getInstance().getSn();
            nfcChargeSession.setCardNo(cardNo);
            nfcChargeSession.setCardUUID(cardUUID);
            nfcChargeSession.setSn(sn);
            nfcChargeSession.setCardType(NFC_CARD_TYPE.U1);
            nfcChargeSession.setUser_type(String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.U1.getType());
            nfcChargeSession.setUser_code(cardNo);
            nfcChargeSession.setDevice_id("sn/" + sn);
            nfcChargeSession.setPort(this.port);
            nfcChargeSession.setCharge_platform(SystemSettingCacheProvider.getInstance().getChargePlatform());
            nfcChargeSession.setTimeout_plugin(LocalSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalChargeCancel());
            authRequest((AuthSign) null);
            return;
        }
        endNFCCharge(cardUUID, cardNo, NFC_CARD_TYPE.U1);
    }

    /* access modifiers changed from: private */
    public void handleU2ChargeMsg(String cardUUID, String cardNo, int balance, byte[] key, XSign sign) {
        if (!NFC_CHARGE_STATUS.idle.equals(this.status)) {
            NFCChargeSession nfcChargeSession = getChargeSession();
            nfcChargeSession.setUser_balance((long) balance);
            nfcChargeSession.setXsign(sign);
            endNFCCharge(cardUUID, cardNo, NFC_CARD_TYPE.U2);
        } else if (!permitU2StartCharge(cardNo, balance, key, sign)) {
            Log.w("C2NFCChargeHandler.handleU2ChargeMsg", "not permit to start charge using card: " + cardNo + ", port: " + this.port);
        } else if (balance <= 0) {
            Log.w("C2NFCChargeHandler.handleU2ChargeMsg", "not permit to start charge for no balance !!! card no: " + cardNo + ", balance: " + balance + ", port: " + this.port);
            C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_CARD_BALANCE_NOT_ENOUGH).toJson()));
        } else {
            Log.i("C2NFCChargeHandler.handleU2ChargeMsg", "charge begin ..., port: " + this.port);
            NFCChargeSession nfcChargeSession2 = getChargeSession();
            String sn = HardwareStatusCacheProvider.getInstance().getSn();
            nfcChargeSession2.setCardNo(cardNo);
            nfcChargeSession2.setCardUUID(cardUUID);
            nfcChargeSession2.setKey(key);
            nfcChargeSession2.setUser_balance((long) balance);
            nfcChargeSession2.setSn(sn);
            nfcChargeSession2.setXsign(sign);
            nfcChargeSession2.setCardType(NFC_CARD_TYPE.U2);
            nfcChargeSession2.setUser_type(String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.U2.getType());
            nfcChargeSession2.setUser_code(cardNo);
            nfcChargeSession2.setDevice_id("sn/" + sn);
            nfcChargeSession2.setPort(this.port);
            nfcChargeSession2.setCharge_platform(SystemSettingCacheProvider.getInstance().getChargePlatform());
            nfcChargeSession2.setTimeout_plugin(LocalSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalChargeCancel());
            nfcChargeSession2.setTimeout_plugout(LocalSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalDelayStart());
            authRequest((AuthSign) null);
        }
    }

    /* access modifiers changed from: private */
    public void handleU3ChargeMsg(String cardUUID, String cardNo, AuthSign sign) {
        if (NFC_CHARGE_STATUS.idle.equals(this.status)) {
            Log.i("C2NFCChargeHandler.handleU3ChargeMsg", "charge begin ..., port: " + this.port);
            NFCChargeSession nfcChargeSession = getChargeSession();
            String sn = HardwareStatusCacheProvider.getInstance().getSn();
            nfcChargeSession.setCardNo(cardNo);
            nfcChargeSession.setCardUUID(cardUUID);
            nfcChargeSession.setSn(sn);
            nfcChargeSession.setCardType(NFC_CARD_TYPE.U3);
            nfcChargeSession.setUser_type(String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.U3.getType());
            nfcChargeSession.setUser_code(cardNo);
            nfcChargeSession.setDevice_id("sn/" + sn);
            nfcChargeSession.setPort(this.port);
            nfcChargeSession.setCharge_platform(SystemSettingCacheProvider.getInstance().getChargePlatform());
            nfcChargeSession.setTimeout_plugin(RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalChargeCancel());
            nfcChargeSession.setTimeout_plugout(RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalDelayStart());
            authRequest(sign);
            return;
        }
        endNFCCharge(cardUUID, cardNo, NFC_CARD_TYPE.U3);
    }

    /* access modifiers changed from: private */
    public void handleCTDemoChargeMsg(String cardUUID, String cardNo) {
        if (NFC_CHARGE_STATUS.idle.equals(this.status)) {
            Log.i("C2NFCChargeHandler.handleCTDemoChargeMsg", "charge begin ..., port: " + this.port);
            NFCChargeSession nfcChargeSession = getChargeSession();
            String sn = HardwareStatusCacheProvider.getInstance().getSn();
            nfcChargeSession.setCardNo(cardNo);
            nfcChargeSession.setCardUUID(cardUUID);
            nfcChargeSession.setSn(sn);
            nfcChargeSession.setCardType(NFC_CARD_TYPE.CT_DEMO);
            nfcChargeSession.setUser_type(String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.CT_DEMO.getType());
            nfcChargeSession.setUser_code(cardNo);
            nfcChargeSession.setDevice_id("sn/" + sn);
            nfcChargeSession.setPort(this.port);
            nfcChargeSession.setCharge_platform(CHARGE_PLATFORM.xcharge);
            nfcChargeSession.setTimeout_plugin(LocalSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalChargeCancel());
            authRequest((AuthSign) null);
            return;
        }
        endNFCCharge(cardUUID, cardNo, NFC_CARD_TYPE.CT_DEMO);
    }

    /* access modifiers changed from: private */
    public void handleAnyo1ChargeMsg(String cardUUID, String cardNo) {
        if (NFC_CHARGE_STATUS.idle.equals(this.status)) {
            Log.i("C2NFCChargeHandler.handleAnyo1ChargeMsg", "charge begin ..., port: " + this.port);
            NFCChargeSession nfcChargeSession = getChargeSession();
            String sn = HardwareStatusCacheProvider.getInstance().getSn();
            nfcChargeSession.setCardNo(cardNo);
            nfcChargeSession.setCardUUID(cardUUID);
            nfcChargeSession.setSn(sn);
            nfcChargeSession.setCardType(NFC_CARD_TYPE.anyo1);
            nfcChargeSession.setUser_type(String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.anyo1.getType());
            nfcChargeSession.setUser_code(cardNo);
            nfcChargeSession.setDevice_id("sn/" + sn);
            nfcChargeSession.setPort(this.port);
            nfcChargeSession.setCharge_platform(CHARGE_PLATFORM.anyo);
            nfcChargeSession.setTimeout_plugin(60);
            authRequest((AuthSign) null);
            return;
        }
        endNFCCharge(cardUUID, cardNo, NFC_CARD_TYPE.anyo1);
    }

    /* access modifiers changed from: private */
    public void handleAnyoSVWChargeMsg(String cardUUID, String cardNo) {
        if (NFC_CHARGE_STATUS.idle.equals(this.status)) {
            Log.i("C2NFCChargeHandler.handleAnyoSVWChargeMsg", "charge begin ..., port: " + this.port);
            NFCChargeSession nfcChargeSession = getChargeSession();
            String sn = HardwareStatusCacheProvider.getInstance().getSn();
            nfcChargeSession.setCardNo(cardNo);
            nfcChargeSession.setCardUUID(cardUUID);
            nfcChargeSession.setSn(sn);
            nfcChargeSession.setCardType(NFC_CARD_TYPE.anyo_svw);
            nfcChargeSession.setUser_type(String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.anyo_svw.getType());
            nfcChargeSession.setUser_code(cardNo);
            nfcChargeSession.setDevice_id("sn/" + sn);
            nfcChargeSession.setPort(this.port);
            nfcChargeSession.setCharge_platform(CHARGE_PLATFORM.anyo);
            nfcChargeSession.setTimeout_plugin(60);
            authRequest((AuthSign) null);
            return;
        }
        endNFCCharge(cardUUID, cardNo, NFC_CARD_TYPE.anyo_svw);
    }

    /* access modifiers changed from: private */
    public void handleCDDZJianQuanChargeMsg(String cardUUID, String cardNo) {
        if (NFC_CHARGE_STATUS.idle.equals(this.status)) {
            Log.i("C2NFCChargeHandler.handleCDDZJianQuanChargeMsg", "charge begin ..., port: " + this.port);
            NFCChargeSession nfcChargeSession = getChargeSession();
            String sn = HardwareStatusCacheProvider.getInstance().getSn();
            nfcChargeSession.setCardNo(cardNo);
            nfcChargeSession.setCardUUID(cardUUID);
            nfcChargeSession.setSn(sn);
            nfcChargeSession.setCardType(NFC_CARD_TYPE.cddz_1);
            nfcChargeSession.setUser_type(String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.cddz_1.getType());
            nfcChargeSession.setUser_code(cardNo);
            nfcChargeSession.setDevice_id("sn/" + sn);
            nfcChargeSession.setPort(this.port);
            nfcChargeSession.setCharge_platform(CHARGE_PLATFORM.cddz);
            nfcChargeSession.setTimeout_plugin(60);
            authRequest((AuthSign) null);
            return;
        }
        endNFCCharge(cardUUID, cardNo, NFC_CARD_TYPE.cddz_1);
    }

    /* access modifiers changed from: private */
    public void handleOCPPChargeMsg(String cardUUID, String cardNo) {
        if (!NFC_CHARGE_STATUS.idle.equals(this.status)) {
            endNFCCharge(cardUUID, cardNo, NFC_CARD_TYPE.ocpp);
        } else if (CHARGE_STATUS.CHARGING.equals(ChargeStatusCacheProvider.getInstance().getPortStatus(this.port).getChargeStatus())) {
            HashMap<String, Object> params = new HashMap<>();
            params.put("action", "end_charging");
            params.put("cardNo", cardNo);
            params.put("cardType", NFC_CARD_TYPE.ocpp);
            queryRequest(QueryDirective.QUERY_ID_CARD_STATUS, params);
        } else {
            Log.i("C2NFCChargeHandler.handleOCPPChargeMsg", "charge begin ..., port: " + this.port);
            NFCChargeSession nfcChargeSession = getChargeSession();
            String sn = HardwareStatusCacheProvider.getInstance().getSn();
            nfcChargeSession.setCardNo(cardNo);
            nfcChargeSession.setCardUUID(cardUUID);
            nfcChargeSession.setSn(sn);
            nfcChargeSession.setCardType(NFC_CARD_TYPE.ocpp);
            nfcChargeSession.setUser_type(String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.ocpp.getType());
            nfcChargeSession.setUser_code(cardNo);
            nfcChargeSession.setDevice_id("sn/" + sn);
            nfcChargeSession.setPort(this.port);
            nfcChargeSession.setCharge_platform(CHARGE_PLATFORM.ocpp);
            nfcChargeSession.setTimeout_plugin(RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalChargeCancel());
            authRequest((AuthSign) null);
        }
    }

    private void endNFCCharge(String cardUUID, String cardNo, NFC_CARD_TYPE cardType) {
        Log.i("C2NFCChargeHandler.endNFCCharge", "charge end ..., status: " + this.status.getStatus());
        NFCChargeSession nfcChargeSession = getChargeSession();
        if (isChargeUser(cardUUID, cardNo, cardType)) {
            if (NFC_CARD_TYPE.U2.equals(nfcChargeSession.getCardType())) {
                if (!handlerU2ReleaseAndPay()) {
                    return;
                }
            } else if (!NFC_CARD_TYPE.ocpp.equals(cardType) || !NFC_CHARGE_STATUS.charging.equals(this.status)) {
                C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
            }
            if (NFC_CHARGE_STATUS.charging.equals(this.status)) {
                if (NFC_CARD_TYPE.ocpp.equals(cardType)) {
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("action", "end_charging");
                    params.put("cardNo", cardNo);
                    params.put("cardType", NFC_CARD_TYPE.ocpp);
                    queryRequest(QueryDirective.QUERY_ID_CARD_STATUS, params);
                    return;
                }
                stopTimer(this.status);
                finRequest(FIN_MODE.normal, (ErrorCode) null);
            } else if (NFC_CHARGE_STATUS.init_sended.equals(this.status) || NFC_CHARGE_STATUS.inited.equals(this.status)) {
                stopTimer(this.status);
                finRequest(FIN_MODE.cancel, (ErrorCode) null);
            } else if (NFC_CHARGE_STATUS.auth_sended.equals(this.status)) {
                stopTimer(this.status);
                clearChargeSession();
                this.status = NFC_CHARGE_STATUS.idle;
            } else if (NFC_CHARGE_STATUS.stopped.equals(this.status)) {
                stopTimer(this.status);
                finRequest(FIN_MODE.normal, (ErrorCode) null);
            }
        } else {
            Log.w("C2NFCChargeHandler.endNFCCharge", "not charge card !!! charge card no: " + nfcChargeSession.getCardNo() + ", but scan card no: " + cardNo);
            C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_NOT_INIT_CHARGE_CARD).toJson()));
        }
    }

    private boolean handlerU2ReleaseAndPay() {
        NFCChargeSession nfcChargeSession = getChargeSession();
        String cardNo = nfcChargeSession.getCardNo();
        if (NFC_CHARGE_STATUS.inited.equals(this.status) || NFC_CHARGE_STATUS.charging.equals(this.status)) {
            if (!isU2Reserved(cardNo, (int) (nfcChargeSession.getUser_balance() & XMSZHead.ID_BROADCAST), nfcChargeSession.getKey(), nfcChargeSession.getXsign())) {
                Log.w("C2NFCChargeHandler.handlerU2ReleaseAndPay", "not start charge on this pile using u2 card: " + cardNo + ", port: " + this.port + ", status: " + this.status.getStatus());
                if (NFC_CHARGE_STATUS.charging.equals(this.status)) {
                    return pay4NowBill(false);
                }
                C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
                return true;
            } else if (NFC_CHARGE_STATUS.inited.equals(this.status)) {
                if (!releaseU2((int) (nfcChargeSession.getUser_balance() & XMSZHead.ID_BROADCAST), nfcChargeSession.getKey())) {
                    Log.w("C2NFCChargeHandler.handlerU2ReleaseAndPay", "failed to release u2 card: " + cardNo + ", port: " + this.port + ", status: " + this.status.getStatus());
                    C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_CARD_RELEASE_FAIL).toJson()));
                    return false;
                }
                C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
                return true;
            } else if (NFC_CHARGE_STATUS.charging.equals(this.status)) {
                return pay4NowBill(true);
            }
        } else if (NFC_CHARGE_STATUS.stopped.equals(this.status)) {
            if (isU2Reserved(cardNo, (int) (nfcChargeSession.getUser_balance() & XMSZHead.ID_BROADCAST), nfcChargeSession.getKey(), nfcChargeSession.getXsign())) {
                return pay4NowBill(true);
            }
            Log.w("C2NFCChargeHandler.handlerU2ReleaseAndPay", "not start charge on this pile using u2 card: " + cardNo + ", port: " + this.port + ", status: " + this.status.getStatus());
            return pay4NowBill(false);
        }
        C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
        return true;
    }

    private boolean pay4NowBill(boolean isReserved) {
        NFCChargeSession nfcChargeSession = getChargeSession();
        String cardNo = nfcChargeSession.getCardNo();
        String billId = nfcChargeSession.getCharge_id();
        if (nfcChargeSession.isPaid()) {
            Log.i("C2NFCChargeHandler.pay4NowBill", "bill has been paid !!! card: " + cardNo + ", bill: " + billId);
            C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
            return true;
        }
        int cardBalance = (int) (nfcChargeSession.getUser_balance() & XMSZHead.ID_BROADCAST);
        ChargeBill bill = ChargeContentProxy.getInstance().getChargeBill(billId);
        if (bill != null) {
            int billFee = bill.getTotal_fee();
            if (billFee > 0) {
                if (cardBalance < billFee) {
                    Log.w("C2NFCChargeHandler.pay4NowBill", "not enough balance!!! card: " + cardNo + ", bill fee: " + billFee + ", balance: " + cardBalance);
                    C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_CARD_BALANCE_NOT_ENOUGH).toJson()));
                    return true;
                } else if (NFCUtils.consumeU2(cardBalance, billFee, nfcChargeSession.getKey(), nfcChargeSession.getCardUUID(), nfcChargeSession.getCardNo(), 0)) {
                    Log.i("C2NFCChargeHandler.pay4NowBill", "succeeded to consume !!! card: " + cardNo + ", bill fee: " + billFee + ", balance: " + nfcChargeSession.getUser_balance());
                    ChargeContentProxy.getInstance().setPaidFlag(billId, 1);
                    nfcChargeSession.setUser_balance((long) (cardBalance - billFee));
                    nfcChargeSession.setPaid(true);
                    C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
                    return true;
                } else {
                    Log.w("C2NFCChargeHandler.pay4NowBill", "failed to consume !!! card: " + cardNo + ", bill fee: " + billFee + ", balance: " + cardBalance);
                    ErrorCode error = new ErrorCode(ErrorCode.EC_NFC_CARD_CONSUME_FAIL);
                    HashMap<String, String> attachData = new HashMap<>();
                    attachData.put(ContentDB.NFCConsumeFailCacheTable.BALANCE, String.valueOf(cardBalance));
                    attachData.put(ChargeStopCondition.TYPE_FEE, String.valueOf(billFee));
                    error.setData(attachData);
                    C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, error.toJson()));
                    return false;
                }
            } else if (isReserved) {
                Log.i("C2NFCChargeHandler.pay4NowBill", "bill fee is zero, release u2 card: " + cardNo + ", bill: " + billId);
                if (!releaseU2(cardBalance, nfcChargeSession.getKey())) {
                    Log.w("C2NFCChargeHandler.pay4NowBill", "failed to release u2 card: " + cardNo + ", port: " + this.port + ", status: " + this.status.getStatus());
                    C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_CARD_RELEASE_FAIL).toJson()));
                    return false;
                }
                nfcChargeSession.setPaid(true);
                C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
                return true;
            } else {
                nfcChargeSession.setPaid(true);
                C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
                return true;
            }
        } else {
            Log.w("C2NFCChargeHandler.pay4NowBill", "no bill !!! card: " + cardNo + ", bill: " + billId);
            C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_ERROR).toJson()));
            return false;
        }
    }

    public DCAPMessage createRequest(String from, String op, CAPDirectiveOption opt, Object directive) {
        String sn = HardwareStatusCacheProvider.getInstance().getSn();
        CAPMessage requestCap = new CAPMessage();
        DCAPMessage request = new DCAPMessage();
        request.setFrom(from);
        request.setTo("device:sn/" + sn);
        request.setType("cap");
        request.setCtime(System.currentTimeMillis());
        request.setSeq(Sequence.getAgentDCAPSequence());
        requestCap.setOp(op);
        requestCap.setOpt(opt);
        requestCap.setData(directive);
        request.setData(requestCap);
        return request;
    }

    private void authRequest(AuthSign cardSign) {
        NFCChargeSession session = getChargeSession();
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setAuth_id("init");
        opt.setPort_id(session.getPort());
        AuthDirective auth = new AuthDirective();
        auth.setInit_type(CHARGE_INIT_TYPE.nfc);
        auth.setUser_type(session.getUser_type());
        auth.setUser_code(session.getUser_code());
        auth.setDevice_id(session.getDevice_id());
        auth.setPort(session.getPort());
        if (cardSign != null) {
            HashMap<String, Object> userData = new HashMap<>();
            userData.put("timestamp", String.valueOf(cardSign.getTime()));
            userData.put("nonce", cardSign.getRand());
            userData.put("sign", cardSign.getSign());
            auth.setUser_data(userData);
        }
        DCAPMessage request = createRequest("user:" + session.getUser_type() + MqttTopic.TOPIC_LEVEL_SEPARATOR + session.getUser_code(), "auth", opt, auth);
        DCAPProxy.getInstance().sendRequest(request);
        this.sendDCAPReqestState.put(String.valueOf(request.getSeq()), request);
        this.handlerTimer.startTimer(30000, MSG_TIMEOUT_DCAP_REQUEST, request);
        this.status = NFC_CHARGE_STATUS.auth_sended;
        this.handlerTimer.startTimer(10000, MSG_TIMEOUT_AUTH_SENDED, (Object) null);
    }

    private void initRequest() {
        NFCChargeSession session = getChargeSession();
        String chargeId = session.getCharge_id();
        if (TextUtils.isEmpty(chargeId)) {
            chargeId = LocalIdGenerator.getChargeId();
        }
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(chargeId);
        InitDirective init = new InitDirective();
        init.setInit_type(CHARGE_INIT_TYPE.nfc);
        init.setUser_type(session.getUser_type());
        init.setUser_code(session.getUser_code());
        init.setDevice_id(session.getDevice_id());
        init.setPort(session.getPort());
        init.setFee_rate(session.getFee_rate());
        init.setUser_tc_type(session.getUser_tc_type());
        init.setUser_tc_value(session.getUser_tc_value());
        init.setUser_balance(session.getUser_balance());
        init.setIs_free(session.getIs_free());
        init.setBinded_user(session.getBinded_user());
        init.setCharge_platform(session.getCharge_platform());
        init.setTimeout_plugin(session.getTimeout_plugin());
        init.setTimeout_start(session.getTimeout_start());
        init.setTimeout_plugout(session.getTimeout_plugout());
        DCAPProxy.getInstance().sendRequest(createRequest("user:" + session.getUser_type() + MqttTopic.TOPIC_LEVEL_SEPARATOR + session.getUser_code(), "init", opt, init));
        this.status = NFC_CHARGE_STATUS.init_sended;
        this.handlerTimer.startTimer(10000, MSG_TIMEOUT_INIT_SENDED, (Object) null);
    }

    private void stopRequest() {
        NFCChargeSession session = getChargeSession();
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(session.getCharge_id());
        DCAPMessage request = createRequest("user:" + session.getUser_type() + MqttTopic.TOPIC_LEVEL_SEPARATOR + session.getUser_code(), "stop", opt, new StopDirective());
        DCAPProxy.getInstance().sendRequest(request);
        this.sendDCAPReqestState.put(String.valueOf(request.getSeq()), request);
        this.handlerTimer.startTimer(30000, MSG_TIMEOUT_DCAP_REQUEST, request);
        this.status = NFC_CHARGE_STATUS.stop_sended;
        this.handlerTimer.startTimer(10000, MSG_TIMEOUT_STOP_SENDED, (Object) null);
    }

    /* access modifiers changed from: private */
    public void finRequest(FIN_MODE mode, ErrorCode error) {
        NFCChargeSession session = getChargeSession();
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(session.getCharge_id());
        FinDirective fin = new FinDirective();
        fin.setFin_mode(mode);
        fin.setError(error);
        DCAPMessage request = createRequest("user:" + session.getUser_type() + MqttTopic.TOPIC_LEVEL_SEPARATOR + session.getUser_code(), "fin", opt, fin);
        DCAPProxy.getInstance().sendRequest(request);
        this.sendDCAPReqestState.put(String.valueOf(request.getSeq()), request);
        this.handlerTimer.startTimer(30000, MSG_TIMEOUT_DCAP_REQUEST, request);
        this.status = NFC_CHARGE_STATUS.fin_sended;
        this.handlerTimer.startTimer(10000, MSG_TIMEOUT_FIN_SENDED, (Object) null);
    }

    private void fin3rdCharge(String chargeId, String from, FIN_MODE mode, ErrorCode error) {
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(chargeId);
        FinDirective fin = new FinDirective();
        fin.setFin_mode(mode);
        fin.setError(error);
        DCAPProxy.getInstance().sendRequest(createRequest(from, "fin", opt, fin));
    }

    private void queryRequest(String queryId, HashMap<String, Object> params) {
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setQuery_id(queryId);
        QueryDirective query = new QueryDirective();
        query.setParams(params);
        DCAPMessage request = createRequest("user:nfc.", "query", opt, query);
        DCAPProxy.getInstance().sendRequest(request);
        this.sendDCAPReqestState.put(String.valueOf(request.getSeq()), request);
        this.handlerTimer.startTimer(30000, MSG_TIMEOUT_DCAP_REQUEST, request);
    }

    public boolean ackResponse(DCAPMessage response) {
        return DCAPProxy.getInstance().sendResponse(response, "cap", "ack", new AckDirective());
    }

    private void stopTimer(NFC_CHARGE_STATUS status2) {
        switch (m20x4f4c7f31()[status2.ordinal()]) {
            case 2:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_AUTH_SENDED);
                return;
            case 3:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_INIT_SENDED);
                return;
            case 6:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_STOP_SENDED);
                return;
            case 8:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_FIN_SENDED);
                return;
            default:
                return;
        }
    }

    private boolean permitU2StartCharge(String cardNo, int balance, byte[] key, XSign sign) {
        if (NFCUtils.checkU2Sign(NFCUtils.intToBytes(balance), key, sign, (byte[]) null)) {
            return true;
        }
        if (NFCUtils.checkU2Sign(NFCUtils.intToBytes(balance), key, sign, HardwareStatusCacheProvider.getInstance().getSn().getBytes())) {
            Log.w("C2NFCChargeHandler.permitU2StartCharge", "maybe not release on this pile !!! card: " + cardNo);
            if (releaseU2(balance, key)) {
                return true;
            }
            Log.w("C2NFCChargeHandler.permitU2StartCharge", "failed to release card: " + cardNo);
            C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_CARD_RESERVE_FAIL).toJson()));
            return false;
        }
        Log.w("C2NFCChargeHandler.permitU2StartCharge", "maybe reserved by other pile !!! card: " + cardNo);
        C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_CARD_RESERVED).toJson()));
        return false;
    }

    private boolean reserveU2(int balance, byte[] key) {
        String sn = HardwareStatusCacheProvider.getInstance().getSn();
        boolean isOK = NFCUtils.setUserCardSign(key, NFCUtils.signU2(NFCUtils.intToBytes(balance), key, 0, sn.getBytes()));
        if (isOK) {
            Log.d("C2NFCChargeHandler.reserveU2", "succeed to reserve card, balance: " + balance + ", sn: " + sn);
        }
        return isOK;
    }

    private boolean isU2Reserved(String cardNo, int balance, byte[] key, XSign sign) {
        if (NFCUtils.checkU2Sign(NFCUtils.intToBytes(balance), key, sign, HardwareStatusCacheProvider.getInstance().getSn().getBytes())) {
            return true;
        }
        Log.w("C2NFCChargeHandler.isU2Reserved", "not reserved by this pile !!! card: " + cardNo);
        return false;
    }

    private boolean releaseU2(int balance, byte[] key) {
        boolean isOk = NFCUtils.setUserCardSign(key, NFCUtils.signU2(NFCUtils.intToBytes(balance), key, 0, (byte[]) null));
        if (isOk) {
            Log.d("C2NFCChargeHandler.reserveU2", "succeed to release card, balance: " + balance);
        }
        return isOk;
    }
}
