package com.tanaguru.service.impl;

import com.tanaguru.domain.constant.EAuditLogLevel;
import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.entity.audit.*;
import com.tanaguru.domain.entity.membership.Act;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.repository.*;
import com.tanaguru.service.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * @author rcharre
 */
@Service
@Transactional
public class AuditServiceImpl implements AuditService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final ActRepository actRepository;
    private final AuditActService auditActService;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;
    private final AuditParameterService auditParameterService;
    private final AuditReferenceRepository auditReferenceRepository;
    private final AuditRepository auditRepository;
    private final PageService pageService;
    private final TestHierarchyService testHierarchyService;
    private final AuditAuditParameterValueRepository auditAuditParameterValueRepository;
    private final TestHierarchyRepository testHierarchyRepository;
    
    @Autowired
    public AuditServiceImpl(
            ActRepository actRepository,
            AuditActService auditActService,
            AuditLogRepository auditLogRepository,
            AuditLogService auditLogService,
            AuditParameterService auditParameterServiceImpl,
            AuditReferenceRepository auditReferenceRepository,
            AuditRepository auditRepository,
            PageService pageService,
            TestHierarchyResultService testHierarchyResultService,
            TestHierarchyService testHierarchyService, AuditAuditParameterValueRepository auditAuditParameterValueRepository, TestHierarchyRepository testHierarchyRepository) {
        this.actRepository = actRepository;
        this.auditActService = auditActService;
        this.auditLogRepository = auditLogRepository;
        this.auditLogService = auditLogService;
        this.auditParameterService = auditParameterServiceImpl;
        this.auditReferenceRepository = auditReferenceRepository;
        this.auditRepository = auditRepository;
        this.pageService = pageService;
        this.testHierarchyService = testHierarchyService;
        this.auditAuditParameterValueRepository = auditAuditParameterValueRepository;
        this.testHierarchyRepository = testHierarchyRepository;
    }
    
    @Autowired
    private Environment environment;

    public Collection<Audit> findAllByProject(Project project) {
        return actRepository.findAllByProject(project).stream()
                .map((Act::getAudit))
                .collect(Collectors.toList());
    }

    public org.springframework.data.domain.Page<Audit> findAllByProject(Project project, Pageable pageable) {
        Collection<Audit> audits = actRepository.findAllByProject(project).stream()
                .map((Act::getAudit))
                .collect(Collectors.toList());
        return new PageImpl<>(new ArrayList<>(audits), pageable, audits.size());

    }

    public void deleteAuditByProject(Project project) {
        LOGGER.info("[Project {}] Delete all audits", project.getId());
        for (Act act : project.getActs()) {
            deleteAudit(act.getAudit());
        }
    }

    public void log(Audit audit, EAuditLogLevel level, String message) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAudit(audit);
        auditLog.setLevel(level);
        auditLog.setDate(new Date());
        auditLog.setMessage(message);
        auditLogRepository.save(auditLog);
    }

    public boolean canShowAudit(Audit audit, String shareCode){
        return !audit.isPrivate() || (shareCode != null && !shareCode.isEmpty() && audit.getShareCode().equals(shareCode));
    }


    public void deleteAudit(Audit audit){
        audit = auditRepository.findById(audit.getId())
                .orElseThrow(CustomEntityNotFoundException::new);

        LOGGER.info("[Audit " + audit.getId() + "] delete act");
        actRepository.findByAudit(audit)
                .ifPresent(actRepository::delete);

        LOGGER.info("[Audit " + audit.getId() + "] delete content");
        pageService.deletePageByAudit(audit);

        LOGGER.info("[Audit " + audit.getId() + "] delete parameters");
        deleteAuditParameterByAudit(audit);

        Collection<TestHierarchy> auditReferences = audit.getAuditReferences().stream()
                .map(AuditReference::getTestHierarchy)
                .collect(Collectors.toList());

        LOGGER.info("[Audit {}] delete", audit.getId());
        auditRepository.delete(audit);
        auditReferences.stream()
                .filter(testHierarchy ->
                                testHierarchy.isDeleted() &&
                                !auditReferenceRepository.existsByTestHierarchy(testHierarchy) &&
                                testHierarchyRepository.findById(testHierarchy.getId()).isPresent()
                        )
                .forEach(testHierarchyService::deleteReference);
    }

    public void deleteAuditParameterByAudit(Audit audit){
        auditAuditParameterValueRepository.deleteAllByAudit(audit);
    }
    
    /**
     * Return a json object with the information of the audit
     * @param audit the relevant audit
     * @return json object
     */
    public JSONObject toJson(Audit audit) {
        LOGGER.info("[Audit {}] export to json", audit.getId());
        JSONObject jsonAuditObject = new JSONObject();
        jsonAuditObject.put("auditLogs", auditLogService.toJson(audit).get("auditLogs"));
        jsonAuditObject.put("act", auditActService.toJson(audit));
        jsonAuditObject.put("auditParametersValues", auditParameterService.toJson(audit));
        Collection<Page> pages = audit.getPages();
        for(Page page : pages) {
            jsonAuditObject.append("pages", pageService.toJson(page));
        }
        return jsonAuditObject;
    }
    
    /**
     * Get all audits incorrectly deleted
     * @return @see Audit list
     */
    public Collection <Audit> getAllAuditIncorrectlyDeleted() {
    	Collection <Audit> audits = auditRepository.findAllWithoutAct();
    	int total = auditRepository.numberTotalOfAudit();
    	
    	LOGGER.info("{} audits incorrectly deleted on {} audits", audits.size(), total);
    	
        return audits;
    }

    @Override
    public org.springframework.data.domain.Page<Audit> findAllByProjectAndType(Project project, EAuditType type,
            Pageable pageable) {
        return actRepository.findAllAuditByProjectAndAudit_Type(project, type, pageable);
    }

	@Override
	public String getTotalAuditsToBePurged() {
		return environment.getProperty("purge.total");
	}

	@Override
	public String getPurgeStatus() {
		return environment.getProperty("purge.status");
	}
}
