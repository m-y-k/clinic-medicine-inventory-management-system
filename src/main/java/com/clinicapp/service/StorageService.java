package com.clinicapp.service;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URL;
import java.time.Duration;
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


    // S3
    @Value("${aws.s3.bucket:}")
    private String s3Bucket;

    @Value("${aws.s3.region:}")
    private String s3Region;

    @Value("${aws.accessKeyId:}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey:}")
    private String secretAccessKey;

    private S3Client s3Client;
    private S3Presigner presigner;


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
        else if ("s3".equalsIgnoreCase(provider)) {

            AwsBasicCredentials creds =
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey);

            s3Client = S3Client.builder()
                    .region(Region.of(s3Region))
                    .credentialsProvider(StaticCredentialsProvider.create(creds))
                    .build();

            presigner = S3Presigner.builder()
                    .region(Region.of(s3Region))
                    .credentialsProvider(StaticCredentialsProvider.create(creds))
                    .build();
        }
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
        }
        else if ("s3".equalsIgnoreCase(provider)) {

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(s3Bucket)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        }
        else {
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
        else if ("s3".equalsIgnoreCase(provider)) {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                            .bucket(s3Bucket)
                            .key(objectKey)
                            .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofSeconds(expirySeconds))
                            .getObjectRequest(getObjectRequest)
                            .build();

            PresignedGetObjectRequest presigned = presigner.presignGetObject(presignRequest);

            return presigned.url().toString();
        }
        throw new UnsupportedOperationException("Only minio/S3 provider implemented");
    }

    public void remove(String objectKey) throws Exception {
        if ("minio".equalsIgnoreCase(provider)) {
            RemoveObjectArgs args = RemoveObjectArgs.builder().bucket(minioBucket).object(objectKey).build();
            minioClient.removeObject(args);
        }
        else if ("s3".equalsIgnoreCase(provider)) {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                                        .bucket(s3Bucket)
                                        .key(objectKey)
                                        .build());
        }
        else {
            throw new UnsupportedOperationException("Only minio/S3 provider implemented");
        }
    }

    public InputStream downloadStream(String objectKey) throws Exception {
        if ("minio".equalsIgnoreCase(provider)) {
            return minioClient.getObject(GetObjectArgs.builder().bucket(minioBucket).object(objectKey).build());
        }
        throw new UnsupportedOperationException("Only minio/S3 provider implemented");
    }
}
