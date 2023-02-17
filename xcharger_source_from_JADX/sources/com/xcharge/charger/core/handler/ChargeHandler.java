package com.xcharge.charger.core.handler;

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
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.Sequence;
import com.xcharge.charger.core.api.bean.AckDirective;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.NackDirective;
import com.xcharge.charger.core.api.bean.cap.AuthDirective;
import com.xcharge.charger.core.api.bean.cap.CAPDirectiveOption;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.core.api.bean.cap.ConditionDirective;
import com.xcharge.charger.core.api.bean.cap.EventDirective;
import com.xcharge.charger.core.api.bean.cap.FinDirective;
import com.xcharge.charger.core.api.bean.cap.InitAckDirective;
import com.xcharge.charger.core.api.bean.cap.InitDirective;
import com.xcharge.charger.core.api.bean.cap.StartDirective;
import com.xcharge.charger.core.api.bean.cap.StopDirective;
import com.xcharge.charger.core.bean.ChargeSession;
import com.xcharge.charger.core.bean.RequestSession;
import com.xcharge.charger.core.controller.ChargeController;
import com.xcharge.charger.core.controller.OSSController;
import com.xcharge.charger.core.type.CHARGE_FSM_STATUS;
import com.xcharge.charger.core.type.CHARGE_REFUSE_CAUSE;
import com.xcharge.charger.core.type.FIN_MODE;
import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.FeeRate;
import com.xcharge.charger.data.bean.PortFeeRate;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.setting.FeeRateSetting;
import com.xcharge.charger.data.bean.setting.PortSetting;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.ADVERT_POLICY;
import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.CHARGE_MODE;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_STATUS;
import com.xcharge.charger.data.bean.type.CHARGE_STOP_CAUSE;
import com.xcharge.charger.data.bean.type.CHARGE_USER_STATUS;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.bean.type.DELAY_PRICE_UNIT;
import com.xcharge.charger.data.bean.type.DEVICE_STATUS;
import com.xcharge.charger.data.bean.type.GUN_LOCK_MODE;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.bean.type.OCPP_CHARGE_STATUS;
import com.xcharge.charger.data.bean.type.SERVICE_PRICE_UNIT;
import com.xcharge.charger.data.bean.type.SWITCH_STATUS;
import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.data.proxy.PortStatusObserver;
import com.xcharge.charger.device.adpter.DeviceProxy;
import com.xcharge.charger.device.p005c2.bean.PortRuntimeData;
import com.xcharge.charger.p006ui.adapter.api.UIServiceProxy;
import com.xcharge.charger.p006ui.adapter.type.CHARGE_UI_STAGE;
import com.xcharge.charger.p006ui.adapter.type.UI_MODE;
import com.xcharge.charger.p006ui.p009c2.activity.BaseActivity;
import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.charger.utils.BillUtils;
import com.xcharge.common.utils.HandlerTimer;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.TimeUtils;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class ChargeHandler {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$core$type$CHARGE_FSM_STATUS = null;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$core$type$FIN_MODE = null;
    public static final long INTERVAL_CHECK_PLUGIN_TIMING = 5000;
    public static final long INTERVAL_CONDITION_TIMING = 1000;
    public static final long INTERVAL_DELAY_TIMING = 1000;
    public static final int MSG_BILL_PAID_EVENT = 32833;
    public static final int MSG_CHARGE_REFUSE_EVENT = 32776;
    public static final int MSG_DUMMY = 32769;
    public static final int MSG_INTERVAL_CONDITION_TIMING = 32824;
    public static final int MSG_INTERVAL_DELAY_TIMING = 32823;
    public static final int MSG_PLUGIN_CHECK_EVENT = 32834;
    public static final int MSG_PLUGIN_CHECK_TIMING = 32835;
    public static final int MSG_PORT_AUTH_INVALID = 32792;
    public static final int MSG_PORT_AUTH_VALID = 32791;
    public static final int MSG_PORT_CHARGE_FULL = 32788;
    public static final int MSG_PORT_CHARGE_RESUME = 32790;
    public static final int MSG_PORT_CHARGE_STARTED = 32786;
    public static final int MSG_PORT_CHARGE_STOPPED = 32787;
    public static final int MSG_PORT_CHARGE_SUSPEND = 32789;
    public static final int MSG_PORT_LOCK_MODE_CHANGED = 32832;
    public static final int MSG_PORT_PARK_STATUS = 32802;
    public static final int MSG_PORT_PLUGIN = 32793;
    public static final int MSG_PORT_PLUGOUT = 32800;
    public static final int MSG_PORT_RADAR_CALIBRATION = 32801;
    public static final int MSG_PORT_UPDATE = 32803;
    public static final int MSG_PORT_WARN = 32804;
    public static final int MSG_REQUEST_AUTH = 32770;
    public static final int MSG_REQUEST_CONDITION = 32774;
    public static final int MSG_REQUEST_FIN = 32772;
    public static final int MSG_REQUEST_INIT = 32771;
    public static final int MSG_REQUEST_START = 32773;
    public static final int MSG_REQUEST_STOP = 32775;
    public static final int MSG_RESPONSE_ACK = 32784;
    public static final int MSG_RESPONSE_NACK = 32785;
    public static final int MSG_SCAN_ADVERT_FIN_EVENT = 32777;
    public static final int MSG_TIMEOUT_AUTHED = 32805;
    public static final int MSG_TIMEOUT_AUTH_SENDED = 32817;
    public static final int MSG_TIMEOUT_CHARGE_FIN = 32838;
    public static final int MSG_TIMEOUT_CHARGE_STOP_DELAY = 32836;
    public static final int MSG_TIMEOUT_FIN_SENDED = 32816;
    public static final int MSG_TIMEOUT_INITED = 32807;
    public static final int MSG_TIMEOUT_INIT_ACK_SENDED = 32806;
    public static final int MSG_TIMEOUT_INIT_ADVERT = 32825;
    public static final int MSG_TIMEOUT_PLUGIN = 32818;
    public static final int MSG_TIMEOUT_PLUGOUT = 32819;
    public static final int MSG_TIMEOUT_PRESTOP_CHECK = 32837;
    public static final int MSG_TIMEOUT_PRE_STOP = 32822;
    public static final int MSG_TIMEOUT_RESERVE_CHECK = 32820;
    public static final int MSG_TIMEOUT_RESERVE_WAIT = 32821;
    public static final int MSG_TIMEOUT_STOPPED = 32809;
    public static final int MSG_TIMEOUT_STOP_SENDED = 32808;
    public static final int MSG_TIMEOUT_USER_RESERVED = 32839;
    public static final int MSG_TIMEOUT_USER_RESERVE_WAIT_PLUGIN = 32840;
    public static final long TIMEOUT_AUTHED = 10000;
    public static final long TIMEOUT_AUTH_SENDED = 10000;
    public static final long TIMEOUT_CHARGE_FIN = 4000;
    public static final long TIMEOUT_CHARGE_STOP_DELAY = 1000;
    public static final long TIMEOUT_FIN_SENDED = 10000;
    public static final long TIMEOUT_INIT_ACK_SENDED = 10000;
    public static final long TIMEOUT_PLUGOUT = 2000;
    public static final long TIMEOUT_PRESTOP_CHECK = 2000;
    public static final long TIMEOUT_PRE_STOP = 30000;
    public static final long TIMEOUT_RESERVE_CHECK = 2000;
    public static final long TIMEOUT_RESERVE_WAIT = 60000;
    public static final long TIMEOUT_STOP_SENDED = 5000;
    private double ammeter = 0.0d;
    private BillPayObserver billPayObserver = null;
    private ChargeSession chargeSession = null;
    private Context context = null;
    private MsgHandler handler = null;
    /* access modifiers changed from: private */
    public HandlerTimer handlerTimer = null;
    /* access modifiers changed from: private */
    public OCPP_CHARGE_STATUS ocppChargeStatus = null;
    /* access modifiers changed from: private */
    public String port = null;
    private PortLockModeObserver portLockModeObserver = null;
    private PortStatusObserver portStatusObserver = null;
    private double startPower = 0.0d;
    /* access modifiers changed from: private */
    public CHARGE_FSM_STATUS status = null;
    private HandlerThread thread = null;

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$core$type$CHARGE_FSM_STATUS() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$core$type$CHARGE_FSM_STATUS;
        if (iArr == null) {
            iArr = new int[CHARGE_FSM_STATUS.values().length];
            try {
                iArr[CHARGE_FSM_STATUS.auth_sended.ordinal()] = 2;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.authed.ordinal()] = 3;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.charging.ordinal()] = 11;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.fin_sended.ordinal()] = 16;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.idle.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.init_ack_sended.ordinal()] = 4;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.init_advert.ordinal()] = 5;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.inited.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.paused.ordinal()] = 13;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.plugin.ordinal()] = 9;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.pre_stop.ordinal()] = 12;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.reserve_wait.ordinal()] = 10;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.stop_sended.ordinal()] = 14;
            } catch (NoSuchFieldError e13) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.stopped.ordinal()] = 15;
            } catch (NoSuchFieldError e14) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.user_reserve_wait_plugin.ordinal()] = 7;
            } catch (NoSuchFieldError e15) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.user_reserved.ordinal()] = 6;
            } catch (NoSuchFieldError e16) {
            }
            $SWITCH_TABLE$com$xcharge$charger$core$type$CHARGE_FSM_STATUS = iArr;
        }
        return iArr;
    }

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

    private static class PortLockModeObserver extends ContentObserver {
        private Handler handler = null;

        public PortLockModeObserver(Handler handler2) {
            super(handler2);
            this.handler = handler2;
        }

        public void onChange(boolean selfChange, Uri uri) {
            Log.d("ChargeHandler.PortLockModeObserver.onChange", "selfChange: " + selfChange + ", uri: " + uri.toString());
            super.onChange(selfChange, uri);
            this.handler.obtainMessage(ChargeHandler.MSG_PORT_LOCK_MODE_CHANGED, uri).sendToTarget();
        }
    }

    private static class BillPayObserver extends ContentObserver {
        private Handler handler = null;

        public BillPayObserver(Handler handler2) {
            super(handler2);
            this.handler = handler2;
        }

        public void onChange(boolean selfChange, Uri uri) {
            Log.d("ChargeHandler.BillPayObserver.onChange", "selfChange: " + selfChange + ", uri: " + uri.toString());
            super.onChange(selfChange, uri);
            this.handler.obtainMessage(ChargeHandler.MSG_BILL_PAID_EVENT, uri).sendToTarget();
        }
    }

    private class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r23) {
            /*
                r22 = this;
                r15 = 0
                r16 = 0
                r9 = 0
                r14 = 0
                r18 = 0
                r0 = r23
                int r2 = r0.what     // Catch:{ Exception -> 0x0058 }
                switch(r2) {
                    case 32770: goto L_0x0012;
                    case 32771: goto L_0x0088;
                    case 32772: goto L_0x00cf;
                    case 32773: goto L_0x0116;
                    case 32774: goto L_0x015d;
                    case 32775: goto L_0x01a4;
                    case 32776: goto L_0x01eb;
                    case 32777: goto L_0x0232;
                    case 32784: goto L_0x0279;
                    case 32785: goto L_0x02b7;
                    case 32786: goto L_0x042b;
                    case 32787: goto L_0x04a2;
                    case 32788: goto L_0x0467;
                    case 32789: goto L_0x0527;
                    case 32790: goto L_0x0562;
                    case 32791: goto L_0x02f5;
                    case 32792: goto L_0x0330;
                    case 32793: goto L_0x036b;
                    case 32800: goto L_0x03f0;
                    case 32803: goto L_0x059d;
                    case 32805: goto L_0x0653;
                    case 32806: goto L_0x0761;
                    case 32807: goto L_0x09b4;
                    case 32808: goto L_0x090b;
                    case 32809: goto L_0x0c57;
                    case 32816: goto L_0x06e4;
                    case 32817: goto L_0x05d8;
                    case 32818: goto L_0x0a2b;
                    case 32819: goto L_0x0b8b;
                    case 32820: goto L_0x0ccd;
                    case 32821: goto L_0x0a9e;
                    case 32822: goto L_0x0b26;
                    case 32823: goto L_0x0d17;
                    case 32824: goto L_0x0d31;
                    case 32825: goto L_0x07e4;
                    case 32832: goto L_0x0d3a;
                    case 32833: goto L_0x0d7c;
                    case 32834: goto L_0x0da5;
                    case 32835: goto L_0x0d8b;
                    case 32836: goto L_0x04dd;
                    case 32837: goto L_0x03a6;
                    case 32838: goto L_0x0be7;
                    case 32839: goto L_0x08b7;
                    case 32840: goto L_0x0838;
                    case 139265: goto L_0x0dae;
                    default: goto L_0x000e;
                }
            L_0x000e:
                super.handleMessage(r23)
                return
            L_0x0012:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.core.bean.RequestSession r0 = (com.xcharge.charger.core.bean.RequestSession) r0     // Catch:{ Exception -> 0x0058 }
                r15 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", receive auth request: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r4 = r15.getRequest()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r3 = r15.getRequest()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r4 = r15.getConfirm()     // Catch:{ Exception -> 0x0058 }
                r2.handleAuthRequest(r3, r4)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0058:
                r12 = move-exception
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder
                java.lang.String r4 = "except: "
                r3.<init>(r4)
                java.lang.String r4 = android.util.Log.getStackTraceString(r12)
                java.lang.StringBuilder r3 = r3.append(r4)
                java.lang.String r3 = r3.toString()
                android.util.Log.e(r2, r3)
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                java.lang.String r3 = "ChargeHandler handleMessage exception: "
                r2.<init>(r3)
                java.lang.String r3 = android.util.Log.getStackTraceString(r12)
                java.lang.StringBuilder r2 = r2.append(r3)
                java.lang.String r2 = r2.toString()
                com.xcharge.common.utils.LogUtils.syslog(r2)
                goto L_0x000e
            L_0x0088:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.core.bean.RequestSession r0 = (com.xcharge.charger.core.bean.RequestSession) r0     // Catch:{ Exception -> 0x0058 }
                r15 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", receive init request: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r4 = r15.getRequest()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r3 = r15.getRequest()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r4 = r15.getConfirm()     // Catch:{ Exception -> 0x0058 }
                r2.handleInitRequest(r3, r4)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x00cf:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.core.bean.RequestSession r0 = (com.xcharge.charger.core.bean.RequestSession) r0     // Catch:{ Exception -> 0x0058 }
                r15 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", receive fin request: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r4 = r15.getRequest()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r3 = r15.getRequest()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r4 = r15.getConfirm()     // Catch:{ Exception -> 0x0058 }
                r2.handleFinRequest(r3, r4)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0116:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.core.bean.RequestSession r0 = (com.xcharge.charger.core.bean.RequestSession) r0     // Catch:{ Exception -> 0x0058 }
                r15 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", receive start request: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r4 = r15.getRequest()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r3 = r15.getRequest()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r4 = r15.getConfirm()     // Catch:{ Exception -> 0x0058 }
                r2.handleStartRequest(r3, r4)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x015d:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.core.bean.RequestSession r0 = (com.xcharge.charger.core.bean.RequestSession) r0     // Catch:{ Exception -> 0x0058 }
                r15 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", receive condition request: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r4 = r15.getRequest()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r3 = r15.getRequest()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r4 = r15.getConfirm()     // Catch:{ Exception -> 0x0058 }
                r2.handleConditionRequest(r3, r4)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x01a4:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.core.bean.RequestSession r0 = (com.xcharge.charger.core.bean.RequestSession) r0     // Catch:{ Exception -> 0x0058 }
                r15 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", receive stop request: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r4 = r15.getRequest()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r3 = r15.getRequest()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r4 = r15.getConfirm()     // Catch:{ Exception -> 0x0058 }
                r2.handleStopRequest(r3, r4)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x01eb:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.core.bean.RequestSession r0 = (com.xcharge.charger.core.bean.RequestSession) r0     // Catch:{ Exception -> 0x0058 }
                r15 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", receive charge refuse event: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r4 = r15.getRequest()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r3 = r15.getRequest()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r4 = r15.getConfirm()     // Catch:{ Exception -> 0x0058 }
                r2.handleChargeRefuseEvent(r3, r4)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0232:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.core.bean.RequestSession r0 = (com.xcharge.charger.core.bean.RequestSession) r0     // Catch:{ Exception -> 0x0058 }
                r15 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", receive scan advert finished event: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r4 = r15.getRequest()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r3 = r15.getRequest()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r4 = r15.getConfirm()     // Catch:{ Exception -> 0x0058 }
                r2.handleScanAdvertFinishedEvent(r3, r4)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0279:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.core.api.bean.DCAPMessage r0 = (com.xcharge.charger.core.api.bean.DCAPMessage) r0     // Catch:{ Exception -> 0x0058 }
                r16 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", receive ack response: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r16.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r0 = r16
                r2.handleAckResponse(r0)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x02b7:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.core.api.bean.DCAPMessage r0 = (com.xcharge.charger.core.api.bean.DCAPMessage) r0     // Catch:{ Exception -> 0x0058 }
                r16 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", receive nack response: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r16.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r0 = r16
                r2.handleNackResponse(r0)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x02f5:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.data.bean.status.PortStatus r0 = (com.xcharge.charger.data.bean.status.PortStatus) r0     // Catch:{ Exception -> 0x0058 }
                r14 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "receive auth valid event !!! port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", status: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r14.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handleAuthValid(r14)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0330:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.data.bean.status.PortStatus r0 = (com.xcharge.charger.data.bean.status.PortStatus) r0     // Catch:{ Exception -> 0x0058 }
                r14 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "receive auth invalid event !!! port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", status: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r14.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handleAuthInvalid(r14)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x036b:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.data.bean.status.PortStatus r0 = (com.xcharge.charger.data.bean.status.PortStatus) r0     // Catch:{ Exception -> 0x0058 }
                r14 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "receive plugin event !!! port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", status: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r14.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handlePlugin(r14)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x03a6:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.data.bean.status.PortStatus r0 = (com.xcharge.charger.data.bean.status.PortStatus) r0     // Catch:{ Exception -> 0x0058 }
                r14 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "charge prestop check timeout !!! port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", status: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r14.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r0 = r23
                int r3 = r0.what     // Catch:{ Exception -> 0x0058 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handlePrestopCheckTimeout(r14)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x03f0:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.data.bean.status.PortStatus r0 = (com.xcharge.charger.data.bean.status.PortStatus) r0     // Catch:{ Exception -> 0x0058 }
                r14 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "receive plugout event !!! port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", status: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r14.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handlePlugout(r14)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x042b:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.data.bean.status.PortStatus r0 = (com.xcharge.charger.data.bean.status.PortStatus) r0     // Catch:{ Exception -> 0x0058 }
                r14 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "receive charge start event !!! port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", status: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r14.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r3 = 1
                r2.handleChargeStarted(r3, r14)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0467:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.data.bean.status.PortStatus r0 = (com.xcharge.charger.data.bean.status.PortStatus) r0     // Catch:{ Exception -> 0x0058 }
                r14 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "receive charge full event !!! port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", status: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r14.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handleChargeFull(r14)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x04a2:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.data.bean.status.PortStatus r0 = (com.xcharge.charger.data.bean.status.PortStatus) r0     // Catch:{ Exception -> 0x0058 }
                r14 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "receive charge stop event, delay it here!!! port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", status: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r14.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handleChargeStopEvent(r14)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x04dd:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.data.bean.status.PortStatus r0 = (com.xcharge.charger.data.bean.status.PortStatus) r0     // Catch:{ Exception -> 0x0058 }
                r14 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "charge stop delay timeout, handle it now !!! port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", status: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r14.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r0 = r23
                int r3 = r0.what     // Catch:{ Exception -> 0x0058 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handleChargePreStop(r14)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0527:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.data.bean.status.PortStatus r0 = (com.xcharge.charger.data.bean.status.PortStatus) r0     // Catch:{ Exception -> 0x0058 }
                r14 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "receive charge suspend event !!! port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", status: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r14.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handleSuspend(r14)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0562:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.data.bean.status.PortStatus r0 = (com.xcharge.charger.data.bean.status.PortStatus) r0     // Catch:{ Exception -> 0x0058 }
                r14 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "receive charge resume event !!! port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", status: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r14.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handleResume(r14)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x059d:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.data.bean.status.PortStatus r0 = (com.xcharge.charger.data.bean.status.PortStatus) r0     // Catch:{ Exception -> 0x0058 }
                r14 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "receive charge update event !!! port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", status: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r14.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handleUpdate(r14)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x05d8:
                r0 = r23
                java.lang.Object r10 = r0.obj     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r10 = (com.xcharge.charger.core.api.bean.DCAPMessage) r10     // Catch:{ Exception -> 0x0058 }
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                r3.<init>()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.CHARGE_FSM_STATUS r4 = com.xcharge.charger.core.type.CHARGE_FSM_STATUS.auth_sended     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = " state timeout !!! port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.bean.ChargeSession r4 = r4.getChargeSession()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r0 = r23
                int r3 = r0.what     // Catch:{ Exception -> 0x0058 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0058 }
                r2 = 10000(0x2710, float:1.4013E-41)
                java.lang.String r3 = "auth to server timeout"
                r4 = 0
                com.xcharge.charger.core.controller.ChargeController.nackConfirm(r10, r2, r3, r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.clearChargeSession()     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.CHARGE_FSM_STATUS r3 = com.xcharge.charger.core.type.CHARGE_FSM_STATUS.idle     // Catch:{ Exception -> 0x0058 }
                r2.status = r3     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.data.bean.type.OCPP_CHARGE_STATUS r3 = com.xcharge.charger.data.bean.type.OCPP_CHARGE_STATUS.SESSION_ENDED     // Catch:{ Exception -> 0x0058 }
                r2.ocppChargeStatus = r3     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.notifyChargeEnded2OSS()     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0653:
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                r3.<init>()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.CHARGE_FSM_STATUS r4 = com.xcharge.charger.core.type.CHARGE_FSM_STATUS.authed     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = " state timeout !!! port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.bean.ChargeSession r4 = r4.getChargeSession()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r0 = r23
                int r3 = r0.what     // Catch:{ Exception -> 0x0058 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.clearChargeSession()     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.CHARGE_FSM_STATUS r3 = com.xcharge.charger.core.type.CHARGE_FSM_STATUS.idle     // Catch:{ Exception -> 0x0058 }
                r2.status = r3     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.data.bean.type.OCPP_CHARGE_STATUS r3 = com.xcharge.charger.data.bean.type.OCPP_CHARGE_STATUS.SESSION_ENDED     // Catch:{ Exception -> 0x0058 }
                r2.ocppChargeStatus = r3     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.notifyChargeEnded2OSS()     // Catch:{ Exception -> 0x0058 }
                android.os.Bundle r11 = new android.os.Bundle     // Catch:{ Exception -> 0x0058 }
                r11.<init>()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r2 = "stage"
                com.xcharge.charger.ui.adapter.type.CHARGE_UI_STAGE r3 = com.xcharge.charger.p006ui.adapter.type.CHARGE_UI_STAGE.refuse     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.getStage()     // Catch:{ Exception -> 0x0058 }
                r11.putString(r2, r3)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r2 = "cause"
                com.xcharge.charger.core.type.CHARGE_REFUSE_CAUSE r3 = com.xcharge.charger.core.type.CHARGE_REFUSE_CAUSE.AUTH_TIMEOUT     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.getCause()     // Catch:{ Exception -> 0x0058 }
                r11.putString(r2, r3)     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.ui.adapter.api.UIServiceProxy r2 = com.xcharge.charger.p006ui.adapter.api.UIServiceProxy.getInstance()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.ui.adapter.type.UI_MODE r3 = com.xcharge.charger.p006ui.adapter.type.UI_MODE.charge     // Catch:{ Exception -> 0x0058 }
                r2.sendUIModeEvent(r3, r11)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x06e4:
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                r3.<init>()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.CHARGE_FSM_STATUS r4 = com.xcharge.charger.core.type.CHARGE_FSM_STATUS.fin_sended     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = " state timeout !!! port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.bean.ChargeSession r4 = r4.getChargeSession()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r0 = r23
                int r3 = r0.what     // Catch:{ Exception -> 0x0058 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.CHARGE_FSM_STATUS r3 = com.xcharge.charger.core.type.CHARGE_FSM_STATUS.idle     // Catch:{ Exception -> 0x0058 }
                r2.status = r3     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.data.bean.type.OCPP_CHARGE_STATUS r3 = com.xcharge.charger.data.bean.type.OCPP_CHARGE_STATUS.SESSION_ENDED     // Catch:{ Exception -> 0x0058 }
                r2.ocppChargeStatus = r3     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r3 = 0
                r4 = 0
                r2.updateChargeData(r3, r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.notifyChargeSessionFinished()     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.clearChargeSession()     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.notifyChargeEnded2OSS()     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0761:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.core.bean.RequestSession r0 = (com.xcharge.charger.core.bean.RequestSession) r0     // Catch:{ Exception -> 0x0058 }
                r15 = r0
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.bean.ChargeSession r9 = r2.getChargeSession()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                r3.<init>()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.CHARGE_FSM_STATUS r4 = com.xcharge.charger.core.type.CHARGE_FSM_STATUS.init_ack_sended     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = " state timeout !!! port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r9.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r0 = r23
                int r3 = r0.what     // Catch:{ Exception -> 0x0058 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.FIN_MODE r3 = com.xcharge.charger.core.type.FIN_MODE.timeout     // Catch:{ Exception -> 0x0058 }
                r4 = 0
                com.xcharge.charger.data.proxy.ChargeBill r5 = r9.getChargeBill()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r5 = r5.getUser_type()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.data.proxy.ChargeBill r6 = r9.getChargeBill()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r6 = r6.getUser_code()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.data.proxy.ChargeBill r7 = r9.getChargeBill()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r7 = r7.getCharge_id()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r8 = r15.getRequest()     // Catch:{ Exception -> 0x0058 }
                long r20 = r8.getSeq()     // Catch:{ Exception -> 0x0058 }
                java.lang.Long r8 = java.lang.Long.valueOf(r20)     // Catch:{ Exception -> 0x0058 }
                r2.finIndicate(r3, r4, r5, r6, r7, r8)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x07e4:
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.bean.ChargeSession r9 = r2.getChargeSession()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                r3.<init>()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.CHARGE_FSM_STATUS r4 = com.xcharge.charger.core.type.CHARGE_FSM_STATUS.init_advert     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = " state timeout !!! port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r9.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r0 = r23
                int r3 = r0.what     // Catch:{ Exception -> 0x0058 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handleInitAdvertTimeout()     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0838:
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.bean.ChargeSession r9 = r2.getChargeSession()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                r3.<init>()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.CHARGE_FSM_STATUS r4 = com.xcharge.charger.core.type.CHARGE_FSM_STATUS.user_reserve_wait_plugin     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = " state timeout !!! port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r9.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r0 = r23
                int r3 = r0.what     // Catch:{ Exception -> 0x0058 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.device.adpter.DeviceProxy r2 = com.xcharge.charger.device.adpter.DeviceProxy.getInstance()     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r3 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.port     // Catch:{ Exception -> 0x0058 }
                r2.closeGunLed(r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.FIN_MODE r3 = com.xcharge.charger.core.type.FIN_MODE.plugin_timeout     // Catch:{ Exception -> 0x0058 }
                r4 = 0
                com.xcharge.charger.data.proxy.ChargeBill r5 = r9.getChargeBill()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r5 = r5.getUser_type()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.data.proxy.ChargeBill r6 = r9.getChargeBill()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r6 = r6.getUser_code()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.data.proxy.ChargeBill r7 = r9.getChargeBill()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r7 = r7.getCharge_id()     // Catch:{ Exception -> 0x0058 }
                r8 = 0
                r2.finIndicate(r3, r4, r5, r6, r7, r8)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x08b7:
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.bean.ChargeSession r9 = r2.getChargeSession()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                r3.<init>()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.CHARGE_FSM_STATUS r4 = com.xcharge.charger.core.type.CHARGE_FSM_STATUS.user_reserved     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = " state timeout !!! port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r9.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r0 = r23
                int r3 = r0.what     // Catch:{ Exception -> 0x0058 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handleUserReservedTimeout()     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x090b:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                com.xcharge.charger.core.bean.RequestSession r0 = (com.xcharge.charger.core.bean.RequestSession) r0     // Catch:{ Exception -> 0x0058 }
                r15 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                r3.<init>()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.CHARGE_FSM_STATUS r4 = com.xcharge.charger.core.type.CHARGE_FSM_STATUS.stop_sended     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = " state timeout !!! port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.bean.ChargeSession r4 = r4.getChargeSession()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r0 = r23
                int r3 = r0.what     // Catch:{ Exception -> 0x0058 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.bean.ChargeSession r2 = r2.getChargeSession()     // Catch:{ Exception -> 0x0058 }
                int r17 = r2.getStop_retry()     // Catch:{ Exception -> 0x0058 }
                r2 = 1
                r0 = r17
                if (r0 != r2) goto L_0x09a1
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "failed to retry to stop charge !!! port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.w(r2, r3)     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r2 = r15.getConfirm()     // Catch:{ Exception -> 0x0058 }
                r3 = 10000(0x2710, float:1.4013E-41)
                java.lang.String r4 = "timeout"
                r5 = 0
                com.xcharge.charger.core.controller.ChargeController.nackConfirm(r2, r3, r4, r5)     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.device.adpter.DeviceProxy r2 = com.xcharge.charger.device.adpter.DeviceProxy.getInstance()     // Catch:{ Exception -> 0x0058 }
                r3 = 3
                r2.beep(r3)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x09a1:
                if (r17 != 0) goto L_0x000e
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r3 = r15.getRequest()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.DCAPMessage r4 = r15.getConfirm()     // Catch:{ Exception -> 0x0058 }
                r2.retryStopCharge(r3, r4)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x09b4:
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.bean.ChargeSession r9 = r2.getChargeSession()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                r3.<init>()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.CHARGE_FSM_STATUS r4 = com.xcharge.charger.core.type.CHARGE_FSM_STATUS.inited     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = " state timeout !!! port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r9.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r0 = r23
                int r3 = r0.what     // Catch:{ Exception -> 0x0058 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.cancelPortChargeAuth()     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.FIN_MODE r3 = com.xcharge.charger.core.type.FIN_MODE.plugin_timeout     // Catch:{ Exception -> 0x0058 }
                r4 = 0
                com.xcharge.charger.data.proxy.ChargeBill r5 = r9.getChargeBill()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r5 = r5.getUser_type()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.data.proxy.ChargeBill r6 = r9.getChargeBill()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r6 = r6.getUser_code()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.data.proxy.ChargeBill r7 = r9.getChargeBill()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r7 = r7.getCharge_id()     // Catch:{ Exception -> 0x0058 }
                r8 = 0
                r2.finIndicate(r3, r4, r5, r6, r7, r8)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0a2b:
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                r3.<init>()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.CHARGE_FSM_STATUS r4 = com.xcharge.charger.core.type.CHARGE_FSM_STATUS.plugin     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = " state timeout !!! port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.bean.ChargeSession r4 = r4.getChargeSession()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r0 = r23
                int r3 = r0.what     // Catch:{ Exception -> 0x0058 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.startCharge()     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r3 = 32820(0x8034, float:4.599E-41)
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r4 = 2000(0x7d0, double:9.88E-321)
                r3 = 32820(0x8034, float:4.599E-41)
                r6 = 0
                r2.startTimer(r4, r3, r6)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0a9e:
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.bean.ChargeSession r9 = r2.getChargeSession()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                r3.<init>()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.CHARGE_FSM_STATUS r4 = com.xcharge.charger.core.type.CHARGE_FSM_STATUS.reserve_wait     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = " state timeout !!! port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.bean.ChargeSession r4 = r4.getChargeSession()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r0 = r23
                int r3 = r0.what     // Catch:{ Exception -> 0x0058 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.data.proxy.ChargeBill r2 = r9.getChargeBill()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.data.bean.type.CHARGE_STOP_CAUSE r3 = com.xcharge.charger.data.bean.type.CHARGE_STOP_CAUSE.car     // Catch:{ Exception -> 0x0058 }
                r2.setStop_cause(r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.FIN_MODE r3 = com.xcharge.charger.core.type.FIN_MODE.car     // Catch:{ Exception -> 0x0058 }
                r4 = 0
                com.xcharge.charger.data.proxy.ChargeBill r5 = r9.getChargeBill()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r5 = r5.getUser_type()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.data.proxy.ChargeBill r6 = r9.getChargeBill()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r6 = r6.getUser_code()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.data.proxy.ChargeBill r7 = r9.getChargeBill()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r7 = r7.getCharge_id()     // Catch:{ Exception -> 0x0058 }
                r8 = 0
                r2.finIndicate(r3, r4, r5, r6, r7, r8)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.cancelPortChargeAuth()     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0b26:
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.bean.ChargeSession r9 = r2.getChargeSession()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                r3.<init>()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.CHARGE_FSM_STATUS r4 = com.xcharge.charger.core.type.CHARGE_FSM_STATUS.pre_stop     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = " state timeout !!! port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.bean.ChargeSession r4 = r4.getChargeSession()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r0 = r23
                int r3 = r0.what     // Catch:{ Exception -> 0x0058 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.stopCharge()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.data.proxy.ChargeBill r2 = r9.getChargeBill()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.data.bean.type.CHARGE_STOP_CAUSE r3 = com.xcharge.charger.data.bean.type.CHARGE_STOP_CAUSE.car     // Catch:{ Exception -> 0x0058 }
                r2.setStop_cause(r3)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0b8b:
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "plugout timeout, consider charge ended indeed !!! charge status: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.CHARGE_FSM_STATUS r4 = r4.status     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.bean.ChargeSession r4 = r4.getChargeSession()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r0 = r23
                int r3 = r0.what     // Catch:{ Exception -> 0x0058 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handlePlugoutIndeed()     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0be7:
                r0 = r23
                java.lang.Object r13 = r0.obj     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.api.bean.cap.FinDirective r13 = (com.xcharge.charger.core.api.bean.cap.FinDirective) r13     // Catch:{ Exception -> 0x0058 }
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "charge fin timeout, also consider charge ended !!! charge status: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.CHARGE_FSM_STATUS r4 = r4.status     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.bean.ChargeSession r4 = r4.getChargeSession()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", fin directive: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r13.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r0 = r23
                int r3 = r0.what     // Catch:{ Exception -> 0x0058 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handleChargeFinIndeed(r13)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0c57:
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.bean.ChargeSession r9 = r2.getChargeSession()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                r3.<init>()     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.CHARGE_FSM_STATUS r4 = com.xcharge.charger.core.type.CHARGE_FSM_STATUS.stopped     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = " state timeout !!! port: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r9.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r0 = r23
                int r3 = r0.what     // Catch:{ Exception -> 0x0058 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0058 }
                boolean r2 = r9.isDelayWaitStarted()     // Catch:{ Exception -> 0x0058 }
                if (r2 == 0) goto L_0x0cb3
                r2 = 1
                r9.setDelayStarted(r2)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handleDelayStarted()     // Catch:{ Exception -> 0x0058 }
            L_0x0cb3:
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r4 = 1000(0x3e8, double:4.94E-321)
                r3 = 32823(0x8037, float:4.5995E-41)
                r6 = 0
                r2.startTimer(r4, r3, r6)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handleDelayTiming()     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0ccd:
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "check reserve charge timeout !!! port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", charge session: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.bean.ChargeSession r4 = r4.getChargeSession()     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.toJson()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r0 = r23
                int r3 = r0.what     // Catch:{ Exception -> 0x0058 }
                r2.stopTimer(r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handleReserveCheckTimeout()     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0d17:
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0e13 }
                r2.handleDelayTiming()     // Catch:{ Exception -> 0x0e13 }
            L_0x0d1e:
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r4 = 1000(0x3e8, double:4.94E-321)
                r3 = 32823(0x8037, float:4.5995E-41)
                r6 = 0
                r2.startTimer(r4, r3, r6)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0d31:
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handleConditionTiming()     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0d3a:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                android.net.Uri r0 = (android.net.Uri) r0     // Catch:{ Exception -> 0x0058 }
                r18 = r0
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "lock mode changed !!! port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ",  charge status: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.charger.core.type.CHARGE_FSM_STATUS r4 = r4.status     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r0 = r18
                r2.handlePortLockModeChanged(r0)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0d7c:
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r3 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                android.net.Uri r2 = (android.net.Uri) r2     // Catch:{ Exception -> 0x0058 }
                r3.handleBillPaidEvent(r2)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0d8b:
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0e11 }
                r2.checkPlugin()     // Catch:{ Exception -> 0x0e11 }
            L_0x0d92:
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0058 }
                r4 = 5000(0x1388, double:2.4703E-320)
                r3 = 32835(0x8043, float:4.6012E-41)
                r6 = 0
                r2.startTimer(r4, r3, r6)     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0da5:
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.checkPlugin()     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0dae:
                r0 = r23
                java.lang.Object r2 = r0.obj     // Catch:{ Exception -> 0x0058 }
                r0 = r2
                android.net.Uri r0 = (android.net.Uri) r0     // Catch:{ Exception -> 0x0058 }
                r18 = r0
                java.lang.String r2 = r18.getPath()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "ports/"
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "/plugin"
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                boolean r2 = r2.contains(r3)     // Catch:{ Exception -> 0x0058 }
                if (r2 == 0) goto L_0x000e
                java.lang.String r2 = "ChargeHandler.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = "port plugin status changed, port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r4 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r4.port     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = ", uri: "
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r4 = r18.toString()     // Catch:{ Exception -> 0x0058 }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0058 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0058 }
                android.util.Log.d(r2, r3)     // Catch:{ Exception -> 0x0058 }
                r0 = r22
                com.xcharge.charger.core.handler.ChargeHandler r2 = com.xcharge.charger.core.handler.ChargeHandler.this     // Catch:{ Exception -> 0x0058 }
                r2.handlePortPluginStatusChanged()     // Catch:{ Exception -> 0x0058 }
                goto L_0x000e
            L_0x0e11:
                r2 = move-exception
                goto L_0x0d92
            L_0x0e13:
                r2 = move-exception
                goto L_0x0d1e
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.core.handler.ChargeHandler.MsgHandler.handleMessage(android.os.Message):void");
        }
    }

    public void init(Context context2, String port2) {
        this.context = context2;
        this.port = port2;
        this.status = CHARGE_FSM_STATUS.idle;
        this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
        this.thread = new HandlerThread("ChargeHandler#" + this.port, 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context2);
        this.handlerTimer.startTimer(5000, MSG_PLUGIN_CHECK_TIMING, (Object) null);
        this.portLockModeObserver = new PortLockModeObserver(this.handler);
        this.context.getContentResolver().registerContentObserver(ChargeStatusCacheProvider.getInstance().getUriFor("ports/lock/mode/" + this.port), true, this.portLockModeObserver);
        this.portStatusObserver = new PortStatusObserver(this.context, this.port, this.handler);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/" + this.port), true, this.portStatusObserver);
        this.billPayObserver = new BillPayObserver(this.handler);
        this.context.getContentResolver().registerContentObserver(ChargeContentProxy.getInstance().getUriFor("pay"), true, this.billPayObserver);
    }

    public void destroy() {
        this.context.getContentResolver().unregisterContentObserver(this.portStatusObserver);
        this.context.getContentResolver().unregisterContentObserver(this.portLockModeObserver);
        this.context.getContentResolver().unregisterContentObserver(this.billPayObserver);
        this.handlerTimer.destroy();
        this.handler.removeMessages(MSG_REQUEST_AUTH);
        this.handler.removeMessages(MSG_REQUEST_INIT);
        this.handler.removeMessages(MSG_REQUEST_FIN);
        this.handler.removeMessages(MSG_REQUEST_START);
        this.handler.removeMessages(MSG_REQUEST_CONDITION);
        this.handler.removeMessages(MSG_REQUEST_STOP);
        this.handler.removeMessages(MSG_CHARGE_REFUSE_EVENT);
        this.handler.removeMessages(MSG_SCAN_ADVERT_FIN_EVENT);
        this.handler.removeMessages(MSG_RESPONSE_ACK);
        this.handler.removeMessages(MSG_RESPONSE_NACK);
        this.handler.removeMessages(MSG_PORT_CHARGE_STARTED);
        this.handler.removeMessages(MSG_PORT_CHARGE_STOPPED);
        this.handler.removeMessages(MSG_PORT_CHARGE_FULL);
        this.handler.removeMessages(MSG_PORT_CHARGE_SUSPEND);
        this.handler.removeMessages(MSG_PORT_CHARGE_RESUME);
        this.handler.removeMessages(MSG_PORT_AUTH_VALID);
        this.handler.removeMessages(MSG_PORT_AUTH_INVALID);
        this.handler.removeMessages(MSG_PORT_PLUGIN);
        this.handler.removeMessages(MSG_PORT_PLUGOUT);
        this.handler.removeMessages(MSG_PORT_RADAR_CALIBRATION);
        this.handler.removeMessages(MSG_PORT_PARK_STATUS);
        this.handler.removeMessages(MSG_PORT_UPDATE);
        this.handler.removeMessages(MSG_PORT_WARN);
        this.handler.removeMessages(MSG_TIMEOUT_AUTHED);
        this.handler.removeMessages(MSG_TIMEOUT_INIT_ACK_SENDED);
        this.handler.removeMessages(MSG_TIMEOUT_INITED);
        this.handler.removeMessages(MSG_TIMEOUT_STOP_SENDED);
        this.handler.removeMessages(MSG_TIMEOUT_STOPPED);
        this.handler.removeMessages(MSG_TIMEOUT_FIN_SENDED);
        this.handler.removeMessages(MSG_TIMEOUT_AUTH_SENDED);
        this.handler.removeMessages(MSG_TIMEOUT_PLUGIN);
        this.handler.removeMessages(MSG_TIMEOUT_PLUGOUT);
        this.handler.removeMessages(MSG_TIMEOUT_RESERVE_CHECK);
        this.handler.removeMessages(MSG_TIMEOUT_RESERVE_WAIT);
        this.handler.removeMessages(MSG_TIMEOUT_PRE_STOP);
        this.handler.removeMessages(MSG_INTERVAL_DELAY_TIMING);
        this.handler.removeMessages(MSG_INTERVAL_CONDITION_TIMING);
        this.handler.removeMessages(MSG_TIMEOUT_INIT_ADVERT);
        this.handler.removeMessages(MSG_PORT_LOCK_MODE_CHANGED);
        this.handler.removeMessages(MSG_BILL_PAID_EVENT);
        this.handler.removeMessages(MSG_PLUGIN_CHECK_EVENT);
        this.handler.removeMessages(MSG_PLUGIN_CHECK_TIMING);
        this.handler.removeMessages(MSG_TIMEOUT_CHARGE_STOP_DELAY);
        this.handler.removeMessages(MSG_TIMEOUT_PRESTOP_CHECK);
        this.handler.removeMessages(MSG_TIMEOUT_CHARGE_FIN);
        this.handler.removeMessages(MSG_TIMEOUT_USER_RESERVED);
        this.handler.removeMessages(MSG_TIMEOUT_USER_RESERVE_WAIT_PLUGIN);
        this.handler.removeMessages(PortStatusObserver.MSG_PORT_STATUS_CHANGE);
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

    public boolean sendEmptyMessage(int what) {
        return this.handler.sendEmptyMessage(what);
    }

    public boolean hasCharge(String chargeId) {
        if (TextUtils.isEmpty(chargeId)) {
            return false;
        }
        return chargeId.equals(getChargeSession().getChargeBill().getCharge_id());
    }

    public boolean isIdle(CHARGE_INIT_TYPE initType, boolean isLocal) {
        if (initType == null) {
            return CHARGE_FSM_STATUS.idle.equals(this.status);
        }
        ChargeSession chargeSession2 = getChargeSession();
        if (!initType.equals(chargeSession2.getChargeBill().getInit_type()) || CHARGE_FSM_STATUS.idle.equals(this.status)) {
            return true;
        }
        if (!CHARGE_INIT_TYPE.nfc.equals(initType)) {
            return false;
        }
        NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeSession2.getChargeBill().getUser_type());
        if (isLocal) {
            if (NFC_CARD_TYPE.U1.equals(nfcCardType) || NFC_CARD_TYPE.U2.equals(nfcCardType) || NFC_CARD_TYPE.anyo_svw.equals(nfcCardType) || NFC_CARD_TYPE.CT_DEMO.equals(nfcCardType)) {
                return false;
            }
            return true;
        } else if (NFC_CARD_TYPE.U1.equals(nfcCardType) || NFC_CARD_TYPE.U2.equals(nfcCardType) || NFC_CARD_TYPE.anyo_svw.equals(nfcCardType) || NFC_CARD_TYPE.CT_DEMO.equals(nfcCardType)) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: private */
    public ChargeSession getChargeSession() {
        if (this.chargeSession == null) {
            this.chargeSession = new ChargeSession();
        }
        if (this.chargeSession.getChargeBill() == null) {
            this.chargeSession.setChargeBill(new ChargeBill());
        }
        return this.chargeSession;
    }

    /* access modifiers changed from: private */
    public void clearChargeSession() {
        this.chargeSession = null;
    }

    /* access modifiers changed from: private */
    public void handlePortLockModeChanged(Uri uri) {
    }

    private void setPortLockMode(String userType, CHARGE_INIT_TYPE initType) {
        GUN_LOCK_MODE mode;
        boolean usingRemote = false;
        boolean usingDefault = false;
        if (CHARGE_INIT_TYPE.nfc.equals(initType)) {
            NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(userType);
            if (NFC_CARD_TYPE.anyo1.equals(nfcCardType) || NFC_CARD_TYPE.U3.equals(nfcCardType) || NFC_CARD_TYPE.cddz_1.equals(nfcCardType) || NFC_CARD_TYPE.cddz_2.equals(nfcCardType) || NFC_CARD_TYPE.ocpp.equals(nfcCardType)) {
                usingRemote = true;
            }
        } else {
            usingRemote = true;
        }
        PortSetting portSetting = null;
        if (usingRemote) {
            if (!RemoteSettingCacheProvider.getInstance().hasRemoteSetting()) {
                usingDefault = true;
            } else {
                portSetting = RemoteSettingCacheProvider.getInstance().getChargePortSetting(this.port);
            }
        } else if (!LocalSettingCacheProvider.getInstance().hasLocalSetting()) {
            usingDefault = true;
        } else {
            portSetting = LocalSettingCacheProvider.getInstance().getChargePortSetting(this.port);
        }
        if (usingDefault) {
            mode = ChargeStatusCacheProvider.getInstance().getPortLockMode(this.port);
        } else if (portSetting != null) {
            mode = portSetting.getGunLockSetting().getMode();
        } else {
            mode = ChargeStatusCacheProvider.getInstance().getPortLockMode(this.port);
        }
        LOCK_STATUS lockStatus = ChargeStatusCacheProvider.getInstance().getPortLockStatus(this.port);
        Log.d("ChargeHandler.setPortLockMode", "port: " + this.port + ", mode: " + mode + ", lockStatus: " + lockStatus + ", usingDefault: " + usingDefault + ", usingRemote: " + usingRemote);
        if (GUN_LOCK_MODE.disable.equals(mode)) {
            if (!LOCK_STATUS.disable.equals(lockStatus)) {
                DeviceProxy.getInstance().unlockGun(this.port);
                DeviceProxy.getInstance().disableGunLock(this.port);
                ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.disable);
            }
        } else if (LOCK_STATUS.disable.equals(lockStatus)) {
            DeviceProxy.getInstance().enableGunLock(this.port);
            DeviceProxy.getInstance().unlockGun(this.port);
            ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.unlock);
        }
        getChargeSession().setGunMode(mode);
    }

    /* access modifiers changed from: private */
    public void handleBillPaidEvent(Uri uri) {
        ChargeSession chargeSession2 = getChargeSession();
        String billId = uri.getLastPathSegment();
        if (billId.equals(chargeSession2.getChargeBill().getCharge_id())) {
            chargeSession2.getChargeBill().setUser_balance(chargeSession2.getChargeBill().getUser_balance() - ((long) chargeSession2.getChargeBill().getTotal_fee()));
            if (GUN_LOCK_MODE.unlock_after_pay.equals(chargeSession2.getGunMode())) {
                if (!LOCK_STATUS.disable.equals(ChargeStatusCacheProvider.getInstance().getPortLockStatus(this.port))) {
                    DeviceProxy.getInstance().unlockGun(this.port);
                    ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.unlock);
                }
            }
            Bundle data = new Bundle();
            data.putString("stage", CHARGE_UI_STAGE.paid.getStage());
            data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession2.getChargeBill().getInit_type()));
            data.putString("chargeId", billId);
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
        }
    }

    /* access modifiers changed from: private */
    public void handleAuthRequest(DCAPMessage request, DCAPMessage confirm) {
        if (!isIdle((CHARGE_INIT_TYPE) null, false)) {
            Log.w("ChargeHandler.handleAuthRequest", "busy now ! port: " + this.port);
            ChargeController.nackConfirm(confirm, 10000, "port busy", (HashMap<String, Object>) null);
            Bundle data = new Bundle();
            data.putString("stage", CHARGE_UI_STAGE.refuse.getStage());
            data.putString("cause", CHARGE_REFUSE_CAUSE.BUSY.getCause());
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
            return;
        }
        ErrorCode error = HardwareStatusCacheProvider.getInstance().getPortFault(this.port);
        if (error != null && error.getCode() != 200) {
            Log.w("ChargeHandler.handleAuthRequest", "port is except now ! port: " + this.port + ", error: " + error.toJson());
            ChargeController.nackConfirm(confirm, 10000, "port except", (HashMap<String, Object>) null);
            Bundle data2 = new Bundle();
            data2.putString("stage", CHARGE_UI_STAGE.refuse.getStage());
            data2.putString("cause", CHARGE_REFUSE_CAUSE.EXCEPT.getCause());
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data2);
        } else if (!ChargeStatusCacheProvider.getInstance().getPortSwitch(this.port)) {
            Log.w("ChargeHandler.handleAuthRequest", "port is forbidened now ! port: " + this.port);
            ChargeController.nackConfirm(confirm, 10000, "port forbidened", (HashMap<String, Object>) null);
            Bundle data3 = new Bundle();
            data3.putString("stage", CHARGE_UI_STAGE.refuse.getStage());
            data3.putString("cause", CHARGE_REFUSE_CAUSE.PORT_FORBIDEN.getCause());
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data3);
        } else {
            CAPMessage cap = (CAPMessage) request.getData();
            CAPDirectiveOption opt = cap.getOpt();
            AuthDirective auth = (AuthDirective) cap.getData();
            if (!"init".equals(opt.getAuth_id())) {
                Log.e("ChargeHandler.handleAuthRequest", "illegal auth id: " + opt.getAuth_id());
                ChargeController.nackConfirm(confirm, 10000, "illegal auth id", (HashMap<String, Object>) null);
                return;
            }
            CHARGE_INIT_TYPE initType = auth.getInit_type();
            String userType = auth.getUser_type();
            ChargeSession chargeSession2 = getChargeSession();
            chargeSession2.getChargeBill().setInit_type(initType);
            chargeSession2.getChargeBill().setUser_type(userType);
            chargeSession2.getChargeBill().setUser_code(auth.getUser_code());
            chargeSession2.setDevice_id(auth.getDevice_id());
            chargeSession2.getChargeBill().setPort(auth.getPort());
            chargeSession2.setConfirm4Auth(confirm);
            chargeSession2.setPlugined(HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port));
            if (initType.equals(CHARGE_INIT_TYPE.nfc)) {
                NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(userType);
                if (nfcCardType == null) {
                    Log.w("ChargeHandler.handleAuthRequest", "illegal NFC type: " + nfcCardType);
                    ChargeController.nackConfirm(confirm, 10000, "illegal nfc card type", (HashMap<String, Object>) null);
                    clearChargeSession();
                } else if (nfcCardType.equals(NFC_CARD_TYPE.U1)) {
                    ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
                    this.status = CHARGE_FSM_STATUS.authed;
                    this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_STARTED;
                    this.handlerTimer.startTimer(10000, MSG_TIMEOUT_AUTHED, (Object) null);
                } else if (nfcCardType.equals(NFC_CARD_TYPE.U2)) {
                    handleU2CardAuthRequest();
                } else if (nfcCardType.equals(NFC_CARD_TYPE.U3)) {
                    authIndicate(CHARGE_PLATFORM.xcharge, auth.getUser_data());
                } else if (nfcCardType.equals(NFC_CARD_TYPE.CT_DEMO)) {
                    ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
                    this.status = CHARGE_FSM_STATUS.authed;
                    this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_STARTED;
                    this.handlerTimer.startTimer(10000, MSG_TIMEOUT_AUTHED, (Object) null);
                } else if (nfcCardType.equals(NFC_CARD_TYPE.anyo1)) {
                    authIndicate(CHARGE_PLATFORM.anyo, (HashMap<String, Object>) null);
                } else if (nfcCardType.equals(NFC_CARD_TYPE.anyo_svw)) {
                    ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
                    this.status = CHARGE_FSM_STATUS.authed;
                    this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_STARTED;
                    this.handlerTimer.startTimer(10000, MSG_TIMEOUT_AUTHED, (Object) null);
                } else if (nfcCardType.equals(NFC_CARD_TYPE.cddz_1)) {
                    authIndicate(CHARGE_PLATFORM.cddz, (HashMap<String, Object>) null);
                } else if (nfcCardType.equals(NFC_CARD_TYPE.ocpp)) {
                    authIndicate(CHARGE_PLATFORM.ocpp, (HashMap<String, Object>) null);
                } else {
                    Log.w("ChargeHandler.handleAuthRequest", "not impleted NFC card type: " + nfcCardType);
                    ChargeController.nackConfirm(confirm, 10000, "not impleted NFC card type", (HashMap<String, Object>) null);
                    clearChargeSession();
                }
            } else {
                Log.w("ChargeHandler.handleAuthRequest", "init type is not nfc, no auth progress !!!");
            }
        }
    }

    private void handleU2CardAuthRequest() {
        ChargeSession chargeSession2 = getChargeSession();
        String userType = chargeSession2.getChargeBill().getUser_type();
        String userCode = chargeSession2.getChargeBill().getUser_code();
        DCAPMessage confirm = chargeSession2.getConfirm4Auth();
        ArrayList<ChargeBill> unpaidBills = ChargeContentProxy.getInstance().getUnpaidBills(userType, userCode);
        if (unpaidBills == null || unpaidBills.size() <= 0) {
            ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
            this.status = CHARGE_FSM_STATUS.authed;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_STARTED;
            this.handlerTimer.startTimer(10000, MSG_TIMEOUT_AUTHED, (Object) null);
            return;
        }
        HashMap<String, Object> attach = new HashMap<>();
        attach.put("user_status", CHARGE_USER_STATUS.need_pay.getStatus());
        attach.put("bill_id", unpaidBills.get(0).getCharge_id());
        attach.put(ChargeStopCondition.TYPE_FEE, String.valueOf(unpaidBills.get(0).getTotal_fee()));
        ChargeController.nackConfirm(confirm, 10000, "unpaid user bill", attach);
        clearChargeSession();
    }

    /* access modifiers changed from: private */
    public void handleInitRequest(DCAPMessage request, DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) request.getData();
        CAPDirectiveOption opt = cap.getOpt();
        InitDirective init = (InitDirective) cap.getData();
        CHARGE_INIT_TYPE initType = init.getInit_type();
        String port2 = init.getPort();
        String userType = init.getUser_type();
        String userCode = init.getUser_code();
        String chargeId = opt.getCharge_id();
        CHARGE_PLATFORM platform = init.getCharge_platform();
        if (CHARGE_INIT_TYPE.nfc.equals(initType)) {
            if (!CHARGE_FSM_STATUS.authed.equals(this.status)) {
                Log.w("ChargeHandler.handleInitRequest", "refuse nfc init request, port: " + port2 + ", status: " + this.status.getStatus());
                refuseInit(FIN_MODE.busy, (ErrorCode) null, userType, userCode, chargeId, Long.valueOf(request.getSeq()));
                return;
            }
        } else if (!CHARGE_FSM_STATUS.idle.equals(this.status)) {
            Log.w("ChargeHandler.handleInitRequest", "refuse init request, port: " + port2 + ", status: " + this.status.getStatus());
            refuseInit(FIN_MODE.busy, (ErrorCode) null, userType, userCode, chargeId, Long.valueOf(request.getSeq()));
            return;
        } else {
            ErrorCode error = HardwareStatusCacheProvider.getInstance().getPortFault(this.port);
            if (!(error == null || error.getCode() == 200)) {
                Log.w("ChargeHandler.handleInitRequest", "refuse init request for except now ! port: " + port2 + ", error: " + error.toJson());
                refuseInit(FIN_MODE.error, error, userType, userCode, chargeId, Long.valueOf(request.getSeq()));
                return;
            }
        }
        if (!CHARGE_FSM_STATUS.idle.equals(this.status) || ChargeStatusCacheProvider.getInstance().getPortSwitch(this.port)) {
            ChargeSession chargeSession2 = getChargeSession();
            chargeSession2.getChargeBill().setUser_type(userType);
            chargeSession2.getChargeBill().setUser_code(userCode);
            chargeSession2.setDevice_id(init.getDevice_id());
            chargeSession2.getChargeBill().setPort(port2);
            chargeSession2.getChargeBill().setInit_type(initType);
            chargeSession2.getChargeBill().setCharge_id(chargeId);
            chargeSession2.getChargeBill().setUser_tc_type(init.getUser_tc_type());
            chargeSession2.getChargeBill().setUser_tc_value(init.getUser_tc_value());
            chargeSession2.getChargeBill().setUser_balance(init.getUser_balance());
            chargeSession2.getChargeBill().setIs_free(init.getIs_free());
            chargeSession2.getChargeBill().setBinded_user(init.getBinded_user());
            chargeSession2.getChargeBill().setCharge_platform(platform);
            chargeSession2.setPlugined(HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port));
            NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(userType);
            if (needFeeRata(platform, nfcCardType)) {
                FeeRate feeRate = agreeChargeFeeRate(initType, nfcCardType, port2, init.getFee_rate());
                if (feeRate == null) {
                    Log.w("ChargeHandler.handleInitRequest", "refuse init request for unavailable fee rate, port: " + port2);
                    refuseInit(FIN_MODE.no_feerate, (ErrorCode) null, userType, userCode, chargeId, Long.valueOf(request.getSeq()));
                    if (platform.equals(CHARGE_PLATFORM.xcharge) && NFC_CARD_TYPE.U3.equals(nfcCardType)) {
                        Log.w("ChargeHandler.handleInitRequest", "no fee rate for U3, keep this status: " + this.status.getStatus() + ", port: " + this.port);
                        if (CHARGE_FSM_STATUS.authed.equals(this.status)) {
                            stopTimer(this.status);
                            this.handlerTimer.startTimer(10000, MSG_TIMEOUT_AUTHED, (Object) null);
                            return;
                        }
                        return;
                    } else if (CHARGE_FSM_STATUS.authed.equals(this.status)) {
                        stopTimer(this.status);
                        this.status = CHARGE_FSM_STATUS.idle;
                        this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
                        notifyChargeEnded2OSS();
                        return;
                    } else {
                        return;
                    }
                } else {
                    chargeSession2.getChargeBill().setFee_rate_id(feeRate.getFeeRateId());
                    chargeSession2.getChargeBill().setFee_rate(feeRate);
                }
            }
            stopTimer(this.status);
            int timeoutPlugin = init.getTimeout_plugin();
            int timeoutChargeStart = init.getTimeout_start();
            int timeoutPlugout = init.getTimeout_plugout();
            if (timeoutPlugin > 0) {
                chargeSession2.setTimeout_plugin(timeoutPlugin);
            }
            if (timeoutChargeStart > 0) {
                chargeSession2.setTimeout_start(timeoutChargeStart);
            }
            if (timeoutPlugout > 0) {
                chargeSession2.setTimeout_plugout(timeoutPlugout);
            }
            chargeSession2.setUserReservedTime(init.getReserve_time());
            setPortLockMode(userType, initType);
            initAckIndicate(request.getSeq());
            this.status = CHARGE_FSM_STATUS.init_ack_sended;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_STARTED;
            RequestSession requestSession = new RequestSession();
            requestSession.setRequest(request);
            requestSession.setConfirm(confirm);
            this.handlerTimer.startTimer(10000, MSG_TIMEOUT_INIT_ACK_SENDED, requestSession);
            return;
        }
        Log.w("ChargeHandler.handleInitRequest", "port is forbidened now ! port: " + port2);
        refuseInit(FIN_MODE.port_forbiden, (ErrorCode) null, userType, userCode, chargeId, Long.valueOf(request.getSeq()));
    }

    private boolean needFeeRata(CHARGE_PLATFORM platform, NFC_CARD_TYPE cardType) {
        if (CHARGE_PLATFORM.xcharge.equals(platform)) {
            if (NFC_CARD_TYPE.U1.equals(cardType) || NFC_CARD_TYPE.CT_DEMO.equals(cardType)) {
                return false;
            }
            return true;
        } else if (CHARGE_PLATFORM.anyo.equals(platform)) {
            return false;
        } else {
            if (CHARGE_PLATFORM.cddz.equals(platform)) {
                return true;
            }
            if (CHARGE_PLATFORM.ocpp.equals(platform)) {
                return false;
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void handleFinRequest(DCAPMessage request, DCAPMessage confirm) {
        FinDirective fin = (FinDirective) ((CAPMessage) request.getData()).getData();
        if (CHARGE_FSM_STATUS.init_ack_sended.equals(this.status) || CHARGE_FSM_STATUS.init_advert.equals(this.status) || CHARGE_FSM_STATUS.user_reserved.equals(this.status) || CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status) || CHARGE_FSM_STATUS.inited.equals(this.status) || CHARGE_FSM_STATUS.plugin.equals(this.status) || CHARGE_FSM_STATUS.reserve_wait.equals(this.status)) {
            stopTimer(this.status);
            this.handlerTimer.stopTimer(MSG_TIMEOUT_RESERVE_CHECK);
            cancelPortChargeAuth();
            if (CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status)) {
                DeviceProxy.getInstance().closeGunLed(this.port);
            }
            HashMap<String, Object> attach = new HashMap<>();
            attach.put("fin_mode", fin.getFin_mode().getMode());
            ErrorCode error = fin.getError();
            if (error != null) {
                attach.put("error", error.toJson());
            }
            ChargeController.ackConfirm(confirm, attach);
            this.status = CHARGE_FSM_STATUS.idle;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
            updateChargeData(false, (PortStatus) null);
            notifyChargeSessionFinished();
            clearChargeSession();
            notifyChargeEnded2OSS();
            return;
        }
        ChargeSession chargeSession2 = getChargeSession();
        if ((CHARGE_INIT_TYPE.nfc.equals(chargeSession2.getChargeBill().getInit_type()) || FIN_MODE.remote.equals(fin.getFin_mode()) || FIN_MODE.nfc.equals(fin.getFin_mode())) && (CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.paused.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status) || CHARGE_FSM_STATUS.stopped.equals(this.status) || CHARGE_FSM_STATUS.stop_sended.equals(this.status) || CHARGE_FSM_STATUS.fin_sended.equals(this.status))) {
            stopTimer(this.status);
            this.handlerTimer.stopTimer(MSG_TIMEOUT_CHARGE_STOP_DELAY);
            this.handlerTimer.stopTimer(MSG_TIMEOUT_PRESTOP_CHECK);
            this.handlerTimer.stopTimer(MSG_INTERVAL_DELAY_TIMING);
            this.handlerTimer.stopTimer(MSG_INTERVAL_CONDITION_TIMING);
            if (CHARGE_FSM_STATUS.stop_sended.equals(this.status) || CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status) || CHARGE_FSM_STATUS.paused.equals(this.status)) {
                chargeSession2.getChargeBill().setStop_time(System.currentTimeMillis());
                chargeSession2.getChargeBill().setTotal_time(new BigDecimal((chargeSession2.getChargeBill().getStop_time() - chargeSession2.getChargeBill().getStart_time()) / 1000).setScale(0, 4).intValue());
            }
            cancelPortChargeAuth();
            HashMap<String, Object> attach2 = new HashMap<>();
            attach2.put("fin_mode", fin.getFin_mode().getMode());
            ErrorCode error2 = fin.getError();
            if (error2 != null) {
                attach2.put("error", error2.toJson());
            }
            ChargeController.ackConfirm(confirm, attach2);
            if (CHARGE_FSM_STATUS.fin_sended.equals(this.status)) {
                if (chargeSession2.getChargeBill().getStop_cause() == null) {
                    if (FIN_MODE.remote.equals(fin.getFin_mode())) {
                        chargeSession2.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.remote_user);
                    } else {
                        chargeSession2.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.local_user);
                    }
                }
                this.status = CHARGE_FSM_STATUS.idle;
                this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
                updateChargeData(false, (PortStatus) null);
                notifyChargeSessionFinished();
                clearChargeSession();
                notifyChargeEnded2OSS();
                return;
            }
            this.handlerTimer.stopTimer(MSG_TIMEOUT_CHARGE_FIN);
            this.handlerTimer.startTimer(TIMEOUT_CHARGE_FIN, MSG_TIMEOUT_CHARGE_FIN, fin);
            return;
        }
        Log.w("ChargeHandler.handleFinRequest", "ignore fin request, port: " + this.port + ", status: " + this.status.getStatus());
    }

    /* access modifiers changed from: private */
    public void handleStartRequest(DCAPMessage request, DCAPMessage confirm) {
        ChargeSession chargeSession2 = getChargeSession();
        if (CHARGE_FSM_STATUS.plugin.equals(this.status)) {
            stopTimer(this.status);
            StartDirective start = (StartDirective) ((CAPMessage) request.getData()).getData();
            chargeSession2.getChargeBill().setUser_tc_type(start.getUser_tc_type());
            chargeSession2.getChargeBill().setUser_tc_value(start.getUser_tc_value());
            startCharge();
            ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
            this.handlerTimer.stopTimer(MSG_TIMEOUT_RESERVE_CHECK);
            this.handlerTimer.startTimer(2000, MSG_TIMEOUT_RESERVE_CHECK, (Object) null);
        } else if (CHARGE_FSM_STATUS.inited.equals(this.status)) {
            if (HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port)) {
                ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
            } else {
                ChargeController.nackConfirm(confirm, 10000, "port not plugin", (HashMap<String, Object>) null);
            }
        } else if (CHARGE_FSM_STATUS.user_reserved.equals(this.status)) {
            requestPortChargeAuth();
        } else {
            Log.w("ChargeHandler.handleStartRequest", "ignore start request, port: " + this.port + ", status: " + this.status.getStatus());
        }
    }

    /* access modifiers changed from: private */
    public void handleConditionRequest(DCAPMessage request, DCAPMessage confirm) {
        if (CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status) || CHARGE_FSM_STATUS.paused.equals(this.status)) {
            ChargeSession chargeSession2 = getChargeSession();
            CAPMessage cap = (CAPMessage) request.getData();
            CAPDirectiveOption opt = cap.getOpt();
            if (chargeSession2.getChargeBill().getCharge_id().equals(opt.getCharge_id())) {
                ConditionDirective condition = (ConditionDirective) cap.getData();
                String conditionId = opt.getCondition_id();
                if (!ConditionDirective.CONDITION_USER_STOP.equals(conditionId)) {
                    Log.w("ChargeHandler.handleConditionRequest", "not supported condition: " + conditionId);
                    ChargeController.nackConfirm(confirm, 10000, "not support", (HashMap<String, Object>) null);
                    return;
                } else if (condition.getUserTcType() == null) {
                    Log.i("ChargeHandler.handleConditionRequest", "clear erver setted condition: " + chargeSession2.getChargeBill().getUser_tc_type() + ", " + chargeSession2.getChargeBill().getUser_tc_value());
                    chargeSession2.getChargeBill().setUser_tc_type((USER_TC_TYPE) null);
                    chargeSession2.getChargeBill().setUser_tc_value((String) null);
                    ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
                    return;
                } else {
                    chargeSession2.getChargeBill().setUser_tc_type(condition.getUserTcType());
                    chargeSession2.getChargeBill().setUser_tc_value(condition.getUserTcValue());
                    if (chargeSession2.isAnyErrorExist()) {
                        Log.i("ChargeHandler.handleConditionRequest", "in error, and only set condition: " + chargeSession2.getChargeBill().getUser_tc_type() + ", " + chargeSession2.getChargeBill().getUser_tc_value());
                        if (USER_TC_TYPE.time.equals(chargeSession2.getChargeBill().getUser_tc_type())) {
                            this.handlerTimer.startTimer(1000, MSG_INTERVAL_CONDITION_TIMING, (Object) null);
                        }
                        ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
                        return;
                    }
                    USER_TC_TYPE utct = chargeSession2.getChargeBill().getUser_tc_type();
                    if (USER_TC_TYPE.time.equals(utct)) {
                        handleConditionTiming();
                    } else if (USER_TC_TYPE.fee.equals(utct)) {
                        try {
                            int feeConditon = Integer.parseInt(chargeSession2.getChargeBill().getUser_tc_value());
                            if (chargeSession2.getChargeBill().getTotal_fee() >= feeConditon) {
                                Log.w("ChargeHandler.handleConditionRequest", "fee is more than user setted: " + feeConditon);
                                stopCharge();
                                chargeSession2.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.user_set);
                            }
                        } catch (Exception e) {
                            Log.w("ChargeHandler.handleConditionRequest", Log.getStackTraceString(e));
                        }
                    } else if (USER_TC_TYPE.power.equals(utct)) {
                        try {
                            double powerConditon = Double.parseDouble(chargeSession2.getChargeBill().getUser_tc_value());
                            if (chargeSession2.getChargeBill().getTotal_power() >= powerConditon) {
                                Log.w("ChargeHandler.handleConditionRequest", "power is more than user setted: " + powerConditon);
                                stopCharge();
                                chargeSession2.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.user_set);
                            }
                        } catch (Exception e2) {
                            Log.w("ChargeHandler.handleConditionRequest", Log.getStackTraceString(e2));
                        }
                    }
                    ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
                    return;
                }
            } else {
                Log.w("ChargeHandler.handleConditionRequest", "not current charge: " + chargeSession2.getChargeBill().getCharge_id() + ", port: " + this.port);
            }
        } else if (CHARGE_FSM_STATUS.user_reserved.equals(this.status)) {
            ChargeSession chargeSession3 = getChargeSession();
            CAPMessage cap2 = (CAPMessage) request.getData();
            CAPDirectiveOption opt2 = cap2.getOpt();
            if (chargeSession3.getChargeBill().getCharge_id().equals(opt2.getCharge_id())) {
                ConditionDirective condition2 = (ConditionDirective) cap2.getData();
                String conditionId2 = opt2.getCondition_id();
                if (ConditionDirective.CONDITION_USER_RESERVE.equals(conditionId2)) {
                    chargeSession3.setUserReservedTime(condition2.getReserveTime());
                    stopTimer(this.status);
                    Bundle data = new Bundle();
                    data.putString("stage", CHARGE_UI_STAGE.user_reserved.getStage());
                    data.putLong(ChargeStopCondition.TYPE_TIME, chargeSession3.getUserReservedTime().longValue());
                    data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession3.getChargeBill().getInit_type()));
                    data.putString("chargeId", chargeSession3.getChargeBill().getCharge_id());
                    UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
                    this.handlerTimer.startTimer(chargeSession3.getUserReservedTime().longValue() - System.currentTimeMillis(), MSG_TIMEOUT_USER_RESERVED, (Object) null);
                    ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
                    return;
                }
                Log.w("ChargeHandler.handleConditionRequest", "not supported condition: " + conditionId2);
                ChargeController.nackConfirm(confirm, 10000, "not support", (HashMap<String, Object>) null);
                return;
            }
        } else {
            Log.w("ChargeHandler.handleConditionRequest", "invalid condition request, port: " + this.port + ", status: " + this.status.getStatus());
        }
        ChargeController.nackConfirm(confirm, 10000, "not found", (HashMap<String, Object>) null);
    }

    /* access modifiers changed from: private */
    public void handleStopRequest(DCAPMessage request, DCAPMessage confirm) {
        ChargeSession chargeSession2 = getChargeSession();
        if (CHARGE_FSM_STATUS.charging.equals(this.status) || (CHARGE_FSM_STATUS.pre_stop.equals(this.status) && !chargeSession2.isAnyErrorExist())) {
            stopCharge();
            chargeSession2.setStop_request_seq(Long.valueOf(request.getSeq()));
            CHARGE_STOP_CAUSE stopCause = CHARGE_STOP_CAUSE.user;
            String from = request.getFrom();
            if (from.startsWith("user:" + CHARGE_USER_TYPE.nfc)) {
                stopCause = CHARGE_STOP_CAUSE.local_user;
            } else if (from.startsWith("user:")) {
                stopCause = CHARGE_STOP_CAUSE.remote_user;
            } else if (from.startsWith("server:")) {
                stopCause = CHARGE_STOP_CAUSE.system_user;
            }
            chargeSession2.getChargeBill().setStop_cause(stopCause);
            this.status = CHARGE_FSM_STATUS.stop_sended;
            updateChargeData(false, (PortStatus) null);
            RequestSession requestSession = new RequestSession();
            requestSession.setRequest(request);
            requestSession.setConfirm(confirm);
            this.handlerTimer.startTimer(5000, MSG_TIMEOUT_STOP_SENDED, requestSession);
            return;
        }
        Log.w("ChargeHandler.handleStopRequest", "ignore stop request, port: " + this.port + ", status: " + this.status.getStatus());
    }

    /* access modifiers changed from: private */
    public void handleChargeRefuseEvent(DCAPMessage request, DCAPMessage confirm) {
        if (CHARGE_FSM_STATUS.idle.equals(this.status)) {
            EventDirective event = (EventDirective) ((CAPMessage) request.getData()).getData();
            CHARGE_REFUSE_CAUSE cause = event.getRefuse_cause();
            HashMap<String, Object> attch = event.getAttach();
            Bundle data = new Bundle();
            data.putString("stage", CHARGE_UI_STAGE.refuse.getStage());
            data.putString("cause", cause.getCause());
            if (attch != null) {
                for (String key : attch.keySet()) {
                    data.putString(key, attch.get(key).toString());
                }
            }
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
        }
    }

    /* access modifiers changed from: private */
    public void handleScanAdvertFinishedEvent(DCAPMessage request, DCAPMessage confirm) {
        handleInitAdertFinished();
    }

    /* access modifiers changed from: private */
    public void retryStopCharge(DCAPMessage request, DCAPMessage confirm) {
        stopCharge();
        getChargeSession().incStop_retry();
        RequestSession requestSession = new RequestSession();
        requestSession.setRequest(request);
        requestSession.setConfirm(confirm);
        this.handlerTimer.startTimer(5000, MSG_TIMEOUT_STOP_SENDED, requestSession);
    }

    /* access modifiers changed from: private */
    public void handleAckResponse(DCAPMessage response) {
        String peerOp = ((CAPMessage) response.getData()).getOpt().getOp();
        if (CAPMessage.DIRECTIVE_INIT_ACK.equals(peerOp)) {
            handleInitAckResponse(response);
        } else if ("fin".equals(peerOp)) {
            handleFinResponse(response);
        } else if ("auth".equals(peerOp)) {
            handleAuthResponse(response, true);
        }
    }

    /* access modifiers changed from: private */
    public void handleNackResponse(DCAPMessage response) {
        if ("auth".equals(((CAPMessage) response.getData()).getOpt().getOp())) {
            handleAuthResponse(response, false);
        }
    }

    private void handleAuthResponse(DCAPMessage response, boolean isAck) {
        if (CHARGE_FSM_STATUS.auth_sended.equals(this.status)) {
            CAPMessage cap = (CAPMessage) response.getData();
            CAPDirectiveOption opt = cap.getOpt();
            ChargeSession chargeSession2 = getChargeSession();
            if (chargeSession2.getExpected_resopnse() == null || !opt.getSeq().equals(chargeSession2.getExpected_resopnse())) {
                Log.w("ChargeHandler.handleAuthResponse", "unhandle auth response, charge session: " + chargeSession2.toJson() + ", response: " + response.toJson());
                return;
            }
            stopTimer(this.status);
            chargeSession2.setExpected_resopnse((Long) null);
            if (isAck) {
                ChargeController.ackConfirm(chargeSession2.getConfirm4Auth(), ((AckDirective) cap.getData()).getAttach());
                this.status = CHARGE_FSM_STATUS.authed;
                if (chargeSession2.isPlugined()) {
                    this.ocppChargeStatus = OCPP_CHARGE_STATUS.TRANSACTION_STARTED;
                }
                this.handlerTimer.startTimer(10000, MSG_TIMEOUT_AUTHED, (Object) null);
                return;
            }
            NackDirective nack = (NackDirective) cap.getData();
            ChargeController.nackConfirm(chargeSession2.getConfirm4Auth(), 10000, "server refused auth", nack.getAttach());
            Bundle data = new Bundle();
            data.putString("stage", CHARGE_UI_STAGE.refuse.getStage());
            HashMap<String, Object> attach = nack.getAttach();
            if (attach != null) {
                String cause = (String) attach.get("cause");
                if (!TextUtils.isEmpty(cause)) {
                    data.putString("cause", cause);
                    for (String key : attach.keySet()) {
                        if (!key.equals("cause")) {
                            data.putString(key, attach.get(key).toString());
                        }
                    }
                } else {
                    data.putString("cause", CHARGE_REFUSE_CAUSE.AUTH_REFUSE.getCause());
                }
            } else if (nack.getError() == 10004) {
                data.putString("cause", CHARGE_REFUSE_CAUSE.NOT_PLUGGED.getCause());
            } else {
                data.putString("cause", CHARGE_REFUSE_CAUSE.AUTH_REFUSE.getCause());
            }
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
            clearChargeSession();
            this.status = CHARGE_FSM_STATUS.idle;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
            notifyChargeEnded2OSS();
            return;
        }
        Log.w("ChargeHandler.handleAuthResponse", "ignore auth response, port: " + this.port + ", status: " + this.status.getStatus());
    }

    private void handleInitAckResponse(DCAPMessage response) {
        if (CHARGE_FSM_STATUS.init_ack_sended.equals(this.status)) {
            CAPDirectiveOption opt = ((CAPMessage) response.getData()).getOpt();
            ChargeSession chargeSession2 = getChargeSession();
            if (chargeSession2.getExpected_resopnse() == null || !opt.getSeq().equals(chargeSession2.getExpected_resopnse()) || !chargeSession2.getChargeBill().getCharge_id().equals(opt.getCharge_id())) {
                Log.w("ChargeHandler.handleInitAckResponse", "unhandle init_ack response, charge session: " + chargeSession2.toJson() + ", response: " + response.toJson());
                return;
            }
            if (chargeSession2.isPlugined()) {
                this.ocppChargeStatus = OCPP_CHARGE_STATUS.TRANSACTION_STARTED;
            }
            chargeSession2.setExpected_resopnse((Long) null);
            long initAdvertTime = getInitAdvertTime();
            if (initAdvertTime > 0) {
                stopTimer(this.status);
                enterInitAdvertStatus(5 + initAdvertTime);
            } else if (chargeSession2.getUserReservedTime() != null) {
                stopTimer(this.status);
                if (HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port)) {
                    enterUserReservedStatus();
                    return;
                }
                if (CHARGE_PLATFORM.ocpp.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
                    enterUserReservedStatus();
                } else {
                    enterUserReservedWaitPluginStatus();
                }
            } else {
                chargeSession2.getTimeout_start();
                requestPortChargeAuth();
            }
        } else {
            Log.w("ChargeHandler.handleInitAckResponse", "ignore init_ack response, port: " + this.port + ", status: " + this.status.getStatus());
        }
    }

    /* access modifiers changed from: private */
    public void handleInitAdvertTimeout() {
        handleInitAdertFinished();
    }

    private void handleInitAdertFinished() {
        if (CHARGE_FSM_STATUS.init_advert.equals(this.status)) {
            stopTimer(this.status);
            ChargeSession chargeSession2 = getChargeSession();
            if (HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port) && chargeSession2.getTimeout_start() > 0) {
                enterPluginStatus();
            } else if (chargeSession2.getUserReservedTime() == null) {
                requestPortChargeAuth();
            } else if (HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port)) {
                enterUserReservedStatus();
            } else {
                if (CHARGE_PLATFORM.ocpp.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
                    enterUserReservedStatus();
                } else {
                    enterUserReservedWaitPluginStatus();
                }
            }
        } else {
            Log.w("ChargeHandler.handleInitAdertFinished", "unhandled init advert finished, port: " + this.port + ", status: " + this.status.getStatus());
        }
    }

    private void enterInitAdvertStatus(long timeout) {
        ChargeSession chargeSession2 = getChargeSession();
        this.status = CHARGE_FSM_STATUS.init_advert;
        chargeSession2.setTimeout_init_advert(timeout);
        updateChargeData(false, (PortStatus) null);
        Bundle data = new Bundle();
        data.putString("stage", CHARGE_UI_STAGE.scan_advert.getStage());
        data.putLong(ChargeStopCondition.TYPE_TIME, timeout);
        data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession2.getChargeBill().getInit_type()));
        data.putString("chargeId", chargeSession2.getChargeBill().getCharge_id());
        UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
        this.handlerTimer.startTimer(1000 * timeout, MSG_TIMEOUT_INIT_ADVERT, (Object) null);
    }

    /* access modifiers changed from: private */
    public void handleUserReservedTimeout() {
        if (CHARGE_FSM_STATUS.user_reserved.equals(this.status)) {
            stopTimer(this.status);
            requestPortChargeAuth();
            return;
        }
        Log.w("ChargeHandler.handleUserReservedTimeout", "ignore user reserved timeout, port: " + this.port + ", status: " + this.status.getStatus());
    }

    private void enterUserReservedStatus() {
        DeviceProxy.getInstance().closeGunLed(this.port);
        ChargeSession chargeSession2 = getChargeSession();
        this.status = CHARGE_FSM_STATUS.user_reserved;
        updateChargeData(false, (PortStatus) null);
        Bundle data = new Bundle();
        data.putString("stage", CHARGE_UI_STAGE.user_reserved.getStage());
        data.putLong(ChargeStopCondition.TYPE_TIME, chargeSession2.getUserReservedTime().longValue());
        data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession2.getChargeBill().getInit_type()));
        data.putString("chargeId", chargeSession2.getChargeBill().getCharge_id());
        UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
        this.handlerTimer.startTimer(chargeSession2.getUserReservedTime().longValue() - System.currentTimeMillis(), MSG_TIMEOUT_USER_RESERVED, (Object) null);
    }

    private void enterUserReservedWaitPluginStatus() {
        DeviceProxy.getInstance().openGunLed(this.port);
        ChargeSession chargeSession2 = getChargeSession();
        this.status = CHARGE_FSM_STATUS.user_reserve_wait_plugin;
        updateChargeData(false, (PortStatus) null);
        Bundle data = new Bundle();
        data.putString("stage", CHARGE_UI_STAGE.inited.getStage());
        data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession2.getChargeBill().getInit_type()));
        data.putString("chargeId", chargeSession2.getChargeBill().getCharge_id());
        int timeout = chargeSession2.getTimeout_plugin();
        if (timeout > 0) {
            data.putInt("waitPlugin", timeout);
            this.handlerTimer.startTimer((long) (timeout * 1000), MSG_TIMEOUT_USER_RESERVE_WAIT_PLUGIN, (Object) null);
        }
        UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
    }

    private void enterInitedStatus() {
        ChargeSession chargeSession2 = getChargeSession();
        this.status = CHARGE_FSM_STATUS.inited;
        updateChargeData(false, (PortStatus) null);
        Bundle data = new Bundle();
        data.putString("stage", CHARGE_UI_STAGE.inited.getStage());
        data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession2.getChargeBill().getInit_type()));
        data.putString("chargeId", chargeSession2.getChargeBill().getCharge_id());
        int timeout = chargeSession2.getTimeout_plugin();
        if (timeout > 0) {
            data.putInt("waitPlugin", timeout);
            this.handlerTimer.startTimer((long) (timeout * 1000), MSG_TIMEOUT_INITED, (Object) null);
        }
        UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
    }

    private long getInitAdvertTime() {
        ArrayList<ContentItem> scanAdvertSite;
        long totalTime = 0;
        try {
            if (ChargeStatusCacheProvider.getInstance().isAdvertEnabled() && (scanAdvertSite = RemoteSettingCacheProvider.getInstance().getAdvertContent(ADVERT_POLICY.scanAdvsite)) != null) {
                Iterator<ContentItem> it = scanAdvertSite.iterator();
                while (it.hasNext()) {
                    ContentItem item = it.next();
                    String localFile = item.getLocalPath();
                    if (!TextUtils.isEmpty(localFile) && new File(localFile).exists()) {
                        totalTime += item.getDuration();
                    }
                }
            }
            return totalTime;
        } catch (Exception e) {
            Log.w("ChargeHandler.getInitAdvertTime", Log.getStackTraceString(e));
            return 0;
        }
    }

    private void handleFinResponse(DCAPMessage response) {
        if (CHARGE_FSM_STATUS.fin_sended.equals(this.status)) {
            CAPDirectiveOption opt = ((CAPMessage) response.getData()).getOpt();
            ChargeSession chargeSession2 = getChargeSession();
            if (chargeSession2.getExpected_resopnse() == null || !opt.getSeq().equals(chargeSession2.getExpected_resopnse()) || (!(chargeSession2.getChargeBill().getCharge_id() == null && opt.getCharge_id() == null) && (chargeSession2.getChargeBill().getCharge_id() == null || !chargeSession2.getChargeBill().getCharge_id().equals(opt.getCharge_id())))) {
                Log.w("ChargeHandler.handleFinResponse", "unhandle fin response, charge session: " + chargeSession2.toJson() + ", response: " + response.toJson());
                return;
            }
            stopTimer(this.status);
            chargeSession2.setExpected_resopnse((Long) null);
            this.status = CHARGE_FSM_STATUS.idle;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
            updateChargeData(false, (PortStatus) null);
            notifyChargeSessionFinished();
            clearChargeSession();
            notifyChargeEnded2OSS();
            return;
        }
        Log.w("ChargeHandler.handleFinResponse", "ignore fin response, port: " + this.port + ", status: " + this.status.getStatus());
    }

    /* access modifiers changed from: private */
    public void handleAuthValid(PortStatus portStatus) {
        ChargeSession chargeSession2 = getChargeSession();
        chargeSession2.setDeviceStatus(portStatus.getPortRuntimeStatus());
        if (CHARGE_FSM_STATUS.init_ack_sended.equals(this.status) || CHARGE_FSM_STATUS.init_advert.equals(this.status) || CHARGE_FSM_STATUS.user_reserved.equals(this.status)) {
            stopTimer(this.status);
            chargeSession2.getChargeBill().setInit_time(System.currentTimeMillis());
            enterInitedStatus();
            if (HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port)) {
                this.handlerTimer.stopTimer(MSG_TIMEOUT_RESERVE_CHECK);
                this.handlerTimer.startTimer(2000, MSG_TIMEOUT_RESERVE_CHECK, (Object) null);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleAuthInvalid(PortStatus portStatus) {
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
        ChargeSession chargeSession2 = getChargeSession();
        DEVICE_STATUS deviceStatus = chargeSession2.getDeviceStatus();
        if (deviceStatus != null && DEVICE_STATUS.idle.getStatus() != deviceStatus.getStatus()) {
            chargeSession2.setDeviceStatus(portStatus.getPortRuntimeStatus());
        }
    }

    /* access modifiers changed from: private */
    public void handlePortPluginStatusChanged() {
        boolean isPlugin = HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port);
        ChargeSession chargeSession2 = getChargeSession();
        if (chargeSession2.isDeviceAuth()) {
            return;
        }
        if (isPlugin) {
            this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
            chargeSession2.setPlugined(true);
            if (CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status)) {
                stopTimer(this.status);
                updateChargeData(false, (PortStatus) null);
                enterUserReservedStatus();
                return;
            }
            return;
        }
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
        if (CHARGE_PLATFORM.ocpp.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
            if (!CHARGE_FSM_STATUS.user_reserved.equals(this.status) && chargeSession2.isPlugined()) {
                this.handlerTimer.startTimer(2000, MSG_TIMEOUT_PLUGOUT, (Object) null);
            }
        } else if (chargeSession2.isPlugined()) {
            this.handlerTimer.startTimer(2000, MSG_TIMEOUT_PLUGOUT, (Object) null);
        }
    }

    /* access modifiers changed from: private */
    public void handlePlugin(PortStatus portStatus) {
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
        ChargeSession chargeSession2 = getChargeSession();
        chargeSession2.setDeviceStatus(portStatus.getPortRuntimeStatus());
        chargeSession2.setPlugined(true);
        HardwareStatusCacheProvider.getInstance().updatePortPluginStatus(this.port, true);
        if (CHARGE_FSM_STATUS.init_advert.equals(this.status) || CHARGE_FSM_STATUS.user_reserved.equals(this.status) || CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status) || CHARGE_FSM_STATUS.inited.equals(this.status)) {
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.TRANSACTION_STARTED;
        }
        int startTimeout = chargeSession2.getTimeout_start();
        if (!chargeSession2.isDeviceAuth()) {
            return;
        }
        if (CHARGE_FSM_STATUS.inited.equals(this.status)) {
            if (startTimeout > 0) {
                enterPluginStatus();
                return;
            }
            stopTimer(this.status);
            updateChargeData(false, (PortStatus) null);
            this.handlerTimer.stopTimer(MSG_TIMEOUT_RESERVE_CHECK);
            this.handlerTimer.startTimer(2000, MSG_TIMEOUT_RESERVE_CHECK, (Object) null);
        } else if (CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status) || CHARGE_FSM_STATUS.paused.equals(this.status)) {
            this.handlerTimer.stopTimer(MSG_TIMEOUT_PRESTOP_CHECK);
            this.handlerTimer.startTimer(2000, MSG_TIMEOUT_PRESTOP_CHECK, portStatus);
        } else {
            Log.w("ChargeHandler.handlePlugin", "ignore plugin event, port: " + this.port + ", status: " + this.status.getStatus());
        }
    }

    /* access modifiers changed from: private */
    public void handlePrestopCheckTimeout(PortStatus portStatus) {
        ChargeSession chargeSession2 = getChargeSession();
        if (CHARGE_FSM_STATUS.charging.equals(this.status)) {
            if (!LOCK_STATUS.disable.equals(ChargeStatusCacheProvider.getInstance().getPortLockStatus(this.port))) {
                DeviceProxy.getInstance().unlockGun(this.port);
                ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.unlock);
            }
            chargeSession2.setEnteredNormalCharging(false);
            this.status = CHARGE_FSM_STATUS.pre_stop;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_OFFER_SUSPEND;
            updateChargeData(false, (PortStatus) null);
            HashMap<String, Object> attach = new HashMap<>();
            attach.put("error", new ErrorCode(ErrorCode.EC_CAR_STOP_CHARGE).toJson());
            attach.put(ChargeStopCondition.TYPE_TIME, String.valueOf(System.currentTimeMillis()));
            eventIndicate(EventDirective.EVENT_CHARGE_PAUSE, attach);
        } else if (CHARGE_FSM_STATUS.pre_stop.equals(this.status)) {
            updateChargeData(false, (PortStatus) null);
        } else if (CHARGE_FSM_STATUS.paused.equals(this.status)) {
            this.status = CHARGE_FSM_STATUS.pre_stop;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_OFFER_SUSPEND;
            updateChargeData(false, (PortStatus) null);
            chargeSession2.isAnyErrorExist();
        } else {
            Log.w("ChargeHandler.handlePrestopCheckTimeout", "ignore prestop check timeout, port: " + this.port + ", status: " + this.status.getStatus());
        }
    }

    private void enterPluginStatus() {
        ChargeSession chargeSession2 = getChargeSession();
        int startTimeout = chargeSession2.getTimeout_start();
        stopTimer(this.status);
        this.status = CHARGE_FSM_STATUS.plugin;
        updateChargeData(false, (PortStatus) null);
        eventIndicate(EventDirective.EVENT_PLUGIN, (HashMap<String, Object>) null);
        Bundle data = new Bundle();
        data.putString("stage", CHARGE_UI_STAGE.plugin.getStage());
        data.putInt("waitStart", startTimeout);
        data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession2.getChargeBill().getInit_type()));
        data.putString("chargeId", chargeSession2.getChargeBill().getCharge_id());
        UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
        this.handlerTimer.startTimer((long) (startTimeout * 1000), MSG_TIMEOUT_PLUGIN, (Object) null);
    }

    /* access modifiers changed from: private */
    public void handleReserveCheckTimeout() {
        if (CHARGE_FSM_STATUS.inited.equals(this.status) || CHARGE_FSM_STATUS.plugin.equals(this.status)) {
            this.status = CHARGE_FSM_STATUS.reserve_wait;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.TRANSACTION_STARTED;
            this.handlerTimer.startTimer(60000, MSG_TIMEOUT_RESERVE_WAIT, (Object) null);
            Bundle data = new Bundle();
            data.putString("stage", CHARGE_UI_STAGE.reserve.getStage());
            data.putBoolean("isClean", false);
            data.putInt("waitStart", 60);
            data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(this.chargeSession.getChargeBill().getInit_type()));
            data.putString("chargeId", this.chargeSession.getChargeBill().getCharge_id());
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
            return;
        }
        Log.w("ChargeHandler.handleReserveCheckTimeout", "ignore reserve check timeout, port: " + this.port + ", status: " + this.status.getStatus());
    }

    /* access modifiers changed from: private */
    public void handlePlugout(PortStatus portStatus) {
        ChargeSession chargeSession2 = getChargeSession();
        chargeSession2.setDeviceStatus(portStatus.getPortRuntimeStatus());
        this.handlerTimer.stopTimer(MSG_TIMEOUT_CHARGE_STOP_DELAY);
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PRESTOP_CHECK);
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
        if (chargeSession2.isPlugined()) {
            this.handlerTimer.startTimer(2000, MSG_TIMEOUT_PLUGOUT, (Object) null);
        }
    }

    /* access modifiers changed from: private */
    public void handlePlugoutIndeed() {
        ChargeSession chargeSession2 = getChargeSession();
        chargeSession2.setPlugined(false);
        HardwareStatusCacheProvider.getInstance().updatePortPluginStatus(this.port, false);
        if (CHARGE_FSM_STATUS.init_advert.equals(this.status) || CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status) || CHARGE_FSM_STATUS.user_reserved.equals(this.status) || CHARGE_FSM_STATUS.plugin.equals(this.status) || CHARGE_FSM_STATUS.reserve_wait.equals(this.status) || CHARGE_FSM_STATUS.inited.equals(this.status) || CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.paused.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status) || CHARGE_FSM_STATUS.stopped.equals(this.status) || CHARGE_FSM_STATUS.stop_sended.equals(this.status)) {
            stopTimer(this.status);
            this.handlerTimer.stopTimer(MSG_TIMEOUT_CHARGE_FIN);
            this.handlerTimer.stopTimer(MSG_TIMEOUT_RESERVE_CHECK);
            this.handlerTimer.stopTimer(MSG_INTERVAL_DELAY_TIMING);
            this.handlerTimer.stopTimer(MSG_INTERVAL_CONDITION_TIMING);
            if (CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status)) {
                DeviceProxy.getInstance().closeGunLed(this.port);
            }
            if (!CHARGE_FSM_STATUS.init_advert.equals(this.status) && !CHARGE_FSM_STATUS.user_reserved.equals(this.status) && !CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status) && !CHARGE_FSM_STATUS.inited.equals(this.status) && !CHARGE_FSM_STATUS.plugin.equals(this.status) && !CHARGE_FSM_STATUS.reserve_wait.equals(this.status) && chargeSession2.getChargeBill().getStop_cause() == null) {
                chargeSession2.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.plugout);
            }
            boolean isStopAmmeter = false;
            if (CHARGE_FSM_STATUS.stop_sended.equals(this.status) || CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status) || CHARGE_FSM_STATUS.paused.equals(this.status)) {
                chargeSession2.getChargeBill().setStop_time(System.currentTimeMillis());
                chargeSession2.getChargeBill().setTotal_time(new BigDecimal((chargeSession2.getChargeBill().getStop_time() - chargeSession2.getChargeBill().getStart_time()) / 1000).setScale(0, 4).intValue());
                handleStopAmmeter(true);
                isStopAmmeter = true;
            } else if (CHARGE_FSM_STATUS.stopped.equals(this.status)) {
                handleStopAmmeter(true);
                isStopAmmeter = true;
            }
            if (chargeSession2.getChargeBill() == null || !CHARGE_INIT_TYPE.nfc.equals(chargeSession2.getChargeBill().getInit_type()) || chargeSession2.isDeviceAuth() || (!CHARGE_FSM_STATUS.charging.equals(this.status) && !CHARGE_FSM_STATUS.paused.equals(this.status) && !CHARGE_FSM_STATUS.pre_stop.equals(this.status) && !CHARGE_FSM_STATUS.stopped.equals(this.status) && !CHARGE_FSM_STATUS.stop_sended.equals(this.status))) {
                updateChargeData(isStopAmmeter, (PortStatus) null);
                FIN_MODE finMode = FIN_MODE.normal;
                if (CHARGE_FSM_STATUS.init_advert.equals(this.status) || CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status) || CHARGE_FSM_STATUS.user_reserved.equals(this.status) || CHARGE_FSM_STATUS.inited.equals(this.status) || CHARGE_FSM_STATUS.plugin.equals(this.status) || CHARGE_FSM_STATUS.reserve_wait.equals(this.status)) {
                    finMode = FIN_MODE.cancel;
                }
                finIndicate(finMode, (ErrorCode) null, chargeSession2.getChargeBill().getUser_type(), chargeSession2.getChargeBill().getUser_code(), chargeSession2.getChargeBill().getCharge_id(), (Long) null);
                cancelPortChargeAuth();
                if (chargeSession2.getChargeBill().getDelay_start() > 0) {
                    calcDelayFee(true);
                    return;
                }
                return;
            }
            if (chargeSession2.getChargeBill().getDelay_start() > 0) {
                calcDelayFee(true);
            }
            this.status = CHARGE_FSM_STATUS.idle;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
            updateChargeData(isStopAmmeter, (PortStatus) null);
            notifyChargeSessionFinished();
            clearChargeSession();
            notifyChargeEnded2OSS();
            return;
        }
        Log.w("ChargeHandler.handlePlugout", "ignore indeeded plugout event, port: " + this.port + ", status: " + this.status.getStatus());
    }

    /* access modifiers changed from: private */
    public void handleChargeFinIndeed(FinDirective fin) {
        ChargeSession chargeSession2 = getChargeSession();
        if (chargeSession2.getChargeBill() == null) {
            Log.w("ChargeHandler.handleChargeFinIndeed", "ignore charge fin request for charge session has been finished, port: " + this.port + ", status: " + this.status.getStatus());
        } else if ((CHARGE_INIT_TYPE.nfc.equals(chargeSession2.getChargeBill().getInit_type()) || FIN_MODE.remote.equals(fin.getFin_mode()) || FIN_MODE.nfc.equals(fin.getFin_mode())) && !chargeSession2.isDeviceAuth() && (CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.paused.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status) || CHARGE_FSM_STATUS.stopped.equals(this.status) || CHARGE_FSM_STATUS.stop_sended.equals(this.status))) {
            if (chargeSession2.getChargeBill().getStop_cause() == null) {
                if (FIN_MODE.remote.equals(fin.getFin_mode())) {
                    chargeSession2.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.remote_user);
                } else {
                    chargeSession2.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.local_user);
                }
            }
            if (CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.paused.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status) || CHARGE_FSM_STATUS.stop_sended.equals(this.status)) {
                long now = System.currentTimeMillis();
                chargeSession2.getChargeBill().setStop_time(now);
                chargeSession2.getChargeBill().setTotal_time(new BigDecimal((chargeSession2.getChargeBill().getStop_time() - chargeSession2.getChargeBill().getStart_time()) / 1000).setScale(0, 4).intValue());
                chargeSession2.getChargeBill().setFin_time(now);
            }
            handleStopAmmeter(false);
            if (chargeSession2.getChargeBill().getDelay_start() > 0) {
                calcDelayFee(true);
            }
            this.status = CHARGE_FSM_STATUS.idle;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
            updateChargeData(true, (PortStatus) null);
            notifyChargeSessionFinished();
            clearChargeSession();
            notifyChargeEnded2OSS();
        } else {
            Log.w("ChargeHandler.handleChargeFinIndeed", "ignore charge fin request, port: " + this.port + ", status: " + this.status.getStatus());
        }
    }

    /* access modifiers changed from: private */
    public void handleChargeStarted(boolean isChargeStartedEvent, PortStatus ps) {
        ChargeSession chargeSession2 = getChargeSession();
        chargeSession2.setDeviceStatus(ps.getPortRuntimeStatus());
        this.handlerTimer.stopTimer(MSG_TIMEOUT_CHARGE_STOP_DELAY);
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PRESTOP_CHECK);
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
        if (CHARGE_FSM_STATUS.inited.equals(this.status) || CHARGE_FSM_STATUS.plugin.equals(this.status) || CHARGE_FSM_STATUS.reserve_wait.equals(this.status)) {
            PortStatus portChargeStatus = ps;
            if ((!isChargeStartedEvent || (isChargeStartedEvent && ps.getPower() == null)) && (portChargeStatus = DeviceProxy.getInstance().getPortChargeStatus(this.port)) == null) {
                Log.w("ChargeHandler.handleChargeStarted", "failed to get info from driver, port: " + this.port + ", status: " + this.status.getStatus());
            } else if (portChargeStatus.getPower() == null) {
                Log.w("ChargeHandler.handleChargeStarted", "failed to get ammeter, port: " + this.port + ", status: " + this.status.getStatus() + ", data: " + portChargeStatus.toJson());
            } else {
                stopTimer(this.status);
                this.handlerTimer.stopTimer(MSG_TIMEOUT_RESERVE_CHECK);
                this.startPower = portChargeStatus.getPower().doubleValue();
                Log.i("ChargeHandler.handleChargeStarted", "charge " + chargeSession2.getChargeBill().getCharge_id() + " on port " + this.port + " start power: " + this.startPower);
                chargeSession2.getChargeBill().setStart_ammeter(this.startPower);
                chargeSession2.getChargeBill().setStart_time(System.currentTimeMillis());
                chargeSession2.setLatestPowerMeterTimestamp(chargeSession2.getChargeBill().getStart_time());
                this.ammeter = this.startPower;
                FeeRate feeRate = chargeSession2.getChargeBill().getFee_rate();
                if (feeRate != null) {
                    if (feeRate.getPowerPrice() != null) {
                        chargeSession2.getChargeBill().setPower_info(new ArrayList());
                    }
                    if (SERVICE_PRICE_UNIT.order.equals(feeRate.getServiceUnit())) {
                        chargeSession2.getChargeBill().setService_fee(new BigDecimal(100.0d * ((Double) feeRate.getServicePrice().get(0).get("price")).doubleValue()).setScale(0, 4).intValue());
                    } else if (SERVICE_PRICE_UNIT.degree.equals(feeRate.getServiceUnit()) && feeRate.getServicePrice() != null) {
                        chargeSession2.getChargeBill().setService_info(new ArrayList());
                    }
                }
                this.status = CHARGE_FSM_STATUS.charging;
                this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_OFFER;
                updateChargeData(false, (PortStatus) null);
                eventIndicate(EventDirective.EVENT_CHARGE_START, (HashMap<String, Object>) null);
                if (USER_TC_TYPE.time.equals(chargeSession2.getChargeBill().getUser_tc_type())) {
                    this.handlerTimer.startTimer(1000, MSG_INTERVAL_CONDITION_TIMING, (Object) null);
                }
                Bundle data = new Bundle();
                data.putString("stage", CHARGE_UI_STAGE.charging.getStage());
                data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession2.getChargeBill().getInit_type()));
                data.putString("chargeId", chargeSession2.getChargeBill().getCharge_id());
                UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
            }
        } else if (CHARGE_FSM_STATUS.pre_stop.equals(this.status)) {
            stopTimer(this.status);
            this.status = CHARGE_FSM_STATUS.charging;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_OFFER;
            updateChargeData(false, (PortStatus) null);
            HashMap<String, Object> attach = new HashMap<>();
            attach.put("error", String.valueOf(ErrorCode.EC_CAR_STOP_CHARGE));
            attach.put(ChargeStopCondition.TYPE_TIME, String.valueOf(System.currentTimeMillis()));
            eventIndicate(EventDirective.EVENT_CHARGE_RESUME, attach);
            Bundle data2 = new Bundle();
            data2.putString("stage", CHARGE_UI_STAGE.charging.getStage());
            data2.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession2.getChargeBill().getInit_type()));
            data2.putString("chargeId", chargeSession2.getChargeBill().getCharge_id());
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data2);
        } else if (CHARGE_FSM_STATUS.paused.equals(this.status)) {
            this.status = CHARGE_FSM_STATUS.charging;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_OFFER;
            HashMap<String, Object> attach2 = new HashMap<>();
            if (chargeSession2.getLatestResumedError() != null) {
                attach2.put("error", String.valueOf(chargeSession2.getLatestResumedError().getCode()));
            }
            attach2.put(ChargeStopCondition.TYPE_TIME, String.valueOf(System.currentTimeMillis()));
            eventIndicate(EventDirective.EVENT_CHARGE_RESUME, attach2);
        } else {
            Log.w("ChargeHandler.handleChargeStarted", "ignore charge start event, port: " + this.port + ", status: " + this.status.getStatus());
        }
    }

    /* access modifiers changed from: private */
    public void handleChargeFull(PortStatus portStatus) {
        ChargeSession chargeSession2 = getChargeSession();
        chargeSession2.setDeviceStatus(portStatus.getPortRuntimeStatus());
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
        if (CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status)) {
            chargeSession2.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.full);
            handleChargeStopped(true, portStatus);
            return;
        }
        Log.w("ChargeHandler.handleChargeStarted", "ignore charge full event, port: " + this.port + ", status: " + this.status.getStatus());
    }

    /* access modifiers changed from: private */
    public void handleSuspend(PortStatus portStatus) {
        boolean z = true;
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
        ChargeSession chargeSession2 = getChargeSession();
        chargeSession2.setDeviceStatus(portStatus.getPortRuntimeStatus());
        DEVICE_STATUS status2 = portStatus.getPortRuntimeStatus();
        if (DEVICE_STATUS.emergencyStop.equals(status2)) {
            chargeSession2.setEmergencyStopped(true);
        }
        updateChargeData(false, (PortStatus) null);
        if (DEVICE_STATUS.notInited.getStatus() <= status2.getStatus()) {
            chargeSession2.setAnyErrorExist(true);
            if (CHARGE_FSM_STATUS.reserve_wait.equals(this.status)) {
                this.handlerTimer.stopTimer(MSG_TIMEOUT_RESERVE_WAIT);
                Bundle data = new Bundle();
                data.putString("stage", CHARGE_UI_STAGE.reserve.getStage());
                data.putBoolean("isClean", true);
                data.putInt("waitStart", 60);
                data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession2.getChargeBill().getInit_type()));
                data.putString("chargeId", chargeSession2.getChargeBill().getCharge_id());
                UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
            } else if (CHARGE_FSM_STATUS.charging.equals(this.status)) {
                this.status = CHARGE_FSM_STATUS.paused;
                this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_OFFER_SUSPEND;
                chargeSession2.setEnteredNormalCharging(false);
                updateChargeData(false, (PortStatus) null);
                HashMap<String, Object> attach = new HashMap<>();
                ErrorCode error = new ErrorCode(status2.getStatus() + 30000);
                HashMap<String, Object> errData = new HashMap<>();
                errData.put("portChargeStatus", portStatus.toJson());
                error.setData(errData);
                attach.put("error", error.toJson());
                attach.put(ChargeStopCondition.TYPE_TIME, String.valueOf(System.currentTimeMillis()));
                eventIndicate(EventDirective.EVENT_CHARGE_PAUSE, attach);
            } else {
                CHARGE_FSM_STATUS.pre_stop.equals(this.status);
            }
        } else if (DEVICE_STATUS.chargeFull.equals(status2) || DEVICE_STATUS.stopped.equals(status2)) {
            if (!DEVICE_STATUS.chargeFull.equals(status2)) {
                z = false;
            }
            handleChargeStopped(z, portStatus);
        } else {
            Log.w("ChargeHandler.handleSuspend", "ignore suspend event, port: " + this.port + ", charge status: " + this.status + ", suspend status: " + portStatus.toJson());
        }
    }

    private void handleErrorTolerance(PortStatus portStatus) {
        ArrayList<HashMap<String, Integer>> errorCnt = portStatus.getErrorCnt();
        boolean isToleranced = false;
        int errorCode = 200;
        int cnt = 0;
        if (errorCnt != null) {
            Iterator<HashMap<String, Integer>> it = errorCnt.iterator();
            while (true) {
                if (it.hasNext()) {
                    HashMap<String, Integer> error = it.next();
                    if (!error.containsKey("EmergencyStop")) {
                        if (!error.containsKey(PortStatus.KEY_ERROR_NO_GROUD)) {
                            if (!error.containsKey("OverVoltage")) {
                                if (!error.containsKey(PortStatus.KEY_ERROR_OVER_CURRENT)) {
                                    if (!error.containsKey(PortStatus.KEY_ERROR_OVER_TEMPERATURE)) {
                                        if (!error.containsKey(PortStatus.KEY_ERROR_LEAKAGE_CURRENT)) {
                                            if (error.containsKey(PortStatus.KEY_ERROR_CP_EXCEPT) && error.get(PortStatus.KEY_ERROR_CP_EXCEPT).intValue() >= 7) {
                                                isToleranced = true;
                                                errorCode = ErrorCode.EC_DEVICE_COMM_ERROR;
                                                cnt = error.get(PortStatus.KEY_ERROR_CP_EXCEPT).intValue();
                                                break;
                                            }
                                        } else if (error.get(PortStatus.KEY_ERROR_LEAKAGE_CURRENT).intValue() >= 1) {
                                            isToleranced = true;
                                            errorCode = ErrorCode.EC_DEVICE_POWER_LEAK;
                                            cnt = error.get(PortStatus.KEY_ERROR_LEAKAGE_CURRENT).intValue();
                                            break;
                                        }
                                    } else if (error.get(PortStatus.KEY_ERROR_OVER_TEMPERATURE).intValue() >= 3) {
                                        isToleranced = true;
                                        errorCode = ErrorCode.EC_DEVICE_TEMP_ERROR;
                                        cnt = error.get(PortStatus.KEY_ERROR_OVER_TEMPERATURE).intValue();
                                        break;
                                    }
                                } else if (error.get(PortStatus.KEY_ERROR_OVER_CURRENT).intValue() >= 3) {
                                    isToleranced = true;
                                    errorCode = ErrorCode.EC_DEVICE_AMP_ERROR;
                                    cnt = error.get(PortStatus.KEY_ERROR_OVER_CURRENT).intValue();
                                    break;
                                }
                            } else if (error.get("OverVoltage").intValue() >= 7) {
                                isToleranced = true;
                                errorCode = ErrorCode.EC_DEVICE_VOLT_ERROR;
                                cnt = error.get("OverVoltage").intValue();
                                break;
                            }
                        } else if (error.get(PortStatus.KEY_ERROR_NO_GROUD).intValue() >= 1) {
                            isToleranced = true;
                            errorCode = ErrorCode.EC_DEVICE_NO_GROUND;
                            cnt = error.get(PortStatus.KEY_ERROR_NO_GROUD).intValue();
                            break;
                        }
                    } else if (error.get("EmergencyStop").intValue() >= 7) {
                        isToleranced = true;
                        errorCode = ErrorCode.EC_DEVICE_EMERGENCY_STOP;
                        cnt = error.get("EmergencyStop").intValue();
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        ChargeSession chargeSession2 = getChargeSession();
        if (chargeSession2.isDeviceAuth() && isToleranced) {
            Bundle data = new Bundle();
            data.putString("stage", CHARGE_UI_STAGE.error_stop.getStage());
            data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession2.getChargeBill().getInit_type()));
            data.putInt("error", errorCode);
            data.putInt("cnt", cnt);
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
        }
    }

    private void handleStopAmmeter(boolean isPlugout) {
        ChargeSession chargeSession2 = getChargeSession();
        PortStatus fullPortChargeStatus = DeviceProxy.getInstance().getPortChargeStatus(this.port);
        if (fullPortChargeStatus == null) {
            Log.e("ChargeHandler.handleStopAmmeter", "failed to get info from driver, port: " + this.port + ", status: " + this.status.getStatus());
        } else if (fullPortChargeStatus.getPower() == null) {
            Log.e("ChargeHandler.handleStopAmmeter", "failed to get ammeter, port: " + this.port + ", status: " + this.status.getStatus() + ", data: " + fullPortChargeStatus.toJson());
        } else {
            double deltaPower = new BigDecimal(fullPortChargeStatus.getPower().doubleValue() - this.ammeter).setScale(2, 4).doubleValue();
            this.ammeter = fullPortChargeStatus.getPower().doubleValue();
            HardwareStatusCacheProvider.getInstance().updatePortAmmeter(this.port, this.ammeter);
            chargeSession2.getChargeBill().setStop_ammeter(this.ammeter);
            if (deltaPower > 0.0d) {
                double newPower = chargeSession2.getChargeBill().getTotal_power() + deltaPower;
                chargeSession2.getChargeBill().setTotal_power(newPower);
                calcPowerAndServiceFee(true, deltaPower);
                PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port);
                portStatus.setAmmeter(Double.valueOf(this.ammeter));
                portStatus.setPower(Double.valueOf(newPower));
                portStatus.setTotalFee(new BigDecimal((double) (((float) chargeSession2.getChargeBill().getTotal_fee()) / 100.0f)).setScale(2, 4).doubleValue());
                ChargeStatusCacheProvider.getInstance().updatePortStatus(this.port, portStatus);
            }
            LogUtils.applog("charge: " + chargeSession2.getChargeBill().getCharge_id() + (isPlugout ? " plugout" : " stopped") + ", start ammeter: " + String.format(BaseActivity.TWODP, new Object[]{Double.valueOf(this.startPower)}) + ", stop ammeter: " + String.format(BaseActivity.TWODP, new Object[]{Double.valueOf(this.ammeter)}) + ", total power: " + String.format(BaseActivity.TWODP, new Object[]{Double.valueOf(chargeSession2.getChargeBill().getTotal_power())}));
        }
    }

    /* access modifiers changed from: private */
    public void handleUpdate(PortStatus portStatus) {
        DEVICE_STATUS status2;
        PortStatus fullPortChargeStatus = portStatus;
        if (portStatus.getPower() == null || portStatus.getVolts().get(0) == null || portStatus.getAmps().get(1) == null) {
            fullPortChargeStatus = DeviceProxy.getInstance().getPortChargeStatus(this.port);
            if (fullPortChargeStatus == null) {
                Log.w("ChargeHandler.handleUpdate", "failed to get info from driver, but use: " + portStatus.toJson() + " to continue, port: " + this.port + ", status: " + this.status.getStatus());
                fullPortChargeStatus = portStatus;
            } else {
                fullPortChargeStatus.getAmps().set(0, portStatus.getAmps().get(0));
                fullPortChargeStatus.setChargeMode(portStatus.getChargeMode());
            }
        }
        if ((CHARGE_FSM_STATUS.paused.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status)) && (status2 = fullPortChargeStatus.getPortRuntimeStatus()) != null && DEVICE_STATUS.charging.getStatus() == status2.getStatus()) {
            handleChargeStarted(false, fullPortChargeStatus);
        }
        ChargeSession chargeSession2 = getChargeSession();
        chargeSession2.setDeviceStatus(fullPortChargeStatus.getPortRuntimeStatus());
        if (CHARGE_MODE.normal_charge.equals(fullPortChargeStatus.getChargeMode()) && !chargeSession2.isEnteredNormalCharging()) {
            chargeSession2.setEnteredNormalCharging(true);
            if (!LOCK_STATUS.disable.equals(ChargeStatusCacheProvider.getInstance().getPortLockStatus(this.port))) {
                DeviceProxy.getInstance().lockGun(this.port);
                ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.lock);
            }
        }
        if (CHARGE_FSM_STATUS.charging.equals(this.status)) {
            if (fullPortChargeStatus.getAmps().get(0) == null || fullPortChargeStatus.getAmps().get(0).doubleValue() <= 0.0d) {
                this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_OFFER;
            } else {
                this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_TRANSFER;
            }
            double deltaPower = new BigDecimal(fullPortChargeStatus.getPower().doubleValue() - this.ammeter).setScale(2, 4).doubleValue();
            this.ammeter = fullPortChargeStatus.getPower().doubleValue();
            if (deltaPower > 0.0d) {
                double newPower = chargeSession2.getChargeBill().getTotal_power() + deltaPower;
                fullPortChargeStatus.setPower(Double.valueOf(newPower));
                chargeSession2.getChargeBill().setTotal_power(newPower);
                calcPowerAndServiceFee(false, deltaPower);
            } else {
                fullPortChargeStatus.setPower(Double.valueOf(chargeSession2.getChargeBill().getTotal_power()));
            }
            if (USER_TC_TYPE.power.equals(chargeSession2.getChargeBill().getUser_tc_type())) {
                try {
                    double powerConditon = Double.parseDouble(chargeSession2.getChargeBill().getUser_tc_value());
                    if (chargeSession2.getChargeBill().getTotal_power() >= powerConditon) {
                        Log.w("ChargeHandler.handleUpdate", "power is more than user setted: " + powerConditon);
                        stopCharge();
                        chargeSession2.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.user_set);
                    }
                } catch (Exception e) {
                    Log.w("ChargeHandler.handleUpdate", Log.getStackTraceString(e));
                }
            }
        } else if (fullPortChargeStatus.getPower() != null) {
            this.ammeter = fullPortChargeStatus.getPower().doubleValue();
        }
        if (CHARGE_FSM_STATUS.idle.equals(this.status) || CHARGE_FSM_STATUS.auth_sended.equals(this.status) || CHARGE_FSM_STATUS.authed.equals(this.status) || CHARGE_FSM_STATUS.init_ack_sended.equals(this.status) || CHARGE_FSM_STATUS.init_advert.equals(this.status) || CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status) || CHARGE_FSM_STATUS.user_reserved.equals(this.status) || CHARGE_FSM_STATUS.plugin.equals(this.status) || CHARGE_FSM_STATUS.inited.equals(this.status) || CHARGE_FSM_STATUS.reserve_wait.equals(this.status)) {
            fullPortChargeStatus.setPower(Double.valueOf(0.0d));
        }
        updateChargeData(false, fullPortChargeStatus);
    }

    private void calcPowerAndServiceFee(boolean isStopAmmeter, double deltaPower) {
        ArrayList<HashMap<String, Object>> servicePriceSections;
        ChargeSession chargeSession2 = getChargeSession();
        FeeRate feeRate = chargeSession2.getChargeBill().getFee_rate();
        if (feeRate != null) {
            long nowTimestamp = System.currentTimeMillis();
            HashMap<String, Object> deltaPowerMeter = new HashMap<>();
            deltaPowerMeter.put("begin", Long.valueOf(chargeSession2.getLatestPowerMeterTimestamp()));
            deltaPowerMeter.put("end", Long.valueOf(nowTimestamp));
            deltaPowerMeter.put("meter", Double.valueOf(deltaPower));
            chargeSession2.setLatestPowerMeterTimestamp(nowTimestamp);
            ArrayList<HashMap<String, Object>> powerPriceSections = feeRate.getPowerPrice();
            if (powerPriceSections != null) {
                BillUtils.updateMeterSections(deltaPowerMeter, chargeSession2.getChargeBill().getPower_info(), powerPriceSections);
                chargeSession2.getChargeBill().setPower_fee(new BigDecimal(100.0d * BillUtils.calcIntervalCost(powerPriceSections, chargeSession2.getChargeBill().getPower_info())).setScale(0, 4).intValue());
            }
            if (SERVICE_PRICE_UNIT.degree.equals(feeRate.getServiceUnit()) && (servicePriceSections = feeRate.getServicePrice()) != null) {
                BillUtils.updateMeterSections(deltaPowerMeter, chargeSession2.getChargeBill().getService_info(), servicePriceSections);
                chargeSession2.getChargeBill().setService_fee(new BigDecimal(100.0d * BillUtils.calcIntervalCost(servicePriceSections, chargeSession2.getChargeBill().getService_info())).setScale(0, 4).intValue());
            }
            chargeSession2.getChargeBill().setTotal_fee(chargeSession2.getChargeBill().getPower_fee() + chargeSession2.getChargeBill().getService_fee() + chargeSession2.getChargeBill().getDelay_fee());
            if (!isStopAmmeter) {
                if (!NFC_CARD_TYPE.U2.equals(getNFCTypeFromUserType(chargeSession2.getChargeBill().getUser_type())) || chargeSession2.getChargeBill().getIs_free() == 1 || chargeSession2.getChargeBill().getUser_balance() - ((long) chargeSession2.getChargeBill().getTotal_fee()) >= 100) {
                    if (USER_TC_TYPE.fee.equals(chargeSession2.getChargeBill().getUser_tc_type())) {
                        try {
                            int feeConditon = Integer.parseInt(chargeSession2.getChargeBill().getUser_tc_value());
                            if (chargeSession2.getChargeBill().getTotal_fee() >= feeConditon) {
                                Log.w("ChargeHandler.calcPowerAneServiceFee", "fee is more than user setted: " + feeConditon);
                                stopCharge();
                                chargeSession2.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.user_set);
                            }
                        } catch (Exception e) {
                            Log.w("ChargeHandler.calcPowerAneServiceFee", Log.getStackTraceString(e));
                        }
                    }
                } else {
                    Log.w("ChargeHandler.calcPowerAneServiceFee", "if balanced for this charge, balance on card is less than 1 yuan !!!");
                    stopCharge();
                    chargeSession2.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.no_balance);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleResume(PortStatus portStatus) {
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
        ChargeSession chargeSession2 = getChargeSession();
        chargeSession2.setDeviceStatus(portStatus.getPortRuntimeStatus());
        DEVICE_STATUS status2 = portStatus.getPortRuntimeStatus();
        if (DEVICE_STATUS.notInited.getStatus() <= status2.getStatus()) {
            chargeSession2.setEmergencyStopped(false);
            chargeSession2.setAnyErrorExist(false);
        }
        updateChargeData(false, (PortStatus) null);
        if (DEVICE_STATUS.notInited.getStatus() <= status2.getStatus()) {
            if (CHARGE_FSM_STATUS.reserve_wait.equals(this.status)) {
                this.handlerTimer.startTimer(60000, MSG_TIMEOUT_RESERVE_WAIT, (Object) null);
                Bundle data = new Bundle();
                data.putString("stage", CHARGE_UI_STAGE.reserve.getStage());
                data.putBoolean("isClean", false);
                data.putInt("waitStart", 60);
                data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession2.getChargeBill().getInit_type()));
                data.putString("chargeId", chargeSession2.getChargeBill().getCharge_id());
                UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
            } else {
                CHARGE_FSM_STATUS.pre_stop.equals(this.status);
            }
            chargeSession2.setLatestResumedError(new ErrorCode(status2.getStatus() + 30000));
        } else if (DEVICE_STATUS.charging.getStatus() == status2.getStatus()) {
            handleChargeStarted(false, portStatus);
        } else {
            Log.w("ChargeHandler.handleResume", "ignore resume event, port: " + this.port + ", charge status: " + this.status + ", resume status: " + portStatus.toJson());
        }
    }

    private boolean isPlugoutCp(int cp) {
        if (cp == 12000) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void handleChargeStopEvent(PortStatus portStatus) {
        ChargeSession chargeSession2 = getChargeSession();
        chargeSession2.setDeviceStatus(portStatus.getPortRuntimeStatus());
        if (chargeSession2.isAnyErrorExist()) {
            this.handlerTimer.stopTimer(MSG_TIMEOUT_CHARGE_STOP_DELAY);
            handleChargePreStop(portStatus);
            return;
        }
        this.handlerTimer.stopTimer(MSG_TIMEOUT_CHARGE_STOP_DELAY);
        this.handlerTimer.startTimer(1000, MSG_TIMEOUT_CHARGE_STOP_DELAY, portStatus);
    }

    /* access modifiers changed from: private */
    public void handleChargePreStop(PortStatus portStatus) {
        ChargeSession chargeSession2 = getChargeSession();
        chargeSession2.setDeviceStatus(portStatus.getPortRuntimeStatus());
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
        if (CHARGE_FSM_STATUS.charging.equals(this.status)) {
            if (!LOCK_STATUS.disable.equals(ChargeStatusCacheProvider.getInstance().getPortLockStatus(this.port))) {
                DeviceProxy.getInstance().unlockGun(this.port);
                ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.unlock);
            }
            chargeSession2.setEnteredNormalCharging(false);
            this.status = CHARGE_FSM_STATUS.pre_stop;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_OFFER_SUSPEND;
            updateChargeData(false, (PortStatus) null);
            HashMap<String, Object> attach = new HashMap<>();
            attach.put("error", new ErrorCode(ErrorCode.EC_CAR_STOP_CHARGE).toJson());
            attach.put(ChargeStopCondition.TYPE_TIME, String.valueOf(System.currentTimeMillis()));
            eventIndicate(EventDirective.EVENT_CHARGE_PAUSE, attach);
        } else if (CHARGE_FSM_STATUS.stop_sended.equals(this.status)) {
            handleChargeStopped(false, portStatus);
        } else if (CHARGE_FSM_STATUS.pre_stop.equals(this.status)) {
            updateChargeData(false, (PortStatus) null);
            chargeSession2.isAnyErrorExist();
        } else if (CHARGE_FSM_STATUS.paused.equals(this.status)) {
            this.status = CHARGE_FSM_STATUS.pre_stop;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_OFFER_SUSPEND;
            updateChargeData(false, (PortStatus) null);
            chargeSession2.isAnyErrorExist();
        } else {
            Log.w("ChargeHandler.handleChargePreStop", "ignore charge stop event, port: " + this.port + ", status: " + this.status.getStatus());
        }
    }

    private void handleChargeStopped(boolean isFull, PortStatus portStatus) {
        HashMap<String, Object> delayPriceSection;
        Double price;
        ChargeSession chargeSession2 = getChargeSession();
        chargeSession2.setDeviceStatus(portStatus.getPortRuntimeStatus());
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
        this.handlerTimer.stopTimer(MSG_INTERVAL_CONDITION_TIMING);
        if (CHARGE_FSM_STATUS.stop_sended.equals(this.status) || CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status)) {
            stopTimer(this.status);
            boolean isUserStopped = false;
            if (CHARGE_FSM_STATUS.stop_sended.equals(this.status)) {
                isUserStopped = true;
            } else if (!isFull && chargeSession2.getChargeBill().getStop_cause() == null) {
                chargeSession2.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.car);
            }
            chargeSession2.getChargeBill().setStop_time(System.currentTimeMillis());
            chargeSession2.getChargeBill().setTotal_time(new BigDecimal((chargeSession2.getChargeBill().getStop_time() - chargeSession2.getChargeBill().getStart_time()) / 1000).setScale(0, 4).intValue());
            handleStopAmmeter(false);
            this.status = CHARGE_FSM_STATUS.stopped;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.TRANSACTION_ENDED;
            updateChargeData(true, (PortStatus) null);
            if (isUserStopped) {
                stopConfirm();
            } else {
                eventIndicate(EventDirective.EVENT_CHARGE_STOP, (HashMap<String, Object>) null);
            }
            NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeSession2.getChargeBill().getUser_type());
            LOCK_STATUS lockStatus = ChargeStatusCacheProvider.getInstance().getPortLockStatus(this.port);
            GUN_LOCK_MODE gunMode = chargeSession2.getGunMode();
            if (GUN_LOCK_MODE.unlock_before_pay.equals(gunMode)) {
                if (!LOCK_STATUS.disable.equals(lockStatus)) {
                    DeviceProxy.getInstance().unlockGun(this.port);
                    ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.unlock);
                }
            } else if (GUN_LOCK_MODE.unlock_after_pay.equals(gunMode)) {
                if ((!NFC_CARD_TYPE.U2.equals(nfcCardType) || chargeSession2.getChargeBill().getTotal_fee() <= 0 || chargeSession2.getChargeBill().getIs_free() == 1) && !LOCK_STATUS.disable.equals(lockStatus)) {
                    DeviceProxy.getInstance().unlockGun(this.port);
                    ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.unlock);
                }
            } else if (GUN_LOCK_MODE.auto.equals(gunMode) && !LOCK_STATUS.disable.equals(lockStatus)) {
                DeviceProxy.getInstance().unlockGun(this.port);
                ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.unlock);
            }
            long nowTime = System.currentTimeMillis();
            double nowDelayPrice = getDelayPrice(nowTime);
            boolean willDelayHandleNow = nowDelayPrice > 0.0d;
            if (willDelayHandleNow) {
                chargeSession2.setDelayWaitStarted(true);
                eventIndicate(EventDirective.EVENT_DEALY_WAIT_START, (HashMap<String, Object>) null);
                chargeSession2.setDelayPrice(nowDelayPrice);
                updateChargeData(false, (PortStatus) null);
            }
            Bundle data = new Bundle();
            data.putString("stage", CHARGE_UI_STAGE.stopped.getStage());
            data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession2.getChargeBill().getInit_type()));
            data.putString("chargeId", chargeSession2.getChargeBill().getCharge_id());
            data.putBoolean("willDelayHandleNow", willDelayHandleNow);
            int timeout = chargeSession2.getTimeout_plugout();
            if (timeout > 0) {
                if (!willDelayHandleNow && (delayPriceSection = getDelayPriceSection(nowTime + ((long) (timeout * 1000)))) != null && (price = (Double) delayPriceSection.get("price")) != null && price.doubleValue() > 0.0d) {
                    long beginTs = TimeUtils.getDataTime(nowTime, (String) delayPriceSection.get("begin"));
                    if (beginTs < nowTime) {
                        beginTs += DateUtils.MILLIS_PER_DAY;
                    }
                    timeout = (int) (((beginTs - nowTime) / 1000) & XMSZHead.ID_BROADCAST);
                }
                data.putInt("waitPlugout", timeout);
                this.handlerTimer.startTimer((long) (timeout * 1000), MSG_TIMEOUT_STOPPED, (Object) null);
            }
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
            return;
        }
        Log.w("ChargeHandler.handleChargeStopped", "ignore charge stop event, port: " + this.port + ", status: " + this.status.getStatus());
    }

    /* access modifiers changed from: private */
    public void handleDelayStarted() {
        ChargeSession chargeSession2 = getChargeSession();
        chargeSession2.getChargeBill().setDelay_start(System.currentTimeMillis());
        FeeRate feeRate = chargeSession2.getChargeBill().getFee_rate();
        if (!(feeRate == null || feeRate.getDelayPrice() == null)) {
            chargeSession2.setLatestDelayMeterTimestamp(chargeSession2.getChargeBill().getDelay_start());
            chargeSession2.getChargeBill().setDelay_info(new ArrayList());
            chargeSession2.setDelayPrice(getDelayPrice(chargeSession2.getChargeBill().getDelay_start()));
        }
        updateChargeData(false, (PortStatus) null);
        eventIndicate("delay_start", (HashMap<String, Object>) null);
        Bundle data = new Bundle();
        data.putString("stage", CHARGE_UI_STAGE.delay.getStage());
        data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession2.getChargeBill().getInit_type()));
        data.putString("chargeId", chargeSession2.getChargeBill().getCharge_id());
        UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
        this.handler.sendEmptyMessageDelayed(MSG_INTERVAL_DELAY_TIMING, 1000);
    }

    /* access modifiers changed from: private */
    public void handleConditionTiming() {
        ChargeSession chargeSession2 = getChargeSession();
        if (!USER_TC_TYPE.time.equals(chargeSession2.getChargeBill().getUser_tc_type())) {
            return;
        }
        if (CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.paused.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status)) {
            try {
                if (CHARGE_FSM_STATUS.charging.equals(this.status) || (CHARGE_FSM_STATUS.pre_stop.equals(this.status) && !chargeSession2.isAnyErrorExist())) {
                    long conditionTime = (long) ((Integer.parseInt(chargeSession2.getChargeBill().getUser_tc_value()) & -1) * 1000);
                    if (System.currentTimeMillis() - chargeSession2.getChargeBill().getStart_time() >= conditionTime) {
                        Log.w("ChargeHandler.handleConditionTiming", "time is more than user setted: " + (conditionTime / 1000));
                        stopCharge();
                        chargeSession2.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.user_set);
                        this.handlerTimer.stopTimer(MSG_INTERVAL_CONDITION_TIMING);
                        return;
                    }
                }
            } catch (Exception e) {
                Log.w("ChargeHandler.handleConditionTiming", Log.getStackTraceString(e));
            }
            this.handlerTimer.startTimer(1000, MSG_INTERVAL_CONDITION_TIMING, (Object) null);
        }
    }

    /* access modifiers changed from: private */
    public void handleDelayTiming() {
        ChargeSession chargeSession2 = getChargeSession();
        if (!chargeSession2.isDelayWaitStarted()) {
            double nowDelayPrice = getDelayPrice(System.currentTimeMillis());
            if (nowDelayPrice > 0.0d) {
                this.handlerTimer.stopTimer(MSG_INTERVAL_DELAY_TIMING);
                eventIndicate(EventDirective.EVENT_DEALY_WAIT_START, (HashMap<String, Object>) null);
                chargeSession2.setDelayWaitStarted(true);
                int waitPlugoutTimeout = chargeSession2.getTimeout_plugout();
                this.handlerTimer.startTimer((long) (waitPlugoutTimeout * 1000), MSG_TIMEOUT_STOPPED, (Object) null);
                chargeSession2.setDelayPrice(nowDelayPrice);
                updateChargeData(false, (PortStatus) null);
                Bundle data = new Bundle();
                data.putString("stage", CHARGE_UI_STAGE.delay_wait.getStage());
                data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession2.getChargeBill().getInit_type()));
                data.putString("chargeId", chargeSession2.getChargeBill().getCharge_id());
                data.putInt("waitPlugout", waitPlugoutTimeout);
                UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
            }
        } else if (chargeSession2.isDelayStarted()) {
            calcDelayFee(false);
            if (chargeSession2.getChargeBill().getTotal_delay() % 6 == 0) {
                updateChargeData(false, (PortStatus) null);
            }
        }
    }

    private void calcDelayFee(boolean isEnd) {
        long j;
        try {
            ChargeSession chargeSession2 = getChargeSession();
            long nowTimestamp = System.currentTimeMillis();
            long delayTime = nowTimestamp - chargeSession2.getChargeBill().getDelay_start();
            if (isEnd && chargeSession2.getChargeBill().getFin_time() > 0) {
                delayTime = chargeSession2.getChargeBill().getFin_time() - chargeSession2.getChargeBill().getDelay_start();
            }
            chargeSession2.getChargeBill().setTotal_delay(new BigDecimal((double) (((float) delayTime) / 1000.0f)).setScale(0, 3).intValue());
            FeeRate feeRate = chargeSession2.getChargeBill().getFee_rate();
            if (feeRate != null && DELAY_PRICE_UNIT.minute.equals(feeRate.getDelayUnit()) && feeRate.getDelayPrice() != null) {
                if ((chargeSession2.getChargeBill().getTotal_delay() > 0 && isEnd) || (chargeSession2.getChargeBill().getTotal_delay() > 0 && chargeSession2.getChargeBill().getTotal_delay() % 60 == 0)) {
                    int latestCostDelay = chargeSession2.getLatestCostDelay();
                    int minutes = new BigDecimal((double) (((float) (chargeSession2.getChargeBill().getTotal_delay() - latestCostDelay)) / 60.0f)).setScale(0, 3).intValue();
                    if (isEnd) {
                        minutes = new BigDecimal((double) (((float) (chargeSession2.getChargeBill().getTotal_delay() - latestCostDelay)) / 60.0f)).setScale(0, 4).intValue();
                    }
                    for (int i = 0; i < minutes; i++) {
                        HashMap<String, Object> deltaDelayMeter = new HashMap<>();
                        deltaDelayMeter.put("begin", Long.valueOf(chargeSession2.getLatestDelayMeterTimestamp() + ((long) (i * 60 * 1000))));
                        deltaDelayMeter.put("end", Long.valueOf(chargeSession2.getLatestDelayMeterTimestamp() + ((long) ((i + 1) * 60 * 1000))));
                        if (isEnd && i == minutes - 1) {
                            if (chargeSession2.getChargeBill().getFin_time() > 0) {
                                j = chargeSession2.getChargeBill().getFin_time();
                            } else {
                                j = nowTimestamp;
                            }
                            deltaDelayMeter.put("end", Long.valueOf(j));
                        }
                        deltaDelayMeter.put("meter", Double.valueOf(1.0d));
                        BillUtils.updateMeterSections(deltaDelayMeter, chargeSession2.getChargeBill().getDelay_info(), feeRate.getDelayPrice());
                    }
                    if (minutes > 0) {
                        chargeSession2.setLatestDelayMeterTimestamp(nowTimestamp);
                        chargeSession2.setLatestCostDelay(chargeSession2.getChargeBill().getTotal_delay());
                        chargeSession2.getChargeBill().setDelay_fee(new BigDecimal(100.0d * BillUtils.calcIntervalCost(feeRate.getDelayPrice(), chargeSession2.getChargeBill().getDelay_info())).setScale(0, 4).intValue());
                        chargeSession2.getChargeBill().setTotal_fee(chargeSession2.getChargeBill().getPower_fee() + chargeSession2.getChargeBill().getService_fee() + chargeSession2.getChargeBill().getDelay_fee());
                        chargeSession2.setDelayPrice(getDelayPrice(nowTimestamp));
                    }
                }
            }
        } catch (Exception e) {
            Log.e("ChargeHandler.calcDelayFee", Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
    public void checkPlugin() {
        ChargeSession chargeSession2 = getChargeSession();
        if (!chargeSession2.isDeviceAuth()) {
            Port runtimePortChargeStatus = DeviceProxy.getInstance().getPortRuntimeStatus(this.port);
            if (isPlugoutCp(runtimePortChargeStatus.getCpVoltage().intValue())) {
                HardwareStatusCacheProvider.getInstance().updatePortPluginStatus(this.port, false);
            } else {
                HardwareStatusCacheProvider.getInstance().updatePortPluginStatus(this.port, true);
                if (CHARGE_FSM_STATUS.init_advert.equals(this.status) || CHARGE_FSM_STATUS.user_reserved.equals(this.status) || CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status) || CHARGE_FSM_STATUS.inited.equals(this.status)) {
                    this.ocppChargeStatus = OCPP_CHARGE_STATUS.TRANSACTION_STARTED;
                }
            }
            DeviceProxy.getInstance().notifyPortStatusUpdatedByCmd(runtimePortChargeStatus);
            DEVICE_STATUS status2 = runtimePortChargeStatus.getPortRuntimeStatus();
            if (chargeSession2.getDeviceStatus() != null && chargeSession2.getDeviceStatus().getStatus() != status2.getStatus() && chargeSession2.getDeviceStatus().getStatus() >= DEVICE_STATUS.notInited.getStatus() && status2.getStatus() < DEVICE_STATUS.notInited.getStatus()) {
                Log.w("ChargeHandler.checkPlugin", "error: " + chargeSession2.getDeviceStatus().getStatus() + " -> normal: " + status2.getStatus());
                chargeSession2.setDeviceStatus(status2);
                chargeSession2.setEmergencyStopped(false);
                chargeSession2.setAnyErrorExist(false);
                PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port);
                portStatus.setPortRuntimeStatus(status2);
                portStatus.setEmergencyStopStatus(SWITCH_STATUS.off);
                ChargeStatusCacheProvider.getInstance().updatePortStatus(this.port, portStatus);
            }
        }
    }

    private FeeRate agreeChargeFeeRate(CHARGE_INIT_TYPE initType, NFC_CARD_TYPE nfcType, String port2, String expected) {
        HashMap<String, PortFeeRate> portsFeeRate;
        String localFeeRate = null;
        PortFeeRate LocalportFeeRate = null;
        if (!initType.equals(CHARGE_INIT_TYPE.nfc) || !NFC_CARD_TYPE.U2.equals(nfcType)) {
            LocalportFeeRate = RemoteSettingCacheProvider.getInstance().getPortFeeRate(port2);
            if (LocalportFeeRate != null) {
                localFeeRate = LocalportFeeRate.getActiveFeeRateId();
            }
        } else {
            FeeRateSetting feeRateSetting = LocalSettingCacheProvider.getInstance().getFeeRateSetting();
            if (!(feeRateSetting == null || (portsFeeRate = feeRateSetting.getPortsFeeRate()) == null || (LocalportFeeRate = portsFeeRate.get(port2)) == null)) {
                localFeeRate = LocalportFeeRate.getActiveFeeRateId();
            }
        }
        String feeRate = expected;
        if (TextUtils.isEmpty(feeRate)) {
            feeRate = localFeeRate;
        } else if (!feeRate.equals(localFeeRate)) {
            if (LocalportFeeRate == null) {
                feeRate = null;
            } else if (LocalportFeeRate.getFeeRates() == null) {
                feeRate = null;
            } else if (!LocalportFeeRate.getFeeRates().containsKey(feeRate)) {
                feeRate = null;
            }
            TextUtils.isEmpty(feeRate);
        }
        if (!TextUtils.isEmpty(feeRate)) {
            return LocalportFeeRate.getFeeRates().get(feeRate);
        }
        return null;
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

    private void authIndicate(CHARGE_PLATFORM platform, HashMap<String, Object> userData) {
        AuthDirective auth = new AuthDirective();
        ChargeSession chargeSession2 = getChargeSession();
        auth.setInit_type(chargeSession2.getChargeBill().getInit_type());
        auth.setUser_type(chargeSession2.getChargeBill().getUser_type());
        auth.setUser_code(chargeSession2.getChargeBill().getUser_code());
        auth.setDevice_id(chargeSession2.getDevice_id());
        auth.setPort(chargeSession2.getChargeBill().getPort());
        auth.setUser_data(userData);
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setAuth_id("init");
        opt.setPort_id(chargeSession2.getChargeBill().getPort());
        DCAPMessage indicate = ChargeController.createIndicate("server:" + platform.getPlatform(), "auth", opt, auth);
        chargeSession2.setExpected_resopnse(Long.valueOf(indicate.getSeq()));
        DCAPProxy.getInstance().sendIndicate(indicate);
        this.status = CHARGE_FSM_STATUS.auth_sended;
        this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_STARTED;
        this.handlerTimer.startTimer(10000, MSG_TIMEOUT_AUTH_SENDED, chargeSession2.getConfirm4Auth());
    }

    private boolean initAckIndicate(long ackInitRequestSeq) {
        InitAckDirective initAck = new InitAckDirective();
        ChargeSession chargeSession2 = getChargeSession();
        String userType = chargeSession2.getChargeBill().getUser_type();
        String userCode = chargeSession2.getChargeBill().getUser_code();
        initAck.setUser_type(userType);
        initAck.setUser_code(userCode);
        initAck.setDevice_id(chargeSession2.getDevice_id());
        initAck.setPort(chargeSession2.getChargeBill().getPort());
        initAck.setFee_rate(chargeSession2.getChargeBill().getFee_rate_id());
        initAck.setUser_tc_type(chargeSession2.getChargeBill().getUser_tc_type());
        initAck.setUser_tc_value(chargeSession2.getChargeBill().getUser_tc_value());
        initAck.setUser_balance(chargeSession2.getChargeBill().getUser_balance());
        initAck.setIs_free(chargeSession2.getChargeBill().getIs_free());
        initAck.setBinded_user(chargeSession2.getChargeBill().getBinded_user());
        initAck.setCharge_platform(chargeSession2.getChargeBill().getCharge_platform());
        initAck.setTimeout_plugin(chargeSession2.getTimeout_plugin());
        initAck.setTimeout_start(chargeSession2.getTimeout_start());
        initAck.setTimeout_plugout(chargeSession2.getTimeout_plugout());
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(getChargeSession().getChargeBill().getCharge_id());
        opt.setOp("init");
        opt.setSeq(Long.valueOf(ackInitRequestSeq));
        DCAPMessage indicate = ChargeController.createIndicate("user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode, CAPMessage.DIRECTIVE_INIT_ACK, opt, initAck);
        chargeSession2.setExpected_resopnse(Long.valueOf(indicate.getSeq()));
        return DCAPProxy.getInstance().sendIndicate(indicate);
    }

    private boolean eventIndicate(String eventId, HashMap<String, Object> attach) {
        ChargeSession chargeSession2 = getChargeSession();
        EventDirective event = new EventDirective();
        event.setCharge_status(getChargeStatus(this.status));
        event.setStart_time(chargeSession2.getChargeBill().getStart_time());
        event.setStop_time(chargeSession2.getChargeBill().getStop_time());
        event.setTotal_power(chargeSession2.getChargeBill().getTotal_power());
        event.setDelay_start(chargeSession2.getChargeBill().getDelay_start());
        event.setTotal_delay(chargeSession2.getChargeBill().getTotal_delay());
        event.setDelay_fee(chargeSession2.getChargeBill().getDelay_fee());
        event.setAttach(attach);
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(chargeSession2.getChargeBill().getCharge_id());
        opt.setEvent_id(eventId);
        return DCAPProxy.getInstance().sendIndicate(ChargeController.createIndicate("user:" + chargeSession2.getChargeBill().getUser_type() + MqttTopic.TOPIC_LEVEL_SEPARATOR + chargeSession2.getChargeBill().getUser_code(), "event", opt, event));
    }

    private boolean stopConfirm() {
        ChargeSession chargeSession2 = getChargeSession();
        if (chargeSession2.getStop_request_seq() == null) {
            return false;
        }
        StopDirective stop = new StopDirective();
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(chargeSession2.getChargeBill().getCharge_id());
        opt.setOp("stop");
        opt.setSeq(chargeSession2.getStop_request_seq());
        boolean isOk = DCAPProxy.getInstance().sendConfirm(createConfirmBySession("ack", opt, stop));
        chargeSession2.setStop_request_seq((Long) null);
        return isOk;
    }

    /* access modifiers changed from: private */
    public void startCharge() {
        DeviceProxy.getInstance().startCharge(this.port);
    }

    /* access modifiers changed from: private */
    public void stopCharge() {
        DeviceProxy.getInstance().stopCharge(this.port);
    }

    private void refuseInit(FIN_MODE mode, ErrorCode error, String userType, String userCode, String chargeId, Long nackInitRequestSeq) {
        FinDirective fin = new FinDirective();
        fin.setFin_mode(mode);
        fin.setError(error);
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(chargeId);
        if (nackInitRequestSeq != null) {
            opt.setOp("init");
            opt.setSeq(nackInitRequestSeq);
        }
        DCAPProxy.getInstance().sendIndicate(ChargeController.createIndicate("user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode, "fin", opt, fin));
        String refuseCause = null;
        switch ($SWITCH_TABLE$com$xcharge$charger$core$type$FIN_MODE()[mode.ordinal()]) {
            case 4:
                refuseCause = CHARGE_REFUSE_CAUSE.PORT_FORBIDEN.getCause();
                break;
            case 5:
                refuseCause = CHARGE_REFUSE_CAUSE.BUSY.getCause();
                break;
            case 6:
                refuseCause = CHARGE_REFUSE_CAUSE.NO_FEERATE.getCause();
                break;
        }
        if (!TextUtils.isEmpty(refuseCause)) {
            Bundle data = new Bundle();
            data.putString("stage", CHARGE_UI_STAGE.refuse.getStage());
            data.putString("cause", refuseCause);
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
        }
    }

    /* access modifiers changed from: private */
    public void finIndicate(FIN_MODE mode, ErrorCode error, String userType, String userCode, String chargeId, Long nackInitRequestSeq) {
        FinDirective fin = new FinDirective();
        fin.setFin_mode(mode);
        fin.setError(error);
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(chargeId);
        if (nackInitRequestSeq != null) {
            opt.setOp("init");
            opt.setSeq(nackInitRequestSeq);
        }
        DCAPMessage indicate = ChargeController.createIndicate("user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode, "fin", opt, fin);
        DCAPProxy.getInstance().sendIndicate(indicate);
        getChargeSession().setExpected_resopnse(Long.valueOf(indicate.getSeq()));
        this.status = CHARGE_FSM_STATUS.fin_sended;
        this.handlerTimer.startTimer(10000, MSG_TIMEOUT_FIN_SENDED, (Object) null);
    }

    /* access modifiers changed from: private */
    public void updateChargeData(boolean isStopAmmeter, PortStatus status2) {
        ChargeSession chargeSession2 = getChargeSession();
        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port);
        portStatus.setCharge_id(chargeSession2.getChargeBill().getCharge_id());
        portStatus.setChargeStatus(getChargeStatus(this.status));
        portStatus.setOcppChargeStatus(this.ocppChargeStatus);
        portStatus.setAmmeter(Double.valueOf(this.ammeter));
        portStatus.setTotalFee(new BigDecimal((double) (((float) chargeSession2.getChargeBill().getTotal_fee()) / 100.0f)).setScale(2, 4).doubleValue());
        if (status2 == null) {
            if (chargeSession2.getDeviceStatus() != null) {
                portStatus.setPortRuntimeStatus(chargeSession2.getDeviceStatus());
            }
            portStatus.setEmergencyStopStatus(chargeSession2.isEmergencyStopped() ? SWITCH_STATUS.on : SWITCH_STATUS.off);
            portStatus.setPlugin(HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port));
            portStatus.setWaitPluginTimeout(chargeSession2.getTimeout_plugin());
            portStatus.setWaitPlugoutTimeout(chargeSession2.getTimeout_plugout());
            portStatus.setPower(Double.valueOf(chargeSession2.getChargeBill().getTotal_power()));
            portStatus.setDelayPrice(chargeSession2.getDelayPrice());
            portStatus.setDelayStartTime(chargeSession2.getChargeBill().getDelay_start());
            portStatus.setTotalDelayFee(chargeSession2.getChargeBill().getDelay_fee());
            portStatus.setChargeStartTime(chargeSession2.getChargeBill().getStart_time());
            portStatus.setChargeStopTime(chargeSession2.getChargeBill().getStop_time());
            portStatus.setChargeStopCause(chargeSession2.getChargeBill().getStop_cause());
        } else {
            portStatus.setPortRuntimeStatus(status2.getPortRuntimeStatus());
            portStatus.setAmps(status2.getAmps());
            portStatus.setVolts(status2.getVolts());
            portStatus.setKwatt(status2.getKwatt());
            portStatus.setTemprature(status2.getTemprature());
            portStatus.setCp(status2.getCp());
            if (status2.getErrorCnt() != null) {
                portStatus.setErrorCnt(status2.getErrorCnt());
            }
            portStatus.setPower(status2.getPower());
            portStatus.setChargeMode(status2.getChargeMode());
        }
        if (CHARGE_FSM_STATUS.paused.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status)) {
            PortStatus fullPortChargeStatus = DeviceProxy.getInstance().getPortChargeStatus(this.port);
            portStatus.setPortRuntimeStatus(fullPortChargeStatus.getPortRuntimeStatus());
            portStatus.setAmps(fullPortChargeStatus.getAmps());
            portStatus.setVolts(fullPortChargeStatus.getVolts());
            portStatus.setKwatt(Double.valueOf(0.0d));
            portStatus.setChargeMode(CHARGE_MODE.paused);
        }
        if (isStopAmmeter || CHARGE_FSM_STATUS.idle.equals(this.status) || CHARGE_FSM_STATUS.init_advert.equals(this.status) || CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status) || CHARGE_FSM_STATUS.user_reserved.equals(this.status) || CHARGE_FSM_STATUS.inited.equals(this.status) || CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.stopped.equals(this.status) || CHARGE_FSM_STATUS.fin_sended.equals(this.status)) {
            ChargeBill chargeBill = chargeSession2.getChargeBill();
            if (chargeBill.getInit_time() > 0) {
                if (chargeBill.getStart_time() > 0) {
                    chargeBill.setTotal_time(new BigDecimal((System.currentTimeMillis() - chargeBill.getStart_time()) / 1000).setScale(0, 4).intValue());
                }
                Log.i("ChargeHandler.updateChargeData", String.valueOf(this.status.getStatus()) + " save charge and bill info: " + chargeBill.toJson());
                ChargeContentProxy.getInstance().saveChargeBill(chargeBill);
            } else if (CHARGE_FSM_STATUS.init_advert.equals(this.status) || CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status) || CHARGE_FSM_STATUS.user_reserved.equals(this.status)) {
                Log.i("ChargeHandler.updateChargeData", String.valueOf(this.status.getStatus()) + " save charge and bill info: " + chargeBill.toJson());
                ChargeContentProxy.getInstance().saveChargeBill(chargeBill);
            } else if (CHARGE_FSM_STATUS.idle.equals(this.status) && !TextUtils.isEmpty(chargeBill.getCharge_id())) {
                Log.i("ChargeHandler.updateChargeData", String.valueOf(this.status.getStatus()) + " save charge and bill info: " + chargeBill.toJson());
                ChargeContentProxy.getInstance().saveChargeBill(chargeBill);
            }
        }
        ChargeStatusCacheProvider.getInstance().updatePortStatus(this.port, portStatus);
        if (CHARGE_FSM_STATUS.idle.equals(this.status)) {
            clearChargeStatusCache();
        }
    }

    private CHARGE_STATUS getChargeStatus(CHARGE_FSM_STATUS status2) {
        switch ($SWITCH_TABLE$com$xcharge$charger$core$type$CHARGE_FSM_STATUS()[status2.ordinal()]) {
            case 1:
                return CHARGE_STATUS.IDLE;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                return CHARGE_STATUS.CHARGE_START_WAITTING;
            case PortRuntimeData.STATUS_EX_11:
            case PortRuntimeData.STATUS_EX_12:
            case 13:
            case 14:
                return CHARGE_STATUS.CHARGING;
            case 15:
            case 16:
                return CHARGE_STATUS.CHARGE_STOP_WAITTING;
            default:
                return CHARGE_STATUS.IDLE;
        }
    }

    private void requestPortChargeAuth() {
        ChargeSession chargeSession2 = getChargeSession();
        if (!chargeSession2.isDeviceAuth()) {
            if (chargeSession2.getChargeBill().getInit_type().equals(CHARGE_INIT_TYPE.nfc)) {
                DeviceProxy.getInstance().authValid(this.port, getNFCTypeFromUserType(chargeSession2.getChargeBill().getUser_type()).getType(), chargeSession2.getChargeBill().getUser_code());
            } else {
                DeviceProxy.getInstance().authValid(this.port, chargeSession2.getChargeBill().getUser_type(), chargeSession2.getChargeBill().getUser_code());
            }
            if (!LOCK_STATUS.disable.equals(ChargeStatusCacheProvider.getInstance().getPortLockStatus(this.port))) {
                DeviceProxy.getInstance().unlockGun(this.port);
                ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.unlock);
            }
            chargeSession2.setDeviceAuth(true);
        }
    }

    /* access modifiers changed from: private */
    public void cancelPortChargeAuth() {
        ChargeSession chargeSession2 = getChargeSession();
        if (chargeSession2.isDeviceAuth()) {
            GUN_LOCK_MODE gunMode = chargeSession2.getGunMode();
            NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeSession2.getChargeBill().getUser_type());
            if (!NFC_CARD_TYPE.U2.equals(nfcCardType) || chargeSession2.getChargeBill().getTotal_fee() <= 0 || chargeSession2.getChargeBill().getIs_free() == 1 || !GUN_LOCK_MODE.unlock_after_pay.equals(gunMode)) {
                if (!LOCK_STATUS.disable.equals(ChargeStatusCacheProvider.getInstance().getPortLockStatus(this.port))) {
                    DeviceProxy.getInstance().unlockGun(this.port);
                    ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.unlock);
                }
            }
            if (chargeSession2.getChargeBill().getInit_type().equals(CHARGE_INIT_TYPE.nfc)) {
                DeviceProxy.getInstance().authInValid(this.port, nfcCardType.getType(), chargeSession2.getChargeBill().getUser_code());
            } else {
                DeviceProxy.getInstance().authInValid(this.port, chargeSession2.getChargeBill().getUser_type(), chargeSession2.getChargeBill().getUser_code());
            }
            chargeSession2.setDeviceAuth(false);
            chargeSession2.getChargeBill().setFin_time(System.currentTimeMillis());
            chargeSession2.getChargeBill().setBalance_flag(1);
            chargeSession2.setDeviceStatus(DEVICE_STATUS.idle);
        }
    }

    /* access modifiers changed from: private */
    public void notifyChargeSessionFinished() {
        ChargeSession chargeSession2 = getChargeSession();
        if (chargeSession2.getChargeBill().getStop_time() > 0) {
            Bundle data = new Bundle();
            data.putString("stage", CHARGE_UI_STAGE.billed.getStage());
            data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession2.getChargeBill().getInit_type()));
            data.putString("chargeId", chargeSession2.getChargeBill().getCharge_id());
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
            return;
        }
        Bundle data2 = new Bundle();
        data2.putString("stage", CHARGE_UI_STAGE.ready.getStage());
        UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data2);
    }

    public DCAPMessage createConfirmBySession(String op, CAPDirectiveOption opt, Object data) {
        DCAPMessage confirm = new DCAPMessage();
        CAPMessage cap = new CAPMessage();
        ChargeSession chargeSession2 = getChargeSession();
        confirm.setFrom("device:" + chargeSession2.getDevice_id());
        confirm.setTo("user:" + chargeSession2.getChargeBill().getUser_type() + MqttTopic.TOPIC_LEVEL_SEPARATOR + chargeSession2.getChargeBill().getUser_code());
        confirm.setType("cap");
        confirm.setCtime(System.currentTimeMillis());
        confirm.setSeq(Sequence.getCoreDCAPSequence());
        cap.setOp(op);
        cap.setOpt(opt);
        cap.setData(data);
        confirm.setData(cap);
        return confirm;
    }

    private void stopTimer(CHARGE_FSM_STATUS status2) {
        switch ($SWITCH_TABLE$com$xcharge$charger$core$type$CHARGE_FSM_STATUS()[status2.ordinal()]) {
            case 2:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_AUTH_SENDED);
                return;
            case 3:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_AUTHED);
                return;
            case 4:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_INIT_ACK_SENDED);
                return;
            case 5:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_INIT_ADVERT);
                return;
            case 6:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_USER_RESERVED);
                return;
            case 7:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_USER_RESERVE_WAIT_PLUGIN);
                return;
            case 8:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_INITED);
                return;
            case 9:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGIN);
                return;
            case 10:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_RESERVE_WAIT);
                return;
            case PortRuntimeData.STATUS_EX_12:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_PRE_STOP);
                return;
            case 14:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_STOP_SENDED);
                return;
            case 15:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_STOPPED);
                return;
            case 16:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_FIN_SENDED);
                return;
            default:
                return;
        }
    }

    private void clearChargeStatusCache() {
        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port);
        portStatus.setCharge_id((String) null);
        portStatus.setChargeMode((CHARGE_MODE) null);
        portStatus.setChargeStartTime(0);
        portStatus.setChargeStopTime(0);
        portStatus.setChargeStopCause((CHARGE_STOP_CAUSE) null);
        portStatus.setChargeStatus(CHARGE_STATUS.IDLE);
        portStatus.setOcppChargeStatus(OCPP_CHARGE_STATUS.SESSION_ENDED);
        portStatus.setKwatt((Double) null);
        portStatus.setTemprature((Double) null);
        portStatus.setPower(Double.valueOf(0.0d));
        portStatus.setDelayPrice(0.0d);
        portStatus.setTotalFee(0.0d);
        portStatus.setWaitPluginTimeout(0);
        portStatus.setWaitPlugoutTimeout(0);
        ChargeStatusCacheProvider.getInstance().updatePortStatus(this.port, portStatus);
    }

    private double getDelayPrice(long time) {
        HashMap<String, Object> delayPriceSection;
        Double price;
        FeeRate feeRate = getChargeSession().getChargeBill().getFee_rate();
        if (feeRate == null || feeRate.getDelayPrice() == null || (delayPriceSection = BillUtils.getPriceSection(time, feeRate.getDelayPrice())) == null || (price = (Double) delayPriceSection.get("price")) == null || price.doubleValue() <= 0.0d) {
            return 0.0d;
        }
        return price.doubleValue();
    }

    private HashMap<String, Object> getDelayPriceSection(long time) {
        FeeRate feeRate = getChargeSession().getChargeBill().getFee_rate();
        if (feeRate == null || feeRate.getDelayPrice() == null) {
            return null;
        }
        return BillUtils.getPriceSection(time, feeRate.getDelayPrice());
    }

    /* access modifiers changed from: private */
    public void notifyChargeEnded2OSS() {
        OSSController.getInstance().sendMessage(OSSController.getInstance().obtainMessage(OSSController.MSG_CHARGE_TO_IDLE, this.port));
    }
}
