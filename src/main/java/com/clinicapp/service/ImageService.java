package com.clinicapp.service;

import com.clinicapp.model.AppointmentImage;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class ImageService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.endpoint}")
    private String endpoint;

    public AppointmentImage uploadImage(MultipartFile file) throws Exception {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        System.out.println(fileName);
        // Upload to MinIO
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        AppointmentImage img = new AppointmentImage();
        img.setId(UUID.randomUUID().toString());
        img.setFileName(fileName);
        img.setMimeType(file.getContentType());
        img.setSize(file.getSize());
        img.setUrl(endpoint + "/" + bucket + "/" + fileName);
        System.out.println(endpoint + "/" + bucket + "/" + fileName);
        return img;
    }
}
