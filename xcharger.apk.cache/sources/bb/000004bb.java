package com.nostra13.universalimageloader.cache.disc.impl;

import android.graphics.Bitmap;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DefaultConfigurationFactory;
import com.nostra13.universalimageloader.utils.IoUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public class LimitedAgeDiscCache extends BaseDiscCache {
    private final Map<File, Long> loadingDates;
    private final long maxFileAge;

    public LimitedAgeDiscCache(File cacheDir, long maxAge) {
        this(cacheDir, null, DefaultConfigurationFactory.createFileNameGenerator(), maxAge);
    }

    public LimitedAgeDiscCache(File cacheDir, File reserveCacheDir, long maxAge) {
        this(cacheDir, reserveCacheDir, DefaultConfigurationFactory.createFileNameGenerator(), maxAge);
    }

    public LimitedAgeDiscCache(File cacheDir, File reserveCacheDir, FileNameGenerator fileNameGenerator, long maxAge) {
        super(cacheDir, reserveCacheDir, fileNameGenerator);
        this.loadingDates = Collections.synchronizedMap(new HashMap());
        this.maxFileAge = 1000 * maxAge;
    }

    @Override // com.nostra13.universalimageloader.cache.disc.impl.BaseDiscCache, com.nostra13.universalimageloader.cache.disc.DiscCacheAware
    public File get(String imageUri) {
        boolean cached;
        File file = super.get(imageUri);
        if (file != null && file.exists()) {
            Long loadingDate = this.loadingDates.get(file);
            if (loadingDate == null) {
                cached = false;
                loadingDate = Long.valueOf(file.lastModified());
            } else {
                cached = true;
            }
            if (System.currentTimeMillis() - loadingDate.longValue() > this.maxFileAge) {
                file.delete();
                this.loadingDates.remove(file);
            } else if (!cached) {
                this.loadingDates.put(file, loadingDate);
            }
        }
        return file;
    }

    @Override // com.nostra13.universalimageloader.cache.disc.impl.BaseDiscCache, com.nostra13.universalimageloader.cache.disc.DiscCacheAware
    public boolean save(String imageUri, InputStream imageStream, IoUtils.CopyListener listener) throws IOException {
        boolean saved = super.save(imageUri, imageStream, listener);
        rememberUsage(imageUri);
        return saved;
    }

    @Override // com.nostra13.universalimageloader.cache.disc.impl.BaseDiscCache, com.nostra13.universalimageloader.cache.disc.DiscCacheAware
    public boolean save(String imageUri, Bitmap bitmap) throws IOException {
        boolean saved = super.save(imageUri, bitmap);
        rememberUsage(imageUri);
        return saved;
    }

    @Override // com.nostra13.universalimageloader.cache.disc.impl.BaseDiscCache, com.nostra13.universalimageloader.cache.disc.DiscCacheAware
    public boolean remove(String imageUri) {
        this.loadingDates.remove(getFile(imageUri));
        return super.remove(imageUri);
    }

    @Override // com.nostra13.universalimageloader.cache.disc.impl.BaseDiscCache, com.nostra13.universalimageloader.cache.disc.DiscCacheAware
    public void clear() {
        super.clear();
        this.loadingDates.clear();
    }

    private void rememberUsage(String imageUri) {
        File file = getFile(imageUri);
        long currentTime = System.currentTimeMillis();
        file.setLastModified(currentTime);
        this.loadingDates.put(file, Long.valueOf(currentTime));
    }
}