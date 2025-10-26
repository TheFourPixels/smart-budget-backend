package com.teamfourpixels.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем CSRF (обязательно для REST API)
                .csrf(AbstractHttpConfigurer::disable)
                // Отключаем CORS (можно оставить, если настроен)
                .cors(AbstractHttpConfigurer::disable)
                // Главный шаг: Разрешить ВСЕ запросы
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll() // <-- Все пути разрешены без аутентификации
                );

        // Удалили: .addFilterBefore(new JwtAuthenticationFilter(...), ...)

        return http.build();
    }
}