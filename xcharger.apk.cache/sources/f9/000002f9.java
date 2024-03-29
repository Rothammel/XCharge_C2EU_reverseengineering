package com.alibaba.sdk.android.oss.network;

import android.content.Context;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.callback.OSSRetryCallback;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.OSSResult;
import okhttp3.OkHttpClient;

/* loaded from: classes.dex */
public class ExecutionContext<Request extends OSSRequest, Result extends OSSResult> {
    private Context applicationContext;
    private CancellationHandler cancellationHandler;
    private OkHttpClient client;
    private OSSCompletedCallback completedCallback;
    private OSSProgressCallback progressCallback;
    private Request request;
    private OSSRetryCallback retryCallback;

    public ExecutionContext(OkHttpClient client, Request request) {
        this(client, request, null);
    }

    public ExecutionContext(OkHttpClient client, Request request, Context applicationContext) {
        this.cancellationHandler = new CancellationHandler();
        setClient(client);
        setRequest(request);
        this.applicationContext = applicationContext;
    }

    public Context getApplicationContext() {
        return this.applicationContext;
    }

    public Request getRequest() {
        return this.request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public OkHttpClient getClient() {
        return this.client;
    }

    public void setClient(OkHttpClient client) {
        this.client = client;
    }

    public CancellationHandler getCancellationHandler() {
        return this.cancellationHandler;
    }

    public OSSCompletedCallback<Request, Result> getCompletedCallback() {
        return this.completedCallback;
    }

    public void setCompletedCallback(OSSCompletedCallback<Request, Result> completedCallback) {
        this.completedCallback = completedCallback;
    }

    public OSSProgressCallback getProgressCallback() {
        return this.progressCallback;
    }

    public void setProgressCallback(OSSProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    public OSSRetryCallback getRetryCallback() {
        return this.retryCallback;
    }

    public void setRetryCallback(OSSRetryCallback retryCallback) {
        this.retryCallback = retryCallback;
    }
}