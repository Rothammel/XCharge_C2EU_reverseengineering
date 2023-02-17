package com.xcharge.charger.device.c2.nfc;

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
import com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER;
import com.xcharge.charger.data.bean.type.WORK_MODE;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.NFCConsumeFailCacheContentProxy;
import com.xcharge.charger.data.proxy.NFCKeyContentProxy;
import com.xcharge.charger.device.c2.bean.AuthSign;
import com.xcharge.charger.device.c2.bean.CDDZCardKeySeeds;
import com.xcharge.charger.device.c2.bean.ManageCardData;
import com.xcharge.charger.device.c2.bean.NFCCardIDData;
import com.xcharge.charger.device.c2.bean.NFCEventData;
import com.xcharge.charger.device.c2.bean.NFCSign;
import com.xcharge.charger.device.c2.bean.PortRuntimeData;
import com.xcharge.charger.device.c2.bean.XSign;
import com.xcharge.charger.device.c2.nfc.charge.C2NFCChargeHandler;
import com.xcharge.charger.device.c2.service.C2DeviceEventDispatcher;
import com.xcharge.charger.device.c2.service.C2DeviceProxy;
import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.utils.BCDUtils;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;
import com.xcharge.common.utils.HandlerTimer;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.jni.echargenet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.CharEncoding;

/* loaded from: classes.dex */
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
    private Context context = null;
    private String port = null;
    private HandlerThread thread = null;
    private MsgHandler handler = null;
    private HandlerTimer handlerTimer = null;
    private C2NFCChargeHandler chargeHandler = null;
    private int cardUUID = 0;
    private NFC nfcStatus = new NFC();
    private NFC needRecoveryNFCStatus = null;
    private boolean isU1BindMode = false;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class MsgHandler extends Handler {
        private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$NFC_CARD_TYPE;

        static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$NFC_CARD_TYPE() {
            int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$NFC_CARD_TYPE;
            if (iArr == null) {
                iArr = new int[NFC_CARD_TYPE.valuesCustom().length];
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

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            HashMap<String, String> attachData;
            try {
                SwipeCardPermission swipeCardPermission = SystemSettingCacheProvider.getInstance().getPortSwipeCardPermission(C2NFCAgent.this.port);
                switch (msg.what) {
                    case C2NFCAgent.MSG_NFC_CARD_ARRIVAL /* 24577 */:
                        Log.i("C2NFCAgent.MsgHandler", "NFC card arrival !!! port: " + C2NFCAgent.this.port);
                        if (!C2NFCAgent.this.nfcStatus.isHandleStatus()) {
                            C2NFCAgent.this.nfcStatus.setArrived(true);
                            C2NFCAgent.this.nfcStatus.setHandleStatus(true);
                            C2NFCAgent.this.nfcStatus.setTs(0L);
                            C2NFCAgent.this.nfcStatus.setLatestCardNo(null);
                            C2NFCAgent.this.nfcStatus.setLatestCardType(null);
                            C2NFCAgent.this.nfcStatus.setLatestError(new ErrorCode(200));
                            C2NFCAgent.this.nfcStatus.setLatestOprType(null);
                            C2NFCAgent.this.nfcStatus.setBalance(null);
                            C2NFCAgent.this.nfcStatus.setFee(null);
                            C2DeviceEventDispatcher.getInstance().handleNFCStatus(C2NFCAgent.this.port, C2NFCAgent.this.nfcStatus);
                            NFCEventData data = new NFCEventData().fromJson((String) msg.obj);
                            NFCCardIDData cardIDData = NFCUtils.distinguishCard(data.getUuid(), data.getSzuuid());
                            if (cardIDData == null) {
                                C2NFCAgent.this.handler.sendMessage(C2NFCAgent.this.handler.obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_UNRECOGNIZED_CARD).toJson()));
                                return;
                            }
                            String cardNo = cardIDData.getCardNo();
                            NFC_CARD_TYPE cardType = cardIDData.getCardType();
                            C2NFCAgent.this.nfcStatus.setLatestCardType(cardType);
                            C2NFCAgent.this.nfcStatus.setLatestCardNo(cardNo);
                            Log.i("C2NFCAgent.MsgHandler", "card type: " + cardType + ", card no: " + cardNo + ", port: " + C2NFCAgent.this.port);
                            if (swipeCardPermission.isPermitTest()) {
                                Log.w("C2NFCAgent.MsgHandler", "in test mode, only distinguish card !!! card type: " + cardType + ", card no: " + cardNo + ", port: " + C2NFCAgent.this.port);
                                C2NFCAgent.this.handler.sendMessage(C2NFCAgent.this.handler.obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
                                return;
                            } else if (!C2NFCAgent.this.swipeCardPermissionCheck(cardType)) {
                                Log.w("C2NFCAgent.MsgHandler", "refuse for swipe card is forbiden!!! card type: " + cardType + ", port: " + C2NFCAgent.this.port);
                                C2NFCAgent.this.handler.sendMessage(C2NFCAgent.this.handler.obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_NOT_PERMIT_SWIPE).toJson()));
                                return;
                            } else {
                                WORK_MODE workMode = LocalSettingCacheProvider.getInstance().getChargeSetting().getWorkMode();
                                switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$NFC_CARD_TYPE()[cardType.ordinal()]) {
                                    case 1:
                                        if (C2NFCAgent.this.chargeHandler.isCharging()) {
                                            Log.w("C2NFCAgent.MsgHandler", "NFC charging ..., can not set now !!! port: " + C2NFCAgent.this.port);
                                            C2NFCAgent.this.handler.sendMessage(C2NFCAgent.this.handler.obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_SET_REFUSE).toJson()));
                                            break;
                                        } else {
                                            C2NFCAgent.this.handleM1Card(cardNo, data);
                                            break;
                                        }
                                    case 2:
                                        if (C2NFCAgent.this.chargeHandler.isCharging()) {
                                            Log.w("C2NFCAgent.MsgHandler", "NFC charging ..., can not set now !!! port: " + C2NFCAgent.this.port);
                                            C2NFCAgent.this.handler.sendMessage(C2NFCAgent.this.handler.obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_SET_REFUSE).toJson()));
                                            break;
                                        } else {
                                            C2NFCAgent.this.handleM2Card(cardNo, data);
                                            break;
                                        }
                                    case 3:
                                        if (!"U100000000000000".equals(cardNo)) {
                                            if (!workMode.equals(WORK_MODE.personal)) {
                                                Log.w("C2NFCAgent.MsgHandler", "refuse scan U1 !!! port: " + C2NFCAgent.this.port + ", work mode: " + workMode.getMode());
                                                C2NFCAgent.this.handler.sendMessage(C2NFCAgent.this.handler.obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_NOT_PERSONAL_MODE).toJson()));
                                                break;
                                            } else {
                                                C2NFCAgent.this.handleU1Card(cardNo, data);
                                                break;
                                            }
                                        } else {
                                            C2NFCAgent.this.handleU1Card(cardNo, data);
                                            break;
                                        }
                                    case 4:
                                        if (!workMode.equals(WORK_MODE.group)) {
                                            if (C2NFCAgent.this.isU1BindMode) {
                                                Log.w("C2NFCAgent.MsgHandler", "refuse scan U2 for in U1 binding now !!! port: " + C2NFCAgent.this.port + ", work mode: " + workMode.getMode());
                                                C2NFCAgent.this.handler.sendMessage(C2NFCAgent.this.handler.obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_NOT_PERMIT_SWIPE).toJson()));
                                                break;
                                            } else {
                                                Log.w("C2NFCAgent.MsgHandler", "refuse scan U2 !!! port: " + C2NFCAgent.this.port + ", work mode: " + workMode.getMode());
                                                C2NFCAgent.this.handler.sendMessage(C2NFCAgent.this.handler.obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_NOT_GROUP_MODE).toJson()));
                                                break;
                                            }
                                        } else {
                                            C2NFCAgent.this.handleU2Card(cardNo, data);
                                            break;
                                        }
                                    case 5:
                                        if (ChargeStatusCacheProvider.getInstance().isCloudConnected()) {
                                            if (C2NFCAgent.this.isU1BindMode) {
                                                Log.w("C2NFCAgent.MsgHandler", "refuse scan U3 for in U1 binding now !!! port: " + C2NFCAgent.this.port + ", work mode: " + workMode.getMode());
                                                C2NFCAgent.this.handler.sendMessage(C2NFCAgent.this.handler.obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_NOT_PERMIT_SWIPE).toJson()));
                                                break;
                                            } else {
                                                C2NFCAgent.this.handleU3Card(cardNo, data);
                                                break;
                                            }
                                        } else {
                                            Log.w("C2NFCAgent.MsgHandler", "refuse scan U3 for unavailable cloud service !!! port: " + C2NFCAgent.this.port + ", work mode: " + workMode.getMode());
                                            C2NFCAgent.this.handler.sendMessage(C2NFCAgent.this.handler.obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_UNAVAILABLE_CLOUD).toJson()));
                                            break;
                                        }
                                    case 6:
                                        C2NFCAgent.this.handleCTDemoCard(cardNo, data);
                                        break;
                                    case 7:
                                        if (!ChargeStatusCacheProvider.getInstance().isCloudConnected()) {
                                            if (!PLATFORM_CUSTOMER.anyo_svw.equals(SystemSettingCacheProvider.getInstance().getPlatformCustomer())) {
                                                Log.w("C2NFCAgent.MsgHandler", "refuse scan anyo online card for unavailable cloud service !!! port: " + C2NFCAgent.this.port);
                                                C2NFCAgent.this.handler.sendMessage(C2NFCAgent.this.handler.obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_UNAVAILABLE_CLOUD).toJson()));
                                                break;
                                            } else {
                                                C2NFCAgent.this.handleAnyoSvwCard(cardNo, data);
                                                break;
                                            }
                                        } else {
                                            C2NFCAgent.this.handleAnyo1Card(cardNo, data);
                                            break;
                                        }
                                    case PortRuntimeData.STATUS_EX_11 /* 11 */:
                                        if (C2NFCAgent.this.chargeHandler.isCharging()) {
                                            Log.w("C2NFCAgent.MsgHandler", "NFC charging ..., can not set now !!! port: " + C2NFCAgent.this.port);
                                            C2NFCAgent.this.handler.sendMessage(C2NFCAgent.this.handler.obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_SET_REFUSE).toJson()));
                                            break;
                                        } else {
                                            C2NFCAgent.this.handleCDDZMCard(cardIDData);
                                            break;
                                        }
                                    case PortRuntimeData.STATUS_EX_12 /* 12 */:
                                        if (!ChargeStatusCacheProvider.getInstance().isCloudConnected()) {
                                            Log.w("C2NFCAgent.MsgHandler", "refuse scan cddz jianquan card for unavailable cloud service !!! port: " + C2NFCAgent.this.port);
                                            C2NFCAgent.this.handler.sendMessage(C2NFCAgent.this.handler.obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_UNAVAILABLE_CLOUD).toJson()));
                                            break;
                                        } else {
                                            C2NFCAgent.this.handleCDDZJianQuanCard(cardIDData);
                                            break;
                                        }
                                    case 14:
                                        C2NFCAgent.this.handleOCPPCard(cardNo, data);
                                        break;
                                }
                            }
                        } else {
                            Log.w("C2NFCAgent.MsgHandler", "refuse because latest swipe is processing: " + C2NFCAgent.this.nfcStatus.toJson());
                            NFC storedNFCStatus = C2NFCAgent.this.nfcStatus.m8clone();
                            C2NFCAgent.this.nfcStatus.setHandleStatus(false);
                            C2NFCAgent.this.nfcStatus.setLatestError(new ErrorCode(ErrorCode.EC_NFC_SWIPE_PROCESSING));
                            C2NFCAgent.this.nfcStatus.setTs(System.currentTimeMillis());
                            C2DeviceEventDispatcher.getInstance().handleNFCStatus(C2NFCAgent.this.port, C2NFCAgent.this.nfcStatus);
                            C2DeviceProxy.getInstance().beep(3);
                            C2NFCAgent.this.nfcStatus = storedNFCStatus;
                            C2DeviceEventDispatcher.getInstance().handleNFCStatus(C2NFCAgent.this.port, C2NFCAgent.this.nfcStatus);
                            return;
                        }
                    case C2NFCAgent.MSG_NFC_CARD_LEFT /* 24578 */:
                        String uuid = (String) msg.obj;
                        Log.i("C2NFCAgent.MsgHandler", "NFC card left !!! port: " + C2NFCAgent.this.port + ", uuid: " + uuid);
                        C2NFCAgent.this.nfcStatus.setArrived(false);
                        C2DeviceEventDispatcher.getInstance().handleNFCStatus(C2NFCAgent.this.port, C2NFCAgent.this.nfcStatus);
                        break;
                    case C2NFCAgent.MSG_NFC_CARD_BIND_U1 /* 24579 */:
                        Bundle u1 = msg.getData();
                        byte[] key = u1.getByteArray("key");
                        String sign = u1.getString("sign");
                        String bindCardNo = u1.getString("cardNo");
                        Log.i("C2NFCAgent.MsgHandler", "bind U1 !!!  port: " + C2NFCAgent.this.port + ", data: " + u1.toString());
                        NFCSign nfcSign = new NFCSign().fromJson(sign);
                        if (!NFCUtils.setUserCardSign(key, nfcSign)) {
                            C2NFCAgent.this.handler.sendMessage(C2NFCAgent.this.handler.obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_BIND_CARD_FAIL).toJson()));
                            break;
                        } else {
                            LogUtils.syslog("U1 card: " + bindCardNo + ", binded to this pile !!!");
                            C2NFCAgent.this.handler.sendEmptyMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK);
                            break;
                        }
                    case C2NFCAgent.MSG_NFC_SWIPE_CARD_OK /* 24580 */:
                        Log.i("C2NFCAgent.MsgHandler", "NFC swipe card OK, port: " + C2NFCAgent.this.port);
                        C2NFCAgent.this.nfcStatus.setHandleStatus(false);
                        C2NFCAgent.this.nfcStatus.setLatestError(new ErrorCode(200));
                        C2NFCAgent.this.nfcStatus.setTs(System.currentTimeMillis());
                        C2DeviceEventDispatcher.getInstance().handleNFCStatus(C2NFCAgent.this.port, C2NFCAgent.this.nfcStatus);
                        if (swipeCardPermission.isPermitTest()) {
                            C2DeviceProxy.getInstance().beep(2);
                        } else if (!"U100000000000000".equals(C2NFCAgent.this.nfcStatus.getLatestCardNo())) {
                            C2DeviceProxy.getInstance().beep(1);
                        }
                        if ("U100000000000000".equals(C2NFCAgent.this.nfcStatus.getLatestCardNo())) {
                            C2NFCAgent.getInstance(C2NFCAgent.this.port).sendMessage(C2NFCAgent.getInstance(C2NFCAgent.this.port).obtainMessage(C2NFCAgent.MSG_NFC_CARD_LEFT, "0"));
                            break;
                        }
                        break;
                    case C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR /* 24581 */:
                        String error = (String) msg.obj;
                        Log.i("C2NFCAgent.MsgHandler", "NFC swipe card error, port: " + C2NFCAgent.this.port + ", error: " + error);
                        ErrorCode errorCode = new ErrorCode().fromJson(error);
                        C2NFCAgent.this.nfcStatus.setHandleStatus(false);
                        C2NFCAgent.this.nfcStatus.setLatestError(errorCode);
                        if ((40019 == errorCode.getCode() || 40017 == errorCode.getCode()) && (attachData = errorCode.getData()) != null) {
                            C2NFCAgent.this.nfcStatus.setBalance(Integer.valueOf(Integer.parseInt(attachData.get(ContentDB.NFCConsumeFailCacheTable.BALANCE))));
                            C2NFCAgent.this.nfcStatus.setFee(Integer.valueOf(Integer.parseInt(attachData.get(ChargeStopCondition.TYPE_FEE))));
                        }
                        C2NFCAgent.this.nfcStatus.setTs(System.currentTimeMillis());
                        C2DeviceEventDispatcher.getInstance().handleNFCStatus(C2NFCAgent.this.port, C2NFCAgent.this.nfcStatus);
                        if (errorCode.getCode() == 40016) {
                            C2DeviceProxy.getInstance().beep(1);
                        } else {
                            LogUtils.syslog("swipe card error: " + error);
                            if (!"U100000000000000".equals(C2NFCAgent.this.nfcStatus.getLatestCardNo())) {
                                C2DeviceProxy.getInstance().beep(3);
                            }
                        }
                        if (40014 == errorCode.getCode() || 40013 == errorCode.getCode() || 40019 == errorCode.getCode() || 40017 == errorCode.getCode() || 40021 == errorCode.getCode()) {
                            C2NFCAgent.this.needRecoveryNFCStatus = C2NFCAgent.this.nfcStatus.m8clone();
                        }
                        if ("U100000000000000".equals(C2NFCAgent.this.nfcStatus.getLatestCardNo())) {
                            C2NFCAgent.getInstance(C2NFCAgent.this.port).sendMessage(C2NFCAgent.getInstance(C2NFCAgent.this.port).obtainMessage(C2NFCAgent.MSG_NFC_CARD_LEFT, "0"));
                            break;
                        }
                        break;
                    case C2NFCAgent.MSG_NFC_CANCEL_U1_BIND /* 24582 */:
                        Log.i("C2NFCAgent.MsgHandler", "User cancel U1 bind mode, port: " + C2NFCAgent.this.port);
                        C2NFCAgent.this.handlerTimer.stopTimer(C2NFCAgent.MSG_TIMEOUT_NFC_U1_BIND);
                        C2NFCAgent.this.isU1BindMode = false;
                        break;
                    case C2NFCAgent.MSG_TIMEOUT_NFC_U1_BIND /* 24592 */:
                        Log.i("C2NFCAgent.MsgHandler", "timeout in U1 bind mode, port: " + C2NFCAgent.this.port);
                        C2NFCAgent.this.isU1BindMode = false;
                        break;
                }
            } catch (Exception e) {
                Log.e("C2NFCAgent.handleMessage", "except: " + Log.getStackTraceString(e));
                LogUtils.syslog("C2NFCAgent handleMessage exception: " + Log.getStackTraceString(e));
                if (C2NFCAgent.this.nfcStatus.isHandleStatus()) {
                    C2NFCAgent.this.handler.sendMessage(C2NFCAgent.this.handler.obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_ERROR).toJson()));
                }
            }
            super.handleMessage(msg);
        }
    }

    public static C2NFCAgent getInstance(String port) {
        if (instances == null) {
            instances = new HashMap<>();
        }
        C2NFCAgent agent = instances.get(port);
        if (agent == null) {
            C2NFCAgent agent2 = new C2NFCAgent();
            agent2.port = port;
            instances.put(port, agent2);
            return agent2;
        }
        return agent;
    }

    public void init(Context context) {
        this.context = context;
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
        boolean present = data.isPresent();
        if (present) {
            this.cardUUID = data.getUuid();
            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_CARD_ARRIVAL, data.toJson()));
            return;
        }
        this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_CARD_LEFT, String.format("%d", Integer.valueOf(this.cardUUID))));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleM1Card(String cardNo, NFCEventData data) {
        XKeyseed xKeyseed = NFCUtils.getM1PrivateAreaXKeySeed();
        if (xKeyseed == null) {
            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_UNAVAILABLE_KEYSEED).toJson()));
            return;
        }
        byte[] key = NFCUtils.getPrivateKey(data.getUuid(), cardNo, xKeyseed.getSeed());
        ManageCardData M1CardData = NFCUtils.getManageCardInfo(key, NFC_CARD_TYPE.M1.getType());
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
            return;
        }
        boolean isOk = set(M1CardData, NFC_CARD_TYPE.M1, cardNo);
        if (!isOk) {
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

    /* JADX INFO: Access modifiers changed from: private */
    public void handleM2Card(String cardNo, NFCEventData data) {
        String groupId = NFCUtils.getGroupID(cardNo);
        XKeyseed xKeyseed = NFCKeyContentProxy.getInstance().getKeyseed(groupId, NFC_CARD_TYPE.M2.getType());
        if (xKeyseed == null) {
            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_UNAVAILABLE_KEYSEED).toJson()));
            return;
        }
        byte[] key = NFCUtils.getPrivateKey(data.getUuid(), cardNo, xKeyseed.getSeed());
        ManageCardData M2CardData = NFCUtils.getManageCardInfo(key, NFC_CARD_TYPE.M2.getType());
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
            return;
        }
        boolean isOk = set(M2CardData, NFC_CARD_TYPE.M2, cardNo);
        if (!isOk) {
            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_SET_FAIL).toJson()));
            return;
        }
        if (WORK_MODE.personal.equals(LocalSettingCacheProvider.getInstance().getChargeSetting().getWorkMode())) {
            this.handlerTimer.stopTimer(MSG_TIMEOUT_NFC_U1_BIND);
            this.isU1BindMode = true;
            this.handlerTimer.startTimer(60000L, MSG_TIMEOUT_NFC_U1_BIND, null);
        }
        this.handler.sendEmptyMessage(MSG_NFC_SWIPE_CARD_OK);
    }

    private boolean M2Clear(String groupId) {
        NFCKeyContentProxy.getInstance().clearKeyseed(groupId, null);
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
                HardwareStatusCacheProvider.getInstance().updateAmpCapacity(m1SettedCapacityAmp);
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
                    HardwareStatusCacheProvider.getInstance().updateAmpCapacity(m2SettedCapacityAmp);
                    ChargeStatusCacheProvider.getInstance().updateAmpCapacity(m2SettedCapacityAmp);
                } else {
                    chargeSetting.setManufactoryAmpCapacity(m2SettedCapacityAmp);
                    chargeSetting.setAmpCapacity(m2SettedCapacityAmp);
                    C2DeviceProxy.getInstance().setAmpCapacity(m2SettedCapacityAmp);
                    HardwareStatusCacheProvider.getInstance().updateAmpCapacity(m2SettedCapacityAmp);
                    ChargeStatusCacheProvider.getInstance().updateAmpCapacity(m2SettedCapacityAmp);
                }
            }
            adjustAmp = m2SettedCapacityAmp;
            chargeSetting.setAdjustAmp(adjustAmp);
        }
        HashMap<String, PortSetting> ports = chargeSetting.getPortsSetting();
        for (Map.Entry<String, PortSetting> entry : ports.entrySet()) {
            String portNo = entry.getKey();
            PortSetting port = entry.getValue();
            int gunLockMode = setting.getElecLockMode();
            if (gunLockMode != -1) {
                GunLockSetting gunLockSetting = port.getGunLockSetting();
                gunLockSetting.setMode(GUN_LOCK_MODE.valueBy(gunLockMode));
                port.setGunLockSetting(gunLockSetting);
            }
            String radarMode = setting.getRadermode();
            if (!TextUtils.isEmpty(radarMode)) {
                RadarSetting radarSetting = port.getRadarSetting();
                if (radarMode.equals("enable")) {
                    radarSetting.setEnable(true);
                } else if (radarMode.equals("disable")) {
                    radarSetting.setEnable(false);
                }
                port.setRadarSetting(radarSetting);
                HardwareStatusCacheProvider.getInstance().updatePortRadarSwitch(portNo, radarSetting.isEnable());
            }
            double capacityAmp = HardwareStatusCacheProvider.getInstance().getAmpCapacity();
            if (adjustAmp >= 6 && adjustAmp <= capacityAmp) {
                int ampPercent = port.getAmpPercent().intValue();
                int portAdjustAmp = (adjustAmp * ampPercent) / 10000;
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
                String port2 = "80";
                String[] serverAndPort = consoleInfo.split(":");
                if (serverAndPort.length == 2) {
                    server = serverAndPort[0];
                    port2 = serverAndPort[1];
                }
                try {
                    int prt = Integer.parseInt(port2);
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
            ArrayList<ArrayList<Integer>> priceSections = setting.getTimedPrice();
            FeeRate feeRate = DCAPProxy.getInstance().formatFeeRate(priceSections);
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
                    entry2.getKey();
                    PortFeeRate portFeeRate = entry2.getValue();
                    HashMap<String, FeeRate> feeRates = portFeeRate.getFeeRates();
                    if (feeRates == null) {
                        feeRates = new HashMap<>();
                    }
                    feeRate.setFeeRateId(cardNo);
                    feeRates.put(cardNo, feeRate);
                    portFeeRate.setFeeRates(feeRates);
                    portFeeRate.setActiveFeeRateId(cardNo);
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

    /* JADX INFO: Access modifiers changed from: private */
    public void handleU1Card(String cardNo, NFCEventData data) {
        if (!"U100000000000000".equals(cardNo)) {
            String groupId = NFCUtils.getGroupID(cardNo);
            XKeyseed xKeyseed = NFCKeyContentProxy.getInstance().getKeyseed(groupId, NFC_CARD_TYPE.U1.getType());
            if (xKeyseed == null) {
                this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_UNAVAILABLE_KEYSEED).toJson()));
                return;
            }
            String realKey = xKeyseed.getSeed().substring(0, xKeyseed.getSeed().length() - 36);
            byte[] key = NFCUtils.getPrivateKey(data.getUuid(), cardNo, realKey);
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
            } else {
                SwipeCardPermission swipeCardPermission = SystemSettingCacheProvider.getInstance().getPortSwipeCardPermission(data.getPort());
                if (!swipeCardPermission.isPermitBinding()) {
                    Log.w("C2NFCAgent.handleU1Card", "bind U1 card is forbiden!!! port: " + data.getPort());
                    this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_NOT_PERMIT_SWIPE).toJson()));
                    return;
                }
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

    /* JADX INFO: Access modifiers changed from: private */
    public void handleU2Card(String cardNo, NFCEventData data) {
        String groupId = NFCUtils.getGroupID(cardNo);
        XKeyseed xKeyseed = NFCKeyContentProxy.getInstance().getKeyseed(groupId, NFC_CARD_TYPE.U2.getType());
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
        String sn = HardwareStatusCacheProvider.getInstance().getSn();
        if (!NFCUtils.checkU2Sign(NFCUtils.intToBytes(balance), key, sign, sn.getBytes()) && !NFCUtils.checkU2Sign(NFCUtils.intToBytes(balance), key, sign, null)) {
            ConsumeFailCache failedCache = NFCConsumeFailCacheContentProxy.getInstance().getConsumeFailCache(NFC_CARD_TYPE.U2, uuid, cardNo);
            if (failedCache != null) {
                if (failedCache.getBalance() == balance) {
                    if (NFCUtils.setUserCardSign(key, NFCUtils.signU2(NFCUtils.intToBytes(failedCache.getBalance()), key, failedCache.getCount(), null))) {
                        int lns = NFCConsumeFailCacheContentProxy.getInstance().removeConsumeFailCache(NFC_CARD_TYPE.U2, uuid, cardNo);
                        if (lns < 1) {
                            Log.w("C2NFCAgent.handleU2Card", "failed to remove consume fail cache: " + failedCache.toJson());
                        }
                    } else {
                        Log.w("C2NFCAgent.handleU2Card", "failed to recovery balance sign !!! port: " + this.port + ", uuid: " + uuid + ", card no: " + cardNo + ", balance: " + balance);
                        this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_RECOVERY_SIGN_FAIL).toJson()));
                        return;
                    }
                } else {
                    Log.w("C2NFCAgent.handleU2Card", "balance maybe been rewrited !!! port: " + this.port + ", uuid: " + uuid + ", card no: " + cardNo + ", card balance: " + balance + ", cached balance: " + failedCache.getBalance());
                    this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_REWRITED_BALANCE).toJson()));
                    return;
                }
            } else {
                Log.w("C2NFCAgent.handleU2Card", "illegal sigin !!! port: " + this.port + ", uuid: " + uuid + ", card no: " + cardNo + ", balance: " + balance);
                if (this.needRecoveryNFCStatus != null && cardNo.equals(this.needRecoveryNFCStatus.getLatestCardNo())) {
                    if (System.currentTimeMillis() - this.needRecoveryNFCStatus.getTs() <= 120000) {
                        int errorCode = this.needRecoveryNFCStatus.getLatestError().getCode();
                        if (40019 == errorCode || 40017 == errorCode) {
                            if (balance == this.needRecoveryNFCStatus.getBalance().intValue()) {
                                if (!NFCUtils.setUserCardSign(key, NFCUtils.signU2(NFCUtils.intToBytes(balance), key, 0, null))) {
                                    Log.w("C2NFCAgent.handleU2Card", "failed to recovery balance sign error !!! port: " + this.port + ", uuid: " + uuid + ", card no: " + cardNo + ", balance: " + balance);
                                    this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_RECOVERY_SIGN_FAIL).toJson()));
                                    this.needRecoveryNFCStatus = null;
                                    return;
                                }
                            } else {
                                Log.w("C2NFCAgent.handleU2Card", "illegal sigin, maybe balance write error !!! port: " + this.port + ", uuid: " + uuid + ", card no: " + cardNo + ", balance: " + balance + ", correct balance: " + this.needRecoveryNFCStatus.getBalance() + ", fee: " + this.needRecoveryNFCStatus.getFee());
                                this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_SIGN_ERROR).toJson()));
                                this.needRecoveryNFCStatus = null;
                                return;
                            }
                        } else if (!NFCUtils.setUserCardSign(key, NFCUtils.signU2(NFCUtils.intToBytes(balance), key, 0, null))) {
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
                } else {
                    Log.w("C2NFCAgent.handleU2Card", "illegal sigin, maybe reserved by other pile !!! port: " + this.port + ", uuid: " + uuid + ", card no: " + cardNo + ", balance: " + balance);
                    this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_CARD_RESERVED).toJson()));
                    return;
                }
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

    /* JADX INFO: Access modifiers changed from: private */
    public void handleU3Card(String cardNo, NFCEventData data) {
        XKeyseed xKeyseed = NFCUtils.getU3PrivateAreaXKeySeed();
        if (xKeyseed == null) {
            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_UNAVAILABLE_KEYSEED).toJson()));
            return;
        }
        byte[] key = NFCUtils.getPrivateKey(data.getUuid(), cardNo, xKeyseed.getSeed());
        AuthSign sign = NFCUtils.getAuthSign(key);
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

    /* JADX INFO: Access modifiers changed from: private */
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

    /* JADX INFO: Access modifiers changed from: private */
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

    /* JADX INFO: Access modifiers changed from: private */
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

    /* JADX INFO: Access modifiers changed from: private */
    public void handleCDDZMCard(NFCCardIDData id) {
        try {
            byte[] key = NFCUtils.CDDZ_MCARD_KEYA.getBytes(CharEncoding.UTF_8);
            byte[] data = chargerhdNative.chargerhdNFCRead(6, key);
            if (data == null) {
                this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_READ_FAILED).toJson()));
                return;
            }
            long expireTime = EndianUtils.littleBytesToInt(data) & XMSZHead.ID_BROADCAST;
            if (1000 * expireTime < System.currentTimeMillis()) {
                this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_READ_FAILED).toJson()));
                return;
            }
            byte[] data2 = chargerhdNative.chargerhdNFCRead(4, key);
            if (data2 == null) {
                this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_READ_FAILED).toJson()));
                return;
            }
            byte[] seedAKey = "e5hgnKxfr458Fy69".getBytes(CharEncoding.UTF_8);
            String keyASeed = FormatUtils.bytesToHexString(echargenet.aesdecrypt(seedAKey, data2, 1));
            if (TextUtils.isEmpty(keyASeed)) {
                this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_READ_FAILED).toJson()));
                return;
            }
            byte[] data3 = chargerhdNative.chargerhdNFCRead(5, key);
            if (data3 == null) {
                this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_READ_FAILED).toJson()));
                return;
            }
            byte[] seedBKey = "55huiKxfr458Fy60".getBytes(CharEncoding.UTF_8);
            String keyBSeed = FormatUtils.bytesToHexString(echargenet.aesdecrypt(seedBKey, data3, 1));
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
                ChargeSetting chargeSetting = LocalSettingCacheProvider.getInstance().getChargeSetting();
                chargeSetting.setOperatorId(String.valueOf(operatorCode));
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
        } catch (Exception e) {
            Log.e("C2NFCAgent.handleCDDZMCard", "except: " + Log.getStackTraceString(e));
            this.handler.sendMessage(this.handler.obtainMessage(MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_ERROR).toJson()));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
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

    /* JADX INFO: Access modifiers changed from: private */
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

    /* JADX INFO: Access modifiers changed from: private */
    public boolean swipeCardPermissionCheck(NFC_CARD_TYPE cardType) {
        SwipeCardPermission swipeCardPermission = SystemSettingCacheProvider.getInstance().getPortSwipeCardPermission(this.port);
        if (NFC_CARD_TYPE.M1.equals(cardType) || NFC_CARD_TYPE.M2.equals(cardType)) {
            return swipeCardPermission.isPermitSetting();
        } else if (NFC_CARD_TYPE.U2.equals(cardType) || NFC_CARD_TYPE.U3.equals(cardType) || NFC_CARD_TYPE.anyo1.equals(cardType) || NFC_CARD_TYPE.anyo_svw.equals(cardType) || NFC_CARD_TYPE.CT_DEMO.equals(cardType) || NFC_CARD_TYPE.ocpp.equals(cardType)) {
            return swipeCardPermission.isPermitChargeCtrl();
        } else if (NFC_CARD_TYPE.U1.equals(cardType)) {
            return swipeCardPermission.isPermitChargeCtrl() || swipeCardPermission.isPermitBinding();
        } else {
            return true;
        }
    }
}
