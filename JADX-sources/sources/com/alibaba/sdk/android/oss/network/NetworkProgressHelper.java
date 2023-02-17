package com.alibaba.sdk.android.oss.network;

import java.io.IOException;
import java.io.InputStream;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/* loaded from: classes.dex */
public class NetworkProgressHelper {
    public static OkHttpClient addProgressResponseListener(OkHttpClient client, final ExecutionContext context) {
        OkHttpClient newClient = client.newBuilder().addNetworkInterceptor(new Interceptor() { // from class: com.alibaba.sdk.android.oss.network.NetworkProgressHelper.1
            @Override // okhttp3.Interceptor
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder().body(new ProgressTouchableResponseBody(originalResponse.body(), ExecutionContext.this)).build();
            }
        }).build();
        return newClient;
    }

    public static ProgressTouchableRequestBody addProgressRequestBody(InputStream input, long contentLength, String contentType, ExecutionContext context) {
        return new ProgressTouchableRequestBody(input, contentLength, contentType, context);
    }
}
