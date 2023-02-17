package org.apache.mina.core.service;

import java.lang.reflect.Constructor;
import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class SimpleIoProcessorPool<S extends AbstractIoSession> implements IoProcessor<S> {
    private final boolean createdExecutor;
    private final Object disposalLock;
    private volatile boolean disposed;
    private volatile boolean disposing;
    private final Executor executor;
    private final IoProcessor<S>[] pool;
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleIoProcessorPool.class);
    private static final int DEFAULT_SIZE = Runtime.getRuntime().availableProcessors() + 1;
    private static final AttributeKey PROCESSOR = new AttributeKey(SimpleIoProcessorPool.class, "processor");

    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.apache.mina.core.service.IoProcessor
    public /* bridge */ /* synthetic */ void add(IoSession ioSession) {
        add((SimpleIoProcessorPool<S>) ((AbstractIoSession) ioSession));
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.apache.mina.core.service.IoProcessor
    public /* bridge */ /* synthetic */ void flush(IoSession ioSession) {
        flush((SimpleIoProcessorPool<S>) ((AbstractIoSession) ioSession));
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.apache.mina.core.service.IoProcessor
    public /* bridge */ /* synthetic */ void remove(IoSession ioSession) {
        remove((SimpleIoProcessorPool<S>) ((AbstractIoSession) ioSession));
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.apache.mina.core.service.IoProcessor
    public /* bridge */ /* synthetic */ void updateTrafficControl(IoSession ioSession) {
        updateTrafficControl((SimpleIoProcessorPool<S>) ((AbstractIoSession) ioSession));
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.apache.mina.core.service.IoProcessor
    public /* bridge */ /* synthetic */ void write(IoSession ioSession, WriteRequest writeRequest) {
        write((SimpleIoProcessorPool<S>) ((AbstractIoSession) ioSession), writeRequest);
    }

    public SimpleIoProcessorPool(Class<? extends IoProcessor<S>> processorType) {
        this(processorType, null, DEFAULT_SIZE, null);
    }

    public SimpleIoProcessorPool(Class<? extends IoProcessor<S>> processorType, int size) {
        this(processorType, null, size, null);
    }

    public SimpleIoProcessorPool(Class<? extends IoProcessor<S>> processorType, int size, SelectorProvider selectorProvider) {
        this(processorType, null, size, selectorProvider);
    }

    public SimpleIoProcessorPool(Class<? extends IoProcessor<S>> processorType, Executor executor) {
        this(processorType, executor, DEFAULT_SIZE, null);
    }

    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:32:0x00f6 -> B:66:0x007c). Please submit an issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:54:0x01b1 -> B:51:0x0197). Please submit an issue!!! */
    public SimpleIoProcessorPool(Class<? extends IoProcessor<S>> processorType, Executor executor, int size, SelectorProvider selectorProvider) {
        this.disposalLock = new Object();
        if (processorType == null) {
            throw new IllegalArgumentException("processorType");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size: " + size + " (expected: positive integer)");
        }
        this.createdExecutor = executor == null;
        if (this.createdExecutor) {
            this.executor = Executors.newCachedThreadPool();
            ((ThreadPoolExecutor) this.executor).setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        } else {
            this.executor = executor;
        }
        this.pool = new IoProcessor[size];
        boolean success = false;
        Constructor<? extends IoProcessor<S>> processorConstructor = null;
        boolean usesExecutorArg = true;
        try {
            try {
                try {
                    try {
                        processorConstructor = processorType.getConstructor(ExecutorService.class);
                        this.pool[0] = processorConstructor.newInstance(this.executor);
                    } catch (RuntimeException re) {
                        LOGGER.error("Cannot create an IoProcessor :{}", re.getMessage());
                        throw re;
                    }
                } catch (NoSuchMethodException e) {
                    try {
                        if (selectorProvider == null) {
                            processorConstructor = processorType.getConstructor(Executor.class);
                            this.pool[0] = processorConstructor.newInstance(this.executor);
                        } else {
                            processorConstructor = processorType.getConstructor(Executor.class, SelectorProvider.class);
                            this.pool[0] = processorConstructor.newInstance(this.executor, selectorProvider);
                        }
                    } catch (NoSuchMethodException e2) {
                        try {
                            processorConstructor = processorType.getConstructor(new Class[0]);
                            usesExecutorArg = false;
                            this.pool[0] = processorConstructor.newInstance(new Object[0]);
                        } catch (NoSuchMethodException e3) {
                        }
                    }
                }
                if (processorConstructor == null) {
                    String msg = String.valueOf(processorType) + " must have a public constructor with one " + ExecutorService.class.getSimpleName() + " parameter, a public constructor with one " + Executor.class.getSimpleName() + " parameter or a public default constructor.";
                    LOGGER.error(msg);
                    throw new IllegalArgumentException(msg);
                }
                int i = 1;
                while (i < this.pool.length) {
                    if (usesExecutorArg) {
                        if (selectorProvider == null) {
                            try {
                                this.pool[i] = processorConstructor.newInstance(this.executor);
                            } catch (Exception e4) {
                            }
                        } else {
                            this.pool[i] = processorConstructor.newInstance(this.executor, selectorProvider);
                        }
                    } else {
                        this.pool[i] = processorConstructor.newInstance(new Object[0]);
                    }
                    i++;
                }
                success = true;
            } finally {
                if (!success) {
                    dispose();
                }
            }
        } catch (Exception e5) {
            String msg2 = "Failed to create a new instance of " + processorType.getName() + ":" + e5.getMessage();
            LOGGER.error(msg2, (Throwable) e5);
            throw new RuntimeIoException(msg2, e5);
        }
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

    @Override // org.apache.mina.core.service.IoProcessor
    public boolean isDisposed() {
        return this.disposed;
    }

    @Override // org.apache.mina.core.service.IoProcessor
    public boolean isDisposing() {
        return this.disposing;
    }

    @Override // org.apache.mina.core.service.IoProcessor
    public final void dispose() {
        IoProcessor<S>[] ioProcessorArr;
        if (!this.disposed) {
            synchronized (this.disposalLock) {
                if (!this.disposing) {
                    this.disposing = true;
                    for (IoProcessor<S> ioProcessor : this.pool) {
                        if (ioProcessor != null && !ioProcessor.isDisposing()) {
                            try {
                                ioProcessor.dispose();
                            } catch (Exception e) {
                                LOGGER.warn("Failed to dispose the {} IoProcessor.", ioProcessor.getClass().getSimpleName(), e);
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
