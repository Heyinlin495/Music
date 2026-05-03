package com.example.music.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final long EXPIRE_MS = 5 * 60 * 1000L;
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final ConcurrentHashMap<String, CaptchaEntry> store = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public record CaptchaImage(String captchaId, byte[] imageBytes) {}

    public CaptchaImage generateCaptcha() throws IOException {
        cleanupExpired();

        String captchaId = java.util.UUID.randomUUID().toString().replace("-", "");
        String code = generateCode();
        store.put(captchaId, new CaptchaEntry(code, System.currentTimeMillis()));

        byte[] imageBytes = renderImage(code);
        return new CaptchaImage(captchaId, imageBytes);
    }

    public boolean verifyCaptcha(String captchaId, String userInput) {
        if (captchaId == null || userInput == null) return false;
        CaptchaEntry entry = store.remove(captchaId);
        if (entry == null) return false;
        if (System.currentTimeMillis() - entry.createdAt > EXPIRE_MS) return false;
        return entry.code.equalsIgnoreCase(userInput.trim());
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private byte[] renderImage(String code) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // Background
        g.setColor(new Color(240 + random.nextInt(16), 240 + random.nextInt(16), 240 + random.nextInt(16)));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Noise lines
        for (int i = 0; i < 6; i++) {
            g.setColor(randomColor(160, 220));
            int x1 = random.nextInt(WIDTH), y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH), y2 = random.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // Characters
        int fontSize = 28;
        g.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        for (int i = 0; i < code.length(); i++) {
            g.setColor(randomColor(20, 120));
            g.translate(22 + i * 22, 30 + random.nextInt(6) - 3);
            g.rotate(Math.toRadians(random.nextInt(30) - 15));
            g.drawString(String.valueOf(code.charAt(i)), 0, 0);
            g.setTransform(new java.awt.geom.AffineTransform());
        }

        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    private Color randomColor(int min, int max) {
        int r = min + random.nextInt(max - min);
        int g = min + random.nextInt(max - min);
        int b = min + random.nextInt(max - min);
        return new Color(r, g, b);
    }

    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        store.entrySet().removeIf(e -> now - e.getValue().createdAt > EXPIRE_MS);
    }

    private record CaptchaEntry(String code, long createdAt) {}
}
