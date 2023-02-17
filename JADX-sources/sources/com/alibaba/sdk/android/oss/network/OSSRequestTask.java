package com.alibaba.sdk.android.oss.network;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.CRC64;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.internal.OSSRetryHandler;
import com.alibaba.sdk.android.oss.internal.OSSRetryType;
import com.alibaba.sdk.android.oss.internal.RequestMessage;
import com.alibaba.sdk.android.oss.internal.ResponseMessage;
import com.alibaba.sdk.android.oss.internal.ResponseParser;
import com.alibaba.sdk.android.oss.internal.ResponseParsers;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.OSSResult;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.CheckedInputStream;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpHeadHC4;

/* loaded from: classes.dex */
public class OSSRequestTask<T extends OSSResult> implements Callable<T> {
    private OkHttpClient client;
    private ExecutionContext context;
    private int currentRetryCount = 0;
    private RequestMessage message;
    private ResponseParser<T> responseParser;
    private OSSRetryHandler retryHandler;

    public OSSRequestTask(RequestMessage message, ResponseParser parser, ExecutionContext context, int maxRetry) {
        this.responseParser = parser;
        this.message = message;
        this.context = context;
        this.client = context.getClient();
        this.retryHandler = new OSSRetryHandler(maxRetry);
    }

    @Override // java.util.concurrent.Callable
    public T call() throws Exception {
        Exception exception;
        OSSRequest ossRequest;
        InputStream inputStream;
        Request request = null;
        ResponseMessage responseMessage = null;
        Exception exception2 = null;
        Call call = null;
        try {
            if (this.context.getApplicationContext() != null) {
                OSSLog.logInfo(OSSUtils.buildBaseLogInfo(this.context.getApplicationContext()));
            }
            OSSLog.logDebug("[call] - ");
            ossRequest = this.context.getRequest();
            OSSUtils.ensureRequestValid(ossRequest, this.message);
            OSSUtils.signRequest(this.message);
        } catch (Exception e) {
            OSSLog.logError("Encounter local execpiton: " + e.toString());
            if (OSSLog.isEnableLog()) {
                e.printStackTrace();
            }
            exception2 = new ClientException(e.getMessage(), e);
        }
        if (this.context.getCancellationHandler().isCancelled()) {
            throw new InterruptedIOException("This task is cancelled!");
        }
        Request.Builder requestBuilder = new Request.Builder();
        String url = this.message.buildCanonicalURL();
        Request.Builder requestBuilder2 = requestBuilder.url(url);
        for (String key : this.message.getHeaders().keySet()) {
            requestBuilder2 = requestBuilder2.addHeader(key, (String) this.message.getHeaders().get(key));
        }
        String contentType = (String) this.message.getHeaders().get("Content-Type");
        switch (this.message.getMethod()) {
            case POST:
            case PUT:
                OSSUtils.assertTrue(contentType != null, "Content type can't be null when upload!");
                String stringBody = null;
                long length = 0;
                if (this.message.getUploadData() != null) {
                    InputStream inputStream2 = new ByteArrayInputStream(this.message.getUploadData());
                    length = this.message.getUploadData().length;
                    inputStream = inputStream2;
                } else if (this.message.getUploadFilePath() != null) {
                    File file = new File(this.message.getUploadFilePath());
                    InputStream inputStream3 = new FileInputStream(file);
                    length = file.length();
                    inputStream = inputStream3;
                } else if (this.message.getContent() != null) {
                    InputStream inputStream4 = this.message.getContent();
                    length = this.message.getContentLength();
                    inputStream = inputStream4;
                } else {
                    stringBody = this.message.getStringBody();
                    inputStream = null;
                }
                if (inputStream != null) {
                    InputStream inputStream5 = this.message.isCheckCRC64() ? new CheckedInputStream(inputStream, new CRC64()) : inputStream;
                    this.message.setContent(inputStream5);
                    this.message.setContentLength(length);
                    requestBuilder2 = requestBuilder2.method(this.message.getMethod().toString(), NetworkProgressHelper.addProgressRequestBody(inputStream5, length, contentType, this.context));
                    break;
                } else if (stringBody != null) {
                    requestBuilder2 = requestBuilder2.method(this.message.getMethod().toString(), RequestBody.create(MediaType.parse(contentType), stringBody.getBytes(CharEncoding.UTF_8)));
                    break;
                } else {
                    requestBuilder2 = requestBuilder2.method(this.message.getMethod().toString(), RequestBody.create((MediaType) null, new byte[0]));
                    break;
                }
            case GET:
                requestBuilder2 = requestBuilder2.get();
                break;
            case HEAD:
                requestBuilder2 = requestBuilder2.head();
                break;
            case DELETE:
                requestBuilder2 = requestBuilder2.delete();
                break;
        }
        request = requestBuilder2.build();
        if (ossRequest instanceof GetObjectRequest) {
            this.client = NetworkProgressHelper.addProgressResponseListener(this.client, this.context);
            OSSLog.logDebug("getObject");
        }
        call = this.client.newCall(request);
        this.context.getCancellationHandler().setCall(call);
        Response response = call.execute();
        if (OSSLog.isEnableLog()) {
            Map<String, List<String>> headerMap = response.headers().toMultimap();
            StringBuilder printRsp = new StringBuilder();
            printRsp.append("response:---------------------\n");
            printRsp.append("response code: " + response.code() + " for url: " + request.url() + StringUtils.LF);
            printRsp.append("response body: " + response.body().toString() + StringUtils.LF);
            for (String key2 : headerMap.keySet()) {
                printRsp.append("responseHeader [" + key2 + "]: ").append(headerMap.get(key2).get(0) + StringUtils.LF);
            }
            OSSLog.logDebug(printRsp.toString());
        }
        responseMessage = buildResponseMessage(this.message, response);
        if (responseMessage != null) {
            String responseDateString = (String) responseMessage.getHeaders().get("Date");
            try {
                long serverTime = DateUtil.parseRfc822Date(responseDateString).getTime();
                DateUtil.setCurrentServerTime(serverTime);
            } catch (Exception e2) {
            }
        }
        if (exception2 == null && (responseMessage.getStatusCode() == 203 || responseMessage.getStatusCode() >= 300)) {
            Exception exception3 = ResponseParsers.parseResponseErrorXML(responseMessage, request.method().equals(HttpHeadHC4.METHOD_NAME));
            exception = exception3;
        } else if (exception2 == null) {
            try {
                T result = this.responseParser.parse(responseMessage);
                if (this.context.getCompletedCallback() != null) {
                    try {
                        this.context.getCompletedCallback().onSuccess(this.context.getRequest(), result);
                        return result;
                    } catch (Exception e3) {
                        return result;
                    }
                }
                return result;
            } catch (IOException e4) {
                Exception exception4 = new ClientException(e4.getMessage(), e4);
                exception = exception4;
            }
        } else {
            exception = exception2;
        }
        Exception exception5 = ((call == null || !call.isCanceled()) && !this.context.getCancellationHandler().isCancelled()) ? exception : new ClientException("Task is cancelled!", exception.getCause(), true);
        OSSRetryType retryType = this.retryHandler.shouldRetry(exception5, this.currentRetryCount);
        OSSLog.logError("[run] - retry, retry type: " + retryType);
        if (retryType == OSSRetryType.OSSRetryTypeShouldRetry) {
            this.currentRetryCount++;
            if (this.context.getRetryCallback() != null) {
                this.context.getRetryCallback().onRetryCallback();
            }
            return call();
        } else if (retryType == OSSRetryType.OSSRetryTypeShouldFixedTimeSkewedAndRetry) {
            if (responseMessage != null) {
                this.message.getHeaders().put("Date", responseMessage.getHeaders().get("Date"));
            }
            this.currentRetryCount++;
            if (this.context.getRetryCallback() != null) {
                this.context.getRetryCallback().onRetryCallback();
            }
            return call();
        } else {
            if (exception5 instanceof ClientException) {
                if (this.context.getCompletedCallback() != null) {
                    this.context.getCompletedCallback().onFailure(this.context.getRequest(), (ClientException) exception5, null);
                }
            } else if (this.context.getCompletedCallback() != null) {
                this.context.getCompletedCallback().onFailure(this.context.getRequest(), null, (ServiceException) exception5);
            }
            throw exception5;
        }
    }

    private ResponseMessage buildResponseMessage(RequestMessage request, Response response) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setRequest(request);
        responseMessage.setResponse(response);
        Map<String, String> headers = new HashMap<>();
        Headers responseHeaders = response.headers();
        for (int i = 0; i < responseHeaders.size(); i++) {
            headers.put(responseHeaders.name(i), responseHeaders.value(i));
        }
        responseMessage.setHeaders(headers);
        responseMessage.setStatusCode(response.code());
        responseMessage.setContentLength(response.body().contentLength());
        responseMessage.setContent(response.body().byteStream());
        return responseMessage;
    }
}
