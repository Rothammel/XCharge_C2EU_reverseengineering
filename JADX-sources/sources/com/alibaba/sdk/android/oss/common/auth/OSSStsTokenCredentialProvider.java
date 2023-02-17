package com.alibaba.sdk.android.oss.common.auth;

/* loaded from: classes.dex */
public class OSSStsTokenCredentialProvider implements OSSCredentialProvider {
    private String accessKeyId;
    private String secretKeyId;
    private String securityToken;

    public OSSStsTokenCredentialProvider(String accessKeyId, String secretKeyId, String securityToken) {
        setAccessKeyId(accessKeyId.trim());
        setSecretKeyId(secretKeyId.trim());
        setSecurityToken(securityToken.trim());
    }

    public OSSStsTokenCredentialProvider(OSSFederationToken token) {
        setAccessKeyId(token.getTempAK().trim());
        setSecretKeyId(token.getTempSK().trim());
        setSecurityToken(token.getSecurityToken().trim());
    }

    public String getAccessKeyId() {
        return this.accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretKeyId() {
        return this.secretKeyId;
    }

    public void setSecretKeyId(String secretKeyId) {
        this.secretKeyId = secretKeyId;
    }

    public String getSecurityToken() {
        return this.securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    @Override // com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider
    public OSSFederationToken getFederationToken() {
        return new OSSFederationToken(this.accessKeyId, this.secretKeyId, this.securityToken, Long.MAX_VALUE);
    }
}
