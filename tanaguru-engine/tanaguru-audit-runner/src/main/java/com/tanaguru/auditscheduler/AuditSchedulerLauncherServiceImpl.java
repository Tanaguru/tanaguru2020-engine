package com.tanaguru.auditscheduler;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditScheduler;
import com.tanaguru.domain.entity.membership.Act;
import com.tanaguru.factory.AuditFactory;
import com.tanaguru.repository.ActRepository;
import com.tanaguru.service.AuditRunnerService;
import com.tanaguru.service.AuditSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Service
@Transactional
@ConditionalOnProperty(
        name = "auditrunner.scheduler.enabled",
        havingValue = "true"
)
public class AuditSchedulerLauncherServiceImpl implements AuditSchedulerLauncherService{
    private final Logger LOGGER = LoggerFactory.getLogger(AuditSchedulerLauncherServiceImpl.class);

    private final AuditSchedulerService auditSchedulerService;
    private final ActRepository actRepository;
    private final AuditFactory auditFactory;
    private final AuditRunnerService auditRunnerService;

    public AuditSchedulerLauncherServiceImpl(
            AuditSchedulerService auditSchedulerService,
            ActRepository actRepository,
            AuditFactory auditFactory,
            AuditRunnerService auditRunnerService) {
        this.auditSchedulerService = auditSchedulerService;
        this.actRepository = actRepository;
        this.auditFactory = auditFactory;
        this.auditRunnerService = auditRunnerService;
    }


    @Scheduled(fixedDelay = 60000)
    public void checkSchedules(){
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        synchronized (auditSchedulerService.getSchedulersMap()){

            for(AuditScheduler auditScheduler : auditSchedulerService.getSchedulersMap().values()){
                if(auditScheduler.getLastExecution() != null) {
                    calendar.setTime(auditScheduler.getLastExecution());
                }
                calendar.add(Calendar.SECOND, auditScheduler.getScheduler());

                if(now.after(calendar.getTime())){
                    Audit audit = auditFactory.createFromAudit(auditScheduler.getAudit());
                    Optional<Act> actOpt = actRepository.findByAudit(auditScheduler.getAudit());
                    if(actOpt.isPresent()){
                        Act reference = actOpt.get();
                        Act scheduledAct = new Act();
                        scheduledAct.setDate(new Date());
                        scheduledAct.setAudit(audit);
                        scheduledAct.setProject(reference.getProject());
                        actRepository.save(scheduledAct);
                    }

                    LOGGER.info("[Audit {}] Launch scheduled audit based on audit {}", audit.getId(), auditScheduler.getAudit().getId());
                    auditRunnerService.runAudit(audit);
                    auditSchedulerService.modifyAuditScheduler(auditScheduler, auditScheduler.getScheduler(), new Date());
                }
            }
        }
    }
}
