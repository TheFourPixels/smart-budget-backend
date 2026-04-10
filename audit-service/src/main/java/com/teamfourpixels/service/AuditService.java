package com.teamfourpixels.service;

import com.teamfourpixels.dto.AuditEventDto;
import com.teamfourpixels.dto.BudgetLimitEvent;
import com.teamfourpixels.entity.AuditLog;
import com.teamfourpixels.mapper.AuditMapper;
import com.teamfourpixels.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditMapper auditMapper;

    @Transactional
    public void saveAuditEvent(AuditEventDto eventDto) {
        AuditLog auditLog = auditMapper.toEntity(eventDto);
        auditLogRepository.save(auditLog);
        log.info("Событие аудита {} успешно сохранено.", eventDto.getEventId());
    }

    @Transactional
    public void saveLimitEvent(BudgetLimitEvent eventDto) {
        AuditLog auditLog = auditMapper.toEntity(eventDto);
        auditLogRepository.save(auditLog);
        log.info("Событие лимита для пользователя {} сохранено.", eventDto.getUserId());
    }
}