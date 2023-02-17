package com.xcharge.charger.protocol.anyo.router;

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
import com.xcharge.charger.core.api.bean.cap.FinDirective;
import com.xcharge.charger.core.api.bean.cap.InitAckDirective;
import com.xcharge.charger.core.api.bean.cap.UpgradeDirective;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.UpgradeData;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.anyo.bean.request.StartUpgradeRequest;
import com.xcharge.charger.protocol.anyo.bean.response.AuthResponse;
import com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent;
import com.xcharge.charger.protocol.anyo.session.AnyoUpgradeSession;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.HandlerTimer;

public class AnyoDCAPGateway {
    public static final int INTERVAL_ADAPTER_MAINTAIN = 1000;
    public static final int MSG_ANYO_REQUEST = 77827;
    public static final int MSG_ANYO_RESPONSE = 77828;
    public static final int MSG_DCAP_CONFIRM = 77826;
    public static final int MSG_DCAP_INDICATE = 77825;
    public static final int MSG_FAILED_REQUEST_SERVER = 77829;
    public static final int MSG_TIMER_MAINTAIN_ADAPTER = 77830;
    private static AnyoDCAPGateway instance = null;
    /* access modifiers changed from: private */
    public AnyoAdapter anyoAdapter = null;
    private Context context = null;
    private DCAPAdapter dcapAdapter = null;
    private MsgHandler handler = null;
    /* access modifiers changed from: private */
    public HandlerTimer handlerTimer = null;
    private HandlerThread thread = null;

    public static AnyoDCAPGateway getInstance() {
        if (instance == null) {
            instance = new AnyoDCAPGateway();
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
                java.lang.Object r3 = r11.obj     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.core.api.bean.DCAPMessage r3 = (com.xcharge.charger.core.api.bean.DCAPMessage) r3     // Catch:{ Exception -> 0x002b }
                java.lang.String r5 = "AnyoDCAPGateway.handleMessage"
                java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002b }
                java.lang.String r7 = "receive DCAP indicate: "
                r6.<init>(r7)     // Catch:{ Exception -> 0x002b }
                java.lang.String r7 = r3.toJson()     // Catch:{ Exception -> 0x002b }
                java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ Exception -> 0x002b }
                java.lang.String r6 = r6.toString()     // Catch:{ Exception -> 0x002b }
                android.util.Log.i(r5, r6)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.router.AnyoDCAPGateway r5 = com.xcharge.charger.protocol.anyo.router.AnyoDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                r5.handleDCAPIndicate(r3)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            L_0x002b:
                r4 = move-exception
                java.lang.String r5 = "AnyoDCAPGateway.handleMessage"
                java.lang.StringBuilder r6 = new java.lang.StringBuilder
                java.lang.String r7 = "except: "
                r6.<init>(r7)
                java.lang.String r7 = android.util.Log.getStackTraceString(r4)
                java.lang.StringBuilder r6 = r6.append(r7)
                java.lang.String r6 = r6.toString()
                android.util.Log.e(r5, r6)
                java.lang.StringBuilder r5 = new java.lang.StringBuilder
                java.lang.String r6 = "AnyoDCAPGateway handleMessage exception: "
                r5.<init>(r6)
                java.lang.String r6 = android.util.Log.getStackTraceString(r4)
                java.lang.StringBuilder r5 = r5.append(r6)
                java.lang.String r5 = r5.toString()
                com.xcharge.common.utils.LogUtils.syslog(r5)
                goto L_0x0005
            L_0x005b:
                java.lang.Object r2 = r11.obj     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.core.api.bean.DCAPMessage r2 = (com.xcharge.charger.core.api.bean.DCAPMessage) r2     // Catch:{ Exception -> 0x002b }
                java.lang.String r5 = "AnyoDCAPGateway.handleMessage"
                java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002b }
                java.lang.String r7 = "receive DCAP Confirm: "
                r6.<init>(r7)     // Catch:{ Exception -> 0x002b }
                java.lang.String r7 = r2.toJson()     // Catch:{ Exception -> 0x002b }
                java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ Exception -> 0x002b }
                java.lang.String r6 = r6.toString()     // Catch:{ Exception -> 0x002b }
                android.util.Log.i(r5, r6)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.router.AnyoDCAPGateway r5 = com.xcharge.charger.protocol.anyo.router.AnyoDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                r5.handleDCAPConfirm(r2)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            L_0x007d:
                java.lang.Object r0 = r11.obj     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.bean.AnyoMessage r0 = (com.xcharge.charger.protocol.anyo.bean.AnyoMessage) r0     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.router.AnyoDCAPGateway r5 = com.xcharge.charger.protocol.anyo.router.AnyoDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                r5.handleAnyoRequest(r0)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            L_0x0088:
                java.lang.Object r1 = r11.obj     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.bean.AnyoMessage r1 = (com.xcharge.charger.protocol.anyo.bean.AnyoMessage) r1     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.router.AnyoDCAPGateway r5 = com.xcharge.charger.protocol.anyo.router.AnyoDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                r5.handleAnyoResponse(r1)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            L_0x0093:
                com.xcharge.charger.protocol.anyo.router.AnyoDCAPGateway r5 = com.xcharge.charger.protocol.anyo.router.AnyoDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.router.AnyoAdapter r6 = r5.anyoAdapter     // Catch:{ Exception -> 0x002b }
                java.lang.Object r5 = r11.obj     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.bean.AnyoMessage r5 = (com.xcharge.charger.protocol.anyo.bean.AnyoMessage) r5     // Catch:{ Exception -> 0x002b }
                r6.handleFailedAnyoRequest(r5)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            L_0x00a2:
                com.xcharge.charger.protocol.anyo.router.AnyoDCAPGateway r5 = com.xcharge.charger.protocol.anyo.router.AnyoDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                r5.maitainAdapter()     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.router.AnyoDCAPGateway r5 = com.xcharge.charger.protocol.anyo.router.AnyoDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                com.xcharge.common.utils.HandlerTimer r5 = r5.handlerTimer     // Catch:{ Exception -> 0x002b }
                r6 = 1000(0x3e8, double:4.94E-321)
                r8 = 77830(0x13006, float:1.09063E-40)
                r9 = 0
                r5.startTimer(r6, r8, r9)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.anyo.router.AnyoDCAPGateway.MsgHandler.handleMessage(android.os.Message):void");
        }
    }

    public void init(Context context2) {
        this.context = context2;
        this.anyoAdapter = new AnyoAdapter();
        this.anyoAdapter.init(this.context);
        this.dcapAdapter = new DCAPAdapter();
        this.dcapAdapter.init(this.context);
        this.thread = new HandlerThread("AnyoDCAPGateway", 10);
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
        this.anyoAdapter.destroy();
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
        this.anyoAdapter.maintainSession();
        this.dcapAdapter.maintainSession();
    }

    /* access modifiers changed from: private */
    public void handleAnyoRequest(AnyoMessage request) {
        switch (request.getHead().getCmdCode()) {
            case Byte.MIN_VALUE:
                this.dcapAdapter.handleResetChargeRequest(request);
                return;
            case 49:
                this.dcapAdapter.handleRebootRequest(request);
                return;
            case 60:
                this.dcapAdapter.handleStartChargeRequest(request);
                return;
            case 61:
                this.dcapAdapter.handleStopChargeRequest(request);
                return;
            case 62:
                handleStartUpgradeRequest(request);
                return;
            case 81:
                this.dcapAdapter.handleUnlockPortRequest(request);
                return;
            default:
                return;
        }
    }

    private void handleStartUpgradeRequest(AnyoMessage request) {
        CAPDirectiveOption opt = new CAPDirectiveOption();
        StartUpgradeRequest startUpgradeRequest = (StartUpgradeRequest) request;
        UpgradeDirective upgrade = new UpgradeDirective();
        upgrade.setComponent(UpgradeData.COM_ALL);
        upgrade.setVersion(String.valueOf(startUpgradeRequest.getPrimaryVersion()) + "." + startUpgradeRequest.getSecondaryVersion() + "." + startUpgradeRequest.getReviseVersion());
        AnyoUpgradeSession upgradeSession = AnyoProtocolAgent.getInstance().getUpgradeSession();
        upgrade.setSrcPath(upgradeSession.getDownloadFile());
        DCAPMessage setRequest = createRequest("server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform(), "upgrade", opt, upgrade);
        DCAPProxy.getInstance().sendRequest(setRequest);
        upgradeSession.setStage("update");
        upgradeSession.setUpgradeDCAPRequestSeq(Long.valueOf(setRequest.getSeq()));
    }

    private void handleUpgradeConfirm(DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) confirm.getData();
        Long peerSeq = cap.getOpt().getSeq();
        AnyoUpgradeSession upgradeSession = AnyoProtocolAgent.getInstance().getUpgradeSession();
        if (!peerSeq.equals(upgradeSession.getUpgradeDCAPRequestSeq())) {
            Log.w("AnyoDCAPGateway.handleUpgradeConfirm", "not confirm for now upgrade session: " + upgradeSession.toJson());
            return;
        }
        StartUpgradeRequest requestUpgrade = upgradeSession.getRequestUpgrade();
        String op = cap.getOp();
        if ("ack".equals(op)) {
            Log.i("AnyoDCAPGateway.handleUpgradeConfirm", "succeed to install update !!!");
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
            ErrorCode error = SoftwareStatusCacheProvider.getInstance().getUpgradeProgress().getError();
            if (error == null || error.getCode() != 60010) {
                Log.i("AnyoDCAPGateway.handleUpgradeConfirm", "failed to install update !!!");
            } else {
                Log.w("AnyoDCAPGateway.handleUpgradeConfirm", "charging now, will install update later !!!");
                return;
            }
        }
        AnyoProtocolAgent.getInstance().clearUpgradeSession();
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
    public void handleAnyoResponse(AnyoMessage response) {
        switch (response.getHead().getCmdCode()) {
            case 17:
                this.anyoAdapter.handleAuthResponse((AuthResponse) response);
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: private */
    public void handleDCAPIndicate(DCAPMessage indicate) {
        CAPMessage cap = (CAPMessage) indicate.getData();
        String op = cap.getOp();
        if ("auth".equals(op)) {
            cap.setData((AuthDirective) new AuthDirective().fromJson(JsonBean.ObjectToJson(cap.getData())));
            this.anyoAdapter.handleAuthIndicate(indicate);
        } else if (CAPMessage.DIRECTIVE_INIT_ACK.equals(op)) {
            cap.setData((InitAckDirective) new InitAckDirective().fromJson(JsonBean.ObjectToJson(cap.getData())));
            this.dcapAdapter.handleInitAckIndicate(indicate);
        } else if ("fin".equals(op)) {
            cap.setData((FinDirective) new FinDirective().fromJson(JsonBean.ObjectToJson(cap.getData())));
            this.dcapAdapter.handleFinIndicate(indicate);
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
            Log.w("AnyoDCAPGateway.handleDCAPConfirm", "illegal DCAP conform: " + confirm.toJson());
            return;
        }
        String peerOp = cap.getOpt().getOp();
        if ("start".equals(peerOp)) {
            this.dcapAdapter.handleStartComfirm(confirm);
        } else if ("stop".equals(peerOp)) {
            this.dcapAdapter.handleStopConfirm(confirm);
        } else if ("upgrade".equals(peerOp)) {
            handleUpgradeConfirm(confirm);
        } else if ("set".equals(peerOp)) {
            this.dcapAdapter.handleSetConfirm(confirm);
        }
    }
}
