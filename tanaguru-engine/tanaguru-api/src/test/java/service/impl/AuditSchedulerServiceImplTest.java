package service.impl;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditScheduler;
import com.tanaguru.domain.exception.InvalidEntityException;
import com.tanaguru.repository.AuditSchedulerRepository;
import com.tanaguru.service.impl.AuditSchedulerServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class AuditSchedulerServiceImplTest {

    @Mock
    AuditSchedulerRepository auditSchedulerRepository;

    @InjectMocks
    AuditSchedulerServiceImpl auditSchedulerService;

    @Test
    public void setupTest_Empty(){
        Mockito.when(auditSchedulerRepository.findAll()).thenReturn(Collections.EMPTY_LIST);
        auditSchedulerService.setup();

        Assert.assertEquals(0, auditSchedulerService.getSchedulersMap().size());
    }

    @Test
    public void setupTest_WithElement(){
        AuditScheduler auditScheduler1 = new AuditScheduler();
        auditScheduler1.setId(1L);
        AuditScheduler auditScheduler2 = new AuditScheduler();
        auditScheduler2.setId(2L);
        List<AuditScheduler> all = Arrays.asList(auditScheduler1, auditScheduler2);

        Mockito.when(auditSchedulerRepository.findAll()).thenReturn(all);
        auditSchedulerService.setup();

        Assert.assertEquals(2, auditSchedulerService.getSchedulersMap().size());
    }

    @Test(expected = InvalidEntityException.class)
    public void createAuditSchedulerTest_InvalidTime(){
        auditSchedulerService.createAuditScheduler(new Audit(), 0);
    }

    @Test(expected = InvalidEntityException.class)
    public void createAuditSchedulerTest_AuditAlreadyHasSchedule(){
        Mockito.when(auditSchedulerRepository.findByAudit(any(Audit.class))).thenReturn(Optional.of(new AuditScheduler()));
        auditSchedulerService.createAuditScheduler(new Audit(), 86400);
    }

    @Test
    public void createAuditSchedulerTest_Valid(){
        Mockito.when(auditSchedulerRepository.findByAudit(any(Audit.class))).thenReturn(Optional.empty());
        Mockito.when(auditSchedulerRepository.save(any(AuditScheduler.class))).thenReturn(new AuditScheduler());

        AuditScheduler auditScheduler = auditSchedulerService.createAuditScheduler(new Audit(), 86400);
        Assert.assertNotNull(auditScheduler);
        Assert.assertFalse(auditSchedulerService.getSchedulersMap().isEmpty());
    }

    @Test(expected = InvalidEntityException.class)
    public void modifyAuditSchedulerTest_InvalidTime(){
        AuditScheduler auditScheduler = new AuditScheduler();
        auditScheduler.setScheduler(0);
        auditSchedulerService.modifyAuditScheduler(auditScheduler, 0, new Date());
    }

    @Test
    public void modifyAuditSchedulerTest_Valid(){
        Audit toReference = new Audit();

        AuditScheduler to = new AuditScheduler();
        to.setId(0L);
        to.setScheduler(86400);
        to.setLastExecution(new Date());
        to.setAudit(toReference);

        AuditScheduler existing = new AuditScheduler();
        existing.setId(0L);
        existing.setScheduler(90000);
        existing.setAudit(toReference);
        existing.setLastExecution(new Date());

        auditSchedulerService.modifyAuditScheduler(to, 86400, new Date());
    }
}
