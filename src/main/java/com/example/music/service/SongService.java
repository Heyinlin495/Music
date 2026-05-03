package com.example.music.service;

import com.example.music.dto.AlbumDTO;
import com.example.music.dto.SongDTO;
import com.example.music.entity.Song;
import com.example.music.entity.User;
import com.example.music.repository.SongRepository;
import com.example.music.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.example.music.dto.GenreSummaryDTO;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Value("${music.upload.path}")
    private String musicUploadPath;

    @Value("${music.cover.path}")
    private String coverUploadPath;
    
    @Transactional(readOnly = true)
    public Page<SongDTO> getAllSongs(Pageable pageable, Long currentUserId) {
        Page<Song> songs = songRepository.findAll(pageable);
        Set<Long> favoriteIds = loadFavoriteIds(currentUserId);
        return songs.map(song -> convertToDTOWithFavorites(song, favoriteIds));
    }
    
    @Transactional(readOnly = true)
    public Optional<SongDTO> getSongById(Long id, Long currentUserId) {
        return songRepository.findById(id)
                .map(song -> {
                    Set<Long> favoriteIds = loadFavoriteIds(currentUserId);
                    return convertToDTOWithFavorites(song, favoriteIds);
                });
    }
    
    @Transactional(readOnly = true)
    public Page<SongDTO> searchSongs(String keyword, Pageable pageable, Long currentUserId) {
        Page<Song> songs = songRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
                keyword, keyword, pageable);
        Set<Long> favoriteIds = loadFavoriteIds(currentUserId);
        return songs.map(song -> convertToDTOWithFavorites(song, favoriteIds));
    }
    
    @Transactional(readOnly = true)
    public List<SongDTO> getTopSongs(int limit, Long currentUserId) {
        List<Song> songs = songRepository.findTopByPlayCount(PageRequest.of(0, limit));
        Set<Long> favoriteIds = loadFavoriteIds(currentUserId);
        return songs.stream()
                .map(song -> convertToDTOWithFavorites(song, favoriteIds))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AlbumDTO> getRecommendedAlbums(Pageable pageable) {
        List<Object[]> results = songRepository.findRecommendedAlbums(pageable);
        List<AlbumDTO> albums = results.stream()
                .map(row -> AlbumDTO.builder()
                        .album((String) row[0])
                        .artist((String) row[1])
                        .coverUrl((String) row[2])
                        .songCount((Long) row[3])
                        .build())
                .collect(Collectors.toList());
        return new org.springframework.data.domain.PageImpl<>(albums, pageable, albums.size());
    }

    @Transactional(readOnly = true)
    public List<SongDTO> getLatestSongs(int limit, Long currentUserId) {
        List<Song> songs = songRepository.findLatestSongs(PageRequest.of(0, limit));
        Set<Long> favoriteIds = loadFavoriteIds(currentUserId);
        return songs.stream()
                .map(song -> convertToDTOWithFavorites(song, favoriteIds))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<SongDTO> getSongsByGenre(String genre, Pageable pageable, Long currentUserId) {
        Set<Long> favoriteIds = loadFavoriteIds(currentUserId);
        return songRepository.findByGenre(genre, pageable)
                .map(song -> convertToDTOWithFavorites(song, favoriteIds));
    }
    
    @Transactional(readOnly = true)
    public List<String> getAllGenres() {
        return songRepository.findAllGenres();
    }

    @Transactional(readOnly = true)
    public List<GenreSummaryDTO> getGenreSummaries() {
        List<Object[]> counts = songRepository.findGenreCounts();
        return counts.stream()
                .map(row -> {
                    String genre = (String) row[0];
                    Long songCount = (Long) row[1];
                    String coverUrl = songRepository.findTopCoverByGenre(genre);
                    return GenreSummaryDTO.builder()
                            .genre(genre)
                            .songCount(songCount)
                            .coverUrl(coverUrl)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    @Transactional
    public SongDTO uploadSong(MultipartFile file, MultipartFile cover, String title,
            String artist, String album, String genre, Integer duration,
            String lyrics, Long uploaderId) throws IOException {

        // Check for duplicate song by title+artist
        if (songRepository.findByTitleIgnoreCaseAndArtistIgnoreCase(title, artist).isPresent()) {
            throw new RuntimeException("歌曲已存在: " + artist + " - " + title);
        }

        // Validate files
        com.example.music.util.FileValidator.validateAudioFile(file);
        com.example.music.util.FileValidator.validateImageFile(cover);

        // Generate unique filenames
        String musicExt = getExtension(file.getOriginalFilename());
        String musicObjectName = UUID.randomUUID() + musicExt;

        // Upload music file via storage service
        String fileUrl;
        try (InputStream is = file.getInputStream()) {
            fileUrl = storageService.uploadFile(musicObjectName, is, file.getContentType(), file.getSize());
        }

        // Upload cover if provided
        String coverUrl = null;
        if (cover != null && !cover.isEmpty()) {
            String coverExt = getExtension(cover.getOriginalFilename());
            String coverObjectName = "covers/" + UUID.randomUUID() + coverExt;
            try (InputStream is = cover.getInputStream()) {
                coverUrl = storageService.uploadFile(coverObjectName, is, cover.getContentType(), cover.getSize());
            }
        }

        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Song song = Song.builder()
                .title(title)
                .artist(artist)
                .album(album)
                .genre(genre)
                .duration(duration)
                .fileUrl(fileUrl)
                .coverUrl(coverUrl)
                .lyrics(lyrics)
                .uploader(uploader)
                .build();

        song = songRepository.save(song);
        return convertToDTOWithFavorites(song, Collections.emptySet());
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }
    
    @Transactional
    public void incrementPlayCount(Long songId) {
        songRepository.incrementPlayCountById(songId);
    }
    
    @Transactional
    public void deleteSong(Long songId, Long userId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));
        
        if (!song.getUploader().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own songs");
        }
        
        songRepository.delete(song);
    }
    
    private Set<Long> loadFavoriteIds(Long currentUserId) {
        if (currentUserId == null) return Collections.emptySet();
        User user = userRepository.findByIdWithFavorites(currentUserId).orElse(null);
        if (user == null) return Collections.emptySet();
        return user.getFavorites().stream()
                .map(Song::getId)
                .collect(Collectors.toSet());
    }

    private SongDTO convertToDTOWithFavorites(Song song, Set<Long> favoriteSongIds) {
        return SongDTO.builder()
                .id(song.getId())
                .title(song.getTitle())
                .artist(song.getArtist())
                .album(song.getAlbum())
                .genre(song.getGenre())
                .duration(song.getDuration())
                .coverUrl(song.getCoverUrl())
                .fileUrl(song.getFileUrl())
                .songmid(song.getSongmid())
                .lyrics(song.getLyrics())
                .playCount(song.getPlayCount())
                .uploaderId(song.getUploader() != null ? song.getUploader().getId() : null)
                .uploaderName(song.getUploader() != null ? song.getUploader().getNickname() : null)
                .createdAt(song.getCreatedAt())
                .isFavorite(favoriteSongIds.contains(song.getId()))
                .build();
    }
}
