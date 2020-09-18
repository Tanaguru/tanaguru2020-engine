package com.tanaguru.service;

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
}
