package org.apache.http.auth;

/* loaded from: classes.dex */
public enum AuthProtocolState {
    UNCHALLENGED,
    CHALLENGED,
    HANDSHAKE,
    FAILURE,
    SUCCESS;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static AuthProtocolState[] valuesCustom() {
        AuthProtocolState[] valuesCustom = values();
        int length = valuesCustom.length;
        AuthProtocolState[] authProtocolStateArr = new AuthProtocolState[length];
        System.arraycopy(valuesCustom, 0, authProtocolStateArr, 0, length);
        return authProtocolStateArr;
    }
}