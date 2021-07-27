package com.tanaguru.auditscheduler;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditScheduler;
import com.tanaguru.domain.entity.membership.Act;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.factory.AuditFactory;
import com.tanaguru.repository.ActRepository;
import com.tanaguru.service.AuditRunnerService;
import com.tanaguru.service.AuditSchedulerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class AuditSchedulerLauncherServiceImplTest {
    @Mock
    AuditFactory auditFactory;

    @Mock
    ActRepository actRepository;

    @Mock
    AuditRunnerService auditRunnerService;

    @Mock
    AuditSchedulerService auditSchedulerService;

    @InjectMocks
    AuditSchedulerLauncherServiceImpl auditSchedulerLauncherService;

    @Test
    public void checkSchedules_1ScheduleNoAct(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -2);

        Audit base = new Audit();
        base.setId(0L);

        Audit scheduled = new Audit();
        scheduled.setId(1L);

        AuditScheduler auditScheduler = new AuditScheduler();
        auditScheduler.setId(0L);
        auditScheduler.setAudit(base);
        auditScheduler.setLastExecution(calendar.getTime());
        auditScheduler.setScheduler(86400);

        Map<Long, AuditScheduler> auditSchedulerMap = new HashMap<>();
        auditSchedulerMap.put(auditScheduler.getId(), auditScheduler);

        Mockito.when(auditSchedulerService.getSchedulersMap()).thenReturn(auditSchedulerMap);
        Mockito.when(auditFactory.createFromAudit(base)).thenReturn(scheduled);
        Mockito.when(actRepository.findByAudit(base)).thenReturn(Optional.empty());

        auditSchedulerLauncherService.checkSchedules();
        Mockito.verify(auditRunnerService, times(1)).runAudit(scheduled);
    }

    @Test
    public void checkSchedules_1ScheduleWithAct(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -2);

        Audit base = new Audit();
        base.setId(0L);

        Audit scheduled = new Audit();
        scheduled.setId(1L);

        AuditScheduler auditScheduler = new AuditScheduler();
        auditScheduler.setId(0L);
        auditScheduler.setAudit(base);
        auditScheduler.setLastExecution(calendar.getTime());
        auditScheduler.setScheduler(86400);

        Act act = new Act();
        act.setProject(new Project());

        Map<Long, AuditScheduler> auditSchedulerMap = new HashMap<>();
        auditSchedulerMap.put(auditScheduler.getId(), auditScheduler);

        Mockito.when(auditSchedulerService.getSchedulersMap()).thenReturn(auditSchedulerMap);
        Mockito.when(auditFactory.createFromAudit(base)).thenReturn(scheduled);
        Mockito.when(actRepository.findByAudit(base)).thenReturn(Optional.of(act));

        auditSchedulerLauncherService.checkSchedules();
        Mockito.verify(auditRunnerService, times(1)).runAudit(scheduled);
    }

    @Test
    public void checkSchedules_MultipleSchedules(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -2);

        Audit base = new Audit();
        base.setId(0L);

        Audit scheduled = new Audit();
        scheduled.setId(1L);

        AuditScheduler auditScheduler = new AuditScheduler();
        auditScheduler.setId(0L);
        auditScheduler.setAudit(base);
        auditScheduler.setLastExecution(calendar.getTime());
        auditScheduler.setScheduler(86400);

        AuditScheduler auditScheduler2 = new AuditScheduler();
        auditScheduler2.setId(1L);
        auditScheduler2.setAudit(base);
        auditScheduler2.setLastExecution(calendar.getTime());
        auditScheduler2.setScheduler(10000000);

        Map<Long, AuditScheduler> auditSchedulerMap = new HashMap<>();
        auditSchedulerMap.put(auditScheduler.getId(), auditScheduler);
        auditSchedulerMap.put(auditScheduler2.getId(), auditScheduler2);

        Mockito.when(auditSchedulerService.getSchedulersMap()).thenReturn(auditSchedulerMap);
        Mockito.when(auditFactory.createFromAudit(base)).thenReturn(scheduled);
        Mockito.when(actRepository.findByAudit(base)).thenReturn(Optional.empty());

        auditSchedulerLauncherService.checkSchedules();

        Mockito.verify(auditRunnerService, times(1)).runAudit(scheduled);
    }
}
