package com.teamfourpixels.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FakeMailService {
    public void sendResetCode(String email, String code) {
        log.info("Отправка кода сброса {} на email {}", code, email);
    }
}
