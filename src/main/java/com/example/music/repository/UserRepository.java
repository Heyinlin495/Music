package com.example.music.repository;

import com.example.music.entity.Role;
import com.example.music.entity.User;
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
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    List<User> findByRole(Role role);
    Page<User> findByUsernameContainingIgnoreCaseOrNicknameContainingIgnoreCase(
            String username, String nickname, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(Role role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countActiveUsers();

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.favorites WHERE u.id = :id")
    Optional<User> findByIdWithFavorites(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM User u JOIN u.favorites s WHERE u.id = :userId AND s.id = :songId")
    boolean isFavorite(@Param("userId") Long userId, @Param("songId") Long songId);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM user_favorites WHERE song_id = :songId", nativeQuery = true)
    void removeSongFromFavorites(@Param("songId") Long songId);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM user_favorites WHERE user_id = :userId", nativeQuery = true)
    void removeUserFromFavorites(@Param("userId") Long userId);
}
