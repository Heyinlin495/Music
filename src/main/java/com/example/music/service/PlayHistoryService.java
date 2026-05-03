package com.example.music.service;

import com.example.music.dto.SongDTO;
import com.example.music.entity.PlayHistory;
import com.example.music.entity.Song;
import com.example.music.entity.User;
import com.example.music.repository.PlayHistoryRepository;
import com.example.music.repository.SongRepository;
import com.example.music.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayHistoryService {
    
    private final PlayHistoryRepository playHistoryRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    
    @Transactional
    public void recordPlay(Long userId, Long songId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));
        
        PlayHistory history = PlayHistory.builder()
                .user(user)
                .song(song)
                .build();
        
        playHistoryRepository.save(history);
    }
    
    @Transactional
    public void clearPlayHistory(Long userId) {
        playHistoryRepository.deleteByUserId(userId);
    }

    @Transactional
    public void deleteHistoryItem(Long userId, Long songId) {
        playHistoryRepository.deleteByUserIdAndSongId(userId, songId);
    }

    @Transactional
    public void deleteHistoryItems(Long userId, List<Long> songIds) {
        for (Long songId : songIds) {
            playHistoryRepository.deleteByUserIdAndSongId(userId, songId);
        }
    }

    @Transactional(readOnly = true)
    public Page<SongDTO> getPlayHistory(Long userId, Pageable pageable) {
        return playHistoryRepository.findByUserIdOrderByPlayedAtDesc(userId, pageable)
                .map(history -> SongDTO.builder()
                        .id(history.getSong().getId())
                        .title(history.getSong().getTitle())
                        .artist(history.getSong().getArtist())
                        .album(history.getSong().getAlbum())
                        .genre(history.getSong().getGenre())
                        .duration(history.getSong().getDuration())
                        .coverUrl(history.getSong().getCoverUrl())
                        .fileUrl(history.getSong().getFileUrl())
                        .playCount(history.getSong().getPlayCount())
                        .build());
    }
}
