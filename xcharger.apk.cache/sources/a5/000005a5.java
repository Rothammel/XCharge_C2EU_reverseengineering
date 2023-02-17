package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum AMP_DISTR_POLICY {
    auto(0),
    first_prior(1),
    internal_prior(2);
    
    private int policy;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static AMP_DISTR_POLICY[] valuesCustom() {
        AMP_DISTR_POLICY[] valuesCustom = values();
        int length = valuesCustom.length;
        AMP_DISTR_POLICY[] amp_distr_policyArr = new AMP_DISTR_POLICY[length];
        System.arraycopy(valuesCustom, 0, amp_distr_policyArr, 0, length);
        return amp_distr_policyArr;
    }

    AMP_DISTR_POLICY(int policy) {
        this.policy = 0;
        this.policy = policy;
    }

    public static AMP_DISTR_POLICY valueBy(int policy) {
        switch (policy) {
            case 0:
                return auto;
            case 1:
                return first_prior;
            case 2:
                return internal_prior;
            default:
                return auto;
        }
    }

    public int getPolicy() {
        return this.policy;
    }
}