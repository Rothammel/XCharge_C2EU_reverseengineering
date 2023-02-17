package com.alibaba.sdk.android.oss.model;

import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import java.io.File;

/* loaded from: classes.dex */
public class ResumableUploadRequest extends MultipartUploadRequest {
    private Boolean deleteUploadOnCancelling;
    private String recordDirectory;

    public ResumableUploadRequest(String bucketName, String objectKey, String uploadFilePath) {
        this(bucketName, objectKey, uploadFilePath, null, null);
    }

    public ResumableUploadRequest(String bucketName, String objectKey, String uploadFilePath, ObjectMetadata metadata) {
        this(bucketName, objectKey, uploadFilePath, metadata, null);
    }

    public ResumableUploadRequest(String bucketName, String objectKey, String uploadFilePath, String recordDirectory) {
        this(bucketName, objectKey, uploadFilePath, null, recordDirectory);
    }

    public ResumableUploadRequest(String bucketName, String objectKey, String uploadFilePath, ObjectMetadata metadata, String recordDirectory) {
        super(bucketName, objectKey, uploadFilePath, metadata);
        this.deleteUploadOnCancelling = true;
        setRecordDirectory(recordDirectory);
    }

    public String getRecordDirectory() {
        return this.recordDirectory;
    }

    public void setRecordDirectory(String recordDirectory) {
        if (!OSSUtils.isEmptyString(recordDirectory)) {
            File file = new File(recordDirectory);
            if (!file.exists() || !file.isDirectory()) {
                throw new IllegalArgumentException("Record directory must exist, and it should be a directory!");
            }
        }
        this.recordDirectory = recordDirectory;
    }

    public Boolean deleteUploadOnCancelling() {
        return this.deleteUploadOnCancelling;
    }

    public void setDeleteUploadOnCancelling(Boolean deleteUploadOnCancelling) {
        this.deleteUploadOnCancelling = deleteUploadOnCancelling;
    }
}