package com.xcharge.charger.protocol.anyo.bean.response;

import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import java.util.InputMismatchException;

/* loaded from: classes.dex */
public class QuerySysInfoResponse extends AnyoMessage {
    private byte primaryVersion = 0;
    private byte secondaryVersion = 0;
    private byte reviseVersion = 0;
    private int pileNameLength = 0;
    private String pileName = null;
    private int operatorNameLength = 0;
    private String operatorName = null;
    private int hostIp = 0;
    private int hostPort = 0;

    public byte getPrimaryVersion() {
        return this.primaryVersion;
    }

    public void setPrimaryVersion(byte primaryVersion) {
        this.primaryVersion = primaryVersion;
    }

    public byte getSecondaryVersion() {
        return this.secondaryVersion;
    }

    public void setSecondaryVersion(byte secondaryVersion) {
        this.secondaryVersion = secondaryVersion;
    }

    public byte getReviseVersion() {
        return this.reviseVersion;
    }

    public void setReviseVersion(byte reviseVersion) {
        this.reviseVersion = reviseVersion;
    }

    public int getPileNameLength() {
        return this.pileNameLength;
    }

    public void setPileNameLength(int pileNameLength) {
        this.pileNameLength = pileNameLength;
    }

    public String getPileName() {
        return this.pileName;
    }

    public void setPileName(String pileName) {
        this.pileName = pileName;
    }

    public int getOperatorNameLength() {
        return this.operatorNameLength;
    }

    public void setOperatorNameLength(int operatorNameLength) {
        this.operatorNameLength = operatorNameLength;
    }

    public String getOperatorName() {
        return this.operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public int getHostIp() {
        return this.hostIp;
    }

    public void setHostIp(int hostIp) {
        this.hostIp = hostIp;
    }

    public int getHostPort() {
        return this.hostPort;
    }

    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public byte[] bodyToBytes() throws Exception {
        if (this.pileNameLength > 20 || this.operatorNameLength > 20) {
            Log.e("QuerySysInfoResponse.bodyToBytes", "illegal pile name length or operator name length in message: " + toJson());
            throw new InputMismatchException();
        }
        return null;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}