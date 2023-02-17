package com.xcharge.charger.protocol.xmsz.router;

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
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.charger.protocol.xmsz.bean.cloud.AuthorizeIDResponse;
import com.xcharge.charger.protocol.xmsz.bean.cloud.UpdateFirmwareRequest;
import com.xcharge.charger.protocol.xmsz.handler.XMSZProtocolAgent;
import com.xcharge.charger.protocol.xmsz.session.XMSZUpgradeSession;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.HandlerTimer;

public class XMSZDCAPGateway {
    public static final int INTERVAL_ADAPTER_MAINTAIN = 1000;
    public static final int MSG_DCAP_CONFIRM = 77826;
    public static final int MSG_DCAP_INDICATE = 77825;
    public static final int MSG_FAILED_REQUEST_SERVER = 77829;
    public static final int MSG_TIMER_MAINTAIN_ADAPTER = 77830;
    public static final int MSG_XMSZ_REQUEST = 77827;
    public static final int MSG_XMSZ_RESPONSE = 77828;
    private static XMSZDCAPGateway instance = null;
    private Context context = null;
    private DCAPAdapter dcapAdapter = null;
    private MsgHandler handler = null;
    /* access modifiers changed from: private */
    public HandlerTimer handlerTimer = null;
    private HandlerThread thread = null;
    /* access modifiers changed from: private */
    public XMSZAdapter xmszAdapter = null;

    public static XMSZDCAPGateway getInstance() {
        if (instance == null) {
            instance = new XMSZDCAPGateway();
        }
        return instance;
    }

    private class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 77825:
                    DCAPMessage dcapIndicate = (DCAPMessage) msg.obj;
                    Log.i("XMSZDCAPGateway.MsgHandler", "receive DCAP indicate: " + dcapIndicate.toJson());
                    XMSZDCAPGateway.this.handleDCAPIndicate(dcapIndicate);
                    break;
                case 77826:
                    DCAPMessage dcapConfirm = (DCAPMessage) msg.obj;
                    Log.i("XMSZDCAPGateway.MsgHandler", "receive DCAP Confirm: " + dcapConfirm.toJson());
                    XMSZDCAPGateway.this.handleDCAPConfirm(dcapConfirm);
                    break;
                case 77827:
                    XMSZDCAPGateway.this.handleXMSZRequest((XMSZMessage) msg.obj);
                    break;
                case 77828:
                    XMSZDCAPGateway.this.handleXMSZResponse((XMSZMessage) msg.obj);
                    break;
                case 77829:
                    XMSZDCAPGateway.this.xmszAdapter.handleFailedXMSZRequest((XMSZMessage) msg.obj);
                    break;
                case 77830:
                    XMSZDCAPGateway.this.maitainAdapter();
                    XMSZDCAPGateway.this.handlerTimer.startTimer(1000, 77830, (Object) null);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public void init(Context context2) {
        this.context = context2;
        this.xmszAdapter = new XMSZAdapter();
        this.xmszAdapter.init(this.context);
        this.dcapAdapter = new DCAPAdapter();
        this.dcapAdapter.init(this.context);
        this.thread = new HandlerThread("XMSZDCAPGateway", 10);
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
        this.xmszAdapter.destroy();
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
        this.xmszAdapter.maintainSession();
        this.dcapAdapter.maintainSession();
    }

    /* access modifiers changed from: private */
    public void handleXMSZRequest(XMSZMessage request) {
        switch (request.getHead().getFunctionCode()) {
            case 2:
                this.dcapAdapter.handleRemoteStartChargingRequest(request);
                return;
            case 3:
                this.dcapAdapter.handleRemoteStopChargingRequest(request);
                return;
            case 9:
                handleUpdateFirmwareRequest(request);
                return;
            default:
                return;
        }
    }

    private void handleUpdateFirmwareRequest(XMSZMessage request) {
        CAPDirectiveOption opt = new CAPDirectiveOption();
        UpgradeDirective upgrade = new UpgradeDirective();
        upgrade.setComponent(UpgradeData.COM_ALL);
        upgrade.setVersion(((UpdateFirmwareRequest) request).getVersion());
        XMSZUpgradeSession upgradeSession = XMSZProtocolAgent.getInstance().getUpgradeSession();
        upgrade.setSrcPath(upgradeSession.getDownloadFile());
        DCAPMessage setRequest = createRequest("server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform(), "upgrade", opt, upgrade);
        DCAPProxy.getInstance().sendRequest(setRequest);
        upgradeSession.setStage("update");
        upgradeSession.setUpgradeDCAPRequestSeq(Long.valueOf(setRequest.getSeq()));
    }

    private void handleUpgradeConfirm(DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) confirm.getData();
        Long peerSeq = cap.getOpt().getSeq();
        XMSZUpgradeSession upgradeSession = XMSZProtocolAgent.getInstance().getUpgradeSession();
        if (!peerSeq.equals(upgradeSession.getUpgradeDCAPRequestSeq())) {
            Log.w("XMSZDCAPGateway.handleUpgradeConfirm", "not confirm for now upgrade session: " + upgradeSession.toJson());
            return;
        }
        UpdateFirmwareRequest requestUpgrade = upgradeSession.getRequestUpgrade();
        String op = cap.getOp();
        if ("ack".equals(op)) {
            Log.i("XMSZDCAPGateway.handleUpgradeConfirm", "succeed to install update !!!");
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
            ErrorCode error = SoftwareStatusCacheProvider.getInstance().getUpgradeProgress().getError();
            if (error == null || error.getCode() != 60010) {
                Log.i("XMSZDCAPGateway.handleUpgradeConfirm", "failed to install update !!!");
            } else {
                Log.w("XMSZDCAPGateway.handleUpgradeConfirm", "charging now, will install update later !!!");
                return;
            }
        }
        XMSZProtocolAgent.getInstance().clearUpgradeSession();
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
    public void handleXMSZResponse(XMSZMessage response) {
        switch (response.getHead().getFunctionCode()) {
            case -110:
                this.xmszAdapter.handleAuthorizeIDResponse((AuthorizeIDResponse) response);
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
            this.xmszAdapter.handleAuthIndicate(indicate);
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
            Log.w("XMSZDCAPGateway.handleDCAPConfirm", "illegal DCAP conform: " + confirm.toJson());
            return;
        }
        String peerOp = cap.getOpt().getOp();
        if ("stop".equals(peerOp)) {
            this.dcapAdapter.handleStopConfirm(confirm);
        } else if ("upgrade".equals(peerOp)) {
            handleUpgradeConfirm(confirm);
        }
    }
}
