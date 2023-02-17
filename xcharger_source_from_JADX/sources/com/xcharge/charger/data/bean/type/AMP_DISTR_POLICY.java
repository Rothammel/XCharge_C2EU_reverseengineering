package com.xcharge.charger.data.bean.type;

public enum AMP_DISTR_POLICY {
    auto(0),
    first_prior(1),
    internal_prior(2);
    
    private int policy;

    private AMP_DISTR_POLICY(int policy2) {
        this.policy = 0;
        this.policy = policy2;
    }

    public static AMP_DISTR_POLICY valueBy(int policy2) {
        switch (policy2) {
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
