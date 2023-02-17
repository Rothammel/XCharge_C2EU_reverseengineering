package org.apache.mina.core.filterchain;

/* loaded from: classes.dex */
public interface IoFilterChainBuilder {
    public static final IoFilterChainBuilder NOOP = new IoFilterChainBuilder() { // from class: org.apache.mina.core.filterchain.IoFilterChainBuilder.1
        @Override // org.apache.mina.core.filterchain.IoFilterChainBuilder
        public void buildFilterChain(IoFilterChain chain) throws Exception {
        }

        public String toString() {
            return "NOOP";
        }
    };

    void buildFilterChain(IoFilterChain ioFilterChain) throws Exception;
}