package it.sauronsoftware.ftp4j.extrecognizers;

import it.sauronsoftware.ftp4j.FTPTextualExtensionRecognizer;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class ParametricTextualExtensionRecognizer implements FTPTextualExtensionRecognizer {
    private ArrayList exts = new ArrayList();

    public ParametricTextualExtensionRecognizer() {
    }

    public ParametricTextualExtensionRecognizer(String[] exts) {
        for (String str : exts) {
            addExtension(str);
        }
    }

    public ParametricTextualExtensionRecognizer(ArrayList exts) {
        int size = exts.size();
        for (int i = 0; i < size; i++) {
            Object aux = exts.get(i);
            if (aux instanceof String) {
                String ext = (String) aux;
                addExtension(ext);
            }
        }
    }

    public void addExtension(String ext) {
        synchronized (this.exts) {
            this.exts.add(ext.toLowerCase());
        }
    }

    public void removeExtension(String ext) {
        synchronized (this.exts) {
            this.exts.remove(ext.toLowerCase());
        }
    }

    public String[] getExtensions() {
        String[] ret;
        synchronized (this.exts) {
            int size = this.exts.size();
            ret = new String[size];
            for (int i = 0; i < size; i++) {
                ret[i] = (String) this.exts.get(i);
            }
        }
        return ret;
    }

    @Override // it.sauronsoftware.ftp4j.FTPTextualExtensionRecognizer
    public boolean isTextualExt(String ext) {
        boolean contains;
        synchronized (this.exts) {
            contains = this.exts.contains(ext);
        }
        return contains;
    }
}