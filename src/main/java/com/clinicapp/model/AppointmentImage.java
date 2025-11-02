//package com.clinicapp.model;
//
//import java.util.Date;
//
//public class AppointmentImage {
//    private String id;          // UUID
//    private String key;         // object key in storage (unique)
//    private String url;         // public or presigned URL
//    private String thumbnailKey;
//    private String thumbnailUrl;
//    private String mimeType;
//    private long size;
//    private Date uploadedAt;
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getKey() {
//        return key;
//    }
//
//    public void setKey(String key) {
//        this.key = key;
//    }
//
//    public String getUrl() {
//        return url;
//    }
//
//    public void setUrl(String url) {
//        this.url = url;
//    }
//
//    public String getThumbnailKey() {
//        return thumbnailKey;
//    }
//
//    public void setThumbnailKey(String thumbnailKey) {
//        this.thumbnailKey = thumbnailKey;
//    }
//
//    public String getThumbnailUrl() {
//        return thumbnailUrl;
//    }
//
//    public void setThumbnailUrl(String thumbnailUrl) {
//        this.thumbnailUrl = thumbnailUrl;
//    }
//
//    public String getMimeType() {
//        return mimeType;
//    }
//
//    public void setMimeType(String mimeType) {
//        this.mimeType = mimeType;
//    }
//
//    public long getSize() {
//        return size;
//    }
//
//    public void setSize(long size) {
//        this.size = size;
//    }
//
//    public Date getUploadedAt() {
//        return uploadedAt;
//    }
//
//    public void setUploadedAt(Date uploadedAt) {
//        this.uploadedAt = uploadedAt;
//    }
//
//    public AppointmentImage() {
//        this.uploadedAt = new Date();
//    }
//
//    // getters & setters...
//}


package com.clinicapp.model;

import java.util.Date;

public class AppointmentImage {

    private String id;          // UUID
    private String key;         // object key in storage (unique)

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    private String url;         // MinIO URL
    private String fileName;
    private String mimeType;
    private long size;
    private Date uploadedAt;
    private String thumbnailKey;

    public String getThumbnailKey() {
        return thumbnailKey;
    }

    public void setThumbnailKey(String thumbnailKey) {
        this.thumbnailKey = thumbnailKey;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    private String thumbnailUrl;

    public AppointmentImage() {
        this.uploadedAt = new Date();
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public Date getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Date uploadedAt) { this.uploadedAt = uploadedAt; }
}
