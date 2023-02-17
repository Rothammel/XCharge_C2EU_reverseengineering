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

public class OSSRequestTask<T extends OSSResult> implements Callable<T> {
    private OkHttpClient client;
    private ExecutionContext context;
    private int currentRetryCount = 0;
    private RequestMessage message;
    private ResponseParser<T> responseParser;
    private OSSRetryHandler retryHandler;

    public OSSRequestTask(RequestMessage message2, ResponseParser parser, ExecutionContext context2, int maxRetry) {
        this.responseParser = parser;
        this.message = message2;
        this.context = context2;
        this.client = context2.getClient();
        this.retryHandler = new OSSRetryHandler(maxRetry);
    }

    public T call() throws Exception {
        Exception exception;
        Exception exception2;
        InputStream inputStream;
        InputStream inputStream2;
        Request request = null;
        ResponseMessage responseMessage = null;
        Exception exception3 = null;
        Call call = null;
        try {
            if (this.context.getApplicationContext() != null) {
                OSSLog.logInfo(OSSUtils.buildBaseLogInfo(this.context.getApplicationContext()));
            }
            OSSLog.logDebug("[call] - ");
            OSSRequest ossRequest = this.context.getRequest();
            OSSUtils.ensureRequestValid(ossRequest, this.message);
            OSSUtils.signRequest(this.message);
            if (this.context.getCancellationHandler().isCancelled()) {
                throw new InterruptedIOException("This task is cancelled!");
            }
            Request.Builder requestBuilder = new Request.Builder().url(this.message.buildCanonicalURL());
            for (String key : this.message.getHeaders().keySet()) {
                requestBuilder = requestBuilder.addHeader(key, (String) this.message.getHeaders().get(key));
            }
            String contentType = (String) this.message.getHeaders().get("Content-Type");
            switch (this.message.getMethod()) {
                case POST:
                case PUT:
                    OSSUtils.assertTrue(contentType != null, "Content type can't be null when upload!");
                    String stringBody = null;
                    long length = 0;
                    if (this.message.getUploadData() != null) {
                        InputStream inputStream3 = new ByteArrayInputStream(this.message.getUploadData());
                        length = (long) this.message.getUploadData().length;
                        inputStream = inputStream3;
                    } else if (this.message.getUploadFilePath() != null) {
                        File file = new File(this.message.getUploadFilePath());
                        InputStream inputStream4 = new FileInputStream(file);
                        length = file.length();
                        inputStream = inputStream4;
                    } else if (this.message.getContent() != null) {
                        InputStream inputStream5 = this.message.getContent();
                        length = this.message.getContentLength();
                        inputStream = inputStream5;
                    } else {
                        stringBody = this.message.getStringBody();
                        inputStream = null;
                    }
                    if (inputStream == null) {
                        if (stringBody == null) {
                            requestBuilder = requestBuilder.method(this.message.getMethod().toString(), RequestBody.create((MediaType) null, new byte[0]));
                            break;
                        } else {
                            requestBuilder = requestBuilder.method(this.message.getMethod().toString(), RequestBody.create(MediaType.parse(contentType), stringBody.getBytes(CharEncoding.UTF_8)));
                            break;
                        }
                    } else {
                        if (this.message.isCheckCRC64()) {
                            inputStream2 = new CheckedInputStream(inputStream, new CRC64());
                        } else {
                            inputStream2 = inputStream;
                        }
                        this.message.setContent(inputStream2);
                        this.message.setContentLength(length);
                        requestBuilder = requestBuilder.method(this.message.getMethod().toString(), NetworkProgressHelper.addProgressRequestBody(inputStream2, length, contentType, this.context));
                        break;
                    }
                case GET:
                    requestBuilder = requestBuilder.get();
                    break;
                case HEAD:
                    requestBuilder = requestBuilder.head();
                    break;
                case DELETE:
                    requestBuilder = requestBuilder.delete();
                    break;
            }
            request = requestBuilder.build();
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
                printRsp.append("response code: " + response.code() + " for url: " + request.url() + StringUtils.f146LF);
                printRsp.append("response body: " + response.body().toString() + StringUtils.f146LF);
                for (String key2 : headerMap.keySet()) {
                    printRsp.append("responseHeader [" + key2 + "]: ").append(((String) headerMap.get(key2).get(0)) + StringUtils.f146LF);
                }
                OSSLog.logDebug(printRsp.toString());
            }
            responseMessage = buildResponseMessage(this.message, response);
            if (responseMessage != null) {
                try {
                    DateUtil.setCurrentServerTime(DateUtil.parseRfc822Date((String) responseMessage.getHeaders().get("Date")).getTime());
                } catch (Exception e) {
                }
            }
            if (exception3 == null && (responseMessage.getStatusCode() == 203 || responseMessage.getStatusCode() >= 300)) {
                exception = ResponseParsers.parseResponseErrorXML(responseMessage, request.method().equals(HttpHeadHC4.METHOD_NAME));
            } else if (exception3 == null) {
                try {
                    T result = this.responseParser.parse(responseMessage);
                    if (this.context.getCompletedCallback() == null) {
                        return result;
                    }
                    try {
                        this.context.getCompletedCallback().onSuccess(this.context.getRequest(), result);
                        return result;
                    } catch (Exception e2) {
                        return result;
                    }
                } catch (IOException e3) {
                    exception = new ClientException(e3.getMessage(), e3);
                }
            } else {
                exception = exception3;
            }
            if ((call == null || !call.isCanceled()) && !this.context.getCancellationHandler().isCancelled()) {
                exception2 = exception;
            } else {
                exception2 = new ClientException("Task is cancelled!", exception.getCause(), 1);
            }
            OSSRetryType retryType = this.retryHandler.shouldRetry(exception2, this.currentRetryCount);
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
                if (exception2 instanceof ClientException) {
                    if (this.context.getCompletedCallback() != null) {
                        this.context.getCompletedCallback().onFailure(this.context.getRequest(), (ClientException) exception2, (ServiceException) null);
                    }
                } else if (this.context.getCompletedCallback() != null) {
                    this.context.getCompletedCallback().onFailure(this.context.getRequest(), (ClientException) null, (ServiceException) exception2);
                }
                throw exception2;
            }
        } catch (Exception e4) {
            OSSLog.logError("Encounter local execpiton: " + e4.toString());
            if (OSSLog.isEnableLog()) {
                e4.printStackTrace();
            }
            exception3 = new ClientException(e4.getMessage(), e4);
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
