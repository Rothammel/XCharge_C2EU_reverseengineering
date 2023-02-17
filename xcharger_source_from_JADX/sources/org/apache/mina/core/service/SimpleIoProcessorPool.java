package org.apache.mina.core.service;

import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.write.WriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleIoProcessorPool<S extends AbstractIoSession> implements IoProcessor<S> {
    private static final int DEFAULT_SIZE = (Runtime.getRuntime().availableProcessors() + 1);
    private static final Logger LOGGER = LoggerFactory.getLogger((Class<?>) SimpleIoProcessorPool.class);
    private static final AttributeKey PROCESSOR = new AttributeKey(SimpleIoProcessorPool.class, "processor");
    private final boolean createdExecutor;
    private final Object disposalLock;
    private volatile boolean disposed;
    private volatile boolean disposing;
    private final Executor executor;
    private final IoProcessor<S>[] pool;

    public SimpleIoProcessorPool(Class<? extends IoProcessor<S>> processorType) {
        this(processorType, (Executor) null, DEFAULT_SIZE, (SelectorProvider) null);
    }

    public SimpleIoProcessorPool(Class<? extends IoProcessor<S>> processorType, int size) {
        this(processorType, (Executor) null, size, (SelectorProvider) null);
    }

    public SimpleIoProcessorPool(Class<? extends IoProcessor<S>> processorType, int size, SelectorProvider selectorProvider) {
        this(processorType, (Executor) null, size, selectorProvider);
    }

    public SimpleIoProcessorPool(Class<? extends IoProcessor<S>> processorType, Executor executor2) {
        this(processorType, executor2, DEFAULT_SIZE, (SelectorProvider) null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00c0, code lost:
        r10 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00c1, code lost:
        if (0 == 0) goto L_0x00c3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00c3, code lost:
        dispose();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00c6, code lost:
        throw r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        r6 = r16.getConstructor(new java.lang.Class[0]);
        r9 = false;
        r15.pool[0] = (org.apache.mina.core.service.IoProcessor) r6.newInstance(new java.lang.Object[0]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x013d, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        LOGGER.error("Cannot create an IoProcessor :{}", (java.lang.Object) r7.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0149, code lost:
        throw r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x014a, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x014b, code lost:
        r5 = "Failed to create a new instance of " + r16.getName() + ":" + r1.getMessage();
        LOGGER.error(r5, (java.lang.Throwable) r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x017a, code lost:
        throw new org.apache.mina.core.RuntimeIoException(r5, r1);
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x013d A[ExcHandler: RuntimeException (r7v0 're' java.lang.RuntimeException A[CUSTOM_DECLARE]), Splitter:B:13:0x005c] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x014a A[Catch:{ NoSuchMethodException -> 0x010f, RuntimeException -> 0x013d, Exception -> 0x014a, all -> 0x00c0 }, ExcHandler: Exception (r1v0 'e' java.lang.Exception A[CUSTOM_DECLARE, Catch:{  }]), Splitter:B:13:0x005c] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SimpleIoProcessorPool(java.lang.Class<? extends org.apache.mina.core.service.IoProcessor<S>> r16, java.util.concurrent.Executor r17, int r18, java.nio.channels.spi.SelectorProvider r19) {
        /*
            r15 = this;
            r15.<init>()
            java.lang.Object r10 = new java.lang.Object
            r10.<init>()
            r15.disposalLock = r10
            if (r16 != 0) goto L_0x0014
            java.lang.IllegalArgumentException r10 = new java.lang.IllegalArgumentException
            java.lang.String r11 = "processorType"
            r10.<init>(r11)
            throw r10
        L_0x0014:
            if (r18 > 0) goto L_0x0037
            java.lang.IllegalArgumentException r10 = new java.lang.IllegalArgumentException
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r12 = "size: "
            java.lang.StringBuilder r11 = r11.append(r12)
            r0 = r18
            java.lang.StringBuilder r11 = r11.append(r0)
            java.lang.String r12 = " (expected: positive integer)"
            java.lang.StringBuilder r11 = r11.append(r12)
            java.lang.String r11 = r11.toString()
            r10.<init>(r11)
            throw r10
        L_0x0037:
            if (r17 != 0) goto L_0x00c7
            r10 = 1
        L_0x003a:
            r15.createdExecutor = r10
            boolean r10 = r15.createdExecutor
            if (r10 == 0) goto L_0x00ca
            java.util.concurrent.ExecutorService r10 = java.util.concurrent.Executors.newCachedThreadPool()
            r15.executor = r10
            java.util.concurrent.Executor r10 = r15.executor
            java.util.concurrent.ThreadPoolExecutor r10 = (java.util.concurrent.ThreadPoolExecutor) r10
            java.util.concurrent.ThreadPoolExecutor$CallerRunsPolicy r11 = new java.util.concurrent.ThreadPoolExecutor$CallerRunsPolicy
            r11.<init>()
            r10.setRejectedExecutionHandler(r11)
        L_0x0052:
            r0 = r18
            org.apache.mina.core.service.IoProcessor[] r10 = new org.apache.mina.core.service.IoProcessor[r0]
            r15.pool = r10
            r8 = 0
            r6 = 0
            r9 = 1
            r10 = 1
            java.lang.Class[] r10 = new java.lang.Class[r10]     // Catch:{ NoSuchMethodException -> 0x00cf, RuntimeException -> 0x013d, Exception -> 0x014a }
            r11 = 0
            java.lang.Class<java.util.concurrent.ExecutorService> r12 = java.util.concurrent.ExecutorService.class
            r10[r11] = r12     // Catch:{ NoSuchMethodException -> 0x00cf, RuntimeException -> 0x013d, Exception -> 0x014a }
            r0 = r16
            java.lang.reflect.Constructor r6 = r0.getConstructor(r10)     // Catch:{ NoSuchMethodException -> 0x00cf, RuntimeException -> 0x013d, Exception -> 0x014a }
            org.apache.mina.core.service.IoProcessor<S>[] r11 = r15.pool     // Catch:{ NoSuchMethodException -> 0x00cf, RuntimeException -> 0x013d, Exception -> 0x014a }
            r12 = 0
            r10 = 1
            java.lang.Object[] r10 = new java.lang.Object[r10]     // Catch:{ NoSuchMethodException -> 0x00cf, RuntimeException -> 0x013d, Exception -> 0x014a }
            r13 = 0
            java.util.concurrent.Executor r14 = r15.executor     // Catch:{ NoSuchMethodException -> 0x00cf, RuntimeException -> 0x013d, Exception -> 0x014a }
            r10[r13] = r14     // Catch:{ NoSuchMethodException -> 0x00cf, RuntimeException -> 0x013d, Exception -> 0x014a }
            java.lang.Object r10 = r6.newInstance(r10)     // Catch:{ NoSuchMethodException -> 0x00cf, RuntimeException -> 0x013d, Exception -> 0x014a }
            org.apache.mina.core.service.IoProcessor r10 = (org.apache.mina.core.service.IoProcessor) r10     // Catch:{ NoSuchMethodException -> 0x00cf, RuntimeException -> 0x013d, Exception -> 0x014a }
            r11[r12] = r10     // Catch:{ NoSuchMethodException -> 0x00cf, RuntimeException -> 0x013d, Exception -> 0x014a }
        L_0x007c:
            if (r6 != 0) goto L_0x017b
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ all -> 0x00c0 }
            r10.<init>()     // Catch:{ all -> 0x00c0 }
            java.lang.String r11 = java.lang.String.valueOf(r16)     // Catch:{ all -> 0x00c0 }
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ all -> 0x00c0 }
            java.lang.String r11 = " must have a public constructor with one "
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ all -> 0x00c0 }
            java.lang.Class<java.util.concurrent.ExecutorService> r11 = java.util.concurrent.ExecutorService.class
            java.lang.String r11 = r11.getSimpleName()     // Catch:{ all -> 0x00c0 }
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ all -> 0x00c0 }
            java.lang.String r11 = " parameter, a public constructor with one "
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ all -> 0x00c0 }
            java.lang.Class<java.util.concurrent.Executor> r11 = java.util.concurrent.Executor.class
            java.lang.String r11 = r11.getSimpleName()     // Catch:{ all -> 0x00c0 }
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ all -> 0x00c0 }
            java.lang.String r11 = " parameter or a public default constructor."
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ all -> 0x00c0 }
            java.lang.String r5 = r10.toString()     // Catch:{ all -> 0x00c0 }
            org.slf4j.Logger r10 = LOGGER     // Catch:{ all -> 0x00c0 }
            r10.error(r5)     // Catch:{ all -> 0x00c0 }
            java.lang.IllegalArgumentException r10 = new java.lang.IllegalArgumentException     // Catch:{ all -> 0x00c0 }
            r10.<init>(r5)     // Catch:{ all -> 0x00c0 }
            throw r10     // Catch:{ all -> 0x00c0 }
        L_0x00c0:
            r10 = move-exception
            if (r8 != 0) goto L_0x00c6
            r15.dispose()
        L_0x00c6:
            throw r10
        L_0x00c7:
            r10 = 0
            goto L_0x003a
        L_0x00ca:
            r0 = r17
            r15.executor = r0
            goto L_0x0052
        L_0x00cf:
            r2 = move-exception
            if (r19 != 0) goto L_0x0112
            r10 = 1
            java.lang.Class[] r10 = new java.lang.Class[r10]     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            r11 = 0
            java.lang.Class<java.util.concurrent.Executor> r12 = java.util.concurrent.Executor.class
            r10[r11] = r12     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            r0 = r16
            java.lang.reflect.Constructor r6 = r0.getConstructor(r10)     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            org.apache.mina.core.service.IoProcessor<S>[] r11 = r15.pool     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            r12 = 0
            r10 = 1
            java.lang.Object[] r10 = new java.lang.Object[r10]     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            r13 = 0
            java.util.concurrent.Executor r14 = r15.executor     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            r10[r13] = r14     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            java.lang.Object r10 = r6.newInstance(r10)     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            org.apache.mina.core.service.IoProcessor r10 = (org.apache.mina.core.service.IoProcessor) r10     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            r11[r12] = r10     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            goto L_0x007c
        L_0x00f4:
            r3 = move-exception
            r10 = 0
            java.lang.Class[] r10 = new java.lang.Class[r10]     // Catch:{ NoSuchMethodException -> 0x010f, RuntimeException -> 0x013d, Exception -> 0x014a }
            r0 = r16
            java.lang.reflect.Constructor r6 = r0.getConstructor(r10)     // Catch:{ NoSuchMethodException -> 0x010f, RuntimeException -> 0x013d, Exception -> 0x014a }
            r9 = 0
            org.apache.mina.core.service.IoProcessor<S>[] r11 = r15.pool     // Catch:{ NoSuchMethodException -> 0x010f, RuntimeException -> 0x013d, Exception -> 0x014a }
            r12 = 0
            r10 = 0
            java.lang.Object[] r10 = new java.lang.Object[r10]     // Catch:{ NoSuchMethodException -> 0x010f, RuntimeException -> 0x013d, Exception -> 0x014a }
            java.lang.Object r10 = r6.newInstance(r10)     // Catch:{ NoSuchMethodException -> 0x010f, RuntimeException -> 0x013d, Exception -> 0x014a }
            org.apache.mina.core.service.IoProcessor r10 = (org.apache.mina.core.service.IoProcessor) r10     // Catch:{ NoSuchMethodException -> 0x010f, RuntimeException -> 0x013d, Exception -> 0x014a }
            r11[r12] = r10     // Catch:{ NoSuchMethodException -> 0x010f, RuntimeException -> 0x013d, Exception -> 0x014a }
            goto L_0x007c
        L_0x010f:
            r10 = move-exception
            goto L_0x007c
        L_0x0112:
            r10 = 2
            java.lang.Class[] r10 = new java.lang.Class[r10]     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            r11 = 0
            java.lang.Class<java.util.concurrent.Executor> r12 = java.util.concurrent.Executor.class
            r10[r11] = r12     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            r11 = 1
            java.lang.Class<java.nio.channels.spi.SelectorProvider> r12 = java.nio.channels.spi.SelectorProvider.class
            r10[r11] = r12     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            r0 = r16
            java.lang.reflect.Constructor r6 = r0.getConstructor(r10)     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            org.apache.mina.core.service.IoProcessor<S>[] r11 = r15.pool     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            r12 = 0
            r10 = 2
            java.lang.Object[] r10 = new java.lang.Object[r10]     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            r13 = 0
            java.util.concurrent.Executor r14 = r15.executor     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            r10[r13] = r14     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            r13 = 1
            r10[r13] = r19     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            java.lang.Object r10 = r6.newInstance(r10)     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            org.apache.mina.core.service.IoProcessor r10 = (org.apache.mina.core.service.IoProcessor) r10     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            r11[r12] = r10     // Catch:{ NoSuchMethodException -> 0x00f4, RuntimeException -> 0x013d, Exception -> 0x014a }
            goto L_0x007c
        L_0x013d:
            r7 = move-exception
            org.slf4j.Logger r10 = LOGGER     // Catch:{ all -> 0x00c0 }
            java.lang.String r11 = "Cannot create an IoProcessor :{}"
            java.lang.String r12 = r7.getMessage()     // Catch:{ all -> 0x00c0 }
            r10.error((java.lang.String) r11, (java.lang.Object) r12)     // Catch:{ all -> 0x00c0 }
            throw r7     // Catch:{ all -> 0x00c0 }
        L_0x014a:
            r1 = move-exception
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ all -> 0x00c0 }
            r10.<init>()     // Catch:{ all -> 0x00c0 }
            java.lang.String r11 = "Failed to create a new instance of "
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ all -> 0x00c0 }
            java.lang.String r11 = r16.getName()     // Catch:{ all -> 0x00c0 }
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ all -> 0x00c0 }
            java.lang.String r11 = ":"
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ all -> 0x00c0 }
            java.lang.String r11 = r1.getMessage()     // Catch:{ all -> 0x00c0 }
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ all -> 0x00c0 }
            java.lang.String r5 = r10.toString()     // Catch:{ all -> 0x00c0 }
            org.slf4j.Logger r10 = LOGGER     // Catch:{ all -> 0x00c0 }
            r10.error((java.lang.String) r5, (java.lang.Throwable) r1)     // Catch:{ all -> 0x00c0 }
            org.apache.mina.core.RuntimeIoException r10 = new org.apache.mina.core.RuntimeIoException     // Catch:{ all -> 0x00c0 }
            r10.<init>(r5, r1)     // Catch:{ all -> 0x00c0 }
            throw r10     // Catch:{ all -> 0x00c0 }
        L_0x017b:
            r4 = 1
        L_0x017c:
            org.apache.mina.core.service.IoProcessor<S>[] r10 = r15.pool     // Catch:{ all -> 0x00c0 }
            int r10 = r10.length     // Catch:{ all -> 0x00c0 }
            if (r4 >= r10) goto L_0x01c0
            if (r9 == 0) goto L_0x01b2
            if (r19 != 0) goto L_0x019a
            org.apache.mina.core.service.IoProcessor<S>[] r11 = r15.pool     // Catch:{ Exception -> 0x01b0 }
            r10 = 1
            java.lang.Object[] r10 = new java.lang.Object[r10]     // Catch:{ Exception -> 0x01b0 }
            r12 = 0
            java.util.concurrent.Executor r13 = r15.executor     // Catch:{ Exception -> 0x01b0 }
            r10[r12] = r13     // Catch:{ Exception -> 0x01b0 }
            java.lang.Object r10 = r6.newInstance(r10)     // Catch:{ Exception -> 0x01b0 }
            org.apache.mina.core.service.IoProcessor r10 = (org.apache.mina.core.service.IoProcessor) r10     // Catch:{ Exception -> 0x01b0 }
            r11[r4] = r10     // Catch:{ Exception -> 0x01b0 }
        L_0x0197:
            int r4 = r4 + 1
            goto L_0x017c
        L_0x019a:
            org.apache.mina.core.service.IoProcessor<S>[] r11 = r15.pool     // Catch:{ Exception -> 0x01b0 }
            r10 = 2
            java.lang.Object[] r10 = new java.lang.Object[r10]     // Catch:{ Exception -> 0x01b0 }
            r12 = 0
            java.util.concurrent.Executor r13 = r15.executor     // Catch:{ Exception -> 0x01b0 }
            r10[r12] = r13     // Catch:{ Exception -> 0x01b0 }
            r12 = 1
            r10[r12] = r19     // Catch:{ Exception -> 0x01b0 }
            java.lang.Object r10 = r6.newInstance(r10)     // Catch:{ Exception -> 0x01b0 }
            org.apache.mina.core.service.IoProcessor r10 = (org.apache.mina.core.service.IoProcessor) r10     // Catch:{ Exception -> 0x01b0 }
            r11[r4] = r10     // Catch:{ Exception -> 0x01b0 }
            goto L_0x0197
        L_0x01b0:
            r10 = move-exception
            goto L_0x0197
        L_0x01b2:
            org.apache.mina.core.service.IoProcessor<S>[] r11 = r15.pool     // Catch:{ Exception -> 0x01b0 }
            r10 = 0
            java.lang.Object[] r10 = new java.lang.Object[r10]     // Catch:{ Exception -> 0x01b0 }
            java.lang.Object r10 = r6.newInstance(r10)     // Catch:{ Exception -> 0x01b0 }
            org.apache.mina.core.service.IoProcessor r10 = (org.apache.mina.core.service.IoProcessor) r10     // Catch:{ Exception -> 0x01b0 }
            r11[r4] = r10     // Catch:{ Exception -> 0x01b0 }
            goto L_0x0197
        L_0x01c0:
            r8 = 1
            if (r8 != 0) goto L_0x01c6
            r15.dispose()
        L_0x01c6:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.core.service.SimpleIoProcessorPool.<init>(java.lang.Class, java.util.concurrent.Executor, int, java.nio.channels.spi.SelectorProvider):void");
    }

    public final void add(S session) {
        getProcessor(session).add(session);
    }

    public final void flush(S session) {
        getProcessor(session).flush(session);
    }

    public final void write(S session, WriteRequest writeRequest) {
        getProcessor(session).write(session, writeRequest);
    }

    public final void remove(S session) {
        getProcessor(session).remove(session);
    }

    public final void updateTrafficControl(S session) {
        getProcessor(session).updateTrafficControl(session);
    }

    public boolean isDisposed() {
        return this.disposed;
    }

    public boolean isDisposing() {
        return this.disposing;
    }

    public final void dispose() {
        if (!this.disposed) {
            synchronized (this.disposalLock) {
                if (!this.disposing) {
                    this.disposing = true;
                    for (IoProcessor<S> ioProcessor : this.pool) {
                        if (ioProcessor != null && !ioProcessor.isDisposing()) {
                            try {
                                ioProcessor.dispose();
                            } catch (Exception e) {
                                LOGGER.warn("Failed to dispose the {} IoProcessor.", (Object) ioProcessor.getClass().getSimpleName(), (Object) e);
                            }
                        }
                    }
                    if (this.createdExecutor) {
                        ((ExecutorService) this.executor).shutdown();
                    }
                }
                Arrays.fill(this.pool, (Object) null);
                this.disposed = true;
            }
        }
    }

    private IoProcessor<S> getProcessor(S session) {
        IoProcessor<S> processor = (IoProcessor) session.getAttribute(PROCESSOR);
        if (processor == null) {
            if (this.disposed || this.disposing) {
                throw new IllegalStateException("A disposed processor cannot be accessed.");
            }
            processor = this.pool[Math.abs((int) session.getId()) % this.pool.length];
            if (processor == null) {
                throw new IllegalStateException("A disposed processor cannot be accessed.");
            }
            session.setAttributeIfAbsent(PROCESSOR, processor);
        }
        return processor;
    }
}
