package com.tanaguru.service;

import org.json.JSONObject;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;

public interface PageService {
    /**
     *
     * @param page The page to delete
     */
    void deletePage(Page page);

    /**
     * Delete all @see Page for a given @see Audit
     * @param audit The given @see Audit
     */
    void deletePageByAudit(Audit audit);
    
    /**
     * Return a json object with the information of the page
     * @param page The given @see Page
     * @return json object
     */
    JSONObject toJson(Page page);
    
    /**
     * Return a json object with the information of the page and of the audit
     * @param page The given @see Page
     * @return json object
     */
    JSONObject toJsonWithAuditInfo(Page page);
}
