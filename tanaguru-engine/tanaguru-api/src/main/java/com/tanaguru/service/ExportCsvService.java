package com.tanaguru.service;

import java.io.Writer;

import com.tanaguru.domain.dto.AuditSynthesisDTO;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.TestHierarchy;

public interface ExportCsvService {

    /**
     * Write to csv file the synthesis of the audit results
     * @param writer output file writer
     * @param audit the audit
     * @param testHierarchyReference the test hierarchy reference
     * @param auditSynthesis the synthesis of the results
     */
    void writeAuditResultsToCsv(Writer writer, Audit audit, TestHierarchy testHierarchy, AuditSynthesisDTO auditSynthesisDTO);
    
}
