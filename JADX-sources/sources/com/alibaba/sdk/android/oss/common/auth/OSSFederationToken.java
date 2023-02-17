package com.alibaba.sdk.android.oss.common.auth;

import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/* loaded from: classes.dex */
public class OSSFederationToken {
    private long expiration;
    private String securityToken;
    private String tempAk;
    private String tempSk;

    public OSSFederationToken(String tempAK, String tempSK, String securityToken, long expiration) {
        setTempAk(tempAK);
        setTempSk(tempSK);
        setSecurityToken(securityToken);
        setExpiration(expiration);
    }

    public OSSFederationToken(String tempAK, String tempSK, String securityToken, String expirationInGMTFormat) {
        setTempAk(tempAK);
        setTempSk(tempSK);
        setSecurityToken(securityToken);
        setExpirationInGMTFormat(expirationInGMTFormat);
    }

    public String toString() {
        return "OSSFederationToken [tempAk=" + this.tempAk + ", tempSk=" + this.tempSk + ", securityToken=" + this.securityToken + ", expiration=" + this.expiration + "]";
    }

    public String getTempAK() {
        return this.tempAk;
    }

    public String getTempSK() {
        return this.tempSk;
    }

    public String getSecurityToken() {
        return this.securityToken;
    }

    public void setTempAk(String tempAk) {
        this.tempAk = tempAk;
    }

    public void setTempSk(String tempSk) {
        this.tempSk = tempSk;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public long getExpiration() {
        return this.expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public void setExpirationInGMTFormat(String expirationInGMTFormat) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(expirationInGMTFormat);
            this.expiration = date.getTime() / 1000;
        } catch (ParseException e) {
            if (OSSLog.isEnableLog()) {
                e.printStackTrace();
            }
            this.expiration = (DateUtil.getFixedSkewedTimeMillis() / 1000) + 30;
        }
    }
}
