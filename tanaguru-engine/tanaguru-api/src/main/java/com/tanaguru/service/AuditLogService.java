package com.tanaguru.service;

import com.tanaguru.domain.entity.audit.Audit;
import org.json.JSONObject;

public interface AuditLogService {

    /**
     * Return a json object with audit log values
     * @param audit the given @see Audit
     * @return json object
     */
    JSONObject toJson(Audit audit);
    
}
