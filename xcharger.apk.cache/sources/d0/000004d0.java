package com.nostra13.universalimageloader.cache.memory.impl;

import android.graphics.Bitmap;
import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/* loaded from: classes.dex */
public class FuzzyKeyMemoryCache implements MemoryCache {
    private final MemoryCache cache;
    private final Comparator<String> keyComparator;

    public FuzzyKeyMemoryCache(MemoryCache cache, Comparator<String> keyComparator) {
        this.cache = cache;
        this.keyComparator = keyComparator;
    }

    @Override // com.nostra13.universalimageloader.cache.memory.MemoryCacheAware
    public boolean put(String key, Bitmap value) {
        synchronized (this.cache) {
            String keyToRemove = null;
            Iterator i$ = this.cache.keys().iterator();
            while (true) {
                if (!i$.hasNext()) {
                    break;
                }
                String cacheKey = i$.next();
                if (this.keyComparator.compare(key, cacheKey) == 0) {
                    keyToRemove = cacheKey;
                    break;
                }
            }
            if (keyToRemove != null) {
                this.cache.remove(keyToRemove);
            }
        }
        return this.cache.put(key, value);
    }

    @Override // com.nostra13.universalimageloader.cache.memory.MemoryCacheAware
    public Bitmap get(String key) {
        return this.cache.get(key);
    }

    @Override // com.nostra13.universalimageloader.cache.memory.MemoryCacheAware
    public Bitmap remove(String key) {
        return this.cache.remove(key);
    }

    @Override // com.nostra13.universalimageloader.cache.memory.MemoryCacheAware
    public void clear() {
        this.cache.clear();
    }

    @Override // com.nostra13.universalimageloader.cache.memory.MemoryCacheAware
    public Collection<String> keys() {
        return this.cache.keys();
    }
}