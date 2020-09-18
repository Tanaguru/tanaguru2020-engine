package com.tanaguru.service;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.pageresult.TestHierarchyResult;
import com.tanaguru.domain.entity.pageresult.TestResult;
import com.tanaguru.webextresult.WebextPageResult;

import java.util.Collection;
import java.util.HashMap;

public interface ResultAnalyzerService {
    /**
     *
     * @param webextPageResult
     * @param audit
     * @param page
     */
    void extractWebextPageResult(WebextPageResult webextPageResult, Audit audit, Page page);

}
