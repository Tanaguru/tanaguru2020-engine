package com.tanaguru.service;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.webextresult.WebextPageResult;

public interface ResultAnalyzerService {
    /**
     *
     * @param webextPageResult
     * @param audit
     * @param page
     */
    void extractWebextPageResult(WebextPageResult webextPageResult, Audit audit, Page page);

}
