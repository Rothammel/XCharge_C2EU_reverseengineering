package org.apache.http.impl.conn;

import android.util.Log;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.http.conn.DnsResolver;
import org.apache.http.util.Args;

/* loaded from: classes.dex */
public class InMemoryDnsResolver implements DnsResolver {
    private static final String TAG = "HttpClient";
    private final Map<String, InetAddress[]> dnsMap = new ConcurrentHashMap();

    public void add(String host, InetAddress... ips) {
        Args.notNull(host, "Host name");
        Args.notNull(ips, "Array of IP addresses");
        this.dnsMap.put(host, ips);
    }

    @Override // org.apache.http.conn.DnsResolver
    public InetAddress[] resolve(String host) throws UnknownHostException {
        InetAddress[] resolvedAddresses = this.dnsMap.get(host);
        if (Log.isLoggable(TAG, 4)) {
            Log.i(TAG, "Resolving " + host + " to " + Arrays.deepToString(resolvedAddresses));
        }
        if (resolvedAddresses == null) {
            throw new UnknownHostException(String.valueOf(host) + " cannot be resolved");
        }
        return resolvedAddresses;
    }
}