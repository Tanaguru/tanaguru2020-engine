package service.impl;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.repository.ActRepository;
import com.tanaguru.repository.AuditLogRepository;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.service.ProjectService;
import com.tanaguru.service.impl.AuditServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuditServiceImplTest {
    @Mock
    private AuditRepository auditRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ActRepository actRepository;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private AuditServiceImpl auditService;

    @Test
    public void userCanShowAudit_public() {
        Audit audit = new Audit();
        audit.setPrivate(false);
        Assert.assertTrue(auditService.canShowAudit(audit, null));
    }

    @Test
    public void userCanShowAudit_validSharecode() {
        Audit audit = new Audit();
        audit.setPrivate(true);
        audit.setShareCode("test");
        Assert.assertTrue(auditService.canShowAudit(audit, "test"));
    }

    @Test
    public void userCanShowAudit_invalidSharecode() {
        Audit audit = new Audit();
        audit.setPrivate(true);
        audit.setShareCode("test");

        Assert.assertFalse(auditService.canShowAudit(audit, "test2"));
    }
}