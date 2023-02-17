package org.apache.mina.filter.codec.statemachine;

public abstract class ConsumeToLinearWhitespaceDecodingState extends ConsumeToDynamicTerminatorDecodingState {
    /* access modifiers changed from: protected */
    public boolean isTerminator(byte b) {
        return b == 32 || b == 9;
    }
}
