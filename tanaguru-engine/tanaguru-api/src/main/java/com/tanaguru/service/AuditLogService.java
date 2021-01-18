package com.tanaguru.service;

import org.json.JSONObject;

import com.tanaguru.domain.entity.audit.Audit;

public interface AuditLogService {

    /**
     * Return a json object with audit log values
     * @param audit the given @see Audit
     * @return json object
     */
    JSONObject toJson(Audit audit);
    
}
