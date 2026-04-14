package com.teamfourpixels.service;

import com.teamfourpixels.dto.AnalyticsEventDto;
import com.teamfourpixels.dto.AuthRequest;
import com.teamfourpixels.dto.AuthResponse;
import com.teamfourpixels.dto.RegisterRequest;
import com.teamfourpixels.entity.User;
import com.teamfourpixels.repository.UserRepository;
import com.teamfourpixels.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private final KafkaTemplate<String, Object> analyticsKafkaTemplate;

    private static final String ANALYTICS_TOPIC = "analytics-events";

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

        sendAnalyticsEvent(savedUser.getId(), "USER_REGISTERED", "{\"email\":\"" + savedUser.getEmail() + "\"}");

        sendAnalyticsEvent(savedUser.getId(), "USER_LOGGED_IN", "{\"source\":\"registration\"}");

        String token = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getEmail());
        return new AuthResponse(token, savedUser.getId(), savedUser.getName());
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        sendAnalyticsEvent(user.getId(), "USER_LOGGED_IN", "{\"source\":\"manual_login\"}");

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName());
    }

    public boolean isEmailRegistered(String email) {
        return userRepository.existsByEmail(email);
    }

    private void sendAnalyticsEvent(Long userId, String eventType, String payload) {
        try {
            AnalyticsEventDto event = AnalyticsEventDto.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(eventType)
                    .userId(userId)
                    .timestamp(LocalDateTime.now())
                    .platform("UNKNOWN")
                    .payload(payload)
                    .build();

            analyticsKafkaTemplate.send(ANALYTICS_TOPIC, userId.toString(), event);
            log.info("Аналитическое событие {} успешно отправлено для пользователя {}", eventType, userId);

        } catch (Exception e) {
            log.error("Ошибка при отправке аналитики в Kafka: {}", e.getMessage());
        }
    }
}