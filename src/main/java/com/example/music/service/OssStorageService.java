package com.example.music.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "oss")
public class OssStorageService implements StorageService {

    @Value("${storage.oss.endpoint}")
    private String endpoint;

    @Value("${storage.oss.access-key-id}")
    private String accessKeyId;

    @Value("${storage.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${storage.oss.bucket-name}")
    private String bucketName;

    @Value("${storage.oss.cdn-domain:}")
    private String cdnDomain;

    private OSS ossClient;

    @PostConstruct
    public void init() {
        ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        log.info("Alibaba Cloud OSS client initialized for bucket: {}", bucketName);
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    @Override
    public String uploadFile(String objectName, InputStream inputStream, String contentType, long size) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(size);

            PutObjectRequest request = new PutObjectRequest(bucketName, objectName, inputStream, metadata);
            ossClient.putObject(request);
            log.info("Uploaded file to OSS: {}", objectName);
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("Failed to upload file to OSS: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to OSS", e);
        }
    }

    @Override
    public void deleteFile(String objectName) {
        try {
            ossClient.deleteObject(bucketName, objectName);
            log.info("Deleted file from OSS: {}", objectName);
        } catch (Exception e) {
            log.error("Failed to delete file from OSS: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getFileUrl(String objectName) {
        if (cdnDomain != null && !cdnDomain.isEmpty()) {
            return cdnDomain + "/" + objectName;
        }
        return "https://" + bucketName + "." + endpoint + "/" + objectName;
    }

    @Override
    public InputStream getFileStream(String objectName) {
        try {
            OSSObject ossObject = ossClient.getObject(bucketName, objectName);
            return ossObject.getObjectContent();
        } catch (Exception e) {
            log.error("Failed to get file stream from OSS: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get file from OSS", e);
        }
    }

    @Override
    public long getFileSize(String objectName) {
        try {
            ObjectMetadata metadata = ossClient.getObjectMetadata(bucketName, objectName);
            return metadata.getContentLength();
        } catch (Exception e) {
            log.error("Failed to get file size from OSS: {}", e.getMessage(), e);
            return -1;
        }
    }
}
