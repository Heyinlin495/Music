package com.example.music.config;

import com.example.music.entity.Song;
import com.example.music.repository.SongRepository;
import com.example.music.service.QQMusicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class QQMusicDataInitializer implements CommandLineRunner {

    private final SongRepository songRepository;
    private final QQMusicService qqMusicService;

    @Override
    public void run(String... args) {
        Thread.ofVirtual().name("qqmusic-init").start(() -> {
            try {
                // Wait for DataInitializer and DB to be ready
                Thread.sleep(15000);
                populateSongmids();
            } catch (Exception e) {
                log.error("QQMusic ID population failed", e);
            }
        });
    }

    private void populateSongmids() {
        List<Song> songs = songRepository.findAll().stream()
                .filter(s -> s.getSongmid() == null)
                .toList();

        if (songs.isEmpty()) {
            log.info("No songs need QQMusic ID population");
            return;
        }

        log.info("Starting QQMusic ID population for {} songs...", songs.size());
        int matched = 0;

        for (Song song : songs) {
            try {
                String songmid = qqMusicService.searchAndMatch(song.getTitle(), song.getArtist());
                if (songmid != null) {
                    song.setSongmid(songmid);
                    songRepository.save(song);
                    matched++;
                    log.info("Matched: {} - {} -> songmid {}", song.getTitle(), song.getArtist(), songmid);
                } else {
                    log.warn("No QQ Music match: {} - {}", song.getTitle(), song.getArtist());
                }
                Thread.sleep(300);
            } catch (Exception e) {
                log.error("Error matching song: {} - {}", song.getTitle(), song.getArtist(), e);
            }
        }

        log.info("QQMusic ID population complete: {}/{} matched", matched, songs.size());
    }
}
