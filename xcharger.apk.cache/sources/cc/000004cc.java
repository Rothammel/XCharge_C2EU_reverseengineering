package com.nostra13.universalimageloader.cache.memory;

import android.graphics.Bitmap;
import com.nostra13.universalimageloader.utils.L;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/* loaded from: classes.dex */
public abstract class LimitedMemoryCache extends BaseMemoryCache {
    private static final int MAX_NORMAL_CACHE_SIZE = 16777216;
    private static final int MAX_NORMAL_CACHE_SIZE_IN_MB = 16;
    private final int sizeLimit;
    private final List<Bitmap> hardCache = Collections.synchronizedList(new LinkedList());
    private final AtomicInteger cacheSize = new AtomicInteger();

    protected abstract int getSize(Bitmap bitmap);

    protected abstract Bitmap removeNext();

    public LimitedMemoryCache(int sizeLimit) {
        this.sizeLimit = sizeLimit;
        if (sizeLimit > 16777216) {
            L.w("You set too large memory cache size (more than %1$d Mb)", 16);
        }
    }

    @Override // com.nostra13.universalimageloader.cache.memory.BaseMemoryCache, com.nostra13.universalimageloader.cache.memory.MemoryCacheAware
    public boolean put(String key, Bitmap value) {
        boolean putSuccessfully = false;
        int valueSize = getSize(value);
        int sizeLimit = getSizeLimit();
        int curCacheSize = this.cacheSize.get();
        if (valueSize < sizeLimit) {
            while (curCacheSize + valueSize > sizeLimit) {
                Bitmap removedValue = removeNext();
                if (this.hardCache.remove(removedValue)) {
                    curCacheSize = this.cacheSize.addAndGet(-getSize(removedValue));
                }
            }
            this.hardCache.add(value);
            this.cacheSize.addAndGet(valueSize);
            putSuccessfully = true;
        }
        super.put(key, value);
        return putSuccessfully;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.nostra13.universalimageloader.cache.memory.BaseMemoryCache, com.nostra13.universalimageloader.cache.memory.MemoryCacheAware
    public Bitmap remove(String key) {
        Bitmap value = super.get(key);
        if (value != null && this.hardCache.remove(value)) {
            this.cacheSize.addAndGet(-getSize(value));
        }
        return super.remove(key);
    }

    @Override // com.nostra13.universalimageloader.cache.memory.BaseMemoryCache, com.nostra13.universalimageloader.cache.memory.MemoryCacheAware
    public void clear() {
        this.hardCache.clear();
        this.cacheSize.set(0);
        super.clear();
    }

    protected int getSizeLimit() {
        return this.sizeLimit;
    }
}