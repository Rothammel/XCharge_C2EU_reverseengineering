package org.apache.mina.filter.ssl;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;

/* loaded from: classes.dex */
public class BogusTrustManagerFactory extends TrustManagerFactory {
    private static final X509TrustManager X509 = new X509TrustManager() { // from class: org.apache.mina.filter.ssl.BogusTrustManagerFactory.2
        @Override // javax.net.ssl.X509TrustManager
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override // javax.net.ssl.X509TrustManager
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override // javax.net.ssl.X509TrustManager
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };
    private static final TrustManager[] X509_MANAGERS = {X509};

    public BogusTrustManagerFactory() {
        super(new BogusTrustManagerFactorySpi(), new Provider("MinaBogus", 1.0d, "") { // from class: org.apache.mina.filter.ssl.BogusTrustManagerFactory.1
            private static final long serialVersionUID = -4024169055312053827L;
        }, "MinaBogus");
    }

    /* loaded from: classes.dex */
    private static class BogusTrustManagerFactorySpi extends TrustManagerFactorySpi {
        private BogusTrustManagerFactorySpi() {
        }

        @Override // javax.net.ssl.TrustManagerFactorySpi
        protected TrustManager[] engineGetTrustManagers() {
            return BogusTrustManagerFactory.X509_MANAGERS;
        }

        @Override // javax.net.ssl.TrustManagerFactorySpi
        protected void engineInit(KeyStore keystore) throws KeyStoreException {
        }

        @Override // javax.net.ssl.TrustManagerFactorySpi
        protected void engineInit(ManagerFactoryParameters managerFactoryParameters) throws InvalidAlgorithmParameterException {
        }
    }
}
