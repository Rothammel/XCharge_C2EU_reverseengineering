package com.alibaba.sdk.android.oss.model;

import com.alibaba.sdk.android.oss.common.HttpMethod;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public class GeneratePresignedUrlRequest {
    private String bucketName;
    private String contentMD5;
    private String contentType;
    private long expiration;
    private String key;
    private HttpMethod method;
    private String process;
    private Map<String, String> queryParam;

    public GeneratePresignedUrlRequest(String bucketName, String key) {
        this(bucketName, key, 3600L);
    }

    public GeneratePresignedUrlRequest(String bucketName, String key, long expiration) {
        this(bucketName, key, 3600L, HttpMethod.GET);
    }

    public GeneratePresignedUrlRequest(String bucketName, String key, long expiration, HttpMethod method) {
        this.queryParam = new HashMap();
        this.bucketName = bucketName;
        this.key = key;
        this.expiration = expiration;
        this.method = method;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public String getContentMD5() {
        return this.contentMD5;
    }

    public HttpMethod getMethod() {
        return this.method;
    }

    public void setMethod(HttpMethod method) {
        if (method != HttpMethod.GET && method != HttpMethod.PUT) {
            throw new IllegalArgumentException("Only GET or PUT is supported!");
        }
        this.method = method;
    }

    public String getBucketName() {
        return this.bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getExpiration() {
        return this.expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public Map<String, String> getQueryParameter() {
        return this.queryParam;
    }

    public void setQueryParameter(Map<String, String> queryParam) {
        if (queryParam == null) {
            throw new NullPointerException("The argument 'queryParameter' is null.");
        }
        if (this.queryParam != null && this.queryParam.size() > 0) {
            this.queryParam.clear();
        }
        this.queryParam.putAll(queryParam);
    }

    public void addQueryParameter(String key, String value) {
        this.queryParam.put(key, value);
    }

    public String getProcess() {
        return this.process;
    }

    public void setProcess(String process) {
        this.process = process;
    }
}