package com.xcharge.charger.core.controller;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.bean.AckDirective;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.NackDirective;
import com.xcharge.charger.core.api.bean.cap.CAPDirectiveOption;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.core.api.bean.cap.QueryDirective;
import com.xcharge.charger.core.api.bean.cap.SetDirective;
import com.xcharge.charger.core.api.bean.cap.UpgradeDirective;
import com.xcharge.charger.core.bean.GatewaySession;
import com.xcharge.charger.core.bean.IndicateSession;
import com.xcharge.charger.core.bean.RequestSession;
import com.xcharge.charger.core.handler.ChargeHandler;
import com.xcharge.charger.core.handler.UpgradeAgent;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.UpgradeProgress;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.setting.SwipeCardPermission;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.device.adpter.DeviceProxy;
import com.xcharge.charger.ui.adapter.api.UIServiceProxy;
import com.xcharge.charger.ui.adapter.type.CHALLENGE_TYPE;
import com.xcharge.charger.ui.adapter.type.UI_MODE;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.HandlerTimer;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.TimeUtils;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/* loaded from: classes.dex */
public class OSSController {
    private static final long INTERVAL_UPGRADE_CHECK = 30000;
    public static final int MSG_CHARGE_TO_IDLE = 196613;
    public static final int MSG_DELAYED_UPGRADE_CHECK = 196612;
    public static final int MSG_DUMMY = 196608;
    public static final int MSG_REQUEST_QUERY = 196609;
    public static final int MSG_REQUEST_SET = 196610;
    public static final int MSG_REQUEST_UPGRADE = 196611;
    public static final int MSG_RESPONSE_QUERY = 196614;
    public static final int MSG_TIMEOUT_DCAP_GATEWAY_SESSION = 196615;
    public static final long TIMEOUT_DEFAULT_DCAP_GATEWAY_SESSION = 30000;
    private static OSSController instance = null;
    private Context context = null;
    private HandlerThread thread = null;
    private MsgHandler handler = null;
    private HandlerTimer handlerTimer = null;
    private RequestSession delayedUpgradeRequest = null;
    private boolean isDelayCloudTimeSynch = false;
    private long cloud_ts = 0;
    private long local_ts = 0;
    private boolean isDelayLocaleSetting = false;
    private HashMap<String, Object> delayLocaleSetting = null;
    private HashMap<String, GatewaySession> dcapGatewaySessions = null;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case OSSController.MSG_REQUEST_QUERY /* 196609 */:
                        RequestSession requestSession = (RequestSession) msg.obj;
                        Log.i("OSSController.handleMessage", "receive query request: " + requestSession.getRequest().toJson());
                        OSSController.this.handleQueryRequest(requestSession.getRequest(), requestSession.getConfirm());
                        break;
                    case OSSController.MSG_REQUEST_SET /* 196610 */:
                        RequestSession requestSession2 = (RequestSession) msg.obj;
                        Log.i("OSSController.handleMessage", "receive set request: " + requestSession2.getRequest().toJson());
                        OSSController.this.handleSetRequest(requestSession2.getRequest(), requestSession2.getConfirm());
                        break;
                    case OSSController.MSG_REQUEST_UPGRADE /* 196611 */:
                        RequestSession requestSession3 = (RequestSession) msg.obj;
                        Log.i("OSSController.handleMessage", "receive upgrade request: " + requestSession3.getRequest().toJson());
                        OSSController.this.handleUpgradeRequest(requestSession3.getRequest(), requestSession3.getConfirm());
                        break;
                    case OSSController.MSG_DELAYED_UPGRADE_CHECK /* 196612 */:
                        OSSController.this.handleDelayedUpgradeCheck();
                        break;
                    case OSSController.MSG_CHARGE_TO_IDLE /* 196613 */:
                        String port = (String) msg.obj;
                        Log.d("OSSController.handleMessage", "receive port charge to idle event, port: " + port);
                        OSSController.this.handlePortCharge2Idle(port);
                        break;
                    case OSSController.MSG_RESPONSE_QUERY /* 196614 */:
                        DCAPMessage response = (DCAPMessage) msg.obj;
                        Log.i("OSSController.handleMessage", "receive query response: " + response.toJson());
                        OSSController.this.handleQueryResponse(response);
                        break;
                    case OSSController.MSG_TIMEOUT_DCAP_GATEWAY_SESSION /* 196615 */:
                        GatewaySession gatewaySession = (GatewaySession) msg.obj;
                        Log.d("OSSController.handleMessage", "timeout on gateway session: " + gatewaySession.toJson());
                        OSSController.this.dcapGatewaySessions.remove(String.valueOf(gatewaySession.getIndicateSession().getIndicate().getSeq()));
                        ChargeController.nackConfirm(gatewaySession.getRequestSession().getConfirm(), ErrorCode.EC_INTERNAL_ERROR, "internal error", null);
                        break;
                }
            } catch (Exception e) {
                Log.e("OSSController.handleMessage", "except: " + Log.getStackTraceString(e));
                LogUtils.syslog("OSSController handleMessage exception: " + Log.getStackTraceString(e));
            }
            super.handleMessage(msg);
        }
    }

    public static OSSController getInstance() {
        if (instance == null) {
            instance = new OSSController();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        this.dcapGatewaySessions = new HashMap<>();
        UpgradeAgent.getInstance().init(this.context);
        this.thread = new HandlerThread("OSSController", 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context);
    }

    public void destroy() {
        this.handlerTimer.destroy();
        this.handler.removeMessages(MSG_REQUEST_QUERY);
        this.handler.removeMessages(MSG_REQUEST_SET);
        this.handler.removeMessages(MSG_REQUEST_UPGRADE);
        this.handler.removeMessages(MSG_DELAYED_UPGRADE_CHECK);
        this.handler.removeMessages(MSG_CHARGE_TO_IDLE);
        this.handler.removeMessages(MSG_RESPONSE_QUERY);
        this.handler.removeMessages(MSG_TIMEOUT_DCAP_GATEWAY_SESSION);
        this.thread.quit();
        UpgradeAgent.getInstance().destroy();
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

    public void handleRequest(DCAPMessage request, DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) request.getData();
        String op = cap.getOp();
        int msgId = MSG_DUMMY;
        try {
            if ("query".equals(op)) {
                QueryDirective query = new QueryDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(query);
                msgId = MSG_REQUEST_QUERY;
            } else if ("set".equals(op)) {
                SetDirective set = new SetDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(set);
                msgId = MSG_REQUEST_SET;
            } else if ("upgrade".equals(op)) {
                UpgradeDirective upgrade = new UpgradeDirective().fromJson2(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(upgrade);
                msgId = MSG_REQUEST_UPGRADE;
            }
            if (msgId != 196608) {
                RequestSession requestSession = new RequestSession();
                requestSession.setRequest(request);
                requestSession.setConfirm(confirm);
                this.handler.sendMessage(this.handler.obtainMessage(msgId, requestSession));
            }
        } catch (Exception e) {
            Log.e("OSSController.handleRequest", "request: " + request.toJson() + ", exception: " + Log.getStackTraceString(e));
            ChargeController.nackConfirm(confirm, ErrorCode.EC_INTERNAL_ERROR, e.toString(), null);
        }
    }

    public void handleResponse(DCAPMessage response) {
        CAPMessage cap = (CAPMessage) response.getData();
        String op = cap.getOp();
        String peerOp = cap.getOpt().getOp();
        int msgId = MSG_DUMMY;
        try {
            if ("ack".equals(op)) {
                AckDirective ack = new AckDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(ack);
                if ("query".equals(peerOp)) {
                    msgId = MSG_RESPONSE_QUERY;
                }
            } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
                NackDirective nack = new NackDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(nack);
                if ("query".equals(peerOp)) {
                    msgId = MSG_RESPONSE_QUERY;
                }
            } else {
                return;
            }
            if (msgId != 196608) {
                this.handler.sendMessage(this.handler.obtainMessage(msgId, response));
            }
        } catch (Exception e) {
            Log.e("OSSController.handleResponse", "response: " + response.toJson() + ", exception: " + Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleQueryRequest(DCAPMessage request, DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) request.getData();
        String queryId = cap.getOpt().getQuery_id();
        if (queryId.startsWith("device.port.")) {
            String[] queryIdSplit = queryId.split("\\.");
            String port = queryIdSplit[2];
            Object portStatus = DeviceProxy.getInstance().getPortRuntimeStatus(port);
            if (portStatus != null) {
                HashMap<String, Object> attach = new HashMap<>();
                attach.put(queryId, portStatus);
                ChargeController.ackConfirm(confirm, attach);
                return;
            }
        } else if (queryId.equals("device.verification")) {
            QueryDirective query = (QueryDirective) cap.getData();
            HashMap<String, Object> params = query.getParams();
            String customer = (String) params.get("customer");
            String expired = (String) params.get("expired");
            String xid = String.valueOf(request.getSeq());
            Bundle data = new Bundle();
            data.putString("type", CHALLENGE_TYPE.verification.getType());
            data.putString("xid", xid);
            data.putString("customer", customer);
            data.putInt("expired", Integer.parseInt(expired));
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.challenge, data);
            return;
        } else if (queryId.startsWith(QueryDirective.QUERY_ID_PORT_PLUGIN_UPDATE)) {
            String[] queryIdSplit2 = queryId.split("\\.");
            String port2 = queryIdSplit2[3];
            ChargeController.getInstance().getChargeHandler(port2).sendEmptyMessage(ChargeHandler.MSG_PLUGIN_CHECK_EVENT);
            return;
        } else if (queryId.equals(QueryDirective.QUERY_ID_CARD_STATUS)) {
            QueryDirective query2 = (QueryDirective) cap.getData();
            HashMap<String, Object> params2 = query2.getParams();
            NFC_CARD_TYPE cardType = NFC_CARD_TYPE.valueOf((String) params2.get("cardType"));
            String cardNo = (String) params2.get("cardNo");
            CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
            if (CHARGE_PLATFORM.ocpp.equals(platform) && NFC_CARD_TYPE.ocpp.equals(cardType)) {
                CAPDirectiveOption indicateOpt = new CAPDirectiveOption();
                indicateOpt.setQuery_id(queryId);
                HashMap<String, Object> indicateParams = new HashMap<>();
                indicateParams.put("cardNo", cardNo);
                QueryDirective queryIndicate = new QueryDirective();
                queryIndicate.setParams(indicateParams);
                String to = "server:" + platform;
                DCAPMessage indicate = ChargeController.createIndicate(to, "query", indicateOpt, queryIndicate);
                DCAPProxy.getInstance().sendIndicate(indicate);
                RequestSession requestSession = new RequestSession();
                requestSession.setRequest(request);
                requestSession.setConfirm(confirm);
                IndicateSession indicateSession = new IndicateSession();
                indicateSession.setIndicate(indicate);
                GatewaySession gatewaySession = new GatewaySession();
                gatewaySession.setRequestSession(requestSession);
                gatewaySession.setIndicateSession(indicateSession);
                this.dcapGatewaySessions.put(String.valueOf(indicate.getSeq()), gatewaySession);
                this.handlerTimer.startTimer(30000L, MSG_TIMEOUT_DCAP_GATEWAY_SESSION, gatewaySession);
                return;
            }
        }
        Log.w("OSSController.handleQueryRequest", "failed to handle query request: " + request.toJson());
        ChargeController.nackConfirm(confirm, ErrorCode.EC_INTERNAL_ERROR, "internal error", null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleQueryResponse(DCAPMessage response) {
        GatewaySession gatewaySession = this.dcapGatewaySessions.remove(String.valueOf(((CAPMessage) response.getData()).getOpt().getSeq()));
        if (gatewaySession != null) {
            DCAPMessage confirm = gatewaySession.getRequestSession().getConfirm();
            CAPMessage responseCap = (CAPMessage) response.getData();
            String responseOp = responseCap.getOp();
            if ("ack".equals(responseOp)) {
                AckDirective ack = (AckDirective) responseCap.getData();
                ChargeController.ackConfirm(confirm, ack.getAttach());
            } else if (CAPMessage.DIRECTIVE_NACK.equals(responseOp)) {
                NackDirective nack = (NackDirective) responseCap.getData();
                ChargeController.nackConfirm(gatewaySession.getRequestSession().getConfirm(), nack.getError(), nack.getMsg(), nack.getAttach());
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSetRequest(DCAPMessage request, DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) request.getData();
        CAPDirectiveOption opt = cap.getOpt();
        String setId = opt.getSet_id();
        SetDirective set = (SetDirective) cap.getData();
        HashMap<String, Object> values = set.getValues();
        if (SetDirective.SET_ID_DEVICE.equals(setId)) {
            if ("reboot".equals((String) values.get("opr"))) {
                Intent intent = new Intent(DCAPProxy.ACTION_DEVICE_REBOOT_EVENT);
                LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
                ChargeController.ackConfirm(confirm, null);
                return;
            }
        } else if (SetDirective.SET_ID_PORT_GUNLOCK.equals(setId)) {
            String port = (String) values.get(ContentDB.ChargeTable.PORT);
            if (TextUtils.isEmpty(port)) {
                port = "1";
            }
            String opr = (String) values.get("opr");
            if ("guest".equals(request.getFrom())) {
                LogUtils.applog("receive gunlock " + opr + " request from UI");
            }
            LOCK_STATUS lockStatus = ChargeStatusCacheProvider.getInstance().getPortLockStatus(port);
            if (!LOCK_STATUS.disable.equals(lockStatus)) {
                if (SetDirective.OPR_LOCK.equals(opr)) {
                    DeviceProxy.getInstance().lockGun(port);
                    ChargeStatusCacheProvider.getInstance().updatePortLockStatus(port, LOCK_STATUS.lock);
                } else if (SetDirective.OPR_UNLOCK.equals(opr)) {
                    DeviceProxy.getInstance().unlockGun(port);
                    ChargeStatusCacheProvider.getInstance().updatePortLockStatus(port, LOCK_STATUS.unlock);
                } else {
                    "disable".equals(opr);
                }
            } else if (!"enable".equals(opr)) {
                if (SetDirective.OPR_LOCK.equals(opr)) {
                    DeviceProxy.getInstance().enableGunLock(port);
                    DeviceProxy.getInstance().lockGun(port);
                    DeviceProxy.getInstance().disableGunLock(port);
                } else if (SetDirective.OPR_UNLOCK.equals(opr)) {
                    DeviceProxy.getInstance().enableGunLock(port);
                    DeviceProxy.getInstance().unlockGun(port);
                    DeviceProxy.getInstance().disableGunLock(port);
                }
            }
            ChargeController.ackConfirm(confirm, null);
            return;
        } else if (SetDirective.SET_ID_PORT_AMP_WORK.equals(setId)) {
            String port2 = (String) values.get(ContentDB.ChargeTable.PORT);
            if (TextUtils.isEmpty(port2)) {
                port2 = "1";
            }
            Double value = Double.valueOf(Double.parseDouble((String) values.get("value")));
            int adjustAmp = new BigDecimal(value.doubleValue()).setScale(0, 4).intValue();
            DeviceProxy.getInstance().ajustChargeAmp(port2, adjustAmp);
            ChargeStatusCacheProvider.getInstance().updateAdjustAmp(adjustAmp);
            ChargeController.ackConfirm(confirm, null);
            return;
        } else if ("device.verification".equals(setId)) {
            String xid = (String) values.get("xid");
            String result = (String) values.get("result");
            String to = "server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
            CAPDirectiveOption queryOpt = new CAPDirectiveOption();
            queryOpt.setQuery_id("device.verification");
            queryOpt.setOp("query");
            queryOpt.setSeq(Long.valueOf(Long.parseLong(xid)));
            DCAPMessage verificationConfirm = DCAPProxy.getInstance().createCAPConfirm(to, queryOpt);
            if ("1".equals(result)) {
                ChargeController.ackConfirm(verificationConfirm, null);
                return;
            } else {
                ChargeController.nackConfirm(verificationConfirm, ErrorCode.EC_INTERNAL_ERROR, "internal error", null);
                return;
            }
        } else if (SetDirective.SET_ID_DEVICE_TIME_CLOUDSYNCH.equals(setId)) {
            String cloud_ts = (String) values.get("cloud_ts");
            String local_ts = (String) values.get("local_ts");
            if (ChargeController.getInstance().hasCharge(null, false)) {
                LogUtils.syslog("charging now, delay to synch cloud time, cloud ts: " + cloud_ts + ", local ts: " + local_ts);
                this.isDelayCloudTimeSynch = true;
                this.cloud_ts = Long.parseLong(cloud_ts);
                this.local_ts = Long.parseLong(local_ts);
            } else {
                synchCloudTime(Long.parseLong(cloud_ts), Long.parseLong(local_ts));
            }
            ChargeController.ackConfirm(confirm, null);
            return;
        } else if (SetDirective.SET_ID_DEVICE_CP_RANGE.equals(setId)) {
            int cpRange = Integer.parseInt((String) values.get("value"));
            DeviceProxy.getInstance().setCPRange(cpRange);
            ChargeStatusCacheProvider.getInstance().updateCPRange(cpRange);
            ChargeController.ackConfirm(confirm, null);
            return;
        } else if (SetDirective.SET_ID_DEVICE_VOLT_RANGE.equals(setId)) {
            int voltRange = Integer.parseInt((String) values.get("value"));
            DeviceProxy.getInstance().setVoltageRange(voltRange);
            ChargeStatusCacheProvider.getInstance().updateVoltageRange(voltRange);
            ChargeController.ackConfirm(confirm, null);
            return;
        } else if (SetDirective.SET_ID_DEVICE_LEAKAGE_TOLERANCE.equals(setId)) {
            int leakageTolerance = Integer.parseInt((String) values.get("value"));
            DeviceProxy.getInstance().setLeakageTolerance(leakageTolerance);
            ChargeStatusCacheProvider.getInstance().updateLeakageTolerance(Integer.valueOf(leakageTolerance));
            ChargeController.ackConfirm(confirm, null);
            return;
        } else if (SetDirective.SET_ID_DEVICE_EARTH_DISABLE.equals(setId)) {
            boolean earthDisable = "disable".equals((String) values.get("value"));
            DeviceProxy.getInstance().setEarthDisable(earthDisable);
            ChargeStatusCacheProvider.getInstance().updateEarthDisable(Boolean.valueOf(earthDisable));
            ChargeController.ackConfirm(confirm, null);
            return;
        } else if (SetDirective.SET_ID_DEVICE_LOCALE.equals(setId)) {
            if (ChargeController.getInstance().hasCharge(null, false)) {
                LogUtils.syslog("charging now, delay to set locale params: " + JsonBean.mapToJson(values));
                this.delayLocaleSetting = values;
                this.isDelayLocaleSetting = true;
            } else {
                setLocaleParams(values);
            }
            ChargeController.ackConfirm(confirm, null);
            return;
        }
        Log.w("OSSController.handleSetRequest", "failed to handle set request: " + request.toJson());
        ChargeController.nackConfirm(confirm, ErrorCode.EC_INTERNAL_ERROR, "internal error", null);
    }

    private void setLocaleParams(Map localeSetting) {
        Log.i("OSSController.setLocaleParams", "local locale before set: " + CountrySettingCacheProvider.getInstance().getCountrySetting().toJson());
        if (localeSetting.containsKey("dst")) {
            CountrySettingCacheProvider.getInstance().updateUseDaylightTime(((Boolean) localeSetting.get("dst")).booleanValue());
        }
        String localTimezone = CountrySettingCacheProvider.getInstance().getZone();
        if (localeSetting.containsKey("zone")) {
            localTimezone = (String) localeSetting.get("zone");
        }
        if (localeSetting.containsKey("zone") || localeSetting.containsKey("dst")) {
            boolean useDST = CountrySettingCacheProvider.getInstance().isUseDaylightTime();
            String zoneId = TimeUtils.getTimezoneId(localTimezone, useDST);
            if (TextUtils.isEmpty(zoneId)) {
                Log.w("OSSController.setLocaleParams", "unavailable id for timezone: " + localTimezone);
            } else {
                AlarmManager alarmManager = (AlarmManager) this.context.getSystemService("alarm");
                alarmManager.setTimeZone(zoneId);
                if (localeSetting.containsKey("zone")) {
                    CountrySettingCacheProvider.getInstance().updateZone(localTimezone);
                }
                boolean realUseDST = TimeZone.getTimeZone(zoneId).useDaylightTime();
                if (realUseDST != useDST) {
                    CountrySettingCacheProvider.getInstance().updateUseDaylightTime(realUseDST);
                }
                Log.i("OSSController.setLocaleParams", "set timezone: " + localTimezone + " using id: " + zoneId + ", useDST: " + realUseDST);
                LogUtils.syslog("set timezone: " + localTimezone + " using id: " + zoneId + ", useDST: " + realUseDST);
            }
        }
        if (localeSetting.containsKey("lang")) {
            CountrySettingCacheProvider.getInstance().updateLang((String) localeSetting.get("lang"));
        }
        if (localeSetting.containsKey("money")) {
            CountrySettingCacheProvider.getInstance().updateMoney((String) localeSetting.get("money"));
        }
        if (localeSetting.containsKey("moneyDisp")) {
            CountrySettingCacheProvider.getInstance().updateMoneyDisp((String) localeSetting.get("moneyDisp"));
        }
        Log.i("OSSController.setLocaleParams", "local locale after set: " + CountrySettingCacheProvider.getInstance().getCountrySetting().toJson());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleUpgradeRequest(DCAPMessage request, DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) request.getData();
        UpgradeDirective upgrade = (UpgradeDirective) cap.getData();
        HashMap<String, SwipeCardPermission> restoreSwipeCardPermissions = new HashMap<>();
        HashMap<String, Port> ports = HardwareStatusCacheProvider.getInstance().getPorts();
        for (String port : ports.keySet()) {
            SwipeCardPermission swipeCardPermission = SystemSettingCacheProvider.getInstance().getPortSwipeCardPermission(port);
            restoreSwipeCardPermissions.put(port, swipeCardPermission);
            SwipeCardPermission forbidenSwipeCard = new SwipeCardPermission();
            forbidenSwipeCard.setPermitSetting(false);
            forbidenSwipeCard.setPermitChargeCtrl(false);
            forbidenSwipeCard.setPermitBinding(false);
            SystemSettingCacheProvider.getInstance().updatePortSwipeCardPermission(port, forbidenSwipeCard);
        }
        this.handlerTimer.stopTimer(MSG_DELAYED_UPGRADE_CHECK);
        this.delayedUpgradeRequest = null;
        if (UpgradeAgent.getInstance().update(upgrade)) {
            ChargeController.ackConfirm(confirm, null);
        } else {
            ChargeController.nackConfirm(confirm, SoftwareStatusCacheProvider.getInstance().getUpgradeProgress().getError().getCode(), null, null);
            UpgradeProgress upgradeProgress = SoftwareStatusCacheProvider.getInstance().getUpgradeProgress();
            ErrorCode error = upgradeProgress.getError();
            if (error != null && error.getCode() == 60010) {
                this.delayedUpgradeRequest = new RequestSession();
                this.delayedUpgradeRequest.setRequest(request);
                this.delayedUpgradeRequest.setConfirm(confirm);
                this.handlerTimer.startTimer(30000L, MSG_DELAYED_UPGRADE_CHECK, null);
            }
        }
        for (Map.Entry<String, SwipeCardPermission> entry : restoreSwipeCardPermissions.entrySet()) {
            SwipeCardPermission swipeCardPermission2 = entry.getValue();
            SystemSettingCacheProvider.getInstance().updatePortSwipeCardPermission(entry.getKey(), swipeCardPermission2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDelayedUpgradeCheck() {
        if (this.delayedUpgradeRequest != null) {
            if (ChargeController.getInstance().hasCharge(null, false)) {
                this.handlerTimer.startTimer(30000L, MSG_DELAYED_UPGRADE_CHECK, null);
            } else {
                this.handler.sendMessageDelayed(this.handler.obtainMessage(MSG_REQUEST_UPGRADE, this.delayedUpgradeRequest), 120000L);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePortCharge2Idle(String port) {
        if (!ChargeController.getInstance().hasCharge(null, false) && this.isDelayCloudTimeSynch) {
            LogUtils.syslog("charge ended, try to synch cloud time, cloud ts: " + this.cloud_ts + ", local ts: " + this.local_ts);
            synchCloudTime(this.cloud_ts, this.local_ts);
        }
        if (!ChargeController.getInstance().hasCharge(null, false) && this.isDelayLocaleSetting) {
            setLocaleParams(this.delayLocaleSetting);
            this.isDelayLocaleSetting = false;
        }
    }

    private void synchCloudTime(long cloudTs, long localTs) {
        long localTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (localTime >= localTs) {
            long cloudTime = (cloudTs + localTime) - localTs;
            Log.i("OSSController.synchCloudTime", "local time before synch set: " + sdf.format(new Date(localTime)));
            Log.i("OSSController.synchCloudTime", "cloud time: " + sdf.format(new Date(cloudTime)));
            SystemClock.setCurrentTimeMillis(cloudTime);
            LogUtils.syslog("synch cloud time: " + sdf.format(new Date(cloudTime)));
            Log.i("OSSController.synchCloudTime", "local time after synch setted: " + sdf.format(new Date(System.currentTimeMillis())));
            this.isDelayCloudTimeSynch = false;
            ChargeStatusCacheProvider.getInstance().updateCloudTimeSynch(true);
            return;
        }
        Log.w("OSSController.synchCloudTime", "invalid time, now time " + sdf.format(new Date(localTime)) + " is lower than local time received synch from cloud: " + sdf.format(new Date(localTs)));
        LogUtils.syslog("failed to synch cloud time, and now time " + sdf.format(new Date(localTime)) + " is lower than local time received synch from cloud: " + sdf.format(new Date(localTs)));
    }
}