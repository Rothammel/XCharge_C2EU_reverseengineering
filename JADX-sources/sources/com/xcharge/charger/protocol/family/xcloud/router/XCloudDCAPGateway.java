package com.xcharge.charger.protocol.family.xcloud.router;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.xcharge.charger.core.api.DCAPProxy;
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
import com.xcharge.charger.core.api.bean.cap.UpgradeDirective;
import com.xcharge.charger.core.type.FIN_MODE;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.UpgradeData;
import com.xcharge.charger.data.bean.UpgradeProgress;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.PHASE;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestUpgrade;
import com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent;
import com.xcharge.charger.protocol.family.xcloud.session.XCloudRequestSession;
import com.xcharge.charger.protocol.family.xcloud.session.XCloudUpgradeSession;
import com.xcharge.charger.protocol.family.xcloud.type.EnumActionStatus;
import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.FileUtils;
import com.xcharge.common.utils.HandlerTimer;
import com.xcharge.common.utils.HttpDownloadManager;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.TimeUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/* loaded from: classes.dex */
public class XCloudDCAPGateway {
    public static final int INTERVAL_ADAPTER_MAINTAIN = 1000;
    public static final int MSG_DCAP_CONFIRM = 77826;
    public static final int MSG_DCAP_INDICATE = 77825;
    public static final int MSG_FAILED_REQUEST_SERVER = 77829;
    public static final int MSG_TIMER_MAINTAIN_ADAPTER = 77830;
    public static final int MSG_U3_AUTH_REFUSED = 77831;
    public static final int MSG_XCLOUD_REQUEST = 77827;
    public static final int MSG_XCLOUD_RESPONSE = 77828;
    private static XCloudDCAPGateway instance = null;
    private HandlerThread thread = null;
    private MsgHandler handler = null;
    private HandlerTimer handlerTimer = null;
    private Context context = null;
    private XCloudAdapter xcloudAdapter = null;
    private DCAPAdapter dcapAdapter = null;
    private XCloudUpgradeSession upgradeSession = null;

    public static XCloudDCAPGateway getInstance() {
        if (instance == null) {
            instance = new XCloudDCAPGateway();
        }
        return instance;
    }

    /* loaded from: classes.dex */
    public class MsgHandler extends Handler {
        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public MsgHandler(Looper looper) {
            super(looper);
            XCloudDCAPGateway.this = r1;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 77825:
                        DCAPMessage dcapIndicate = (DCAPMessage) msg.obj;
                        Log.i("XCloudDCAPGateway.handleMessage", "receive DCAP indicate: " + dcapIndicate.toJson());
                        XCloudDCAPGateway.this.handleDCAPIndicate(dcapIndicate);
                        break;
                    case 77826:
                        DCAPMessage dcapConfirm = (DCAPMessage) msg.obj;
                        Log.i("XCloudDCAPGateway.handleMessage", "receive DCAP Confirm: " + dcapConfirm.toJson());
                        XCloudDCAPGateway.this.handleDCAPConfirm(dcapConfirm);
                        break;
                    case 77827:
                        XCloudMessage request = (XCloudMessage) msg.obj;
                        XCloudDCAPGateway.this.handleXCloudRequest(request);
                        break;
                    case 77828:
                        XCloudRequestSession requstSession = (XCloudRequestSession) msg.obj;
                        XCloudDCAPGateway.this.handleXCloudResponse(requstSession.getRequest(), requstSession.getResponse());
                        break;
                    case 77829:
                        XCloudDCAPGateway.this.xcloudAdapter.handleSendXCloudRequestFail((XCloudMessage) msg.obj);
                        break;
                    case 77830:
                        XCloudDCAPGateway.this.maitainAdapter();
                        XCloudDCAPGateway.this.handlerTimer.startTimer(1000L, 77830, null);
                        break;
                    case XCloudDCAPGateway.MSG_U3_AUTH_REFUSED /* 77831 */:
                        XCloudRequestSession xcloudRequestSession = (XCloudRequestSession) msg.obj;
                        XCloudDCAPGateway.this.xcloudAdapter.handU3AuthRefused(xcloudRequestSession);
                        break;
                }
            } catch (Exception e) {
                Log.e("XCloudDCAPGateway.handleMessage", "except: " + Log.getStackTraceString(e));
                LogUtils.syslog("XCloudDCAPGateway handleMessage exception: " + Log.getStackTraceString(e));
            }
            super.handleMessage(msg);
        }
    }

    public void init(Context context) {
        this.context = context;
        this.xcloudAdapter = new XCloudAdapter();
        this.xcloudAdapter.init(this.context);
        this.dcapAdapter = new DCAPAdapter();
        this.dcapAdapter.init(this.context);
        this.thread = new HandlerThread("XCloudDCAPGateway", 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context);
        this.handlerTimer.startTimer(1000L, 77830, null);
    }

    public void destroy() {
        this.handlerTimer.destroy();
        this.handler.removeMessages(77826);
        this.handler.removeMessages(77825);
        this.handler.removeMessages(77827);
        this.handler.removeMessages(77828);
        this.handler.removeMessages(77829);
        this.handler.removeMessages(77830);
        this.handler.removeMessages(MSG_U3_AUTH_REFUSED);
        this.thread.quit();
        this.xcloudAdapter.destroy();
        this.dcapAdapter.destroy();
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

    private XCloudUpgradeSession getUpgradeSession() {
        if (this.upgradeSession == null) {
            this.upgradeSession = new XCloudUpgradeSession();
        }
        return this.upgradeSession;
    }

    public void clearUpgradeSession() {
        String downloadFile = getUpgradeSession().getDownloadFile();
        if (!TextUtils.isEmpty(downloadFile)) {
            FileUtils.deleteFile(downloadFile);
        }
        this.upgradeSession = null;
    }

    public void maitainAdapter() {
        this.xcloudAdapter.maintainSession();
        this.dcapAdapter.maintainSession();
    }

    public void handleXCloudResponse(XCloudMessage request, XCloudMessage response) {
        String name = response.getMessageName();
        if (XCloudMessage.RequestStartCharge.equals(name)) {
            this.xcloudAdapter.handleRequestStartCharge(response);
        } else if (XCloudMessage.RequestRefuseCharge.equals(name)) {
            this.xcloudAdapter.handleRequestRefuseCharge(response);
        }
    }

    public void handleXCloudRequest(XCloudMessage request) {
        String name = request.getMessageName();
        if (XCloudMessage.RequestStartCharge.equals(name)) {
            this.dcapAdapter.handleRequestStartCharge(request);
        } else if (XCloudMessage.RequestRefuseCharge.equals(name)) {
            this.dcapAdapter.handleRequestRefuseCharge(request);
        } else if (XCloudMessage.RequestStopCharge.equals(name)) {
            this.dcapAdapter.handleRequestStopCharge(request);
        } else if (XCloudMessage.RequestAutoStop.equals(name)) {
            this.dcapAdapter.handleRequestAutoStop(request);
        } else if (XCloudMessage.CancelAutoStop.equals(name)) {
            this.dcapAdapter.handleCancelAutoStop(request);
        } else if (XCloudMessage.RequestAction.equals(name)) {
            this.dcapAdapter.handleRequestAction(request);
        } else if (XCloudMessage.RequestUpgrade.equals(name)) {
            handleUpgradeRequest(request);
        } else if (XCloudMessage.RequestVerification.equals(name)) {
            this.dcapAdapter.handleRequestVerification(request);
        } else if (XCloudMessage.RequestUpdateStartTime.equals(name)) {
            this.dcapAdapter.handleRequestUpdateStartTime(request);
        } else if (XCloudMessage.RequestEndCharge.equals(name)) {
            this.dcapAdapter.handleRequestEndCharge(request);
        }
    }

    private void handleUpgradeRequest(XCloudMessage request) {
        final RequestUpgrade requestUpgrade = (RequestUpgrade) request.getBody();
        XCloudUpgradeSession upgradeSession = getUpgradeSession();
        if (TextUtils.isEmpty(upgradeSession.getStage())) {
            upgradeSession.setStage(UpgradeProgress.STAGE_DOWNLOAD);
            upgradeSession.setRequestUpgrade(requestUpgrade);
            final String url = requestUpgrade.getFileUrl();
            upgradeSession.setDownloadFile("/data/data/com.xcharge.charger/download/upgrade/update.dat");
            Log.i("XCloudDCAPGateway.handleUpgradeRequest", "start download ..., url: " + url);
            downloadProgress(200, 1, 0);
            HttpDownloadManager.getInstance().downloadFile(this.context, url, "/data/data/com.xcharge.charger/download/upgrade/update.dat", new HttpDownloadManager.DownLoadListener() { // from class: com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway.1
                {
                    XCloudDCAPGateway.this = this;
                }

                @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                public void onDownLoadPercentage(long curPosition, long total) {
                }

                @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                public void onDownLoadPercentage(int p) {
                    XCloudDCAPGateway.this.downloadProgress(200, 2, p);
                }

                @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                public void onDownLoadFail() {
                    LogUtils.cloudlog("failed to download upgrade resource: " + url);
                    XCloudProtocolAgent.getInstance().reportActionResult(requestUpgrade.getSid(), EnumActionStatus.failed, new DeviceError("ERROR", "download failed", null));
                    XCloudDCAPGateway.this.downloadProgress(ErrorCode.EC_UPGRADE_DOWNLOAD_FAIL, 0, 0);
                    XCloudDCAPGateway.this.clearUpgradeSession();
                }

                @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                public void onDownLoadComplete() {
                    if (XCloudDCAPGateway.this.verifyFileMD5()) {
                        XCloudProtocolAgent.getInstance().reportActionResult(requestUpgrade.getSid(), EnumActionStatus.received, null);
                        XCloudDCAPGateway.this.upgradeRequest();
                        return;
                    }
                    XCloudProtocolAgent.getInstance().reportActionResult(requestUpgrade.getSid(), EnumActionStatus.failed, new DeviceError("ERROR", "md5 error", null));
                    XCloudDCAPGateway.this.clearUpgradeSession();
                }
            });
            return;
        }
        LogUtils.cloudlog("in handle upgrade, reject new upgrade request !!!");
        XCloudProtocolAgent.getInstance().reportActionResult(requestUpgrade.getSid(), EnumActionStatus.failed, new DeviceError(DeviceError.NOT_IDLE, "upgrading now", null));
    }

    public boolean verifyFileMD5() {
        XCloudUpgradeSession upgradeSession = getUpgradeSession();
        RequestUpgrade requestUpgrade = upgradeSession.getRequestUpgrade();
        if (TextUtils.isEmpty(requestUpgrade.getFileMD5())) {
            Log.i("XCloudDCAPGateway.verifyFileMD5", "need not verify file md5 !!!");
            return true;
        }
        String md5 = "";
        String sourceMD5 = requestUpgrade.getFileMD5().toLowerCase();
        boolean isOk = false;
        String downloadFile = upgradeSession.getDownloadFile();
        try {
            Log.i("XCloudDCAPGateway.verifyFileMD5", "begin integrity check: " + downloadFile);
            downloadProgress(200, 3, 0);
            md5 = FileUtils.getMD5(new File(downloadFile));
            if (!md5.equals(sourceMD5)) {
                isOk = false;
            } else {
                isOk = true;
            }
        } catch (Exception e) {
            Log.e("XCloudDCAPGateway.verifyFileMD5", Log.getStackTraceString(e));
        }
        if (!isOk) {
            Log.w("XCloudDCAPGateway.verifyFileMD5", "sourceMD5: " + sourceMD5 + ", fileMD5: " + md5);
            LogUtils.cloudlog("failed to verify upgrade file MD5, downloaded file MD5 is " + md5 + ", but cloud MD5 is " + sourceMD5);
            downloadProgress(ErrorCode.EC_UPGRADE_NOT_INTEGRATED, 0, 0);
            return isOk;
        }
        return isOk;
    }

    public void upgradeRequest() {
        CAPDirectiveOption opt = new CAPDirectiveOption();
        XCloudUpgradeSession upgradeSession = getUpgradeSession();
        RequestUpgrade requestUpgrade = upgradeSession.getRequestUpgrade();
        UpgradeDirective upgrade = new UpgradeDirective();
        if (RequestUpgrade.COM_OS.equals(requestUpgrade.getComponent())) {
            upgrade.setComponent(UpgradeData.COM_ALL);
        } else if ("app".equals(requestUpgrade.getComponent())) {
            upgrade.setComponent("app");
        } else {
            upgrade.setComponent(UpgradeData.COM_ALL);
        }
        upgrade.setDependentVersion(requestUpgrade.getDependentVersion());
        upgrade.setVersion(requestUpgrade.getVersion());
        upgrade.setSrcPath(upgradeSession.getDownloadFile());
        String from = "server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
        DCAPMessage setRequest = createRequest(from, "upgrade", opt, upgrade);
        DCAPProxy.getInstance().sendRequest(setRequest);
        upgradeSession.setStage("update");
        upgradeSession.setUpgradeDCAPRequestSeq(Long.valueOf(setRequest.getSeq()));
    }

    private void handleUpgradeConfirm(DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) confirm.getData();
        CAPDirectiveOption opt = cap.getOpt();
        Long peerSeq = opt.getSeq();
        XCloudUpgradeSession upgradeSession = getUpgradeSession();
        if (!peerSeq.equals(upgradeSession.getUpgradeDCAPRequestSeq())) {
            Log.w("XCloudDCAPGateway.handleUpgradeConfirm", "not confirm for now upgrade session: " + upgradeSession.toJson());
            return;
        }
        RequestUpgrade requestUpgrade = upgradeSession.getRequestUpgrade();
        String op = cap.getOp();
        if ("ack".equals(op)) {
            XCloudProtocolAgent.getInstance().reportActionResult(requestUpgrade.getSid(), EnumActionStatus.success, null);
            Log.i("XCloudDCAPGateway.handleUpgradeConfirm", "succeed to install update !!!");
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
            UpgradeProgress upgradeProgress = SoftwareStatusCacheProvider.getInstance().getUpgradeProgress();
            ErrorCode error = upgradeProgress.getError();
            if (error != null && error.getCode() == 60010) {
                Log.w("XCloudDCAPGateway.handleUpgradeConfirm", "charging now, will install update later !!!");
                return;
            } else {
                XCloudProtocolAgent.getInstance().reportActionResult(requestUpgrade.getSid(), EnumActionStatus.failed, new DeviceError("ERROR", "update failed", null));
                Log.i("XCloudDCAPGateway.handleUpgradeConfirm", "failed to install update !!!");
            }
        }
        clearUpgradeSession();
    }

    private DCAPMessage createRequest(String from, String op, CAPDirectiveOption opt, Object directive) {
        CAPMessage requestCap = new CAPMessage();
        DCAPMessage request = new DCAPMessage();
        request.setFrom(from);
        request.setTo("device:sn/" + HardwareStatusCacheProvider.getInstance().getSn());
        request.setType("cap");
        request.setCtime(System.currentTimeMillis());
        request.setSeq(Sequence.getAgentDCAPSequence());
        requestCap.setOp(op);
        requestCap.setOpt(opt);
        requestCap.setData(directive);
        request.setData(requestCap);
        return request;
    }

    public void downloadProgress(int error, int status, int progress) {
        UpgradeProgress upgradeProgress = SoftwareStatusCacheProvider.getInstance().getUpgradeProgress();
        upgradeProgress.setStage(UpgradeProgress.STAGE_DOWNLOAD);
        upgradeProgress.setUpgradeData(null);
        upgradeProgress.setError(new ErrorCode(error));
        upgradeProgress.setStatus(status);
        upgradeProgress.setProgress(progress);
        SoftwareStatusCacheProvider.getInstance().updateUpgradeProgress(upgradeProgress);
    }

    public void handleDCAPIndicate(DCAPMessage indicate) {
        CAPMessage cap = (CAPMessage) indicate.getData();
        String op = cap.getOp();
        if ("auth".equals(op)) {
            AuthDirective auth = new AuthDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
            cap.setData(auth);
            this.xcloudAdapter.handleAuthIndicate(indicate);
        } else if (CAPMessage.DIRECTIVE_INIT_ACK.equals(op)) {
            InitAckDirective initAck = new InitAckDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
            cap.setData(initAck);
            this.dcapAdapter.handleInitAckIndicate(indicate);
        } else if ("fin".equals(op)) {
            FinDirective fin = new FinDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
            cap.setData(fin);
            this.dcapAdapter.handleFinIndicate(indicate);
        } else if ("event".equals(op)) {
            EventDirective event = new EventDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
            cap.setData(event);
            handleEventIndicate(indicate);
        }
    }

    public void handleDCAPConfirm(DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) confirm.getData();
        CAPDirectiveOption opt = cap.getOpt();
        String peerOp = opt.getOp();
        String op = cap.getOp();
        if ("ack".equals(op)) {
            AckDirective ack = new AckDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
            cap.setData(ack);
            if ("fin".equals(peerOp)) {
                FIN_MODE mode = FIN_MODE.normal;
                ErrorCode error = null;
                HashMap<String, Object> attach = ack.getAttach();
                if (attach != null) {
                    String finMode = (String) attach.get("fin_mode");
                    if (!TextUtils.isEmpty(finMode)) {
                        mode = FIN_MODE.valueOf(finMode);
                    }
                    String finError = (String) attach.get("error");
                    if (!TextUtils.isEmpty(finError)) {
                        error = new ErrorCode().fromJson(finError);
                    }
                }
                String chargeId = opt.getCharge_id();
                XCloudProtocolAgent.getInstance().handleFinConfirm(chargeId, mode, error);
            }
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
            NackDirective nack = new NackDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
            cap.setData(nack);
        } else {
            Log.w("XCloudDCAPGateway.handleDCAPConfirm", "illegal DCAP conform: " + confirm.toJson());
            return;
        }
        if ("stop".equals(peerOp)) {
            this.dcapAdapter.handleStopConfirm(confirm);
        } else if (CAPMessage.DIRECTIVE_CONDITION.equals(peerOp)) {
            this.dcapAdapter.handleConditionConfirm(confirm);
        } else if ("set".equals(peerOp)) {
            this.dcapAdapter.handleSetConfirm(confirm);
        } else if ("query".equals(peerOp)) {
            this.dcapAdapter.handleQueryConfirm(confirm);
        } else if ("upgrade".equals(peerOp)) {
            handleUpgradeConfirm(confirm);
        }
    }

    private void handleEventIndicate(DCAPMessage indicate) {
        CAPMessage cap = (CAPMessage) indicate.getData();
        EventDirective event = (EventDirective) cap.getData();
        CAPDirectiveOption opt = cap.getOpt();
        String chargeId = opt.getCharge_id();
        String eventId = opt.getEvent_id();
        if (EventDirective.EVENT_CHARGE_PAUSE.equals(eventId)) {
            DeviceError deviceError = null;
            long pauseTime = TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone());
            HashMap<String, Object> attach = event.getAttach();
            if (attach != null) {
                String error = (String) attach.get("error");
                String time = (String) attach.get(ChargeStopCondition.TYPE_TIME);
                if (!TextUtils.isEmpty(error)) {
                    ErrorCode errorCode = new ErrorCode().fromJson(error);
                    PortStatus portStatus = null;
                    HashMap<String, Object> errorData = errorCode.getData();
                    if (errorData != null) {
                        String portChargeStatus = (String) errorData.get("portChargeStatus");
                        if (!TextUtils.isEmpty(portChargeStatus)) {
                            portStatus = new PortStatus().fromJson(portChargeStatus);
                        }
                    }
                    if (errorCode.getCode() >= 30010 && errorCode.getCode() <= 30018) {
                        JsonObject deviceErrorData = new JsonObject();
                        deviceErrorData.addProperty("code", String.valueOf((errorCode.getCode() - 30000) + 5000));
                        if (portStatus != null) {
                            PHASE phase = HardwareStatusCacheProvider.getInstance().getPhase();
                            if (errorCode.getCode() == 30015) {
                                if (PHASE.SINGLE_PHASE.getPhase() == phase.getPhase()) {
                                    JsonObject deviceErrorDataAttach = new JsonObject();
                                    deviceErrorDataAttach.addProperty("current", portStatus.getAmps().get(1));
                                    deviceErrorData.add("data", deviceErrorDataAttach);
                                } else if (PHASE.THREE_PHASE.getPhase() == phase.getPhase()) {
                                    JsonObject deviceErrorDataAttach2 = new JsonObject();
                                    JsonArray deviceErrorDataAttachData = new JsonParser().parse(JsonBean.getGsonBuilder().create().toJson(new ArrayList(Arrays.asList(portStatus.getAmps().get(1), portStatus.getAmps().get(2), portStatus.getAmps().get(3))), new TypeToken<ArrayList>() { // from class: com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway.2
                                        {
                                            XCloudDCAPGateway.this = this;
                                        }
                                    }.getType())).getAsJsonArray();
                                    deviceErrorDataAttach2.add("current", deviceErrorDataAttachData);
                                    deviceErrorData.add("data", deviceErrorDataAttach2);
                                }
                            } else if (errorCode.getCode() == 30014) {
                                if (PHASE.SINGLE_PHASE.getPhase() == phase.getPhase()) {
                                    JsonObject deviceErrorDataAttach3 = new JsonObject();
                                    deviceErrorDataAttach3.addProperty("voltage", portStatus.getVolts().get(0));
                                    deviceErrorData.add("data", deviceErrorDataAttach3);
                                } else if (PHASE.THREE_PHASE.getPhase() == phase.getPhase()) {
                                    JsonObject deviceErrorDataAttach4 = new JsonObject();
                                    JsonArray deviceErrorDataAttachData2 = new JsonParser().parse(JsonBean.getGsonBuilder().create().toJson(portStatus.getVolts(), new TypeToken<ArrayList>() { // from class: com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway.3
                                        {
                                            XCloudDCAPGateway.this = this;
                                        }
                                    }.getType())).getAsJsonArray();
                                    deviceErrorDataAttach4.add("voltage", deviceErrorDataAttachData2);
                                    deviceErrorData.add("data", deviceErrorDataAttach4);
                                }
                            } else if (errorCode.getCode() == 30017) {
                                JsonObject deviceErrorDataAttach5 = new JsonObject();
                                deviceErrorDataAttach5.addProperty("current", portStatus.getLeakAmp());
                                deviceErrorData.add("data", deviceErrorDataAttach5);
                            } else if (errorCode.getCode() == 30016) {
                                JsonObject deviceErrorDataAttach6 = new JsonObject();
                                deviceErrorDataAttach6.addProperty("temperature", Double.valueOf(portStatus.getTemprature().doubleValue() / 10.0d));
                                deviceErrorData.add("data", deviceErrorDataAttach6);
                            } else if (errorCode.getCode() == 30018) {
                                JsonObject deviceErrorDataAttach7 = new JsonObject();
                                deviceErrorDataAttach7.addProperty("voltage", portStatus.getCpVoltage());
                                deviceErrorData.add("data", deviceErrorDataAttach7);
                            }
                        }
                        deviceError = new DeviceError("ERROR", null, deviceErrorData);
                    } else if (50001 == errorCode.getCode()) {
                        JsonObject deviceErrorData2 = new JsonObject();
                        deviceErrorData2.addProperty("code", "5019");
                        deviceError = new DeviceError("ERROR", null, deviceErrorData2);
                    } else {
                        deviceError = new DeviceError("ERROR", null, null);
                    }
                }
                if (!TextUtils.isEmpty(time)) {
                    pauseTime = TimeUtils.getXCloudFormat(Long.parseLong(time), RemoteSettingCacheProvider.getInstance().getProtocolTimezone());
                }
            }
            XCloudProtocolAgent.getInstance().sendReportChargePaused(chargeId, deviceError, pauseTime);
        } else if (EventDirective.EVENT_CHARGE_RESUME.equals(eventId)) {
            DeviceError deviceError2 = null;
            long resumeTime = TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone());
            HashMap<String, Object> attach2 = event.getAttach();
            if (attach2 != null) {
                String error2 = (String) attach2.get("error");
                String time2 = (String) attach2.get(ChargeStopCondition.TYPE_TIME);
                if (!TextUtils.isEmpty(error2)) {
                    int errorCode2 = Integer.parseInt(error2);
                    String xchargeErrorCode = null;
                    if (errorCode2 >= 30010 && errorCode2 <= 30018) {
                        xchargeErrorCode = String.valueOf((errorCode2 - 30000) + 5000);
                    } else if (errorCode2 == 50001) {
                        xchargeErrorCode = "5019";
                    }
                    if (!TextUtils.isEmpty(xchargeErrorCode)) {
                        JsonObject deviceErrorData3 = new JsonObject();
                        deviceErrorData3.addProperty("code", xchargeErrorCode);
                        deviceError2 = new DeviceError("ERROR", null, deviceErrorData3);
                    }
                }
                if (!TextUtils.isEmpty(time2)) {
                    resumeTime = TimeUtils.getXCloudFormat(Long.parseLong(time2), RemoteSettingCacheProvider.getInstance().getProtocolTimezone());
                }
            }
            XCloudProtocolAgent.getInstance().sendReportChargeResumed(chargeId, deviceError2, resumeTime);
        } else if ("delay_start".equals(eventId)) {
            long delayStartTime = event.getDelay_start();
            XCloudProtocolAgent.getInstance().sendReportDelayFeeStarted(chargeId, delayStartTime);
        } else if (EventDirective.EVENT_DEALY_WAIT_START.equals(eventId)) {
            XCloudProtocolAgent.getInstance().sendReportDelayCountStarted(chargeId);
        }
    }
}
