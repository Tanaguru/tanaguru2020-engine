package com.tanaguru.service.impl;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.service.AsyncAuditService;
import com.tanaguru.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class AsyncAuditServiceImpl implements AsyncAuditService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncAuditServiceImpl.class);

    private final AuditService auditService;
    private final AuditRepository auditRepository;

    private final Set<Audit> deletionSet = Collections.synchronizedSet( new HashSet<>());

    public AsyncAuditServiceImpl(AuditService auditService, AuditRepository auditRepository) {
        this.auditService = auditService;
        this.auditRepository = auditRepository;
    }

    @PostConstruct
    private void startDeletedAuditCleanup(){
        LOGGER.info("Resume audit deletion");
        auditRepository.findAllByDeletedIsTrue()
                .forEach(this::deleteAudit);
    }

    @Async("threadPoolTaskExecutor")
    @Override
    public void deleteAudit(Audit audit) {
        if(!deletionSet.contains(audit)){
            deletionSet.add(audit);
            auditService.deleteAudit(audit);
        }
    }
}
