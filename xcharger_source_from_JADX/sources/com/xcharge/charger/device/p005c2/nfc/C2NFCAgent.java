package com.xcharge.charger.device.p005c2.nfc;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.android.chargerhd.chargerhdNative;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.data.bean.ConsumeFailCache;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.FeeRate;
import com.xcharge.charger.data.bean.PortFeeRate;
import com.xcharge.charger.data.bean.XKeyseed;
import com.xcharge.charger.data.bean.device.NFC;
import com.xcharge.charger.data.bean.setting.ChargeSetting;
import com.xcharge.charger.data.bean.setting.ConsoleSetting;
import com.xcharge.charger.data.bean.setting.FeeRateSetting;
import com.xcharge.charger.data.bean.setting.GunLockSetting;
import com.xcharge.charger.data.bean.setting.PortSetting;
import com.xcharge.charger.data.bean.setting.RadarSetting;
import com.xcharge.charger.data.bean.setting.SwipeCardPermission;
import com.xcharge.charger.data.bean.setting.UserDefineUISetting;
import com.xcharge.charger.data.bean.type.GUN_LOCK_MODE;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.bean.type.NFC_OPR_TYPE;
import com.xcharge.charger.data.bean.type.WORK_MODE;
import com.xcharge.charger.data.p004db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.NFCConsumeFailCacheContentProxy;
import com.xcharge.charger.data.proxy.NFCKeyContentProxy;
import com.xcharge.charger.device.p005c2.bean.AuthSign;
import com.xcharge.charger.device.p005c2.bean.CDDZCardKeySeeds;
import com.xcharge.charger.device.p005c2.bean.ManageCardData;
import com.xcharge.charger.device.p005c2.bean.NFCCardIDData;
import com.xcharge.charger.device.p005c2.bean.NFCEventData;
import com.xcharge.charger.device.p005c2.bean.NFCSign;
import com.xcharge.charger.device.p005c2.bean.XSign;
import com.xcharge.charger.device.p005c2.nfc.charge.C2NFCChargeHandler;
import com.xcharge.charger.device.p005c2.service.C2DeviceEventDispatcher;
import com.xcharge.charger.device.p005c2.service.C2DeviceProxy;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.utils.BCDUtils;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;
import com.xcharge.common.utils.HandlerTimer;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.jni.echargenet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.CharEncoding;

/* renamed from: com.xcharge.charger.device.c2.nfc.C2NFCAgent */
public class C2NFCAgent {
    public static final int MSG_NFC_CANCEL_U1_BIND = 24582;
    public static final int MSG_NFC_CARD_ARRIVAL = 24577;
    public static final int MSG_NFC_CARD_BIND_U1 = 24579;
    public static final int MSG_NFC_CARD_LEFT = 24578;
    public static final int MSG_NFC_SWIPE_CARD_ERROR = 24581;
    public static final int MSG_NFC_SWIPE_CARD_OK = 24580;
    public static final int MSG_TIMEOUT_NFC_U1_BIND = 24592;
    public static final long U1_BIND_TIMEOUT = 60000;
    private static HashMap<String, C2NFCAgent> instances = null;
    private int cardUUID = 0;
    /* access modifiers changed from: private */
    public C2NFCChargeHandler chargeHandler = null;
    private Context context = null;
    /* access modifiers changed from: private */
    public MsgHandler handler = null;
    /* access modifiers changed from: private */
    public HandlerTimer handlerTimer = null;
    /* access modifiers changed from: private */
    public boolean isU1BindMode = false;
    /* access modifiers changed from: private */
    public NFC needRecoveryNFCStatus = null;
    /* access modifiers changed from: private */
    public NFC nfcStatus = new NFC();
    /* access modifiers changed from: private */
    public String port = null;
    private HandlerThread thread = null;

    /* renamed from: com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler */
    private class MsgHandler extends Handler {
        private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$NFC_CARD_TYPE;

        static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$NFC_CARD_TYPE() {
            int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$NFC_CARD_TYPE;
            if (iArr == null) {
                iArr = new int[NFC_CARD_TYPE.values().length];
                try {
                    iArr[NFC_CARD_TYPE.CT_DEMO.ordinal()] = 6;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[NFC_CARD_TYPE.M1.ordinal()] = 1;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[NFC_CARD_TYPE.M2.ordinal()] = 2;
                } catch (NoSuchFieldError e3) {
                }
                try {
                    iArr[NFC_CARD_TYPE.U1.ordinal()] = 3;
                } catch (NoSuchFieldError e4) {
                }
                try {
                    iArr[NFC_CARD_TYPE.U2.ordinal()] = 4;
                } catch (NoSuchFieldError e5) {
                }
                try {
                    iArr[NFC_CARD_TYPE.U3.ordinal()] = 5;
                } catch (NoSuchFieldError e6) {
                }
                try {
                    iArr[NFC_CARD_TYPE.anyo1.ordinal()] = 7;
                } catch (NoSuchFieldError e7) {
                }
                try {
                    iArr[NFC_CARD_TYPE.anyo_svw.ordinal()] = 8;
                } catch (NoSuchFieldError e8) {
                }
                try {
                    iArr[NFC_CARD_TYPE.cddz_1.ordinal()] = 12;
                } catch (NoSuchFieldError e9) {
                }
                try {
                    iArr[NFC_CARD_TYPE.cddz_2.ordinal()] = 13;
                } catch (NoSuchFieldError e10) {
                }
                try {
                    iArr[NFC_CARD_TYPE.cddz_m.ordinal()] = 11;
                } catch (NoSuchFieldError e11) {
                }
                try {
                    iArr[NFC_CARD_TYPE.ecw1.ordinal()] = 10;
                } catch (NoSuchFieldError e12) {
                }
                try {
                    iArr[NFC_CARD_TYPE.ocpp.ordinal()] = 14;
                } catch (NoSuchFieldError e13) {
                }
                try {
                    iArr[NFC_CARD_TYPE.ptne1.ordinal()] = 9;
                } catch (NoSuchFieldError e14) {
                }
                $SWITCH_TABLE$com$xcharge$charger$data$bean$type$NFC_CARD_TYPE = iArr;
            }
            return iArr;
        }

        public MsgHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r25) {
            /*
                r24 = this;
                com.xcharge.charger.data.provider.SystemSettingCacheProvider r19 = com.xcharge.charger.data.provider.SystemSettingCacheProvider.getInstance()     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                java.lang.String r20 = r20.port     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.data.bean.setting.SwipeCardPermission r15 = r19.getPortSwipeCardPermission(r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r25
                int r0 = r0.what     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                switch(r19) {
                    case 24577: goto L_0x001f;
                    case 24578: goto L_0x07e0;
                    case 24579: goto L_0x0840;
                    case 24580: goto L_0x095c;
                    case 24581: goto L_0x0a37;
                    case 24582: goto L_0x0922;
                    case 24583: goto L_0x001b;
                    case 24584: goto L_0x001b;
                    case 24585: goto L_0x001b;
                    case 24586: goto L_0x001b;
                    case 24587: goto L_0x001b;
                    case 24588: goto L_0x001b;
                    case 24589: goto L_0x001b;
                    case 24590: goto L_0x001b;
                    case 24591: goto L_0x001b;
                    case 24592: goto L_0x08f7;
                    default: goto L_0x001b;
                }
            L_0x001b:
                super.handleMessage(r25)
            L_0x001e:
                return
            L_0x001f:
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "NFC card arrival !!! port: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.i(r19, r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                boolean r19 = r19.isHandleStatus()     // Catch:{ Exception -> 0x00fe }
                if (r19 == 0) goto L_0x0168
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "refuse because latest swipe is processing: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                com.xcharge.charger.data.bean.device.NFC r21 = r21.nfcStatus     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = r21.toJson()     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.w(r19, r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.data.bean.device.NFC r14 = r19.clone()     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r20 = 0
                r19.setHandleStatus(r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.data.bean.ErrorCode r20 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x00fe }
                r21 = 40030(0x9c5e, float:5.6094E-41)
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r19.setLatestError(r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                long r20 = java.lang.System.currentTimeMillis()     // Catch:{ Exception -> 0x00fe }
                r19.setTs(r20)     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.device.c2.service.C2DeviceEventDispatcher r19 = com.xcharge.charger.device.p005c2.service.C2DeviceEventDispatcher.getInstance()     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                java.lang.String r20 = r20.port     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                com.xcharge.charger.data.bean.device.NFC r21 = r21.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r19.handleNFCStatus(r20, r21)     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.device.c2.service.C2DeviceProxy r19 = com.xcharge.charger.device.p005c2.service.C2DeviceProxy.getInstance()     // Catch:{ Exception -> 0x00fe }
                r20 = 3
                r19.beep(r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                r0 = r19
                r0.nfcStatus = r14     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.device.c2.service.C2DeviceEventDispatcher r19 = com.xcharge.charger.device.p005c2.service.C2DeviceEventDispatcher.getInstance()     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                java.lang.String r20 = r20.port     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                com.xcharge.charger.data.bean.device.NFC r21 = r21.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r19.handleNFCStatus(r20, r21)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001e
            L_0x00fe:
                r8 = move-exception
                java.lang.String r19 = "C2NFCAgent.handleMessage"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder
                java.lang.String r21 = "except: "
                r20.<init>(r21)
                java.lang.String r21 = android.util.Log.getStackTraceString(r8)
                java.lang.StringBuilder r20 = r20.append(r21)
                java.lang.String r20 = r20.toString()
                android.util.Log.e(r19, r20)
                java.lang.StringBuilder r19 = new java.lang.StringBuilder
                java.lang.String r20 = "C2NFCAgent handleMessage exception: "
                r19.<init>(r20)
                java.lang.String r20 = android.util.Log.getStackTraceString(r8)
                java.lang.StringBuilder r19 = r19.append(r20)
                java.lang.String r19 = r19.toString()
                com.xcharge.common.utils.LogUtils.syslog(r19)
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus
                boolean r19 = r19.isHandleStatus()
                if (r19 == 0) goto L_0x001b
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this
                r19 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r19 = r19.handler
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this
                r20 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r20 = r20.handler
                r21 = 24581(0x6005, float:3.4445E-41)
                com.xcharge.charger.data.bean.ErrorCode r22 = new com.xcharge.charger.data.bean.ErrorCode
                r23 = 40000(0x9c40, float:5.6052E-41)
                r22.<init>(r23)
                java.lang.String r22 = r22.toJson()
                android.os.Message r20 = r20.obtainMessage(r21, r22)
                r19.sendMessage(r20)
                goto L_0x001b
            L_0x0168:
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r20 = 1
                r19.setArrived(r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r20 = 1
                r19.setHandleStatus(r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r20 = 0
                r19.setTs(r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r20 = 0
                r19.setLatestCardNo(r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r20 = 0
                r19.setLatestCardType(r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.data.bean.ErrorCode r20 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x00fe }
                r21 = 200(0xc8, float:2.8E-43)
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r19.setLatestError(r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r20 = 0
                r19.setLatestOprType(r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r20 = 0
                r19.setBalance(r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r20 = 0
                r19.setFee(r20)     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.device.c2.service.C2DeviceEventDispatcher r19 = com.xcharge.charger.device.p005c2.service.C2DeviceEventDispatcher.getInstance()     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                java.lang.String r20 = r20.port     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                com.xcharge.charger.data.bean.device.NFC r21 = r21.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r19.handleNFCStatus(r20, r21)     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.device.c2.bean.NFCEventData r20 = new com.xcharge.charger.device.c2.bean.NFCEventData     // Catch:{ Exception -> 0x00fe }
                r20.<init>()     // Catch:{ Exception -> 0x00fe }
                r0 = r25
                java.lang.Object r0 = r0.obj     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                java.lang.String r19 = (java.lang.String) r19     // Catch:{ Exception -> 0x00fe }
                r0 = r20
                r1 = r19
                java.lang.Object r7 = r0.fromJson(r1)     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.device.c2.bean.NFCEventData r7 = (com.xcharge.charger.device.p005c2.bean.NFCEventData) r7     // Catch:{ Exception -> 0x00fe }
                int r19 = r7.getUuid()     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r7.getSzuuid()     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.device.c2.bean.NFCCardIDData r4 = com.xcharge.charger.device.p005c2.nfc.NFCUtils.distinguishCard(r19, r20)     // Catch:{ Exception -> 0x00fe }
                if (r4 != 0) goto L_0x025f
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r19 = r19.handler     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r20 = r20.handler     // Catch:{ Exception -> 0x00fe }
                r21 = 24581(0x6005, float:3.4445E-41)
                com.xcharge.charger.data.bean.ErrorCode r22 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x00fe }
                r23 = 40001(0x9c41, float:5.6053E-41)
                r22.<init>(r23)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r22 = r22.toJson()     // Catch:{ Exception -> 0x00fe }
                android.os.Message r20 = r20.obtainMessage(r21, r22)     // Catch:{ Exception -> 0x00fe }
                r19.sendMessage(r20)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001e
            L_0x025f:
                java.lang.String r5 = r4.getCardNo()     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.data.bean.type.NFC_CARD_TYPE r6 = r4.getCardType()     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r0 = r19
                r0.setLatestCardType(r6)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r0 = r19
                r0.setLatestCardNo(r5)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "card type: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r20
                java.lang.StringBuilder r20 = r0.append(r6)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = ", card no: "
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r20
                java.lang.StringBuilder r20 = r0.append(r5)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = ", port: "
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.i(r19, r20)     // Catch:{ Exception -> 0x00fe }
                boolean r19 = r15.isPermitTest()     // Catch:{ Exception -> 0x00fe }
                if (r19 == 0) goto L_0x0316
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "in test mode, only distinguish card !!! card type: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r20
                java.lang.StringBuilder r20 = r0.append(r6)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = ", card no: "
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r20
                java.lang.StringBuilder r20 = r0.append(r5)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = ", port: "
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.w(r19, r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r19 = r19.handler     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r20 = r20.handler     // Catch:{ Exception -> 0x00fe }
                r21 = 24580(0x6004, float:3.4444E-41)
                android.os.Message r20 = r20.obtainMessage(r21)     // Catch:{ Exception -> 0x00fe }
                r19.sendMessage(r20)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001e
            L_0x0316:
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                r0 = r19
                boolean r19 = r0.swipeCardPermissionCheck(r6)     // Catch:{ Exception -> 0x00fe }
                if (r19 != 0) goto L_0x0379
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "refuse for swipe card is forbiden!!! card type: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r20
                java.lang.StringBuilder r20 = r0.append(r6)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = ", port: "
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.w(r19, r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r19 = r19.handler     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r20 = r20.handler     // Catch:{ Exception -> 0x00fe }
                r21 = 24581(0x6005, float:3.4445E-41)
                com.xcharge.charger.data.bean.ErrorCode r22 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x00fe }
                r23 = 40026(0x9c5a, float:5.6088E-41)
                r22.<init>(r23)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r22 = r22.toJson()     // Catch:{ Exception -> 0x00fe }
                android.os.Message r20 = r20.obtainMessage(r21, r22)     // Catch:{ Exception -> 0x00fe }
                r19.sendMessage(r20)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001e
            L_0x0379:
                com.xcharge.charger.data.provider.LocalSettingCacheProvider r19 = com.xcharge.charger.data.provider.LocalSettingCacheProvider.getInstance()     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.data.bean.setting.ChargeSetting r19 = r19.getChargeSetting()     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.data.bean.type.WORK_MODE r18 = r19.getWorkMode()     // Catch:{ Exception -> 0x00fe }
                int[] r19 = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$NFC_CARD_TYPE()     // Catch:{ Exception -> 0x00fe }
                int r20 = r6.ordinal()     // Catch:{ Exception -> 0x00fe }
                r19 = r19[r20]     // Catch:{ Exception -> 0x00fe }
                switch(r19) {
                    case 1: goto L_0x0394;
                    case 2: goto L_0x03fa;
                    case 3: goto L_0x0460;
                    case 4: goto L_0x04e3;
                    case 5: goto L_0x05b2;
                    case 6: goto L_0x0683;
                    case 7: goto L_0x0690;
                    case 8: goto L_0x0392;
                    case 9: goto L_0x0392;
                    case 10: goto L_0x0392;
                    case 11: goto L_0x070d;
                    case 12: goto L_0x0773;
                    case 13: goto L_0x0392;
                    case 14: goto L_0x07d3;
                    default: goto L_0x0392;
                }     // Catch:{ Exception -> 0x00fe }
            L_0x0392:
                goto L_0x001b
            L_0x0394:
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r19 = r19.chargeHandler     // Catch:{ Exception -> 0x00fe }
                boolean r19 = r19.isCharging()     // Catch:{ Exception -> 0x00fe }
                if (r19 != 0) goto L_0x03b1
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                r0 = r19
                r0.handleM1Card(r5, r7)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x03b1:
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "NFC charging ..., can not set now !!! port: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.w(r19, r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r19 = r19.handler     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r20 = r20.handler     // Catch:{ Exception -> 0x00fe }
                r21 = 24581(0x6005, float:3.4445E-41)
                com.xcharge.charger.data.bean.ErrorCode r22 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x00fe }
                r23 = 40007(0x9c47, float:5.6062E-41)
                r22.<init>(r23)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r22 = r22.toJson()     // Catch:{ Exception -> 0x00fe }
                android.os.Message r20 = r20.obtainMessage(r21, r22)     // Catch:{ Exception -> 0x00fe }
                r19.sendMessage(r20)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x03fa:
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r19 = r19.chargeHandler     // Catch:{ Exception -> 0x00fe }
                boolean r19 = r19.isCharging()     // Catch:{ Exception -> 0x00fe }
                if (r19 != 0) goto L_0x0417
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                r0 = r19
                r0.handleM2Card(r5, r7)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x0417:
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "NFC charging ..., can not set now !!! port: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.w(r19, r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r19 = r19.handler     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r20 = r20.handler     // Catch:{ Exception -> 0x00fe }
                r21 = 24581(0x6005, float:3.4445E-41)
                com.xcharge.charger.data.bean.ErrorCode r22 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x00fe }
                r23 = 40007(0x9c47, float:5.6062E-41)
                r22.<init>(r23)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r22 = r22.toJson()     // Catch:{ Exception -> 0x00fe }
                android.os.Message r20 = r20.obtainMessage(r21, r22)     // Catch:{ Exception -> 0x00fe }
                r19.sendMessage(r20)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x0460:
                java.lang.String r19 = "U100000000000000"
                r0 = r19
                boolean r19 = r0.equals(r5)     // Catch:{ Exception -> 0x00fe }
                if (r19 == 0) goto L_0x0477
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                r0 = r19
                r0.handleU1Card(r5, r7)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x0477:
                com.xcharge.charger.data.bean.type.WORK_MODE r19 = com.xcharge.charger.data.bean.type.WORK_MODE.personal     // Catch:{ Exception -> 0x00fe }
                boolean r19 = r18.equals(r19)     // Catch:{ Exception -> 0x00fe }
                if (r19 == 0) goto L_0x048c
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                r0 = r19
                r0.handleU1Card(r5, r7)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x048c:
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "refuse scan U1 !!! port: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = ", work mode: "
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = r18.getMode()     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.w(r19, r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r19 = r19.handler     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r20 = r20.handler     // Catch:{ Exception -> 0x00fe }
                r21 = 24581(0x6005, float:3.4445E-41)
                com.xcharge.charger.data.bean.ErrorCode r22 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x00fe }
                r23 = 40024(0x9c58, float:5.6086E-41)
                r22.<init>(r23)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r22 = r22.toJson()     // Catch:{ Exception -> 0x00fe }
                android.os.Message r20 = r20.obtainMessage(r21, r22)     // Catch:{ Exception -> 0x00fe }
                r19.sendMessage(r20)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x04e3:
                com.xcharge.charger.data.bean.type.WORK_MODE r19 = com.xcharge.charger.data.bean.type.WORK_MODE.group     // Catch:{ Exception -> 0x00fe }
                boolean r19 = r18.equals(r19)     // Catch:{ Exception -> 0x00fe }
                if (r19 == 0) goto L_0x04f8
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                r0 = r19
                r0.handleU2Card(r5, r7)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x04f8:
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                boolean r19 = r19.isU1BindMode     // Catch:{ Exception -> 0x00fe }
                if (r19 == 0) goto L_0x055b
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "refuse scan U2 for in U1 binding now !!! port: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = ", work mode: "
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = r18.getMode()     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.w(r19, r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r19 = r19.handler     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r20 = r20.handler     // Catch:{ Exception -> 0x00fe }
                r21 = 24581(0x6005, float:3.4445E-41)
                com.xcharge.charger.data.bean.ErrorCode r22 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x00fe }
                r23 = 40026(0x9c5a, float:5.6088E-41)
                r22.<init>(r23)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r22 = r22.toJson()     // Catch:{ Exception -> 0x00fe }
                android.os.Message r20 = r20.obtainMessage(r21, r22)     // Catch:{ Exception -> 0x00fe }
                r19.sendMessage(r20)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x055b:
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "refuse scan U2 !!! port: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = ", work mode: "
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = r18.getMode()     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.w(r19, r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r19 = r19.handler     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r20 = r20.handler     // Catch:{ Exception -> 0x00fe }
                r21 = 24581(0x6005, float:3.4445E-41)
                com.xcharge.charger.data.bean.ErrorCode r22 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x00fe }
                r23 = 40023(0x9c57, float:5.6084E-41)
                r22.<init>(r23)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r22 = r22.toJson()     // Catch:{ Exception -> 0x00fe }
                android.os.Message r20 = r20.obtainMessage(r21, r22)     // Catch:{ Exception -> 0x00fe }
                r19.sendMessage(r20)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x05b2:
                com.xcharge.charger.data.provider.ChargeStatusCacheProvider r19 = com.xcharge.charger.data.provider.ChargeStatusCacheProvider.getInstance()     // Catch:{ Exception -> 0x00fe }
                boolean r19 = r19.isCloudConnected()     // Catch:{ Exception -> 0x00fe }
                if (r19 == 0) goto L_0x062c
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                boolean r19 = r19.isU1BindMode     // Catch:{ Exception -> 0x00fe }
                if (r19 == 0) goto L_0x061f
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "refuse scan U3 for in U1 binding now !!! port: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = ", work mode: "
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = r18.getMode()     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.w(r19, r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r19 = r19.handler     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r20 = r20.handler     // Catch:{ Exception -> 0x00fe }
                r21 = 24581(0x6005, float:3.4445E-41)
                com.xcharge.charger.data.bean.ErrorCode r22 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x00fe }
                r23 = 40026(0x9c5a, float:5.6088E-41)
                r22.<init>(r23)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r22 = r22.toJson()     // Catch:{ Exception -> 0x00fe }
                android.os.Message r20 = r20.obtainMessage(r21, r22)     // Catch:{ Exception -> 0x00fe }
                r19.sendMessage(r20)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x061f:
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                r0 = r19
                r0.handleU3Card(r5, r7)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x062c:
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "refuse scan U3 for unavailable cloud service !!! port: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = ", work mode: "
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = r18.getMode()     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.w(r19, r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r19 = r19.handler     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r20 = r20.handler     // Catch:{ Exception -> 0x00fe }
                r21 = 24581(0x6005, float:3.4445E-41)
                com.xcharge.charger.data.bean.ErrorCode r22 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x00fe }
                r23 = 40025(0x9c59, float:5.6087E-41)
                r22.<init>(r23)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r22 = r22.toJson()     // Catch:{ Exception -> 0x00fe }
                android.os.Message r20 = r20.obtainMessage(r21, r22)     // Catch:{ Exception -> 0x00fe }
                r19.sendMessage(r20)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x0683:
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                r0 = r19
                r0.handleCTDemoCard(r5, r7)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x0690:
                com.xcharge.charger.data.provider.ChargeStatusCacheProvider r19 = com.xcharge.charger.data.provider.ChargeStatusCacheProvider.getInstance()     // Catch:{ Exception -> 0x00fe }
                boolean r19 = r19.isCloudConnected()     // Catch:{ Exception -> 0x00fe }
                if (r19 == 0) goto L_0x06a7
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                r0 = r19
                r0.handleAnyo1Card(r5, r7)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x06a7:
                com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER r19 = com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER.anyo_svw     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.data.provider.SystemSettingCacheProvider r20 = com.xcharge.charger.data.provider.SystemSettingCacheProvider.getInstance()     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER r20 = r20.getPlatformCustomer()     // Catch:{ Exception -> 0x00fe }
                boolean r19 = r19.equals(r20)     // Catch:{ Exception -> 0x00fe }
                if (r19 == 0) goto L_0x06c4
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                r0 = r19
                r0.handleAnyoSvwCard(r5, r7)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x06c4:
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "refuse scan anyo online card for unavailable cloud service !!! port: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.w(r19, r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r19 = r19.handler     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r20 = r20.handler     // Catch:{ Exception -> 0x00fe }
                r21 = 24581(0x6005, float:3.4445E-41)
                com.xcharge.charger.data.bean.ErrorCode r22 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x00fe }
                r23 = 40025(0x9c59, float:5.6087E-41)
                r22.<init>(r23)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r22 = r22.toJson()     // Catch:{ Exception -> 0x00fe }
                android.os.Message r20 = r20.obtainMessage(r21, r22)     // Catch:{ Exception -> 0x00fe }
                r19.sendMessage(r20)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x070d:
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler r19 = r19.chargeHandler     // Catch:{ Exception -> 0x00fe }
                boolean r19 = r19.isCharging()     // Catch:{ Exception -> 0x00fe }
                if (r19 != 0) goto L_0x072a
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                r0 = r19
                r0.handleCDDZMCard(r4)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x072a:
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "NFC charging ..., can not set now !!! port: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.w(r19, r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r19 = r19.handler     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r20 = r20.handler     // Catch:{ Exception -> 0x00fe }
                r21 = 24581(0x6005, float:3.4445E-41)
                com.xcharge.charger.data.bean.ErrorCode r22 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x00fe }
                r23 = 40007(0x9c47, float:5.6062E-41)
                r22.<init>(r23)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r22 = r22.toJson()     // Catch:{ Exception -> 0x00fe }
                android.os.Message r20 = r20.obtainMessage(r21, r22)     // Catch:{ Exception -> 0x00fe }
                r19.sendMessage(r20)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x0773:
                com.xcharge.charger.data.provider.ChargeStatusCacheProvider r19 = com.xcharge.charger.data.provider.ChargeStatusCacheProvider.getInstance()     // Catch:{ Exception -> 0x00fe }
                boolean r19 = r19.isCloudConnected()     // Catch:{ Exception -> 0x00fe }
                if (r19 == 0) goto L_0x078a
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                r0 = r19
                r0.handleCDDZJianQuanCard(r4)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x078a:
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "refuse scan cddz jianquan card for unavailable cloud service !!! port: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.w(r19, r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r19 = r19.handler     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r20 = r20.handler     // Catch:{ Exception -> 0x00fe }
                r21 = 24581(0x6005, float:3.4445E-41)
                com.xcharge.charger.data.bean.ErrorCode r22 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x00fe }
                r23 = 40025(0x9c59, float:5.6087E-41)
                r22.<init>(r23)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r22 = r22.toJson()     // Catch:{ Exception -> 0x00fe }
                android.os.Message r20 = r20.obtainMessage(r21, r22)     // Catch:{ Exception -> 0x00fe }
                r19.sendMessage(r20)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x07d3:
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                r0 = r19
                r0.handleOCPPCard(r5, r7)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x07e0:
                r0 = r25
                java.lang.Object r0 = r0.obj     // Catch:{ Exception -> 0x00fe }
                r17 = r0
                java.lang.String r17 = (java.lang.String) r17     // Catch:{ Exception -> 0x00fe }
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "NFC card left !!! port: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = ", uuid: "
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r20
                r1 = r17
                java.lang.StringBuilder r20 = r0.append(r1)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.i(r19, r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r20 = 0
                r19.setArrived(r20)     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.device.c2.service.C2DeviceEventDispatcher r19 = com.xcharge.charger.device.p005c2.service.C2DeviceEventDispatcher.getInstance()     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                java.lang.String r20 = r20.port     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                com.xcharge.charger.data.bean.device.NFC r21 = r21.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r19.handleNFCStatus(r20, r21)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x0840:
                android.os.Bundle r16 = r25.getData()     // Catch:{ Exception -> 0x00fe }
                java.lang.String r19 = "key"
                r0 = r16
                r1 = r19
                byte[] r11 = r0.getByteArray(r1)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r19 = "sign"
                r0 = r16
                r1 = r19
                java.lang.String r13 = r0.getString(r1)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r19 = "cardNo"
                r0 = r16
                r1 = r19
                java.lang.String r3 = r0.getString(r1)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "bind U1 !!!  port: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = ", data: "
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = r16.toString()     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.i(r19, r20)     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.device.c2.bean.NFCSign r19 = new com.xcharge.charger.device.c2.bean.NFCSign     // Catch:{ Exception -> 0x00fe }
                r19.<init>()     // Catch:{ Exception -> 0x00fe }
                r0 = r19
                java.lang.Object r12 = r0.fromJson(r13)     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.device.c2.bean.NFCSign r12 = (com.xcharge.charger.device.p005c2.bean.NFCSign) r12     // Catch:{ Exception -> 0x00fe }
                boolean r19 = com.xcharge.charger.device.p005c2.nfc.NFCUtils.setUserCardSign(r11, r12)     // Catch:{ Exception -> 0x00fe }
                if (r19 == 0) goto L_0x08cc
                java.lang.StringBuilder r19 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = "U1 card: "
                r19.<init>(r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r19
                java.lang.StringBuilder r19 = r0.append(r3)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = ", binded to this pile !!!"
                java.lang.StringBuilder r19 = r19.append(r20)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r19 = r19.toString()     // Catch:{ Exception -> 0x00fe }
                com.xcharge.common.utils.LogUtils.syslog(r19)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r19 = r19.handler     // Catch:{ Exception -> 0x00fe }
                r20 = 24580(0x6004, float:3.4444E-41)
                r19.sendEmptyMessage(r20)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x08cc:
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r19 = r19.handler     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                com.xcharge.charger.device.c2.nfc.C2NFCAgent$MsgHandler r20 = r20.handler     // Catch:{ Exception -> 0x00fe }
                r21 = 24581(0x6005, float:3.4445E-41)
                com.xcharge.charger.data.bean.ErrorCode r22 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x00fe }
                r23 = 40006(0x9c46, float:5.606E-41)
                r22.<init>(r23)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r22 = r22.toJson()     // Catch:{ Exception -> 0x00fe }
                android.os.Message r20 = r20.obtainMessage(r21, r22)     // Catch:{ Exception -> 0x00fe }
                r19.sendMessage(r20)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x08f7:
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "timeout in U1 bind mode, port: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.i(r19, r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                r20 = 0
                r19.isU1BindMode = r20     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x0922:
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "User cancel U1 bind mode, port: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.i(r19, r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.common.utils.HandlerTimer r19 = r19.handlerTimer     // Catch:{ Exception -> 0x00fe }
                r20 = 24592(0x6010, float:3.4461E-41)
                r19.stopTimer(r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                r20 = 0
                r19.isU1BindMode = r20     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x095c:
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "NFC swipe card OK, port: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.i(r19, r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r20 = 0
                r19.setHandleStatus(r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.data.bean.ErrorCode r20 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x00fe }
                r21 = 200(0xc8, float:2.8E-43)
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r19.setLatestError(r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                long r20 = java.lang.System.currentTimeMillis()     // Catch:{ Exception -> 0x00fe }
                r19.setTs(r20)     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.device.c2.service.C2DeviceEventDispatcher r19 = com.xcharge.charger.device.p005c2.service.C2DeviceEventDispatcher.getInstance()     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                java.lang.String r20 = r20.port     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                com.xcharge.charger.data.bean.device.NFC r21 = r21.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r19.handleNFCStatus(r20, r21)     // Catch:{ Exception -> 0x00fe }
                boolean r19 = r15.isPermitTest()     // Catch:{ Exception -> 0x00fe }
                if (r19 == 0) goto L_0x0a17
                com.xcharge.charger.device.c2.service.C2DeviceProxy r19 = com.xcharge.charger.device.p005c2.service.C2DeviceProxy.getInstance()     // Catch:{ Exception -> 0x00fe }
                r20 = 2
                r19.beep(r20)     // Catch:{ Exception -> 0x00fe }
            L_0x09d8:
                java.lang.String r19 = "U100000000000000"
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                com.xcharge.charger.data.bean.device.NFC r20 = r20.nfcStatus     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.getLatestCardNo()     // Catch:{ Exception -> 0x00fe }
                boolean r19 = r19.equals(r20)     // Catch:{ Exception -> 0x00fe }
                if (r19 == 0) goto L_0x001b
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                java.lang.String r19 = r19.port     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r19 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.getInstance(r19)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                java.lang.String r20 = r20.port     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r20 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.getInstance(r20)     // Catch:{ Exception -> 0x00fe }
                r21 = 24578(0x6002, float:3.4441E-41)
                java.lang.String r22 = "0"
                android.os.Message r20 = r20.obtainMessage(r21, r22)     // Catch:{ Exception -> 0x00fe }
                r19.sendMessage(r20)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x0a17:
                java.lang.String r19 = "U100000000000000"
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                com.xcharge.charger.data.bean.device.NFC r20 = r20.nfcStatus     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.getLatestCardNo()     // Catch:{ Exception -> 0x00fe }
                boolean r19 = r19.equals(r20)     // Catch:{ Exception -> 0x00fe }
                if (r19 != 0) goto L_0x09d8
                com.xcharge.charger.device.c2.service.C2DeviceProxy r19 = com.xcharge.charger.device.p005c2.service.C2DeviceProxy.getInstance()     // Catch:{ Exception -> 0x00fe }
                r20 = 1
                r19.beep(r20)     // Catch:{ Exception -> 0x00fe }
                goto L_0x09d8
            L_0x0a37:
                r0 = r25
                java.lang.Object r9 = r0.obj     // Catch:{ Exception -> 0x00fe }
                java.lang.String r9 = (java.lang.String) r9     // Catch:{ Exception -> 0x00fe }
                java.lang.String r19 = "C2NFCAgent.MsgHandler"
                java.lang.StringBuilder r20 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = "NFC swipe card error, port: "
                r20.<init>(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                java.lang.String r21 = r21.port     // Catch:{ Exception -> 0x00fe }
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r21 = ", error: "
                java.lang.StringBuilder r20 = r20.append(r21)     // Catch:{ Exception -> 0x00fe }
                r0 = r20
                java.lang.StringBuilder r20 = r0.append(r9)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.toString()     // Catch:{ Exception -> 0x00fe }
                android.util.Log.i(r19, r20)     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.data.bean.ErrorCode r19 = new com.xcharge.charger.data.bean.ErrorCode     // Catch:{ Exception -> 0x00fe }
                r19.<init>()     // Catch:{ Exception -> 0x00fe }
                r0 = r19
                java.lang.Object r10 = r0.fromJson(r9)     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.data.bean.ErrorCode r10 = (com.xcharge.charger.data.bean.ErrorCode) r10     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r20 = 0
                r19.setHandleStatus(r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r0 = r19
                r0.setLatestError(r10)     // Catch:{ Exception -> 0x00fe }
                r19 = 40019(0x9c53, float:5.6079E-41)
                int r20 = r10.getCode()     // Catch:{ Exception -> 0x00fe }
                r0 = r19
                r1 = r20
                if (r0 == r1) goto L_0x0aac
                r19 = 40017(0x9c51, float:5.6076E-41)
                int r20 = r10.getCode()     // Catch:{ Exception -> 0x00fe }
                r0 = r19
                r1 = r20
                if (r0 != r1) goto L_0x0af8
            L_0x0aac:
                java.util.HashMap r2 = r10.getData()     // Catch:{ Exception -> 0x00fe }
                if (r2 == 0) goto L_0x0af8
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r20 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                java.lang.String r19 = "balance"
                r0 = r19
                java.lang.Object r19 = r2.get(r0)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r19 = (java.lang.String) r19     // Catch:{ Exception -> 0x00fe }
                int r19 = java.lang.Integer.parseInt(r19)     // Catch:{ Exception -> 0x00fe }
                java.lang.Integer r19 = java.lang.Integer.valueOf(r19)     // Catch:{ Exception -> 0x00fe }
                r0 = r20
                r1 = r19
                r0.setBalance(r1)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r20 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                java.lang.String r19 = "fee"
                r0 = r19
                java.lang.Object r19 = r2.get(r0)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r19 = (java.lang.String) r19     // Catch:{ Exception -> 0x00fe }
                int r19 = java.lang.Integer.parseInt(r19)     // Catch:{ Exception -> 0x00fe }
                java.lang.Integer r19 = java.lang.Integer.valueOf(r19)     // Catch:{ Exception -> 0x00fe }
                r0 = r20
                r1 = r19
                r0.setFee(r1)     // Catch:{ Exception -> 0x00fe }
            L_0x0af8:
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                com.xcharge.charger.data.bean.device.NFC r19 = r19.nfcStatus     // Catch:{ Exception -> 0x00fe }
                long r20 = java.lang.System.currentTimeMillis()     // Catch:{ Exception -> 0x00fe }
                r19.setTs(r20)     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.device.c2.service.C2DeviceEventDispatcher r19 = com.xcharge.charger.device.p005c2.service.C2DeviceEventDispatcher.getInstance()     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                java.lang.String r20 = r20.port     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r21 = r0
                com.xcharge.charger.data.bean.device.NFC r21 = r21.nfcStatus     // Catch:{ Exception -> 0x00fe }
                r19.handleNFCStatus(r20, r21)     // Catch:{ Exception -> 0x00fe }
                int r19 = r10.getCode()     // Catch:{ Exception -> 0x00fe }
                r20 = 40016(0x9c50, float:5.6074E-41)
                r0 = r19
                r1 = r20
                if (r0 != r1) goto L_0x0bd1
                com.xcharge.charger.device.c2.service.C2DeviceProxy r19 = com.xcharge.charger.device.p005c2.service.C2DeviceProxy.getInstance()     // Catch:{ Exception -> 0x00fe }
                r20 = 1
                r19.beep(r20)     // Catch:{ Exception -> 0x00fe }
            L_0x0b3a:
                r19 = 40014(0x9c4e, float:5.6072E-41)
                int r20 = r10.getCode()     // Catch:{ Exception -> 0x00fe }
                r0 = r19
                r1 = r20
                if (r0 == r1) goto L_0x0b7b
                r19 = 40013(0x9c4d, float:5.607E-41)
                int r20 = r10.getCode()     // Catch:{ Exception -> 0x00fe }
                r0 = r19
                r1 = r20
                if (r0 == r1) goto L_0x0b7b
                r19 = 40019(0x9c53, float:5.6079E-41)
                int r20 = r10.getCode()     // Catch:{ Exception -> 0x00fe }
                r0 = r19
                r1 = r20
                if (r0 == r1) goto L_0x0b7b
                r19 = 40017(0x9c51, float:5.6076E-41)
                int r20 = r10.getCode()     // Catch:{ Exception -> 0x00fe }
                r0 = r19
                r1 = r20
                if (r0 == r1) goto L_0x0b7b
                r19 = 40021(0x9c55, float:5.6081E-41)
                int r20 = r10.getCode()     // Catch:{ Exception -> 0x00fe }
                r0 = r19
                r1 = r20
                if (r0 != r1) goto L_0x0b92
            L_0x0b7b:
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                com.xcharge.charger.data.bean.device.NFC r20 = r20.nfcStatus     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.data.bean.device.NFC r20 = r20.clone()     // Catch:{ Exception -> 0x00fe }
                r19.needRecoveryNFCStatus = r20     // Catch:{ Exception -> 0x00fe }
            L_0x0b92:
                java.lang.String r19 = "U100000000000000"
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                com.xcharge.charger.data.bean.device.NFC r20 = r20.nfcStatus     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.getLatestCardNo()     // Catch:{ Exception -> 0x00fe }
                boolean r19 = r19.equals(r20)     // Catch:{ Exception -> 0x00fe }
                if (r19 == 0) goto L_0x001b
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r19 = r0
                java.lang.String r19 = r19.port     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r19 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.getInstance(r19)     // Catch:{ Exception -> 0x00fe }
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                java.lang.String r20 = r20.port     // Catch:{ Exception -> 0x00fe }
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r20 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.getInstance(r20)     // Catch:{ Exception -> 0x00fe }
                r21 = 24578(0x6002, float:3.4441E-41)
                java.lang.String r22 = "0"
                android.os.Message r20 = r20.obtainMessage(r21, r22)     // Catch:{ Exception -> 0x00fe }
                r19.sendMessage(r20)     // Catch:{ Exception -> 0x00fe }
                goto L_0x001b
            L_0x0bd1:
                java.lang.StringBuilder r19 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = "swipe card error: "
                r19.<init>(r20)     // Catch:{ Exception -> 0x00fe }
                r0 = r19
                java.lang.StringBuilder r19 = r0.append(r9)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r19 = r19.toString()     // Catch:{ Exception -> 0x00fe }
                com.xcharge.common.utils.LogUtils.syslog(r19)     // Catch:{ Exception -> 0x00fe }
                java.lang.String r19 = "U100000000000000"
                r0 = r24
                com.xcharge.charger.device.c2.nfc.C2NFCAgent r0 = com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.this     // Catch:{ Exception -> 0x00fe }
                r20 = r0
                com.xcharge.charger.data.bean.device.NFC r20 = r20.nfcStatus     // Catch:{ Exception -> 0x00fe }
                java.lang.String r20 = r20.getLatestCardNo()     // Catch:{ Exception -> 0x00fe }
                boolean r19 = r19.equals(r20)     // Catch:{ Exception -> 0x00fe }
                if (r19 != 0) goto L_0x0b3a
                com.xcharge.charger.device.c2.service.C2DeviceProxy r19 = com.xcharge.charger.device.p005c2.service.C2DeviceProxy.getInstance()     // Catch:{ Exception -> 0x00fe }
                r20 = 3
                r19.beep(r20)     // Catch:{ Exception -> 0x00fe }
                goto L_0x0b3a
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.device.p005c2.nfc.C2NFCAgent.MsgHandler.handleMessage(android.os.Message):void");
        }
    }

    public static C2NFCAgent getInstance(String port2) {
        if (instances == null) {
            instances = new HashMap<>();
        }
        C2NFCAgent agent = instances.get(port2);
        if (agent != null) {
            return agent;
        }
        C2NFCAgent agent2 = new C2NFCAgent();
        agent2.port = port2;
        instances.put(port2, agent2);
        return agent2;
    }

    public void init(Context context2) {
        this.context = context2;
        this.chargeHandler = new C2NFCChargeHandler();
        this.chargeHandler.init(this.context, this.port);
        this.thread = new HandlerThread("C2NFCAgent", 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(this.context);
    }

    public void destroy() {
        this.handlerTimer.destroy();
        this.handler.removeMessages(MSG_NFC_CARD_ARRIVAL);
        this.handler.removeMessages(MSG_NFC_CARD_LEFT);
        this.handler.removeMessages(MSG_NFC_CARD_BIND_U1);
        this.handler.removeMessages(MSG_NFC_SWIPE_CARD_OK);
        this.handler.removeMessages(MSG_NFC_SWIPE_CARD_ERROR);
        this.handler.removeMessages(MSG_NFC_CANCEL_U1_BIND);
        this.handler.removeMessages(MSG_TIMEOUT_NFC_U1_BIND);
        this.thread.quit();
        this.chargeHandler.destroy();
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

    public void handleEvent(NFCEventData data) {
        if (data.isPresent()) {
            this.cardUUID = data.getUuid();
            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_CARD_ARRIVAL, data.toJson()));
            return;
        }
        this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_CARD_LEFT, String.format("%d", new Object[]{Integer.valueOf(this.cardUUID)})));
    }

    /* access modifiers changed from: private */
    public void handleM1Card(String cardNo, NFCEventData data) {
        XKeyseed xKeyseed = NFCUtils.getM1PrivateAreaXKeySeed();
        if (xKeyseed == null) {
            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_UNAVAILABLE_KEYSEED).toJson()));
            return;
        }
        ManageCardData M1CardData = NFCUtils.getManageCardInfo(NFCUtils.getPrivateKey(data.getUuid(), cardNo, xKeyseed.getSeed()), NFC_CARD_TYPE.M1.getType());
        if (M1CardData == null) {
            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_INVALID_MANAGE_CARD_DATA).toJson()));
            return;
        }
        this.nfcStatus.setLatestOprType(NFC_OPR_TYPE.set);
        this.nfcStatus.setHandleStatus(true);
        C2DeviceEventDispatcher.getInstance().handleNFCStatus(this.port, this.nfcStatus);
        NFCKeyContentProxy.getInstance().clearAllKeyseed();
        if (M1CardData.isClean()) {
            M1Clear();
            LogUtils.syslog("M1 card: " + cardNo + ", local setting clean !!!");
            this.handler.sendEmptyMessage(MSG_NFC_SWIPE_CARD_OK);
        } else if (!set(M1CardData, NFC_CARD_TYPE.M1, cardNo)) {
            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_SET_FAIL).toJson()));
        } else {
            this.handler.sendEmptyMessage(MSG_NFC_SWIPE_CARD_OK);
        }
    }

    private boolean M1Clear() {
        LocalSettingCacheProvider.getInstance().getChargeSetting().setWorkMode(WORK_MODE.Public);
        LocalSettingCacheProvider.getInstance().updateFeeRateSetting(new FeeRateSetting());
        LocalSettingCacheProvider.getInstance().updateConsoleSetting(new ConsoleSetting());
        LocalSettingCacheProvider.getInstance().persist();
        return true;
    }

    /* access modifiers changed from: private */
    public void handleM2Card(String cardNo, NFCEventData data) {
        String groupId = NFCUtils.getGroupID(cardNo);
        XKeyseed xKeyseed = NFCKeyContentProxy.getInstance().getKeyseed(groupId, NFC_CARD_TYPE.M2.getType());
        if (xKeyseed == null) {
            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_UNAVAILABLE_KEYSEED).toJson()));
            return;
        }
        ManageCardData M2CardData = NFCUtils.getManageCardInfo(NFCUtils.getPrivateKey(data.getUuid(), cardNo, xKeyseed.getSeed()), NFC_CARD_TYPE.M2.getType());
        if (M2CardData == null) {
            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_INVALID_MANAGE_CARD_DATA).toJson()));
            return;
        }
        this.nfcStatus.setLatestOprType(NFC_OPR_TYPE.set);
        this.nfcStatus.setHandleStatus(true);
        C2DeviceEventDispatcher.getInstance().handleNFCStatus(this.port, this.nfcStatus);
        if (M2CardData.isClean()) {
            M2Clear(groupId);
            LogUtils.syslog("M2 card: " + cardNo + ", local group " + groupId + " setting clean !!!");
            this.handler.sendEmptyMessage(MSG_NFC_SWIPE_CARD_OK);
        } else if (!set(M2CardData, NFC_CARD_TYPE.M2, cardNo)) {
            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_SET_FAIL).toJson()));
        } else {
            if (WORK_MODE.personal.equals(LocalSettingCacheProvider.getInstance().getChargeSetting().getWorkMode())) {
                this.handlerTimer.stopTimer(MSG_TIMEOUT_NFC_U1_BIND);
                this.isU1BindMode = true;
                this.handlerTimer.startTimer(60000, MSG_TIMEOUT_NFC_U1_BIND, (Object) null);
            }
            this.handler.sendEmptyMessage(MSG_NFC_SWIPE_CARD_OK);
        }
    }

    private boolean M2Clear(String groupId) {
        NFCKeyContentProxy.getInstance().clearKeyseed(groupId, (String) null);
        LocalSettingCacheProvider.getInstance().updateConsoleSetting(new ConsoleSetting());
        LocalSettingCacheProvider.getInstance().persist();
        return true;
    }

    private boolean set(ManageCardData setting, NFC_CARD_TYPE type, String cardNo) {
        Set<String> portNos = HardwareStatusCacheProvider.getInstance().getPorts().keySet();
        ChargeSetting chargeSetting = LocalSettingCacheProvider.getInstance().getChargeSetting();
        if (!TextUtils.isEmpty(setting.getWorkmode())) {
            WORK_MODE workMode = WORK_MODE.valueBy(setting.getWorkmode());
            chargeSetting.setWorkMode(workMode);
            ChargeStatusCacheProvider.getInstance().updateWorkMode(workMode);
        }
        if (setting.getCpRange() != null && setting.getCpRange().intValue() >= 0 && setting.getCpRange().intValue() <= 100) {
            int cpRange = setting.getCpRange().intValue();
            chargeSetting.setCpRange(cpRange);
            C2DeviceProxy.getInstance().setCPRange(cpRange);
            ChargeStatusCacheProvider.getInstance().updateCPRange(cpRange);
        }
        if (setting.getVoltageRange() != null && setting.getVoltageRange().intValue() >= 0 && setting.getVoltageRange().intValue() <= 100) {
            int voltageRange = setting.getVoltageRange().intValue();
            chargeSetting.setVoltageRange(voltageRange);
            C2DeviceProxy.getInstance().setVoltageRange(voltageRange);
            ChargeStatusCacheProvider.getInstance().updateVoltageRange(voltageRange);
        }
        int restoreAdjustAmp = chargeSetting.getAdjustAmp();
        int adjustAmp = -1;
        if (type.equals(NFC_CARD_TYPE.M1)) {
            int m1SettedCapacityAmp = setting.getOutpower();
            if (m1SettedCapacityAmp >= 6) {
                chargeSetting.setManufactoryAmpCapacity(m1SettedCapacityAmp);
                chargeSetting.setAmpCapacity(m1SettedCapacityAmp);
                C2DeviceProxy.getInstance().setAmpCapacity(m1SettedCapacityAmp);
                HardwareStatusCacheProvider.getInstance().updateAmpCapacity((double) m1SettedCapacityAmp);
                ChargeStatusCacheProvider.getInstance().updateAmpCapacity(m1SettedCapacityAmp);
                adjustAmp = m1SettedCapacityAmp;
                chargeSetting.setAdjustAmp(adjustAmp);
            }
        }
        if (type.equals(NFC_CARD_TYPE.M2)) {
            int m2SettedCapacityAmp = setting.getOutpower();
            if (m2SettedCapacityAmp >= 6) {
                if (m2SettedCapacityAmp <= chargeSetting.getManufactoryAmpCapacity()) {
                    chargeSetting.setManufactoryAmpCapacity(m2SettedCapacityAmp);
                    chargeSetting.setAmpCapacity(m2SettedCapacityAmp);
                    C2DeviceProxy.getInstance().setAmpCapacity(m2SettedCapacityAmp);
                    HardwareStatusCacheProvider.getInstance().updateAmpCapacity((double) m2SettedCapacityAmp);
                    ChargeStatusCacheProvider.getInstance().updateAmpCapacity(m2SettedCapacityAmp);
                } else {
                    chargeSetting.setManufactoryAmpCapacity(m2SettedCapacityAmp);
                    chargeSetting.setAmpCapacity(m2SettedCapacityAmp);
                    C2DeviceProxy.getInstance().setAmpCapacity(m2SettedCapacityAmp);
                    HardwareStatusCacheProvider.getInstance().updateAmpCapacity((double) m2SettedCapacityAmp);
                    ChargeStatusCacheProvider.getInstance().updateAmpCapacity(m2SettedCapacityAmp);
                }
            }
            adjustAmp = m2SettedCapacityAmp;
            chargeSetting.setAdjustAmp(adjustAmp);
        }
        HashMap<String, PortSetting> ports = chargeSetting.getPortsSetting();
        for (Map.Entry<String, PortSetting> entry : ports.entrySet()) {
            String portNo = entry.getKey();
            PortSetting port2 = entry.getValue();
            int gunLockMode = setting.getElecLockMode();
            if (gunLockMode != -1) {
                GunLockSetting gunLockSetting = port2.getGunLockSetting();
                gunLockSetting.setMode(GUN_LOCK_MODE.valueBy(gunLockMode));
                port2.setGunLockSetting(gunLockSetting);
            }
            String radarMode = setting.getRadermode();
            if (!TextUtils.isEmpty(radarMode)) {
                RadarSetting radarSetting = port2.getRadarSetting();
                if (radarMode.equals("enable")) {
                    radarSetting.setEnable(true);
                } else if (radarMode.equals("disable")) {
                    radarSetting.setEnable(false);
                }
                port2.setRadarSetting(radarSetting);
                HardwareStatusCacheProvider.getInstance().updatePortRadarSwitch(portNo, radarSetting.isEnable());
            }
            double capacityAmp = HardwareStatusCacheProvider.getInstance().getAmpCapacity();
            if (adjustAmp >= 6 && ((double) adjustAmp) <= capacityAmp) {
                int portAdjustAmp = (adjustAmp * port2.getAmpPercent().intValue()) / 10000;
                if (portAdjustAmp >= 6) {
                    C2DeviceProxy.getInstance().ajustChargeAmp(portNo, portAdjustAmp);
                } else {
                    Log.w("C2NFCAgent.set", "port adjust amp must be more than 6 !!! but local set value is " + portAdjustAmp + ", port: " + portNo);
                    chargeSetting.setAdjustAmp(restoreAdjustAmp);
                }
            }
        }
        chargeSetting.setPortsSetting(ports);
        ChargeStatusCacheProvider.getInstance().updateAdjustAmp(chargeSetting.getAdjustAmp());
        if (RemoteSettingCacheProvider.getInstance().hasRemoteSetting()) {
            ChargeSetting remoteChargeSetting = RemoteSettingCacheProvider.getInstance().getChargeSetting();
            boolean needPersist = false;
            if (remoteChargeSetting.getManufactoryAmpCapacity() != chargeSetting.getManufactoryAmpCapacity()) {
                remoteChargeSetting.setManufactoryAmpCapacity(chargeSetting.getManufactoryAmpCapacity());
                needPersist = true;
            }
            if (remoteChargeSetting.getAmpCapacity() != chargeSetting.getAmpCapacity()) {
                remoteChargeSetting.setAmpCapacity(chargeSetting.getAmpCapacity());
                needPersist = true;
            }
            if (remoteChargeSetting.getAdjustAmp() != chargeSetting.getAdjustAmp()) {
                remoteChargeSetting.setAdjustAmp(chargeSetting.getAdjustAmp());
                needPersist = true;
            }
            if (remoteChargeSetting.getCpRange() != chargeSetting.getCpRange()) {
                remoteChargeSetting.setCpRange(chargeSetting.getCpRange());
                needPersist = true;
            }
            if (remoteChargeSetting.getVoltageRange() != chargeSetting.getVoltageRange()) {
                remoteChargeSetting.setVoltageRange(chargeSetting.getVoltageRange());
                needPersist = true;
            }
            if (needPersist) {
                RemoteSettingCacheProvider.getInstance().updateChargeSetting(remoteChargeSetting);
                RemoteSettingCacheProvider.getInstance().persist();
            }
        }
        LocalSettingCacheProvider.getInstance().updateChargeSetting(chargeSetting);
        WORK_MODE workMode2 = chargeSetting.getWorkMode();
        UserDefineUISetting userDefineUISetting = LocalSettingCacheProvider.getInstance().getUserDefineUISetting();
        ConsoleSetting consoleSetting = LocalSettingCacheProvider.getInstance().getConsoleSetting();
        String welcome = setting.getWelcome();
        if (!TextUtils.isEmpty(welcome)) {
            if (userDefineUISetting == null) {
                userDefineUISetting = new UserDefineUISetting();
            }
            String[] withConsole = welcome.split("\\|");
            userDefineUISetting.setWelcome(withConsole[0]);
            LocalSettingCacheProvider.getInstance().updateUserDefineUISetting(userDefineUISetting);
            if (withConsole.length == 2 && !workMode2.equals(WORK_MODE.Public)) {
                String consoleInfo = withConsole[1];
                String server = consoleInfo;
                String port3 = "80";
                String[] serverAndPort = consoleInfo.split(":");
                if (serverAndPort.length == 2) {
                    server = serverAndPort[0];
                    port3 = serverAndPort[1];
                }
                try {
                    int prt = Integer.parseInt(port3);
                    if (consoleSetting == null) {
                        consoleSetting = new ConsoleSetting();
                    }
                    consoleSetting.setIp(server);
                    consoleSetting.setPort(prt);
                    LocalSettingCacheProvider.getInstance().updateConsoleSetting(consoleSetting);
                } catch (Exception e) {
                    Log.e("C2NFCAgent.set", Log.getStackTraceString(e));
                }
            }
        }
        if (!workMode2.equals(WORK_MODE.personal)) {
            FeeRateSetting feeRateSetting = LocalSettingCacheProvider.getInstance().getFeeRateSetting();
            FeeRate feeRate = DCAPProxy.getInstance().formatFeeRate(setting.getTimedPrice());
            if (feeRate != null) {
                if (feeRateSetting == null) {
                    feeRateSetting = new FeeRateSetting();
                }
                HashMap<String, PortFeeRate> portsFeeRate = feeRateSetting.getPortsFeeRate();
                if (portsFeeRate == null) {
                    portsFeeRate = new HashMap<>();
                    for (String portNo2 : portNos) {
                        portsFeeRate.put(portNo2, new PortFeeRate());
                    }
                }
                for (Map.Entry<String, PortFeeRate> entry2 : portsFeeRate.entrySet()) {
                    String key = entry2.getKey();
                    PortFeeRate portFeeRate = entry2.getValue();
                    HashMap<String, FeeRate> feeRates = portFeeRate.getFeeRates();
                    if (feeRates == null) {
                        feeRates = new HashMap<>();
                    }
                    String feeRateId = cardNo;
                    feeRate.setFeeRateId(feeRateId);
                    feeRates.put(feeRateId, feeRate);
                    portFeeRate.setFeeRates(feeRates);
                    portFeeRate.setActiveFeeRateId(feeRateId);
                }
                feeRateSetting.setPortsFeeRate(portsFeeRate);
                LocalSettingCacheProvider.getInstance().updateFeeRateSetting(feeRateSetting);
            }
        }
        String groupId = setting.getGroup();
        String keyseed = setting.getKeyseed();
        if (type.equals(NFC_CARD_TYPE.M1)) {
            if (!TextUtils.isEmpty(groupId) && !TextUtils.isEmpty(keyseed)) {
                NFCKeyContentProxy.getInstance().saveKeyseed(groupId, keyseed, NFC_CARD_TYPE.M2.getType());
            }
        } else if (type.equals(NFC_CARD_TYPE.M2)) {
            String groupId2 = NFCUtils.getGroupID(cardNo);
            if (!TextUtils.isEmpty(groupId2) && !TextUtils.isEmpty(keyseed)) {
                NFCKeyContentProxy.getInstance().saveKeyseed(groupId2, String.valueOf(keyseed) + UUID.randomUUID().toString().toUpperCase(), NFC_CARD_TYPE.U1.getType());
                NFCKeyContentProxy.getInstance().saveKeyseed(groupId2, keyseed, NFC_CARD_TYPE.U2.getType());
            }
        }
        Log.i("C2NFCAgent.set", "setted: " + LocalSettingCacheProvider.getInstance().getLocalSetting().toJson());
        LocalSettingCacheProvider.getInstance().persist();
        LogUtils.syslog(String.valueOf(type.getType()) + " card: " + cardNo + ", setted data: " + setting.toJson());
        return true;
    }

    /* access modifiers changed from: private */
    public void handleU1Card(String cardNo, NFCEventData data) {
        if (!"U100000000000000".equals(cardNo)) {
            XKeyseed xKeyseed = NFCKeyContentProxy.getInstance().getKeyseed(NFCUtils.getGroupID(cardNo), NFC_CARD_TYPE.U1.getType());
            if (xKeyseed == null) {
                this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_UNAVAILABLE_KEYSEED).toJson()));
                return;
            }
            byte[] key = NFCUtils.getPrivateKey(data.getUuid(), cardNo, xKeyseed.getSeed().substring(0, xKeyseed.getSeed().length() - 36));
            String sn = HardwareStatusCacheProvider.getInstance().getSn();
            if (!this.isU1BindMode) {
                XSign sign = NFCUtils.getUserCardSign(key);
                if (sign == null) {
                    Log.w("C2NFCAgent.handleU1Card", "failed to read card sign !!! port: " + this.port + ", card no: " + cardNo);
                    this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_READ_FAILED).toJson()));
                    return;
                } else if ((!"C2011601CNQITYNN".equals(sn) || (!"U100000000000613".equals(cardNo) && !"U100000000000614".equals(cardNo))) && ((!"C2011601CNETZQNI".equals(sn) || (!"U101012610000007".equals(cardNo) && !"U101012610000008".equals(cardNo))) && !NFCUtils.checkU1Sign(sn.getBytes(), xKeyseed.getSeed().getBytes(), sign))) {
                    this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_SIGN_ERROR).toJson()));
                    return;
                }
            } else if (!SystemSettingCacheProvider.getInstance().getPortSwipeCardPermission(data.getPort()).isPermitBinding()) {
                Log.w("C2NFCAgent.handleU1Card", "bind U1 card is forbiden!!! port: " + data.getPort());
                this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_NOT_PERMIT_SWIPE).toJson()));
                return;
            } else {
                this.nfcStatus.setLatestOprType(NFC_OPR_TYPE.bind);
                this.nfcStatus.setHandleStatus(true);
                C2DeviceEventDispatcher.getInstance().handleNFCStatus(this.port, this.nfcStatus);
                NFCSign sign2 = NFCUtils.signU1(sn.getBytes(), xKeyseed.getSeed().getBytes());
                Bundle u1 = new Bundle();
                u1.putString("cardNo", cardNo);
                u1.putByteArray("key", key);
                u1.putString("sign", sign2.toJson());
                Message msg = this.handler.obtainMessage(MSG_NFC_CARD_BIND_U1);
                msg.setData(u1);
                this.handler.sendMessage(msg);
                return;
            }
        }
        this.nfcStatus.setLatestOprType(NFC_OPR_TYPE.charge);
        this.nfcStatus.setHandleStatus(true);
        C2DeviceEventDispatcher.getInstance().handleNFCStatus(this.port, this.nfcStatus);
        Bundle u1ChargeData = new Bundle();
        u1ChargeData.putString(ContentDB.NFCConsumeFailCacheTable.UUID, String.valueOf(data.getUuid()));
        u1ChargeData.putString("cardno", cardNo);
        Message u1ChargeMsg = this.chargeHandler.obtainMessage(C2NFCChargeHandler.MSG_U1_CHARGE);
        u1ChargeMsg.setData(u1ChargeData);
        this.chargeHandler.sendMessage(u1ChargeMsg);
    }

    /* access modifiers changed from: private */
    public void handleU2Card(String cardNo, NFCEventData data) {
        XKeyseed xKeyseed = NFCKeyContentProxy.getInstance().getKeyseed(NFCUtils.getGroupID(cardNo), NFC_CARD_TYPE.U2.getType());
        if (xKeyseed == null) {
            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_UNAVAILABLE_KEYSEED).toJson()));
            return;
        }
        byte[] key = NFCUtils.getPrivateKey(data.getUuid(), cardNo, xKeyseed.getSeed());
        int balance = NFCUtils.getU2CardBalance(key);
        if (balance < 0) {
            Log.w("C2NFCAgent.handleU2Card", "failed to read card balance !!! port: " + this.port + ", card no: " + cardNo + ", balance: " + balance);
            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_READ_FAILED).toJson()));
            return;
        }
        String uuid = String.valueOf(data.getUuid());
        XSign sign = NFCUtils.getUserCardSign(key);
        if (sign == null) {
            Log.w("C2NFCAgent.handleU2Card", "failed to read card sign !!! port: " + this.port + ", card no: " + cardNo + ", balance: " + balance);
            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_READ_FAILED).toJson()));
            return;
        }
        if (!NFCUtils.checkU2Sign(NFCUtils.intToBytes(balance), key, sign, HardwareStatusCacheProvider.getInstance().getSn().getBytes()) && !NFCUtils.checkU2Sign(NFCUtils.intToBytes(balance), key, sign, (byte[]) null)) {
            ConsumeFailCache failedCache = NFCConsumeFailCacheContentProxy.getInstance().getConsumeFailCache(NFC_CARD_TYPE.U2, uuid, cardNo);
            if (failedCache == null) {
                Log.w("C2NFCAgent.handleU2Card", "illegal sigin !!! port: " + this.port + ", uuid: " + uuid + ", card no: " + cardNo + ", balance: " + balance);
                if (this.needRecoveryNFCStatus != null) {
                    if (cardNo.equals(this.needRecoveryNFCStatus.getLatestCardNo())) {
                        if (System.currentTimeMillis() - this.needRecoveryNFCStatus.getTs() <= 120000) {
                            int errorCode = this.needRecoveryNFCStatus.getLatestError().getCode();
                            if (40019 == errorCode || 40017 == errorCode) {
                                if (balance != this.needRecoveryNFCStatus.getBalance().intValue()) {
                                    Log.w("C2NFCAgent.handleU2Card", "illegal sigin, maybe balance write error !!! port: " + this.port + ", uuid: " + uuid + ", card no: " + cardNo + ", balance: " + balance + ", correct balance: " + this.needRecoveryNFCStatus.getBalance() + ", fee: " + this.needRecoveryNFCStatus.getFee());
                                    this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_SIGN_ERROR).toJson()));
                                    this.needRecoveryNFCStatus = null;
                                    return;
                                } else if (!NFCUtils.setUserCardSign(key, NFCUtils.signU2(NFCUtils.intToBytes(balance), key, 0, (byte[]) null))) {
                                    Log.w("C2NFCAgent.handleU2Card", "failed to recovery balance sign error !!! port: " + this.port + ", uuid: " + uuid + ", card no: " + cardNo + ", balance: " + balance);
                                    this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_RECOVERY_SIGN_FAIL).toJson()));
                                    this.needRecoveryNFCStatus = null;
                                    return;
                                }
                            } else if (!NFCUtils.setUserCardSign(key, NFCUtils.signU2(NFCUtils.intToBytes(balance), key, 0, (byte[]) null))) {
                                Log.w("C2NFCAgent.handleU2Card", "failed to recovery balance sign error !!! port: " + this.port + ", uuid: " + uuid + ", card no: " + cardNo + ", balance: " + balance);
                                this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_RECOVERY_SIGN_FAIL).toJson()));
                                this.needRecoveryNFCStatus = null;
                                return;
                            }
                            Log.i("C2NFCAgent.handleU2Card", "succeed to recovery illegal sigin !!! port: " + this.port + ", uuid: " + uuid + ", card no: " + cardNo + ", balance: " + balance);
                            this.needRecoveryNFCStatus = null;
                        } else {
                            Log.w("C2NFCAgent.handleU2Card", "illegal sigin, timeout for error recovery !!! port: " + this.port + ", uuid: " + uuid + ", card no: " + cardNo + ", balance: " + balance + ", error: " + this.needRecoveryNFCStatus.toJson());
                            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_SIGN_ERROR).toJson()));
                            this.needRecoveryNFCStatus = null;
                            return;
                        }
                    }
                }
                Log.w("C2NFCAgent.handleU2Card", "illegal sigin, maybe reserved by other pile !!! port: " + this.port + ", uuid: " + uuid + ", card no: " + cardNo + ", balance: " + balance);
                this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_CARD_RESERVED).toJson()));
                return;
            } else if (failedCache.getBalance() != balance) {
                Log.w("C2NFCAgent.handleU2Card", "balance maybe been rewrited !!! port: " + this.port + ", uuid: " + uuid + ", card no: " + cardNo + ", card balance: " + balance + ", cached balance: " + failedCache.getBalance());
                this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_REWRITED_BALANCE).toJson()));
                return;
            } else if (!NFCUtils.setUserCardSign(key, NFCUtils.signU2(NFCUtils.intToBytes(failedCache.getBalance()), key, failedCache.getCount(), (byte[]) null))) {
                Log.w("C2NFCAgent.handleU2Card", "failed to recovery balance sign !!! port: " + this.port + ", uuid: " + uuid + ", card no: " + cardNo + ", balance: " + balance);
                this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_RECOVERY_SIGN_FAIL).toJson()));
                return;
            } else if (NFCConsumeFailCacheContentProxy.getInstance().removeConsumeFailCache(NFC_CARD_TYPE.U2, uuid, cardNo) < 1) {
                Log.w("C2NFCAgent.handleU2Card", "failed to remove consume fail cache: " + failedCache.toJson());
            }
        }
        NFCConsumeFailCacheContentProxy.getInstance().removeConsumeFailCache(NFC_CARD_TYPE.U2, uuid, cardNo);
        this.nfcStatus.setLatestOprType(NFC_OPR_TYPE.charge);
        this.nfcStatus.setHandleStatus(true);
        C2DeviceEventDispatcher.getInstance().handleNFCStatus(this.port, this.nfcStatus);
        Bundle u2ChargeData = new Bundle();
        u2ChargeData.putByteArray("key", key);
        u2ChargeData.putString(ContentDB.NFCConsumeFailCacheTable.UUID, uuid);
        u2ChargeData.putString("cardno", cardNo);
        u2ChargeData.putInt(ContentDB.NFCConsumeFailCacheTable.BALANCE, balance);
        u2ChargeData.putString("xsign", sign.toJson());
        Message u2ChargeMsg = this.chargeHandler.obtainMessage(C2NFCChargeHandler.MSG_U2_CHARGE);
        u2ChargeMsg.setData(u2ChargeData);
        this.chargeHandler.sendMessage(u2ChargeMsg);
    }

    /* access modifiers changed from: private */
    public void handleU3Card(String cardNo, NFCEventData data) {
        XKeyseed xKeyseed = NFCUtils.getU3PrivateAreaXKeySeed();
        if (xKeyseed == null) {
            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_UNAVAILABLE_KEYSEED).toJson()));
            return;
        }
        AuthSign sign = NFCUtils.getAuthSign(NFCUtils.getPrivateKey(data.getUuid(), cardNo, xKeyseed.getSeed()));
        if (sign == null) {
            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_READ_FAILED).toJson()));
            return;
        }
        this.nfcStatus.setLatestOprType(NFC_OPR_TYPE.charge);
        this.nfcStatus.setHandleStatus(true);
        C2DeviceEventDispatcher.getInstance().handleNFCStatus(this.port, this.nfcStatus);
        Bundle u3ChargeData = new Bundle();
        u3ChargeData.putString("sign", sign.toJson());
        u3ChargeData.putString(ContentDB.NFCConsumeFailCacheTable.UUID, String.valueOf(data.getUuid()));
        u3ChargeData.putString("cardno", cardNo);
        Message u3ChargeMsg = this.chargeHandler.obtainMessage(C2NFCChargeHandler.MSG_U3_CHARGE);
        u3ChargeMsg.setData(u3ChargeData);
        this.chargeHandler.sendMessage(u3ChargeMsg);
    }

    /* access modifiers changed from: private */
    public void handleCTDemoCard(String cardNo, NFCEventData data) {
        this.nfcStatus.setLatestOprType(NFC_OPR_TYPE.charge);
        this.nfcStatus.setHandleStatus(true);
        C2DeviceEventDispatcher.getInstance().handleNFCStatus(this.port, this.nfcStatus);
        Bundle ctDemoChargeData = new Bundle();
        ctDemoChargeData.putString(ContentDB.NFCConsumeFailCacheTable.UUID, String.valueOf(data.getUuid()));
        ctDemoChargeData.putString("cardno", cardNo);
        Message ctDemoChargeMsg = this.chargeHandler.obtainMessage(C2NFCChargeHandler.MSG_CT_DEMO_CHARGE);
        ctDemoChargeMsg.setData(ctDemoChargeData);
        this.chargeHandler.sendMessage(ctDemoChargeMsg);
    }

    /* access modifiers changed from: private */
    public void handleAnyo1Card(String cardNo, NFCEventData data) {
        this.nfcStatus.setLatestOprType(NFC_OPR_TYPE.charge);
        this.nfcStatus.setHandleStatus(true);
        C2DeviceEventDispatcher.getInstance().handleNFCStatus(this.port, this.nfcStatus);
        Bundle anyo1ChargeData = new Bundle();
        anyo1ChargeData.putString(ContentDB.NFCConsumeFailCacheTable.UUID, String.valueOf(data.getUuid()));
        anyo1ChargeData.putString("cardno", cardNo);
        Message anyo1ChargeMsg = this.chargeHandler.obtainMessage(C2NFCChargeHandler.MSG_ANYO1_CHARGE);
        anyo1ChargeMsg.setData(anyo1ChargeData);
        this.chargeHandler.sendMessage(anyo1ChargeMsg);
    }

    /* access modifiers changed from: private */
    public void handleAnyoSvwCard(String cardNo, NFCEventData data) {
        this.nfcStatus.setLatestCardType(NFC_CARD_TYPE.anyo_svw);
        this.nfcStatus.setLatestOprType(NFC_OPR_TYPE.charge);
        this.nfcStatus.setHandleStatus(true);
        C2DeviceEventDispatcher.getInstance().handleNFCStatus(this.port, this.nfcStatus);
        Bundle anyoSVWChargeData = new Bundle();
        anyoSVWChargeData.putString(ContentDB.NFCConsumeFailCacheTable.UUID, String.valueOf(data.getUuid()));
        anyoSVWChargeData.putString("cardno", cardNo);
        Message anyoSVWChargeMsg = this.chargeHandler.obtainMessage(C2NFCChargeHandler.MSG_ANYO_SVW_CHARGE);
        anyoSVWChargeMsg.setData(anyoSVWChargeData);
        this.chargeHandler.sendMessage(anyoSVWChargeMsg);
    }

    /* access modifiers changed from: private */
    public void handleCDDZMCard(NFCCardIDData id) {
        try {
            byte[] key = NFCUtils.CDDZ_MCARD_KEYA.getBytes(CharEncoding.UTF_8);
            byte[] data = chargerhdNative.chargerhdNFCRead(6, key);
            if (data == null) {
                this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_READ_FAILED).toJson()));
            } else if (1000 * (((long) EndianUtils.littleBytesToInt(data)) & XMSZHead.ID_BROADCAST) < System.currentTimeMillis()) {
                this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_READ_FAILED).toJson()));
            } else {
                byte[] data2 = chargerhdNative.chargerhdNFCRead(4, key);
                if (data2 == null) {
                    this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_READ_FAILED).toJson()));
                    return;
                }
                String keyASeed = FormatUtils.bytesToHexString(echargenet.aesdecrypt("e5hgnKxfr458Fy69".getBytes(CharEncoding.UTF_8), data2, 1));
                if (TextUtils.isEmpty(keyASeed)) {
                    this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_READ_FAILED).toJson()));
                    return;
                }
                byte[] data3 = chargerhdNative.chargerhdNFCRead(5, key);
                if (data3 == null) {
                    this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_READ_FAILED).toJson()));
                    return;
                }
                String keyBSeed = FormatUtils.bytesToHexString(echargenet.aesdecrypt("55huiKxfr458Fy60".getBytes(CharEncoding.UTF_8), data3, 1));
                if (TextUtils.isEmpty(keyBSeed)) {
                    this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_READ_FAILED).toJson()));
                    return;
                }
                byte[] data4 = chargerhdNative.chargerhdNFCRead(8, key);
                if (data4 == null) {
                    this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_READ_FAILED).toJson()));
                    return;
                }
                byte[] operatorBytes = new byte[11];
                for (int i = 0; i <= 10; i++) {
                    operatorBytes[i] = data4[10 - i];
                }
                long operatorCode = BCDUtils.bcdBytes2Long(operatorBytes);
                this.nfcStatus.setLatestOprType(NFC_OPR_TYPE.set);
                this.nfcStatus.setHandleStatus(true);
                CDDZCardKeySeeds cddzCardKeySeeds = new CDDZCardKeySeeds();
                cddzCardKeySeeds.setSeedA(keyASeed);
                cddzCardKeySeeds.setSeedB(keyBSeed);
                Log.d("C2NFCAgent.handleCDDZMCard", "key seeds: " + cddzCardKeySeeds.toJson());
                if (NFCKeyContentProxy.getInstance().saveKeyseed(String.valueOf(operatorCode), cddzCardKeySeeds.toJson(), NFC_CARD_TYPE.cddz_m.getType())) {
                    LocalSettingCacheProvider.getInstance().getChargeSetting().setOperatorId(String.valueOf(operatorCode));
                    if (LocalSettingCacheProvider.getInstance().persist()) {
                        this.handler.sendEmptyMessage(MSG_NFC_SWIPE_CARD_OK);
                        return;
                    }
                    Log.e("C2NFCAgent.handleCDDZMCard", "failed to save operator code: " + operatorCode);
                    this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_ERROR).toJson()));
                    return;
                }
                Log.e("C2NFCAgent.handleCDDZMCard", "failed to save keyseeds: " + cddzCardKeySeeds.toJson() + " for operator: " + operatorCode);
                this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_ERROR).toJson()));
            }
        } catch (Exception e) {
            Log.e("C2NFCAgent.handleCDDZMCard", "except: " + Log.getStackTraceString(e));
            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_ERROR).toJson()));
        }
    }

    /* access modifiers changed from: private */
    public void handleCDDZJianQuanCard(NFCCardIDData id) {
        this.nfcStatus.setLatestOprType(NFC_OPR_TYPE.charge);
        this.nfcStatus.setHandleStatus(true);
        C2DeviceEventDispatcher.getInstance().handleNFCStatus(this.port, this.nfcStatus);
        Bundle cddz1ChargeData = new Bundle();
        cddz1ChargeData.putString(ContentDB.NFCConsumeFailCacheTable.UUID, String.valueOf(id.getUuid()));
        cddz1ChargeData.putString("cardno", id.getCardNo());
        Message cddz1ChargeMsg = this.chargeHandler.obtainMessage(C2NFCChargeHandler.MSG_CDDZ1_CHARGE);
        cddz1ChargeMsg.setData(cddz1ChargeData);
        this.chargeHandler.sendMessage(cddz1ChargeMsg);
    }

    /* access modifiers changed from: private */
    public void handleOCPPCard(String cardNo, NFCEventData data) {
        this.nfcStatus.setLatestOprType(NFC_OPR_TYPE.charge);
        this.nfcStatus.setHandleStatus(true);
        C2DeviceEventDispatcher.getInstance().handleNFCStatus(this.port, this.nfcStatus);
        Bundle ocppChargeData = new Bundle();
        ocppChargeData.putString(ContentDB.NFCConsumeFailCacheTable.UUID, String.valueOf(data.getUuid()));
        ocppChargeData.putString("cardno", cardNo);
        Message ocppChargeMsg = this.chargeHandler.obtainMessage(C2NFCChargeHandler.MSG_OCPP_CHARGE);
        ocppChargeMsg.setData(ocppChargeData);
        this.chargeHandler.sendMessage(ocppChargeMsg);
    }

    /* access modifiers changed from: private */
    public boolean swipeCardPermissionCheck(NFC_CARD_TYPE cardType) {
        SwipeCardPermission swipeCardPermission = SystemSettingCacheProvider.getInstance().getPortSwipeCardPermission(this.port);
        if (NFC_CARD_TYPE.M1.equals(cardType) || NFC_CARD_TYPE.M2.equals(cardType)) {
            if (swipeCardPermission.isPermitSetting()) {
                return true;
            }
            return false;
        } else if (NFC_CARD_TYPE.U2.equals(cardType) || NFC_CARD_TYPE.U3.equals(cardType) || NFC_CARD_TYPE.anyo1.equals(cardType) || NFC_CARD_TYPE.anyo_svw.equals(cardType) || NFC_CARD_TYPE.CT_DEMO.equals(cardType) || NFC_CARD_TYPE.ocpp.equals(cardType)) {
            if (swipeCardPermission.isPermitChargeCtrl()) {
                return true;
            }
            return false;
        } else if (!NFC_CARD_TYPE.U1.equals(cardType)) {
            return true;
        } else {
            if (swipeCardPermission.isPermitChargeCtrl() || swipeCardPermission.isPermitBinding()) {
                return true;
            }
            return false;
        }
    }
}
