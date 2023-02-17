package com.alibaba.sdk.android.oss.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.MimeTypeMap;
import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.RequestParameters;
import com.alibaba.sdk.android.oss.common.auth.HmacSHA1Signature;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.exception.InconsistentException;
import com.alibaba.sdk.android.oss.internal.RequestMessage;
import com.alibaba.sdk.android.oss.model.CopyObjectRequest;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;
import com.alibaba.sdk.android.oss.model.DeleteBucketRequest;
import com.alibaba.sdk.android.oss.model.GetBucketACLRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PartETag;
import com.xcharge.charger.data.bean.device.MobileNet;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class OSSUtils {
    private static final String NEW_LINE = "\n";
    private static final List<String> SIGNED_PARAMTERS = Arrays.asList(RequestParameters.SUBRESOURCE_ACL, RequestParameters.SUBRESOURCE_UPLOADS, RequestParameters.SUBRESOURCE_LOCATION, RequestParameters.SUBRESOURCE_CORS, RequestParameters.SUBRESOURCE_LOGGING, RequestParameters.SUBRESOURCE_WEBSITE, RequestParameters.SUBRESOURCE_REFERER, RequestParameters.SUBRESOURCE_LIFECYCLE, RequestParameters.SUBRESOURCE_DELETE, RequestParameters.SUBRESOURCE_APPEND, RequestParameters.UPLOAD_ID, RequestParameters.PART_NUMBER, RequestParameters.SECURITY_TOKEN, RequestParameters.POSITION, RequestParameters.RESPONSE_HEADER_CACHE_CONTROL, RequestParameters.RESPONSE_HEADER_CONTENT_DISPOSITION, RequestParameters.RESPONSE_HEADER_CONTENT_ENCODING, RequestParameters.RESPONSE_HEADER_CONTENT_LANGUAGE, RequestParameters.RESPONSE_HEADER_CONTENT_TYPE, RequestParameters.RESPONSE_HEADER_EXPIRES, RequestParameters.X_OSS_PROCESS);

    public static void populateRequestMetadata(Map<String, String> headers, ObjectMetadata metadata) {
        if (metadata != null) {
            Map<String, Object> rawMetadata = metadata.getRawMetadata();
            if (rawMetadata != null) {
                for (Map.Entry<String, Object> entry : rawMetadata.entrySet()) {
                    headers.put(entry.getKey(), entry.getValue().toString());
                }
            }
            Map<String, String> userMetadata = metadata.getUserMetadata();
            if (userMetadata != null) {
                for (Map.Entry<String, String> entry2 : userMetadata.entrySet()) {
                    String key = entry2.getKey();
                    String value = entry2.getValue();
                    if (key != null) {
                        key = key.trim();
                    }
                    if (value != null) {
                        value = value.trim();
                    }
                    headers.put(key, value);
                }
            }
        }
    }

    public static void populateListObjectsRequestParameters(ListObjectsRequest listObjectsRequest, Map<String, String> params) {
        if (listObjectsRequest.getPrefix() != null) {
            params.put(RequestParameters.PREFIX, listObjectsRequest.getPrefix());
        }
        if (listObjectsRequest.getMarker() != null) {
            params.put(RequestParameters.MARKER, listObjectsRequest.getMarker());
        }
        if (listObjectsRequest.getDelimiter() != null) {
            params.put(RequestParameters.DELIMITER, listObjectsRequest.getDelimiter());
        }
        if (listObjectsRequest.getMaxKeys() != null) {
            params.put(RequestParameters.MAX_KEYS, Integer.toString(listObjectsRequest.getMaxKeys().intValue()));
        }
        if (listObjectsRequest.getEncodingType() != null) {
            params.put(RequestParameters.ENCODING_TYPE, listObjectsRequest.getEncodingType());
        }
    }

    public static boolean checkParamRange(long param, long from, boolean leftInclusive, long to, boolean rightInclusive) {
        return (leftInclusive && rightInclusive) ? from <= param && param <= to : (!leftInclusive || rightInclusive) ? (leftInclusive || rightInclusive) ? from < param && param <= to : from < param && param < to : from <= param && param < to;
    }

    /* loaded from: classes.dex */
    private enum MetadataDirective {
        COPY("COPY"),
        REPLACE("REPLACE");
        
        private final String directiveAsString;

        MetadataDirective(String directiveAsString) {
            this.directiveAsString = directiveAsString;
        }

        @Override // java.lang.Enum
        public String toString() {
            return this.directiveAsString;
        }
    }

    public static void populateCopyObjectHeaders(CopyObjectRequest copyObjectRequest, Map<String, String> headers) {
        String copySourceHeader = MqttTopic.TOPIC_LEVEL_SEPARATOR + copyObjectRequest.getSourceBucketName() + MqttTopic.TOPIC_LEVEL_SEPARATOR + HttpUtil.urlEncode(copyObjectRequest.getSourceKey(), "utf-8");
        headers.put(OSSHeaders.COPY_OBJECT_SOURCE, copySourceHeader);
        addDateHeader(headers, OSSHeaders.COPY_OBJECT_SOURCE_IF_MODIFIED_SINCE, copyObjectRequest.getModifiedSinceConstraint());
        addDateHeader(headers, OSSHeaders.COPY_OBJECT_SOURCE_IF_UNMODIFIED_SINCE, copyObjectRequest.getUnmodifiedSinceConstraint());
        addStringListHeader(headers, OSSHeaders.COPY_OBJECT_SOURCE_IF_MATCH, copyObjectRequest.getMatchingETagConstraints());
        addStringListHeader(headers, OSSHeaders.COPY_OBJECT_SOURCE_IF_NONE_MATCH, copyObjectRequest.getNonmatchingEtagConstraints());
        addHeader(headers, OSSHeaders.OSS_SERVER_SIDE_ENCRYPTION, copyObjectRequest.getServerSideEncryption());
        ObjectMetadata newObjectMetadata = copyObjectRequest.getNewObjectMetadata();
        if (newObjectMetadata != null) {
            headers.put(OSSHeaders.COPY_OBJECT_METADATA_DIRECTIVE, MetadataDirective.REPLACE.toString());
            populateRequestMetadata(headers, newObjectMetadata);
        }
        removeHeader(headers, "Content-Length");
    }

    public static String buildXMLFromPartEtagList(List<PartETag> partETagList) {
        StringBuilder builder = new StringBuilder();
        builder.append("<CompleteMultipartUpload>\n");
        for (PartETag partETag : partETagList) {
            builder.append("<Part>\n");
            builder.append("<PartNumber>" + partETag.getPartNumber() + "</PartNumber>\n");
            builder.append("<ETag>" + partETag.getETag() + "</ETag>\n");
            builder.append("</Part>\n");
        }
        builder.append("</CompleteMultipartUpload>\n");
        return builder.toString();
    }

    public static void addHeader(Map<String, String> headers, String header, String value) {
        if (value != null) {
            headers.put(header, value);
        }
    }

    public static void addDateHeader(Map<String, String> headers, String header, Date value) {
        if (value != null) {
            headers.put(header, DateUtil.formatRfc822Date(value));
        }
    }

    public static void addStringListHeader(Map<String, String> headers, String header, List<String> values) {
        if (values != null && !values.isEmpty()) {
            headers.put(header, join(values));
        }
    }

    public static void removeHeader(Map<String, String> headers, String header) {
        if (header != null && headers.containsKey(header)) {
            headers.remove(header);
        }
    }

    public static String join(List<String> strings) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (String s : strings) {
            if (!first) {
                result.append(", ");
            }
            result.append(s);
            first = false;
        }
        return result.toString();
    }

    public static boolean isEmptyString(String str) {
        return TextUtils.isEmpty(str);
    }

    public static String buildCanonicalString(RequestMessage request) {
        StringBuilder canonicalString = new StringBuilder();
        canonicalString.append(request.getMethod().toString() + "\n");
        Map<String, String> headers = request.getHeaders();
        TreeMap<String, String> headersToSign = new TreeMap<>();
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                if (header.getKey() != null) {
                    String lowerKey = header.getKey().toLowerCase();
                    if (lowerKey.equals("Content-Type".toLowerCase()) || lowerKey.equals("Content-MD5".toLowerCase()) || lowerKey.equals("Date".toLowerCase()) || lowerKey.startsWith(OSSHeaders.OSS_PREFIX)) {
                        headersToSign.put(lowerKey, header.getValue().trim());
                    }
                }
            }
        }
        if (!headersToSign.containsKey("Content-Type".toLowerCase())) {
            headersToSign.put("Content-Type".toLowerCase(), "");
        }
        if (!headersToSign.containsKey("Content-MD5".toLowerCase())) {
            headersToSign.put("Content-MD5".toLowerCase(), "");
        }
        for (Map.Entry<String, String> entry : headersToSign.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.startsWith(OSSHeaders.OSS_PREFIX)) {
                canonicalString.append(key).append(':').append(value);
            } else {
                canonicalString.append(value);
            }
            canonicalString.append("\n");
        }
        canonicalString.append(buildCanonicalizedResource(request.getBucketName(), request.getObjectKey(), request.getParameters()));
        return canonicalString.toString();
    }

    public static String buildCanonicalizedResource(String bucketName, String objectKey, Map<String, String> parameters) {
        String resourcePath;
        if (bucketName == null && objectKey == null) {
            resourcePath = MqttTopic.TOPIC_LEVEL_SEPARATOR;
        } else if (objectKey == null) {
            resourcePath = MqttTopic.TOPIC_LEVEL_SEPARATOR + bucketName + MqttTopic.TOPIC_LEVEL_SEPARATOR;
        } else {
            resourcePath = MqttTopic.TOPIC_LEVEL_SEPARATOR + bucketName + MqttTopic.TOPIC_LEVEL_SEPARATOR + objectKey;
        }
        return buildCanonicalizedResource(resourcePath, parameters);
    }

    public static String buildCanonicalizedResource(String resourcePath, Map<String, String> parameters) {
        StringBuilder builder = new StringBuilder();
        builder.append(resourcePath);
        if (parameters != null) {
            String[] parameterNames = (String[]) parameters.keySet().toArray(new String[parameters.size()]);
            Arrays.sort(parameterNames);
            char separater = '?';
            for (String paramName : parameterNames) {
                if (SIGNED_PARAMTERS.contains(paramName)) {
                    builder.append(separater);
                    builder.append(paramName);
                    String paramValue = parameters.get(paramName);
                    if (!isEmptyString(paramValue)) {
                        builder.append("=").append(paramValue);
                    }
                    separater = '&';
                }
            }
        }
        return builder.toString();
    }

    public static String paramToQueryString(Map<String, String> params, String charset) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        StringBuilder paramString = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> p : params.entrySet()) {
            String key = p.getKey();
            String value = p.getValue();
            if (!first) {
                paramString.append("&");
            }
            paramString.append(HttpUtil.urlEncode(key, charset));
            if (!isEmptyString(value)) {
                paramString.append("=").append(HttpUtil.urlEncode(value, charset));
            }
            first = false;
        }
        return paramString.toString();
    }

    public static String populateMapToBase64JsonString(Map<String, String> map) {
        JSONObject jsonObj = new JSONObject(map);
        return Base64.encodeToString(jsonObj.toString().getBytes(), 2);
    }

    public static String sign(String accessKey, String screctKey, String content) {
        try {
            String signature = new HmacSHA1Signature().computeSignature(screctKey, content);
            return "OSS " + accessKey + ":" + signature.trim();
        } catch (Exception e) {
            throw new IllegalStateException("Compute signature failed!", e);
        }
    }

    public static boolean isCname(String host) {
        String[] strArr;
        for (String suffix : OSSConstants.DEFAULT_CNAME_EXCLUDE_LIST) {
            if (host.toLowerCase().endsWith(suffix)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isInCustomCnameExcludeList(String endpoint, List<String> customCnameExludeList) {
        for (String host : customCnameExludeList) {
            if (endpoint.endsWith(host.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public static boolean validateBucketName(String bucketName) {
        if (bucketName == null) {
            return false;
        }
        return bucketName.matches("^[a-z0-9][a-z0-9_\\-]{2,62}$");
    }

    public static void ensureBucketNameValid(String bucketName) {
        if (!validateBucketName(bucketName)) {
            throw new IllegalArgumentException("The bucket name is invalid. \nA bucket name must: \n1) be comprised of lower-case characters, numbers or dash(-); \n2) start with lower case or numbers; \n3) be between 3-63 characters long. ");
        }
    }

    public static boolean validateObjectKey(String objectKey) {
        if (objectKey != null && objectKey.length() > 0 && objectKey.length() <= 1023) {
            try {
                objectKey.getBytes("utf-8");
                char[] keyChars = objectKey.toCharArray();
                char beginKeyChar = keyChars[0];
                if (beginKeyChar == '/' || beginKeyChar == '\\') {
                    return false;
                }
                for (char keyChar : keyChars) {
                    if (keyChar != '\t' && keyChar < ' ') {
                        return false;
                    }
                }
                return true;
            } catch (UnsupportedEncodingException e) {
                return false;
            }
        }
        return false;
    }

    public static void ensureObjectKeyValid(String objectKey) {
        if (!validateObjectKey(objectKey)) {
            throw new IllegalArgumentException("The object key is invalid. \nAn object name should be: \n1) between 1 - 1023 bytes long when encoded as UTF-8 \n2) cannot contain LF or CR or unsupported chars in XML1.0, \n3) cannot begin with \"/\" or \"\\\".");
        }
    }

    public static boolean doesRequestNeedObjectKey(OSSRequest request) {
        return ((request instanceof ListObjectsRequest) || (request instanceof CreateBucketRequest) || (request instanceof DeleteBucketRequest) || (request instanceof GetBucketACLRequest)) ? false : true;
    }

    public static void ensureRequestValid(OSSRequest request, RequestMessage message) {
        ensureBucketNameValid(message.getBucketName());
        if (doesRequestNeedObjectKey(request)) {
            ensureObjectKeyValid(message.getObjectKey());
        }
        if (request instanceof CopyObjectRequest) {
            ensureObjectKeyValid(((CopyObjectRequest) request).getDestinationKey());
        }
    }

    public static String determineContentType(String initValue, String srcPath, String toObjectKey) {
        if (initValue != null) {
            return initValue;
        }
        MimeTypeMap typeMap = MimeTypeMap.getSingleton();
        if (srcPath != null) {
            String extension = srcPath.substring(srcPath.lastIndexOf(46) + 1);
            String contentType = typeMap.getMimeTypeFromExtension(extension);
            if (contentType != null) {
                return contentType;
            }
        }
        if (toObjectKey != null) {
            String extension2 = toObjectKey.substring(toObjectKey.lastIndexOf(46) + 1);
            String contentType2 = typeMap.getMimeTypeFromExtension(extension2);
            if (contentType2 != null) {
                return contentType2;
            }
        }
        return OSSConstants.DEFAULT_OBJECT_CONTENT_TYPE;
    }

    public static void signRequest(RequestMessage message) throws Exception {
        if (message.isAuthorizationRequired()) {
            if (message.getCredentialProvider() == null) {
                throw new IllegalStateException("当前CredentialProvider为空！！！\n1. 请检查您是否在初始化OSSService时设置CredentialProvider;\n2. 如果您bucket为公共权限，请确认获取到Bucket后已经调用Bucket中接口声明ACL;");
            }
            OSSCredentialProvider credentialProvider = message.getCredentialProvider();
            OSSFederationToken federationToken = null;
            if (credentialProvider instanceof OSSFederationCredentialProvider) {
                federationToken = ((OSSFederationCredentialProvider) credentialProvider).getValidFederationToken();
                if (federationToken == null) {
                    OSSLog.logError("Can't get a federation token");
                    throw new IOException("Can't get a federation token");
                }
                message.getHeaders().put(OSSHeaders.OSS_SECURITY_TOKEN, federationToken.getSecurityToken());
            } else if (credentialProvider instanceof OSSStsTokenCredentialProvider) {
                federationToken = credentialProvider.getFederationToken();
                message.getHeaders().put(OSSHeaders.OSS_SECURITY_TOKEN, federationToken.getSecurityToken());
            }
            String contentToSign = buildCanonicalString(message);
            String signature = "---initValue---";
            if ((credentialProvider instanceof OSSFederationCredentialProvider) || (credentialProvider instanceof OSSStsTokenCredentialProvider)) {
                signature = sign(federationToken.getTempAK(), federationToken.getTempSK(), contentToSign);
            } else if (credentialProvider instanceof OSSPlainTextAKSKCredentialProvider) {
                signature = sign(((OSSPlainTextAKSKCredentialProvider) credentialProvider).getAccessKeyId(), ((OSSPlainTextAKSKCredentialProvider) credentialProvider).getAccessKeySecret(), contentToSign);
            } else if (credentialProvider instanceof OSSCustomSignerCredentialProvider) {
                signature = ((OSSCustomSignerCredentialProvider) credentialProvider).signContent(contentToSign);
            }
            OSSLog.logDebug("signed content: " + contentToSign + "   \n ---------   signature: " + signature, false);
            message.getHeaders().put("Authorization", signature);
        }
    }

    public static String buildBaseLogInfo(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("=====[device info]=====\n");
        sb.append("[INFO]: android_version：" + Build.VERSION.RELEASE + "\n");
        sb.append("[INFO]: mobile_model：" + Build.MODEL + "\n");
        String operatorName = getOperatorName(context);
        if (!TextUtils.isEmpty(operatorName)) {
            sb.append("[INFO]: operator_name：" + operatorName + "\n");
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        String networkState = "unconnected";
        String netType = "unknown";
        if (activeNetworkInfo != null && activeNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
            netType = activeNetworkInfo.getTypeName() + StringUtils.SPACE;
            networkState = "connected";
        }
        sb.append("[INFO]: network_state：" + networkState + "\n");
        sb.append("[INFO]: network_type：" + netType);
        return sb.toString();
    }

    private static String getOperatorName(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        String operator = telephonyManager.getSimOperator();
        if (operator == null) {
            return "";
        }
        if (operator.equals("46000") || operator.equals("46002")) {
            return MobileNet.OPR_CMCC;
        }
        if (operator.equals("46001")) {
            return MobileNet.OPR_CUCC;
        }
        if (operator.equals("46003")) {
            return "CTCC";
        }
        return operator;
    }

    public static void checkChecksum(Long clientChecksum, Long serverChecksum, String requestId) throws InconsistentException {
        if (clientChecksum != null && serverChecksum != null && !clientChecksum.equals(serverChecksum)) {
            throw new InconsistentException(clientChecksum, serverChecksum, requestId);
        }
    }
}