package com.xcharge.common.utils;

import android.text.TextUtils;
import android.util.Log;
import com.xcharge.common.bean.JsonBean;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import p010it.sauronsoftware.ftp4j.FTPClient;
import p010it.sauronsoftware.ftp4j.FTPDataTransferListener;

public class FtpUtils {
    private static ThreadPoolExecutor cmdThreadPoolExecutor;
    private static DeadTaskMonitorThread deadTaskMonitorThread;
    /* access modifiers changed from: private */
    public static ConcurrentHashMap<String, TransferProfile> transferProfiles;
    private static ThreadPoolExecutor transferThreadPoolExecutor;

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
        transferThreadPoolExecutor = new ThreadPoolExecutor(3, 3, 30, TimeUnit.SECONDS, new LinkedBlockingQueue(), new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                Log.w("FtpUtils.transferThreadPoolExecutor", "rejected runnable: " + r.toString() + ", active runnables: " + executor.getActiveCount());
                if (r instanceof DownloadTask) {
                    FtpUtils.transferProfiles.remove(((DownloadTask) r).getProfileKey());
                } else if (r instanceof UploadTask) {
                    FtpUtils.transferProfiles.remove(((UploadTask) r).getProfileKey());
                }
            }
        });
        transferThreadPoolExecutor.allowCoreThreadTimeOut(true);
        cmdThreadPoolExecutor = new ThreadPoolExecutor(3, 3, 30, TimeUnit.SECONDS, new LinkedBlockingQueue(), new RejectedExecutionHandler() {
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
        if (newNum == nowNum) {
            return true;
        }
        transferThreadPoolExecutor.setCorePoolSize(newNum);
        Log.w("FtpUtils.changeTransferCoreThreadNum", "change transfer thread pool core number to: " + newNum);
        return true;
    }

    private static class DeadTaskMonitorThread extends Thread {
        private volatile boolean shutdown = false;
        private final ThreadPoolExecutor threadPoolExecutor;
        private final ConcurrentHashMap<String, TransferProfile> transferProfiles;

        public DeadTaskMonitorThread(ThreadPoolExecutor threadPoolExecutor2, ConcurrentHashMap<String, TransferProfile> transferProfiles2) {
            this.threadPoolExecutor = threadPoolExecutor2;
            this.transferProfiles = transferProfiles2;
        }

        /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r14 = this;
            L_0x0000:
                boolean r10 = r14.shutdown     // Catch:{ Exception -> 0x002c }
                if (r10 == 0) goto L_0x0005
            L_0x0004:
                return
            L_0x0005:
                monitor-enter(r14)     // Catch:{ Exception -> 0x002c }
                r10 = 30000(0x7530, double:1.4822E-319)
                r14.wait(r10)     // Catch:{ all -> 0x0029 }
                long r8 = java.lang.System.currentTimeMillis()     // Catch:{ all -> 0x0029 }
                java.util.HashMap r1 = new java.util.HashMap     // Catch:{ all -> 0x0029 }
                r1.<init>()     // Catch:{ all -> 0x0029 }
                java.util.concurrent.ConcurrentHashMap<java.lang.String, com.xcharge.common.utils.FtpUtils$TransferProfile> r10 = r14.transferProfiles     // Catch:{ all -> 0x0029 }
                r1.putAll(r10)     // Catch:{ all -> 0x0029 }
                java.util.Set r10 = r1.entrySet()     // Catch:{ all -> 0x0029 }
                java.util.Iterator r6 = r10.iterator()     // Catch:{ all -> 0x0029 }
            L_0x0021:
                boolean r10 = r6.hasNext()     // Catch:{ all -> 0x0029 }
                if (r10 != 0) goto L_0x0037
                monitor-exit(r14)     // Catch:{ all -> 0x0029 }
                goto L_0x0000
            L_0x0029:
                r10 = move-exception
                monitor-exit(r14)     // Catch:{ all -> 0x0029 }
                throw r10     // Catch:{ Exception -> 0x002c }
            L_0x002c:
                r3 = move-exception
                java.lang.String r10 = "FtpUtils.DeadTaskMonitorThread"
                java.lang.String r11 = android.util.Log.getStackTraceString(r3)
                android.util.Log.w(r10, r11)
                goto L_0x0004
            L_0x0037:
                java.lang.Object r2 = r6.next()     // Catch:{ all -> 0x0029 }
                java.util.Map$Entry r2 = (java.util.Map.Entry) r2     // Catch:{ all -> 0x0029 }
                java.lang.Object r7 = r2.getValue()     // Catch:{ all -> 0x0029 }
                com.xcharge.common.utils.FtpUtils$TransferProfile r7 = (com.xcharge.common.utils.FtpUtils.TransferProfile) r7     // Catch:{ all -> 0x0029 }
                it.sauronsoftware.ftp4j.FTPClient r0 = r7.getClient()     // Catch:{ all -> 0x0029 }
                if (r0 == 0) goto L_0x0021
                long r10 = r7.getTotal()     // Catch:{ all -> 0x0029 }
                r12 = 0
                int r10 = (r10 > r12 ? 1 : (r10 == r12 ? 0 : -1))
                if (r10 == 0) goto L_0x005f
                long r10 = r7.getCount()     // Catch:{ all -> 0x0029 }
                long r12 = r7.getTotal()     // Catch:{ all -> 0x0029 }
                int r10 = (r10 > r12 ? 1 : (r10 == r12 ? 0 : -1))
                if (r10 >= 0) goto L_0x0021
            L_0x005f:
                long r10 = r7.getTs()     // Catch:{ all -> 0x0029 }
                long r10 = r8 - r10
                r12 = 120000(0x1d4c0, double:5.9288E-319)
                int r10 = (r10 > r12 ? 1 : (r10 == r12 ? 0 : -1))
                if (r10 < 0) goto L_0x0021
                java.lang.String r10 = "FtpUtils.DeadTaskMonitorThread"
                java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ all -> 0x0029 }
                java.lang.String r12 = "clear dead transfer task: "
                r11.<init>(r12)     // Catch:{ all -> 0x0029 }
                java.lang.String r12 = r7.getProfileKey()     // Catch:{ all -> 0x0029 }
                java.lang.StringBuilder r11 = r11.append(r12)     // Catch:{ all -> 0x0029 }
                java.lang.String r11 = r11.toString()     // Catch:{ all -> 0x0029 }
                android.util.Log.w(r10, r11)     // Catch:{ all -> 0x0029 }
                boolean r5 = r0.isConnected()     // Catch:{ all -> 0x0029 }
                if (r5 == 0) goto L_0x0105
                boolean r10 = r7.isTransferStarted()     // Catch:{ all -> 0x0029 }
                if (r10 != 0) goto L_0x009a
                long r10 = r7.getCount()     // Catch:{ all -> 0x0029 }
                r12 = 0
                int r10 = (r10 > r12 ? 1 : (r10 == r12 ? 0 : -1))
                if (r10 <= 0) goto L_0x0100
            L_0x009a:
                r10 = 1
                com.xcharge.common.utils.FtpUtils.interrupt(r0, r10)     // Catch:{ all -> 0x0029 }
            L_0x009e:
                java.util.concurrent.Future r4 = r7.getTaskFuture()     // Catch:{ all -> 0x0029 }
                boolean r10 = r4.isDone()     // Catch:{ all -> 0x0029 }
                if (r10 != 0) goto L_0x00ac
                r10 = 1
                r4.cancel(r10)     // Catch:{ all -> 0x0029 }
            L_0x00ac:
                java.util.concurrent.ThreadPoolExecutor r10 = r14.threadPoolExecutor     // Catch:{ all -> 0x0029 }
                r10.purge()     // Catch:{ all -> 0x0029 }
                com.xcharge.common.utils.FtpUtils$TransferListener r10 = r7.getListener()     // Catch:{ all -> 0x0029 }
                if (r10 == 0) goto L_0x00ca
                if (r5 == 0) goto L_0x0109
                boolean r10 = r7.isTransferResultNotified()     // Catch:{ all -> 0x0029 }
                if (r10 != 0) goto L_0x00c6
                com.xcharge.common.utils.FtpUtils$TransferListener r10 = r7.getListener()     // Catch:{ all -> 0x0029 }
                r10.onTransferFail()     // Catch:{ all -> 0x0029 }
            L_0x00c6:
                r10 = 1
                r7.setTransferResultNotified(r10)     // Catch:{ all -> 0x0029 }
            L_0x00ca:
                boolean r10 = r7.isDownload()     // Catch:{ all -> 0x0029 }
                if (r10 == 0) goto L_0x00d7
                java.lang.String r10 = r7.getTmpDownloadFile()     // Catch:{ all -> 0x0029 }
                com.xcharge.common.utils.FileUtils.deleteFile(r10)     // Catch:{ all -> 0x0029 }
            L_0x00d7:
                java.util.concurrent.ConcurrentHashMap<java.lang.String, com.xcharge.common.utils.FtpUtils$TransferProfile> r10 = r14.transferProfiles     // Catch:{ all -> 0x0029 }
                java.lang.String r11 = r7.getProfileKey()     // Catch:{ all -> 0x0029 }
                r10.remove(r11)     // Catch:{ all -> 0x0029 }
                java.lang.String r10 = "FtpUtils.DeadTaskMonitorThread"
                java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ all -> 0x0029 }
                java.lang.String r12 = "dead transfer task: "
                r11.<init>(r12)     // Catch:{ all -> 0x0029 }
                java.lang.String r12 = r7.getProfileKey()     // Catch:{ all -> 0x0029 }
                java.lang.StringBuilder r11 = r11.append(r12)     // Catch:{ all -> 0x0029 }
                java.lang.String r12 = " has been forced termination"
                java.lang.StringBuilder r11 = r11.append(r12)     // Catch:{ all -> 0x0029 }
                java.lang.String r11 = r11.toString()     // Catch:{ all -> 0x0029 }
                android.util.Log.i(r10, r11)     // Catch:{ all -> 0x0029 }
                goto L_0x0021
            L_0x0100:
                r10 = 1
                com.xcharge.common.utils.FtpUtils.disconnect(r0, r10)     // Catch:{ all -> 0x0029 }
                goto L_0x009e
            L_0x0105:
                com.xcharge.common.utils.FtpUtils.closeSocket(r0)     // Catch:{ all -> 0x0029 }
                goto L_0x009e
            L_0x0109:
                boolean r10 = r7.isConnectResultNotified()     // Catch:{ all -> 0x0029 }
                if (r10 != 0) goto L_0x0116
                com.xcharge.common.utils.FtpUtils$TransferListener r10 = r7.getListener()     // Catch:{ all -> 0x0029 }
                r10.onConnectFail()     // Catch:{ all -> 0x0029 }
            L_0x0116:
                r10 = 1
                r7.setConnectResultNotified(r10)     // Catch:{ all -> 0x0029 }
                goto L_0x00ca
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.common.utils.FtpUtils.DeadTaskMonitorThread.run():void");
        }

        public void shutdown() {
            this.shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }

    private static class TransferProfile {
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

        /* renamed from: ts */
        private long f135ts;

        private TransferProfile() {
            this.isDownload = true;
            this.count = 0;
            this.total = 0;
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
            this.f135ts = System.currentTimeMillis();
            this.taskFuture = null;
            this.tmpDownloadFile = null;
        }

        /* synthetic */ TransferProfile(TransferProfile transferProfile) {
            this();
        }

        public synchronized boolean isDownload() {
            return this.isDownload;
        }

        public synchronized void setDownload(boolean isDownload2) {
            this.isDownload = isDownload2;
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

        public synchronized void setTotal(long total2) {
            this.total = total2;
        }

        public synchronized int getPrePercent() {
            return this.prePercent;
        }

        public synchronized int getPercent() {
            return this.percent;
        }

        public synchronized int compareGetPercent() {
            int i;
            if (this.percent > this.prePercent) {
                i = this.percent;
            } else {
                i = -1;
            }
            return i;
        }

        public synchronized String getRemoteFile() {
            return this.remoteFile;
        }

        public synchronized void setRemoteFile(String remoteFile2) {
            this.remoteFile = remoteFile2;
        }

        public synchronized String getLocalFile() {
            return this.localFile;
        }

        public synchronized void setLocalFile(String localFile2) {
            this.localFile = localFile2;
        }

        public synchronized FtpConfig getFtpConfig() {
            return this.ftpConfig;
        }

        public synchronized void setFtpConfig(FtpConfig ftpConfig2) {
            this.ftpConfig = ftpConfig2;
        }

        public String getProfileKey() {
            return String.valueOf(this.ftpConfig.getSecurity()) + "://" + this.ftpConfig.getUsername() + ":" + this.ftpConfig.getPassword() + "@" + this.ftpConfig.getHost() + ":" + this.ftpConfig.getPort() + MqttTopic.TOPIC_LEVEL_SEPARATOR + this.remoteFile + "+" + this.localFile;
        }

        public synchronized TransferListener getListener() {
            return this.listener;
        }

        public synchronized void setListener(TransferListener listener2) {
            this.listener = listener2;
        }

        public synchronized boolean isConnectResultNotified() {
            return this.connectResultNotified;
        }

        public synchronized void setConnectResultNotified(boolean connectResultNotified2) {
            this.connectResultNotified = connectResultNotified2;
        }

        public synchronized boolean isTransferResultNotified() {
            return this.transferResultNotified;
        }

        public synchronized void setTransferResultNotified(boolean transferResultNotified2) {
            this.transferResultNotified = transferResultNotified2;
        }

        public synchronized boolean isTransferStarted() {
            return this.transferStarted;
        }

        public synchronized void setTransferStarted(boolean transferStarted2) {
            this.transferStarted = transferStarted2;
        }

        public synchronized FTPClient getClient() {
            return this.client;
        }

        public synchronized void setClient(FTPClient client2) {
            this.client = client2;
        }

        public synchronized long getTs() {
            return this.f135ts;
        }

        public synchronized void updateTs() {
            this.f135ts = System.currentTimeMillis();
        }

        public synchronized Future getTaskFuture() {
            return this.taskFuture;
        }

        public synchronized void setTaskFuture(Future taskFuture2) {
            this.taskFuture = taskFuture2;
        }

        public synchronized String getTmpDownloadFile() {
            return this.tmpDownloadFile;
        }

        public synchronized void setTmpDownloadFile(String tmpDownloadFile2) {
            this.tmpDownloadFile = tmpDownloadFile2;
        }
    }

    public static class FtpConfig extends JsonBean<FtpConfig> {
        private int closeTimeout = 15;
        private int connectTimeout = 30;
        private String host = null;
        /* access modifiers changed from: private */
        public boolean passiveMode = true;
        private String password = null;
        private int port = 21;
        private int readTimeout = 30;
        private int security = 0;
        /* access modifiers changed from: private */
        public int transferType = 0;
        private String username = null;

        public String getHost() {
            return this.host;
        }

        public void setHost(String host2) {
            this.host = host2;
        }

        public int getPort() {
            return this.port;
        }

        public void setPort(int port2) {
            this.port = port2;
        }

        public String getUsername() {
            return this.username;
        }

        public void setUsername(String username2) {
            this.username = username2;
        }

        public String getPassword() {
            return this.password;
        }

        public void setPassword(String password2) {
            this.password = password2;
        }

        public int getSecurity() {
            return this.security;
        }

        public void setSecurity(int security2) {
            this.security = security2;
        }

        public boolean isPassiveMode() {
            return this.passiveMode;
        }

        public void setPassiveMode(boolean passiveMode2) {
            this.passiveMode = passiveMode2;
        }

        public int getTransferType() {
            return this.transferType;
        }

        public void setTransferType(int transferType2) {
            this.transferType = transferType2;
        }

        public int getConnectTimeout() {
            return this.connectTimeout;
        }

        public void setConnectTimeout(int connectTimeout2) {
            this.connectTimeout = connectTimeout2;
        }

        public int getReadTimeout() {
            return this.readTimeout;
        }

        public void setReadTimeout(int readTimeout2) {
            this.readTimeout = readTimeout2;
        }

        public int getCloseTimeout() {
            return this.closeTimeout;
        }

        public void setCloseTimeout(int closeTimeout2) {
            this.closeTimeout = closeTimeout2;
        }
    }

    private static String getProfileKey(String remoteFile, String localFile, FtpConfig config) {
        return String.valueOf(config.getSecurity()) + "://" + config.getUsername() + ":" + config.getPassword() + "@" + config.getHost() + ":" + config.getPort() + MqttTopic.TOPIC_LEVEL_SEPARATOR + remoteFile + "+" + localFile;
    }

    private static class DownloadTask implements Runnable {
        /* access modifiers changed from: private */
        public FTPClient client = null;
        /* access modifiers changed from: private */
        public TransferProfile profile = null;

        public DownloadTask(TransferProfile profile2) {
            this.profile = profile2;
        }

        public String getProfileKey() {
            return this.profile.getProfileKey();
        }

        public void run() {
            final String localFile = this.profile.getLocalFile();
            String remoteFileName = this.profile.getRemoteFile();
            FtpConfig config = this.profile.getFtpConfig();
            try {
                this.client = new FTPClient();
                this.profile.setClient(this.client);
                boolean unused = FtpUtils.connect(this.client, config);
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
                        this.client.download(remoteFileName, new File(this.profile.getTmpDownloadFile()), (FTPDataTransferListener) new FTPDataTransferListener() {
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

                            public void started() {
                                Log.i("FtpUtils.DownloadTask", "download started: " + DownloadTask.this.getProfileKey());
                                DownloadTask.this.profile.setTransferStarted(true);
                            }

                            public void transferred(int length) {
                                DownloadTask.this.profile.incCount((long) length);
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

    private static class UploadTask implements Runnable {
        /* access modifiers changed from: private */
        public FTPClient client = null;
        /* access modifiers changed from: private */
        public String localFileName = null;
        /* access modifiers changed from: private */
        public TransferProfile profile = null;
        /* access modifiers changed from: private */
        public String remoteFileName = null;

        public UploadTask(TransferProfile profile2) {
            this.profile = profile2;
        }

        public String getProfileKey() {
            return this.profile.getProfileKey();
        }

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
                boolean unused = FtpUtils.connect(this.client, config);
                this.profile.updateTs();
                if (!this.profile.isConnectResultNotified() && this.profile.getListener() != null) {
                    this.profile.getListener().onConnected();
                }
                this.profile.setConnectResultNotified(true);
                try {
                    this.client.login(config.getUsername(), config.getPassword());
                    this.profile.setTotal(new File(localFile).length());
                    if (!TextUtils.isEmpty(remoteDir)) {
                        this.client.changeDirectory(remoteDir);
                    }
                    this.profile.updateTs();
                    try {
                        this.client.upload(new File(localFile), (FTPDataTransferListener) new FTPDataTransferListener() {
                            public void aborted() {
                                Log.w("FtpUtils.UploadTask", "upload aborted: " + UploadTask.this.profile.getCount() + ", " + UploadTask.this.getProfileKey());
                                if (!UploadTask.this.profile.isTransferResultNotified() && UploadTask.this.profile.getListener() != null) {
                                    UploadTask.this.profile.getListener().onTransferFail();
                                }
                                UploadTask.this.profile.setTransferResultNotified(true);
                                FtpUtils.disconnect(UploadTask.this.client, true);
                                FtpUtils.transferProfiles.remove(UploadTask.this.profile.getProfileKey());
                            }

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

                            public void failed() {
                                Log.w("FtpUtils.UploadTask", "upload failed: " + UploadTask.this.profile.getCount() + ", " + UploadTask.this.getProfileKey());
                                if (!UploadTask.this.profile.isTransferResultNotified() && UploadTask.this.profile.getListener() != null) {
                                    UploadTask.this.profile.getListener().onTransferFail();
                                }
                                UploadTask.this.profile.setTransferResultNotified(true);
                                FtpUtils.disconnect(UploadTask.this.client, true);
                                FtpUtils.transferProfiles.remove(UploadTask.this.profile.getProfileKey());
                            }

                            public void started() {
                                Log.i("FtpUtils.UploadTask", "upload started: " + UploadTask.this.getProfileKey());
                                UploadTask.this.profile.setTransferStarted(true);
                            }

                            public void transferred(int length) {
                                UploadTask.this.profile.incCount((long) length);
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
                        if (this.client != null) {
                            FtpUtils.disconnect(this.client, true);
                            FtpUtils.transferProfiles.remove(this.profile.getProfileKey());
                        }
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

    /* access modifiers changed from: private */
    public static boolean connect(FTPClient client, FtpConfig config) throws Exception {
        client.setPassive(config.passiveMode);
        client.setType(config.transferType);
        client.getConnector().setConnectionTimeout(config.getConnectTimeout());
        client.getConnector().setReadTimeout(config.getReadTimeout());
        client.getConnector().setCloseTimeout(config.getCloseTimeout());
        Log.d("FtpUtils.connect", "ftp connect begin: " + System.currentTimeMillis());
        String[] reply = client.connect(config.getHost(), config.getPort());
        Log.d("FtpUtils.connect", "ftp connect end: " + System.currentTimeMillis());
        if (reply == null) {
            return true;
        }
        int length = reply.length;
        for (int i = 0; i < length; i++) {
            Log.d("FtpUtils.connect", "ftp connect reply: " + reply[i]);
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static void closeSocket(final FTPClient client) {
        Log.d("FtpUtils.closeSocket", "now active ftp cmd task count: " + cmdThreadPoolExecutor.getActiveCount());
        cmdThreadPoolExecutor.execute(new Runnable() {
            public void run() {
                try {
                    FTPClient.this.abortCurrentConnectionAttempt();
                } catch (Exception e) {
                    Log.e("FtpUtils.closeSocket", Log.getStackTraceString(e));
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public static void interrupt(final FTPClient client, final boolean isForce) {
        Log.d("FtpUtils.interrupt", "now active ftp cmd task count: " + cmdThreadPoolExecutor.getActiveCount());
        cmdThreadPoolExecutor.execute(new Runnable() {
            public void run() {
                try {
                    FTPClient.this.abortCurrentDataTransfer(!isForce);
                } catch (Exception e) {
                    Log.e("FtpUtils.interrupt", Log.getStackTraceString(e));
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public static void disconnect(final FTPClient client, final boolean isForce) {
        Log.d("FtpUtils.disconnect", "now active ftp cmd task count: " + cmdThreadPoolExecutor.getActiveCount());
        cmdThreadPoolExecutor.execute(new Runnable() {
            public void run() {
                try {
                    if (FTPClient.this.isConnected()) {
                        FTPClient.this.disconnect(!isForce);
                    }
                } catch (Exception e) {
                    Log.e("FtpUtils.disconnect", Log.getStackTraceString(e));
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public static void createFile(String fileName, long fileSize) throws Exception {
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(fileName), "rw");
            raf.setLength(fileSize);
            raf.close();
        } catch (Exception e) {
            Log.e("FtpUtils.createFile", "Exception stack: " + Log.getStackTraceString(e) + ", fileName: " + fileName + ", fileSize: " + fileSize);
            throw e;
        }
    }

    /* access modifiers changed from: private */
    public static boolean renameDownloadedFile(String tmpFile, String toFile) {
        File file = new File(tmpFile);
        if (!file.exists()) {
            return false;
        }
        boolean isOk = file.renameTo(new File(toFile));
        if (isOk || FileUtils.fileChannelCopy(new File(tmpFile), new File(toFile)) <= 0) {
            return isOk;
        }
        FileUtils.deleteFile(tmpFile);
        return true;
    }

    public static void download(String remoteFile, String localFile, FtpConfig config, TransferListener listener) {
        try {
            String profileKey = getProfileKey(remoteFile, localFile, config);
            TransferProfile profile = transferProfiles.remove(profileKey);
            if (!(profile == null || profile.getClient() == null)) {
                if (profile.isTransferStarted() || profile.getCount() > 0) {
                    interrupt(profile.getClient(), true);
                } else {
                    disconnect(profile.getClient(), true);
                }
            }
            TransferProfile downloadProfile = new TransferProfile((TransferProfile) null);
            downloadProfile.setDownload(true);
            downloadProfile.setRemoteFile(remoteFile);
            downloadProfile.setLocalFile(localFile);
            downloadProfile.setTmpDownloadFile(String.valueOf(localFile) + "." + System.currentTimeMillis());
            downloadProfile.setFtpConfig(config);
            downloadProfile.setListener(listener);
            transferProfiles.put(profileKey, downloadProfile);
            Log.d("FtpUtils.download", "now active ftp transfer task count: " + transferThreadPoolExecutor.getActiveCount());
            downloadProfile.setTaskFuture(transferThreadPoolExecutor.submit(new DownloadTask(downloadProfile)));
        } catch (Exception e) {
            Log.e("FtpUtils.download", Log.getStackTraceString(e));
        }
    }

    public static void upload(String localFile, String remoteFile, FtpConfig config, TransferListener listener) {
        try {
            String profileKey = getProfileKey(remoteFile, localFile, config);
            TransferProfile profile = transferProfiles.remove(profileKey);
            if (!(profile == null || profile.getClient() == null)) {
                if (profile.isTransferStarted() || profile.getCount() > 0) {
                    interrupt(profile.getClient(), true);
                } else {
                    disconnect(profile.getClient(), true);
                }
            }
            TransferProfile uploadProfile = new TransferProfile((TransferProfile) null);
            uploadProfile.setDownload(false);
            uploadProfile.setRemoteFile(remoteFile);
            uploadProfile.setLocalFile(localFile);
            uploadProfile.setFtpConfig(config);
            uploadProfile.setListener(listener);
            transferProfiles.put(profileKey, uploadProfile);
            Log.d("FtpUtils.upload", "now active ftp transfer task count: " + transferThreadPoolExecutor.getActiveCount());
            uploadProfile.setTaskFuture(transferThreadPoolExecutor.submit(new UploadTask(uploadProfile)));
        } catch (Exception e) {
            Log.e("FtpUtils.upload", Log.getStackTraceString(e));
        }
    }
}
