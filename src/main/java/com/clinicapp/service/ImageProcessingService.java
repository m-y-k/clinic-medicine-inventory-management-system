package com.clinicapp.service;

import com.clinicapp.model.AppointmentImage;
import com.clinicapp.repository.AppointmentImageRepository;
import com.clinicapp.util.InMemoryMultipartFile;
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

    public MultipartFile compressImageAndVideo(MultipartFile file) throws Exception {

        String type = file.getContentType();

        if (type == null) {
            return file;
        }

        if (type.startsWith("image/")) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Thumbnails.of(file.getInputStream())
                    .scale(1.0)
                    .outputFormat("jpeg")
                    .outputQuality(0.4f) // compression here
                    .toOutputStream(baos);

            byte[] bytes = baos.toByteArray();

            return new InMemoryMultipartFile(
                    file.getName(),
                    file.getOriginalFilename(),
                    "image/jpeg",
                    bytes
            );
        } // don't process for videos, as video upload will take higher memory on cloud.
//        else if (type.startsWith("video/")) {
//
//            File input = File.createTempFile("raw_", ".mp4");
//            File output = File.createTempFile("compressed_", ".mp4");
//
//            try {
//                file.transferTo(input);
//
//                new ProcessBuilder(
//                        "ffmpeg", "-y",
//                        "-i", input.getAbsolutePath(),
//                        "-vcodec", "libx264",
//                        "-crf", "28",
//                        "-preset", "fast",
//                        "-acodec", "aac",
//                        output.getAbsolutePath()
//                ).start().waitFor();
//
//                return new InMemoryMultipartFile(
//                        file.getName(),
//                        file.getOriginalFilename(),
//                        "video/mp4",
//                        java.nio.file.Files.readAllBytes(output.toPath())
//                );
//
//            } finally {
//                input.delete();
//                output.delete();
//            }
//        }
        else {
            return file;
        }
    }

}
