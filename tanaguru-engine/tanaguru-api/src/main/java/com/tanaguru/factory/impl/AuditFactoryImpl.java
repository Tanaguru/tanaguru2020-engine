package com.tanaguru.factory.impl;

import com.tanaguru.domain.constant.EAuditLogLevel;
import com.tanaguru.domain.constant.EAuditParameter;
import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditReference;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.audit.parameter.AuditAuditParameterValue;
import com.tanaguru.domain.entity.audit.parameter.AuditParameter;
import com.tanaguru.domain.entity.audit.parameter.AuditParameterValue;
import com.tanaguru.domain.entity.membership.Act;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.factory.AuditFactory;
import com.tanaguru.repository.ActRepository;
import com.tanaguru.repository.AuditAuditParameterValueRepository;
import com.tanaguru.repository.AuditReferenceRepository;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.service.AuditParameterService;
import com.tanaguru.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

@Component
@Transactional
public class AuditFactoryImpl implements AuditFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditFactoryImpl.class);

    private final AuditRepository auditRepository;
    private final AuditParameterService auditParameterService;
    private final AuditAuditParameterValueRepository auditAuditParameterValueRepository;
    private final AuditService auditService;
    private final AuditReferenceRepository auditReferenceRepository;
    private final ActRepository actRepository;

    public AuditFactoryImpl(
            AuditRepository auditRepository,
            AuditParameterService auditParameterService,
            AuditAuditParameterValueRepository auditAuditParameterValueRepository,
            AuditService auditService, AuditReferenceRepository auditReferenceRepository, ActRepository actRepository) {
        this.auditRepository = auditRepository;
        this.auditParameterService = auditParameterService;
        this.auditAuditParameterValueRepository = auditAuditParameterValueRepository;
        this.auditService = auditService;
        this.auditReferenceRepository = auditReferenceRepository;
        this.actRepository = actRepository;
    }

    public Audit createAudit(
            String name,
            Map<EAuditParameter, String> auditParameters,
            EAuditType auditType,
            boolean isPrivate,
            Project project,
            ArrayList<TestHierarchy> references,
            TestHierarchy main) {
        Map<AuditParameter, AuditParameterValue> definiteParameterMap =
                auditParameters != null ?
                        auditParameterService.getParameterMapForAuditTypeWithParameterOverride(auditType, auditParameters, project) :
                        auditParameterService.getDefaultParameterForAuditType(auditType);

        Audit audit = new Audit();
        audit.setName(name);
        audit.setShareCode(String.valueOf(new Date().hashCode()));
        audit.setPrivate(isPrivate);
        audit.setType(auditType);
        audit = auditRepository.save(audit);

        Collection<AuditReference> auditReferences = new ArrayList<>();
        for (TestHierarchy reference : references) {
            AuditReference auditReference = new AuditReference();
            auditReference.setAudit(audit);
            auditReference.setTestHierarchy(reference);
            auditReference.setMain(reference.getId() == main.getId());
            auditReferences.add(auditReferenceRepository.save(auditReference));
        }
        audit.setAuditReferences(auditReferences);

        Collection<AuditAuditParameterValue> auditAuditParameterValues = new ArrayList<>();
        for(AuditParameterValue auditParameterValue : definiteParameterMap.values()){
            AuditAuditParameterValue auditAuditParameterValue = new AuditAuditParameterValue();
            auditAuditParameterValue.setAudit(audit);
            auditAuditParameterValue.setAuditParameterValue(auditParameterValue);
            auditAuditParameterValues.add(auditAuditParameterValueRepository.save(auditAuditParameterValue));
        }

        audit.setParameters(auditAuditParameterValues);

        if(project != null){
            Act act = new Act();
            act.setDate(new Date());
            act.setAudit(audit);
            act.setProject(project);
            actRepository.save(act);
        }

        auditService.log(audit, EAuditLogLevel.INFO, "Audit created for scope " + auditType);
        LOGGER.info("[Audit {}] Created for scope [{}]", audit.getId(), auditType);
        return audit;
    }

    public Audit createFromAudit(Audit from){
        Audit audit = new Audit();
        audit.setName(from.getName() + " - " + new Date().toString());
        audit.setType(from.getType());
        audit.setPrivate(from.isPrivate());
        audit = auditRepository.save(audit);

        Collection<AuditAuditParameterValue> auditAuditParameterValues = new ArrayList<>();
        for(AuditAuditParameterValue auditAuditParameterValueFrom : from.getParameters()){
            AuditAuditParameterValue auditAuditParameterValue = new AuditAuditParameterValue();
            auditAuditParameterValue.setAudit(from);
            auditAuditParameterValue.setAuditParameterValue(auditAuditParameterValueFrom.getAuditParameterValue());
            auditAuditParameterValues.add(auditAuditParameterValueRepository.save(auditAuditParameterValue));
        }

        audit.setParameters(auditAuditParameterValues);

        LOGGER.info("[Audit {}] Created for scope [{}] from audit {}", audit.getId(), audit.getType(), from.getId());
        return audit;
    }
}
