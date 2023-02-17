package org.apache.mina.filter.logging;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.util.CommonEventFilter;
import org.slf4j.MDC;

public class MdcInjectionFilter extends CommonEventFilter {
    private static final AttributeKey CONTEXT_KEY = new AttributeKey(MdcInjectionFilter.class, "context");
    private ThreadLocal<Integer> callDepth;
    private EnumSet<MdcKey> mdcKeys;

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
        this.callDepth = new ThreadLocal<Integer>() {
            /* access modifiers changed from: protected */
            public Integer initialValue() {
                return 0;
            }
        };
        this.mdcKeys = keys.clone();
    }

    public MdcInjectionFilter(MdcKey... keys) {
        this.callDepth = new ThreadLocal<Integer>() {
            /* access modifiers changed from: protected */
            public Integer initialValue() {
                return 0;
            }
        };
        this.mdcKeys = EnumSet.copyOf(new HashSet<>(Arrays.asList(keys)));
    }

    public MdcInjectionFilter() {
        this.callDepth = new ThreadLocal<Integer>() {
            /* access modifiers changed from: protected */
            public Integer initialValue() {
                return 0;
            }
        };
        this.mdcKeys = EnumSet.allOf(MdcKey.class);
    }

    /*  JADX ERROR: StackOverflow in pass: MarkFinallyVisitor
        jadx.core.utils.exceptions.JadxOverflowException: 
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:47)
        	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:81)
        */
    protected void filter(org.apache.mina.core.filterchain.IoFilterEvent r8) throws java.lang.Exception {
        /*
            r7 = this;
            java.lang.ThreadLocal<java.lang.Integer> r4 = r7.callDepth
            java.lang.Object r4 = r4.get()
            java.lang.Integer r4 = (java.lang.Integer) r4
            int r1 = r4.intValue()
            java.lang.ThreadLocal<java.lang.Integer> r4 = r7.callDepth
            int r5 = r1 + 1
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)
            r4.set(r5)
            org.apache.mina.core.session.IoSession r4 = r8.getSession()
            java.util.Map r0 = r7.getAndFillContext(r4)
            if (r1 != 0) goto L_0x0045
            java.util.Set r4 = r0.entrySet()
            java.util.Iterator r6 = r4.iterator()
        L_0x0029:
            boolean r4 = r6.hasNext()
            if (r4 == 0) goto L_0x0045
            java.lang.Object r2 = r6.next()
            java.util.Map$Entry r2 = (java.util.Map.Entry) r2
            java.lang.Object r4 = r2.getKey()
            java.lang.String r4 = (java.lang.String) r4
            java.lang.Object r5 = r2.getValue()
            java.lang.String r5 = (java.lang.String) r5
            org.slf4j.MDC.put(r4, r5)
            goto L_0x0029
        L_0x0045:
            r8.fire()     // Catch:{ all -> 0x0072 }
            if (r1 != 0) goto L_0x0068
            java.util.Set r4 = r0.keySet()
            java.util.Iterator r4 = r4.iterator()
        L_0x0052:
            boolean r5 = r4.hasNext()
            if (r5 == 0) goto L_0x0062
            java.lang.Object r3 = r4.next()
            java.lang.String r3 = (java.lang.String) r3
            org.slf4j.MDC.remove(r3)
            goto L_0x0052
        L_0x0062:
            java.lang.ThreadLocal<java.lang.Integer> r4 = r7.callDepth
            r4.remove()
        L_0x0067:
            return
        L_0x0068:
            java.lang.ThreadLocal<java.lang.Integer> r4 = r7.callDepth
            java.lang.Integer r5 = java.lang.Integer.valueOf(r1)
            r4.set(r5)
            goto L_0x0067
        L_0x0072:
            r4 = move-exception
            if (r1 != 0) goto L_0x0093
            java.util.Set r5 = r0.keySet()
            java.util.Iterator r5 = r5.iterator()
        L_0x007d:
            boolean r6 = r5.hasNext()
            if (r6 == 0) goto L_0x008d
            java.lang.Object r3 = r5.next()
            java.lang.String r3 = (java.lang.String) r3
            org.slf4j.MDC.remove(r3)
            goto L_0x007d
        L_0x008d:
            java.lang.ThreadLocal<java.lang.Integer> r5 = r7.callDepth
            r5.remove()
        L_0x0092:
            throw r4
        L_0x0093:
            java.lang.ThreadLocal<java.lang.Integer> r5 = r7.callDepth
            java.lang.Integer r6 = java.lang.Integer.valueOf(r1)
            r5.set(r6)
            goto L_0x0092
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.mina.filter.logging.MdcInjectionFilter.filter(org.apache.mina.core.filterchain.IoFilterEvent):void");
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
        if (context != null) {
            return context;
        }
        Map<String, String> context2 = new ConcurrentHashMap<>();
        session.setAttribute(CONTEXT_KEY, context2);
        return context2;
    }

    /* access modifiers changed from: protected */
    public void fillContext(IoSession session, Map<String, String> context) {
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
        String answer = getContext(session).get(key);
        return answer != null ? answer : MDC.get(key);
    }

    public static void setProperty(IoSession session, String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("key should not be null");
        }
        if (value == null) {
            removeProperty(session, key);
        }
        getContext(session).put(key, value);
        MDC.put(key, value);
    }

    public static void removeProperty(IoSession session, String key) {
        if (key == null) {
            throw new IllegalArgumentException("key should not be null");
        }
        getContext(session).remove(key);
        MDC.remove(key);
    }
}
