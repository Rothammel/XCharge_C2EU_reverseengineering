package com.nostra13.universalimageloader.cache.memory.impl;

import android.graphics.Bitmap;
import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;

/* loaded from: classes.dex */
public class LruMemoryCache implements MemoryCache {
    private final LinkedHashMap<String, Bitmap> map;
    private final int maxSize;
    private int size;

    public LruMemoryCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.maxSize = maxSize;
        this.map = new LinkedHashMap<>(0, 0.75f, true);
    }

    @Override // com.nostra13.universalimageloader.cache.memory.MemoryCacheAware
    public final Bitmap get(String key) {
        Bitmap bitmap;
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        synchronized (this) {
            bitmap = this.map.get(key);
        }
        return bitmap;
    }

    @Override // com.nostra13.universalimageloader.cache.memory.MemoryCacheAware
    public final boolean put(String key, Bitmap value) {
        if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }
        synchronized (this) {
            this.size += sizeOf(key, value);
            Bitmap previous = this.map.put(key, value);
            if (previous != null) {
                this.size -= sizeOf(key, previous);
            }
        }
        trimToSize(this.maxSize);
        return true;
    }

    /* JADX WARN: Code restructure failed: missing block: B:10:0x0031, code lost:
        throw new java.lang.IllegalStateException(getClass().getName() + ".sizeOf() is reporting inconsistent results!");
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void trimToSize(int r7) {
        /*
            r6 = this;
        L0:
            monitor-enter(r6)
            int r3 = r6.size     // Catch: java.lang.Throwable -> L32
            if (r3 < 0) goto L11
            java.util.LinkedHashMap<java.lang.String, android.graphics.Bitmap> r3 = r6.map     // Catch: java.lang.Throwable -> L32
            boolean r3 = r3.isEmpty()     // Catch: java.lang.Throwable -> L32
            if (r3 == 0) goto L35
            int r3 = r6.size     // Catch: java.lang.Throwable -> L32
            if (r3 == 0) goto L35
        L11:
            java.lang.IllegalStateException r3 = new java.lang.IllegalStateException     // Catch: java.lang.Throwable -> L32
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> L32
            r4.<init>()     // Catch: java.lang.Throwable -> L32
            java.lang.Class r5 = r6.getClass()     // Catch: java.lang.Throwable -> L32
            java.lang.String r5 = r5.getName()     // Catch: java.lang.Throwable -> L32
            java.lang.StringBuilder r4 = r4.append(r5)     // Catch: java.lang.Throwable -> L32
            java.lang.String r5 = ".sizeOf() is reporting inconsistent results!"
            java.lang.StringBuilder r4 = r4.append(r5)     // Catch: java.lang.Throwable -> L32
            java.lang.String r4 = r4.toString()     // Catch: java.lang.Throwable -> L32
            r3.<init>(r4)     // Catch: java.lang.Throwable -> L32
            throw r3     // Catch: java.lang.Throwable -> L32
        L32:
            r3 = move-exception
            monitor-exit(r6)     // Catch: java.lang.Throwable -> L32
            throw r3
        L35:
            int r3 = r6.size     // Catch: java.lang.Throwable -> L32
            if (r3 <= r7) goto L41
            java.util.LinkedHashMap<java.lang.String, android.graphics.Bitmap> r3 = r6.map     // Catch: java.lang.Throwable -> L32
            boolean r3 = r3.isEmpty()     // Catch: java.lang.Throwable -> L32
            if (r3 == 0) goto L43
        L41:
            monitor-exit(r6)     // Catch: java.lang.Throwable -> L32
        L42:
            return
        L43:
            java.util.LinkedHashMap<java.lang.String, android.graphics.Bitmap> r3 = r6.map     // Catch: java.lang.Throwable -> L32
            java.util.Set r3 = r3.entrySet()     // Catch: java.lang.Throwable -> L32
            java.util.Iterator r3 = r3.iterator()     // Catch: java.lang.Throwable -> L32
            java.lang.Object r1 = r3.next()     // Catch: java.lang.Throwable -> L32
            java.util.Map$Entry r1 = (java.util.Map.Entry) r1     // Catch: java.lang.Throwable -> L32
            if (r1 != 0) goto L57
            monitor-exit(r6)     // Catch: java.lang.Throwable -> L32
            goto L42
        L57:
            java.lang.Object r0 = r1.getKey()     // Catch: java.lang.Throwable -> L32
            java.lang.String r0 = (java.lang.String) r0     // Catch: java.lang.Throwable -> L32
            java.lang.Object r2 = r1.getValue()     // Catch: java.lang.Throwable -> L32
            android.graphics.Bitmap r2 = (android.graphics.Bitmap) r2     // Catch: java.lang.Throwable -> L32
            java.util.LinkedHashMap<java.lang.String, android.graphics.Bitmap> r3 = r6.map     // Catch: java.lang.Throwable -> L32
            r3.remove(r0)     // Catch: java.lang.Throwable -> L32
            int r3 = r6.size     // Catch: java.lang.Throwable -> L32
            int r4 = r6.sizeOf(r0, r2)     // Catch: java.lang.Throwable -> L32
            int r3 = r3 - r4
            r6.size = r3     // Catch: java.lang.Throwable -> L32
            monitor-exit(r6)     // Catch: java.lang.Throwable -> L32
            goto L0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache.trimToSize(int):void");
    }

    @Override // com.nostra13.universalimageloader.cache.memory.MemoryCacheAware
    public final Bitmap remove(String key) {
        Bitmap previous;
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        synchronized (this) {
            previous = this.map.remove(key);
            if (previous != null) {
                this.size -= sizeOf(key, previous);
            }
        }
        return previous;
    }

    @Override // com.nostra13.universalimageloader.cache.memory.MemoryCacheAware
    public Collection<String> keys() {
        HashSet hashSet;
        synchronized (this) {
            hashSet = new HashSet(this.map.keySet());
        }
        return hashSet;
    }

    @Override // com.nostra13.universalimageloader.cache.memory.MemoryCacheAware
    public void clear() {
        trimToSize(-1);
    }

    private int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }

    public final synchronized String toString() {
        return String.format("LruCache[maxSize=%d]", Integer.valueOf(this.maxSize));
    }
}
