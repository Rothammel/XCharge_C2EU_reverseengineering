package org.apache.http.config;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.http.annotation.ThreadSafe;

@ThreadSafe
/* loaded from: classes.dex */
public final class Registry<I> implements Lookup<I> {
    private final Map<String, I> map;

    /* JADX INFO: Access modifiers changed from: package-private */
    public Registry(Map<String, I> map) {
        this.map = new ConcurrentHashMap(map);
    }

    @Override // org.apache.http.config.Lookup
    public I lookup(String key) {
        if (key == null) {
            return null;
        }
        return this.map.get(key.toLowerCase(Locale.US));
    }

    public String toString() {
        return this.map.toString();
    }
}