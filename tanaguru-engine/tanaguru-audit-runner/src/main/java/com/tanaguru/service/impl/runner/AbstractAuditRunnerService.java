package com.tanaguru.service.impl.runner;

import com.tanaguru.domain.constant.EAuditLogLevel;
import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.PageContent;
import com.tanaguru.domain.entity.membership.project.ProjectAppUser;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.repository.*;
import com.tanaguru.runner.AuditRunner;
import com.tanaguru.runner.listener.AuditRunnerListener;
import com.tanaguru.service.AuditRunnerService;
import com.tanaguru.service.AuditService;
import com.tanaguru.service.MailService;
import com.tanaguru.service.ResultAnalyzerService;
import com.tanaguru.service.impl.MessageService;
import com.tanaguru.webextresult.WebextPageResult;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;

import java.util.Collection;
import java.util.Date;

import static com.tanaguru.domain.constant.EAuditStatus.*;

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
    protected final MailService mailService;
    protected final MessageService messageService;
    protected final ActRepository actRepository;
    protected final ContractUserRepository contractUserRepository;
    protected final ProjectUserRepository projectUserRepository;


    @Value("${webapp.url}")
    private String webappUrl;

    public AbstractAuditRunnerService(
            PageRepository pageRepository,
            AuditRepository auditRepository,
            AuditService auditService,
            PageContentRepository pageContentRepository,
            TestResultRepository testResultRepository,
            TestHierarchyResultRepository testHierarchyResultRepository,
            ResultAnalyzerService resultAnalyzerService,
            TestHierarchyRepository testHierarchyRepository,
            ElementResultRepository elementResultRepository,
            MailService mailService,
            MessageService messageService,
            ActRepository actRepository,
            ContractUserRepository contractUserRepository,
            ProjectUserRepository projectUserRepository) {
        this.pageRepository = pageRepository;
        this.auditRepository = auditRepository;
        this.auditService = auditService;
        this.pageContentRepository = pageContentRepository;
        this.testResultRepository = testResultRepository;
        this.testHierarchyResultRepository = testHierarchyResultRepository;
        this.resultAnalyzerService = resultAnalyzerService;
        this.testHierarchyRepository = testHierarchyRepository;
        this.elementResultRepository = elementResultRepository;
        this.mailService = mailService;
        this.messageService = messageService;
        this.actRepository = actRepository;
        this.contractUserRepository = contractUserRepository;
        this.projectUserRepository = projectUserRepository;
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
        auditRepository.save(audit);
        auditService.log(auditRunner.getAudit(), EAuditLogLevel.INFO, "Audit start");
        onAuditStartImpl(auditRunner);
    }

    @Override
    public final void onAuditEnd(AuditRunner auditRunner) {
        Audit audit = auditRunner.getAudit();
        Collection<Page> pages = pageRepository.findAllByAudit_Id(audit.getId());
        if (pages.isEmpty()) {
            audit.setStatus(ERROR);
            auditService.log(auditRunner.getAudit(), EAuditLogLevel.ERROR, "Audit failed because it does not contain pages");
        } else {
            audit.setStatus(DONE);
        }
        audit.setDateEnd(new Date());
        audit = auditRepository.save(audit);
        onAuditEndImpl(auditRunner);
        auditService.log(auditRunner.getAudit(), EAuditLogLevel.INFO, "Audit end");

        if (audit.getType().equals(EAuditType.SITE) || audit.getType().equals(EAuditType.SCENARIO) || pages.size() >= 2) {
            Audit finalAudit = audit;
            actRepository.findByAudit(audit).ifPresent(act1 -> {
                Collection<ProjectAppUser> projectAppUsers = projectUserRepository.findAllByProject(act1.getProject());
                String domain = act1.getProject().getDomain();
                String url = webappUrl + "audits/" + finalAudit.getId();
                for (ProjectAppUser projectAppUser : projectAppUsers) {
                    User user = projectAppUser.getContractAppUser().getUser();
                    try {
                        boolean emailSent = mailService.sendMimeMessage(user.getEmail(), messageService.getMessage("mail.auditEnd.subject"), messageService.getMessage("mail.auditEnd.body").replace("domain", domain).replaceAll("url", url));
                        if (emailSent) {
                            auditService.log(auditRunner.getAudit(), EAuditLogLevel.INFO, "E-mail notifying the end of the audit sent to " + user.getEmail());
                        } else {
                            auditService.log(auditRunner.getAudit(), EAuditLogLevel.ERROR, "Failed to send email at the end of the audit to " + user.getEmail());
                        }
                    } catch (MailException e) {
                        LOGGER.error("[Audit {}] Failed to send email at the end of the audit to {}", finalAudit.getId(), user.getEmail());
                        auditService.log(auditRunner.getAudit(), EAuditLogLevel.ERROR, "Failed to send email at the end of the audit to " + user.getEmail());
                    }
                }
            });
        }
    }

    @Override
    public void onAuditLog(AuditRunner auditRunner, EAuditLogLevel logLevel, String message) {
        auditService.log(auditRunner.getAudit(), logLevel, message);
    }

    @Override
    public void stopAudit(Audit audit){
        throw new NotImplementedException("Function not implemented yet");
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
