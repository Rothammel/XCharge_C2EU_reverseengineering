package org.apache.mina.filter.ssl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

public class KeyStoreFactory {
    private byte[] data = null;
    private char[] password = null;
    private String provider = null;
    private String type = "JKS";

    public KeyStore newInstance() throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore ks;
        if (this.data == null) {
            throw new IllegalStateException("data property is not set.");
        }
        if (this.provider == null) {
            ks = KeyStore.getInstance(this.type);
        } else {
            ks = KeyStore.getInstance(this.type, this.provider);
        }
        InputStream is = new ByteArrayInputStream(this.data);
        try {
            ks.load(is, this.password);
            return ks;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
    }

    public void setType(String type2) {
        if (type2 == null) {
            throw new IllegalArgumentException("type");
        }
        this.type = type2;
    }

    public void setPassword(String password2) {
        if (password2 != null) {
            this.password = password2.toCharArray();
        } else {
            this.password = null;
        }
    }

    public void setProvider(String provider2) {
        this.provider = provider2;
    }

    public void setData(byte[] data2) {
        byte[] copy = new byte[data2.length];
        System.arraycopy(data2, 0, copy, 0, data2.length);
        this.data = copy;
    }

    private void setData(InputStream dataStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while (true) {
            try {
                int data2 = dataStream.read();
                if (data2 < 0) {
                    setData(out.toByteArray());
                    try {
                        return;
                    } catch (IOException e) {
                        return;
                    }
                } else {
                    out.write(data2);
                }
            } finally {
                try {
                    dataStream.close();
                } catch (IOException e2) {
                }
            }
        }
    }

    public void setDataFile(File dataFile) throws IOException {
        setData((InputStream) new BufferedInputStream(new FileInputStream(dataFile)));
    }

    public void setDataUrl(URL dataUrl) throws IOException {
        setData(dataUrl.openStream());
    }
}
