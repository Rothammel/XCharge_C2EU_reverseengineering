package com.alibaba.sdk.android.oss.common.auth;

/* loaded from: classes.dex */
public abstract class OSSCustomSignerCredentialProvider implements OSSCredentialProvider {
    public abstract String signContent(String str);

    @Override // com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider
    public OSSFederationToken getFederationToken() {
        return null;
    }
}
