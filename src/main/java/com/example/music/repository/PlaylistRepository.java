package com.example.music.repository;

import com.example.music.entity.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    @Query("SELECT p FROM Playlist p LEFT JOIN FETCH p.songs WHERE p.user.id = :userId")
    List<Playlist> findByUserIdWithSongs(@Param("userId") Long userId);

    @Query("SELECT p FROM Playlist p LEFT JOIN FETCH p.songs WHERE p.id = :id")
    Optional<Playlist> findByIdWithSongs(@Param("id") Long id);

    List<Playlist> findByUserId(Long userId);
    Page<Playlist> findByIsPublicTrue(Pageable pageable);
    Page<Playlist> findByUserIdAndIsPublicTrue(Long userId, Pageable pageable);
    Page<Playlist> findByNameContainingIgnoreCaseAndIsPublicTrue(String name, Pageable pageable);

    @Query("SELECT p FROM Playlist p LEFT JOIN FETCH p.songs WHERE p.isPublic = true ORDER BY SIZE(p.songs) DESC")
    List<Playlist> findFeaturedPlaylists(Pageable pageable);

    Page<Playlist> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM playlist_songs WHERE song_id = :songId", nativeQuery = true)
    void deleteSongFromPlaylists(@Param("songId") Long songId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Playlist p WHERE p.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
