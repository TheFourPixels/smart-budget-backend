package com.teamfourpixels.service;

import com.teamfourpixels.dto.AuthRequest;
import com.teamfourpixels.dto.AuthResponse;
import com.teamfourpixels.dto.RegisterRequest;
import com.teamfourpixels.entity.User;
import com.teamfourpixels.repository.UserRepository;
import com.teamfourpixels.security.JwtTokenProvider;
import com.teamfourpixels.dto.*;
import com.teamfourpixels.entity.PasswordResetToken;
import com.teamfourpixels.repository.PasswordResetTokenRepository;
import java.time.LocalDateTime;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AnalyticsService analyticsService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final FakeMailService fakeMailService;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    private static final String TEST_RESET_CODE = "123456";

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(user);

        analyticsService.sendEvent(savedUser.getId(), "USER_REGISTERED", 
                UserAnalyticsPayload.builder().email(savedUser.getEmail()).build());
        analyticsService.sendEvent(savedUser.getId(), "USER_LOGGED_IN", 
                UserAnalyticsPayload.builder().source("registration").build());

        String token = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getEmail());
        return new AuthResponse(token, savedUser.getId(), savedUser.getName());
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        analyticsService.sendEvent(user.getId(), "USER_LOGGED_IN", 
                UserAnalyticsPayload.builder().source("manual_login").build());

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName());
    }

    public boolean isEmailRegistered(String email) {
        return userRepository.existsByEmail(email);
    }


    @Transactional
    public void processForgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с таким email не найден"));

        String code = String.format("%06d", new Random().nextInt(1000000));
        
        PasswordResetToken token = passwordResetTokenRepository.findByUserEmail(user.getEmail())
                .orElse(PasswordResetToken.builder().user(user).build());
        
        token.setToken(code);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        passwordResetTokenRepository.save(token);

        fakeMailService.sendResetCode(user.getEmail(), code);
        log.info("Код восстановления пароля отправлен на email: {}", user.getEmail());
    }

    public void verifyResetCode(VerifyCodeRequest request) {
        if (isTestMode() && TEST_RESET_CODE.equals(request.getCode())) {
            log.info("Использован тестовый код восстановления для email: {}", request.getEmail());
            return;
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getCode())
                .filter(t -> t.getUser().getEmail().equals(request.getEmail()))
                .orElseThrow(() -> new IllegalArgumentException("Неверный код восстановления или email"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Код восстановления просрочен");
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user;
        if (isTestMode() && TEST_RESET_CODE.equals(request.getCode())) {
            user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
            log.info("Сброс пароля через тестовый код для email: {}", request.getEmail());
        } else {
            PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getCode())
                    .filter(t -> t.getUser().getEmail().equals(request.getEmail()))
                    .orElseThrow(() -> new IllegalArgumentException("Неверный код восстановления или email"));

            if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Код восстановления просрочен");
            }
            user = resetToken.getUser();
            passwordResetTokenRepository.delete(resetToken);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Пароль успешно изменен для пользователя: {}", user.getEmail());
    }

    private boolean isTestMode() {
        return activeProfile != null && activeProfile.contains("docker");
    }
}
