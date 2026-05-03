package com.example.music.service;

import com.example.music.dto.PlaylistDTO;
import com.example.music.dto.SongDTO;
import com.example.music.entity.Playlist;
import com.example.music.entity.Song;
import com.example.music.entity.User;
import com.example.music.repository.PlaylistRepository;
import com.example.music.repository.SongRepository;
import com.example.music.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaylistService {
    
    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public List<PlaylistDTO> getUserPlaylists(Long userId) {
        return playlistRepository.findByUserIdWithSongs(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<PlaylistDTO> getPublicPlaylists(Pageable pageable) {
        return playlistRepository.findByIsPublicTrue(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<PlaylistDTO> getFeaturedPlaylists(int limit) {
        List<Playlist> playlists = playlistRepository.findFeaturedPlaylists(
                org.springframework.data.domain.PageRequest.of(0, limit));
        return playlists.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public PlaylistDTO getPlaylistById(Long id, Long currentUserId) {
        Playlist playlist = playlistRepository.findByIdWithSongs(id)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));
        
        if (!playlist.getIsPublic() && !playlist.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("You don't have permission to view this playlist");
        }
        
        return convertToDetailDTO(playlist, currentUserId);
    }
    
    @Transactional
    public PlaylistDTO createPlaylist(String name, String description, Boolean isPublic, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Playlist playlist = Playlist.builder()
                .name(name)
                .description(description)
                .isPublic(isPublic != null ? isPublic : true)
                .user(user)
                .build();
        
        playlist = playlistRepository.save(playlist);
        return convertToDTO(playlist);
    }
    
    @Transactional
    public PlaylistDTO updatePlaylist(Long id, String name, String description,
            Boolean isPublic, Long userId) {
        return updatePlaylist(id, name, description, isPublic, null, userId);
    }

    @Transactional
    public PlaylistDTO updatePlaylist(Long id, String name, String description,
            Boolean isPublic, String coverUrl, Long userId) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        if (!playlist.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only edit your own playlists");
        }

        if (name != null) playlist.setName(name);
        if (description != null) playlist.setDescription(description);
        if (isPublic != null) playlist.setIsPublic(isPublic);
        if (coverUrl != null) playlist.setCoverUrl(coverUrl);

        playlist = playlistRepository.save(playlist);
        return convertToDTO(playlist);
    }
    
    @Transactional
    public void deletePlaylist(Long id, Long userId) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));
        
        if (!playlist.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own playlists");
        }
        
        playlistRepository.delete(playlist);
    }
    
    @Transactional
    public PlaylistDTO addSongToPlaylist(Long playlistId, Long songId, Long userId) {
        Playlist playlist = playlistRepository.findByIdWithSongs(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));
        
        if (!playlist.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only edit your own playlists");
        }
        
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));
        
        if (!playlist.getSongs().contains(song)) {
            playlist.getSongs().add(song);
            playlist = playlistRepository.save(playlist);
        }
        
        return convertToDTO(playlist);
    }
    
    @Transactional
    public PlaylistDTO removeSongFromPlaylist(Long playlistId, Long songId, Long userId) {
        Playlist playlist = playlistRepository.findByIdWithSongs(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));
        
        if (!playlist.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only edit your own playlists");
        }
        
        playlist.getSongs().removeIf(s -> s.getId().equals(songId));
        playlist = playlistRepository.save(playlist);
        
        return convertToDTO(playlist);
    }
    
    private PlaylistDTO convertToDTO(Playlist playlist) {
        return PlaylistDTO.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .coverUrl(playlist.getCoverUrl())
                .isPublic(playlist.getIsPublic())
                .userId(playlist.getUser().getId())
                .userName(playlist.getUser().getNickname())
                .songCount(playlist.getSongs().size())
                .createdAt(playlist.getCreatedAt())
                .updatedAt(playlist.getUpdatedAt())
                .build();
    }
    
    private PlaylistDTO convertToDetailDTO(Playlist playlist, Long currentUserId) {
        List<SongDTO> songs = playlist.getSongs().stream()
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
        
        return PlaylistDTO.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .coverUrl(playlist.getCoverUrl())
                .isPublic(playlist.getIsPublic())
                .userId(playlist.getUser().getId())
                .userName(playlist.getUser().getNickname())
                .songCount(playlist.getSongs().size())
                .songs(songs)
                .createdAt(playlist.getCreatedAt())
                .updatedAt(playlist.getUpdatedAt())
                .build();
    }
}
