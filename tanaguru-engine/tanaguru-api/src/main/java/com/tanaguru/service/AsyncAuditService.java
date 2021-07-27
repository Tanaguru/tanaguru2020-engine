package com.tanaguru.service;

import com.tanaguru.domain.entity.audit.Audit;

public interface AsyncAuditService {
    void deleteAudit(Audit audit);
}
