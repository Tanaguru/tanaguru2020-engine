package com.tanaguru.service.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.dto.AuditSynthesisDTO;
import com.tanaguru.domain.dto.TestHierarchyResultDTO;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.pageresult.TestHierarchyResult;
import com.tanaguru.domain.entity.pageresult.TestResult;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.repository.TestHierarchyRepository;
import com.tanaguru.repository.TestHierarchyResultRepository;
import com.tanaguru.service.ExportCsvService;
import com.tanaguru.service.TestHierarchyResultService;

@Service
public class ExportCsvServiceImpl implements ExportCsvService{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportCsvServiceImpl.class);
    private final TestHierarchyRepository testHierarchyRepository;
    private final TestHierarchyResultRepository testHierarchyResultRepository;
    private final TestHierarchyResultService testHierarchyResultService;
    
    @Autowired
    public ExportCsvServiceImpl(TestHierarchyRepository testHierarchyRepository,
            TestHierarchyResultRepository testHierarchyResultRepository,
            TestHierarchyResultService testHierarchyResultService) {
        this.testHierarchyRepository = testHierarchyRepository;
        this.testHierarchyResultRepository = testHierarchyResultRepository;
        this.testHierarchyResultService = testHierarchyResultService;
    }

    /**
     * Write to csv file the synthesis of the audit results
     * @param writer output file writer
     * @param audit the audit
     * @param testHierarchyReference the test hierarchy reference
     * @param auditSynthesis the synthesis of the results
     */
    @Override
    public void writeAuditResultsToCsv(Writer writer, Audit audit, TestHierarchy testHierarchyReference,
            AuditSynthesisDTO auditSynthesis) {
        
        //resultat des tests sur l'ensemble des pages
        Map<String,String> testsStatusAllPages = testHierarchyResultService.getTestStatusByAuditAndTestHierarchy(audit, testHierarchyReference);
        
        String[] headers = { "test", "intitule", "nom page", "url", "statut", "details"};
        Collection<Page> pages = audit.getPages();
        Map<String, Map<Long, TestHierarchyResultDTO>> content = auditSynthesis.getContent();
        
        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.TDF.withHeader(headers))) { //TDF Format = separator TAB
            for(String testHierarchyCode : content.keySet()) {
                Optional<TestHierarchy> th = this.testHierarchyRepository.findByCodeAndReference(testHierarchyCode, testHierarchyReference);
                csvPrinter.printRecord(testHierarchyCode, th.get().getName(), "Ensemble des pages", "", this.getFrenchStatusName(testsStatusAllPages.get(testHierarchyCode)) ,"");
                for(Long pageId : content.get(testHierarchyCode).keySet()) {
                    TestHierarchyResultDTO thrDTO = content.get(testHierarchyCode).get(pageId);
                    Page page = this.getPageWithId(pages, pageId);
                    String details = this.getTestHierarchyResultDetails(thrDTO.id);
                    csvPrinter.printRecord(testHierarchyCode, th.get().getName(), page.getName(), page.getUrl(), this.getFrenchStatusName(thrDTO.status), details);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error While writing CSV ", e);
        }
    }
    
    /**
     * Return the page from the list from the id
     * @param pages list of page
     * @param pageId id to search
     * @return page
     */
    private Page getPageWithId(Collection<Page> pages, Long pageId) {
        Page p = null;
        for(Page page : pages) {
            if(page.getId() == pageId) {
                p = page;
            }
        }
        return p;
    }
    
    /**
     * Return the translation of the status name
     * @param status name in english
     * @return status name in french
     */
    private String getFrenchStatusName(String status) {
        String result = "";
        if(status.equals("untested")) {
            result = "Non testé";
        }else if(status.equals("inapplicable")) {
            result = "Non applicable";
        }else if(status.equals("passed")) {
            result = "Conforme";
        }else if(status.equals("cantTell")) {
            result = "A Qualifier";
        }else if(status.equals("failed")) {
            result = "Non conforme";
        }
        return result;
    }
    
    /**
     * Return the details of the test results
     * @param testHierarchyResultId
     * @return details
     */
    private String getTestHierarchyResultDetails(Long testHierarchyResultId) {
        TestHierarchyResult testHierarchyResult = testHierarchyResultRepository.findById(testHierarchyResultId)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_HIERARCHY_RESULT_NOT_FOUND, testHierarchyResultId ));

        Collection<TestResult> testResults = testHierarchyResult.getTestResults();
        String details = "";
        for(TestResult tr : testResults) {
            if(tr.getStatus().equals("passed") && tr.getNbElementPassed() > 0){
                details = details + "Conforme ("+tr.getNbElementPassed()+") "+tr.getTanaguruTest().getName()+"\n";
            }else if(tr.getStatus().equals("inapplicable") && tr.getNbElementUntested() > 0) {
                details = details + "Non applicable ("+tr.getNbElementUntested()+") "+tr.getTanaguruTest().getName()+"\n";     
            }else if(tr.getStatus().equals("failed") && tr.getNbElementFailed() > 0) {
                details = details + "Non conforme ("+tr.getNbElementFailed()+") "+tr.getTanaguruTest().getName()+"\n";
            }else if(tr.getStatus().equals("cantTell") && tr.getNbElementCantTell() > 0 ) {
                details = details + "A Qualifier ("+tr.getNbElementCantTell()+") "+tr.getTanaguruTest().getName()+"\n";
            }else if(tr.getStatus().equals("untested") && tr.getNbElementUntested() > 0) {
                details = details + "Non testé ("+tr.getNbElementUntested()+") "+tr.getTanaguruTest().getName()+"\n";
            }
        }
        return details;
    }

}
