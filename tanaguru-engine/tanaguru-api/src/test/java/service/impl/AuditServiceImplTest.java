package service.impl;

import com.tanaguru.domain.constant.AppAuthorityName;
import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.constant.ProjectAuthorityName;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.membership.Act;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.domain.entity.membership.user.AppAuthority;
import com.tanaguru.domain.entity.membership.user.AppRole;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.repository.ActRepository;
import com.tanaguru.repository.AppRoleRepository;
import com.tanaguru.repository.AuditLogRepository;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.service.ProjectService;
import com.tanaguru.service.impl.AppRoleServiceImpl;
import com.tanaguru.service.impl.AuditServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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