package org.apache.http.conn.ssl;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import org.apache.http.util.Args;

public final class PrivateKeyDetails {
    private final X509Certificate[] certChain;
    private final String type;

    public PrivateKeyDetails(String type2, X509Certificate[] certChain2) {
        this.type = (String) Args.notNull(type2, "Private key type");
        this.certChain = certChain2;
    }

    public String getType() {
        return this.type;
    }

    public X509Certificate[] getCertChain() {
        return this.certChain;
    }

    public String toString() {
        return String.valueOf(this.type) + ':' + Arrays.toString(this.certChain);
    }
}
