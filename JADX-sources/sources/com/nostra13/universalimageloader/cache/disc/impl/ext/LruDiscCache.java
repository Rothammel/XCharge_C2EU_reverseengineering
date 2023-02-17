package com.nostra13.universalimageloader.cache.disc.impl.ext;

import android.graphics.Bitmap;
import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.utils.IoUtils;
import com.nostra13.universalimageloader.utils.L;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/* loaded from: classes.dex */
public class LruDiscCache implements DiskCache {
    public static final int DEFAULT_BUFFER_SIZE = 32768;
    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;
    public static final int DEFAULT_COMPRESS_QUALITY = 100;
    private static final String ERROR_ARG_NEGATIVE = " argument must be positive number";
    private static final String ERROR_ARG_NULL = " argument must be not null";
    protected int bufferSize;
    protected DiskLruCache cache;
    protected Bitmap.CompressFormat compressFormat;
    protected int compressQuality;
    protected final FileNameGenerator fileNameGenerator;
    private File reserveCacheDir;

    public LruDiscCache(File cacheDir, FileNameGenerator fileNameGenerator, long cacheMaxSize) throws IOException {
        this(cacheDir, null, fileNameGenerator, cacheMaxSize, 0);
    }

    public LruDiscCache(File cacheDir, File reserveCacheDir, FileNameGenerator fileNameGenerator, long cacheMaxSize, int cacheMaxFileCount) throws IOException {
        this.bufferSize = 32768;
        this.compressFormat = DEFAULT_COMPRESS_FORMAT;
        this.compressQuality = 100;
        if (cacheDir == null) {
            throw new IllegalArgumentException("cacheDir argument must be not null");
        }
        if (cacheMaxSize < 0) {
            throw new IllegalArgumentException("cacheMaxSize argument must be positive number");
        }
        if (cacheMaxFileCount < 0) {
            throw new IllegalArgumentException("cacheMaxFileCount argument must be positive number");
        }
        if (fileNameGenerator == null) {
            throw new IllegalArgumentException("fileNameGenerator argument must be not null");
        }
        cacheMaxSize = cacheMaxSize == 0 ? Long.MAX_VALUE : cacheMaxSize;
        cacheMaxFileCount = cacheMaxFileCount == 0 ? Integer.MAX_VALUE : cacheMaxFileCount;
        this.reserveCacheDir = reserveCacheDir;
        this.fileNameGenerator = fileNameGenerator;
        initCache(cacheDir, reserveCacheDir, cacheMaxSize, cacheMaxFileCount);
    }

    private void initCache(File cacheDir, File reserveCacheDir, long cacheMaxSize, int cacheMaxFileCount) throws IOException {
        try {
            this.cache = DiskLruCache.open(cacheDir, 1, 1, cacheMaxSize, cacheMaxFileCount);
        } catch (IOException e) {
            L.e(e);
            if (reserveCacheDir != null) {
                initCache(reserveCacheDir, null, cacheMaxSize, cacheMaxFileCount);
            }
            if (this.cache == null) {
                throw e;
            }
        }
    }

    @Override // com.nostra13.universalimageloader.cache.disc.DiscCacheAware
    public File getDirectory() {
        return this.cache.getDirectory();
    }

    @Override // com.nostra13.universalimageloader.cache.disc.DiscCacheAware
    public File get(String imageUri) {
        DiskLruCache.Snapshot snapshot = null;
        try {
            try {
                snapshot = this.cache.get(getKey(imageUri));
                r2 = snapshot != null ? snapshot.getFile(0) : null;
            } catch (IOException e) {
                L.e(e);
                if (snapshot != null) {
                    snapshot.close();
                }
            }
            return r2;
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }
    }

    @Override // com.nostra13.universalimageloader.cache.disc.DiscCacheAware
    public boolean save(String imageUri, InputStream imageStream, IoUtils.CopyListener listener) throws IOException {
        boolean copied = false;
        DiskLruCache.Editor editor = this.cache.edit(getKey(imageUri));
        if (editor != null) {
            OutputStream os = new BufferedOutputStream(editor.newOutputStream(0), this.bufferSize);
            try {
                copied = IoUtils.copyStream(imageStream, os, listener, this.bufferSize);
                IoUtils.closeSilently(os);
                if (copied) {
                    editor.commit();
                } else {
                    editor.abort();
                }
            } catch (Throwable th) {
                IoUtils.closeSilently(os);
                if (0 != 0) {
                    editor.commit();
                } else {
                    editor.abort();
                }
                throw th;
            }
        }
        return copied;
    }

    @Override // com.nostra13.universalimageloader.cache.disc.DiscCacheAware
    public boolean save(String imageUri, Bitmap bitmap) throws IOException {
        boolean savedSuccessfully = false;
        DiskLruCache.Editor editor = this.cache.edit(getKey(imageUri));
        if (editor != null) {
            OutputStream os = new BufferedOutputStream(editor.newOutputStream(0), this.bufferSize);
            try {
                savedSuccessfully = bitmap.compress(this.compressFormat, this.compressQuality, os);
                if (savedSuccessfully) {
                    editor.commit();
                } else {
                    editor.abort();
                }
            } finally {
                IoUtils.closeSilently(os);
            }
        }
        return savedSuccessfully;
    }

    @Override // com.nostra13.universalimageloader.cache.disc.DiscCacheAware
    public boolean remove(String imageUri) {
        try {
            return this.cache.remove(getKey(imageUri));
        } catch (IOException e) {
            L.e(e);
            return false;
        }
    }

    @Override // com.nostra13.universalimageloader.cache.disc.DiscCacheAware
    public void close() {
        try {
            this.cache.close();
        } catch (IOException e) {
            L.e(e);
        }
        this.cache = null;
    }

    @Override // com.nostra13.universalimageloader.cache.disc.DiscCacheAware
    public void clear() {
        try {
            this.cache.delete();
        } catch (IOException e) {
            L.e(e);
        }
        try {
            initCache(this.cache.getDirectory(), this.reserveCacheDir, this.cache.getMaxSize(), this.cache.getMaxFileCount());
        } catch (IOException e2) {
            L.e(e2);
        }
    }

    private String getKey(String imageUri) {
        return this.fileNameGenerator.generate(imageUri);
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setCompressFormat(Bitmap.CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
    }

    public void setCompressQuality(int compressQuality) {
        this.compressQuality = compressQuality;
    }
}
