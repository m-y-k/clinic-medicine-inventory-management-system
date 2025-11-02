package com.clinicapp.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@Service
public class ImageProcessingService {

    @Autowired
    private StorageService storageService;

    // create thumbnail and upload back to storage; return thumbnail key
    @Async
    public void generateAndUploadThumbnail(String appointmentId, String originalKey, String thumbnailKey) {
        try (InputStream in = storageService.downloadStream(originalKey);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Resize to max width/height 300px keeping aspect ratio
            Thumbnails.of(in).size(300, 300).toOutputStream(baos);

            byte[] thumbBytes = baos.toByteArray();
            MultipartFile thumbFile = new com.clinicapp.util.InMemoryMultipartFile(thumbnailKey, thumbnailKey, "image/jpeg", thumbBytes);

            storageService.upload(thumbFile, thumbnailKey);
        } catch (Exception ex) {
            // log, but don’t fail main request
            ex.printStackTrace();
        }
    }
}
