package com.alibaba.sdk.android.oss.internal;

import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.model.OSSResult;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CheckedInputStream;
import okhttp3.Headers;
import okhttp3.Response;

/* loaded from: classes.dex */
public abstract class AbstractResponseParser<T extends OSSResult> implements ResponseParser {
    abstract T parseData(ResponseMessage responseMessage, T t) throws Exception;

    public boolean needCloseResponse() {
        return true;
    }

    @Override // com.alibaba.sdk.android.oss.internal.ResponseParser
    public T parse(ResponseMessage response) throws IOException {
        try {
            try {
                Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                Class<?> classType = (Class) type;
                T result = (T) classType.newInstance();
                if (result != null) {
                    result.setRequestId((String) response.getHeaders().get(OSSHeaders.OSS_HEADER_REQUEST_ID));
                    result.setStatusCode(response.getStatusCode());
                    result.setResponseHeader(parseResponseHeader(response.getResponse()));
                    setCRC(result, response);
                    result = parseData(response, result);
                }
                return result;
            } catch (Exception e) {
                IOException ioException = new IOException(e.getMessage(), e);
                e.printStackTrace();
                OSSLog.logThrowable2Local(e);
                throw ioException;
            }
        } finally {
            if (needCloseResponse()) {
                safeCloseResponse(response);
            }
        }
    }

    public static void safeCloseResponse(ResponseMessage response) {
        try {
            response.close();
        } catch (Exception e) {
        }
    }

    private Map<String, String> parseResponseHeader(Response response) {
        Map<String, String> result = new HashMap<>();
        Headers headers = response.headers();
        for (int i = 0; i < headers.size(); i++) {
            result.put(headers.name(i), headers.value(i));
        }
        return result;
    }

    public <Result extends OSSResult> void setCRC(Result result, ResponseMessage response) {
        InputStream inputStream = response.getRequest().getContent();
        if (inputStream != null && (inputStream instanceof CheckedInputStream)) {
            CheckedInputStream checkedInputStream = (CheckedInputStream) inputStream;
            result.setClientCRC(Long.valueOf(checkedInputStream.getChecksum().getValue()));
        }
        String strSrvCrc = (String) response.getHeaders().get(OSSHeaders.OSS_HASH_CRC64_ECMA);
        if (strSrvCrc != null) {
            BigInteger bi = new BigInteger(strSrvCrc);
            result.setServerCRC(Long.valueOf(bi.longValue()));
        }
    }
}
