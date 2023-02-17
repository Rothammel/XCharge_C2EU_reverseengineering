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
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
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
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.apache.http.util.TextUtils;
import org.apache.mina.proxy.handlers.http.HttpProxyConstants;
import org.json.JSONObject;

/* loaded from: classes.dex */
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

    public void init(Context context) {
        this.context = context;
        this.uploadThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue(1), new RejectedExecutionHandler() { // from class: com.xcharge.charger.protocol.family.xcloud.handler.LogUploadAgent.1
            {
                LogUploadAgent.this = this;
            }

            @Override // java.util.concurrent.RejectedExecutionHandler
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                Log.w("LogUploadAgent.ThreadPoolExecutor.rejectedExecution", "rejected runnable: " + r.toString() + ", active runnables: " + executor.getActiveCount());
                if (r instanceof LogUploadTask) {
                    LogUploadTask logUploadTask = (LogUploadTask) r;
                    LogUtils.cloudlog("in handle upload log files, reject new log query request !!!");
                    XCloudProtocolAgent.getInstance().sendUploadLog(logUploadTask.getQueryLog().getSid(), new DeviceError(DeviceError.NOT_IDLE, "upload now", null));
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

    /* loaded from: classes.dex */
    public class LogUploadTask implements Runnable {
        private QueryLog queryLog;

        public LogUploadTask(QueryLog queryLog) {
            LogUploadAgent.this = r2;
            this.queryLog = null;
            this.queryLog = queryLog;
        }

        public QueryLog getQueryLog() {
            return this.queryLog;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                int ret = FileUtils.execShell("rm -rf /data/data/com.xcharge.charger/logcat.log /data/data/com.xcharge.charger/logcat.log.zip", 4);
                Log.i("LogUploadAgent.LogUploadTask", "exec shell cmd, ret: " + ret + ", script: /data/data/com.xcharge.charger/logcat.log /data/data/com.xcharge.charger/logcat.log.zip");
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
                    error = new DeviceError("ERROR", "No log file now", null);
                } else if (!LogUploadAgent.this.uploadLog(this.queryLog.getUploadUrl(), fileList)) {
                    Log.e("LogUploadAgent.LogUploadTask", "upload fail !!!");
                    error = new DeviceError("ERROR", "upload fail", null);
                }
            } catch (Exception e2) {
                Log.e("LogUploadAgent.LogUploadTask", Log.getStackTraceString(e2));
                LogUtils.cloudlog("zip log files exception: " + Log.getStackTraceString(e2));
                error = new DeviceError("ERROR", "zip log files error", null);
            }
            try {
                int ret2 = FileUtils.execShell("rm -rf /data/data/com.xcharge.charger/logcat.log /data/data/com.xcharge.charger/logcat.log.zip", 4);
                Log.i("LogUploadAgent.LogUploadTask", "exec shell cmd, ret: " + ret2 + ", script: /data/data/com.xcharge.charger/logcat.log /data/data/com.xcharge.charger/logcat.log.zip");
            } catch (Exception e3) {
                Log.e("LogUploadAgent.LogUploadTask", Log.getStackTraceString(e3));
            }
            XCloudProtocolAgent.getInstance().sendUploadLog(this.queryLog.getSid(), error);
        }
    }

    public void readLogcat() {
        try {
            FileUtils.execShell("logcat -b main -b system -v time -d *:d > /data/data/com.xcharge.charger/logcat.log", 4);
        } catch (Exception e) {
            Log.w("LogUploadAgent.readLog", Log.getStackTraceString(e));
        }
    }

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
            if (traceFiles.exists() && traceFiles.isDirectory()) {
                File[] files = traceFiles.listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        if (file.getName().endsWith(".trace")) {
                            resFileList.add(file);
                        }
                    }
                } else {
                    Log.w("LogUploadAgent.zipLogs", "not any traces in /data/chargerhd !!!");
                    LogUtils.cloudlog("failed to get log files from /data/chargerhd !!!");
                }
            } else {
                Log.w("LogUploadAgent.zipLogs", "/data/chargerhd is not exist !!!");
                LogUtils.cloudlog("/data/chargerhd is not exist !!!");
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
            if (dropboxFiles.exists() && dropboxFiles.isDirectory()) {
                File[] files2 = dropboxFiles.listFiles();
                if (files2 != null && files2.length > 0) {
                    for (File file2 : files2) {
                        resFileList.add(file2);
                    }
                } else {
                    Log.d("LogUploadAgent.zipLogs", "not any traces in /data/system/dropbox !!!");
                }
            } else {
                Log.d("LogUploadAgent.zipLogs", "/data/system/dropbox is not exist !!!");
            }
            File tombstoneFiles = new File("/data/tombstones");
            if (tombstoneFiles.exists() && tombstoneFiles.isDirectory()) {
                File[] files3 = tombstoneFiles.listFiles();
                if (files3 != null && files3.length > 0) {
                    for (File file3 : files3) {
                        resFileList.add(file3);
                    }
                } else {
                    Log.d("LogUploadAgent.zipLogs", "not any traces in /data/tombstones !!!");
                }
            } else {
                Log.d("LogUploadAgent.zipLogs", "/data/tombstones is not exist !!!");
            }
        } catch (Exception e) {
            Log.w("LogUploadAgent.zipLogs", Log.getStackTraceString(e));
        }
        ZipUtils.zipFiles(resFileList, new File("/data/data/com.xcharge.charger/logcat.log.zip"));
        return resFileList;
    }

    public boolean uploadLog(String RequestURL, List<File> fileList) {
        if (uploadFile(new File("/data/data/com.xcharge.charger/logcat.log.zip"), RequestURL, fileList)) {
            return true;
        }
        Log.w("LogUploadAgent.uploadLog", "failed to upload, try it again !!!");
        return uploadFile(new File("/data/data/com.xcharge.charger/logcat.log.zip"), RequestURL, fileList);
    }

    public boolean uploadFile(File file, String RequestURL, List<File> fileList) {
        String BOUNDARY = UUID.randomUUID().toString();
        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
                byte[] end_data = (String.valueOf("--") + BOUNDARY + "--" + HttpProxyConstants.CRLF).getBytes();
                dos.write(end_data);
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
                String result = sb1.toString();
                Log.d("LogUploadAgent.uploadFile", "response body:" + result);
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
        new Thread(new Runnable() { // from class: com.xcharge.charger.protocol.family.xcloud.handler.LogUploadAgent.2
            {
                LogUploadAgent.this = this;
            }

            @Override // java.lang.Runnable
            public void run() {
                File billFile = null;
                String billFilePath = LogUploadAgent.this.queryLogByBillId("/data/chargerhd", billId);
                if (!TextUtils.isEmpty(billFilePath)) {
                    billFile = new File(billFilePath);
                }
                if (billFile != null) {
                    if (!TextUtils.isEmpty(url)) {
                        LogUploadAgent.this.uploadFile(billFile, url, null);
                        return;
                    }
                    String objectKey = "billLog/" + HardwareStatusCacheProvider.getInstance().getSn() + "_" + billFile.getName();
                    LogUploadAgent.this.upload2AliOSS(objectKey, billFilePath);
                }
            }
        }).start();
    }

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
            OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(accessKeyId, accessKeySecret, securityToken);
            OSS oss = new OSSClient(this.context, endpoint, credentialProvider, conf);
            PutObjectRequest put = new PutObjectRequest(bucketId, objectKey, uploadFile);
            put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() { // from class: com.xcharge.charger.protocol.family.xcloud.handler.LogUploadAgent.3
                {
                    LogUploadAgent.this = this;
                }

                @Override // com.alibaba.sdk.android.oss.callback.OSSProgressCallback
                public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                    Log.d("LogUploadAgent.upload2AliOSS", "currentSize: " + currentSize + " totalSize: " + totalSize);
                }
            });
            oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() { // from class: com.xcharge.charger.protocol.family.xcloud.handler.LogUploadAgent.4
                {
                    LogUploadAgent.this = this;
                }

                @Override // com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
                public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                    Log.d("LogUploadAgent.upload2AliOSS", "UploadSuccess, ETag: " + result.getETag() + ", RequestId: " + result.getRequestId());
                }

                @Override // com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
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
            BasicHttpParams basicHttpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(basicHttpParams, 10000);
            HttpConnectionParams.setSoTimeout(basicHttpParams, 10000);
            DefaultHttpClient httpClient = new DefaultHttpClient(basicHttpParams);
            HttpGet request = new HttpGet("http://oss.yzxtech.net/charge/user/config/alioss?bucketId=c2log");
            request.setHeader(HttpHeaders.CONNECTION, "Close");
            request.setHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity, CharEncoding.UTF_8);
                JSONObject resp = new JSONObject(result);
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

    /* loaded from: classes.dex */
    static class CompratorByLastModified implements Comparator<File> {
        CompratorByLastModified() {
        }

        @Override // java.util.Comparator
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
