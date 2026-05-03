package com.example.music.util;

import com.example.music.exception.InvalidFileTypeException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class FileValidatorTest {

    // --- Audio file validation ---

    @Test
    void validateAudioFile_ValidMP3() {
        // MP3 with ID3 header
        byte[] mp3Header = new byte[]{0x49, 0x44, 0x33, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        MockMultipartFile file = new MockMultipartFile("file", "test.mp3", "audio/mpeg", mp3Header);

        assertDoesNotThrow(() -> FileValidator.validateAudioFile(file));
    }

    @Test
    void validateAudioFile_ValidMPEGSync() {
        // MP3 with MPEG sync word (0xFF 0xE0)
        byte[] header = new byte[]{(byte) 0xFF, (byte) 0xFB, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        MockMultipartFile file = new MockMultipartFile("file", "test.mp3", "audio/mpeg", header);

        assertDoesNotThrow(() -> FileValidator.validateAudioFile(file));
    }

    @Test
    void validateAudioFile_ValidWAV() {
        byte[] wavHeader = new byte[]{0x52, 0x49, 0x46, 0x46, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        MockMultipartFile file = new MockMultipartFile("file", "test.wav", "audio/wav", wavHeader);

        assertDoesNotThrow(() -> FileValidator.validateAudioFile(file));
    }

    @Test
    void validateAudioFile_ValidFLAC() {
        byte[] flacHeader = new byte[]{0x66, 0x4C, 0x61, 0x43, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        MockMultipartFile file = new MockMultipartFile("file", "test.flac", "audio/flac", flacHeader);

        assertDoesNotThrow(() -> FileValidator.validateAudioFile(file));
    }

    @Test
    void validateAudioFile_ValidM4A() {
        byte[] m4aHeader = new byte[]{0x00, 0x00, 0x00, 0x20, 0x66, 0x74, 0x79, 0x70, 0x4D, 0x34, 0x41, 0x20};
        MockMultipartFile file = new MockMultipartFile("file", "test.m4a", "audio/x-m4a", m4aHeader);

        assertDoesNotThrow(() -> FileValidator.validateAudioFile(file));
    }

    @Test
    void validateAudioFile_NullFile_ThrowsException() {
        assertThrows(InvalidFileTypeException.class, () -> FileValidator.validateAudioFile(null));
    }

    @Test
    void validateAudioFile_EmptyFile_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.mp3", "audio/mpeg", new byte[0]);

        InvalidFileTypeException ex = assertThrows(InvalidFileTypeException.class,
                () -> FileValidator.validateAudioFile(file));
        assertEquals("File is empty", ex.getMessage());
    }

    @Test
    void validateAudioFile_InvalidContentType_ThrowsException() {
        byte[] header = new byte[]{0x49, 0x44, 0x33, 0x03, 0x00, 0x00, 0x00, 0x00};
        MockMultipartFile file = new MockMultipartFile("file", "test.mp3", "application/pdf", header);

        assertThrows(InvalidFileTypeException.class, () -> FileValidator.validateAudioFile(file));
    }

    @Test
    void validateAudioFile_InvalidMagicNumber_ThrowsException() {
        // Valid content type but wrong magic number
        byte[] header = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        MockMultipartFile file = new MockMultipartFile("file", "test.mp3", "audio/mpeg", header);

        InvalidFileTypeException ex = assertThrows(InvalidFileTypeException.class,
                () -> FileValidator.validateAudioFile(file));
        assertTrue(ex.getMessage().contains("does not match audio format"));
    }

    // --- Image file validation ---

    @Test
    void validateImageFile_ValidJPEG() {
        byte[] jpegHeader = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00};
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", jpegHeader);

        assertDoesNotThrow(() -> FileValidator.validateImageFile(file));
    }

    @Test
    void validateImageFile_ValidPNG() {
        byte[] pngHeader = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x00, 0x00, 0x00, 0x00};
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", pngHeader);

        assertDoesNotThrow(() -> FileValidator.validateImageFile(file));
    }

    @Test
    void validateImageFile_ValidWebP() {
        // RIFF....WEBP
        byte[] webpHeader = new byte[]{
                0x52, 0x49, 0x46, 0x46, 0x00, 0x00, 0x00, 0x00,
                0x57, 0x45, 0x42, 0x50, 0x00, 0x00, 0x00, 0x00
        };
        MockMultipartFile file = new MockMultipartFile("file", "test.webp", "image/webp", webpHeader);

        assertDoesNotThrow(() -> FileValidator.validateImageFile(file));
    }

    @Test
    void validateImageFile_NullFile_Returns() {
        // Cover is optional, so null/empty should not throw
        assertDoesNotThrow(() -> FileValidator.validateImageFile(null));
    }

    @Test
    void validateImageFile_EmptyFile_Returns() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);

        assertDoesNotThrow(() -> FileValidator.validateImageFile(file));
    }

    @Test
    void validateImageFile_InvalidContentType_ThrowsException() {
        byte[] header = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "application/octet-stream", header);

        assertThrows(InvalidFileTypeException.class, () -> FileValidator.validateImageFile(file));
    }

    @Test
    void validateImageFile_InvalidMagicNumber_ThrowsException() {
        byte[] header = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", header);

        assertThrows(InvalidFileTypeException.class, () -> FileValidator.validateImageFile(file));
    }

    @Test
    void validateAudioFile_NullContentType_ThrowsException() {
        byte[] header = new byte[]{0x49, 0x44, 0x33, 0x03};
        MockMultipartFile file = new MockMultipartFile("file", "test.mp3", null, header);

        assertThrows(InvalidFileTypeException.class, () -> FileValidator.validateAudioFile(file));
    }
}
