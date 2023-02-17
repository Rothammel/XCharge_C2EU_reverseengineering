package com.xcharge.charger.protocol.ocpp.handler;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.protocol.ocpp.bean.cloud.GetDiagnosticsReq;
import com.xcharge.charger.protocol.ocpp.bean.types.DiagnosticsStatus;
import com.xcharge.common.utils.FileUtils;
import com.xcharge.common.utils.FtpUtils;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.ZipUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.mina.proxy.handlers.http.HttpProxyConstants;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.json.JSONArray;

/* loaded from: classes.dex */
public class LogUploadAgent {
    private static final String CHARSET = "utf-8";
    private static final String LOGCAT_LOG_PATH = "/data/data/com.xcharge.charger/logcat.log";
    private static final int TIME_OUT = 30000;
    private static LogUploadAgent instance = null;
    private String logcat_log_name;
    private String logcat_log_path;
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
        this.uploadThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue(1), new RejectedExecutionHandler() { // from class: com.xcharge.charger.protocol.ocpp.handler.LogUploadAgent.1
            @Override // java.util.concurrent.RejectedExecutionHandler
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                Log.w("LogUploadAgent.ThreadPoolExecutor.rejectedExecution", "rejected runnable: " + r.toString() + ", active runnables: " + executor.getActiveCount());
                if (r instanceof LogUploadTask) {
                    LogUploadTask logUploadTask = (LogUploadTask) r;
                    LogUtils.cloudlog("in handle upload log files, reject new log query request !!!");
                    OcppProtocolAgent.getInstance().sendDiagnosticsStatusNotificationReq(false, DiagnosticsStatus.UploadFailed);
                }
            }
        });
    }

    public void destroy() {
        this.uploadThreadPoolExecutor.shutdown();
    }

    public void upload(JSONArray jsonArray) {
        this.uploadThreadPoolExecutor.execute(new LogUploadTask(jsonArray));
    }

    /* loaded from: classes.dex */
    private class LogUploadTask implements Runnable {
        private JSONArray jsonArray;

        public LogUploadTask(JSONArray jsonArray) {
            this.jsonArray = null;
            this.jsonArray = jsonArray;
        }

        public JSONArray getJsonArray() {
            return this.jsonArray;
        }

        @Override // java.lang.Runnable
        public void run() {
            LogUploadAgent.this.logcat_log_name = String.valueOf(HardwareStatusCacheProvider.getInstance().getSn()) + "_" + System.currentTimeMillis();
            LogUploadAgent.this.logcat_log_path = "/data/data/com.xcharge.charger/" + LogUploadAgent.this.logcat_log_name;
            LogUploadAgent.this.readLogcat();
            try {
                List<File> fileList = LogUploadAgent.this.zipLogs();
                if (fileList != null && fileList.size() != 0) {
                    OcppProtocolAgent.getInstance().sendGetDiagnosticsConf(this.jsonArray.getString(1), String.valueOf(LogUploadAgent.this.logcat_log_name) + ".zip");
                    LogUploadAgent.this.uploadLog(this.jsonArray, fileList);
                } else {
                    Log.e("LogUploadAgent.LogUploadTask", "no log file !!!");
                    LogUtils.cloudlog("no log file for upload !!!");
                    OcppProtocolAgent.getInstance().sendGetDiagnosticsConf(this.jsonArray.getString(1), null);
                }
            } catch (Exception e) {
                Log.e("LogUploadAgent.LogUploadTask", Log.getStackTraceString(e));
                LogUtils.cloudlog("zip log files exception: " + Log.getStackTraceString(e));
                LogUploadAgent.this.deleteFile();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void readLogcat() {
        try {
            FileUtils.execShell("logcat -b main -b system -v time -d *:d > /data/data/com.xcharge.charger/logcat.log", 4);
        } catch (Exception e) {
            Log.w("LogUploadAgent.readLog", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
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
        ZipUtils.zipFiles(resFileList, new File(String.valueOf(this.logcat_log_path) + ".zip"));
        return resFileList;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void uploadLog(JSONArray jsonArray, List<File> fileList) {
        try {
            String location = new GetDiagnosticsReq().fromJson(jsonArray.getJSONObject(3).toString()).getLocation();
            String scheme = new URI(location).getScheme();
            Boolean type = OcppProtocolAgent.getInstance().isSupported(scheme);
            if (type == null || type.booleanValue()) {
                if (OcppProtocolAgent.getInstance().isHttp(scheme)) {
                    httpUploadFile(new File(String.valueOf(this.logcat_log_path) + ".zip"), location, fileList);
                } else {
                    ftpUploadFile(String.valueOf(this.logcat_log_path) + ".zip", location, fileList);
                }
            } else {
                OcppProtocolAgent.getInstance().sendGetDiagnosticsConf(jsonArray.getString(1), null);
            }
        } catch (Exception e) {
            Log.w("LogUploadAgent.uploadLog", Log.getStackTraceString(e));
            deleteFile();
        }
    }

    private void httpUploadFile(File file, String RequestURL, List<File> fileList) {
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
                OcppProtocolAgent.getInstance().sendDiagnosticsStatusNotificationReq(false, DiagnosticsStatus.Uploading);
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
                    OcppProtocolAgent.getInstance().sendDiagnosticsStatusNotificationReq(false, DiagnosticsStatus.Uploaded);
                } else {
                    LogUtils.cloudlog("failed to upload log file, and http response: " + res);
                    OcppProtocolAgent.getInstance().sendDiagnosticsStatusNotificationReq(false, DiagnosticsStatus.UploadFailed);
                }
                deleteFile();
            }
        } catch (Exception e) {
            Log.w("LogUploadAgent.httpUploadFile", Log.getStackTraceString(e));
            LogUtils.cloudlog("upload log file exception: " + Log.getStackTraceString(e));
            deleteFile();
            OcppProtocolAgent.getInstance().sendDiagnosticsStatusNotificationReq(false, DiagnosticsStatus.UploadFailed);
        }
    }

    private void ftpUploadFile(String file, String requestURL, List<File> fileList) {
        String username = null;
        String password = null;
        String host = null;
        String path = null;
        try {
            if (!TextUtils.isEmpty(requestURL)) {
                URI uri = new URI(requestURL);
                String[] login = uri.getRawUserInfo().split(":");
                username = login[0];
                password = login[1];
                host = uri.getHost();
                path = uri.getPath();
                if (-1 != -1) {
                    host = String.valueOf(host) + ":-1";
                }
            }
            FtpUtils.FtpConfig cfg = new FtpUtils.FtpConfig();
            cfg.setHost(host);
            cfg.setUsername(username);
            cfg.setPassword(password);
            FtpUtils.upload(file, String.valueOf(path) + MqttTopic.TOPIC_LEVEL_SEPARATOR + this.logcat_log_name + ".zip", cfg, new FtpUtils.TransferListener() { // from class: com.xcharge.charger.protocol.ocpp.handler.LogUploadAgent.2
                @Override // com.xcharge.common.utils.FtpUtils.TransferListener
                public void onConnected() {
                }

                @Override // com.xcharge.common.utils.FtpUtils.TransferListener
                public void onConnectFail() {
                    LogUploadAgent.this.deleteFile();
                    OcppProtocolAgent.getInstance().sendDiagnosticsStatusNotificationReq(false, DiagnosticsStatus.UploadFailed);
                }

                @Override // com.xcharge.common.utils.FtpUtils.TransferListener
                public void onTransferPercentage(int percent) {
                }

                @Override // com.xcharge.common.utils.FtpUtils.TransferListener
                public void onTransferPercentage(long downloaded, long total) {
                }

                @Override // com.xcharge.common.utils.FtpUtils.TransferListener
                public void onTransferComplete() {
                    LogUploadAgent.this.deleteFile();
                    OcppProtocolAgent.getInstance().sendDiagnosticsStatusNotificationReq(false, DiagnosticsStatus.Uploaded);
                }

                @Override // com.xcharge.common.utils.FtpUtils.TransferListener
                public void onTransferFail() {
                    LogUploadAgent.this.deleteFile();
                    OcppProtocolAgent.getInstance().sendDiagnosticsStatusNotificationReq(false, DiagnosticsStatus.UploadFailed);
                }
            });
        } catch (Exception e) {
            Log.w("LogUploadAgent.ftpUploadFile", Log.getStackTraceString(e));
            deleteFile();
            OcppProtocolAgent.getInstance().sendDiagnosticsStatusNotificationReq(false, DiagnosticsStatus.UploadFailed);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void deleteFile() {
        String script = String.valueOf(this.logcat_log_path) + StringUtils.SPACE + this.logcat_log_path + ".zip";
        try {
            int ret = FileUtils.execShell("rm -rf " + script, 4);
            Log.i("LogUploadAgent.deleteFile", "exec shell cmd, ret: " + ret + ", script: " + script);
        } catch (Exception e) {
            Log.e("LogUploadAgent.deleteFile", Log.getStackTraceString(e));
        }
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