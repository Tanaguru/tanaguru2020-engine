package factory.impl;

import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.parameter.AuditParameter;
import com.tanaguru.domain.entity.audit.parameter.AuditParameterValue;
import com.tanaguru.factory.impl.AuditFactoryImpl;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.service.AuditParameterService;
import com.tanaguru.service.AuditService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

@RunWith(MockitoJUnitRunner.class)
public class AuditFactoryImplTest {
    @Mock
    private AuditRepository auditRepository;

    @Mock
    private AuditParameterService auditParameterService;

    @Mock
    private AuditService auditService;


    @InjectMocks
    private AuditFactoryImpl auditFactory;

    @Test
    public void createAuditTest_NullParameterMap() {
        Mockito.when(auditParameterService.getDefaultParameterForAuditType(EAuditType.SITE)).thenReturn(new HashMap<>());
        Mockito.when(auditRepository.save(Mockito.any(Audit.class))).thenReturn(new Audit());
        auditFactory.createAudit("", null, EAuditType.SITE, true, null, new ArrayList<>(Collections.EMPTY_LIST), null);
    }

    @Test
    public void createAuditTest_NotNullParameterMap() {
        Mockito.when(auditRepository.save(Mockito.any(Audit.class))).thenReturn(new Audit());
        Mockito.when(auditParameterService.getParameterMapForAuditTypeWithParameterOverride(Mockito.any(EAuditType.class), Mockito.any(HashMap.class), Mockito.any()))
                .thenReturn(new HashMap<AuditParameter, AuditParameterValue>());

        auditFactory.createAudit("", new HashMap<>(), EAuditType.SITE, true, null, new ArrayList<>(Collections.EMPTY_LIST), null);
        Mockito.verify(auditParameterService, Mockito.times(1)).getParameterMapForAuditTypeWithParameterOverride(Mockito.any(EAuditType.class), Mockito.any(HashMap.class), Mockito.any());
    }
}
