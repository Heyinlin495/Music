package com.example.music.service;

import java.io.InputStream;

public interface StorageService {

    /**
     * Upload a file and return its access URL.
     */
    String uploadFile(String objectName, InputStream inputStream, String contentType, long size);

    /**
     * Delete a file by its object name.
     */
    void deleteFile(String objectName);

    /**
     * Get a presigned/temporary URL for downloading a file.
     */
    String getFileUrl(String objectName);

    /**
     * Get an InputStream for streaming a file (used for range requests).
     */
    InputStream getFileStream(String objectName);

    /**
     * Get the total size of a file in bytes.
     */
    long getFileSize(String objectName);
}
