package org.apache.mina.filter.logging;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.mina.core.filterchain.IoFilterEvent;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.util.CommonEventFilter;
import org.slf4j.MDC;

/* loaded from: classes.dex */
public class MdcInjectionFilter extends CommonEventFilter {
    private static final AttributeKey CONTEXT_KEY = new AttributeKey(MdcInjectionFilter.class, "context");
    private ThreadLocal<Integer> callDepth;
    private EnumSet<MdcKey> mdcKeys;

    /* loaded from: classes.dex */
    public enum MdcKey {
        handlerClass,
        remoteAddress,
        localAddress,
        remoteIp,
        remotePort,
        localIp,
        localPort
    }

    public MdcInjectionFilter(EnumSet<MdcKey> keys) {
        this.callDepth = new ThreadLocal<Integer>() { // from class: org.apache.mina.filter.logging.MdcInjectionFilter.1
            /* JADX INFO: Access modifiers changed from: protected */
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // java.lang.ThreadLocal
            public Integer initialValue() {
                return 0;
            }
        };
        this.mdcKeys = keys.clone();
    }

    public MdcInjectionFilter(MdcKey... keys) {
        this.callDepth = new ThreadLocal<Integer>() { // from class: org.apache.mina.filter.logging.MdcInjectionFilter.1
            /* JADX INFO: Access modifiers changed from: protected */
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // java.lang.ThreadLocal
            public Integer initialValue() {
                return 0;
            }
        };
        Set<MdcKey> keySet = new HashSet<>(Arrays.asList(keys));
        this.mdcKeys = EnumSet.copyOf((Collection) keySet);
    }

    public MdcInjectionFilter() {
        this.callDepth = new ThreadLocal<Integer>() { // from class: org.apache.mina.filter.logging.MdcInjectionFilter.1
            /* JADX INFO: Access modifiers changed from: protected */
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // java.lang.ThreadLocal
            public Integer initialValue() {
                return 0;
            }
        };
        this.mdcKeys = EnumSet.allOf(MdcKey.class);
    }

    @Override // org.apache.mina.filter.util.CommonEventFilter
    protected void filter(IoFilterEvent event) throws Exception {
        int currentCallDepth = this.callDepth.get().intValue();
        this.callDepth.set(Integer.valueOf(currentCallDepth + 1));
        Map<String, String> context = getAndFillContext(event.getSession());
        if (currentCallDepth == 0) {
            for (Map.Entry<String, String> e : context.entrySet()) {
                MDC.put(e.getKey(), e.getValue());
            }
        }
        try {
            event.fire();
            if (currentCallDepth == 0) {
                for (String key : context.keySet()) {
                    MDC.remove(key);
                }
                this.callDepth.remove();
                return;
            }
            this.callDepth.set(Integer.valueOf(currentCallDepth));
        } catch (Throwable th) {
            if (currentCallDepth == 0) {
                for (String key2 : context.keySet()) {
                    MDC.remove(key2);
                }
                this.callDepth.remove();
            } else {
                this.callDepth.set(Integer.valueOf(currentCallDepth));
            }
            throw th;
        }
    }

    private Map<String, String> getAndFillContext(IoSession session) {
        Map<String, String> context = getContext(session);
        if (context.isEmpty()) {
            fillContext(session, context);
        }
        return context;
    }

    private static Map<String, String> getContext(IoSession session) {
        Map<String, String> context = (Map) session.getAttribute(CONTEXT_KEY);
        if (context == null) {
            Map<String, String> context2 = new ConcurrentHashMap<>();
            session.setAttribute(CONTEXT_KEY, context2);
            return context2;
        }
        return context;
    }

    protected void fillContext(IoSession session, Map<String, String> context) {
        if (this.mdcKeys.contains(MdcKey.handlerClass)) {
            context.put(MdcKey.handlerClass.name(), session.getHandler().getClass().getName());
        }
        if (this.mdcKeys.contains(MdcKey.remoteAddress)) {
            context.put(MdcKey.remoteAddress.name(), session.getRemoteAddress().toString());
        }
        if (this.mdcKeys.contains(MdcKey.localAddress)) {
            context.put(MdcKey.localAddress.name(), session.getLocalAddress().toString());
        }
        if (session.getTransportMetadata().getAddressType() == InetSocketAddress.class) {
            InetSocketAddress remoteAddress = (InetSocketAddress) session.getRemoteAddress();
            InetSocketAddress localAddress = (InetSocketAddress) session.getLocalAddress();
            if (this.mdcKeys.contains(MdcKey.remoteIp)) {
                context.put(MdcKey.remoteIp.name(), remoteAddress.getAddress().getHostAddress());
            }
            if (this.mdcKeys.contains(MdcKey.remotePort)) {
                context.put(MdcKey.remotePort.name(), String.valueOf(remoteAddress.getPort()));
            }
            if (this.mdcKeys.contains(MdcKey.localIp)) {
                context.put(MdcKey.localIp.name(), localAddress.getAddress().getHostAddress());
            }
            if (this.mdcKeys.contains(MdcKey.localPort)) {
                context.put(MdcKey.localPort.name(), String.valueOf(localAddress.getPort()));
            }
        }
    }

    public static String getProperty(IoSession session, String key) {
        if (key == null) {
            throw new IllegalArgumentException("key should not be null");
        }
        Map<String, String> context = getContext(session);
        String answer = context.get(key);
        return answer != null ? answer : MDC.get(key);
    }

    public static void setProperty(IoSession session, String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("key should not be null");
        }
        if (value == null) {
            removeProperty(session, key);
        }
        Map<String, String> context = getContext(session);
        context.put(key, value);
        MDC.put(key, value);
    }

    public static void removeProperty(IoSession session, String key) {
        if (key == null) {
            throw new IllegalArgumentException("key should not be null");
        }
        Map<String, String> context = getContext(session);
        context.remove(key);
        MDC.remove(key);
    }
}
