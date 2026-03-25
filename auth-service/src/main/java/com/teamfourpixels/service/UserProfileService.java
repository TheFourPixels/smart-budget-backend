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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Client s3Client;

    @Value("${s3.bucket}")
    private String bucketName;

    @Value("${s3.public-url-prefix}")
    private String publicUrlPrefix;

    private static final int TARGET_IMAGE_SIZE = 300;

    public UserProfileDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        return new UserProfileDto(user.getId(), user.getEmail(), user.getName(), user.getAvatarUrl(), user.isPushEnabled(), user.isEmailEnabled());
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
        validateFile(file);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        deleteOldAvatarIfExists(user.getAvatarUrl());

        String fileName = processAndUploadImageToS3(file);

        String fileUrl = publicUrlPrefix + "/" + bucketName + "/" + fileName;

        user.setAvatarUrl(fileUrl);
        userRepository.save(user);

        log.info("Загружен новый аватар для пользователя {} в S3: {}", userId, fileName);
        return getProfile(userId);
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

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл не может быть пустым");
        }
    }

    private void deleteOldAvatarIfExists(String currentAvatarUrl) {
        if (currentAvatarUrl != null && currentAvatarUrl.contains(bucketName)) {
            try {
                String oldFileName = currentAvatarUrl.substring(currentAvatarUrl.lastIndexOf("/") + 1);

                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(oldFileName)
                        .build());

                log.info("Удален старый файл аватара из S3: {}", oldFileName);
            } catch (S3Exception e) {
                log.error("Ошибка при удалении старого аватара из S3: {}", e.awsErrorDetails().errorMessage());
            }
        }
    }

    private String processAndUploadImageToS3(MultipartFile file) {
        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new IllegalArgumentException("Неподдерживаемый формат изображения");
            }

            BufferedImage resizedImage = resizeImage(originalImage);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", os);
            byte[] imageBytes = os.toByteArray();

            String fileName = UUID.randomUUID() + ".jpg";

            ensureBucketExistsAndIsPublic();

            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .contentType("image/jpeg")
                            .build(),
                    RequestBody.fromBytes(imageBytes));

            return fileName;

        } catch (Exception e) {
            log.error("Критическая ошибка при загрузке аватара в S3", e);
            throw new RuntimeException("Не удалось сохранить изображение в S3", e);
        }
    }

    private void ensureBucketExistsAndIsPublic() {
        boolean bucketExists = false;
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            bucketExists = true;
        } catch (NoSuchBucketException e) {
            bucketExists = false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                bucketExists = false;
            } else {
                throw e;
            }
        }

        if (!bucketExists) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());

            String policy = """
                    {
                      "Statement": [
                        {
                          "Action": "s3:GetObject",
                          "Effect": "Allow",
                          "Principal": "*",
                          "Resource": "arn:aws:s3:::%s/*"
                        }
                      ],
                      "Version": "2012-10-17"
                    }
                    """.formatted(bucketName);

            s3Client.putBucketPolicy(PutBucketPolicyRequest.builder()
                    .bucket(bucketName)
                    .policy(policy)
                    .build());

            log.info("Создан бакет {} и установлена публичная политика чтения", bucketName);
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage) {
        BufferedImage resizedImage = new BufferedImage(TARGET_IMAGE_SIZE, TARGET_IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, TARGET_IMAGE_SIZE, TARGET_IMAGE_SIZE, null);
        g.dispose();

        return resizedImage;
    }
}