package org.apache.mina.filter.codec.statemachine;

/* loaded from: classes.dex */
public abstract class LinearWhitespaceSkippingState extends SkippingState {
    @Override // org.apache.mina.filter.codec.statemachine.SkippingState
    protected boolean canSkip(byte b) {
        return b == 32 || b == 9;
    }
}
