package com.example.music.service;

import com.example.music.dto.AuthDTO;
import com.example.music.entity.User;
import com.example.music.repository.UserRepository;
import com.example.music.security.JwtTokenProvider;
import com.example.music.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final CaptchaService captchaService;

    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {
        if (!captchaService.verifyCaptcha(request.getCaptchaId(), request.getCaptchaCode())) {
            throw new RuntimeException("验证码错误");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        return AuthDTO.AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .userId(userPrincipal.getId())
                .username(userPrincipal.getUsername())
                .nickname(userPrincipal.getNickname())
                .avatar(userPrincipal.getAvatar())
                .email(userPrincipal.getEmail())
                .build();
    }

    @Transactional
    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        if (request.getNickname() != null && request.getNickname().length() > 5) {
            throw new RuntimeException("昵称不能超过5个字");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .build();

        user = userRepository.save(user);

        String jwt = tokenProvider.generateTokenFromUserId(user.getId());

        return AuthDTO.AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .build();
    }
}
