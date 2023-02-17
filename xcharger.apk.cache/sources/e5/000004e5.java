package com.nostra13.universalimageloader.core;

import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class ImageLoaderEngine {
    final ImageLoaderConfiguration configuration;
    private Executor taskExecutor;
    private Executor taskExecutorForCachedImages;
    private final Map<Integer, String> cacheKeysForImageAwares = Collections.synchronizedMap(new HashMap());
    private final Map<String, ReentrantLock> uriLocks = new WeakHashMap();
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicBoolean networkDenied = new AtomicBoolean(false);
    private final AtomicBoolean slowNetwork = new AtomicBoolean(false);
    private final Object pauseLock = new Object();
    private Executor taskDistributor = DefaultConfigurationFactory.createTaskDistributor();

    /* JADX INFO: Access modifiers changed from: package-private */
    public ImageLoaderEngine(ImageLoaderConfiguration configuration) {
        this.configuration = configuration;
        this.taskExecutor = configuration.taskExecutor;
        this.taskExecutorForCachedImages = configuration.taskExecutorForCachedImages;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void submit(final LoadAndDisplayImageTask task) {
        this.taskDistributor.execute(new Runnable() { // from class: com.nostra13.universalimageloader.core.ImageLoaderEngine.1
            @Override // java.lang.Runnable
            public void run() {
                File image = ImageLoaderEngine.this.configuration.diskCache.get(task.getLoadingUri());
                boolean isImageCachedOnDisk = image != null && image.exists();
                ImageLoaderEngine.this.initExecutorsIfNeed();
                if (isImageCachedOnDisk) {
                    ImageLoaderEngine.this.taskExecutorForCachedImages.execute(task);
                } else {
                    ImageLoaderEngine.this.taskExecutor.execute(task);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void submit(ProcessAndDisplayImageTask task) {
        initExecutorsIfNeed();
        this.taskExecutorForCachedImages.execute(task);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initExecutorsIfNeed() {
        if (!this.configuration.customExecutor && ((ExecutorService) this.taskExecutor).isShutdown()) {
            this.taskExecutor = createTaskExecutor();
        }
        if (!this.configuration.customExecutorForCachedImages && ((ExecutorService) this.taskExecutorForCachedImages).isShutdown()) {
            this.taskExecutorForCachedImages = createTaskExecutor();
        }
    }

    private Executor createTaskExecutor() {
        return DefaultConfigurationFactory.createExecutor(this.configuration.threadPoolSize, this.configuration.threadPriority, this.configuration.tasksProcessingType);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public String getLoadingUriForView(ImageAware imageAware) {
        return this.cacheKeysForImageAwares.get(Integer.valueOf(imageAware.getId()));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void prepareDisplayTaskFor(ImageAware imageAware, String memoryCacheKey) {
        this.cacheKeysForImageAwares.put(Integer.valueOf(imageAware.getId()), memoryCacheKey);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void cancelDisplayTaskFor(ImageAware imageAware) {
        this.cacheKeysForImageAwares.remove(Integer.valueOf(imageAware.getId()));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void denyNetworkDownloads(boolean denyNetworkDownloads) {
        this.networkDenied.set(denyNetworkDownloads);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void handleSlowNetwork(boolean handleSlowNetwork) {
        this.slowNetwork.set(handleSlowNetwork);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void pause() {
        this.paused.set(true);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void resume() {
        this.paused.set(false);
        synchronized (this.pauseLock) {
            this.pauseLock.notifyAll();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void stop() {
        if (!this.configuration.customExecutor) {
            ((ExecutorService) this.taskExecutor).shutdownNow();
        }
        if (!this.configuration.customExecutorForCachedImages) {
            ((ExecutorService) this.taskExecutorForCachedImages).shutdownNow();
        }
        this.cacheKeysForImageAwares.clear();
        this.uriLocks.clear();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void fireCallback(Runnable r) {
        this.taskDistributor.execute(r);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ReentrantLock getLockForUri(String uri) {
        ReentrantLock lock = this.uriLocks.get(uri);
        if (lock == null) {
            ReentrantLock lock2 = new ReentrantLock();
            this.uriLocks.put(uri, lock2);
            return lock2;
        }
        return lock;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public AtomicBoolean getPause() {
        return this.paused;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Object getPauseLock() {
        return this.pauseLock;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isNetworkDenied() {
        return this.networkDenied.get();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isSlowNetwork() {
        return this.slowNetwork.get();
    }
}