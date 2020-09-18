package com.tanaguru.service;

import com.tanaguru.domain.entity.audit.Audit;

/**
 * @author rcharre
 */
public interface AuditRunnerService {
    /**
     * Start an audit
     *
     * @param audit
     */
    void runAudit(Audit audit);
}
