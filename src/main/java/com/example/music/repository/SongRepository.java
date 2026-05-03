package com.example.music.repository;

import com.example.music.entity.Song;
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
public interface SongRepository extends JpaRepository<Song, Long> {

    Optional<Song> findByTitleIgnoreCaseAndArtistIgnoreCase(String title, String artist);
    Page<Song> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
        String title, String artist, Pageable pageable);
    
    Page<Song> findByGenre(String genre, Pageable pageable);
    
    Page<Song> findByArtistContainingIgnoreCase(String artist, Pageable pageable);
    
    @Query("SELECT s FROM Song s ORDER BY s.playCount DESC")
    List<Song> findTopByPlayCount(Pageable pageable);
    
    @Query("SELECT s FROM Song s ORDER BY s.createdAt DESC")
    List<Song> findLatestSongs(Pageable pageable);
    
    @Query("SELECT DISTINCT s.genre FROM Song s WHERE s.genre IS NOT NULL")
    List<String> findAllGenres();

    Page<Song> findByUploaderId(Long uploaderId, Pageable pageable);

    @Modifying
    @Query("UPDATE Song s SET s.playCount = s.playCount + 1 WHERE s.id = :id")
    void incrementPlayCountById(@Param("id") Long id);

    @Query("SELECT COALESCE(SUM(s.playCount), 0) FROM Song s")
    Long sumAllPlayCounts();

    @Query("SELECT s.genre, COUNT(s) FROM Song s WHERE s.genre IS NOT NULL GROUP BY s.genre ORDER BY COUNT(s) DESC")
    List<Object[]> findGenreCounts();

    @Query("SELECT s.coverUrl FROM Song s WHERE s.genre = :genre AND s.coverUrl IS NOT NULL ORDER BY s.playCount DESC LIMIT 1")
    String findTopCoverByGenre(@Param("genre") String genre);

    @Query("SELECT s.album, s.artist, MAX(s.coverUrl), COUNT(s) FROM Song s WHERE s.album IS NOT NULL AND s.album <> '' GROUP BY s.album, s.artist ORDER BY MAX(s.playCount) DESC")
    List<Object[]> findRecommendedAlbums(Pageable pageable);
}
