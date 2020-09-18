package com.tanaguru.domain.entity.pageresult;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TestHierarchy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

/**
 * @author rcharre
 */
@Table(name = "test_hierarchy_result")
@Entity
public class TestHierarchyResult implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonIgnore
    @ManyToOne
    private TestHierarchyResult parent;

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne
    private TestHierarchy testHierarchy;

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne
    private Page page;

    @Column
    private int nbFailed = 0;

    @Column
    private int nbPassed = 0;

    @Column
    private int nbInapplicable = 0;

    @Column
    private int nbUntested = 0;

    @Column
    private int nbCantTell = 0;

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
    private int nbElementUntested = 0;

    @Column
    private int nbElementTested = 0;

    @Column
    private String status = "untested";

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @ManyToMany(targetEntity = TestResult.class, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "test_hierarchy_result_test_result",
            joinColumns = {@JoinColumn(name = "test_hierarchy_result_id")},
            inverseJoinColumns = @JoinColumn(name = "test_result_id"))
    private Collection<TestResult> testResults;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    private Collection<TestHierarchyResult> children;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TestHierarchyResult getParent() {
        return parent;
    }

    public void setParent(TestHierarchyResult parent) {
        this.parent = parent;
    }


    public TestHierarchy getTestHierarchy() {
        return testHierarchy;
    }

    public void setTestHierarchy(TestHierarchy testHierarchy) {
        this.testHierarchy = testHierarchy;
    }

    public Collection<TestResult> getTestResults() {
        return testResults;
    }

    public void setTestResults(Collection<TestResult> testResults) {
        this.testResults = testResults;
    }

    public Collection<TestHierarchyResult> getChildren() {
        return children;
    }

    public void setChildren(Collection<TestHierarchyResult> children) {
        this.children = children;
    }

    public void addTestResult(TestResult testResult){
        this.testResults.add(testResult);
    }

    public int getNbFailed() {
        return nbFailed;
    }

    public void setNbFailed(int nbFailed) {
        this.nbFailed = nbFailed;
    }

    public int getNbInapplicable() {
        return nbInapplicable;
    }

    public void setNbInapplicable(int nbInapplicable) {
        this.nbInapplicable = nbInapplicable;
    }

    public int getNbUntested() {
        return nbUntested;
    }

    public void setNbUntested(int nbUntested) {
        this.nbUntested = nbUntested;
    }

    public int getNbCantTell() {
        return nbCantTell;
    }

    public void setNbCantTell(int nbCantTell) {
        this.nbCantTell = nbCantTell;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public int getNbPassed() {
        return nbPassed;
    }

    public void setNbPassed(int nbPassed) {
        this.nbPassed = nbPassed;
    }

    public int getNbTestPassed() {
        return nbTestPassed;
    }

    public void setNbTestPassed(int nbTestPassed) {
        this.nbTestPassed = nbTestPassed;
    }

    public int getNbElementUntested() {
        return nbElementUntested;
    }

    public void setNbElementUntested(int nbElementUntested) {
        this.nbElementUntested = nbElementUntested;
    }

    public int getNbTestUntested() {
        return nbTestUntested;
    }

    public void setNbTestUntested(int nbTestUntested) {
        this.nbTestUntested = nbTestUntested;
    }
}