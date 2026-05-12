package com.example.music.service;

import com.example.music.dto.PlaylistDTO;
import com.example.music.dto.SongDTO;
import com.example.music.dto.UserDTO;
import com.example.music.entity.Playlist;
import com.example.music.entity.Role;
import com.example.music.entity.Song;
import com.example.music.entity.User;
import com.example.music.repository.CommentRepository;
import com.example.music.repository.PlayHistoryRepository;
import com.example.music.repository.PlaylistRepository;
import com.example.music.repository.SongRepository;
import com.example.music.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;
    private final CommentRepository commentRepository;
    private final PlayHistoryRepository playHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;
    
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalSongs", songRepository.count());
        stats.put("totalPlaylists", playlistRepository.count());
        stats.put("totalComments", commentRepository.count());
        stats.put("activeUsers", userRepository.countActiveUsers());
        stats.put("adminCount", userRepository.countByRole(Role.ADMIN));
        
        stats.put("totalPlays", songRepository.sumAllPlayCounts());
        
        return stats;
    }
    
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCaseOrNicknameContainingIgnoreCase(
                keyword, keyword, pageable);
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @Transactional
    public User updateUserRole(Long userId, Role role) {
        User user = getUserById(userId);
        user.setRole(role);
        return userRepository.save(user);
    }
    
    @Transactional
    public User toggleUserStatus(Long userId) {
        User user = getUserById(userId);
        user.setEnabled(!user.getEnabled());
        return userRepository.save(user);
    }
    
    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        if (user.getRole() == Role.ADMIN) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                throw new RuntimeException("Cannot delete the last admin");
            }
        }
        // Clean up all foreign key references before deleting
        playHistoryRepository.deleteByUserId(userId);
        commentRepository.deleteByUserId(userId);
        playlistRepository.deleteByUserId(userId);
        userRepository.removeUserFromFavorites(userId);
        userRepository.delete(user);
    }
    
    @Transactional
    public User createAdmin(String username, String password, String email, String nickname) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        User admin = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .nickname(nickname)
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        
        return userRepository.save(admin);
    }
    
    @Transactional(readOnly = true)
    public Page<SongDTO> getAllSongs(Pageable pageable) {
        return songRepository.findAll(pageable).map(this::convertSongToDTO);
    }

    @Transactional(readOnly = true)
    public Page<SongDTO> searchSongs(String keyword, Pageable pageable) {
        return songRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
                keyword, keyword, pageable).map(this::convertSongToDTO);
    }

    private SongDTO convertSongToDTO(Song song) {
        return SongDTO.builder()
                .id(song.getId())
                .title(song.getTitle())
                .artist(song.getArtist())
                .album(song.getAlbum())
                .genre(song.getGenre())
                .duration(song.getDuration())
                .coverUrl(song.getCoverUrl())
                .fileUrl(song.getFileUrl())
                .playCount(song.getPlayCount())
                .uploaderId(song.getUploader() != null ? song.getUploader().getId() : null)
                .uploaderName(song.getUploader() != null ? song.getUploader().getNickname() : null)
                .createdAt(song.getCreatedAt())
                .build();
    }
    
    @Transactional
    public void deleteSong(Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));
        // Clean up all foreign key references before deleting
        playHistoryRepository.deleteBySongId(songId);
        commentRepository.deleteBySongId(songId);
        playlistRepository.deleteSongFromPlaylists(songId);
        userRepository.removeSongFromFavorites(songId);
        songRepository.delete(song);
    }
    
    @Transactional
    public SongDTO updateSong(Long songId, String title, String artist, String album, String genre) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        if (title != null) song.setTitle(title);
        if (artist != null) song.setArtist(artist);
        if (album != null) song.setAlbum(album);
        if (genre != null) song.setGenre(genre);

        return convertSongToDTO(songRepository.save(song));
    }

    // Playlist Management
    @Transactional(readOnly = true)
    public Page<PlaylistDTO> getAllPlaylists(Pageable pageable) {
        return playlistRepository.findAll(pageable).map(this::convertPlaylistToDTO);
    }

    @Transactional(readOnly = true)
    public Page<PlaylistDTO> searchPlaylists(String keyword, Pageable pageable) {
        return playlistRepository.findByNameContainingIgnoreCase(keyword, pageable).map(this::convertPlaylistToDTO);
    }

    @Transactional
    public PlaylistDTO createPlaylist(String name, String description, Boolean isPublic, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlist.setDescription(description);
        playlist.setIsPublic(isPublic != null ? isPublic : true);
        playlist.setUser(user);

        playlist = playlistRepository.save(playlist);
        return convertPlaylistToDTO(playlist);
    }

    @Transactional
    public PlaylistDTO updatePlaylist(Long id, String name, String description,
            Boolean isPublic, String coverUrl) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        if (name != null) playlist.setName(name);
        if (description != null) playlist.setDescription(description);
        if (isPublic != null) playlist.setIsPublic(isPublic);
        if (coverUrl != null) playlist.setCoverUrl(coverUrl);

        playlist = playlistRepository.save(playlist);
        return convertPlaylistToDTO(playlist);
    }

    @Transactional
    public void deletePlaylist(Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));
        playlistRepository.delete(playlist);
    }

    @Transactional(readOnly = true)
    public PlaylistDTO getPlaylistDetail(Long playlistId) {
        Playlist playlist = playlistRepository.findByIdWithSongs(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));
        return convertPlaylistToDetailDTO(playlist);
    }

    @Transactional
    public PlaylistDTO addSongToPlaylist(Long playlistId, Long songId) {
        Playlist playlist = playlistRepository.findByIdWithSongs(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        if (!playlist.getSongs().contains(song)) {
            playlist.getSongs().add(song);
            playlist = playlistRepository.save(playlist);
        }

        return convertPlaylistToDetailDTO(playlist);
    }

    @Transactional
    public PlaylistDTO removeSongFromPlaylist(Long playlistId, Long songId) {
        Playlist playlist = playlistRepository.findByIdWithSongs(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        playlist.getSongs().removeIf(s -> s.getId().equals(songId));
        playlist = playlistRepository.save(playlist);

        return convertPlaylistToDetailDTO(playlist);
    }

    @Transactional
    public Map<String, Object> importLocalMusic(String directoryPath) {
        Path dir = Paths.get(directoryPath).toAbsolutePath().normalize();
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            throw new RuntimeException("目录不存在: " + directoryPath);
        }

        User admin = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("管理员用户不存在"));

        int imported = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        try (Stream<Path> files = Files.list(dir)) {
            List<Path> mp3Files = files
                    .filter(p -> p.toString().toLowerCase().endsWith(".mp3"))
                    .filter(p -> !p.getFileName().toString().startsWith("."))
                    .toList();

            for (Path mp3File : mp3Files) {
                try {
                    String filename = mp3File.getFileName().toString();
                    // Remove .mp3 extension
                    String nameWithoutExt = filename.substring(0, filename.lastIndexOf('.'));

                    String artist;
                    String title;
                    // Parse "Artist - Title" pattern
                    if (nameWithoutExt.contains(" - ")) {
                        String[] parts = nameWithoutExt.split(" - ", 2);
                        artist = parts[0].trim();
                        title = parts[1].trim();
                    } else {
                        artist = "未知歌手";
                        title = nameWithoutExt.trim();
                    }

                    // Check if already imported by title+artist
                    if (songRepository.findByTitleIgnoreCaseAndArtistIgnoreCase(title, artist).isPresent()) {
                        skipped++;
                        continue;
                    }

                    // Copy file to uploads directory
                    String ext = ".mp3";
                    String objectName = UUID.randomUUID() + ext;
                    try (InputStream is = Files.newInputStream(mp3File)) {
                        storageService.uploadFile(objectName, is, "audio/mpeg", Files.size(mp3File));
                    }

                    String fileUrl = "/api/files/music/" + objectName;

                    Song song = Song.builder()
                            .title(title)
                            .artist(artist)
                            .album("")
                            .genre("其他")
                            .fileUrl(fileUrl)
                            .playCount(0L)
                            .uploader(admin)
                            .build();
                    songRepository.save(song);
                    imported++;
                } catch (Exception e) {
                    errors.add(mp3File.getFileName() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("读取目录失败: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("imported", imported);
        result.put("skipped", skipped);
        result.put("errors", errors);
        return result;
    }

    private PlaylistDTO convertPlaylistToDTO(Playlist playlist) {
        int songCount = 0;
        if (playlist.getSongs() != null) {
            songCount = playlist.getSongs().size();
        }
        return PlaylistDTO.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .coverUrl(playlist.getCoverUrl())
                .isPublic(playlist.getIsPublic())
                .userId(playlist.getUser().getId())
                .userName(playlist.getUser().getNickname())
                .songCount(songCount)
                .createdAt(playlist.getCreatedAt())
                .updatedAt(playlist.getUpdatedAt())
                .build();
    }

    private PlaylistDTO convertPlaylistToDetailDTO(Playlist playlist) {
        java.util.List<SongDTO> songs = java.util.Collections.emptyList();
        if (playlist.getSongs() != null) {
            songs = playlist.getSongs().stream()
                    .map(song -> SongDTO.builder()
                            .id(song.getId())
                            .title(song.getTitle())
                            .artist(song.getArtist())
                            .album(song.getAlbum())
                            .genre(song.getGenre())
                            .duration(song.getDuration())
                            .coverUrl(song.getCoverUrl())
                            .fileUrl(song.getFileUrl())
                            .playCount(song.getPlayCount())
                            .build())
                    .collect(Collectors.toList());
        }
        return PlaylistDTO.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .coverUrl(playlist.getCoverUrl())
                .isPublic(playlist.getIsPublic())
                .userId(playlist.getUser().getId())
                .userName(playlist.getUser().getNickname())
                .songCount(songs.size())
                .songs(songs)
                .createdAt(playlist.getCreatedAt())
                .updatedAt(playlist.getUpdatedAt())
                .build();
    }
}
