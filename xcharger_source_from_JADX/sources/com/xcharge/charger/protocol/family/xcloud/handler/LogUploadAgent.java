package com.xcharge.charger.protocol.family.xcloud.handler;

import android.content.Context;
import android.util.Log;
import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.QueryLog;
import com.xcharge.common.utils.FileUtils;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.ZipUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.CharEncoding;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.http.util.TextUtils;
import org.apache.mina.proxy.handlers.http.HttpProxyConstants;
import org.json.JSONObject;

public class LogUploadAgent {
    private static final String CHARSET = "utf-8";
    private static final String LOGCAT_LOG_PATH = "/data/data/com.xcharge.charger/logcat.log";
    private static final int TIME_OUT = 30000;
    private static LogUploadAgent instance = null;
    private Context context = null;
    private ThreadPoolExecutor uploadThreadPoolExecutor = null;

    public static LogUploadAgent getInstance() {
        if (instance == null) {
            instance = new LogUploadAgent();
        }
        return instance;
    }

    public void init(Context context2) {
        this.context = context2;
        this.uploadThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue(1), new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                Log.w("LogUploadAgent.ThreadPoolExecutor.rejectedExecution", "rejected runnable: " + r.toString() + ", active runnables: " + executor.getActiveCount());
                if (r instanceof LogUploadTask) {
                    LogUtils.cloudlog("in handle upload log files, reject new log query request !!!");
                    XCloudProtocolAgent.getInstance().sendUploadLog(((LogUploadTask) r).getQueryLog().getSid(), new DeviceError(DeviceError.NOT_IDLE, "upload now", (Object) null));
                }
            }
        });
    }

    public void destroy() {
        this.uploadThreadPoolExecutor.shutdown();
    }

    public void upload(QueryLog queryLog) {
        this.uploadThreadPoolExecutor.execute(new LogUploadTask(queryLog));
    }

    private class LogUploadTask implements Runnable {
        private QueryLog queryLog = null;

        public LogUploadTask(QueryLog queryLog2) {
            this.queryLog = queryLog2;
        }

        public QueryLog getQueryLog() {
            return this.queryLog;
        }

        public void run() {
            try {
                Log.i("LogUploadAgent.LogUploadTask", "exec shell cmd, ret: " + FileUtils.execShell("rm -rf " + "/data/data/com.xcharge.charger/logcat.log /data/data/com.xcharge.charger/logcat.log.zip", 4) + ", script: " + "/data/data/com.xcharge.charger/logcat.log /data/data/com.xcharge.charger/logcat.log.zip");
            } catch (Exception e) {
                Log.e("LogUploadAgent.LogUploadTask", Log.getStackTraceString(e));
            }
            DeviceError error = null;
            LogUploadAgent.this.readLogcat();
            try {
                List<File> fileList = LogUploadAgent.this.zipLogs();
                if (fileList == null || fileList.size() == 0) {
                    Log.e("LogUploadAgent.LogUploadTask", "no log file !!!");
                    LogUtils.cloudlog("no log file for upload !!!");
                    error = new DeviceError("ERROR", "No log file now", (Object) null);
                    try {
                        Log.i("LogUploadAgent.LogUploadTask", "exec shell cmd, ret: " + FileUtils.execShell("rm -rf " + "/data/data/com.xcharge.charger/logcat.log /data/data/com.xcharge.charger/logcat.log.zip", 4) + ", script: " + "/data/data/com.xcharge.charger/logcat.log /data/data/com.xcharge.charger/logcat.log.zip");
                    } catch (Exception e2) {
                        Log.e("LogUploadAgent.LogUploadTask", Log.getStackTraceString(e2));
                    }
                    XCloudProtocolAgent.getInstance().sendUploadLog(this.queryLog.getSid(), error);
                }
                if (!LogUploadAgent.this.uploadLog(this.queryLog.getUploadUrl(), fileList)) {
                    Log.e("LogUploadAgent.LogUploadTask", "upload fail !!!");
                    error = new DeviceError("ERROR", "upload fail", (Object) null);
                }
                Log.i("LogUploadAgent.LogUploadTask", "exec shell cmd, ret: " + FileUtils.execShell("rm -rf " + "/data/data/com.xcharge.charger/logcat.log /data/data/com.xcharge.charger/logcat.log.zip", 4) + ", script: " + "/data/data/com.xcharge.charger/logcat.log /data/data/com.xcharge.charger/logcat.log.zip");
                XCloudProtocolAgent.getInstance().sendUploadLog(this.queryLog.getSid(), error);
            } catch (Exception e3) {
                Log.e("LogUploadAgent.LogUploadTask", Log.getStackTraceString(e3));
                LogUtils.cloudlog("zip log files exception: " + Log.getStackTraceString(e3));
                error = new DeviceError("ERROR", "zip log files error", (Object) null);
            }
        }
    }

    /* access modifiers changed from: private */
    public void readLogcat() {
        try {
            FileUtils.execShell("logcat -b main -b system -v time -d *:d > /data/data/com.xcharge.charger/logcat.log", 4);
        } catch (Exception e) {
            Log.w("LogUploadAgent.readLog", Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
    public List<File> zipLogs() throws Exception {
        List<File> resFileList = new ArrayList<>();
        File logcatFile = new File(LOGCAT_LOG_PATH);
        if (logcatFile.exists()) {
            resFileList.add(logcatFile);
        }
        File crashLogcatFile = new File("/data/data/com.xcharge.charger/logcat.log.crash");
        if (crashLogcatFile.exists()) {
            resFileList.add(crashLogcatFile);
        }
        try {
            File traceFiles = new File("/data/chargerhd");
            if (!traceFiles.exists() || !traceFiles.isDirectory()) {
                Log.w("LogUploadAgent.zipLogs", "/data/chargerhd is not exist !!!");
                LogUtils.cloudlog("/data/chargerhd is not exist !!!");
            } else {
                File[] files = traceFiles.listFiles();
                if (files == null || files.length <= 0) {
                    Log.w("LogUploadAgent.zipLogs", "not any traces in /data/chargerhd !!!");
                    LogUtils.cloudlog("failed to get log files from /data/chargerhd !!!");
                } else {
                    for (File file : files) {
                        if (file.getName().endsWith(".trace")) {
                            resFileList.add(file);
                        }
                    }
                }
            }
            File applogFile = new File("/data/data/com.xcharge.charger/logs/charger_app.log");
            File applogFile1 = new File("/data/data/com.xcharge.charger/logs/charger_app.log.1");
            File applogFile2 = new File("/data/data/com.xcharge.charger/logs/charger_app.log.2");
            if (applogFile.exists()) {
                resFileList.add(applogFile);
                if (applogFile1.exists()) {
                    resFileList.add(applogFile1);
                }
            } else if (!applogFile1.exists()) {
                Log.w("LogUploadAgent.zipLogs", "not any logs in /data/data/com.xcharge.charger/logs !!!");
                LogUtils.cloudlog("failed to get log files from /data/data/com.xcharge.charger/logs !!!");
            } else {
                resFileList.add(applogFile1);
                if (applogFile2.exists()) {
                    resFileList.add(applogFile2);
                }
            }
            File anrLogFile = new File("data/anr/traces.txt");
            if (anrLogFile.exists()) {
                resFileList.add(anrLogFile);
            }
            File dropboxFiles = new File("/data/system/dropbox");
            if (!dropboxFiles.exists() || !dropboxFiles.isDirectory()) {
                Log.d("LogUploadAgent.zipLogs", "/data/system/dropbox is not exist !!!");
            } else {
                File[] files2 = dropboxFiles.listFiles();
                if (files2 == null || files2.length <= 0) {
                    Log.d("LogUploadAgent.zipLogs", "not any traces in /data/system/dropbox !!!");
                } else {
                    for (File file2 : files2) {
                        resFileList.add(file2);
                    }
                }
            }
            File tombstoneFiles = new File("/data/tombstones");
            if (!tombstoneFiles.exists() || !tombstoneFiles.isDirectory()) {
                Log.d("LogUploadAgent.zipLogs", "/data/tombstones is not exist !!!");
                ZipUtils.zipFiles(resFileList, new File("/data/data/com.xcharge.charger/logcat.log.zip"));
                return resFileList;
            }
            File[] files3 = tombstoneFiles.listFiles();
            if (files3 == null || files3.length <= 0) {
                Log.d("LogUploadAgent.zipLogs", "not any traces in /data/tombstones !!!");
                ZipUtils.zipFiles(resFileList, new File("/data/data/com.xcharge.charger/logcat.log.zip"));
                return resFileList;
            }
            for (File file3 : files3) {
                resFileList.add(file3);
            }
            ZipUtils.zipFiles(resFileList, new File("/data/data/com.xcharge.charger/logcat.log.zip"));
            return resFileList;
        } catch (Exception e) {
            Log.w("LogUploadAgent.zipLogs", Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
    public boolean uploadLog(String RequestURL, List<File> fileList) {
        if (uploadFile(new File("/data/data/com.xcharge.charger/logcat.log.zip"), RequestURL, fileList)) {
            return true;
        }
        Log.w("LogUploadAgent.uploadLog", "failed to upload, try it again !!!");
        return uploadFile(new File("/data/data/com.xcharge.charger/logcat.log.zip"), RequestURL, fileList);
    }

    /* access modifiers changed from: private */
    public boolean uploadFile(File file, String RequestURL, List<File> list) {
        String BOUNDARY = UUID.randomUUID().toString();
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(RequestURL).openConnection();
            conn.setReadTimeout(30000);
            conn.setConnectTimeout(30000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod(HttpPostHC4.METHOD_NAME);
            conn.setRequestProperty("Charset", "utf-8");
            conn.setRequestProperty(HttpHeaders.CONNECTION, "Keep-Alive");
            conn.setRequestProperty("Content-Type", String.valueOf("multipart/form-data") + ";boundary=" + BOUNDARY);
            if (file != null) {
                conn.setChunkedStreamingMode(65536);
                BufferedOutputStream dos = new BufferedOutputStream(conn.getOutputStream(), 65536);
                StringBuffer sb = new StringBuffer();
                sb.append("--");
                sb.append(BOUNDARY);
                sb.append(HttpProxyConstants.CRLF);
                sb.append("Content-Type: application/octet-stream; charset=utf-8" + HttpProxyConstants.CRLF);
                sb.append(HttpProxyConstants.CRLF);
                dos.write(sb.toString().getBytes());
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                while (true) {
                    int len = is.read(bytes);
                    if (len == -1) {
                        break;
                    }
                    dos.write(bytes, 0, len);
                }
                is.close();
                dos.write(HttpProxyConstants.CRLF.getBytes());
                dos.write((String.valueOf("--") + BOUNDARY + "--" + HttpProxyConstants.CRLF).getBytes());
                dos.flush();
                int res = conn.getResponseCode();
                Log.d("LogUploadAgent.uploadFile", "response code:" + res);
                InputStream input = conn.getInputStream();
                StringBuffer sb1 = new StringBuffer();
                while (true) {
                    int ss = input.read();
                    if (ss == -1) {
                        break;
                    }
                    sb1.append((char) ss);
                }
                Log.d("LogUploadAgent.uploadFile", "response body:" + sb1.toString());
                if (res == 200) {
                    return true;
                }
                LogUtils.cloudlog("failed to upload log file, and http response: " + res);
            }
        } catch (Exception e) {
            Log.w("LogUploadAgent.uploadFile", Log.getStackTraceString(e));
            LogUtils.cloudlog("upload log file exception: " + Log.getStackTraceString(e));
        }
        return false;
    }

    /* access modifiers changed from: private */
    public String queryLogByBillId(String path, String billId) {
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files.length == 0) {
                return null;
            }
            for (File file2 : files) {
                if (file2.isDirectory()) {
                    String billFile = queryLogByBillId(file2.getAbsolutePath(), billId);
                    if (!TextUtils.isEmpty(billFile)) {
                        return billFile;
                    }
                } else if (file2.getName().endsWith(".trace") && file2.getName().contains(billId)) {
                    return file2.getAbsolutePath();
                }
            }
        }
        return null;
    }

    public void uploadBillLog(final String billId, final String url) {
        new Thread(new Runnable() {
            public void run() {
                File billFile = null;
                String billFilePath = LogUploadAgent.this.queryLogByBillId("/data/chargerhd", billId);
                if (!TextUtils.isEmpty(billFilePath)) {
                    billFile = new File(billFilePath);
                }
                if (billFile == null) {
                    return;
                }
                if (TextUtils.isEmpty(url)) {
                    LogUploadAgent.this.upload2AliOSS("billLog/" + HardwareStatusCacheProvider.getInstance().getSn() + "_" + billFile.getName(), billFilePath);
                    return;
                }
                boolean unused = LogUploadAgent.this.uploadFile(billFile, url, (List<File>) null);
            }
        }).start();
    }

    /* access modifiers changed from: private */
    public void upload2AliOSS(String objectKey, String uploadFile) {
        JSONObject acParams = getAliOSSAccessParams();
        if (acParams == null) {
            Log.w("LogUploadAgent.upload2AliOSS", "failed to get aliOSS access params !");
            return;
        }
        try {
            String endpoint = acParams.getString("endpoint");
            String accessKeyId = acParams.getString("AccessKeyId");
            String accessKeySecret = acParams.getString("AccessKeySecret");
            String securityToken = acParams.getString("SecurityToken");
            String bucketId = acParams.getString("bucketId");
            ClientConfiguration conf = new ClientConfiguration();
            conf.setConnectionTimeout(15000);
            conf.setSocketTimeout(15000);
            conf.setMaxConcurrentRequest(5);
            conf.setMaxErrorRetry(2);
            OSS oss = new OSSClient(this.context, endpoint, new OSSStsTokenCredentialProvider(accessKeyId, accessKeySecret, securityToken), conf);
            PutObjectRequest put = new PutObjectRequest(bucketId, objectKey, uploadFile);
            put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
                public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                    Log.d("LogUploadAgent.upload2AliOSS", "currentSize: " + currentSize + " totalSize: " + totalSize);
                }
            });
            oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
                public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                    Log.d("LogUploadAgent.upload2AliOSS", "UploadSuccess, ETag: " + result.getETag() + ", RequestId: " + result.getRequestId());
                }

                public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                    if (clientExcepion != null) {
                        Log.e("LogUploadAgent.upload2AliOSS", Log.getStackTraceString(clientExcepion));
                    }
                    if (serviceException != null) {
                        Log.e("LogUploadAgent.upload2AliOSS", "ErrorCode: " + serviceException.getErrorCode() + ", RequestId: " + serviceException.getRequestId() + ", HostId: " + serviceException.getHostId() + ", RawMessage: " + serviceException.getRawMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e("LogUploadAgent.upload2AliOSS", Log.getStackTraceString(e));
        }
    }

    private JSONObject getAliOSSAccessParams() {
        try {
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 10000);
            HttpConnectionParams.setSoTimeout(params, 10000);
            DefaultHttpClient httpClient = new DefaultHttpClient(params);
            HttpGet request = new HttpGet("http://oss.yzxtech.net/charge/user/config/alioss?bucketId=c2log");
            request.setHeader(HttpHeaders.CONNECTION, "Close");
            request.setHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            HttpEntity entity = httpClient.execute(request).getEntity();
            if (entity != null) {
                JSONObject resp = new JSONObject(EntityUtils.toString(entity, CharEncoding.UTF_8));
                if (resp.getInt("code") == 200) {
                    Log.d("LogUploadAgent.getAliOSSAccessParams", "ok response: " + resp.toString());
                    return resp.getJSONObject("data");
                }
                Log.w("LogUploadAgent.getAliOSSAccessParams", "response error: " + resp.toString());
            }
        } catch (Exception e) {
            Log.e("LogUploadAgent.getAliOSSAccessParams", Log.getStackTraceString(e));
        }
        return null;
    }

    static class CompratorByLastModified implements Comparator<File> {
        CompratorByLastModified() {
        }

        public int compare(File f1, File f2) {
            long diff = f1.lastModified() - f2.lastModified();
            if (diff > 0) {
                return 1;
            }
            if (diff == 0) {
                return 0;
            }
            return -1;
        }
    }
}
