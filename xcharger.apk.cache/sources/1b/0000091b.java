package com.xcharge.common.utils;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.http.HeaderElement;
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
import org.apache.http.protocol.HttpContext;

/* loaded from: classes.dex */
public class HttpDownloadManager {
    private static HttpDownloadManager downloadManager = null;
    private ConnectionKeepAliveStrategy connectionKeepAliveStrategy;
    private PoolingHttpClientConnectionManager connectionManager;
    private DeadDownloadTaskMonitorThread deadDownloadTaskMonitorThread;
    private ConcurrentHashMap<String, DownloadCount> downloadedCounts;
    private IdleConnectionMonitorThread idleConnectionMonitorThread;
    private ThreadPoolExecutor threadPoolExecutor;
    private long unitSize = 1048576;

    /* loaded from: classes.dex */
    public interface DownLoadListener {
        void onDownLoadComplete();

        void onDownLoadFail();

        void onDownLoadPercentage(int i);

        void onDownLoadPercentage(long j, long j2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public interface DownLoadTaskListener {
        void onDownLoadComplete(long j, long j2);

        void onDownLoadFail(long j, long j2, long j3);

        void onDownLoadProgress(long j, long j2, long j3);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class DownloadCount {
        private long total;
        private long count = 0;
        private int prePercent = 0;
        private int percent = 0;
        private long ts = System.currentTimeMillis();
        private boolean interruptFlag = false;
        private String url = null;
        private String storageFile = null;
        private int retryCnt = 0;
        List<Future> downloadTaskFutures = new ArrayList();
        DownLoadListener downloadListener = null;

        public DownloadCount(long total) {
            this.total = 0L;
            this.total = total;
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
            return this.percent > this.prePercent ? this.percent : -1;
        }

        public synchronized boolean isCompleted() {
            boolean z;
            if (this.count > 0 && this.total > 0) {
                z = this.count == this.total;
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
            return this.ts;
        }

        public synchronized void updateTs() {
            this.ts = System.currentTimeMillis();
        }

        public synchronized String getUrl() {
            return this.url;
        }

        public synchronized void setUrl(String url) {
            this.url = url;
        }

        public synchronized String getStorageFile() {
            return this.storageFile;
        }

        public synchronized void setStorageFile(String storageFile) {
            this.storageFile = storageFile;
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

        public synchronized void setDownloadListener(DownLoadListener downloadListener) {
            this.downloadListener = downloadListener;
        }
    }

    /* loaded from: classes.dex */
    public static class DeadDownloadTaskMonitorThread extends Thread {
        private final ConcurrentHashMap<String, DownloadCount> downloadedCounts;
        private volatile boolean shutdown;
        private final ThreadPoolExecutor threadPoolExecutor;

        public DeadDownloadTaskMonitorThread(ThreadPoolExecutor threadPoolExecutor, ConcurrentHashMap<String, DownloadCount> downloadedCounts) {
            this.threadPoolExecutor = threadPoolExecutor;
            this.downloadedCounts = downloadedCounts;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (!this.shutdown) {
                try {
                    synchronized (this) {
                        wait(30000L);
                        long now = System.currentTimeMillis();
                        HashMap<String, DownloadCount> clonedDownloadedCounts = new HashMap<>();
                        clonedDownloadedCounts.putAll(this.downloadedCounts);
                        for (Map.Entry<String, DownloadCount> entry : clonedDownloadedCounts.entrySet()) {
                            DownloadCount downloadCount = entry.getValue();
                            if (downloadCount.getCount() < downloadCount.getTotal() && now - downloadCount.getTs() >= 120000) {
                                Log.i("HttpDownloadManager.DeadDownloadTaskMonitorThread", "clear dead download: " + downloadCount.getCountKey());
                                downloadCount.interrupt();
                                Collection<? extends Future> futures = downloadCount.getDownloadTaskFutures();
                                List<Future> clonedFutures = new ArrayList<>();
                                clonedFutures.addAll(futures);
                                for (Future future : clonedFutures) {
                                    if (!future.isDone()) {
                                        future.cancel(true);
                                    }
                                }
                                this.threadPoolExecutor.purge();
                                downloadCount.getDownloadListener().onDownLoadFail();
                                this.downloadedCounts.remove(downloadCount.getCountKey());
                            }
                        }
                    }
                } catch (Exception ex) {
                    Log.w("HttpDownloadManager.DeadDownloadTaskMonitorThread", Log.getStackTraceString(ex));
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
    public static class IdleConnectionMonitorThread extends Thread {
        private final HttpClientConnectionManager connMgr;
        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            this.connMgr = connMgr;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (!this.shutdown) {
                try {
                    synchronized (this) {
                        wait(5000L);
                        this.connMgr.closeExpiredConnections();
                        this.connMgr.closeIdleConnections(30L, TimeUnit.SECONDS);
                    }
                } catch (Exception ex) {
                    Log.w("HttpDownloadManager.IdleConnectionMonitorThread", Log.getStackTraceString(ex));
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

    public static void config(HttpRequestBase httpRequestBase) {
        BasicHttpParams basicHttpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(basicHttpParams, 30000);
        HttpConnectionParams.setSoTimeout(basicHttpParams, 30000);
        httpRequestBase.setParams(basicHttpParams);
    }

    private HttpDownloadManager() {
        this.connectionManager = null;
        this.idleConnectionMonitorThread = null;
        this.connectionKeepAliveStrategy = null;
        this.deadDownloadTaskMonitorThread = null;
        this.threadPoolExecutor = null;
        this.downloadedCounts = null;
        this.connectionKeepAliveStrategy = new ConnectionKeepAliveStrategy() { // from class: com.xcharge.common.utils.HttpDownloadManager.1
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                BasicHeaderElementIterator basicHeaderElementIterator = new BasicHeaderElementIterator(response.headerIterator("Keep-Alive"));
                while (basicHeaderElementIterator.hasNext()) {
                    HeaderElement he = basicHeaderElementIterator.nextElement();
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
                return 30000L;
            }
        };
        this.connectionManager = new PoolingHttpClientConnectionManager();
        this.connectionManager.setMaxTotal(100);
        this.connectionManager.setDefaultMaxPerRoute(10);
        SocketConfig defaultSocketConfig = SocketConfig.custom().setSoTimeout(30000).setSoLinger(30).build();
        this.connectionManager.setDefaultSocketConfig(defaultSocketConfig);
        this.downloadedCounts = new ConcurrentHashMap<>();
        this.threadPoolExecutor = new ThreadPoolExecutor(3, 3, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue(), new RejectedExecutionHandler() { // from class: com.xcharge.common.utils.HttpDownloadManager.2
            @Override // java.util.concurrent.RejectedExecutionHandler
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                Log.w("HttpDownloadManager.ThreadPoolExecutor.rejectedExecution", "rejected runnable: " + r.toString() + ", active runnables: " + executor.getActiveCount());
                if (r instanceof DownLoadTask) {
                    DownLoadTask downLoadTask = (DownLoadTask) r;
                    HttpDownloadManager.this.downloadedCounts.remove(downLoadTask.getDownloadCount().getCountKey());
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
        if (newNum != nowNum) {
            this.threadPoolExecutor.setCorePoolSize(newNum);
            Log.w("HttpDownloadManager.changeDownloadCoreThreadNum", "change download thread pool core number to: " + newNum);
            return true;
        }
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
            Long taskCount = Long.valueOf(fileSize / fileSize);
            long offset = 0;
            for (int i = 0; i < taskCount.longValue(); i++) {
                asyncDownload(context, url, tmpFile, toFile, offset, fileSize, downloadCount, listener);
                offset += fileSize;
            }
            long remainder = fileSize % fileSize;
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
        DownloadCount count;
        count = this.downloadedCounts.get(key);
        return count != null ? count.isInterrupted() : false;
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

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isNetWorkOk(Context context) {
        return ContextUtils.isNetworkConnected(context);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void asyncDownload(final Context context, final String url, final String tmpFile, final String toFile, long offset, long length, final DownloadCount downloadCount, final DownLoadListener listener) throws Exception {
        if (downloadCount.isInterrupted()) {
            Log.w("HttpDownloadManager.asyncDownload", "download interrupted, can not start new async download task");
            return;
        }
        DownLoadTask dlTask = new DownLoadTask(url, tmpFile, offset, length, downloadCount, new DownLoadTaskListener() { // from class: com.xcharge.common.utils.HttpDownloadManager.3
            @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadTaskListener
            public void onDownLoadProgress(long beginPos, long inc, long downloaded) {
                int percent = downloadCount.compareGetPercent();
                if (percent > 0) {
                    Log.i("HttpDownloadManager.asyncDownload.onDownLoadProgress", "downloading ...: " + percent + "%");
                    listener.onDownLoadPercentage(downloadCount.getPercent());
                }
                listener.onDownLoadPercentage(downloadCount.getCount(), downloadCount.getTotal());
            }

            @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadTaskListener
            public void onDownLoadFail(long beginPos, long downloaded, long total) {
                if (downloadCount.isInterrupted()) {
                    Log.w("HttpDownloadManager.asyncDownload.onDownLoadFail", "interrupted: " + downloadCount.getPercent());
                    listener.onDownLoadFail();
                    return;
                }
                Log.d("HttpDownloadManager.asyncDownload.onDownLoadFail", "download failed, try it again, beginPos: " + beginPos + ", downloaded: " + downloaded + ", need download length: " + total + ", total downloaded: " + downloadCount.getCount());
                downloadCount.incCount((-1) * downloaded);
                int networkCheckCnt = 0;
                while (!HttpDownloadManager.this.isNetWorkOk(context)) {
                    try {
                        if (downloadCount.isInterrupted()) {
                            Log.w("HttpDownloadManager.asyncDownload.onDownLoadFail", "no network, and interrupted: " + downloadCount.getPercent());
                            listener.onDownLoadFail();
                            return;
                        } else if (networkCheckCnt >= 12) {
                            downloadCount.interrupt();
                            Log.w("HttpDownloadManager.asyncDownload.onDownLoadFail", "no network !!! " + downloadCount.getPercent());
                            listener.onDownLoadFail();
                            return;
                        } else {
                            Thread.sleep(5000L);
                            networkCheckCnt++;
                        }
                    } catch (Exception e) {
                        Log.e("HttpDownloadManager.asyncDownload.onDownLoadFail", Log.getStackTraceString(e));
                        downloadCount.interrupt();
                        Log.w("HttpDownloadManager.asyncDownload.onDownLoadFail", "download failed: " + downloadCount.getPercent());
                        listener.onDownLoadFail();
                        return;
                    }
                }
                if (downloadCount.getRetryCnt() < 3) {
                    HttpDownloadManager.this.asyncDownload(context, url, tmpFile, toFile, beginPos, total, downloadCount, listener);
                    downloadCount.incRetryCnt();
                    Log.i("HttpDownloadManager.asyncDownload.onDownLoadFail", "try to download again, beginPos: " + beginPos + ", downloadLength: " + total + ", total downloaded: " + downloadCount.getCount() + ", retry cnt: " + downloadCount.getRetryCnt());
                    return;
                }
                downloadCount.interrupt();
                Log.w("HttpDownloadManager.asyncDownload.onDownLoadFail", "download failed over 3 times: " + downloadCount.getPercent());
                listener.onDownLoadFail();
            }

            @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadTaskListener
            public void onDownLoadComplete(long beginPos, long downloaded) {
                if (downloadCount.isCompleted()) {
                    downloadCount.interrupt();
                    HttpDownloadManager.this.renameDownloadedFile(tmpFile, toFile);
                    Log.i("HttpDownloadManager.asyncDownload.onDownLoadComplete", "download completed: " + downloadCount.getCount());
                    listener.onDownLoadComplete();
                    return;
                }
                Log.i("HttpDownloadManager.asyncDownload.onDownLoadComplete", "partial download completed, countinue..., beginPos: " + beginPos + ", downloaded: " + downloaded + ", total downloaded: " + downloadCount.getCount());
            }
        });
        Future future = this.threadPoolExecutor.submit(dlTask);
        downloadCount.addDownloadTaskFuture(future);
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
                fileSize = httpConnection.getContentLength();
                Log.i("HttpDownloadManager.getFileSize", "file (" + fileUrl + ") size: " + fileSize);
            } else {
                Log.w("HttpDownloadManager.getFileSize", "response code is not OK: " + responseCode + ", fileUrl: " + fileUrl);
            }
            return fileSize;
        } catch (Exception e) {
            Log.e("HttpDownloadManager.getFileSize", "Exception stack: " + Log.getStackTraceString(e) + ", fileUrl: " + fileUrl);
            return 0L;
        }
    }

    private void createFile(String fileName, long fileSize) throws Exception {
        try {
            File newFile = new File(fileName);
            RandomAccessFile raf = new RandomAccessFile(newFile, "rw");
            raf.setLength(fileSize);
            raf.close();
        } catch (Exception e) {
            Log.e("HttpDownloadManager.createFile", "Exception stack: " + Log.getStackTraceString(e) + ", fileName: " + fileName + ", fileSize: " + fileSize);
            throw e;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
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
            return new DownloadCount(0L);
        }
        return count;
    }

    private void incCount(String key, long inc) {
        DownloadCount count = this.downloadedCounts.get(key);
        if (count != null) {
            count.incCount(inc);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class DownLoadTask implements Runnable {
        private long beginPos;
        DownloadCount downloadCount;
        private long downloadLength;
        private String fileName;
        private String fileUrl;
        private DownLoadTaskListener listener;

        public DownLoadTask(String fileUrl, String fileName, long beginPos, long downloadLength, DownloadCount downloadCount, DownLoadTaskListener listener) {
            this.fileUrl = null;
            this.fileName = null;
            this.beginPos = 0L;
            this.downloadLength = 0L;
            this.downloadCount = null;
            this.listener = null;
            this.fileUrl = fileUrl;
            this.fileName = fileName;
            this.beginPos = beginPos;
            this.downloadLength = downloadLength;
            this.downloadCount = downloadCount;
            this.listener = listener;
        }

        public DownloadCount getDownloadCount() {
            return this.downloadCount;
        }

        @Override // java.lang.Runnable
        public void run() {
            downloadFile();
        }

        /* JADX WARN: Removed duplicated region for block: B:20:0x0136  */
        /* JADX WARN: Removed duplicated region for block: B:52:? A[RETURN, SYNTHETIC] */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        public void downloadFile() {
            /*
                Method dump skipped, instructions count: 501
                To view this dump change 'Code comments level' option to 'DEBUG'
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.common.utils.HttpDownloadManager.DownLoadTask.downloadFile():void");
        }
    }
}