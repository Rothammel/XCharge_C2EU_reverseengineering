package com.xcharge.common.utils;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpHeadHC4;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

public class HttpDownloadManager {
    private static HttpDownloadManager downloadManager = null;
    /* access modifiers changed from: private */
    public ConnectionKeepAliveStrategy connectionKeepAliveStrategy;
    /* access modifiers changed from: private */
    public PoolingHttpClientConnectionManager connectionManager;
    private DeadDownloadTaskMonitorThread deadDownloadTaskMonitorThread;
    /* access modifiers changed from: private */
    public ConcurrentHashMap<String, DownloadCount> downloadedCounts;
    private IdleConnectionMonitorThread idleConnectionMonitorThread;
    private ThreadPoolExecutor threadPoolExecutor;
    private long unitSize;

    public interface DownLoadListener {
        void onDownLoadComplete();

        void onDownLoadFail();

        void onDownLoadPercentage(int i);

        void onDownLoadPercentage(long j, long j2);
    }

    private interface DownLoadTaskListener {
        void onDownLoadComplete(long j, long j2);

        void onDownLoadFail(long j, long j2, long j3);

        void onDownLoadProgress(long j, long j2, long j3);
    }

    private class DownloadCount {
        private long count = 0;
        DownLoadListener downloadListener = null;
        List<Future> downloadTaskFutures = new ArrayList();
        private boolean interruptFlag = false;
        private int percent = 0;
        private int prePercent = 0;
        private int retryCnt = 0;
        private String storageFile = null;
        private long total = 0;

        /* renamed from: ts */
        private long f136ts = System.currentTimeMillis();
        private String url = null;

        public DownloadCount(long total2) {
            this.total = total2;
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

        public synchronized long getCount() {
            return this.count;
        }

        public synchronized long getTotal() {
            return this.total;
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

        public synchronized boolean isCompleted() {
            boolean z;
            if (this.count <= 0 || this.total <= 0 || this.count != this.total) {
                z = false;
            } else {
                z = true;
            }
            return z;
        }

        public synchronized void interrupt() {
            this.interruptFlag = true;
        }

        public synchronized boolean isInterrupted() {
            return this.interruptFlag;
        }

        public synchronized long getTs() {
            return this.f136ts;
        }

        public synchronized void updateTs() {
            this.f136ts = System.currentTimeMillis();
        }

        public synchronized String getUrl() {
            return this.url;
        }

        public synchronized void setUrl(String url2) {
            this.url = url2;
        }

        public synchronized String getStorageFile() {
            return this.storageFile;
        }

        public synchronized void setStorageFile(String storageFile2) {
            this.storageFile = storageFile2;
        }

        public synchronized String getCountKey() {
            return String.valueOf(this.url) + "-" + this.storageFile;
        }

        public synchronized int getRetryCnt() {
            return this.retryCnt;
        }

        public synchronized void incRetryCnt() {
            this.retryCnt++;
        }

        public synchronized List<Future> getDownloadTaskFutures() {
            return this.downloadTaskFutures;
        }

        public synchronized void addDownloadTaskFuture(Future downloadTaskFuture) {
            this.downloadTaskFutures.add(downloadTaskFuture);
        }

        public synchronized DownLoadListener getDownloadListener() {
            return this.downloadListener;
        }

        public synchronized void setDownloadListener(DownLoadListener downloadListener2) {
            this.downloadListener = downloadListener2;
        }
    }

    public static class DeadDownloadTaskMonitorThread extends Thread {
        private final ConcurrentHashMap<String, DownloadCount> downloadedCounts;
        private volatile boolean shutdown;
        private final ThreadPoolExecutor threadPoolExecutor;

        public DeadDownloadTaskMonitorThread(ThreadPoolExecutor threadPoolExecutor2, ConcurrentHashMap<String, DownloadCount> downloadedCounts2) {
            this.threadPoolExecutor = threadPoolExecutor2;
            this.downloadedCounts = downloadedCounts2;
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
                java.util.HashMap r0 = new java.util.HashMap     // Catch:{ all -> 0x0029 }
                r0.<init>()     // Catch:{ all -> 0x0029 }
                java.util.concurrent.ConcurrentHashMap<java.lang.String, com.xcharge.common.utils.HttpDownloadManager$DownloadCount> r10 = r14.downloadedCounts     // Catch:{ all -> 0x0029 }
                r0.putAll(r10)     // Catch:{ all -> 0x0029 }
                java.util.Set r10 = r0.entrySet()     // Catch:{ all -> 0x0029 }
                java.util.Iterator r7 = r10.iterator()     // Catch:{ all -> 0x0029 }
            L_0x0021:
                boolean r10 = r7.hasNext()     // Catch:{ all -> 0x0029 }
                if (r10 != 0) goto L_0x0037
                monitor-exit(r14)     // Catch:{ all -> 0x0029 }
                goto L_0x0000
            L_0x0029:
                r10 = move-exception
                monitor-exit(r14)     // Catch:{ all -> 0x0029 }
                throw r10     // Catch:{ Exception -> 0x002c }
            L_0x002c:
                r4 = move-exception
                java.lang.String r10 = "HttpDownloadManager.DeadDownloadTaskMonitorThread"
                java.lang.String r11 = android.util.Log.getStackTraceString(r4)
                android.util.Log.w(r10, r11)
                goto L_0x0004
            L_0x0037:
                java.lang.Object r3 = r7.next()     // Catch:{ all -> 0x0029 }
                java.util.Map$Entry r3 = (java.util.Map.Entry) r3     // Catch:{ all -> 0x0029 }
                java.lang.Object r2 = r3.getValue()     // Catch:{ all -> 0x0029 }
                com.xcharge.common.utils.HttpDownloadManager$DownloadCount r2 = (com.xcharge.common.utils.HttpDownloadManager.DownloadCount) r2     // Catch:{ all -> 0x0029 }
                long r10 = r2.getCount()     // Catch:{ all -> 0x0029 }
                long r12 = r2.getTotal()     // Catch:{ all -> 0x0029 }
                int r10 = (r10 > r12 ? 1 : (r10 == r12 ? 0 : -1))
                if (r10 >= 0) goto L_0x0021
                long r10 = r2.getTs()     // Catch:{ all -> 0x0029 }
                long r10 = r8 - r10
                r12 = 120000(0x1d4c0, double:5.9288E-319)
                int r10 = (r10 > r12 ? 1 : (r10 == r12 ? 0 : -1))
                if (r10 < 0) goto L_0x0021
                java.lang.String r10 = "HttpDownloadManager.DeadDownloadTaskMonitorThread"
                java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ all -> 0x0029 }
                java.lang.String r12 = "clear dead download: "
                r11.<init>(r12)     // Catch:{ all -> 0x0029 }
                java.lang.String r12 = r2.getCountKey()     // Catch:{ all -> 0x0029 }
                java.lang.StringBuilder r11 = r11.append(r12)     // Catch:{ all -> 0x0029 }
                java.lang.String r11 = r11.toString()     // Catch:{ all -> 0x0029 }
                android.util.Log.i(r10, r11)     // Catch:{ all -> 0x0029 }
                r2.interrupt()     // Catch:{ all -> 0x0029 }
                java.util.List r6 = r2.getDownloadTaskFutures()     // Catch:{ all -> 0x0029 }
                java.util.ArrayList r1 = new java.util.ArrayList     // Catch:{ all -> 0x0029 }
                r1.<init>()     // Catch:{ all -> 0x0029 }
                r1.addAll(r6)     // Catch:{ all -> 0x0029 }
                java.util.Iterator r10 = r1.iterator()     // Catch:{ all -> 0x0029 }
            L_0x0087:
                boolean r11 = r10.hasNext()     // Catch:{ all -> 0x0029 }
                if (r11 != 0) goto L_0x00a4
                java.util.concurrent.ThreadPoolExecutor r10 = r14.threadPoolExecutor     // Catch:{ all -> 0x0029 }
                r10.purge()     // Catch:{ all -> 0x0029 }
                com.xcharge.common.utils.HttpDownloadManager$DownLoadListener r10 = r2.getDownloadListener()     // Catch:{ all -> 0x0029 }
                r10.onDownLoadFail()     // Catch:{ all -> 0x0029 }
                java.util.concurrent.ConcurrentHashMap<java.lang.String, com.xcharge.common.utils.HttpDownloadManager$DownloadCount> r10 = r14.downloadedCounts     // Catch:{ all -> 0x0029 }
                java.lang.String r11 = r2.getCountKey()     // Catch:{ all -> 0x0029 }
                r10.remove(r11)     // Catch:{ all -> 0x0029 }
                goto L_0x0021
            L_0x00a4:
                java.lang.Object r5 = r10.next()     // Catch:{ all -> 0x0029 }
                java.util.concurrent.Future r5 = (java.util.concurrent.Future) r5     // Catch:{ all -> 0x0029 }
                boolean r11 = r5.isDone()     // Catch:{ all -> 0x0029 }
                if (r11 != 0) goto L_0x0087
                r11 = 1
                r5.cancel(r11)     // Catch:{ all -> 0x0029 }
                goto L_0x0087
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.common.utils.HttpDownloadManager.DeadDownloadTaskMonitorThread.run():void");
        }

        public void shutdown() {
            this.shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }

    public static class IdleConnectionMonitorThread extends Thread {
        private final HttpClientConnectionManager connMgr;
        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr2) {
            this.connMgr = connMgr2;
        }

        /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r5 = this;
            L_0x0000:
                boolean r1 = r5.shutdown     // Catch:{ Exception -> 0x001e }
                if (r1 == 0) goto L_0x0005
            L_0x0004:
                return
            L_0x0005:
                monitor-enter(r5)     // Catch:{ Exception -> 0x001e }
                r2 = 5000(0x1388, double:2.4703E-320)
                r5.wait(r2)     // Catch:{ all -> 0x001b }
                org.apache.http.conn.HttpClientConnectionManager r1 = r5.connMgr     // Catch:{ all -> 0x001b }
                r1.closeExpiredConnections()     // Catch:{ all -> 0x001b }
                org.apache.http.conn.HttpClientConnectionManager r1 = r5.connMgr     // Catch:{ all -> 0x001b }
                r2 = 30
                java.util.concurrent.TimeUnit r4 = java.util.concurrent.TimeUnit.SECONDS     // Catch:{ all -> 0x001b }
                r1.closeIdleConnections(r2, r4)     // Catch:{ all -> 0x001b }
                monitor-exit(r5)     // Catch:{ all -> 0x001b }
                goto L_0x0000
            L_0x001b:
                r1 = move-exception
                monitor-exit(r5)     // Catch:{ all -> 0x001b }
                throw r1     // Catch:{ Exception -> 0x001e }
            L_0x001e:
                r0 = move-exception
                java.lang.String r1 = "HttpDownloadManager.IdleConnectionMonitorThread"
                java.lang.String r2 = android.util.Log.getStackTraceString(r0)
                android.util.Log.w(r1, r2)
                goto L_0x0004
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.common.utils.HttpDownloadManager.IdleConnectionMonitorThread.run():void");
        }

        public void shutdown() {
            this.shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }

    public static void config(HttpRequestBase httpRequestBase) {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 30000);
        HttpConnectionParams.setSoTimeout(params, 30000);
        httpRequestBase.setParams(params);
    }

    private HttpDownloadManager() {
        this.connectionManager = null;
        this.idleConnectionMonitorThread = null;
        this.connectionKeepAliveStrategy = null;
        this.deadDownloadTaskMonitorThread = null;
        this.threadPoolExecutor = null;
        this.unitSize = 1048576;
        this.downloadedCounts = null;
        this.connectionKeepAliveStrategy = new ConnectionKeepAliveStrategy() {
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator("Keep-Alive"));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        try {
                            Log.d("HttpDownloadManager.getKeepAliveDuration", "receive response, and Head Keep-Alive: " + value + " seconds !!!");
                            return Long.parseLong(value) * 1000;
                        } catch (NumberFormatException ignore) {
                            Log.w("HttpDownloadManager.getKeepAliveDuration", Log.getStackTraceString(ignore));
                        }
                    }
                }
                Log.d("HttpDownloadManager.getKeepAliveDuration", "receive response, and no Head Keep-Alive, using 30 seconds !!!");
                return 30000;
            }
        };
        this.connectionManager = new PoolingHttpClientConnectionManager();
        this.connectionManager.setMaxTotal(100);
        this.connectionManager.setDefaultMaxPerRoute(10);
        this.connectionManager.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(30000).setSoLinger(30).build());
        this.downloadedCounts = new ConcurrentHashMap<>();
        this.threadPoolExecutor = new ThreadPoolExecutor(3, 3, 30, TimeUnit.SECONDS, new LinkedBlockingQueue(), new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                Log.w("HttpDownloadManager.ThreadPoolExecutor.rejectedExecution", "rejected runnable: " + r.toString() + ", active runnables: " + executor.getActiveCount());
                if (r instanceof DownLoadTask) {
                    HttpDownloadManager.this.downloadedCounts.remove(((DownLoadTask) r).getDownloadCount().getCountKey());
                }
            }
        });
        this.threadPoolExecutor.allowCoreThreadTimeOut(true);
        this.idleConnectionMonitorThread = new IdleConnectionMonitorThread(this.connectionManager);
        this.idleConnectionMonitorThread.start();
        this.deadDownloadTaskMonitorThread = new DeadDownloadTaskMonitorThread(this.threadPoolExecutor, this.downloadedCounts);
        this.deadDownloadTaskMonitorThread.start();
    }

    public static synchronized HttpDownloadManager getInstance() {
        HttpDownloadManager httpDownloadManager;
        synchronized (HttpDownloadManager.class) {
            if (downloadManager == null) {
                downloadManager = new HttpDownloadManager();
            }
            httpDownloadManager = downloadManager;
        }
        return httpDownloadManager;
    }

    public boolean changeDownloadCoreThreadNum(int newNum) {
        int nowNum = this.threadPoolExecutor.getCorePoolSize();
        int maxNum = this.threadPoolExecutor.getMaximumPoolSize();
        if (newNum < 0) {
            newNum = nowNum;
        }
        if (newNum > maxNum) {
            newNum = maxNum;
        }
        if (newNum == nowNum) {
            return true;
        }
        this.threadPoolExecutor.setCorePoolSize(newNum);
        Log.w("HttpDownloadManager.changeDownloadCoreThreadNum", "change download thread pool core number to: " + newNum);
        return true;
    }

    private String createCountKey(String url, String storageFile) {
        return String.valueOf(url) + "-" + storageFile;
    }

    public void downloadFile(Context context, String url, String toFile, DownLoadListener listener) {
        try {
            interrupt(createCountKey(url, toFile));
            long fileSize = getFileSize(url);
            if (fileSize == 0) {
                Log.w("HttpDownloadManager.downloadFile", "failed to get file size, fileUrl: " + url);
                listener.onDownLoadFail();
                return;
            }
            String tmpFile = String.valueOf(toFile) + "." + Math.abs(url.hashCode());
            createFile(tmpFile, fileSize);
            DownloadCount downloadCount = new DownloadCount(fileSize);
            downloadCount.setUrl(url);
            downloadCount.setStorageFile(toFile);
            downloadCount.setDownloadListener(listener);
            this.downloadedCounts.put(downloadCount.getCountKey(), downloadCount);
            long sizePerTask = fileSize;
            Long taskCount = Long.valueOf(fileSize / sizePerTask);
            long offset = 0;
            for (int i = 0; ((long) i) < taskCount.longValue(); i++) {
                asyncDownload(context, url, tmpFile, toFile, offset, sizePerTask, downloadCount, listener);
                offset += sizePerTask;
            }
            long remainder = fileSize % sizePerTask;
            if (remainder > 0) {
                asyncDownload(context, url, tmpFile, toFile, offset, remainder, downloadCount, listener);
            }
        } catch (Exception e) {
            Log.e("HttpDownloadManager.downloadFile", "Exception stack: " + Log.getStackTraceString(e) + ", url: " + url + ", toFile: " + toFile);
            listener.onDownLoadFail();
        }
    }

    public boolean isDownloadCompleted(String key) {
        DownloadCount count = this.downloadedCounts.get(key);
        if (count != null) {
            return count.isCompleted();
        }
        return false;
    }

    public synchronized boolean isDownloadInterrupted(String key) {
        boolean z;
        DownloadCount count = this.downloadedCounts.get(key);
        if (count != null) {
            z = count.isInterrupted();
        } else {
            z = false;
        }
        return z;
    }

    public void interrupt(String key) {
        DownloadCount count = this.downloadedCounts.get(key);
        if (count != null) {
            count.interrupt();
        }
    }

    public void interruptAll() {
        Enumeration<DownloadCount> enumeration = this.downloadedCounts.elements();
        while (true) {
            try {
                DownloadCount count = enumeration.nextElement();
                if (count != null) {
                    count.interrupt();
                } else {
                    return;
                }
            } catch (Exception e) {
                Log.e("HttpDownloadManager.interruptAll", Log.getStackTraceString(e));
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isNetWorkOk(Context context) {
        return ContextUtils.isNetworkConnected(context);
    }

    /* access modifiers changed from: private */
    public void asyncDownload(Context context, String url, String tmpFile, String toFile, long offset, long length, DownloadCount downloadCount, DownLoadListener listener) throws Exception {
        if (downloadCount.isInterrupted()) {
            Log.w("HttpDownloadManager.asyncDownload", "download interrupted, can not start new async download task");
            return;
        }
        final DownloadCount downloadCount2 = downloadCount;
        final DownLoadListener downLoadListener = listener;
        final Context context2 = context;
        final String str = url;
        final String str2 = tmpFile;
        final String str3 = toFile;
        DownloadCount downloadCount3 = downloadCount;
        downloadCount3.addDownloadTaskFuture(this.threadPoolExecutor.submit(new DownLoadTask(url, tmpFile, offset, length, downloadCount, new DownLoadTaskListener() {
            public void onDownLoadProgress(long beginPos, long inc, long downloaded) {
                int percent = downloadCount2.compareGetPercent();
                if (percent > 0) {
                    Log.i("HttpDownloadManager.asyncDownload.onDownLoadProgress", "downloading ...: " + percent + "%");
                    downLoadListener.onDownLoadPercentage(downloadCount2.getPercent());
                }
                downLoadListener.onDownLoadPercentage(downloadCount2.getCount(), downloadCount2.getTotal());
            }

            public void onDownLoadFail(long beginPos, long downloaded, long total) {
                if (downloadCount2.isInterrupted()) {
                    Log.w("HttpDownloadManager.asyncDownload.onDownLoadFail", "interrupted: " + downloadCount2.getPercent());
                    downLoadListener.onDownLoadFail();
                    return;
                }
                Log.d("HttpDownloadManager.asyncDownload.onDownLoadFail", "download failed, try it again, beginPos: " + beginPos + ", downloaded: " + downloaded + ", need download length: " + total + ", total downloaded: " + downloadCount2.getCount());
                long newBeginPos = beginPos;
                long newDownloadLength = total;
                downloadCount2.incCount(-1 * downloaded);
                int networkCheckCnt = 0;
                while (!HttpDownloadManager.this.isNetWorkOk(context2)) {
                    try {
                        if (downloadCount2.isInterrupted()) {
                            Log.w("HttpDownloadManager.asyncDownload.onDownLoadFail", "no network, and interrupted: " + downloadCount2.getPercent());
                            downLoadListener.onDownLoadFail();
                            return;
                        } else if (networkCheckCnt >= 12) {
                            downloadCount2.interrupt();
                            Log.w("HttpDownloadManager.asyncDownload.onDownLoadFail", "no network !!! " + downloadCount2.getPercent());
                            downLoadListener.onDownLoadFail();
                            return;
                        } else {
                            Thread.sleep(5000);
                            networkCheckCnt++;
                        }
                    } catch (Exception e) {
                        Log.e("HttpDownloadManager.asyncDownload.onDownLoadFail", Log.getStackTraceString(e));
                        downloadCount2.interrupt();
                        Log.w("HttpDownloadManager.asyncDownload.onDownLoadFail", "download failed: " + downloadCount2.getPercent());
                        downLoadListener.onDownLoadFail();
                        return;
                    }
                }
                if (downloadCount2.getRetryCnt() >= 3) {
                    downloadCount2.interrupt();
                    Log.w("HttpDownloadManager.asyncDownload.onDownLoadFail", "download failed over 3 times: " + downloadCount2.getPercent());
                    downLoadListener.onDownLoadFail();
                    return;
                }
                HttpDownloadManager.this.asyncDownload(context2, str, str2, str3, newBeginPos, newDownloadLength, downloadCount2, downLoadListener);
                downloadCount2.incRetryCnt();
                Log.i("HttpDownloadManager.asyncDownload.onDownLoadFail", "try to download again, beginPos: " + newBeginPos + ", downloadLength: " + newDownloadLength + ", total downloaded: " + downloadCount2.getCount() + ", retry cnt: " + downloadCount2.getRetryCnt());
            }

            public void onDownLoadComplete(long beginPos, long downloaded) {
                if (downloadCount2.isCompleted()) {
                    downloadCount2.interrupt();
                    boolean unused = HttpDownloadManager.this.renameDownloadedFile(str2, str3);
                    Log.i("HttpDownloadManager.asyncDownload.onDownLoadComplete", "download completed: " + downloadCount2.getCount());
                    downLoadListener.onDownLoadComplete();
                    return;
                }
                Log.i("HttpDownloadManager.asyncDownload.onDownLoadComplete", "partial download completed, countinue..., beginPos: " + beginPos + ", downloaded: " + downloaded + ", total downloaded: " + downloadCount2.getCount());
            }
        })));
    }

    private long getFileSize(String fileUrl) {
        long fileSize = 0;
        try {
            HttpURLConnection httpConnection = (HttpURLConnection) new URL(fileUrl).openConnection();
            httpConnection.setConnectTimeout(30000);
            httpConnection.setReadTimeout(30000);
            httpConnection.setRequestMethod(HttpHeadHC4.METHOD_NAME);
            httpConnection.connect();
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == 200) {
                fileSize = (long) httpConnection.getContentLength();
                Log.i("HttpDownloadManager.getFileSize", "file (" + fileUrl + ") size: " + fileSize);
            } else {
                Log.w("HttpDownloadManager.getFileSize", "response code is not OK: " + responseCode + ", fileUrl: " + fileUrl);
            }
            return fileSize;
        } catch (Exception e) {
            Log.e("HttpDownloadManager.getFileSize", "Exception stack: " + Log.getStackTraceString(e) + ", fileUrl: " + fileUrl);
            return 0;
        }
    }

    private void createFile(String fileName, long fileSize) throws Exception {
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(fileName), "rw");
            raf.setLength(fileSize);
            raf.close();
        } catch (Exception e) {
            Log.e("HttpDownloadManager.createFile", "Exception stack: " + Log.getStackTraceString(e) + ", fileName: " + fileName + ", fileSize: " + fileSize);
            throw e;
        }
    }

    /* access modifiers changed from: private */
    public boolean renameDownloadedFile(String tmpFile, String toFile) {
        File file = new File(tmpFile);
        if (file.exists()) {
            return file.renameTo(new File(toFile));
        }
        return false;
    }

    private DownloadCount getDownloadCount(String key) {
        DownloadCount count = this.downloadedCounts.get(key);
        if (count == null) {
            return new DownloadCount(0);
        }
        return count;
    }

    private void incCount(String key, long inc) {
        DownloadCount count = this.downloadedCounts.get(key);
        if (count != null) {
            count.incCount(inc);
        }
    }

    private class DownLoadTask implements Runnable {
        private long beginPos = 0;
        DownloadCount downloadCount = null;
        private long downloadLength = 0;
        private String fileName = null;
        private String fileUrl = null;
        private DownLoadTaskListener listener = null;

        public DownLoadTask(String fileUrl2, String fileName2, long beginPos2, long downloadLength2, DownloadCount downloadCount2, DownLoadTaskListener listener2) {
            this.fileUrl = fileUrl2;
            this.fileName = fileName2;
            this.beginPos = beginPos2;
            this.downloadLength = downloadLength2;
            this.downloadCount = downloadCount2;
            this.listener = listener2;
        }

        public DownloadCount getDownloadCount() {
            return this.downloadCount;
        }

        public void run() {
            downloadFile();
        }

        /* JADX WARNING: Removed duplicated region for block: B:22:0x0136  */
        /* JADX WARNING: Removed duplicated region for block: B:46:? A[RETURN, SYNTHETIC] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void downloadFile() {
            /*
                r23 = this;
                r0 = r23
                com.xcharge.common.utils.HttpDownloadManager$DownloadCount r3 = r0.downloadCount
                boolean r3 = r3.isInterrupted()
                if (r3 == 0) goto L_0x0023
                java.lang.String r3 = "HttpDownloadManager.DownLoadTask.downloadFile"
                java.lang.StringBuilder r4 = new java.lang.StringBuilder
                java.lang.String r5 = "download ended, exit download task, beginPos: "
                r4.<init>(r5)
                r0 = r23
                long r6 = r0.beginPos
                java.lang.StringBuilder r4 = r4.append(r6)
                java.lang.String r4 = r4.toString()
                android.util.Log.w(r3, r4)
            L_0x0022:
                return
            L_0x0023:
                r13 = 0
                r21 = 0
                r15 = 0
                r17 = 0
                r10 = 0
                r16 = 0
                org.apache.http.impl.client.HttpClientBuilder r3 = org.apache.http.impl.client.HttpClients.custom()     // Catch:{ Exception -> 0x01eb }
                r0 = r23
                com.xcharge.common.utils.HttpDownloadManager r4 = com.xcharge.common.utils.HttpDownloadManager.this     // Catch:{ Exception -> 0x01eb }
                org.apache.http.impl.conn.PoolingHttpClientConnectionManager r4 = r4.connectionManager     // Catch:{ Exception -> 0x01eb }
                org.apache.http.impl.client.HttpClientBuilder r3 = r3.setConnectionManager(r4)     // Catch:{ Exception -> 0x01eb }
                r0 = r23
                com.xcharge.common.utils.HttpDownloadManager r4 = com.xcharge.common.utils.HttpDownloadManager.this     // Catch:{ Exception -> 0x01eb }
                org.apache.http.conn.ConnectionKeepAliveStrategy r4 = r4.connectionKeepAliveStrategy     // Catch:{ Exception -> 0x01eb }
                org.apache.http.impl.client.HttpClientBuilder r3 = r3.setKeepAliveStrategy(r4)     // Catch:{ Exception -> 0x01eb }
                org.apache.http.impl.client.CloseableHttpClient r13 = r3.build()     // Catch:{ Exception -> 0x01eb }
                java.net.URI r22 = new java.net.URI     // Catch:{ Exception -> 0x01eb }
                r0 = r23
                java.lang.String r3 = r0.fileUrl     // Catch:{ Exception -> 0x01eb }
                r0 = r22
                r0.<init>(r3)     // Catch:{ Exception -> 0x01eb }
                org.apache.http.client.methods.HttpGet r14 = new org.apache.http.client.methods.HttpGet     // Catch:{ Exception -> 0x01ad }
                r0 = r22
                r14.<init>(r0)     // Catch:{ Exception -> 0x01ad }
                com.xcharge.common.utils.HttpDownloadManager.config(r14)     // Catch:{ Exception -> 0x01ad }
                java.lang.String r3 = "Range"
                java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x01ad }
                java.lang.String r5 = "bytes="
                r4.<init>(r5)     // Catch:{ Exception -> 0x01ad }
                r0 = r23
                long r6 = r0.beginPos     // Catch:{ Exception -> 0x01ad }
                java.lang.StringBuilder r4 = r4.append(r6)     // Catch:{ Exception -> 0x01ad }
                java.lang.String r5 = "-"
                java.lang.StringBuilder r4 = r4.append(r5)     // Catch:{ Exception -> 0x01ad }
                r0 = r23
                long r6 = r0.beginPos     // Catch:{ Exception -> 0x01ad }
                r0 = r23
                long r8 = r0.downloadLength     // Catch:{ Exception -> 0x01ad }
                long r6 = r6 + r8
                r8 = 1
                long r6 = r6 - r8
                java.lang.StringBuilder r4 = r4.append(r6)     // Catch:{ Exception -> 0x01ad }
                java.lang.String r4 = r4.toString()     // Catch:{ Exception -> 0x01ad }
                r14.addHeader(r3, r4)     // Catch:{ Exception -> 0x01ad }
                java.lang.String r3 = "HttpDownloadManager.DownLoadTask.downloadFile"
                java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x01ad }
                java.lang.String r5 = "download begin pos: "
                r4.<init>(r5)     // Catch:{ Exception -> 0x01ad }
                r0 = r23
                long r6 = r0.beginPos     // Catch:{ Exception -> 0x01ad }
                java.lang.StringBuilder r4 = r4.append(r6)     // Catch:{ Exception -> 0x01ad }
                java.lang.String r5 = ", need download length: "
                java.lang.StringBuilder r4 = r4.append(r5)     // Catch:{ Exception -> 0x01ad }
                r0 = r23
                long r6 = r0.downloadLength     // Catch:{ Exception -> 0x01ad }
                java.lang.StringBuilder r4 = r4.append(r6)     // Catch:{ Exception -> 0x01ad }
                java.lang.String r4 = r4.toString()     // Catch:{ Exception -> 0x01ad }
                android.util.Log.i(r3, r4)     // Catch:{ Exception -> 0x01ad }
                org.apache.http.client.methods.CloseableHttpResponse r20 = r13.execute((org.apache.http.client.methods.HttpUriRequest) r14)     // Catch:{ Exception -> 0x01ad }
                org.apache.http.HttpEntity r12 = r20.getEntity()     // Catch:{ all -> 0x01f3 }
                java.io.InputStream r15 = r12.getContent()     // Catch:{ all -> 0x01f3 }
                java.io.RandomAccessFile r18 = new java.io.RandomAccessFile     // Catch:{ all -> 0x01f3 }
                r0 = r23
                java.lang.String r3 = r0.fileName     // Catch:{ all -> 0x01f3 }
                java.lang.String r4 = "rw"
                r0 = r18
                r0.<init>(r3, r4)     // Catch:{ all -> 0x01f3 }
                r3 = 8192(0x2000, float:1.14794E-41)
                byte[] r2 = new byte[r3]     // Catch:{ all -> 0x01a3 }
                r19 = -1
            L_0x00d4:
                int r19 = r15.read(r2)     // Catch:{ all -> 0x01a3 }
                r3 = -1
                r0 = r19
                if (r0 != r3) goto L_0x0178
            L_0x00dd:
                java.lang.String r3 = "HttpDownloadManager.DownLoadTask.downloadFile"
                java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x01a3 }
                java.lang.String r5 = "total downloaded: "
                r4.<init>(r5)     // Catch:{ all -> 0x01a3 }
                java.lang.StringBuilder r4 = r4.append(r10)     // Catch:{ all -> 0x01a3 }
                java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x01a3 }
                android.util.Log.i(r3, r4)     // Catch:{ all -> 0x01a3 }
                long r4 = (long) r10     // Catch:{ all -> 0x01a3 }
                r0 = r23
                long r6 = r0.downloadLength     // Catch:{ all -> 0x01a3 }
                int r3 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
                if (r3 != 0) goto L_0x012a
                r16 = 1
                java.lang.String r3 = "HttpDownloadManager.DownLoadTask.downloadFile"
                java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x01a3 }
                java.lang.String r5 = "download completed, beginPos: "
                r4.<init>(r5)     // Catch:{ all -> 0x01a3 }
                r0 = r23
                long r6 = r0.beginPos     // Catch:{ all -> 0x01a3 }
                java.lang.StringBuilder r4 = r4.append(r6)     // Catch:{ all -> 0x01a3 }
                java.lang.String r5 = ", downloaded: "
                java.lang.StringBuilder r4 = r4.append(r5)     // Catch:{ all -> 0x01a3 }
                java.lang.StringBuilder r4 = r4.append(r10)     // Catch:{ all -> 0x01a3 }
                java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x01a3 }
                android.util.Log.i(r3, r4)     // Catch:{ all -> 0x01a3 }
                r0 = r23
                com.xcharge.common.utils.HttpDownloadManager$DownLoadTaskListener r3 = r0.listener     // Catch:{ all -> 0x01a3 }
                r0 = r23
                long r4 = r0.beginPos     // Catch:{ all -> 0x01a3 }
                long r6 = (long) r10     // Catch:{ all -> 0x01a3 }
                r3.onDownLoadComplete(r4, r6)     // Catch:{ all -> 0x01a3 }
            L_0x012a:
                r18.close()     // Catch:{ Exception -> 0x01ed }
                r15.close()     // Catch:{ Exception -> 0x01ed }
                r17 = r18
                r21 = r22
            L_0x0134:
                if (r16 != 0) goto L_0x0022
                java.lang.String r3 = "HttpDownloadManager.DownLoadTask.downloadFile"
                java.lang.StringBuilder r4 = new java.lang.StringBuilder
                java.lang.String r5 = "download failed, beginPos: "
                r4.<init>(r5)
                r0 = r23
                long r6 = r0.beginPos
                java.lang.StringBuilder r4 = r4.append(r6)
                java.lang.String r5 = ", downloaded: "
                java.lang.StringBuilder r4 = r4.append(r5)
                java.lang.StringBuilder r4 = r4.append(r10)
                java.lang.String r5 = ", need download length: "
                java.lang.StringBuilder r4 = r4.append(r5)
                r0 = r23
                long r6 = r0.downloadLength
                java.lang.StringBuilder r4 = r4.append(r6)
                java.lang.String r4 = r4.toString()
                android.util.Log.i(r3, r4)
                r0 = r23
                com.xcharge.common.utils.HttpDownloadManager$DownLoadTaskListener r3 = r0.listener
                r0 = r23
                long r4 = r0.beginPos
                long r6 = (long) r10
                r0 = r23
                long r8 = r0.downloadLength
                r3.onDownLoadFail(r4, r6, r8)
                goto L_0x0022
            L_0x0178:
                r0 = r23
                com.xcharge.common.utils.HttpDownloadManager$DownloadCount r3 = r0.downloadCount     // Catch:{ all -> 0x01a3 }
                r3.updateTs()     // Catch:{ all -> 0x01a3 }
                r0 = r23
                com.xcharge.common.utils.HttpDownloadManager$DownloadCount r3 = r0.downloadCount     // Catch:{ all -> 0x01a3 }
                boolean r3 = r3.isInterrupted()     // Catch:{ all -> 0x01a3 }
                if (r3 == 0) goto L_0x01bb
                java.lang.String r3 = "HttpDownloadManager.DownLoadTask.downloadFile"
                java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x01a3 }
                java.lang.String r5 = "download interrupted, exit download task, beginPos: "
                r4.<init>(r5)     // Catch:{ all -> 0x01a3 }
                r0 = r23
                long r6 = r0.beginPos     // Catch:{ all -> 0x01a3 }
                java.lang.StringBuilder r4 = r4.append(r6)     // Catch:{ all -> 0x01a3 }
                java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x01a3 }
                android.util.Log.w(r3, r4)     // Catch:{ all -> 0x01a3 }
                goto L_0x00dd
            L_0x01a3:
                r3 = move-exception
                r17 = r18
            L_0x01a6:
                r17.close()     // Catch:{ Exception -> 0x01ad }
                r15.close()     // Catch:{ Exception -> 0x01ad }
                throw r3     // Catch:{ Exception -> 0x01ad }
            L_0x01ad:
                r11 = move-exception
                r21 = r22
            L_0x01b0:
                java.lang.String r3 = "HttpDownloadManager.DownLoadTask.downloadFile"
                java.lang.String r4 = android.util.Log.getStackTraceString(r11)
                android.util.Log.e(r3, r4)
                goto L_0x0134
            L_0x01bb:
                r0 = r23
                long r4 = r0.beginPos     // Catch:{ all -> 0x01a3 }
                long r6 = (long) r10     // Catch:{ all -> 0x01a3 }
                long r4 = r4 + r6
                r0 = r18
                r0.seek(r4)     // Catch:{ all -> 0x01a3 }
                r3 = 0
                r0 = r18
                r1 = r19
                r0.write(r2, r3, r1)     // Catch:{ all -> 0x01a3 }
                r0 = r23
                com.xcharge.common.utils.HttpDownloadManager$DownloadCount r3 = r0.downloadCount     // Catch:{ all -> 0x01a3 }
                r0 = r19
                long r4 = (long) r0     // Catch:{ all -> 0x01a3 }
                r3.incCount(r4)     // Catch:{ all -> 0x01a3 }
                int r10 = r10 + r19
                r0 = r23
                com.xcharge.common.utils.HttpDownloadManager$DownLoadTaskListener r3 = r0.listener     // Catch:{ all -> 0x01a3 }
                r0 = r23
                long r4 = r0.beginPos     // Catch:{ all -> 0x01a3 }
                r0 = r19
                long r6 = (long) r0     // Catch:{ all -> 0x01a3 }
                long r8 = (long) r10     // Catch:{ all -> 0x01a3 }
                r3.onDownLoadProgress(r4, r6, r8)     // Catch:{ all -> 0x01a3 }
                goto L_0x00d4
            L_0x01eb:
                r11 = move-exception
                goto L_0x01b0
            L_0x01ed:
                r11 = move-exception
                r17 = r18
                r21 = r22
                goto L_0x01b0
            L_0x01f3:
                r3 = move-exception
                goto L_0x01a6
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.common.utils.HttpDownloadManager.DownLoadTask.downloadFile():void");
        }
    }
}
