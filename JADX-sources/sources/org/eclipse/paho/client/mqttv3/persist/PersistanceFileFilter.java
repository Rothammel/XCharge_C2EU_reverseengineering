package org.eclipse.paho.client.mqttv3.persist;

import java.io.File;
import java.io.FileFilter;

/* loaded from: classes.dex */
public class PersistanceFileFilter implements FileFilter {
    private final String fileExtension;

    public PersistanceFileFilter(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    @Override // java.io.FileFilter
    public boolean accept(File pathname) {
        return pathname.getName().endsWith(this.fileExtension);
    }
}
