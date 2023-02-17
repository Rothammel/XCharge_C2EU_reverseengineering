package com.alibaba.sdk.android.oss.internal;

import android.util.Xml;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.utils.CRC64;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.AppendObjectResult;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.CopyObjectResult;
import com.alibaba.sdk.android.oss.model.CreateBucketResult;
import com.alibaba.sdk.android.oss.model.DeleteBucketResult;
import com.alibaba.sdk.android.oss.model.DeleteObjectResult;
import com.alibaba.sdk.android.oss.model.GetBucketACLResult;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.ListObjectsResult;
import com.alibaba.sdk.android.oss.model.ListPartsResult;
import com.alibaba.sdk.android.oss.model.OSSObjectSummary;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.Owner;
import com.alibaba.sdk.android.oss.model.PartSummary;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.alibaba.sdk.android.oss.model.UploadPartResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* loaded from: classes.dex */
public final class ResponseParsers {

    /* loaded from: classes.dex */
    public static final class PutObjectResponseParser extends AbstractResponseParser<PutObjectResult> {
        @Override // com.alibaba.sdk.android.oss.internal.AbstractResponseParser
        public PutObjectResult parseData(ResponseMessage response, PutObjectResult result) throws IOException {
            result.setETag(ResponseParsers.trimQuotes((String) response.getHeaders().get("ETag")));
            if (response.getContentLength() > 0) {
                result.setServerCallbackReturnBody(response.getResponse().body().string());
            }
            return result;
        }
    }

    /* loaded from: classes.dex */
    public static final class AppendObjectResponseParser extends AbstractResponseParser<AppendObjectResult> {
        @Override // com.alibaba.sdk.android.oss.internal.AbstractResponseParser
        public AppendObjectResult parseData(ResponseMessage response, AppendObjectResult result) throws IOException {
            String nextPosition = (String) response.getHeaders().get(OSSHeaders.OSS_NEXT_APPEND_POSITION);
            if (nextPosition != null) {
                result.setNextPosition(Long.valueOf(nextPosition));
            }
            result.setObjectCRC64((String) response.getHeaders().get(OSSHeaders.OSS_HASH_CRC64_ECMA));
            return result;
        }
    }

    /* loaded from: classes.dex */
    public static final class HeadObjectResponseParser extends AbstractResponseParser<HeadObjectResult> {
        @Override // com.alibaba.sdk.android.oss.internal.AbstractResponseParser
        public HeadObjectResult parseData(ResponseMessage response, HeadObjectResult result) throws IOException {
            result.setMetadata(ResponseParsers.parseObjectMetadata(result.getResponseHeader()));
            return result;
        }
    }

    /* loaded from: classes.dex */
    public static final class GetObjectResponseParser extends AbstractResponseParser<GetObjectResult> {
        @Override // com.alibaba.sdk.android.oss.internal.AbstractResponseParser
        public GetObjectResult parseData(ResponseMessage response, GetObjectResult result) throws IOException {
            result.setMetadata(ResponseParsers.parseObjectMetadata(result.getResponseHeader()));
            result.setContentLength(response.getContentLength());
            if (response.getRequest().isCheckCRC64()) {
                result.setObjectContent(new CheckCRC64DownloadInputStream(response.getContent(), new CRC64(), response.getContentLength(), result.getServerCRC().longValue(), result.getRequestId()));
            } else {
                result.setObjectContent(response.getContent());
            }
            return result;
        }

        @Override // com.alibaba.sdk.android.oss.internal.AbstractResponseParser
        public boolean needCloseResponse() {
            return false;
        }
    }

    /* loaded from: classes.dex */
    public static final class CopyObjectResponseParser extends AbstractResponseParser<CopyObjectResult> {
        @Override // com.alibaba.sdk.android.oss.internal.AbstractResponseParser
        public CopyObjectResult parseData(ResponseMessage response, CopyObjectResult result) throws Exception {
            return ResponseParsers.parseCopyObjectResponseXML(response.getContent(), result);
        }
    }

    /* loaded from: classes.dex */
    public static final class CreateBucketResponseParser extends AbstractResponseParser<CreateBucketResult> {
        @Override // com.alibaba.sdk.android.oss.internal.AbstractResponseParser
        public CreateBucketResult parseData(ResponseMessage response, CreateBucketResult result) throws IOException {
            if (result.getResponseHeader().containsKey("Location")) {
                result.bucketLocation = result.getResponseHeader().get("Location");
            }
            return result;
        }
    }

    /* loaded from: classes.dex */
    public static final class DeleteBucketResponseParser extends AbstractResponseParser<DeleteBucketResult> {
        @Override // com.alibaba.sdk.android.oss.internal.AbstractResponseParser
        public DeleteBucketResult parseData(ResponseMessage response, DeleteBucketResult result) throws IOException {
            return result;
        }
    }

    /* loaded from: classes.dex */
    public static final class GetBucketACLResponseParser extends AbstractResponseParser<GetBucketACLResult> {
        @Override // com.alibaba.sdk.android.oss.internal.AbstractResponseParser
        public GetBucketACLResult parseData(ResponseMessage response, GetBucketACLResult result) throws Exception {
            return ResponseParsers.parseGetBucketACLResponse(response.getContent(), result);
        }
    }

    /* loaded from: classes.dex */
    public static final class DeleteObjectResponseParser extends AbstractResponseParser<DeleteObjectResult> {
        @Override // com.alibaba.sdk.android.oss.internal.AbstractResponseParser
        public DeleteObjectResult parseData(ResponseMessage response, DeleteObjectResult result) throws IOException {
            return result;
        }
    }

    /* loaded from: classes.dex */
    public static final class ListObjectsResponseParser extends AbstractResponseParser<ListObjectsResult> {
        @Override // com.alibaba.sdk.android.oss.internal.AbstractResponseParser
        public ListObjectsResult parseData(ResponseMessage response, ListObjectsResult result) throws Exception {
            return ResponseParsers.parseObjectListResponse(response.getContent(), result);
        }
    }

    /* loaded from: classes.dex */
    public static final class InitMultipartResponseParser extends AbstractResponseParser<InitiateMultipartUploadResult> {
        @Override // com.alibaba.sdk.android.oss.internal.AbstractResponseParser
        public InitiateMultipartUploadResult parseData(ResponseMessage response, InitiateMultipartUploadResult result) throws Exception {
            return ResponseParsers.parseInitMultipartResponseXML(response.getContent(), result);
        }
    }

    /* loaded from: classes.dex */
    public static final class UploadPartResponseParser extends AbstractResponseParser<UploadPartResult> {
        @Override // com.alibaba.sdk.android.oss.internal.AbstractResponseParser
        public UploadPartResult parseData(ResponseMessage response, UploadPartResult result) throws IOException {
            result.setETag(ResponseParsers.trimQuotes((String) response.getHeaders().get("ETag")));
            return result;
        }
    }

    /* loaded from: classes.dex */
    public static final class AbortMultipartUploadResponseParser extends AbstractResponseParser<AbortMultipartUploadResult> {
        @Override // com.alibaba.sdk.android.oss.internal.AbstractResponseParser
        public AbortMultipartUploadResult parseData(ResponseMessage response, AbortMultipartUploadResult result) throws IOException {
            return result;
        }
    }

    /* loaded from: classes.dex */
    public static final class CompleteMultipartUploadResponseParser extends AbstractResponseParser<CompleteMultipartUploadResult> {
        @Override // com.alibaba.sdk.android.oss.internal.AbstractResponseParser
        public CompleteMultipartUploadResult parseData(ResponseMessage response, CompleteMultipartUploadResult result) throws Exception {
            if (((String) response.getHeaders().get("Content-Type")).equals("application/xml")) {
                return ResponseParsers.parseCompleteMultipartUploadResponseXML(response.getContent(), result);
            }
            if (response.getResponse().body() != null) {
                result.setServerCallbackReturnBody(response.getResponse().body().string());
                return result;
            }
            return result;
        }
    }

    /* loaded from: classes.dex */
    public static final class ListPartsResponseParser extends AbstractResponseParser<ListPartsResult> {
        @Override // com.alibaba.sdk.android.oss.internal.AbstractResponseParser
        public ListPartsResult parseData(ResponseMessage response, ListPartsResult result) throws Exception {
            return ResponseParsers.parseListPartsResponseXML(response.getContent(), result);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static CopyObjectResult parseCopyObjectResponseXML(InputStream in, CopyObjectResult result) throws XmlPullParserException, IOException, ParseException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, "utf-8");
        int eventType = parser.getEventType();
        while (eventType != 1) {
            switch (eventType) {
                case 2:
                    String name = parser.getName();
                    if ("LastModified".equals(name)) {
                        result.setLastModified(DateUtil.parseIso8601Date(parser.nextText()));
                        break;
                    } else if ("ETag".equals(name)) {
                        result.setEtag(parser.nextText());
                        break;
                    }
                    break;
            }
            eventType = parser.next();
            if (eventType == 4) {
                eventType = parser.next();
            }
        }
        return result;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ListPartsResult parseListPartsResponseXML(InputStream in, ListPartsResult result) throws IOException, XmlPullParserException, ParseException {
        List<PartSummary> partEtagList = new ArrayList<>();
        PartSummary partSummary = null;
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, "utf-8");
        int eventType = parser.getEventType();
        while (eventType != 1) {
            switch (eventType) {
                case 2:
                    String name = parser.getName();
                    if ("Bucket".equals(name)) {
                        result.setBucketName(parser.nextText());
                        break;
                    } else if ("Key".equals(name)) {
                        result.setKey(parser.nextText());
                        break;
                    } else if ("UploadId".equals(name)) {
                        result.setUploadId(parser.nextText());
                        break;
                    } else if ("PartNumberMarker".equals(name)) {
                        String partNumberMarker = parser.nextText();
                        if (!OSSUtils.isEmptyString(partNumberMarker)) {
                            result.setPartNumberMarker(Integer.valueOf(partNumberMarker).intValue());
                            break;
                        }
                    } else if ("NextPartNumberMarker".equals(name)) {
                        String nextPartNumberMarker = parser.nextText();
                        if (!OSSUtils.isEmptyString(nextPartNumberMarker)) {
                            result.setNextPartNumberMarker(Integer.valueOf(nextPartNumberMarker).intValue());
                            break;
                        }
                    } else if ("MaxParts".equals(name)) {
                        String maxParts = parser.nextText();
                        if (!OSSUtils.isEmptyString(maxParts)) {
                            result.setMaxParts(Integer.valueOf(maxParts).intValue());
                            break;
                        }
                    } else if ("IsTruncated".equals(name)) {
                        String isTruncated = parser.nextText();
                        if (!OSSUtils.isEmptyString(isTruncated)) {
                            result.setTruncated(Boolean.valueOf(isTruncated).booleanValue());
                            break;
                        }
                    } else if ("StorageClass".equals(name)) {
                        result.setStorageClass(parser.nextText());
                        break;
                    } else if ("Part".equals(name)) {
                        partSummary = new PartSummary();
                        break;
                    } else if ("PartNumber".equals(name)) {
                        String partNum = parser.nextText();
                        if (!OSSUtils.isEmptyString(partNum)) {
                            partSummary.setPartNumber(Integer.valueOf(partNum).intValue());
                            break;
                        }
                    } else if ("LastModified".equals(name)) {
                        partSummary.setLastModified(DateUtil.parseIso8601Date(parser.nextText()));
                        break;
                    } else if ("ETag".equals(name)) {
                        partSummary.setETag(parser.nextText());
                        break;
                    } else if ("Size".equals(name)) {
                        String size = parser.nextText();
                        if (!OSSUtils.isEmptyString(size)) {
                            partSummary.setSize(Long.valueOf(size).longValue());
                            break;
                        }
                    }
                    break;
                case 3:
                    if ("Part".equals(parser.getName())) {
                        partEtagList.add(partSummary);
                        break;
                    }
                    break;
            }
            eventType = parser.next();
            if (eventType == 4) {
                eventType = parser.next();
            }
        }
        if (partEtagList.size() > 0) {
            result.setParts(partEtagList);
        }
        return result;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static CompleteMultipartUploadResult parseCompleteMultipartUploadResponseXML(InputStream in, CompleteMultipartUploadResult result) throws IOException, XmlPullParserException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, "utf-8");
        int eventType = parser.getEventType();
        while (eventType != 1) {
            switch (eventType) {
                case 2:
                    String name = parser.getName();
                    if ("Location".equals(name)) {
                        result.setLocation(parser.nextText());
                        break;
                    } else if ("Bucket".equals(name)) {
                        result.setBucketName(parser.nextText());
                        break;
                    } else if ("Key".equals(name)) {
                        result.setObjectKey(parser.nextText());
                        break;
                    } else if ("ETag".equals(name)) {
                        result.setETag(parser.nextText());
                        break;
                    }
                    break;
            }
            eventType = parser.next();
            if (eventType == 4) {
                eventType = parser.next();
            }
        }
        return result;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static InitiateMultipartUploadResult parseInitMultipartResponseXML(InputStream in, InitiateMultipartUploadResult result) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, "utf-8");
        int eventType = parser.getEventType();
        while (eventType != 1) {
            switch (eventType) {
                case 2:
                    String name = parser.getName();
                    if ("Bucket".equals(name)) {
                        result.setBucketName(parser.nextText());
                        break;
                    } else if ("Key".equals(name)) {
                        result.setObjectKey(parser.nextText());
                        break;
                    } else if ("UploadId".equals(name)) {
                        result.setUploadId(parser.nextText());
                        break;
                    }
                    break;
            }
            eventType = parser.next();
            if (eventType == 4) {
                eventType = parser.next();
            }
        }
        return result;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static GetBucketACLResult parseGetBucketACLResponse(InputStream in, GetBucketACLResult result) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, "utf-8");
        int eventType = parser.getEventType();
        while (eventType != 1) {
            switch (eventType) {
                case 2:
                    String name = parser.getName();
                    if ("Grant".equals(name)) {
                        result.setBucketACL(parser.nextText());
                        break;
                    } else if ("ID".equals(name)) {
                        result.setBucketOwnerID(parser.nextText());
                        break;
                    } else if ("DisplayName".equals(name)) {
                        result.setBucketOwner(parser.nextText());
                        break;
                    }
                    break;
            }
            eventType = parser.next();
            if (eventType == 4) {
                eventType = parser.next();
            }
        }
        return result;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ListObjectsResult parseObjectListResponse(InputStream in, ListObjectsResult result) throws XmlPullParserException, IOException, ParseException {
        result.clearCommonPrefixes();
        result.clearObjectSummaries();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, "utf-8");
        int eventType = parser.getEventType();
        OSSObjectSummary object = null;
        Owner owner = null;
        boolean isCommonPrefixes = false;
        while (eventType != 1) {
            switch (eventType) {
                case 2:
                    String name = parser.getName();
                    if ("Name".equals(name)) {
                        result.setBucketName(parser.nextText());
                        break;
                    } else if ("Prefix".equals(name)) {
                        if (isCommonPrefixes) {
                            String commonPrefix = parser.nextText();
                            if (!OSSUtils.isEmptyString(commonPrefix)) {
                                result.addCommonPrefix(commonPrefix);
                                break;
                            }
                        } else {
                            result.setPrefix(parser.nextText());
                            break;
                        }
                    } else if ("Marker".equals(name)) {
                        result.setMarker(parser.nextText());
                        break;
                    } else if ("Delimiter".equals(name)) {
                        result.setDelimiter(parser.nextText());
                        break;
                    } else if ("EncodingType".equals(name)) {
                        result.setEncodingType(parser.nextText());
                        break;
                    } else if ("MaxKeys".equals(name)) {
                        String maxKeys = parser.nextText();
                        if (!OSSUtils.isEmptyString(maxKeys)) {
                            result.setMaxKeys(Integer.valueOf(maxKeys).intValue());
                            break;
                        }
                    } else if ("NextMarker".equals(name)) {
                        result.setNextMarker(parser.nextText());
                        break;
                    } else if ("IsTruncated".equals(name)) {
                        String isTruncated = parser.nextText();
                        if (!OSSUtils.isEmptyString(isTruncated)) {
                            result.setTruncated(Boolean.valueOf(isTruncated).booleanValue());
                            break;
                        }
                    } else if ("Contents".equals(name)) {
                        object = new OSSObjectSummary();
                        break;
                    } else if ("Key".equals(name)) {
                        object.setKey(parser.nextText());
                        break;
                    } else if ("LastModified".equals(name)) {
                        object.setLastModified(DateUtil.parseIso8601Date(parser.nextText()));
                        break;
                    } else if ("Size".equals(name)) {
                        String size = parser.nextText();
                        if (!OSSUtils.isEmptyString(size)) {
                            object.setSize(Long.valueOf(size).longValue());
                            break;
                        }
                    } else if ("ETag".equals(name)) {
                        object.setETag(parser.nextText());
                        break;
                    } else if ("Type".equals(name)) {
                        object.setType(parser.nextText());
                        break;
                    } else if ("StorageClass".equals(name)) {
                        object.setStorageClass(parser.nextText());
                        break;
                    } else if ("Owner".equals(name)) {
                        owner = new Owner();
                        break;
                    } else if ("ID".equals(name)) {
                        owner.setId(parser.nextText());
                        break;
                    } else if ("DisplayName".equals(name)) {
                        owner.setDisplayName(parser.nextText());
                        break;
                    } else if ("CommonPrefixes".equals(name)) {
                        isCommonPrefixes = true;
                        break;
                    }
                    break;
                case 3:
                    String endTagName = parser.getName();
                    if ("Owner".equals(parser.getName())) {
                        if (owner != null) {
                            object.setOwner(owner);
                            break;
                        }
                    } else if ("Contents".equals(endTagName)) {
                        if (object != null) {
                            object.setBucketName(result.getBucketName());
                            result.addObjectSummary(object);
                            break;
                        }
                    } else if ("CommonPrefixes".equals(endTagName)) {
                        isCommonPrefixes = false;
                        break;
                    }
                    break;
            }
            eventType = parser.next();
            if (eventType == 4) {
                eventType = parser.next();
            }
        }
        return result;
    }

    public static String trimQuotes(String s) {
        if (s == null) {
            return null;
        }
        String s2 = s.trim();
        if (s2.startsWith("\"")) {
            s2 = s2.substring(1);
        }
        return s2.endsWith("\"") ? s2.substring(0, s2.length() - 1) : s2;
    }

    public static ObjectMetadata parseObjectMetadata(Map<String, String> headers) throws IOException {
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            for (String key : headers.keySet()) {
                if (key.indexOf(OSSHeaders.OSS_USER_METADATA_PREFIX) >= 0) {
                    objectMetadata.addUserMetadata(key, headers.get(key));
                } else if (key.equals("Last-Modified") || key.equals("Date")) {
                    try {
                        objectMetadata.setHeader(key, DateUtil.parseRfc822Date(headers.get(key)));
                    } catch (ParseException pe) {
                        throw new IOException(pe.getMessage(), pe);
                    }
                } else if (key.equals("Content-Length")) {
                    Long value = Long.valueOf(headers.get(key));
                    objectMetadata.setHeader(key, value);
                } else if (key.equals("ETag")) {
                    objectMetadata.setHeader(key, trimQuotes(headers.get(key)));
                } else {
                    objectMetadata.setHeader(key, headers.get(key));
                }
            }
            return objectMetadata;
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public static ServiceException parseResponseErrorXML(ResponseMessage response, boolean isHeadRequest) throws ClientException {
        int statusCode = response.getStatusCode();
        String requestId = response.getResponse().header(OSSHeaders.OSS_HEADER_REQUEST_ID);
        String code = null;
        String message = null;
        String hostId = null;
        String errorMessage = null;
        if (!isHeadRequest) {
            try {
                errorMessage = response.getResponse().body().string();
                InputStream inputStream = new ByteArrayInputStream(errorMessage.getBytes());
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(inputStream, "utf-8");
                int eventType = parser.getEventType();
                while (eventType != 1) {
                    switch (eventType) {
                        case 2:
                            if ("Code".equals(parser.getName())) {
                                code = parser.nextText();
                                break;
                            } else if ("Message".equals(parser.getName())) {
                                message = parser.nextText();
                                break;
                            } else if ("RequestId".equals(parser.getName())) {
                                requestId = parser.nextText();
                                break;
                            } else if ("HostId".equals(parser.getName())) {
                                hostId = parser.nextText();
                                break;
                            }
                            break;
                    }
                    eventType = parser.next();
                    if (eventType == 4) {
                        eventType = parser.next();
                    }
                }
            } catch (IOException e) {
                throw new ClientException(e);
            } catch (XmlPullParserException e2) {
                throw new ClientException(e2);
            }
        }
        return new ServiceException(statusCode, message, code, requestId, hostId, errorMessage);
    }
}
