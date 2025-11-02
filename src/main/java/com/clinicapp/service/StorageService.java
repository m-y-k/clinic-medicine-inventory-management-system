package com.clinicapp.service;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class StorageService {

    @Value("${storage.provider:minio}")
    private String provider;

    // MinIO
    @Value("${minio.endpoint:}")
    private String minioEndpoint;
    @Value("${minio.accessKey:}")
    private String minioAccessKey;
    @Value("${minio.secretKey:}")
    private String minioSecretKey;
    @Value("${minio.bucket:clinic-uploads}")
    private String minioBucket;
    @Value("${minio.region:us-east-1}")
    private String minioRegion;

    private MinioClient minioClient;

    @PostConstruct
    public void init() throws Exception {
        if ("minio".equalsIgnoreCase(provider)) {
            minioClient = MinioClient.builder()
                    .endpoint(minioEndpoint)
                    .credentials(minioAccessKey, minioSecretKey)
                    .build();

            // create bucket if not exists
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioBucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioBucket).build());
            }
        }

        // If provider == 's3', you'd init AWS S3 client here (not shown)
    }

    public String generateObjectKey(String appointmentId, String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String ext = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) ext = originalFilename.substring(i);
        return "appointments/" + appointmentId + "/" + uuid + ext;
    }

    public void upload(MultipartFile file, String objectKey) throws Exception {
        if ("minio".equalsIgnoreCase(provider)) {
            try (InputStream in = file.getInputStream()) {
                PutObjectArgs args = PutObjectArgs.builder()
                        .bucket(minioBucket)
                        .object(objectKey)
                        .stream(in, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build();
                minioClient.putObject(args);
            }
        } else {
            throw new UnsupportedOperationException("Only minio provider implemented in StorageService");
        }
    }

    public String getPresignedUrl(String objectKey, int expirySeconds) throws Exception {
        if ("minio".equalsIgnoreCase(provider)) {
            // Generate presigned GET URL
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioBucket)
                            .object(objectKey)
                            .expiry(expirySeconds, TimeUnit.SECONDS)
                            .build());
        }
        throw new UnsupportedOperationException("Only minio provider implemented");
    }

    public void remove(String objectKey) throws Exception {
        if ("minio".equalsIgnoreCase(provider)) {
            RemoveObjectArgs args = RemoveObjectArgs.builder().bucket(minioBucket).object(objectKey).build();
            minioClient.removeObject(args);
        } else {
            throw new UnsupportedOperationException("Only minio provider implemented");
        }
    }

    public InputStream downloadStream(String objectKey) throws Exception {
        if ("minio".equalsIgnoreCase(provider)) {
            return minioClient.getObject(GetObjectArgs.builder().bucket(minioBucket).object(objectKey).build());
        }
        throw new UnsupportedOperationException("Only minio provider implemented");
    }
}
