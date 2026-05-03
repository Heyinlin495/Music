package com.example.music.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    @Value("${music.upload.path:./uploads/music}")
    private String uploadPath;

    @Override
    public String uploadFile(String objectName, InputStream inputStream, String contentType, long size) {
        try {
            Path dir = Paths.get(uploadPath).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path filePath = dir.resolve(objectName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Saved file locally: {}", filePath);
            return "/api/files/music/" + objectName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file locally", e);
        }
    }

    @Override
    public void deleteFile(String objectName) {
        try {
            Path filePath = Paths.get(uploadPath).toAbsolutePath().normalize().resolve(objectName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete local file: {}", e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String objectName) {
        return "/api/files/music/" + objectName;
    }

    @Override
    public InputStream getFileStream(String objectName) {
        try {
            Path filePath = Paths.get(uploadPath).toAbsolutePath().normalize().resolve(objectName);
            return new FileInputStream(filePath.toFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + objectName, e);
        }
    }

    @Override
    public long getFileSize(String objectName) {
        try {
            Path filePath = Paths.get(uploadPath).toAbsolutePath().normalize().resolve(objectName);
            return Files.size(filePath);
        } catch (IOException e) {
            return -1;
        }
    }
}
