package com.clinicapp.controller;

import com.clinicapp.model.Appointment;
import com.clinicapp.model.AppointmentImage;
import com.clinicapp.repository.AppointmentRepository;
import com.clinicapp.service.ImageProcessingService;
import com.clinicapp.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin
public class AppointmentImageController {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private StorageService storageService;
    @Autowired private ImageProcessingService imageProcessingService;

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImages(@PathVariable("id") String appointmentId,
                                          @RequestPart("files") MultipartFile[] files) {
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);
        if (!opt.isPresent()) return ResponseEntity.status(404).body("Appointment not found");

        Appointment appt = opt.get();
        if (appt.getImages() == null) appt.setImages(new ArrayList<>());

        List<AppointmentImage> added = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                String objectKey = storageService.generateObjectKey(appointmentId, file.getOriginalFilename());
                storageService.upload(file, objectKey);

                // generate a thumbnail key (same folder)
                String thumbKey = objectKey.replaceFirst("(\\.[^.]+)$", "_thumb$1");
                // async thumbnail generation
                imageProcessingService.generateAndUploadThumbnail(appointmentId, objectKey, thumbKey);

                // create metadata
                AppointmentImage ai = new AppointmentImage();
                ai.setId(UUID.randomUUID().toString());
                ai.setKey(objectKey);
                ai.setThumbnailKey(thumbKey.replace(".mp4", ".jpg"));
                ai.setMimeType(file.getContentType());
                ai.setSize(file.getSize());
                // we can create a presigned URL valid for e.g., 7 days
                ai.setUrl(storageService.getPresignedUrl(objectKey, 7 * 24 * 3600));
                ai.setThumbnailUrl(storageService.getPresignedUrl(thumbKey.replace(".mp4", ".jpg"), 7 * 24 * 3600));
                appt.getImages().add(ai);
                added.add(ai);
            } catch (Exception ex) {
                ex.printStackTrace();
                return ResponseEntity.status(500).body("Upload failed: " + ex.getMessage());
            }
        }

        appointmentRepository.save(appt);
        return ResponseEntity.ok(added);
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable String id, @PathVariable String imageId) {
        Optional<Appointment> opt = appointmentRepository.findById(id);
        if (!opt.isPresent()) return ResponseEntity.status(404).body("Appointment not found");

        Appointment appt = opt.get();
        if (appt.getImages() == null) return ResponseEntity.status(404).body("No images");

        AppointmentImage toRemove = null;
        for (AppointmentImage ai : appt.getImages()) {
            if (imageId.equals(ai.getId())) { toRemove = ai; break; }
        }
        if (toRemove == null) return ResponseEntity.status(404).body("Image not found");

        try {
            if (toRemove.getKey() != null) storageService.remove(toRemove.getKey());
            if (toRemove.getThumbnailKey() != null) storageService.remove(toRemove.getThumbnailKey());
        } catch (Exception ex) {
            ex.printStackTrace();
            // continue to remove metadata anyway
        }

        appt.getImages().remove(toRemove);
        appointmentRepository.save(appt);
        return ResponseEntity.ok("Deleted");
    }
}
