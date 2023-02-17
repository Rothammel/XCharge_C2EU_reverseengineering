package org.apache.http.auth;

/* loaded from: classes.dex */
public enum ChallengeState {
    TARGET,
    PROXY;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static ChallengeState[] valuesCustom() {
        ChallengeState[] valuesCustom = values();
        int length = valuesCustom.length;
        ChallengeState[] challengeStateArr = new ChallengeState[length];
        System.arraycopy(valuesCustom, 0, challengeStateArr, 0, length);
        return challengeStateArr;
    }
}