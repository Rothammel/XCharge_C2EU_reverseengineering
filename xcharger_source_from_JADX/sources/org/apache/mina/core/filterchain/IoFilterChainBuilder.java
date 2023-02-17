package org.apache.mina.core.filterchain;

public interface IoFilterChainBuilder {
    public static final IoFilterChainBuilder NOOP = new IoFilterChainBuilder() {
        public void buildFilterChain(IoFilterChain chain) throws Exception {
        }

        public String toString() {
            return "NOOP";
        }
    };

    void buildFilterChain(IoFilterChain ioFilterChain) throws Exception;
}
