package com.teamfourpixels.repository;

import com.teamfourpixels.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findAllByUserId(Long userId);

    boolean existsByUserIdAndToken(Long userId, String token);

    @Transactional
    void deleteByToken(String token);
}