package com.tanaguru.service;

import com.tanaguru.domain.entity.audit.Audit;
import org.json.JSONObject;

public interface AuditActService {
    
    /**
     * Return a json object with audit act values
     * @param audit the given @see Audit
     * @return json object
     */
    JSONObject toJson(Audit audit);
}
