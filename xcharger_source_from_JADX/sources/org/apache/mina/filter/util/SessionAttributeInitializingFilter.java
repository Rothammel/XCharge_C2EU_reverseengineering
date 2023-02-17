package org.apache.mina.filter.util;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;

public class SessionAttributeInitializingFilter extends IoFilterAdapter {
    private final Map<String, Object> attributes = new ConcurrentHashMap();

    public SessionAttributeInitializingFilter() {
    }

    public SessionAttributeInitializingFilter(Map<String, ? extends Object> attributes2) {
        setAttributes(attributes2);
    }

    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    public Object setAttribute(String key, Object value) {
        if (value == null) {
            return removeAttribute(key);
        }
        return this.attributes.put(key, value);
    }

    public Object setAttribute(String key) {
        return this.attributes.put(key, Boolean.TRUE);
    }

    public Object removeAttribute(String key) {
        return this.attributes.remove(key);
    }

    /* access modifiers changed from: package-private */
    public boolean containsAttribute(String key) {
        return this.attributes.containsKey(key);
    }

    public Set<String> getAttributeKeys() {
        return this.attributes.keySet();
    }

    public void setAttributes(Map<String, ? extends Object> attributes2) {
        if (attributes2 == null) {
            attributes2 = new ConcurrentHashMap<>();
        }
        this.attributes.clear();
        this.attributes.putAll(attributes2);
    }

    public void sessionCreated(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        for (Map.Entry<String, Object> e : this.attributes.entrySet()) {
            session.setAttribute(e.getKey(), e.getValue());
        }
        nextFilter.sessionCreated(session);
    }
}
