package org.apache.mina.filter.codec.statemachine;

/* loaded from: classes.dex */
public abstract class ConsumeToLinearWhitespaceDecodingState extends ConsumeToDynamicTerminatorDecodingState {
    @Override // org.apache.mina.filter.codec.statemachine.ConsumeToDynamicTerminatorDecodingState
    protected boolean isTerminator(byte b) {
        return b == 32 || b == 9;
    }
}