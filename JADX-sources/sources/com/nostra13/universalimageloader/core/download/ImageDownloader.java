package com.nostra13.universalimageloader.core.download;

import com.xcharge.charger.data.bean.setting.ConsoleSetting;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/* loaded from: classes.dex */
public interface ImageDownloader {
    InputStream getStream(String str, Object obj) throws IOException;

    /* loaded from: classes.dex */
    public enum Scheme {
        HTTP(ConsoleSetting.SCHEMA_HTTP),
        HTTPS("https"),
        FILE("file"),
        CONTENT("content"),
        ASSETS("assets"),
        DRAWABLE("drawable"),
        UNKNOWN("");
        
        private String scheme;
        private String uriPrefix;

        Scheme(String scheme) {
            this.scheme = scheme;
            this.uriPrefix = scheme + "://";
        }

        public static Scheme ofUri(String uri) {
            if (uri != null) {
                Scheme[] arr$ = values();
                for (Scheme s : arr$) {
                    if (s.belongsTo(uri)) {
                        return s;
                    }
                }
            }
            return UNKNOWN;
        }

        private boolean belongsTo(String uri) {
            return uri.toLowerCase(Locale.US).startsWith(this.uriPrefix);
        }

        public String wrap(String path) {
            return this.uriPrefix + path;
        }

        public String crop(String uri) {
            if (!belongsTo(uri)) {
                throw new IllegalArgumentException(String.format("URI [%1$s] doesn't have expected scheme [%2$s]", uri, this.scheme));
            }
            return uri.substring(this.uriPrefix.length());
        }
    }
}
