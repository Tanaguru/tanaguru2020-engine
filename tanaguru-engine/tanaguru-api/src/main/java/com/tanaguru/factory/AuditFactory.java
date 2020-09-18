package com.tanaguru.factory;

import com.tanaguru.domain.constant.EAuditParameter;
import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.membership.project.Project;
import org.aspectj.weaver.ast.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public interface AuditFactory {
    /**
     * Create an audit entity
     *
     * @param name The name of the @see Audit
     * @param auditParameters The audit parameters AuditParameter, AuditParameterValue
     * @param auditType       The audit type auditType
     * @param isPrivate       The private tag of the audit
     * @param project         The Project of the audit
     * @param references      The audit references
     * @param main             The main reference
     * @return The saved audit
     */
    Audit createAudit(
            String name,
            Map<EAuditParameter, String> auditParameters,
            EAuditType auditType,
            boolean isPrivate,
            Project project,
            ArrayList<TestHierarchy> references,
            TestHierarchy main);

    /**
     * Create an empty @see Audit from another
     * @param from The source @see Audit
     * @return the new @see Audit
     */
    Audit createFromAudit(Audit from);
}
