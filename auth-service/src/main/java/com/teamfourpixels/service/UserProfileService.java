package com.teamfourpixels.service;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.entity.*;
import com.teamfourpixels.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
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
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken myToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(60))
                .build();
        tokenRepository.save(myToken);

        System.out.println(">>> EMAIL SENT TO " + email + ": Reset link: http://localhost:8080/reset?token=" + token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken passToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (passToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expired");
        }

        User user = passToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(passToken);
    }
}