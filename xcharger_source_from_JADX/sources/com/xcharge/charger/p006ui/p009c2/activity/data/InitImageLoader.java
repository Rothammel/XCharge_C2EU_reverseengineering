package com.xcharge.charger.p006ui.p009c2.activity.data;

import android.content.Context;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/* renamed from: com.xcharge.charger.ui.c2.activity.data.InitImageLoader */
public class InitImageLoader {
    private static volatile InitImageLoader instance = null;
    public ImageLoader loader;

    public static InitImageLoader getInstance(Context context) {
        if (instance == null) {
            synchronized (InitImageLoader.class) {
                if (instance == null) {
                    instance = new InitImageLoader();
                    instance.initImageLoaderOptions(context);
                }
            }
        }
        return instance;
    }

    public synchronized void initImageLoaderOptions(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).threadPoolSize(5).discCacheFileCount(10).diskCacheSize(20971520).memoryCacheSize(2097152).build();
        this.loader = ImageLoader.getInstance();
        this.loader.init(config);
    }

    public synchronized ImageLoader getLoader() {
        return this.loader;
    }
}
