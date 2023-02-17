package org.apache.mina.filter.codec.statemachine;

public abstract class LinearWhitespaceSkippingState extends SkippingState {
    /* access modifiers changed from: protected */
    public boolean canSkip(byte b) {
        return b == 32 || b == 9;
    }
}
