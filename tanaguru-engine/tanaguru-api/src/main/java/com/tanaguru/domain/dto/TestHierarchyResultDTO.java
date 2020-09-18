package com.tanaguru.domain.dto;

import com.tanaguru.domain.entity.pageresult.TestHierarchyResult;

public class TestHierarchyResultDTO {
    public long id;

    public int nbF = 0;

    public int nbP = 0;

    public int nbI = 0;

    public int nbU = 0;

    public int nbCT = 0;

    public int nbTF = 0;

    public int nbTP = 0;

    public int nbTI = 0;

    public int nbTCT = 0;

    public int nbECT = 0;

    public int nbEF = 0;

    public int nbEP = 0;

    public int nbET = 0;

    public String status = "untested";

    public TestHierarchyDTO testHierarchy = null;

    public TestHierarchyResultDTO(){}
    public TestHierarchyResultDTO(TestHierarchyResult testHierarchyResult) {
        this.id = testHierarchyResult.getId();
        this.nbF = testHierarchyResult.getNbFailed();
        this.nbP = testHierarchyResult.getNbPassed();
        this.nbI = testHierarchyResult.getNbInapplicable();
        this.nbU = testHierarchyResult.getNbUntested();
        this.nbCT = testHierarchyResult.getNbCantTell();
        this.nbECT = testHierarchyResult.getNbElementCantTell();
        this.nbEF = testHierarchyResult.getNbElementFailed();
        this.nbEP = testHierarchyResult.getNbElementPassed();

        this.nbTCT = testHierarchyResult.getNbTestCantTell();
        this.nbTF = testHierarchyResult.getNbTestFailed();
        this.nbTI = testHierarchyResult.getNbTestInapplicable();
        this.nbTP = testHierarchyResult.getNbTestPassed();

        this.nbET = testHierarchyResult.getNbElementTested();
        this.status = testHierarchyResult.getStatus();
        this.testHierarchy = new TestHierarchyDTO(testHierarchyResult.getTestHierarchy());
    }

}
