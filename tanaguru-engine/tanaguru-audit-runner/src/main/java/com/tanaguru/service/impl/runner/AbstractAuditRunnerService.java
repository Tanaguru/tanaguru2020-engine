package com.tanaguru.service.impl.runner;

import com.tanaguru.domain.constant.EAuditLogLevel;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.PageContent;
import com.tanaguru.repository.*;
import com.tanaguru.runner.AuditRunner;
import com.tanaguru.runner.listener.AuditRunnerListener;
import com.tanaguru.service.AuditRunnerService;
import com.tanaguru.service.AuditService;
import com.tanaguru.service.ResultAnalyzerService;
import com.tanaguru.webextresult.WebextPageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.tanaguru.domain.constant.EAuditStatus.DONE;
import static com.tanaguru.domain.constant.EAuditStatus.RUNNING;

/**
 * @author rcharre
 */
public abstract class AbstractAuditRunnerService implements AuditRunnerListener, AuditRunnerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAuditRunnerService.class);

    protected final PageRepository pageRepository;
    protected final AuditRepository auditRepository;
    protected final AuditService auditService;
    protected final PageContentRepository pageContentRepository;
    protected final TestResultRepository testResultRepository;
    protected final TestHierarchyResultRepository testHierarchyResultRepository;
    protected final ResultAnalyzerService resultAnalyzerService;
    protected final TestHierarchyRepository testHierarchyRepository;
    protected final ElementResultRepository elementResultRepository;

    public AbstractAuditRunnerService(
            PageRepository pageRepository,
            AuditRepository auditRepository,
            AuditService auditService,
            PageContentRepository pageContentRepository,
            TestResultRepository testResultRepository,
            TestHierarchyResultRepository testHierarchyResultRepository,
            ResultAnalyzerService resultAnalyzerService,
            TestHierarchyRepository testHierarchyRepository,
            ElementResultRepository elementResultRepository) {
        this.pageRepository = pageRepository;
        this.auditRepository = auditRepository;
        this.auditService = auditService;
        this.pageContentRepository = pageContentRepository;
        this.testResultRepository = testResultRepository;
        this.testHierarchyResultRepository = testHierarchyResultRepository;
        this.resultAnalyzerService = resultAnalyzerService;
        this.testHierarchyRepository = testHierarchyRepository;
        this.elementResultRepository = elementResultRepository;
    }

    @Override
    public final void onAuditNewPage(AuditRunner auditRunner, String name, String url, int rank, WebextPageResult result, String screenshot, String source) {
        LOGGER.debug("[Audit {}] Persist new page {}", auditRunner.getAudit().getId(), url);
        Audit audit = auditRunner.getAudit();
        Page page = new Page();
        page.setAudit(audit);
        page.setRank(rank);
        page.setName(name);
        page.setUrl(url);
        page = pageRepository.save(page);

        PageContent pageContent = new PageContent();
        pageContent.setPage(page);
        pageContent.setScreenshot(screenshot);
        pageContent.setSource(source);
        pageContentRepository.save(pageContent);

        LOGGER.info("[Audit {}] Persisting result for page {}", auditRunner.getAudit().getId(), url);
        resultAnalyzerService.extractWebextPageResult(result, audit, page);

        auditService.log(auditRunner.getAudit(), EAuditLogLevel.INFO, "New page audited " + name + " for url " + url);
        onAuditNewPageImpl(auditRunner, page);
    }

    @Override
    public final void onAuditStart(AuditRunner auditRunner) {
        Audit audit = auditRunner.getAudit();
        audit.setDateStart(new Date());
        audit.setStatus(RUNNING);
        audit = auditRepository.save(audit);
        auditRunner.setAudit(audit);
        auditService.log(auditRunner.getAudit(), EAuditLogLevel.INFO, "Audit start");
        onAuditStartImpl(auditRunner);
    }

    @Override
    public final void onAuditEnd(AuditRunner auditRunner) {
        Audit audit = auditRunner.getAudit();
        audit.setDateEnd(new Date());
        audit.setStatus(DONE);
        audit = auditRepository.save(audit);
        auditRunner.setAudit(audit);
        onAuditEndImpl(auditRunner);
        auditService.log(auditRunner.getAudit(), EAuditLogLevel.INFO, "Audit end");
    }

    @Override
    public void onAuditLog(AuditRunner auditRunner, EAuditLogLevel logLevel, String message) {
        auditService.log(auditRunner.getAudit(), logLevel, message);
    }


    /**
     * onAuditStart Impl
     *
     * @param auditRunner The auditRunner that started
     */
    public abstract void onAuditStartImpl(AuditRunner auditRunner);

    /**
     * onAuditNewPage Impl
     *
     * @param auditRunner The auditRunner that get a new page
     */
    public abstract void onAuditNewPageImpl(AuditRunner auditRunner, Page page);

    /**
     * onAuditEnd Impl
     *
     * @param auditRunner The auditRunner that ended
     */
    public abstract void onAuditEndImpl(AuditRunner auditRunner);
}
