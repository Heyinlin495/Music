package com.example.music.util;

import com.example.music.exception.InvalidFileTypeException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class FileValidator {

    private static final Set<String> ALLOWED_AUDIO_TYPES = Set.of(
        "audio/mpeg", "audio/mp3", "audio/wav", "audio/flac", "audio/x-m4a"
    );

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
        "image/jpeg", "image/png", "image/webp"
    );

    private static final long MAX_AUDIO_SIZE = 200 * 1024 * 1024; // 200MB
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;  // 5MB

    public static void validateAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileTypeException("File is empty");
        }

        if (file.getSize() > MAX_AUDIO_SIZE) {
            throw new InvalidFileTypeException("Audio file size exceeds 200MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_AUDIO_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileTypeException("Invalid audio file type. Allowed types: MP3, WAV, FLAC, M4A");
        }

        validateAudioMagicNumber(file);
    }

    public static void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return; // Cover is optional
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new InvalidFileTypeException("Image file size exceeds 5MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileTypeException("Invalid image file type. Allowed types: JPEG, PNG, WebP");
        }

        validateImageMagicNumber(file);
    }

    private static void validateAudioMagicNumber(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int bytesRead = is.read(header);

            if (bytesRead < 4) {
                throw new InvalidFileTypeException("Invalid audio file: unable to read file header");
            }

            // MP3: ID3 tag or MPEG sync
            if ((header[0] == 'I' && header[1] == 'D' && header[2] == '3') ||
                (header[0] == (byte)0xFF && (header[1] & 0xE0) == 0xE0)) {
                return;
            }

            // WAV: RIFF header
            if (header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F') {
                return;
            }

            // FLAC: fLaC header
            if (header[0] == 'f' && header[1] == 'L' && header[2] == 'a' && header[3] == 'C') {
                return;
            }

            // M4A: ftyp header (at offset 4)
            if (bytesRead >= 12 && header[4] == 'f' && header[5] == 't' && header[6] == 'y' && header[7] == 'p') {
                return;
            }

            throw new InvalidFileTypeException("Invalid audio file: file content does not match audio format");
        } catch (IOException e) {
            throw new InvalidFileTypeException("Unable to validate audio file: " + e.getMessage());
        }
    }

    private static void validateImageMagicNumber(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int bytesRead = is.read(header);

            if (bytesRead < 4) {
                throw new InvalidFileTypeException("Invalid image file: unable to read file header");
            }

            // JPEG: FF D8 FF
            if (header[0] == (byte)0xFF && header[1] == (byte)0xD8 && header[2] == (byte)0xFF) {
                return;
            }

            // PNG: 89 50 4E 47
            if (header[0] == (byte)0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
                return;
            }

            // WebP: RIFF....WEBP
            if (bytesRead >= 12 &&
                header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F' &&
                header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
                return;
            }

            throw new InvalidFileTypeException("Invalid image file: file content does not match image format");
        } catch (IOException e) {
            throw new InvalidFileTypeException("Unable to validate image file: " + e.getMessage());
        }
    }
}
