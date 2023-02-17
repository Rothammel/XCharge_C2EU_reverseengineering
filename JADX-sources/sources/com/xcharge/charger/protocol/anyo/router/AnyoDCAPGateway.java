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
import com.xcharge.charger.data.bean.UpgradeProgress;
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
import com.xcharge.common.utils.LogUtils;

/* loaded from: classes.dex */
public class AnyoDCAPGateway {
    public static final int INTERVAL_ADAPTER_MAINTAIN = 1000;
    public static final int MSG_ANYO_REQUEST = 77827;
    public static final int MSG_ANYO_RESPONSE = 77828;
    public static final int MSG_DCAP_CONFIRM = 77826;
    public static final int MSG_DCAP_INDICATE = 77825;
    public static final int MSG_FAILED_REQUEST_SERVER = 77829;
    public static final int MSG_TIMER_MAINTAIN_ADAPTER = 77830;
    private static AnyoDCAPGateway instance = null;
    private HandlerThread thread = null;
    private MsgHandler handler = null;
    private HandlerTimer handlerTimer = null;
    private Context context = null;
    private AnyoAdapter anyoAdapter = null;
    private DCAPAdapter dcapAdapter = null;

    public static AnyoDCAPGateway getInstance() {
        if (instance == null) {
            instance = new AnyoDCAPGateway();
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
                        Log.i("AnyoDCAPGateway.handleMessage", "receive DCAP indicate: " + dcapIndicate.toJson());
                        AnyoDCAPGateway.this.handleDCAPIndicate(dcapIndicate);
                        break;
                    case 77826:
                        DCAPMessage dcapConfirm = (DCAPMessage) msg.obj;
                        Log.i("AnyoDCAPGateway.handleMessage", "receive DCAP Confirm: " + dcapConfirm.toJson());
                        AnyoDCAPGateway.this.handleDCAPConfirm(dcapConfirm);
                        break;
                    case 77827:
                        AnyoMessage anyoRequest = (AnyoMessage) msg.obj;
                        AnyoDCAPGateway.this.handleAnyoRequest(anyoRequest);
                        break;
                    case 77828:
                        AnyoMessage anyoResponse = (AnyoMessage) msg.obj;
                        AnyoDCAPGateway.this.handleAnyoResponse(anyoResponse);
                        break;
                    case 77829:
                        AnyoDCAPGateway.this.anyoAdapter.handleFailedAnyoRequest((AnyoMessage) msg.obj);
                        break;
                    case 77830:
                        AnyoDCAPGateway.this.maitainAdapter();
                        AnyoDCAPGateway.this.handlerTimer.startTimer(1000L, 77830, null);
                        break;
                }
            } catch (Exception e) {
                Log.e("AnyoDCAPGateway.handleMessage", "except: " + Log.getStackTraceString(e));
                LogUtils.syslog("AnyoDCAPGateway handleMessage exception: " + Log.getStackTraceString(e));
            }
            super.handleMessage(msg);
        }
    }

    public void init(Context context) {
        this.context = context;
        this.anyoAdapter = new AnyoAdapter();
        this.anyoAdapter.init(this.context);
        this.dcapAdapter = new DCAPAdapter();
        this.dcapAdapter.init(this.context);
        this.thread = new HandlerThread("AnyoDCAPGateway", 10);
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

    /* JADX INFO: Access modifiers changed from: private */
    public void maitainAdapter() {
        this.anyoAdapter.maintainSession();
        this.dcapAdapter.maintainSession();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAnyoRequest(AnyoMessage request) {
        byte cmd = request.getHead().getCmdCode();
        switch (cmd) {
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
        String version = String.valueOf((int) startUpgradeRequest.getPrimaryVersion()) + "." + ((int) startUpgradeRequest.getSecondaryVersion()) + "." + ((int) startUpgradeRequest.getReviseVersion());
        upgrade.setVersion(version);
        AnyoUpgradeSession upgradeSession = AnyoProtocolAgent.getInstance().getUpgradeSession();
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
        AnyoUpgradeSession upgradeSession = AnyoProtocolAgent.getInstance().getUpgradeSession();
        if (!peerSeq.equals(upgradeSession.getUpgradeDCAPRequestSeq())) {
            Log.w("AnyoDCAPGateway.handleUpgradeConfirm", "not confirm for now upgrade session: " + upgradeSession.toJson());
            return;
        }
        upgradeSession.getRequestUpgrade();
        String op = cap.getOp();
        if ("ack".equals(op)) {
            Log.i("AnyoDCAPGateway.handleUpgradeConfirm", "succeed to install update !!!");
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
            UpgradeProgress upgradeProgress = SoftwareStatusCacheProvider.getInstance().getUpgradeProgress();
            ErrorCode error = upgradeProgress.getError();
            if (error != null && error.getCode() == 60010) {
                Log.w("AnyoDCAPGateway.handleUpgradeConfirm", "charging now, will install update later !!!");
                return;
            }
            Log.i("AnyoDCAPGateway.handleUpgradeConfirm", "failed to install update !!!");
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

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAnyoResponse(AnyoMessage response) {
        byte cmd = response.getHead().getCmdCode();
        switch (cmd) {
            case 17:
                this.anyoAdapter.handleAuthResponse((AuthResponse) response);
                return;
            default:
                return;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDCAPIndicate(DCAPMessage indicate) {
        CAPMessage cap = (CAPMessage) indicate.getData();
        String op = cap.getOp();
        if ("auth".equals(op)) {
            AuthDirective auth = new AuthDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
            cap.setData(auth);
            this.anyoAdapter.handleAuthIndicate(indicate);
        } else if (CAPMessage.DIRECTIVE_INIT_ACK.equals(op)) {
            InitAckDirective initAck = new InitAckDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
            cap.setData(initAck);
            this.dcapAdapter.handleInitAckIndicate(indicate);
        } else if ("fin".equals(op)) {
            FinDirective fin = new FinDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
            cap.setData(fin);
            this.dcapAdapter.handleFinIndicate(indicate);
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
            Log.w("AnyoDCAPGateway.handleDCAPConfirm", "illegal DCAP conform: " + confirm.toJson());
            return;
        }
        CAPDirectiveOption opt = cap.getOpt();
        String peerOp = opt.getOp();
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
