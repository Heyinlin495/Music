package com.example.music.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Map<String, String> AUDIO_MIME_TYPES = Map.of(
        "mp3", "audio/mpeg",
        "wav", "audio/wav",
        "flac", "audio/flac",
        "m4a", "audio/mp4",
        "aac", "audio/aac",
        "ogg", "audio/ogg"
    );

    @Value("${music.upload.path}")
    private String musicUploadPath;

    @Value("${music.cover.path}")
    private String coverUploadPath;

    @Value("${avatar.upload.path:./uploads/avatars}")
    private String avatarUploadPath;

    private boolean isValidFilename(String filename) {
        return filename != null
            && !filename.contains("..")
            && !filename.contains("/")
            && !filename.contains("\\")
            && filename.matches("^[a-zA-Z0-9_-]+\\.[a-zA-Z0-9]+$");
    }

    @GetMapping("/music/{filename}")
    public ResponseEntity<Resource> getMusicFile(
            @PathVariable String filename,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {
        if (!isValidFilename(filename)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Path uploadDir = Paths.get(musicUploadPath).toAbsolutePath().normalize();
            Path filePath = uploadDir.resolve(filename).normalize();

            if (!filePath.startsWith(uploadDir) || !Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            long fileSize = Files.size(filePath);
            String contentType = getAudioContentType(filename);

            // Support Range requests for seeking
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String rangeValue = rangeHeader.substring(6);
                String[] parts = rangeValue.split("-");
                long start = Long.parseLong(parts[0]);
                long end = parts.length > 1 && !parts[1].isEmpty()
                        ? Long.parseLong(parts[1]) : fileSize - 1;
                end = Math.min(end, fileSize - 1);
                long contentLength = end - start + 1;

                byte[] buffer = new byte[(int) contentLength];
                try (InputStream is = Files.newInputStream(filePath)) {
                    is.skipNBytes(start);
                    int bytesRead = 0;
                    while (bytesRead < contentLength) {
                        int read = is.read(buffer, bytesRead, (int) (contentLength - bytesRead));
                        if (read == -1) break;
                        bytesRead += read;
                    }
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(contentType));
                headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
                headers.setContentLength(contentLength);
                headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);
                headers.set(HttpHeaders.CACHE_CONTROL, "public, max-age=3600");

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .headers(headers)
                        .body(new org.springframework.core.io.ByteArrayResource(buffer));
            }

            // Full file response
            Resource resource = new UrlResource(filePath.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String getAudioContentType(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0) {
            String ext = filename.substring(dotIndex + 1).toLowerCase();
            return AUDIO_MIME_TYPES.getOrDefault(ext, "application/octet-stream");
        }
        return "application/octet-stream";
    }

    @GetMapping("/covers/{filename}")
    public ResponseEntity<Resource> getCoverFile(@PathVariable String filename) {
        if (!isValidFilename(filename)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Path uploadDir = Paths.get(coverUploadPath).toAbsolutePath().normalize();
            Path filePath = uploadDir.resolve(filename).normalize();

            if (!filePath.startsWith(uploadDir)) {
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String contentType = "image/jpeg";
                if (filename.endsWith(".png")) {
                    contentType = "image/png";
                } else if (filename.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (filename.endsWith(".webp")) {
                    contentType = "image/webp";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/avatars/{filename}")
    public ResponseEntity<Resource> getAvatarFile(@PathVariable String filename) {
        if (!isValidFilename(filename)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Path uploadDir = Paths.get(avatarUploadPath).toAbsolutePath().normalize();
            Path filePath = uploadDir.resolve(filename).normalize();

            if (!filePath.startsWith(uploadDir)) {
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String contentType = "image/jpeg";
                if (filename.endsWith(".png")) {
                    contentType = "image/png";
                } else if (filename.endsWith(".webp")) {
                    contentType = "image/webp";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
