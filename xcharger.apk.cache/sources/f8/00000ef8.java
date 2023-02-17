package org.eclipse.paho.client.mqttv3.persist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttPersistable;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.internal.FileLock;
import org.eclipse.paho.client.mqttv3.internal.MqttPersistentData;

/* loaded from: classes.dex */
public class MqttDefaultFilePersistence implements MqttClientPersistence {
    private static FilenameFilter FILENAME_FILTER = null;
    private static final String LOCK_FILENAME = ".lck";
    private static final String MESSAGE_BACKUP_FILE_EXTENSION = ".bup";
    private static final String MESSAGE_FILE_EXTENSION = ".msg";
    private File clientDir;
    private File dataDir;
    private FileLock fileLock;

    private static FilenameFilter getFilenameFilter() {
        if (FILENAME_FILTER == null) {
            FILENAME_FILTER = new PersistanceFileNameFilter(MESSAGE_FILE_EXTENSION);
        }
        return FILENAME_FILTER;
    }

    public MqttDefaultFilePersistence() {
        this(System.getProperty("user.dir"));
    }

    public MqttDefaultFilePersistence(String directory) {
        this.clientDir = null;
        this.fileLock = null;
        this.dataDir = new File(directory);
    }

    @Override // org.eclipse.paho.client.mqttv3.MqttClientPersistence
    public void open(String clientId, String theConnection) throws MqttPersistenceException {
        if (this.dataDir.exists() && !this.dataDir.isDirectory()) {
            throw new MqttPersistenceException();
        }
        if (!this.dataDir.exists() && !this.dataDir.mkdirs()) {
            throw new MqttPersistenceException();
        }
        if (!this.dataDir.canWrite()) {
            throw new MqttPersistenceException();
        }
        StringBuffer keyBuffer = new StringBuffer();
        for (int i = 0; i < clientId.length(); i++) {
            char c = clientId.charAt(i);
            if (isSafeChar(c)) {
                keyBuffer.append(c);
            }
        }
        keyBuffer.append("-");
        for (int i2 = 0; i2 < theConnection.length(); i2++) {
            char c2 = theConnection.charAt(i2);
            if (isSafeChar(c2)) {
                keyBuffer.append(c2);
            }
        }
        synchronized (this) {
            if (this.clientDir == null) {
                String key = keyBuffer.toString();
                this.clientDir = new File(this.dataDir, key);
                if (!this.clientDir.exists()) {
                    this.clientDir.mkdir();
                }
            }
            try {
                this.fileLock = new FileLock(this.clientDir, LOCK_FILENAME);
            } catch (Exception e) {
            }
            restoreBackups(this.clientDir);
        }
    }

    private void checkIsOpen() throws MqttPersistenceException {
        if (this.clientDir == null) {
            throw new MqttPersistenceException();
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.MqttClientPersistence
    public void close() throws MqttPersistenceException {
        synchronized (this) {
            if (this.fileLock != null) {
                this.fileLock.release();
            }
            if (getFiles().length == 0) {
                this.clientDir.delete();
            }
            this.clientDir = null;
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.MqttClientPersistence
    public void put(String key, MqttPersistable message) throws MqttPersistenceException {
        checkIsOpen();
        File file = new File(this.clientDir, String.valueOf(key) + MESSAGE_FILE_EXTENSION);
        File backupFile = new File(this.clientDir, String.valueOf(key) + MESSAGE_FILE_EXTENSION + MESSAGE_BACKUP_FILE_EXTENSION);
        if (file.exists()) {
            boolean result = file.renameTo(backupFile);
            if (!result) {
                backupFile.delete();
                file.renameTo(backupFile);
            }
        }
        try {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(message.getHeaderBytes(), message.getHeaderOffset(), message.getHeaderLength());
                if (message.getPayloadBytes() != null) {
                    fos.write(message.getPayloadBytes(), message.getPayloadOffset(), message.getPayloadLength());
                }
                fos.getFD().sync();
                fos.close();
                if (backupFile.exists()) {
                    backupFile.delete();
                }
            } catch (IOException ex) {
                throw new MqttPersistenceException(ex);
            }
        } finally {
            if (backupFile.exists()) {
                boolean result2 = backupFile.renameTo(file);
                if (!result2) {
                    file.delete();
                    backupFile.renameTo(file);
                }
            }
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.MqttClientPersistence
    public MqttPersistable get(String key) throws MqttPersistenceException {
        checkIsOpen();
        try {
            File file = new File(this.clientDir, String.valueOf(key) + MESSAGE_FILE_EXTENSION);
            FileInputStream fis = new FileInputStream(file);
            int size = fis.available();
            byte[] data = new byte[size];
            for (int read = 0; read < size; read += fis.read(data, read, size - read)) {
            }
            fis.close();
            MqttPersistable result = new MqttPersistentData(key, data, 0, data.length, null, 0, 0);
            return result;
        } catch (IOException ex) {
            throw new MqttPersistenceException(ex);
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.MqttClientPersistence
    public void remove(String key) throws MqttPersistenceException {
        checkIsOpen();
        File file = new File(this.clientDir, String.valueOf(key) + MESSAGE_FILE_EXTENSION);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.MqttClientPersistence
    public Enumeration keys() throws MqttPersistenceException {
        checkIsOpen();
        File[] files = getFiles();
        Vector result = new Vector(files.length);
        for (File file : files) {
            String filename = file.getName();
            String key = filename.substring(0, filename.length() - MESSAGE_FILE_EXTENSION.length());
            result.addElement(key);
        }
        return result.elements();
    }

    private File[] getFiles() throws MqttPersistenceException {
        checkIsOpen();
        File[] files = this.clientDir.listFiles(getFilenameFilter());
        if (files == null) {
            throw new MqttPersistenceException();
        }
        return files;
    }

    private boolean isSafeChar(char c) {
        return Character.isJavaIdentifierPart(c) || c == '-';
    }

    private void restoreBackups(File dir) throws MqttPersistenceException {
        File[] files = dir.listFiles(new PersistanceFileFilter(MESSAGE_BACKUP_FILE_EXTENSION));
        if (files == null) {
            throw new MqttPersistenceException();
        }
        for (int i = 0; i < files.length; i++) {
            File originalFile = new File(dir, files[i].getName().substring(0, files[i].getName().length() - MESSAGE_BACKUP_FILE_EXTENSION.length()));
            boolean result = files[i].renameTo(originalFile);
            if (!result) {
                originalFile.delete();
                files[i].renameTo(originalFile);
            }
        }
    }

    @Override // org.eclipse.paho.client.mqttv3.MqttClientPersistence
    public boolean containsKey(String key) throws MqttPersistenceException {
        checkIsOpen();
        File file = new File(this.clientDir, String.valueOf(key) + MESSAGE_FILE_EXTENSION);
        return file.exists();
    }

    @Override // org.eclipse.paho.client.mqttv3.MqttClientPersistence
    public void clear() throws MqttPersistenceException {
        checkIsOpen();
        File[] files = getFiles();
        for (File file : files) {
            file.delete();
        }
        this.clientDir.delete();
    }
}