package com.example.music.controller;

import com.example.music.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    public ResponseEntity<Map<String, String>> getCaptcha() {
        try {
            CaptchaService.CaptchaImage captcha = captchaService.generateCaptcha();
            String base64 = Base64.getEncoder().encodeToString(captcha.imageBytes());
            return ResponseEntity.ok(Map.of(
                    "captchaId", captcha.captchaId(),
                    "image", "data:image/png;base64," + base64
            ));
        } catch (Exception e) {
            log.error("Failed to generate captcha", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "验证码生成失败"));
        }
    }
}
