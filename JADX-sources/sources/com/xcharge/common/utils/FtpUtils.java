package com.xcharge.common.utils;

import android.text.TextUtils;
import android.util.Log;
import com.xcharge.common.bean.JsonBean;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class FtpUtils {
    private static ThreadPoolExecutor cmdThreadPoolExecutor;
    private static DeadTaskMonitorThread deadTaskMonitorThread;
    private static ConcurrentHashMap<String, TransferProfile> transferProfiles;
    private static ThreadPoolExecutor transferThreadPoolExecutor;

    /* loaded from: classes.dex */
    public interface TransferListener {
        void onConnectFail();

        void onConnected();

        void onTransferComplete();

        void onTransferFail();

        void onTransferPercentage(int i);

        void onTransferPercentage(long j, long j2);
    }

    static {
        transferProfiles = null;
        transferThreadPoolExecutor = null;
        cmdThreadPoolExecutor = null;
        deadTaskMonitorThread = null;
        transferProfiles = new ConcurrentHashMap<>();
        transferThreadPoolExecutor = new ThreadPoolExecutor(3, 3, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue(), new RejectedExecutionHandler() { // from class: com.xcharge.common.utils.FtpUtils.1
            @Override // java.util.concurrent.RejectedExecutionHandler
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                Log.w("FtpUtils.transferThreadPoolExecutor", "rejected runnable: " + r.toString() + ", active runnables: " + executor.getActiveCount());
                if (r instanceof DownloadTask) {
                    DownloadTask downloadTask = (DownloadTask) r;
                    FtpUtils.transferProfiles.remove(downloadTask.getProfileKey());
                } else if (r instanceof UploadTask) {
                    UploadTask upLoadTask = (UploadTask) r;
                    FtpUtils.transferProfiles.remove(upLoadTask.getProfileKey());
                }
            }
        });
        transferThreadPoolExecutor.allowCoreThreadTimeOut(true);
        cmdThreadPoolExecutor = new ThreadPoolExecutor(3, 3, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue(), new RejectedExecutionHandler() { // from class: com.xcharge.common.utils.FtpUtils.2
            @Override // java.util.concurrent.RejectedExecutionHandler
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                Log.w("FtpUtils.ftpCmdThreadPoolExecutor", "rejected runnable: " + r.toString() + ", active runnables: " + executor.getActiveCount());
            }
        });
        cmdThreadPoolExecutor.allowCoreThreadTimeOut(true);
        deadTaskMonitorThread = new DeadTaskMonitorThread(transferThreadPoolExecutor, transferProfiles);
        deadTaskMonitorThread.start();
    }

    public static boolean changeTransferCoreThreadNum(int newNum) {
        int nowNum = transferThreadPoolExecutor.getCorePoolSize();
        int maxNum = transferThreadPoolExecutor.getMaximumPoolSize();
        if (newNum < 0) {
            newNum = nowNum;
        }
        if (newNum > maxNum) {
            newNum = maxNum;
        }
        if (newNum != nowNum) {
            transferThreadPoolExecutor.setCorePoolSize(newNum);
            Log.w("FtpUtils.changeTransferCoreThreadNum", "change transfer thread pool core number to: " + newNum);
            return true;
        }
        return true;
    }

    /* loaded from: classes.dex */
    private static class DeadTaskMonitorThread extends Thread {
        private volatile boolean shutdown = false;
        private final ThreadPoolExecutor threadPoolExecutor;
        private final ConcurrentHashMap<String, TransferProfile> transferProfiles;

        public DeadTaskMonitorThread(ThreadPoolExecutor threadPoolExecutor, ConcurrentHashMap<String, TransferProfile> transferProfiles) {
            this.threadPoolExecutor = threadPoolExecutor;
            this.transferProfiles = transferProfiles;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (!this.shutdown) {
                try {
                    synchronized (this) {
                        wait(30000L);
                        long now = System.currentTimeMillis();
                        HashMap<String, TransferProfile> clonedTransferProfiles = new HashMap<>();
                        clonedTransferProfiles.putAll(this.transferProfiles);
                        for (Map.Entry<String, TransferProfile> entry : clonedTransferProfiles.entrySet()) {
                            TransferProfile transferProfile = entry.getValue();
                            FTPClient client = transferProfile.getClient();
                            if (client != null && (transferProfile.getTotal() == 0 || transferProfile.getCount() < transferProfile.getTotal())) {
                                if (now - transferProfile.getTs() >= 120000) {
                                    Log.w("FtpUtils.DeadTaskMonitorThread", "clear dead transfer task: " + transferProfile.getProfileKey());
                                    boolean isConnected = client.isConnected();
                                    if (isConnected) {
                                        if (transferProfile.isTransferStarted() || transferProfile.getCount() > 0) {
                                            FtpUtils.interrupt(client, true);
                                        } else {
                                            FtpUtils.disconnect(client, true);
                                        }
                                    } else {
                                        FtpUtils.closeSocket(client);
                                    }
                                    Future future = transferProfile.getTaskFuture();
                                    if (!future.isDone()) {
                                        future.cancel(true);
                                    }
                                    this.threadPoolExecutor.purge();
                                    if (transferProfile.getListener() != null) {
                                        if (isConnected) {
                                            if (!transferProfile.isTransferResultNotified()) {
                                                transferProfile.getListener().onTransferFail();
                                            }
                                            transferProfile.setTransferResultNotified(true);
                                        } else {
                                            if (!transferProfile.isConnectResultNotified()) {
                                                transferProfile.getListener().onConnectFail();
                                            }
                                            transferProfile.setConnectResultNotified(true);
                                        }
                                    }
                                    if (transferProfile.isDownload()) {
                                        FileUtils.deleteFile(transferProfile.getTmpDownloadFile());
                                    }
                                    this.transferProfiles.remove(transferProfile.getProfileKey());
                                    Log.i("FtpUtils.DeadTaskMonitorThread", "dead transfer task: " + transferProfile.getProfileKey() + " has been forced termination");
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    Log.w("FtpUtils.DeadTaskMonitorThread", Log.getStackTraceString(ex));
                    return;
                }
            }
        }

        public void shutdown() {
            this.shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }

    /* loaded from: classes.dex */
    public static class TransferProfile {
        private FTPClient client;
        private boolean connectResultNotified;
        private long count;
        private FtpConfig ftpConfig;
        private boolean isDownload;
        private TransferListener listener;
        private String localFile;
        private int percent;
        private int prePercent;
        private String remoteFile;
        private Future taskFuture;
        private String tmpDownloadFile;
        private long total;
        private boolean transferResultNotified;
        private boolean transferStarted;
        private long ts;

        private TransferProfile() {
            this.isDownload = true;
            this.count = 0L;
            this.total = 0L;
            this.prePercent = 0;
            this.percent = 0;
            this.remoteFile = null;
            this.localFile = null;
            this.ftpConfig = null;
            this.listener = null;
            this.connectResultNotified = false;
            this.transferResultNotified = false;
            this.transferStarted = false;
            this.client = null;
            this.ts = System.currentTimeMillis();
            this.taskFuture = null;
            this.tmpDownloadFile = null;
        }

        /* synthetic */ TransferProfile(TransferProfile transferProfile) {
            this();
        }

        public synchronized boolean isDownload() {
            return this.isDownload;
        }

        public synchronized void setDownload(boolean isDownload) {
            this.isDownload = isDownload;
        }

        public synchronized long getCount() {
            return this.count;
        }

        public synchronized long incCount(long value) {
            this.count += value;
            this.prePercent = this.percent;
            this.percent = (int) ((((float) this.count) * 100.0f) / ((float) this.total));
            if (value < 0) {
                this.prePercent = this.percent;
            }
            return this.count;
        }

        public synchronized long getTotal() {
            return this.total;
        }

        public synchronized void setTotal(long total) {
            this.total = total;
        }

        public synchronized int getPrePercent() {
            return this.prePercent;
        }

        public synchronized int getPercent() {
            return this.percent;
        }

        public synchronized int compareGetPercent() {
            return this.percent > this.prePercent ? this.percent : -1;
        }

        public synchronized String getRemoteFile() {
            return this.remoteFile;
        }

        public synchronized void setRemoteFile(String remoteFile) {
            this.remoteFile = remoteFile;
        }

        public synchronized String getLocalFile() {
            return this.localFile;
        }

        public synchronized void setLocalFile(String localFile) {
            this.localFile = localFile;
        }

        public synchronized FtpConfig getFtpConfig() {
            return this.ftpConfig;
        }

        public synchronized void setFtpConfig(FtpConfig ftpConfig) {
            this.ftpConfig = ftpConfig;
        }

        public String getProfileKey() {
            return String.valueOf(this.ftpConfig.getSecurity()) + "://" + this.ftpConfig.getUsername() + ":" + this.ftpConfig.getPassword() + "@" + this.ftpConfig.getHost() + ":" + this.ftpConfig.getPort() + MqttTopic.TOPIC_LEVEL_SEPARATOR + this.remoteFile + "+" + this.localFile;
        }

        public synchronized TransferListener getListener() {
            return this.listener;
        }

        public synchronized void setListener(TransferListener listener) {
            this.listener = listener;
        }

        public synchronized boolean isConnectResultNotified() {
            return this.connectResultNotified;
        }

        public synchronized void setConnectResultNotified(boolean connectResultNotified) {
            this.connectResultNotified = connectResultNotified;
        }

        public synchronized boolean isTransferResultNotified() {
            return this.transferResultNotified;
        }

        public synchronized void setTransferResultNotified(boolean transferResultNotified) {
            this.transferResultNotified = transferResultNotified;
        }

        public synchronized boolean isTransferStarted() {
            return this.transferStarted;
        }

        public synchronized void setTransferStarted(boolean transferStarted) {
            this.transferStarted = transferStarted;
        }

        public synchronized FTPClient getClient() {
            return this.client;
        }

        public synchronized void setClient(FTPClient client) {
            this.client = client;
        }

        public synchronized long getTs() {
            return this.ts;
        }

        public synchronized void updateTs() {
            this.ts = System.currentTimeMillis();
        }

        public synchronized Future getTaskFuture() {
            return this.taskFuture;
        }

        public synchronized void setTaskFuture(Future taskFuture) {
            this.taskFuture = taskFuture;
        }

        public synchronized String getTmpDownloadFile() {
            return this.tmpDownloadFile;
        }

        public synchronized void setTmpDownloadFile(String tmpDownloadFile) {
            this.tmpDownloadFile = tmpDownloadFile;
        }
    }

    /* loaded from: classes.dex */
    public static class FtpConfig extends JsonBean<FtpConfig> {
        private String host = null;
        private int port = 21;
        private String username = null;
        private String password = null;
        private int security = 0;
        private boolean passiveMode = true;
        private int transferType = 0;
        private int connectTimeout = 30;
        private int readTimeout = 30;
        private int closeTimeout = 15;

        public String getHost() {
            return this.host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return this.port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return this.username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return this.password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getSecurity() {
            return this.security;
        }

        public void setSecurity(int security) {
            this.security = security;
        }

        public boolean isPassiveMode() {
            return this.passiveMode;
        }

        public void setPassiveMode(boolean passiveMode) {
            this.passiveMode = passiveMode;
        }

        public int getTransferType() {
            return this.transferType;
        }

        public void setTransferType(int transferType) {
            this.transferType = transferType;
        }

        public int getConnectTimeout() {
            return this.connectTimeout;
        }

        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public int getReadTimeout() {
            return this.readTimeout;
        }

        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }

        public int getCloseTimeout() {
            return this.closeTimeout;
        }

        public void setCloseTimeout(int closeTimeout) {
            this.closeTimeout = closeTimeout;
        }
    }

    private static String getProfileKey(String remoteFile, String localFile, FtpConfig config) {
        return String.valueOf(config.getSecurity()) + "://" + config.getUsername() + ":" + config.getPassword() + "@" + config.getHost() + ":" + config.getPort() + MqttTopic.TOPIC_LEVEL_SEPARATOR + remoteFile + "+" + localFile;
    }

    /* loaded from: classes.dex */
    public static class DownloadTask implements Runnable {
        private FTPClient client = null;
        private TransferProfile profile;

        public DownloadTask(TransferProfile profile) {
            this.profile = null;
            this.profile = profile;
        }

        public String getProfileKey() {
            return this.profile.getProfileKey();
        }

        @Override // java.lang.Runnable
        public void run() {
            final String localFile = this.profile.getLocalFile();
            String remoteFileName = this.profile.getRemoteFile();
            FtpConfig config = this.profile.getFtpConfig();
            try {
                this.client = new FTPClient();
                this.profile.setClient(this.client);
                FtpUtils.connect(this.client, config);
                this.profile.updateTs();
                if (!this.profile.isConnectResultNotified() && this.profile.getListener() != null) {
                    this.profile.getListener().onConnected();
                }
                this.profile.setConnectResultNotified(true);
                try {
                    this.client.login(config.getUsername(), config.getPassword());
                    long fileSize = this.client.fileSize(remoteFileName);
                    this.profile.setTotal(fileSize);
                    FtpUtils.createFile(this.profile.getTmpDownloadFile(), fileSize);
                    this.profile.updateTs();
                    try {
                        this.client.download(remoteFileName, new File(this.profile.getTmpDownloadFile()), new FTPDataTransferListener() { // from class: com.xcharge.common.utils.FtpUtils.DownloadTask.1
                            {
                                DownloadTask.this = this;
                            }

                            @Override // it.sauronsoftware.ftp4j.FTPDataTransferListener
                            public void aborted() {
                                Log.w("FtpUtils.DownloadTask", "download aborted: " + DownloadTask.this.profile.getCount() + ", " + DownloadTask.this.getProfileKey());
                                if (!DownloadTask.this.profile.isTransferResultNotified() && DownloadTask.this.profile.getListener() != null) {
                                    DownloadTask.this.profile.getListener().onTransferFail();
                                }
                                DownloadTask.this.profile.setTransferResultNotified(true);
                                FileUtils.deleteFile(DownloadTask.this.profile.getTmpDownloadFile());
                                FtpUtils.disconnect(DownloadTask.this.client, true);
                                FtpUtils.transferProfiles.remove(DownloadTask.this.profile.getProfileKey());
                            }

                            @Override // it.sauronsoftware.ftp4j.FTPDataTransferListener
                            public void completed() {
                                Log.i("FtpUtils.DownloadTask", "download completed: " + DownloadTask.this.profile.getCount() + ", " + DownloadTask.this.getProfileKey());
                                if (!FtpUtils.renameDownloadedFile(DownloadTask.this.profile.getTmpDownloadFile(), localFile)) {
                                    Log.w("FtpUtils.DownloadTask", "failed to rename from " + DownloadTask.this.profile.getTmpDownloadFile() + " to " + localFile + ", " + DownloadTask.this.getProfileKey());
                                }
                                if (!DownloadTask.this.profile.isTransferResultNotified() && DownloadTask.this.profile.getListener() != null) {
                                    DownloadTask.this.profile.getListener().onTransferComplete();
                                }
                                DownloadTask.this.profile.setTransferResultNotified(true);
                                FtpUtils.disconnect(DownloadTask.this.client, false);
                                FtpUtils.transferProfiles.remove(DownloadTask.this.profile.getProfileKey());
                            }

                            @Override // it.sauronsoftware.ftp4j.FTPDataTransferListener
                            public void failed() {
                                Log.w("FtpUtils.DownloadTask", "download failed: " + DownloadTask.this.profile.getCount() + ", " + DownloadTask.this.getProfileKey());
                                if (!DownloadTask.this.profile.isTransferResultNotified() && DownloadTask.this.profile.getListener() != null) {
                                    DownloadTask.this.profile.getListener().onTransferFail();
                                }
                                DownloadTask.this.profile.setTransferResultNotified(true);
                                FileUtils.deleteFile(DownloadTask.this.profile.getTmpDownloadFile());
                                FtpUtils.disconnect(DownloadTask.this.client, true);
                                FtpUtils.transferProfiles.remove(DownloadTask.this.profile.getProfileKey());
                            }

                            @Override // it.sauronsoftware.ftp4j.FTPDataTransferListener
                            public void started() {
                                Log.i("FtpUtils.DownloadTask", "download started: " + DownloadTask.this.getProfileKey());
                                DownloadTask.this.profile.setTransferStarted(true);
                            }

                            @Override // it.sauronsoftware.ftp4j.FTPDataTransferListener
                            public void transferred(int length) {
                                DownloadTask.this.profile.incCount(length);
                                DownloadTask.this.profile.updateTs();
                                int percent = DownloadTask.this.profile.compareGetPercent();
                                if (percent > 0) {
                                    Log.i("FtpUtils.DownloadTask", "downloading ...: " + percent + "%");
                                    if (DownloadTask.this.profile.getListener() != null) {
                                        DownloadTask.this.profile.getListener().onTransferPercentage(percent);
                                    }
                                }
                                if (DownloadTask.this.profile.getListener() != null) {
                                    DownloadTask.this.profile.getListener().onTransferPercentage(DownloadTask.this.profile.getCount(), DownloadTask.this.profile.getTotal());
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.e("FtpUtils.DownloadTask", Log.getStackTraceString(e));
                        if (!this.profile.isTransferResultNotified() && this.profile.getListener() != null) {
                            this.profile.getListener().onTransferFail();
                        }
                        this.profile.setTransferResultNotified(true);
                        FtpUtils.disconnect(this.client, true);
                        FtpUtils.transferProfiles.remove(this.profile.getProfileKey());
                    }
                } catch (Exception e2) {
                    Log.e("FtpUtils.DownloadTask", Log.getStackTraceString(e2));
                    FtpUtils.disconnect(this.client, true);
                    if (this.profile.getListener() != null) {
                        this.profile.getListener().onConnectFail();
                    }
                    this.profile.setConnectResultNotified(true);
                    FtpUtils.transferProfiles.remove(this.profile.getProfileKey());
                }
            } catch (Exception e3) {
                Log.e("FtpUtils.DownloadTask", Log.getStackTraceString(e3));
                if (!this.profile.isConnectResultNotified() && this.profile.getListener() != null) {
                    this.profile.getListener().onConnectFail();
                }
                this.profile.setConnectResultNotified(true);
                FtpUtils.transferProfiles.remove(this.profile.getProfileKey());
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class UploadTask implements Runnable {
        private TransferProfile profile;
        private FTPClient client = null;
        private String remoteFileName = null;
        private String localFileName = null;

        public UploadTask(TransferProfile profile) {
            this.profile = null;
            this.profile = profile;
        }

        public String getProfileKey() {
            return this.profile.getProfileKey();
        }

        @Override // java.lang.Runnable
        public void run() {
            String localFileSeparator = File.separator;
            if ("\\".equals(localFileSeparator)) {
                localFileSeparator = "\\\\";
            }
            String localFile = this.profile.getLocalFile();
            if (!localFile.endsWith(File.separator)) {
                String[] localFileSplit = localFile.split(localFileSeparator);
                if (localFileSplit.length == 1) {
                    this.localFileName = localFile;
                } else {
                    this.localFileName = localFileSplit[localFileSplit.length - 1];
                }
            }
            String remoteDir = null;
            String remoteFile = this.profile.getRemoteFile();
            if (!TextUtils.isEmpty(remoteFile)) {
                if (remoteFile.endsWith(MqttTopic.TOPIC_LEVEL_SEPARATOR)) {
                    remoteDir = remoteFile;
                } else {
                    String[] remoteFileSplit = remoteFile.split(MqttTopic.TOPIC_LEVEL_SEPARATOR);
                    if (remoteFileSplit.length == 1) {
                        this.remoteFileName = remoteFile;
                    } else {
                        this.remoteFileName = remoteFileSplit[remoteFileSplit.length - 1];
                        remoteDir = remoteFile.substring(0, remoteFile.length() - this.remoteFileName.length());
                    }
                }
            }
            FtpConfig config = this.profile.getFtpConfig();
            try {
                this.client = new FTPClient();
                this.profile.setClient(this.client);
                FtpUtils.connect(this.client, config);
                this.profile.updateTs();
                if (!this.profile.isConnectResultNotified() && this.profile.getListener() != null) {
                    this.profile.getListener().onConnected();
                }
                this.profile.setConnectResultNotified(true);
                try {
                    this.client.login(config.getUsername(), config.getPassword());
                    long fileSize = new File(localFile).length();
                    this.profile.setTotal(fileSize);
                    if (!TextUtils.isEmpty(remoteDir)) {
                        this.client.changeDirectory(remoteDir);
                    }
                    this.profile.updateTs();
                    try {
                        this.client.upload(new File(localFile), new FTPDataTransferListener() { // from class: com.xcharge.common.utils.FtpUtils.UploadTask.1
                            {
                                UploadTask.this = this;
                            }

                            @Override // it.sauronsoftware.ftp4j.FTPDataTransferListener
                            public void aborted() {
                                Log.w("FtpUtils.UploadTask", "upload aborted: " + UploadTask.this.profile.getCount() + ", " + UploadTask.this.getProfileKey());
                                if (!UploadTask.this.profile.isTransferResultNotified() && UploadTask.this.profile.getListener() != null) {
                                    UploadTask.this.profile.getListener().onTransferFail();
                                }
                                UploadTask.this.profile.setTransferResultNotified(true);
                                FtpUtils.disconnect(UploadTask.this.client, true);
                                FtpUtils.transferProfiles.remove(UploadTask.this.profile.getProfileKey());
                            }

                            @Override // it.sauronsoftware.ftp4j.FTPDataTransferListener
                            public void completed() {
                                Log.i("FtpUtils.UploadTask", "upload completed: " + UploadTask.this.profile.getCount() + ", " + UploadTask.this.getProfileKey());
                                if (!TextUtils.isEmpty(UploadTask.this.remoteFileName)) {
                                    try {
                                        UploadTask.this.client.rename(UploadTask.this.localFileName, UploadTask.this.remoteFileName);
                                    } catch (Exception e) {
                                        Log.w("FtpUtils.UploadTask", "failed to rename from " + UploadTask.this.localFileName + " to " + UploadTask.this.remoteFileName + ", " + UploadTask.this.getProfileKey() + ", except: " + Log.getStackTraceString(e));
                                    }
                                }
                                if (!UploadTask.this.profile.isTransferResultNotified() && UploadTask.this.profile.getListener() != null) {
                                    UploadTask.this.profile.getListener().onTransferComplete();
                                }
                                UploadTask.this.profile.setTransferResultNotified(true);
                                FtpUtils.disconnect(UploadTask.this.client, false);
                                FtpUtils.transferProfiles.remove(UploadTask.this.profile.getProfileKey());
                            }

                            @Override // it.sauronsoftware.ftp4j.FTPDataTransferListener
                            public void failed() {
                                Log.w("FtpUtils.UploadTask", "upload failed: " + UploadTask.this.profile.getCount() + ", " + UploadTask.this.getProfileKey());
                                if (!UploadTask.this.profile.isTransferResultNotified() && UploadTask.this.profile.getListener() != null) {
                                    UploadTask.this.profile.getListener().onTransferFail();
                                }
                                UploadTask.this.profile.setTransferResultNotified(true);
                                FtpUtils.disconnect(UploadTask.this.client, true);
                                FtpUtils.transferProfiles.remove(UploadTask.this.profile.getProfileKey());
                            }

                            @Override // it.sauronsoftware.ftp4j.FTPDataTransferListener
                            public void started() {
                                Log.i("FtpUtils.UploadTask", "upload started: " + UploadTask.this.getProfileKey());
                                UploadTask.this.profile.setTransferStarted(true);
                            }

                            @Override // it.sauronsoftware.ftp4j.FTPDataTransferListener
                            public void transferred(int length) {
                                UploadTask.this.profile.incCount(length);
                                UploadTask.this.profile.updateTs();
                                int percent = UploadTask.this.profile.compareGetPercent();
                                if (percent > 0) {
                                    Log.i("FtpUtils.UploadTask", "uploading ...: " + percent + "%");
                                    if (UploadTask.this.profile.getListener() != null) {
                                        UploadTask.this.profile.getListener().onTransferPercentage(percent);
                                    }
                                }
                                if (UploadTask.this.profile.getListener() != null) {
                                    UploadTask.this.profile.getListener().onTransferPercentage(UploadTask.this.profile.getCount(), UploadTask.this.profile.getTotal());
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.e("FtpUtils.UploadTask", Log.getStackTraceString(e));
                        if (!this.profile.isTransferResultNotified() && this.profile.getListener() != null) {
                            this.profile.getListener().onTransferFail();
                        }
                        this.profile.setTransferResultNotified(true);
                        if (this.client == null) {
                            return;
                        }
                        FtpUtils.disconnect(this.client, true);
                        FtpUtils.transferProfiles.remove(this.profile.getProfileKey());
                    }
                } catch (Exception e2) {
                    Log.e("FtpUtils.UploadTask", Log.getStackTraceString(e2));
                    FtpUtils.disconnect(this.client, true);
                    if (this.profile.getListener() != null) {
                        this.profile.getListener().onConnectFail();
                    }
                    this.profile.setConnectResultNotified(true);
                    FtpUtils.transferProfiles.remove(this.profile.getProfileKey());
                }
            } catch (Exception e3) {
                Log.e("FtpUtils.UploadTask", Log.getStackTraceString(e3));
                if (!this.profile.isConnectResultNotified() && this.profile.getListener() != null) {
                    this.profile.getListener().onConnectFail();
                }
                this.profile.setConnectResultNotified(true);
                FtpUtils.transferProfiles.remove(this.profile.getProfileKey());
            }
        }
    }

    public static boolean connect(FTPClient client, FtpConfig config) throws Exception {
        client.setPassive(config.passiveMode);
        client.setType(config.transferType);
        client.getConnector().setConnectionTimeout(config.getConnectTimeout());
        client.getConnector().setReadTimeout(config.getReadTimeout());
        client.getConnector().setCloseTimeout(config.getCloseTimeout());
        Log.d("FtpUtils.connect", "ftp connect begin: " + System.currentTimeMillis());
        String[] reply = client.connect(config.getHost(), config.getPort());
        Log.d("FtpUtils.connect", "ftp connect end: " + System.currentTimeMillis());
        if (reply != null) {
            for (String value : reply) {
                Log.d("FtpUtils.connect", "ftp connect reply: " + value);
            }
            return true;
        }
        return true;
    }

    public static void closeSocket(final FTPClient client) {
        Log.d("FtpUtils.closeSocket", "now active ftp cmd task count: " + cmdThreadPoolExecutor.getActiveCount());
        cmdThreadPoolExecutor.execute(new Runnable() { // from class: com.xcharge.common.utils.FtpUtils.3
            @Override // java.lang.Runnable
            public void run() {
                try {
                    client.abortCurrentConnectionAttempt();
                } catch (Exception e) {
                    Log.e("FtpUtils.closeSocket", Log.getStackTraceString(e));
                }
            }
        });
    }

    public static void interrupt(final FTPClient client, final boolean isForce) {
        Log.d("FtpUtils.interrupt", "now active ftp cmd task count: " + cmdThreadPoolExecutor.getActiveCount());
        cmdThreadPoolExecutor.execute(new Runnable() { // from class: com.xcharge.common.utils.FtpUtils.4
            @Override // java.lang.Runnable
            public void run() {
                try {
                    client.abortCurrentDataTransfer(!isForce);
                } catch (Exception e) {
                    Log.e("FtpUtils.interrupt", Log.getStackTraceString(e));
                }
            }
        });
    }

    public static void disconnect(final FTPClient client, final boolean isForce) {
        Log.d("FtpUtils.disconnect", "now active ftp cmd task count: " + cmdThreadPoolExecutor.getActiveCount());
        cmdThreadPoolExecutor.execute(new Runnable() { // from class: com.xcharge.common.utils.FtpUtils.5
            @Override // java.lang.Runnable
            public void run() {
                try {
                    if (client.isConnected()) {
                        client.disconnect(!isForce);
                    }
                } catch (Exception e) {
                    Log.e("FtpUtils.disconnect", Log.getStackTraceString(e));
                }
            }
        });
    }

    public static void createFile(String fileName, long fileSize) throws Exception {
        try {
            File newFile = new File(fileName);
            RandomAccessFile raf = new RandomAccessFile(newFile, "rw");
            raf.setLength(fileSize);
            raf.close();
        } catch (Exception e) {
            Log.e("FtpUtils.createFile", "Exception stack: " + Log.getStackTraceString(e) + ", fileName: " + fileName + ", fileSize: " + fileSize);
            throw e;
        }
    }

    public static boolean renameDownloadedFile(String tmpFile, String toFile) {
        File file = new File(tmpFile);
        if (file.exists()) {
            boolean isOk = file.renameTo(new File(toFile));
            if (!isOk) {
                long len = FileUtils.fileChannelCopy(new File(tmpFile), new File(toFile));
                if (len > 0) {
                    FileUtils.deleteFile(tmpFile);
                    return true;
                }
                return isOk;
            }
            return isOk;
        }
        return false;
    }

    public static void download(String remoteFile, String localFile, FtpConfig config, TransferListener listener) {
        try {
            String profileKey = getProfileKey(remoteFile, localFile, config);
            TransferProfile profile = transferProfiles.remove(profileKey);
            if (profile != null && profile.getClient() != null) {
                if (profile.isTransferStarted() || profile.getCount() > 0) {
                    interrupt(profile.getClient(), true);
                } else {
                    disconnect(profile.getClient(), true);
                }
            }
            TransferProfile downloadProfile = new TransferProfile(null);
            downloadProfile.setDownload(true);
            downloadProfile.setRemoteFile(remoteFile);
            downloadProfile.setLocalFile(localFile);
            downloadProfile.setTmpDownloadFile(String.valueOf(localFile) + "." + System.currentTimeMillis());
            downloadProfile.setFtpConfig(config);
            downloadProfile.setListener(listener);
            transferProfiles.put(profileKey, downloadProfile);
            Log.d("FtpUtils.download", "now active ftp transfer task count: " + transferThreadPoolExecutor.getActiveCount());
            Future future = transferThreadPoolExecutor.submit(new DownloadTask(downloadProfile));
            downloadProfile.setTaskFuture(future);
        } catch (Exception e) {
            Log.e("FtpUtils.download", Log.getStackTraceString(e));
        }
    }

    public static void upload(String localFile, String remoteFile, FtpConfig config, TransferListener listener) {
        try {
            String profileKey = getProfileKey(remoteFile, localFile, config);
            TransferProfile profile = transferProfiles.remove(profileKey);
            if (profile != null && profile.getClient() != null) {
                if (profile.isTransferStarted() || profile.getCount() > 0) {
                    interrupt(profile.getClient(), true);
                } else {
                    disconnect(profile.getClient(), true);
                }
            }
            TransferProfile uploadProfile = new TransferProfile(null);
            uploadProfile.setDownload(false);
            uploadProfile.setRemoteFile(remoteFile);
            uploadProfile.setLocalFile(localFile);
            uploadProfile.setFtpConfig(config);
            uploadProfile.setListener(listener);
            transferProfiles.put(profileKey, uploadProfile);
            Log.d("FtpUtils.upload", "now active ftp transfer task count: " + transferThreadPoolExecutor.getActiveCount());
            Future future = transferThreadPoolExecutor.submit(new UploadTask(uploadProfile));
            uploadProfile.setTaskFuture(future);
        } catch (Exception e) {
            Log.e("FtpUtils.upload", Log.getStackTraceString(e));
        }
    }
}
