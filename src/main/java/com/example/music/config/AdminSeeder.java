package com.example.music.config;

import com.example.music.entity.Role;
import com.example.music.entity.User;
import com.example.music.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default admin account on first startup if no admin exists.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        long adminCount = userRepository.countByRole(Role.ADMIN);
        if (adminCount == 0) {
            log.info("No admin user found. Creating default admin account...");

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@music.local")
                    .nickname("管理员")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();

            userRepository.save(admin);
            log.info("Default admin created: username=admin, password=admin123 (please change it!)");
        } else {
            log.info("{} admin user(s) already exist, skipping seed.", adminCount);
        }
    }
}
