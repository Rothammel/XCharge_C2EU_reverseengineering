package com.xcharge.charger.protocol.family.xcloud.router;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
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
    private Context context = null;
    private DCAPAdapter dcapAdapter = null;
    private MsgHandler handler = null;
    /* access modifiers changed from: private */
    public HandlerTimer handlerTimer = null;
    private HandlerThread thread = null;
    private XCloudUpgradeSession upgradeSession = null;
    /* access modifiers changed from: private */
    public XCloudAdapter xcloudAdapter = null;

    public static XCloudDCAPGateway getInstance() {
        if (instance == null) {
            instance = new XCloudDCAPGateway();
        }
        return instance;
    }

    private class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r12) {
            /*
                r11 = this;
                int r6 = r12.what     // Catch:{ Exception -> 0x002b }
                switch(r6) {
                    case 77825: goto L_0x0009;
                    case 77826: goto L_0x005b;
                    case 77827: goto L_0x007d;
                    case 77828: goto L_0x0088;
                    case 77829: goto L_0x009b;
                    case 77830: goto L_0x00aa;
                    case 77831: goto L_0x00c0;
                    default: goto L_0x0005;
                }
            L_0x0005:
                super.handleMessage(r12)
                return
            L_0x0009:
                java.lang.Object r1 = r12.obj     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.core.api.bean.DCAPMessage r1 = (com.xcharge.charger.core.api.bean.DCAPMessage) r1     // Catch:{ Exception -> 0x002b }
                java.lang.String r6 = "XCloudDCAPGateway.handleMessage"
                java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002b }
                java.lang.String r8 = "receive DCAP indicate: "
                r7.<init>(r8)     // Catch:{ Exception -> 0x002b }
                java.lang.String r8 = r1.toJson()     // Catch:{ Exception -> 0x002b }
                java.lang.StringBuilder r7 = r7.append(r8)     // Catch:{ Exception -> 0x002b }
                java.lang.String r7 = r7.toString()     // Catch:{ Exception -> 0x002b }
                android.util.Log.i(r6, r7)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway r6 = com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                r6.handleDCAPIndicate(r1)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            L_0x002b:
                r2 = move-exception
                java.lang.String r6 = "XCloudDCAPGateway.handleMessage"
                java.lang.StringBuilder r7 = new java.lang.StringBuilder
                java.lang.String r8 = "except: "
                r7.<init>(r8)
                java.lang.String r8 = android.util.Log.getStackTraceString(r2)
                java.lang.StringBuilder r7 = r7.append(r8)
                java.lang.String r7 = r7.toString()
                android.util.Log.e(r6, r7)
                java.lang.StringBuilder r6 = new java.lang.StringBuilder
                java.lang.String r7 = "XCloudDCAPGateway handleMessage exception: "
                r6.<init>(r7)
                java.lang.String r7 = android.util.Log.getStackTraceString(r2)
                java.lang.StringBuilder r6 = r6.append(r7)
                java.lang.String r6 = r6.toString()
                com.xcharge.common.utils.LogUtils.syslog(r6)
                goto L_0x0005
            L_0x005b:
                java.lang.Object r0 = r12.obj     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.core.api.bean.DCAPMessage r0 = (com.xcharge.charger.core.api.bean.DCAPMessage) r0     // Catch:{ Exception -> 0x002b }
                java.lang.String r6 = "XCloudDCAPGateway.handleMessage"
                java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002b }
                java.lang.String r8 = "receive DCAP Confirm: "
                r7.<init>(r8)     // Catch:{ Exception -> 0x002b }
                java.lang.String r8 = r0.toJson()     // Catch:{ Exception -> 0x002b }
                java.lang.StringBuilder r7 = r7.append(r8)     // Catch:{ Exception -> 0x002b }
                java.lang.String r7 = r7.toString()     // Catch:{ Exception -> 0x002b }
                android.util.Log.i(r6, r7)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway r6 = com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                r6.handleDCAPConfirm(r0)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            L_0x007d:
                java.lang.Object r3 = r12.obj     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage r3 = (com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage) r3     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway r6 = com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                r6.handleXCloudRequest(r3)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            L_0x0088:
                java.lang.Object r4 = r12.obj     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.family.xcloud.session.XCloudRequestSession r4 = (com.xcharge.charger.protocol.family.xcloud.session.XCloudRequestSession) r4     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway r6 = com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage r7 = r4.getRequest()     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage r8 = r4.getResponse()     // Catch:{ Exception -> 0x002b }
                r6.handleXCloudResponse(r7, r8)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            L_0x009b:
                com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway r6 = com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.family.xcloud.router.XCloudAdapter r7 = r6.xcloudAdapter     // Catch:{ Exception -> 0x002b }
                java.lang.Object r6 = r12.obj     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage r6 = (com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage) r6     // Catch:{ Exception -> 0x002b }
                r7.handleSendXCloudRequestFail(r6)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            L_0x00aa:
                com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway r6 = com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                r6.maitainAdapter()     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway r6 = com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                com.xcharge.common.utils.HandlerTimer r6 = r6.handlerTimer     // Catch:{ Exception -> 0x002b }
                r8 = 1000(0x3e8, double:4.94E-321)
                r7 = 77830(0x13006, float:1.09063E-40)
                r10 = 0
                r6.startTimer(r8, r7, r10)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            L_0x00c0:
                java.lang.Object r5 = r12.obj     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.family.xcloud.session.XCloudRequestSession r5 = (com.xcharge.charger.protocol.family.xcloud.session.XCloudRequestSession) r5     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway r6 = com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.family.xcloud.router.XCloudAdapter r6 = r6.xcloudAdapter     // Catch:{ Exception -> 0x002b }
                r6.handU3AuthRefused(r5)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway.MsgHandler.handleMessage(android.os.Message):void");
        }
    }

    public void init(Context context2) {
        this.context = context2;
        this.xcloudAdapter = new XCloudAdapter();
        this.xcloudAdapter.init(this.context);
        this.dcapAdapter = new DCAPAdapter();
        this.dcapAdapter.init(this.context);
        this.thread = new HandlerThread("XCloudDCAPGateway", 10);
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

    /* access modifiers changed from: private */
    public void clearUpgradeSession() {
        String downloadFile = getUpgradeSession().getDownloadFile();
        if (!TextUtils.isEmpty(downloadFile)) {
            FileUtils.deleteFile(downloadFile);
        }
        this.upgradeSession = null;
    }

    /* access modifiers changed from: private */
    public void maitainAdapter() {
        this.xcloudAdapter.maintainSession();
        this.dcapAdapter.maintainSession();
    }

    /* access modifiers changed from: private */
    public void handleXCloudResponse(XCloudMessage request, XCloudMessage response) {
        String name = response.getMessageName();
        if (XCloudMessage.RequestStartCharge.equals(name)) {
            this.xcloudAdapter.handleRequestStartCharge(response);
        } else if (XCloudMessage.RequestRefuseCharge.equals(name)) {
            this.xcloudAdapter.handleRequestRefuseCharge(response);
        }
    }

    /* access modifiers changed from: private */
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
        XCloudUpgradeSession upgradeSession2 = getUpgradeSession();
        if (TextUtils.isEmpty(upgradeSession2.getStage())) {
            upgradeSession2.setStage(UpgradeProgress.STAGE_DOWNLOAD);
            upgradeSession2.setRequestUpgrade(requestUpgrade);
            final String url = requestUpgrade.getFileUrl();
            upgradeSession2.setDownloadFile("/data/data/com.xcharge.charger/download/upgrade/update.dat");
            Log.i("XCloudDCAPGateway.handleUpgradeRequest", "start download ..., url: " + url);
            downloadProgress(200, 1, 0);
            HttpDownloadManager.getInstance().downloadFile(this.context, url, "/data/data/com.xcharge.charger/download/upgrade/update.dat", new HttpDownloadManager.DownLoadListener() {
                public void onDownLoadPercentage(long curPosition, long total) {
                }

                public void onDownLoadPercentage(int p) {
                    XCloudDCAPGateway.this.downloadProgress(200, 2, p);
                }

                public void onDownLoadFail() {
                    LogUtils.cloudlog("failed to download upgrade resource: " + url);
                    XCloudProtocolAgent.getInstance().reportActionResult(requestUpgrade.getSid(), EnumActionStatus.failed, new DeviceError("ERROR", "download failed", (Object) null));
                    XCloudDCAPGateway.this.downloadProgress(ErrorCode.EC_UPGRADE_DOWNLOAD_FAIL, 0, 0);
                    XCloudDCAPGateway.this.clearUpgradeSession();
                }

                public void onDownLoadComplete() {
                    if (XCloudDCAPGateway.this.verifyFileMD5()) {
                        XCloudProtocolAgent.getInstance().reportActionResult(requestUpgrade.getSid(), EnumActionStatus.received, (DeviceError) null);
                        XCloudDCAPGateway.this.upgradeRequest();
                        return;
                    }
                    XCloudProtocolAgent.getInstance().reportActionResult(requestUpgrade.getSid(), EnumActionStatus.failed, new DeviceError("ERROR", "md5 error", (Object) null));
                    XCloudDCAPGateway.this.clearUpgradeSession();
                }
            });
            return;
        }
        LogUtils.cloudlog("in handle upgrade, reject new upgrade request !!!");
        XCloudProtocolAgent.getInstance().reportActionResult(requestUpgrade.getSid(), EnumActionStatus.failed, new DeviceError(DeviceError.NOT_IDLE, "upgrading now", (Object) null));
    }

    /* access modifiers changed from: private */
    public boolean verifyFileMD5() {
        XCloudUpgradeSession upgradeSession2 = getUpgradeSession();
        RequestUpgrade requestUpgrade = upgradeSession2.getRequestUpgrade();
        if (TextUtils.isEmpty(requestUpgrade.getFileMD5())) {
            Log.i("XCloudDCAPGateway.verifyFileMD5", "need not verify file md5 !!!");
            return true;
        }
        String md5 = "";
        String sourceMD5 = requestUpgrade.getFileMD5().toLowerCase();
        boolean isOk = false;
        String downloadFile = upgradeSession2.getDownloadFile();
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
        if (isOk) {
            return isOk;
        }
        Log.w("XCloudDCAPGateway.verifyFileMD5", "sourceMD5: " + sourceMD5 + ", fileMD5: " + md5);
        LogUtils.cloudlog("failed to verify upgrade file MD5, downloaded file MD5 is " + md5 + ", but cloud MD5 is " + sourceMD5);
        downloadProgress(ErrorCode.EC_UPGRADE_NOT_INTEGRATED, 0, 0);
        return isOk;
    }

    /* access modifiers changed from: private */
    public void upgradeRequest() {
        CAPDirectiveOption opt = new CAPDirectiveOption();
        XCloudUpgradeSession upgradeSession2 = getUpgradeSession();
        RequestUpgrade requestUpgrade = upgradeSession2.getRequestUpgrade();
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
        upgrade.setSrcPath(upgradeSession2.getDownloadFile());
        DCAPMessage setRequest = createRequest("server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform(), "upgrade", opt, upgrade);
        DCAPProxy.getInstance().sendRequest(setRequest);
        upgradeSession2.setStage("update");
        upgradeSession2.setUpgradeDCAPRequestSeq(Long.valueOf(setRequest.getSeq()));
    }

    private void handleUpgradeConfirm(DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) confirm.getData();
        Long peerSeq = cap.getOpt().getSeq();
        XCloudUpgradeSession upgradeSession2 = getUpgradeSession();
        if (!peerSeq.equals(upgradeSession2.getUpgradeDCAPRequestSeq())) {
            Log.w("XCloudDCAPGateway.handleUpgradeConfirm", "not confirm for now upgrade session: " + upgradeSession2.toJson());
            return;
        }
        RequestUpgrade requestUpgrade = upgradeSession2.getRequestUpgrade();
        String op = cap.getOp();
        if ("ack".equals(op)) {
            XCloudProtocolAgent.getInstance().reportActionResult(requestUpgrade.getSid(), EnumActionStatus.success, (DeviceError) null);
            Log.i("XCloudDCAPGateway.handleUpgradeConfirm", "succeed to install update !!!");
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
            ErrorCode error = SoftwareStatusCacheProvider.getInstance().getUpgradeProgress().getError();
            if (error == null || error.getCode() != 60010) {
                XCloudProtocolAgent.getInstance().reportActionResult(requestUpgrade.getSid(), EnumActionStatus.failed, new DeviceError("ERROR", "update failed", (Object) null));
                Log.i("XCloudDCAPGateway.handleUpgradeConfirm", "failed to install update !!!");
            } else {
                Log.w("XCloudDCAPGateway.handleUpgradeConfirm", "charging now, will install update later !!!");
                return;
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

    /* access modifiers changed from: private */
    public void handleDCAPIndicate(DCAPMessage indicate) {
        CAPMessage cap = (CAPMessage) indicate.getData();
        String op = cap.getOp();
        if ("auth".equals(op)) {
            cap.setData((AuthDirective) new AuthDirective().fromJson(JsonBean.ObjectToJson(cap.getData())));
            this.xcloudAdapter.handleAuthIndicate(indicate);
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
        CAPDirectiveOption opt = cap.getOpt();
        String peerOp = opt.getOp();
        String op = cap.getOp();
        if ("ack".equals(op)) {
            AckDirective ack = (AckDirective) new AckDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
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
                        error = (ErrorCode) new ErrorCode().fromJson(finError);
                    }
                }
                XCloudProtocolAgent.getInstance().handleFinConfirm(opt.getCharge_id(), mode, error);
            }
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
            cap.setData((NackDirective) new NackDirective().fromJson(JsonBean.ObjectToJson(cap.getData())));
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
                    ErrorCode errorCode = (ErrorCode) new ErrorCode().fromJson(error);
                    PortStatus portStatus = null;
                    HashMap<String, Object> errorData = errorCode.getData();
                    if (errorData != null) {
                        String portChargeStatus = (String) errorData.get("portChargeStatus");
                        if (!TextUtils.isEmpty(portChargeStatus)) {
                            portStatus = (PortStatus) new PortStatus().fromJson(portChargeStatus);
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
                                    deviceErrorDataAttach.addProperty("current", (Number) portStatus.getAmps().get(1));
                                    deviceErrorData.add("data", deviceErrorDataAttach);
                                } else {
                                    if (PHASE.THREE_PHASE.getPhase() == phase.getPhase()) {
                                        JsonObject deviceErrorDataAttach2 = new JsonObject();
                                        JsonParser jsonParser = new JsonParser();
                                        String json = JsonBean.getGsonBuilder().create().toJson((Object) new ArrayList(Arrays.asList(new Double[]{portStatus.getAmps().get(1), portStatus.getAmps().get(2), portStatus.getAmps().get(3)})), new TypeToken<ArrayList>() {
                                        }.getType());
                                        deviceErrorDataAttach2.add("current", jsonParser.parse(json).getAsJsonArray());
                                        deviceErrorData.add("data", deviceErrorDataAttach2);
                                    }
                                }
                            } else if (errorCode.getCode() == 30014) {
                                if (PHASE.SINGLE_PHASE.getPhase() == phase.getPhase()) {
                                    JsonObject deviceErrorDataAttach3 = new JsonObject();
                                    deviceErrorDataAttach3.addProperty("voltage", (Number) portStatus.getVolts().get(0));
                                    deviceErrorData.add("data", deviceErrorDataAttach3);
                                } else {
                                    if (PHASE.THREE_PHASE.getPhase() == phase.getPhase()) {
                                        JsonObject deviceErrorDataAttach4 = new JsonObject();
                                        deviceErrorDataAttach4.add("voltage", new JsonParser().parse(JsonBean.getGsonBuilder().create().toJson((Object) portStatus.getVolts(), new TypeToken<ArrayList>() {
                                        }.getType())).getAsJsonArray());
                                        deviceErrorData.add("data", deviceErrorDataAttach4);
                                    }
                                }
                            } else if (errorCode.getCode() == 30017) {
                                JsonObject deviceErrorDataAttach5 = new JsonObject();
                                deviceErrorDataAttach5.addProperty("current", (Number) portStatus.getLeakAmp());
                                deviceErrorData.add("data", deviceErrorDataAttach5);
                            } else if (errorCode.getCode() == 30016) {
                                JsonObject deviceErrorDataAttach6 = new JsonObject();
                                deviceErrorDataAttach6.addProperty("temperature", (Number) Double.valueOf(portStatus.getTemprature().doubleValue() / 10.0d));
                                deviceErrorData.add("data", deviceErrorDataAttach6);
                            } else if (errorCode.getCode() == 30018) {
                                JsonObject deviceErrorDataAttach7 = new JsonObject();
                                deviceErrorDataAttach7.addProperty("voltage", (Number) portStatus.getCpVoltage());
                                deviceErrorData.add("data", deviceErrorDataAttach7);
                            }
                        }
                        deviceError = new DeviceError("ERROR", (String) null, deviceErrorData);
                    } else if (50001 == errorCode.getCode()) {
                        JsonObject deviceErrorData2 = new JsonObject();
                        deviceErrorData2.addProperty("code", "5019");
                        deviceError = new DeviceError("ERROR", (String) null, deviceErrorData2);
                    } else {
                        deviceError = new DeviceError("ERROR", (String) null, (Object) null);
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
                        deviceError2 = new DeviceError("ERROR", (String) null, deviceErrorData3);
                    }
                }
                if (!TextUtils.isEmpty(time2)) {
                    resumeTime = TimeUtils.getXCloudFormat(Long.parseLong(time2), RemoteSettingCacheProvider.getInstance().getProtocolTimezone());
                }
            }
            XCloudProtocolAgent.getInstance().sendReportChargeResumed(chargeId, deviceError2, resumeTime);
        } else if ("delay_start".equals(eventId)) {
            XCloudProtocolAgent.getInstance().sendReportDelayFeeStarted(chargeId, event.getDelay_start());
        } else if (EventDirective.EVENT_DEALY_WAIT_START.equals(eventId)) {
            XCloudProtocolAgent.getInstance().sendReportDelayCountStarted(chargeId);
        }
    }
}
