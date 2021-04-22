package com.tanaguru.domain.entity.pageresult;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TestHierarchy;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author rcharre
 */
@Table(name = "status_result")
@Entity
public class StatusResult implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne
    private TestHierarchy reference;

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne
    private Page page;

    @Column
    private int nbTestFailed = 0;

    @Column
    private int nbTestPassed = 0;

    @Column
    private int nbTestInapplicable = 0;

    @Column
    private int nbTestCantTell = 0;

    @Column
    private int nbTestUntested = 0;

    @Column
    private int nbElementFailed = 0;

    @Column
    private int nbElementPassed = 0;

    @Column
    private int nbElementCantTell = 0;

    @Column
    private int nbElementTested = 0;

    @Column
    private int nbElementUntested = 0;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TestHierarchy getReference() {
        return reference;
    }

    public void setReference(TestHierarchy reference) {
        this.reference = reference;
    }

    public int getNbElementFailed() {
        return nbElementFailed;
    }

    public void setNbElementFailed(int nbElementFailed) {
        this.nbElementFailed = nbElementFailed;
    }

    public int getNbElementPassed() {
        return nbElementPassed;
    }

    public void setNbElementPassed(int nbElementPassed) {
        this.nbElementPassed = nbElementPassed;
    }

    public int getNbElementCantTell() {
        return nbElementCantTell;
    }

    public void setNbElementCantTell(int nbElementCantTell) {
        this.nbElementCantTell = nbElementCantTell;
    }

    public int getNbElementTested() {
        return nbElementTested;
    }

    public void setNbElementTested(int nbElementTested) {
        this.nbElementTested = nbElementTested;
    }

    public int getNbTestFailed() {
        return nbTestFailed;
    }

    public void setNbTestFailed(int nbTestFailed) {
        this.nbTestFailed = nbTestFailed;
    }


    public int getNbTestInapplicable() {
        return nbTestInapplicable;
    }

    public void setNbTestInapplicable(int nbTestInapplicable) {
        this.nbTestInapplicable = nbTestInapplicable;
    }

    public int getNbTestCantTell() {
        return nbTestCantTell;
    }

    public void setNbTestCantTell(int nbTestCantTell) {
        this.nbTestCantTell = nbTestCantTell;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public int getNbTestPassed() {
        return nbTestPassed;
    }

    public void setNbTestPassed(int nbTestPassed) {
        this.nbTestPassed = nbTestPassed;
    }

    public int getNbTestUntested() {
        return nbTestUntested;
    }

    public void setNbTestUntested(int nbTestUntested) {
        this.nbTestUntested = nbTestUntested;
    }

    public int getNbElementUntested() {
        return nbElementUntested;
    }

    public void setNbElementUntested(int nbElementUntested) {
        this.nbElementUntested = nbElementUntested;
    }
}