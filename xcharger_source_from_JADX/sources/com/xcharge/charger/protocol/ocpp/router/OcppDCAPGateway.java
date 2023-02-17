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
import java.net.URI;
import java.util.HashMap;
import org.apache.http.util.TextUtils;
import org.json.JSONArray;

public class OcppDCAPGateway {
    public static final int INTERVAL_ADAPTER_MAINTAIN = 1000;
    public static final int MSG_DCAP_CONFIRM = 77826;
    public static final int MSG_DCAP_INDICATE = 77825;
    public static final int MSG_FAILED_REQUEST_SERVER = 77829;
    public static final int MSG_OCPP_REQUEST = 77827;
    public static final int MSG_OCPP_RESPONSE = 77828;
    public static final int MSG_TIMER_MAINTAIN_ADAPTER = 77830;
    private static OcppDCAPGateway instance = null;
    private Context context = null;
    private DCAPAdapter dcapAdapter = null;
    public double downloadProgress = 0.0d;
    private MsgHandler handler = null;
    /* access modifiers changed from: private */
    public HandlerTimer handlerTimer = null;
    /* access modifiers changed from: private */
    public OcppAdapter ocppAdapter = null;
    private HandlerThread thread = null;
    private OcppUpgradeSession upgradeSession = null;

    public static OcppDCAPGateway getInstance() {
        if (instance == null) {
            instance = new OcppDCAPGateway();
        }
        return instance;
    }

    private class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r11) {
            /*
                r10 = this;
                int r5 = r11.what     // Catch:{ Exception -> 0x002b }
                switch(r5) {
                    case 77825: goto L_0x0009;
                    case 77826: goto L_0x005b;
                    case 77827: goto L_0x007d;
                    case 77828: goto L_0x0088;
                    case 77829: goto L_0x0093;
                    case 77830: goto L_0x00a2;
                    default: goto L_0x0005;
                }
            L_0x0005:
                super.handleMessage(r11)
                return
            L_0x0009:
                java.lang.Object r1 = r11.obj     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.core.api.bean.DCAPMessage r1 = (com.xcharge.charger.core.api.bean.DCAPMessage) r1     // Catch:{ Exception -> 0x002b }
                java.lang.String r5 = "OcppDCAPGateway.handleMessage"
                java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002b }
                java.lang.String r7 = "receive DCAP indicate: "
                r6.<init>(r7)     // Catch:{ Exception -> 0x002b }
                java.lang.String r7 = r1.toJson()     // Catch:{ Exception -> 0x002b }
                java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ Exception -> 0x002b }
                java.lang.String r6 = r6.toString()     // Catch:{ Exception -> 0x002b }
                android.util.Log.i(r5, r6)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway r5 = com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                r5.handleDCAPIndicate(r1)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            L_0x002b:
                r2 = move-exception
                java.lang.String r5 = "OcppDCAPGateway.handleMessage"
                java.lang.StringBuilder r6 = new java.lang.StringBuilder
                java.lang.String r7 = "except: "
                r6.<init>(r7)
                java.lang.String r7 = android.util.Log.getStackTraceString(r2)
                java.lang.StringBuilder r6 = r6.append(r7)
                java.lang.String r6 = r6.toString()
                android.util.Log.e(r5, r6)
                java.lang.StringBuilder r5 = new java.lang.StringBuilder
                java.lang.String r6 = "OcppDCAPGateway handleMessage exception: "
                r5.<init>(r6)
                java.lang.String r6 = android.util.Log.getStackTraceString(r2)
                java.lang.StringBuilder r5 = r5.append(r6)
                java.lang.String r5 = r5.toString()
                com.xcharge.common.utils.LogUtils.syslog(r5)
                goto L_0x0005
            L_0x005b:
                java.lang.Object r0 = r11.obj     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.core.api.bean.DCAPMessage r0 = (com.xcharge.charger.core.api.bean.DCAPMessage) r0     // Catch:{ Exception -> 0x002b }
                java.lang.String r5 = "OcppDCAPGateway.handleMessage"
                java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002b }
                java.lang.String r7 = "receive DCAP Confirm: "
                r6.<init>(r7)     // Catch:{ Exception -> 0x002b }
                java.lang.String r7 = r0.toJson()     // Catch:{ Exception -> 0x002b }
                java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ Exception -> 0x002b }
                java.lang.String r6 = r6.toString()     // Catch:{ Exception -> 0x002b }
                android.util.Log.i(r5, r6)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway r5 = com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                r5.handleDCAPConfirm(r0)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            L_0x007d:
                java.lang.Object r3 = r11.obj     // Catch:{ Exception -> 0x002b }
                org.json.JSONArray r3 = (org.json.JSONArray) r3     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway r5 = com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                r5.handleOcppRequest(r3)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            L_0x0088:
                java.lang.Object r4 = r11.obj     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.ocpp.session.OcppRequestSession r4 = (com.xcharge.charger.protocol.ocpp.session.OcppRequestSession) r4     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway r5 = com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                r5.handleOcppResponse(r4)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            L_0x0093:
                com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway r5 = com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.ocpp.router.OcppAdapter r6 = r5.ocppAdapter     // Catch:{ Exception -> 0x002b }
                java.lang.Object r5 = r11.obj     // Catch:{ Exception -> 0x002b }
                org.json.JSONArray r5 = (org.json.JSONArray) r5     // Catch:{ Exception -> 0x002b }
                r6.handleFailedOcppRequest(r5)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            L_0x00a2:
                com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway r5 = com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                r5.maitainAdapter()     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway r5 = com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                com.xcharge.common.utils.HandlerTimer r5 = r5.handlerTimer     // Catch:{ Exception -> 0x002b }
                r6 = 1000(0x3e8, double:4.94E-321)
                r8 = 77830(0x13006, float:1.09063E-40)
                r9 = 0
                r5.startTimer(r6, r8, r9)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway.MsgHandler.handleMessage(android.os.Message):void");
        }
    }

    public void init(Context context2) {
        this.context = context2;
        this.ocppAdapter = new OcppAdapter();
        this.ocppAdapter.init(this.context);
        this.dcapAdapter = new DCAPAdapter();
        this.dcapAdapter.init(this.context);
        this.thread = new HandlerThread("OcppDCAPGateway", 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context2);
        this.handlerTimer.startTimer(1000, 77830, (Object) null);
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

    /* access modifiers changed from: private */
    public void maitainAdapter() {
        this.ocppAdapter.maintainSession();
        this.dcapAdapter.maintainSession();
    }

    /* access modifiers changed from: private */
    public void handleOcppRequest(JSONArray jsonArray) {
        try {
            String string = jsonArray.getString(2);
            switch (string.hashCode()) {
                case -977722974:
                    if (string.equals(OcppMessage.RemoteStartTransaction)) {
                        this.dcapAdapter.handleRemoteStartTransactionReq(jsonArray);
                        return;
                    }
                    return;
                case -687158844:
                    if (string.equals(OcppMessage.UpdateFirmware)) {
                        handleUpdateFirmwareReq(jsonArray);
                        return;
                    }
                    return;
                case -595338935:
                    if (string.equals(OcppMessage.UnlockConnector)) {
                        this.dcapAdapter.handleUnlockConnectorReq(jsonArray);
                        return;
                    }
                    return;
                case -391574318:
                    if (string.equals(OcppMessage.CancelReservation)) {
                        this.dcapAdapter.handleCancelReservationReq(jsonArray);
                        return;
                    }
                    return;
                case -362842058:
                    if (string.equals(OcppMessage.RemoteStopTransaction)) {
                        this.dcapAdapter.handleRemoteStopTransactionReq(jsonArray);
                        return;
                    }
                    return;
                case 78851375:
                    if (string.equals(OcppMessage.Reset)) {
                        this.dcapAdapter.handleResetReq(jsonArray);
                        return;
                    }
                    return;
                case 280557722:
                    if (string.equals(OcppMessage.ReserveNow)) {
                        this.dcapAdapter.handleReserveNowReq(jsonArray);
                        return;
                    }
                    return;
                default:
                    return;
            }
        } catch (Exception e) {
            Log.w("OcppDCAPGateway.handleOcppRequest", Log.getStackTraceString(e));
        }
        Log.w("OcppDCAPGateway.handleOcppRequest", Log.getStackTraceString(e));
    }

    private void handleUpdateFirmwareReq(JSONArray jsonArray) {
        try {
            String scheme = new URI(((UpdateFirmwareReq) new UpdateFirmwareReq().fromJson(jsonArray.getJSONObject(3).toString())).getLocation()).getScheme();
            Boolean type = OcppProtocolAgent.getInstance().isSupported(scheme);
            if (type != null && !type.booleanValue()) {
                OcppProtocolAgent.getInstance().sendFirmwareStatusNotificationReq(false, FirmwareStatus.DownloadFailed);
            } else if (OcppProtocolAgent.getInstance().isHttp(scheme)) {
                handleHttpUpdateFirmwareReq(jsonArray);
            } else {
                handleFtpUpdateFirmwareReq(jsonArray);
            }
        } catch (Exception e) {
            Log.w("OcppDCAPGateway.handleUpdateFirmwareReq", Log.getStackTraceString(e));
        }
    }

    private void handleHttpUpdateFirmwareReq(JSONArray jsonArray) {
        try {
            UpdateFirmwareReq updateFirmwareReq = (UpdateFirmwareReq) new UpdateFirmwareReq().fromJson(jsonArray.getJSONObject(3).toString());
            OcppUpgradeSession upgradeSession2 = getUpgradeSession();
            if (TextUtils.isEmpty(upgradeSession2.getStage())) {
                upgradeSession2.setStage(UpgradeProgress.STAGE_DOWNLOAD);
                upgradeSession2.setUpdateFirmwareReq(updateFirmwareReq);
                String url = updateFirmwareReq.getLocation();
                upgradeSession2.setDownloadFile("/data/data/com.xcharge.charger/download/upgrade/update.dat");
                Log.i("OcppDCAPGateway.handleUpgeadeRequest", "start download ..., url: " + url);
                downloadProgress(200, 1, 0);
                OcppProtocolAgent.getInstance().sendFirmwareStatusNotificationReq(false, FirmwareStatus.Downloading);
                HttpDownloadManager.getInstance().downloadFile(this.context, url, "/data/data/com.xcharge.charger/download/upgrade/update.dat", new HttpDownloadManager.DownLoadListener() {
                    public void onDownLoadPercentage(long curPosition, long total) {
                    }

                    public void onDownLoadPercentage(int p) {
                        OcppDCAPGateway.this.downloadProgress(200, 2, p);
                    }

                    public void onDownLoadFail() {
                        OcppDCAPGateway.this.exitUpgrade();
                    }

                    public void onDownLoadComplete() {
                        OcppProtocolAgent.getInstance().sendFirmwareStatusNotificationReq(false, FirmwareStatus.Downloaded);
                        OcppDCAPGateway.this.upgradeRequest();
                    }
                });
                return;
            }
            OcppProtocolAgent.getInstance().sendFirmwareStatusNotificationReq(false, FirmwareStatus.DownloadFailed);
        } catch (Exception e) {
            Log.w("OcppDCAPGateway.handleHttpUpdateFirmwareReq", Log.getStackTraceString(e));
            exitUpgrade();
        }
    }

    private void handleFtpUpdateFirmwareReq(JSONArray jsonArray) {
        try {
            UpdateFirmwareReq updateFirmwareReq = (UpdateFirmwareReq) new UpdateFirmwareReq().fromJson(jsonArray.getJSONObject(3).toString());
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
            OcppUpgradeSession upgradeSession2 = getUpgradeSession();
            if (TextUtils.isEmpty(upgradeSession2.getStage())) {
                upgradeSession2.setStage(UpgradeProgress.STAGE_DOWNLOAD);
                upgradeSession2.setUpdateFirmwareReq(updateFirmwareReq);
                downloadProgress(200, 1, 0);
                upgradeSession2.setDownloadFile("/data/data/com.xcharge.charger/download/upgrade/update.dat");
                FtpUtils.FtpConfig cfg = new FtpUtils.FtpConfig();
                cfg.setHost(host);
                cfg.setUsername(username);
                cfg.setPassword(password);
                FtpUtils.download(path, "/data/data/com.xcharge.charger/download/upgrade/update.dat", cfg, new FtpUtils.TransferListener() {
                    public void onConnected() {
                        OcppProtocolAgent.getInstance().sendFirmwareStatusNotificationReq(false, FirmwareStatus.Downloading);
                    }

                    public void onConnectFail() {
                        OcppDCAPGateway.this.exitUpgrade();
                    }

                    public void onTransferPercentage(long downloaded, long total) {
                    }

                    public void onTransferPercentage(int percent) {
                        OcppDCAPGateway.this.downloadProgress(200, 2, percent);
                    }

                    public void onTransferFail() {
                        OcppDCAPGateway.this.exitUpgrade();
                    }

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

    /* access modifiers changed from: private */
    public void downloadProgress(int error, int status, int progress) {
        UpgradeProgress upgradeProgress = SoftwareStatusCacheProvider.getInstance().getUpgradeProgress();
        upgradeProgress.setStage(UpgradeProgress.STAGE_DOWNLOAD);
        upgradeProgress.setUpgradeData((UpgradeData) null);
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

    /* access modifiers changed from: private */
    public void upgradeRequest() {
        CAPDirectiveOption opt = new CAPDirectiveOption();
        OcppUpgradeSession upgradeSession2 = getUpgradeSession();
        UpdateFirmwareReq updateFirmwareReq = upgradeSession2.getUpdateFirmwareReq();
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
        upgrade.setSrcPath(upgradeSession2.getDownloadFile());
        DCAPMessage setRequest = createRequest("server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform(), "upgrade", opt, upgrade);
        DCAPProxy.getInstance().sendRequest(setRequest);
        upgradeSession2.setStage("update");
        upgradeSession2.setUpgradeDCAPRequestSeq(Long.valueOf(setRequest.getSeq()));
        OcppProtocolAgent.getInstance().sendFirmwareStatusNotificationReq(false, FirmwareStatus.Installing);
    }

    public void setDCAPRequest(String setId, HashMap<String, Object> values) {
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setSet_id(setId);
        SetDirective set = new SetDirective();
        set.setValues(values);
        DCAPProxy.getInstance().sendRequest(createRequest("server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform(), "set", opt, set));
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

    /* access modifiers changed from: private */
    public void handleOcppResponse(OcppRequestSession session) {
        try {
            JSONArray request = session.getRequest();
            JSONArray response = session.getResponse();
            String string = request.getString(2);
            switch (string.hashCode()) {
                case -815388727:
                    if (string.equals(OcppMessage.Authorize)) {
                        this.ocppAdapter.handleAuthResponse(response);
                        return;
                    }
                    return;
                case 77777212:
                    if (string.equals(OcppMessage.StartTransaction)) {
                        this.dcapAdapter.setMaxChargeEnergy(request);
                        return;
                    }
                    return;
                default:
                    return;
            }
        } catch (Exception e) {
            Log.w("OcppDCAPGateway.handleOcppResponse", Log.getStackTraceString(e));
        }
        Log.w("OcppDCAPGateway.handleOcppResponse", Log.getStackTraceString(e));
    }

    private void handleUpgradeConfirm(DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) confirm.getData();
        Long peerSeq = cap.getOpt().getSeq();
        OcppUpgradeSession upgradeSession2 = OcppProtocolAgent.getInstance().getUpgradeSession();
        if (!peerSeq.equals(upgradeSession2.getUpgradeDCAPRequestSeq())) {
            Log.w("OcppDCAPGateway.handleUpgradeConfirm", "not confirm for now upgrade session: " + upgradeSession2.toJson());
            return;
        }
        String op = cap.getOp();
        if ("ack".equals(op)) {
            Log.i("OcppDCAPGateway.handleUpgradeConfirm", "succeed to install update !!!");
            OcppProtocolAgent.getInstance().sendFirmwareStatusNotificationReq(false, FirmwareStatus.Installed);
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
            ErrorCode error = SoftwareStatusCacheProvider.getInstance().getUpgradeProgress().getError();
            if (error == null || error.getCode() != 60010) {
                OcppProtocolAgent.getInstance().sendFirmwareStatusNotificationReq(false, FirmwareStatus.InstallationFailed);
                Log.i("OcppDCAPGateway.handleUpgradeConfirm", "failed to install update !!!");
            } else {
                Log.w("OcppDCAPGateway.handleUpgradeConfirm", "charging now, will install update later !!!");
                return;
            }
        }
        clearUpgradeSession();
    }

    /* access modifiers changed from: private */
    public void handleDCAPIndicate(DCAPMessage indicate) {
        CAPMessage cap = (CAPMessage) indicate.getData();
        String op = cap.getOp();
        if ("auth".equals(op)) {
            cap.setData((AuthDirective) new AuthDirective().fromJson(JsonBean.ObjectToJson(cap.getData())));
            this.ocppAdapter.handleAuthIndicate(indicate);
        } else if ("query".equals(op)) {
            cap.setData((QueryDirective) new QueryDirective().fromJson(JsonBean.ObjectToJson(cap.getData())));
            this.ocppAdapter.handleQueryIndicate(indicate);
        } else if (CAPMessage.DIRECTIVE_INIT_ACK.equals(op)) {
            cap.setData((InitAckDirective) new InitAckDirective().fromJson(JsonBean.ObjectToJson(cap.getData())));
            this.dcapAdapter.handleInitAckIndicate(indicate);
        } else if ("fin".equals(op)) {
            cap.setData((FinDirective) new FinDirective().fromJson(JsonBean.ObjectToJson(cap.getData())));
            this.dcapAdapter.handleFinIndicate(indicate);
        } else if ("event".equals(op)) {
            cap.setData((EventDirective) new EventDirective().fromJson(JsonBean.ObjectToJson(cap.getData())));
            handleEventIndicate(indicate);
        }
    }

    /* access modifiers changed from: private */
    public void handleDCAPConfirm(DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) confirm.getData();
        String op = cap.getOp();
        if ("ack".equals(op)) {
            cap.setData((AckDirective) new AckDirective().fromJson(JsonBean.ObjectToJson(cap.getData())));
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
            cap.setData((NackDirective) new NackDirective().fromJson(JsonBean.ObjectToJson(cap.getData())));
        } else {
            Log.w("OcppDCAPGateway.handleDCAPConfirm", "illegal DCAP conform: " + confirm.toJson());
            return;
        }
        String peerOp = cap.getOpt().getOp();
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
        String charge_id = opt.getCharge_id();
        String eventId = opt.getEvent_id();
        if (EventDirective.EVENT_CHARGE_PAUSE.equals(eventId)) {
            HashMap<String, Object> attach2 = event.getAttach();
            if (attach2 != null) {
                String error = (String) attach2.get("error");
                String str = (String) attach2.get(ChargeStopCondition.TYPE_TIME);
                if (!TextUtils.isEmpty(error) && 50001 == ((ErrorCode) new ErrorCode().fromJson(error)).getCode() && (portHandler2 = OcppProtocolAgent.getInstance().getPortHandler("1")) != null) {
                    portHandler2.getChargeSession().setEvSuspendStatus(true);
                    portHandler2.sendStatusNotificationReq(false, "50019");
                }
            }
        } else if (EventDirective.EVENT_CHARGE_RESUME.equals(eventId) && (attach = event.getAttach()) != null) {
            String error2 = (String) attach.get("error");
            String str2 = (String) attach.get(ChargeStopCondition.TYPE_TIME);
            if (!TextUtils.isEmpty(error2) && Integer.parseInt(error2) == 50001 && (portHandler = OcppProtocolAgent.getInstance().getPortHandler("1")) != null) {
                portHandler.getChargeSession().setEvSuspendStatus(false);
                portHandler.sendStatusNotificationReq(false, (String) null);
            }
        }
    }
}
