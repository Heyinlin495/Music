package com.example.music.controller;

import com.example.music.entity.Song;
import com.example.music.repository.SongRepository;
import com.example.music.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicStreamController {

    private final SongRepository songRepository;
    private final StorageService storageService;

    @GetMapping("/stream/local/{songId}")
    public ResponseEntity<?> streamLocalSong(
            @PathVariable Long songId,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found"));

        if (song.getFileUrl() == null || song.getFileUrl().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "该歌曲没有本地音频文件");
        }

        // Extract object name from fileUrl (e.g., /api/files/music/xxx.mp3 -> xxx.mp3)
        String fileUrl = song.getFileUrl();
        String objectName;
        if (fileUrl.startsWith("/api/files/music/")) {
            objectName = fileUrl.substring("/api/files/music/".length());
        } else {
            objectName = fileUrl;
        }

        try {
            long fileSize = storageService.getFileSize(objectName);
            if (fileSize <= 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "音频文件不存在");
            }

            // Parse Range header
            long start = 0;
            long end = fileSize - 1;

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String rangeValue = rangeHeader.substring(6);
                String[] parts = rangeValue.split("-");
                start = Long.parseLong(parts[0]);
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    end = Long.parseLong(parts[1]);
                }
                end = Math.min(end, fileSize - 1);
            }

            long contentLength = end - start + 1;

            // Read the requested byte range
            InputStream fullStream = storageService.getFileStream(objectName);
            fullStream.skipNBytes(start);
            byte[] buffer = new byte[(int) contentLength];
            int bytesRead = 0;
            while (bytesRead < contentLength) {
                int read = fullStream.read(buffer, bytesRead, (int) (contentLength - bytesRead));
                if (read == -1) break;
                bytesRead += read;
            }
            fullStream.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(getAudioContentType(objectName)));
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.setContentLength(contentLength);
            headers.set(HttpHeaders.CACHE_CONTROL, "public, max-age=3600");

            if (rangeHeader != null) {
                headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .headers(headers)
                        .body(buffer);
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(buffer);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error streaming local song {}: {}", songId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "音频流读取失败");
        }
    }

    private static final Map<String, String> AUDIO_MIME_TYPES = Map.of(
        "mp3", "audio/mpeg",
        "wav", "audio/wav",
        "flac", "audio/flac",
        "m4a", "audio/mp4",
        "aac", "audio/aac",
        "ogg", "audio/ogg"
    );

    private String getAudioContentType(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0) {
            String ext = filename.substring(dotIndex + 1).toLowerCase();
            return AUDIO_MIME_TYPES.getOrDefault(ext, "application/octet-stream");
        }
        return "application/octet-stream";
    }
}
