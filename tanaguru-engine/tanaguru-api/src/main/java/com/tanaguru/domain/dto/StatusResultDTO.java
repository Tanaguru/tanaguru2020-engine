package com.tanaguru.domain.dto;

import com.tanaguru.domain.entity.pageresult.StatusResult;

public class StatusResultDTO {
    public long id;
    public long reference;
    public long page;
    public int nbTF = 0;
    public int nbTP = 0;
    public int nbTI = 0;
    public int nbTCT = 0;
    public int nbTU = 0;
    public int nbEF = 0;
    public int nbEP = 0;
    public int nbECT = 0;
    public int nbET = 0;
    public int nbEU = 0;

    public StatusResultDTO(){}
    public StatusResultDTO(StatusResult statusResult){
        this.id = statusResult.getId();
        this.reference = statusResult.getReference().getId();
        this.page = statusResult.getPage().getId();
        this.nbECT = statusResult.getNbElementCantTell();
        this.nbEF = statusResult.getNbElementFailed();
        this.nbEP = statusResult.getNbElementPassed();
        this.nbTCT = statusResult.getNbTestCantTell();
        this.nbTF = statusResult.getNbTestFailed();
        this.nbTI = statusResult.getNbTestInapplicable();
        this.nbTP = statusResult.getNbTestPassed();
        this.nbET = statusResult.getNbElementTested();
        this.nbTU = statusResult.getNbTestUntested();
        this.nbEU = statusResult.getNbElementUntested();
    }
}
