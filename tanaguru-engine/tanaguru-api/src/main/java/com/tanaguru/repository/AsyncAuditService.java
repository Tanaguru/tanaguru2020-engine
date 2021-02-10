package com.tanaguru.repository;

import com.tanaguru.domain.entity.audit.Audit;

public interface AsyncAuditService {
    void deleteAudit(Audit audit);

}
