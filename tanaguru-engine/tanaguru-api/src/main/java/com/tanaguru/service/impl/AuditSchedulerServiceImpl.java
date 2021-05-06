package com.tanaguru.service.impl;

import com.tanaguru.domain.constant.AppAuthorityName;
import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.constant.ProjectAuthorityName;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditScheduler;
import com.tanaguru.domain.entity.membership.Act;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.domain.exception.CustomInvalidEntityException;
import com.tanaguru.repository.ActRepository;
import com.tanaguru.repository.AuditSchedulerRepository;
import com.tanaguru.service.AppRoleService;
import com.tanaguru.service.AuditSchedulerService;
import com.tanaguru.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class AuditSchedulerServiceImpl implements AuditSchedulerService {
    private final Logger LOGGER = LoggerFactory.getLogger(AuditSchedulerServiceImpl.class);
    public final int MIN_TIMER = 86400;

    private final AuditSchedulerRepository auditSchedulerRepository;
    private final ActRepository actRepository;
    private final ProjectService projectService;
    private final AppRoleService appRoleService;

    private final Map<Long, AuditScheduler> schedulersMap = new HashMap<>();

    public AuditSchedulerServiceImpl(
            AuditSchedulerRepository auditSchedulerRepository,
            ActRepository actRepository,
            ProjectService projectService,
            AppRoleService appRoleService) {

        this.auditSchedulerRepository = auditSchedulerRepository;
        this.actRepository = actRepository;
        this.projectService = projectService;
        this.appRoleService = appRoleService;
    }

    @PostConstruct
    public void setup(){
        synchronized (schedulersMap){
            for (AuditScheduler auditScheduler : auditSchedulerRepository.findAll()) {
                schedulersMap.put(auditScheduler.getId(), auditScheduler);
            }
        }
    }

    public AuditScheduler createAuditScheduler(Audit audit, int timer){
        if(timer < MIN_TIMER){
            throw new CustomInvalidEntityException(CustomError.TIMER_VALUE_TOO_SHORT, String.valueOf(timer) );
        }

        Optional<AuditScheduler> auditSchedulerOpt = auditSchedulerRepository.findByAudit(audit);
        if(auditSchedulerOpt.isPresent()){
            throw new CustomInvalidEntityException(CustomError.SCHEDULER_ALREADY_EXISTS_FOR_AUDIT, String.valueOf(audit.getId()) );
        }

        AuditScheduler auditScheduler = new AuditScheduler();
        auditScheduler.setAudit(audit);
        auditScheduler.setScheduler(timer);
        synchronized (schedulersMap) {
            schedulersMap.put(auditScheduler.getId(), auditScheduler);
        }
        return auditSchedulerRepository.save(auditScheduler);
    }

    public AuditScheduler modifyAuditScheduler(AuditScheduler auditScheduler, int timer, Date lastExecution){
        if(auditScheduler.getScheduler() < MIN_TIMER){
            throw new CustomInvalidEntityException(CustomError.TIMER_VALUE_TOO_SHORT, String.valueOf(auditScheduler.getScheduler()) );
        }

        auditScheduler.setScheduler(timer);
        auditScheduler.setLastExecution(lastExecution);

        synchronized (schedulersMap) {
            schedulersMap.put(auditScheduler.getId(), auditScheduler);
        }

        return auditSchedulerRepository.save(auditScheduler);
    }

    public void deleteAuditScheduler(AuditScheduler auditScheduler){
        synchronized (schedulersMap){
            schedulersMap.remove(auditScheduler.getId());
        }
        auditSchedulerRepository.delete(auditScheduler);
    }

    public Map<Long, AuditScheduler> getSchedulersMap() {
        return schedulersMap;
    }

    public boolean userCanScheduleOnAudit(User user, Audit audit){
        Optional<Act> actOpt = actRepository.findByAudit(audit);
        return actOpt.map(act -> projectService.hasAuthority(user, ProjectAuthorityName.START_AUDIT, act.getProject(), true))
                .orElseGet(() -> ! audit.isPrivate() && appRoleService.getAppAuthorityByAppRole(user.getAppRole().getName())
                    .contains(AppAuthorityName.PUBLIC_SCHEDULE_ACCESS));
    }
}
