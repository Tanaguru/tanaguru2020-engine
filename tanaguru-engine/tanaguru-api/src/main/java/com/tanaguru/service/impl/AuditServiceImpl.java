package com.tanaguru.service.impl;

import com.tanaguru.domain.constant.EAuditLogLevel;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditLog;
import com.tanaguru.domain.entity.audit.AuditReference;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.membership.Act;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.repository.ActRepository;
import com.tanaguru.repository.AuditLogRepository;
import com.tanaguru.repository.AuditReferenceRepository;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.service.AuditService;
import com.tanaguru.service.PageService;
import com.tanaguru.service.TestHierarchyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author rcharre
 */
@Service
@Transactional
public class AuditServiceImpl implements AuditService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditRepository auditRepository;
    private final AuditLogRepository auditLogRepository;
    private final ActRepository actRepository;
    private final PageService pageService;
    private final AuditReferenceRepository auditReferenceRepository;
    private final TestHierarchyService testHierarchyService;

    @Autowired
    public AuditServiceImpl(
            AuditRepository auditRepository, AuditLogRepository auditLogRepository, ActRepository actRepository, PageService pageService, AuditReferenceRepository auditReferenceRepository, TestHierarchyService testHierarchyService) {
        this.auditRepository = auditRepository;
        this.auditLogRepository = auditLogRepository;
        this.actRepository = actRepository;
        this.pageService = pageService;
        this.auditReferenceRepository = auditReferenceRepository;
        this.testHierarchyService = testHierarchyService;
    }

    public Collection<Audit> findAllByProject(Project project) {
        return actRepository.findAllByProject(project).stream()
                .map((Act::getAudit))
                .collect(Collectors.toList());
    }

    public void deleteAuditByProject(Project project) {
        LOGGER.debug("[Project {}] Delete all audits", project.getId());
        for (Act act : project.getActs()) {
            deleteAudit(act.getAudit());
        }
    }

    public void log(Audit audit, EAuditLogLevel level, String message) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAudit(audit);
        auditLog.setLevel(level);
        auditLog.setMessage(message);
        auditLogRepository.save(auditLog);
    }

    public boolean canShowAudit(Audit audit, String shareCode){
        return !audit.isPrivate() || (shareCode != null && !shareCode.isEmpty() && audit.getShareCode().equals(shareCode));
    }

    public void deleteAudit(Audit audit){
        actRepository.findByAudit(audit).ifPresent(actRepository::delete);
        pageService.deletePageByAudit(audit);

        Collection<TestHierarchy> auditReferences = audit.getAuditReferences()
                .stream().map(AuditReference::getTestHierarchy).collect(Collectors.toList());
        auditRepository.deleteById(audit.getId());
        for(TestHierarchy reference : auditReferences){
            if(reference.isDeleted() && !auditReferenceRepository.existsByTestHierarchy(reference)){
                testHierarchyService.deleteReference(reference);
            }
        }
    }
}
