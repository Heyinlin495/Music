package com.example.music.repository;

import com.example.music.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findBySongIdOrderByCreatedAtDesc(Long songId, Pageable pageable);
    long countBySongId(Long songId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Comment c WHERE c.song.id = :songId")
    void deleteBySongId(@Param("songId") Long songId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Comment c WHERE c.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
