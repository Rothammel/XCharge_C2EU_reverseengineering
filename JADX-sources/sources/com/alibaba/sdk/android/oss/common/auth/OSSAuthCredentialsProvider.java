package com.alibaba.sdk.android.oss.common.auth;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class OSSAuthCredentialsProvider extends OSSFederationCredentialProvider {
    private String mAuthServerUrl;
    private AuthDecoder mDecoder;

    /* loaded from: classes.dex */
    public interface AuthDecoder {
        String decode(String str);
    }

    public OSSAuthCredentialsProvider(String authServerUrl) {
        this.mAuthServerUrl = authServerUrl;
    }

    public void setAuthServerUrl(String authServerUrl) {
        this.mAuthServerUrl = authServerUrl;
    }

    public void setDecoder(AuthDecoder decoder) {
        this.mDecoder = decoder;
    }

    @Override // com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider, com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider
    public OSSFederationToken getFederationToken() throws ClientException {
        try {
            URL stsUrl = new URL(this.mAuthServerUrl);
            HttpURLConnection conn = (HttpURLConnection) stsUrl.openConnection();
            conn.setConnectTimeout(10000);
            InputStream input = conn.getInputStream();
            String authData = IOUtils.readStreamAsString(input, "utf-8");
            if (this.mDecoder != null) {
                authData = this.mDecoder.decode(authData);
            }
            JSONObject jsonObj = new JSONObject(authData);
            int statusCode = jsonObj.getInt("StatusCode");
            if (statusCode == 200) {
                String ak = jsonObj.getString("AccessKeyId");
                String sk = jsonObj.getString("AccessKeySecret");
                String token = jsonObj.getString("SecurityToken");
                String expiration = jsonObj.getString("Expiration");
                OSSFederationToken authToken = new OSSFederationToken(ak, sk, token, expiration);
                return authToken;
            }
            String errorCode = jsonObj.getString("ErrorCode");
            String errorMessage = jsonObj.getString("ErrorMessage");
            throw new ClientException("ErrorCode: " + errorCode + "| ErrorMessage: " + errorMessage);
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }
}
