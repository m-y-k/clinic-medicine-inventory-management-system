package com.clinicapp.service;

import com.clinicapp.model.AppointmentImage;
import com.clinicapp.repository.AppointmentImageRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.model.Picture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Optional;

@Service
public class ImageProcessingService {

    @Autowired
    private StorageService storageService;

    @Autowired
    private AppointmentImageRepository appointmentImageRepository;

    @Async
    public void generateAndUploadThumbnail(String appointmentId, String originalKey, String thumbnailKey) {
        try {
            String fileExt = getFileExtension(originalKey).toLowerCase();

            // --- IMAGE HANDLING ---
            if (fileExt.matches("jpg|jpeg|png|gif|bmp")) {
                createImageThumbnail(originalKey, thumbnailKey);
            }

            // --- VIDEO HANDLING ---
            else if (fileExt.matches("mp4|mov|avi|mkv|webm")) {
                createVideoThumbnail(originalKey, thumbnailKey.replace(".mp4", ".jpg"));
            }

            else {
                System.out.println("Skipping thumbnail for unsupported file: " + originalKey);
            }

        } catch (Exception ex) {
            ex.printStackTrace(); // can also log with SLF4J instead
        }
    }


    // Create image thumbnail

    private void createImageThumbnail(String originalKey, String thumbnailKey) throws Exception {
        try (InputStream in = storageService.downloadStream(originalKey);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Thumbnails.of(in)
                    .size(300, 300)
                    .outputFormat("jpeg")
                    .toOutputStream(baos);

            byte[] thumbBytes = baos.toByteArray();
            MultipartFile thumbFile = new com.clinicapp.util.InMemoryMultipartFile(
                    thumbnailKey, thumbnailKey, "image/jpeg", thumbBytes
            );

            storageService.upload(thumbFile, thumbnailKey);
            System.out.println("Image thumbnail uploaded: " + thumbnailKey);
        }
    }

    // -----------------------------
    // Create video thumbnail using JCodec
    // -----------------------------
    private void createVideoThumbnail(String originalKey, String thumbnailKey) throws Exception {
        // Use only safe characters for the temp file name
        String safeName = originalKey.replaceAll("[^a-zA-Z0-9.-]", "_");

        // Create temp file in system temp directory
        File tempVideo = File.createTempFile("video_", "_" + safeName);

        // Download video to temp file
        try (InputStream in = storageService.downloadStream(originalKey);
             OutputStream out = new FileOutputStream(tempVideo)) {
            in.transferTo(out);
        }

        // Grab first frame and convert to BufferedImage
        Picture picture = FrameGrab.getFrameFromFile(tempVideo, 0);
        BufferedImage bufferedImage = org.jcodec.scale.AWTUtil.toBufferedImage(picture);

        // Write thumbnail to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", baos);
        byte[] thumbBytes = baos.toByteArray();

        MultipartFile thumbFile = new com.clinicapp.util.InMemoryMultipartFile(
                thumbnailKey, thumbnailKey, "image/jpeg", thumbBytes
        );

        storageService.upload(thumbFile, thumbnailKey);
        System.out.println("Video thumbnail uploaded: " + thumbnailKey);

        tempVideo.delete();
    }

    // Utility
    private String getFileExtension(String key) {
        int lastDot = key.lastIndexOf('.');
        return (lastDot != -1) ? key.substring(lastDot + 1) : "";
    }


}
