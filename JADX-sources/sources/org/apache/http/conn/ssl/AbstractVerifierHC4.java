package org.apache.http.conn.ssl;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.Immutable;
import org.apache.http.conn.util.InetAddressUtilsHC4;
import org.apache.http.util.TextUtils;
import org.slf4j.Marker;

@Immutable
/* loaded from: classes.dex */
public abstract class AbstractVerifierHC4 implements X509HostnameVerifier {
    private static final String[] BAD_COUNTRY_2LDS = {"ac", "co", "com", "ed", "edu", "go", "gouv", "gov", "info", "lg", "ne", "net", "or", "org"};
    private static final String TAG = "HttpClient";

    static {
        Arrays.sort(BAD_COUNTRY_2LDS);
    }

    @Override // org.apache.http.conn.ssl.X509HostnameVerifier
    public final void verify(String host, SSLSocket ssl) throws IOException {
        if (host == null) {
            throw new NullPointerException("host to verify is null");
        }
        SSLSession session = ssl.getSession();
        if (session == null) {
            InputStream in = ssl.getInputStream();
            in.available();
            session = ssl.getSession();
            if (session == null) {
                ssl.startHandshake();
                session = ssl.getSession();
            }
        }
        Certificate[] certs = session.getPeerCertificates();
        X509Certificate x509 = (X509Certificate) certs[0];
        verify(host, x509);
    }

    @Override // org.apache.http.conn.ssl.X509HostnameVerifier, javax.net.ssl.HostnameVerifier
    public final boolean verify(String host, SSLSession session) {
        try {
            Certificate[] certs = session.getPeerCertificates();
            X509Certificate x509 = (X509Certificate) certs[0];
            verify(host, x509);
            return true;
        } catch (SSLException e) {
            return false;
        }
    }

    @Override // org.apache.http.conn.ssl.X509HostnameVerifier
    public final void verify(String host, X509Certificate cert) throws SSLException {
        String[] cns = getCNs(cert);
        String[] subjectAlts = getSubjectAlts(cert, host);
        verify(host, cns, subjectAlts);
    }

    public final void verify(String host, String[] cns, String[] subjectAlts, boolean strictWithSubDomains) throws SSLException {
        LinkedList<String> names = new LinkedList<>();
        if (cns != null && cns.length > 0 && cns[0] != null) {
            names.add(cns[0]);
        }
        if (subjectAlts != null) {
            for (String subjectAlt : subjectAlts) {
                if (subjectAlt != null) {
                    names.add(subjectAlt);
                }
            }
        }
        if (names.isEmpty()) {
            String msg = "Certificate for <" + host + "> doesn't contain CN or DNS subjectAlt";
            throw new SSLException(msg);
        }
        StringBuilder buf = new StringBuilder();
        String hostName = normaliseIPv6Address(host.trim().toLowerCase(Locale.ENGLISH));
        boolean match = false;
        Iterator<String> it2 = names.iterator();
        while (it2.hasNext()) {
            String cn = it2.next().toLowerCase(Locale.ENGLISH);
            buf.append(" <");
            buf.append(cn);
            buf.append('>');
            if (it2.hasNext()) {
                buf.append(" OR");
            }
            String[] parts = cn.split("\\.");
            boolean doWildcard = parts.length >= 3 && parts[0].endsWith(Marker.ANY_MARKER) && validCountryWildcard(cn) && !isIPAddress(host);
            if (doWildcard) {
                String firstpart = parts[0];
                if (firstpart.length() > 1) {
                    String prefix = firstpart.substring(0, firstpart.length() - 1);
                    String suffix = cn.substring(firstpart.length());
                    String hostSuffix = hostName.substring(prefix.length());
                    match = hostName.startsWith(prefix) && hostSuffix.endsWith(suffix);
                } else {
                    match = hostName.endsWith(cn.substring(1));
                }
                if (match && strictWithSubDomains) {
                    if (countDots(hostName) == countDots(cn)) {
                        match = true;
                        continue;
                    } else {
                        match = false;
                        continue;
                    }
                }
            } else {
                match = hostName.equals(normaliseIPv6Address(cn));
                continue;
            }
            if (match) {
                break;
            }
        }
        if (!match) {
            throw new SSLException("hostname in certificate didn't match: <" + host + "> !=" + ((Object) buf));
        }
    }

    @Deprecated
    public static boolean acceptableCountryWildcard(String cn) {
        String[] parts = cn.split("\\.");
        return (parts.length == 3 && parts[2].length() == 2 && Arrays.binarySearch(BAD_COUNTRY_2LDS, parts[1]) >= 0) ? false : true;
    }

    boolean validCountryWildcard(String cn) {
        String[] parts = cn.split("\\.");
        return (parts.length == 3 && parts[2].length() == 2 && Arrays.binarySearch(BAD_COUNTRY_2LDS, parts[1]) >= 0) ? false : true;
    }

    public static String[] getCNs(X509Certificate cert) {
        String subjectPrincipal = cert.getSubjectX500Principal().toString();
        try {
            return extractCNs(subjectPrincipal);
        } catch (SSLException e) {
            return null;
        }
    }

    static String[] extractCNs(String subjectPrincipal) throws SSLException {
        if (subjectPrincipal == null) {
            return null;
        }
        List<String> cns = new ArrayList<>();
        List<NameValuePair> nvps = DistinguishedNameParser.INSTANCE.parse(subjectPrincipal);
        for (int i = 0; i < nvps.size(); i++) {
            NameValuePair nvp = nvps.get(i);
            String attribName = nvp.getName();
            String attribValue = nvp.getValue();
            if (TextUtils.isBlank(attribName)) {
                throw new SSLException(String.valueOf(subjectPrincipal) + " is not a valid X500 distinguished name");
            }
            if (attribName.equalsIgnoreCase("cn")) {
                cns.add(attribValue);
            }
        }
        if (cns.isEmpty()) {
            return null;
        }
        return (String[]) cns.toArray(new String[cns.size()]);
    }

    private static String[] getSubjectAlts(X509Certificate cert, String hostname) {
        int subjectType;
        if (isIPAddress(hostname)) {
            subjectType = 7;
        } else {
            subjectType = 2;
        }
        LinkedList<String> subjectAltList = new LinkedList<>();
        Collection<List<?>> c = null;
        try {
            c = cert.getSubjectAlternativeNames();
        } catch (CertificateParsingException e) {
        }
        if (c != null) {
            for (List<?> aC : c) {
                int type = ((Integer) aC.get(0)).intValue();
                if (type == subjectType) {
                    String s = (String) aC.get(1);
                    subjectAltList.add(s);
                }
            }
        }
        if (!subjectAltList.isEmpty()) {
            String[] subjectAlts = new String[subjectAltList.size()];
            subjectAltList.toArray(subjectAlts);
            return subjectAlts;
        }
        return null;
    }

    public static String[] getDNSSubjectAlts(X509Certificate cert) {
        return getSubjectAlts(cert, null);
    }

    public static int countDots(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '.') {
                count++;
            }
        }
        return count;
    }

    private static boolean isIPAddress(String hostname) {
        return hostname != null && (InetAddressUtilsHC4.isIPv4Address(hostname) || InetAddressUtilsHC4.isIPv6Address(hostname));
    }

    private String normaliseIPv6Address(String hostname) {
        if (hostname != null && InetAddressUtilsHC4.isIPv6Address(hostname)) {
            try {
                InetAddress inetAddress = InetAddress.getByName(hostname);
                return inetAddress.getHostAddress();
            } catch (UnknownHostException uhe) {
                Log.e(TAG, "Unexpected error converting " + hostname, uhe);
                return hostname;
            }
        }
        return hostname;
    }
}
