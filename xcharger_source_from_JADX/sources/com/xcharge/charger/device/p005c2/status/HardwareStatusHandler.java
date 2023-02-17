package com.xcharge.charger.device.p005c2.status;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.p000v4.view.ViewCompat;
import android.util.Log;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.device.NFC;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.device.Radar;
import com.xcharge.charger.data.bean.type.DEVICE_STATUS;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.PARK_STATUS;
import com.xcharge.charger.data.bean.type.SWITCH_STATUS;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.device.p005c2.bean.PortRuntimeData;
import com.xcharge.charger.device.p005c2.service.C2DeviceProxy;
import com.xcharge.common.utils.LogUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/* renamed from: com.xcharge.charger.device.c2.status.HardwareStatusHandler */
public class HardwareStatusHandler {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$DEVICE_STATUS = null;
    public static final int MSG_NETWORK_CONNECTION = 86049;
    public static final int MSG_PORT_AUTH_INVALID = 86018;
    public static final int MSG_PORT_AUTH_VALID = 86017;
    public static final int MSG_PORT_CHARGE_FULL = 86023;
    public static final int MSG_PORT_CHARGE_STARTED = 86021;
    public static final int MSG_PORT_CHARGE_STOPPED = 86022;
    public static final int MSG_PORT_ENABLE_STATUS = 86052;
    public static final int MSG_PORT_NFC_STATUS = 86048;
    public static final int MSG_PORT_PARK_STATUS = 86035;
    public static final int MSG_PORT_PLUGIN = 86019;
    public static final int MSG_PORT_PLUGOUT = 86020;
    public static final int MSG_PORT_RADAR_CALIBRATION = 86034;
    public static final int MSG_PORT_RADAR_STATUS = 86050;
    public static final int MSG_PORT_RESUME = 86025;
    public static final int MSG_PORT_SUSPEND = 86024;
    public static final int MSG_PORT_UPDATE = 86032;
    public static final int MSG_PORT_UPDATE_BY_CMD = 86051;
    public static final int MSG_PORT_WARN = 86033;
    private Context context = null;
    private int deviceStatus = 0;
    private MsgHandler handler = null;
    private PortEnableStatusObserver portEnableStatusObserver = null;
    private PortRadarStatusObserver portRadarStatusObserver = null;
    private HandlerThread thread = null;

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$DEVICE_STATUS() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$DEVICE_STATUS;
        if (iArr == null) {
            iArr = new int[DEVICE_STATUS.values().length];
            try {
                iArr[DEVICE_STATUS.chargeFull.ordinal()] = 6;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[DEVICE_STATUS.charging.ordinal()] = 5;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[DEVICE_STATUS.emergencyStop.ordinal()] = 11;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[DEVICE_STATUS.errorAmp.ordinal()] = 13;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[DEVICE_STATUS.errorComm.ordinal()] = 16;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[DEVICE_STATUS.errorTemp.ordinal()] = 14;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[DEVICE_STATUS.errorVolt.ordinal()] = 12;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[DEVICE_STATUS.idle.ordinal()] = 1;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[DEVICE_STATUS.lostPhase.ordinal()] = 10;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[DEVICE_STATUS.noGround.ordinal()] = 9;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[DEVICE_STATUS.notInited.ordinal()] = 8;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[DEVICE_STATUS.plugin.ordinal()] = 4;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[DEVICE_STATUS.plugout.ordinal()] = 3;
            } catch (NoSuchFieldError e13) {
            }
            try {
                iArr[DEVICE_STATUS.powerLeak.ordinal()] = 15;
            } catch (NoSuchFieldError e14) {
            }
            try {
                iArr[DEVICE_STATUS.selfCheck.ordinal()] = 2;
            } catch (NoSuchFieldError e15) {
            }
            try {
                iArr[DEVICE_STATUS.stopped.ordinal()] = 7;
            } catch (NoSuchFieldError e16) {
            }
            $SWITCH_TABLE$com$xcharge$charger$data$bean$type$DEVICE_STATUS = iArr;
        }
        return iArr;
    }

    /* renamed from: com.xcharge.charger.device.c2.status.HardwareStatusHandler$PortRadarStatusObserver */
    private class PortRadarStatusObserver extends ContentObserver {
        private Handler handler = null;

        public PortRadarStatusObserver(Handler handler2) {
            super(handler2);
            this.handler = handler2;
        }

        public void onChange(boolean selfChange, Uri uri) {
            Log.d("HardwareStatusHandler.PortRadarStatusObserver.onChange", "selfChange: " + selfChange + ", uri: " + uri.toString());
            super.onChange(selfChange, uri);
            this.handler.obtainMessage(HardwareStatusHandler.MSG_PORT_RADAR_STATUS, uri).sendToTarget();
        }
    }

    /* renamed from: com.xcharge.charger.device.c2.status.HardwareStatusHandler$PortEnableStatusObserver */
    private class PortEnableStatusObserver extends ContentObserver {
        private Handler handler = null;

        public PortEnableStatusObserver(Handler handler2) {
            super(handler2);
            this.handler = handler2;
        }

        public void onChange(boolean selfChange, Uri uri) {
            Log.d("HardwareStatusHandler.PortEnableStatusObserver.onChange", "selfChange: " + selfChange + ", uri: " + uri.toString());
            super.onChange(selfChange, uri);
            this.handler.obtainMessage(HardwareStatusHandler.MSG_PORT_ENABLE_STATUS, uri).sendToTarget();
        }
    }

    /* renamed from: com.xcharge.charger.device.c2.status.HardwareStatusHandler$MsgHandler */
    private class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r12) {
            /*
                r11 = this;
                r1 = 0
                r7 = 0
                int r8 = r12.what     // Catch:{ Exception -> 0x002f }
                switch(r8) {
                    case 86017: goto L_0x000b;
                    case 86018: goto L_0x005f;
                    case 86019: goto L_0x0083;
                    case 86020: goto L_0x00a8;
                    case 86021: goto L_0x00cd;
                    case 86022: goto L_0x0117;
                    case 86023: goto L_0x00f2;
                    case 86024: goto L_0x013c;
                    case 86025: goto L_0x0161;
                    case 86026: goto L_0x0007;
                    case 86027: goto L_0x0007;
                    case 86028: goto L_0x0007;
                    case 86029: goto L_0x0007;
                    case 86030: goto L_0x0007;
                    case 86031: goto L_0x0007;
                    case 86032: goto L_0x0186;
                    case 86033: goto L_0x01ab;
                    case 86034: goto L_0x020a;
                    case 86035: goto L_0x01cb;
                    case 86036: goto L_0x0007;
                    case 86037: goto L_0x0007;
                    case 86038: goto L_0x0007;
                    case 86039: goto L_0x0007;
                    case 86040: goto L_0x0007;
                    case 86041: goto L_0x0007;
                    case 86042: goto L_0x0007;
                    case 86043: goto L_0x0007;
                    case 86044: goto L_0x0007;
                    case 86045: goto L_0x0007;
                    case 86046: goto L_0x0007;
                    case 86047: goto L_0x0007;
                    case 86048: goto L_0x0249;
                    case 86049: goto L_0x0280;
                    case 86050: goto L_0x02a3;
                    case 86051: goto L_0x02c4;
                    case 86052: goto L_0x02e7;
                    default: goto L_0x0007;
                }
            L_0x0007:
                super.handleMessage(r12)
                return
            L_0x000b:
                java.lang.Object r8 = r12.obj     // Catch:{ Exception -> 0x002f }
                r0 = r8
                com.xcharge.charger.device.c2.bean.PortRuntimeData r0 = (com.xcharge.charger.device.p005c2.bean.PortRuntimeData) r0     // Catch:{ Exception -> 0x002f }
                r1 = r0
                java.lang.String r8 = "HardwareStatusHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = "port auth valid !!! data: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = r1.toJson()     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002f }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.device.c2.status.HardwareStatusHandler r8 = com.xcharge.charger.device.p005c2.status.HardwareStatusHandler.this     // Catch:{ Exception -> 0x002f }
                r8.handleAuthValid(r1)     // Catch:{ Exception -> 0x002f }
                goto L_0x0007
            L_0x002f:
                r2 = move-exception
                java.lang.String r8 = "HardwareStatusHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder
                java.lang.String r10 = "except: "
                r9.<init>(r10)
                java.lang.String r10 = android.util.Log.getStackTraceString(r2)
                java.lang.StringBuilder r9 = r9.append(r10)
                java.lang.String r9 = r9.toString()
                android.util.Log.e(r8, r9)
                java.lang.StringBuilder r8 = new java.lang.StringBuilder
                java.lang.String r9 = "HardwareStatusHandler handleMessage exception: "
                r8.<init>(r9)
                java.lang.String r9 = android.util.Log.getStackTraceString(r2)
                java.lang.StringBuilder r8 = r8.append(r9)
                java.lang.String r8 = r8.toString()
                com.xcharge.common.utils.LogUtils.syslog(r8)
                goto L_0x0007
            L_0x005f:
                java.lang.Object r8 = r12.obj     // Catch:{ Exception -> 0x002f }
                r0 = r8
                com.xcharge.charger.device.c2.bean.PortRuntimeData r0 = (com.xcharge.charger.device.p005c2.bean.PortRuntimeData) r0     // Catch:{ Exception -> 0x002f }
                r1 = r0
                java.lang.String r8 = "HardwareStatusHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = "port auth invalid !!!  data: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = r1.toJson()     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002f }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.device.c2.status.HardwareStatusHandler r8 = com.xcharge.charger.device.p005c2.status.HardwareStatusHandler.this     // Catch:{ Exception -> 0x002f }
                r8.handleAuthInvalid(r1)     // Catch:{ Exception -> 0x002f }
                goto L_0x0007
            L_0x0083:
                java.lang.Object r8 = r12.obj     // Catch:{ Exception -> 0x002f }
                r0 = r8
                com.xcharge.charger.device.c2.bean.PortRuntimeData r0 = (com.xcharge.charger.device.p005c2.bean.PortRuntimeData) r0     // Catch:{ Exception -> 0x002f }
                r1 = r0
                java.lang.String r8 = "HardwareStatusHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = "port plugin !!!  data: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = r1.toJson()     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002f }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.device.c2.status.HardwareStatusHandler r8 = com.xcharge.charger.device.p005c2.status.HardwareStatusHandler.this     // Catch:{ Exception -> 0x002f }
                r8.handlePlugin(r1)     // Catch:{ Exception -> 0x002f }
                goto L_0x0007
            L_0x00a8:
                java.lang.Object r8 = r12.obj     // Catch:{ Exception -> 0x002f }
                r0 = r8
                com.xcharge.charger.device.c2.bean.PortRuntimeData r0 = (com.xcharge.charger.device.p005c2.bean.PortRuntimeData) r0     // Catch:{ Exception -> 0x002f }
                r1 = r0
                java.lang.String r8 = "HardwareStatusHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = "port plugout !!!  data: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = r1.toJson()     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002f }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.device.c2.status.HardwareStatusHandler r8 = com.xcharge.charger.device.p005c2.status.HardwareStatusHandler.this     // Catch:{ Exception -> 0x002f }
                r8.handlePlugout(r1)     // Catch:{ Exception -> 0x002f }
                goto L_0x0007
            L_0x00cd:
                java.lang.Object r8 = r12.obj     // Catch:{ Exception -> 0x002f }
                r0 = r8
                com.xcharge.charger.device.c2.bean.PortRuntimeData r0 = (com.xcharge.charger.device.p005c2.bean.PortRuntimeData) r0     // Catch:{ Exception -> 0x002f }
                r1 = r0
                java.lang.String r8 = "HardwareStatusHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = "port charge started !!!  data: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = r1.toJson()     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002f }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.device.c2.status.HardwareStatusHandler r8 = com.xcharge.charger.device.p005c2.status.HardwareStatusHandler.this     // Catch:{ Exception -> 0x002f }
                r8.handleChargeStart(r1)     // Catch:{ Exception -> 0x002f }
                goto L_0x0007
            L_0x00f2:
                java.lang.Object r8 = r12.obj     // Catch:{ Exception -> 0x002f }
                r0 = r8
                com.xcharge.charger.device.c2.bean.PortRuntimeData r0 = (com.xcharge.charger.device.p005c2.bean.PortRuntimeData) r0     // Catch:{ Exception -> 0x002f }
                r1 = r0
                java.lang.String r8 = "HardwareStatusHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = "port charge full !!!  data: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = r1.toJson()     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002f }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.device.c2.status.HardwareStatusHandler r8 = com.xcharge.charger.device.p005c2.status.HardwareStatusHandler.this     // Catch:{ Exception -> 0x002f }
                r8.handleChargeFull(r1)     // Catch:{ Exception -> 0x002f }
                goto L_0x0007
            L_0x0117:
                java.lang.Object r8 = r12.obj     // Catch:{ Exception -> 0x002f }
                r0 = r8
                com.xcharge.charger.device.c2.bean.PortRuntimeData r0 = (com.xcharge.charger.device.p005c2.bean.PortRuntimeData) r0     // Catch:{ Exception -> 0x002f }
                r1 = r0
                java.lang.String r8 = "HardwareStatusHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = "port charge stopped !!!  data: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = r1.toJson()     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002f }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.device.c2.status.HardwareStatusHandler r8 = com.xcharge.charger.device.p005c2.status.HardwareStatusHandler.this     // Catch:{ Exception -> 0x002f }
                r8.handleChargeStop(r1)     // Catch:{ Exception -> 0x002f }
                goto L_0x0007
            L_0x013c:
                java.lang.Object r8 = r12.obj     // Catch:{ Exception -> 0x002f }
                r0 = r8
                com.xcharge.charger.device.c2.bean.PortRuntimeData r0 = (com.xcharge.charger.device.p005c2.bean.PortRuntimeData) r0     // Catch:{ Exception -> 0x002f }
                r1 = r0
                java.lang.String r8 = "HardwareStatusHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = "port suspended !!!  data: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = r1.toJson()     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002f }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.device.c2.status.HardwareStatusHandler r8 = com.xcharge.charger.device.p005c2.status.HardwareStatusHandler.this     // Catch:{ Exception -> 0x002f }
                r8.handleSuspend(r1)     // Catch:{ Exception -> 0x002f }
                goto L_0x0007
            L_0x0161:
                java.lang.Object r8 = r12.obj     // Catch:{ Exception -> 0x002f }
                r0 = r8
                com.xcharge.charger.device.c2.bean.PortRuntimeData r0 = (com.xcharge.charger.device.p005c2.bean.PortRuntimeData) r0     // Catch:{ Exception -> 0x002f }
                r1 = r0
                java.lang.String r8 = "HardwareStatusHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = "port resumed !!!  data: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = r1.toJson()     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002f }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.device.c2.status.HardwareStatusHandler r8 = com.xcharge.charger.device.p005c2.status.HardwareStatusHandler.this     // Catch:{ Exception -> 0x002f }
                r8.handleResume(r1)     // Catch:{ Exception -> 0x002f }
                goto L_0x0007
            L_0x0186:
                java.lang.Object r8 = r12.obj     // Catch:{ Exception -> 0x002f }
                r0 = r8
                com.xcharge.charger.device.c2.bean.PortRuntimeData r0 = (com.xcharge.charger.device.p005c2.bean.PortRuntimeData) r0     // Catch:{ Exception -> 0x002f }
                r1 = r0
                java.lang.String r8 = "HardwareStatusHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = "port updated !!!  data: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = r1.toJson()     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002f }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.device.c2.status.HardwareStatusHandler r8 = com.xcharge.charger.device.p005c2.status.HardwareStatusHandler.this     // Catch:{ Exception -> 0x002f }
                r8.handleUpdate(r1)     // Catch:{ Exception -> 0x002f }
                goto L_0x0007
            L_0x01ab:
                java.lang.Object r8 = r12.obj     // Catch:{ Exception -> 0x002f }
                r0 = r8
                com.xcharge.charger.device.c2.bean.PortRuntimeData r0 = (com.xcharge.charger.device.p005c2.bean.PortRuntimeData) r0     // Catch:{ Exception -> 0x002f }
                r1 = r0
                java.lang.String r8 = "HardwareStatusHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = "port warning !!! port: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = r1.getPort()     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002f }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002f }
                goto L_0x0007
            L_0x01cb:
                java.lang.Object r8 = r12.obj     // Catch:{ Exception -> 0x002f }
                r0 = r8
                com.xcharge.charger.device.c2.bean.PortRuntimeData r0 = (com.xcharge.charger.device.p005c2.bean.PortRuntimeData) r0     // Catch:{ Exception -> 0x002f }
                r1 = r0
                java.lang.String r8 = "HardwareStatusHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = "port park status changed !!! port: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = r1.getPort()     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = ", park status: "
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.data.bean.type.PARK_STATUS r10 = r1.getParkStatus()     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = r10.getStatus()     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002f }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.device.c2.status.HardwareStatusHandler r8 = com.xcharge.charger.device.p005c2.status.HardwareStatusHandler.this     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r1.getPort()     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.data.bean.type.PARK_STATUS r10 = r1.getParkStatus()     // Catch:{ Exception -> 0x002f }
                r8.handleParkChanged(r9, r10)     // Catch:{ Exception -> 0x002f }
                goto L_0x0007
            L_0x020a:
                java.lang.Object r8 = r12.obj     // Catch:{ Exception -> 0x002f }
                r0 = r8
                com.xcharge.charger.device.c2.bean.PortRuntimeData r0 = (com.xcharge.charger.device.p005c2.bean.PortRuntimeData) r0     // Catch:{ Exception -> 0x002f }
                r1 = r0
                java.lang.String r8 = "HardwareStatusHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = "port radar calibration result changed !!! port: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = r1.getPort()     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = ", radar calibration result: "
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.Boolean r10 = r1.getIsRadarCalibrated()     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002f }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.device.c2.status.HardwareStatusHandler r8 = com.xcharge.charger.device.p005c2.status.HardwareStatusHandler.this     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r1.getPort()     // Catch:{ Exception -> 0x002f }
                java.lang.Boolean r10 = r1.getIsRadarCalibrated()     // Catch:{ Exception -> 0x002f }
                boolean r10 = r10.booleanValue()     // Catch:{ Exception -> 0x002f }
                r8.handleRadarCalibration(r9, r10)     // Catch:{ Exception -> 0x002f }
                goto L_0x0007
            L_0x0249:
                android.os.Bundle r8 = r12.getData()     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = "port"
                java.lang.String r5 = r8.getString(r9)     // Catch:{ Exception -> 0x002f }
                java.lang.Object r4 = r12.obj     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.data.bean.device.NFC r4 = (com.xcharge.charger.data.bean.device.NFC) r4     // Catch:{ Exception -> 0x002f }
                java.lang.String r8 = "HardwareStatusHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = "port nfc status changed !!! port: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r5)     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = ", nfc status: "
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = r4.toJson()     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002f }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.device.c2.status.HardwareStatusHandler r8 = com.xcharge.charger.device.p005c2.status.HardwareStatusHandler.this     // Catch:{ Exception -> 0x002f }
                r8.handleNFCStatus(r5, r4)     // Catch:{ Exception -> 0x002f }
                goto L_0x0007
            L_0x0280:
                java.lang.Object r8 = r12.obj     // Catch:{ Exception -> 0x002f }
                java.lang.Boolean r8 = (java.lang.Boolean) r8     // Catch:{ Exception -> 0x002f }
                boolean r3 = r8.booleanValue()     // Catch:{ Exception -> 0x002f }
                java.lang.String r8 = "HardwareStatusHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = "network connection status changed !!! is network connected: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r3)     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002f }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.device.c2.status.HardwareStatusHandler r8 = com.xcharge.charger.device.p005c2.status.HardwareStatusHandler.this     // Catch:{ Exception -> 0x002f }
                r8.handleNetworkConnectionStatus(r3)     // Catch:{ Exception -> 0x002f }
                goto L_0x0007
            L_0x02a3:
                java.lang.Object r8 = r12.obj     // Catch:{ Exception -> 0x002f }
                r0 = r8
                android.net.Uri r0 = (android.net.Uri) r0     // Catch:{ Exception -> 0x002f }
                r7 = r0
                java.lang.String r8 = "HardwareStatusHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = "radar status changed !!! uri: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r7)     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002f }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.device.c2.status.HardwareStatusHandler r8 = com.xcharge.charger.device.p005c2.status.HardwareStatusHandler.this     // Catch:{ Exception -> 0x002f }
                r8.handleRadarStatusChanged(r7)     // Catch:{ Exception -> 0x002f }
                goto L_0x0007
            L_0x02c4:
                java.lang.Object r6 = r12.obj     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.data.bean.device.Port r6 = (com.xcharge.charger.data.bean.device.Port) r6     // Catch:{ Exception -> 0x002f }
                java.lang.String r8 = "HardwareStatusHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = "receive port status by update cmd: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = r6.toJson()     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002f }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.device.c2.status.HardwareStatusHandler r8 = com.xcharge.charger.device.p005c2.status.HardwareStatusHandler.this     // Catch:{ Exception -> 0x002f }
                r8.handlePortUpdateByCmd(r6)     // Catch:{ Exception -> 0x002f }
                goto L_0x0007
            L_0x02e7:
                java.lang.Object r8 = r12.obj     // Catch:{ Exception -> 0x002f }
                r0 = r8
                android.net.Uri r0 = (android.net.Uri) r0     // Catch:{ Exception -> 0x002f }
                r7 = r0
                java.lang.String r8 = "HardwareStatusHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002f }
                java.lang.String r10 = "port enable status changed !!! uri: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002f }
                java.lang.StringBuilder r9 = r9.append(r7)     // Catch:{ Exception -> 0x002f }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002f }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002f }
                com.xcharge.charger.device.c2.status.HardwareStatusHandler r8 = com.xcharge.charger.device.p005c2.status.HardwareStatusHandler.this     // Catch:{ Exception -> 0x002f }
                r8.handlePortEnableStatusChanged(r7)     // Catch:{ Exception -> 0x002f }
                goto L_0x0007
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.device.p005c2.status.HardwareStatusHandler.MsgHandler.handleMessage(android.os.Message):void");
        }
    }

    public void init(Context context2) {
        this.context = context2;
        this.thread = new HandlerThread("HardwareStatusHandler", 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.portRadarStatusObserver = new PortRadarStatusObserver(this.handler);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/radar/status"), true, this.portRadarStatusObserver);
        this.portEnableStatusObserver = new PortEnableStatusObserver(this.handler);
        this.context.getContentResolver().registerContentObserver(ChargeStatusCacheProvider.getInstance().getUriFor("ports/enable"), true, this.portEnableStatusObserver);
    }

    public void destroy() {
        this.context.getContentResolver().unregisterContentObserver(this.portRadarStatusObserver);
        this.context.getContentResolver().unregisterContentObserver(this.portEnableStatusObserver);
        this.handler.removeMessages(MSG_PORT_AUTH_VALID);
        this.handler.removeMessages(MSG_PORT_AUTH_INVALID);
        this.handler.removeMessages(MSG_PORT_PLUGIN);
        this.handler.removeMessages(MSG_PORT_PLUGOUT);
        this.handler.removeMessages(MSG_PORT_CHARGE_STARTED);
        this.handler.removeMessages(MSG_PORT_CHARGE_STOPPED);
        this.handler.removeMessages(MSG_PORT_CHARGE_FULL);
        this.handler.removeMessages(MSG_PORT_SUSPEND);
        this.handler.removeMessages(MSG_PORT_RESUME);
        this.handler.removeMessages(MSG_PORT_UPDATE);
        this.handler.removeMessages(MSG_PORT_WARN);
        this.handler.removeMessages(MSG_PORT_RADAR_CALIBRATION);
        this.handler.removeMessages(MSG_PORT_PARK_STATUS);
        this.handler.removeMessages(MSG_PORT_NFC_STATUS);
        this.handler.removeMessages(MSG_NETWORK_CONNECTION);
        this.handler.removeMessages(MSG_PORT_RADAR_STATUS);
        this.handler.removeMessages(MSG_PORT_UPDATE_BY_CMD);
        this.handler.removeMessages(MSG_PORT_ENABLE_STATUS);
        this.thread.quit();
    }

    public Message obtainMessage(int what, Object obj) {
        return this.handler.obtainMessage(what, obj);
    }

    public boolean sendMessage(Message msg) {
        return this.handler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    public void handleNFCStatus(String port, NFC nfcStatus) {
        HardwareStatusCacheProvider.getInstance().updatePortNFCStatus(port, nfcStatus);
    }

    /* access modifiers changed from: private */
    public void handleRadarStatusChanged(Uri uri) {
        String port = uri.getLastPathSegment();
        if (HardwareStatusCacheProvider.getInstance().getPortRadarSwitch(port)) {
            updateParkStatusBLN(port);
        }
    }

    /* access modifiers changed from: private */
    public void handlePortEnableStatusChanged(Uri uri) {
        String port = uri.getLastPathSegment();
        if (ChargeStatusCacheProvider.getInstance().getPortSwitch(port)) {
            C2DeviceProxy.getInstance().setSystemStatusBLN(port, this.deviceStatus, Color.parseColor(RemoteSettingCacheProvider.getInstance().getDefaultBLNColor()) & ViewCompat.MEASURED_SIZE_MASK);
        }
    }

    /* access modifiers changed from: private */
    public void handleNetworkConnectionStatus(boolean isConnected) {
        updateNetworkStatusBLN();
    }

    /* access modifiers changed from: private */
    public void handleAuthValid(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        portStatus.setDeviceError(getErrorCode(DEVICE_STATUS.valueBy(data.getStatus().intValue())));
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    /* access modifiers changed from: private */
    public void handleAuthInvalid(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        portStatus.setDeviceError(getErrorCode(DEVICE_STATUS.valueBy(data.getStatus().intValue())));
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    /* access modifiers changed from: private */
    public void handlePlugin(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        portStatus.setDeviceError(getErrorCode(DEVICE_STATUS.valueBy(data.getStatus().intValue())));
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    /* access modifiers changed from: private */
    public void handlePlugout(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        portStatus.setDeviceError(getErrorCode(DEVICE_STATUS.valueBy(data.getStatus().intValue())));
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    /* access modifiers changed from: private */
    public void handleChargeStart(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        portStatus.setDeviceError(getErrorCode(DEVICE_STATUS.valueBy(data.getStatus().intValue())));
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    /* access modifiers changed from: private */
    public void handleChargeFull(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        portStatus.setDeviceError(getErrorCode(DEVICE_STATUS.valueBy(data.getStatus().intValue())));
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    /* access modifiers changed from: private */
    public void handleChargeStop(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        portStatus.setDeviceError(getErrorCode(DEVICE_STATUS.valueBy(data.getStatus().intValue())));
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    /* access modifiers changed from: private */
    public void handleSuspend(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        DEVICE_STATUS deviceStatus2 = DEVICE_STATUS.valueBy(data.getStatus().intValue());
        ErrorCode error = getErrorCode(deviceStatus2);
        HashMap<String, Object> errData = new HashMap<>();
        errData.put("raw", data.toJson());
        error.setData(errData);
        if (error.getCode() >= 30010 && error.getCode() <= 30018) {
            PortRuntimeData fullPortRuntimeInfo = C2DeviceProxy.getInstance().getPortRuntimeInfo(portNo);
            Double[] dArr = new Double[3];
            dArr[0] = Double.valueOf(fullPortRuntimeInfo.getCurrentA() == null ? 0.0d : fullPortRuntimeInfo.getCurrentA().doubleValue());
            dArr[1] = Double.valueOf(fullPortRuntimeInfo.getCurrentB() == null ? 0.0d : fullPortRuntimeInfo.getCurrentB().doubleValue());
            dArr[2] = Double.valueOf(fullPortRuntimeInfo.getCurrentC() == null ? 0.0d : fullPortRuntimeInfo.getCurrentC().doubleValue());
            portStatus.setAmps(new ArrayList(Arrays.asList(dArr)));
            Double[] dArr2 = new Double[3];
            dArr2[0] = Double.valueOf(fullPortRuntimeInfo.getVoltageA() == null ? 0.0d : fullPortRuntimeInfo.getVoltageA().doubleValue());
            dArr2[1] = Double.valueOf(fullPortRuntimeInfo.getVoltageB() == null ? 0.0d : fullPortRuntimeInfo.getVoltageB().doubleValue());
            dArr2[2] = Double.valueOf(fullPortRuntimeInfo.getVoltageC() == null ? 0.0d : fullPortRuntimeInfo.getVoltageC().doubleValue());
            portStatus.setVolts(new ArrayList(Arrays.asList(dArr2)));
            portStatus.setLeakAmp(Double.valueOf(fullPortRuntimeInfo.getCurrentN() == null ? 0.0d : fullPortRuntimeInfo.getCurrentN().doubleValue()));
            portStatus.setChipTemp(Double.valueOf(fullPortRuntimeInfo.getChipTemp() == null ? 0.0d : fullPortRuntimeInfo.getChipTemp().doubleValue()));
            portStatus.setCpVoltage(Integer.valueOf(fullPortRuntimeInfo.getCpVoltage() == null ? 0 : fullPortRuntimeInfo.getCpVoltage().intValue()));
            if (DEVICE_STATUS.emergencyStop.equals(deviceStatus2)) {
                portStatus.getEmergencyStop().setStatus(SWITCH_STATUS.on);
            }
            errData.put("portStatus", portStatus.toJson());
            error.setData(errData);
            portStatus.setDeviceError(error);
            if (!LOCK_STATUS.disable.equals(ChargeStatusCacheProvider.getInstance().getPortLockStatus(portNo))) {
                C2DeviceProxy.getInstance().unlockGun(portNo);
                ChargeStatusCacheProvider.getInstance().updatePortLockStatus(portNo, LOCK_STATUS.unlock);
            }
        }
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        if (error.getCode() >= 30010 && error.getCode() <= 30018) {
            HardwareStatusCacheProvider.getInstance().putDeviceError(portNo, error);
            HardwareStatusCacheProvider.getInstance().removeHigherPriorityDeviceErrors(portNo, error);
            LogUtils.syslog("receive error Suspend: " + data.toJson());
        }
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    /* access modifiers changed from: private */
    public void handleResume(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        ErrorCode error = getErrorCode(DEVICE_STATUS.valueBy(data.getStatus().intValue()));
        if (error.getCode() >= 30010 && error.getCode() <= 30018) {
            portStatus.setDeviceError(new ErrorCode(200));
            portStatus.getEmergencyStop().setStatus(SWITCH_STATUS.off);
        }
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        if (error.getCode() >= 30010 && error.getCode() <= 30018) {
            HardwareStatusCacheProvider.getInstance().removeDeviceError(portNo, (ErrorCode) null);
            LogUtils.syslog("receive error Resume: " + data.toJson());
        }
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    private void handleWarning(String port) {
        double d = 0.0d;
        PortRuntimeData fullPortRuntimeInfo = C2DeviceProxy.getInstance().getPortRuntimeInfo(port);
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(port);
        Double[] dArr = new Double[3];
        dArr[0] = Double.valueOf(fullPortRuntimeInfo.getCurrentA() == null ? 0.0d : fullPortRuntimeInfo.getCurrentA().doubleValue());
        dArr[1] = Double.valueOf(fullPortRuntimeInfo.getCurrentB() == null ? 0.0d : fullPortRuntimeInfo.getCurrentB().doubleValue());
        dArr[2] = Double.valueOf(fullPortRuntimeInfo.getCurrentC() == null ? 0.0d : fullPortRuntimeInfo.getCurrentC().doubleValue());
        portStatus.setAmps(new ArrayList(Arrays.asList(dArr)));
        Double[] dArr2 = new Double[3];
        dArr2[0] = Double.valueOf(fullPortRuntimeInfo.getVoltageA() == null ? 0.0d : fullPortRuntimeInfo.getVoltageA().doubleValue());
        dArr2[1] = Double.valueOf(fullPortRuntimeInfo.getVoltageB() == null ? 0.0d : fullPortRuntimeInfo.getVoltageB().doubleValue());
        dArr2[2] = Double.valueOf(fullPortRuntimeInfo.getVoltageC() == null ? 0.0d : fullPortRuntimeInfo.getVoltageC().doubleValue());
        portStatus.setVolts(new ArrayList(Arrays.asList(dArr2)));
        portStatus.setLeakAmp(Double.valueOf(fullPortRuntimeInfo.getCurrentN() == null ? 0.0d : fullPortRuntimeInfo.getCurrentN().doubleValue()));
        if (fullPortRuntimeInfo.getChipTemp() != null) {
            d = fullPortRuntimeInfo.getChipTemp().doubleValue();
        }
        portStatus.setChipTemp(Double.valueOf(d));
        portStatus.setCpVoltage(Integer.valueOf(fullPortRuntimeInfo.getCpVoltage() == null ? 0 : fullPortRuntimeInfo.getCpVoltage().intValue()));
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
    }

    /* access modifiers changed from: private */
    public void handleUpdate(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        ErrorCode error = portStatus.getDeviceError();
        ErrorCode updateError = getErrorCode(DEVICE_STATUS.valueBy(data.getStatus().intValue()));
        portStatus.setDeviceError(updateError);
        if (data.getStatus().intValue() >= DEVICE_STATUS.notInited.getStatus()) {
            PortRuntimeData fullPortRuntimeInfo = C2DeviceProxy.getInstance().getPortRuntimeInfo(portNo);
            Double[] dArr = new Double[3];
            dArr[0] = Double.valueOf(fullPortRuntimeInfo.getCurrentA() == null ? 0.0d : fullPortRuntimeInfo.getCurrentA().doubleValue());
            dArr[1] = Double.valueOf(fullPortRuntimeInfo.getCurrentB() == null ? 0.0d : fullPortRuntimeInfo.getCurrentB().doubleValue());
            dArr[2] = Double.valueOf(fullPortRuntimeInfo.getCurrentC() == null ? 0.0d : fullPortRuntimeInfo.getCurrentC().doubleValue());
            portStatus.setAmps(new ArrayList(Arrays.asList(dArr)));
            Double[] dArr2 = new Double[3];
            dArr2[0] = Double.valueOf(fullPortRuntimeInfo.getVoltageA() == null ? 0.0d : fullPortRuntimeInfo.getVoltageA().doubleValue());
            dArr2[1] = Double.valueOf(fullPortRuntimeInfo.getVoltageB() == null ? 0.0d : fullPortRuntimeInfo.getVoltageB().doubleValue());
            dArr2[2] = Double.valueOf(fullPortRuntimeInfo.getVoltageC() == null ? 0.0d : fullPortRuntimeInfo.getVoltageC().doubleValue());
            portStatus.setVolts(new ArrayList(Arrays.asList(dArr2)));
            portStatus.setLeakAmp(Double.valueOf(fullPortRuntimeInfo.getCurrentN() == null ? 0.0d : fullPortRuntimeInfo.getCurrentN().doubleValue()));
            portStatus.setChipTemp(Double.valueOf(fullPortRuntimeInfo.getChipTemp() == null ? 0.0d : fullPortRuntimeInfo.getChipTemp().doubleValue()));
            portStatus.setCpVoltage(Integer.valueOf(fullPortRuntimeInfo.getCpVoltage() == null ? 0 : fullPortRuntimeInfo.getCpVoltage().intValue()));
            HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        } else if (((error.getCode() < 30010 || error.getCode() > 30018) && (portStatus.getAllDeviceErrorCache() == null || portStatus.getAllDeviceErrorCache().size() <= 0)) || updateError.getCode() != 200) {
            HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        } else {
            Log.w("HardwareStatusHandler.handleUpdate", "error: " + error.toJson() + " -> normal: " + updateError.toJson());
            LogUtils.applog("error: " + (error.getCode() - 30000) + " resumed by update event !!!");
            portStatus.setDeviceError(new ErrorCode(200));
            portStatus.getEmergencyStop().setStatus(SWITCH_STATUS.off);
            HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
            HardwareStatusCacheProvider.getInstance().removeDeviceError(portNo, (ErrorCode) null);
        }
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    /* access modifiers changed from: private */
    public void handleParkChanged(String port, PARK_STATUS status) {
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(port);
        portStatus.getParkStatus().setParkStatus(status);
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        updateParkStatusBLN(port);
    }

    /* access modifiers changed from: private */
    public void handleRadarCalibration(String port, boolean isSuccess) {
        int i = 0;
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(port);
        portStatus.getRadar().setCalibration(isSuccess);
        PortRuntimeData fullPortRuntimeInfo = C2DeviceProxy.getInstance().getPortRuntimeInfo(port);
        portStatus.getRadar().setCalibrateDist(Integer.valueOf(fullPortRuntimeInfo.getRaderCalibration() == null ? 0 : fullPortRuntimeInfo.getRaderCalibration().intValue()));
        Radar radar = portStatus.getRadar();
        if (fullPortRuntimeInfo.getRader() != null) {
            i = fullPortRuntimeInfo.getRader().intValue();
        }
        radar.setDetectDist(Integer.valueOf(i));
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
    }

    /* access modifiers changed from: private */
    public void handlePortUpdateByCmd(Port updatePortStatus) {
        int updateStatus = updatePortStatus.getPortRuntimeStatus().getStatus();
        String portNo = updatePortStatus.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        ErrorCode error = portStatus.getDeviceError();
        ErrorCode updateError = getErrorCode(updatePortStatus.getPortRuntimeStatus());
        if (((error.getCode() >= 30010 && error.getCode() <= 30018) || (portStatus.getAllDeviceErrorCache() != null && portStatus.getAllDeviceErrorCache().size() > 0)) && updateError.getCode() == 200 && getErrorCode(C2DeviceProxy.getInstance().getPortRuntimeStatus(portNo).getPortRuntimeStatus()).getCode() == 200) {
            Log.w("HardwareStatusHandler.handlePortUpdateByCmd", "error: " + error.toJson() + " -> normal: " + updateError.toJson());
            LogUtils.applog("error: " + (error.getCode() - 30000) + " resumed by update cmd !!!");
            portStatus.setDeviceError(new ErrorCode(200));
            portStatus.getEmergencyStop().setStatus(SWITCH_STATUS.off);
            HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
            HardwareStatusCacheProvider.getInstance().removeDeviceError(portNo, (ErrorCode) null);
            C2DeviceProxy.getInstance().setSystemStatusBLN(portNo, updateStatus, Color.parseColor(RemoteSettingCacheProvider.getInstance().getDefaultBLNColor()) & ViewCompat.MEASURED_SIZE_MASK);
            this.deviceStatus = updateStatus;
        }
    }

    private void updateDeviceStatusBLN(String port, int status) {
        int defaultNorColor = Color.parseColor(RemoteSettingCacheProvider.getInstance().getDefaultBLNColor()) & ViewCompat.MEASURED_SIZE_MASK;
        if (status != this.deviceStatus) {
            C2DeviceProxy.getInstance().setSystemStatusBLN(port, status, defaultNorColor);
        }
    }

    private void updateParkStatusBLN(String port) {
        int defaultColor = Color.parseColor(RemoteSettingCacheProvider.getInstance().getDefaultBLNColor()) & ViewCompat.MEASURED_SIZE_MASK;
        if (HardwareStatusCacheProvider.getInstance().getDeviceFaultStatus().getCode() != 200) {
            C2DeviceProxy.getInstance().setSystemStatusBLN((String) null, DEVICE_STATUS.notInited.getStatus(), defaultColor);
        } else if (this.deviceStatus < 10) {
            ErrorCode portFault = HardwareStatusCacheProvider.getInstance().getPort(port).getDeviceError();
            if (portFault.getCode() == 200) {
                C2DeviceProxy.getInstance().setSystemStatusBLN(port, this.deviceStatus, defaultColor);
            } else if (portFault.getCode() < 30010 || portFault.getCode() > 30018) {
                C2DeviceProxy.getInstance().setSystemStatusBLN((String) null, DEVICE_STATUS.notInited.getStatus(), defaultColor);
            } else {
                C2DeviceProxy.getInstance().setSystemStatusBLN((String) null, 10, defaultColor);
            }
        } else {
            C2DeviceProxy.getInstance().setSystemStatusBLN(port, this.deviceStatus, defaultColor);
        }
    }

    private void updateNetworkStatusBLN() {
        int defaultColor = Color.parseColor(RemoteSettingCacheProvider.getInstance().getDefaultBLNColor()) & ViewCompat.MEASURED_SIZE_MASK;
        if (HardwareStatusCacheProvider.getInstance().getDeviceFaultStatus().getCode() != 200) {
            C2DeviceProxy.getInstance().setSystemStatusBLN((String) null, DEVICE_STATUS.notInited.getStatus(), defaultColor);
        } else if (this.deviceStatus < 10) {
            ErrorCode portFault = HardwareStatusCacheProvider.getInstance().getPort("1").getDeviceError();
            if (portFault.getCode() == 200) {
                C2DeviceProxy.getInstance().setSystemStatusBLN("1", this.deviceStatus, defaultColor);
            } else if (portFault.getCode() < 30010 || portFault.getCode() > 30018) {
                C2DeviceProxy.getInstance().setSystemStatusBLN((String) null, DEVICE_STATUS.notInited.getStatus(), defaultColor);
            } else {
                C2DeviceProxy.getInstance().setSystemStatusBLN((String) null, 10, defaultColor);
            }
        } else {
            C2DeviceProxy.getInstance().setSystemStatusBLN("1", this.deviceStatus, defaultColor);
        }
    }

    private ErrorCode getErrorCode(DEVICE_STATUS status) {
        int ec = 200;
        switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$DEVICE_STATUS()[status.ordinal()]) {
            case 8:
                ec = ErrorCode.EC_DEVICE_NOT_INIT;
                break;
            case 9:
                ec = ErrorCode.EC_DEVICE_NO_GROUND;
                break;
            case 10:
                ec = ErrorCode.EC_DEVICE_LOST_PHASE;
                break;
            case PortRuntimeData.STATUS_EX_11:
                ec = ErrorCode.EC_DEVICE_EMERGENCY_STOP;
                break;
            case PortRuntimeData.STATUS_EX_12:
                ec = ErrorCode.EC_DEVICE_VOLT_ERROR;
                break;
            case 13:
                ec = ErrorCode.EC_DEVICE_AMP_ERROR;
                break;
            case 14:
                ec = ErrorCode.EC_DEVICE_TEMP_ERROR;
                break;
            case 15:
                ec = ErrorCode.EC_DEVICE_POWER_LEAK;
                break;
            case 16:
                ec = ErrorCode.EC_DEVICE_COMM_ERROR;
                break;
        }
        return new ErrorCode(ec);
    }
}
