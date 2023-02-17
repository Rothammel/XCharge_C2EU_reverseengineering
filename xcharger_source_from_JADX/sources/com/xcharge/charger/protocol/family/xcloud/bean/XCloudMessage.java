package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

public class XCloudMessage extends JsonBean<XCloudMessage> {
    public static final String AnswerHello = "AnswerHello";
    public static final String ApplySetting = "ApplySetting";
    public static final String CancelAutoStop = "CancelAutoStop";
    public static final String ConfirmChargeEnded = "ConfirmChargeEnded";
    public static final String ConfirmChargeStarted = "ConfirmChargeStarted";
    public static final String ConfirmLocalChargeBill = "ConfirmLocalChargeBill";
    public static final String QueryLog = "QueryLog";
    public static final String QueryState = "QueryState";
    public static final String QuerySystemInfo = "QuerySystemInfo";
    public static final String ReportActionResult = "ReportActionResult";
    public static final String ReportAutoStopResult = "ReportAutoStopResult";
    public static final String ReportChargeCancelled = "ReportChargeCancelled";
    public static final String ReportChargeEnded = "ReportChargeEnded";
    public static final String ReportChargePaused = "ReportChargePaused";
    public static final String ReportChargeResumed = "ReportChargeResumed";
    public static final String ReportChargeStarted = "ReportChargeStarted";
    public static final String ReportChargeStatus = "ReportChargeStatus";
    public static final String ReportChargeStopped = "ReportChargeStopped";
    public static final String ReportDelayCountStarted = "ReportDelayCountStarted";
    public static final String ReportDelayFeeStarted = "ReportDelayFeeStarted";
    public static final String ReportError = "ReportError";
    public static final String ReportErrorRecovery = "ReportErrorRecovery";
    public static final String ReportLocalChargeBill = "ReportLocalChargeBill";
    public static final String ReportLocalChargeEnded = "ReportLocalChargeEnded";
    public static final String ReportLocalChargeStarted = "ReportLocalChargeStarted";
    public static final String ReportNetworkStatus = "ReportNetworkStatus";
    public static final String ReportPlugStatus = "ReportPlugStatus";
    public static final String ReportSettingResult = "ReportSettingResult";
    public static final String ReportState = "ReportState";
    public static final String ReportSystemInfo = "ReportSystemInfo";
    public static final String ReportVerification = "ReportVerification";
    public static final String RequestAction = "RequestAction";
    public static final String RequestAutoStop = "RequestAutoStop";
    public static final String RequestChargeQRCode = "RequestChargeQRCode";
    public static final String RequestChargeWithIDCard = "RequestChargeWithIDCard ";
    public static final String RequestEndCharge = "RequestEndCharge";
    public static final String RequestRefuseCharge = "RequestRefuseCharge";
    public static final String RequestSetting = "RequestSetting";
    public static final String RequestStartCharge = "RequestStartCharge";
    public static final String RequestStopCharge = "RequestStopCharge";
    public static final String RequestUpdateStartTime = "RequestUpdateStartTime";
    public static final String RequestUpgrade = "RequestUpgrade";
    public static final String RequestVerification = "RequestVerification";
    public static final String SayHello = "SayHello";
    public static final String SendChargeQRCode = "SendChargeQRCode";
    public static final String UploadLog = "UploadLog";
    public static final String ver = "v1.0";
    private Object body = null;
    private String data = null;
    private String localChargeId = null;
    private String messageName = null;
    private String port = null;
    private int resendCnt = 0;
    private String sessionId = null;
    private String srcId = null;
    private String version = "v1.0";

    public String getMessageName() {
        return this.messageName;
    }

    public void setMessageName(String messageName2) {
        this.messageName = messageName2;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version2) {
        this.version = version2;
    }

    public String getSrcId() {
        return this.srcId;
    }

    public void setSrcId(String srcId2) {
        this.srcId = srcId2;
    }

    public Object getBody() {
        return this.body;
    }

    public void setBody(Object body2) {
        this.body = body2;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data2) {
        this.data = data2;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(String sessionId2) {
        this.sessionId = sessionId2;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port2) {
        this.port = port2;
    }

    public int getResendCnt() {
        return this.resendCnt;
    }

    public void setResendCnt(int resendCnt2) {
        this.resendCnt = resendCnt2;
    }

    public String getLocalChargeId() {
        return this.localChargeId;
    }

    public void setLocalChargeId(String localChargeId2) {
        this.localChargeId = localChargeId2;
    }
}
