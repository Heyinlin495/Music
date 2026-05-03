package com.example.music.controller;

import com.example.music.entity.Song;
import com.example.music.repository.SongRepository;
import com.example.music.service.QQMusicService;
import com.example.music.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicStreamController {

    private final SongRepository songRepository;
    private final QQMusicService qqMusicService;
    private final RestTemplate restTemplate;
    private final StorageService storageService;

    @GetMapping("/stream/{songId}")
    public ResponseEntity<byte[]> streamSong(@PathVariable Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found"));

        if (song.getSongmid() == null) {
            log.warn("Song {} has no songmid", songId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "该歌曲没有QQ音乐源");
        }

        log.info("Streaming song {} (songmid: {})", songId, song.getSongmid());
        String audioUrl = qqMusicService.getAudioUrl(song.getSongmid());
        log.info("Audio URL for song {}: {}", songId, audioUrl);
        if (audioUrl == null || audioUrl.isEmpty()) {
            log.warn("No audio URL available for song {} (songmid: {})", songId, song.getSongmid());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "无法获取音频URL，QQ音乐Key可能已过期");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    audioUrl, HttpMethod.GET, entity, byte[].class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "获取音频数据失败");
            }

            byte[] audioData = response.getBody();
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(audioData.length))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                    .body(audioData);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Error streaming song {} (songmid: {}): {}", songId, song.getSongmid(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "音频流获取失败: " + e.getMessage());
        }
    }

    /**
     * Stream locally stored music files with Range request support.
     */
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
            headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
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

    @GetMapping("/lyrics/{songId}")
    public ResponseEntity<Map<String, Object>> getLyrics(@PathVariable Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found"));

        if (song.getSongmid() == null) {
            return ResponseEntity.ok(Map.of("result", 404, "message", "No QQ Music source"));
        }

        String lyrics = qqMusicService.getLyrics(song.getSongmid());
        if (lyrics == null) {
            return ResponseEntity.ok(Map.of("result", 404, "message", "Lyrics not available"));
        }

        return ResponseEntity.ok(Map.of("result", 200, "lyric", lyrics));
    }
}
