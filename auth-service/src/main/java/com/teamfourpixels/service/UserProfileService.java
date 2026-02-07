package com.teamfourpixels.service;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.entity.*;
import com.teamfourpixels.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${server.base-url:http://localhost:8089}")
    private String baseUrl;

    private static final int TARGET_IMAGE_SIZE = 300;

    public UserProfileDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        return new UserProfileDto(user.getId(), user.getEmail(), user.getName(), user.getAvatarUrl());
    }

    @Transactional
    public UserProfileDto updateProfile(Long userId, UpdateProfileRequest req) {
        User user = userRepository.findById(userId).orElseThrow();
        if (req.getName() != null) user.setName(req.getName());
        if (req.getAvatarUrl() != null) user.setAvatarUrl(req.getAvatarUrl());
        return getProfile(userId);
    }

    @Transactional
    public UserProfileDto uploadAvatar(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл не может быть пустым");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String currentAvatarUrl = user.getAvatarUrl();
            if (currentAvatarUrl != null && currentAvatarUrl.startsWith(baseUrl)) {
                try {
                    String oldFileName = currentAvatarUrl.substring(currentAvatarUrl.lastIndexOf("/") + 1);
                    Path oldFilePath = uploadPath.resolve(oldFileName);
                    if (Files.deleteIfExists(oldFilePath)) {
                        log.info("Удален старый файл аватара: {}", oldFileName);
                    }
                } catch (Exception e) {
                    log.error("Ошибка при удалении старого аватара: {}", e.getMessage());
                }
            }

            String fileName = UUID.randomUUID() + ".jpg";
            Path filePath = uploadPath.resolve(fileName);

            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new IllegalArgumentException("Неподдерживаемый формат изображения");
            }

            BufferedImage resizedImage = new BufferedImage(TARGET_IMAGE_SIZE, TARGET_IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();

            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(originalImage, 0, 0, TARGET_IMAGE_SIZE, TARGET_IMAGE_SIZE, null);
            g.dispose();

            ImageIO.write(resizedImage, "jpg", filePath.toFile());

            String fileUrl = baseUrl + "/uploads/" + fileName;
            user.setAvatarUrl(fileUrl);
            userRepository.save(user);

            log.info("Загружен новый аватар для пользователя {}: {}", userId, fileName);
            return getProfile(userId);

        } catch (IOException e) {
            log.error("Критическая ошибка при загрузке аватара", e);
            throw new RuntimeException("Не удалось сохранить изображение", e);
        }
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken myToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(60))
                .build();
        tokenRepository.save(myToken);

        log.info("Инструкции по сбросу пароля отправлены на {}", email);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken passToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Неверный токен"));

        if (passToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Срок действия токена истек");
        }

        User user = passToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(passToken);
    }
}