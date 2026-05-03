package com.example.music.service;

import com.example.music.dto.UserDTO;
import com.example.music.dto.SongDTO;
import com.example.music.entity.Song;
import com.example.music.entity.User;
import com.example.music.repository.SongRepository;
import com.example.music.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${avatar.upload.path:./uploads/avatars}")
    private String avatarUploadPath;
    
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id).map(this::convertToDTO);
    }
    
    public Optional<UserDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username).map(this::convertToDTO);
    }
    
    @Transactional
    public UserDTO updateAvatar(Long userId, MultipartFile file) throws IOException {
        String ext = getFileExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;

        Path dir = Paths.get(avatarUploadPath).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Path filePath = dir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setAvatar("/api/files/avatars/" + filename);
        user = userRepository.save(user);
        return convertToDTO(user);
    }

    private String getFileExtension(String filename) {
        if (filename == null) return ".jpg";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".jpg";
    }

    @Transactional
    public UserDTO updateUser(Long id, String nickname, String bio, String avatar, String email) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (nickname != null && nickname.length() > 5) {
            throw new RuntimeException("昵称不能超过5个字");
        }

        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new RuntimeException("该邮箱已被使用");
            }
            user.setEmail(email);
        }
        if (nickname != null) user.setNickname(nickname);
        if (bio != null) user.setBio(bio);
        if (avatar != null) user.setAvatar(avatar);

        user = userRepository.save(user);
        return convertToDTO(user);
    }
    
    @Transactional
    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    @Transactional
    public void addFavorite(Long userId, Long songId) {
        User user = userRepository.findByIdWithFavorites(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));
        
        user.getFavorites().add(song);
        userRepository.save(user);
    }
    
    @Transactional
    public void removeFavorite(Long userId, Long songId) {
        User user = userRepository.findByIdWithFavorites(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.getFavorites().removeIf(s -> s.getId().equals(songId));
        userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public List<SongDTO> getFavorites(Long userId) {
        User user = userRepository.findByIdWithFavorites(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return user.getFavorites().stream()
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
                        .isFavorite(true)
                        .build())
                .collect(Collectors.toList());
    }
    
    public boolean isFavorite(Long userId, Long songId) {
        return userRepository.isFavorite(userId, songId);
    }
    
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
