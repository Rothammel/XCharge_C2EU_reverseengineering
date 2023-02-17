package com.xcharge.charger.protocol.anyo.bean.response;

import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import java.util.InputMismatchException;

public class QuerySysInfoResponse extends AnyoMessage {
    private int hostIp = 0;
    private int hostPort = 0;
    private String operatorName = null;
    private int operatorNameLength = 0;
    private String pileName = null;
    private int pileNameLength = 0;
    private byte primaryVersion = 0;
    private byte reviseVersion = 0;
    private byte secondaryVersion = 0;

    public byte getPrimaryVersion() {
        return this.primaryVersion;
    }

    public void setPrimaryVersion(byte primaryVersion2) {
        this.primaryVersion = primaryVersion2;
    }

    public byte getSecondaryVersion() {
        return this.secondaryVersion;
    }

    public void setSecondaryVersion(byte secondaryVersion2) {
        this.secondaryVersion = secondaryVersion2;
    }

    public byte getReviseVersion() {
        return this.reviseVersion;
    }

    public void setReviseVersion(byte reviseVersion2) {
        this.reviseVersion = reviseVersion2;
    }

    public int getPileNameLength() {
        return this.pileNameLength;
    }

    public void setPileNameLength(int pileNameLength2) {
        this.pileNameLength = pileNameLength2;
    }

    public String getPileName() {
        return this.pileName;
    }

    public void setPileName(String pileName2) {
        this.pileName = pileName2;
    }

    public int getOperatorNameLength() {
        return this.operatorNameLength;
    }

    public void setOperatorNameLength(int operatorNameLength2) {
        this.operatorNameLength = operatorNameLength2;
    }

    public String getOperatorName() {
        return this.operatorName;
    }

    public void setOperatorName(String operatorName2) {
        this.operatorName = operatorName2;
    }

    public int getHostIp() {
        return this.hostIp;
    }

    public void setHostIp(int hostIp2) {
        this.hostIp = hostIp2;
    }

    public int getHostPort() {
        return this.hostPort;
    }

    public void setHostPort(int hostPort2) {
        this.hostPort = hostPort2;
    }

    public byte[] bodyToBytes() throws Exception {
        if (this.pileNameLength <= 20 && this.operatorNameLength <= 20) {
            return null;
        }
        Log.e("QuerySysInfoResponse.bodyToBytes", "illegal pile name length or operator name length in message: " + toJson());
        throw new InputMismatchException();
    }

    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
