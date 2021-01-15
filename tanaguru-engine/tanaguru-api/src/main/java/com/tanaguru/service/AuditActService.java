package com.tanaguru.service;

import org.json.JSONObject;

import com.tanaguru.domain.entity.audit.Audit;

public interface AuditActService {
    
    /**
     * Return a json object with audit act values
     * @param audit the given @see Audit
     * @return json object
     */
    JSONObject toJson(Audit audit);
}
