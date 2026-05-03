package com.example.music.repository;

import com.example.music.entity.PlayHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {
    Page<PlayHistory> findByUserIdOrderByPlayedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT ph.song.id, COUNT(ph) as cnt FROM PlayHistory ph GROUP BY ph.song.id ORDER BY cnt DESC")
    List<Object[]> findMostPlayedSongIds(Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PlayHistory ph WHERE ph.user.id = :userId AND ph.song.id = :songId")
    void deleteByUserIdAndSongId(@Param("userId") Long userId, @Param("songId") Long songId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PlayHistory ph WHERE ph.song.id = :songId")
    void deleteBySongId(@Param("songId") Long songId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PlayHistory ph WHERE ph.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
