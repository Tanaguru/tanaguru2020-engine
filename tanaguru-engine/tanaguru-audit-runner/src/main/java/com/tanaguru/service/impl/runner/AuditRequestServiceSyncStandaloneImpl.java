package com.tanaguru.service.impl.runner;

import com.tanaguru.domain.constant.EAuditLogLevel;
import com.tanaguru.domain.constant.EAuditStatus;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.repository.*;
import com.tanaguru.runner.AuditRunner;
import com.tanaguru.runner.factory.AuditRunnerFactory;
import com.tanaguru.service.AuditRunnerService;
import com.tanaguru.service.AuditService;
import com.tanaguru.service.MailService;
import com.tanaguru.service.ResultAnalyzerService;
import com.tanaguru.service.impl.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
@ConditionalOnProperty(
        name = "auditrunner.profile",
        havingValue = "sync",
        matchIfMissing = true
)
@Primary
public class AuditRequestServiceSyncStandaloneImpl extends AbstractAuditRunnerService implements AuditRunnerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditRequestServiceSyncStandaloneImpl.class);

    private final AuditRunnerFactory auditRunnerFactory;

    private AuditRunner currentRunner = null;

    @Autowired
    public AuditRequestServiceSyncStandaloneImpl(
            PageRepository pageRepository,
            AuditRepository auditRepository,
            AuditService auditService,
            AuditRunnerFactory auditRunnerFactory,
            PageContentRepository pageContentRepository,
            TestResultRepository testResultRepository,
            TestHierarchyResultRepository testHierarchyResultRepository,
            ResultAnalyzerService resultAnalyzerService,
            TestHierarchyRepository testHierarchyRepository,
            ElementResultRepository elementResultRepository,
            MailService mailService,
            MessageService messageService,
            ActRepository actRepository,
            ContractUserRepository contractUserRepository) {

        super(pageRepository,
                auditRepository,
                auditService,
                pageContentRepository,
                testResultRepository,
                testHierarchyResultRepository,
                resultAnalyzerService,
                testHierarchyRepository,
                elementResultRepository,
                mailService,
                messageService,
                actRepository,
                contractUserRepository);
        this.auditRunnerFactory = auditRunnerFactory;
    }

    public void runAudit(Audit audit) {
        Optional<AuditRunner> auditRunnerOptional = auditRunnerFactory.create(audit);
        if (auditRunnerOptional.isPresent()) {
            AuditRunner auditRunner = auditRunnerOptional.get();
            auditRunner.addListener(this);
            this.currentRunner = auditRunner;
            auditRunner.run();
        }else{
            audit.setStatus(EAuditStatus.ERROR);
            audit = auditRepository.save(audit);
            LOGGER.error("[Audit {}] Unable to start audit", audit.getId());
        }
    }

    @Override
    public void onAuditStartImpl(AuditRunner auditRunner) {
    }

    @Override
    public void onAuditNewPageImpl(AuditRunner auditRunner, Page page) {
    }

    @Override
    public void onAuditEndImpl(AuditRunner auditRunner) {
        this.currentRunner = null;
    }

    @PreDestroy
    private void cleanRunningAudit() {
        if (currentRunner != null) {
            LOGGER.warn("[Audit {}] Interrupting audit", currentRunner.getAudit().getId());
            auditService.log(currentRunner.getAudit(), EAuditLogLevel.ERROR, "Audit interrupted by server");
        }
    }
}
