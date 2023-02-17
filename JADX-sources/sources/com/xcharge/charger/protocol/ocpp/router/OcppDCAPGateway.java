package com.xcharge.charger.protocol.ocpp.router;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
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
import com.xcharge.charger.core.api.bean.cap.QueryDirective;
import com.xcharge.charger.core.api.bean.cap.SetDirective;
import com.xcharge.charger.core.api.bean.cap.UpgradeDirective;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.UpgradeData;
import com.xcharge.charger.data.bean.UpgradeProgress;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.charger.protocol.ocpp.bean.OcppMessage;
import com.xcharge.charger.protocol.ocpp.bean.cloud.UpdateFirmwareReq;
import com.xcharge.charger.protocol.ocpp.bean.types.FirmwareStatus;
import com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler;
import com.xcharge.charger.protocol.ocpp.handler.OcppProtocolAgent;
import com.xcharge.charger.protocol.ocpp.session.OcppRequestSession;
import com.xcharge.charger.protocol.ocpp.session.OcppUpgradeSession;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.FileUtils;
import com.xcharge.common.utils.FtpUtils;
import com.xcharge.common.utils.HandlerTimer;
import com.xcharge.common.utils.HttpDownloadManager;
import com.xcharge.common.utils.LogUtils;
import java.net.URI;
import java.util.HashMap;
import org.apache.http.util.TextUtils;
import org.json.JSONArray;

/* loaded from: classes.dex */
public class OcppDCAPGateway {
    public static final int INTERVAL_ADAPTER_MAINTAIN = 1000;
    public static final int MSG_DCAP_CONFIRM = 77826;
    public static final int MSG_DCAP_INDICATE = 77825;
    public static final int MSG_FAILED_REQUEST_SERVER = 77829;
    public static final int MSG_OCPP_REQUEST = 77827;
    public static final int MSG_OCPP_RESPONSE = 77828;
    public static final int MSG_TIMER_MAINTAIN_ADAPTER = 77830;
    private static OcppDCAPGateway instance = null;
    private HandlerThread thread = null;
    private MsgHandler handler = null;
    private HandlerTimer handlerTimer = null;
    private Context context = null;
    private OcppAdapter ocppAdapter = null;
    private DCAPAdapter dcapAdapter = null;
    private OcppUpgradeSession upgradeSession = null;
    public double downloadProgress = 0.0d;

    public static OcppDCAPGateway getInstance() {
        if (instance == null) {
            instance = new OcppDCAPGateway();
        }
        return instance;
    }

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
                    case 77825:
                        DCAPMessage dcapIndicate = (DCAPMessage) msg.obj;
                        Log.i("OcppDCAPGateway.handleMessage", "receive DCAP indicate: " + dcapIndicate.toJson());
                        OcppDCAPGateway.this.handleDCAPIndicate(dcapIndicate);
                        break;
                    case 77826:
                        DCAPMessage dcapConfirm = (DCAPMessage) msg.obj;
                        Log.i("OcppDCAPGateway.handleMessage", "receive DCAP Confirm: " + dcapConfirm.toJson());
                        OcppDCAPGateway.this.handleDCAPConfirm(dcapConfirm);
                        break;
                    case 77827:
                        JSONArray request = (JSONArray) msg.obj;
                        OcppDCAPGateway.this.handleOcppRequest(request);
                        break;
                    case 77828:
                        OcppRequestSession session = (OcppRequestSession) msg.obj;
                        OcppDCAPGateway.this.handleOcppResponse(session);
                        break;
                    case 77829:
                        OcppDCAPGateway.this.ocppAdapter.handleFailedOcppRequest((JSONArray) msg.obj);
                        break;
                    case 77830:
                        OcppDCAPGateway.this.maitainAdapter();
                        OcppDCAPGateway.this.handlerTimer.startTimer(1000L, 77830, null);
                        break;
                }
            } catch (Exception e) {
                Log.e("OcppDCAPGateway.handleMessage", "except: " + Log.getStackTraceString(e));
                LogUtils.syslog("OcppDCAPGateway handleMessage exception: " + Log.getStackTraceString(e));
            }
            super.handleMessage(msg);
        }
    }

    public void init(Context context) {
        this.context = context;
        this.ocppAdapter = new OcppAdapter();
        this.ocppAdapter.init(this.context);
        this.dcapAdapter = new DCAPAdapter();
        this.dcapAdapter.init(this.context);
        this.thread = new HandlerThread("OcppDCAPGateway", 10);
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
        this.thread.quit();
        this.dcapAdapter.destroy();
        this.ocppAdapter.destroy();
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

    /* JADX INFO: Access modifiers changed from: private */
    public void maitainAdapter() {
        this.ocppAdapter.maintainSession();
        this.dcapAdapter.maintainSession();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleOcppRequest(JSONArray jsonArray) {
        try {
            String string = jsonArray.getString(2);
            switch (string.hashCode()) {
                case -977722974:
                    if (string.equals(OcppMessage.RemoteStartTransaction)) {
                        this.dcapAdapter.handleRemoteStartTransactionReq(jsonArray);
                        break;
                    }
                    break;
                case -687158844:
                    if (string.equals(OcppMessage.UpdateFirmware)) {
                        handleUpdateFirmwareReq(jsonArray);
                        break;
                    }
                    break;
                case -595338935:
                    if (string.equals(OcppMessage.UnlockConnector)) {
                        this.dcapAdapter.handleUnlockConnectorReq(jsonArray);
                        break;
                    }
                    break;
                case -391574318:
                    if (string.equals(OcppMessage.CancelReservation)) {
                        this.dcapAdapter.handleCancelReservationReq(jsonArray);
                        break;
                    }
                    break;
                case -362842058:
                    if (string.equals(OcppMessage.RemoteStopTransaction)) {
                        this.dcapAdapter.handleRemoteStopTransactionReq(jsonArray);
                        break;
                    }
                    break;
                case 78851375:
                    if (string.equals(OcppMessage.Reset)) {
                        this.dcapAdapter.handleResetReq(jsonArray);
                        break;
                    }
                    break;
                case 280557722:
                    if (string.equals(OcppMessage.ReserveNow)) {
                        this.dcapAdapter.handleReserveNowReq(jsonArray);
                        break;
                    }
                    break;
            }
        } catch (Exception e) {
            Log.w("OcppDCAPGateway.handleOcppRequest", Log.getStackTraceString(e));
        }
    }

    private void handleUpdateFirmwareReq(JSONArray jsonArray) {
        try {
            URI uri = new URI(new UpdateFirmwareReq().fromJson(jsonArray.getJSONObject(3).toString()).getLocation());
            String scheme = uri.getScheme();
            Boolean type = OcppProtocolAgent.getInstance().isSupported(scheme);
            if (type == null || type.booleanValue()) {
                if (OcppProtocolAgent.getInstance().isHttp(scheme)) {
                    handleHttpUpdateFirmwareReq(jsonArray);
                } else {
                    handleFtpUpdateFirmwareReq(jsonArray);
                }
            } else {
                OcppProtocolAgent.getInstance().sendFirmwareStatusNotificationReq(false, FirmwareStatus.DownloadFailed);
            }
        } catch (Exception e) {
            Log.w("OcppDCAPGateway.handleUpdateFirmwareReq", Log.getStackTraceString(e));
        }
    }

    private void handleHttpUpdateFirmwareReq(JSONArray jsonArray) {
        try {
            UpdateFirmwareReq updateFirmwareReq = new UpdateFirmwareReq().fromJson(jsonArray.getJSONObject(3).toString());
            OcppUpgradeSession upgradeSession = getUpgradeSession();
            if (TextUtils.isEmpty(upgradeSession.getStage())) {
                upgradeSession.setStage(UpgradeProgress.STAGE_DOWNLOAD);
                upgradeSession.setUpdateFirmwareReq(updateFirmwareReq);
                String url = updateFirmwareReq.getLocation();
                upgradeSession.setDownloadFile("/data/data/com.xcharge.charger/download/upgrade/update.dat");
                Log.i("OcppDCAPGateway.handleUpgeadeRequest", "start download ..., url: " + url);
                downloadProgress(200, 1, 0);
                OcppProtocolAgent.getInstance().sendFirmwareStatusNotificationReq(false, FirmwareStatus.Downloading);
                HttpDownloadManager.getInstance().downloadFile(this.context, url, "/data/data/com.xcharge.charger/download/upgrade/update.dat", new HttpDownloadManager.DownLoadListener() { // from class: com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway.1
                    @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                    public void onDownLoadPercentage(long curPosition, long total) {
                    }

                    @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                    public void onDownLoadPercentage(int p) {
                        OcppDCAPGateway.this.downloadProgress(200, 2, p);
                    }

                    @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                    public void onDownLoadFail() {
                        OcppDCAPGateway.this.exitUpgrade();
                    }

                    @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                    public void onDownLoadComplete() {
                        OcppProtocolAgent.getInstance().sendFirmwareStatusNotificationReq(false, FirmwareStatus.Downloaded);
                        OcppDCAPGateway.this.upgradeRequest();
                    }
                });
            } else {
                OcppProtocolAgent.getInstance().sendFirmwareStatusNotificationReq(false, FirmwareStatus.DownloadFailed);
            }
        } catch (Exception e) {
            Log.w("OcppDCAPGateway.handleHttpUpdateFirmwareReq", Log.getStackTraceString(e));
            exitUpgrade();
        }
    }

    private void handleFtpUpdateFirmwareReq(JSONArray jsonArray) {
        try {
            UpdateFirmwareReq updateFirmwareReq = new UpdateFirmwareReq().fromJson(jsonArray.getJSONObject(3).toString());
            String username = null;
            String password = null;
            String host = null;
            String path = null;
            String location = updateFirmwareReq.getLocation();
            if (!TextUtils.isEmpty(location)) {
                URI uri = new URI(location);
                String[] login = uri.getRawUserInfo().split(":");
                username = login[0];
                password = login[1];
                host = uri.getHost();
                path = uri.getPath();
                int port = uri.getPort();
                if (port != -1) {
                    host = String.valueOf(host) + ":" + port;
                }
            }
            OcppUpgradeSession upgradeSession = getUpgradeSession();
            if (TextUtils.isEmpty(upgradeSession.getStage())) {
                upgradeSession.setStage(UpgradeProgress.STAGE_DOWNLOAD);
                upgradeSession.setUpdateFirmwareReq(updateFirmwareReq);
                downloadProgress(200, 1, 0);
                upgradeSession.setDownloadFile("/data/data/com.xcharge.charger/download/upgrade/update.dat");
                FtpUtils.FtpConfig cfg = new FtpUtils.FtpConfig();
                cfg.setHost(host);
                cfg.setUsername(username);
                cfg.setPassword(password);
                FtpUtils.download(path, "/data/data/com.xcharge.charger/download/upgrade/update.dat", cfg, new FtpUtils.TransferListener() { // from class: com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway.2
                    @Override // com.xcharge.common.utils.FtpUtils.TransferListener
                    public void onConnected() {
                        OcppProtocolAgent.getInstance().sendFirmwareStatusNotificationReq(false, FirmwareStatus.Downloading);
                    }

                    @Override // com.xcharge.common.utils.FtpUtils.TransferListener
                    public void onConnectFail() {
                        OcppDCAPGateway.this.exitUpgrade();
                    }

                    @Override // com.xcharge.common.utils.FtpUtils.TransferListener
                    public void onTransferPercentage(long downloaded, long total) {
                    }

                    @Override // com.xcharge.common.utils.FtpUtils.TransferListener
                    public void onTransferPercentage(int percent) {
                        OcppDCAPGateway.this.downloadProgress(200, 2, percent);
                    }

                    @Override // com.xcharge.common.utils.FtpUtils.TransferListener
                    public void onTransferFail() {
                        OcppDCAPGateway.this.exitUpgrade();
                    }

                    @Override // com.xcharge.common.utils.FtpUtils.TransferListener
                    public void onTransferComplete() {
                        OcppProtocolAgent.getInstance().sendFirmwareStatusNotificationReq(false, FirmwareStatus.Downloaded);
                        OcppDCAPGateway.this.downloadProgress(200, 2, 100);
                        OcppDCAPGateway.this.upgradeRequest();
                    }
                });
                return;
            }
            OcppProtocolAgent.getInstance().sendFirmwareStatusNotificationReq(false, FirmwareStatus.DownloadFailed);
        } catch (Exception e) {
            Log.w("OcppDCAPGateway.handleFtpUpdateFirmwareReq", Log.getStackTraceString(e));
            exitUpgrade();
        }
    }

    public void exitUpgrade() {
        OcppProtocolAgent.getInstance().sendFirmwareStatusNotificationReq(false, FirmwareStatus.DownloadFailed);
        downloadProgress(ErrorCode.EC_UPGRADE_DOWNLOAD_FAIL, 0, 0);
        clearUpgradeSession();
    }

    public OcppUpgradeSession getUpgradeSession() {
        if (this.upgradeSession == null) {
            this.upgradeSession = new OcppUpgradeSession();
        }
        return this.upgradeSession;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void downloadProgress(int error, int status, int progress) {
        UpgradeProgress upgradeProgress = SoftwareStatusCacheProvider.getInstance().getUpgradeProgress();
        upgradeProgress.setStage(UpgradeProgress.STAGE_DOWNLOAD);
        upgradeProgress.setUpgradeData(null);
        upgradeProgress.setError(new ErrorCode(error));
        upgradeProgress.setStatus(status);
        upgradeProgress.setProgress(progress);
        SoftwareStatusCacheProvider.getInstance().updateUpgradeProgress(upgradeProgress);
    }

    private void clearUpgradeSession() {
        String downloadFile = getUpgradeSession().getDownloadFile();
        if (!TextUtils.isEmpty(downloadFile)) {
            FileUtils.deleteFile(downloadFile);
        }
        this.upgradeSession = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void upgradeRequest() {
        CAPDirectiveOption opt = new CAPDirectiveOption();
        OcppUpgradeSession upgradeSession = getUpgradeSession();
        UpdateFirmwareReq updateFirmwareReq = upgradeSession.getUpdateFirmwareReq();
        UpgradeDirective upgrade = new UpgradeDirective();
        String fileUrl = updateFirmwareReq.getLocation();
        if (TextUtils.isEmpty(fileUrl)) {
            upgrade.setComponent(UpgradeData.COM_ALL);
        } else {
            String upgradeType = fileUrl.substring(fileUrl.length() - 3).trim();
            if ("zip".equals(upgradeType)) {
                upgrade.setComponent(UpgradeData.COM_ALL);
            } else if ("apk".equals(upgradeType)) {
                upgrade.setComponent("app");
            } else {
                upgrade.setComponent(UpgradeData.COM_ALL);
            }
        }
        upgrade.setSrcPath(upgradeSession.getDownloadFile());
        String from = "server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
        DCAPMessage setRequest = createRequest(from, "upgrade", opt, upgrade);
        DCAPProxy.getInstance().sendRequest(setRequest);
        upgradeSession.setStage("update");
        upgradeSession.setUpgradeDCAPRequestSeq(Long.valueOf(setRequest.getSeq()));
        OcppProtocolAgent.getInstance().sendFirmwareStatusNotificationReq(false, FirmwareStatus.Installing);
    }

    public void setDCAPRequest(String setId, HashMap<String, Object> values) {
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setSet_id(setId);
        SetDirective set = new SetDirective();
        set.setValues(values);
        String from = "server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
        DCAPMessage setRequest = createRequest(from, "set", opt, set);
        DCAPProxy.getInstance().sendRequest(setRequest);
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

    /* JADX INFO: Access modifiers changed from: private */
    public void handleOcppResponse(OcppRequestSession session) {
        try {
            JSONArray request = session.getRequest();
            JSONArray response = session.getResponse();
            String string = request.getString(2);
            switch (string.hashCode()) {
                case -815388727:
                    if (string.equals(OcppMessage.Authorize)) {
                        this.ocppAdapter.handleAuthResponse(response);
                        break;
                    }
                    break;
                case 77777212:
                    if (string.equals(OcppMessage.StartTransaction)) {
                        this.dcapAdapter.setMaxChargeEnergy(request);
                        break;
                    }
                    break;
            }
        } catch (Exception e) {
            Log.w("OcppDCAPGateway.handleOcppResponse", Log.getStackTraceString(e));
        }
    }

    private void handleUpgradeConfirm(DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) confirm.getData();
        CAPDirectiveOption opt = cap.getOpt();
        Long peerSeq = opt.getSeq();
        OcppUpgradeSession upgradeSession = OcppProtocolAgent.getInstance().getUpgradeSession();
        if (!peerSeq.equals(upgradeSession.getUpgradeDCAPRequestSeq())) {
            Log.w("OcppDCAPGateway.handleUpgradeConfirm", "not confirm for now upgrade session: " + upgradeSession.toJson());
            return;
        }
        String op = cap.getOp();
        if ("ack".equals(op)) {
            Log.i("OcppDCAPGateway.handleUpgradeConfirm", "succeed to install update !!!");
            OcppProtocolAgent.getInstance().sendFirmwareStatusNotificationReq(false, FirmwareStatus.Installed);
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
            UpgradeProgress upgradeProgress = SoftwareStatusCacheProvider.getInstance().getUpgradeProgress();
            ErrorCode error = upgradeProgress.getError();
            if (error != null && error.getCode() == 60010) {
                Log.w("OcppDCAPGateway.handleUpgradeConfirm", "charging now, will install update later !!!");
                return;
            } else {
                OcppProtocolAgent.getInstance().sendFirmwareStatusNotificationReq(false, FirmwareStatus.InstallationFailed);
                Log.i("OcppDCAPGateway.handleUpgradeConfirm", "failed to install update !!!");
            }
        }
        clearUpgradeSession();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDCAPIndicate(DCAPMessage indicate) {
        CAPMessage cap = (CAPMessage) indicate.getData();
        String op = cap.getOp();
        if ("auth".equals(op)) {
            AuthDirective auth = new AuthDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
            cap.setData(auth);
            this.ocppAdapter.handleAuthIndicate(indicate);
        } else if ("query".equals(op)) {
            QueryDirective query = new QueryDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
            cap.setData(query);
            this.ocppAdapter.handleQueryIndicate(indicate);
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

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDCAPConfirm(DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) confirm.getData();
        String op = cap.getOp();
        if ("ack".equals(op)) {
            AckDirective ack = new AckDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
            cap.setData(ack);
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
            NackDirective nack = new NackDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
            cap.setData(nack);
        } else {
            Log.w("OcppDCAPGateway.handleDCAPConfirm", "illegal DCAP conform: " + confirm.toJson());
            return;
        }
        CAPDirectiveOption opt = cap.getOpt();
        String peerOp = opt.getOp();
        if ("stop".equals(peerOp)) {
            this.dcapAdapter.handleStopConfirm(confirm);
        } else if (CAPMessage.DIRECTIVE_CONDITION.equals(peerOp)) {
            this.dcapAdapter.handleConditionConfirm(confirm);
        } else if ("upgrade".equals(peerOp)) {
            handleUpgradeConfirm(confirm);
        } else if ("set".equals(peerOp)) {
            this.dcapAdapter.handleSetConfirm(confirm);
        }
    }

    private void handleEventIndicate(DCAPMessage indicate) {
        HashMap<String, Object> attach;
        OcppPortHandler portHandler;
        OcppPortHandler portHandler2;
        CAPMessage cap = (CAPMessage) indicate.getData();
        EventDirective event = (EventDirective) cap.getData();
        CAPDirectiveOption opt = cap.getOpt();
        opt.getCharge_id();
        String eventId = opt.getEvent_id();
        if (EventDirective.EVENT_CHARGE_PAUSE.equals(eventId)) {
            HashMap<String, Object> attach2 = event.getAttach();
            if (attach2 != null) {
                String error = (String) attach2.get("error");
                String str = (String) attach2.get(ChargeStopCondition.TYPE_TIME);
                if (!TextUtils.isEmpty(error)) {
                    ErrorCode errorCode = new ErrorCode().fromJson(error);
                    if (50001 == errorCode.getCode() && (portHandler2 = OcppProtocolAgent.getInstance().getPortHandler("1")) != null) {
                        portHandler2.getChargeSession().setEvSuspendStatus(true);
                        portHandler2.sendStatusNotificationReq(false, "50019");
                    }
                }
            }
        } else if (EventDirective.EVENT_CHARGE_RESUME.equals(eventId) && (attach = event.getAttach()) != null) {
            String error2 = (String) attach.get("error");
            String str2 = (String) attach.get(ChargeStopCondition.TYPE_TIME);
            if (!TextUtils.isEmpty(error2)) {
                int errorCode2 = Integer.parseInt(error2);
                if (errorCode2 == 50001 && (portHandler = OcppProtocolAgent.getInstance().getPortHandler("1")) != null) {
                    portHandler.getChargeSession().setEvSuspendStatus(false);
                    portHandler.sendStatusNotificationReq(false, null);
                }
            }
        }
    }
}
