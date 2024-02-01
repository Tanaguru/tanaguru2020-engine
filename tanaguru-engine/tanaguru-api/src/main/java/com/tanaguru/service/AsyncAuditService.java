package com.tanaguru.service;

import com.tanaguru.domain.entity.audit.Audit;

public interface AsyncAuditService {
	void updatePurgeProperties(String status, int total);
    void deleteAudit(Audit audit);
}
